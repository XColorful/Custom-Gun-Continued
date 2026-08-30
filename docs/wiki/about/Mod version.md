[English](#English)

# 模组版本
> wiki 版本：`0.0.8`.1

## 版本号命名

### 版本号格式

模组版本号格式为：`年份`.`月份`.`版本数`
- 年份取末两位，如 2026年对应`26`
- 月份不补前导零，如 1 月对应`1`
- `版本数`每次更新加 1
- 仅在单个版本临时修复时，`版本数`后额外跟一个从 1 开始增长的`临时修复数`；当`版本数`增加时，该编号清空

> 模组首个正式版发布前，年份和月份为`0`

### 发布类型

|说明|版本号格式|全版本同步|全版本发布|GitHub Releases|CurseForge/Modrinth Channel|
|---|---|---|---|---|---|
|正式版发布|`Y.M.x`|✅|✅|自定义枪械永续 `Y.M.x`|Release|
|内容更新|`Y.M.x`|✅||开发版 `Y.M.x`|Beta|
|不稳定内容更新|`Y.M.x`|✅||开发版 `Y.M.x` (Pre-release)|Alpha|
|临时修复版|`Y.M.x`.`x`|❌|❌|开发版 `Y.M.x`.`x` (Pre-release)|Alpha|
|（早期）测试版发布|`0.0.x`|✅|✅|自定义枪械永续 `0.0.x` (Pre-releases)|Alpha|
|（早期）测试内容更新|`0.0.x`|✅||开发版 `0.0.x` (Pre-releases)|Alpha|

- 全版本同步：本次更新内容已经同步全部支持的版本，可以在源码仓库构建对应版本`.jar`文件
- 全版本发布：发布页包含全部支持的版本的`.jar`文件

### 更新日志

- 若两个相同类型的发布版本之间穿插了其他类型的版本，更新日志不再重复说明已涵盖的内容

## Wiki 版本号命名

### Wiki 版本号格式

wiki 版本号格式为：`模组版本`.`wiki版本数`
- `模组版本`为 wiki 更新时的模组[版本号格式](#版本号格式)（不含`临时修复数`）
- `wiki版本数`每次更新加 1
- 即使中/英文单独更新，下次更新 wiki 时仍然累计`wiki版本数`
> 例如中文 wiki 版本为`0.0.0`.0，英文更新后为`0.0.0`.1，则下一次 wiki 更新时使用的`版本数`应为 2

### Wiki 更新说明

- 当 wiki 更新随代码一并提交时，更新说明为对应的 commit 概要
- 否则，单独描述更新说明

# English
> wiki version: `0.0.8`.1

## Version naming

### Version format

The mod version format is: `Year`.`Month`.`Version number`
- Year takes the last two digits, e.g., 2026 corresponds to `26`
- Month does not include leading zeros, e.g., January corresponds to `1`
- `Version number` increments by 1 with each update
- For hotfixes restricted to a single version, an additional `Hotfix number` starting from 1 is appended after `Version number`; this number is cleared when `Version number` increments

> Before the first official release of the mod, both Year and Month are `0`

### Release types

|Description|Version format|All-version sync|All-version release|GitHub Releases|CurseForge/Modrinth Channel|
|---|---|---|---|---|---|
|Official release|`Y.M.x`|✅|✅|自定义枪械永续 `Y.M.x`|Release|
|Content update|`Y.M.x`|✅||开发版 `Y.M.x`|Beta|
|Unstable content update|`Y.M.x`|✅||开发版 `Y.M.x` (Pre-release)|Alpha|
|Hotfix version|`Y.M.x`.`x`|❌|❌|开发版 `Y.M.x`.`x` (Pre-release)|Alpha|
|(Early) Beta release|`0.0.x`|✅|✅|自定义枪械永续 `0.0.x` (Pre-releases)|Alpha|
|(Early) Beta content update|`0.0.x`|✅||开发版 `0.0.x` (Pre-releases)|Alpha|

- All-version sync: The content of this update has been synced across all supported game versions; corresponding `.jar` files can be built from the source code repository
- All-version release: The release page contains `.jar` files for all supported game versions

### Changelog

- If other release types intervene between two releases of the same type, the changelog will not repeat previously covered details
    
      
    

## Wiki version naming

### Wiki version format

The wiki version format is: `Mod version`.`wiki version number`
- `Mod version` corresponds to the mod [Version Format](#Version-format) at the time of the wiki update (excluding `Hotfix Number`)
- `Wiki Version Number` increments by 1 with each update
- Even if Chinese/English documentation is updated separately, `Wiki version number` continues to accumulate for the next wiki update
> For example, if the Chinese wiki version is `0.0.0`.0 and the English wiki is updated to `0.0.0`.1, the `Wiki version number` used for the next wiki update will be 2

### Wiki update description

- When a wiki update is submitted together with code, the update description should use the corresponding commit summary
- Otherwise, describe the update separately