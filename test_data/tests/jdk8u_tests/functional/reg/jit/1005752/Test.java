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



import java.io.*;

public class Test {

    public static void main(String[] args) {
         System.out.println("drem: value2 is zero, result must be NaN");
         double d1 = 1.0d;
         d1 = d1 % 0.0d; 
         if (Double.isNaN(d1)) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + d1);
         }
         System.out.println("drem: value1 is infinity, result must be NaN");
         d1 = Double.POSITIVE_INFINITY;
         d1 = d1 % 1.0d; 
         if (Double.isNaN(d1)) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + d1);
         }
         System.out.println("drem: value2 is infinity, result must be value1");
         d1 = 1.0d;
         double d2 = d1 % Double.POSITIVE_INFINITY;
         if (d2 == d1) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + d2+" " + d1);
         }         
         System.out.println("drem: value1 is infinity, value 2 is zero, "
             + "result must be NaN");
         d1 = Double.POSITIVE_INFINITY;
         d2 = d1 % 0.0d;
         if (Double.isNaN(d2)) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + d2);
         }
 
         System.out.println("frem: value2 is zero, result must be NaN");
         float f1 = 1.0f;
         f1 = f1 % 0.0f; 
         if (Float.isNaN(f1)) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + f1);
         }
         System.out.println("frem: value1 is infinity, result must be NaN");
         f1 = Float.POSITIVE_INFINITY;
         f1 = f1 % 1.0f; 
         if (Float.isNaN(f1)) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + f1);
         } 
         System.out.println("frem: value2 is infinity, result must be value1");
         f1 = 1.0f;
         float f2 = f1 % Float.POSITIVE_INFINITY;
         if (f2 == f1) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + f2+" " + f1);
         }
         System.out.println("frem: value1 is infinity, value 2 is zero, "
             + "result must be NaN");
         f1 = Float.POSITIVE_INFINITY;
         f2 = f1 % 0.0f;
         if (Float.isNaN(f2)) {
             System.out.println("Test passes");
         } else { 
             throw new Error("Test fails: " + f2);
         }
    } 
}