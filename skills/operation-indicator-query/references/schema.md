# 运管查询模型

## 平台对象

- 本体：`管控对象`、`指标定义`、`运营指标观测`。
- Binding：`运管-管控对象绑定`、`运管-指标定义绑定`、`运管-运营指标观测绑定`。
- 查询前要求目标 Binding 状态为 `ENABLED`，且字段映射包含所需属性编码。

## 观测唯一性

一条指标观测由 `(PERIOD, OBJECT_ID, INDICATOR_CODE)` 唯一确定。`OBSERVATION_KEY` 的物理值为 `period|object_id|indicator_code`。

默认查询 `CURRENT_VALUE`。同比变动率在数据库中保存为小数，展示时乘以 100 并添加 `%`。

## 名称解析

- 企业：先精确匹配 `OBJECT_ID`、`OBJECT_CODE`、`OBJECT_NAME`，再使用管控对象 Binding 的 `SHORT_NAME`，最后进行包含匹配。
- 指标：先精确匹配 `INDICATOR_CODE`、`INDICATOR_NAME` 和维护的别名，再进行包含匹配。
- 完全同名的不同指标必须返回候选，不能静默选择。

## 数据边界

平台只保存本体、属性、数据源和 Binding；真实观测数据保留在 `operation_management` 业务库。Skill 使用平台 API Key 获取映射，用专用只读数据库账号执行参数化查询。
