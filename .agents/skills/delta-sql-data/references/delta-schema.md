# Delta 数据库结构索引

来源：`1.sql`，快照时间 `30/06/2026 15:53:43`。

## 核心关联链

```text
delta_club_application ──审核通过──> system_tenant ── delta_club_profile ── delta_club_service_scope

system_tenant
├── member_user
│   ├── delta_worker_application
│   └── delta_worker ── delta_worker_skill
├── product_spu ── product_sku ── delta_product_service_config
├── trade_order ── trade_order_item
│   └── delta_service_order
│       ├── delta_order_assignment / progress / evidence / acceptance / rework / log
│       ├── delta_order_market_listing ── delta_order_market_log
│       ├── delta_worker_settlement
│       ├── delta_order_cancel
│       └── delta_after_sale ── arbitration
│           ├── delta_refund_record ── delta_refund_log
│           └── delta_fund_recovery ── delta_fund_recovery_log
└── delta_event_outbox ── delta_member_notification / delta_reminder_record
```

## 俱乐部入驻与平台管理（Phase 9）

### `delta_club_application`

俱乐部入驻申请表；当前 Skill 结构来源：用户补充的 Phase 9 DDL。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint | 否 |  |  |
| application_no | varchar(32) | 否 |  | 申请编号 (DCA + yyyyMMddHHmmss + 自增) |
| applicant_member_id | bigint | 否 |  | 申请会员用户ID (关联 member_user.id) |
| club_name | varchar(100) | 否 |  | 俱乐部名称 |
| contact_name | varchar(50) | 否 |  | 联系人姓名 |
| contact_mobile | varchar(20) | 否 |  | 联系人手机号 |
| contact_wechat | varchar(50) | 是 | '' | 联系人微信 |
| description | varchar(2000) | 是 | '' | 俱乐部描述 |
| logo_url | varchar(500) | 是 | '' | Logo URL |
| qualification_urls | varchar(3000) | 是 | '' | 资质凭证图片URL列表 (JSON数组) |
| application_status | tinyint | 否 | 0 | 申请状态：0-待审核 1-已通过 2-已拒绝 3-已撤销 |
| reject_reason | varchar(500) | 是 | '' | 拒绝原因 |
| auditor_id | bigint | 是 | NULL | 审核人ID (关联 admin_user.id) |
| audit_time | datetime | 是 | NULL | 审核时间 |
| approved_tenant_id | bigint | 是 | NULL | 审批通过后关联的租户ID |
| remark | varchar(500) | 是 | '' | 审核备注 |
| version | int | 否 | 0 | 乐观锁版本号 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_application_no (application_no)`
- `INDEX idx_applicant_member (applicant_member_id)`
- `INDEX idx_status (application_status)`
- `INDEX idx_auditor (auditor_id)`

### `delta_club_profile`

俱乐部档案表；当前 Skill 结构来源：用户补充的 Phase 9 DDL。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint | 否 |  |  |
| tenant_id | bigint | 否 | 0 | 租户ID |
| club_code | varchar(32) | 否 |  | 俱乐部编码（系统唯一） |
| club_name | varchar(100) | 否 |  | 俱乐部名称 |
| owner_member_id | bigint | 否 |  | 俱乐部所有者会员用户ID |
| contact_name | varchar(50) | 是 | '' | 联系人姓名 |
| contact_mobile | varchar(20) | 是 | '' | 联系人手机号 |
| contact_wechat | varchar(50) | 是 | '' | 联系人微信 |
| logo_url | varchar(500) | 是 | '' | Logo URL |
| description | varchar(2000) | 是 | '' | 俱乐部描述 |
| business_status | tinyint | 否 | 1 | 经营状态：0-停用 1-启用 |
| platform_commission_rate | int | 否 | 0 | 平台抽成比例（万分制，如500=5.00%） |
| max_concurrent_orders | int | 是 | 100 | 最大并发订单数 |
| application_id | bigint | 是 | NULL | 关联的入驻申请ID |
| remark | varchar(500) | 是 | '' | 备注 |
| version | int | 否 | 0 | 乐观锁版本号 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_tenant_id (tenant_id)`
- `UNIQUE KEY uk_club_code (club_code)`
- `UNIQUE KEY uk_application_id (application_id)`
- `INDEX idx_owner_member (owner_member_id)`
- `INDEX idx_business_status (business_status)`

### `delta_club_service_scope`

俱乐部服务范围表；当前 Skill 结构来源：用户补充的 Phase 9 DDL。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint | 否 |  |  |
| tenant_id | bigint | 否 | 0 | 租户ID |
| club_profile_id | bigint | 否 |  | 俱乐部档案ID (关联 delta_club_profile.id) |
| service_type | tinyint | 否 |  | 服务类型：1-陪玩 2-护航 3-趣味单 |
| enabled | tinyint(1) | 否 | 1 | 是否启用：0-禁用 1-启用 |
| remark | varchar(200) | 是 | '' | 备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_tenant_profile_type (tenant_id, club_profile_id, service_type, deleted)`
- `INDEX idx_club_profile (club_profile_id)`

**业务规则：**

- `service_type`：1-陪玩、2-护航、3-趣味单。
- `platform_commission_rate` 为万分制，1500 表示 15.00%。
- `delta_club_application` 不属于租户数据，申请发生在租户创建前。
- `delta_club_profile` 与 `delta_club_service_scope` 属于租户数据。
- 审核通过后，申请、租户、俱乐部档案之间必须形成一致关联。
- `delta_club_profile.tenant_id`、`club_code`、`application_id` 均有唯一约束。
- `delta_club_service_scope` 的唯一键包含 `deleted`，生成数据时应避免同一租户、俱乐部、服务类型产生重复有效记录。

精确 DDL：`references/phase9-club-tables.sql`。

## 核心身份与配置

### `delta_worker_application`

打手申请表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| user_id | bigint(20) | 否 |  | 会员用户ID |
| real_name | varchar(32) | 否 |  | 真实姓名 |
| phone | varchar(20) | 否 |  | 手机号 |
| game_uid | varchar(64) | 是 | '' | 游戏账号UID |
| device_type | tinyint(4) | 否 |  | 设备类型：1-手机 2-平板 3-PC |
| introduction | varchar(500) | 是 | '' | 个人介绍 |
| experience | varchar(500) | 是 | '' | 打手经验描述 |
| evidence_urls | varchar(2000) | 是 | NULL | 凭证图片URL列表（JSON数组） |
| check_evidence_url | varchar(255) | 是 | '' | 审核凭证图片URL |
| application_status | tinyint(4) | 否 | 0 | 申请状态：0-待审核 1-审核通过 2-审核驳回 |
| reject_reason | varchar(255) | 是 | '' | 驳回原因 |
| reviewer_id | bigint(20) | 是 | NULL | 审核人ID（关联 admin_user.id） |
| reviewed_at | datetime | 是 | NULL | 审核时间 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_user_status(tenant_id, user_id, application_status) USING BTREE`
- `INDEX idx_tenant_status(tenant_id, application_status) USING BTREE`

### `delta_worker`

打手资料表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| user_id | bigint(20) | 否 |  | 会员用户ID（关联 member_user.id） |
| worker_no | varchar(32) | 否 |  | 打手编号 |
| real_name | varchar(32) | 是 | '' | 真实姓名 |
| display_name | varchar(32) | 是 | '' | 展示名称 |
| phone | varchar(20) | 是 | '' | 手机号 |
| avatar | varchar(255) | 是 | '' | 头像URL |
| audit_status | tinyint(4) | 否 | 0 | 审核状态：0-未申请 1-审核中 2-审核通过 3-审核驳回 4-已停用 5-已拉黑 |
| work_status | tinyint(4) | 否 | 0 | 工作状态：0-离线 1-在线 2-忙碌 3-暂停接单 |
| level | tinyint(4) | 否 | 1 | 打手等级：1-初级 2-中级 3-高级 4-资深 |
| score | int(11) | 否 | 0 | 评分（万分制，如49500表示4.95分） |
| commission_rate | int(11) | 是 | 0 | 抽成比例（万分制，如1500表示15.00%） |
| max_order_count | int(11) | 否 | 5 | 最大同时接单数 |
| current_order_count | int(11) | 否 | 0 | 当前进行中订单数 |
| completed_order_count | int(11) | 否 | 0 | 历史完成订单数 |
| cancel_order_count | int(11) | 否 | 0 | 取消订单数 |
| is_recommend | tinyint(1) | 否 | 0 | 是否推荐：0-否 1-是 |
| status | tinyint(4) | 否 | 0 | 状态：0-开启 1-关闭（对应 CommonStatusEnum） |
| audit_remark | varchar(255) | 是 | '' | 审核备注 |
| approved_at | datetime | 是 | NULL | 审核通过时间 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_user_id(tenant_id, user_id) USING BTREE`
- `UNIQUE INDEX uk_tenant_worker_no(tenant_id, worker_no) USING BTREE`
- `INDEX idx_audit_status(tenant_id, audit_status) USING BTREE`
- `INDEX idx_work_status(tenant_id, work_status) USING BTREE`

### `delta_worker_skill`

打手技能表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| worker_id | bigint(20) | 否 |  | 打手ID（关联 delta_worker.id） |
| device_type | tinyint(4) | 否 |  | 设备类型：1-手机 2-平板 3-PC |
| service_type | tinyint(4) | 否 |  | 服务类型：1-陪玩 2-护航 3-趣味单 |
| skill_level | tinyint(4) | 否 | 1 | 技能等级：1-初级 2-中级 3-高级 4-资深 |
| status | tinyint(4) | 否 | 0 | 状态：0-开启 1-关闭（对应 CommonStatusEnum） |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_worker_skill(tenant_id, worker_id, device_type, service_type) USING BTREE`
- `INDEX idx_tenant_worker(tenant_id, worker_id) USING BTREE`

### `delta_product_service_config`

商品服务配置表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| spu_id | bigint(20) | 否 |  | 商品SPU ID（关联 product_spu.id） |
| sku_id | bigint(20) | 否 |  | 商品SKU ID（关联 product_sku.id） |
| service_type | tinyint(4) | 否 |  | 服务类型：1-陪玩 2-护航 3-趣味单 |
| device_type | tinyint(4) | 否 |  | 设备类型：1-手机 2-平板 3-PC |
| required_worker_level | tinyint(4) | 否 | 1 | 要求打手等级：1-初级 2-中级 3-高级 4-资深 |
| allow_designated_worker | tinyint(1) | 否 | 0 | 是否允许指定打手：0-否 1-是 |
| allow_public_claim | tinyint(1) | 否 | 1 | 是否允许大厅抢单：0-否 1-是 |
| default_dispatch_mode | tinyint(4) | 否 | 3 | 默认派单方式：1-客户指定 2-客服派单 3-接单大厅 |
| max_service_hours | int(11) | 是 | NULL | 最大服务时长（小时） |
| commission_rate | int(11) | 否 | 0 | 抽成比例（万分制，如1500表示15.00%） |
| enabled | tinyint(1) | 否 | 1 | 是否启用：0-禁用 1-启用 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_sku_id(tenant_id, sku_id) USING BTREE`
- `INDEX idx_tenant_spu_id(tenant_id, spu_id) USING BTREE`

## 服务订单履约

### `delta_service_order`

服务履约订单表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| service_order_no | varchar(32) | 否 |  | 服务订单号 |
| trade_order_id | bigint(20) | 否 |  | 商城订单ID（关联 trade_order.id） |
| trade_order_no | varchar(32) | 否 |  | 商城订单号 |
| trade_order_item_id | bigint(20) | 否 |  | 商城订单项ID（关联 trade_order_item.id） |
| buyer_user_id | bigint(20) | 否 |  | 买家会员用户ID |
| spu_id | bigint(20) | 否 |  | 商品SPU ID |
| sku_id | bigint(20) | 否 |  | 商品SKU ID |
| product_name | varchar(128) | 否 |  | 商品名称（快照） |
| sku_name | varchar(128) | 是 | '' | SKU名称（快照） |
| service_type | tinyint(4) | 否 |  | 服务类型：1-陪玩 2-护航 3-趣味单 |
| device_type | tinyint(4) | 否 |  | 设备类型：1-手机 2-平板 3-PC |
| service_amount | int(11) | 否 |  | 服务金额（分，快照） |
| dispatch_mode | tinyint(4) | 否 |  | 派单方式：1-客户指定 2-客服派单 3-接单大厅 |
| preferred_worker_id | bigint(20) | 是 | NULL | 客户指定打手ID |
| assigned_worker_id | bigint(20) | 是 | NULL | 最终指派打手ID |
| status | tinyint(4) | 否 |  | 服务状态：10-待派单 20-等待指定打手确认 30-接单大厅待领取 40-已接单待开始 50-服务进行中 60-打手已提交完成 70-待客户或客服验收 80-已完成 90-售后处理中 100-纠纷处理中 110-已取消 |
| version | int(11) | 否 | 0 | 乐观锁版本号 |
| customer_remark | varchar(500) | 是 | '' | 客户备注 |
| admin_remark | varchar(500) | 是 | '' | 后台备注 |
| claim_deadline | datetime | 是 | NULL | 大厅接单截止时间 |
| accepted_at | datetime | 是 | NULL | 接单时间 |
| started_at | datetime | 是 | NULL | 服务开始时间 |
| submitted_at | datetime | 是 | NULL | 提交完成时间 |
| verified_at | datetime | 是 | NULL | 验收时间 |
| completed_at | datetime | 是 | NULL | 完成时间 |
| cancel_reason | varchar(255) | 是 | '' | 取消原因 |
| commission_rate | int(11) | 是 | 0 | 抽成比例（万分制，快照，如1500表示15.00%） |
| platform_fee | int(11) | 是 | 0 | 平台抽成金额（分） |
| worker_amount | int(11) | 是 | 0 | 打手收入金额（分） |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_service_order_no(tenant_id, service_order_no) USING BTREE`
- `UNIQUE INDEX uk_tenant_trade_order_item(tenant_id, trade_order_item_id) USING BTREE`
- `INDEX idx_tenant_buyer(tenant_id, buyer_user_id) USING BTREE`
- `INDEX idx_tenant_assigned_worker(tenant_id, assigned_worker_id) USING BTREE`
- `INDEX idx_tenant_status(tenant_id, status) USING BTREE`

### `delta_order_assignment`

派单记录表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| service_order_id | bigint(20) | 否 |  | 服务订单ID（关联 delta_service_order.id） |
| worker_id | bigint(20) | 否 |  | 打手ID（关联 delta_worker.id） |
| assignment_type | tinyint(4) | 否 |  | 派单类型：1-客户指定 2-客服派单 3-大厅抢单 4-改派 |
| assignment_status | tinyint(4) | 否 |  | 派单状态：1-待确认 2-已接受 3-已拒绝 4-已超时 |
| operator_type | tinyint(4) | 否 |  | 操作人类型：1-客户 2-客服 3-系统 |
| operator_id | bigint(20) | 否 |  | 操作人ID |
| reason | varchar(255) | 是 | '' | 操作原因/备注 |
| expired_at | datetime | 是 | NULL | 过期时间 |
| accepted_at | datetime | 是 | NULL | 接受/拒绝时间 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id) USING BTREE`
- `INDEX idx_tenant_worker(tenant_id, worker_id) USING BTREE`

### `delta_order_progress`

服务进度表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| worker_id | bigint(20) | 否 |  | 打手ID |
| progress_type | tinyint(4) | 否 |  | 进度类型：1-开始服务 2-进度更新 3-异常报告 |
| progress_percent | tinyint(4) | 否 | 0 | 进度百分比（0-100） |
| content | varchar(500) | 是 | '' | 进度内容 |
| image_urls | varchar(2000) | 是 | NULL | 进度图片URL列表（JSON数组） |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id, create_time) USING BTREE`

### `delta_order_evidence`

完成凭证表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| worker_id | bigint(20) | 否 |  | 打手ID |
| evidence_type | tinyint(4) | 否 |  | 凭证类型：1-截图 2-视频 3-文字说明 4-文件 |
| content | varchar(500) | 是 | '' | 凭证内容/说明 |
| image_urls | varchar(2000) | 是 | NULL | 凭证图片URL列表（JSON数组） |
| video_url | varchar(255) | 是 | '' | 凭证视频URL |
| review_status | tinyint(4) | 否 | 0 | 审核状态：0-待审核 1-审核通过 2-审核驳回 |
| review_remark | varchar(255) | 是 | '' | 审核备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id) USING BTREE`

### `delta_order_acceptance`

无表注释；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| service_order_id | bigint(20) | 否 |  |  |
| worker_id | bigint(20) | 是 | NULL |  |
| acceptance_result | tinyint(4) | 否 | 1 |  |
| operator_type | tinyint(4) | 否 |  |  |
| operator_id | bigint(20) | 是 | NULL |  |
| remark | varchar(512) | 是 | '' |  |
| before_status | int(11) | 是 | NULL |  |
| after_status | int(11) | 是 | NULL |  |
| acceptance_time | datetime | 是 | NULL |  |
| creator | varchar(64) | 是 | '' |  |
| create_time | datetime | 否 | CURRENT_TIMESTAMP |  |
| updater | varchar(64) | 是 | '' |  |
| update_time | datetime | 否 | CURRENT_TIMESTAMP |  |
| deleted | tinyint(4) | 否 | 0 |  |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_service_order(service_order_id) USING BTREE`

### `delta_order_rework`

无表注释；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| service_order_id | bigint(20) | 否 |  |  |
| worker_id | bigint(20) | 是 | NULL |  |
| rework_no | int(11) | 否 | 1 |  |
| reason | varchar(512) | 否 |  |  |
| operator_type | tinyint(4) | 否 |  |  |
| operator_id | bigint(20) | 是 | NULL |  |
| before_status | int(11) | 是 | NULL |  |
| after_status | int(11) | 是 | NULL |  |
| creator | varchar(64) | 是 | '' |  |
| create_time | datetime | 否 | CURRENT_TIMESTAMP |  |
| updater | varchar(64) | 是 | '' |  |
| update_time | datetime | 否 | CURRENT_TIMESTAMP |  |
| deleted | tinyint(4) | 否 | 0 |  |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_service_order(service_order_id) USING BTREE`

### `delta_order_log`

订单日志表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| operator_type | tinyint(4) | 否 |  | 操作人类型：1-客户 2-打手 3-客服 4-系统 |
| operator_id | bigint(20) | 否 |  | 操作人ID |
| operation | varchar(64) | 否 |  | 操作名称 |
| before_status | tinyint(4) | 是 | NULL | 操作前状态 |
| after_status | tinyint(4) | 是 | NULL | 操作后状态 |
| content | varchar(500) | 是 | '' | 日志内容 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_service_order_time(tenant_id, service_order_id, create_time) USING BTREE`

## 订单市场

### `delta_order_market_listing`

平台订单市场挂牌表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| listing_no | varchar(32) | 否 |  | 挂牌编号 (DML + yyyyMMddHHmmss + 自增) |
| service_order_id | bigint(20) | 否 |  | 服务订单ID (关联 delta_service_order.id) |
| service_order_no | varchar(32) | 否 |  | 服务订单号 |
| source_tenant_id | bigint(20) | 否 |  | 订单来源租户ID |
| service_type | tinyint(4) | 否 |  | 服务类型：1-陪玩 2-护航 3-趣味单 |
| service_amount | int(11) | 否 |  | 服务金额（单位：分） |
| requirement_summary | varchar(2000) | 是 | '' | 需求摘要（脱敏后） |
| listing_status | tinyint(4) | 否 | 0 | 挂牌状态：0-可抢 1-已被接单 2-已撤回 3-已过期 4-已关闭 |
| publish_time | datetime | 否 |  | 发布时间 |
| expire_time | datetime | 是 | NULL | 过期时间 |
| claimed_club_id | bigint(20) | 是 | NULL | 接单俱乐部档案ID (关联 delta_club_profile.id) |
| claimed_club_tenant_id | bigint(20) | 是 | NULL | 接单俱乐部租户ID |
| claim_time | datetime | 是 | NULL | 接单时间 |
| publisher_id | bigint(20) | 否 |  | 发布人ID (关联 admin_user.id) |
| withdraw_reason | varchar(500) | 是 | '' | 撤回原因 |
| remark | varchar(500) | 是 | '' | 备注 |
| version | int(11) | 否 | 0 | 乐观锁版本号 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |
| active_flag | tinyint(4) | 是 | NULL | 活动标记：1-有效 0-无效，与service_order_id组成唯一约束防止并发创建多个有效挂牌 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_listing_no(listing_no) USING BTREE`
- `INDEX idx_service_order_id(service_order_id) USING BTREE`
- `INDEX idx_listing_status(listing_status) USING BTREE`
- `INDEX idx_claimed_club(claimed_club_id) USING BTREE`
- `INDEX idx_claimed_club_tenant(claimed_club_tenant_id) USING BTREE`
- `INDEX idx_publish_time(publish_time) USING BTREE`
- `INDEX idx_expire_time(expire_time) USING BTREE`

### `delta_order_market_log`

市场操作日志表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| listing_id | bigint(20) | 否 |  | 挂牌ID (关联 delta_order_market_listing.id) |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| operation_type | varchar(32) | 否 |  | 操作类型：PUBLISH/CLAIM/ASSIGN/WITHDRAW/EXPIRE/CLOSE/CLAIM_FAILED |
| operator_type | varchar(20) | 否 |  | 操作方类型：PLATFORM/CLUB/SYSTEM |
| operator_id | bigint(20) | 是 | NULL | 操作人ID |
| club_id | bigint(20) | 是 | NULL | 操作俱乐部ID |
| club_tenant_id | bigint(20) | 是 | NULL | 操作俱乐部租户ID |
| before_status | tinyint(4) | 是 | NULL | 操作前状态 |
| after_status | tinyint(4) | 是 | NULL | 操作后状态 |
| success | tinyint(1) | 否 | 1 | 是否成功：0-失败 1-成功 |
| failure_reason | varchar(500) | 是 | '' | 失败原因 |
| remark | varchar(500) | 是 | '' | 备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_listing_id(listing_id) USING BTREE`
- `INDEX idx_service_order_id(service_order_id) USING BTREE`
- `INDEX idx_operation_type(operation_type) USING BTREE`

## 取消、售后、退款、追回

### `delta_order_cancel`

取消申请表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| cancel_no | varchar(32) | 否 |  | 取消单号 |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| buyer_user_id | bigint(20) | 是 | NULL | 买家用户ID |
| worker_id | bigint(20) | 是 | NULL | 打手ID |
| apply_reason | varchar(512) | 是 | '' | 申请原因 |
| apply_remark | varchar(512) | 是 | '' | 申请备注 |
| apply_status | tinyint(4) | 否 | 0 | 取消状态：0-待审核 1-已通过 2-已驳回 |
| original_order_status | int(11) | 是 | NULL | 原始服务单状态（快照） |
| cancel_type | tinyint(4) | 是 | 1 | 取消类型：1-买家取消 |
| refund_amount | int(11) | 是 | 0 | 退款金额（分） |
| responsibility_type | tinyint(4) | 是 | NULL | 责任归属 |
| reviewer_id | bigint(20) | 是 | NULL | 审核人ID |
| review_time | datetime | 是 | NULL | 审核时间 |
| review_remark | varchar(512) | 是 | '' | 审核备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_cancel_no(tenant_id, cancel_no) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id) USING BTREE`
- `INDEX idx_tenant_buyer(tenant_id, buyer_user_id) USING BTREE`
- `INDEX idx_tenant_status(tenant_id, apply_status) USING BTREE`

### `delta_after_sale`

售后案件表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| after_sale_no | varchar(32) | 否 |  | 售后单号 |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| buyer_user_id | bigint(20) | 是 | NULL | 买家用户ID |
| worker_id | bigint(20) | 是 | NULL | 打手ID |
| after_sale_type | tinyint(4) | 是 | NULL | 售后类型 |
| reason_type | tinyint(4) | 是 | NULL | 原因类型 |
| reason | varchar(512) | 是 | '' | 申请原因 |
| description | varchar(1024) | 是 | '' | 问题描述 |
| evidence_urls | varchar(2048) | 是 | '' | 凭证图片URLs（JSON数组） |
| status | tinyint(4) | 否 | 0 | 售后状态：0-待处理 1-已受理 2-已驳回 3-已仲裁 4-已关闭 |
| original_order_status | int(11) | 是 | NULL | 原始服务单状态（快照） |
| requested_refund_amount | int(11) | 是 | 0 | 请求退款金额（分） |
| approved_refund_amount | int(11) | 是 | 0 | 批准退款金额（分） |
| responsibility_type | tinyint(4) | 是 | NULL | 责任归属 |
| handler_id | bigint(20) | 是 | NULL | 处理人ID |
| handle_time | datetime | 是 | NULL | 处理时间 |
| handle_remark | varchar(512) | 是 | '' | 处理备注 |
| need_manual_recovery | tinyint(4) | 是 | 0 | 需要人工追回（已打款结算时标记） |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_after_sale_no(tenant_id, after_sale_no) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id) USING BTREE`
- `INDEX idx_tenant_buyer(tenant_id, buyer_user_id) USING BTREE`
- `INDEX idx_tenant_status(tenant_id, status) USING BTREE`

### `delta_after_sale_arbitration`

仲裁记录表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| after_sale_id | bigint(20) | 否 |  | 售后案件ID |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| decision_type | tinyint(4) | 否 |  | 仲裁决定类型：0-不退款 1-全额退款 2-部分退款 3-继续服务 |
| refund_amount | int(11) | 是 | 0 | 退款金额（分） |
| worker_deduction_amount | int(11) | 是 | 0 | 打手扣减金额（分） |
| platform_bear_amount | int(11) | 是 | 0 | 平台承担金额（分） |
| responsibility_type | tinyint(4) | 是 | NULL | 责任归属 |
| operator_id | bigint(20) | 是 | NULL | 操作人ID |
| remark | varchar(512) | 是 | '' | 仲裁备注 |
| before_status | int(11) | 是 | NULL | 仲裁前状态 |
| after_status | int(11) | 是 | NULL | 仲裁后状态 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_after_sale(tenant_id, after_sale_id) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id) USING BTREE`

### `delta_refund_record`

内部退款记录表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| refund_no | varchar(32) | 否 |  | 退款单号 |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| after_sale_id | bigint(20) | 是 | NULL | 售后案件ID |
| buyer_user_id | bigint(20) | 是 | NULL | 买家用户ID |
| refund_amount | int(11) | 否 |  | 退款金额（分） |
| refund_reason | varchar(512) | 是 | '' | 退款原因 |
| refund_status | tinyint(4) | 否 | 0 | 退款状态：0-待人工退款 1-人工退款处理中 2-人工退款已完成 3-已取消 |
| refund_channel | varchar(32) | 是 | '' | 退款渠道 |
| external_refund_no | varchar(64) | 是 | '' | 外部退款流水号 |
| operator_id | bigint(20) | 是 | NULL | 操作人ID |
| remark | varchar(512) | 是 | '' | 备注 |
| handler_id | bigint(20) | 是 | NULL | 处理人ID |
| handle_time | datetime | 是 | NULL | 开始处理时间 |
| completed_time | datetime | 是 | NULL | 退款完成时间 |
| failed_time | datetime | 是 | NULL | 退款失败时间 |
| refund_method | tinyint(4) | 是 | NULL | 人工退款方式：1-人工微信 2-银行卡 3-支付宝 4-其他 |
| external_reference | varchar(128) | 是 | '' | 外部参考号（人工转账流水号） |
| failure_reason | varchar(512) | 是 | '' | 退款失败原因 |
| proof_urls | varchar(1024) | 是 | '' | 凭证URL列表（逗号分隔） |
| process_remark | varchar(512) | 是 | '' | 处理备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_refund_no(tenant_id, refund_no) USING BTREE`
- `INDEX idx_tenant_after_sale(tenant_id, after_sale_id) USING BTREE`
- `INDEX idx_tenant_service_order(tenant_id, service_order_id) USING BTREE`

### `delta_refund_log`

退款操作日志；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| refund_record_id | bigint(20) | 否 |  | 退款记录ID |
| service_order_id | bigint(20) | 是 | NULL | 服务订单ID |
| after_sale_id | bigint(20) | 是 | NULL | 售后案件ID |
| operation_type | varchar(32) | 否 |  | 操作类型：CREATE/START/COMPLETE/FAIL/RETRY/CANCEL |
| before_status | tinyint(4) | 是 | NULL | 操作前状态 |
| after_status | tinyint(4) | 是 | NULL | 操作后状态 |
| operator_type | varchar(16) | 是 | 'ADMIN' | 操作人类型 |
| operator_id | bigint(20) | 是 | NULL | 操作人ID |
| content | varchar(1024) | 是 | '' | 日志内容 |
| amount_snapshot | int(11) | 是 | 0 | 金额快照（分） |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_refund_record(tenant_id, refund_record_id) USING BTREE`
- `INDEX idx_tenant_create_time(tenant_id, create_time) USING BTREE`

### `delta_fund_recovery`

人工追回任务；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| recovery_no | varchar(32) | 否 |  | 追回单号 |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| after_sale_id | bigint(20) | 是 | NULL | 售后案件ID |
| arbitration_id | bigint(20) | 是 | NULL | 仲裁记录ID |
| settlement_id | bigint(20) | 是 | NULL | 结算记录ID |
| worker_id | bigint(20) | 是 | NULL | 打手ID |
| responsibility_type | tinyint(4) | 是 | NULL | 责任归属 |
| should_recover_amount | int(11) | 否 | 0 | 应追回金额（分） |
| recovered_amount | int(11) | 否 | 0 | 已追回金额（分） |
| remaining_amount | int(11) | 否 | 0 | 剩余追回金额（分） |
| recovery_status | tinyint(4) | 否 | 0 | 追回状态：0-待处理 1-处理中 2-部分追回 3-已全部追回 4-追回失败 5-已取消 |
| recovery_method | tinyint(4) | 是 | NULL | 追回方式 |
| external_reference | varchar(128) | 是 | '' | 外部参考号 |
| proof_urls | varchar(1024) | 是 | '' | 凭证URL列表（逗号分隔） |
| handler_id | bigint(20) | 是 | NULL | 处理人ID |
| handle_time | datetime | 是 | NULL | 开始处理时间 |
| completed_time | datetime | 是 | NULL | 追回完成时间 |
| failure_reason | varchar(512) | 是 | '' | 追回失败原因 |
| remark | varchar(512) | 是 | '' | 备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_recovery_no(tenant_id, recovery_no) USING BTREE`
- `UNIQUE INDEX uk_tenant_arbitration(tenant_id, arbitration_id) USING BTREE`
- `INDEX idx_tenant_settlement(tenant_id, settlement_id) USING BTREE`
- `INDEX idx_tenant_worker(tenant_id, worker_id) USING BTREE`
- `INDEX idx_tenant_recovery_status(tenant_id, recovery_status) USING BTREE`
- `INDEX idx_tenant_after_sale(tenant_id, after_sale_id) USING BTREE`

### `delta_fund_recovery_log`

追回操作日志；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  |  |
| tenant_id | bigint(20) | 否 | 0 |  |
| recovery_id | bigint(20) | 否 |  | 追回任务ID |
| service_order_id | bigint(20) | 是 | NULL | 服务订单ID |
| settlement_id | bigint(20) | 是 | NULL | 结算记录ID |
| operation_type | varchar(32) | 否 |  | 操作类型：GENERATE/START/RECORD_PARTIAL/RECORD_COMPLETE/FAIL/RETRY/CANCEL |
| before_status | tinyint(4) | 是 | NULL | 操作前状态 |
| after_status | tinyint(4) | 是 | NULL | 操作后状态 |
| operator_id | bigint(20) | 是 | NULL | 操作人ID |
| content | varchar(1024) | 是 | '' | 日志内容 |
| amount | int(11) | 是 | 0 | 本次追回金额（分） |
| total_recovered_amount | int(11) | 是 | 0 | 累计已追回金额（分） |
| remaining_amount | int(11) | 是 | 0 | 剩余金额（分） |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_recovery(tenant_id, recovery_id) USING BTREE`
- `INDEX idx_tenant_create_time(tenant_id, create_time) USING BTREE`

## 结算与财务

### `delta_worker_settlement`

打手结算表；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 编号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| settlement_no | varchar(32) | 否 |  | 结算单号 |
| service_order_id | bigint(20) | 否 |  | 服务订单ID |
| worker_id | bigint(20) | 否 |  | 打手ID |
| service_amount | int(11) | 否 |  | 服务金额（分） |
| commission_rate | int(11) | 否 |  | 抽成比例（万分制，快照） |
| platform_fee | int(11) | 否 | 0 | 平台抽成金额（分） |
| worker_amount | int(11) | 否 |  | 打手收入金额（分） |
| settlement_status | tinyint(4) | 否 | 0 | 结算状态：0-待结算 1-已结算 2-已冻结 3-已取消 |
| settled_at | datetime | 是 | NULL | 结算时间 |
| pay_channel | varchar(32) | 是 | '' | 打款渠道 |
| pay_reference | varchar(64) | 是 | '' | 打款流水号 |
| remark | varchar(255) | 是 | '' | 备注 |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(1) | 否 | 0 | 是否删除 0-否 1-是 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_settlement_no(tenant_id, settlement_no) USING BTREE`
- `UNIQUE INDEX uk_tenant_service_order(tenant_id, service_order_id) USING BTREE`
- `INDEX idx_tenant_worker(tenant_id, worker_id) USING BTREE`
- `INDEX idx_tenant_status(tenant_id, settlement_status) USING BTREE`

### `delta_finance_reconciliation`

财务对账记录；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 主键 |
| reconciliation_no | varchar(64) | 否 |  | 对账单号 |
| reconciliation_date | date | 否 |  | 对账日期 |
| period_start_time | datetime | 否 |  | 对账周期开始 |
| period_end_time | datetime | 否 |  | 对账周期结束 |
| service_order_count | int(11) | 是 | 0 | 服务订单数 |
| service_order_amount | bigint(20) | 是 | 0 | 服务订单金额（分） |
| settlement_count | int(11) | 是 | 0 | 结算笔数 |
| settlement_amount | bigint(20) | 是 | 0 | 结算金额（分） |
| paid_settlement_amount | bigint(20) | 是 | 0 | 已打款金额（分） |
| refund_count | int(11) | 是 | 0 | 退款笔数 |
| refund_amount | bigint(20) | 是 | 0 | 退款金额（分） |
| recovery_count | int(11) | 是 | 0 | 追回笔数 |
| should_recover_amount | bigint(20) | 是 | 0 | 应追回金额（分） |
| recovered_amount | bigint(20) | 是 | 0 | 已追回金额（分） |
| expected_platform_amount | bigint(20) | 是 | 0 | 预期平台收入（分） |
| actual_platform_amount | bigint(20) | 是 | 0 | 实际平台收入（分） |
| difference_amount | bigint(20) | 是 | 0 | 差异金额（分） |
| status | tinyint(4) | 否 | 0 | 状态：0-待计算 1-对账一致 2-存在差异 3-已确认 4-计算失败 5-已取消 |
| failure_reason | varchar(1024) | 是 | NULL | 失败原因 |
| confirm_remark | varchar(512) | 是 | NULL | 确认备注 |
| confirmer_id | bigint(20) | 是 | NULL | 确认人ID |
| confirmed_time | datetime | 是 | NULL | 确认时间 |
| calculate_time | datetime | 是 | NULL | 计算完成时间 |
| version | int(11) | 否 | 0 | 乐观锁版本号 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | bit(1) | 否 | b'0' | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_reconciliation_date(tenant_id, reconciliation_date, deleted) USING BTREE`
- `INDEX idx_reconciliation_no(reconciliation_no) USING BTREE`
- `INDEX idx_status(status) USING BTREE`
- `INDEX idx_create_time(create_time) USING BTREE`

## 事件、通知与提醒

### `delta_event_outbox`

Delta 领域事件 Outbox；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 主键 |
| event_no | varchar(64) | 否 |  | 事件编号 |
| event_type | varchar(64) | 否 |  | 事件类型（DeltaEventTypeEnum.type） |
| aggregate_type | varchar(32) | 是 | NULL | 聚合类型（SERVICE_ORDER/SETTLEMENT/REFUND等） |
| aggregate_id | bigint(20) | 是 | NULL | 聚合根ID |
| biz_key | varchar(256) | 否 |  | 幂等业务键 |
| recipient_type | varchar(16) | 是 | NULL | 接收人类型（BUYER/WORKER/ADMIN/SYSTEM） |
| recipient_id | bigint(20) | 是 | NULL | 接收人用户ID（member_user.id） |
| payload | text | 是 |  | 事件参数JSON |
| event_status | tinyint(4) | 否 | 0 | 事件状态：0待处理 1处理中 2成功 3失败 4死亡 |
| retry_count | int(11) | 否 | 0 | 已重试次数 |
| next_retry_time | datetime | 是 | NULL | 下次重试时间 |
| last_error | varchar(500) | 是 | NULL | 最近错误摘要 |
| processed_time | datetime | 是 | NULL | 处理完成时间 |
| template_code | varchar(64) | 是 | NULL | 关联通知模板编码 |
| template_params | text | 是 |  | 模板渲染参数JSON |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_biz_key(tenant_id, biz_key) USING BTREE`
- `INDEX idx_tenant_status_retry(tenant_id, event_status, next_retry_time) USING BTREE`
- `INDEX idx_tenant_agg_type_id(tenant_id, aggregate_type, aggregate_id) USING BTREE`
- `INDEX idx_tenant_create_time(tenant_id, create_time) USING BTREE`

### `delta_member_notification`

Delta 会员站内通知；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 主键 |
| user_id | bigint(20) | 否 |  | 接收用户ID（member_user.id） |
| user_type | varchar(16) | 否 |  | 用户类型（BUYER/WORKER，接收时的角色） |
| notification_type | varchar(32) | 否 |  | 通知类型（SYSTEM/ORDER/SETTLEMENT/REFUND/REMINDER） |
| title | varchar(200) | 否 |  | 通知标题 |
| content | varchar(500) | 否 |  | 通知内容 |
| biz_type | varchar(32) | 是 | NULL | 业务类型（SERVICE_ORDER/SETTLEMENT/REFUND等） |
| biz_id | bigint(20) | 是 | NULL | 业务ID |
| read_status | tinyint(1) | 否 | 0 | 是否已读：0未读 1已读 |
| read_time | datetime | 是 | NULL | 已读时间 |
| outbox_event_id | bigint(20) | 是 | NULL | 关联的Outbox事件ID（幂等键） |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `INDEX idx_tenant_user_read(tenant_id, user_id, read_status, create_time) USING BTREE`
- `INDEX idx_tenant_biz(tenant_id, biz_type, biz_id) USING BTREE`
- `INDEX idx_tenant_type_time(tenant_id, notification_type, create_time) USING BTREE`
- `INDEX idx_outbox_event(outbox_event_id) USING BTREE`

### `delta_reminder_record`

Delta 提醒记录；快照数据：**0 行**。

| 字段 | 类型 | 可空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | bigint(20) | 否 |  | 主键 |
| reminder_type | varchar(32) | 否 |  | 提醒类型（DISPATCH_PENDING等） |
| biz_type | varchar(32) | 否 |  | 业务类型（SERVICE_ORDER/REFUND等） |
| biz_id | bigint(20) | 否 |  | 业务ID |
| recipient_id | bigint(20) | 是 | NULL | 接收人用户ID |
| recipient_type | varchar(16) | 是 | NULL | 接收人类型（BUYER/WORKER/ADMIN） |
| last_remind_time | datetime | 否 |  | 最后提醒时间 |
| remind_count | int(11) | 否 | 1 | 提醒次数 |
| tenant_id | bigint(20) | 否 | 0 | 租户ID |
| creator | varchar(64) | 是 | '' | 创建者 |
| create_time | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| updater | varchar(64) | 是 | '' | 更新者 |
| update_time | datetime | 否 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | tinyint(4) | 否 | 0 | 是否删除 |

**索引/约束：**

- `PRIMARY KEY (id) USING BTREE`
- `UNIQUE INDEX uk_tenant_reminder(tenant_id, reminder_type, biz_type, biz_id, recipient_id) USING BTREE`
- `INDEX idx_tenant_remind_time(tenant_id, last_remind_time) USING BTREE`
