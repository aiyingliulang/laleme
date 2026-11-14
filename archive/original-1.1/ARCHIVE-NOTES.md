# 本地保留的 1.1 原始工程

2026-11-14 从作者保留的 `007_人生资产软件` 收录；版本 1.1 / versionCode 2，应用 ID `com.example.lifeassets`。保留完整 app/src（包括原有测试）、Gradle 配置、Wrapper 和原 README，未改写成 1.6。启动入口为 PooLogActivity，同时保留人生资产模块。

SOURCE-MANIFEST.json 记录收录原文件的 SHA-256。未收录缓存、编译产物、本机路径配置、签名私钥和根目录个人图片。原始目录未改动。

Windows 使用 Android SDK 35、现有兼容 JDK 21 和 Wrapper 8.10.2，设置 ANDROID_HOME 后在本目录运行 `gradlew.bat --no-daemon --max-workers=2 assembleDebug testDebugUnitTest`。从仓库根目录也可运行 `gradlew.bat -p archive/original-1.1 --no-daemon --max-workers=2 assembleDebug testDebugUnitTest`。

原工程配置将 JVM 测试限定在 Java 编译输出，不能仅凭任务成功声称 Kotlin 测试都执行；验证见根目录 SOURCE-COMPLETENESS.md。此包不能覆盖升级原发行 1.6 APK，未做手机安装或数据迁移。
