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

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.DateTimeAgent;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

public class EmailConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

    private Application app;
    private EmailManager manager;
    private EmailQueue emailQueue;

    /**
     * Create an EmailConsumer
     *
     * @param manager   EmailManager object
     * @param emailQueue   EmailQueue object
     */
    public EmailConsumer(EmailManager manager, EmailQueue emailQueue) {
        Agent agent = new DateTimeAgent();
        Router router = new SingleAgentRouter(agent);
        app = new Application(router);
        this.manager = manager;
        this.emailQueue = emailQueue;
    }

    /**
     * Run consumer thread
     */
    public void run() {
        while (true) {
            handleSeenMesage();
        }
    }

    /**
     * Handle seen messages
     */
    private void handleSeenMesage() {
        try {
            if (manager.getCount(FolderType.INBOX) > 0) {
                getAndReplyToEmail();
            }
        } catch (MessagingException  e) {
            logAndOutputError(e);
        }
        sleepThread();
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
     * @return the response from a Dorset agent
     */
    private String processMessage(String text) {
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

    /**
     * Log and output an error message
     *
     * @param e   the exception thrown
     */
    private void logAndOutputError(MessagingException exception) {
        logger.error("Could not process email. ", exception);
        System.err.println("Could not connect to server. Check your network connection. Quitting now.");
        System.exit(-1);
    }

    /**
     * Sleep thread
     */
    private void sleepThread() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.info("Consumer thread was interupted");
        }
    }
}
