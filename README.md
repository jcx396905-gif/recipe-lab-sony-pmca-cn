# 配方实验室 中文版 · Recipe Lab CN

> 索尼 PlayMemories 相机应用（PMCA）的中文汉化版 —— 77 种胶片色彩配方，实时预览，一键写入相机，在所有拍摄模式长期生效。

本仓库基于 **[voxivoid/recipe-lab-sony-pmca](https://github.com/voxivoid/recipe-lab-sony-pmca)**（MIT License）进行界面汉化与字体适配。
原项目由 André Domingues 开发，作者：voxivoid。**本仓库为汉化分支，非官方版本。**

---

## 这是什么

一个直接运行在索尼相机上的应用。滚动拨轮浏览 77 种"胶片配方"（富士、柯达、徕卡、哈苏、理光 GR、电影胶片、伊尔福德等色彩风格），
画面实时预览变化，按下中央按钮即把该风格**永久写入相机设置** —— 之后在 P/A/S/M、照片和视频所有模式下都持续生效，
关机重启也不会丢失。

- **不碰固件、不越狱**：只写入你本来也能在相机菜单里手动修改的设置项
- **完全可逆**：应用内按 TRASH 键即可恢复出厂色彩
- **实时预览**：拨动拨轮即可看到画面变化，满意了再写入

## 中文版做了什么

| 项目 | 说明 |
|---|---|
| 界面汉化 | 面板、提示、弹窗、品牌浏览器、开发工具菜单、收藏夹等全部中文 |
| 配方名称 | 77 个配方名与 12 个品牌分组全部中文（柯达 Portra 400、经典正片、徕卡经典…） |
| 创意风格 | 采用**索尼官方中文菜单用语**：标准 / 生动 / 中性 / 肖像 / 风景 / 黑白 / 清晰 / 深色 / 明快 / 黄昏 / 夜景 / 红叶 / 棕褐色 |
| 照片效果 | 采用索尼官方中文名：玩具相机 / 流行色彩 / 色调分离 / 复古照片 / 柔和高亮 / 局部彩色 / 强反差单色 / 柔焦 / HDR 绘画 / 丰富色调单色 / 微缩景观 / 插图 / 水彩画 |
| 内置中文字体 | 相机固件字体不含中文字形，故内置 DroidSansFallback 按需子集（56 KB），并显式应用到所有文本控件与 Canvas 文字 |
| 版本号 | `versionCode 10302000` / `versionName 1.3.1-cn`，高于官方 1.3.1，可覆盖安装并保留偏好 |

未翻译的仅有运行时传给相机 HAL 的参数键值（如 `standard`、`toy-camera`），这些是协议值，必须保持原样。

## 实机验证

以下为 **A7R II 上实际运行本中文版**的屏幕实拍（相机贴有碳纤维保护贴）。可见中文字体渲染完全正常（无方框乱码），
品牌浏览器、配方名称、参数条与按键提示均已完整汉化。

| 品牌浏览器（理光 GR 分组） | 哈苏 HNCS 自然色 | 徕卡 Monochrom 黑白 |
|---|---|---|
| [![品牌浏览器](docs/screenshots/01-brand-browser.jpg)](docs/screenshots/01-brand-browser.jpg) | [![哈苏 HNCS 自然色](docs/screenshots/02-hasselblad-hncs.jpg)](docs/screenshots/02-hasselblad-hncs.jpg) | [![徕卡 Monochrom 黑白](docs/screenshots/03-leica-monochrom.jpg)](docs/screenshots/03-leica-monochrom.jpg) |

- **左图**：品牌浏览器 —— 左侧为中文品牌分组及配方数量（富士模拟 16、富士胶片 5、柯达 14、电影 4、理光 GR 8、徕卡 4、哈苏 1、佳能/尼康 5、松下/奥林巴斯 5…），右侧为「理光 GR」组的 8 个中文配方，底部提示「配方 / 关闭」
- **中图**：主面板显示「哈苏 HNCS 自然色」（60 / 77），带「预览」状态徽章，参数条标签（画质/风格/饱和/对比/锐度/矩阵/特效/子项/白平衡/色温/A-B/G-M/DRO）与底部按键提示（浏览/选定/收藏(长按)/出厂/隐藏/退出）全部中文
- **右图**：主面板显示「徕卡 Monochrom 黑白」（62 / 77），同样中文渲染正常

## 支持机型

**官方确认可用**（写入后能跨关机保存）：

| 机型 | 型号代码 | 备注 |
|---|---|---|
| **A7R II** | **ILCE-7RM2** | **固件 4.00 / 4.01** |
| A7R | ILCE-7R | 固件 3.2 |
| A7 II | ILCE-7M2 | 已确认可用 |
| A7 | ILCE-7 | 固件 3.20 |
| A6000 | ILCE-6000 | 原项目开发机型，固件 3.21 |
| A6500 / A6300 | ILCE-6500 / ILCE-6300 | — |
| A5100 | ILCE-5100 | 无 Fn / AEL 键，部分功能不可达 |
| NEX-5T | NEX-5T | 最早支持应用的一代 |
| RX100 V | DSC-RX100M5 | 固件 2.00 |
| HX60 / HX60V | DSC-HX60 | 首台确认可用的卡片机，变焦与闪光灯不可用 |

**不支持**：2016 年底之后发布的机型（A7 III 及以后、A9/A1 系列、A6100/A6400/A6600/A6700、ZV-E10/ZV-1、FX3/FX30、RX100 VA+、RX10 IV、RX0、HX99 等）——
这些机型固件已加密，没有 `MENU → 应用程序` 入口，无法安装任何相机应用。

## 下载与安装

1. 到本仓库 **[Releases](../../releases)** 下载 `RecipeLabCN.apk`
2. 下载安装工具 **[Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE/releases)**（Windows 用 `pmca-gui-v0.18-win.exe`）
3. 相机设置：`MENU → 设置 → USB 连接 → 大容量存储`，插好 SD 卡并开机
4. USB 连接电脑，运行 pmca-gui，点击 **Install app from file**，选择 `RecipeLabCN.apk`
5. 安装时相机黑屏 / 闪动属正常现象，**以电脑端 "Task completed successfully" 为准**，不要拔线
6. 拔线后**关机再开机**，然后 `MENU → 应用程序 → 应用程序列表 → 配方实验室`

更详细的图文步骤、操作说明与常见问题见 [docs/INSTALL-CN.md](docs/INSTALL-CN.md)。

### 操作速查

| 按键 | 功能 |
|---|---|
| 控制拨轮 / 转盘 | 浏览配方（实时预览） |
| Fn | 打开品牌列表（第一项为收藏夹） |
| 中央按钮（短按） | **选定**当前配方，写入相机 |
| 中央按钮（长按约 1 秒） | 收藏 / 取消收藏 |
| AEL / DISP | 切换面板：完整 → 精简 → 隐藏 |
| TRASH | 恢复出厂色彩 |
| MENU | 退出应用 |
| C1 | 开发工具菜单（快照对比、只读检查、样片拍摄） |

> **关键**：选定配方后请关机再开机，该色彩风格才会在全部模式中默认生效。

## 从源码构建

### 依赖

- JDK（`javac --release 8` 可用即可，已在 Zulu JDK 17 上验证）
- Android SDK build-tools **30.0.3**（提供 `aapt` / `zipalign` / `apksigner` / `d8`）
- Android platform **android-28** 的 `android.jar`（仅作编译期 classpath）

### 方式一：复用官方预编译原生库（推荐，无需 NDK）

原生库 `librecipelab.so` 可直接从官方发布 APK 中提取，因此**不必安装 Android NDK**：

```bash
unzip -oq RecipeLab-official.apk "lib/armeabi/librecipelab.so" -d out/apklib
```

随后执行 `tools/` 中的构建步骤（等价于原项目 `build.cmd`）：

```bash
BT=<build-tools 目录>; AJ=<android.jar 路径>
"$BT/aapt.exe" package -f -m -J out/gen -M AndroidManifest.xml -S res -I "$AJ"
javac -encoding UTF-8 --release 8 -Xlint:-options -cp "$AJ" -d out/classes \
  out/gen/com/voxivoid/recipelab/R.java src/com/voxivoid/recipelab/*.java
java -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 10 \
  --lib "$AJ" --output out/dex $(find out/classes -name "*.class" | tr '\n' ' ')
"$BT/aapt.exe" package -f -M AndroidManifest.xml -S res -A assets -I "$AJ" -F out/unaligned.apk
(cd out/dex    && "$BT/aapt.exe" add ../unaligned.apk classes.dex)
(cd out/apklib && "$BT/aapt.exe" add ../unaligned.apk lib/armeabi/librecipelab.so)
"$BT/zipalign.exe" -f 4 out/unaligned.apk out/aligned.apk
"$BT/apksigner.bat" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android \
  --min-sdk-version 10 --v1-signing-enabled true --v2-signing-enabled false --v3-signing-enabled false \
  --out RecipeLabCN.apk out/aligned.apk
```

### 方式二：完整构建原生库

按原项目 `build.cmd` / `build.sh`，配合 Android NDK r16b 与
[OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform)（本仓库 `jni/platform` 已内置其源码）。

### 构建注意事项

- **签名用任意自签密钥即可**：PMCA 应用只使用 v1 签名（JAR signing），相机不校验证书链。
- **构建路径不要含中文**：`aapt` 无法加载位于非 ASCII 路径下的 `-I android.jar`，会误报
  `No resource identifier found for attribute 'color' in package 'android'`。请把工程与工具放在纯英文路径下构建。
- **有 assets 时必须加 `-A assets`**，否则内置字体不会被打包。

## 项目结构

```
src/com/voxivoid/recipelab/   应用源码（Java）
  Recipes.java                77 个配方的参数表与名称
  Params.java                 参数行、设置存储编解码、HUD 文案
  MainActivity.java           状态、相机交互、按键处理
  Cn.java                     内置中文字体加载与应用
  Legend/HintBar/PickerView/PromptView/MenuView   自绘视图
assets/cn.ttf                 中文子集字体（DroidSansFallback，Apache-2.0）
jni/                          原生库源码（OpenMemories-Platform 已内置）
res/                          资源（布局、图形、ids.txt）
docs/                         开发与安装文档
```

## 已知限制

- 配方是基于本机可存储参数对其他品牌色彩的**近似还原**，不是原厂色彩科学的复制
- 相机不存储色调曲线，因此对数曲线（S-Log、V-Log、CineLike D）与着色黑白（硒调、蓝晒）无法实现
- 设置槽位 ID 是在 A6000 上逆向得到的，在未经测试的机型上可能落在不同位置
- 照片效果类配方与 RAW 互斥（相机会忽略效果），应用会自动切换到 JPEG

## 致谢

- **[ma1co](https://github.com/ma1co)** —— PlayMemories 平台逆向工程（Sony-PMCA-RE、OpenMemories-Platform、OpenMemories-Tweak），没有这些工作本项目不可能存在
- **[voxivoid](https://github.com/voxivoid)** —— 原项目 Recipe Lab 的作者
- **Veres Deni Alex** —— 配方数值参考的索尼胶片模拟样片

## 许可

- 本项目沿用原项目 **MIT License**，见 [LICENSE](LICENSE)
- 内置字体 `assets/cn.ttf` 为 DroidSansFallback 子集，**Apache License 2.0**
- 第三方组件与字体来源详见 [NOTICE.md](NOTICE.md)
