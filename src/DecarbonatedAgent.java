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
        public int queSeqIndex;
        
        public SuffixNode(String initSuffix, int initFailCount, int initTries) {
            this.suffix = initSuffix;
            this.failCount = initFailCount;
            this.tries = initTries;
            this.queueSeq = null;
        }//ctor

        public String toString() {
            String output = suffix + ":";
            if (queueSeq != null) {
                output += queueSeq;
            }
            
            return output + "," + failCount + "/" + tries;
        }

    }//class SuffixNode    

    /*---=== CONSTANTS ===---*/

    //minimum tries before a suffix node is expanded
    public static final int MIN_TRIES = 15;

    //how much weight to apply to the g-value of a suffix node
    //(should be in the range (0.0..1.0)
    public static final double GWEIGHT = 0.2;

    /*---==== MEMBER VARIABLES ===---*/

    //TODO
    int lastPermutationIndex = 0;
    
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

                //TODO: REMOVE DEBUG
                boolean firsttry = true;
                {
                    String s = "";
                    for(int i = 0; i < (15-nextSeqToTry.length()); ++i) {
                        s += ".";
                    }
                    System.out.print("trying: " + s + nextSeqToTry);
                    
                   
                }
                while(tryPath(path))
                {
                    if (Sucesses >= NUM_GOALS) { 
                        return;  //if we reach this, path is likely universal sequence
                    }

                    //Update n to reflect the success
                    activeNode.tries++;
                    //TODO: REMOVE DEBUG
                    {
                        String s = "";
                        for(int i = 0; i < (15-nextSeqToTry.length()); ++i) {
                            s += ".";
                        }
                        
                        if(firsttry){
                        	firsttry = false;
                        }else{
                            System.out.print("trying: " + s + nextSeqToTry + "\t\t");
                        }
                    }
                    
                }

                //Update node to reflect failure
                activeNode.tries++;
                activeNode.failCount++;
                if(!firsttry){
                	String s = "";
                	for(int i = 0; i < (15-nextSeqToTry.length()); ++i) {
                		s += ".";
                	}
                	System.out.print("trying: " + s + nextSeqToTry);
                }
                System.out.println("");
                
            }//if

            //Save this sequence until the active node has a matching suffix
            else {
                for(SuffixNode node : fringe) {
                    if (node == activeNode) continue;
                    if (nextSeqToTry.endsWith(node.suffix) && node.queueSeq == null) {
                        node.queueSeq = nextSeqToTry;
                        node.queSeqIndex = lastPermutationIndex;
                        break;
                    }
                }
            }

            //See if the active node has enough attempts that it can be expanded
            if (activeNode.tries >= this.MIN_TRIES) {
            	expandNode();
            	
            	double theBEASLIESTCombo = 17976931348623157.0;
            	int bestNodeIndex = 0;
            	
            	for (SuffixNode node: fringe){
            		double gVal = (double)node.suffix.length() * this.GWEIGHT;
            		double failRate = ((double)node.failCount) / ((double)node.tries);
            		if((failRate + gVal) < theBEASLIESTCombo){
            			theBEASLIESTCombo = failRate + gVal;
            			bestNodeIndex = fringe.indexOf(node);
            		}
            	}
            	activeNode = fringe.get(bestNodeIndex);

                //TODO: REMOVE DEBUG
                System.out.println("New active node: " + activeNode);
            	
            	if (activeNode.queueSeq != null) {
            		this.nextSeqToTry = activeNode.queueSeq;
            		lastPermutationIndex = activeNode.queSeqIndex;
            	}else{
            		this.nextSeqToTry = nextPermutation();
            	}
            }else{
            	this.nextSeqToTry = nextPermutation();
            }
            


        }//while
    }//exploreEnvironment
    
    /**
     * nextSuffix
     * 
     * finds the next suffix to try
     */
    
    public String nextPermutation() {
        lastPermutationIndex++;
        int index = lastPermutationIndex;
        if (index <= 0) 
            throw new IndexOutOfBoundsException("index must be a positive number");
        if (index <= alphabet.length)
            return Character.toString(alphabet[index - 1]);
        StringBuffer sb = new StringBuffer();
        while (index > 0) {
            sb.insert(0, alphabet[--index % alphabet.length]);
            index /= alphabet.length;
        }
        return sb.toString();
    }//nextPermutation
    
    
    /**
     * expandNode
     *
     * Expands the active node
     *
     * creates child nodes and adds them to the fringe. Removes itself from the fringe.
     */
    public void expandNode(){
        //TODO: REMOVE DEBUG
        System.out.print("Spliting " + activeNode + " into: ");
        int[] tryCountVals = new int[2];
            
    	for (int i = 0; i < alphabet.length; i++){
    		tryCountVals = getTryCount(alphabet[i] + activeNode.suffix);
            SuffixNode newNode = new SuffixNode(alphabet[i] + activeNode.suffix, tryCountVals[1] , tryCountVals[0]);

            //TODO: REMOVE DEBUG
            System.out.print("" + newNode + ",");
            
    		fringe.add(newNode);
    	}

        fringe.remove(activeNode);
        activeNode = null;
        
        //TODO: REMOVE DEBUG
        System.out.println();
            
    }
    
    /**
     * getTryCount
     * 
     * scans memory and finds counts the number of times a sequence has been tried and how many times it has failed to reach the goal
     * 
     */
    public int[] getTryCount(String suffix){
    	
    	int numTries = 0; //number of times sequence was tried
    	int numFails = 0; //number of times sequence wasn't tried and failed
    	int[] returnVal = new int[2];
    	
    	for (int i = 0; i < episodicMemory.size(); i++){ //for each episode in memory
    			
			boolean suffixEqual = true; //if we have found a usage of the suffix
			
			//check the next letters and see if we have found a usage of the suffix
			for(int j = 0; j < suffix.length(); j ++){ //for each char in the suffix
				
				//if we have not run past the length of the memory or if the memory's next command does not equal the suffix's next command
				if (i+j >= episodicMemory.size() || episodicMemory.get(i + j).command != suffix.charAt(j)){
    				suffixEqual = false;
    				break;
    			}
			}
			
			//tally tries and fails
			if (suffixEqual){ //if we found a usage of the suffix
    			for(int j = 0; j < suffix.length(); j ++){ //for each char in the suffix
        			
    				if (i+j < episodicMemory.size()){ //safety check to make sure we are inbounds
	        			if (episodicMemory.get(i + j).sensorValue == GOAL) { //if reached goal before finishing sequence
	        				numTries ++;
	        				break;
	        			}
	        			
	        			if (j == suffix.length() - 1){  //if reached end of sequence
	        				numTries ++;
	        				if (episodicMemory.get(i + j).sensorValue != GOAL) { //if didn't reach goal by end of sequence
	        					numFails ++;
	        				}
	        				break;
	        			}
	        		}
    				
    			}
			}
		}
    	
    	//return values
    	returnVal[0] = numTries;
    	returnVal[1] = numFails;
    	return returnVal; //return
    }


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
                gilligan.env.printStateMachineGraph();
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
        System.out.println("avg solution length: " + tryAvgWithShortPath(100));
        tryGenLearningCurves();
	}

}//class DecarbonatedAgent
