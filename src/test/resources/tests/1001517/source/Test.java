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
        Byte obj = new Byte((byte) ((Byte.MAX_VALUE + Byte.MIN_VALUE)/2));

        long res = obj.longValue();
        long res2 = 0L;

        boolean ret = true;
        
        System.out.println("res: " + res);
        
        if (! (res <= Byte.MAX_VALUE  && res >= Byte.MIN_VALUE)) {
            System.out.println("Failed: 1");
            ret = false;
        }
        
        if (! (res2 <= Byte.MAX_VALUE  && res2 >= Byte.MIN_VALUE)) {
               System.out.println("Failed: 2");
               ret = false;
        }
        
        if (! (res == (byte) 0)) {
            System.out.println("Failed: 3");
            ret = false;
        }
        
        if (! (res2 == (byte)0)) {
            System.out.println("Failed: 4");
            ret = false;
        }

        return ret ? (0) : (1);
    }        
}
