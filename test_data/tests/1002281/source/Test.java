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



import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Test {
       public static void main(String[] argv) {
               Test t = new Test();
               System.out.println("Start testBinarySearchArrayListSubList()...");
               t.testBinarySearchArrayListSubList();
               System.out.println("\ntestBinarySearchLinkedListSubList()..."); 
               t.testBinarySearchLinkedListSubList();
               System.out.println("\nOK!");
        }

        public void testBinarySearchArrayListSubList() {
            List list = new ArrayList();
            fillSortedList(list, 1000);
            checkBinSearch(list, 0);
        }

        public void testBinarySearchLinkedListSubList() {
            List list = new LinkedList();
            fillSortedList(list, 1000);
            checkBinSearch(list, 0);
            
        }

        protected void fillSortedList(List list, int size) {
            for (int i = 0; i < size; i++) {
                list.add(new Double(i));
            }
        }

        private void checkBinSearch(List list, int offset) {
            try {
                try {
                    Collections.binarySearch(list, new Object());
                    System.out.println("ClassCastException should be thrown!");
                    System.exit(-99);
                } catch (ClassCastException e) {
                    System.out.println("OK: " + e + " was thrown.");
                }

                try {
                    Collections.binarySearch(list, null);
                    System.out.println("NullPointerException should be thrown!");
                    System.exit(-99);
                } catch (NullPointerException e) {
                    System.out.println("OK: " + e + " was thrown.");
                }
            } catch (Throwable e) {
                System.out.println("Unexpected error was thrown:");
                e.printStackTrace();
                System.exit(-99);
            }
        }
}

