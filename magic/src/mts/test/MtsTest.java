package mts.test;

import mts.ClassMeta;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static mts.Hello.loadAndScanJar;
import static org.junit.Assert.assertEquals;

public class MtsTest {

    public static final String TEST_JAR = "../reality/mts_test.jar";

    @Test
    public void findSingletons() throws IOException, ClassNotFoundException {
        Map<String, ClassMeta> info = new TreeMap<>();
        loadAndScanJar(new File(TEST_JAR), info);

        Map<String, ClassMeta> result = new HashMap<>();
        info.entrySet().stream()
                .filter(x -> x.getValue().isSingleton())
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));


        assertEquals(1, result.size());
    }


    @Test
    public void findBeans() throws IOException, ClassNotFoundException {
        Map<String, ClassMeta> info = new TreeMap<>();
        loadAndScanJar(new File(TEST_JAR), info);

        Map<String, ClassMeta> result = new HashMap<>();
        info.entrySet().stream()
                .filter(x -> x.getValue().isSpringBean())
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        assertEquals(2, result.size());
    }
}
