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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.agent.AgentRegistry;
import edu.jhuapl.dorset.demos.UltimateAgent;
import edu.jhuapl.dorset.routing.FixedAgentRouter;
import edu.jhuapl.dorset.routing.Router;

public class AppInitializer implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        AgentRegistry registry = new AgentRegistry();
        registry.register("ultimate", new UltimateAgent(), new Properties());
        Router router = new FixedAgentRouter("ultimate");
        Application.setApplication(new Application(registry, router));
    }

}
