package edu.jhuapl.dorset.demos;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmailProducer.class);

    private EmailManager manager;
    private EmailList emailList;
    
    public EmailProducer(EmailManager manager, EmailList emailList) {
        System.out.println("creating producer");
        this.manager = manager;
        this.emailList = emailList;
    }
    
    public void run() {
        System.out.println("running producer");
        while (true) {
            try {
                if (manager.getCount(FolderType.INBOX) > 0) {
                    logger.info("Inbox contains " + manager.getCount(FolderType.INBOX) + " messages");
                    emailList.putMessage(manager.getUnseenMessage(FolderType.INBOX));
                    System.out.println("message in queue");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Producer thread was interupted");
                }
                System.out.println("inbox was empty");
            } catch (MessagingException e) {
                logger.error("Could not access inbox " + e);
                System.err.println("Could not connect to server. Check your network connection and account/server configurations. Quitting now.");
                System.exit(-1);                
            }
        }
    }
}
