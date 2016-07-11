import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Will Goolkasian
 */
public class GoolRoseAgent extends Agent{
    
    /** WILL NOTES **
     * accept things and move onto the next endstringlength to find possible endings 
     * based on a high percentage. but whilst you are searching through them to try 
     * to get to that value, delete other endings that are just complete shit. 
     * if you have 33%, 33%, 15%, 2%, 3%, 4%, 10%, you can sucessfully drop out 
     * the 2, 3, and 4 percent most likely but should still keep looking for whatever 
     * length it is you are looking for because you havent found a good one yet.
     */
    
    
    
    private int lastPermutationIndex;
    private String lastAttempt;
    private boolean lastWasGoal;
    private ArrayList<String> possibleEndings = new ArrayList<String>();
    private ArrayList<String> goals = new ArrayList<String>();
    private String currentGoalMemory;
    
    private int endStringLength = 1;//used in updateEndStrings() increments as it goes.
    
    private static final int ASSURANCE_PERCENTAGE = 25; //used in updateEndStrings() 
    private int GOALS_NEEDED_TO_COMPARE = 10*endStringLength;
    
    
    public static final String OUTPUT_FILE = "results.txt";
    
    private String suffix;
    
    public GoolRoseAgent(){
        informationColumns = 2;
        int lastPermutationIndex = 0;
        String lastAttempt = "";
        boolean lastWasGoal = false;
        possibleEndings.add("");//add a blank ending so for the first few runs it will accept anything, will be deleted and replaced later in updateEndStrings()
        currentGoalMemory = "";
        suffix = "";
    }
    
    public static void main(String [ ] args) {
            tryGenLearningCurves();
    }//main
    
    public static void tryGenLearningCurves()
    {
        double sumOfAvgSteps = 0.00;
        double currentBaseline = 0.00;
        double sumOfFoundSteps = 0.00;
        double currentFoundSteps = 0.00;
        try {
            FileWriter csv = new FileWriter(OUTPUT_FILE);
            for(int i = 0; i < NUM_MACHINES; ++i) {
                System.out.println("Starting on Machine" + i);
                GoolRoseAgent gilligan = new GoolRoseAgent();
                gilligan.exploreEnvironment();
                gilligan.recordLearningCurve(csv);
                System.out.println("last run = '" + gilligan.lastAttempt + "'");
                System.out.println("Done with machine" + i);
                
                String path = gilligan.env.shortestPathToGoal(); //will's
//                String path2 = gilligan.env.shortestBlindPathToGoal(); //nux's
//                if(path.length() > path2.length())
//                    path = path2;
                sumOfAvgSteps += gilligan.env.avgStepsToGoalWithPath(path);
                currentBaseline = sumOfAvgSteps/(i+1);
                System.out.println("Current average shortest steps to goal = " + (currentBaseline) + "\n");
                
                String foundPath = gilligan.lastAttempt; //will's
//                String path2 = gilligan.env.shortestBlindPathToGoal(); //nux's
//                if(path.length() > path2.length())
//                    path = path2;
                sumOfFoundSteps += foundPath.length();
                currentFoundSteps = sumOfFoundSteps/(i+1);
                System.out.println("Current average of found paths = " + (currentFoundSteps) + "\n");
                
                System.out.println("Current quality percentage = " + (currentBaseline/currentFoundSteps)*100 + "\n");
            }
            System.out.println("\n\nFINAL AVERAGE LENGTH OF SHORTEST(OPTIOMAL) PATHS = " + currentBaseline);
            System.out.println("FINAL AVERAGE LENGTH OF AIs BEST FOUND PATHS = " + currentFoundSteps);
            System.out.println("QUALITY OF THESE LENGTHS AS A PERCENTAGE = " + (currentBaseline/currentFoundSteps)*100);
            
            recordColumnAverage(csv);
            recordBaseline(csv, currentBaseline);
            csv.close();
        }
        catch (IOException e) {
            System.out.println("tryGenLearningCurves: Error creating file");
            System.out.println("***YOU PROBABLY HAVE THE FILE OPEN***");
            System.exit(-1);
        }
    }//tryGenLearningCurves
    
    @Override
    public void exploreEnvironment(){
        while (memory.length() < MAX_EPISODES && Successes <= NUM_GOALS) {
            //System.out.println(memory);
            if(lastWasGoal){
                attempt(lastAttempt);
                continue;
            } //do the lastAttempt string because it worked last
            else
            {
                updateEndStrings();
                lastAttempt = nextPermutation() + suffix;
                while(checkIfDone(lastAttempt) || isEndingBad(lastAttempt))//while you have done this already
                {
                    lastAttempt = nextPermutation() + suffix; //find next until you have not done it
                }
            }
            attempt(lastAttempt);
        }//while
        
        
        
    }//exploreEnvironment
    
    /**
     * will eventually be an LMS type system that looks for patterns at the end 
     * of strings of each goal to determine if a sus generated permutation
     * should be attempted or not
     * 
     * PROBLEM: changed it so it wont be assured of the answer with only a sample size of 1 goal,
     * but the "4 goals or greater" to compare is arbitrary.
     * 
     * PROBLEM2: ties are not accounted for.
     * 
     * really don't have much faith in this anymore, needs to be re-written. perhaps "take out the bottom option" is the best way to go.
     * 
     */
    public void updateEndStrings()
    {
        if(goals.size() >= GOALS_NEEDED_TO_COMPARE){ //arbitrary. makes the Ai need at least three goal strings to compare before it can officially say it has reason to believe in an ending
            HashMap<String, Integer> compareEndings = new HashMap<String, Integer>();
            //compareEndings.put("!",0); //to prevent an empty hashmap error in the finding max line for testing
            for(String goal : goals){ 
                String lastBit = goal.substring(goal.length()-endStringLength, goal.length()); //out of bounds when goal is tiny like ba and looking for length of 3(ex). fixed by deleting these goals in updateGoals()
                if(compareEndings.containsKey(lastBit))
                    compareEndings.put(lastBit, compareEndings.get(lastBit) + 1);
                else
                    compareEndings.put(lastBit, 1);
            }


            //////////////////////////print the hashmap///////////////////////
            if(Agent.debug)
            {
                System.out.println("\n HASHMAP OF ENDINGS OF LENGTH " + endStringLength);
                for (Entry<String, Integer> entry : compareEndings.entrySet()){
                    System.out.println(entry.getKey() + ": " + entry.getValue() + " -- " + (entry.getValue()*100/goals.size()) + "%");
                }
                System.out.println("");
            }
            //////////////////////////print the hashmap///////////////////////


            for (Entry<String, Integer> entry : compareEndings.entrySet()) //itterate through each entry in hashmap
            {
                if((entry.getValue()*100)/goals.size() >= ASSURANCE_PERCENTAGE) //if the entry fits our assurance
                {
                    possibleEndings.add(entry.getKey());     // add that ending to PossibleEndings
                    endStringLength = entry.getKey().length() + 1; //set the new endLength to the next integer larger than what was just found
                }
            }
            deleteObsoleteEndings();
            deleteObsoleteGoals();//see comment in method as to why this might not be best
            if(comparePossibleEndings())
            {
                updateLastPermutationForSuffix();
            }
            
            
            
            
            
//            Integer maxValueInMap = 0;
//            if(!compareEndings.isEmpty())
//                maxValueInMap = Collections.max(compareEndings.values()); //this is crude. if bab and bcb are equally likely and true solutions, you might get 50% of ab's and 50% of cb's when looking for the length 2 endings.
//            if((maxValueInMap*100)/goals.size() >= ASSURANCE_PERCENTAGE){
//                for (Entry<String, Integer> entry : compareEndings.entrySet())   // Itrate through hashmap to find which one it was
//                    if (entry.getValue()==maxValueInMap) {
//                        possibleEndings.add(entry.getKey());     // add that ending to PossibleEndings
//                        //scan and remove all endings that are shorter (when you find a 1 length, remoev all 0. when you find a 2, remove all 1. (will remove blank)
//                        for(String endings : possibleEndings){
//                                possibleEndings.remove(endings);
//                            }
//                        endStringLength++; //go looking for the next endings incrementally larger
//                        deleteObsoleteGoals();//see comment in method as to why this might not be best
//                    }
//            }
        }
        
        if(Agent.debug)
        {
            System.out.println("\nPOSSIBLEeNDINGS:");
            for(String endings : possibleEndings)
                System.out.println(endings);
            System.out.println("");
        }
        
        
        //maybe this returrns a value saying "according to me, signifigant strings end in one of these sequences: (ArrayList)
        //then that goes through to some other check method that checks if the current next permutation 's end matches these.
        
        //give strings a buffer layer? use matcher to find it. if ALL(or some percentage) goals end in cac, only look for strings that end in ac. 
        //eventually all strings will start to end in somehting like dcac so then look for strings with cac at end. 
        //leave the last character as a buffer to allow for other possibilities, but then as patterns emerge past some threshold, take them as permenant.
    }
    
    public void updateLastPermutationForSuffix()
    {
        String debatable = "";
        if(lastAttempt.length() > suffix.length())
            debatable = lastAttempt.substring(0, lastAttempt.length()-suffix.length());
        lastPermutationIndex = permutationToNumber(debatable);
    }
    
    public void deleteObsoleteEndings()
    {
        //doesnt work because you cant remove while itterating.
//        for(String ending : possibleEndings)
//        {
//            if(ending.length() < endStringLength-1)
//                possibleEndings.remove(ending);
//        }
        
        int cursor = 0;
        do {
            if(possibleEndings.get(cursor).length() >= endStringLength-1)
                cursor++;
            else
                possibleEndings.remove(cursor);
        } while (cursor != possibleEndings.size());
    }
    
    public void attempt(String attempt)
    {
        Agent.debugPrintln(attempt);
        
        boolean lastStep;
        lastWasGoal = false;
        for(int i=0; i<attempt.length(); i++){
            currentGoalMemory = currentGoalMemory + attempt.charAt(i);
            lastStep = move(attempt.charAt(i));
            if(lastStep){
                Successes++;
                lastWasGoal = true;
                updateGoals();
                return;
            }
        }
    }
    
    public void updateGoals(){
        goals.add(currentGoalMemory);
        currentGoalMemory = "";
        deleteObsoleteGoals();
    }
    
    /**
     * remove all goals that are less than the string length we are looking for.
     * this will stop out of bounds, though kinda throws data out
     * though we do not need the data because we already have accounted for it.
     * might be better to have a check in the checkEndStrings that will then up all counters in 
     * the hashmap who's keys end in whatever goal it is we are disregarding.
     * 
     * also deletes strings that do NOT end in the list of current possible endings
     */
    public void deleteObsoleteGoals(){
        //doesnt work because you cant remove while itterating.
//        for(String g : goals)
//            if(g.length() < endStringLength)
//                goals.remove(g);

        //removes the too short goals for bounds and goals that do not end with any of the possibleEndings   
        int cursor = 0;
        do {
            boolean isValid = true;
            for(String ending : possibleEndings){
                if(goals.get(cursor).endsWith(ending) && goals.get(cursor).length() >= endStringLength ){
                    isValid = true;
                    break;
                }
                else
                    isValid = false;
            }
            if (!isValid)
                goals.remove(cursor);
            else 
                cursor++;
        } while (cursor != goals.size());
            
        
    }
    
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
            String tempMemory = memory;
            tempMemory.replaceAll("|", "");
            csv.append(tempMemory.length() + "\n"); 
            csv.flush();
            int prevGoalPoint = 0; //which episode I last reached the goal at
            for(int i = 0; i < memory.length(); ++i) {
                char c = memory.charAt(i);
                if (c == '|') { 
                    csv.append(i - prevGoalPoint + "\n");
                    csv.flush();
                    prevGoalPoint = i+1;
                }//if
            }//for
            
            csv.append("end\n");
            csv.flush();
        }
        catch (IOException e) {
            System.out.println("recordLearningCurve: Could not write to given csv file.");
            System.exit(-1);
        }
    }//recordLearningCurve
    
    /**
     * based on
     * @param index
     * @return
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
    
    public int permutationToNumber(String permutation) {
        permutation = permutation.toUpperCase();
        int number = 0;
        for (int i = 0; i < permutation.length(); i++) {
            number = number * alphabet.length + (permutation.charAt(i) - ('A' - 1));
        }
        return number;
    }
    
    
    /**
     * Takes a permutation and checks if it exists in current memory
     *
     * @param permutation      The permutation you want to try to find in current memory
     *
     * @return rtnVal     Returns true if it exists in memory, false if it does not exist in memory
     *
     * caveat:
     *      if permutation = "bab", then if only "bab|" exists in memory return false, but if there exists
     *      a 'bab" in memory anywhere return true reguardless.  If the permutation was able to get the agent to the 
     *      goal, we want to keep using it, as it may be the shortest path.
     */
    private boolean checkIfDone(String permutation){
        if(memory != null && !memory.isEmpty())
        {
            for(char c : alphabet)
                if( memory.contains(permutation+c))
                    return true;
            
            if(memory.endsWith(permutation))
            {
                return true;
            }
        }
        return false;
    }
    
    private boolean isEndingBad(String permutation){
        for(String ending : possibleEndings)
            if (permutation.endsWith(ending))
            {
                //System.out.println(permutation + " ends with '" + ending + "'");
                return false;
            }
        
        
        return true; //the ending is bad.
    }
    
    private boolean comparePossibleEndings()
    {
        try
        {
            int counter = 0;
            //the last character of the first potential ending disregarding the already established suffix
            String potentialSuffix = possibleEndings.get(0).substring(possibleEndings.get(0).length()-suffix.length()-1); 
            for(String ending : possibleEndings)
                if(!ending.endsWith(potentialSuffix))
                    return false; //nothing has changed
                else
                    counter++;

            if(counter == possibleEndings.size()) //if all the  possibleEndings end in the potentialSuffix
            {
                suffix = potentialSuffix;
                comparePossibleEndings(); //recurse to make sure when it is called, it finds the best suffix

                debugPrintln("FOUND A NEW SUFFIX! '" + suffix + "'");

                return true;
            }
            else
                return false;
        }
        catch(Exception e){/**the first possibleEnding is exactly the length of the suffix so do nothing or possibleEndings is empty so do nothing**/}
        return false;  
    }
    
}
