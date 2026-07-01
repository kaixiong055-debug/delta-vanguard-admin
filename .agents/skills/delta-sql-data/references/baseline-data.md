# 安全基础数据摘要

来源快照：`admin_portal`，`30/06/2026 15:53:43`。

> 仅保留测试数据生成需要的 ID、状态和金额。密码哈希、手机号、邮箱、Token、IP、地址、访问日志内容均未写入 Skill。

## 租户

| tenantId | 名称 | 状态 | 套餐 | 过期时间 | 账号上限 | deleted |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 芋道源码 | 0 | 0 | 2099-02-19 17:14:16 | 9999 | 0 |
| 121 | 小租户 | 0 | 111 | 2026-07-10 00:00:00 | 30 | 0 |
| 122 | 测试租户 | 0 | 111 | 2023-04-29 00:00:00 | 50 | 0 |

注意：快照中的部分 `system_users.tenant_id` 为 118、119、120，但 `system_tenant` 中没有对应记录。测试数据不要使用这些孤立租户。

## Member 用户与钱包

| memberUserId | 状态 | tenantId | 注册终端 | deleted | 创建时间 |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | 1 | 10 | 0 | 2026-06-15 10:17:07 |
| 2 | 0 | 1 | 10 | 0 | 2026-06-16 14:43:31 |

| walletId | memberUserId | userType | 余额(分) | 支出(分) | 充值(分) | tenantId |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 1 | 0 | 0 | 0 | 1 |
| 2 | 2 | 1 | 0 | 0 | 0 | 1 |

## 商品 SPU

| spuId | 商品 | 状态原值 | 价格(分) | 库存 | SKU数 | tenantId |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 三角洲陪玩 | -1 | 5500 | 999 | 1 | 1 |
| 2 | 三角洲陪玩 | -1 | 5500 | 999 | 0 | 1 |
| 3 | 三角洲陪玩 | -1 | 5500 | 999 | 0 | 1 |
| 4 | 三角洲陪玩 | -1 | 5500 | 0 | 1 | 1 |
| 5 | 三角洲绝密体验单 | 1 | 9800 | 3996 | 4 | 1 |
| 6 | 三角洲监狱体验单 | 1 | 12800 | 1998 | 2 | 1 |
| 7 | 三角洲特色堵约单 | 1 | 58800 | 1998 | 2 | 1 |
| 8 | 三角洲单局堵约单 | 1 | 9800 | 11988 | 12 | 1 |

## 商品 SKU

| skuId | spuId | 价格(分) | 市场价(分) | 成本(分) | 库存 | tenantId |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 1 | 5500 | 6600 | 5500 | 999 | 1 |
| 2 | 5 | 9800 | 12800 | 9900 | 999 | 1 |
| 3 | 5 | 9800 | 12800 | 9900 | 999 | 1 |
| 4 | 5 | 16800 | 18800 | 9900 | 999 | 1 |
| 5 | 5 | 16800 | 18800 | 9900 | 999 | 1 |
| 6 | 6 | 12800 | 15800 | 15800 | 999 | 1 |
| 7 | 6 | 12800 | 15800 | 15800 | 999 | 1 |
| 8 | 7 | 58800 | 68800 | 48800 | 999 | 1 |
| 9 | 7 | 58800 | 68800 | 48800 | 999 | 1 |
| 10 | 8 | 9800 | 9800 | 9800 | 999 | 1 |
| 11 | 8 | 9800 | 9800 | 9800 | 999 | 1 |
| 12 | 8 | 12800 | 12800 | 9800 | 999 | 1 |
| 13 | 8 | 12800 | 12800 | 9800 | 999 | 1 |
| 14 | 8 | 15800 | 15800 | 9800 | 999 | 1 |
| 15 | 8 | 15800 | 15800 | 9800 | 999 | 1 |
| 16 | 8 | 18800 | 18800 | 9800 | 999 | 1 |
| 17 | 8 | 18800 | 18800 | 9800 | 999 | 1 |
| 18 | 8 | 48800 | 48800 | 9800 | 999 | 1 |
| 19 | 8 | 48800 | 48800 | 9800 | 999 | 1 |
| 20 | 8 | 58800 | 58800 | 9800 | 999 | 1 |
| 21 | 8 | 58800 | 58800 | 9800 | 999 | 1 |
| 22 | 4 | 5500 | 6500 | 6000 | 0 | 1 |

## 业务空表

以下关键链路在快照中没有任何记录：

```text
trade_order
trade_order_item
全部 delta_* 表
```

因此生成 Delta 流程测试数据时不能只插入末端表。至少要补齐商城订单、订单项、Delta 服务订单及目标场景依赖记录。

## Delta 菜单快照

快照内识别到 42 条 Delta 页面或按钮菜单。该列表可能早于当前线上菜单，只用于理解 ID 和层级，不能代替实时 `system_menu`。

| id | 名称 | parentId | path | component | componentName | permission | type |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 5237 | 服务订单列表 | 5236 | serviceOrder | delta/service-order/index | DeltaServiceOrder |  | 2 |
| 5238 | 服务订单查询 | 5237 |  |  |  | delta:service-order:query | 3 |
| 5239 | 服务订单派单 | 5237 |  |  |  | delta:service-order:dispatch | 3 |
| 5240 | 服务订单改派 | 5237 |  |  |  | delta:service-order:reassign | 3 |
| 5241 | 退回接单池 | 5237 |  |  |  | delta:service-order:return-pool | 3 |
| 5242 | 派单中心 | 5236 | dispatch | delta/dispatch/index | DeltaDispatch |  | 2 |
| 5243 | 派单查询 | 5242 |  |  |  | delta:service-order:query | 3 |
| 5244 | 派单 | 5242 |  |  |  | delta:service-order:dispatch | 3 |
| 5245 | 改派 | 5242 |  |  |  | delta:service-order:reassign | 3 |
| 5246 | 退回接单池 | 5242 |  |  |  | delta:service-order:return-pool | 3 |
| 6736 | Outbox事件查询 | 6735 | list | delta/eventOutbox/index |  | delta:event-outbox:query | 2 |
| 6737 | 事件重试 | 6736 |  |  |  | delta:event-outbox:retry | 3 |
| 6738 | 事件标记死亡 | 6736 |  |  |  | delta:event-outbox:update | 3 |
| 7001 | 打手列表 | 7000 | worker | delta/worker/index | DeltaWorker |  | 2 |
| 7002 | 打手查询 | 7001 |  |  |  | delta:worker:query | 3 |
| 7003 | 打手编辑 | 7001 |  |  |  | delta:worker:update | 3 |
| 7004 | 打手技能 | 7001 |  |  |  | delta:worker:update-skill | 3 |
| 7005 | 打手启停 | 7001 |  |  |  | delta:worker:update-status | 3 |
| 7007 | 入驻审核列表 | 7006 | worker-application | delta/worker-application/index | DeltaWorkerApplication |  | 2 |
| 7008 | 申请查询 | 7007 |  |  |  | delta:worker-application:query | 3 |
| 7009 | 通过申请 | 7007 |  |  |  | delta:worker-application:approve | 3 |
| 7010 | 驳回申请 | 7007 |  |  |  | delta:worker-application:reject | 3 |
| 7012 | 结算列表 | 7011 | settlement | delta/settlement/index | DeltaSettlement |  | 2 |
| 7014 | 配置列表 | 7013 | product-service-config | delta/product-service-config/index | DeltaProductServiceConfig |  | 2 |
| 7060 | 运营统计 | 7078 | statistics | delta/statistics/index | DeltaStatistics |  | 2 |
| 7061 | 运营统计查询 | 7060 |  |  |  | delta:statistics:query | 3 |
| 7062 | 财务汇总 | 7078 | finance | delta/finance/index | DeltaFinance |  | 2 |
| 7063 | 财务汇总查询 | 7062 |  |  |  | delta:finance:query | 3 |
| 7064 | 财务对账 | 7078 | finance-reconciliation | delta/financeReconciliation/index | DeltaFinanceReconciliation |  | 2 |
| 7065 | 对账记录查询 | 7064 |  |  |  | delta:finance-reconciliation:query | 3 |
| 7066 | 生成对账 | 7064 |  |  |  | delta:finance-reconciliation:generate | 3 |
| 7067 | 确认对账 | 7064 |  |  |  | delta:finance-reconciliation:confirm | 3 |
| 7068 | 重试对账 | 7064 |  |  |  | delta:finance-reconciliation:retry | 3 |
| 7069 | 取消对账 | 7064 |  |  |  | delta:finance-reconciliation:cancel | 3 |
| 7070 | 导出对账 | 7064 |  |  |  | delta:finance-reconciliation:export | 3 |
| 7072 | 平台挂牌管理 | 7071 | listing | delta/orderMarket/index |  | delta:order-market:query | 2 |
| 7073 | 可接订单 | 7071 | available | delta/orderMarket/available |  | delta:order-market:claim | 2 |
| 7074 | 发布挂牌 | 7072 |  |  |  | delta:order-market:publish | 3 |
| 7075 | 撤回挂牌 | 7072 |  |  |  | delta:order-market:withdraw | 3 |
| 7076 | 指定俱乐部 | 7072 |  |  |  | delta:order-market:assign | 3 |
| 7077 | 已接订单 | 7073 |  |  |  | delta:order-market:query | 3 |
| 7078 | Delta 先锋俱乐部 | 0 | /delta-vanguard |  |  |  | 1 |

## 俱乐部表结构补充

Skill 已知以下 Phase 9 表：

```text
delta_club_application
delta_club_profile
delta_club_service_scope
```

这些表不在原始 `1.sql` 快照中，因此没有可复用的现有 clubId、applicationId 或服务范围记录。生成数据前只需做最小实时查询：

```sql
SHOW TABLES LIKE 'delta_club_%';

SELECT id, application_no, applicant_member_id, club_name,
       application_status, approved_tenant_id, deleted
FROM delta_club_application
WHERE deleted = 0
ORDER BY id DESC
LIMIT 20;

SELECT id, tenant_id, club_code, club_name, owner_member_id,
       business_status, platform_commission_rate,
       max_concurrent_orders, application_id, deleted
FROM delta_club_profile
WHERE deleted = 0
ORDER BY id DESC
LIMIT 20;

SELECT id, tenant_id, club_profile_id, service_type, enabled, deleted
FROM delta_club_service_scope
WHERE deleted = 0
ORDER BY id DESC
LIMIT 50;
```
