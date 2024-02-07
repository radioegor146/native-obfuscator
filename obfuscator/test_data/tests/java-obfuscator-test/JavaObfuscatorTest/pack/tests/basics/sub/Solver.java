package pack.tests.basics.sub;

public class Solver {
    public Solver() {
        if (SolAdd.get() == 3) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }
}
