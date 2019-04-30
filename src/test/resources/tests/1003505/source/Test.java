/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 */





import java.lang.reflect.Field;

public class Test {

    public static void main(String[] args) {
        System.exit(new Test().test(args));
    }
    
    public int test(String [] args) {        
        try {
            Field f = Fvalue.class.getField("f");
            try {
                Fvalue value = new Fvalue();
                float obtainF = f.getFloat(value);
                if (obtainF != value.f) {
                    System.out.println("obtain: " + obtainF + " (" + value.f
                            + " expected)");
                    return (1);
                } else {
                    return (0);
                }
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } 
        return 1;
    }
}

class Fvalue {
    public float f = 46f;
}
