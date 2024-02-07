package pack.tests.basics.sub;

public class SolAdd {
    public static int get() {
        return new med(1, 2).result;
    }
}

class med {
    int result;

    med(int a, int b) {
        result = new flo().solve(a, b);
    }
}

class flo {
    int solve(int a, int b) {
        return a + b;
    }
}