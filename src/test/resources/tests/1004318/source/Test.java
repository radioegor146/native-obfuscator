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
        // first argument - number of threads, second - number of objects
        System.exit(new Test().test());
    }

    public int test() {
        // input parameters
        int numThreads=100; // number of threats to be create
        int numObjects=100000; // number of big objects to pad the memory
        int OOMError = 1;     // OutOfMemoryError (low memory conditions). 
                            // 0 == off, 1 == on.
        
        // arrays
        Thread arrayOfThreads[]; // array of threads
        TestObject1 arrayOfObjects[]; // array of big padding objects

        // creating arrays
        arrayOfThreads = new Thread[numThreads]; // array of threads
        arrayOfObjects = new TestObject1[numObjects]; 
                                                // array of big padding objects

        // creating threads        
        try {
            Object nullObject = null; // null object
            int aIOOBEarray[] = new int[1];
            for (int cnt1 = 0; cnt1 < numThreads; cnt1++) {
                arrayOfThreads[cnt1] = new Thread();
            }
        }
        catch (OutOfMemoryError er) {
            return -99;
        }
        System.out.println("Threads created (ok)");

    //****************************************************//
        if (OOMError == 1) {
            // initiating OutOfMemory
            try {
                for (int cnt2 = 0; cnt2 < numObjects; cnt2++) {
                    arrayOfObjects[cnt2] = new TestObject1(); 
                            // padding memory by big objects
                }
            }
            catch (OutOfMemoryError oome) {}
        }
    //****************************************************//
        System.out.println("~PASSED");
        return 0; // return pass
    }

}

/* big padding object */
class TestObject1 {
    int testArray[][][] = new int[100][100][100];
}
