import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.floor;
import static java.lang.Math.log;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Will Goolkasian
 */
public abstract class Agent {
    //Instance Variables
    protected StateMachineEnvironment env;
    protected char[] alphabet;
    protected ArrayList<Episode> episodicMemory;
    protected String memory;
    protected String sensorMemory;
    protected int Successes = 0;


    //This will be useful
    public static Random random = new Random();
    //These are used as indexes into the the sensor array
    public static final int IS_NEW_STATE = 0;
    public static final int IS_GOAL = 1;
    //filename to store experimental results
    public static  String OUTPUT_FILE = "AIReport.csv";
    
    double DUPLICATE_FORGIVENESS = .25; //25% chance a duplicate is permitted (S.W.A.G.)
    
    
    /** Number of episodes per run */
    public static final int MAX_EPISODES = 2000000;
    public static final int NUM_GOALS = 50;
    /** Number of state machines to test a given constant combo with */
    public static final int NUM_MACHINES = 1;
    
    public static int informationColumns = 0; //for now before consolidation of recording data must be declared in each agent
    public static int informationRows = 1; //how many header rows there are before the data in the csv

    public static boolean azerAgentMode = true; //turn on when AzerAgent is running

    /** Turn this on to print debugging messages */
    public static boolean debug = false;
    /** println for debug messages only */
    public static void debugPrintln(String s) { if (debug) System.out.println(s); }
    public static void debugPrint(String s) { if (debug) System.out.print(s); }

    
    /**
     * ctor
     *
     * creates a new environment for hte agent and then initializes variables
     *
     **/
    public Agent()
    {
        //gets called when child instances are instantiated. could probably copy StateMachineAgent's(as NewAgent and NSMagent call it anyway) but didn't want to yet. so its emppty.
        if (debug) {
            int[][] transitions = new int[][] {{1,0}, {2, 3}, {1,0}, {2,4}, {4,4}};
            env = new StateMachineEnvironment(transitions,2 );
        }
        else{
            env = new StateMachineEnvironment();
        }
        alphabet = env.getAlphabet();
        episodicMemory = new ArrayList<Episode>();
        memory = "";
        sensorMemory = "1";
    }
    
    /**
     * method the Agents ******SHOULD****** be using to return only Boolean, not array.
     * GoolRose Agent uses
     * 
     * @param charToMove the input command
     * @return 
     */
    public boolean move(char charToMove)
    {
        memory = memory +charToMove;
        boolean result = env.move(charToMove);
        if(result)
            memory = memory +"|";
        return result;
    }
    
    /**
     * also ****UNUSED**** by agents so far 
     * @return 
     */
    public char randomMove()
    {
        int randomPos = random.nextInt(alphabet.length);
        char x = "abcdefghijklmnopqrstuvwxyz".charAt(randomPos);
        return x;
    }
    
     /**
     * also ****UNUSED**** by agents so far 
     * @return Episode
     */
    public Episode lastEpisode()
    {
        return episodicMemory.get(episodicMemory.size() - 1);
    }
    
    /**
     * also ****UNUSED**** by agents so far 
     * @return String of entire Memory. allows for use of string operations and printing memory. Testted in NSMAgent.
     */
    public String memoryToString()
    {
        String memory = "";
        for(int i = 0; i<episodicMemory.size(); i++)
        {
            memory = memory + episodicMemory.get(i).command;

            if(episodicMemory.get(i).sensorValue.GOAL_SENSOR)
                memory = memory + "|";
        }
        return memory;
    }

    /**
     * extractSubstringFromEpMem
     *
     * (used for debugging) extracts a subsequence of episodes from
     * episodicMemory and returns it as a String using only the command letters
     *
     * @param index  - starting index of substring
     * @param len    - length of the substring (zero is okay)
     *
     * @return the substring or null on invalid input
     */
    public String extractSubstringFromEpMem(int index, int len)
    {
        //check for invalid input
        if (index + len > episodicMemory.size()) return null;
        if (index < 0) return null;
        if (len < 0) return null;

        //Generate the result
        String result = "";
        for(int i = 0; i < len; ++i)
        {
            result += episodicMemory.get(index + i).command;
        }

        return result;
    }//extractSubstringFromEpMem
    
    /**
     * recordAverage
     * 
     * Called after recording all data for all the runs and adds the "=average(b1:b25)" 
     * row at the bottom. numbers/rows change dynamically
     * 
     * only works for non-columnify recording of data
     * 
     * @param csv needs to write the output file so needs to take that file in
     */
    public static void recordAverage(FileWriter csv) {
        try {
            for(int i=0; i<informationColumns-1; i++)
                csv.append(""+",");
            csv.append("AVG" + ",");
            csv.flush();
            for(int i=0; i < NUM_GOALS; i++)
            {
                String colStr = getColumnString(i+informationColumns+1);
                String range = colStr + (informationRows + 1) + ":"+colStr+(NUM_MACHINES + informationRows);
                csv.append("=average("+range+"),");
                csv.flush();
            }

            csv.append("\n");
            csv.flush();
        }
        catch (IOException eO) {
            System.out.println("Could not write to given csv file.");
            System.exit(-1);
        }
                
	}//recordAverage
    
    /**
     * recordColumnAverage
     * 
     * Called after recording all data for all the runs and adds the "=average(b1:b25)" 
     * row at the bottom. numbers/rows change dynamically
     * 
     * only works for non-columnify recording of data
     * 
     * @param csv needs to write the output file so needs to take that file in
     */
    public static void recordColumnAverage(FileWriter csv) {
        try {
            for(int i=0; i<informationColumns-2; i++)
                csv.append(""+"\n");
            csv.append("AVG" + "\n");
            csv.flush();
            for(int i=informationColumns;i <= NUM_GOALS+informationColumns; i++)
            {
                csv.append("=average(a"+ i + ":"+getColumnString(NUM_MACHINES)+i+")" + "\n");
                csv.flush();
            }

            csv.append("end\n");
            csv.flush();
        }
        catch (IOException eO) {
            System.out.println("Could not write to given csv file.");
            System.exit(-1);
        }
                
	}//recordAverage
    
    /**
     * recordBaseline
     * 
     * only works for columnify version of recording data
     * 
     * @param csv needs to write the output file so needs to take that file in
     */
    public static void recordBaseline(FileWriter csv, double baseline) {
        try {
            for(int i=0; i<informationColumns-2; i++)
                csv.append(""+"\n");
            csv.append("BASELINE" + ",,");
            csv.flush();
            for(int i=informationColumns; i < NUM_GOALS+informationColumns; i++)
            {
                csv.append(baseline + ",");
                csv.flush();
            }

            csv.append("\n");
            csv.flush();
        }
        catch (IOException eO) {
            System.out.println("Could not write to given csv file.");
            System.exit(-1);
        }
                
	}//recordBaseline
    
     /**
     * getColumnString
     * 
     * Helper method for recordAverage. turns integers for columns into 
     * Microsoft Excel's string based column system.
     * 
     * @param n takes an int of what column number it is on 
     * @return String that represents the column char(or string in case of AA, AB, ...)
     */
    public static String getColumnString(int n) {
        char[] buf = new char[(int) floor(log(25 * (n + 1)) / log(26))];
        for (int i = buf.length - 1; i >= 0; i--) {
            n--;
            buf[i] = (char) ('A' + n % 26);
            n /= 26;
        }
        return new String(buf);
    }
   
    /**
     * exploreEnvironment
     * 
     * abstract method all agents should override. main method used for agents to navigate
     */
    public abstract void exploreEnvironment();
    
    
    
    
    
    
/////////////////////////////////////unchanged inherited Methods from Old Code/////////////////////////////////////
    
    
        /**
     * findLastGoal
     *
     * Searches backwards through the list of move-result pairs from the given index
     * @param toStart The index from which to start the backwards search
     * @return The index of the previous goal
     */
    protected int findLastGoal(int toStart) {
        for (int i = toStart - 1; i > 0; i --) {
            if (episodicMemory.get(i).sensorValue.GOAL_SENSOR) {
                return i;
            }
        }
        return -1;
    }
    
    /**
    * A helper method which determines a given letter's
    * location in the alphabet
    * 
    * @param letter
    * 		The letter who's index we wish to find
    * @return
    * 		The index of the given letter (or -1 if the letter was not found)
    */
    protected int findAlphabetIndex(char letter) {
       // Iterate the through the alphabet to find the index of letter
       for(int i = 0; i < alphabet.length; i++){
           if(alphabet[i] == letter)
               return i;
        }

        // Error if letter is not found
        return -1;
    }
   
    /**
     * generateSemiRandomAction
     *
     * Generates a semi random action for the Agent to take There is a
     * disposition against making the same move again since prior research has
     * shown duplicate commands are rarely successful
     * 
     * @return A random action for the Agent to take
     */
    public char generateSemiRandomAction() {
        //decide if a dup command is acceptable
        double chanceForDup = Math.random();
        boolean dupPermitted = false;
        if (chanceForDup < DUPLICATE_FORGIVENESS) {
            dupPermitted = true;
        }

        //keep generating random moves till it is different from last or dups are allowed
        char possibleCmd;
        Episode lastEpisode = episodicMemory.get(episodicMemory.size() - 1);
        char lastCommand = lastEpisode.command;

        do {
            possibleCmd = alphabet[random.nextInt(alphabet.length)];
            if (dupPermitted)//if they are allowed we don't care to check for dup
                break;
        } while (possibleCmd == lastCommand); //same cmd, redo loop
        return possibleCmd;
    }
    
     /**
     * stringToPath
     *
     * Takes a string of chars and converts them into a path
     *
     * @param commands string to be converted
     */
    public Path stringToPath(String commands) {
        ArrayList<Character> generatedPath = new ArrayList<Character>();
        for (int i=0; i<commands.length(); i++) {
            generatedPath.add(i, commands.charAt(i));
        }
        return new Path(generatedPath);
    }//stringToPath

    /**
     * tryPath
     *
     * Given a full string of moves, tryPath will enter the moves
     * one by one and determine if the entered path is successful
     * A path is successful (returns true) only if it reaches the goal
     * on the last cmd, otherwise it will return false. If it reaches the
     * goal prematurely it will not execute anymore cmd's and return false
     *
     * TODO:  This is a deprecated version and should be phased out
     *
     * @param pathToTry; An ArrayList of Characters representing the path to try
     * 
     * @return A boolean which is true if the path was reached the goal and
     * false if it did not
     */
    public boolean tryPath(Path pathToTry) {
        Sensors sensors;
        // Enter each character in the path
        for (int i = 0; i < pathToTry.size(); i++) {
            sensors = env.tick(pathToTry.get(i));
            Sensors encodedSensorResult = new Sensors(sensors);//encodeSensors(sensors);
            episodicMemory.add(new Episode(pathToTry.get(i), encodedSensorResult));
            if (sensors.GOAL_SENSOR){
                Successes++;
                //debugPrintln("Success after " + (i + 1) + " steps.");
                return true;
           }
        }
        // If we make it through the entire loop, the path was unsuccessful
        return false;
    }//tryPath


    
    /**
     * tryPath
     *
     * Given a full string of moves, tryPath will enter the moves
     * one by one until it reaches the goal.  Once the goal is reached the
     * method stops so the entire given path may not be tried.
     *
     * NOTE: Should we really stop without finishing the entire path when we
     * reach a success?  Will the agent perform better or worse if it always
     * finishes the path?  [15 Mar 2017:  MaRzAgent now relies on current behavior.
     * I think it's best as is.]
     *
     * @param pathToTry; a string representing the path to try
     * 
     * @return the amount of the given path that was actually tried or the code
     * "FAIL" if the entire path was tried without reaching the goal
     */
    public String tryPath(String pathToTry) {
        Sensors sensors;
        String temp;
       if(env.resetSensorValue) //trying to get the first value
        {
            if(env.currentState%2 == 0)
            {
                temp = " | 0";

            }
            else {
                temp = " | 1";
            }
            sensorMemory = sensorMemory.substring(0, sensorMemory.length()-1) + temp;
            env.resetSensorValue = false;
        }
        // Enter each character in the path
        for (int i = 0; i < pathToTry.length(); i++) {
            sensors = env.tick(pathToTry.charAt(i));
            Sensors encodedSensorResult = new Sensors(sensors);
            episodicMemory.add(new Episode(pathToTry.charAt(i), encodedSensorResult));
            memory = memory + pathToTry.charAt(i);
            sensorMemory = sensorMemory + pathToTry.charAt(i) + episodicMemory.get(i).sensorValue.sensorRepresentation();
            if (sensors.GOAL_SENSOR) {
                Successes++;
                //debugPrintln("Success after " + (i + 1) + " steps.");

                
                return pathToTry.substring(0,i+1);
            }
        }
        // If we make it through the entire loop, the path was unsuccessful
        return "FAIL";
    }//tryPath

    /**
     * checkSeq
     *
     * checks to see if a given sequence has already been tried
     *
     * @param pathToTry; a string representing the path to try
     * 
     * @return the amount of the given path that was actually tried or the code
     * "FAIL" if the entire path was tried without reaching the goal
     */
    public String checkSeq(String pathToTry, String suffix) {
        int pathIndex = 0;
        int epIndex = 0;
        char currChar = pathToTry.charAt(pathIndex);
        String result = "NOT FOUND";

        //the index at which the suffix begins (we'll need this in the loop)
        int suffixIndex = pathToTry.length() - suffix.length();

        //iterate through all of epmem looking for a match
        for(epIndex = 0; epIndex < episodicMemory.size(); epIndex++)
        {
            char epChar = episodicMemory.get(epIndex).command;

            //does the current epmem episode match the path so far?
            if (epChar == pathToTry.charAt(pathIndex))
            {
                //If we reached the goal that's a special case
                if (episodicMemory.get(epIndex).sensorValue.GOAL_SENSOR)
                {
                    //if goal is in the suffix that's a success
                    if (pathIndex >= suffixIndex)
                    {
                        result = pathToTry.substring(0, pathIndex + 1);
                        break;
                    }
                    //otherwise (before the suffix) that's a mismatch
                    else
                    {
                        pathIndex = 0;
                    }
                }

                //it's a regular match (no goal)
                else
                {
                    //increment to match next char on next iteration
                    pathIndex++;
                }
                
                //If we've matched the entire path with no goal that's a fail
                if (pathIndex == pathToTry.length())
                {
                    result = "FAIL";
                    break;
                }
            }
            else //no match, so reset
            {
                pathIndex = 0;
            }
        }//for

        return result;
    }//checkSeq


    
    /**
     * Returns the index of the given character in the
     *
     * array
     * @param toCheck the character to find the index of
     * @return the index of toCheck
     */
    protected int indexOfCharacter(char toCheck) {
        for (int i = 0; i < alphabet.length; i++) {
            if (alphabet[i] == toCheck) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * matchedMemoryStringLength
     *
     * Starts from a given index and the end of the Agent's episodic memory and
     * moves backwards, comparing each episode to the present episode and it
     * prededessors until the corresponding episdoes no longer match.
     *
     * @param endOfStringIndex The index from which to start the backwards search
     * @return the number of consecutive matching characters
     */
    protected int matchedMemoryStringLength(int endOfStringIndex) {
        int length = 0;
        int indexOfMatchingAction = episodicMemory.size() - 1;
        boolean match;
        for (int i = endOfStringIndex; i >= 0; i--) {
            //We want to compare the command from the prev episode and the
            //sensors from the "right now" episode to the sequence at the
            //index indicated by 'i'
            char currCmd = episodicMemory.get(indexOfMatchingAction).command;
            Sensors currSensors = episodicMemory.get(indexOfMatchingAction).sensorValue;
            char prevCmd = episodicMemory.get(i).command;
            Sensors prevSensors = episodicMemory.get(i).sensorValue;

            match = ( (currCmd == prevCmd) && (currSensors.equals(prevSensors)) );

            if (match) {
                length++;
                indexOfMatchingAction--;
            }
            else {
                return length;
            }
        }//for

        return length;
    }//matchedMemoryStringLength
    
    /**
     * fillPermutations
     *
     * driver method to generate all strings for the sequencesNotPerformed
     * arraylist
     *
     * @param set set of chars that can be used to build strings (alphabet)
     * @param k length of string to build up to
     * @param permutations place to store the strings
     */
    public void fillPermutations(char set[], int k, ArrayList<String> permutations){
        int n = set.length;
        buildPermutations(set, "", n, k, permutations);
    }
    
    /**
     * buildPermutations
     *
     * helper method to actually build all the permutations of the strings and
     * store them in the arraylist
     *
     * @param set set of chars that can be used to build strings (alphabet)
     * @param prefix used to slowly build up different permutations
     * @param n length of set (sort of clumsy way to do it right now)
     * @param k length of string to build up to
     * @param permutations place to store the strings
     */
    public void buildPermutations(char set[], String prefix, int n, int k, ArrayList<String> permutations) {
        // Base case: k is 0
        if (k == 0) {
            permutations.add(prefix);
            return;
        }

        // One by one add all characters from set and recursively
        // call for k equals to k-1
        for (int i = 0; i < n; ++i) {
            // Next character of input added
            String newPrefix = prefix + set[i];
            // k is decreased, because we have added a new character
            buildPermutations(set, newPrefix, n, k - 1, permutations);
        }
    }//buildPermutations
    
    /**
     * findLastGoal
     *
     * Searches backwards through the list of move-result pairs from the given index
     * @param //toStart The index from which to start the backwards search
     * @return The index of the previous goal
     */
    protected int findLastGoal() {
        for (int i = episodicMemory.size() - 1; i >= 0; i--) {
            if (episodicMemory.get(i).sensorValue.GOAL_SENSOR) {
                return i;
            }
        }
        return -1;
    }

    /**
     * makeNowString
     *
     * generates a string representing right now that contains no characters
     * that are file system file name unfriendly
     */
    protected static String makeNowString() {
        String nowStr = new Date().toString();
        int spaceIndex = nowStr.indexOf(" ");
        while(spaceIndex > -1)
        {
            nowStr = nowStr.substring(0,spaceIndex) + nowStr.substring(spaceIndex+1);
            spaceIndex = nowStr.indexOf(" ");
        }
        spaceIndex = nowStr.indexOf(":");
        while(spaceIndex > -1)
        {
            nowStr = nowStr.substring(0,spaceIndex) + nowStr.substring(spaceIndex+1);
            spaceIndex = nowStr.indexOf(":");
        }
        return nowStr;
    }//makeNowString

    

}//abstract class Agent
