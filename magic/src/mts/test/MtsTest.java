package mts.test;

import mts.ClassMeta;
import mts.State;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static mts.Hello.loadAndScanJar;
import static org.junit.Assert.assertEquals;

public class MtsTest {

    public static final String TEST_JAR = "../reality/mts_test.jar";

    @Test
    public void findSingletons() throws IOException, ClassNotFoundException {
        State state = new State();
        loadAndScanJar(new File(TEST_JAR), state);


        assertEquals(2, state.singletons.size());
    }


    @Test
    public void findBeans() throws IOException, ClassNotFoundException {
        State state = new State();
        loadAndScanJar(new File(TEST_JAR), state);

        Map<String, ClassMeta> result = new HashMap<>();
        state.info.entrySet().stream()
                .filter(x -> x.getValue().isSpringBean())
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        assertEquals(2, result.size());
    }
}
