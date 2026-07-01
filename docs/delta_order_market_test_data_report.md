# Delta 订单市场本地测试数据报告

## 1. 环境结论

- 当前 Java 默认 profile：`dev`。
- 当前默认 JDBC：公网 `43.136.92.78:3306/admin_portal`。
- 仓库另有本地配置：`127.0.0.1:3306/ruoyi-vue-pro`，但它不是当前激活 profile。
- 实际使用数据库：未连接、未执行；因此没有对任何数据库写入数据。
- 是否确认开发环境：否。默认配置指向公网，不能证明它是安全的本地开发库。
- 两份 SQL 都设置 `@confirm_local_dev := 0`。只有人工确认本地连接并改为 `1` 后才会写入或回滚；临时存储过程的异常处理保证任一 DML 失败时执行 ROLLBACK。

## 2. 已核对的真实代码与结构

读取了《接口文档.md》、市场/俱乐部 Controller、Service、DO、Mapper、枚举、错误码，以及商城、会员和 Delta 建表 SQL。相关 Delta Mapper 均为 Java Mapper，没有该流程专用 Mapper XML。

实际表名：

- `system_tenant`、`system_users`、`member_user`
- `product_spu`、`product_sku`、`delta_product_service_config`
- `trade_order`、`trade_order_item`
- `delta_club_application`、`delta_club_profile`、`delta_club_service_scope`
- `delta_service_order`
- `delta_order_market_listing`、`delta_order_market_log`
- 已检查但本脚本不写入：`delta_worker`、`delta_order_assignment`、`delta_event_outbox`、`delta_member_notification`

真实状态：AVAILABLE=0、CLAIMED=1、WITHDRAWN=2、EXPIRED=3、CLOSED=4。服务单测试状态使用 PENDING_DISPATCH=10。AVAILABLE 使用 `active_flag=1`；CLAIMED、EXPIRED 使用 `active_flag=0`。

## 3. 数据库迁移阻塞

当前 `sql/mysql1/1.sql` 快照存在以下差异：

1. 不存在 `delta_club_application`、`delta_club_profile`、`delta_club_service_scope`，说明 Phase 9 正式迁移尚未体现在快照中。
2. `delta_order_market_listing` 缺少源码迁移定义的唯一索引 `uk_service_order_active(service_order_id, active_flag)`，且快照中 `active_flag` 可空、默认 NULL。
3. `delta_service_order` 缺少 DO 已使用的 `product_pic_url` 和 `count` 字段；字段位于 Phase 3 迁移。

初始化脚本不会自行创建或修复业务表。必须先在本地数据库应用正式迁移，再运行测试数据脚本。

## 4. 前置数据选择规则

脚本不硬编码 tenantId/memberId/clubId：

- 平台租户：自动选择同时存在有效后台管理员、商品和库存 SKU 的有效租户。
- 目标俱乐部租户：选择不同于平台、存在有效后台管理员、尚无俱乐部档案的有效租户。
- 平台管理员和目标俱乐部管理员：从各自租户的 `system_users` 动态选择。
- 商品、SKU：优先复用已有启用 Delta 配置，否则选择尚未配置的库存 SKU并新增最小 `delta_product_service_config`。
- 平台买家：在平台租户新增无手机号、无微信、无可登录密码的 `TEST_DELTA_BUYER`。
- 俱乐部 owner：在各自俱乐部租户新增 `TEST_CLUB_OWNER_*` member 记录，避免跨租户会员关联。

数据库快照显示可推断的平台租户为 1、平台管理员为 1；可用候选俱乐部租户为 121、管理员为 110。但因为没有连接真实本地数据库，最终 ID 必须以脚本执行后的验证查询为准。

## 5. 预期新增数量

在迁移完整且前置数据满足时：

| 表 | 预期新增 |
|---|---:|
| system_tenant | 2（容量、停用测试租户） |
| member_user | 4 |
| delta_club_application | 3 |
| delta_club_profile | 3 |
| delta_club_service_scope | 8 |
| delta_product_service_config | 0–3（优先复用） |
| trade_order | 10 |
| trade_order_item | 10 |
| delta_service_order | 10 |
| delta_order_market_listing | 10 |
| delta_order_market_log | 14 |
| delta_event_outbox | 0 |
| delta_member_notification | 0 |
| delta_order_assignment | 0 |

没有执行 SQL，以上为设计数量而非数据库实测结果。

## 6. 测试场景和挂牌编号

可正常抢单：

- `TEST_MARKET_AVAILABLE_01`：serviceType=1
- `TEST_MARKET_AVAILABLE_02`：serviceType=2
- `TEST_MARKET_AVAILABLE_03`：serviceType=1

目标俱乐部已接：

- `TEST_MARKET_CLAIMED_01`
- `TEST_MARKET_CLAIMED_02`

已过期：`TEST_MARKET_EXPIRED_01`。

服务范围不匹配：`TEST_MARKET_SCOPE_MISMATCH`，serviceType=3；目标俱乐部仅配置 1、2。因为有效服务类型只有 1、2、3，要同时覆盖三种可抢单并为同一俱乐部制造合法不匹配在逻辑上不可同时满足，因此三条正常可抢数据覆盖 1、2，类型 3 专用于不匹配验证。

容量已满：

- `TEST_MARKET_CAPACITY_OCCUPIED` 已由容量俱乐部接单。
- 容量俱乐部 `max_concurrent_orders=1`。
- 对 `TEST_MARKET_CAPACITY_TARGET` 调用平台 assign 并指定容量俱乐部，应返回 `ORDER_MARKET_CLUB_CAPACITY_FULL`。

俱乐部停用：对 `TEST_MARKET_DISABLED_TARGET` 调用平台 assign 并指定 `business_status=0` 的停用俱乐部，应返回 `ORDER_MARKET_CLUB_DISABLED`。

## 7. 关联关系

每个挂牌依次关联：

`delta_order_market_listing.service_order_id` → `delta_service_order.id` → `trade_order_item.id` / `trade_order.id` → `product_sku.id` / `product_spu.id` / `member_user.id`。

服务订单始终保留平台/source tenantId。俱乐部归属只写入 `claimed_club_id` 和 `claimed_club_tenant_id`，没有修改服务订单 tenantId。

容量统计按真实 Service：统计目标 `claimed_club_tenant_id` 下 CLAIMED 挂牌，并排除服务单状态 80、90、100、110；不读取 `delta_order_assignment`。

## 8. 支付、日志和 Outbox

- 商城订单标记为已支付、状态 10，渠道使用真实枚举编码 `mock`，`pay_order_id=NULL`，不会形成真实待支付任务，也不会触发真实支付、退款或转账。
- 市场日志包含 10 条 PUBLISH、3 条 CLAIM、1 条 EXPIRE。
- SQL 直插不会经过 `DeltaOrderMarketService`，因此不会自动发布 Outbox。本脚本故意不伪造 Outbox 或已发送通知，避免定时消费者产生消息推送。
- 回滚脚本会先清理测试期间由真实市场接口产生、且 `biz_key` 对应 `TEST_MARKET_` 挂牌的站内通知和 Outbox，再删除市场日志与挂牌。
- 市场接单当前只更新挂牌归属，不创建 `delta_order_assignment`；接口文档也将“已接订单履约派单”列为后续阶段。

## 9. 页面与接口验证状态

SQL 末尾提供以下数据库等价查询：平台挂牌分页/详情、挂牌日志、俱乐部列表基础数据、服务订单、目标俱乐部可抢分页、我的已接订单、容量统计及孤儿检查。

未验证：没有连接数据库、没有启动 Java/Vue、没有实际调用任何接口或页面。

## 10. 发现的后端问题

1. 订单市场只有 Admin Controller；不存在 App 订单市场接口。
2. `DeltaOrderMarketListingRespVO` 只有 `claimedClubId` 和 `claimedClubTenantId`，没有俱乐部名称；“接单俱乐部展示”只能展示 ID，除非前端额外查俱乐部。
3. 当前快照缺少 Phase 9 俱乐部表、Phase 3 服务单字段以及挂牌活动唯一索引。
4. 可接订单分页只按启用状态、AVAILABLE、未过期和服务范围过滤，不预先过滤容量；容量错误只在 claim/assign 时返回，这是当前代码行为。
5. 俱乐部身份由当前 Admin tenantId 决定，不由 member owner 自动决定；容量和停用测试租户没有登录账号，只能由平台 assign 接口验证错误。
6. 直接 SQL 与真实 Service 的差异是没有 Redis 锁、CAS 动作过程和 Outbox；最终数据状态、市场日志和容量口径保持一致。

## 11. 执行文件

- 初始化：`sql/dev/delta_order_market_test_data.sql`
- 回滚：`sql/dev/delta_order_market_test_data_rollback.sql`

执行前顺序：切换为本地 profile → 确认 JDBC 为 127.0.0.1/localhost → 应用缺失正式迁移 → 备份本地库 → 将初始化 SQL 的 `@confirm_local_dev` 改为 1 → 执行并检查末尾验证查询。回滚同样需要手工确认安全锁。

## 12. 最终输出汇总

1. 实际使用的数据库名称：未连接、未使用任何数据库；当前启用配置指向公网 `admin_portal`，未启用的 local 配置指向 `ruoyi-vue-pro`。
2. 是否确认是开发环境：否，因此没有执行初始化或回滚 SQL。
3. 实际表名：见第 2 节，均来自 DO、Mapper 与建表 SQL 的交叉核对。
4. 新增数据数量：数据库实增 0；安全执行后的设计数量见第 5 节。
5. 测试账号或 member 用户：不创建可登录账号；复用动态选出的平台/目标租户后台管理员，并创建 4 个无手机号、无微信、无登录密码的 member 测试记录。
6. 平台 tenantId：执行时动态确定；快照候选值为 1，不作为脚本硬编码值。
7. 目标俱乐部 tenantId：执行时动态确定；快照候选值为 121，不作为脚本硬编码值。
8. 目标 clubId：由数据库生成，未执行前无实际值；末尾验证查询会输出。
9. 可抢挂牌编号：`TEST_MARKET_AVAILABLE_01`、`TEST_MARKET_AVAILABLE_02`、`TEST_MARKET_AVAILABLE_03`。
10. 已接挂牌编号：`TEST_MARKET_CLAIMED_01`、`TEST_MARKET_CLAIMED_02`。
11. 已过期挂牌编号：`TEST_MARKET_EXPIRED_01`。
12. 数据关联关系：见第 7 节；挂牌、服务单、商城订单项、订单、SKU/SPU、会员全部有真实关联。
13. 已验证的页面或接口：未做运行时页面/接口验证；只完成真实代码核对和对应 SQL 查询设计。
14. 未验证内容：真实数据库执行结果、接口鉴权、租户运行时上下文、Vue 页面展示和两项业务错误返回。
15. 发现的后端问题：见第 10 节，包括仅有 Admin API、VO 缺少俱乐部名称和迁移快照不完整。
16. 初始化 SQL 路径：`sql/dev/delta_order_market_test_data.sql`。
17. 回滚 SQL 路径：`sql/dev/delta_order_market_test_data_rollback.sql`。
