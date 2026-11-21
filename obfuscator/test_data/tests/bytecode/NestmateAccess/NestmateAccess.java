import by.radioegor146.nativeobfuscator.Native;

public class NestmateAccess {
    private static class Box {
        private final int value;

        private Box(int value) {
            this.value = value;
        }
    }

    @Native
    private static int add(int left, int right) {
        return left + right;
    }

    public static void main(String[] args) {
        Box box = new Box(40);
        System.out.println(add(box.value, 2));
    }
}
