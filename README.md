# 自定义枪械永续 | Custom Gun Continued
<p align="center">
  <img src="./logo/title.png">
</p>

[中文](#自定义枪械永续) | [English](#custom-gun-continued)

# 自定义枪械永续

😎[wiki](https://github.com/XColorful/Custom-Gun-Continued/wiki) | 📄[docs](https://github.com/XColorful/Custom-Gun-Continued/tree/HEAD/docs)

`本模组目前处于开发阶段，功能尚不完整，暂无法正常投入使用`

本模组是基于采用 [GPL-3.0-only](https://www.gnu.org/licenses/gpl-3.0.txt) 许可证的 [TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8 的衍生作品；由于 [CC-BY-NC-ND-4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/)，本模组**不包含原资产**。

---

`该模组需要安装在服务端和客户端`

## 主要特色
<p align="left">
 <img src="./pic/attachment_gun.png">
</p>

本模组在 TaCZ 的基础上进行了深度的重构：

### 跨版本支持

本模组从零开始重构，整体架构引入平台抽象层，从而：
- 降低了模组移植到 Minecraft 新版本的复杂度
- 模组更新会同步支持多版本

### 重写的资源体系

由于 TaCZ 1.1.4 起的枪包基于 Minecraft 原版资源体系，因此可以充分利用原版资源管理机制：
- 数据包和资源包遵循原版规范路径安装，移除了合并安装在指定目录的限制
- 枪包作为 Minecraft Mod 的扩展内容，其分发和使用受 Mojang 相关规则约束，因此**所有枪包都必须免费分发**
- 原版资源包上架平台（如 CurseForge / Modrinth）后即可支持启动器**自动下载与管理**
- 支持以`.jar`格式便捷分发，也支持资源包在客户端独立安装、数据包按存档单独安装

本模组针对枪包进行了优化：
- 统一使用流式解析代替基于树状的反序列化，**大幅提升读取性能**
- 对枪包进行了几乎全字段重命名，使其名称更对应实际用途
- 兼容旧字段名称读取

## 内容披露

衍生内容：
- [TaCZ](https://github.com/MCModderAnchor/TACZ)：本模组是基于采用 [GPL-3.0-only](https://www.gnu.org/licenses/gpl-3.0.txt) 许可证的 [TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8 的衍生作品；由于 [CC-BY-NC-ND-4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/)，本模组不包含原资产
- [自定义大逃杀](https://github.com/XColorful/BattleRoyale)：本模组大量复用了采用 [GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.txt) 许可证的[自定义大逃杀](https://github.com/XColorful/BattleRoyale)代码
- [LuaJ](https://github.com/luaj/luaj)：本模组包含采用 [MIT](https://opensource.org/license/mit) 许可证的 [LuaJ](https://github.com/luaj/luaj)
- [Apache Commons Math 3.6.1](https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/)：本模组包含采用 [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证的 [Apache Commons Math 3.6.1](https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/)

## 许可证

- 代码：[GPL-3.0-only](https://www.gnu.org/licenses/gpl-3.0.txt)
- 资产：[CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/)

# Custom Gun Continued

😎[wiki](https://github.com/XColorful/Custom-Gun-Continued/wiki) | 📄[docs](https://github.com/XColorful/Custom-Gun-Continued/tree/HEAD/docs)

`This mod is currently under development with incomplete features, and is not ready for normal use`

This mod is a derivative work based on [TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8, licensed under [GPL-3.0-only](https://www.gnu.org/licenses/gpl-3.0.txt). Due to the original art assets being licensed under [CC-BY-NC-ND-4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/), this mod **does not include any original assets**.

---

`This mod needs to be installed on both the server and the client.`

## Main Features
<p align="left">
 <img src="./pic/attachment_gun.png">
</p>

This mod performs a deep refactor on top of TaCZ:

### Cross-Version Support

This mod is rebuilt from scratch, introducing a platform abstraction layer into the overall architecture, thereby:
- Reducing the complexity of porting the mod to new Minecraft versions
- Allowing mod updates to support multiple Minecraft versions simultaneously

### Rewritten Resource System

Since gunpacks in TaCZ 1.1.4 and later are based on the vanilla Minecraft resource system, they can make full use of Minecraft's native resource management mechanisms:
- Data packs and resource packs follow the standard vanilla directory structure, removing the requirement to bundle them together in a designated directory
- As extensions to a Minecraft Mod, gunpacks are subject to Mojang's rules regarding their distribution and use; therefore, **all gunpacks must be distributed for free**
- Once vanilla resource packs are published on platforms (such as CurseForge / Modrinth), launchers can support **automatic downloading and management**
- Supports convenient distribution via `.jar` format, as well as standalone client-side installation of resource packs and per-world installation of data packs

This mod features targeted optimizations for gunpacks:
- Consistently using streaming parsing instead of tree-based deserialization, **significantly improving parsing performance**
- Renaming almost all fields in gunpacks to better reflect their actual purposes
- Maintaining compatibility with legacy field names when reading

## Content disclosures

Derivative content:
- [TaCZ](https://github.com/MCModderAnchor/TACZ): This mod is a derivative work based on [TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8, licensed under [GPL-3.0-only](https://www.gnu.org/licenses/gpl-3.0.txt). Due to the original art assets being licensed under [CC-BY-NC-ND-4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/), this mod does not include any original assets
- [Custom BattleRoyale](https://github.com/XColorful/BattleRoyale): This mod reuses substantial amounts of code from [Custom BattleRoyale](https://github.com/XColorful/BattleRoyale), licensed under [GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.txt)
- [LuaJ](https://github.com/luaj/luaj): This mod includes [LuaJ](https://github.com/luaj/luaj), licensed under [MIT](https://opensource.org/license/mit)
- [Apache Commons Math 3.6.1](https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/): This mod includes [Apache Commons Math 3.6.1](https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/), licensed under [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0)

## License

- Code: [GPL-3.0-only](https://www.gnu.org/licenses/gpl-3.0.txt)
- Assets: [CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/)