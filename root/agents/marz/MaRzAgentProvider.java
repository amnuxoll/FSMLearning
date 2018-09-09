package agents.marz;

import framework.IAgent;
import framework.IAgentProvider;

/**
 * MaRzAgentProvider
 *
 * Builds instances of a MaRz agent.
 *
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public class MaRzAgentProvider implements IAgentProvider {
    /**
     * Gets a MaRz agent.
     * @return the {@link MaRzAgent}.
     */
    @Override
    public IAgent getAgent() {
        return new MaRzAgent();
    }
}
