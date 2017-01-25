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
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.EchoAgent;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

public class SlackBotDemo {

    /**
     * Get Slack API Token
     * 
     * @return the API token
     * @throws IOException IOException while getting API token
     */
    public String getSlackApiToken() throws IOException {
        Properties properties = new Properties();
        try (InputStream in = SlackBot.class.getResourceAsStream("/slackbot.properties")) {
            properties.load(in);
        }
        return properties.getProperty("slackBotToken");
    }

    /**
     * Runs the program.
     * 
     * @param args  A string array containing the command line arguments
     * @throws IOException IOException 
     */
    public static void main(String[] args) throws IOException {
        Agent agent = new EchoAgent();
        Router router = new SingleAgentRouter(agent);
        Application app = new Application(router);

        SlackBotDemo demo = new SlackBotDemo();
        System.out.println("SlackBot Echo Test.");
        System.out.println("");
        String apiToken = demo.getSlackApiToken();

        SlackBot slackBot = new SlackBot(app);
        slackBot.connectToSlack(apiToken);
        Collection<String> channels = slackBot.getChannels();
        System.out.println("Channels listing:");
        for (String ch : channels) {
            System.out.println(ch);
        }
        System.out.println();

        slackBot.startListening();
        System.out.println("Listening for messages...");
    }

}
