> #20 提示词

```
现在有三个项目的工作目录，分别是Custom-Gun-Continued, BattleRoyale, TACZ，你必须遵守以下要求：
- 所有的修改都不准使用git提交，不准使用git commit
- 不得切换分支或worktree
- 不得修改git仓库信息
- 不允许在BattleRoyale和TACZ项目目录下进行修改，把这两个仓库当成只读的
- 允许使用git来浏览项目历史来获取更多信息，但执行的git相关操作严禁对仓库造成任何修改
- 当遇到需要由我确认才能继续进行的内容时，立即停止并向我报告

背景说明：
- Custom-Gun-Continued是基于TACZ的重构
- Custom-Gun-Continued的设计思想基于BattleRoyale文档所示
- TACZ仓库里没有TACZ的文档
- BattleRoyale的文档在根目录\docs\README.md下，其中\docs\architecture\Home.md为架构文档，\docs\api\下的文件夹为主要API结构（跟代码api包保持同构）
- Custom-Gun-Continued目前尚未编写architecture文档
- Custom-Gun-Continued的项目设计背景在根目录README.md有说明；Custom-Gun-Continued尚未完成渲染部分，完成了渲染以外部分的框架，其中部分模块已经完整实现

Custom-Gun-Continued新架构说明：
- 采用了模组主类同目录下core和client目录严格隔离，以代替模组主类放在core下，而只有client放在主类同目录下的架构
- core和client包的目录结构是同构的，如都有api, command, config, item, init等子目录
- 关于Custom-Gun-Continued主要的设计思想，见BattleRoyale目录下的\docs\architecture\design\下的文件

以下说明助于辅助完成工作：
- Custom-Gun-Continued是基于TACZ的重构，包含了类名修改（更正语义），或者将原先的功能改名+移动到更合适的目录下，如有需要，你可以自由浏览两个项目的代码文件
- 由于Custom-Gun-Continued的设计思想跟BattleRoyale高度重合，当书写文档时，通常BattleRoyale有相应的实现：例如字符串的Tag类在文档里都是不写的，遇到Custom-Gun-Continued里API的包含若干static final字符串的xxxTag类，也效仿BattleRoyale不写进文档的做法
- 我已用IDEA提供MCP功能来方便浏览三个项目

你需要完成以下工作：
- 效仿BattleRoyale的文档体系，完成Custom-Gun-Continued下的\docs\architecture\Home.md、\docs\architecture\core、\docs\architecture\client
- 编辑规范跟BattleRoyale要求相同，指导文档在BattleRoyale\docs\Editing Standards.md
- 只需完成文档的中文部分，不需要完成英文（即“# English”后的部分），但每个文件都要保留该一级标题以及开头的“[English](#English)”
- core和client下的文档，书写包目录时仍然保持_./core/包名_，而不要省略core和client，这是为了使文档各处都显式强调core/client分离的架构
- 对于文本需要链接到\docs\api下的，可以效仿BattleRoyale的模式，在\docs\api\core\或\docs\api\client\下创建相应的空文件来占位
- 对于主-子管理器的同构分形设计，在文档中要完成相应的Mermaid图，示范参考BattleRoyale的\docs\architecture\common\game-framework.md，由于设计模式完全相同，Mermaid的编写顺序，使用的形状等范式都与BattleRoyale保持相同

目前有两个显式的，需要完成像game-framework.md一样完成Mermaid图的同构分形结构：
- xiao.customgun.core.gun.GunManager（xiao.customgun.core.api.gun.IGunManager）
- xiao.customgun.core.projectile.ProjectileManager（xiao.customgun.core.api.projectile.IProjectileManager）
- 以上两个结构的相应内容写在\docs\architecture\core\gun\gun-framework.md和\docs\architecture\core\projectile\projectile-framework.md
```

## 

```
/fork 我刚才对文档core部分进行了完整的审阅和修改，core部分的文档以我最近一次的commit为最终定稿
- 你可以从最新版的文档了解术语命名，同时也已经在\docs\Term Translation.md列出

完成以下工作：
- 对照我core的文档，更正、修改并补充client模块的文档，使其对其core部分的文档
- 本次允许的修改范围在\docs\architecture\client和\docs\api\client，其余部分均为参照且不得修改

补充说明：
- API目录下创建的接口，只包含package和接口实现即可，不需要带上import语句（参考现在core修改后的版本即可）
- 所有的修改都不准使用git提交，不准使用git commit
- 我新建了git分支，你不得切换分支或worktree，不得修改git仓库信息
```

```
现在完成\docs\wiki\command\下的若干文件，按照\docs\wiki\command\Attachment lock command.md的格式：
- 每一个三级标题对应一个指令
- 服务端指令和客户端指令用不同的二级标题，如无客户端指令则省略客户端的二级标题
- 先描述指令功能，紧接着列表为参数
```

```
现在完成\docs\wiki\configuration\下的若干文件，\docs\wiki\configuration\Ammo config.md给出了完成的格式
- 一级标题下写"路径：``"
- 每一项的格式跟之前指令相同
- 最后在```toml里默认值（需翻越代码得知），不需要包含comment
```

```
- 缺少# English的部分，需要补上
- 整数类型如无限制则不需要写0~2147483647
- 诸如Render config的枚举类型需要CrosshairType补全
```

## 

```
我刚才对core和client的architecture文档做了审阅和最终修改，目前版本为定稿（除API文档外）

效仿BattleRoyale \docs\architecture\api\api-index.md，完成以下工作：
- \docs\architecture\core\api\api-index.md
- \docs\architecture\client\api\api-index.md
- 如有链接，则在\docs\api目录下创建相应文件以供链接
```