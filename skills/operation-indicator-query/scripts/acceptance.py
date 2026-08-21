#!/usr/bin/env python3
"""Live acceptance suite for the operation indicator Skill."""

from __future__ import annotations

import sys
from decimal import Decimal
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent / "_vendor"))

from query import OperationQueryService, QueryError


def expect(condition: bool, label: str) -> None:
    if not condition:
        raise AssertionError(label)
    print(f"PASS {label}")


def close(actual, expected, tolerance=1e-8):
    return abs(Decimal(str(actual)) - Decimal(str(expected))) <= Decimal(str(tolerance))


def main() -> int:
    service = OperationQueryService()
    doctor = service.doctor()
    expect(doctor["platform"] == "ok" and doctor["database"] == "ok", "平台和数据库连通")
    expect(doctor["observationCount"] == 4719, "观测记录数为 4719")

    full = service.query("四川省蜀盛产业投资集团有限公司", "净资产收益率", "2026-07", "current_value")
    expect(full["indicator_code"] == "IND_095" and close(full["value"], 53.9), "企业全称 + 指标别名查询本期值")
    by_code = service.query("0", "IND_095", "2026-07", "current_value")
    expect(by_code["object_id"] == "0" and close(by_code["value"], 53.9), "企业编码 + 指标编码查询")
    by_short = service.query("蜀盛集团", "ROE", None, "current_value")
    expect(by_short["period"] == "2026-07" and close(by_short["value"], 53.9), "企业简称 + ROE + 最新期间查询")
    previous = service.query("0", "IND_095", "2026-07", "previous_value")
    expect(close(previous["value"], 49.95), "上期值查询")
    yoy = service.query("0", "IND_095", "2026-07", "yoy_rate")
    expect(close(yoy["value"], 0.0791), "同比变动率查询")

    try:
        service.query("0", "应收账款", "2026-07", "current_value")
        raise AssertionError("同名指标应返回歧义")
    except QueryError as exc:
        expect(exc.code == "INDICATOR_AMBIGUOUS" and len(exc.candidates) >= 2, "同名指标歧义保护")

    try:
        service.query("不存在企业' OR 1=1 --", "IND_095", "2026-07", "current_value")
        raise AssertionError("注入样式企业名不应命中")
    except QueryError as exc:
        expect(exc.code == "OBJECT_NOT_FOUND", "参数化查询抵御注入样式输入")

    after = service.doctor()
    expect(after["observationCount"] == 4719, "异常输入后数据完整")
    print("ACCEPTANCE PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
