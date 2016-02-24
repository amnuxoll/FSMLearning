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
 * @author Will Goolkasian and Ashley Rosenberg
 */
public class GoolRoseAgent extends Agent{
    private int lastPermutationIndex;
    private String lastAttempt;
    private boolean lastWasGoal;
    
    public GoolRoseAgent(){
        System.out.println(getColumnString(1500));
        informationColumns = 2;
        int lastPermutationIndex = 0;
        String lastAttempt = "";
        boolean lastWasGoal = false;
    }
    
    public static void main(String [ ] args) {
        //        GoolRoseAgent gilligan = new GoolRoseAgent();
        //        boolean trysd = gilligan.checkPermutation(lastAttempt);
        //        if(trysd)
        //            System.out.println("true");
        //        else
        //            System.out.println("false");
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
                while(checkPermutation(lastAttempt))//while you have done this
                {
                    lastAttempt = nextPermutation(); //find next until you have not done it
                }
            }
            attempt(lastAttempt);
        }//while
        
    }//exploreEnvironment
    
    public void attempt(String attempt)
    {
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
        System.out.println(attempt);
    }//attempt
    
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
    private boolean checkPermutation(String permutation){
        boolean rtnVal = false;
        String memory = this.memoryToString();
        
        //if the substring exists in memory followed by another letter in the alphabet then we have tried that combo
        for(char i: this.alphabet){
            if(memory.contains(permutation + i)){
                rtnVal = true;
                break;
            }
        }
        
        //check if the permutation exists at the very end of the string
        if(memory.endsWith(permutation)){
            rtnVal = true;
        }
        
        return rtnVal;
        
    }//checkPermutations

}