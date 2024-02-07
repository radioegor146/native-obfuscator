package pack.tests.reflects.annot;

import java.lang.reflect.Method;

public class annot {
    public void run() throws Exception {
        annoe a = new annoe();
        for (Method m : annoe.class.getDeclaredMethods()) {
            m.setAccessible(true);
            anno an = m.getAnnotation(anno.class);
            if (an != null) {
                if (an.val().equals("yes")) {
                    m.invoke(a);
                }
            }
        }
    }
}
