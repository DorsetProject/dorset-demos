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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

public class EmailManager {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);
    
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String MAIL_STORE_TYPE_KEY = "mailStoreType";
    private static final String HOST_KEY = "host";

    private String user;
    private String password;
    private String mailStoreType;
    private String host;
    
    private Session session;
    private Store store;
    private Folder inboxFolder;
    private Folder errorFolder;
    private Folder completeFolder;
    private Message[] unreadMessages = new Message[1];
    private Message[] processingMessages = new Message[1];
    private ArrayDeque<Message> handler = new ArrayDeque<Message>();
    private static final int HOLDS_MESSAGES = 1;
    private String response = "";
    
    /**
    * EmailManager Constructor
    * 
    * @param config  Configuration object that stores mail server information, username, and password
    */
    public EmailManager(Config config) {
        
        user = config.getString(USERNAME_KEY);
        password = config.getString(PASSWORD_KEY);
        mailStoreType = config.getString(MAIL_STORE_TYPE_KEY);
        host = config.getString(HOST_KEY); 
        Properties prop = extractProperties(config);
        
        try {
            session = Session.getDefaultInstance(prop);
            store = session.getStore(mailStoreType);
            store.connect(host, user, password);
        } catch (MessagingException e) {
            logger.error("Failed to start email session");
        }
    }
    
    /**
     * Extract properties from configuration object
     * 
     * @param config  Configuration object that stores mail server information, username, and password
     * @return the properties file
     */
    private Properties extractProperties(Config config) {
        Properties prop = new Properties();
        for (java.util.Map.Entry<java.lang.String, ConfigValue> entry : config.entrySet()) {
            prop.setProperty(entry.getKey(), config.getString(entry.getKey()));
        }        
        return prop;
    }
    /**
     * Retrieve folders and create them if they do not already exist
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
            logger.error("Failed to initialize folders");
        }
    }

    /**
     * Determine which folder should be accessed
     * 
     * @param folder   the folder to be returned. Expressed by an integer value
     * @return Folder   the folder to be accessed
     */
    public Folder determineFolder(EmailType folder) {
        switch (folder) {
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
     * Determine which message array should be used
     * 
     * @param msg   the message to be accessed. Expressed by an integer value
     * @return Message   the message to be used
     */
    public Message[] determineMsg(EmailType msg) {
        switch (msg) {
            case UNREAD:
                return unreadMessages;
            case READ:
                return processingMessages;
            default:
                return null;
        }
    }

    /**
     * Open specified folder
     * 
     * @param folder   the folder to be opened. Expressed by an integer value  
     */
    public void openFolder(EmailType folder) {
        try {
            determineFolder(folder).open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            logger.error("Failed to open folders");
        }
    }

    /**
     * Return whether specified folder is open or not
     * 
     * @param folder  the folder to be tested. Expressed by an integer value
     * @return boolean   whether the folder is open or not
     */
    public boolean folderIsOpen(EmailType folder) {
        return determineFolder(folder).isOpen();
    }

    /**
     * Close specified folder
     * 
     * @param folder   the folder to be close. Expressed by an integer value
     */
    public void closeFolder(EmailType folder) {
        try {
            determineFolder(folder).close(false);
        } catch (MessagingException e) {
            logger.error("Failed to close folders");
        }
    }
    
    /**
     * Close store
     */
    public void closeStore() {
        try {
            store.close();
        } catch (MessagingException e) {
            logger.error("Failed to close store");
        }
    }

    /**
     * Return number of messages in specified folder
     * 
     * @param folder   the folder to look in. Expressed by an integer value
     * @return int value   messages left in folder
     */
    public int getCount(EmailType folder) {
        try {
            return determineFolder(folder).getMessageCount();
        } catch (MessagingException e) {
            logger.error("Failed to count emails");
            return -1;
        }
    }

    /**
     * Assign the next email to be handled to a variable
     * 
     * @param folder the folder message will be found in. Expressed by an integer value
     * @param msg   the Message variable to populate
     * @return boolean value   whether or not a message was available
     */
    public boolean getEmail(EmailType folder, EmailType msg) {
        try {
            determineMsg(msg)[0] = determineFolder(folder).getMessage(1);
            if (msg == EmailType.UNREAD) {
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
            logger.error("Failed to retrieve email");
            return false;
        }
    }
    
    /**
     * Mark an email as seen
     * 
     * @param msg  the message to be marked seen
     */
    public void markSeen(EmailType msg) {
        try {
            determineMsg(msg)[0].setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
            logger.error("Failed to mark email as seen");
        }
    }

    /**
     * Add an email to the queue
     * 
     * @param msg   the message to be added to the queue
     */
    public void addToQ(EmailType msg) {
        handler.addLast(determineMsg(msg)[0]);
    }

    /**
     * Determine if the queue is empty
     * 
     * @return boolean   whether the queue is empty
     */
    public boolean isQEmpty() {
        return handler.isEmpty();
    }

    /**
     * Remove email from the head of the queue
     * 
     * @param msg   the Message variable to assign the removed message to
     */
    public void removeFromQ(EmailType msg) {
        determineMsg(msg)[0] = handler.removeFirst();
    }

    /**
     * Retrieve and return text from the specified email
     * 
     * @param msg   the message to read. Expressed by an integer value
     * @return msgContent  the text from the specified email   
     */
    public String readEmail(EmailType msg) {
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
            logger.error("Failed to fetch email body");
            return "Error occured while reading email";
        }
        
    }

    /**
     * Retrieve the body of an email
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
        } catch (MessagingException e) {
            logger.error("Failed to fetch email body");
            return "An error occured while processing your email.";
        } catch (IOException e) {
            logger.error("Failed to fetch email body");
            return "An error occured while processing your email.";
        }

    }

    /**
     * Copy specified email from one folder to another
     * 
     * @param fromFolder   the folder email is currently located in
     * @param toFolder   the folder email is going to be moved to
     * @param msg   the message to be moved
     * @return message that email was moved TODO
     */
    public String copyEmail(EmailType fromFolder, EmailType toFolder, EmailType msg) {
        try {
            determineFolder(fromFolder).copyMessages(determineMsg(msg), determineFolder(toFolder));
            return "email moved from " + fromFolder + " to " + toFolder;
        } catch (MessagingException e) {
            logger.error("Failed to copy email");
            return "Error occured while moving email";
        }
    }

    /**
     * Delete specified email from a folder
     * 
     * @param folder   the folder the email should be deleted from
     * @param msg   the message to be deleted
     * @return   message the email was deleted TODO
     */
    public String deleteEmail(EmailType folder, EmailType msg) {
        try {
            determineMsg(msg)[0].setFlag(Flags.Flag.DELETED, true);
            determineFolder(folder).expunge();
            determineFolder(folder).close(false);
            determineFolder(folder).open(Folder.READ_WRITE);
            return "email deleted from " + folder;
        } catch (MessagingException e) {
            logger.error("Failed to delete email");
            return "Error occured while deleting email";
        }
    }

    /**
     * Send a reply to the specified email
     * 
     * @param response   the text to be used for the body of the reply
     * @param msg  the message to be responded to
     * @return message to indicate that email was sent TODO
     */
    public String replyToEmail(String response, EmailType msg) {
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
            logger.error("Failed to rely to email");
            return "Error occured while sending email";
        }
    }
}
