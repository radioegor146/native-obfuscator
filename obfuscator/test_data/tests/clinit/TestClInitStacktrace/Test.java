public class Test {

    private static void printStackTrace() {
        StackTraceElement[] stack = new Exception().getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            System.out.printf("%d: %s.%s\n", i, stack[i].getClassName(), stack[i].getMethodName());
        }
    }

    public static void main(String[] args) {
    }

    static {
        printStackTrace();
    }
}