/*
 * This file is part of the native-obfuscator annotations module.
 *
 * SPDX-License-Identifier: LGPL-3.0-only
 * Full license text: annotations/LICENSE
 *
 * This module is compile-time only: it contains no executable code, and both
 * annotations use RetentionPolicy.CLASS, so they are neither present nor
 * required at runtime.
 */
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
