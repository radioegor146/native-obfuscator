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



import java.util.logging.Logger;



public class Test {
   
    public static void main(String[] args) {
         System.exit(new Test().test(Logger.global, args));
    }
    
    public int test(Logger logger, String [] args) {
        java.util.Random r = new java.util.Random();
        boolean ret = true;
        
        for (int i = 0; i <= 10000; i++) {
            short rndm = (short) r.nextInt(32767);
            byte bv = (byte) rndm;
            byte newval = new Short(rndm).byteValue();
            if( bv != newval) {
                System.out.println("FAIL (" + i + "): bv = " + bv 
                        + ", newval = " + newval);
                ret = false;
            } 
        }
        return ret ? (0) : (1);
    }        
}
