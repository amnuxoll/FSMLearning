//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.*;
//
//
//
///**
// * class NewAgent
// *
// * This is a "trial" agent that is being used to test a new algorithm for
// * finding the shortest path to the goal. This algorithm looks at
// * sequences of a set length (length determined by constant)in episodic memory and combines the scores from
// * directly aligned matches, a constituency/substring match algorithm, and the
// * number of steps to the goal to find the best possible
// * next move.
// * @author: Sara Meisburger and Christine Chen
// *
// */
//
//
//public class ChenMeisAgent extends Agent
//{
//
//
//    //constants
//    protected static int NUM_TOP_ACTIONS = 8; //number of top scores we will keep track of
//    protected static int COMPARE_SIZE = 8; //length of sequence to compare to get quality score
//
//    //constants for scores
//    //please note: elsewhere in the code we refer to a "quality constant." This is lingo
//    //we used to refer to both the counting and aligned constants simultaneously as we only tested with
//    //both of those constants at equal values. Thus, in tryOneCombo, for example,
//    //the counting constant and aligned constant are both set to some quality weight.
//    private static int COUNTING_CONSTANT = 10; //multiplier for counting score
//    private static int ALIGNED_CONSTANT= 10; //multiplier for aligned score
//
//    //constant for steps to goal functionality
//    //a cutoff value of -1 turns off this feature
//    private static int STEPS_FROM_GOAL_CUTOFF = 25;
//
//    //declare array of Recommendation objects
//    //keeps track of recommended char, steps to goal, and quality score for top
//    //scoring found sequences
//    private Recommendation[] topNextActions;
//
//    //array to hold the number of recommendations for each char action based on the info from
//    //topNextActions
//    private int[] frequencyNextActions;
//
//    //percentage variables for method usage
//    //(how often we use each method)
//    double percentSUS = 0;
//    double percentRandom = 0;
//    double percentQuality = 0;
//
//    ///////COPPIED VARIABLES///////
//    protected static int SUS_CONSTANT = 96; //will become final after testing to find values
//    protected static int RANDOM_SCORE = 16; //will become final after testing
//    protected double susScore = 0;
//     private static final int MAX_SEQUENCE_SIZE = 10; //just picked 7 as a guess
//    private ArrayList<ArrayList<String>> sequencesNotPerformed;
//
//    //number of runs we want to do
//    //called in main method if multiple csv files are desired
//    protected static int NUM_RUNS = 1;
//
//    //name of file we are saving run info in
//    //declared in main
//    protected static String fileName;
//
//
//    //DEBUGGING
//    protected static long totalMachineTime = 0;
//    protected static double avgRunTimeCheckCond = 0;
//    protected static double avgRunTimeFoundSeq = 0;
//
//    /**
//     * NewAgent()
//     * constructor
//     * calls super from StateMachine Agent
//     * initializes topNextActions array and frequencyNextActions array
//     */
//    public ChenMeisAgent()
//    {
//        informationColumns = 8;
//
//        //prime the epmem with a first episode that is empty
//        episodicMemory.add(new Episode(' ', NO_TRANSITION));//the space cmd means unknown cmd for first memory
//
//        //build the permutations of all sequences (up to max SUS len)
//        sequencesNotPerformed = new ArrayList<ArrayList<String>>();
//        sequencesNotPerformed.add(0, null);//since a path of size 0 should be skipped (might not be necessary)
//        for(int lengthSize=1; lengthSize<=MAX_SEQUENCE_SIZE; lengthSize++){
//            ArrayList<String> tempList = new ArrayList<String>();
//            fillPermutations(alphabet, lengthSize, tempList);
//            sequencesNotPerformed.add(lengthSize, tempList);
//        }
//
//        //use NUM_TOP_ACTIONS+1 so we can sort and keep NUM_TOP_ACTIONS
//        topNextActions = new Recommendation[NUM_TOP_ACTIONS+1];
//        frequencyNextActions = new int[env.ALPHABET_SIZE];
//    }
//
//
//    /**
//     * exploreEnvironment
//     *
//     * Once an agent is created it is set loose to explore its environment based
//     * on a determined number of episodes (MAX_EPISODES).
//     * In determining what move to make next, the agent takes an original sequence and
//     * compares it to found sequences in its episodic memory.
//     * For each found sequence, the agent then generates a score based on matched subsequences
//     * and direct matches and stores top scores along the way in an array (topNextActions).
//     *
//     * When it comes time to make the final decision,
//     * the average score from topNextActions is calculated and compared against SUS score and random score:
//     * if average array score is highest, do most frequently recommended char from array
//     * if SUS has highest score, try SUS path
//     * if random has highest score, try random, or semi random char
//     *
//     * The topNextActions array and frequencyNextActions array are reset for each new original sequence
//     */
//    @Override
//    public void exploreEnvironment() {
//
//        //initializing everything
//        Episode[] originalSequence = new Episode[COMPARE_SIZE]; //initialize originalSequence
//        Episode[] foundSequence = new Episode[COMPARE_SIZE]; //initialize foundSequence
//
//        int lastGoalIndex; //index of last goal
//
//        double susCounter = 0; //record how many times we choose SUS
//        double randomCounter = 0;//record how many times we choose random
//        double qualityCounter = 0;//record how many times we choose quality
//        double decisionCounter = 0; //counter for how many times we make a decision
//        int stepsFromGoal = 0; //how far found sequence is from last goal index
//
//        //DEBUGGING
//        //for testing run times
//        long sumRunTimesCheckCond = 0;
//        int numCallsCheckCond = 0;
//        long sumRunTimesFoundSeq = 0;
//        int numFound = 0;
//
//        //while we have not exceeded the max number of episodes, keep getting new original sequences to test
//        //then make moves
//        while (episodicMemory.size() < MAX_EPISODES && Successes <= NUM_GOALS) {
//            lastGoalIndex = findLastGoal(episodicMemory.size()); //get last goal index
//
//            //DEBUGGING to see how long it takes checkConditions to execute/////////////////////////
//            long startCheckCond = System.currentTimeMillis();
//            lastGoalIndex = checkConditions(lastGoalIndex); //check conditions to ensure you can get original seq
//            long endCheckCond = System.currentTimeMillis();
//
//            sumRunTimesCheckCond = (endCheckCond-startCheckCond) + sumRunTimesCheckCond;
//            numCallsCheckCond++;
//            /////////////////////////////////////
//
//            originalSequence = getOriginalSequence(); //get the original sequence of size COMPARE_SIZE
//            //reset frequency arrays for each new original sequence
//            resetFrequencyArrays();
//
//            //DEBUGGING to see how long it takes to get a found sequence///////////////////////////////////////
//            boolean metFoundCond = true;
//            long startFoundCond = 0;
//
//            //iterate through episodic memory and get found sequences of length COMPARE_SIZE
//            //get a quality score for each and fill in the top scores in the topNextActions array
//            for(int w = lastGoalIndex; w >= COMPARE_SIZE; w--){
//
//                if(metFoundCond)
//                {
//                    //a found sequence has been "found"...time to refresh the start time
//                    startFoundCond = System.currentTimeMillis();
//                }
//
//
//                int meetsFoundConditions = checkFoundConditions(w); //check that we can get a found seq
//                if(meetsFoundConditions == -1){
//
//                    //we're good to go...
//
//                    ///ALL PART OF CALCULATING RUNTIME FOR GETTING A FOUND SEQUENCE//////////
//                    //get the end time
//                    long endFoundCond = System.currentTimeMillis();
//                    //add the difference between endFoundCond and startFoundCond to sumRunTimesCheckFoundCond
//                    sumRunTimesFoundSeq = sumRunTimesFoundSeq + (endFoundCond-startFoundCond);
//                    //increment counter (keeps track of how many found sequences were found)
//                    numFound++;
//                    //set metFoundCond to true
//                    metFoundCond = true;
//                    ////////////////////////////////////////////////////////////////////////
//
//                    foundSequence = getFoundSequence(w); //fill found sequence
//                    //w is the indice corresponding to the first character of the found sequence in episodic memory
//                    stepsFromGoal = lastGoalIndex - w;
//
//                    //if the cutoff value functionality is -1 (meaning it has been turned off)
//                    //completely ignore this if statement
//                    if(stepsFromGoal > STEPS_FROM_GOAL_CUTOFF && STEPS_FROM_GOAL_CUTOFF != -1)
//                    {
//                        //otherwise, don't calculate the quality score for a found sequence
//                        //that is so far away from the goal...just continue on to the next found sequence
//                        continue;
//                    }
//
//                }
//                else
//                {
//                    w = meetsFoundConditions; //doesn't meet conditions, start at next goal
//                    //update lastGoalIndex
//                    lastGoalIndex = w;
//
//                    //IMPORTANT for the purposes of determining the runtime for finding a found sequence//////
//                    metFoundCond = false;
//                    //////////////////////////////////////
//                    continue;
//                }
//
//                //call our quality methods to get scores
//                double countingScore = getCountingScore(originalSequence, foundSequence);
//                double alignedMatches = getAlignedMatchesScore(originalSequence, foundSequence);
//
//                //get a total quality score based on the two quality methods
//                double tempQualityScore = (double)((COUNTING_CONSTANT)*countingScore + (ALIGNED_CONSTANT)*alignedMatches);
//
//
//                //make a Recommendation object containing the score, steps to Goal, and recommended next character
//                //place in the last spot in the topNextActions array
//                topNextActions[NUM_TOP_ACTIONS] = new Recommendation(tempQualityScore, stepsFromGoal, episodicMemory.get(w+1).command);
//
//                //sort the array (array will be sorted from ascending to descending)
//                Arrays.sort(topNextActions);
//
//                //the last spot holds the Recommendation object with the lowest score, set it to null
//                topNextActions[NUM_TOP_ACTIONS] = null;
//            }
//
//            double sumOfTopScores  = 0.0;
//            //loop through topNextActions and record the frequencies of the different
//            //recommended action chars in frequencyNextActions
//            for (int i = 0; i < topNextActions.length - 1; i++)
//            {
//                //get the Recommendation object's recommended action
//                char action = topNextActions[i].recommendedAction;
//
//                //add the score of the recommendation object to sumOfTopScores
//                sumOfTopScores = topNextActions[i].score + sumOfTopScores;
//
//                //get the index of that action in the alphabet array
//                int indexOfAction = findAlphabetIndex(action);
//
//                //increment the value in frequencyNextActions[indexOfAction]
//                frequencyNextActions[indexOfAction]++;
//            }
//
//            //get average of the top scores
//            double avgTopScores = sumOfTopScores/NUM_TOP_ACTIONS;
//
//            //get the SUS score to be compared
//            determineSusScore();
//
//            //loop through frequencyNextActions and determine most frequently recommended move
//            int indexBestMove = 0;
//            int highestFreq = 0;
//
//            for(int j = 0; j < frequencyNextActions.length; j++)
//            {
//                if(frequencyNextActions[j]>highestFreq)
//                {
//                    indexBestMove = j;
//                    highestFreq = frequencyNextActions[j];
//                }
//            }
//
//            //increment the decision counter as the agent is about to decide what move to make next
//            //based on all the information gathered
//            decisionCounter++;
//
//            //if the RANDOM_SCORE is highest, do a random move
//            if (RANDOM_SCORE > susScore && RANDOM_SCORE > avgTopScores) {
//                String pathWeAttempt = "" + generateSemiRandomAction();
//                Path finalPath = stringToPath(pathWeAttempt);
//                tryPath(finalPath);
//                randomCounter++;
//            }
//            else if (susScore > avgTopScores){
//                String pathToAttempt = getSus();
//
//                //in case there are no more SUS left...
//                if (pathToAttempt == null)
//                {
//                    pathToAttempt = "" + generateSemiRandomAction();
//                    susCounter--;
//                }
//                Path finalPath = stringToPath(pathToAttempt);
//                tryPath(finalPath);
//                susCounter++;
//            }
//            //if the avgTopScores is higher, do most frequently recommended char
//            else {
//                tryPath(stringToPath(Character.toString(alphabet[indexBestMove])));
//                qualityCounter++;
//            }
//        }
//        percentSUS = (susCounter/decisionCounter)*100.0;
//        percentRandom = (randomCounter/decisionCounter)*100.0;
//        percentQuality = (qualityCounter/decisionCounter)*100.0;
//
//        //DEBUGGING--Runtimes
//        avgRunTimeCheckCond = sumRunTimesCheckCond/(double)numCallsCheckCond;
//        avgRunTimeFoundSeq = sumRunTimesFoundSeq/(double)numFound;
//
//
//    }
//
//     /**
//     * determineSusScore
//     *
//     * set the susScore based on the summation equation and constant
//     */
//    public void determineSusScore() {
//        //loop through mem to find length of sus
//        int susLength=0;
//
//        //get shortest length for sus
//        for (int i=1; i<sequencesNotPerformed.size(); i++){
//            if (!sequencesNotPerformed.get(i).isEmpty()) {
//                susLength = i;
//                break;
//            }
//        }
//
//        //if the length is still 0 the sus has dried up, set to 0
//        if (susLength == 0){
//            susScore = 0;
//            return;
//        }
//
//        //otherwise add up summation and multiply by constant
//        double sum = 0;
//        for (int i=susLength; i>0; i--) {
//            sum+=i;
//        }
//
//        susScore = (1 / sum) * SUS_CONSTANT;
//    }//determineSusScore
//
//    /**
//     * getSus
//     *
//     * Returns the sus by fishing through the sequencesNotPerformed and getting
//     * a path of the smallest length
//     *
//     * @return a string if a sus is found or null if none found
//     */
//    public String getSus() {
//        for (int i=1; i<sequencesNotPerformed.size(); i++) { //go through path sizes
//            if (!sequencesNotPerformed.get(i).isEmpty()) { //if not empty, there's a victim inside
//                return sequencesNotPerformed.get(i).remove(0);//returns and removes sus
//            }
//        }
//        return null;
//    }
//
//
//    /**
//     * ResetFrequencyArrays()
//     * resets top next actions array to have dummy recommendation objects
//     * resets the frequencyNextActions array as well
//     *
//     */
//    private void resetFrequencyArrays(){
//        for (int i = 0; i < topNextActions.length; i++)
//        {
//            topNextActions[i] = new Recommendation(0.0, 0, alphabet[random.nextInt(alphabet.length)]);
//        }
//
//        //fill frequencyNextActions with 0s
//        for (int i = 0; i < frequencyNextActions.length; i++)
//        {
//            frequencyNextActions[i] = 0;
//        }
//    }
//
//    /*
//    * getAlignedMatchesScore
//    * @param originalEpisodes array of Episodes
//    * @param foundEpisodes array of Episodes to compare against original episodes
//    * @return scoreToReturn number of directly aligned matches divided by COMPARE_SIZE
//    *
//    *loop through original and found arrays to tally when a char in the found sequence directly
//    * matches with the corresponding char in the original sequence
//    */
//    protected double getAlignedMatchesScore(Episode[] originalEpisodes, Episode[] foundEpisodes)
//    {
//        //convert Episode arrays into char arrays
//        char[] originalChars = new char[COMPARE_SIZE];
//        char[] foundChars = new char[COMPARE_SIZE];
//
//        for (int i = 0; i<COMPARE_SIZE; i++)
//        {
//            originalChars[i] = originalEpisodes[i].command;
//            foundChars[i] = foundEpisodes[i].command;
//        }
//
//        //initialize counter
//        int numAlignedChars = 0;
//
//        //iterate through the two char arrays to determine how many direct aligned matches there are
//        for(int i = 0; i<COMPARE_SIZE; i++)
//        {
//            if(originalChars[i] == (foundChars[i]))
//            {
//                numAlignedChars++;
//            }
//        }
//
//        //the total direct aligned matches over the COMPARE_SIZE to get a score between 0-1
//        double scoreToReturn = (double)numAlignedChars/COMPARE_SIZE;
//
//        return scoreToReturn;
//    }
//
//
//
//    /**
//     *getCountingScore()
//     * @param original array of Episodes
//     * @param found array of Episodes to compare against original episodes
//     * @return finalCountingScore sum of the lengths of the matching subsequences divided by the total
//     * number of subsequences based on sequence length
//     * finds all subsequences of original and found sequences
//     * compares all found subsequences against original subsequences
//     * score is determined by the sum of the length of matching subsequences divided by max number of subseq
//     */
//    protected double getCountingScore(Episode[] original, Episode[] found){
//
//        //create array list of strings to hold all subsequences for original and found
//        ArrayList<String> originalSubsequences = new ArrayList<String>();
//        ArrayList<String> foundSubsequences = new ArrayList<String>();
//
//        int count = 0; //counter for how many subsequences match, not currently used but good to have
//        int score = 0; //score for matching subsequences, longer sub = higher score
//        int totalLengthOfSeqs = 0; //sum of length of all subsequences
//
//        double finalCountingScore; //score to return
//
//        //convert Episode arrays into char arrays
//        char[] originalChars = new char[COMPARE_SIZE];
//        char[] foundChars = new char[COMPARE_SIZE];
//
//        for (int i = 0; i<COMPARE_SIZE; i++)
//        {
//            originalChars[i] = original[i].command;
//            foundChars[i] = found[i].command;
//        }
//
//        //append char arrays into string so we can manipulate to get subsequences
//        String originalString = new String (originalChars);
//        String foundString = new String (foundChars);
//
//        //fill arrayLists with all subsequences of original and found strings we created
//        for(int i=1; i<=originalString.length(); i++){ // i determines length of string
//            for(int j=0; j<=originalString.length()-i; j++){ // j determines where we start (indice) in string
//                originalSubsequences.add(originalString.substring(j,j+i));
//                foundSubsequences.add(foundString.substring(j,j+i));
//            }
//        }
//        //for each subsequence in original, compare to see if it is in found list of subsequences
//        for(int p=0; p<originalSubsequences.size(); p++){
//
//            //sum up all the lengths of the subsequences to use later when determining score
//            String mySubstring = originalSubsequences.get(p);
//            totalLengthOfSeqs = totalLengthOfSeqs + mySubstring.length();
//
//            for(int q=0; q<foundSubsequences.size(); q++){
//                if(originalSubsequences.get(p).equals(foundSubsequences.get(q))){
//                    String temp = originalSubsequences.get(p);
//                    count++; //increment counter if we found a matching subsequence
//
//                    //get length of matching subsequence
//                    //longer matching subsequences mean higher scores
//                    score = temp.length() + score;
//
//                    //avoid overcounting, once a subsequence is found to be matched,
//                    //remove that subseq from found subsequences arraylist
//                    foundSubsequences.remove(q);
//                    break;
//                }
//            }
//        }
//
//        //calculate finalCountingScore between 0-1
//        finalCountingScore = (double)score/totalLengthOfSeqs;
//        return finalCountingScore;
//    }
//
//    /**
//     * checkConditions
//     * @param lastGoalIndex, the most recent goal
//     * @return index of goal that has at least COMPARE_SIZE episodes before it
//     * we need to make sure certain conditions are met before we start getting original sequences
//     * we meed to make sure that:
//     * 1) the agent has at least one goal in episodic memory
//     * 2) the agent doesn't try to go out of bounds in the memory array--we thus
//     * ensure that random actions are generated until the most recent episodes in gilligan's episodic memory
//     * can be used to make a valid original sequence (an original sequence contains no "goal" actions and is
//     * COMPARE_SIZE long)
//     */
//    private int checkConditions(int lastGoalIndex){
//
//        //while we don't have a goal in episodic memory, keep making random moves
//        while (lastGoalIndex == -1) {
//
//
//            String pathWeAttempt = "" + generateSemiRandomAction();
//            Path finalPath = stringToPath(pathWeAttempt);
//            tryPath(finalPath);
//
//            lastGoalIndex = findLastGoal(episodicMemory.size()-1);
//        }
//
//
//        //If we've just reached the goal in the last COMPARE_SIZE characters, then generate random steps until long enough
//        //or if our episodic memory is not large enough, keep generating random actions
//        //or if we have reached a goal in the first COMPARE_SIZE moves, we can't get a found sequence so do random move
//        while (lastGoalIndex >= episodicMemory.size() - COMPARE_SIZE || episodicMemory.size() < COMPARE_SIZE || lastGoalIndex < COMPARE_SIZE){
//            String pathWeAttempt = "" + generateSemiRandomAction();
//            Path finalPath = stringToPath(pathWeAttempt);
//            tryPath(finalPath);
//
//            lastGoalIndex = findLastGoal(episodicMemory.size()-1);
//        }
//
//        return lastGoalIndex;
//    }
//
//    /**
//     * checkFoundConditions
//     * @param indice some indice in Episodic memory passed in from explore Environment that we
//     * want to verify is valid (i.e. has COMPARE_SIZE-1 valid episodes directly preceding it in episodic memory)
//     * @return if there is a goal in the COMPARE_SIZE-1 episodes directly preceding indice, return the index of the goal,
//     * otherwise return -1 to indicate all is well.
//     */
//    private int checkFoundConditions(int indice){
//        //start i at the furthest possible character, increment i to move towards indice
//        //go until i<indice because we don't care if the episode at indice is a goal or not
//        for(int i=(indice-COMPARE_SIZE)+1; i<indice; i++){
//
//            //if the episode is a goal, return the index of that episode
//            if(episodicMemory.get(i).sensorValue == GOAL){
//                return i;
//            }
//        }
//        return -1;
//    }
//    /**
//     * getOriginalSequence()
//     * @return an array of the most recent Episodes (array has size COMPARE_SIZE)
//     */
//    private Episode[] getOriginalSequence(){
//
//        //fill the array with the most recent episodes
//        Episode[] originalSequence = new Episode[COMPARE_SIZE];
//
//        for (int k=1; k<=COMPARE_SIZE; k++){
//
//            originalSequence[k-1] = (episodicMemory.get(episodicMemory.size()-k));
//        }
//        return originalSequence;
//    }
//
//    /**
//     * getFoundSequence()
//     * @param indice some valid indice in the Episodic Memory that is passed in from exploreEnvironment()
//     * @return an array of the Episodes starting at indice and counting back by COMPARE_SIZE
//     */
//    private Episode[] getFoundSequence(int indice){
//
//        Episode[] foundSequence = new Episode[COMPARE_SIZE];
//
//        for (int j=1; j<=COMPARE_SIZE; j++){
//
//            foundSequence[j-1] = (episodicMemory.get(indice));
//            indice--;
//        }
//        return foundSequence;
//    }
//
//    /**
//     * tryGenLearningCurves()
//     * overwriting method from StateMachineAgent.java to use the NewAgent
//     */
//    public static void tryGenLearningCurves()
//    {
//        try {
//
//            FileWriter csv = new FileWriter(fileName);
//
//            for(int i = 0; i < NUM_MACHINES; ++i) {
//                //keep track of what machine we are on in console
//                System.out.println("machine number: " + (i+1));
//                ChenMeisAgent gilligan = new ChenMeisAgent();
//
//                //DEBUGGING--Runtimes
//                long startTime = System.currentTimeMillis();
//                gilligan.exploreEnvironment();
//                long endTime = System.currentTimeMillis();
//                totalMachineTime = endTime - startTime;
//
//                gilligan.recordLearningCurve(csv);
//            }
//            recordAverage(csv);
//            csv.close();
//        }
//        catch (IOException e) {
//            System.out.println("tryGenLearningCurves: Could not create file, what a noob :( ...");
//            System.exit(-1);
//        }
//    }//tryGenLearningCurves
//
//    /**
//     * recordLearningCurve
//     *
//     * examines the agents memory and prints out how many steps the agent took
//     * to reach the goal each time
//     * record percentage of time we use SUS, Random, and Quality
//     *
//     * @param csv         an open file to write to
//     */
//    protected void recordLearningCurve(FileWriter csv) {
//        try {
//            csv.append(episodicMemory.size() + ",");
//            csv.flush();
//            int prevGoalPoint = 0; //which episode I last reached the goal at
//
//            //record each methods constant and how often we used it
//            csv.append(" SUS constant: " + SUS_CONSTANT + " ,");
//            csv.append(" SUS percentage: " + percentSUS + ",");
//            csv.append(" Random constant: " + RANDOM_SCORE + ",");
//            csv.append(" Random percentage: " + percentRandom + " ,");
//            csv.append(" Quality constant: " + ALIGNED_CONSTANT + ",");
//            csv.append(" Quality percentage: " + percentQuality + ",");
//
//
//
//            for (int i = 0; i < episodicMemory.size(); ++i) {
//                Episode ep = episodicMemory.get(i);
//                if (ep.sensorValue == GOAL) {
//                    csv.append(i - prevGoalPoint + ",");
//                    csv.flush();
//                    prevGoalPoint = i;
//                }//if
//            }//for
//
//            csv.append("\n");
//            csv.flush();
//        } catch (IOException e) {
//            System.out.println("recordLearningCurve: Could not write to given csv file :( .");
//            System.exit(-1);
//        }
//    }
//    /**
//     * tryOneCombo
//     *
//     * a helper method for trying one particular combination of SUS/Quality/Random
//     * weights.
//     *
//     * @param csv         an open file to write to
//     * @param randWeight  weight for Random choice
//     * @param susWeight   weight for SUS choice
//     * @param qualityWeight   weight for Quality choice
//     */
//    public static void tryOneCombo(FileWriter csv, int randWeight, int susWeight, int qualityWeight)
//    {
//
//        double sum = 0;//total num successes
//        for (int l = 0; l < NUM_MACHINES; l++) {//test with multiple FSMs
//
//            ChenMeisAgent gilligan = new ChenMeisAgent();
//            RANDOM_SCORE = randWeight;
//            SUS_CONSTANT = susWeight;
//            COUNTING_CONSTANT = qualityWeight;
//            ALIGNED_CONSTANT = qualityWeight;
//
//            gilligan.exploreEnvironment();
//            gilligan.recordLearningCurve(csv);
//
//
//            sum += gilligan.Successes;
//        }//for
//        double averageSuccesses = sum / NUM_MACHINES;
//
//        try {
//
//            System.out.println("tryOneCombo...");
//            csv.append("\n");
//            csv.flush();
//        }
//        catch (IOException e) {
//            System.out.println("tryOneCombo: Could not create file, what a noob...");
//            System.exit(-1);
//        }
//
//
//    }//tryOneCombo
//
//    /**
//     * tryAllCombos
//     *
//     * exhaustively tests all permutations of weights within specified ranges.
//     *
//     * TODO: Range values are hard-coded at the moment.
//     */
//    public static void tryAllCombos()
//    {
//        try {
//            FileWriter csv = new FileWriter(OUTPUT_FILE);
//            //csv.append("Random,SUS,Quality,Average Score\n");
//
//            //constants loops (trying many permutations of values)
//            for (int i = 2; i < 30; i+=1) {//random loop
//                for (int j = 1; j < 48; j+=1) {//sus loop
//                    for (int k = 1; k < 50; k+=1) {//quality loop
//                        System.out.println("Testing Random Constant: " + i
//                                + " ~~~ Testing SUS Constant: " + j
//                                + " ~~~ Testing Quality Constant: " + k);
//
//                        tryOneCombo(csv, i, j, k);
//
//                    }//quality
//                }//sus
//            }//random
//            recordAverage(csv);
//            csv.close();
//        }
//        catch (IOException e) {
//            System.out.println("tryAllCombos: Could not create file, what a noob...");
//            System.exit(-1);
//        }
//    }//tryAllCombos
//
//
//    /**
//     * main
//     *
//     * Modify this method to call the methods you want
//     * whether it be tryGenLearningCurves, or tryAllCombos, etc...
//     */
//    public static void main(String [ ] args) {
//
//        for(int i=0; i < NUM_RUNS; i++){
//            //name our csv file after what run number we are currently on
//            fileName = ("AIReport"+i+".csv");
//
//            tryGenLearningCurves();
//        }
//        //when gilligan is done, say so to console
//        System.out.println("Done.");
//    }
//}
