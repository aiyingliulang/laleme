> 当前入口提示：本工程目前启动“拉了么”如厕记录应用，提供一键记录时间、今日次数、连续记录天数及日历入口。原“人生资产”的资产与固定支出管理代码仍保留，但不是当前启动界面。详细文件位置见下文“当前代码核对：项目里有什么”。本次仅静态核对源码和配置，未运行 Android 构建或测试。

# 人生资产

一款离线的 Android 资产与固定支出记录应用。数据保存在手机本地，支持资产生命周期成本、贷款月供、周期订阅、搜索筛选和 JSON/CSV 备份。

## 打开与构建

使用 Android Studio 打开本目录，等待 Gradle 同步后运行 `app` 配置即可。

本工程使用：

- Kotlin + Jetpack Compose + Material 3
- Room 本地数据库
- DataStore 设置存储
- Android Photo Picker 图片选择
- Android 8.0（API 26）及以上

命令行构建命令为：

```powershell
.\gradlew.bat assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

安装到已连接的安卓手机：

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 数据说明

金额以“分”保存，日期保存为 `yyyy-MM-dd`。JSON 备份包含完整记录和图片路径；CSV 适合查看和二次处理。导入默认合并，替换本机数据需要在应用内二次确认。

当前版本不包含联网、账号、云同步和系统提醒。

Windows 注意：Android Gradle Plugin 在包含中文字符的路径下，部分 JVM 单元测试任务可能出现测试类加载失败。APK 构建不受影响；如需运行 `testDebugUnitTest`，请将工程复制到不含中文的路径后执行测试。

## 当前代码核对：项目里有什么

当前工程包含两套应用相关代码，以上“人生资产”介绍对应保留的资产管理部分；手机实际启动的入口已切换到“拉了么”。

### 当前启动的应用：拉了么

- 显示名称：拉了么（`app/src/main/res/values/strings.xml`）。
- 启动入口：`PooLogActivity`，由 `AndroidManifest.xml` 中的 MAIN/LAUNCHER 声明指定。
- 应用初始化：`poolog/PooLogApplication.kt`，创建 Room 数据库及如厕记录仓库。
- 主要界面：`poolog/ui/PooLogApp.kt`，包含“记录”和“日历”两个标签；记录页支持点击记录当前时间、今日次数、连续记录天数和今日时间线，并提供触觉及庆祝反馈。
- 状态管理：`poolog/ui/PooLogViewModel.kt`，观察记录列表并将当前时间写入仓库。
- 数据在本机 Room 数据库中保存；当前清单未声明网络权限。

上述 Kotlin 文件均位于 `app/src/main/java/com/example/lifeassets/` 下。

### 保留的资产管理部分

`MainActivity.kt` 仍包含资产、固定支出和个人设置页面导航，以及资产和支出的新增、编辑、详情入口。当前清单未将它配置为启动 Activity，因此不能将旧 README 描述的功能视为当前启动界面的功能。

### 工程配置与其他文件

- Gradle 工程名：LifeAssets；应用模块：`app`。
- 应用包名：`com.example.lifeassets`；版本：1.1（versionCode 2）。
- Android 最低版本：API 26；compileSdk / targetSdk：35；Java / Kotlin JVM 目标：17。
- 主要依赖声明：Kotlin、Jetpack Compose、Material 3、Navigation、Room、DataStore 和 Coil。
- 根目录包含 Gradle 配置、`gradle` 目录、`gradlew.bat`、`.kotlin` 目录和两张 JPG 图片；图片内容未核对。

本节依据目录、源码及配置文件静态核对编写，未执行 Android 构建、安装或运行测试。
