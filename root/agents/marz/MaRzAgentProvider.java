package agents.marz;

import framework.IAgent;
import framework.IAgentProvider;

public class MaRzAgentProvider implements IAgentProvider {
    @Override
    public IAgent getAgent() {
        return new MaRzAgent();
    }
}
