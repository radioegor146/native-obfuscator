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



import java.lang.reflect.Method;
import java.util.logging.Logger;



public class Test {

    public static void main(String[] args) {
        System.exit(new Test().test(Logger.global, args));
    }
    
    public int test(Logger logger, String [] args) {
        Method m = null;
        try {
            m = MyClass.class.getDeclaredMethod("test_ch", new Class[] {});
            int i5 = ((Character) m.invoke(
                    new MyClass(), new Object[] {})).charValue();
            System.out.println("i5: " + i5);
            System.out.println("i5==42: " + (i5 == 42) + "\n");
            
            int i6 = new MyClass().test_ch();
            System.out.println("i6: " + i6);
            System.out.println("i6==42: " + (i6 == 42));  
            System.out.println("end of test");
            
            return (i5 == 42) && (i6 == 42) ? (0) : (1);
        } catch (Exception e) {
            System.out.println("Unexpected exception was thrown:");
            e.printStackTrace();
            return 1;
        }
    }
}

class MyClass 
{
   public char test_ch(){
       return (char)42;
    }
}