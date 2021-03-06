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
        try {
            TestClassLoader clsLoader = new TestClassLoader(
                    ClassLoader.getSystemClassLoader());
            Class cls = clsLoader.loadClass("UnknownClass");
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound exception was thrown!");
        } 
    }
}
        
class TestClassLoader extends ClassLoader {

    public TestClassLoader(ClassLoader clsLoader) {
        super(clsLoader);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        System.out.println("I'm in TestClassLoader findClass() method now!");
        return super.findClass(name);
    }
}
