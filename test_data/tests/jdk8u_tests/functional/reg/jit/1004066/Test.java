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
    
    public static void main(String args[]) {
        Object o = new Object();
        Thread1 t1 = new Thread1(o);
        Thread2 t2 = new Thread2(o);
        t1.start();
        t2.start();
    }
}

class Thread1 extends Thread {
    private Object o;
    
    public Thread1(Object o) {
        this.o = o;
    }

    public void run() {
        synchronized (o) {
            throw new RuntimeException("exception!");
        }
    }
}

class Thread2 extends Thread {
    private Object o;
    
    public Thread2(Object o) {
        this.o = o;
    }

    public void run() {

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Thread2 interrupted!");
        }

        synchronized (o) {
            System.out.println("Thread2 successfully synchronized");
        }
    }
}
