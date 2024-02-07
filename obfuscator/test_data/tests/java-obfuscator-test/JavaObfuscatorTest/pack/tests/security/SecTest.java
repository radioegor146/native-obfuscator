package pack.tests.security;

import java.lang.reflect.Method;

public class SecTest {
    public void run() {
        System.setSecurityManager(new Sman());
        System.out.print("FAIL");
        try {
            Method m = SecExec.class.getDeclaredMethod("doShutdown");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Throwable t) {
            Throwable r, f = t;
            while (true) {
                r = f.getCause();
                if (r != null) {
                    f = r;
                    continue;
                }
                break;
            }
            String str = f.getMessage();
            if (str == null) {
                return;
            }
            if (str.contains("HOOK")) {
                System.out.println("\b\b\b\bPASS");
            }
        }
    }
}
