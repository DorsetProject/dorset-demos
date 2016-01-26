package edu.jhuapl.dorset;

import edu.jhuapl.dorset.agent.Agent;
import edu.jhuapl.dorset.agent.AgentRequest;
import edu.jhuapl.dorset.agent.AgentResponse;

public class UltimateAgent implements Agent {

    @Override
    public AgentResponse process(AgentRequest request) {
        return new AgentResponse("42");
    }

}
