package testMarzfromOldSource;
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
    protected String sensoryMemory;
    protected String completeMemory;
    protected int Successes = 0;
    protected int currIndex = 0;
    
    //Sensor values
    //Important Note: we discovered a bug with the way the sensor constant values in the StateMachineAgent in the
    //main branch worked with the NewAgent and thus changed the TRANSITION_ONLY constant from 1 to 2
    //and the GOAL constant from 2 to 1
    //TODO i took out 'transition only' -- it's not used 
    public static final int GOAL = 1;
    
    //This will be useful
    public static Random random = new Random();
    //These are used as indexes into the the sensor array
    //TODO i took out IS_NEW_STATE it's not used
    public static final int IS_GOAL = 1;
    //filename to store experimental results
    public static  String OUTPUT_FILE = "AIReport.csv";
    
    double DUPLICATE_FORGIVENESS = .25; //25% chance a duplicate is permitted (S.W.A.G.)
    
    
    /** Number of episodes per run */
    public static final int MAX_EPISODES = 2000000;
    public static final int NUM_GOALS = 1000;
    /** Number of state machines to test a given constant combo with */
    public static final int NUM_MACHINES = 50;
    
    public static int informationColumns = 0; //for now before consolidation of recording data must be declared in each agent
    public static int informationRows = 1; //how many header rows there are before the data in the csv
    
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
        env = new StateMachineEnvironment();
        alphabet = env.getAlphabet();
        episodicMemory = new ArrayList<Episode>();
        memory = "";
        sensoryMemory = "";
        completeMemory = "";
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
            for(int i=0; i <= NUM_GOALS; i++)
            {
                String colStr = getColumnString(i+informationColumns+2);
                String range = colStr + (informationRows) + ":"+colStr+(NUM_MACHINES + informationRows -1);
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
        Sensors sensorsWhichResultFromTryingPath;
        // Enter each character in the path
        for (int i = 0; i < pathToTry.length(); i++) {
        	sensorsWhichResultFromTryingPath = env.tick(pathToTry.charAt(i));
            
            //update memories based on new state we are in
            episodicMemory.add(new Episode(pathToTry.charAt(i), sensorsWhichResultFromTryingPath));
            memory = memory + pathToTry.charAt(i);
            sensoryMemory = sensoryMemory +pathToTry.charAt(i) +  episodicMemory.get(currIndex).sensorValue.sensorRepresentation(); 
            completeMemory = completeMemory + pathToTry.charAt(i) + episodicMemory.get(currIndex).sensorValue.sensorRepresentation()
            			+ "(" + sensorsWhichResultFromTryingPath.STATE_NUMBER + ")";
            currIndex++;
            if (sensorsWhichResultFromTryingPath.GOAL_SENSOR) {
                Successes++;
                debugPrintln("Success after " + (i + 1) + " steps.");
                //update memory
                //take out the last sensor val before the goal 
                sensoryMemory = sensoryMemory.substring(0, sensoryMemory.length()-1) + " | ";
                completeMemory = completeMemory + " | ";
                int firstStateVal = env.reset();
                if (firstStateVal > 0){
                	int oddOrEven  = 0; //odd
                	if (firstStateVal % 2 == 0) oddOrEven = 1; //if even
                	completeMemory = completeMemory + Integer.toString(oddOrEven)+ "(" + firstStateVal + ")";
                	sensoryMemory = sensoryMemory + Integer.toString(oddOrEven);
                }
                return pathToTry.substring(0,i+1);

            }
            env.reset();
        }
        // If we make it through the entire loop, the path was unsuccessful
        return "FAIL";
    }//tryPath


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