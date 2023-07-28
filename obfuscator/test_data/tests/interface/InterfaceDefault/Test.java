public class Test {

    interface TestInterface {
        default void defaultMethod() {
            System.err.println("default method");
        }
    }

    static class Test2 implements TestInterface {
    }

    public static void main(String[] args) {
        new Test2().defaultMethod();
    }
}