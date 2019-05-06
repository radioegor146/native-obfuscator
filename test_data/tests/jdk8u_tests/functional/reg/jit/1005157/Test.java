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

    static float fnan1 = Float.NaN;
    static float fnan2 = Float.NaN;
    static double dnan1 = Double.NaN;
    static double dnan2 = Double.NaN;

    public static void main(String[] args) {
        boolean ret1 = (fnan1 <= fnan2);
        System.out.println("fnan1 <= fnan2: " + ret1);
        
        boolean ret2 = (fnan1 >= fnan2);
        System.out.println("fnan1 >= fnan2: " + ret2);
        
        boolean ret3 = ((fnan1 < fnan2) == ! (fnan1 > fnan2));
        // false == !false
        System.out.println("((fnan1 < fnan2) == ! (fnan1 > fnan2)): " + ret3);

        boolean ret4 = (dnan1 <= dnan2);
        System.out.println("dnan1 <= dnan2: " + ret4);
        
        boolean ret5 = (dnan1 >= dnan2);
        System.out.println("dnan1 >= dnan2: " + ret5);
        
        boolean ret6 = ((dnan1 < dnan2) == ! (dnan1 > dnan2));
        // false == !false
        System.out.println("((dnan1 < dnan2) == ! (dnan1 > dnan2)): " + ret6);
        
        System.exit(!ret1 && !ret2 && !ret3 && !ret4 && !ret5 && !ret6 
                ? (0) : (1));
    }

}