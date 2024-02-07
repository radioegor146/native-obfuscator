package pack.tests.reflects.retrace;

public class Tracer {
    public void run() throws Exception {
        new Tracee().toTrace(5);
        if (Tracee.p == 5) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }
}
