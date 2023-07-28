package by.radioegor146.source;

import by.radioegor146.ClassMethodList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

public class ClassMethodListTest {

    @Test
    public void testStaticList() {
        ClassMethodList list = ClassMethodList.parse(Arrays.asList(
                "asd/asd/asd",
                "asd/gas/asd"
        ));

        assertTrue(list.contains("asd/asd/asd"));
        assertTrue(list.contains("asd/gas/asd"));
        assertFalse(list.contains("asda/asd/asd"));
    }

    @Test
    public void testPatternList() {
        ClassMethodList list = ClassMethodList.parse(Arrays.asList(
                "gas/*/gas",
                "gas/kjh/**",
                "gas/**/tgas"
        ));

        assertTrue(list.contains("gas/test/gas"));
        assertFalse(list.contains("gas/test/asd/gas"));
        assertTrue(list.contains("gas/test/asd/tgas"));
        assertTrue(list.contains("gas/test/tgas"));
        assertTrue(list.contains("gas/kjh/lkjlk"));
        assertTrue(list.contains("gas/kjh/lkjlk/lkjlk"));
    }
}
