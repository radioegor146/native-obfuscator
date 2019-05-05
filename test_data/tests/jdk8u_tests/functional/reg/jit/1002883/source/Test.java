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
    public static void main(String[] argv) {
        System.out.println("bug2883 test started...");
        try {
            TransferThread t[] = new TransferThread[10];
            for (int i = 0; i < 10; i++) {
                t[i] = new TransferThread();
                t[i].start();
            }
            for (int i = 0; i < 10; i++) {
                t[i].join();
            }
            System.out.println("OK!");
        } catch (Exception e) {
            System.out.println("Exception found:");            
            e.printStackTrace();
            System.exit(-99);
        }
    }

    
    static class TransferThread extends Thread {
        public void run() {
            try {
                String name = this.getName();
                new String(name + name + name + name + name + name + name + 
                           name + name + name + name + name + name + name +
                           name + name + name + name + name + name + name + 
                           name + name + name + name + name + name + name + 
                           name + name + name + name);
                sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Exception found:");            
                e.printStackTrace();                
            }
        }
    }

}
