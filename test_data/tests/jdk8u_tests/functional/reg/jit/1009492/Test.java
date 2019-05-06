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

    static float f1 = Float.MAX_VALUE;
    static float f2 = -Float.MAX_VALUE;
    static double d1 = Double.MAX_VALUE;
    static double d2 = -Double.MAX_VALUE;

    public static void main(String[] args) {            

        int ret = 0;

        float res = f1 % 1.0f;
        if (Float.compare(res, 0.0f) == 0) {
            System.out.println("Float.MAX_VALUE: testcase passed");
        } else {
            System.out.println("Float.MAX_VALUE is " + res + 
                    ". (0.0f expected): testcase failed");
            ret = -99;
        }
        
        res = f2 % -50;
        if (Float.compare(res, -40.0f) == 0) {
            System.out.println("-Float.MAX_VALUE: testcase passed");
        } else {
            System.out.println("-Float.MAX_VALUE is " + res + 
                    ". (-40.0f expected): testcase failed");
            ret = -99;
        }

        double dres = d1 % 30;
        if (Double.compare(dres, 8.0) == 0) {
            System.out.println("Double.MAX_VALUE: testcase passed");
        } else {
            System.out.println("Double.MAX_VALUE is " + dres + 
                    ". (8.0f expected): testcase failed");
            ret = -99;
        }

            
        dres = d2 % 1;
        if (Double.compare(dres, -0.0) == 0) {
            System.out.println("-Double.MAX_VALUE is testcase passed");
        } else {
            System.out.println("Double.MAX_VALUE: " + dres + 
                    ". (-0.0 expected): testcase failed");
            ret = -99;
        }
        
        System.exit(ret);
    }
}

