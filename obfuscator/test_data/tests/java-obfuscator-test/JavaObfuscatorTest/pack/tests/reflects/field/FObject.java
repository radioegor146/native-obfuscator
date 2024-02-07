package pack.tests.reflects.field;

public class FObject {
    private int i;

    private FObject(int i) {
        this.i = i;
    }

    private void add() {
        i += 3;
    }
}
