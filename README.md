# 拉了么 · 1.6 恢复工程

一款离线如厕时间记录 Android 应用。数据保存在本机 Room 数据库，不需要账号、不请求网络权限。

**这是恢复工程，不是遗失的 1.6 原始源码，也不是逐字节复刻。** 以作者保留的 1.1 Kotlin / Compose 源码为基础，依据作者自己的 1.6 APK 中可验证的增强逻辑重建。详细证据及限制见 [RECOVERY.md](RECOVERY.md)。

## 功能

- 一键记录时间、今日次数、连续记录天数、时间线及记录庆祝动画。
- 月历与选中日期记录；单条记录删除前明确确认；撤销最近记录。
- 距上次记录时间与 Lv.0 至 Lv.6 累计次数称号。
- 今日 24 小时、本周 7 天、近 12 个月、近 5 年统计；横向滚动柱状图及近周/月折线图。

## 构建

需要 JDK 17、Android SDK 35。复制 Android SDK 路径到本机 `local.properties`（例如 `sdk.dir=/path/to/sdk`）。

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Windows 使用 `gradlew.bat`。技术栈：Kotlin 2.0.21、Compose、Room、AGP 8.7.3、Gradle 8.10.2。最低 Android 8.0 / API 26，目标 API 35。

## 安装与数据

恢复版应用 ID 为 `com.laleme.recovered`，原 APK 为 `com.laleme.android16.v15`。源码包名保留 `com.example.lifeassets`。二者可并存；恢复版**不能覆盖原版升级，也不会自动读取原版数据**。本恢复工程不包含原发行签名私钥。不要为安装恢复版卸载原应用，避免丢失本机数据。

## 许可

作者自有源码以 MIT 许可提供；第三方依赖与构建工具保留各自许可，详见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。不提交完整 APK、反编译第三方库或签名密钥。
