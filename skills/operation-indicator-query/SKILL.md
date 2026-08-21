---
name: operation-indicator-query
description: 通过业务本体推理平台的 API Key 和启用 Binding，只读查询运管领域的企业指标数据。适用于查询企业某期间的本期值、上期值、同比变动额或同比变动率；不用于修改平台配置、导入数据或执行任意 SQL。
---

# 运管指标查询

使用本 Skill 回答管控对象、指标和期间相关的运管数据问题。平台是本体和 Binding 白名单的事实来源，业务数据库只执行参数化只读查询。

## 查询流程

1. 从用户问题识别企业或管控对象、指标、期间和口径。
2. 运行 `scripts/query.py query`。企业和指标使用用户原文；未指定期间时不传 `--period`，脚本会取该对象和指标的最新有效期间。
3. 优先使用脚本的 JSON 输出组织答案。必须返回期间、标准企业名称、标准指标名称、指标编码、值和单位。
4. 多个候选时列出候选并请求用户澄清；无数据时明确说明，不得把缺失值当作 0。

```powershell
python scripts/query.py query --object "四川省蜀盛产业投资集团有限公司" --indicator "净资产收益率" --json
python scripts/query.py query --object "0" --indicator "IND_095" --period "2026-07" --measure previous_value --json
```

允许的 `measure`：`current_value`、`previous_value`、`yoy_amount`、`yoy_rate`。用户没有指定时使用 `current_value`。

## 运行约束

- 禁止接受或拼接用户提供的 SQL、表名、字段名、排序或表达式。
- 表名和字段名只能来自平台中名称匹配且状态为 `ENABLED` 的 Binding。
- API Key 与数据库密码只从 Windows DPAPI 凭据或运行时环境变量读取，不得写入答案、日志或仓库。
- Windows DPAPI 凭据绑定于配置它的桌面用户。Codex 新任务运行 `doctor`、`query` 或 `acceptance.py` 时，必须对该脚本请求授权提升，以同一 Windows 用户身份执行。若普通沙箱执行返回 `CREDENTIAL_REQUIRES_ELEVATION`，立即用授权提升重跑同一命令，不得报告为“没有数据”。
- 不自动运行 `sync_mock_data.py`。只有用户明确要求同步测试数据时才可执行。
- 查询失败时先运行 `python scripts/query.py doctor --json`，不要绕过 Binding 直接猜测表结构。

需要理解本体、Binding 和数据库字段时读取 [references/schema.md](references/schema.md)。需要执行或扩展验收时读取 [references/acceptance.md](references/acceptance.md)。
