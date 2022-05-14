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



import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

public class Test {

    int NUMBER = 1000;
    String filename = "test.tmp";
    
    public static void main(String[] args) {
        Test t = new Test();
        if (args.length > 0) {
            t.filename = args[0];
            t.testRegression01();
            new File(t.filename).delete();      
        }
    }

    public void testRegression01() {
        try {
            System.out.println("Start file writing...");
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            Hashtable hashes[] = new Hashtable[NUMBER];
            for (int i = 0; i < NUMBER; i++) {
                hashes[i] = new Hashtable();
                hashes[i].put(new Integer(i), "aaaaaaaaa");
                hashes[i].put(new Integer(i + NUMBER + 1), "bbbbbbbb");
                hashes[i].put(new Integer(i + 1), "cccccccc");
            }

            for (int i = 0; i < NUMBER; i++) {
                oos.writeInt(i);
                oos.writeObject(hashes[i]);
            }

            oos.close();
            fos.close();            
            System.out.println("End file writing...");

            System.out.println("Start file reading...");            
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fis);

            for (int i = 0; i < NUMBER; i++) {
                ois.readInt();
                Hashtable h = (Hashtable) ois.readObject();
            }
            System.out.println("End file reading...");            

        } catch (Exception e) {
            System.out.println("Unexpected exception was thrown:");
            e.printStackTrace();
            System.exit(-99);
        }
        
        System.out.println(
                "File writing and reading were completed correctly!");
    }

}

