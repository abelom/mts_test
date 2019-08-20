package mts_test.beans;

import mts_test.legacy.LegacyS1;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class BeanParent {
    @Autowired
    private Objects x;

    public void method() {
        LegacyS1.getInstance().hello();
    }
}
