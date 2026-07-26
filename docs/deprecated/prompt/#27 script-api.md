> #27 提示词

```
浏览git暂存区的\docs\architecture\core\entity\shooter\modifier\design-evolution.md，我修改了最终方案。

把新增的内容修改成跟前几个已有内容相同的书写格式和简洁度/表述方式，其中：
- 菱形继承链可以在缓存Class参数验证里就提及，之前的最终方案已经需要更改，之前的javadoc的描述在xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache#getValue的javadoc里有写，现在已经需要调整
- 新的写法的原理参考xiao.customgun.core.api.item.gun.modifier.IFireAspectModifier新增的内容，以及xiao.customgun.core.entity.projectile._GunProjectileConstructor里IFireAspectModifier.getValue(shooterGunModifierCache, AttachmentModifierType.FIRE_ASPECT)

你只需要修改\docs\architecture\core\entity\shooter\modifier\design-evolution.md、ShooterGunModifierCache的javadoc
```

```
把胖接口拆分和菱形继承的内容提前到缓存 Class 参数验证，并说明不行的原因是IGunModifier子接口当作Class<? extends IGunModifier<T, K, V>>参数时，该接口固定的泛型是不保留的，等于Class<IFireAspectModifier>而没有泛型，这是java语言的边界
最终方案改成了用IGunModifier子接口来getValue，利用子接口固定的泛型，使得编译器能验证左值V的类型，该模组作为API公开时，只需要重新编译即可得知api是否变化，而不需要等到运行时，编辑期检查即是为了这个用处
修改后的文档应该只有一个最终版本的标题

不要过度思考，代码暂存区只是临时修改，直接按照我上面说的意思改
```

```
再在最终方案补充一点，由于Class<? extends IGunModifier<T, K, V>>这个参数是没法通过的，只能修改成Class<? extends IGunModifier>，所以不能保证直接用该方法来存入错误类型，但是预期的调用方式已经确保了泛型V匹配，即使api变化而扩展模组没更新，则崩溃并重新编译找所有变化点即可更新版本
在做完以上修改后，将英文部分也同步中文版的修改
```

```
我更新了ShooterGunModifierCache的javadoc的说明，你调整一下表述，表达的内容跟我已经写的应该相同：
- 首先指出应该去哪找正确入口（像我现在给出包目录的link），文字加上红色来强调
- 其次开始描述原因：类型安全、编译器检查、以及编译器检查是为了自动检查api更新（无需运行时才看到error log），这些用ul列表
只需要修改该javadoc
```

```
现在javadoc修改好了，把design-evolution里文档里也改成像这样列表的几点来说明编译器检查和编译器检查的用处
```

```
帮我把IFireAspectModifier同包的接口都像IFireAspectModifier那样补上getValue和setValue
```

# 

```
浏览以下文件：
- xiao.customgun.core.api.projectile.IProjectileRuntime的StateCache
- xiao.customgun.core.api.entity.GunProjectileProperty
- xiao.customgun.core.api.entity.projectile.IGunProjectileStateAccess
- xiao.customgun.core.api.entity.projectile.GunProjectileDataAccessor
- xiao.customgun.core.api.entity.GunProjectilePropertyTag
- xiao.customgun.core.entity.projectile.GunProjectile

1. 你只需要看以上文件，不需要过多思考
2. 把StateCache里对于GunProjectileProperty新增的属性，补到以上文件相应位置，模仿已有的模式即可
```

```
- xiao.customgun.core.api.entity.projectile.GunProjectileNBTAccessor
- xiao.customgun.core.api.entity.projectile.IGunProjectileNBTAccess
- GunProjectile的addAdditionalSaveData

以上几处也在补充的范围，漏掉的属性补上
```

```
GunProjectileProperty仍然没有完成，GunProjectileProperty里的属性需要补到以上文件相应位置
```

> 小鲸鱼实在太傻了，烧钱的废物

# 

```
现在需要研究TaCZ的ModernKineticGunScriptAPI体系并写文档

上下文快速交代：
- TaCZ的modifier系统已经重构完成，如有需要则见\docs\architecture\core\entity\shooter\modifier下的文档及相关源码
- \docs-tacz\TaCZ Migration Mapping.md有重构和语义化重命名的迁移映射
- tacz枪械脚本硬编码的lua函数名在CGC已经移到GunScriptMethodType

需要了解的内容包括但不限于以下类，列出仅用于加速查找：
- TaCZ类：ModernKineticGunScriptAPI, ModernKineticGunItem, EntityKineticBullet, IGun, AbstractGunItem, ScriptManager
- CGC类：GunScriptApi, GunItem, GunManager, GunScriptManager, IGunScriptRuntime, ScriptManager, DataScript, IGun, GunProjectile, IGunProjectile

文档编写的语法跟modifier文档使用相同标准：
- 列表项冒号左边不使用加粗
- 表格文字跟左右的竖线之间不空格
- java类名/方法等需要使用``语法包起来，在中文部分，``语法包起来的跟前后中文不需要空格
- 标题不使用数字序号，不滥用二级标题，注意文档层次
- 文档的表述和简洁度参考modifier文档体系
- 尽可能避免复制代码到文档，文档是介绍体系而不是具体的实现
- 若形成体系，则使用直观的Mermaid图，而不是纯文字描述

你需要遵守：
- 请务必全面了解信息，可通过IDEA提供的MCP来进一步获取调用逻辑
- 注意思考深度，不要陷入循环反复思考，缺少信息或信息模糊则不要私自猜测
- 修改范围只涉及\docs\architecture\core\gun\script和\docs-tacz\architecture\item\script这两个目录
- \docs\architecture\core\gun\script\gun-script-manager.md目前不需要修改

你需要完成以下内容，目前暂不需要写英文文档：
- 脚本体系入口页需要整个体系的Mermaid图，涵盖从.lua读取到各处调用之间的关系/各模块交互，参考modifier文档的Home.md

文档其余内容目前尚不清楚调查哪些方面，以下供参考，你调查后列出研究计划并向我报告，然后再开始：
- tacz枪械脚本修改的属性跟modifier系统不清楚是否有关联，如果有关联，
- \docs-tacz\下的文档不包含CGC重构后的内容；\docs\下的内容仅为CGC重构后的内容
```

```
- tacz的ModernKineticGunScriptAPI里大而全的方法，在CGC里目前是考虑是否跟ILivingShooter的方法重复，从而同样的逻辑满足单一事实来源，或让脚本简单调用持有的ILivingShooter方法；对于枪械的操作逻辑CGC是打算分散到各GunSubManager里
- 不需要提及ScriptMethod的旧字段兼容
- 如果脚本体系有用到modifier，则可去了解下modifier最终方案里实现左值V编译器确定的方式，我看tacz里modifiyProperty似乎是直接指定类的方式。如果是在代码里调用方法，我可能会采用接口泛型的方式，如果是在脚本里调用，则我可能采用GunProperty里_IGunPropertyAccess那样的getter/setter方式；但这些目前都不确定

开始调查
```

```
- tacz的GunProperties的类型调用已经在CGC改成了IGunModifier同包子接口statc方法绑定泛型V来实现编译器检查，为已移植内容
- 你需要调查tacz这个脚本里的方法是否跟其entity/shooter/LivingEntity*的重复，如果重复，我是准备复用的，我在IGunAttackRuntime写了todo说明，从而避免脚本又写一遍entity/shooter/LivingShooterShoot及同类别
- tacz的文档需要补一个值类型的文件，对标CGC里对编译器类型的文档
- docs-tacz/architecture/item/script/Home.md已经存在，直接写在里面
- CGC的modifier文档不需要修改，如果需要引用，则让脚本的文档写markdown链接指过去

开始编写
```

```
- markdown链接对于父目录不使用..，而直接从根目录开始
- \docs-tacz\的文档不得专门开标题讲CGC，重构和改进是写在\docs\里的
- \docs\里不要把TODO写进体系里
- tacz的文档对比cgc的少了很多东西
```

```
- tacz文档的Home拆成多个文件
```

```
对这两个文档目录下的完成英文部分
- 英文使用的markdown语法跟中文版对齐，例如中文使用加粗或``或表格的，英文也使用
- 英文只需要一个# English标题，不需要额外的一级标题，不需要标题重复文件名
```

# 

```
对xiao.customgun.core.api.item.gun.modifier包下的每一个IGunModifier子类，像IArmorIgnoreModifier一样补充evalByScript，格式如下：
static @NotNull V evalByScript(GunScriptApi scriptApi, @NotNull V value) {  
    return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, value);  
}
```

# 

```
对xiao.customgun.core.api.item.gun.modifier包下的每一个IGunModifier子类的evalByScript方法，像IAdsModifier里一样补充GunModifierType参数
```

# 

```
浏览我最新的一次commit的变动，\docs\architecture\core\gun\script下的文档是否跟我最新的实现/体系有出入，如没有则向我报告即可，如有则修改：
- 格式和语法使用规范跟已有文档保持一致
- 英文部分也一并修改
```
