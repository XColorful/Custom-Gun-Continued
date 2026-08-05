```java
package dev.xcolorful.customgun.core.api.gun;

public interface IGunMainManager extends IGunSubManager {
    boolean registerRuntimeGroup(GunManagerGroup gunManagerGroup);
    @NotNull GunManagerGroup getManagerGroup(String managerGroupTag);
}
```