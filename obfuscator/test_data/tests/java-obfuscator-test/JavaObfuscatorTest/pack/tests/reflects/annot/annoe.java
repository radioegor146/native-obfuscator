package pack.tests.reflects.annot;

import java.lang.reflect.Field;

public class annoe {
    @anno(val = "PASS")
    private static final String fail = "WHAT";

    @anno
    public void dox() throws Exception {
        String toGet = "FAIL";
        for (Field f : annoe.class.getDeclaredFields()) {
            f.setAccessible(true);
            anno obj = f.getAnnotation(anno.class);
            if (obj != null) {
                toGet = obj.val();
            }
        }
        System.out.println(toGet);
    }

    @anno(val = "no")
    public void dov() {
        System.out.println("FAIL");
    }
}
