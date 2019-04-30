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
 


public class Test {   
    
    public static void main(String[] args) {

        System.out.println("Start testcase 1 ...");
        try {
            int i = C.a;
        } catch (ExceptionInInitializerError e) {
            System.out.println("Expected exception was thrown:");
            e.printStackTrace();
            System.out.println("Testcase 1 PASSED!");
        }
    
        System.out.println("Start testcase 2 ...");        
        try {
            C c = new C();
        } catch (NoClassDefFoundError e) {
            System.out.println("Expected exception was thrown:");
            e.printStackTrace();
            System.out.println("Testcase 2 PASSED!");
        }
    }
}

class C {
    static int a;
    
    static {
        a = 0 / 0;   
    }
    
    public C() {
        a = 0 / 0;
    }
}
