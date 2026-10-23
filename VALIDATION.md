# 验证记录

验证日期：2026-10-23（Asia/Shanghai）。

## 构建与单元测试

环境：Windows、JDK 17.0.20.1、Gradle 8.10.2、Android SDK 35。

实际执行：

```text
gradle --no-daemon --max-workers=2 testDebugUnitTest assembleDebug
BUILD SUCCESSFUL in 1m 12s
45 actionable tasks: 16 executed, 29 up-to-date
```

JUnit XML 确认 RecoveredTrendTest 共 **4 项，0 失败、0 错误、0 跳过**，覆盖：

- 当地时区当天 0 点与 23 点桶，排除前一天。
- 本周从周一起算，排除上周日。
- 近 12 个月及近 5 年的边界，排除窗口外旧记录。
- 空数据在四种趋势窗口中保持全零。

本机调试 APK：`app/build/outputs/apk/debug/app-debug.apk`，18,361,359 字节。
SHA-256：`7579c6c8c241179b90160dcd6eab062cb038d81d41dcbcacae425fc66d8b8d4a`（以下无空格完整值为准）。

完整 SHA-256：`7579c6c8c241179b90160dcd6eab062cb038d81d41dcbcacae425fc66d8b8d4a`。

## 范围与限制

- 验证主源码及 Room 生成代码可编译、APK 可打包、趋势计算测试真实执行。
- 未连接用户手机，未操作已有模拟器；没有执行安装、清空或记录删除。删除确认/撤销路径完成静态接线检查，尚未做设备运行验证。
- 未声称 APK 已通过设备启动、像素对照、全部交互或原版数据库迁移测试。
- 构建提示 android.overridePathCheck 实验选项及第三方 so 未 strip；未阻止构建。
- 初次恢复脚本中断造成 DAO / ViewModel 小段重复声明；修复为注释保留。最终以上单实例构建成功为准，不将早期失败作为验证通过。
