# Delta Java 后端仓库规则

## 1. 仓库身份

仓库：

```text
kaixiong055-debug/delta-vanguard-admin
```

本仓库虽然名称包含 `admin`，但它是 **Java 后端 Maven 多模块工程**，不是 Vue3 管理后台仓库。

核心业务模块：

```text
yudao-module-delta/
```

主要技术栈：

```text
Java
Spring Boot 2.7.18
MyBatis-Plus
MySQL
Redis / Redisson
芋道多租户
Maven
```

## 2. 默认修改范围

除非任务明确扩大范围，只允许修改：

```text
yudao-module-delta/**
接口文档.md
AGENTS.md
.agents/skills/delta-java-backend/**
```

只有任务明确涉及 GitHub Actions 时，才允许修改：

```text
.github/workflows/delta-java-ci.yml
```

只有任务明确要求数据库变更时，才允许增加对应的增量 SQL。

禁止修改：

```text
sql/mysql1/**
UniApp 微信小程序代码
Vue3 管理后台代码
DIY JSON 协议和装修渲染器
与 Delta 任务无关的芋道模块
```

未明确要求时：

```text
不新增数据库表
不新增数据库字段
不修改数据库索引
不修改 Maven Java 版本
不进行无关重构
```

## 3. Java 与构建基线

GitHub Actions 使用：

```text
JDK 17
```

源码兼容级别仍为：

```text
根 pom.xml：Java 8
yudao-module-delta：source/target 9
```

因此：

1. JDK 17 只是 CI 运行环境，不代表可以使用 Java 17 语法。
2. Delta 代码必须保持 Java 9 及以下语法兼容。
3. 禁止使用 `var`、`record`、文本块、switch 表达式、模式匹配、sealed class 等 Java 10+ 语法。
4. 不得擅自统一根工程和 Delta 模块的 Java 版本。
5. 不得为了使用新语法而修改 `pom.xml` 编译级别。

## 4. 固定业务边界

必须遵守：

1. 老板和打手共用现有 `member_user` 微信登录体系。
2. 商城 `trade_order` 与 Delta 服务履约订单必须分离。
3. 不修改商城会员、商品、SKU、购物车、支付和原商城订单状态机。
4. 不创建第二套 Delta 履约订单。
5. 所有 Delta 数据必须遵守现有多租户机制。
6. App Controller 与 Admin Controller 必须分开核对。
7. Controller 内不得重复声明 `/app-api` 或 `/admin-api`。
8. 不信任客户端提交的身份或租户字段。

客户端不得用于决定身份的字段包括但不限于：

```text
tenantId
sourceTenantId
workerTenantId
claimedClubTenantId
clubId
ownerMemberId
memberUserId
workerId
operatorId
operatorType
```

请求中确实存在业务 `workerId` 时，例如俱乐部分派候选打手，仍必须由服务端校验该打手是否属于当前可信俱乐部，不能直接信任请求值。

## 5. 多租户和跨租户规则

### 5.1 普通订单

普通同租户订单中：

```text
服务订单租户 = 打手租户
```

查询和写入必须继续遵守当前租户过滤。

### 5.2 俱乐部跨租户订单

俱乐部订单中：

```text
服务订单租户 = sourceTenantId
打手租户 = claimedClubTenantId
```

以下数据写入服务订单来源租户：

```text
delta_service_order
delta_order_assignment
delta_order_progress
delta_order_evidence
delta_order_log
delta_order_acceptance
delta_order_rework
delta_worker_settlement
delta_worker_settlement_log
买家 Outbox
```

以下数据写入俱乐部租户：

```text
delta_worker
打手 Outbox
```

### 5.3 跨租户访问校验

跨租户访问不得只校验 `assignedWorkerId`。

必须联合校验：

```text
CLAIMED 挂牌存在
listing.serviceOrderId == order.id
listing.sourceTenantId == order.tenantId
listing.claimedClubId == 当前可信俱乐部.id
listing.claimedClubTenantId == 当前打手.tenantId
order.assignedWorkerId == 当前打手.id
order.dispatchMode == CLUB_ASSIGN
有效派单记录存在
assignment.workerId == 当前打手.id
assignment.assignmentType == CLUB_ASSIGN
assignment 状态有效
```

跨租户校验逻辑必须复用统一的 Access Service 或 Tenant Resolver，不得在多个 Service 中复制不同版本。

### 5.4 TenantUtils 使用规则

`TenantUtils.executeIgnore(...)` 只能用于必要且范围最小的可信查询，例如：

```text
定位跨租户订单
定位 CLAIMED 挂牌
查找来源租户
验证派单关系
```

禁止使用 `TenantUtils.executeIgnore(...)` 包裹完整业务写流程。

真正的业务写操作必须进入明确的：

```text
sourceTenantId 来源租户上下文
或
workerTenantId 俱乐部租户上下文
```

切换租户上下文后必须确保正常恢复，禁止污染后续线程执行。

## 6. 锁、CAS、事务和幂等

涉及以下业务时必须检查并发安全：

```text
抢单
派单
开始服务
提交完成
验收
返工
结算
退款
追回
Outbox 消费
超时任务
```

俱乐部跨租户履约订单的锁键必须使用：

```text
sourceTenantId + serviceOrderId
```

禁止使用：

```text
workerTenantId + serviceOrderId
当前请求租户 + serviceOrderId
claimedClubTenantId + serviceOrderId
```

必须优先复用现有：

```text
Redisson RLock
tryLock
try/finally
isHeldByCurrentThread
数据库 CAS
唯一约束
幂等 bizKey
@Transactional(rollbackFor = Exception.class)
```

禁止：

```text
捕获异常后静默返回成功
为通过测试而降低权限校验
关闭租户隔离
删除 CAS 条件
绕过状态机
重复发布 Outbox
```

## 7. 开工顺序

每次任务按照以下顺序执行：

1. 执行 `git status --short`，保护工作区已有改动。
2. 确认当前分支和实际 HEAD，不依赖提示词中的旧提交号。
3. 读取根目录 `AGENTS.md`。
4. 按任务读取 `接口文档.md` 的相关章节，不全量重写文档。
5. 从真实 Controller 开始追踪：

```text
Controller
→ ReqVO / RespVO
→ Service 接口
→ ServiceImpl / CoreService
→ DO
→ Mapper / Mapper XML
→ 枚举
→ 错误码
→ 测试
```

6. 文档与代码冲突时，以当前 Java 代码为准，并同步修正文档。
7. 修改前先列出预计修改文件和明确不修改的边界。
8. 发现前端缺口时只记录，不在 Java 仓库代替前端实现。
9. 发现数据库缺口时先报告，未明确授权不得新增表字段。
10. 不创建文档中存在但代码中不存在的接口。

## 8. 代码实现要求

1. Controller 只负责参数接收、登录身份取得和结果返回。
2. 复杂状态机、跨租户和事务逻辑放在 Service 或 CoreService。
3. 优先复用现有 Service、Mapper、枚举、错误码和测试 Fixture。
4. 不重复创建相同语义的错误码。
5. 不把 Admin 用户 ID、Member 用户 ID、Worker ID 混用。
6. 数据库分页必须在数据库层完成。
7. 禁止先查全部数据后在 Java 中过滤并手工分页。
8. 禁止分页查询产生 N+1。
9. 状态更新优先使用带旧状态条件的 CAS。
10. 删除凭证、进度或其他子资源时，必须同时校验资源、订单、租户和当前操作人归属。
11. 新增跨租户代码必须写必要中文注释，说明可信关系和租户边界。
12. 不进行与当前任务无关的全局格式化。

## 9. 测试要求

修改业务代码时应同步新增或调整测试代码。

测试至少根据任务覆盖：

```text
成功路径
身份不匹配
租户不匹配
状态不允许
并发 CAS
锁键
事务回滚
Outbox 租户
敏感字段限制
普通同租户兼容
跨租户可信校验
```

测试不能只验证方法被调用；涉及多租户时，还应验证调用发生在哪个租户上下文。

Codex 只负责修改测试代码，不在 Codex 环境实际执行 Maven。

## 10. Codex 允许执行的检查

Codex 可以执行：

```bash
git status --short
git diff --check
git diff --name-only
git diff
rg
find
```

允许使用必要的只读命令检查文件和代码。

## 11. Codex 禁止执行的操作

Codex 不执行 Maven 编译和测试。

禁止执行：

```bash
mvn compile
mvn test
mvn install
mvn package
./mvnw
任何 Maven 编译或测试命令
```

除非用户在当前任务中明确要求，否则 Codex 不得：

```text
commit
push
创建分支
创建 Pull Request
修改 Git 配置
删除用户已有改动
强制 reset
```

## 12. GitHub Actions 验证

Maven 编译和测试统一由 GitHub Actions 执行。

工作流：

```text
.github/workflows/delta-java-ci.yml
```

运行环境：

```text
JDK 17
```

编译和测试命令：

```bash
mvn -B -ntp -pl yudao-module-delta -am -DskipTests install
mvn -B -ntp -pl yudao-module-delta test
```

固定流程：

```text
Codex 修改代码
→ 执行静态检查
→ 用户提交到 GitHub
→ GitHub Actions 编译和测试
→ 检查 Job 日志
→ 检查 Surefire Artifact
→ 根据真实失败给出最小修复
```

GitHub Actions 未成功前，不得声称：

```text
编译通过
测试通过
完整完成
可以验收
```

可以表述为：

```text
代码修改已完成，等待 GitHub Actions 验证
```

## 13. 接口文档维护规则

只有代码真实存在后，才更新根目录：

```text
接口文档.md
```

接口文档必须记录：

```text
Admin 或 App 类型
Controller 内部路径
完整外部 URL
HTTP Method
ReqVO 字段
RespVO 字段
权限编码
登录身份来源
租户来源
状态流转
锁和 CAS
Outbox 租户
错误码
敏感信息限制
未实现能力
```

必须区分：

```text
Admin 外部前缀：/admin-api
App 外部前缀：/app-api
Controller 内部前缀：/delta
前端调用路径：/delta/**
```

禁止：

```text
把计划中的接口写成已实现
把 Admin API 当作 App API
把 App API 当作 Admin API
在前端路径重复写 /admin-api 或 /app-api
省略跨租户数据归属
省略尚未实现能力
```

## 14. SQL 与测试数据

涉及数据库结构、菜单、流程测试数据、初始化或回滚 SQL 时，优先读取：

```text
$delta-sql-data
```

该 Skill 中的数据库内容只是安全快照，不是实时数据库事实。

执行写入前必须：

1. 确认是本地或开发环境。
2. 对目标表做最小实时检查。
3. 核对 Java DO、Mapper、枚举和状态机。
4. 提供幂等初始化 SQL。
5. 提供对应回滚 SQL。
6. 不扫描和重放完整 SQL 转储。
7. 不修改 `sql/mysql1/**`。

## 15. 完成报告

完成后必须报告：

```text
范围：
实际修改文件：
真实接口契约：
身份来源：
租户归属：
状态流转：
锁、CAS 与事务：
Outbox：
测试代码：
接口文档：
静态检查：
GitHub Actions 建议命令：
未验证项：
跨仓库待办：
```

静态检查结果必须包含：

```bash
git diff --check
git status --short
git diff --name-only
```

最后明确说明：

```text
Codex 未执行 Maven；编译和测试交由 GitHub Actions 完成。
```
