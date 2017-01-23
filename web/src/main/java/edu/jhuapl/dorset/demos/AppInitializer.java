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

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import edu.jhuapl.dorset.Application;
import edu.jhuapl.dorset.agents.Agent;
import edu.jhuapl.dorset.agents.DateTimeAgent;
import edu.jhuapl.dorset.agents.DuckDuckGoAgent;
import edu.jhuapl.dorset.config.MultiValuedMap;
import edu.jhuapl.dorset.http.apache.ApacheHttpClient;
import edu.jhuapl.dorset.routing.ChainedRouter;
import edu.jhuapl.dorset.routing.KeywordRouter;
import edu.jhuapl.dorset.routing.Router;
import edu.jhuapl.dorset.routing.RouterAgentConfig;
import edu.jhuapl.dorset.routing.SingleAgentRouter;

/**
 * Initialize resources for the Dorset api
 * 
 * This uses Jersey's default dependency injection framework.
 */
public class AppInitializer extends ResourceConfig {
    private final Application app;

    /**
     * Create the app and bind it for injection
     */
    public AppInitializer() {

        app = new Application(initializeRouter());

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(app).to(Application.class);
            }
        });

        // uncomment for logging requests and responses at the INFO level
        // registerInstances(new
        // LoggingFilter(Logger.getLogger("org.glassfish.jersey"), true));
    }

    private Router initializeRouter() {
      
        MultiValuedMap timeAgentParams = new MultiValuedMap();
        timeAgentParams.addString(KeywordRouter.KEYWORDS, "time");
        timeAgentParams.addString(KeywordRouter.KEYWORDS, "date");
        timeAgentParams.addString(KeywordRouter.KEYWORDS, "day");
        RouterAgentConfig kwConfig = RouterAgentConfig.create();
        kwConfig.add(new DateTimeAgent(), timeAgentParams);
        Router kwRouter = new KeywordRouter(kwConfig);

        Agent wikiAgent = new DuckDuckGoAgent(new ApacheHttpClient());
        Router wikiRouter = new SingleAgentRouter(wikiAgent);

        Router mainRouter = new ChainedRouter(kwRouter, wikiRouter);
        return mainRouter;
    }
}