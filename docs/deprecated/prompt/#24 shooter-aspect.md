> #24 提示词

```
说明：
- 你可能需要了解本项目架构来获取更多对应的API信息
- \docs-tacz\TaCZ Migration Mapping.md有TaCZ到Custom-Gun-Continued的迁移映射
- xiao.customgun.core.entity.shooter.LivingShooterAim目前是定稿和示范，不要清理里面的TODO

以LivingShooterAim对比原TaCZ的修改为示范，完成xiao.customgun.core.entity.shooter下的其他文件
- 该目录以外的部分，如遇到没完成的，就跟LivingShooterAim一样留TODO位置
- 完成后通知我审阅
```

```
我完整修改了所有LivingShooter*，已经commit定稿，不得再修改xiao.customgun.core.entity.shooter下的类
- 你重新对照原版和我修改后的，学习编写模式和风格
- LocalShooterAim已完成的部分也是定稿，你接着完成该类以及xiao.customgun.client.entity.shooter下的类

特别指出几个需要注意的风格：
- 无论在哪里使用ResourceLocation，左值的类型都得用var（基于高版本有类名差异的自动兼容）
- gunLocation这样的RL获取需要专门一行，不要inline到iGun里
```

> 真是场酣畅淋漓的赤石😐，我活到头了吗，还有程序猿吗
