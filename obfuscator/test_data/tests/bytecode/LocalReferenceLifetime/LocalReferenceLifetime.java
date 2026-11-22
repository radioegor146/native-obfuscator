import by.radioegor146.nativeobfuscator.Native;

public class LocalReferenceLifetime {
    @Native
    private static byte[] allocate(int count, boolean use) {
        byte[] result = new byte[1];
        for (int i = 0; i < count; i++) {
            byte[] item = new byte[16 * 1024];
            if (use) {
                item[0] = (byte) i;
                result[0] = item[0];
            }
            item = null;
        }
        return result;
    }

    public static void main(String[] args) {
        int count = args.length == 0 ? 1000 : Integer.parseInt(args[0]);
        System.out.println(allocate(count, true)[0]);
    }
}
