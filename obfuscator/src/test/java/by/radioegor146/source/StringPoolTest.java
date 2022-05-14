package by.radioegor146.source;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringPoolTest {

    @Test
    public void testBuild() {
        StringPool stringPool = new StringPool();

        stringPool.get("test");

        assertEquals(
                "#include \"string_pool.hpp\"\n" +
                        "\n" +
                        "namespace native_jvm::string_pool {\n" +
                        "    static char pool[5LL] = { 116, 101, 115, 116, 0 };\n" +
                        "\n" +
                        "    char *get_pool() {\n" +
                        "        return pool;\n" +
                        "    }\n" +
                        "}", stringPool.build());

        stringPool.get("other");

        assertEquals(
                "#include \"string_pool.hpp\"\n" +
                        "\n" +
                        "namespace native_jvm::string_pool {\n" +
                        "    static char pool[11LL] = { 116, 101, 115, 116, 0, 111, 116, 104, 101, 114, 0 };\n" +
                        "\n" +
                        "    char *get_pool() {\n" +
                        "        return pool;\n" +
                        "    }\n" +
                        "}", stringPool.build());
    }

    @Test
    public void testGet() {
        StringPool stringPool = new StringPool();

        assertEquals("((char *)(string_pool + 0LL))", stringPool.get("test"));
        assertEquals("((char *)(string_pool + 0LL))", stringPool.get("test"));

        assertEquals("((char *)(string_pool + 5LL))", stringPool.get("\u0080\u0050"));
        assertEquals("((char *)(string_pool + 9LL))", stringPool.get("\u0800"));
        assertEquals("((char *)(string_pool + 13LL))", stringPool.get("\u0080"));
    }

}
