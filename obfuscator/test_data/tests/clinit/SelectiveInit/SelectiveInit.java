import by.radioegor146.nativeobfuscator.Native;

public class SelectiveInit {
    private static final int VALUE = add(40, 2);

    @Native
    private static int add(int a, int b) {
        return a + b;
    }

    public static void main(String[] args) {
        System.out.println(VALUE);
    }
}
