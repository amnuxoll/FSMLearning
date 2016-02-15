import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class StateMachineAgent extends Agent{
    
    /**
     * The SUS is the shortest unique sequence that has not been performed yet.
     * A score will be made to evaluate if it will be chosen as the next path
     * to execute. The idea being that it will slowly help find the best string
     * of cmds to execute in tricky situations
     */
    //variables related to the SUS
    protected double susScore = 0;
    private static final int MAX_SEQUENCE_SIZE = 10; //just picked 7 as a guess
    private ArrayList<ArrayList<String>> sequencesNotPerformed;
    protected static int SUS_CONSTANT = 96; //will become final after testing to find values

    /**
     * The LMS (llama) is the longest matching sequence that matching with what the agent
     * has just executed. A score will be built related to the length of the sequence
     * and the amount of moves to execute to "get to the goal." The idea behind this
     * is that eventually there will be long matching strings that occur frequently
     * and regularly get the agent to the goal... cross your fingers
     */
    //variables related to the LMS
    private double lmsScore;
    private static int LMS_CONSTANT = 10; //will become final after testing
    public static final int MATCHED_INDEX = 0;
    public static final int MATCHED_LENGTH = 1;

    protected static int RANDOM_SCORE = 16; //will become final after testing

    //chance that a duplicate cmd is allowed if a random action is necessary


	//specify path to take for testing if boolean is true
	ArrayList<Character> testPath = new ArrayList<Character>(Arrays.asList('b', 'b'));
	boolean useDefinedPath = false;

	/**
	 * In addition to initializing instance variables, the ctor generates all
	 * possible SUS actions (up to a maximum length) for use during execution. 
	 * 
	 */
	public StateMachineAgent() {
        
        informationColumns = 2;
            
        env = new StateMachineEnvironment();
		alphabet = env.getAlphabet();
		episodicMemory = new ArrayList<Episode>();

		//prime the epmem with a first episode that is empty
		episodicMemory.add(new Episode(' ', NO_TRANSITION));//the space cmd means unknown cmd for first memory

        //build the permutations of all sequences (up to max SUS len) 
        sequencesNotPerformed = new ArrayList<ArrayList<String>>();
        sequencesNotPerformed.add(0, null);//since a path of size 0 should be skipped (might not be necessary)
        for(int lengthSize=1; lengthSize<=MAX_SEQUENCE_SIZE; lengthSize++){
            ArrayList<String> tempList = new ArrayList<String>();
            fillPermutations(alphabet, lengthSize, tempList);
            sequencesNotPerformed.add(lengthSize, tempList);
        }
	}//StateMachineAgent ctor
        
    /** accessor */
	protected StateMachineEnvironment getEnv() {
		return env;
	}

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
            //Find sus and lms scores
            determineSusScore();
            String currentLms = determineLmsScore();

            String pathToAttempt;
            //pick larger score of the three
            if (RANDOM_SCORE > susScore && RANDOM_SCORE > lmsScore) {
                pathToAttempt = "" + generateSemiRandomAction();
            }
            else if (susScore > lmsScore) {
                pathToAttempt = getSus();
            }
            else if (lmsScore > susScore) {
                pathToAttempt = currentLms;
            }
            else {//if we tied, default to a random to hopefully tweak them
                pathToAttempt = "" + generateSemiRandomAction();
            }

            //execute "the chosen one"
            Path finalPath = stringToPath(pathToAttempt);
            tryPath(finalPath);

            scanAndRemoveNewSequences(finalPath.size());
        }//while
    }//exploreEnvironment

    /**
     * ************************************************************************************
     * METHODS FOR THE SUS
     * ************************************************************************************
     */

    /**
     * scanAndRemoveNewSequences
     *
     * Rips through the memory and finds all unique sequences that have been
     * performed and not yet removed from sequencesNotPerformed. Should be
     * called after every command the agent makes
     *
     * @param numCmdsExecuted the number of cmds commited by the last try path
     */
    public void scanAndRemoveNewSequences(int numCmdsExecuted){
        //iterate through all sizes of possible paths, if no unique paths exist, move on
        //otherwise scan mem and remove matching sequences from SNP arraylist
        for (int i=1; i<=MAX_SEQUENCE_SIZE; i++){
            //if there are no paths of this length, move on
            if (sequencesNotPerformed.get(i).isEmpty()){
                continue;
            }

            //make sure current path size is smaller than mem size, if not break out
            if (i > episodicMemory.size()-1) { //sub 1 because first " " command doesn't count
                break;
            }

            //otherwise we'll go through recently changed memory of this size and check against SNP
            //iterate over all commands - i to stop fence posting error
            //determine starting position
            int startPosition = episodicMemory.size() - 1;
            int changedMemory = MAX_SEQUENCE_SIZE + numCmdsExecuted;
            while (startPosition > 1 && startPosition >= episodicMemory.size() - changedMemory) {
                startPosition--;
            }
            for (int j=startPosition; j<=episodicMemory.size()-i; j++){
                String currentPath = ""; //path in memory to test
                for (int k=0; k<i; k++){ //iterate the size of the path through
                    currentPath += episodicMemory.get(j+k).command;
                }

                //test if this path is in SNP and remove if so, huzzah!
                if (sequencesNotPerformed.get(i).contains(currentPath)){
                    sequencesNotPerformed.get(i).remove(currentPath);
                }
            }//for
        }//for
    }//scanAndRemoveNewSequences

    /**
     * getSus
     *
     * Returns the sus by fishing through the sequencesNotPerformed and getting
     * a path of the smallest length
     *
     * @return a string if a sus is found or null if none found
     */
    public String getSus() {
        for (int i=1; i<sequencesNotPerformed.size(); i++) { //go through path sizes
            if (!sequencesNotPerformed.get(i).isEmpty()) { //if not empty, there's a victim inside
                return sequencesNotPerformed.get(i).remove(0);//returns and removes sus
            }
        }
        return null;
    }

    /**
     * determineSusScore
     *
     * set the susScore based on the summation equation and constant
     */
    public void determineSusScore() {
        //loop through mem to find length of sus
        int susLength=0;

        //get shortest length for sus
        for (int i=1; i<sequencesNotPerformed.size(); i++){
            if (!sequencesNotPerformed.get(i).isEmpty()) {
                susLength = i;
                break;
            }
        }

        //if the length is still 0 the sus has dried up, set to 0
        if (susLength == 0){
            susScore = 0;
            return;
        }

        //otherwise add up summation and multiply by constant
        double sum = 0;
        for (int i=susLength; i>0; i--) {
            sum+=i;
        }

        susScore = (1 / sum) * SUS_CONSTANT;
    }//determineSusScore

    /**
     * ************************************************************************************
     * METHODS FOR THE LMS
     * ************************************************************************************
     */

    /**
     * maxMatchedString
     *
     * Finds the ending index of the longest substring in episodic memory before
     * the previous goal matching the final string of actions the agent has
     * taken and the length of the matched string
     *
     * @return The ending index of the longest substring matching the final string of actions
     *         the agent has taken and the length of the matched string
     */
    protected int[] maxMatchedString() {
        int lastGoalIndex = findLastGoal(episodicMemory.size());
        int[] scoreInfo = {0,0};//var to be returned

        if (lastGoalIndex == -1) {
            return scoreInfo;//since init to 0's the ending score will be poor
        }

        //If we've just reached the goal, then there is nothing to match
        if (lastGoalIndex == episodicMemory.size() - 1)
        {
            return scoreInfo;//again, both are 0's for bad outcome
        }

        //Find the longest matching subsequence (LMS)
        int maxStringIndex = -1;
        int maxStringLength = 0;
        int currStringLength;
        for (int i = lastGoalIndex-1; i >= 0; i--) {
            currStringLength = matchedMemoryStringLength(i);
            if (currStringLength > maxStringLength) {
                maxStringLength = currStringLength;
                maxStringIndex = i+1;
            }
        }//for

        if (maxStringIndex < 0) {
            return scoreInfo;//bad score
        }

        else {
            scoreInfo[MATCHED_INDEX] = maxStringIndex;
            scoreInfo[MATCHED_LENGTH] = maxStringLength;
            return scoreInfo;
        }
    }//maxMatchedString

    /**
     * determineLmsScore
     *
     * Figures out the lms score and sets it using passed info from maxMatchedString
     *
     * @return pathToAttempt the string to exec if lms is chosen
     */
    private String determineLmsScore() {
        int[] matchedStringInfo = maxMatchedString();
        String pathToAttempt = stepsToGoal(matchedStringInfo[MATCHED_INDEX]);
        //calc score lengthMatched/numStepsToGoal
        double lengthMatched = matchedStringInfo[MATCHED_LENGTH];
        double numStepsToGoal = pathToAttempt.length();

        lmsScore = (lengthMatched / numStepsToGoal) * LMS_CONSTANT;
        return pathToAttempt;
    }//determineLmsScore

    /**
     * stepsToGoal
     *
     * takes an index and finds the path to reach the next goal
     *
     * @return steps the string to exec to "reach" goal
     */
    protected String stepsToGoal(int idx) {
        //loop to next goal appending all chars
        String steps = "";
        if (idx ==0)//no mem to evaluate
            return " ";
        for (int i=idx; i<episodicMemory.size(); i++) {
            steps += episodicMemory.get(i).command;
            //break if at goal
            if (episodicMemory.get(i).sensorValue == GOAL){
                break;
            }
        }
        return steps;
    }

    /**
     * ************************************************************************************
     * METHODS FOR NAVIGATION
     * ************************************************************************************
     */

	

   

	/**
	 * getMostRecentPath
	 * 
	 * Gets the most recent path present in Episodic Memory
     *
	 * @return The most recent path in episodic memory
	 */
	public Path getMostRecentPath() {
		int lastGoal = findLastGoal(episodicMemory.size() - 2) + 1;
		ArrayList<Character> pathChars = new ArrayList<Character>();
		for (int i = lastGoal; i < episodicMemory.size(); i++) {
			pathChars.add(episodicMemory.get(i).command);
		}
		return new Path(pathChars);
	}

//    //TODO: Save this method for later use
//	/**
//     * reset
//     *
//	 * Resets the agent by having it act randomly until it reaches the goal.
//	 * This will be changed to a more intelligent scheme later on
//	 */
//	public void reset() {
//		char toCheck;
//		boolean[] sensors;
//		int encodedSensorResult;
//
//		//Currently, the agent will just move randomly until it reaches the goal
//		//and magically resets itself
//		do {
//			toCheck = generateSemiRandomAction();
//			sensors = env.tick(toCheck);
//			encodedSensorResult = encodeSensors(sensors);
//			episodicMemory.add(new Episode(toCheck, encodedSensorResult));
//			/*if (episodicMemory.size() > 500000000) {
//				System.exit(0);
//			}*/
//
//		} while (!sensors[IS_GOAL]); // Keep going until we've found the goal
//	}

	

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
                StateMachineAgent gilligan = new StateMachineAgent();
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
     * tryOneCombo
     *
     * a helper method for trying one particular combination of SUS/LMS/Random
     * weights.  THis is meant to be called from main()
     *
     * @param csv         an open file to write to
     * @param randWeight  weight for random choice
     * @param susWeight   weight for SUS choice
     * @param lmsWeight   weight for LMS choice
     */
    public static void tryOneCombo(FileWriter csv, int randWeight, int susWeight, int lmsWeight)
    {
        double sum = 0;//total num successes
        for (int l = 0; l < NUM_MACHINES; l++) {//test with multiple FSMs
            
            StateMachineAgent gilligan = new StateMachineAgent();
            gilligan.RANDOM_SCORE = randWeight;
            gilligan.SUS_CONSTANT = susWeight;
            gilligan.LMS_CONSTANT = lmsWeight;
            
            gilligan.exploreEnvironment();
            
            sum += gilligan.Sucesses;
        }//for
        double averageSuccesses = sum / NUM_MACHINES;

        //write the results of this combo to the file
        try {
            System.out.println("tryOneCombo: Could not write to given csv file.");
            csv.flush();
        }
        catch (IOException e) {
            System.out.println("Could not create file, what a noob...");
            System.exit(-1);
        }


    }//tryOneCombo

	/**
	 * tryAllCombos
     *
     * exhaustively tests all permutations of weights within specified ranges.
     * 
     * TODO: Range values are hard-coded at the moment.  
	 */
    public static void tryAllCombos()
    {
        try {
            FileWriter csv = new FileWriter(OUTPUT_FILE);
            csv.append("Random,SUS,LMS,Average Score\n");

            //constants loops (trying many permutations of values)
            for (int i = 1; i < 6; i+=1) {//random loop
                for (int j = 1; j < 50; j+=1) {//sus loop
                    for (int k = 1; k < 50; k+=1) {//lms loop
                        System.out.println("Testing Random Constant: " + i
                                + " ~~~ Testing SUS Constant: " + j
                                + " ~~~ Testing LMS Constant: " + k);

                        tryOneCombo(csv, i, j, k);
                    }//lms
                }//sus
            }//random
            csv.close();
        }
        catch (IOException e) {
            System.out.println("tryAllCombos: Could not create file, what a noob...");
            System.exit(-1);
        }
    }//tryAllCombos

	/**
	 * tryRandomCombos
     *
     * tests several randomly selected permutations of weights within specified ranges.
     *
     * TODO: Range values are hard-coded at the moment.
     *
     * @param numCombos  number of combinations to test
	 */
    public static void tryRandomCombos(int numCombos)
    {
        try {
            FileWriter csv = new FileWriter(OUTPUT_FILE);
            csv.append("Random,SUS,LMS,Average Score\n");

            for(int i = 0; i < numCombos; ++i)
            {
                int randWeight = random.nextInt(6) + 1;
                int susWeight = random.nextInt(50) + 1;
                int lmsWeight = random.nextInt(50) + 1;

                tryOneCombo(csv, randWeight, susWeight, lmsWeight);
            }
            csv.close();
        }
        catch (IOException e) {
            System.out.println("Could not create file, what a noob...");
            System.exit(-1);
        }
    }//tryRandomCombos

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

}//class StateMachineAgent
