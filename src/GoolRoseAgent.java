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
 * @author Will
 */
public class GoolRoseAgent extends Agent{
     private ArrayList<ArrayList<String>> sequencesNotPerformed;
     private static final int MAX_SEQUENCE_SIZE = 10; //should be dynamic based on size. May be easier to write something that stores done sequences and puts more into it as time goes on
     
    
    public GoolRoseAgent(){
        informationColumns = 2;
        env = new StateMachineEnvironment();
        alphabet = env.getAlphabet();
        episodicMemory = new ArrayList<Episode>();

        sequencesNotPerformed = new ArrayList<ArrayList<String>>();
        sequencesNotPerformed.add(0, null);//since a path of size 0 should be skipped (might not be necessary)
        for(int lengthSize=1; lengthSize<=MAX_SEQUENCE_SIZE; lengthSize++)
        {
            ArrayList<String> tempList = new ArrayList<String>();
            fillPermutations(alphabet, lengthSize, tempList);
            sequencesNotPerformed.add(lengthSize, tempList);
        }
    }
    
    public static void main(String [ ] args) {
        tryGenLearningCurves();
	}
    
    @Override
    public void exploreEnvironment(){
        
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
     * Takes a permutation and checks if it exists in current memory
     *
     * @param permutation      The permutation you want to try to find in current memory
     *
     * @return rtnVal     Returns true if it exists in memory, false if it does not exist in memory
     * 
     * caveat:
     *      if permutation = "bab", then if "bab|" is a substring return false, but if there exists
     *      a 'bab" in memory anywhere return true reguardless
     */
    private boolean checkPermutations(String permutation){
        boolean rtnVal = false;
        String memory = this.memoryToString();
        ArrayList<Integer> idxVals = this.findAllInstancesOf(permutation);
        if(idxVals.isEmpty){
            return rtnVal;
        }
        
        int substringBarCount = 0;
        for(Integer i: idxVals){
            if(memory.charAt(i+permutation.length()) == '|'){
                substringBarCount++;
            }
        }
        if(substringBarCount != idxVals.size()){
            rtnVal = true;
        }
        
        return rtnVal;
        
    }
    
    /**
     * Helper method for checkPermutations. This method searches through memory to find all 
     * instances of the given substring.
     *
     * @param str   String that is being searched for in memory
     *
     * @return rtnVal   an ArrayList of starting indexes of all instances of the substring
     *
     */
    private ArrayList<Integer> findAllInstancesOf(String str){
        String memory = this.memoryToString();
        ArrayList<Integer> rtnVal = new ArrayList<Integer>();
        while(memory != ""){
            Integer idxVal = memory.LastIndexOf(str);
            if(idxVal != -1){
                if(!rtnVal.contains(idxVal)){
                    rtnVal.add(idxVal);
                }
                memory = memory.substring(0, memory.length()-1)
            }
            else{
                //if there are no more instances of the substring then we are done
                memory = "";
            }
        }
        //remove duplicates through LinkedHashSet functionality
        //rtnval = new ArrayList<Integer>(new LinkedHashSet<Integer>(rtnVal));
    }
   
    
}
