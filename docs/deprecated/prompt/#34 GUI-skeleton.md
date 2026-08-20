> #34 提示词

```
现在需要完成dev.xcolorful.customgun.client.gui.tooltip.gun包下的tooltip part移植

以下信息辅助你完成任务：
- /docs-tacz/TaCZ Mirgration Mapping有tacz到CGC的迁移映射，如有需要，便于你查询其他api到哪找
- ClientGunTooltip已经重构拆解为多个tooltip part，ClientGunTooltip应该不怎么需要修改，你只需要完成包下的其他类即可
- 注意一些翻译键的差异，CGC重构后有相应枚举，可查阅dev.xcolorful.customgun.core.init.registry.ModCreativeTabs跟tacz的差异
- tacz的源码可以通过IDEA提供的MCP访问

请确保你了解了我为什么这么重构以及如何重构，如果有对重构后的结构不清晰的地方，查看已有代码/父类/实现接口即可，目前的移植是对照tacz原实现而没有添加新功能的

现在开始移植，完成后通知我检查。
```

## 

```
/fork 按照ClientGunTooltip里修改的方式，手动计算pY，从而去掉getYOffset接口
```

```
现在的GunAmmoInfoPart是我修改的最终版本，模仿我的手法，对其他gun tooltip检查并修改：
- 对于Nullable变量的获取，前面加上@Nullable
- 当一个变量不是刚需放在前面时，不要提前
- 例如currentAmmoCount，gunIndexInstance、gunData、boltType都是只为求这个值而使用的，则使用大括号的模式，从而达到可折叠的效用
- 对于从GunData这样的Resource获取数据，不得使用gunIndexInstance.getGunData().getBoltType()，每一步必须展开，如果获取接口有@Nullable，则不得省略，以提示用；Resource的getter没有@Nullable的默认不为空
- 我对AmmoInfoPart的功能进行了删减，你对其他tooltip part不要这样做，只做格式调整
- 注意到renderText、renderImage里调用的写法，非同类参数换行写

现在开始执行，完成后叫我检查
```

## 

```
/fork 现在效仿我修改后的GunTooltipMask和几个INSTANCE的模式，完成AttachmentTooltipMask及对应的INSTANCE
- description方式相同
- baseInfo、enchantmentInfo、guide_tip暂时留空
- detailInfo方式相同

注意这是模板复制，不要过度思考。

现在开始执行，完成后叫我检查
```
