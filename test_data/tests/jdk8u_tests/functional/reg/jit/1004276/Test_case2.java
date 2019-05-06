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
 


public class Test_case2 {   

    public static Object smpObject = new Object();

    public static void main(String args []) {
        label: {
            Object t1 [] = { null };
            synchronized(smpObject) {
                for (int j = 1; j < t1.length; j++ ) {
                    try {
                        int i = t1.length;
                    } catch (Exception e) {
                        break label;
                    }
                }
            }

            Object t2 [] = { null };
            for (int j = 1; j < t2.length; j++ ) {}        
        }
    }

    static void foo(){}

}
