/*
 * Copyright (c) 2004, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug     4984908 5058132 6653154
 * @summary Basic test of valueOf(String)
 * @author  Josh Bloch
 *
 * @compile ValueOf.java
 * @run main ValueOf
 */

import java.util.*;
import java.lang.reflect.Method;

public class ValueOf {
    static Random rnd = new Random();

    public static void main(String[] args) throws Exception {
        test(Silly0.class);
        test(Silly1.class);
        test(Silly31.class);
        test(Silly32.class);
        test(Silly33.class);
        test(Silly63.class);
        test(Silly64.class);
        test(Silly65.class);
        test(Silly127.class);
        test(Silly128.class);
        test(Silly129.class);
        test(Specialized.class);

        testMissingException();
    }

    static <T extends Enum<T>> void test(Class<T> enumClass) throws Exception {
        Set<T> s  = EnumSet.allOf(enumClass);
        test(enumClass, s);

        // Delete half the elements from set at random
        for (Iterator<T> i = s.iterator(); i.hasNext(); ) {
            i.next();
            if (rnd.nextBoolean())
                i.remove();
        }

        test(enumClass, s);
    }

    static <T extends Enum<T>> void test(Class<T> enumClass, Set<T> s)
        throws Exception
    {
        Method valueOf = enumClass.getDeclaredMethod("valueOf", String.class);
        Set<T> copy  = EnumSet.noneOf(enumClass);
        for (T e : s)
            copy.add((T) valueOf.invoke(null, e.name()));
        if (!copy.equals(s))
            throw new Exception(copy + " != " + s);
    }

    static void testMissingException() {
        try {
            Enum.valueOf(Specialized.class, "BAZ");
            throw new RuntimeException("Expected IllegalArgumentException not thrown.");
        } catch(IllegalArgumentException iae) {
            String message = iae.getMessage();
            if (! "No enum constant ValueOf.Specialized.BAZ".equals(message))
                throw new RuntimeException("Unexpected detail message: ``" + message + "''.");
        }
    }

    enum Silly0 { };

    enum Silly1 { e1 }

    enum Silly31 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30
    }

    enum Silly32 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31
    }

    enum Silly33 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32
    }

    enum Silly63 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46,
        e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61,
        e62
    }

    enum Silly64 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46,
        e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61,
        e62, e63
    }

    enum Silly65 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46,
        e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61,
        e62, e63, e64
    }

    enum Silly127 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46,
        e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61,
        e62, e63, e64, e65, e66, e67, e68, e69, e70, e71, e72, e73, e74, e75, e76,
        e77, e78, e79, e80, e81, e82, e83, e84, e85, e86, e87, e88, e89, e90, e91,
        e92, e93, e94, e95, e96, e97, e98, e99, e100, e101, e102, e103, e104, e105,
        e106, e107, e108, e109, e110, e111, e112, e113, e114, e115, e116, e117,
        e118, e119, e120, e121, e122, e123, e124, e125, e126
    }

    enum Silly128 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46,
        e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61,
        e62, e63, e64, e65, e66, e67, e68, e69, e70, e71, e72, e73, e74, e75, e76,
        e77, e78, e79, e80, e81, e82, e83, e84, e85, e86, e87, e88, e89, e90, e91,
        e92, e93, e94, e95, e96, e97, e98, e99, e100, e101, e102, e103, e104, e105,
        e106, e107, e108, e109, e110, e111, e112, e113, e114, e115, e116, e117,
        e118, e119, e120, e121, e122, e123, e124, e125, e126, e127
    }

    enum Silly129 {
        e0, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16,
        e17, e18, e19, e20, e21, e22, e23, e24, e25, e26, e27, e28, e29, e30, e31,
        e32, e33, e34, e35, e36, e37, e38, e39, e40, e41, e42, e43, e44, e45, e46,
        e47, e48, e49, e50, e51, e52, e53, e54, e55, e56, e57, e58, e59, e60, e61,
        e62, e63, e64, e65, e66, e67, e68, e69, e70, e71, e72, e73, e74, e75, e76,
        e77, e78, e79, e80, e81, e82, e83, e84, e85, e86, e87, e88, e89, e90, e91,
        e92, e93, e94, e95, e96, e97, e98, e99, e100, e101, e102, e103, e104, e105,
        e106, e107, e108, e109, e110, e111, e112, e113, e114, e115, e116, e117,
        e118, e119, e120, e121, e122, e123, e124, e125, e126, e127, e128
    }

    enum Specialized {
        FOO {
            public void foo() {}
        };
        abstract public void foo();
    };

}
