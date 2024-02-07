package pack.tests.basics.runable;

public class Exec {
    public static int i = 1;
    private int d;

    public Exec(int delta) {
        d = delta;
    }

    void doAdd() {
        try {
            Thread.sleep(200L);
        } catch (Exception ignored) {

        }
        i += d;
    }
}
