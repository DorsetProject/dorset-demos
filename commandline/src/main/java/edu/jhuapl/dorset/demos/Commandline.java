/**
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

import java.util.Properties;
import java.util.Scanner;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;
import edu.jhuapl.dorset.agent.AgentRegistry;
import edu.jhuapl.dorset.routing.FixedAgentRouter;
import edu.jhuapl.dorset.routing.Router;


public class Commandline {
	
    public static void main(String[] args) {
        
        AgentRegistry registry = new AgentRegistry();
        registry.register("test", new TestAgent(), new Properties());
        Router router = new FixedAgentRouter("test");
        Application app = new Application(registry, router);
        
        System.out.println("Welcome to the Dorset command line demo! "
                + "Ask any question or press 'q' if you would like to end the session.");
        
        String input = ""; 
        Scanner in = new Scanner(System.in);
        
        while (true) {
            System.out.println("\nWhat would you like to ask?");
            input = in.nextLine();
            
            if (input.equals("q")) {
                break;
            }

            Request request = new Request(input);
            Response response = app.process(request);
            
            System.out.println(response.getText());
           
        }
        
        System.out.println("\nThank you for your questions! Come back soon!");
        in.close();

    }
  
}
