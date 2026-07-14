```java
package xiao.customgun.core.api.gun;

public record GunManagerGroup(String managerGroupTag,
                              @NotNull IGunActionManager gunActionManager,
                              @NotNull IGunAttackManager gunAttackManager,
                              @NotNull IGunInventoryManager gunInventoryManager,
                              @NotNull IGunStateManager gunStateManager) {
}
```