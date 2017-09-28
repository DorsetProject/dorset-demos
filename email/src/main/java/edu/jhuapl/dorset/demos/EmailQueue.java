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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.mail.Message;

public class EmailQueue {

    private BlockingQueue<Message> messages;

    /**
     * Create an EmailQueue
     */
    public EmailQueue() {
        messages = new ArrayBlockingQueue<Message>(20);
    }

    /**
     * Put a message into the queue
     *
     * @param msg   the message to be put into the queue
     */
    public void putMessage(Message msg) {
        messages.add(msg);
    }

    /**
     * Take a message off the queue
     * 
     */
    public void removeMessageIfAny() {
        messages.remove();
    }

    /**
     * Return whether the queue is empty or not
     *
     * @return whether the queue is empty or not
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }
}
