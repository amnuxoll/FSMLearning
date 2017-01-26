/**
 * NSMAgentTest
 *
 * a set of unit tests for NSMAgent.java
 */

public class NSMAgentTest {

    private NSMAgent agent = null;

    //report a failure and exit
    public void fail(String s) {
        System.out.println("\nFAIL: " + s);
        System.exit(-1);
    }

    //Create an agent with a fixed set of episodes
    public void setupAgent() {
        System.out.print("Starting setupAgent...");

        String epmem = "cbacbbacbacabbcaba|baaaaaabaccccaaaaaaabaaccabaaaaaaccaaaaabbcbaaaaaabcbaaaaaccacaacbaaccbaaaabcaaacabbaacccbba|aacaaaaaacaacaaaccbbb|aacbccabcacaacaaba|ba|cbabccaaabacacaabaaabaaaacabcabcccacaaacaccbba|accacbcabb|aabc|babcabcaabaaabaccbbaaba|bcaaba|aabbbaabacaabccbcccaababa|cbaaaba|acacacabbcbabcaaabaaacabcbbaaaabbaaacbabaabc|accbcbb ";
        this.agent = new NSMAgent();
        for(int i = 0; i < epmem.length()-1; ++i) {
            char cmd = epmem.charAt(i);
            int sensor = Agent.NO_TRANSITION;
            if (cmd == '|') {
                i++;
                cmd = epmem.charAt(i);
                sensor = Agent.GOAL;
            }
            agent.episodicMemory.add(new NSMAgent.QEpisode(cmd, sensor));
        }//for

        //Verify the result
        NSMAgent.QEpisode qep = (NSMAgent.QEpisode)agent.episodicMemory.get(17);
        if (qep.command != 'a') fail("Wrong cmd for QEp at index 17: " + qep.command);
        if (qep.sensorValue != Agent.NO_TRANSITION) fail("Wrong sensor for QEp at index 17: " + qep.sensorValue);
        qep = (NSMAgent.QEpisode)agent.episodicMemory.get(18);
        if (qep.command != 'b') fail("Wrong cmd for QEp at index 18: " + qep.command);
        if (qep.sensorValue != Agent.GOAL) fail("Wrong sensor for QEp at index 17: " + qep.sensorValue);

        System.out.println("Success.");

    }//setupAgent
    
    public void populateNHoodsTest() {
        setupAgent();

        System.out.print("Starting populateNHoodsTest...");

        agent.populateNHoods();

        for(int i = 0; i < agent.alphabet.length; ++i) {
            NSMAgent.NHood nhood = agent.nhoods[i];
            for(NSMAgent.NBor nbor : nhood.nbors) {
                for(int j = nbor.end-1; j > nbor.begin; --j) {
                    Episode ep1 = agent.episodicMemory.get(j);
                    int index = agent.episodicMemory.size() - 1 - (nbor.end - j);
                    Episode ep2 = agent.episodicMemory.get(index);
                    if (! ep1.toString().equals(ep2.toString())) {
                        System.out.println(agent.episodicMemory.get(j-1).command);
                        System.out.println(agent.episodicMemory.get(j).command);
                        System.out.println(agent.episodicMemory.get(j+1).command);
                        System.out.println(agent.episodicMemory.get(index-1).command);
                        System.out.println(agent.episodicMemory.get(index).command);
                        System.out.println(agent.episodicMemory.get(index+1).command);
                        
                        fail("" + ep1 + " at index " + j + " should match " + ep2 + " at index " + index);
                    }
                }
            }
        }
        
        System.out.println("Success.");
        
    }//populateNHoods


    public static void main(String[] args) {
        NSMAgentTest nsmTest = new NSMAgentTest();
        nsmTest.populateNHoodsTest();
    }//main
}//class NSMAgentTest
