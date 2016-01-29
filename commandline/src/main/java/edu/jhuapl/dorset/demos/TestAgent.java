package edu.jhuapl.dorset.demos;

import edu.jhuapl.dorset.agent.AgentBase;
import edu.jhuapl.dorset.agent.AgentRequest;
import edu.jhuapl.dorset.agent.AgentResponse;
import edu.jhuapl.dorset.agent.Description;

public class TestAgent extends AgentBase  {

    public TestAgent() {
        this.setName("test");
        this.setDescription(new Description(name, "Test agent", "Where am I?"));
    }

    @Override
    public AgentResponse process(AgentRequest request) {
        return new AgentResponse("Here is a place holder response for a command line question.");
    }

}
