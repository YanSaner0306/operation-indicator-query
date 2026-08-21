# 验收说明

运行完整验收：

```powershell
python scripts/acceptance.py
```

验收覆盖平台 API、三个启用 Binding、4719 条观测、企业全称/编码/简称、指标编码/名称/ROE 别名、本期值/上期值/同比，以及歧义、无数据和注入样式输入。

同步 MOCK 数据是独立、显式操作：

```powershell
python scripts/sync_mock_data.py "D:\实习\业务本体推理平台\运管司库方案\运管\MOCK-指标数据表.xlsx"
```
