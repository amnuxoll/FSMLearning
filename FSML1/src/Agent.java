import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.floor;
import static java.lang.Math.log;
import java.util.ArrayList;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Will
 */
public abstract class Agent {
    //Instance Variables
    protected StateMachineEnvironment env;
    protected char[] alphabet;
    protected ArrayList<Episode> episodicMemory;
    protected String memory;
    protected int Successes = 0; //THIS WAS MADE BY WILL AND ASHLEY TO RENAME THE INT BELOW. UNUSED
    
    //Sensor values
    //Important Note: we discovered a bug with the way the sensor constant values in the StateMachineAgent in the
    //main branch worked with the NewAgent and thus changed the TRANSITION_ONLY constant from 1 to 2
    //and the GOAL constant from 2 to 1
    public static final int NO_TRANSITION = 0;
    public static final int TRANSITION_ONLY = 2;
    public static final int GOAL = 1;
    
    //This will be useful
    public static Random random = new Random();
    //These are used as indexes into the the sensor array
    public static final int IS_NEW_STATE = 0;
    public static final int IS_GOAL = 1;
    //filename to store experimental results
    public static final String OUTPUT_FILE = "AIReport.csv";
    
    double DUPLICATE_FORGIVENESS = .25; //25% chance a duplicate is permitted (S.W.A.G.)
    
    
    /** Number of episodes per run */
    public static final int MAX_EPISODES = 2000000;
    public static final int NUM_GOALS = 500;
    /** Number of state machines to test a given constant combo with */
    public static final int NUM_MACHINES = 100;
    
    public static int informationColumns; //for now before consolidation of recording data must be declared in each agent
    
    /** Turn this on to print debugging messages */
    public static boolean debug = true;
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
        env = new StateMachineEnvironment();
        alphabet = env.getAlphabet();
        episodicMemory = new ArrayList<Episode>();
        memory = "";
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
            if(episodicMemory.get(i).sensorValue == GOAL)
                memory = memory + "|";
        }
        return memory;
    }
    
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
            for(int i=0; i<informationColumns-2; i++)
                csv.append(""+",");
            csv.append("AVG" + ",");
            csv.flush();
            for(int i=informationColumns+2; i <= NUM_GOALS+informationColumns+2; i++)
            {
                csv.append("=average("+getColumnString(i)+"1:"+getColumnString(i)+""+NUM_MACHINES+")" + ",");
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
            csv.append("BASELINE" + ",");
            csv.flush();
            for(int i=informationColumns; i <= NUM_GOALS+informationColumns; i++)
            {
                csv.append(baseline + ",");
                csv.flush();
            }

            csv.append("end\n");
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
            if (episodicMemory.get(i).sensorValue == GOAL) {
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
        boolean[] sensors;
        // Enter each character in the path
        for (int i = 0; i < pathToTry.size(); i++) {
            sensors = env.tick(pathToTry.get(i));
            int encodedSensorResult = encodeSensors(sensors);
            episodicMemory.add(new Episode(pathToTry.get(i), encodedSensorResult));
            if (sensors[IS_GOAL]){
                Successes++;
                debugPrintln("Success after " + (i + 1) + " steps."); 
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
     * TODO: Should we really stop without finishing the entire path when we
     * reach a success?  Will the agent perform better or worse if it always
     * finishes the path?
     *
     * @param pathToTry; a string representing the path to try
     * 
     * @return the amount of the given path that was actually tried or the code
     * "FAIL" if the entire path was tried without reaching the goal
     */
    public String tryPath(String pathToTry) {
        boolean[] sensors;
        // Enter each character in the path
        for (int i = 0; i < pathToTry.length(); i++) {
            sensors = env.tick(pathToTry.charAt(i));
            int encodedSensorResult = encodeSensors(sensors);
            episodicMemory.add(new Episode(pathToTry.charAt(i), encodedSensorResult));
            memory = memory + pathToTry.charAt(i);
            if (sensors[IS_GOAL]) {
                Successes++;
                debugPrintln("Success after " + (i + 1) + " steps.");

                
                return pathToTry.substring(0,i+1);
            }
        }
        // If we make it through the entire loop, the path was unsuccessful
        return "FAIL";
    }//tryPath

    /**
     * Takes in an agent's sensor data and encodes it into an integer
     * @param sensors The agent's sensor data
     * @return the integer encoding of that sensor data
     */
    protected int encodeSensors(boolean[] sensors) {
        int encodedSensorResult;

        if (sensors[IS_GOAL]) {
            encodedSensorResult = GOAL;
        }

        else if (sensors[IS_NEW_STATE]) {
            encodedSensorResult = TRANSITION_ONLY;
        }

        else {
            encodedSensorResult = NO_TRANSITION;
        }

        return encodedSensorResult;
    }
    
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
            int currSensors = episodicMemory.get(indexOfMatchingAction).sensorValue;
            char prevCmd = episodicMemory.get(i).command;
            int prevSensors = episodicMemory.get(i).sensorValue;

            match = ( (currCmd == prevCmd) && (currSensors == prevSensors) );

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
     * @param toStart The index from which to start the backwards search
     * @return The index of the previous goal
     */
    protected int findLastGoal() {
        for (int i = episodicMemory.size() - 1; i >= 0; i--) {
            if (episodicMemory.get(i).sensorValue == IS_GOAL) {
                return i;
            }
        }
        return -1;
    }
    

}
