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
    private String response = "";
    private static final String ENCAPSULATED = "message/rfc822";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String MULTIPART = "multipart/*";
    
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
            initFolders();
            inboxFolder.open(Folder.READ_WRITE);
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
    private void initFolders() {
        try {
            if (!store.getFolder(FolderType.ERROR.getValue()).exists()) {
                store.getFolder(FolderType.ERROR.getValue()).create(Folder.HOLDS_MESSAGES);
            }
            if (!store.getFolder(FolderType.COMPLETE.getValue()).exists()) {
                store.getFolder(FolderType.COMPLETE.getValue()).create(Folder.HOLDS_MESSAGES);
            }
            inboxFolder = store.getFolder(FolderType.INBOX.getValue());
            errorFolder = store.getFolder(FolderType.ERROR.getValue());
            completeFolder = store.getFolder(FolderType.COMPLETE.getValue());
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
    private Folder getFolder(FolderType folder) {
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
     * Return number of messages in specified folder
     * 
     * @param folder   the folder to look in
     * @return the number of messages left in folder
     */
    public int getCount(FolderType folder) {
        try {
            System.out.println(getFolder(folder));
            return getFolder(folder).getMessageCount();
        } catch (MessagingException e) {
            logger.error("Failed to count emails");
            return -1;
        }
    }
    
    /**
     * Get a message from a folder
     * 
     * @param folder   the folder to retrieve a message from
     * @return the message
     */
    public Message getMessage(FolderType folder) {
        try {
            return getFolder(folder).getMessage(1);
        } catch (MessagingException e) {
            logger.error("Failed to retrieve email");
            return null;
        }  
    }
    
    /**
     * Mark an email as seen
     * 
     * @param msg  the message to be marked seen
     */
    //not currently being used, but may be needed for threading
    public void markSeen(Message msg) {
        try {
            msg.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
            logger.error("Failed to mark email as seen");
        }
    }

    /**
     * Retrieve and return text from an email
     * 
     * @param msg   the message to be read
     * @return msgContent  the text the email
     */
    public String readEmail(Message msg) {
        try {
            System.out.println("---------------------------------");
            System.out.println("From: " + msg.getFrom()[0]);
            System.out.println("Subject: " + msg.getSubject());
    
            String msgContent = "";
            response = "";
            msgContent += writePart(msg);
            System.out.println("Email reads: " + msgContent);
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
    private String writePart(Part part) {
        try {
            if (part.isMimeType(TEXT_PLAIN)) {
                response += ((String) part.getContent());
            } else if (part.isMimeType(MULTIPART)) {
                Multipart mp = (Multipart) part.getContent();
                int count = mp.getCount();
                for (int n = 0; n < count; n++) {
                    @SuppressWarnings("unused")
                    String unusedText = (writePart(mp.getBodyPart(n)));
                }
            } else if (part.isMimeType(ENCAPSULATED)) {
                response += writePart((Part) part.getContent());
            }
            return response;
        } catch (MessagingException e) {
            logger.error("Failed to fetch email body");
            return "error";
        } catch (IOException e) {
            logger.error("Failed to fetch email body");
            return "error";
        }
    }

    /**
     * Send a reply to the specified email
     *
     * @param response   the text to be used for the body of the reply
     * @param msg  the message to be responded to
     */
    public void sendMessage(String response, Message msg) {
        try {
            Message replymsg = new MimeMessage(session);
            String to = InternetAddress.toString(msg.getReplyTo());

            replymsg = (MimeMessage) msg.reply(false);
            replymsg.setFrom(new InternetAddress(to));
            replymsg.setText(response);
            replymsg.setReplyTo(msg.getReplyTo());

            Transport transport = session.getTransport("smtp");
            try {
                transport.connect(user, password);
                transport.sendMessage(replymsg, replymsg.getAllRecipients());
            } finally {
                transport.close();
            }
            System.out.println("email sent");
        } catch (MessagingException e) {
            logger.error("Failed to rely to email");
        }
    }

    /**
     * Copy specified email from one folder to another
     * 
     * @param fromFolder   the folder email is currently located in
     * @param toFolder   the folder email is going to be moved to
     * @param msg   the message to be moved
     */
    public void copyEmail(FolderType fromFolder, FolderType toFolder, Message msg) {
        try {
            Message[] messages = {msg};
            getFolder(fromFolder).copyMessages(messages, getFolder(toFolder));
        } catch (MessagingException e) {
            logger.error("Failed to copy email");
        }
    }

    /**
     * Delete specified email from a folder
     * 
     * @param folder   the folder the email should be deleted from
     * @param msg   the message to be deleted
     */
    public void deleteEmail(FolderType folder, Message msg) {
        try {
            msg.setFlag(Flags.Flag.DELETED, true);
            getFolder(folder).expunge();
            getFolder(folder).close(false);
            getFolder(folder).open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            logger.error("Failed to delete email");
        }
    }

    /**
     * Close inbox folder and store
     * 
     */
    public void close() {
        try {
            inboxFolder.close(false);
            store.close();
        } catch (MessagingException e) {
            logger.error("Failed to close folders and store");
        }
    }
}
