package by.radioegor146;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

public class Util {

    public static Map<String, String> createMap(Object... parts) {
        HashMap<String, String> tokens = new HashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            tokens.put(parts[i].toString(), parts[i + 1].toString());
        }
        return tokens;
    }

    private static String replaceTokens(String string, Map<String, String> tokens, String patternString) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(string);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(tokens.get(matcher.group(1))));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String dynamicFormat(String string, Map<String, String> tokens) {
        String patternString = String.format("\\$(%s)",
                tokens.keySet().stream().map(Util::unicodify).collect(Collectors.joining("|")));
        return replaceTokens(string, tokens, patternString);
    }

    public static String dynamicRawFormat(String string, Map<String, String> tokens) {
        if (tokens.isEmpty()) {
            return string;
        }
        String patternString = String.format("(%s)",
                tokens.keySet().stream().map(Util::unicodify).collect(Collectors.joining("|")));
        return replaceTokens(string, tokens, patternString);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }

    public  static String readResource(String filePath) {
        try(InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream(filePath)) {
            return writeStreamToString(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyResource(Path from, Path to) throws IOException {
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream(from.toString())) {
            Objects.requireNonNull(in, "Can't copy resource " + from);
            Files.copy(in, to.resolve(from.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String writeStreamToString(InputStream stream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transfer(stream, baos);
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeEntry(JarFile f, ZipOutputStream out, JarEntry e) throws IOException {
        out.putNextEntry(new JarEntry(e.getName()));
        try (InputStream in = f.getInputStream(e)) {
            transfer(in, out);
        }
        out.closeEntry();
    }

    public static void writeEntry(ZipOutputStream out, String entryName, byte[] data) throws IOException {
        out.putNextEntry(new JarEntry(entryName));
        out.write(data, 0, data.length);
        out.closeEntry();
    }

    static void transfer(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        for (int r = in.read(buffer, 0, 4096); r != -1; r = in.read(buffer, 0, 4096)) {
            out.write(buffer, 0, r);
        }
    }

    public static String escapeCppNameString(String value) {
        Matcher m = Pattern.compile("([^a-zA-Z_0-9])").matcher(value);
        StringBuffer sb = new StringBuffer(value.length());
        while (m.find()) {
            m.appendReplacement(sb, String.valueOf((int) m.group(1).charAt(0)));
        }
        m.appendTail(sb);
        String output = sb.toString();
        if (output.length() > 0 && (output.charAt(0) >= '0' && output.charAt(0) <= '9')) {
            output = "_" + output;
        }
        return output;
    }

    private static String unicodify(String string) {
        StringBuilder result = new StringBuilder();
        for (char c : string.toCharArray()) {
            result.append("\\u").append(String.format("%04x", (int) c));
        }
        return result.toString();
    }

    public static int byteArrayToInt(byte[] b) {
        if (b.length == 4) {
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
        } else if (b.length == 2) {
            return (b[0] & 0xff) << 8 | (b[1] & 0xff);
        }

        return 0;
    }
}
