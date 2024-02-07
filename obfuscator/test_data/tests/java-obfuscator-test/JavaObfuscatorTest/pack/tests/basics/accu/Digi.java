package pack.tests.basics.accu;

public class Digi {
    public void run() {
        double fl = 0d;
        int co = 0;
        float fx = 1.1f;
        while (true) {
            fl += 0.000000000000000001d;
            co++;
            if (co > 100 || (float) fl == 0.00000000000000002f)
                break;
        }
        if (co == 20) {
            fx += 1.3f;
            if (fx == 2.4f) {
                System.out.println("PASS");
                return;
            }
            System.out.println("FAIL");
        } else {
            System.out.println("FAIL");
        }
    }
}
