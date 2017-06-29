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
    private static final String FROM_KEY = "from";

    private String user;
    private String password;
    private String mailStoreType;
    private String host;
    private String from;
    
    private Session session;
    private Store store;
    private Folder inboxFolder;
    private Folder errorFolder;
    private Folder completeFolder;
    private static final String SMTP = "smtp";
    private static final String ENCAPSULATED = "message/rfc822";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String MULTIPART = "multipart/*";
    
    /**
    * EmailManager Constructor
    * 
    * @param config  Configuration object that stores mail server information, username, and password
    * @throws MessagingException   if connection cannot be established
    */
    public EmailManager(Config config) throws MessagingException {
        
        user = config.getString(USERNAME_KEY);
        password = config.getString(PASSWORD_KEY);
        mailStoreType = config.getString(MAIL_STORE_TYPE_KEY);
        host = config.getString(HOST_KEY); 
        from = config.getString(FROM_KEY);
        Properties prop = extractProperties(config);
        try {
            session = Session.getDefaultInstance(prop);
            store = session.getStore(mailStoreType);
            store.connect(host, user, password);
            initFolders();
            inboxFolder.open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            throw new MessagingException("Failed to set up connection", e);
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
     * Get folders and create them if they do not already exist
     * @throws MessagingException   if folders cannot be initialized
     */
    private void initFolders() throws MessagingException {
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
            store.close();
            throw new MessagingException("Failed to initialize folders", e);
        }
    }

    /**
     * Get folder
     * 
     * @param folder   the folder to be returned
     * @return the folder to be accessed
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
     * Get number of emails in a folder
     * 
     * @param folder   the folder to look in
     * @return the number of emails left in the folder
     * @throws MessagingException   if folder contents cannot be accessed
     */
    public int getCount(FolderType folder) throws MessagingException {
        try {
            return getFolder(folder).getMessageCount();
        } catch (MessagingException e) {
            close();
            throw new MessagingException("Failed access folder contents", e);
        }
    }
    
    /**
     * Get an email from a folder
     * 
     * @param folder   the folder to retrieve an email from
     * @return the email
     * @throws MessagingException   if email cannot be retrieved
     */
    public Message getMessage(FolderType folder) throws MessagingException {
        try {
            return getFolder(folder).getMessage(1);
        } catch (MessagingException e) {
            close();
            throw new MessagingException("Failed to retrieve email", e);
        }  
    }
    
    /**
     * Mark an email as seen
     * 
     * @param msg  the email to be marked seen
     * @throws MessagingException   if email cannot be properly marked
     */
    //not currently being used, but may be needed for threading
    public void markSeen(Message msg) throws MessagingException {
        try {
            msg.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException e) {
            throw new MessagingException("Failed to mark email as seen", e);
        }
    }

    /**
     * Retrieve and return text from an email
     * 
     * @param msg   the email to be read
     * @return the text the email
     * @throws MessagingException   if email cannot be accessed
     * @throws IOException   if email cannot be accessed
     */
    public String readEmail(Message msg) throws MessagingException, IOException {
        return getText(msg);
    }

    /**
     * Get the text of an email
     * 
     * @param part   the email body to be retrieved
     * @return text  the text of the email
     * @throws MessagingException   if mime type cannot be accessed
     * @throws IOException   if email content cannot be accessed
     */
    private String getText(Part part) throws MessagingException, IOException {
        try {
            String text = "";
            if (part.isMimeType(TEXT_PLAIN)) {
                text += ((String) part.getContent());
            } else if (part.isMimeType(MULTIPART)) {
                Multipart mp = (Multipart) part.getContent();
                int count = mp.getCount();
                for (int n = 0; n < count; n++) {
                    text += (getText(mp.getBodyPart(n)));
                }
            } else if (part.isMimeType(ENCAPSULATED)) {
                text += getText((Part) part.getContent());
            }
            logger.info("email reads: " + text);
            return text;
        } catch (MessagingException e) {
            close();
            throw new MessagingException("Failed to read email body", e);
        } catch (IOException e) {
            close();
            throw new IOException("Failed to read email body", e);
        }
    }

    /**
     * Send a reply to the specified email
     *
     * @param response   the text to be used for the body of the reply
     * @param msg  the email to be responded to
     * @throws MessagingException   if rely cannot be sent
     */
    public void sendMessage(String response, Message msg) throws MessagingException {
        try {
            logger.info("response reads: " + response);
            Message replymsg = new MimeMessage(session);
            replymsg = (MimeMessage) msg.reply(false);
            replymsg.setFrom(new InternetAddress(from));
            replymsg.setText(response);
            replymsg.setReplyTo(msg.getReplyTo());

            Transport transport = session.getTransport(SMTP);
            try {
                transport.connect(user, password);
                transport.sendMessage(replymsg, replymsg.getAllRecipients());
            } finally {
                transport.close();
            }
        } catch (MessagingException e) {
            close();
            throw new MessagingException("Failed to rely to email", e);
        }
    }

    /**
     * Copy specified email from one folder to another
     * 
     * @param fromFolder   the folder the email is currently located in
     * @param toFolder   the folder the email is going to be moved to
     * @param msg   the email to be moved
     * @throws MessagingException   if email cannot be copied
     */
    public void copyEmail(FolderType fromFolder, FolderType toFolder, Message msg) throws MessagingException {
        try {
            Message[] messages = {msg};
            getFolder(fromFolder).copyMessages(messages, getFolder(toFolder));
        } catch (MessagingException e) {
            close();
            throw new MessagingException("Failed to copy email from " + fromFolder + " to " + toFolder, e);
        }
    }

    /**
     * Delete email from a folder
     * 
     * @param folder   the folder the email should be deleted from
     * @param msg   the email to be deleted
     * @throws MessagingException   if email cannot be deleted
     */
    public void deleteEmail(FolderType folder, Message msg) throws MessagingException {
        try {
            msg.setFlag(Flags.Flag.DELETED, true);
            getFolder(folder).expunge();
            getFolder(folder).close(false);
            getFolder(folder).open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            throw new MessagingException("Failed to delete email", e);
        }
    }

    /**
     * Close inbox folder and store
     * @throws MessagingException   if folders or store cannot be closed
     */
    public void close() throws MessagingException {
        try {
            inboxFolder.close(false);
            store.close();
        } catch (MessagingException e) {
            throw new MessagingException("Failed to close folders and store", e);
        }
    }
}
