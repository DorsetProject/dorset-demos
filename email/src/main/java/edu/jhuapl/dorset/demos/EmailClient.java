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

    private EmailManager manager;
    private EmailQueue emailQueue;
    private String consumerThreads;

    /**
     * Create an EmailClient
     */
    public EmailClient() {
        Config config = ConfigFactory.load();
        consumerThreads = config.getString(CONSUMER_THREAD_COUNT_KEY);
        emailQueue = new EmailQueue();
        try {
            manager = new EmailManager(config);
        } catch (MessagingException e) {
            System.err.println("Could not connect to server. Check your network connection and account/server configurations. Quitting now.");
            System.exit(-1);
        }
    }

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Running the Email Client. Press Control-C to quit. ");
        EmailClient client = new EmailClient();
        client.createAndStartThreads(client.getConsumerThreadCount());
    }

    /**
     * Parses String consumerThreads into an integer
     *
     * @return consumerThreadCount   the number of consumer threads
     */
    private int getConsumerThreadCount() {
        int consumerThreadCount;
        try {
            consumerThreadCount = Integer.parseInt(consumerThreads);
        } catch (NumberFormatException e) {
            logger.error("Invalid configuration set for consumerThreads. Must be an integer. " + e);
            consumerThreadCount = 1;
        }
        return consumerThreadCount;
    }

    /**
     * Create and start threads
     *
     * @param consumerThreadCount   the number if conumer threads
     */
    private void createAndStartThreads(int consumerThreadCount) {
        EmailProducer producer = new EmailProducer(manager, emailQueue);
        new Thread(producer).start();

        for (int n = 0; n < consumerThreadCount; n++) {
            EmailConsumer consumer = new EmailConsumer(manager, emailQueue);
            new Thread(consumer).start();
        }
    }
}
