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

import java.util.Scanner;

import javax.mail.Message;

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
     */
    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        EmailClient client = new EmailClient(new EmailManager(config));
        client.run();
        client.close();
    }

    /**
     * Process and respond to emails
     */
    private void run() {
        Scanner scan = new Scanner(System.in);

        System.out.println("Inbox: " + manager.getCount(FolderType.INBOX));
        System.out.println("Type c to continue or q to quit");

        while (scan.nextLine().equals("c")) {
            while (manager.getCount(FolderType.INBOX) > 0) {
                Message msg = manager.getMessage(FolderType.INBOX);
                System.out.println("email to be read: ");
                String text = manager.readEmail(msg);
                if (text.equals("error")) {
                    manager.sendMessage("An error ocured while processing your response", msg);
                    manager.copyEmail(FolderType.INBOX, FolderType.ERROR, msg);
                    manager.deleteEmail(FolderType.INBOX, msg);
                } else {
                    manager.sendMessage(processMessage(msg, text), msg);
                    manager.copyEmail(FolderType.INBOX, FolderType.COMPLETE, msg);
                }
                manager.deleteEmail(FolderType.INBOX, msg);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error("Failed to sleep thread");
            }
        }
        scan.close(); 
    }

    /**
     * Close manager and scanner
     */
    private void close() {
        manager.close();
        
    }

    /**
     * Access a Dorset agent and process the message text
     *
     * @param msg   the message to be read
     * @return the response from a Dorset agent
     */
    private static String processMessage(Message msg, String text) {
        Request request = new Request(text);
        Response response = app.process(request);
        return response.getText();
    }
}
