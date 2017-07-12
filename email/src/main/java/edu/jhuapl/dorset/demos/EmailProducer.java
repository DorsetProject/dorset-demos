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
     * Gets messages from inbox and adds them to a queue
     */
    public void run() {
        try {
            System.out.println("\nMessages in Inbox: " + manager.getCount(FolderType.INBOX));
        } catch (MessagingException e) {
            logger.error("Could not access inbox " + e);
            System.err.println(e.getMessage() + " Quitting now.");
            System.exit(-1);
        }
        while (true) {
            try {
                if (manager.getCount(FolderType.INBOX) > 0 && hasUnseen()) {
                    logger.info("Inbox contains " + manager.getCount(FolderType.INBOX) + " messages");
                    emailQueue.putMessage(manager.getUnseenMessage(FolderType.INBOX));
                    manager.markSeen(manager.getUnseenMessage(FolderType.INBOX));
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.info("Producer thread was interupted");
                }
            } catch (MessagingException e) {
                logger.error("Could not access inbox " + e);
                System.err.println(e.getMessage() + " Quitting now.");
                System.exit(-1);
            }
        }
    }
    
    public boolean hasUnseen() {
        try {
            manager.getUnseenMessage(FolderType.INBOX);
        } catch (IndexOutOfBoundsException e) {
            return false;
        } catch (MessagingException e) {
            return true;
        }
        return true;
    }
}
