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
        long startTime = 1111420342104L; //System.currentTimeMillis();        
/*        try{ 
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            System.out.println("Unexpected exception was thrown:");
            e.printStackTrace();
            return error();
        }
*/
        long endTime = 1111420343103L; //System.currentTimeMillis();
        
        long totalTime = endTime - startTime;
        
        System.out.println("Start Time = " + startTime);
        System.out.println("End TIME = " + endTime);
        System.out.println("End TIME = " + totalTime/1000.0);

        return (totalTime/1000.0 == Double.NEGATIVE_INFINITY) 
                ? (1) : (0);
    }        
}
