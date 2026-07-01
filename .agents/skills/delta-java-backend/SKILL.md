---
name: delta-java-backend
description: 用于 Delta 先锋俱乐部 Java 后端、yudao-module-delta、Controller、Service、VO、Mapper、枚举、错误码、多租户、并发、测试和接口文档任务。不要用于 Vue3 管理后台或 UniApp 前端任务。
---

# Delta Java 后端开发工作流

## 任务识别

只有任务明确涉及 Java 后端或 `yudao-module-delta` 时使用本 Skill。

先阅读仓库根目录 `AGENTS.md`，再按需阅读：

```text
references/project-map.md
接口文档.md
```

## 第一步：建立真实契约

按以下顺序定位：

1. Admin 或 App Controller；
2. Controller 的 `@RequestMapping` 和方法路径；
3. ReqVO、RespVO；
4. Service 接口和实现；
5. DO、Mapper、Mapper XML；
6. 状态枚举、业务错误码；
7. 现有单元测试与 Controller 测试。

任何字段、URL、权限和状态都必须来自真实代码。文档与代码冲突时，以代码为准并报告差异。

## 第二步：检查业务边界

每次修改前确认：

- 当前操作属于平台、俱乐部、买家还是打手；
- 登录身份来自 Admin 用户还是 Member 用户；
- 租户 ID 来自框架上下文还是可信数据库关系；
- 是否发生跨租户读取或写入；
- 是否涉及商城订单，但不能改变商城原状态机；
- 是否需要锁、CAS、唯一约束、幂等键或事务；
- 是否需要日志、Outbox、通知和定时任务。

## 第三步：最小改动

优先复用现有：

- Service 和 CoreService；
- MapperX 与查询风格；
- `TenantUtils`；
- Redisson 锁模式；
- CAS 更新模式；
- `CommonResult`、`PageResult`；
- 错误码与枚举；
- 测试基类和 Fixture 风格。

不要为绕过测试而降低校验、关闭租户拦截或捕获并吞掉异常。

## 第四步：同步契约

只有代码真实变化后才更新 `接口文档.md`。文档必须记录：

- 完整 Admin/App 前缀；
- 请求方法和 URL；
- ReqVO、RespVO 字段；
- 权限与登录要求；
- 身份来源；
- 状态规则；
- 错误码；
- 敏感信息限制。

## 第五步：验证

先运行受影响测试类，再运行：

```bash
mvn -pl yudao-module-delta -am test -DfailIfNoTests=false
mvn -pl yudao-module-delta -am compile -DskipTests
```

涉及抢单、审核、退款、结算、追回、Outbox 时，额外检查并发、重复请求和事务回滚场景。

## 输出格式

最终按以下格式汇报：

```text
范围：
真实契约：
修改：
验证：
未验证：
前端需要同步：
```
