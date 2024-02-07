package pack.tests.reflects.res;

public class Accesor {
    public void run() {
        try {
            if (Accesor.class.getResourceAsStream("/pack/tests/reflects/res/file").read() != 'a') {
                throw new RuntimeException();
            }
            if (Accesor.class.getResourceAsStream("file2").read() != 'r') {
                throw new RuntimeException();
            }
            if (Accesor.class.getClassLoader().getResourceAsStream("pack/tests/reflects/res/file3").read() != 'c') {
                throw new RuntimeException();
            }
            System.out.println("PASS");
        } catch (Exception e) {
            System.out.println("FAIL");
        }
    }
}
