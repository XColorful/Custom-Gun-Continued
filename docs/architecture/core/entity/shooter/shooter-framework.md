[English](#English)

# 射手框架

## 射手切面

一般流程：
- 检查射手状态（`ILivingShooter`）
- 调用枪械检查（`IGun`/`IGunRuntime`）
- 调用枪械操作（`IGun`/`IGunRuntime`） -> 射手状态

要点：
- `LivingShooterAspect`只负责射手状态，抽离枪械逻辑
- `LocalShooterAspect`跟`LivingShooterAspect`对齐
- 服务端`ShooterProperty`对应客户端`LocalShooterProperty.clientStateLock`状态锁
- 仅在限定端侧的调用方（如`LivingShooterAspect`/`LocalShooterAspect`），`McLogicalSide`才直接硬编码写死

示范（`LivingShooterBolt`/`LocalShooterBolt`）：
1. 手持枪械检查
2. 射手状态
	1. 状态锁
	2. 其余状态
3. `IGunRuntime`操作结果 -> Shooter状态
	1. 客户端状态锁
```java
// 服务端
public void bolt() {
    // 1. 检查手持枪械
    if (iGun == null) return;
  
    if ( // 2.1 检查状态锁
            // 判断是否正在射击冷却
            this.shoot.getShootCooldown() > 0
            // 检查是否正在换弹
            || this.shooterProperty.reloadStateType.isReloading()
            // 检查是否在切枪
            || this.draw.getDrawCooldown() > 0
    ) return;
    else if ( // 2.2
            // 检查是否在拉栓
            this.shooterProperty.isBolting
    ) return;
  
	// 3. IGunRuntime操作结果 -> Shooter状态
    this.shooterProperty.isBolting = iGun.startBolt(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
	if (!this.shooterProperty.isBolting) {
		// （可选）重置状态
	    return;
	}
	  
	this.shooterProperty.boltTimestamp = System.currentTimeMillis();
}
```
```java
// 客户端
public void bolt() {
    // 1. 检查手持枪械
    if (iGun == null) return;
  
    if ( // 2.1 检查状态锁
            this.localShooterProperty.clientStateLock) return;
    else if ( // 2.2
            // 检查是否在拉栓
            this.localShooterProperty.isBolting
    ) return;
    
	// 3. IGunRuntime操作结果 -> Shooter状态
	this.localShooterProperty.isBolting = true;
	// 3.1 锁上状态锁
	this.localShooterProperty.lockState(ISynGunState::cgc$getSynIsBolting);
  
    SendUtils.sendMessageToServer(new ClientMessagePlayerBoltGun());
    @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
    if (gunDisplayInstance != null) {
        var soundLocation = gunDisplayInstance.getGunSound(GunSoundType.BOLT_SOUND);
        SoundPlayManager.get().playGunSound(soundLocation, this.localShooter);
        // TODO AnimationStateMachine trigger INPUT_BOLT
    }
}
```

# English

## Shooter aspect
