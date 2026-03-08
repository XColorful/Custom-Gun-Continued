package xiao.customgun;

public class CustomGunClient {

    protected static boolean initialized;

    public static void init() {
        if (initialized) return;

        initialized = true;
    }
}
