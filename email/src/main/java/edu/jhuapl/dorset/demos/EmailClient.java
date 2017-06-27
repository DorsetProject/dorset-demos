package edu.jhuapl.dorset.demos;

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

    private static EmailManager email;
    private static int emailsLeftInbox;

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {

        Config config = ConfigFactory.load();
        email = new EmailManager(config);    
        email.initFolders();
        email.openFolder(EmailType.INBOX);
        emailsLeftInbox = email.getCount(EmailType.INBOX);
        System.out.println("Inbox: " + emailsLeftInbox);

        try {
            while (emailsLeftInbox > 0) {
                if (email.getEmail(EmailType.INBOX, EmailType.UNREAD)) {
                    email.markSeen(EmailType.UNREAD);
                    System.out.println("seen");
                    email.addToQ(EmailType.UNREAD);
                }
                emailsLeftInbox = email.getCount(EmailType.INBOX);

                if (!email.isQEmpty()) {
                    email.removeFromQ(EmailType.READ);
                    System.out.println("email to be read: ");
                    String emailText = email.readEmail(EmailType.READ);

                    if (emailText.equals("An error occured while processing your email.")) {
                        System.out.println(email.replyToEmail(emailText, EmailType.READ));
                        System.out.println(email.copyEmail(EmailType.INBOX, EmailType.ERROR, EmailType.READ));
                    } else {
                        Agent agent = determineAgent(emailText);
                        Router router = new SingleAgentRouter(agent);
                        Application app = new Application(router);
                        Request request = new Request(emailText);
                        Response response = app.process(request);

                        System.out.println("response: " + response.getText());
                        System.out.println(email.replyToEmail(response.getText(), EmailType.READ));
                        System.out.println(email.copyEmail(EmailType.INBOX, EmailType.COMPLETE, EmailType.READ));
                    }

                    System.out.println(email.deleteEmail(EmailType.INBOX, EmailType.READ));
                    emailsLeftInbox = email.getCount(EmailType.INBOX);
                }
            }
        } finally {
            email.closeFolder(EmailType.INBOX);
            email.closeStore();
            
        }
    }

    /**
     * Determine which agent the request should be sent to
     *
     * @param text   the email text
     * @return Agent   the agent to send request to
     */
    public static Agent determineAgent(String text) {
        return new DateTimeAgent();
    }
}
