package xiao.customgun.client.api.input;

/**
 * 封装 KeyConflictContext
 */
public interface IKeyConflictContext {

    boolean isActive();
    boolean conflictsWith(IKeyConflictContext otherContext);

    Object getKeyConflictContext();

    enum Type {
        UNIVERSAL,
        GUI,
        IN_GAME
    }
}
