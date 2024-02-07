package pack.tests.basics.inner;

public class Exec {
    public int fuss = 1;

    public void addF() {
        fuss += 2;
    }

    public class Inner {
        int i;

        public Inner(int p) {
            i = p;
        }

        public void doAdd() {
            addF();
            fuss += i;
        }
    }
}
