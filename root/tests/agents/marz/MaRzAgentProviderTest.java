package agents.marz;

import framework.IAgent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaRzAgentProviderTest {

    // getAgent Tests
    @Test
    public void getAgentReturnsMaRzAgents()
    {
        MaRzAgentProvider provider = new MaRzAgentProvider();
        IAgent agent = provider.getAgent();
        assertTrue(agent instanceof MaRzAgent);
    }
}
