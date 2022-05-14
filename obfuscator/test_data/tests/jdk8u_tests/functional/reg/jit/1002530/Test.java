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
        double a = -1.0d;
        double b = 0.0d;
        double c;
        double a2 = 1.0d;
        double b2 = -0.0d;
        
        int ret = 0;
 
        c = a / b;
        if ( c != Double.NEGATIVE_INFINITY ) {
            System.out.println(
                    "-1.0d / 0.0d = " + c +    " (must be negative infinity).");
            ret = 1;
        }
 
        c = a2 / b2;
        if ( c != Double.NEGATIVE_INFINITY ) {
            System.out.println(
                    "1.0d / -0.0d = " + c + " (must be negative infinity).");
            ret = 1;
        }

        System.exit(ret);
    }    

}
