> #76 提示词

# 

```
检查当前工作区的改动：

- 是否能降低原本增加的不必要的实体tick开销
- 是否不影响现有玩家tick
- 取消ILivingShooter的tick本身是否不影响IBulletVictimEntity？即ILivingShooter攻击ILivingShooter是否有影响？
```

# 

我先读一下当前工作区的改动文件。

现在检查 `IBulletVictimEntity`、knockback 重置，以及伤害计算是否依赖射手 tick 更新的状态。

我读完了改动和相关调用链。三个问题的结论如下。

## 1. 能否再降低 tick 开销

当前改动已经消除了**主要**的 tick 开销：非玩家生物在服务端会在 [LivingEntityMixin.java:63](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:63) 直接返回。原先每个生物每 tick 都要跑 6 个状态机，并写 8 个 `SyncedDataKey`（[LivingEntityMixin.java:66](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:66)-91）。这些 key 是 `syncMode ALL`，写入会触发脏实体同步。

还能压的只有两部分，但都不在 tick 上：

- **注入本身**：`tick` 的 RETURN 注入仍会对每个 LivingEntity（含客户端）每 tick 调用一次，代价是一次 `isClientSide` 判断加一次接口调用，可以忽略。
- **构造期分配（真正的剩余开销）**：每个 LivingEntity（服务端+客户端）都会 `new ShooterProperty` 和 11 个 Aspect 对象（[LivingEntityMixin.java:28](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:28)-48）。这是 O(全部生物) 的内存分配，与是否 tick 无关。要真正降低需要懒初始化或按需 holder，但 `cgc$getShooterProperty()` 是公共 API、任意 LivingEntity 都可能被调用，属于较大重构，不建议并进本次改动。

结论：tick 路径的优化空间已经基本吃掉了；继续优化应针对每实体对象分配，另开任务更合适。

## 2. 是否影响现有玩家 tick

不影响。`PlayerMixin` 返回 `true`，且已注册。`this.cgc$enableDefaultShooterTick()` 编译为接口调用，运行时虚分派会命中 Player 类上由 [PlayerMixin.java](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/PlayerMixin.java) 注入的覆盖方法，与原来 `ENABLE_DEFAULT_SHOOTER_TICK = true` 的玩家路径完全一致。

- `ServerPlayer`、Forge `FakePlayer` 都继承 Player，行为与改动前一致。
- 客户端 Player 虽然也返回 `true`，但 [LivingEntityMixin.java:59](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:59) 的 `isClientSide` 先返回，没有变化。

## 3. 取消非玩家 tick 是否影响 IBulletVictimEntity

**受击侧不受影响。** 受击逻辑完全事件/命中驱动，不读 tick 状态：

- 伤害由弹丸数据计算（[_ProjectileHit.java:181](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_ProjectileHit.java:181)）。
- 击退强度在命中时写入（[_ProjectileHit.java:94](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/projectile/impact/_ProjectileHit.java:94)-99），在 `LIVING_KNOCKBACK_EVENT` 中读取并重置（[BulletVictimKnockback.java:30](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/victim/BulletVictimKnockback.java:30)-37）。
- [LivingEntityMixin.java:228](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:228) 的 `cgc$onProjectileImpact` 不涉及 tick 字段。

所以 ILivingShooter 攻击 ILivingShooter，受击侧没有影响。

**受影响的是不 tick 的射手自己**：reload / bolt / heat / melee / aim 状态机不再推进，`ISynGunState` 的同步值停在默认值；`ShootState.fromLivingShooter` 读 `cgc$getSynAimingProgress`（[ShootState.java:112](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/ShootState.java:112)）会得到 0，导致非玩家射手的精度/动画状态错误。目前主模组所有开火、换弹、瞄准的服务端入口都来自玩家包（`C2SMessageShooter*`），因此实际只有通过 API 驱动自定义生物 AI 的枪包实体需要自己 override `cgc$enableDefaultShooterTick()`。

一个值得处理的点：默认 `false` 是静默降级，已有自定义实体若依赖默认 tick 会无声失效。按项目 Rule 10，建议在 `cgc$initLivingShooter` 或首次 `cgc$shoot` 时对“非玩家且未开启 tick”打一次 warn，或在 API 文档里明确要求 opt-in。

# 

```
对于扩展mod自行实现ILivingShooter，又要防止被强行塞入默认tick，是否有必要给一个cgc$shooterTick()方法？现在这样是否更好？
```

# 

结论：**保留现在的 boolean 更好，不建议加 `cgc$shooterTick()`。** 理由如下。

**1. 现状已经满足“不被强行塞默认 tick”**

- 默认 `false`：任何扩展生物不 opt-in 就完全不会被 tick。
- 非 LivingEntity 的自行实现：注入点在 `LivingEntity.tick`，这类实体根本不会经过这段逻辑。
- 即使是 `Player` 子类（继承了 `PlayerMixin` 的 `true`），也可以在自己的类里声明同名方法覆盖掉，不会被强制。

**2. `cgc$shooterTick()` 无法复用默认实现，会变成两个方法**

内置 tick 体依赖 `LivingEntityMixin` 的私有状态对象（`cgc$reload`、`cgc$aim` 等）。若把它变成接口方法：

- 接口 default 为空 → 所有 LivingEntity 不再 tick，仍需保留 gate，等于两套开关同时存在；
- 接口 default 为内置体 → 接口无法访问 mixin 私有字段，实现体只能仍放在 mixin 里；扩展方想“默认 tick + 额外逻辑”时又没有可调用的 super（扩展 mod 编译期面对的 `LivingEntity` 上没有该方法），最终还是要额外暴露一个 `cgc$defaultShooterTick()`。

两条路都导出一个结论：`cgc$shooterTick()` 单独存在没有收益，只会扩大 API 面。

**3. 对非 LivingEntity 实现者有误导性**

接口上放 `cgc$shooterTick()` 会让人以为核心会调用它，但核心的注入只落在 `LivingEntity.tick`。自定义 `Entity` 实现 ILivingShooter 时，这个方法是死的。

**4. “默认 tick + 额外逻辑”已有可行路径**

开启 gate，再在自己的 mixin 里对 `tick` 做 HEAD/TAIL 注入；需要和核心的 RETURN 注入排序时用 mixin priority。比核心再开一个 hook 更符合扩展 mod 的常规做法。

唯一建议补充的是文档语义：在 `ILivingShooterConfig` 上注明“类中的同名方法优先于 mixin 注入的实现；默认不 tick”，避免扩展方不知道该覆盖哪个。命名上 `cgc$shouldTickShooter()` 可能更直白，但只是口味问题，不影响结论。

补充一个反向提醒：如果将来需求变成“所有自定义生物默认 tick、只有原版生物不 tick”，方法开关本身就不合适了，应该走 entity type tag 或配置，而不是继续加接口方法。

# 

```
没有tick方法的话，注入的几个不使用的LivingShooterAspect类的开销体现在哪？应该只有构造函数时一次性分配内存？Aspect的构造函数都只是赋值，应该没有性能问题？
```

# 

我先确认这些 Aspect 的构造函数和字段初始化里到底做了什么，而不是凭印象判断。

确认了：11 个 Aspect 的构造函数全部只是 `super(...)` + 字段赋值，没有任何分配、注册、level 访问。[LivingShooterAspect.java:61](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/LivingShooterAspect.java:61) 也只是存两个引用。所以你的判断基本正确，但开销不止内存分配本身。

## 开销的实际构成

**1. 构造时的一次性分配（服务端 + 客户端都有）**

每个 LivingEntity 实例会构造：

|对象|数量|约大小|
|---|---|---|
|`ShooterProperty`|1|~140 B（10 个 long/double + 若干引用/基本类型）|
|Aspect 子类|11|~24 B 起，`Reload`/`Bolt` 多 2 个引用|
|小计|12 个对象|**~0.4–0.5 KB / 实体**|

这是实体构造期的一次性成本，之后不再有任何 CPU 开销。构造函数纯赋值，不会触发类初始化以外的逻辑。

**2. 实体实例本身的膨胀**

`LivingEntityMixin` 往每个 LivingEntity 对象里加了 13 个引用字段加 1 个 float（[LivingEntityMixin.java:28](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:28)-51），压缩指针下约 **56 B / 实体**。这部分对所有生物都生效，包括永远不会碰枪的牛和蝙蝠。

**3. GC 与堆占用**

- 分配是短暂的：每个实体 spawn 时 12 次小对象分配，年轻代回收，成本很低。
- 占用是长期的：12 个对象随实体一起存活到区块卸载。按 1 万个已加载生物算，约 **5 MB** 的额外堆（服务端和客户端各一份）。
- 高频刷怪/杀怪的农场会放大分配速率，但仍然是每实体一次，不是每 tick。

**4. 类加载**

Aspect 类和静态常量只加载一次，与实体数量无关。

## 所以“没有性能问题”是否成立

- **对 tick 性能：成立。** gate 关掉后，非玩家生物每 tick 只多一次 `isClientSide` 判断和一次接口调用，与这些对象无关。
- **对内存/GC：不能算完全无影响。** 构造函数便宜，但对象数量是 O(所有存活生物)，且服务端、客户端各一份；在大量已加载实体的场景下是实打实的堆占用和分配压力。
- 还有一个隐性点：客户端其实不需要这些 Aspect——远端实体只用到 `ISynGunState` 的同步值，而它读的是 `SyncedDataKey`，不经过 Aspect。目前客户端也在为每个实体构造这 12 个对象。

## 如果要继续优化

方向是**懒初始化**，而不是改 tick：

- 把 `shooterProperty` 和 Aspect 改为首次访问时创建（`cgc$getShooterProperty()`、`cgc$initLivingShooter()` 或服务端 gate 为 true 时）。
- 这样原版生物（gate 为 false 且没人调用 API）连 12 个对象都不会分配；而 `cgc$getShooterProperty()` 是公共 API，懒创建的 holder 仍能保证任意 LivingEntity 被调用时行为正确。
- 代价是每次访问多一层 null 判断/分支，以及要保证线程不相关（实体 tick 单线程，问题不大）。

这属于上一轮提到的“较大的重构”，收益是每实体约 0.5 KB 堆和 12 次分配，换来的复杂度需要你判断是否值得。就当前这次改动而言，不动它是对的。

# 

```
再加一个default boolean false，然后把初始化放在构造函数里，是否可行？
```

# 

我需要先确认两点：`cgc$getShooterProperty()` 的调用面，以及是否存在对任意 LivingEntity 调用枪械 API 的路径（决定字段置 null 后会不会 NPE）。

可行，但我不建议。技术上在这个分支确实能做：1.20.1 的 `LivingEntity` 只有一个构造器 `LivingEntity(EntityType, Level)`，所以 `@Inject(method = "<init>", at = @At("RETURN"))` 没有歧义，`this.cgc$xxx()` 也能正常虚分派到 `PlayerMixin` 的 `true`（flag 是常量方法，不依赖子类字段）。问题出在“字段变成可能为 null”之后。

## 三个具体问题

**1. 公开 API 会立刻出现 NPE 路径**

现在的约定是这些字段永远存在。加 gate 后：

- [GunProjectile.java:83](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/projectile/GunProjectile.java:83) 对任意传入的 `livingShooter` 调 `cgc$getGunModifierCache()`，而实现 [LivingEntityMixin.java:226](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/mixin/entity/LivingEntityMixin.java:226) 直接解引用 `cgc$shooterProperty`。
- `ILivingShooter.cgc$shoot()` 的默认重载读 `cgc$getShooterProperty().serverBaseTimestamp`（[ILivingShooter.java:34](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/api/entity/ILivingShooter.java:34)）。
- 脚本兼容层 `IGunScriptBackCompat` 有十几处只判空 shooter、不判空 property。
- 最现实的一条：[_WorldShooterHandler.java:39](app://localhost/epitaxy/core/src/main/java/dev/xcolorful/customgun/core/entity/shooter/world/_WorldShooterHandler.java:39) 对**任何手持枪的 LivingEntity**（包括捡到枪的僵尸）在跨维度时调用 `cgc$initLivingShooter()`，它会直接解引用 `cgc$shooterProperty`。

要堵住这些，就得在所有访问点加空判断，或者在 getter 里按需创建——但后者就是懒初始化，第二个 flag 和构造器注入就失去意义了。

**2. 两个 flag 是漏配陷阱**

tick 开关和初始化开关是正交的：一个实体可能不 tick 但仍通过 API 被射击/换弹/脚本访问。扩展方只要忘配其中一个，就是运行时 NPE，而且是静默的配置错误。这比现在多一个 flag 更糟，和 Rule 10 也不符。

**3. 构造器注入跨版本更脆弱**

本分支只有一个构造器没问题；但 `core1.20.5+`/`1.21` 的 `LivingEntity` 增加了委托构造器 `(EntityType, Level, BlockPos, BlockState)`，它内部调用 `this(type, level)`。此时注入不带描述符的 `<init>` 会在两个构造器的 RETURN 各执行一次，造成重复分配并把已初始化状态覆盖掉。因此每个分支都必须显式固定描述符——而字段初始化器没有这个问题。

## 结论

如果目标是减少非玩家实体的分配，**懒初始化是唯一自洽的方案**：在 `cgc$getShooterProperty()` / `cgc$getGunModifierCache()` 内部按需创建，保持 API 永远非空，不需要第二个 flag，也不需要构造器注入。代价是字段不能是 `final`，以及每次访问多一个分支。

如果不想承担这个复杂度，就保持现状：tick 开销已经没有了，剩下的只是每实体约 0.5 KB 的常驻分配，且只影响已加载实体。第二个 default false 加构造器初始化，属于“为了省 0.5 KB 引入一类新的空指针风险”，不值得。
