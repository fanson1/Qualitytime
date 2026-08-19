# QualityTime (AI 宝宝)

基于 **Kotlin Multiplatform** + **Compose Multiplatform** 的儿童古诗学习应用，面向 Android、iOS 与 Server 多平台。

以"高质量陪伴时光"为理念，为宝宝提供带拼音、真人朗读的经典古诗库，支持按年级、朝代、作者筛选与搜索。

## 功能特性

- **古诗库**：内置经典古诗，包含原文、拼音、译文、注释与赏析
- **语音朗读 (TTS)**：逐行朗读并同步高亮，支持切换系统语音、调节语速
- **自动连播**：朗读结束后自动播放下一首古诗
- **拼音显示**：可开关古诗标题/作者/朝代/正文的拼音注音
- **筛选与搜索**：按年级、朝代、作者筛选，支持关键字搜索
- **拼音字母索引**：右侧字母侧边栏快速跳转
- **排序**：按标题、年级、朝代、作者排序，可切换升降序
- **全局设置**：语音、语速、自动连播、拼音开关等设置持久化保存

## 项目结构

```
├── app/
│   ├── androidApp/       # Android 入口应用
│   ├── iosApp/           # iOS 入口应用 (SwiftUI)
│   └── shared/           # 共享 UI 与业务逻辑 (Compose Multiplatform)
│       └── src/
│           ├── commonMain/   # 跨平台共享代码
│           ├── androidMain/  # Android 平台实现
│           ├── iosMain/      # iOS 平台实现
│           └── jvmMain/      # JVM 平台实现
├── core/                 # 跨模块共享的核心代码
│   └── src/
│       ├── commonMain/       # 数据模型、TTS / 设置服务接口
│       ├── androidMain/      # Android 端 TTS (TextToSpeech) 与设置 (DataStore) 实现
│       ├── iosMain/          # iOS 端 TTS (AVSpeechSynthesizer) 与设置实现
│       └── jvmMain/          # JVM 端实现
└── server/               # Ktor 服务端应用
```

- [app/shared](./app/shared/src) — Compose Multiplatform 共享界面（启动页、古诗列表、详情、设置）
- [core](./core/src) — 跨模块共享：`Poem` 模型、`TextToSpeechService` 与 `SettingsService` 接口及各平台实现
- [server](./server/src/main/kotlin) — Ktor Server（默认端口 `8080`）

## 技术栈

- Kotlin Multiplatform (Kotlin 2.x) + Compose Multiplatform 1.x
- Material 3、AndroidX Lifecycle ViewModel / Compose
- kotlinx-serialization、Okio、AndroidX DataStore (设置持久化)
- 平台 TTS：Android `android.speech.tts.TextToSpeech`、iOS `AVSpeechSynthesizer`
- Ktor Server (Netty)

## 运行

| 平台 | 命令 / 方式 |
| --- | --- |
| Android | `./gradlew :app:androidApp:assembleDebug`，或使用 IDE 运行配置 |
| iOS | 打开 [app/iosApp](./app/iosApp) 目录，在 Xcode 中运行 |
| Server | `./gradlew :server:run` |

## 学习更多

- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-getting-started.html)
