import by.radioegor146.nativeobfuscator.Native;

@Native
public class NumericSemantics {
    private static void integers(int a, int b) {
        System.out.println(a + b);
        System.out.println(a - b);
        System.out.println(a * b);
        System.out.println(-a);
        System.out.println(a << b);
        System.out.println(a >> b);
        System.out.println(a >>> b);
        try {
            System.out.println(a / b);
            System.out.println(a % b);
        } catch (ArithmeticException exception) {
            System.out.println("int divide by zero");
        }
        a++;
        System.out.println(a);
    }

    private static void longs(long a, long b) {
        System.out.println(a + b);
        System.out.println(a - b);
        System.out.println(a * b);
        System.out.println(-a);
        System.out.println(a << b);
        System.out.println(a >> b);
        System.out.println(a >>> b);
        try {
            System.out.println(a / b);
            System.out.println(a % b);
        } catch (ArithmeticException exception) {
            System.out.println("long divide by zero");
        }
    }

    private static void conversions(float f, double d) {
        System.out.println((int) f);
        System.out.println((long) f);
        System.out.println((int) d);
        System.out.println((long) d);
    }

    public static void main(String[] args) {
        integers(Integer.MAX_VALUE, 2);
        integers(Integer.MIN_VALUE, -1);
        integers(Integer.MIN_VALUE, 0);
        integers(-3, 33);
        longs(Long.MAX_VALUE, 2);
        longs(Long.MIN_VALUE, -1);
        longs(Long.MIN_VALUE, 0);
        longs(-3, 65);
        conversions(Float.NaN, Double.NaN);
        conversions(Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        conversions(Float.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        conversions(0x1p31f, 0x1p63);
        conversions(0x1.fffffep30f, 0x1.fffffffffffffp62);
        conversions(-0x1.000002p31f, -0x1.0000000000001p63);
        conversions(-0x1p63f, 0x1p31);
        conversions(3.75f, -3.75);
    }
}
