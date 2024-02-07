package pack.tests.basics.overwirte;

public class Sub extends Super {
    public void run() {
        Object o = this;
        System.out.println(((Face) o).face(1));
    }

    @Override
    public String face(int i) {
        if (i == 1) {
            return "PASS";
        } else {
            return "FAIL";
        }
    }
}
