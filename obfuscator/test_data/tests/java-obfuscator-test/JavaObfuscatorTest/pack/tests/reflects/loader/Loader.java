package pack.tests.reflects.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Loader extends ClassLoader {
    public static byte[] readAllBytes(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (name.contains("TEST")) {
            return new ByteArrayInputStream("PASS".getBytes());
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = readAllBytes(Loader.class.getClassLoader().getResourceAsStream("pack/tests/reflects/loader/LTest.class"));
        return defineClass(name, data, 0, data.length);
    }
}
