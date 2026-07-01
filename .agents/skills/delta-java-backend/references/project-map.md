# Java 后端项目地图

## 根工程

```text
pom.xml
yudao-server/
yudao-framework/
yudao-module-system/
yudao-module-infra/
yudao-module-member/
yudao-module-pay/
yudao-module-mall/
yudao-module-delta/
接口文档.md
```

## Delta 模块常见定位方式

从 `yudao-module-delta/src/main/java` 开始查找：

```text
controller/admin/**
controller/app/**
controller/**/vo/**
service/**
dal/dataobject/**
dal/mysql/**
enums/**
job/**
```

测试通常位于：

```text
yudao-module-delta/src/test/java/**
```

## 重要规则

- 根 `pom.xml` 声明 Java 8，但 Delta 模块自己的编译插件使用 source/target 9；不要擅自统一版本。
- Delta 模块依赖 member、trade、tenant、security、mybatis、redis。
- `接口文档.md` 很大，只读取当前模块相关章节，不要每次全量加载。
- 接口文档不是代码替代品。
