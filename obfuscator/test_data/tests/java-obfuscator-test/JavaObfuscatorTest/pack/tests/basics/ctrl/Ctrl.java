package pack.tests.basics.ctrl;

public class Ctrl {
    private String ret = "FAIL";

    public void runt() {
        if ("a".equals("b")) {
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void runf() {
        try {
            runt();
        } catch (RuntimeException e) {
            ret = "PASS";
        }
        try {
            runt();
            ret = "FAIL";
        } catch (Exception e) {

        }
    }

    public void run() {
        runf();
        System.out.println(ret);
    }
}
