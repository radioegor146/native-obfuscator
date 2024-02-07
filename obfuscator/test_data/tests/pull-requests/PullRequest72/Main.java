public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("java.lang.invoke.StringConcatFactory");
        } catch (Exception e) {
            System.out.println("No java/lang/invoke/StringConcatFactory found, probably Java 1.8 or older");
            return;
        }
        Class.forName("TestStringConcatFactory").getMethod("test").invoke(null);
    }
}