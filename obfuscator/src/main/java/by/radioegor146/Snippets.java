package by.radioegor146;

import by.radioegor146.source.StringPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Snippets {

    private static final Pattern TOKEN = Pattern.compile("[$#][a-zA-Z_][a-zA-Z_0-9]*");
    private static final Map<String, Template> TEMPLATES = loadTemplates();

    private final StringPool stringPool;

    public Snippets(StringPool stringPool) {
        this.stringPool = stringPool;
    }

    private static Map<String, Template> loadTemplates() {
        Properties snippets = new Properties();
        try (InputStream input = Snippets.class.getResourceAsStream("/sources/cppsnippets.properties")) {
            snippets.load(Objects.requireNonNull(input, "sources/cppsnippets.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Can't load cpp snippets", e);
        }
        Map<String, Template> templates = new HashMap<>();
        for (String key : snippets.stringPropertyNames()) {
            if (!key.endsWith("_S_VARS") && !key.contains("_S_CONST_")) {
                templates.put(key, new Template(snippets, key));
            }
        }
        return templates;
    }

    public String getSnippet(String key) {
        return getSnippet(key, Collections.emptyMap());
    }

    public String getSnippet(String key, Map<String, String> tokens) {
        Template template = Objects.requireNonNull(TEMPLATES.get(key), key);
        if (template.tokens.isEmpty()) {
            return template.tail;
        }
        StringBuilder output = new StringBuilder();
        append(output, key, template, tokens::get);
        return output.toString();
    }

    public void append(StringBuilder output, String key, Function<String, String> tokens) {
        append(output, key, Objects.requireNonNull(TEMPLATES.get(key), key), tokens);
    }

    private void append(StringBuilder output, String key, Template template, Function<String, String> tokens) {
        for (Token token : template.tokens) {
            String value = token.constant ? token.value : tokens.apply(token.value);
            if (value == null) {
                throw new IllegalArgumentException(key + " - missing token " + token.value);
            }
            output.append(token.prefix).append(token.pooled ? stringPool.get(value) : value);
        }
        output.append(template.tail);
    }

    private static final class Template {
        private final List<Token> tokens = new ArrayList<>();
        private final String tail;

        private Template(Properties snippets, String key) {
            Set<String> pooled = new HashSet<>(Arrays.asList(snippets.getProperty(key + "_S_VARS", "").split(",")));
            String value = snippets.getProperty(key);
            Matcher matcher = TOKEN.matcher(value);
            int end = 0;
            while (matcher.find()) {
                String placeholder = matcher.group();
                String name = placeholder.substring(1);
                boolean constant = placeholder.charAt(0) == '#';
                String tokenValue = constant
                        ? Objects.requireNonNull(snippets.getProperty(key + "_S_CONST_" + name), key + " " + placeholder)
                        : name;
                tokens.add(new Token(value.substring(end, matcher.start()), tokenValue, constant,
                        pooled.contains(placeholder)));
                end = matcher.end();
            }
            tail = value.substring(end);
        }
    }

    private static final class Token {
        private final String prefix;
        private final String value;
        private final boolean constant;
        private final boolean pooled;

        private Token(String prefix, String value, boolean constant, boolean pooled) {
            this.prefix = prefix;
            this.value = value;
            this.constant = constant;
            this.pooled = pooled;
        }
    }
}
