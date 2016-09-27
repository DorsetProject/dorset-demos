/*
 * Copyright 2016 The Johns Hopkins University Applied Physics Laboratory LLC
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
import java.util.ArrayList;
import java.util.Collection;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackPersona.SlackPresence;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;

/**
 * Slackbot uses a dorset application to respond to slack requests
 * The bot will listen to all slack channels it has been invited to.
 * It will respond any time a direct message is sent to it, or when its name is 
 * used in a message on another channel.  
 * 
 * The bot cannot join a slack channel on its own.  It has to be invited 
 * and it will automatically accept.  To invite a bot, just type it's name
 * in the new channel @botname and you will be asked to invite the bot. 
 */
public class SlackBot
{
    private SlackSession session = null;
    private Application application = null;

	private SlackMessagePostedListener messagePostedListener = null;
    
    
    /**
     * @param application Application for Dorset-Framework
     */
    public SlackBot(Application application) {
        super();
        this.application = application;
    }
    
    /**
     * Connects to Slack using the apiToken
     * @param apiToken
     * @throws IOException
     */
    public void connectToSlack(String apiToken) throws IOException {
        session = SlackSessionFactory.createWebSocketSlackSession(apiToken);
        session.connect();
    }


    /**
     * Registers a listener to reply back whenever someone asks the bot something directly
     * or mentions the bot in the message. Example: @botname
     * Bot will reply to a channel the bot has joined.
     *   
     */
    public void startListening()
    {
    	if(messagePostedListener == null) {
	        // first define the listener
	        messagePostedListener = new SlackMessagePostedListener()
	        {
	            @Override
	            public void onEvent(SlackMessagePosted event, SlackSession session)
	            {
	                SlackChannel channelOnWhichMessageWasPosted = event.getChannel();
	                String messageContent = event.getMessageContent();
	                SlackUser messageSender = event.getSender();
	
	                // We get notified even if it is our messages, avoid responding to them.
	                if (session.sessionPersona().getId().equals(event.getSender().getId())) {
	                    return;
	                }
	
	                // UserName is converted to <@userId>
	                String userId = "<@" + session.sessionPersona().getId() + ">";
	                
	                if(!(channelOnWhichMessageWasPosted.isDirect() || messageContent.contains(userId))) {
	                	return;
	                }
	                messageContent = messageContent.replaceAll(userId, "").trim();
	                Request request = new Request(messageContent);
	                Response response = application.process(request);
	                String prefix = channelOnWhichMessageWasPosted.isDirect() ? "" 
	                                : ("@" + messageSender.getUserName());
	                session.sendMessage(channelOnWhichMessageWasPosted, prefix + " " + response.getText());
	            }
	        };
	        session.addMessagePostedListener(messagePostedListener);
    	}

    }
    
    /**
     * returns the list of channels
     * @return Collection of channel names.
     */
    public Collection<String> getChannels() {
    	Collection<SlackChannel> channels = session.getChannels();
    	ArrayList<String> channelNames = new ArrayList<String>(channels.size());
    	for(SlackChannel ch : channels) {
    		if( ch.getName() != null) {
    			channelNames.add(ch.getName());
    		}
    	}
    	return channelNames;
    }

    /**
     * Stop Listening to for slack messages
     */
    public void stopListening()
    {
    	if(messagePostedListener != null) {
    		session.removeMessagePostedListener(messagePostedListener);
    		messagePostedListener = null;
    	}
    }
    
    /*
     * Gets the Dorset Application
     */
    public Application getApplication() {
		return application;
	}

    /**
     * Sets the Dorset Application that slack uses to reply with. 
     * @param application Application to be used.
     */
	public void setApplication(Application application) {
		this.application = application;
	}


}