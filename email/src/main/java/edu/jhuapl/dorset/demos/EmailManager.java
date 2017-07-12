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

import edu.jhuapl.dorset.nlp.Tokenizer;
import edu.jhuapl.dorset.nlp.WhiteSpaceTokenizer;

public class EmailManager {

    private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String MAIL_STORE_TYPE_KEY = "mailStoreType";
    private static final String HOST_KEY = "host";
    private static final String FROM_KEY = "from";

    private String username;
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
    * EmailManager Constructor.
    *
    * The close method must be called on every EmailManager object before exiting an application
    *
    * @param config  Configuration object that stores mail server information, username, and password
    * @throws MessagingException   if connection cannot be established
    */
    public EmailManager(Config config) throws MessagingException {
        
        username = config.getString(USERNAME_KEY);
        password = config.getString(PASSWORD_KEY);
        mailStoreType = config.getString(MAIL_STORE_TYPE_KEY);
        host = config.getString(HOST_KEY); 
        from = config.getString(FROM_KEY);
        Properties prop = extractProperties(config);
        session = Session.getDefaultInstance(prop);
        store = session.getStore(mailStoreType);
        try {
            store.connect(host, username, password);
        } catch (MessagingException e) {
            throw new MessagingException("Failed to set up imap connection. Check your network connection and account/server configurations.", e);
        }
        try {
            initFolders();
            inboxFolder.open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            store.close();
            throw new MessagingException("Failed to initialize and open folder");
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
     *
     * @throws MessagingException   if folders cannot be initialized
     */
    private void initFolders() throws MessagingException {
        if (!store.getFolder(FolderType.ERROR.getValue()).exists()) {
            store.getFolder(FolderType.ERROR.getValue()).create(Folder.HOLDS_MESSAGES);
        }
        if (!store.getFolder(FolderType.COMPLETE.getValue()).exists()) {
            store.getFolder(FolderType.COMPLETE.getValue()).create(Folder.HOLDS_MESSAGES);
        }
        inboxFolder = store.getFolder(FolderType.INBOX.getValue());
        errorFolder = store.getFolder(FolderType.ERROR.getValue());
        completeFolder = store.getFolder(FolderType.COMPLETE.getValue());
    }

    /**
     * Get a folder
     *
     * @param folder   the folder to be returned
     * @return the folder to be accessed
     */
    private Folder getFolder(FolderType folder) {
        switch (folder) {
            case ERROR:
                return errorFolder;
            case COMPLETE:
                return completeFolder;
            case INBOX:
            default:
                return inboxFolder;
        }
    }

    /**
     * Get the number of emails in a folder
     *
     * @param folder   the folder to look in
     * @return the number of emails left in the folder
     * @throws MessagingException   if folder contents cannot be accessed
     */
    public synchronized int getCount(FolderType folder) throws MessagingException {
        try {
            return getFolder(folder).getMessageCount();
        } catch (MessagingException e) {
            throw new MessagingException("Failed to access " + folder + " folder contents", e);
        }
    }

    /**
     * Get a seen email from a folder
     *
     * @param folder   the folder to retrieve an email from
     * @return the email
     * @throws MessagingException   if email cannot be retrieved
     */
    public synchronized Message getSeenMessage(FolderType folder) throws MessagingException {
        try {
            return getFolder(folder).getMessage(1);
        } catch (MessagingException e) {
            throw new MessagingException("Failed to retrieve email from " + folder + " folder", e);
        }  
    }

    /**
     * Get an unseen email from a folder
     *
     * @param folder   the folder to retrieve an email from
     * @return the email
     * @throws MessagingException   if email cannot be retrieved
     * @throws IndexOutOfBoundsException   if there are no unseen emails
     */
    public synchronized Message getUnseenMessage(FolderType folder) throws IndexOutOfBoundsException, MessagingException {
        try {    
            int count = 1;
            Message msg = getFolder(folder).getMessage(count);
            while (msg.isSet(Flags.Flag.SEEN)) {
                count++;
                if (count > getFolder(folder).getMessageCount()) {
                    throw new IndexOutOfBoundsException("There were no unseen messages in " + folder + " folder");
                }
                msg = getFolder(folder).getMessage(count);
            }
            return msg;
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("There were no unseen messages in " + folder + " folder");
        } catch (MessagingException e) {
            throw new MessagingException("Failed to retrieve email from " + folder + " folder", e);
        } 
    }

    /**
     * Mark an email as seen
     *
     * @param msg  the email to be marked seen
     * @throws MessagingException   if email cannot be properly marked
     */
    public synchronized void markSeen(Message msg) throws MessagingException {
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
     * @return the text of an email
     * @throws MessagingException   if email cannot be accessed
     */
    public String readEmail(Message msg) throws MessagingException {
        Tokenizer tokenizer = new WhiteSpaceTokenizer();
        String subject = msg.getSubject().toUpperCase();
        String[] subjectTokenized = tokenizer.tokenize(subject);

        if (subject.contains("RE: ") || subjectTokenized.length <= 1) {
            return getBodyText(msg);
        } else {
            return subject;
        }
    }

    /**
     * Get the text of an email body
     *
     * @param part   the email body to be retrieved
     * @return text  the text of the email
     * @throws MessagingException   if email body cannot be retrieved
     */
    private String getBodyText(Part part) throws MessagingException {
        try {
            String text = "";
            if (part.isMimeType(TEXT_PLAIN)) {
                text += ((String) part.getContent());
            } else if (part.isMimeType(MULTIPART)) {
                Multipart mp = (Multipart) part.getContent();
                int count = mp.getCount();
                for (int n = 0; n < count; n++) {
                    text += (getBodyText(mp.getBodyPart(n)));
                }
            } else if (part.isMimeType(ENCAPSULATED)) {
                text += getBodyText((Part) part.getContent());
            }
            logger.info("email reads: " + text);
            return text;
        } catch (MessagingException | IOException e) {
            throw new MessagingException("Failed to read email body", e);
        }
    }

    /**
     * Send a reply to an email
     *
     * @param response   the text to be used for the body of the reply
     * @param msg  the email to be responded to
     * @throws MessagingException   if reply cannot be sent
     */
    public void sendMessage(String response, Message msg) throws MessagingException {
        try {
            logger.info("response reads: " + response);
            Message replyMsg = (MimeMessage) msg.reply(false);
            replyMsg.setFrom(new InternetAddress(from));
            replyMsg.setText(response);
            replyMsg.setReplyTo(msg.getReplyTo());

            if (!msg.getSubject().toUpperCase().contains("RE: ")) {
                replyMsg.setSubject("Re: " + replyMsg.getSubject());
            } else {
                replyMsg.setSubject(msg.getSubject());
            }

            Transport transport = session.getTransport(SMTP);
            try {
                transport.connect(username, password);
                transport.sendMessage(replyMsg, replyMsg.getAllRecipients());
            } finally {
                transport.close();
            }
        } catch (MessagingException e) {
            throw new MessagingException("Failed to reply to email", e);
        }
    }

    /**
     * Copy an email from one folder to another
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
            throw new MessagingException("Failed to copy email from " + fromFolder + " to " + toFolder, e);
        }
    }

    /**
     * Delete an email from a folder
     *
     * @param folder   the folder the email should be deleted from
     * @param msg   the email to be deleted
     */
    public void deleteEmail(FolderType folder, Message msg) {
        try {
            msg.setFlag(Flags.Flag.DELETED, true);
            getFolder(folder).expunge();
            getFolder(folder).close(false);
            getFolder(folder).open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            logger.error("Failed to delete email from " + folder + " folder", e);
        }
    }

    /**
     * Close the inbox folder and store
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
