package pack.tests.reflects.counter;

public class Count {
    public void run() throws Throwable {
        if (Countee.class.getFields().length == 1 && Countee.class.getDeclaredFields().length == 4 && Countee.class.getMethods().length > 4 && Countee.class.getDeclaredMethods().length == 4)
            System.out.println("PASS");
        else
            System.out.println("FAIL");
    }
}
