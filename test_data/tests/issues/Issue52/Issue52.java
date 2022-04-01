public class Issue52 {

    public static void test1() {
        int[][] array = new int[12][2];
        int[][][] array1 = new int[12][2][];
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
    }
}