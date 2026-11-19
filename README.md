# 简约音乐 (PureMusic) — Android 原生离线音乐播放器

完全从零独立编写的 Android 原生离线音乐播放器项目，采用 **Kotlin + Jetpack Compose (Material 3) + AndroidX Media3 (ExoPlayer)** 架构，无广告、无联网追踪、不依赖任何第三方侵权源码。

## 核心功能

- **本地音乐扫描与多维分类**：基于 `MediaStore` 自动扫描手机本地音频（MP3 / FLAC / WAV / M4A / OGG 等），支持按 **歌曲 / 专辑 / 歌手 / 文件夹 / 歌单** 浏览与实时搜索、排序（按歌名、歌手、时长、添加时间）。
- **专业后台播放引擎**：基于 AndroidX Media3 `ExoPlayer` 与 `MediaSessionService`，支持后台播放、通知栏与锁屏控制、耳机拔出自动暂停、音频焦点管理。
- **完整播放控制**：全屏播放页 + 底部迷你播放条、进度拖拽、随机播放、列表/单曲循环、播放队列查看与移除。
- **歌单与收藏持久化**：基于 Jetpack DataStore 本地持久化“我喜欢的音乐”与自定义歌单（新建、添加、移除、删除）。
- **实用工具与主题**：内置系统音频均衡器（预设 + 多频段 dB 调节）、睡眠定时关闭倒计时、浅色/深色/跟随系统模式及 5 套主题强调色。

## 方式一：GitHub 云端自动打包 APK（无需安装 Android Studio）

1. 在 [GitHub](https://github.com) 创建一个新的空仓库（公开或私有均可）。
2. 解压 `PureMusic_Android_Project.zip`，将 **`PureMusicAndroid` 文件夹里面的所有文件和文件夹**（包括隐藏的 `.github` 文件夹、`app`、`gradle`、`build.gradle.kts`、`settings.gradle.kts` 等）上传或 `git push` 到仓库根目录（分支为 `main` 或 `master`）。
   - 如果用命令行推送：
     ```bash
     cd PureMusicAndroid
     git init -b main
     git add .
     git commit -m "Initial PureMusic Android project"
     git remote add origin https://github.com/<你的用户名>/<你的仓库名>.git
     git push -u origin main
     ```
3. 打开仓库顶部的 **Actions** 标签页，会看到 **Build Android APK** 自动开始运行（也可点击 **Run workflow** 手动触发）。
4. 等待约 2–4 分钟编译完成后，点进本次运行记录，在底部 **Artifacts** 区域点击 **`PureMusic-debug-apk`** 即可下载包含 `app-debug.apk` 的压缩包，解压后传到 Android 手机安装即可。

## 方式二：Android Studio 本地编译

1. 将 `PureMusic_Android_Project.zip` 解压到纯英文路径。
2. 用 **Android Studio (Ladybug 2024.2 或更高版本)** 打开 `PureMusicAndroid` 文件夹，使用内置 **JDK 17** 等待 Gradle Sync 完成。
3. 连接手机点击 **Run**，或通过 **Build → Build Bundle(s) / APK(s) → Build APK(s)** 生成 `app/build/outputs/apk/debug/app-debug.apk`。

## 开源与版权声明

本项目所有 Kotlin 源码、布局代码及矢量图标均为独立原创编写，依赖库均为 Google AndroidX / Kotlin / Coil 官方开源组件，可自由修改、编译与个人使用。
