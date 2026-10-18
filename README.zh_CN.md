# 霰 · Graupel

霰是 [Hail（雹）](https://github.com/aistra0528/Hail) 的独立安装变体，用于冻结 Android 应用。保留原作者署名，沿用 [GPL-3.0 许可证](LICENSE)。

- 独立包名 `io.github.localbuild.graupel`，可与雹并行安装。
- 首页排序独立保存，支持名称 A–Z、安装先后（从早到晚），置顶应用优先。
- 启用和关闭认证需先通过系统验证；从后台返回重新锁定，认证不可用时不放行。
- 桌面图标随系统日夜模式切换，夜间黑白反色，蓝色适当降低饱和度和亮度，减轻黑底造成的鲜艳感；安装界面使用原日间图标。
- 首页和应用页的排序使用紧凑单选框，选中即关闭，不再展开右上角二级菜单。
- 底部保留首页、应用、设置；版本、源码和许可证入口移至设置。
- 调整设置水波纹及等宽选项菜单，在点击条目下方原位展开，后续设置顺延。
- 应用内移除指向雹官方发行与更新渠道的入口。

冻结模式、权限和使用说明见[上游文档](https://github.com/aistra0528/Hail/blob/main/README.zh_CN.md)。

## 构建

需要 JDK 21、Android SDK 36：

```sh
./gradlew testDebugUnitTest assembleDebug
./gradlew assembleRelease
```

调试包有 `.debug` 包名后缀。发布构建默认不签名；如需签名，将 `signing.properties.sample` 复制为 `signing.properties` 并填写自己的配置。`storeFile` 相对于 `app` 目录解析。密钥放在仓库外，后续覆盖更新需沿用同一密钥。

仓库不包含签名密钥、密码、本地 SDK 配置或构建产物。详细说明见 [README](README.md)。
