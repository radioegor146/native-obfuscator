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



import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Test {
    static public final int THREADS = 2;
    static public final int READS = 100000;
    static volatile int brr = 0;

    public static void main(String[] args) throws Exception {
      
        System.out.println("Start Test main");
        
        byte[] arr = new byte [THREADS * READS];
        for(int i = 0; i < arr.length; ++i) {
            arr[i] = (byte) ('a' + (i % THREADS));
        }

        final InputStream is = new ByteArrayInputStream(arr);
        final int[] read = new int['z'];

        class Thr extends Thread {
            public void run() {
                byte[] buf = new byte[THREADS];
                int[] read_local = new int['z']; //to avoid synchronization 
                                                   // overhead in the cycle        
                try {
                    synchronized(Test.class) {
                         brr++;
                        if(brr == THREADS) {
                               Test.class.notifyAll();
                         } else {
                             Test.class.wait(); 
                         }
                    }

                    for (int i = 0; i < READS; ++i) {
                          is.read(buf, 0, buf.length);
                        for (int j = 0; j < buf.length; ++j) {
                               ++read_local[buf[j]];
                        }
                    }
                        
                    synchronized(read) {
                        for(int i = 0; i < read_local.length; ++i) {
                            read[i] += read_local[i];
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        Thr[] thrs = new Thr[THREADS];
        for (int i = 0; i < THREADS; ++i) {
            thrs[i] = new Thr();
            thrs[i].start();
        }
        for (int i = 0; i < THREADS; ++i) {
            thrs[i].join();
        }
        is.close();

        for (int i = 0; i < THREADS; ++i) {
            if (read[i + 'a'] != THREADS * READS) {
                System.out.println("expected " + THREADS * READS + " for " 
                           + ('a' + i) + " but got " + read['a' + i]);
            }
        }
        System.out.println("Finish Test main!");
    } 
}
