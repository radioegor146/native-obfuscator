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
        Object o1 = null;
        Object o2 = null;
        boolean ret = true;
        
        int i = 1;
        while (i++ == 1) {
            o2 = new Object();
        }
        
        if (o2 != null) {
            o1 = o2;
            System.out.println("o1: ");
            if (o1 == null) {
                System.out.println("FAILED: o1 is null!");
                ret = false;
            }
        }
        
        System.out.println("o1: ");
        if (o1 == null) {
            System.out.println("FAILED: o1 is null!");
            ret = false;
        }

        System.exit(ret ? (0) : (1));
    }    

}
