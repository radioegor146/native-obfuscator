package pack.tests.reflects.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LTest {
    public static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public void run() throws Exception {
        System.out.println(new String(readAllBytes(LTest.class.getResourceAsStream("TEST"))));
    }
}
