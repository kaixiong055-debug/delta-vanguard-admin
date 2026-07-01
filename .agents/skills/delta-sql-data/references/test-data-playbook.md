# Delta 测试数据生成手册

## 目标

今后用户要求“添加几条数据验证页面或流程”时，先使用本手册和结构索引，不要重新扫描三个项目或整份 SQL 转储。

## 仍然必须做的最小实时检查

Skill 是快照，不能替代数据库实时状态。执行写入前只做以下定向检查：

```sql
SELECT DATABASE();
SHOW TABLES LIKE '目标表';
SELECT ... FROM system_tenant/member_user/product_spu/product_sku
WHERE id IN (...);
SELECT ... FROM 目标表 WHERE 测试业务编号 LIKE 'TEST_DELTA_%';
```

不要重新导出全库，也不要无目的读取所有行。

## 环境规则

- 无法确认是本地或开发环境：只生成 SQL，不执行。
- 原始 `1.sql` 含 `DROP TABLE`，禁止整份执行。
- 禁止 `TRUNCATE`、无条件 `DELETE`、无条件 `UPDATE`。
- 所有测试业务编号统一使用 `TEST_DELTA_`、`TEST_CLUB_`、`TEST_ORDER_`、`TEST_MARKET_`。
- SQL 必须可重复执行、可回滚并使用事务。
- 回滚只删除本次测试前缀数据。

## 基础插入顺序

### A. 商品到服务订单

```text
system_tenant
member_user
product_spu
product_sku
delta_product_service_config
trade_order
trade_order_item
delta_service_order
delta_order_log
```

### B. 打手流程

```text
member_user
delta_worker_application
delta_worker
delta_worker_skill
delta_order_assignment
delta_order_progress
delta_order_evidence
delta_order_acceptance
delta_worker_settlement
```

### C. 取消、售后、退款、追回

```text
delta_service_order
├── delta_order_cancel
└── delta_after_sale
    └── delta_after_sale_arbitration
        ├── delta_refund_record
        │   └── delta_refund_log
        └── delta_fund_recovery
            └── delta_fund_recovery_log
```

### D. 俱乐部入驻与平台订单市场

```text
member_user
delta_club_application
system_tenant
delta_club_profile
delta_club_service_scope
delta_service_order
delta_order_market_listing
delta_order_market_log
```

约束：

- 入驻申请发生在租户创建前，不写 `tenant_id`。
- 审核通过后，`approved_tenant_id`、`delta_club_profile.tenant_id` 和租户记录必须一致。
- `owner_member_id` 必须指向真实 `member_user`。
- 可抢单俱乐部必须启用，并具有对应 `service_type` 的启用服务范围。
- 容量校验使用 `max_concurrent_orders` 与后端真实统计规则。
- 抢单数据不得由前端提交 `clubId`、`tenantId` 或 `operatorId`。
- 订单市场测试数据必须遵守挂牌状态、`active_flag` 和唯一键。

### E. 通知和超时

优先通过真实 Service 触发：

```text
delta_event_outbox
delta_member_notification
delta_reminder_record
```

直接 SQL 造业务数据时，不要伪造“通知已成功发送”。可以不插 Outbox，但报告必须写明与真实业务链的差异。

## 状态一致性

插入每个场景前，必须从 Java 枚举和 Service 状态矩阵确认状态值。数据库注释只用于快速定位，不能替代当前代码。

重点检查：

- `delta_service_order.status`
- `delta_order_assignment.assignment_status`
- `delta_order_market_listing.listing_status` 与 `active_flag`
- `delta_order_cancel.apply_status`
- `delta_after_sale.status`
- `delta_refund_record.refund_status`
- `delta_fund_recovery.recovery_status`
- `delta_worker_settlement.settlement_status`
- `delta_finance_reconciliation.status`

## 金额和快照字段

- 金额单位均按“分”处理。
- `service_amount`、`commission_rate`、`platform_fee`、`worker_amount` 必须数学一致。
- 服务订单必须保留商品名、SKU 名、金额和抽成快照。
- 不要通过修改商城订单状态机来制造 Delta 状态。

## 生成文件

优先使用仓库现有 SQL 目录；若无既有规范：

```text
sql/dev/<feature>_test_data.sql
sql/dev/<feature>_test_data_rollback.sql
docs/<feature>_test_data_report.md
```

## 验证报告

必须输出：

1. 数据库和环境；
2. 复用的 tenantId、memberUserId、spuId、skuId；
3. 新增表和行数；
4. 测试业务编号；
5. 状态链；
6. 已验证页面或接口；
7. 未验证内容；
8. 初始化和回滚 SQL 路径。
