Reference mapping between [TaCZ](https://github.com/MCModderAnchor/TACZ) 1.1.8 and the refactored [Custom Gun Continued](https://github.com/XColorful/Custom-Gun-Continued).

Use this document to:
- locate the corresponding implementation in the refactored project
- understand architectural changes introduced during the refactor
- assist source code navigation and migration

Use original TaCZ package names or type names to find their corresponding implementation in Custom Gun Continued.

This document is intentionally maintained as a single file to simplify searching for both developers and AI agents.

Notation:
- `...` — Java type (`.java` file)
- plain text — package, namespace, field or method
- `*` — wildcard
- _Deprecated_ — already deprecated in TaCZ or intentionally removed in the refactored implementation

## Common

### API
> ```java
> package com.tacz.guns.api;
> ```

|com.tacz.guns.api| |
|---|---|
|`@CacheModifiableByScript`|_Deprecated_|
|`@ValueModifiableAtRuntime`|_Deprecated_|

|com.tacz.guns.api|xiao.customgun.core.api.resource|
|---|---|
|`DefaultAssets`.\*|`ResourceTag`.NULL_LOCATION|
|`TimelessAPI`|`ResourceApi`|

|com.tacz.guns.api|xiao.customgun.client.api.resource|
|---|---|
|`TimelessAPI`|`ClientResourceApi`|

|com.tacz.guns.api|xiao.customgun.core.api.item|
|---|---|
|`GunProperties`.\*|gun.modifier.`GunModifierType`.\*|
|`GunProperties`.`RumtimeOnly`.\*|gun.modifier.`GunModifierType`.\*|
|`GunProperty`.name|gun.modifier.`GunModifierType`.typeName|
|`GunProperty`.type|gun.modifier.`IGunModifier`\<V>|

#### Common API

| |xiao.customgun.core.api.common|
|---|---|
|net.minecraftforge.fml.`LogicalSide`|`McLogicalSide`|
|net.minecraftforge.api.distmarker.`Dist`|`McSide`|

#### Entity API
> ```java
> package com.tacz.guns.api.entity;
> ```

|com.tacz.guns.api.entity|xiao.customgun.core.api.entity|
|---|---|
|`IGunOperator`|`ILivingShooter`|
|`ITargetEntity`|`IBulletVictimEntity`|
|`KnockBackModifier`|`IBulletVictimEntity`|
|`ReloadState`|`ReloadState`|
|`ShootResult`|`ShootResult`|

#### Event API
> ```java
> package com.tacz.guns.api.event;
> ```

|com.tacz.guns.api.event|xiao.customgun.core.api.event|
|---|---|
|common.`AttachmentPropertyEvent`|shooter.`ShooterGunModifierCacheEvent`|
|common.`EntityHurtByGunEvent`.`Pre`|projectile.`ProjectileHitEntityEvent`|
|common.`EntityHurtByGunEvent`.`Post`|projectile.`ProjectileHitEntityFinishEvent`|
|common.`EntityKillByGunEvent`|projectile.`ProjectileKillEntityEvent`|
|common.`GunDrawEvent`|shooter.`ShooterDrawEvent`|
|common.`GunFinishReloadEvent`|shooter.`ShooterReloadFinishEvent`|
|common.`GunFireEvent`|gun.`GunFireEvent`|
|common.`GunFireSelectEvent`|shooter.`ShooterSwitchFireModeEvent`|
|common.`GunMeleeEvent`|shooter.`ShooterMeleeEvent`|
|common.`GunReloadEvent`|shooter.`ShooterReloadEvent`|
|common.`GunShootEvent`|shooter.`ShooterFireEvent`|
|common.`KubeJSGunEventPoster`|_Deprecated_|
|server.`AmmoHitBlockEvent`|projectile.`ProjectileHitBlockEvent`|

|com.tacz.guns.api.event|xiao.customgun.core.api.minecraft|
|---|---|
|common.`GunDamageSourcePart`|damage.`CustomDamageType`.isPierce|

#### Item API
> ```java
> package com.tacz.guns.api.item;
> ```

|com.tacz.guns.api.item|xiao.customgun.core.api.item|
|---|---|
|attachment.`AttachmentType`|attachment.`AttachmentCategory`|
|builder.`*Builder`|builder.`*Builder`|
|builder.`BlockItemBuilder`|_Deprecated_|
|gun.`AbstractGunItem`|gun.`IGunRuntime`|
|gun.`FireMode`|gun.`FireModeType`|
|gun.`GunItemManager`||
|nbt.`*ItemDataAccessor`|\*.`*NBTAccessor`|
|`GunTabType`|gun.`GunCategory`|
|`IAmmo`|`IAmmo`|
|`IAmmoBox`|`IAmmoBox`|
|`IAnimationItem`|`IAnimationItem`|
|`IAttachment`|`IAttachment`|
|`IBlock`|`IBlock`|
|`IGun`|`IGun`|

|com.tacz.guns.api.item|xiao.customgun.core.api.item|
|---|---|
||ammo.`AmmoCategory`|
||ammobox.`AmmoBoxStatus`|
||attachment.`MagazineCategory`|
||gun.`FireSoundType`|
||gun.`MeleeType`|
||gun.`ReloadType`|

#### Modifier API
> ```java
> package com.tacz.guns.api.modifier;
> ```

|com.tacz.guns.api.modifier| |
|---|---|
|`CacheValue`|_Deprecated_|
|`ParameterizedCache`|_Deprecated_|
|`ParameterizedCachePair`|_Deprecated_|

|com.tacz.guns.api.modifier|xiao.customgun.core.api.item|
|---|---|
|`IAttachmentModifier`|attachment.modifier.`IAttachmentModifier`|

|com.tacz.guns.api.modifier| |
|---|---|
|`IAttachmentModifier`.`DiagramsData`||

|com.tacz.guns.api.modifier|xiao.customgun.core.resource|
|---|---|
|`JsonProperty`|data.data.`AttachmentData`.\*|

#### Resource API
> ```java
> package com.tacz.guns.api.resource;
> ```

|com.tacz.guns.api.resource|xiao.customgun.core.api.resource|
|---|---|
|`ResourceManager`|_Deprecated_|

#### Utility API
> ```java
> package com.tacz.guns.api.util;
> ```

|com.tacz.guns.util| |
|---|---|
|`LuaEntityAccessor`||
|`LuaNbtAccessor`||

#### Lua virtual machine library API
> ```java
> package com.tacz.guns.api.vmlib;
> ```

|com.tacz.guns.api.vmlib|xiao.customgun.core.api.script|
|---|---|
|`LuaGunLogicConstant`|`LuaGunLogicLib`|
|`LuaLibrary`|`LuaLibrary`|

|com.tacz.guns.api.vmlib|xiao.customgun.client.api.script|
|---|---|
|`LuaAnimationConstant`|`LuaAnimationLib`|
|`LuaGunAnimationConstant`|`LuaGunAnimationLib`|

### Block
> ```java
> package com.tacz.guns.block;
> ```

|com.tacz.guns.block|xiao.customgun.core.api.block|
|---|---|
|`TargetBlock`.onProjectileHit|`IBulletVictimBlock`.cgc$onProjectileImpact|

### Command
> ```java
> package com.tacz.guns.command;
> ```

|com.tacz.guns.command|xiao.customgun.core.command|
|---|---|
|`RootCommand`|`ServerCommand`|
|sub.`*Command`|sub.`*Command`|
|sub.`*`.String|`CommandArg`.String|

|com.tacz.guns.command|xiao.customgun.client.command|
|---|---|
||`ClientCommand`|

### Mod Compat
> ```java
> package com.tacz.guns.compat;
> ```

|com.tacz.guns.compat|xiao.customgun.core.compat|
|---|---|
|cloth|_Deprecated_|
|carryon||
|controllable||
|jei||
|kubejs|_Deprecated_|

|com.tacz.guns.compat|xiao.customgun.client.compat|
|---|---|
|ar||
|oculus||
|optifine||
|playeranimator|playeranimator|
|shouldersurfing||

### Config
> ```java
> package com.tacz.guns.config;
> ```

|com.tacz.guns.config|xiao.customgun.core.config|
|---|---|
|common.`AmmoConfig`|`AmmoConfig`|
|common.`GunConfig`|`GunConfig`|
|common.`OtherConfig`|`OtherConfig`|
|sync.`SyncConfig`|`SyncConfig`|
|util.`HeadShotAABBConfigRead`|sync.`HeadAABBData`|
|`PreLoadConfig`|_Deprecated_|

|com.tacz.guns.config|xiao.customgun.client.config|
|---|---|
|client.`KeyConfig`|`KeyConfig`|
|client.`RenderConfig`|`RenderConfig`|
|client.`ResourceConfig`|`ResourceConfig`|
|client.`SoundConfig`|`SoundConfig`|
|client.`ZoomConfig`|`ZoomConfig`|
|util.`InteractKeyConfigRead`|sync.`InteractFilterData`|

### Craft
> ```java
> package com.tacz.guns.crafting;
> ```

|com.tacz.guns.crafting.result|package xiao.customgun.core.recipe|
|---|---|
|result.`GunSmithTableResult`|`TableResult`|
|result.`RawGunTableResult`|`_TableResultRaw`|
|result.`RawGunTableResult`.extraData|_Deprecated_|
|`GunSmithTableIngredient`|`TableIngredient`|
|`GunSmithTableRecipe`|`TableRecipe`|
|`GunSmithTableSerializer`|`TableRecipeSerializer`|

### Debug
> ```java
> package com.tacz.guns.debug;
> ```

|com.tacz.guns.crafting.result|package xiao.customgun.core.recipe|
|---|---|
|`GunMeleeDebug`||

### Entity
> ```java
> package com.tacz.guns.entity;
> ```

|com.tacz.guns.entity|xiao.customgun.core.entity|
|---|---|
|shooter.`LivingEntity*`|shooter.`LivingShooter*`|
|shooter.`LivingEntityCrawl`|shooter.`LivingShooterProne`|
|shooter.`LivingEntityDrawGun`|shooter.`LivingShooterDraw`|
|shooter.`LivingEntityFireSelect`|shooter.`LivingShooterSwitchFireMode`|
|sync.core|sync|
|sync.core.`DataHolder`|sync.`SyncDataHolder`|
|sync.`ModSerializers`.RELOAD_STATE|sync.`Serializers`.RELOAD_STATE|
|sync.`ModSyncedEntityData`|`LivingShooterSyncKey`|
|sync.`ModSyncedEntityData`.THROWABLE_USE_TICK|_Deprecated_|
|`EntityKineticBullet`|projectile.`GunProjectile`|
|`EntityKineticBullet`.IEntityAdditionalSpawnData|projectile.`GunProjectile`.constructInitData|

|com.tacz.guns.entity|xiao.customgun.core.api.entity|
|---|---|
|shooter.`ShooterDataHolder`|`ShooterProperty`|
|`EntityKineticBullet`|`IGunProjectile`|
|`TargetMinecart`.onProjectileHit|`IBulletVictimEntity`.cgc$onProjectileImpact|

|com.tacz.guns.entity|xiao.customgun.forge.minecraft|
|---|---|
|sync.core.`DataHolderCapabilityProvider`|capability.`SyncDataCapabilityProvider`|

|com.tacz.guns.entity|xiao.customgun.core.api.entity|
|---|---|
|`EntityKineticBullet`|`IGunProjectile`|

|com.tacz.guns.entity.`EntityKineticBullet`|xiao.customgun.core.projectile|
|---|---|
||effect.`ProjectileEffectManager`|
||impact.`ProjectileImpactManager`|
||physics.`ProjectilePhysicsManager`|
||process.`ProjectileProcessManager`|
||`ProjectileManager`|

|com.tacz.guns.entity|xiao.customgun.client.api.entity|
|---|---|
|`EntityKineticBullet`.\*|`IClientGunProjectile`.\*|

### Event
> ```java
> package com.tacz.guns.event;
> ```

|com.tacz.guns.event|xiao.customgun.core.projectile|
|---|---|
|ammo.`BellRing`.onAmmoHitBlock|impact.`_WorldImpactHandler`.onHitBell|
|ammo.`DestroyGlassBlock`.onAmmoHitBlock|impact.`_WorldImpactHandler`.onHitGlass|
|`EntityDamageEvent`.onLivingHurt|impact.`_EntityImpactHandler`.onLivingHurt|

|com.tacz.guns.event| |
|---|---|
|`CommonLoadPack`|_Deprecated_|
|`HitboxHelperEvent`||

|com.tacz.guns.event|xiao.customgun.core.init|
|---|---|
|`LoadingConfigEvent`.\*|`ModConfig`.`Event`.\*|

|com.tacz.guns.event|xiao.customgun.client.init|
|---|---|
|`LoadingConfigEvent`.\*|`ClientModConfig`.`Event`.\*|

|com.tacz.guns.event|xiao.customgun.core.entity|
|---|---|
|`PlayerRespawnEvent`.onPlayerRespawn|shooter.player.`_PlayerShooterHandler`.autoReload|
|`PreventGunClick`.onLeftClickBlock|shooter.player.`_PlayerShooterHandler`.preventShootInteraction|
|`SyncBaseTimestamp`|`LivingShooterSyncHandler`.onPlayerJoinWorld|
|`TravelToDimensionEvent`.onTravelToDimension|shooter.world.`_WorldShooterHandler`.onTravelDimension|

|com.tacz.guns.event|xiao.customgun.core.api.event|
|---|---|
|`ServerTickEvent`.onServerTick|`CycledEvent`.`Handler`.static|

|com.tacz.guns.event|xiao.customgun.core.entity|
|---|---|
|`ChangeGunPropertyEvent`.internalOnAttachmentPropertyEvent|shooter.modifier.`ShooterGunModifierCache`.initAttachmentModifiers|
|`KnockbackChange`.onKnockback|victim.`BulletVictimKnockback`.onLivingKnockback|

|com.tacz.guns.event|xiao.customgun.core.entity|
|---|---|
|`SyncedEntityDataEvent`|`LivingShooterSyncHandler`|

|com.tacz.guns.event|xiao.customgun.forge.entity|
|---|---|
|`SyncedEntityDataEvent`.attachCapabilities|`_LivingShooterSyncData`.onAttachCapabilities|

| |xiao.customgun.core.event|
|---|---|
|net.minecraftforge.eventbus.api.`@SubscribeEvent`|custom.`CoreEventHandlers`|
|net.minecraftforge.common.`MinecraftForge`.EVENT_BUS.post|`EventPoster`.postCustomEvent|
|net.minecraftforge.eventbus.api.`IEventBus`.register|`EventRegister`.register|

### Initialize
> ```java
> package com.tacz.guns.init;
> ```

|com.tacz.guns.init|xiao.customgun.core.init|
|---|---|
|`CapabilityRegistry`|`CapabilityRegistry`|
|`CommandRegistry`|`CommandRegistry`|
|`CommonRegistry`|`CommonSetup`|
|`CommonRegistry`.registerAttributes||
|`CommonRegistry`.onAddPackFinders|_Deprecated_|
|`CompatRegistry`||
|`ModAttributes`|_Deprecated_|
|`ModBlocks`|_Deprecated_|
|`ModContainer`|_Deprecated_|
|`ModCreativeTabs`|registry.`ModCreativeTabs`|
|`ModDamageTypes`|registry.`ModDamageTypes`|
|`ModEntities`.BULLET|registry.`ModEntities`.GUN_PROJECTILE|
|`ModEntities`.TARGET_MINECART|_Deprecated_|
|`ModItems`|registry.`ModItems`|
|`ModLootModifiers`|_Deprecated_|
|`ModPainting`|_Deprecated_|
|`ModParticles`|registry.`ModParticles`|
|`ModRecipe`|registry.`ModRecipe`|
|`ModSounds`|registry.`ModSounds`|

### Inventory
> ```java
> package com.tacz.guns.inventory;
> ```

|com.tacz.guns.inventory|xiao.customgun.core.gui|
|---|---|
|`GunSmithTableMenu`|_Deprecated_|
|tooltip.`AmmoBoxTooltip`|tooltip.ammobox.`AmmoBoxTooltip`|
|tooltip.`AttachmentItemTooltip`|tooltip.attachment.`AttachmentTooltip`|
|tooltip.`BlockItemTooltip`|_Deprecated_|
|tooltip.`GunTooltip`|tooltip.gun.`GunTooltip`|

### Item
> ```java
> package com.tacz.guns.item;
> ```

|com.tacz.guns.item|xiao.customgun.core.item|
|---|---|
|`AmmoBoxItem`|ammobox.`AmmoBoxItem`|
|`AmmoItem`|ammo.`AmmoItem`|
|`AttachmentItem`|attachment.`AttachmentItem`|
|`DefaultTableItem`|_Deprecated_|
|`GunSmithTableItem`|_Deprecated_|
|`ModernKineticGunItem`|gun.`GunItem`|
|`ModernKineticGunItem`.`DefaultPropertyModification`|_Deprecated_|
|`TargetMinecartItem`|_Deprecated_|

|com.tacz.guns.item|xiao.customgun.client.api.item|
|---|---|
|`GunTooltipPart`|gun.`GunTooltipMask`|

|com.tacz.guns.item|xiao.customgun.core.api.gun|
|---|---|
|`ModernKineticGunItem`|`IGunManager`|
|`ModernKineticGunScriptAPI`|script.`GunScriptApi`|

|com.tacz.guns.item.`ModernKineticGunItem`|xiao.customgun.core.gun|
|---|---|
||action.`GunActionManager`|
||attack.`GunAttackManager`|
||inventory.`GunInventoryManager`|
||state.`GunStateManager`|
||`GunManager`|

### Loot
> ```java
> package com.tacz.guns.loot;
> ```

|com.tacz.guns.loot|_Deprecated_|
|---|---|
|`LootTableInjectorModifier`|_Deprecated_|

### Mixin
> ```java
> package com.tacz.guns.mixin;
> ```

|com.tacz.guns.mixin|xiao.customgun.core.mixin|
|---|---|
|common.`LivingEntityMixin`|entity.`LivingEntityMixin`|
|common.`ServerGamePacketListenerImplMixin`|network.`ServerGamePacketListenerImplMixin`|
|common.`ServerPlayerMixin`|entity.`ServerPlayerMixin`|
|common.`ServerPlayNetHandlerMixin`|network.`ServerGamePacketListenerImplMixin`|

### Network
> ```java
> package com.tacz.guns.network;
> ```

|com.tacz.guns.network|xiao.customgun.core.network|
|---|---|
|message.event.`*`|message.event.`*`|
|message.handshake.`*`|message.handshake.`*`|
|message.`*Message*`|message.`*Message*`|
|message.`ClientMessagePlayerCrawl`|message.`ClientMessagePlayerProne`|
|`LoginIndexHolder`|`LoginIndexHolder`|
|`NetworkHandler`|`NetworkHandler`|

|com.tacz.guns.network|xiao.customgun.client.network|
|---|---|
|message.event.`*`|message.event.`_*`|
|message.`*Message*`|message.`_*Message*`|

|com.tacz.guns.network|xiao.customgun.core.api.network|
|---|---|
|`IMessage`|message.`IMessage`|

|com.tacz.guns.network|xiao.customgun.core.util|
|---|---|
|`NetworkHandler`|`SendUtils`|

### Particle
> ```java
> package com.tacz.guns.particles;
> ```

|com.tacz.guns.particles|xiao.customgun.core.particle|
|---|---|
|`BulletHoleOption`|`BulletHoleOption`|

### Resource
> ```java
> package com.tacz.guns.resource;
> ```

|com.tacz.guns.resource|xiao.customgun.core.resource|
|---|---|
|filter.`*Filter`|data.recipefilter.`RecipeFilterData`|
|index.`Common*Index`|instance.data.`*IndexInstance`|
|manager.`AttachmentDataManager`|data.`DataManager`.`AttachmentDataManager`|
|manager.`AttachmentsTagManager`|data.`ModTagManager`.`*`|
|manager.`CommonDataManager`|_Deprecated_|
|manager.`JsonDataManager`|_Deprecated_|
|manager.`LazyJsonDataManager`|_Deprecated_|
|manager.`LootInjectionManager`|_Deprecated_|
|manager.`RecipeFilterManager`|data.`RecipeFilterDataManager`|
|manager.`ScriptManager`|data.`ScriptManager`|
|network.`DataType`|network.`SyncDataType`|
|pojo.data.attachment.`AttachmentData`|data.data.`AttachmentData`|
|pojo.data.attachment.`*`|data.data.attachment|
|pojo.data.block.`BlockData`|data.data.`BlockData`|
|pojo.data.block.`*`|data.data.block|
|pojo.data.gun.`GunData`|data.data.`GunData`|
|pojo.data.gun.`*`|data.data.gun|
|pojo.data.loot|_Deprecated_|
|pojo.data.recipe.`*`|data.recipe.`RecipeData`|
|pojo.data.recipe.`GunResult`|_Deprecated_|
|pojo.`*IndexPOJO`|data.index.`*Index`|
|serialize.`CommonAmmoIndexSerializer`.deserialize|instance.data.`AmmoIndexInstance`.fromPojo|
|serialize.`CommonAttachmentIndexSerializer`.deserialize|instance.data.`AttachmentIndexInstance`.fromPojo|
|serialize.`CommonBlockIndexSerializer`.deserialize|instance.data.`BlockIndexInstance`.fromPojo|
|serialize.`CommonGunIndexSerializer`.deserialize|instance.data.`GunIndexInstance`.fromPojo|
|serialize.`DistanceDamagePairSerializer`.deserialize|data.data.gun.bullet.damage.`_DistanceDamageData`.fromJsonReader|
|serialize.`GunSmithTableIngredientSerializer`.deserialize|data.recipe.recipe.`_TableIngredientData`.fromJsonReader|
|serialize.`GunSmithTableResultSerializer`.deserialize|data.recipe.recipe.`_TableResultData`.fromJsonReader|
|serialize.`IgniteSerializer`\<Ignite>|data.data.gun.`_BulletData`.fireAspect|
|`CommonAssetsManager`|`_AllDataManager`|
|`GunPackLoader`|_Deprecated_|
|`PackConvertor`|_Deprecated_|
|`PackMeta`|data.meta.`GunpackMeta`|
|`VersionChecker`|_Deprecated_|

|com.tacz.guns.resource|xiao.customgun.core.api.resource|
|---|---|
|manager.`INetworkCacheReloadListener`|`INetworkCacheReloadListener`|
|`ICommonResourceProvider`|`ResourceApi`|

|com.tacz.guns.resource|xiao.customgun.core.item|
|---|---|
|modifier.custom.`*Modifier`|attachment.modifier.`*Modifier`|
|modifier.`AttachmentPropertyManager`.eval|attachment.modifier.`AttachmentModifier`.evalSimpleModifierData|

|com.tacz.guns.resource|xiao.customgun.core.api.entity|
|---|---|
|modifier.`AttachmentCacheProperty`|shooter.modifier.`ShooterGunModifierCache`|

|com.tacz.guns.resource|xiao.customgun.core.entity|
|---|---|
|modifier.`AttachmentPropertyManager`|shooter.modifier.`ShooterGunModifierManager`|

|com.tacz.guns.resource|xiao.customgun.core.api.item|
|---|---|
|modifier.`AttachmentPropertyManager`.MODIFIERS|attachment.modifier.`AttachmentModifierType`|

|com.tacz.guns.resource|xiao.customgun.core.util|
|---|---|
|modifier.`AttachmentPropertyManager`.functionEval|`ScriptUtils`.eval|

|com.tacz.guns.resource|xiao.customgun.client.resource|
|---|---|
|network.`CommonNetworkCache`|network.`SyncDataCache`|

|com.tacz.guns.resource|xiao.customgun.core.api.item|
|---|---|
|pojo.data.gun.`Bolt`|gun.`BoltType`|
|pojo.data.gun.`ChargeType`|gun.`ChargeType`|
|pojo.data.gun.`FeedType`|gun.`AmmoFeedType`|

|com.tacz.guns.resource|xiao.customgun.core.api.entity|
|---|---|
|pojo.data.gun.`InaccuracyType`|`ShootState`|

|com.tacz.guns.resource|xiao.customgun.core.util|
|---|---|
|serialize.`PairSerializer`.deserialize|`JsonUtils`.readFloatArrayFast|
|serialize.`Vec3Serializer`.deserialize|_Deprecated_|

|com.tacz.guns.resource|xiao.customgun.core.resource|
|---|---|
||`ResourceFile`|
||`ResourceFileManager`|
||`ResourcePojo`|
||`ResourcePojoManager`|

### Sound
> ```java
> package com.tacz.guns.sound;
> ```

|com.tacz.guns.sound|xiao.customgun.core.sound|
|---|---|
|`SoundManager`|`SoundManager`|

### Utility
> ```java
> package com.tacz.guns.util;
> ```

|com.tacz.guns.util|xiao.customgun.core.util|
|---|---|
|block.`BlockRayTrace`|`RayTraceUtils`|
|block.`ProjectileExplosion`||
|dafafixer.`AttachmentIdFix`|_Deprecated_|
|math.`Easing`||
|math.`MathUtil`|`MathUtil`|
|math.`PerlinNoise`||
|math.`SecondOrderDynamics`||
|`ColorHex`|`ColorUtils`|
|`EntityUtil`|`RayTraceUtils`|
|`ExplodeUtil`||
|`GetJarResources`|_Deprecated_|
|`HitboxHelper`||
|`Md5Utils`|_Deprecated_|
|`PathHandler`|_Deprecated_|
|`ResourceScanner`|_Deprecated_|
|`TacPathVisitor`|_Deprecated_|

|com.tacz.guns.util|xiao.customgun.core.api.resource|
|---|---|
|`AttachmentDataUtils`.getAllAttachmentData|`ResourceApi`.getAllAttachmentIndexInstance|

|com.tacz.guns.util|xiao.customgun.core.api.event|
|---|---|
|`CycleTaskHelper`|`CycledEvent`|

|com.tacz.guns.util|xiao.customgun.core.resource|
|---|---|
|`AllowAttachmentTagMatcher`|network.`_AttachmentInstallabilityCache`|
|`AttachmentDataUtils`.getMagExtendLevel|data.data.`AttachmentData`.getMagazineCategory|
|`AttachmentDataUtils`.\*||

|com.tacz.guns.util|xiao.customgun.client.api.event|
|---|---|
|`DelayedTask`|`ClientDelayedEvent`|

|com.tacz.guns.util|xiao.customgun.client.util|
|---|---|
|`InputExtraCheck`|`ClientInputUtils`|
|`RenderDistance`||
|`RenderHelper`||

|com.tacz.guns.util|xiao.customgun.core.api.item|
|---|---|
|`LaserColorUtil`|`IAttachment`.getLaserColor|

|com.tacz.guns.util|xiao.customgun.core.api.projectile|
|---|---|
|`TacHitResult`|physics.`IProjectilePhysicsRuntime`.`EntityHitResult`|

## Client

### API (Client)
> ```java
> package com.tacz.guns.api.client;
> ```

#### Animation API (Client)
> ```java
> package com.tacz.guns.api.client.animation;
> ```

|com.tacz.guns.api.client.animation|xiao.customgun.client.api.animation|
|---|---|
|gltf||
|interpolator||
|statemachine||
|`ObjectAnimation`|`ObjectAnimation`|

#### Event API (Client)
> ```java
> package com.tacz.guns.api.client.event;
> ```

|com.tacz.guns.api.client.event|xiao.customgun.client.api.event|
|---|---|
|`BeforeRenderHandEvent`|render.`BeforeRenderHandEvent`|
|`RenderItemInHandBobEvent`.`BobHurt`|render.`ItemInHandBobEvent`.`Hurt`|
|`RenderItemInHandBobEvent`.`BobView`|render.`ItemInHandBobEvent`.`View`|
|`RenderLevelBobEvent`.`BobHurt`|render.`LevelBobEvent`.`Hurt`|
|`RenderLevelBobEvent`.`BobView`|render.`LevelBobEvent`.`View`|
|`SwapItemWithOffHand`|player.`SwapItemWithOffHandEvent`|

#### Gameplay API (Client)
> ```java
> package com.tacz.guns.api.client.gameplay;
> ```

|com.tacz.guns.api.client.gameplay|xiao.customgun.client.api.entity|
|---|---|
|`IClientPlayerGunOperator`|`ILocalShooter`|

|com.tacz.guns.api.client.gameplay|xiao.customgun.client.api.item|
|---|---|
|`LocalPlayerMelee`.MELEE_STOCK_ANIMATION|gun.`MeleeType`|

#### Other API (Client)
> ```java
> package com.tacz.guns.api.client.other;
> ```

|com.tacz.guns.api.client.|xiao.customgun.client.api.|
|---|---|
|`GunModelTypeManager`||
|`IThirdPersonAnimation`||
|`ThirdPersonManager`||

|com.tacz.guns.api.client.other|xiao.customgun.client.api.renderer|
|---|---|
|`KeepingItemRenderer`|`KeepingItemRenderer`|

|com.tacz.guns.api.client.other|xiao.customgun.client.api.item|
|---|---|
|`ThirdPersonManager`.\*|gun.`ThirdPersonAnimationType`.\*|

### Animation (Client)
> ```java
> package com.tacz.guns.client.animation;
> ```

|com.tacz.guns.client.animation|xiao.customgun.client.animation|
|---|---|
|screen.`RefitTransform`||
|statemachine.`GunAnimationConstant`|statemachine.`GunAnimationState`|
|statemachine.`ThrowableAnimationStateContext`|_Deprecated_|
|third.`InnerThirdPersonManager`|third.`InnerThirdPersonManager`|

### Command (Client)

| |xiao.customgun.client.command|
|---|---|
||sub.`_*Command`|
||`ClientCommand`|
||`ClientCommandArg`.String|

### Event (Client)
> ```java
> package com.tacz.guns.client.event;
> ```

|com.tacz.guns.client.event| |
|---|---|
|`CameraSetupEvent`||
|`ClientPreventGunClick`||
|`FirstPersonRenderEvent`||
|`FirstPersonRenderGunEvent`||
|`InventoryEvent`||
|`PlayerEnterWorld`|_Deprecated_|
|`PlayerHurtByGunEvent`||
|`PreventsHotbarEvent`||
|`RefreshClonePlayerDataEvent`||
|`ReloadResourceEvent`||
|`RenderCrosshairEvent`||
|`RenderHeadShotAABB`||
|`TickAnimationEvent`||
|`TooltipEvent`||

|com.tacz.guns.client.event|xiao.customgun.client.entity|
|---|---|
|`ClientHitMark`|`ClientHitMarkHandler`|

|com.tacz.guns.client.event|xiao.customgun.client.init|
|---|---|
|`CommonNetworkCacheEvent`.onClientPlayerLoggingIn|`ClientModEvent`.onClientLoggingIn|

| |xiao.customgun.client.event|
|---|---|
|net.minecraftforge.eventbus.api.`@SubscribeEvent`|custom.`ClientEventHandlers`|

### Gameplay (Client)
> ```java
> package com.tacz.guns.client.gameplay;
> ```

|com.tacz.guns.client.gameplay|xiao.customgun.client.entity|
|---|---|
|`LocalPlayer*`|shooter.`LocalShooter*`|
|`LocalPlayerCrawl`|shooter.`LocalShooterProne`|
|`LocalPlayerFireSelect`|shooter.`LocalShooterSwitchFireMode`|

|com.tacz.guns.client.gameplay|xiao.customgun.client.api.entity|
|---|---|
|`LocalPlayerDataHolder`|`LocalShooterProperty*`|

### GUI (Client)
> ```java
> package com.tacz.guns.client.gui;
> ```

|com.tacz.guns.client.gui|xiao.customgun.client.gui|
|---|---|
|components.refit||
|components.smith||
|components.`*`||
|overlay.`*Overlay`||
|toast.`GunLevelUpToast`|_Deprecated_|
|`GunPackProgressScreen`|_Deprecated_|
|`GunRefitScreen`|`GunRefitScreen`|
|`GunSmithTableScreen`|_Deprecated_|

|com.tacz.guns.client.gui|xiao.customgun.client.compat|
|---|---|
|compat.`ClothConfigScreen`|_Deprecated_|

### Initialize (Client)
> ```java
> package com.tacz.guns.client.init;
> ```

|com.tacz.guns.client.init|xiao.customgun.client.init|
|---|---|
|`ClientSetupEvent`.onClientSetup(FMLClientSetupEvent)||
|`ClientSetupEvent`.onClientSetup(RegisterClientTooltipComponentFactoriesEvent)|`ClientTooltipRegistry`.registerTooltips|
|`ClientSetupEvent`.onClientSetup(RegisterKeyMappingsEvent)|`ClientKeyMappingRegistry`.registerKeyMappings|
|`ClientSetupEvent`.onRegisterGuiOverlays||
|`ModContainerScreen`|_Deprecated_|
|`ModEntitiesRender`||
|`ParticleFactoryRegistry`|`ClientModParticles`|

|com.tacz.guns.client.init|xiao.customgun.client.resource|
|---|---|
|`ClientSetupEvent`.onClientResourceReload|`_AllAssetsManager`.onAddClientReloadListenerEvent|

### Input (Client)
> ```java
> package com.tacz.guns.client.input;
> ```

|com.tacz.guns.client.input| |
|---|---|
|`AimKey`|shooter.`AimKey`|
|`ConfigKey`|config.`ConfigKey`|
|`CrawlKey`|shooter.`ProneKey`|
|`FireSelectKey`|shooter.`SwitchFireModeKey`|
|`InspectKey`|shooter.`InspectKey`|
|`InteractKey`|player.`InteractKey`|
|`MeleeKey`|shooter.`MeleeKey`|
|`RefitKey`|player.`RefitKey`|
|`ReloadKey`|shooter.`ReloadKey`|
|`ShootKey`|shooter.`ShootKey`|
|`ZoomKey`|shooter.`ZoomKey`|

### Mixin (Client)
> ```java
> package com.tacz.guns.mixin.client;
> ```

|com.tacz.guns.mixin.client|xiao.customgun.client.mixin|
|---|---|
|ar||
|`AbstractButtonMixin`|gui.`AbstractButtonMixin`|
|`GameRendererMixin`|renderer.`GameRendererMixin`|
|`HumanoidModelMixin`|model.`HumanoidModelMixin`|
|`ItemInHandLayerMixin`|renderer.`ItemInHandLayerMixin`|
|`ItemInHandRendererMixin`|renderer.`ItemInHandRendererMixin`|
|`LanguageMixin`|_Deprecated_|
|`LocalPlayerMixin`|entity.`LocalPlayerMixin`|
|`MouseHandlerMixin`|`MouseHandlerMixin`|
|`PlayerModelMixin`|model.`PlayerModelMixin`|
|`SoundManagerPreparationsMixin`|sound.`SoundManagerMixin`|
|`StairBlockAccessor`||

### Model (Client)
> ```java
> package com.tacz.guns.client.model;
> ```

|com.tacz.guns.client.model|xiao.customgun.client.model|
|---|---|
|bedrock||
|bedrock.`BedrockModel`|`ModelObject`|
|listener.camera||
|listener.constraint||
|listener.model||
|papi||
|`Bedrock*Model`|`*ModelObject`|
|`FunctionalBedrockPart`||
|`GunModelConstant`||
|`SlotModel`||

|com.tacz.guns.client.model|xiao.customgun.client.renderer|
|---|---|
|functional.`AttachmentRender`||
|functional.`BeamRenderer`||
|functional.`LeftHandRender`||
|functional.`MuzzleFlashRender`|model.`MuzzleFlashRender`|
|functional.`RightHandRender`||
|functional.`ShellRender`||
|functional.`TextShowRender`||

|com.tacz.guns.client.model|xiao.customgun.client.api.model|
|---|---|
|`IFunctionalRenderer`|`IModelComponentRenderer`|

### Particle (Client)
> ```java
> package com.tacz.guns.client.particle;
> ```

|com.tacz.guns.client.particle|xiao.customgun.client.particle|
|---|---|
|`AmmoParticleSpawner`|`AmmoParticleSpawner`|
|`BulletHoleParticle`|`BulletHoleParticle`|

### Renderer (Client)
> ```java
> package com.tacz.guns.client.renderer;
> ```

|com.tacz.guns.client.renderer|xiao.customgun.client.renderer|
|---|---|
|block|_Deprecated_|
|entity.`EntityBulletRenderer`||
|entity.`TargetMinecartRenderer`|_Deprecated_|
|item.`*ItemRenderer`||
|item.`AnimateGeoItemRenderer`|item.`AnimateGeoItemRenderer`|
|item.`GunSmithTableItemRenderer`|_Deprecated_|
|other.`GunHurtBobTweak`|victim.`GunHurtBobTweak`|
|other.`HumanoidOffhandRender`|shooter.`HumanoidOffhandRender`|

|com.tacz.guns.client.renderer|xiao.customgun.client.api.textures|
|---|---|
|crosshair.`CrosshairType`|crosshair.`CrosshairType`|

### Resource (Client)
> ```java
> package com.tacz.guns.client.resource;
> ```

|com.tacz.guns.client.resource|xiao.customgun.client.resource|
|---|---|
|index.`Client*Index`|instance.data.`Client*IndexInstance`|
|manager.`DisplayManager`|assets.`DisplayManager`|
|manager.`GltfManager`|assets.`AnimationManager`.`GltfAnimationManager`|
|manager.`PackInfoManager`|assets.`GunpackInfoManager`|
|pojo.animation.bedrock.`BedrockAnimation`|assets.animation.`BedrockAnimation`|
|pojo.animation.bedrock.`*`|assets.animation.bedrock|
|pojo.animation.gltf||
|pojo.display.ammo.`AmmoDisplay`|assets.display.`AmmoDisplay`|
|pojo.display.ammo.`*`|assets.display.ammo|
|pojo.display.attachment.`AttachmentDisplay`|assets.display.`AttachmentDisplay`|
|pojo.display.attachment.`AttachmentLod`|assets.display.`_LodDisplay`|
|pojo.display.block.`BlockDisplay`|assets.display.`BlockDisplay`|
|pojo.display.gun.`GunDisplay`|assets.display.`GunDisplay`|
|pojo.display.gun.`Align`|assets.display.`_ModelNodeTextDisplay`.xOffsetScale|
|pojo.display.gun.`DefaultAnimationType`|_Deprecated_|
|pojo.display.gun.`*`|assets.display.gun|
|pojo.display.`IDisplay`|_Deprecated_|
|pojo.display.`LaserConfig`|assets.display.`_LaserDisplay`|
|pojo.model.`BedrockModelPOJO`|assets.model.`BedrockModel`|
|pojo.model.`BedrockVersion`|_Deprecated_|
|pojo.model.`*`|assets.model.bedrock|
|pojo.skin|_Deprecated|
|pojo.`CommonTransformObject`|_Deprecated_|
|pojo.`PackInfo`|assets.info.`GunpackInfo`|
|pojo.`TransformScale`|assets.display.`_ModelTransformScale`|
|serialize.`ItemStackSerializer`||
|serialize.`SoundEffectKeyframesSerializer`.deserialize|assets.animation.bedrock.animation.`_SoundEffects`.fromJsonReader|
|serialize.`Vector3fSerializer`|_Deprecated_|
|`ClientAssetLoadDispatcher`|_Deprecated_|
|`ClientAssetsManager`|`_AllAssetsManager`|
|`ClientIndexManager`|`_AssetsInstanceManager`|
|`GunDisplayInstance`|instance.assets.`GunDisplayInstance`|
|`InternalAssetLoader`||

|com.tacz.guns.client.resource|xiao.customgun.core.api.item|
|---|---|
|pojo.display.gun.`AmmoCountStyle`|gun.`AmmoCountType`|
|pojo.display.gun.`DamageStyle`|gun.`DamageDisplayType`|

|com.tacz.guns.client.resource|xiao.customgun.client.util|
|---|---|
|serialize.`AnimationKeyframesSerializer`.deserialize|`ClientJsonUtils`.readKeyFrames|

### Sound (Client)
> ```java
> package com.tacz.guns.client.sound;
> ```

|com.tacz.guns.client.sound|xiao.customgun.client.sound|
|---|---|
|`EntityTrackingGunSoundInstance`|`EntityTrackingSoundInstance`|
|`GunSoundInstance`|`ResourceSoundInstance`|
|`GunSoundInstance`.`TaczSound`|`ResourceSound`|
|`GunSoundPreload`|_Deprecated_|
|`SoundPlayManager`|`SoundPlayManager`|

### Tooltip (Client)
> ```java
> package com.tacz.guns.client.tooltip;
> ```

|com.tacz.guns.client.tooltip|xiao.customgun.client.gui|
|---|---|
|`Client*Tooltip`|tooltip.\*.`Client*Tooltip`|
