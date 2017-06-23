package edu.jhuapl.dorset.demos;

import javax.mail.MessagingException;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.DateTimeAgent;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

public class EmailClient {
    private static final int INBOX = 1;
    private static final int COMPLETE = 2;
    private static final int ERROR = 3;
    private static final int UNREAD = 1;
    private static final int PROCESSING = 2;
    private static EmailManager email;
    private static int emailsLeftInbox;

    /**
     * Main method in Email Client
     * 
     * @param args   command line arguments
     */
    public static void main(String[] args) {

        email = new EmailManager("", "");
        email.initFolders();

        try {
            email.openFolder(EmailType.INBOX.getValue());
            emailsLeftInbox = email.getCount(EmailType.INBOX.getValue());
        } catch (MessagingException e1) {
            e1.printStackTrace();
        }

        System.out.println("Inbox: " + emailsLeftInbox);

        try {
            while (emailsLeftInbox > 0) {
                if (!email.folderIsOpen(EmailType.INBOX.getValue())) {
                    email.openFolder(EmailType.INBOX.getValue());
                }

                if (email.getEmail(EmailType.INBOX.getValue(), EmailType.UNREAD.getValue())) {
                    email.markSeen(EmailType.UNREAD.getValue());
                    System.out.println("seen");
                    email.addToQ(EmailType.UNREAD.getValue());
                }
                emailsLeftInbox = email.getCount(EmailType.INBOX.getValue());

                if (!email.isQEmpty()) {
                    if (!email.folderIsOpen(EmailType.INBOX.getValue())) {
                        email.openFolder(EmailType.INBOX.getValue());
                    }
                    email.removeFromQ(EmailType.PROCESSING.getValue());
                    System.out.println("email to be read: ");
                    String emailText = email.readEmail(EmailType.PROCESSING.getValue());

                    if (emailText.equals("An error occured while processing your email.")) {
                        System.out.println(email.replyToEmail(emailText, EmailType.PROCESSING.getValue()));
                        System.out.println(email.copyEmail(EmailType.INBOX.getValue(), EmailType.ERROR.getValue(),
                                        EmailType.PROCESSING.getValue()));
                    } else {
                        Agent agent = determineAgent(emailText);
                        Router router = new SingleAgentRouter(agent);
                        Application app = new Application(router);
                        Request request = new Request(emailText);
                        Response response = app.process(request);

                        System.out.println("response: " + response.getText());
                        System.out.println(email.replyToEmail(response.getText(), EmailType.PROCESSING.getValue()));
                        System.out.println(email.copyEmail(EmailType.INBOX.getValue(), EmailType.COMPLETE.getValue(), 
                                        EmailType.PROCESSING.getValue()));
                    }

                    System.out.println(email.deleteEmail(EmailType.INBOX.getValue(), EmailType.PROCESSING.getValue()));
                    emailsLeftInbox = email.getCount(EmailType.INBOX.getValue());
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            email.closeFolder(EmailType.INBOX.getValue());
            email.closeStore();
        }
    }

    /**
     * Determines which agent the request should be sent to
     *
     * @param text   the email text
     * @return Agent   the agent to send request to
     */
    public static Agent determineAgent(String text) {
        return new DateTimeAgent();
    }
}
