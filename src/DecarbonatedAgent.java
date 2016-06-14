import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class DecarbonatedAgent extends Agent {

    /**
     * class SuffixNode
     *
     * a possible suffix of the universal sequence and some meta data about it.
     */
    public class SuffixNode {
        public String suffix;
        public int failCount;   //number of failed sequences with this suffix
                                //that we've tried
        public int tries;       //number of times this suffix has been tried
        public String queueSeq; //first sequence with this suffix that was skipped
        
        public SuffixNode(String initSuffix, int initFailCount, int initTries) {
            this.suffix = initSuffix;
            this.failCount = initFailCount;
            this.tries = initTries;
            this.queueSeq = null;
        }//ctor

    }//class SuffixNode    

    /*---=== CONSTANTS ===---*/

    //minimum tries before a suffix node is expanded
    public static final int MIN_TRIES = 5;

    //how much weight to apply to the g-value of a suffix node
    //(should be in the range (0.0..1.0)
    public static final double GWEIGHT = 0.2;

    /*---==== MEMBER VARIABLES ===---*/
    
    /** the list of SuffixNodes that currently candidates for the universal
       sequence */
    ArrayList<SuffixNode> fringe = new ArrayList<SuffixNode>();
    
    /** only one node at a time can be active */
    SuffixNode activeNode = null;
    
    /** the next sequence to consider testing */
    String nextSeqToTry = "a";  //'a' is always safe because of how
                                //StateMachineEnvironment creates FSMs
    

	/**
	 * In addition to initializing instance variables, the ctor generates all
	 * possible SUS actions (up to a maximum length) for use during execution. 
	 * 
	 */
	public DecarbonatedAgent() {
        //Create the initial suffix node
        SuffixNode initNode = new SuffixNode("", 0, 0);
        fringe.add(initNode);
        this.activeNode = initNode;
        
	}//StateMachineAgent ctor

    /**
     * exploreEnvironment
     *
     * Main Driver Method of Program
     *
     * Sets the agent free into the wild allowing him to roam free. This means
     * that he'll use the different scores to decide how to navigate the
     * environment giving him full sentient capabilities...
     */
    @Override
    public void exploreEnvironment() {
        while (episodicMemory.size() < MAX_EPISODES && Sucesses <= NUM_GOALS) { 

            //Is the next sequence worth trying?
            if (nextSeqToTry.endsWith(activeNode.suffix)) {

                //Try this sequence until it fails
                Path path = stringToPath(nextSeqToTry);
                while(tryPath(path))
                {
                    if (Sucesses >= NUM_GOALS) { 
                        return;  //if we reach this, path is likely universal sequence
                    }

                    //Update n to reflect the success
                    activeNode.tries++;
                }

                //Update node to reflect failure
                activeNode.tries++;
                activeNode.failCount++;
                
            }//if

            //Save this sequence until the active node has a matching suffix
            else {
                for(SuffixNode node : fringe) {
                    if (node == activeNode) continue;
                    if (nextSeqToTry.endsWith(node.suffix) && node.queueSeq == null) {
                        node.queueSeq = nextSeqToTry;
                        break;
                    }
                }
            }

            //See if the active node has enough attempts that it can be expanded
            //TBD


        }//while
    }//exploreEnvironment


    /**
     * ************************************************************************************
     * METHODS FOR NAVIGATION
     * ************************************************************************************
     */

	

   

	

	/**
	 * recordLearningCurve
	 * 
	 * examine's the agents memory and prints out how many steps the agent took
	 * to reach the goal each time
	 * 
     * @param csv         an open file to write to
	 */
	protected void recordLearningCurve(FileWriter csv) {
        try {
            csv.append(episodicMemory.size() + ",");
            csv.flush();
            int prevGoalPoint = 0; //which episode I last reached the goal at
            for(int i = 0; i < episodicMemory.size(); ++i) {
                Episode ep = episodicMemory.get(i);
                if (ep.sensorValue == GOAL) {
                    csv.append(i - prevGoalPoint + ",");
                    csv.flush();
                    prevGoalPoint = i;
                }//if
            }//for

            csv.append("\n");
            csv.flush();
        }
        catch (IOException e) {
            System.out.println("recordLearningCurve: Could not write to given csv file.");
            System.exit(-1);
        }
                
	}//recordLearningCurve

	/**
	 * tryGenLearningCurves
     *
     * creates a .csv file containing learning curves of several successive agents
	 */
    public static void tryGenLearningCurves()
    {
        try {

            FileWriter csv = new FileWriter(OUTPUT_FILE);
            for(int i = 0; i < NUM_MACHINES; ++i) {
                System.out.println("Starting on Machine" + i);
                DecarbonatedAgent gilligan = new DecarbonatedAgent();
                gilligan.exploreEnvironment();
                gilligan.recordLearningCurve(csv);
                System.out.println("Done with machine" + i + "\n");
            }
            recordAverage(csv);
            csv.close();
        }
        catch (IOException e) {
            System.out.println("tryAllCombos: Could not create file, what a noob...");
            System.exit(-1);
        }
    }//tryGenLearningCurves

	/**
	 * tryAvgWithShortPath
     *
     * calculates the average near-optimal number of steps to goal given a model
     * of FSM but no knowledge of the environment
     *
     * @param numTimes  number of FSMs to use to calculate the average
	 */
    public static int tryAvgWithShortPath(int numTimes)
    {
        int sumOfAvgSteps = 0;
        for(int i = 0; i < numTimes; ++i)
        {
            StateMachineEnvironment env = new StateMachineEnvironment();
            String path = env.shortestBlindPathToGoal();
            sumOfAvgSteps += env.avgStepsToGoalWithPath(path);
        }

        return sumOfAvgSteps / numTimes;
        
    }//tryAvgWithShortPath

    
    
	/**
	 * main
     *
     * helper methods (above) have been defined to do various things here.
     * Modify this method to call the one(s) you want.
	 */
	public static void main(String [ ] args) {
        tryGenLearningCurves();
        System.out.println(tryAvgWithShortPath(100));
	}

}//class DecarbonatedAgent
