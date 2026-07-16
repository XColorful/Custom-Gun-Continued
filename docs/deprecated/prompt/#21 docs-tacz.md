> #21 提示词

```
现在有三个项目的工作目录，分别是Custom-Gun-Continued, BattleRoyale, TACZ，各自的代码和文档都可以通过IDEA提供的MCP浏览

背景说明：
- Custom-Gun-Continued是基于TACZ的重构
- Custom-Gun-Continued的设计思想基于BattleRoyale文档所示
- TACZ仓库里没有TACZ的文档
- BattleRoyale的文档在根目录\docs\README.md下
- Custom-Gun-Continued的架构与BattleRoyale

你需要完成以下工作：
- 完成Custom-Gun-Continued的\docs\tacz\architecture目录的文档
- 由于TaCZ跟BattleRoyale都没有做成像Custom-Gun-Continued严格隔离core和client的架构，因此文档体系跟BattleRoyale一样是单个\docs-tacz\architecture\Home.md
- 由于Custom-Gun-Continued是基于BattleRoyale的经验后制作的，编写文档的时候以Custom-Gun-Continued的编写模式为准
- 由于\docs-tacz\的性质，不需要创建\docs-tacz\api，即对于\docs-tacz\architecture\api\api-index.md不需要创建超链接
- 我手动编写了\docs-tacz\TaCZ Migration Mapping.md，可能有助于你了解TaCZ的架构，但\docs-tacz\architecture下的内容不需要提及跟Custom-Gun-Continued的差异，只需要描述TaCZ自身的架构即可
```

```
需要澄清的点：
- 不需要创建\docs-tacz\api\目录
- \docs-tacz\architecture\api\api-index.md是需要完成的，只是里面的内容不需要添加到\docs-tacz\api的链接
- 因此，\docs-tacz\architecture\Home.md里API的部分应该像BattleRoyale/Custom-Gun-Continued一样给一个到api-index.md的链接
- Custom-Gun-Continued的architecture文档里，只对特别指明的Minecraft类以及非列表项使用了``markdown格式，其余的模组自己的类是不需要的

需要修改的点：
- 对于列表项的键，去除\docs-tacz\architecture\Home.md里对TaCZ模组自己的类的``markdown语法包围，列表项的值里提到的类不属于这个范围
- 缩进使用"\t"，不要用空格
- \docs-tacz\architecture\下应该跟BattleRoyale和Custom-Gun-Continued类似，不只是只有Home.md一个文件，可以包含一些链接跳转，尽管只是创建一个空文件占位；对于这一点，可以浏览\docs\architecture\core下的完整目录结构来了解
```

```
需要修改的点：
- 事件API（EventPoster, EventRegister）是CGC重构后的设计，已经写在\docs\里，而\docs-tacz\是写TaCZ的文档，里面不要包含CGC的设计
- 比如EntityKineticBullet，他是TaCZ的类，虽然对应枪射物，但是描述时使用TaCZ的名称，只不过可以加括号来链接到\docs\下的文件；对\docs-tacz\文档内其他地方也是这个要求
- 例如TaCZ的./init，并没有采用CGC重构后的./init/registry目录的结构，\docs-tacz\的文档是以TaCZ的结构为准，需要检查并修正\docs-tacz\文档里所有犯了该问题的地方
```

```
- 对于链接到\docs\的链接，markdown直接以[IBulletVictimBlock](/docs/architecture/core/Home.md)这样不带".."的格式即可
- 即对于链接，如果在当前目录或者子目录的，以"./"开头，如果涉及父目录的，则直接以"/docs/"或者"/docs-tacz/"开头
- 例如"\docs-tacz\architecture\Home.md"里的block/entity，### 方块本身就带上了./block目录，子目录不需要重复父目录，应该写成"entity：方块实体"
- 列表项前面有一行说明加上冒号的，则这行描述跟下面的列表项之间不要有多余的空行，例如"\docs-tacz\architecture\Home.md"里### 游戏操作里就是有格式问题的
- 链接到\docs\下的链接，引用模式写成“迁移映射为\[]()”的格式，否则会误以为是参见TaCZ内部设计
- 例如./client/model/bedrock，只需要一个列表项“- bedrock：基岩版模型类”即可，不需要把目录下所有的类都列出来；判断标准是是否只需要了解这个文件夹的说明即可了解架构；否则将导致\docs-tacz\architecture\Home.md有1k行，而\docs\architecture\core\Home.md加上client\Home.md也只有700行
- TaCZ没有ServerCommand和CommandArg，\docs-tacz\下的文档以TaCZ的架构为准，不得混淆CGC独有的架构
- 网络部分存在不必要的冗余，CGC的文档作为示范，CGC架构相似但没有列出全部的网络包类型
```

```
我手动修改了\docs-tacz\architecture\Home.md：
- 能在映射表查的就删掉了迁移映射说明，对于重大重构的我保留了迁移说明

以修改后的中文版为准，修改英文版
```

```
我手动修改了\docs-tacz\architecture\api\api-index.md，以修改后的中文版为准，修改英文版
```