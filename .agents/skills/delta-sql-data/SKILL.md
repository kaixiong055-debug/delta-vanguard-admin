---
name: delta-sql-data
description: 用于 Delta 先锋 admin_portal MySQL 结构、测试数据、流程数据、system_menu、租户、会员、商品、商城订单、Delta 服务订单、订单市场、售后、退款、追回、结算、Outbox 和回滚 SQL 任务。优先使用内置结构索引，避免每次重新扫描全库或三个项目。
---

# Delta SQL 与流程数据工作流

先阅读仓库根目录 `AGENTS.md`，然后按任务读取：

```text
references/database-overview.md
references/delta-schema.md
references/baseline-data.md
references/test-data-playbook.md
references/schema-index.json
```

## 快速原则

- 不重新扫描整份 `1.sql`。
- 不重新遍历三个项目。
- 先从 `schema-index.json` 精确找到目标表、字段、索引和快照数据量。
- 只对目标表做最小实时检查。
- Java 当前 DO、Mapper、枚举和 Service 仍是业务状态的最终事实。
- 数据结构与 Java 冲突时，停止写入并报告迁移未同步。

## 当前快照事实

- 数据库：`admin_portal`
- MySQL：5.7.44
- 快照时间：2026-06-30 15:53:43
- 原始 `1.sql`：158 张表
- 原始 `1.sql`：25 张 Delta 表
- 全部 Delta 表为 0 行
- `trade_order`、`trade_order_item` 为 0 行
- 已有 tenant、member、pay_wallet、product_spu、product_sku
- 已补充 3 张 Phase 9 俱乐部表结构：application/profile/service_scope
- Skill 当前已知：161 张表、28 张 Delta 表
- 三张俱乐部表不在原始转储中，实时数据量未知，写入前需最小表存在检查

## 新增测试数据流程

1. 确认数据库是本地或开发环境。
2. 在 `baseline-data.md` 选择可复用基础 ID。
3. 在 `delta-schema.md` 查看目标表结构。
4. 从 Java 代码核对状态、必填字段、业务编号和联动。
5. 生成幂等初始化 SQL。
6. 生成按依赖逆序删除的回滚 SQL。
7. 执行前做目标表存在、ID 存在、唯一键冲突检查。
8. 执行后按表统计并验证页面/API。

## 禁止

- 执行原始全库转储；
- `DROP TABLE`、`TRUNCATE`；
- 无条件删除或更新；
- 写生产库；
- 写真实手机号、微信、OpenID、UnionID、支付流水、密码、Token；
- 猜测不存在的俱乐部表；
- 只插末端表导致关联残缺；
- 关闭租户隔离或把 tenantId 随意写成 1；
- 用 SQL 绕过必须由 Service 产生的并发、日志、Outbox 和状态联动，却声称流程已完整验证。

## 输出格式

```text
快照依据：
实时最小检查：
复用基础数据：
新增数据链：
初始化 SQL：
回滚 SQL：
验证：
与真实业务入口的差异：
```
