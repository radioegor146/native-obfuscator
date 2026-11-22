import by.radioegor146.nativeobfuscator.Native;

@Native
public class Issue52 {

    private static class Component {
        static {
            System.out.println("Component initialized");
        }
    }

    public static void test1() {
        int[][] array = new int[12][2];
        int[][][] array1 = new int[12][2][];
        System.out.println(array1.length);
        System.out.println(array1[0].length);
        boolean[][] array2 = new boolean[1][2];
        char[][] array31 = new char[1][2];
        short[][] array3 = new short[1][2];
        byte[][] array4 = new byte[1][2];
        float[][] array5 = new float[1][2];
        double[][] array6 = new double[1][2];
        long[][] array7 = new long[1][2];

        for (int i = 0; i < 12; i++) {
            array1[i] = new int[][] { new int[] { 1, 2 } };
        }
    }

    public static void test2() {
        Object[][] array = new Object[12][2];
        Object[][][] array1 = new Object[12][2][];

        for (int i = 0; i < 12; i++) {
            array1[i] = new Object[][] { new Object[] { null, null } };
        }
    }

    public static void main(String[] args) {
        test1();
        test2();
        Component[][][] full = new Component[2][3][4];
        Component[][][] partial = new Component[2][3][];
        System.out.println(full[1][2].length);
        System.out.println(partial[1][2] == null);
        System.out.println(full.getClass() == partial.getClass());
        try {
            int[][] array = new int[0][-1];
            System.out.println(array.length);
        } catch (NegativeArraySizeException exception) {
            System.out.println("negative primitive array size");
        }
        try {
            Object[][][] array = new Object[1][0][-1];
            System.out.println(array.length);
        } catch (NegativeArraySizeException exception) {
            System.out.println("negative object array size");
        }
    }
}
