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

import javax.mail.Message;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.DateTimeAgent;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

public class EmailClient {

    private static final Logger logger = LoggerFactory.getLogger(EmailClient.class);

    private static final String CONSUMER_THREAD_COUNT_KEY = "consumerThreads";

    private EmailManager manager;
    private EmailQueue emailQueue;
    private String consumerThreads;
    private Application app;

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
            System.err.println("Check your network connection and account/server configurations. Quitting now.");
            System.exit(-1);
        }
        
        EmailProducer producer = new EmailProducer(this);
        new Thread(producer).start();
        for (int n = 0; n < getConsumerThreadCount(); n++) {
            EmailConsumer consumer = new EmailConsumer(this);
            new Thread(consumer).start();
        }
    }

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Running the Email Client. Press Control-C to quit. ");
        new EmailClient();
    }

    /**
     * Parse String consumerThreads into an integer
     *
     * @return consumerThreadCount   the number of consumer threads to be created
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
     * Print number of messages in inbox
     */
    public void printNumberOfMessages() {
        try {
            System.out.println("\nMessages in Inbox: " + manager.getCount(FolderType.INBOX));
        } catch (MessagingException e) {
            logAndOutputError(e);
        }
    }

    /**
     * Handle unseen messages
     */
    public synchronized void handleUnseenMessage() {
        try {
            if (manager.getCount(FolderType.INBOX) > 0 && hasUnseenMessages()) {
                emailQueue.putMessage(manager.getUnseenMessage(FolderType.INBOX));
                manager.markSeen(manager.getUnseenMessage(FolderType.INBOX));
            }
            waitThread();
        } catch (MessagingException e) {
            logAndOutputError(e);
        }
    }

    /**
     * Returns whether the inbox has unseen messages
     *
     * @return whether the inbox has unseen messages
     */
    private boolean hasUnseenMessages() {
        try {
            manager.getUnseenMessage(FolderType.INBOX);
        } catch (IndexOutOfBoundsException e) {
            return false;
        } catch (MessagingException e) {
            return true;
        }
        return true;
    }

    /**
     * Log and output an error message
     *
     * @param e   the exception thrown
     */
    private void logAndOutputError(MessagingException exception) {
        logger.error("Could not process email. ", exception);
        System.err.println(exception.getMessage() + " Check your network connection. Quitting now.");
        System.exit(-1);
    }

    /**
     * Wait thread
     */
    public void waitThread() {
        try {
            wait(500);
        } catch (InterruptedException e) {
            logger.info("Thread was interupted");
        }
    }

    /**
     * Handle seen messages
     */
    public synchronized void handleSeenMesage() {
        try {
            if (manager.getCount(FolderType.INBOX) > 0) {
                getAndReplyToEmail();
            }
            waitThread();
        } catch (MessagingException  e) {
            logAndOutputError(e);
        }
    }

    /**
     * Get and reply to an email
     *
     * @throws MessagingException   if email could not be processed
     */
    private void getAndReplyToEmail() throws MessagingException {
        if (emailQueue.removeMessageIfAny()) {
            Message msg = manager.getSeenMessage(FolderType.INBOX);
            String text = manager.readEmail(msg);
            manager.sendMessage(processMessage(text), msg);
            manager.copyEmail(FolderType.INBOX, FolderType.COMPLETE, msg);
            manager.deleteEmail(FolderType.INBOX, msg);
        }
    }

    /**
     * Access a Dorset agent and process the email text
     *
     * @param text   the text sent to a Dorset agent
     * @return reply   the response from a Dorset agent
     */
    private String processMessage(String text) {
        Agent agent = new DateTimeAgent();
        Router router = new SingleAgentRouter(agent);
        app = new Application(router);
        Request request = new Request(text);
        Response response = app.process(request);
        String reply = response.getText();
        if (reply == null) {
            logger.info("No response from Dorset Agent to: " + text);
            reply = "Sorry, we could not understand your request. \nTry asking about the date or time:"
                            + "\nEx) \"What is the time?\"";
        }
        return reply;
    }
}
