package by.radioegor146;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class TestsGenerator {
    
    @TestFactory
    public Collection<DynamicTest> generateTests() throws URISyntaxException {
        Collection<DynamicTest> dynamicTests = new ArrayList<>();
        File[] tests = new File(TestsGenerator.class.getClassLoader().getResource("tests").getFile()).listFiles();
        for (File test : tests)
            if (test.isDirectory())
                dynamicTests.add(DynamicTest.dynamicTest("Test #" + test.getName(), new ClassicTest(test)));
        return dynamicTests;
    }
}
