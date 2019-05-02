package by.radioegor146;

import by.radioegor146.source.StringPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class Snippets {

    private Properties snippets;
    private StringPool stringPool;

    public Snippets(StringPool stringPool) {
        this.stringPool = stringPool;
        snippets = new Properties();

        try {
            snippets.load(NativeObfuscator.class.getClassLoader()
                    .getResourceAsStream("sources/cppsnippets.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Can't load cpp snippets", e);
        }
    }

    private String[] getVars(String key) {
        key += "_S_VARS";
        String result = snippets.getProperty(key);
        if(result == null || result.isEmpty()) {
            return new String[0];
        }
        return result.split(",");
    }

    public String getSnippet(String key) {
        return getSnippet(key, Util.createMap());
    }

    public String getSnippet(String key, Map<String, String> tokens) {
        String value = snippets.getProperty(key);
        Objects.requireNonNull(value, key);

        String[] stringVars = getVars(key);
        Map<String, String> result = new HashMap<>();
        for (String var : stringVars) {
            if (var.startsWith("#")) {
                result.put(var, snippets.getProperty(key + "_S_CONST_" + var.substring(1)));
            } else if (var.startsWith("$")) {
                result.put(var, tokens.get(var.substring(1)));
            } else {
                throw new RuntimeException("Unknown format modifier: " + var);
            }
        }

        result.entrySet().stream().filter(var -> var.getValue() == null).findAny().ifPresent(entry -> {
            throw new RuntimeException(key + " - token value can't be null");
        });

        result.entrySet().forEach(entry -> entry.setValue(stringPool.get(entry.getValue())));
        tokens.forEach((k, v) -> result.putIfAbsent("$" + k, v));

        return Util.dynamicRawFormat(value, result);
    }
}
