package xiao.customgun.forgeclient.input;

import net.minecraftforge.client.settings.KeyConflictContext;
import xiao.customgun.client.api.input.IKeyConflictContext;

public abstract class ForgeKeyConflictContext implements IKeyConflictContext {

    public static ForgeKeyConflictContext convert(Type type) {
        return switch (type) {
            case UNIVERSAL -> Universal.INSTANCE;
            case GUI -> Gui.INSTANCE;
            case IN_GAME -> InGame.INSTANCE;
        };
    }

    private final KeyConflictContext keyConflictContext;

    private ForgeKeyConflictContext(KeyConflictContext keyConflictContext) {
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

    private static class Universal extends ForgeKeyConflictContext {
        public static final Universal INSTANCE = new Universal();
        public Universal() {
            super(KeyConflictContext.UNIVERSAL);
        }
    }
    private static class Gui extends ForgeKeyConflictContext {
        public static final Gui INSTANCE = new Gui();
        public Gui() {
            super(KeyConflictContext.GUI);
        }
    }
    private static class InGame extends ForgeKeyConflictContext {
        public static final InGame INSTANCE = new InGame();
        public InGame() {
            super(KeyConflictContext.IN_GAME);
        }
    }
}
