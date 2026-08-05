package xiao.customgun.neoforgeclient.input;

import net.neoforged.neoforge.client.settings.KeyConflictContext;
import xiao.customgun.client.api.input.IKeyConflictContext;

public abstract class NeoKeyConflictContext implements IKeyConflictContext {

    public static NeoKeyConflictContext convert(Type type) {
        return switch (type) {
            case UNIVERSAL -> Universal.INSTANCE;
            case GUI -> Gui.INSTANCE;
            case IN_GAME -> InGame.INSTANCE;
        };
    }

    private final KeyConflictContext keyConflictContext;

    private NeoKeyConflictContext(KeyConflictContext keyConflictContext) {
        this.keyConflictContext = keyConflictContext;
    }

    @Override public boolean isActive() {
        return this.keyConflictContext.isActive();
    }
    @Override public boolean conflictsWith(IKeyConflictContext otherContext) {
        return this.keyConflictContext.conflicts((KeyConflictContext) otherContext.getKeyConflictContext());
    }
    @Override public KeyConflictContext getKeyConflictContext() {
        return this.keyConflictContext;
    }

    private static class Universal extends NeoKeyConflictContext {
        public static final Universal INSTANCE = new Universal();
        public Universal() {
            super(KeyConflictContext.UNIVERSAL);
        }
    }
    private static class Gui extends NeoKeyConflictContext {
        public static final Gui INSTANCE = new Gui();
        public Gui() {
            super(KeyConflictContext.GUI);
        }
    }
    private static class InGame extends NeoKeyConflictContext {
        public static final InGame INSTANCE = new InGame();
        public InGame() {
            super(KeyConflictContext.IN_GAME);
        }
    }
}
