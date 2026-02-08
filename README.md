# TrollHack-Recode

基于 Fabric 的 TrollHack 重构/迁移工程，用于将旧版 TrollHack（Forge 体系）逐步迁移到 1.21.11（Yarn）环境，并在此基础上做架构与功能迭代。

## 目录结构

- `src/main/java/dev/mahiro/trollhack/`：当前重构代码（事件系统、入口等）
- `src/main/resources/`：Fabric 元数据、mixin 配置、access widener
- `Minecraft源码/`：用于对照与开发的 1.21.11 Yarn 源码
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

## 上面是AI写的，下面才是我要说的话：
首先呢，我这个项目只是测试AI的真正能力，我给AI丢了我Sakura外挂的源代码学习，架构基本都跟我的Sakura大差不差吧。。反正这个是开源项目，我尽力维护，嗯对。
进群QQ交流：1群：1077859841   2群：991876741
我当然欢迎你们所有人提交代码！这个项目我尽量让ai完全按照我Sakura项目的架构进行写了，不过还是有些写的难受的地方，我欢迎你们所有人提交修改。
