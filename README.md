# TrollHack-Recode

基于 Fabric 的 TrollHack 重构/迁移工程，用于将旧版 TrollHack（Forge 体系）逐步迁移到 1.21.11（Yarn）环境，并在此基础上做架构与功能迭代。

## 目录结构

- `src/main/java/dev/mahiro/trollhack/`：当前重构代码（事件系统、入口等）
- `src/main/resources/`：Fabric 元数据、mixin 配置、access widener
- `Minecraft源码/`：用于对照与开发的 1.21.11 Yarn 反编译源码（体积较大）
- `老版本/TrollHack-master/`：旧版 TrollHack 参考实现（架构/模块/配置/GUI 等）
- `参考项目/Sakura/`：Fabric 客户端参考实现（模块/事件/GUI/配置等）

## 构建与运行

本工程使用 Gradle 构建。

- Windows：`gradlew build`
- 运行开发环境：`gradlew runClient`

## 架构目标（迁移方向）

- 保留 TrollHack 的模块/设置/配置/GUI 框架能力
- 将平台相关实现替换为 Fabric callback + Mixin 注入层
- 事件桥接集中化（避免业务逻辑分散进 mixin）
- 配置优先兼容旧格式，后续逐步演进

## 备注

如果你准备提交到 Git，建议将 `Minecraft源码/` 作为本地参考目录处理（按需加入忽略规则），以避免仓库体积膨胀。

