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
    private EmailList emailList;
    
    public EmailConsumer(EmailManager manager, EmailList emailList) {
        System.out.println("creating consumer");
        //set up Dorset
        Agent agent = new DateTimeAgent();
        Router router = new SingleAgentRouter(agent);
        app = new Application(router);
        this.manager = manager;
        this.emailList = emailList;
    }
    
    public void run() {
        System.out.println("running consumer");
        try {
            while (true) {
                if (!emailList.isEmpty() && manager.getCount(FolderType.INBOX) > 0) {
                    System.out.println("queue wasn't empty");
                    emailList.removeMessage();
                    Message msg = manager.getSeenMessage(FolderType.INBOX);
                    String text = manager.readEmail(msg);
                    manager.sendMessage(processMessage(text), msg);
                    manager.copyEmail(FolderType.INBOX, FolderType.COMPLETE, msg);
                    manager.deleteEmail(FolderType.INBOX, msg);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("Consumer thread was interupted");
                }
                System.out.println("queue was empty");
            }
        } catch (MessagingException  e) {
            logger.error("Could not process email. ", e);
            System.err.println("Could not connect to server. Check your network connection. Quitting now.");
            System.exit(-1); 
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
}
