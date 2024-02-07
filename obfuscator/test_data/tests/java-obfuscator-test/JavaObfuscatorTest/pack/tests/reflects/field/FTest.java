package pack.tests.reflects.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FTest {
    public void run() throws Exception {
        Constructor<?> con = FObject.class.getDeclaredConstructor(int.class);
        if (con.isAccessible()) {
            System.out.println("FAIL");
            return;
        }
        con.setAccessible(true);
        FObject obj = (FObject) con.newInstance(1);
        Method m = FObject.class.getDeclaredMethod("add", null);
        if (m.isAccessible()) {
            System.out.println("FAIL");
            return;
        }
        m.setAccessible(true);
        m.invoke(obj);
        Field f = FObject.class.getDeclaredField("i");
        if (f.isAccessible()) {
            System.out.println("FAIL");
            return;
        }
        f.setAccessible(true);
        if (f.getInt(obj) != 4) {
            System.out.println("FAIL");
            return;
        }
        System.out.println("PASS");
    }
}
