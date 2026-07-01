# admin_portal 数据库快照概览

## 快照身份

- 来源文件：`1.sql`
- 数据库：`admin_portal`
- MySQL：`50744 (5.7.44-log)`
- 导出时间：`30/06/2026 15:53:43`
- 原始 `1.sql` 表数量：**158**
- 原始 `1.sql` Delta 表数量：**25**
- Skill 当前已知表数量：**161**
- Skill 当前已知 Delta 表数量：**28**
- 带 INSERT 数据的表：**57**

> 这是一次静态快照，不是实时数据库。后续新增数据时优先使用本 Skill，不要重新扫描整份转储；但执行写入前仍需对目标表做少量实时存在性和唯一键检查。

## 表前缀分布

| 前缀 | 表数量 | 快照 INSERT 数 |
| --- | --- | --- |
| system | 32 | 5395 |
| delta | 25 | 0 |
| promotion | 23 | 11 |
| trade | 16 | 0 |
| pay | 14 | 3 |
| QRTZ | 11 | 37 |
| infra | 11 | 7712 |
| member | 10 | 2 |
| product | 10 | 101 |
| yudao | 5 | 31 |
| market | 1 | 0 |

## 关键结论

1. 原始 `1.sql` 中的 **25 张 `delta_*` 表均为 0 行**；后补的 3 张俱乐部表只提供 DDL，实时数据量未知。
2. `trade_order` 与 `trade_order_item` 也是 0 行。
3. 已存在会员、钱包、商品、SKU，可作为开发测试数据的基础引用。
4. 快照中不存在任何 `delta_club_*` 建表语句，但 `delta_order_market_listing.claimed_club_id` 注释引用 `delta_club_profile.id`。
5. 因此，俱乐部入驻和俱乐部订单市场数据在写入前必须先确认最新数据库已经执行俱乐部相关迁移。
6. 原始转储含 `DROP TABLE`、密码哈希、OAuth Token、手机号、IP、访问日志等敏感或危险内容，禁止把整份 `1.sql` 当测试数据脚本执行。

## 快照中有数据的表

| 表 | INSERT 数 | 说明 |
| --- | --- | --- |
| QRTZ_CRON_TRIGGERS | 11 |  |
| QRTZ_JOB_DETAILS | 11 |  |
| QRTZ_LOCKS | 2 |  |
| QRTZ_SCHEDULER_STATE | 2 |  |
| QRTZ_TRIGGERS | 11 |  |
| infra_api_access_log | 7373 | API 访问日志表 |
| infra_api_error_log | 142 | 系统异常日志 |
| infra_config | 7 | 参数配置表 |
| infra_file | 161 | 文件表 |
| infra_file_config | 11 | 文件配置表 |
| infra_job | 18 | 定时任务表 |
| member_user | 2 | 会员表 |
| pay_app | 1 | 支付应用 |
| pay_wallet | 2 | 会员钱包 |
| product_brand | 1 | 商品品牌表 |
| product_browse_history | 10 | 商品浏览记录 |
| product_category | 8 | 商品分类表 |
| product_property | 9 | 商品规格名称表 |
| product_property_value | 43 | 商品规格值表 |
| product_sku | 22 | 商品SKU表 |
| product_spu | 8 | 商品SPU表 |
| promotion_diy_page | 4 | 装修页面 |
| promotion_diy_template | 1 | 装修模板 |
| promotion_kefu_conversation | 1 | 客服会话 |
| promotion_kefu_message | 5 | 客服消息 |
| system_dept | 16 | 部门表 |
| system_dict_data | 1014 | 字典数据表 |
| system_dict_type | 208 | 字典类型表 |
| system_login_log | 17 | 系统访问记录 |
| system_mail_account | 4 | 邮箱账号表 |
| system_mail_template | 3 | 邮件模版表 |
| system_menu | 1490 | 菜单权限表 |
| system_notice | 3 | 通知公告表 |
| system_notify_message | 9 | 站内信消息表 |
| system_oauth2_access_token | 172 | OAuth2 访问令牌 |
| system_oauth2_client | 4 | OAuth2 客户端表 |
| system_oauth2_refresh_token | 14 | OAuth2 刷新令牌 |
| system_post | 4 | 岗位信息表 |
| system_role | 6 | 角色信息表 |
| system_role_menu | 2340 | 角色和菜单关联表 |
| system_sms_channel | 3 | 短信渠道 |
| system_sms_code | 2 | 手机验证码 |
| system_sms_log | 2 | 短信日志 |
| system_sms_template | 15 | 短信模板 |
| system_social_client | 9 | 社交客户端表 |
| system_social_user | 2 | 社交用户表 |
| system_social_user_bind | 2 | 社交绑定表 |
| system_tenant | 3 | 租户表 |
| system_tenant_package | 1 | 租户套餐表 |
| system_user_post | 11 | 用户岗位表 |
| system_user_role | 21 | 用户和角色关联表 |
| system_users | 20 | 用户信息表 |
| yudao_demo01_contact | 1 | 示例联系人表 |
| yudao_demo02_category | 7 | 示例分类表 |
| yudao_demo03_course | 17 | 学生课程表 |
| yudao_demo03_grade | 3 | 学生班级表 |
| yudao_demo03_student | 3 | 学生表 |
