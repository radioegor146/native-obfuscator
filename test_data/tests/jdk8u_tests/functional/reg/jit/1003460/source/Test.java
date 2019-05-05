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
        boolean ret = true;
        
        // testcase 1 - Double.Nan
        Double d1 = new Double(Double.NaN);
        boolean b = (d1.doubleValue() != Double.NaN);
        if (!b) {
            ret = false;
            System.out.println(
                    "Testcase 1: Double.Nan conversion is incorrect!");
            System.out.println("Testcase 1 FAILED!");
        } else {
            System.out.println("Testcase 1 PASSED!");                        
        }

        // testcase 2 - Float.Nan
        Float f1 = new Float(Float.NaN);
        b = (f1.floatValue() != Float.NaN);
        if (!b) {
            ret = false;
            System.out.println("Testcase 2: Float conversion is incorrect!");
            System.out.println("Testcase 2 FAILED!");
        } else {
            System.out.println("Testcase 2 PASSED!");                        
        }
            
        System.out.println();
        return ret ? (0) : (1);
    }
}

