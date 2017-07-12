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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class EmailClient {

    private static final Logger logger = LoggerFactory.getLogger(EmailClient.class);

    private static final String CONSUMER_THREAD_COUNT_KEY = "consumerThreads";

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Running the Email Client. Press Control-C to quit. ");

        Config config = ConfigFactory.load();
        String consumerThreads = config.getString(CONSUMER_THREAD_COUNT_KEY);
        int consumerThreadCount;
        try {
            consumerThreadCount = Integer.parseInt(consumerThreads);
        } catch (NumberFormatException e) {
            logger.error("Invalid configuration set for consumerThreads. Must be an integer. " + e);
            consumerThreadCount = 1;
        }

        try {
            EmailManager manager = new EmailManager(config);
            EmailQueue emailList = new EmailQueue();

            EmailProducer producer = new EmailProducer(manager, emailList);
            new Thread(producer).start();

            for (int n = 0; n < consumerThreadCount; n++) {
                EmailConsumer consumer = new EmailConsumer(manager, emailList);
                new Thread(consumer).start();
            }
        } catch (MessagingException e) {
            System.err.println("Could not connect to server. Check your network connection and account/server configurations. Quitting now.");
            System.exit(-1);
        }
    }
}
