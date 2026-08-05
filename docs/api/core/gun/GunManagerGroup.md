```java
package dev.xcolorful.customgun.core.api.gun;

public record GunManagerGroup(String managerGroupTag,
                              @NotNull IGunActionManager gunActionManager,
                              @NotNull IGunAttackManager gunAttackManager,
                              @NotNull IGunInventoryManager gunInventoryManager,
                              @NotNull IGunScriptManager gunScriptManager,
                              @NotNull IGunStateManager gunStateManager) {
}
```