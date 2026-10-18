# Graupel · 霰

[中文](README.zh_CN.md) · [日本語](README.ja.md)

Graupel is an independently installable variant of [Hail](https://github.com/aistra0528/Hail), an Android app for freezing applications. Original code and attribution belong to the upstream authors; this fork is distributed under the same [GPL-3.0 license](LICENSE).

## Changes

- Separate application ID: `io.github.localbuild.graupel`; can coexist with Hail.
- Home sorting can be saved independently as A–Z or first-install order (oldest first), with pinned apps first.
- Enabling or disabling biometric login requires system authentication. Returning from the background locks the interface again; unavailable authentication does not bypass the lock.
- Day and night launcher icons follow the system theme. The night icon inverts black/white, with a slightly dimmer, less saturated blue to compensate for its dark background. The installer uses the original daytime icon.
- Frozen indicators can be selected in Settings; grayscale and the blue snowflake before app names are independent. The corner badge stays fixed at 18 dp.
- App icon size is adjustable from 48 to 96 dp. Home columns adapt to the available width and move excess items to the next row.
- Home and Apps sorting use a compact, single-choice dialog instead of a cascading toolbar submenu.
- Three navigation tabs, with version, source and license information in Settings.
- Bounded settings ripple and row-width selection menus that expand inline below the clicked row.
- No links to official Hail release/update distribution channels in the app.

For freezing modes, permissions and usage, see the [upstream documentation](https://github.com/aistra0528/Hail#readme).

## Build

Requires JDK 21 and Android SDK 36. Use the included Gradle wrapper:

```sh
./gradlew testDebugUnitTest assembleDebug
./gradlew assembleRelease
```

Debug builds use the `.debug` application ID suffix. Release builds are unsigned unless a private `signing.properties` file is provided. Copy `signing.properties.sample`, replace its placeholders and keep the signing key outside the repository. `storeFile` is resolved relative to the `app` directory. Keep the same signing key for future updates to an installed Graupel release.

Signing keys, local SDK configuration, build outputs and credentials must remain untracked. No signing material is included in this repository.

## License

Based on [aistra0528/Hail](https://github.com/aistra0528/Hail), upstream commit `966b09633d25ddc53bc8ff889693c9d1c1564c3b`. See [LICENSE](LICENSE) for GPL-3.0 terms. Dependencies retain their respective licenses.
