import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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
    private int lastPermutationIndex;
    private String lastAttempt;
    private boolean lastWasGoal;
    
    public GoolRoseAgent(){
        informationColumns = 2;
        int lastPermutationIndex = 0;
        String lastAttempt = "";
        boolean lastWasGoal = false;
    }
    
    public static void main(String [ ] args) {
        tryGenLearningCurves();
    }//main
    
    @Override
    public void exploreEnvironment(){
        while (episodicMemory.size() < MAX_EPISODES && Sucesses <= NUM_GOALS) {
            if(lastWasGoal){
                attempt(lastAttempt);
                continue;
            } //do the lastAttempt string because it worked last
            else
            {
                lastAttempt = nextPermutation();
                while(checkIfDone(lastAttempt) && isEndingBad(lastAttempt))//while you have done this already
                {
                    lastAttempt = nextPermutation(); //find next until you have not done it
                }
                if(Sucesses > 3)
                {
                    findEndStrings();
                }
            }
            attempt(lastAttempt);
        }//while
        
    }//exploreEnvironment
    
    /**
     * will eventually be an LMS type system that looks for patterns at the end 
     * of strings of each goal to determine if a sus generated permutation
     * should be attempted or not
     */
    public void findEndStrings()
    {
        //maybe this returrns a value saying "according to me, signifigant strings end in one of these sequences: (ArrayList)
        //then that goes through to some other check method that checks if the current next permutation 's end matches these.
        
        //give strings a buffer layer? use matcher to find it. if ALL(or some percentage) goals end in cac, only look for strings that end in ac. 
        //eventually all strings will start to end in somehting like dcac so then look for strings with cac at end. 
        //leave the last character as a buffer to allow for other possibilities, but then as patterns emerge past some threshold, take them as permenant.
    }
    
    public void attempt(String attempt)
    {
        System.out.println(memoryToString());
        boolean lastStep;
        lastWasGoal = false;
        for(int i=0; i<attempt.length(); i++)
        {
            lastStep = move(attempt.charAt(i));
            if(lastStep)
            {
                Sucesses++;
                lastWasGoal = true;
                return;
            }
        }
    }
    
    public static void tryGenLearningCurves()
    {
        try {
            FileWriter csv = new FileWriter(OUTPUT_FILE);
            for(int i = 0; i < NUM_MACHINES; ++i) {
                System.out.println("Starting on Machine" + i);
                GoolRoseAgent gilligan = new GoolRoseAgent();
                gilligan.exploreEnvironment();
                gilligan.recordLearningCurve(csv);
                System.out.println("Done with machine" + i + "\n");
            }
            recordAverage(csv);
            csv.close();
        }
        catch (IOException e) {
            System.out.println("tryGenLearningCurves: Error creating file");
            System.exit(-1);
        }
    }//tryGenLearningCurves
    
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
        String memory = memoryToString();
        if(memory != null && !memory.isEmpty())
        {
            for(char c : alphabet)
                if( memory.contains(permutation+c))
                    return true;
            
            if("".endsWith(permutation)) //may be redundant and never ever be true
            {
                System.out.println("ends with");
                return true;
            }
        }
        return false;
    }
    
    private boolean isEndingBad(String permutation){
        
        return true; //the ending is bad.
    }
    
}
