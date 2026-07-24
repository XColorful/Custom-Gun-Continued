> #26 提示词

```
现在需要研究TaCZ的Attachment/Modifier/Cache体系并写文档。

上下文快速交代：
- 目前重构的大致方向为枚举->具体类（强类型）来代替任意字符串->接口（扩展性）
- 语义化重命名，明确AttachmentCacheProperty为ShooterGunPropertyCache，这个缓存是绑在ILivingShooter生命周期的
- Cache的体系有读取数据、写入缓存、计算缓存、拿缓存值，并不是所有缓存都是数值类型，还有别的值类型
- 这个体系可能还涉及LuaJ脚本更新

需要了解的内容包括但不限于以下类，列出仅用于加速查找：
- xiao.customgun.core.api.item.attachment.AttachmentModifierType
- xiao.customgun.core.api.entity.shooter.ShooterGunPropertyCache
- xiao.customgun.core.item.gun.GunPropertyManager
- xiao.customgun.core.resource.data.data.AttachmentData
- xiao.customgun.core.api.entity.ILivingShooter
- xiao.customgun.core.api.entity.ShooterProperty
- com.tacz.guns.resource.modifier.AttachmentCacheProperty
- com.tacz.guns.resource.modifier.AttachmentPropertyManager
- com.tacz.guns.resource.modifier.custom.*
- com.tacz.guns.resource.pojo.data.attachment.AttachmentData
- com.tacz.guns.api.GunProperties
- com.tacz.guns.api.entity.IGunOperator
- com.tacz.guns.entity.shooter.ShooterDataHolder

请务必全面了解信息，可通过IDEA提供的MCP来进一步获取调用逻辑
- CGC对tacz的重构有\docs-tacz\TaCZ Migration Mapping.md这个迁移映射文档来快速找到重构后的对应位置
- 可以通过迁移映射文档里重构后的语义来更清晰地了解tacz的体系（修复命名和包管理混乱的问题），但仅用于辅助和加速了解，CGC的重构不完全对应tacz
- 可借助\docs\的文档来了解CGC的重构和设计思路来了解CGC重构的目的和解决的tacz混乱的地方

我目前不清楚调查哪些方面，以下供参考，你调查后列出研究计划向我报告，然后再开始：
- 从配置文件读取、格式要求
- 缓存类的生命周期，创建、更新、事件通知脚本执行逻辑
- 缓存从配置文件读取、计算，Modifier接口类跟其他部分交互的Mermaid图
- 整个Modifier体系的闭包图/说明，包含所有调用方和调用线路
- 重构后的体系闭包图/说明，各个部分的命名含义
- 文档能提供的功能，至少应覆盖：为重构做准备，为别人了解这个体系提供帮助

可以对tacz每个部分单独写一个详细的文档说明，也需要写速览介绍和全局的文档
- tacz的部分不包含重构后的内容，写在\docs-tacz\architecture\modifier文件夹下的若干文件
- 重构后的部分写在\docs\architecture\core\modifier文件夹下的若干文件
- \docs\文档不包含tacz的内容，两个文件夹下的文档都应是一个完整的体系说明文档
```

```
需要写的文档不限于你刚才提到的，如有需要可以写更多文档信息
- 核心目的是把体系文档化，能用来指导重构和让别人了解清楚
- 介绍全局的文档开头务必带上Mermaid图来直观了解各模块交互，然后再文档说明。

现在开始调查，全部完成后通知我检查。
```

```
检查我最近6个commit的变动后的最终状态，更新的内容可能需要修改之前写的文档。
- CGC的部分移到了docs\architecture\core\entity\shooter\modifier
- TacZ的部分移到了\docs-tacz\resource\modifier
- 修改后的文档应仍保持在以上目录

几个设计要点：
- 现在明确了shooter modifier体系
- tacz的IAttachmentModifier（即AttachmentModifierType里的todo）已明确会移植成item/attachment/modifier下的类
- 目前shooter gun modifier只有一个attachment modifier参与，但明确了作用于shooter的gun属性，modifier构成是属于attachment物品下的
- AttachmentModifierType会设计成持有接口，具体类为item/attachment下的（作为枚举的非final字段）
- 目前shooter没有别的modifier，但是这个体系本身就更正了语义和架构清晰度
```

```
我新提交了两个commit，又更正了一些命名和确定了一些重构的方向，你需要浏览。

我现在还完成了一些修改，还未提交：
- 你可以读IDEA当前打开的文件来快速获取这些文件列表

现在有几个问题在之前写的文档里没有写清晰，可能需要添加额外的文件或修改Mermaid图来说明：
- tacz的IAttachmentModifier接口里函数有GunData作为参数，我认为这个可能属于读取base值，可能需要放在api/item/gun/modifier下或别的设计
- 重构后的IAttachmentModifier里eval接口负责计算，因为GunData拿的是base值，modifier实际上是从AttachmentData里读的，计算时的场景大概就是遍历iGun身上的配件然后逐个获取AttachmentData计算，所以IAttachmentModifier应该只有AttachmentData相关的参数
- 以上只是我比较模糊的认知，tacz原本的计算流程在文档里还不清晰

现在需要阅读并修改文档：
- 本次任务不准修改代码文件，我之后了解清楚了再继续重构
- 只准修改\docs\和\docs-tacz\

增加的内容可能需要包括：
- 专门针对缓存计算画个图（强调计算），而不是整个系统运作（例如不需要列出详细的调用方）
- 在tacz的文档里，GunData、AttachmentData、IAttachmentModifier这些已知的类是一定要出现的，只是我目前不知道整个计算系统
```

```
目前待定设计如下：
- 准备加一个跟xiao.customgun.core.resource.network._AttachmentInstallabilityCache同级的_AttachmentModifierCache，用来在reload后存一个attachment对应的xiao.customgun.core.util.ClassUtils.ArrayMap<AttachmentModifierType, K>，对标tacz原设计里AttachmentData里只有写了的Modifier的Map（通常一个配件就几个属性），解决快速遍历的问题；从而Manager只需要跟modifier cache层交互，只有在构造Cache的时候才调用modifier里从AttachmentData get modifier的接口
- 在ResourceApi级别加一个刷新缓存值的函数，ResourceApi负责调用xiao.customgun.core.resource.network以及client下持有的实例的缓存刷新，给这些类都加一个接口（原先的resetCache保持private）；这一点可能不太需要，目前不准备添加
- 如果默认AttachmentModifier服务于Gun的Property，应该像把IItemModifier改成IGunModificationModifier这样明确子接口服务的对象，AttachmentModifierType构造函数里指定服务的GunModification，但是GunModification不能是枚举，否则无法保证类型安全
- ShooterGunModifierCache或者新增的Gun的类/接口应该是需要全部modifier value默认值的，GunData的处理一定会放在这里
- 不指定gun和attachment的modifier唯一对应的话，以后可以引入ammo的modifier并集中在shooter/gun的modifier里；或者gun/attachment/ammo的modifier集中在shooter的modifier里
- 无论服务于item/gun/modifier还是shooter的，各种modifier最好都在构造函数里引入一个统一的服务对象的类，来保证类型安全
- IItemModifier限制在Item没有必要，这也不是MC原版的modifier，可以考虑去掉，从而直接指定modifier服务于shooter，但是要考虑绑在item/gun/modifier还是直接ShooterGunModifier
- 直接在IAttachmentModifier里加上GunData的初值获取最省事，但是不如AttachmentModifierType直接指定服务于某个ShooterGun具体类型（使attachment构造函数里两种具体类达成强类型匹配）

所以考虑直接把AttachmentModifierType枚举移到GunModifierType，让Modifier接口里含GunData的初值获取，而没有modifier，从而使得Gun的modifier可以被attachment和ammo修改
- AttachmentModifierType指定作用的GunModifierType
- 如果Ammo有modifier作用，就不方便做多弹种实时切换增益
- 但是可以把初值处理直接在ShooterGunModifierCache里做一层switch case对枚举对应的modifier赋上初值（实现原解耦目标）
- tacz原版还有GunProperty，这个似乎也没在之前的文档里

又想到让枚举指向具体类，具体类之间做泛型匹配
- 但主要矛盾在于想把initCache给分离掉，但是会丢掉强类型

所以目前最终的方案是让IAttachmentModifier额外implements IGunModifier，把非attachment职责移到别的接口，IAttachmentModifier作为全能易用门面，但是内部实现可以分离

1. 把目前的研究过程和分析再单独写一个文档到\docs\里，每个方案都要有Mermaid图来直观地看
2. 模仿现在最新的AdsModifier的模式，把其他Modifier也补充。对于eval不能复用父类函数的就先todo留空。AttachmentModifierType枚举的参数也改成相应的类。
```

```
刚才我修改的这个方案，本质上跟原来的IAttachmentModifier是一样的，手法出处可以引用已有的重构：
- tacz的IGunOperator胖接口被分类拆成ILivingShooter extends一堆接口，对外不变（仅重命名）
- IGun被拆分成一堆接口，对外不变（仅重命名），对内则由GunDataAccessor接口统一实现（用注释分隔）
- Gun/Projectile manager是内部实现分离的方案，对外接口有重命名以外的改动

我的内部实现可以分离的意思是既可以让AdsModifier类代理实现GunData的base值获取，也可以像我现在暂存区里实现的xiao.customgun.core.api.item.gun.modifier.IAdsModifier这样来转移。

1. 更新文档说明，明确跟原来的IAttachmentModifier的区别，胖接口拆分的手法和已有实现的餐卡，以及实现分离是代理或接口default代理的意思（不匹配就接口clash从而强制类型匹配），使得AdsModifier这样的具体类里没有了base值的职责；对这个强类型匹配也需要Mermaid图来明确接口和继承关系
2. 把AdsModifier等类的get base写成IAdsModifier这样的接口
```

```
1. IAdsModifier同类别的漏了GPL3说明，并且IAdsModifier同类的extends IGunModifier不要换行写

2. 文档里没看到
IItemModifier -> IItemModifier -> AttachmentModifier -> AdsModifier
IItemModifier -> IGunModifier -> AttachmentModifier -> AdsModifier
IItemModifier -> IGunModifier -> IAdsModifier -> AttachmentModifier -> AdsModifier
这样的菱形继承，以及在IAdsModifier和AdsModifier（流程中间和末尾）必须完成匹配泛型

3. 关于DamageCalculationModifier的问题在聊天栏跟我说明
```

```
1.文档讲述的顺序有问题，先讲tacz原设计/问题，然后ABCD方案，最后再是胖接口拆分和菱形继承的接口图
- 菱形图里也缺少IAdsModifier指向getbase实现的箭头
- 菱形继承图改成flowchart LR的方向，可以跟当前图里已有的拿来用，而不用专门单独每个接口重新造一个方块
2. DamageCalculationModifier的问题你还没在聊天栏跟我报告
```

```
我修改了零星接口图的顺序，使得线路没有交叉，你不要再修改。

Damage的问题我了解了，改成对每个伤害都应用修改。并且，所有的getBase都需要是copy的，如果getter没自动包含复制则需要手动构建一个。
完成对Damage的getBase的修改，以及对getBase是否有复制的检查（否则后续modifier eval污染pojo原始数据）
```
