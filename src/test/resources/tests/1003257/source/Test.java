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
         System.exit(new Test().test(args));
    }
    
    public int test(String [] args) {
        boolean ret1 = testcase1();
        boolean ret2 = testcase2();
        System.out.println();
        return (ret1 && ret2) ? (0) : (1); 
    }        

    boolean testcase1() {
        boolean ret = true;
        System.out.println("Start testcase 1...");
        
        for (long i = 0; i < 100; i++) {
            long res = i % 3;
            if (res<0 || res>2) {
                System.out.println("Error: " + res);
                ret = false;
            }
        }

        System.out.println("Testcase 1 " + (ret ? "PASSED" : "FAILED"));
        return ret;
    }

    boolean testcase2() {
        boolean ret = true;
        long d = 100;

        System.out.println("Start testcase 2...");

        for (long i = 1; i < 100; i++) {
            long res = d % i;
            if (res >= i) {
                System.out.println("Error: " +d + " % " + i + " = " + res);
                ret = false;
            }
        }

        System.out.println("Testcase 2 " + (ret ? "PASSED" : "FAILED"));
        return ret;
    }
    
}
