# Delta Java 后端仓库规则

## 仓库身份

本仓库名为 `delta-vanguard-admin`，但它是 **Java 后端 Maven 多模块工程**，不是 Vue 管理后台。

核心 Delta 模块：

```text
yudao-module-delta/
```

根工程还包含 system、infra、member、pay、mall 等芋道模块。默认只修改 Delta 模块。

## 默认允许范围

除非任务明确扩大范围，只允许修改：

```text
yudao-module-delta/**
接口文档.md
与本次 Delta 功能直接对应的增量 SQL
```

不要修改 Vue3 管理后台或 UniApp 仓库。

## 固定业务边界

- 共用 `member_user` 会员体系。
- 商城 `trade_order` 与 Delta 服务履约订单必须分离。
- 不重写商城支付、商品、SKU、购物车或原订单状态机。
- 所有租户数据必须遵守芋道多租户隔离。
- 不信任前端提交的 `tenantId`、`clubId`、`operatorId`。
- App 与 Admin Controller 必须分开核对，不能把 Admin API 当 App API。
- 未运行 Maven 与关键并发验证时，不得宣称后端完整验收通过。

## 开工顺序

1. 先执行 `git status --short`，保护已有改动。
2. 读取根目录 `接口文档.md`，但以当前 Java Controller、ReqVO、RespVO、Service、DO、Mapper 为最终事实。
3. 从 Controller 路径向下追踪 Service、DO、Mapper、枚举、错误码和测试。
4. 先列出将修改的文件和不修改的边界，再开始编码。
5. 不创建文档中存在但代码中不存在的接口。
6. 发现跨仓库缺口时只记录，不在本仓库代替前端实现。

## 验证

优先运行定向测试，再运行模块验证：

```bash
mvn -pl yudao-module-delta -am test -DfailIfNoTests=false
mvn -pl yudao-module-delta -am compile -DskipTests
```

如果环境、依赖或时间不足导致未运行，必须明确写“未验证”，不得用代码阅读代替真实结果。

## 完成报告

必须列出：

- 修改文件；
- 新增或变更接口；
- 请求与响应字段；
- 租户和登录身份来源；
- 状态流转与并发控制；
- 实际运行的命令及退出码；
- 未验证项和跨仓库待办。

## SQL 与测试数据

涉及数据库结构、菜单数据、流程测试数据、初始化和回滚 SQL 时，优先调用：

```text
$delta-sql-data
```

该 Skill 已包含 2026-06-30 的 `admin_portal` 安全结构索引和基础数据摘要。不要重新扫描整份 SQL 转储；执行写入前只做目标表的最小实时检查。
