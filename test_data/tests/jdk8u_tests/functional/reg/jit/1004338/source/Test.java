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
 
/*
 * Please, note that this is reproducible with abcd optimization turned on
 * only. 
 * Jitrino command line options for reproducing are:
 * "-Xjitrino opt::skip=off -Xjitrino opt::do_abcd=on"
 */



public class Test {
    static Object[] arr=new Object[10];
    public static void main(String[] args) {
        arr[1] = null;
        System.out.println("arr[1] is " + arr[1]);
        System.out.println("PASSED!");
    }
}