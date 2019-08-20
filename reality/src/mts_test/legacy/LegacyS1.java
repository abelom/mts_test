package mts_test.legacy;

public class LegacyS1 {
    private final static LegacyS1 INSTANCE = new LegacyS1();

    public static LegacyS1 getInstance() {
        return INSTANCE;
    }

    public void hello() {
    }
}
