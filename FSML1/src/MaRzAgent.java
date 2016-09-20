import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
  * MaRzAgent Class
  *
  * @author Christian Rodriguez
  * @author Giselle Marston
  * @version 1.0
  * @date 9/12/2016
  *
  * NOTE: Not Finished
  */
public class MaRzAgent extends Agent {

     /*---====CONSTANTS====---*/

     // hash function for retrieving suffix node
     HashMap<SuffixNode, String> hashFringe;

     // minimum tries before a suffix node is expanded
     public static final int MIN_TRIES = 100;

     // the likeliness to jump back to another node
     // (should be in the range (0.0 - 1.0)
     public static final double G_WEIGHT = 0.2;

     // max size of list of nodes
     public static final int NODE_LIST_SIZE = 1000;

     /*---==== MEMBER VARIABLES ===---*/

     int lastPermutationIndex = 0;
     boolean universal;
     //
     // int largestSeqLength = 0;

     /** only one node at a time can be active */
     SuffixNode activeNode = null;

     /** the next sequence to consider testing */
     String nextSeqToTry = "a"; // 'a' is always safe because of how
     // StateMachineEnvironment creates FSMs

     /** to print a status message every N milliseconds we need to track 
time
      * elapsed */
     long timeOfLastStatus = 0;

     /** for profiling:  log total time spent in various code */
     public static long overallStartTime = 0;
     public static long totalTime = 0;

     /**
      * SufixNode Class
      *
      * @author Christian Rodriguez
      * @author Giselle Marston
      * @version 1.0
      * @date 9/12/2016
      *
      */
     public class SuffixNode {
         /*--==Local Constants==--*/
         public String suffix;   //
         public int failedTries; // number of failed sequences with this suffix
                                 // that we've tried
         public int tries;       // number of times this suffix has been tried
         public String queueSeq; // first skipped sequence to come back to;
                                 // non-active node sequence
         public double heuristic;
         public int g;// distance from root

         /**
          * SuffixNode
          *
          * @param initSuffix
          * @param initFailCount
          * @param initTries
          * @param initHeuristic
          */
         public SuffixNode(String initSuffix, int initFailCount, int 
initTries,
                 double initHeuristic, int initG) {
             this.suffix = initSuffix;
             this.failedTries = initFailCount;
             this.tries = initTries;
             this.queueSeq = "";
             this.heuristic = initHeuristic;
             this.g = initG;
         }// ctor


         /*
          * toString
          *
          * @see java.lang.Object#toString()
          */
         @Override
         public String toString() {
             String output = suffix + ":";
             if (queueSeq != null) {
                 output += queueSeq;
             }

             return output + "," + failedTries + "/" + tries;
         }
     }//SuffixNode Class

     /**
      * MaRzAgent
      *
      */
     public MaRzAgent() {
         hashFringe = new HashMap<MaRzAgent.SuffixNode, String>();

         // Create the initial suffix node
         SuffixNode initNode = new SuffixNode("", 0, 0, 0.0, 0);
         hashFringe.put(initNode, initNode.suffix);
         this.activeNode = initNode;

     }//ctor

     /*
      * exploreEnviroment
      *
      * @see Agent#exploreEnvironment()
      */
     @Override
     public void exploreEnvironment() {

         while (memory.length() < MAX_EPISODES && Successes <= NUM_GOALS) {
             if(activeNode.tries != 0 && !activeNode.suffix.equals("")){
                 if(universal)
                 {
                     System.out.println("**********UNIVERSAL SEQUENCE FOUND: " + activeNode.suffix + " **********");
                     return;
                 }
             }

             //Initial node gets special treatment
             if(activeNode.suffix.length() == 0){
                 nextSeqToTry = "a";
                 splitNode(activeNode.suffix);
             }

             if(hashFringe.size() > NODE_LIST_SIZE){
                 SuffixNode worst = findWorstNodeToTry();
                 hashFringe.remove(worst);
             }

             //System.out.println("FRINGE SIZE: " + hashFringe.size());

             if (nextSeqToTry.endsWith(activeNode.suffix)) {
                 debugPrintln("Trying Sequence: " + nextSeqToTry );

                 //THE SNIPPET :/
                 boolean pass = true;
                 while(pass){
                     if (Successes >= NUM_GOALS) {
                         universal = true;

                         return;  //if we reach this, path is likely universal sequence
                     }

                     pass = tryPath(nextSeqToTry);

                     activeNode.tries++;

                     if (!pass) {
                         activeNode.failedTries++;
                     }

                 }
                 //END SNIPPET

                 trySeq();

             } else {
                 activeNode = findNextNodeToTry();
                 if(activeNode.queueSeq.equals("")){
                     activeNode.queueSeq = nextSeqToTry;
                     debugPrintln("\nTrying Sequence: " + nextSeqToTry);
                     //System.out.println("WE START HERE");
                     trySeq();
                 }
             }
             nextSeqToTry = nextPermutation();

         }// while




     }//exploreEnviroment


     /**
      * splitNode
      *
      * Add new alphabet.length number of new nodes to fringe
      * Delete parent node from the fringe
      *
      */
     public void splitNode(String aSuffix){
         debugPrintln("NODE TO BE SPLIT: " + aSuffix);

         for(int i=0; i < alphabet.length; i++){
             int tempTries[] = new int[2];
         //TBD:  REMOVE - PROFILING
         long startTime = System.currentTimeMillis();

             int[] newTries =  updateTries(alphabet[i] + aSuffix);
         //TBD:  REMOVE - PROFILING
         long endTime = System.currentTimeMillis();

             System.arraycopy(newTries, 0, tempTries, 0, 2);
             this.totalTime += endTime - startTime;

             double heuristic = 0.0;
             if(tempTries[0] == 0){
                 heuristic = (activeNode.g * G_WEIGHT);
             }else{
                 heuristic = (tempTries[1] / tempTries[0]) + 
(activeNode.g * G_WEIGHT);
             }
             debugPrintln("SPLITTING NODE INTO: " + (alphabet[i] + 
aSuffix) + " fails: " + tempTries[1] + " total tries: " + tempTries[0]);
             SuffixNode aNode = new SuffixNode(alphabet[i] + aSuffix, 
tempTries[1], tempTries[0], heuristic, activeNode.g + 1);
             hashFringe.put(aNode, alphabet[i] + aSuffix);
         }

         hashFringe.remove(activeNode);




     }//splitNode

     /**
      * findNextNodeToTry
      *
      * finds node with suffix matching nextSeqToTry and returns it
      *
      * 9/7/16 
http://stackoverflow.com/questions/11420920/search-a-hashmap-in-an-arraylist-of-hashmap
      *
      */

     public SuffixNode findNextNodeToTry(){

         SuffixNode nextNode = activeNode;
         for(Map.Entry<SuffixNode,String> entry : hashFringe.entrySet()){
             if(Objects.equals(nextSeqToTry, entry.getValue())){
                 nextNode = entry.getKey();
             }
         }

         return nextNode;

     }//findNextNodeToTry





     /**
      * findBestNodeToTry
      *
      * finds node with lowest heuristic
      *
      * 9/7/16 
http://stackoverflow.com/questions/11420920/search-a-hashmap-in-an-arraylist-of-hashmap
      *
      */

     public SuffixNode findBestNodeToTry(){
         double theBEASLIESTCombo = 17976931348623157.0;
         SuffixNode bestNode = activeNode; //temporarily sets it to activeNode

         for(Map.Entry<SuffixNode,String> entry : hashFringe.entrySet()){
             if(theBEASLIESTCombo > entry.getKey().heuristic ){
                 theBEASLIESTCombo = entry.getKey().heuristic;
                 bestNode = entry.getKey();
             }
         }

         return bestNode;

     }//findNextNodeToTry


     /**
      * findWorstNode
      *
      * finds node with highest heuristic
      *
      *
      *
      */

     public SuffixNode findWorstNodeToTry(){
         double theBEASLIESTCombo = 0;
         SuffixNode worstNode = activeNode; //temporarily sets it to activeNode

         for(Map.Entry<SuffixNode,String> entry : hashFringe.entrySet()){
             if(theBEASLIESTCombo < entry.getKey().heuristic ){
                 theBEASLIESTCombo = entry.getKey().heuristic;
                 worstNode = entry.getKey();
             }
         }

         return worstNode;

     }//findNextNodeToTry



     /**
      * trySeq
      *
      */
     public void trySeq() {
         boolean pass = true;

         //TBD: DEBUGGING
         long timeSince = System.currentTimeMillis() - timeOfLastStatus;
         if(timeSince > 500){
             System.out.println("Successes: " + Successes);
             System.out.println("TRYING: " + nextSeqToTry);
             this.timeOfLastStatus = System.currentTimeMillis();
         }


         while(pass){
             if (Successes >= NUM_GOALS) {
                 universal = true;
                 return;  //if we reach this, path is likely universal sequence
             }

             pass = tryPath(nextSeqToTry);

             activeNode.tries++;

             if (!pass) {
                 activeNode.failedTries++;
             }

         }





         if(activeNode.tries != 0){
             activeNode.heuristic = (activeNode.failedTries / 
activeNode.tries)
                 + (activeNode.g * G_WEIGHT);
         }
         else{
             activeNode.heuristic = (activeNode.g * G_WEIGHT);
         }

         if(activeNode.tries >= MIN_TRIES){
             splitNode(activeNode.suffix);

             activeNode = findBestNodeToTry();

             if(!activeNode.queueSeq.equals("")){
                 nextSeqToTry = activeNode.queueSeq;
             }
         }





     }//trySeq

     /**
      * updateTries
      *
      * @param suffix
      * @return
      */
     public int[] updateTries(String suffix) {

     //    int numTries = 0; // number of times sequence was tried
         int numFails = 0; // number of times sequence wasn't tried and failed
         int[] returnVal = new int[2];
         ArrayList<Integer> indexSuf = new ArrayList<Integer>();
         String totalMem = "";

         for (int i = 0; i < episodicMemory.size(); i++) {
             totalMem = totalMem + episodicMemory.get(i).command;
         }

         for (int i = 0; i < episodicMemory.size(); i++) {
             indexSuf = getIndexOfSuffix(totalMem, suffix);
         }

         for (int i = 0; i < episodicMemory.size(); i++) {
             if (episodicMemory.get(i).sensorValue != GOAL
                     && indexSuf.contains(i)) {
                 numFails++;
             }
         }

         returnVal[0] = indexSuf.size();
         returnVal[1] = numFails;

         return returnVal; // return
     }//updateTries

     /**
      * getIndexOfSuffix
      *
      * @param aString
      * @param suffix
      * @return
      */
     public ArrayList<Integer> getIndexOfSuffix(String aString, String 
suffix) {
         String word = aString;
         String lookForString = suffix;
         ArrayList<Integer> indexOfSuffix = new ArrayList<Integer>();
         int index = word.indexOf(lookForString);
         while (index >= 0) {
             indexOfSuffix.add(index);
             index = word.indexOf(lookForString, index + 1);
         }

         return indexOfSuffix;

     }//getIndexOfSuffix

     /**
      * nextPermutation
      *
      * @return
      */
     public String nextPermutation() {
         lastPermutationIndex++;
         int index = lastPermutationIndex;
         if (index <= 0)
             throw new IndexOutOfBoundsException(
                     "index must be a positive number");
         if (index <= alphabet.length)
             return Character.toString(alphabet[index - 1]);
         StringBuffer sb = new StringBuffer();
         while (index > 0) {
             sb.insert(0, alphabet[--index % alphabet.length]);
             index /= alphabet.length;
         }
         return sb.toString();
     }// nextPermutation

     /**
      * tryGenLearningCurves
      *
      * creates a .csv file containing learning curves of several successive
      * agents
      */
     public static void tryGenLearningCurves() {
         double sumOfAvgSteps = 0.0;
         double currentBaseline = 0.0;

         try {

             FileWriter csv = new FileWriter(OUTPUT_FILE);

             for (int i = 1; i <= NUM_MACHINES; ++i) {

                 System.out.println("Starting on Machine " + i + " of "
                         + NUM_MACHINES);
                 MaRzAgent gilligan = new MaRzAgent();
                 if (Agent.debug)
                     gilligan.env.printStateMachineGraph();
                 System.out.println("Average Universal Sequence (Cheating): " + gilligan.env.shortestBlindPathToGoal());
                 System.out.println("Average Solution Length (Cheating): " + 
gilligan.env.avgStepsToGoalWithPath(gilligan.env.shortestBlindPathToGoal()));

                 String path = gilligan.env.shortestPathToGoal(); //will's
                 sumOfAvgSteps += gilligan.env.avgStepsToGoalWithPath(path);
                 currentBaseline = sumOfAvgSteps/(i+1);

                 gilligan.exploreEnvironment();
                 gilligan.recordLearningCurve(csv);
                 debugPrintln("Done with machine " + i + "\n");
             }
             recordAverage(csv);
             recordBaseline(csv, currentBaseline);
             csv.close();
         } catch (IOException e) {
             System.out
             .println("tryAllCombos: Could not create file, what a noob...");
             System.exit(-1);
         }

     }// tryGenLearningCurves

     /**
      * recordLearningCurve
      *
      * @param csv
      */
     protected void recordLearningCurve(FileWriter csv) {
         try {
             csv.append(episodicMemory.size() + ",");
             csv.flush();
             int prevGoalPoint = 0; // which episode I last reached the goal at
             for (int i = 0; i < episodicMemory.size(); ++i) {
                 Episode ep = episodicMemory.get(i);
                 if (ep.sensorValue == GOAL) {
                     csv.append(i - prevGoalPoint + ",");
                     csv.flush();
                     prevGoalPoint = i;
                 }// if
             }// for

             csv.append("\n");
             csv.flush();
         } catch (IOException e) {
             System.out.println("recordLearningCurve: Could not write to given csv file.");
             System.exit(-1);
         }

     }// recordLearningCurve


     public static void main(String [ ] args) {

         //TBD:  REMOVE - PROFILING
         MaRzAgent.overallStartTime = System.currentTimeMillis();

         Date date = new Date();
         System.out.println("Start: " + date.toString());
         tryGenLearningCurves();
         Date eDate = new Date();
         System.out.println("End: " + eDate.toString());

         //TBD:  REMOVE - PROFILING
         long overallTotalTime = System.currentTimeMillis() - 
MaRzAgent.overallStartTime;
         System.out.println("TOTAL TIME SPENT: " + overallTotalTime + " ms");
         double percent = 100.0 * (double)MaRzAgent.totalTime / 
(double)overallTotalTime;
         System.out.println("Portion spent: " + MaRzAgent.totalTime + " ms = " + percent + "%");
     }

}