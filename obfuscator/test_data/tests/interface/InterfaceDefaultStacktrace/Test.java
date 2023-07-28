public class Test {

    interface TestInterface {
        default void defaultMethod() {
            new Exception().printStackTrace();
        }
    }

    static class Test2 implements TestInterface {
    }

    public static void main(String[] args) {
        new Test2().defaultMethod();
    }
}