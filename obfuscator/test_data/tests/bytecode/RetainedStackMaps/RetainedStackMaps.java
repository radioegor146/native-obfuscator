import by.radioegor146.nativeobfuscator.Native;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntUnaryOperator;

public class RetainedStackMaps {
    @Native
    private static int size() {
        IntUnaryOperator increment = value -> value + 1;
        return increment.applyAsInt(1);
    }

    private static int available(boolean inMemory) throws IOException {
        InputStream stream = inMemory
                ? new ByteArrayInputStream(new byte[size()])
                : new FileInputStream("unused");
        try {
            return stream.available();
        } finally {
            stream.close();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(available(true));
    }
}
