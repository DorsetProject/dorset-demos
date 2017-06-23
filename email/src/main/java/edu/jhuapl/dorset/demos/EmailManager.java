package edu.jhuapl.dorset.demos;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailManager {

    private String user;
    private String password;
    private String mailStoreType = "imap";
    private String host = "";
    private Properties prop = new Properties();

    private Session session;
    private Store store;
    private Folder inboxFolder;
    private Folder errorFolder;
    private Folder completeFolder;
    private Message[] unreadMessages = new Message[1];
    private Message[] processingMessages = new Message[1];
    private ArrayDeque<Message> handler = new ArrayDeque<Message>();
    
    private static final String IMAP = "imap";
    private static final int HOLDS_MESSAGES = 1;
    private String response = "";

    /**
     * Connects to server
     *  
     * @param user   the email username 
     * @param password   the email password
     */
    public EmailManager(String user, String password) {
        this.user = user;
        this.password = password;

        prop.put("mail.store.protocol", mailStoreType);
        prop.put("mail.imap.host", host);
        prop.put("mail.imap.port", "143");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "25");

        try {
            session = Session.getDefaultInstance(prop);
            store = session.getStore(IMAP);
            store.connect(host, this.user, this.password);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializing 3 folders
     */
    public void initFolders() {
        try {
            if (!store.getFolder(EmailType.ERROR.getValue()).exists()) {
                store.getFolder(EmailType.ERROR.getValue()).create(HOLDS_MESSAGES);
            }
            if (!store.getFolder(EmailType.COMPLETE.getValue()).exists()) {
                store.getFolder(EmailType.COMPLETE.getValue()).create(HOLDS_MESSAGES);
            }
            errorFolder = store.getFolder(EmailType.ERROR.getValue());
            completeFolder = store.getFolder(EmailType.COMPLETE.getValue());
            inboxFolder = store.getFolder(EmailType.INBOX.getValue());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines which folder should be accessed
     * 
     * @param folderNum the folder to be returned. Expressed by an integer value
     * @return Folder   the folder to be accessed
     */
    public Folder determineFolder(String folder) {
        switch (EmailType.getType(folder)) {
            case INBOX:
                return inboxFolder;
            case ERROR:
                return errorFolder;
            case COMPLETE:
                return completeFolder;
            default:
                return null;
        }
    }

    /**
     * Determines which Message array should be used
     * 
     * @param msg   the message to be accessed. Expressed by an integer value
     * @return Message   the message to be used
     */
    public Message[] determineMsg(String msg) {
        switch (EmailType.getType(msg)) {
            case UNREAD:
                return unreadMessages;
            case PROCESSING:
                return processingMessages;
            default:
                return null;
        }
    }

    /**
     * Opens specified folder
     * 
     * @param folder   the folder to be opened. Expressed by an integer value  
     */
    public void openFolder(String folder) {
        try {
            determineFolder(folder).open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns whether specified folder is open or not
     * 
     * @param folder  the folder to be tested. Expressed by an integer value
     * @return boolean   whether the folder is open or not
     */
    public boolean folderIsOpen(String folder) {
        return determineFolder(folder).isOpen();
    }

    /**
     * Closes specified folder
     * 
     * @param folder   the folder to be close. Expressed by an integer value
     */
    public void closeFolder(String folder) {
        try {
            determineFolder(folder).close(false);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    
    public void closeStore() {
        try {
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns number of messages in specified folder
     * 
     * @param folder   the folder to look in. Expressed by an integer value
     * @return int value   messages left in folder
     */
    public int getCount(String folder) throws MessagingException {
        return determineFolder(folder).getMessageCount();
    }

    /**
     * Assigns the next email to be handled to a variable
     * 
     * @param folder the folder message will be found in. Expressed by an integer value
     * @param msg   the Message variable to populate
     * @return boolean value   whether or not a message was available
     */
    public boolean getEmail(String folder, String msg) {
        try {
            determineMsg(msg)[0] = determineFolder(folder).getMessage(1);
            if (msg == EmailType.UNREAD.getValue()) {
                int counter = 1;
                while (determineMsg(msg)[0].isSet(Flags.Flag.SEEN)) {
                    determineMsg(msg)[0] = determineFolder(folder).getMessage(counter);
                    counter++;
                    if (counter > determineFolder(folder).getMessageCount()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Marks an email as seen
     * 
     * @param msg  the message to be marked seen
     */
    public void markSeen(String msg) {
        try {
            determineMsg(msg)[0].setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an email to the queue
     * 
     * @param msg   the message to be added to the queue
     */
    public void addToQ(String msg) {
        handler.addLast(determineMsg(msg)[0]);
    }

    /**
     * Determines if the queue is empty
     * 
     * @return boolean   whether the queue is empty
     */
    public boolean isQEmpty() {
        return handler.isEmpty();
    }

    /**
     * Removes email from the head of the queue
     * 
     * @param msg   the Message variable to assign the removed message to
     */
    public void removeFromQ(String msg) {
        determineMsg(msg)[0] = handler.removeFirst();
    }

    /**
     * Retrieves and returns text from the specified email
     * 
     * @param msg   the message to read. Expressed by an integer value
     * @return msgContent  the text from the specified email   
     */
    public String readEmail(String msg) {
        try {
            Message mess = determineMsg(msg)[0];
            System.out.println("---------------------------------");
            System.out.println("From: " + mess.getFrom()[0]);
            System.out.println("Subject: " + mess.getSubject());
    
            String msgContent = "";
            response = "";
            msgContent += writePart(mess);
            System.out.println("Email reads:  \"" + msgContent + "\"");
    
            return msgContent;
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error occured while reading email";
        }
        
    }

    /**
     * Retrieves the body of an email
     * 
     * @param part   the email body to be retrieved
     * @return response   the text of an email
     */
    public String writePart(Part part) {
        try {
            if (part.isMimeType("text/plain")) {
                response += ((String) part.getContent());
            } else if (part.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) part.getContent();
                int count = mp.getCount();
                for (int n = 0; n < count; n++) {
                    String text = (writePart(mp.getBodyPart(n)));
                }
            } else if (part.isMimeType("message/rfc822")) {
                response += writePart((Part) part.getContent());
            }
            return response;
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "An error occured while processing your email.";
        }

    }

    /**
     * Copies specified email from one folder to another
     * 
     * @param fromFolder   the folder email is currently located in
     * @param toFolder   the folder email is going to be moved to
     * @param msg   the message to be moved
     * @return message that email was moved TODO
     */
    public String copyEmail(String fromFolder, String toFolder, String msg) {
        try {
            determineFolder(fromFolder).copyMessages(determineMsg(msg), determineFolder(toFolder));
            return "email moved from " + fromFolder + " to " + toFolder;
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error occured while moving email";
        }
    }

    /**
     * Deletes specified email from a folder
     * 
     * @param folder   the folder the email should be deleted from
     * @param msg   the message to be deleted
     * @return   message the email was deleted TODO
     */
    public String deleteEmail(String folder, String msg) {
        try {
            determineMsg(msg)[0].setFlag(Flags.Flag.DELETED, true);
            determineFolder(folder).expunge();
            determineFolder(folder).close(false);
            determineFolder(folder).open(Folder.READ_WRITE);
            return "email deleted from " + folder;
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error occured while deleting email";
        }
    }

    /**
     * Sends a reply to the specified email
     * 
     * @param response   the text to be used for the body of the reply
     * @param msg  the message to be responded to
     * @return message to indicate that email was sent TODO
     */
    public String replyToEmail(String response, String msg) {
        try { 
            Message replymsg = new MimeMessage(session);
            String to = InternetAddress.toString(determineMsg(msg)[0].getReplyTo());
    
            replymsg = (MimeMessage) determineMsg(msg)[0].reply(false);
            replymsg.setFrom(new InternetAddress(to));
            replymsg.setText(response);
            replymsg.setReplyTo(determineMsg(msg)[0].getReplyTo());
    
            Transport transport = session.getTransport("smtp");
            try {
                transport.connect(user, password);
                transport.sendMessage(replymsg, replymsg.getAllRecipients());
            } finally {
                transport.close();
            }
            return "email sent";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error occured while sending email";
        }
    }
}
