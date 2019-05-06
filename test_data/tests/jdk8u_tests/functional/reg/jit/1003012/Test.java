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
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Logger;



public class Test {

    public static void main(String[] args) {
        System.exit(new Test().test(Logger.global, args));
    }
    
    public int test(Logger logger, String [] args) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            C c = new C();
            c.i = 1234;
            c.j = 5678;

            oos.writeObject(c);
            oos.close();
            System.out.println("closed");

            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(baos.toByteArray()));
            System.out.println("opened");
        
            c  = (C) ois.readObject();
            System.out.println("read " + c.i + " " + c.j);
            System.out.println("(1234 5678 expected)");

            ois.close();
            return ((c.i == 1234) && (c.j == 5678)) ? (0) : (1);
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected exception was thrown:");
            e.printStackTrace();
            return (1);
        }
    }    
}

class C implements Serializable {
    int i;
    long j;
}

