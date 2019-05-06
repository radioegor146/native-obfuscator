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



import java.util.logging.Logger;



public class Test {
    
    public static void main(String[] args) {
         System.exit(new Test().test(Logger.global, args));
    }
    
    public int test(Logger logger, String [] args) {
        try {
            TestClass.class.getDeclaredFields()[0].get(new TestClass());
        } catch (IllegalAccessException e) {
            return (0);
        } catch (NullPointerException e) {
            System.out.println("FAILED: NullPointerException was thrown!");
            e.printStackTrace();
            return (1);
        }
        catch (Exception e) {
            System.out.println("FAILED: Unexpected exception was thrown!");
            e.printStackTrace();
            return (1);            
        }
        
        System.out.println("FAILED: IllegalAccessException was not thrown!");
        return (1);
    }    
}

class TestClass {
    private String s = "Hello, World!";
}
