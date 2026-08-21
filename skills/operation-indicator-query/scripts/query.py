#!/usr/bin/env python3
"""Query operation indicators through platform-governed Binding metadata."""

from __future__ import annotations

import argparse
import base64
import json
import os
import re
import shutil
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Any

SKILL_DIR = Path(__file__).resolve().parents[1]
SCRIPT_DIR = Path(__file__).resolve().parent


class QueryError(RuntimeError):
    def __init__(self, code: str, message: str, candidates: list[dict[str, Any]] | None = None):
        super().__init__(message)
        self.code = code
        self.candidates = candidates or []


def config_dir() -> Path:
    #返回配置目录
    local = os.environ.get("LOCALAPPDATA")
    if not local:
        raise QueryError("CONFIG_MISSING", "LOCALAPPDATA 未设置")
    return Path(local) / "brrp-codex" / "operation-indicator-query"


def load_config() -> dict[str, Any]:
    #读取config.json
    path = config_dir() / "config.json"
    if not path.exists():
        raise QueryError("CONFIG_MISSING", f"未找到配置：{path}，请先运行 configure.ps1")
    with path.open("r", encoding="utf-8-sig") as handle:
        return json.load(handle)


def read_dpapi_credential(path: Path) -> tuple[str, str]:
    #读取DPAPI凭据
    if not path.exists():
        raise QueryError("CREDENTIAL_MISSING", f"未找到凭据：{path}")
    shell = shutil.which("pwsh") or shutil.which("powershell")
    if not shell:
        raise QueryError("POWERSHELL_MISSING", "无法读取 Windows DPAPI 凭据")
    command = (
        "& { param($p) $c=Import-Clixml -LiteralPath $p; "
        "[Console]::OutputEncoding=[Text.Encoding]::UTF8; "
        "Write-Output $c.UserName; Write-Output $c.GetNetworkCredential().Password }"
    )
    result = subprocess.run(
        [shell, "-NoProfile", "-NonInteractive", "-Command", command, str(path)],
        capture_output=True, text=True, encoding="utf-8", check=False,
    )
    if result.returncode != 0:
        raise QueryError(
            "CREDENTIAL_REQUIRES_ELEVATION",
            "当前 Codex 沙箱身份无法解密桌面用户的 CurrentUser DPAPI 凭据；请授权提升后重新运行同一查询",
        )
    lines = result.stdout.splitlines()
    if len(lines) < 2:
        raise QueryError(
            "CREDENTIAL_REQUIRES_ELEVATION",
            "当前 Codex 沙箱身份无法读取桌面用户的 CurrentUser DPAPI 凭据；请授权提升后重新运行同一查询",
        )
    return lines[0], lines[1]


class PlatformClient:
    def __init__(self, base_url: str, api_key: str, timeout: int = 5):
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout = timeout

    def get(self, path: str, params: dict[str, Any] | None = None) -> Any:
        # 统一 GET
        query = urllib.parse.urlencode({k: v for k, v in (params or {}).items() if v is not None})
        url = self.base_url + path + (("?" + query) if query else "")
        request = urllib.request.Request(url, headers={"X-API-Key": self.api_key, "Accept": "application/json"})
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                payload = json.load(response)
        except urllib.error.HTTPError as exc:
            try:
                detail = json.loads(exc.read().decode("utf-8")).get("message", str(exc))
            except Exception:
                detail = str(exc)
            raise QueryError("PLATFORM_HTTP_ERROR", f"平台 API 返回 {exc.code}：{detail}") from exc
        except OSError as exc:
            raise QueryError("PLATFORM_UNAVAILABLE", f"无法连接平台：{exc}") from exc
        if payload.get("code") != "SUCCESS":
            raise QueryError("PLATFORM_API_ERROR", payload.get("message", "平台 API 调用失败"))
        return payload.get("data")

    def binding(self, name: str) -> dict[str, Any]:
        # 获取启用的 Binding 元数据
        page = self.get("/api/v1/bindings", {"keyword": name, "status": "ENABLED", "page": 1, "size": 100})
        matches = [item for item in page.get("items", []) if item.get("name") == name]
        if len(matches) != 1:
            raise QueryError("BINDING_NOT_READY", f"启用 Binding 应唯一存在：{name}")
        return self.get(f"/api/v1/bindings/{matches[0]['id']}")


IDENTIFIER = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


def quote_identifier(value: str) -> str:
    if not IDENTIFIER.fullmatch(value or ""):
        raise QueryError("UNSAFE_BINDING", f"Binding 中包含不安全标识符：{value}")
    return f"`{value}`"


@dataclass(frozen=True)
class BoundTable:
    name: str
    columns: dict[str, str]

    @classmethod
    def from_binding(cls, binding: dict[str, Any]) -> "BoundTable":
        #从binding元数据提取真实表名和映射
        table = binding.get("tableName")
        quote_identifier(table)
        columns = {m["propertyCode"]: m["sourceColumn"] for m in binding.get("mappings", [])}
        for column in columns.values():
            quote_identifier(column)
        return cls(table, columns)

    def require(self, *codes: str) -> None:
        #要求检验映射是否完整
        missing = [code for code in codes if code not in self.columns]
        if missing:
            raise QueryError("BINDING_INCOMPLETE", f"Binding 缺少属性映射：{', '.join(missing)}")

    def col(self, code: str) -> str:
        #获取映射列名
        self.require(code)
        return quote_identifier(self.columns[code])


def decimal_json(value: Any) -> Any:
    return value


class JdbcCursor:
    # JDBC 查询游标
    def __init__(self, connection: "JdbcConnection"):
        self.connection = connection
        self.rows: list[dict[str, Any]] = []

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, traceback):
        return False

    def execute(self, sql: str, parameters: tuple[Any, ...] = ()) -> int:
        jdbc_sql = sql.replace("%s", "?")
        if jdbc_sql.count("?") != len(parameters):
            raise QueryError("DATABASE_QUERY_FAILED", "SQL 占位符数量与参数数量不一致")

        def encoded(value: Any) -> str:
            return base64.b64encode(str(value).encode("utf-8")).decode("ascii")
        payload = "\n".join([encoded(jdbc_sql), str(len(parameters)), *(encoded(value) for value in parameters)]) + "\n"
        env = os.environ.copy()
        env.update({
            # 数据库连接信息通过环境变量传递
            "BRRP_SKILL_DB_URL": self.connection.url,
            "BRRP_SKILL_DB_USER": self.connection.user,
            "BRRP_SKILL_DB_PASSWORD": self.connection.password,
            "BRRP_SKILL_QUERY_TIMEOUT": str(self.connection.timeout),
        })
        classpath = os.pathsep.join([str(SCRIPT_DIR / "lib" / "jdbc-bridge"), str(SCRIPT_DIR / "lib" / "mysql-connector-j.jar")])
        process = subprocess.run(
            [self.connection.java, "-cp", classpath, "JdbcQuery"], input=payload,
            capture_output=True, text=True, encoding="utf-8", env=env, check=False,
        )
        if process.returncode != 0:
            raise QueryError("DATABASE_QUERY_FAILED", process.stderr.splitlines()[-1] if process.stderr else "数据库查询失败")
        lines = process.stdout.splitlines()
        if len(lines) < 2:
            raise QueryError("DATABASE_QUERY_FAILED", "JDBC 查询返回格式无效")
        count = int(lines[0]); headers = [base64.b64decode(v).decode("utf-8") for v in lines[1].split("\t")]
        if len(headers) != count:
            raise QueryError("DATABASE_QUERY_FAILED", "JDBC 查询列信息无效")
        self.rows = []
        for line in lines[2:]:
            values = line.split("\t")
            decoded = [None if value == "-" else base64.b64decode(value).decode("utf-8") for value in values]
            self.rows.append(dict(zip(headers, decoded)))
        return len(self.rows)

    def fetchall(self) -> list[dict[str, Any]]:
        return list(self.rows)

    def fetchone(self) -> dict[str, Any] | None:
        return self.rows[0] if self.rows else None


class JdbcConnection:
    # JDBC 连接
    def __init__(self, config: dict[str, Any], user: str, password: str):
        self.url = (f"jdbc:mysql://{config['db_host']}:{int(config['db_port'])}/{config['db_name']}"
                    "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true")
        self.user = user; self.password = password
        self.timeout = int(config.get("query_timeout_seconds", 10))
        self.java = shutil.which("java") or "java"

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, traceback):
        return False

    def cursor(self) -> JdbcCursor:
        return JdbcCursor(self)


class OperationQueryService:
    # 操作查询服务
    def __init__(self):
        self.config = load_config()
        _, api_key = read_dpapi_credential(config_dir() / "platform-api-key.xml")
        db_user, db_password = read_dpapi_credential(config_dir() / "business-db.xml")
        self.platform = PlatformClient(
            self.config["platform_base_url"], api_key, int(self.config.get("connect_timeout_seconds", 5))
        )
        self.db_user = db_user
        self.db_password = db_password
        self._bindings: dict[str, BoundTable] = {}

    # 连接数据库
    def connect(self):
        return JdbcConnection(self.config, self.db_user, self.db_password)

    # 获取绑定表结构
    def bound(self, key: str) -> BoundTable:
        if key not in self._bindings:
            self._bindings[key] = BoundTable.from_binding(self.platform.binding(self.config[key]))
        return self._bindings[key]

    def _aliases(self) -> dict[str, str]:
        #可读取设置的映射
        path = SKILL_DIR / "references" / "indicator_aliases.json"
        with path.open("r", encoding="utf-8") as handle:
            return {str(k).casefold(): str(v) for k, v in json.load(handle).items()}

    def resolve_object(self, connection, term: str) -> dict[str, Any]:
        #企业名解析
        obs = self.bound("observation_binding")
        obs.require("OBJECT_ID", "OBJECT_CODE", "OBJECT_NAME")
        fields = [obs.col("OBJECT_ID"), obs.col("OBJECT_CODE"), obs.col("OBJECT_NAME")]
        #依次匹配OBJECT_ID/OBJECT_CODE/PBJECT_NAME
        sql = (f"SELECT DISTINCT {fields[0]} object_id,{fields[1]} object_code,{fields[2]} object_name "
               f"FROM {quote_identifier(obs.name)} WHERE {fields[0]}=%s OR {fields[1]}=%s OR {fields[2]}=%s LIMIT 11")
        with connection.cursor() as cursor:
            cursor.execute(sql, (term, term, term)); rows = cursor.fetchall()
        if len(rows) == 1:
            return rows[0]
        if not rows:
            org = self.bound("organization_binding")
            if "OBJECT_ID" in org.columns:
                lookup_codes = [code for code in ("OBJECT_ID", "OBJECT_CODE", "SHORT_NAME") if code in org.columns]
                predicates = " OR ".join(f"{org.col(code)}=%s" for code in lookup_codes)
                with connection.cursor() as cursor:
                    cursor.execute(
                        f"SELECT DISTINCT {org.col('OBJECT_ID')} object_id FROM {quote_identifier(org.name)} "
                        f"WHERE {predicates} LIMIT 11",
                        tuple(term for _ in lookup_codes),
                    ); short = cursor.fetchall()
                if len(short) == 1:
                    object_id = short[0]["object_id"]
                    with connection.cursor() as cursor:
                        cursor.execute(sql, (object_id, object_id, object_id)); rows = cursor.fetchall()
                    if len(rows) == 1:
                        return rows[0]
            with connection.cursor() as cursor:
                cursor.execute(
                    f"SELECT DISTINCT {fields[0]} object_id,{fields[1]} object_code,{fields[2]} object_name "
                    f"FROM {quote_identifier(obs.name)} WHERE {fields[2]} LIKE %s ORDER BY {fields[2]} LIMIT 11",
                    (f"%{term}%",),
                ); rows = cursor.fetchall()
        if not rows:
            raise QueryError("OBJECT_NOT_FOUND", f"未找到管控对象：{term}")
        raise QueryError("OBJECT_AMBIGUOUS", f"管控对象不唯一：{term}", rows[:10])

    def resolve_indicator(self, connection, term: str) -> dict[str, Any]:
        #指标解析，先查别名表，再匹配CODE、NAME
        table = self.bound("indicator_binding")
        table.require("INDICATOR_CODE", "INDICATOR_NAME", "UNIT")
        normalized = self._aliases().get(term.strip().casefold(), term.strip())
        code, name, unit = table.col("INDICATOR_CODE"), table.col("INDICATOR_NAME"), table.col("UNIT")
        extra = []
        for property_code, alias in (("CATEGORY", "category"), ("INDICATOR_TYPE", "indicator_type")):
            if property_code in table.columns:
                extra.append(f",{table.col(property_code)} {alias}")
        select = f"SELECT {code} indicator_code,{name} indicator_name,{unit} unit{''.join(extra)}"
        with connection.cursor() as cursor:
            cursor.execute(f"{select} FROM {quote_identifier(table.name)} WHERE {code}=%s OR {name}=%s LIMIT 11", (normalized, normalized))
            rows = cursor.fetchall()
        if len(rows) == 1:
            return rows[0]
        if not rows:
            with connection.cursor() as cursor:
                cursor.execute(f"{select} FROM {quote_identifier(table.name)} WHERE {name} LIKE %s ORDER BY {code} LIMIT 11", (f"%{term.strip()}%",))
                rows = cursor.fetchall()
        if not rows:
            raise QueryError("INDICATOR_NOT_FOUND", f"未找到指标：{term}")
        raise QueryError("INDICATOR_AMBIGUOUS", f"指标不唯一：{term}", rows[:10])

    def query(self, object_term: str, indicator_term: str, period: str | None, measure: str) -> dict[str, Any]:
        #主体查询逻辑
        measure_codes = {
            "current_value": "CURRENT_VALUE", "previous_value": "PREVIOUS_VALUE",
            "yoy_amount": "YOY_AMOUNT", "yoy_rate": "YOY_RATE",
        }
        if measure not in measure_codes:
            raise QueryError("MEASURE_INVALID", f"不支持的查询口径：{measure}")
        obs = self.bound("observation_binding")
        obs.require("PERIOD", "OBJECT_ID", "OBJECT_CODE", "OBJECT_NAME", "INDICATOR_CODE", "INDICATOR_NAME", "UNIT", measure_codes[measure])
        with self.connect() as connection:
            obj = self.resolve_object(connection, object_term)
            indicator = self.resolve_indicator(connection, indicator_term)
            if period is None:
                with connection.cursor() as cursor:
                    cursor.execute(
                        f"SELECT MAX({obs.col('PERIOD')}) period FROM {quote_identifier(obs.name)} "
                        f"WHERE {obs.col('OBJECT_ID')}=%s AND {obs.col('INDICATOR_CODE')}=%s",
                        (obj["object_id"], indicator["indicator_code"]),
                    ); period = cursor.fetchone()["period"]
            if not period:
                raise QueryError("OBSERVATION_NOT_FOUND", "没有可用期间数据")
            select_codes = ["PERIOD", "OBJECT_ID", "OBJECT_CODE", "OBJECT_NAME", "INDICATOR_CODE", "INDICATOR_NAME", "UNIT", measure_codes[measure]]
            aliases = ["period", "object_id", "object_code", "object_name", "indicator_code", "indicator_name", "unit", "value"]
            projection = ",".join(f"{obs.col(code)} {alias}" for code, alias in zip(select_codes, aliases))
            with connection.cursor() as cursor:
                cursor.execute(
                    f"SELECT {projection} FROM {quote_identifier(obs.name)} WHERE {obs.col('PERIOD')}=%s "
                    f"AND {obs.col('OBJECT_ID')}=%s AND {obs.col('INDICATOR_CODE')}=%s LIMIT 2",
                    (period, obj["object_id"], indicator["indicator_code"]),
                ); rows = cursor.fetchall()
        if not rows:
            raise QueryError("OBSERVATION_NOT_FOUND", f"指定企业、指标和期间没有数据：{period}")
        if len(rows) > 1:
            raise QueryError("OBSERVATION_AMBIGUOUS", "指定条件返回多条观测记录")
        row = dict(rows[0])
        if row.get("value") is not None:
            row["value"] = float(row["value"])
        row.update({"measure": measure, "binding": self.config["observation_binding"], "table": obs.name})
        return row

    def doctor(self) -> dict[str, Any]:
        bindings = {}
        for key in ("organization_binding", "indicator_binding", "observation_binding"):
            bound = self.bound(key)
            bindings[key] = {"name": self.config[key], "table": bound.name, "mappedProperties": sorted(bound.columns)}
        with self.connect() as connection, connection.cursor() as cursor:
            cursor.execute("SELECT 1 ok"); cursor.fetchone()
            obs = self.bound("observation_binding")
            cursor.execute(f"SELECT COUNT(*) row_count FROM {quote_identifier(obs.name)}")
            row_count = int(cursor.fetchone()["row_count"])
        return {"platform": "ok", "database": "ok", "bindings": bindings, "observationCount": row_count}


def print_result(result: dict[str, Any], as_json: bool) -> None:
    if as_json:
        print(json.dumps(result, ensure_ascii=False, indent=2, default=decimal_json)); return
    value = result["value"]
    if result["measure"] == "yoy_rate" and value is not None:
        display = f"{value * 100:.2f}%"
    else:
        display = f"{value}{result['unit'] or ''}" if value is not None else "无值"
    print(f"{result['period']}，{result['object_name']}的{result['indicator_name']}为 {display}（{result['indicator_code']}）。")


def main() -> int:
    parser = argparse.ArgumentParser(description="运管指标只读查询")
    sub = parser.add_subparsers(dest="command", required=True)
    query = sub.add_parser("query")
    query.add_argument("--object", required=True); query.add_argument("--indicator", required=True)
    query.add_argument("--period"); query.add_argument("--measure", default="current_value", choices=["current_value", "previous_value", "yoy_amount", "yoy_rate"])
    query.add_argument("--json", action="store_true")
    doctor = sub.add_parser("doctor"); doctor.add_argument("--json", action="store_true")
    args = parser.parse_args()
    try:
        service = OperationQueryService()
        result = service.doctor() if args.command == "doctor" else service.query(args.object, args.indicator, args.period, args.measure)
        print_result(result, getattr(args, "json", False)); return 0
    except QueryError as exc:
        payload = {"ok": False, "errorCode": exc.code, "message": str(exc), "candidates": exc.candidates}
        if getattr(args, "json", False):
            print(json.dumps(payload, ensure_ascii=False, indent=2, default=decimal_json))
        else:
            print(f"{exc.code}: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
