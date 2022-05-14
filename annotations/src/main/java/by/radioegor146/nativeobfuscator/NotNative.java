package by.radioegor146.nativeobfuscator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that allows to ignore method from native obfuscation
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface NotNative {
}
