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
        long l = 10000000000L;
        float f = (((float) l) % 1);
        double d = (((double) l) % 1);
        
        System.out.println("Result of (float)" + l + "%1 = " + f 
                + " (0.0f expected)");
        System.out.println("Result of (double)" + d + "%1 = " + d
                + " (0.0d expected)");
        System.exit((f == 0.0f) && (d == 0.0) ? (0) : (1));
    }

}
