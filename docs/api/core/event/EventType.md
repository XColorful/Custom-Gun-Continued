```java
package dev.xcolorful.customgun.core.api.event;

public enum EventType {
    // tick
    SERVER_TICK_EVENT(false),
    CLIENT_TICK_EVENT(true),
    // entity
    ENTITY_JOIN_LEVEL_EVENT(false),
    // living entity
    LIVING_KNOCKBACK_EVENT(false),
    // player
    PLAYER_CLONE_EVENT(false),
    PLAYER_START_TRACKING_EVENT(false),
    // resource
    ADD_SERVER_RELOAD_LISTENER_EVENT(false),
    ADD_CLIENT_RELOAD_LISTENER_EVENT(true),
    TAGS_UPDATED_EVENT(false),
    DATAPACK_SYNC_EVENT(false);

    public final boolean isClientSideOnly;
    EventType(boolean isClientSideOnly) {
        this.isClientSideOnly = isClientSideOnly;
    }
}
```