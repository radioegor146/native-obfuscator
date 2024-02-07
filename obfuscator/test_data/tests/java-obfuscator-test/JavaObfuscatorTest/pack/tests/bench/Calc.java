package pack.tests.bench;

public class Calc {
    public static int count = 0;

    public static void runAll() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            call(100);
            runAdd();
            runStr();
        }
        System.err.println("Calc: " + (System.currentTimeMillis() - start) + "ms");
        if (count != 30000)
            throw new RuntimeException("[ERROR]: Errors occurred in calc!");
    }

    private static void call(int i) {
        if (i == 0)
            count++;
        else
            call(i - 1);
    }

    private static void runAdd() {
        double i = 0d;
        while (i < 100.1d) {
            i += 0.99d;
        }
        count++;
    }

    private static void runStr() {
        String str = "";
        while (str.length() < 101) {
            str += "ax";
        }
        count++;
    }
}
