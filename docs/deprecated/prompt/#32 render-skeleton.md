> #32 提示词

```
现在需要研究TaCZ的渲染体系并写文档。

上下文快速交代：
- CGC的工作流是在1.20.1分支修改后，同步更新至高版本分支，涉及forge接口的会做进forge-compat平台抽象层
- 由于MC原版渲染在1.20.1-最新版变动频繁且差异大，因此暂时不考虑全版本统一的写法
- TaCZ渲染体系至少包含了枪械渲染，配件渲染，本地动画状态机，BlockBench导出的基岩版模型读取，基岩版动画，子弹实体渲染，配件/子弹/子弹盒物品渲染，Lua脚本
- CGC对渲染的重构的方向为仅重命名+更改类所在目录分类，迁移映射见\docs-tacz\TaCZ Migration Mapping.md
- 除了渲染体系以外，剩余部分CGC几乎完成了80%以上的部分，所有内容的框架均已确定，如需要执行相关查询，则直接以现有文档和代码实现为准
- TaCZ读取的POJO类跟CGC不同，CGC的Resource体系是统一的ResourcePojo+ResourcePojoManager模型
- 你可以使用IDEA提供的MCP来获取类之间的引用关系

现在需要调查TaCZ的渲染体系，并为CGC重构做准备，你需要完成以下内容：
- 文档Home首页，用Mermaid画出各模块关系总图，可参考\docs-tacz\architecture\resource\modifier\Home.md和\docs\architecture\core\entity\shooter\modifier\Home.md
- 渲染体系内部分工、渲染流程、渲染跟模型Pojo、动画Pojo的关系
- 包括但不限于以上提到的方面，文档需要对渲染体系研究透彻，涵盖足以让人了解和重构/移植该渲染体系的内容
- 不需要提及TaCZ渲染体系或命名的问题，文档不需要专门写CGC如何重构
- 只需要完成中文文档，英文文档暂时不需要写

格式要求：
- Markdown列表开头不使用加粗
- 对于有冒号的列表，冒号左边不使用加粗
- 表格语法里"|"之间跟文本不要有空格
- TaCZ的渲染体系文档写在\docs-tacz\architecture\client\render目录下
- CGC语义化重构后的渲染体系文档写在\docs\architecture\client\render目录下

你需要遵守：
- 可以自由浏览阅读代码，但不能修改
- 只准在规定目录下编写文档，其余部分不得修改
- 合理设置标题层级，不得滥用二级标题，不得把标题当数字列表用
- 不需要提及CGC里哪些是TODO或待实现
- 如涉及已有文档而不再赘述，可以链接到已有文档；如果目标文档不在当前目录及子目录下，则链接以根目录"\docs..."开头，而不用".."
- 不得大量复制代码到文档里，考虑使用文本描述或Mermaid图来表示关系，不得使用Class diagrams来规避“不得大量复制代码”的限制
- 不得通过“列出全部枚举”或“罗列堆砌代码实现部分”的方式来作说明
- TaCZ的渲染体系文档不需要提及CGC的重构，只需呈现TaCZ相关的内容分
- CGC的文档不需要反复提及对TaCZ的变更，CGC的渲染文档对TaCZ的差异目前应只有语义化重命名和类存储的目录管理差异

现在开始调查并编写文档，全部完成后通知我检查。
```

## 

```
\docs-tacz\的文档里混了CGC重构后的类名，检查并更正为TaCZ的类名
```

## 

```
/fork 现在需要列出渲染体系里所有类/包目录，写到\docs\architecture\client\render\TODO.md

需要完成：
- 所有待移植的类/包目录
- 移植的先后顺序

要求如下：
- 对于迁移映射里已经指定了对应目录的，则使用迁移映射里的
- 对于迁移映射里没有的，则在TODO里移植的目标位置留空
- 对于一个包目录/部分，使用一个单独的表格，左边是tacz类/包目录，右边是目标移植目录/留空
- 每一个表格算一个“先后顺序”的单位，用标题来表示不分先后顺序的部分，前后标题为移植顺序刚需
- 对每个表格，简要说明其对应docs-tacz文档Home.md里的什么地方，以及（如有）对应docs文档Home.md的什么地方
- 可以以Mermaid里的一个subgraph为一个单位，如果已完成，则仍然写上cgc对应的类/包目录
```

```
移植顺序移到开头，添加勾选框，添加到二级标题的跳转，以及回到开头的跳转
```

# 

```
我现在需要完成Custom Gun Continued分支26.2neoforge(PR #32)针对Minecraft 26.2移除了MultiBufferSource而需要的移植。

上下文快速交代：
- CGC是跨版本开发的架构，1.20.1完成代码后，多版本同步更新，该手法基于作者开发Custom BattleRoyale的经验
- CGC目前已经完成 #32 1.20.1-26.1.2的移植，现在需要完成26.2的部分
- 你可以通过IDEA提供的MCP来浏览Custom BattleRoyale的文档，位于BattleRoyale仓库根目录./docs/下
- Custom BattleRoyale针对26.2的移植有留下文档，在./docs/architecture/client/26.2-rendering-system-investigation-report.md

以下人工信息辅助你完成任务:
- 目前在CGC一共搜索到17个文件包含MultiBufferSource的import，由于26.2已经没有了MultiBufferSource，这些文件是一定需要修改的
- Custom BattleRoyale的渲染仅限于方块渲染和单面绘制，与CGC里自定义基岩版模型渲染不同，但对于原版API调用方式仍有一定参考价值
- 对于IRenderHandEvent的使用场景，如果原本使用MultiBufferSource而修改后改用SubmitNodeCollector，那么应使用IRenderHandEvent#getMultiBufferSource_SubmitNodeCollector这个接口
- 默认Custom BattleRoyale的代码都是通过编译且正常运行的
- CBR针对26.2的移植，可以通过调取创建"26.2-rendering-system-investigation-report.md"的git commit之后的几次commit来浏览，commit名为"Port level renderer to 26.2 render system"和Port block renderer to 26.2 render system"
- 调用IDEA提供的MCP时注意区分CGC和CBR的文档、代码

修改限制说明：
- 你可以自由浏览Custom Gun Continued和Custom BattleRoyale项目的源码、文档、git历史信息
- CGC的dev.xcolorful.customgun.client.compat.minecraft.BlockEntityWithoutLevelRenderer是一个占位用的，对子类renderByItem的修改应同步应用于父类
- 只针对原本使用MultiBufferSource的场景进行修改，一定会涉及与已有代码的冲突；尽可能不新增辅助函数，直接在已有的使用处进行修改
- 由于已经完成了1.20.1-26.1.2的差异，且26.1.2与26.2的变动不大，目前默认只需要处理MultiBufferSource的移植
- 如果处理完MultiBufferSource而还有编译不通过的，可以自行研究如何移植
- 你不得切换git worktree，当前待修改的内容已经是从26.1.2合并到26.2的
- 你不得修改git仓库信息，完成任务后先通知我检查

任务具体说明：
- 借助Custom BattleRoyale之前移植留下的文档和git变动历史来研究原版渲染变更
- 在Custom Gun Continued根目录./docs/architecture/client/render/下新增一个报告文档，描述CGC对MultiBufferSource的使用以及原版的变更的调查报告
- 完成对MultiBufferSource的移植，需要通过编译
- 注意上下文长度限制，你可以自行决定是否安排多Agent来完成该任务

现在开始执行任务
```

```
现在需要额外写文档。

你基于刚才修改中学习到的原版变化，在Custom Gun Continued根目录./docs/architecture/client/render/下额外写一个文档：
- 本次移植相关内容不是刚需
- 描述本次移植中研究到的26.1.x到26.2渲染的差异，使其能用于以后更新渲染的时候，能够再次辅助移植
- 由于用于辅助用途，显然需要包含详细说明而不能省略；对于源码具体变更可以增加“去哪找/见源码”的说明，而不要直接大量复制MC源码
- 如有必要，可以包含Mermaid图来呈现渲染体系变更，例如./docs/architecture/core/entity/shooter/modifier/Home.md的图

你需要完成的要求包括但不限于以上提到的。
- 请自行研究该文档应该提供哪些内容，怎么叙述便于Agent下次学习

现在开始研究并编写，完成后通知我检查。
```
