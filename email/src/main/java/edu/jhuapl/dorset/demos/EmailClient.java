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

import java.io.IOException;
import java.util.Scanner;

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
    private static Application app;

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
     * @throws MessagingException   if cannot connect to server
     * @throws IOException   if errors occur while processing email
     */
    public static void main(String[] args) throws MessagingException, IOException {
        Config config = ConfigFactory.load();
        EmailClient client;
        try {
            client = new EmailClient(new EmailManager(config));
        } catch (MessagingException e) {
            throw new MessagingException("Could not connect to server. Check your connection and configurations. Quitting now.", e);
        }
        client.run();
        client.close();
    }

    /**
     * Process and respond to emails
     *
     * @throws MessagingException   if errors occur while processing email
     * @throws IOException   if errors occur while processing email
     */
    private void run() throws MessagingException, IOException {
        Scanner scan = new Scanner(System.in);

        try {
            System.out.println("Inbox: " + manager.getCount(FolderType.INBOX));
            System.out.println("Type c to continue or q to quit: ");

            while (!scan.nextLine().equals("q")) {
                while (manager.getCount(FolderType.INBOX) > 0) {
                    Message msg = manager.getMessage(FolderType.INBOX);
                    String text = manager.readEmail(msg);
                    if (text == null) { 
                        manager.sendMessage("An error ocured while processing your response", msg);
                        manager.copyEmail(FolderType.INBOX, FolderType.ERROR, msg);
                    } else {
                        manager.sendMessage(processMessage(text), msg);
                        manager.copyEmail(FolderType.INBOX, FolderType.COMPLETE, msg);
                    }
                    manager.deleteEmail(FolderType.INBOX, msg);
                    //TODO need to do something with true/false return value
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException exception) {
                    logger.error("Thread was interrupted");
                }
                System.out.println("continue/quit?: ");
            }
        } catch (MessagingException  e) {
            close();
            throw new MessagingException("Fatal error has occured. Quitting now.", e);
        } finally {
            scan.close();
        }
    }

    /**
     * Close EmailManager objects
     * @throws MessagingException   if cannot close properly
     */
    private void close() throws MessagingException {
        manager.close();  
    }

    /**
     * Access a Dorset agent and process the email text
     *
     * @param text   the text sent to a Dorset agent
     * @return the response from a Dorset agent
     */
    private static String processMessage(String text) {
        Request request = new Request(text);
        Response response = app.process(request);
        return response.getText();
    }
}
