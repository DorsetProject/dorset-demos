package edu.jhuapl.dorset.demos;

import edu.jhuapl.dorset.agent.AbstractAgent;
import edu.jhuapl.dorset.agent.AgentRequest;
import edu.jhuapl.dorset.agent.AgentResponse;
import edu.jhuapl.dorset.agent.Description;

public class TestAgent extends AbstractAgent  {

    public TestAgent() {
        this.setDescription(new Description("test", "Test agent", "Where am I?"));
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        return new AgentResponse("Here is a place holder response for a command line question.");
    }

}
