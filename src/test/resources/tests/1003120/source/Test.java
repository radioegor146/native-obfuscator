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



import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;



public class Test {

    public static void main(String[] args) {
        System.exit(new Test().test(Logger.global, args));
    }
    
    public int test(Logger logger, String [] args) {
        Character character1 = new Character('d');
        Character character2 = encodeDecode(character1);
        System.out.println("character1 is '" + character1 + "'");
        System.out.println("character2 is '" + character2 + "'");
        System.out.println(
                "character1.toString().equals(character2.toString()) is " +
                character1.toString().equals(character2.toString()));
        boolean res = character1.equals(character2);
        System.out.println("character1.equals(character2) is " + res);
        return res ? (0) : (1);
    }

    public static Character encodeDecode(Character character) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(out);
        encoder.writeObject(character);
        encoder.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return (Character)new XMLDecoder(in).readObject();

    }
}
