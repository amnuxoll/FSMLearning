/**
 * NSMAgentTest
 *
 * a set of unit tests for NSMAgent.java.  I've started writing these as I add
 * new methods or fix bugs in old ones.  So many tests should be added.
 *
 * Q: "Hey, Nuxoll why aren't you using JUnit?!"
 * A: "Because setting up the CLASSPATH for JUnit is one more hurdle a new
 *     user of this code will have to leap and there is little to gain for it."
 *
 * @author Andrew Nuxoll
 */

public class NSMAgentTest {

    private NSMAgent agent = null;
 
    /** report a failure and exit */
    public void fail(String s) {
        System.out.println("\nFAIL: " + s);
        System.exit(-1);
    }

    /** test for String equality and fail if not equal */
    public void testEqualString(String errMsg, String ret, String expected) {
        if (! ret.equals(expected))
        {
            fail(errMsg + "  Method returned " + ret + " should have returned " + expected + ".");
        }
    }        

    /** Create an agent with a fixed set of episodes */
    public void setupAgent() {
        System.out.print("Starting setupAgent...");

        //Note:  the '|' char indicates the next letter reaches the goal
        // (not the previous one)
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
    
    /* ====================================================================== */
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

    /* ====================================================================== */
    public void checkSeqTest() {
        setupAgent();

        System.out.print("Starting checkSeqTest...");

        //test for "NOT FOUND"
        String ret = agent.checkSeq("cbcbcbccb", "ccb");
        testEqualString("Error should not match for sequence: cbcbcbccb.", ret, "NOT FOUND");

        //test for "FAIL"
        ret = agent.checkSeq("aaaaaab", "aab");
        testEqualString("Error finding failed sequence: aaaaaab", ret, "FAIL");

        //test for success
        ret = agent.checkSeq("ccbcccaababacb", "acb");
        testEqualString("Error finding successful sequence: ccbcccaababacb.", ret, "ccbcccaababac");
        
        //edge case:  one letter sequence that's first letter in epmem
        ret = agent.checkSeq("c", "c");
        testEqualString("Error finding failed sequence: c.", ret, "FAIL");

        //edge case:  sequence at end of epmem
        ret = agent.checkSeq("ccbcbb", "cbb");
        testEqualString("Error finding failed sequence: ccbcbb.", ret, "FAIL");

        //test for "NOT FOUND" due to goal interrupt
        // i.e., caba|baaaaaa in the epmem can't match cababaaaaaa in the path
        ret = agent.checkSeq("cababaaaaaa", "aaa");
        testEqualString("Error should not match for sequence: cababaaaaaa.", ret, "NOT FOUND");

        //test for success with perfect match
        ret = agent.checkSeq("cabbaacccbbaa", "cbbaa");
        testEqualString("Error finding successful perfect match sequence: cabbaacccbbaa.", ret, "cabbaacccbbaa");

        System.out.println("Success.");
        
    }//checkSeqTest


    /* ====================================================================== */
    public static void main(String[] args) {
        NSMAgentTest nsmTest = new NSMAgentTest();
        nsmTest.populateNHoodsTest();
        nsmTest.checkSeqTest();
    }//main
}//class NSMAgentTest
