/*
 * Copyright 2017 The Johns Hopkins University Applied Physics Laboratory LLC
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.jhuapl.dorset.demos;

import javax.mail.MessagingException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class EmailClient {

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     * @throws MessagingException 
     */
    public static void main(String[] args) {
        //config will decide how many consumer threads
        //get config value for number of consumers-- for loop: create x consumers
        Config config = ConfigFactory.load();

        try {
            //EmailClient client = new EmailClient(new EmailManager(config));
            EmailManager manager = new EmailManager(config);
            EmailList emailList = new EmailList();
            //create producer and consumer thread objects
            EmailProducer producer = new EmailProducer(manager, emailList);
            new Thread(producer).start();
            EmailConsumer consumer = new EmailConsumer(manager, emailList);
            new Thread(consumer).start();
        } catch (MessagingException e) {
            System.err.println("Could not connect to server. Check your network connection and account/server configurations. Quitting now.");
            System.exit(-1);
        }
    }
}
