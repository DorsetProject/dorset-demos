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
    private static EmailManager manager;

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        Config config = ConfigFactory.load();
        manager = new EmailManager(config);
        System.out.println("Inbox: " + manager.getCount(FolderType.INBOX));

        try {
            System.out.println("Type c to continue or q to quit");
            while(scan.nextLine().equals("c")) {
                while (manager.getCount(FolderType.INBOX) > 0) {
                    Message msg = manager.getMessage(FolderType.INBOX);
                    String text = getMessageText(msg);
                    if (text.equals("error")) {
                        manager.sendMessage("An error ocured while processing your response", msg);
                        manager.copyEmail(FolderType.INBOX, FolderType.ERROR, msg);
                        manager.deleteEmail(FolderType.INBOX, msg);
                    }
                    else {
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
        } finally {
            manager.close();
            scan.close();
        }
    }

    /**
     * Get the agent the request should be sent to
     *
     * @param text   the email text
     * @return Agent   the agent to send request to
     */
    private static Agent getAgent(String text) {
        return new DateTimeAgent();
    }

    /**
     * Get text from the body of the message
     *
     * @param msg   the message to be read
     * @return the message text
     */
    private static String getMessageText(Message msg) {
        System.out.println("email to be read: ");
        return manager.readEmail(msg);
    }

    /**
     * Access a Dorset agent and process the message text
     *
     * @param msg   the message to be read
     * @return the response from a Dorset agent
     */
    private static String processMessage(Message msg, String text) {
        Agent agent = getAgent(text);
        Router router = new SingleAgentRouter(agent);
        Application app = new Application(router);
        Request request = new Request(text);
        Response response = app.process(request);
        return response.getText();
    }
}
