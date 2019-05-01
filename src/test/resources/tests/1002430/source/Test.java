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
        Double d1 = new Double(0.0d);
        Double d2 = new Double(-0.0d);
        
        Float f1 = new Float(0.0f);
        Float f2 = new Float(-0.0f);
        
        boolean ret = true;
        
        int i = d1.compareTo(d2);
        System.out.println("Test case 1: i = " + i);
        if (i == 1) {
            System.out.println("Test case 1 PASSED");
        } else {
            System.out.println("Test case 1 FAILED");
            ret = false;
        }
        
        i = d2.compareTo(d1);
        System.out.println("Test case 2: i = " + i);
        if (i == -1) {
            System.out.println("Test case 2 PASSED");
        } else {
            System.out.println("Test case 2 FAILED");
            ret = false;
        }
       
        i = f1.compareTo(f2);
        System.out.println("Test case 3: i = " + i);
        if (i == 1) {
            System.out.println("Test case 3 PASSED");
        } else {
            System.out.println("Test case 3 FAILED");
            ret = false;
        }
        
        i = f2.compareTo(f1);
        System.out.println("Test case 4: i = " + i);
        if (i == -1) {
            System.out.println("Test case 4 PASSED");
        } else {
            System.out.println("Test case 4 FAILED");
            ret = false;
        }

        System.exit(ret ? (0) : (1));

    }        
}
