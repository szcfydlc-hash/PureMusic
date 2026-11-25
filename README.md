<div align="center">

# 简约音乐 · PureMusic

**一款献给本地无损音乐的古典纸墨与黑胶播放器**

*Kotlin · Jetpack Compose · Material 3 · Media3 (ExoPlayer)*

</div>

## 设计哲学

市面上大多数本地音乐播放器要么像冰冷的工程仪表盘，要么堆满与听歌无关的社交与推荐。**PureMusic** 试图还原翻阅一本典藏黑胶画册时的安静与质感：

- **古典书籍装帧美学**：宣纸浅色 / 素白纸 / 墨夜暗色三套纸色基底，搭配朱砂印泥、松烟黛绿、普鲁士蓝等 6 套装帧主题色；支持自定义全局字重与字体倾斜度（0°–16°）。
- **可切换的液态玻璃模式**：偏好通透现代感时，可一键开启 4 档折射液态玻璃与悬浮胶囊底栏；关闭则回归纯粹哑光纸页。
- **零打扰，纯本地**：无开屏广告、无会员弹窗、无后台上报；仅在你主动点击「搜歌词 / 搜封面 / 译歌词」时发起按需网络请求。

## 核心特性

### 1. 96 道密纹黑胶与唱片墙

- **物理级黑胶盘面**：采用 96 道交替刻蚀音轨纹与柔亮斜向拉丝光泽，配合 GPU `graphicsLayer` 零重组丝滑旋转；切歌时仅过渡圆心封面，消除一切闪影。
- **直角画廊唱片墙**：支持点击墙面封面直接切歌，深色模式下自动提取当前封面主色渲染右上角微光至底部的丝绒氛围渐变。
- **平板 / 横屏双栏台机模式**：横屏与平板自动切换为左方物理唱片封套（探出实时转动黑胶）+ 右侧左对齐三行歌词与极简控制区。

### 2. 自适应动感排版歌词舞台

- **字号智能撑满**：根据每句歌词长度自动计算最佳排版字号，居中撑满固定歌词舞台，并用当前装帧主题色标出重音短语。
- **末词建筑线框网格**：背景由相邻歌词末尾词动态生成轻微呼吸流淌的建筑线框，让歌词随节奏保持纸面呼吸感。
- **实用歌词工具**：内置 `±0.5s` 实时时间轴微调、一键双语中文翻译缓存（离线保存为双语 `.lrc`），以及 LRCLIB 在线搜词与本地 `.lrc` 导入。

### 3. 发烧友音质与曲库效率

- **极简高效曲库**：单行紧凑工具栏（一键随机、排序、网格切换、批量多选）、`SQ / HQ` 无损与有损微型音质徽章、行尾 `+` 快捷加歌单、A–Z 快速索引与右下角一键定位当前播放曲目。
- **真实音频规格提取**：实时解析 FLAC / WAV / ALAC / MP3 / AAC 的编码格式、采样率（kHz）与比特率（kbps），Hi-Res 曲目自动加盖印章。
- **无缝衔接与交叉淡化**：支持 **Gapless 无缝播放**与 **Crossfade 切歌交叉淡入淡出**，搭配复古 Hi-Fi 5 段竖推均衡器与细腻触觉反馈（Haptic Feedback）。
- **小众而贴心的细节**：长按循环按钮可设置**单曲复听次数（×N）**，播满设定遍数自动切往下一首；支持**自建歌单相册换封面**、**JSON 歌单与收藏导出/恢复**，以及跟随系统浅/深色的**宣纸黑胶桌面小组件**。

## 截图展示

> 💡 提示：将实机截图放入仓库 `screenshots/` 目录并替换下方路径即可在 GitHub 主页直接展示。

| 竖屏黑胶与唱片墙 | 自适应动感歌词 | 横屏 / 平板双栏 | 极简曲库与批量多选 |
| --- | --- | --- | --- |
<img width="896" height="1200" alt="image" src="https://github.com/user-attachments/assets/44b200cb-079b-4818-ad7e-401e5379eeeb" />
<img width="896" height="1200" alt="image" src="https://github.com/user-attachments/assets/c5b135cf-a339-476f-8488-f70b53d3154d" />
<img width="896" height="1200" alt="image" src="https://github.com/user-attachments/assets/f8d65414-9223-48de-b62a-24e7978c4703" />
<img width="896" height="1200" alt="image" src="https://github.com/user-attachments/assets/bd47ef24-5974-408b-8636-036faabea8f9" />
<img width="896" height="1200" alt="image" src="https://github.com/user-attachments/assets/d7b66a8b-490a-4e87-a9a7-5ff5ea10fdfc" />
<img width="896" height="1200" alt="image" src="https://github.com/user-attachments/assets/8e9a6456-65be-486e-a1ec-49b9eb32b457" />

<img width="768" height="1376" alt="image" src="https://github.com/user-attachments/assets/42db6b95-fdc8-4315-a426-f3483a82600e" />







## 下载与安装

1. 前往本仓库右侧 [suspicious link removed] 页面下载最新正式版 `PureMusic-v1.4.apk`。
2. 授予本地音频读取权限后，自动扫描手机本地音乐文件夹（可在「设置 → 曲库」中过滤短音频或屏蔽特定目录）。

## 技术栈

- **语言与框架**：Kotlin 2.0 + Jetpack Compose (Material 3)
- **音频引擎**：AndroidX Media3 (`ExoPlayer` + `MediaSessionService` + `DefaultLoadControl`)
- **图片加载与缓存**：Coil 2.7（25% 内存缓存 + 120MB 磁盘缩略图缓存）
- **持久化存储**：Jetpack DataStore Preferences + 本地 JSON / LRC 缓存
- **最低支持**：Android 8.0 (API 26) · 目标版本 Android 15 (API 35)

<div align="center">

*如果这款安静的本地播放器刚好合你心意，欢迎点亮右上角 **Star**。*

</div>
