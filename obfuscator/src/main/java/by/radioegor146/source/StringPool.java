package by.radioegor146.source;

import by.radioegor146.Util;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringPool {

    private long length;
    private final Map<String, Entry> pool = new LinkedHashMap<>();

    public String get(String value) {
        return pool.computeIfAbsent(value, key -> {
            byte[] bytes = getModifiedUtf8Bytes(key);
            Entry entry = new Entry("((char *)(string_pool + " + length + "LL))", bytes);
            length += bytes.length + 1;
            return entry;
        }).pointer;
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
        StringBuilder values = new StringBuilder("{ ");
        boolean first = true;
        for (Entry entry : pool.values()) {
            if (!first) {
                values.append(", ");
            }
            for (byte value : entry.bytes) {
                values.append(value).append(", ");
            }
            values.append('0');
            first = false;
        }
        values.append(" }");

        String template = Util.readResource("sources/string_pool.cpp");
        return Util.dynamicFormat(template, Util.createMap(
                "size", Math.max(1, length) + "LL",
                "value", values
        ));
    }

    private static final class Entry {
        private final String pointer;
        private final byte[] bytes;

        private Entry(String pointer, byte[] bytes) {
            this.pointer = pointer;
            this.bytes = bytes;
        }
    }
}
