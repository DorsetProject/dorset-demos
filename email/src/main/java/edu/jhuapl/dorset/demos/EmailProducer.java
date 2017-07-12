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

public class EmailProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmailProducer.class);

    private EmailManager manager;
    private EmailQueue emailQueue;

    /**
     * Create an EmailProducer
     *
     * @param manager   EmailManager object
     * @param emailQueue   EmailQueue object
     */
    public EmailProducer(EmailManager manager, EmailQueue emailQueue) {
        this.manager = manager;
        this.emailQueue = emailQueue;
    }

    /**
     * Run producer thread
     */
    public void run() {
        printNumberOfMessages();
        while (true) {
            handleUnseenMessage();
        }
    }

    /**
     * print number of messages in inbox
     */
    private void printNumberOfMessages() {
        try {
            System.out.println("\nMessages in Inbox: " + manager.getCount(FolderType.INBOX));
        } catch (MessagingException e) {
            logAndOutputError(e);
        }
    }

    /**
     * Handle unseen messages
     */
    private void handleUnseenMessage() {
        try {
            if (manager.getCount(FolderType.INBOX) > 0 && hasUnseenMessages()) {
                emailQueue.putMessage(manager.getUnseenMessage(FolderType.INBOX));
                manager.markSeen(manager.getUnseenMessage(FolderType.INBOX));
            }
            sleepThread();
        } catch (MessagingException e) {
            logAndOutputError(e);
        }
    }

    /**
     * Returns whether the inbox has unseen messages
     *
     * @return whether the inbox has unseen messages
     */
    public boolean hasUnseenMessages() {
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
        logger.error("Could not access inbox " + exception);
        System.err.println(exception.getMessage() + " Quitting now.");
        System.exit(-1);
    }

    /**
     * Sleep thread
     */
    private void sleepThread() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.info("Producer thread was interupted");
        }
    }
}
