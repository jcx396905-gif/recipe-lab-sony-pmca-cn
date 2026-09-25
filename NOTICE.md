# NOTICE — 第三方组件与字体许可

本仓库是 [voxivoid/recipe-lab-sony-pmca](https://github.com/voxivoid/recipe-lab-sony-pmca) 的汉化分支，
在原项目基础上修改而来，遵循其 MIT License（见 [LICENSE](LICENSE)）。

## 内置字体

`assets/cn.ttf`

- 来源：**DroidSansFallback**（Android Open Source Project）
- 版权：Copyright (C) 2008 The Android Open Source Project
- 许可：**Apache License, Version 2.0** — https://www.apache.org/licenses/LICENSE-2.0
- 处理：使用 fontTools (`pyftsubset`) 按本应用实际使用的字符进行子集化（320 个汉字 + 全部可打印 ASCII），
  由原始 3.45 MB 缩减至约 56 KB

## 源码中内置的第三方代码

`jni/platform/`

- 来源：**[ma1co/OpenMemories-Platform](https://github.com/ma1co/OpenMemories-Platform)**
- 许可：MIT License（见 `jni/platform/LICENSE.txt`）
- 说明：该目录为上游源码的副本（非 git 子模块），已移除其自身子模块引用。
  上游的 `stlport` 子模块源（SourceForge）已失效；若需完整构建原生库，
  可从其它镜像获取 stlport，或直接复用官方发布 APK 中预编译的 `librecipelab.so`。

## 构建与安装工具（不包含在本仓库中）

本仓库不分发以下二进制，使用者在安装时需自行获取：

| 工具 | 来源 | 许可 |
|---|---|---|
| Sony-PMCA-RE (`pmca-gui`) | [ma1co/Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE) | MIT |
| OpenMemories-Tweak | [ma1co/OpenMemories-Tweak](https://github.com/ma1co/OpenMemories-Tweak) | MIT |

## 商标声明

Sony、PlayMemories、α 及相关标识为索尼公司（Sony Corporation）的商标或注册商标。
本项目与索尼公司无任何关联，未获其授权、赞助或认可。

富士（Fujifilm）、柯达（Kodak）、徕卡（Leica）、哈苏（Hasselblad）、佳能（Canon）、尼康（Nikon）、
理光（Ricoh）、松下（Panasonic）、奥林巴斯（Olympus）、伊尔福德（Ilford）、爱克发（Agfa）、
宝丽来（Polaroid）、CineStill 等名称与商标归各自权利人所有，
本项目中的"配方"仅是以相机自身可调参数对这些品牌色彩风格的近似模拟，不代表原厂色彩科学。
