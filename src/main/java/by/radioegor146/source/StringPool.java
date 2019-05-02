package by.radioegor146.source;

import by.radioegor146.Util;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class StringPool {

    private int length;
    private Map<String, Integer> pool;

    public StringPool() {
        this.length = 0;
        this.pool = new HashMap<>();
    }

    public String get(String value) {
        if (!pool.containsKey(value)) {
            pool.put(value, length);
            length += value.getBytes(StandardCharsets.UTF_8).length + 1;
        }
        return String.format("((char *)(string_pool + %dLL))", pool.get(value));
    }

    public String build() {
        List<Byte> bytes = new ArrayList<>();
        pool.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .forEach(string -> {
                    for (byte b : string.getBytes()) {
                        bytes.add(b);
                    }
                    bytes.add((byte) 0);
                });

        String result = String.format("{ %s }", bytes.stream().map(String::valueOf)
                .collect(Collectors.joining(", ")));

        String template = Util.readResource("sources/string_pool.cpp");
        return Util.dynamicFormat(template, Util.createMap(
                "size", bytes.size() + "LL",
                "value", result
        ));
    }
}
