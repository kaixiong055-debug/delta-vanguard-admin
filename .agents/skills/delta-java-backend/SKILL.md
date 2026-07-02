---
name: delta-java-backend
description: 用于 Delta 先锋俱乐部 Java 后端、yudao-module-delta、Controller、Service、VO、Mapper、枚举、错误码、多租户、并发、测试和接口文档任务。不要用于 Vue3 管理后台或 UniApp 前端任务。
---

# Delta Java 后端开发工作流

## 任务识别

只有任务明确涉及 Java 后端或 `yudao-module-delta` 时使用本 Skill。

开始前必须先读取：

```text
仓库根目录 AGENTS.md
references/project-map.md
接口文档.md 的相关章节
```

根目录 `AGENTS.md` 的规则优先级高于本 Skill。

## 第一步：确认仓库状态

先执行：

```bash
git status --short
```

确认：

```text
当前分支
当前 HEAD
工作区已有改动
本次允许修改的目录
```

不得覆盖、删除或重置用户已有改动。

不要依赖提示词中的旧提交号，必须以当前仓库 HEAD 为准。

## 第二步：建立真实契约

按以下顺序定位：

1. Admin 或 App Controller；
2. Controller 的 `@RequestMapping` 和方法路径；
3. ReqVO、RespVO；
4. Service 接口；
5. ServiceImpl 和 CoreService；
6. DO、Mapper、Mapper XML；
7. 状态枚举和业务错误码；
8. 现有单元测试和 Controller 测试；
9. `接口文档.md` 对应章节。

任何字段、URL、权限、状态、租户和身份规则都必须来自真实代码。

文档与代码冲突时：

```text
以当前 Java 代码为准
同步修正文档
在完成报告中说明差异
```

禁止创建只有文档存在、代码中不存在的接口。

## 第三步：检查业务边界

每次修改前确认：

```text
当前操作属于平台、俱乐部、买家还是打手
登录身份是 Admin 用户还是 Member 用户
Member 用户、Worker、Club 三种 ID 是否被正确区分
数据读取和写入属于哪个租户
是否发生跨租户访问
是否涉及商城交易订单
是否需要锁、CAS、唯一约束、事务或幂等
是否需要日志、Outbox、通知或超时任务
```

固定边界：

```text
共用 member_user
商城交易订单与 Delta 履约订单分离
不修改商城支付、商品、SKU、购物车和原订单状态机
不创建第二套履约订单
不信任客户端提交的租户和操作人身份
不修改 UniApp
不修改 Vue3 管理后台
不修改 sql/mysql1/**
```

## 第四步：检查多租户安全

跨租户业务必须明确区分：

```text
sourceTenantId：服务订单来源租户
workerTenantId：打手或俱乐部租户
```

忽略租户过滤只能用于最小范围的可信定位查询。

禁止使用：

```java
TenantUtils.executeIgnore(...)
```

包裹完整业务写流程。

业务写入必须进入明确租户上下文。

俱乐部跨租户订单必须联合校验：

```text
CLAIMED 挂牌
serviceOrderId
sourceTenantId
claimedClubId
claimedClubTenantId
assignedWorkerId
dispatchMode
有效 CLUB_ASSIGN 派单记录
```

禁止仅凭 `assignedWorkerId` 授权跨租户访问。

跨租户履约锁必须使用：

```text
sourceTenantId + serviceOrderId
```

## 第五步：最小实现

优先复用现有：

```text
Service
CoreService
MapperX
TenantUtils
Redisson 锁模式
CAS 更新模式
CommonResult
PageResult
状态枚举
错误码
测试基类
Fixture
Outbox 发布服务
```

要求：

1. Controller 保持轻量。
2. 跨租户和事务逻辑放在 Service/CoreService。
3. 数据库分页在数据库层完成。
4. 禁止 Java 全量过滤后手工分页。
5. 禁止 N+1。
6. 禁止吞掉异常。
7. 禁止为了通过测试降低权限或状态校验。
8. 禁止无关格式化和重构。
9. 未明确要求时不新增表字段。

## 第六步：Java 兼容性

GitHub Actions 使用 JDK 17，但源码兼容级别不是 Java 17：

```text
根工程：Java 8
Delta 模块：source/target 9
```

代码必须保持 Java 9 及以下语法兼容。

禁止使用：

```text
var
record
文本块
switch 表达式
模式匹配
sealed class
Java 10+ 专属语法
```

不得擅自修改 Maven Java 编译版本。

## 第七步：同步接口文档

只有代码真实变化后才更新：

```text
接口文档.md
```

至少记录：

```text
Admin/App 类型
完整 URL
Controller 内部路径
HTTP Method
ReqVO
RespVO
权限与登录要求
身份来源
租户归属
状态规则
锁与 CAS
Outbox 租户
错误码
敏感信息限制
未实现能力
```

不得把计划能力写成已实现。

## 第八步：测试代码

应同步新增或修改测试代码，重点覆盖：

```text
普通同租户兼容
跨租户访问
身份不匹配
租户不匹配
状态不允许
锁键
CAS
事务回滚
Outbox 租户
敏感信息
```

测试代码需要验证实际租户上下文，而不仅是方法调用次数。

## 第九步：静态检查

Codex 只执行：

```bash
git diff --check
git status --short
git diff --name-only
```

可以执行必要的：

```bash
git diff
rg
find
```

## 第十步：禁止 Maven 和 Git 写操作

Codex 不执行：

```bash
mvn compile
mvn test
mvn install
mvn package
./mvnw
```

编译和测试统一交给 GitHub Actions。

除非用户当前任务明确要求，否则不得：

```text
commit
push
创建分支
创建 PR
修改 Git 配置
```

## GitHub Actions 建议命令

完成后只报告建议由 GitHub Actions 执行：

```bash
mvn -B -ntp -pl yudao-module-delta -am -DskipTests install
mvn -B -ntp -pl yudao-module-delta test
```

不得在 Codex 环境实际运行。

## 输出格式

最终按以下格式汇报：

```text
范围：
当前 HEAD：
真实契约：
修改文件：
身份与租户：
状态与并发：
Outbox：
测试代码：
接口文档：
静态检查：
GitHub Actions 建议命令：
未验证：
前端需要同步：
```

最后必须明确写：

```text
Codex 未执行 Maven；编译和测试等待 GitHub Actions 验证。
```
