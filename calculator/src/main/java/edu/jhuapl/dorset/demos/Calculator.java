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

import java.util.Scanner;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.Response;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.CalculatorAgent;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

public class Calculator {

    public static void main(String[] args) {

        Agent agent = new CalculatorAgent();
        Router router = new SingleAgentRouter(agent);
        Application app = new Application(router);

        System.out.println("Welcome to the Dorset calculator demo. "
                        + "Enter mathematical expressions or press 'q' to end this session.\n");

        String input = "";
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            input = in.nextLine();

            if (input.equals("q")) {
                break;
            }

            Request request = new Request(input);
            Response response = app.process(request);

            System.out.println(response.getText());
        }

        System.out.println("\nThank you for using the calculator.");
        in.close();
    }

}
