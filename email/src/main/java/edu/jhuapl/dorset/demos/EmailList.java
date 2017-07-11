package edu.jhuapl.dorset.demos;

import java.util.ArrayDeque;

import javax.mail.Message;

public class EmailList {

    private ArrayDeque<Message> messages;

    public EmailList() {
        messages = new ArrayDeque<Message>();
    }
    
    public synchronized void putMessage(Message msg) {
        messages.addLast(msg);
    }
    
    public synchronized void removeMessage() {
        messages.removeFirst();
    }
    
    public boolean isEmpty() {
        return messages.isEmpty();
    }
}
