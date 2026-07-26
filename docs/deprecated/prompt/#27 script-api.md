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
