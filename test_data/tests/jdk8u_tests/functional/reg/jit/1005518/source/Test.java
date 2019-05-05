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
    
    public static final int THREADS_COUNT = 10;            //28;
    public static final int LMT_LOOP = 100;
    public static int [] tableResults = new int [THREADS_COUNT];

    public static void main(String args []) {
        try {
            for (int i = 0; i < LMT_LOOP; i++) {
                Thread tThread [] = new Thread [THREADS_COUNT];
                
                for (int j = 0; j < THREADS_COUNT; j++) {
                    tableResults[j] = 0;
                }
                
                for (int j = 0; j < THREADS_COUNT; j++) {
                    tThread[j] = new testThread(j);
                    tThread[j].start();
                }
                
                for (int j = 0; j < THREADS_COUNT; j++) {
                    tThread[j].join();
                }
                
                for (int j = 0; j < THREADS_COUNT; j++) {
                    if (tableResults[j] != 104) {
                        System.out.println("Test fails: thread: " + j + "  " 
                                + tableResults[j] + "  interation: " + i);
                        return;
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println("FAILED - unexpected error was thrown:");
            e.printStackTrace();
            return;
        }
        System.out.println("PASSED!"); 
    }
}

class testThread extends Thread {
    private int index;
    
    public testThread(int index) {
        this.index = index;
    }
    
    public void run() {
        test00 t = new test00();
        test00.cnt++;
        
        try {
            while (true) {
                if (!t.put(t)) {
                    Thread.sleep(1000);
                }
                t.get();
                break;
            }
            Test.tableResults[index] += t.test1();
        } catch (Throwable e) {
            System.out.println("Unexpected exception was thrown:");
            e.printStackTrace();
            Test.tableResults[index] += 105;
        }
    } 
}

class test00 {
    public static int cnt = 0;
    private static test00 Obj = null;
    
    public synchronized boolean put(test00 o) {
        if (Obj != null) {
            return false;
        }
        Obj = o;
        return true;
    } 
    
    public synchronized void get() {
        Obj = null;
    } 
    
    public int test1() {
        return 104;
    }
} 


