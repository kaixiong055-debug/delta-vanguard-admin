# SQL 快速地图

## 快照

```text
数据库：admin_portal
MySQL：50744 (5.7.44-log)
时间：30/06/2026 15:53:43
原始快照表：158
Skill 当前已知表：161
原始快照 Delta 表：25
Skill 当前已知 Delta 表：28
```

## 首选参考文件

- `schema-index.json`：全部 158 张表的字段、索引、注释和快照 INSERT 数。
- `database-overview.md`：模块规模和非空表。
- `delta-schema.md`：28 张已知 Delta 表的完整字段与约束。
- `baseline-data.md`：可安全复用的租户、会员、钱包、SPU、SKU ID。
- `test-data-playbook.md`：各流程的插入与回滚顺序。
- `phase9-club-tables.sql`：俱乐部入驻、档案、服务范围三表精确 DDL。

## 只有这些情况才重新读原始 SQL

- 用户明确上传了更新版本；
- 目标表不在索引中；
- 当前数据库迁移时间晚于 2026-06-30；
- Java DO/Mapper 与快照字段冲突。

原始转储包含敏感数据和破坏性 DDL，默认不复制进 Skill。
