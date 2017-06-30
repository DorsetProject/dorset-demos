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
    private EmailManager manager;
    private Application app;

    /**
     * EmailClient
     *
     * @param manager   EmailManager object
     */
    public EmailClient(EmailManager manager) {
        this.manager = manager;
        Agent agent = new DateTimeAgent();
        Router router = new SingleAgentRouter(agent);
        app = new Application(router);
    }

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        EmailClient client = null;
        try {
            client = new EmailClient(new EmailManager(config));
        } catch (MessagingException e) {
            System.err.println("Could not connect to server. Check your network connection and account/server configurations. Quitting now.");
            System.exit(-1);
        }
        try {
            client.run();
        } catch (MessagingException e) {
            System.err.println("An error occured while processing emails. Check your network connection. Quitting now.");
        }
        client.close();
    }

    /**
     * Process and respond to emails
     *
     * @throws MessagingException   if errors occur while processing email
     */
    private void run() throws MessagingException {

        try {
            System.out.println("Running the Email Client. Press Control-C to quit. \nMessages in Inbox: " + manager.getCount(FolderType.INBOX));
            while (true) {
                if (manager.getCount(FolderType.INBOX) > 0) {
                    logger.info("Inbox contains " + manager.getCount(FolderType.INBOX) + " messages");
                }
                while (manager.getCount(FolderType.INBOX) > 0) {
                    Message msg = manager.getMessage(FolderType.INBOX);
                    String text = manager.readEmail(msg);
                    manager.sendMessage(processMessage(text), msg);
                    manager.copyEmail(FolderType.INBOX, FolderType.COMPLETE, msg);
                    manager.deleteEmail(FolderType.INBOX, msg);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException exception) {
                    logger.error("Thread was interrupted");
                }
            }
        } catch (MessagingException  e) {
            throw new MessagingException("Fatal error has occured. Quitting now.", e);
        } finally {
        }
    }

    /**
     * Close EmailManager objects
     */
    private void close() {
        manager.close();  
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
}
