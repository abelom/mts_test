package mts_test.not_bean;

import mts_test.legacy.LegacyS1;

public class NotBean {

    public static void staticMethod() {
        LegacyS1 s1 = LegacyS1.getInstance();

        s1.hello();

    }

}
