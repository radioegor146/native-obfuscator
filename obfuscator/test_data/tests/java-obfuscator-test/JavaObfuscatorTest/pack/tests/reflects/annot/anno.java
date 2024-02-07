package pack.tests.reflects.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface anno {
    String val() default "yes";

    String val2() default "yes";
}
