# 恢复说明

目标：作者提供的拉了么 1.6 / versionCode 8 APK（包名 `com.laleme.android16.v15`，minSdk 26，targetSdk 35）。本工程不是原始源码恢复证明，而是有证据的可维护重建。

## 来源与方法

- 基础：作者保留的 1.1 项目 `007_人生资产软件` 中 PooLogActivity、poolog 下的 Kotlin 源码与应用资源；不包含无关资产管理模块。
- 本地 REA 证据：PooLogAppKt.java、PooLogViewModel.java、PooEnhancements.java、PooTrendBuilder.java、ClearChartRenderer.java 与类清单。原始证据保留在本机 recovery-evidence，出于避免发布编译产物和无关反编译内容不纳入 Git。
- 重写并接入 1.6 已证实的累计次数等级、距上次时间、单条删除确认、撤销最近记录、四个趋势统计窗口以及图表横向滚动。
- PooTrendBuilder 的反编译输出出现明显失真的条件（如 hour < 0），重建使用日期窗口与合法下标范围判断，不直接复制失真代码。

## 兼容性及限制

- 保留本地数据结构：toilet_visits(id 字符串主键, occurredAt 时间戳)，数据库版本 1，文件名 la-le-me.db。由于更换应用 ID，未迁移原 App 私有数据库，未声称可直接读取原版数据。
- UI 基础布局和资源来自 1.1；1.6 ClearChartRenderer 的 Android Canvas 文字渲染用 Compose 图表重新实现。未声称像素级一致。
- 1.6 部分 Compose 编译代码无法直接作为 Kotlin 源码复用；本工程按已证实行为重写，不能保证每个交互细节与原包完全相同。
- 使用新的本地调试签名，不包含旧私钥。不能覆盖安装原版，不保证原版备份恢复兼容。
- 未对用户手机或原版数据执行安装、清空或删除。

构建和测试结果见 VALIDATION.md。

## 输入 APK 指纹

SHA-256：`12ee33f4319dec942ec12fd76dcdd12780d59fe523deda8df61ab06e1319332d`
