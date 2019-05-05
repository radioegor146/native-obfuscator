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
 


public class Test_case1 {   

    Object smpObject = new Object();        
    
    public static void main(String args[]) {
        Test_case1 t = new Test_case1();
        System.out.println("Start Test_case1 test...");
        t.test();
        System.out.println("PASSED!");
    }

    void test() {
        System.out.println("in");
        ThreadGroup tgObject = new ThreadGroup("tg1");
        label: {
            ThreadExteded t1 [] = { 
                    new ThreadExteded(tgObject, "t11"),
                    new ThreadExteded(tgObject, "t12"),
                    new ThreadExteded(tgObject, "t13")
            };
            
            synchronized(smpObject) {
                for (int j = 1; j < t1.length; j++) {
                    try {
                        t1[j].start();
                        smpObject.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break label;
                    }
                }
            }

            ThreadExteded t2 [] = { 
                    null,
                    new ThreadExteded(tgObject, "t21"),
                    new ThreadExteded(tgObject, "t22"),
                    new ThreadExteded(tgObject, "t23")
            };
            
            for (int j = 1; j < t1.length; j++) {
                t2[j].getName();
            }
        } //label:
        return ;
    }
    
    class ThreadExteded extends Thread {
        ThreadExteded(ThreadGroup tg, String s) {
            super(tg, s);
        }
        
        public void run() {
            synchronized(smpObject) {
                smpObject.notify();
            }
        }
    }

}
