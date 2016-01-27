package edu.jhuapl.dorset.demos;

import edu.jhuapl.dorset.agent.Agent;
import edu.jhuapl.dorset.agent.AgentRequest;
import edu.jhuapl.dorset.agent.AgentResponse;

public class TestAgent implements Agent  {

    @Override
    public AgentResponse process(AgentRequest request) {
        return new AgentResponse("Here is a place holder response for a command line question.");
    }

}
