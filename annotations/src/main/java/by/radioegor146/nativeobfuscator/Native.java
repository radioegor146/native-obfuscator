package by.radioegor146.nativeobfuscator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that allows to include method or class to native obfuscation
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Native {
}