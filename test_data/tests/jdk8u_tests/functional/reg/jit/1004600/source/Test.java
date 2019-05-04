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
        System.exit(new Test().test());
    }
    
    public int test() {        
        try {
            T.method();
        } catch (E e) {
            return (e.counter == 1) ? (0) : (1);
        }
        return (1);
    }

    static class T {
        static void method() throws E{
            throw new E();
        }
    }
    
    static class E extends Exception {
        static int counter = 0;
        public E() {
            super();
            counter++;
        }
    }

}