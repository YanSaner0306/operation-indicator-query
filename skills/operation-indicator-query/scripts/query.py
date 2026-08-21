#!/usr/bin/env python3
"""Query operation indicators through the BRRP mapped-data HTTP API only."""

from __future__ import annotations

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any

SKILL_DIR = Path(__file__).resolve().parents[1]


class QueryError(RuntimeError):
    def __init__(self, code: str, message: str, candidates: list[dict[str, Any]] | None = None):
        super().__init__(message)
        self.code = code
        self.candidates = candidates or []


def config_dir() -> Path:
    local = os.environ.get("LOCALAPPDATA")
    if not local:
        raise QueryError("CONFIG_MISSING", "LOCALAPPDATA 未设置")
    return Path(local) / "brrp-codex" / "operation-indicator-query"


def load_config() -> dict[str, Any]:
    path = config_dir() / "config.json"
    if not path.exists():
        raise QueryError("CONFIG_MISSING", f"未找到配置：{path}，请先运行 configure.ps1")
    with path.open("r", encoding="utf-8-sig") as handle:
        return json.load(handle)


def load_api_key(config: dict[str, Any]) -> str:
    #只读apikey
    env_name = str(config.get("api_key_env", "BRRP_OPERATION_API_KEY"))
    value = os.environ.get(env_name)
    if value:
        return value.strip()
    path = config_dir() / "platform-api-key.txt"
    if path.exists():
        value = path.read_text(encoding="utf-8-sig").strip()
        if value:
            return value
    raise QueryError("API_KEY_MISSING", f"未找到平台 API Key，请设置 {env_name} 或运行 configure.ps1")


class PlatformClient:
    def __init__(self, base_url: str, api_key: str, timeout: int = 10):
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout = timeout

    def request(self, method: str, path: str, params: dict[str, Any] | None = None) -> Any:
        #request拼接url查询串，带X-API-Key头
        query = urllib.parse.urlencode({k: v for k, v in (params or {}).items() if v is not None})
        url = self.base_url + path + (("?" + query) if query else "")
        request = urllib.request.Request(url, method=method, headers={"X-API-Key": self.api_key, "Accept": "application/json"})
        for attempt in range(3):
            try:
                with urllib.request.urlopen(request, timeout=self.timeout) as response:
                    payload = json.load(response)
                break
            except urllib.error.HTTPError as exc:
                if exc.code == 429 and attempt < 2:
                    retry_after = exc.headers.get("Retry-After")
                    delay = float(retry_after) if retry_after and retry_after.isdigit() else 0.25 * (2 ** attempt)
                    time.sleep(min(delay, 2.0))
                    continue
                try:
                    body = json.loads(exc.read().decode("utf-8"))
                    code, message = body.get("code", "PLATFORM_HTTP_ERROR"), body.get("message", str(exc))
                except Exception:
                    code, message = "PLATFORM_HTTP_ERROR", str(exc)
                raise QueryError(str(code), f"平台 API 返回 {exc.code}：{message}") from exc
            except OSError as exc:
                raise QueryError("PLATFORM_UNAVAILABLE", f"无法连接平台：{exc}") from exc
        if payload.get("code") != "SUCCESS":
            raise QueryError(str(payload.get("code", "PLATFORM_API_ERROR")), payload.get("message", "平台 API 调用失败"))
        return payload.get("data")

    def ontologies(self) -> list[dict[str, Any]]:
        #启用本体列表
        page = self.request("GET", "/api/v1/ontologies", {"status": "ENABLED", "page": 0, "size": 100})
        return list(page.get("items", []))
        #本体属性列表
    def properties(self, ontology_id: int) -> list[dict[str, Any]]:
        return list(self.request("GET", f"/api/v1/ontologies/{ontology_id}/properties"))
        #本体的启用Binding
    def bindings(self, ontology_id: int) -> list[dict[str, Any]]:
        page = self.request("GET", "/api/v1/bindings", {"ontologyId": ontology_id, "status": "ENABLED", "page": 1, "size": 100})
        return list(page.get("items", []))
        #按业务主键获取单条映射记录
    def mapped_record(self, ontology_id: int, business_key: str) -> dict[str, Any]:
        encoded = urllib.parse.quote(str(business_key), safe="")
        return self.request("GET", f"/api/v1/mapped-data/{ontology_id}/records/{encoded}")
        #按本体属性查询映射记录
    def find_records(self, ontology_id: int, property_id: int, value: Any, limit: int = 100) -> list[dict[str, Any]]:
        data = self.request("GET", f"/api/v1/mapped-data/{ontology_id}/records", {"propertyId": property_id, "value": str(value), "limit": max(1, min(100, int(limit)))})
        return list(data or [])


class OperationQueryService:
    def __init__(self):
        self.config = load_config()
        self.platform = PlatformClient(self.config["platform_base_url"], load_api_key(self.config), int(self.config.get("connect_timeout_seconds", 10)))
        self._ontology_cache: dict[str, dict[str, Any]] = {}
        self._schema_cache: dict[str, dict[str, Any]] = {}

    def _ontology(self, key: str) -> dict[str, Any]:
        code = str(self.config[key])
        cache_key = code.casefold()
        if cache_key not in self._ontology_cache:
            matches = [item for item in self.platform.ontologies() if str(item.get("code", "")).casefold() == cache_key or str(item.get("name", "")).casefold() == cache_key]
            if len(matches) != 1:
                raise QueryError("ONTOLOGY_NOT_READY", f"启用本体应唯一存在：{code}", matches[:10])
            self._ontology_cache[cache_key] = matches[0]
        return self._ontology_cache[cache_key]

    def _schema(self, key: str) -> dict[str, Any]:
        ontology = self._ontology(key)
        cache_key = str(ontology["id"])
        if cache_key not in self._schema_cache:
            props = self.platform.properties(int(ontology["id"]))
            self._schema_cache[cache_key] = {"ontology": ontology, "properties": {str(item.get("code", "")).casefold(): item for item in props}}
        return self._schema_cache[cache_key]

    def _property(self, ontology_key: str, property_key: str) -> dict[str, Any]:
        configured = self.config.get(property_key)
        if not configured:
            raise QueryError("CONFIG_INVALID", f"缺少配置：{property_key}")
        prop = self._schema(ontology_key)["properties"].get(str(configured).casefold())
        if not prop:
            raise QueryError("PROPERTY_NOT_READY", f"本体中不存在属性：{configured}")
        return prop

    @staticmethod
    def _value(record: dict[str, Any], code: str | None) -> Any:
        if not code:
            return None
        for key, value in record.get("values", {}).items():
            if str(key).casefold() == str(code).casefold():
                return value
        return None

    def _resolve(self, ontology_key: str, property_keys: list[str], term: str, not_found: str, ambiguous: str) -> dict[str, Any]:
        #跳12通用
        schema = self._schema(ontology_key)
        found: dict[str, dict[str, Any]] = {}
        for property_key in property_keys:
            # 如 OBJECT_ID → OBJECT_CODE → OBJECT_NAME → OBJECT_SHORT_NAME
            configured = self.config.get(property_key)
            if not configured:
                continue
            prop = self._property(ontology_key, property_key)
            for record in self.platform.find_records(int(schema["ontology"]["id"]), int(prop["id"]), term, 100):
                # 每次都是一个 GET /mapped-data/.../records 请求
                found[str(record.get("businessKey"))] = record
        if not found:
            raise QueryError(not_found, f"未找到匹配对象：{term}")
        if len(found) > 1:
            raise QueryError(ambiguous, f"匹配结果不唯一：{term}", [{"businessKey": r.get("businessKey"), "values": r.get("values", {})} for r in list(found.values())[:10]])
        return next(iter(found.values()))

    def resolve_object(self, term: str) -> dict[str, Any]:
        #解析出管控对象记录
        return self._resolve("object_ontology_code", ["object_id_property", "object_code_property", "object_name_property", "object_short_name_property"], term, "OBJECT_NOT_FOUND", "OBJECT_AMBIGUOUS")

    def resolve_indicator(self, term: str) -> dict[str, Any]:
        #解析出指标定义记录
        normalized = self._aliases().get(term.strip().casefold(), term.strip())
        return self._resolve("indicator_ontology_code", ["indicator_code_property", "indicator_name_property"], normalized, "INDICATOR_NOT_FOUND", "INDICATOR_AMBIGUOUS")

    def _aliases(self) -> dict[str, str]:
        path = SKILL_DIR / "references" / "indicator_aliases.json"
        with path.open("r", encoding="utf-8") as handle:
            return {str(k).casefold(): str(v) for k, v in json.load(handle).items()}

    def _find_observation(self, object_record: dict[str, Any], indicator_record: dict[str, Any], period: str | None) -> dict[str, Any]:
        #对用跳1+跳2结果汇聚观测记录
        object_id = self._value(object_record, self.config.get("object_id_property"))
        indicator_code = self._value(indicator_record, self.config.get("indicator_code_property"))
        if object_id is None or indicator_code is None:
            raise QueryError("MAPPING_INCOMPLETE", "管控对象或指标缺少业务编码属性")
        observation_schema = self._schema("observation_ontology_code")
        observation_id = int(observation_schema["ontology"]["id"])
        key_template = self.config.get("observation_key_template")
        if period and key_template:
            business_key = str(key_template).format(period=period, object_id=object_id, indicator_code=indicator_code)
            try:
                return self.platform.mapped_record(observation_id, business_key)
            except QueryError as error:
                if error.code not in {"MAPPED_DATA_NOT_FOUND", "40452"}:
                    raise
        indicator_property = self._property("observation_ontology_code", "observation_indicator_code_property")
        records = self.platform.find_records(observation_id, int(indicator_property["id"]), indicator_code, 100)
        matches = [record for record in records if self._value(record, self.config.get("observation_indicator_code_property")) == indicator_code and (period is None or self._value(record, self.config.get("observation_period_property")) == period)]
        if not matches:
            raise QueryError("OBSERVATION_NOT_FOUND", f"指定企业、指标和期间没有数据：{period or '最新期间'}")
        if period is None:
            latest = max((self._value(item, self.config.get("observation_period_property")) for item in matches if self._value(item, self.config.get("observation_period_property")) is not None), default=None)
            matches = [item for item in matches if self._value(item, self.config.get("observation_period_property")) == latest]
        if len(matches) > 1:
            raise QueryError("OBSERVATION_AMBIGUOUS", "指定条件返回多条观测记录", matches[:10])
        return matches[0]

    def query(self, object_term: str, indicator_term: str, period: str | None, measure: str) -> dict[str, Any]:
        #多步查询，对象解析 → 指标解析 → 观测汇聚
        if measure not in {"current_value", "previous_value", "yoy_amount", "yoy_rate"}:
            raise QueryError("MEASURE_INVALID", f"不支持的查询口径：{measure}")
        object_record, indicator_record = self.resolve_object(object_term), self.resolve_indicator(indicator_term)
        observation = self._find_observation(object_record, indicator_record, period)
        value = self._value(observation, self.config[measure + "_property"])
        return {
            "period": self._value(observation, self.config.get("observation_period_property")),
            "object_id": self._value(object_record, self.config.get("object_id_property")),
            "object_code": self._value(object_record, self.config.get("object_code_property")),
            "object_name": self._value(object_record, self.config.get("object_name_property")),
            "indicator_code": self._value(indicator_record, self.config.get("indicator_code_property")),
            "indicator_name": self._value(indicator_record, self.config.get("indicator_name_property")),
            "unit": self._value(indicator_record, self.config.get("indicator_unit_property")) or self._value(observation, self.config.get("observation_unit_property")),
            "value": float(value) if isinstance(value, (int, float)) else value,
            "measure": measure,
            "binding_id": observation.get("bindingId"),
            "ontology_id": observation.get("ontologyId"),
        }

    def doctor(self) -> dict[str, Any]:
        checks = {}
        for key in ("object_ontology_code", "indicator_ontology_code", "observation_ontology_code"):
            schema = self._schema(key)
            bindings = self.platform.bindings(int(schema["ontology"]["id"]))
            if len(bindings) != 1:
                raise QueryError("BINDING_NOT_READY", f"启用 Binding 应唯一存在：{schema['ontology'].get('code')}")
            checks[key] = {"ontologyId": schema["ontology"]["id"], "ontologyCode": schema["ontology"].get("code"), "bindingId": bindings[0].get("id"), "bindingStatus": bindings[0].get("status")}
        return {"platform": "ok", "dataAccess": "platform-managed", "ontologies": checks}


def print_result(result: dict[str, Any], as_json: bool) -> None:
    if as_json:
        print(json.dumps(result, ensure_ascii=False, indent=2, default=str))
        return
    value = result["value"]
    display = f"{float(value) * 100:.2f}%" if result["measure"] == "yoy_rate" and value is not None else f"{value}{result.get('unit') or ''}" if value is not None else "无值"
    print(f"{result['period']}，{result['object_name']}的{result['indicator_name']}为 {display}（{result['indicator_code']}）。")


def main() -> int:
    parser = argparse.ArgumentParser(description="运管指标只读查询（平台 API 模式）")
    sub = parser.add_subparsers(dest="command", required=True)
    query = sub.add_parser("query")
    query.add_argument("--object", required=True)
    query.add_argument("--indicator", required=True)
    query.add_argument("--period")
    query.add_argument("--measure", default="current_value", choices=["current_value", "previous_value", "yoy_amount", "yoy_rate"])
    query.add_argument("--json", action="store_true")
    doctor = sub.add_parser("doctor")
    doctor.add_argument("--json", action="store_true")
    args = parser.parse_args()
    try:
        service = OperationQueryService()
        result = service.doctor() if args.command == "doctor" else service.query(args.object, args.indicator, args.period, args.measure)
        print_result(result, getattr(args, "json", False))
        return 0
    except QueryError as exc:
        payload = {"ok": False, "errorCode": exc.code, "message": str(exc), "candidates": exc.candidates}
        if getattr(args, "json", False):
            print(json.dumps(payload, ensure_ascii=False, indent=2, default=str))
        else:
            print(f"{exc.code}: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
