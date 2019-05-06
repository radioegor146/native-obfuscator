package by.radioegor146.source;

import by.radioegor146.Util;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class StringPool {

    private long length;
    private final Map<String, Long> pool;

    public StringPool() {
        this.length = 0;
        this.pool = new HashMap<>();
    }

    public String get(String value) {
        if (!pool.containsKey(value)) {
            pool.put(value, length);
            length += getModifiedUtf8Bytes(value).length + 1;
        }
        return String.format("((char *)(string_pool + %dLL))", pool.get(value));
    }

    private static byte[] getModifiedUtf8Bytes(String str) {
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new RuntimeException("encoded string too long: " + utflen + " bytes");

        byte[] bytearr = new byte[utflen];

        int i;
        for (i = 0; i < strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) 
               break;
           bytearr[count++] = (byte) c;
        }

        for (; i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
            }
        }
        
        return bytearr;
    }

    public String build() {
        List<Byte> bytes = new ArrayList<>();
        pool.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .forEach(string -> {
                    for (byte b : getModifiedUtf8Bytes(string)) {
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
