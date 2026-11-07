# MobileGlues-Build

为 [FoldCraftLauncher](https://github.com/FoldCraftLauncher/FoldCraftLauncher) 构建 [MobileGlues](https://github.com/FCL-Team/MobileGlues) 渲染器 AAR，目标是 FCL fork 的 **`dev-fcl` 分支**（上游 [MobileGL-Dev/MobileGlues](https://github.com/MobileGL-Dev/MobileGlues) `dev` + FCL 补丁）。

> 本仓库只是构建 wrapper（Gradle 配置 + CI），不含 MG 源码；MG 源码在 [FCL-Team/MobileGlues](https://github.com/FCL-Team/MobileGlues)（fork），补丁直接提交在其 `dev-fcl` 分支，`dev` 分支保持与上游镜像。

本仓库本身不含 MG 源码，只包含构建 wrapper（Gradle 配置 + CI），构建方式对齐 [AngelAuraMC/amethyst-prebuilt-libraries](https://github.com/AngelAuraMC/amethyst-prebuilt-libraries/tree/master/renderers/MobileGlues) 中 `renderers/MobileGlues` 的做法：

- 模块 `MobileGlues/build.gradle.kts` 即上游 MG 自带的构建配置（namespace `top.mobilegl.mobileglues`、NDK 27.3.13750724、CMake 入口 `MobileGlues/MobileGlues-cpp/CMakeLists.txt`），叠加 `assets/licenses` 许可证资源。
- MG 是纯 native 库（无 Java 源码），产物 = 4 个 ABI 的 `libmobileglues.so` + 空 `classes.jar` + LGPL 许可证资产。
- FCL 侧契约：`POJAV_RENDERER=opengles3_mobileglues`，EGL 库名 `libmobileglues.so`。

## 补丁

补丁不再由本仓库在构建前临时应用，而是直接提交在 fork 的 `dev-fcl` 分支（当前基线上游 dev@0c27a3a，提交 `glShaderSource 参数校验、单段显式长度提交与严格错误上报`）：`glShaderSource` 校验参数（负 count、空指针、非着色器对象），按字节长度拼接源码（含内嵌 NUL），驱动侧以单段显式长度提交；转换失败时提交原文而非保留旧源码；被拒调用的 GL 错误按上下文记录，经 `glGetError` 报告。

跟进上游：fork 的 `dev` 分支与上游同步，上游更新后在 fork 上把 `dev` rebase 进 `dev-fcl`（或 cherry-pick 补丁提交）即可，无需改动本仓库。

## CI

`.github/workflows/build.yml`：

| 触发 | 行为 |
| --- | --- |
| `workflow_dispatch` / 每周五 UTC 18:00 定时 | 克隆 FCL fork 的 **`dev-fcl` 分支**（含 glslang、SPIRV-Cross、xxhash、perfetto、flat_hash_map 子模块）→ 构建 → 上传 artifact + 更新滚动预发布 **`dev-latest`** |
| push 到 main | 仅构建 + 上传 artifact（构建配置变更时验证） |

产物 AAR 文件名固定为 `MobileGlues-release.aar`，可直接放入 FCL 的 `FCL/libs/` 替换内置版本；构建所用的上游 commit 记录在 artifact 名与 release 标题中。

固定下载链接：`https://github.com/FCL-Team/MobileGlues-Build/releases/download/dev-latest/MobileGlues-release.aar`

## 本地构建

```bash
git clone https://github.com/FCL-Team/MobileGlues-Build.git
cd MobileGlues
git clone --depth 1 --branch dev-fcl --recurse-submodules --shallow-submodules \
  https://github.com/FCL-Team/MobileGlues.git MobileGlues/MobileGlues
./gradlew :MobileGlues:assembleRelease
```

产物在 `MobileGlues/build/outputs/aar/`。

## 维护提示

- 上游 dev 若修改构建配置（`ndkVersion`、`compileSdk`、CMake 要求等），需同步更新 `MobileGlues/build.gradle.kts`（该文件是上游配置的本地副本 + assets 叠加）。
- 补丁维护直接在 FCL-Team/MobileGlues 的 `dev-fcl` 分支上进行；上游更新后 rebase `dev` → `dev-fcl`。
- AGP / Gradle 版本固定在根 `build.gradle.kts`（AGP 8.11.2）与 `gradle/wrapper/gradle-wrapper.properties`（Gradle 8.14.4），与 amethyst 一致。
