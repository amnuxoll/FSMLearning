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
    
}
