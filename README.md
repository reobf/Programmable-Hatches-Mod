<div align="center">

# Programmable Hatches

**可编程仓室**

面向 GT New Horizons 的自动化附属模组

[![Build and test](https://github.com/reobf/Programmable-Hatches-Mod/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/reobf/Programmable-Hatches-Mod/actions/workflows/build-and-test.yml)
[![Releases](https://img.shields.io/github/v/release/reobf/Programmable-Hatches-Mod?include_prereleases)](https://github.com/reobf/Programmable-Hatches-Mod/releases)
[![Issues](https://img.shields.io/github/issues/reobf/Programmable-Hatches-Mod)](https://github.com/reobf/Programmable-Hatches-Mod/issues)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

## 关于本项目

Programmable Hatches（PH）是一个非官方的 GT New Horizons 社区附属模组，主要改善 GregTech 多方块机器与 Applied Energistics 2 之间的自动化体验。

模组围绕“如何把一份配方可靠地交给机器”扩展了多种自动化设备，其中包括：

- **可编程输入仓**：在同一个仓室中处理物品、流体和虚拟电路，并通过多流体、缓冲及订单隔离型号适应不同产线；
- **样板输入设备**：让输入总线或输入总成直接接入 AE 网络、保存处理样板并接收合成订单；
- **编程器电路系统**：在样板中记录虚拟电路配置，并由输入仓、覆盖板或专用提供器交给机器；
- **AE 网络设备**：提供合成 CPU、库存维护、存储代理、频道连接、样板提交及输入输出扩展；
- **复杂产线工具**：为装配线、数据访问仓、OpenComputers 外设和其它特殊自动化场景提供配套设备。

这些设备既可以简化常见的 AE—GregTech 自动化，也可以处理高并行、多流体、多订单和大型产线中的输入组织问题。

本项目不是 GTNH 官方模组。由 PH 引起的问题请提交到本仓库，不要直接提交给 GTNH 官方问题追踪器。

## 文档

完整的设备说明和使用方法由 GTNH 中文灰机 Wiki 维护：

- [可编程仓室 Wiki 主页](https://gtnh.huijiwiki.com/wiki/PH:%E5%8F%AF%E7%BC%96%E7%A8%8B%E4%BB%93%E5%AE%A4)
- [设备导航](https://gtnh.huijiwiki.com/wiki/%E6%A8%A1%E6%9D%BF:Navbox_PH)

实际游玩时，请同时参考当前安装版本的 NEI、Tooltip 和 Wiki；开发分支的行为可能先于文档发生变化。

## 下载与版本

请选择与整合包版本对应的运行包，不要下载文件名带有 `dev` 或 `sources` 的文件。

| GTNH 版本 | PH 版本 | 下载 |
| --- | --- | --- |
| 2.5.1 | 0.0.18p28 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.0.18p28-beta) |
| 2.6.0–2.6.1 | 0.0.20 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/0.0.20-beta) |
| 2.7.0–2.7.2 | 0.1.1p13 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.1.1p13-beta) |
| 2.7.3–2.7.4 | 0.1.2p44 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.1.2p44-beta) |
| 2.8.0–2.8.4 | 0.1.3p57 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.1.3p57-beta) |
| 2.9.0 Beta 1 | 0.2.0p9（290b1） | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.2.0p9-beta-290b1) |
| 2.9.0 Beta 2 | 0.2.0p19 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.2.0p19-beta) |
| 2.9.0 Beta 3 | 0.2.0p22 | [前往发布页](https://github.com/reobf/Programmable-Hatches-Mod/releases/tag/v0.2.0p22-beta) |
| 2.9.0 Daily | 当前维护版本 | [查看最新发布](https://github.com/reobf/Programmable-Hatches-Mod/releases/latest) |

旧版下载链接固定到各兼容分支的最后版本；2.9.0 Daily 请始终从最新发布页取得当前构建。

当前开发工作位于 [`290-daily-latest`](https://github.com/reobf/Programmable-Hatches-Mod/tree/290-daily-latest) 分支。开发分支会持续变化，更适合测试和反馈，不建议在没有备份的情况下直接用于重要存档。

## 问题反馈

遇到崩溃、材料异常、界面无法打开、设备行为与说明不符或模组兼容问题时，欢迎[提交 Issue](https://github.com/reobf/Programmable-Hatches-Mod/issues/new)。提交前请先搜索是否已有相同问题，并尽量提供：

- GTNH 版本和 PH 版本；若使用开发构建，请附提交号；
- 可以重复问题的操作步骤；
- 预期行为与实际行为；
- 完整日志或崩溃报告，而不是只有最后几行；
- 与问题有关的截图、配置和额外模组列表；
- 如果可能，提供最小化的复现环境或测试存档。

如果不能确定问题来自 PH，可以先在备份环境中移除 PH 或缩小模组范围进行确认。即使尚未完全定位，也可以提交信息充分的 Issue；明确的复现步骤通常比对原因的猜测更有帮助。

功能建议同样欢迎。请说明希望解决的实际使用场景，以及现有设备为什么不能满足需求。

## 参与贡献

Issue、Pull Request、兼容性修复、翻译和文档改进都非常欢迎。提交 PR 时建议：

- 让一次 PR 专注于一个明确问题；
- 说明修改目的、玩家可见变化和验证方法；
- 大型功能或会改变既有行为的方案，先开 Issue 讨论；
- 同步更新新增或变更的语言文本；
- 如果修改了玩家可见机制，同时指出需要更新的 Wiki 页面；
- 提交前运行构建，并确认没有引入新的编译错误。

```bash
./gradlew build
```

不熟悉整个代码库也没有关系。范围清晰的小修复、可靠的复现用例和准确的文档勘误同样有价值。

## 兼容性

PH 主要面向 GTNH 当前开发环境。对于 GTNH 官方内容，会在实现条件允许的范围内维护兼容；第三方社区模组、特殊多方块机器和非标准输入接口可能需要单独适配。

某台机器无法配合 PH 使用时，请不要先假定任何一方必然有错。提交机器名称、连接方式、版本和复现步骤后，项目维护者才能判断是配置问题、接口限制还是需要新增兼容处理。

## 许可证

本项目使用 [MIT License](LICENSE)。欢迎在许可证允许的范围内学习、修改和分发代码。
