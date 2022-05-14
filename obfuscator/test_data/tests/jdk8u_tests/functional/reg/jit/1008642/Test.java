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
    
    static boolean flag = false;     
    
    public static void main(String[] args) {
        int t = 0;
        
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 100; j++) {
                try {
                    throw new TestException();
                } catch (TestException e) {
                    t++;
                }
            }
        }
        
        System.runFinalization();
        System.out.println("Number of exceptions: " + t);
        if (flag) {
            System.exit(0);
        } else {
            System.exit(-99);
        }
    }
}

class TestException extends Exception {
    protected void finalize() throws Throwable {
        Test.flag = true;
    }
}

