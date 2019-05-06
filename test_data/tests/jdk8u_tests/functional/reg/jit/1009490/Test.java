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

    static long varLong1 = 2147483648L;
    static long varLong2 = -2147483649L;

    public static void main(String[] args) {

        int ret = 0;
        
        long res = varLong1 - 1;
        if (res == Integer.MAX_VALUE) {
            System.out.println("Testcase 1 passed");
        } else {
            System.out.println("Testcase 1 failed: result is " + res +
                    " (" + Integer.MAX_VALUE + " expected)");
            ret = -99;
          }

          res = varLong2 + 1;
          if (res == Integer.MIN_VALUE) {
              System.out.println("Testcase 2 passed");
          } else {
              System.out.println("Testcase 2 failed: result is " + res +
                      " (" + Integer.MIN_VALUE + " expected)");
              ret = -99;
          }
          
          System.exit(ret);
    }

}

