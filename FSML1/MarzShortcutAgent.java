package testMarzfromOldSource;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Templates;

/**
 * MarzShortcutAgent Class
 * @author Emily Peterson
 * @author Andrew Ripple
 * @author Zachary Faltersack
 * @author Regan Tierney
 * 
 * @author Christian Rodriguez
 * @author Giselle Marston
 * @author Andrew Nuxoll
 * @version 3.0
 * 
 */
public class MarzShortcutAgent extends Agent
{

	/*---====CONSTANTS====---*/

	// the likeliness to jump back to another node
	// (should be in the range (0.0 - 1.0)
	public static double G_WEIGHT = 0.05;

	// max size of list of nodes
	public static final int NODE_LIST_SIZE = 100000;

	/*---==== MEMBER VARIABLES ===---*/

	/** hash table of all nodes on the fringe of our search */
	HashMap<String, SuffixNode> hashFringe;

	/** this is the node we're currently using to search with */
	SuffixNode activeNode = null;

	/**
	 * each permutation has a number associated with it. This is used to track
	 * the last permutation the agent tried.
	 */
	int lastPermutationIndex = 1;// set to 1 because we hard coded the first
									// permutation to be 'a'

	/**
	 * the next sequence to consider testing (typically generated via
	 * lastPermutationIndex
	 */
	String nextSeqToTry = "a"; // 'a' is always safe because of how

	/**
	 * to print a status message every N milliseconds we need to track time
	 * elapsed
	 */
	long timeOfLastStatus = 0;

	/**
	 * the last sequence that was successful (used for reporting and not
	 * required for the algorithm)
	 */
	String lastSuccessfulSequence = "";

	/** for profiling: log total time spent in various code */
	public static long overallStartTime = 0;
	public static long totalTime = 0;
	
    /**
     * holds the sequence to try based on matched memory (adjusted suffix)
     */
    String newSuffixVal = "";
    String newSuffixValSensors = "";
	
	

	/**
	 * SuffixNode Class
	 * 
	 * @author Christian Rodriguez
	 * @author Giselle Marston
	 * @version 1.4
	 * @date 10/11/2016
	 * 
	 */
	public class SuffixNode
	{
		/*--==Instance Variables==--*/
		public String suffix;
		public int queueSeq; // if this node becomes active, start with this
								// permutation
		public double f; // the current overall potential of this suffix (f = g + h)
		public int g; // distance from root (ala A* search)
		public int tries; // number of times a sequence with this suffix has been tried
		public double failRate;  //[0.0..1.0] fraction of failed tries
		public double parentFailRate;  //save parent's fail rate to track my progress
        public boolean goalFound = false;  //have we found a sequence that ends
                                           //exactly at the goal when this node is active
        public int lastScanIndex = 0;      //The last time epmem was scanned for
                                           //matches to this suffix, it stopped here
        
		/**
		 * indices into episodicMemory of successful/failed sequences with this
		 * suffix
		 */
		public ArrayList<Integer> successIndexList;
		public ArrayList<Integer> failsIndexList;
		
		/**
		 * the length of episodicMemory the last time the above lists were
		 * updated
		 */
		public int indexOfLastEpisodeTried;

		/**
		 * SuffixNode default ctor inits variables for a root node.
		 * 
		 * NOTE: If creating a non-root node (@see #splitNode) these values will
		 * need to be initialized properly. It can't be done in ctor without
		 * creating inefficiencies.
		 * 
		 */
		public SuffixNode()
		{
			this.suffix = "";
			this.queueSeq = 1;
			this.f = 0.0;
			this.g = 0;
			this.indexOfLastEpisodeTried = 0;
			this.successIndexList = new ArrayList<Integer>();
			this.failsIndexList = new ArrayList<Integer>();
			this.tries = 0;
			this.failRate = 0.0;
			this.parentFailRate = 0.0;

		}// ctor

		/**
		 * toString
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			String output = suffix;
            if (queueSeq > 1)
            {
                output += "(q=" + nextPermutation(queueSeq+1) + ")";
            }

			int failedTries = failsIndexList.size();
            int succTries = successIndexList.size();
			int tries = failedTries + successIndexList.size();

            updateHeuristic();
            double truncatedG = (int)(g * G_WEIGHT * 100.0) / 100.0;  //trim to 2 decimal places
			output = output + ":" + truncatedG + "+" + (failedTries) + "/" + (failedTries + succTries);
            double truncatedHeur = (int)(f * 1000.0) / 1000.0;  //trim to 3 decimal places
			output = output + "=" + truncatedHeur;
			return output;
		}
        
        /**
         * updateHeuristic
         * 
         * Recalculate this node's heuristic value (h) and overall value(f)
         */
        public void updateHeuristic()
        {
            double gWeight = this.g * G_WEIGHT;
        
            //special case: avoid divide-by-zero
            if (successIndexList.size() + failsIndexList.size() == 0)  
            {
                this.f = gWeight;
                this.failRate = 0.0;
            }// if

            //this is the usual case
            else
            {
                double numFail = this.failsIndexList.size();
                double numSucc = this.successIndexList.size();
                this.failRate = numFail / (numFail + numSucc);
                this.f = gWeight + this.failRate;
            }// else

        }// updateHeuristic

	}// SuffixNode Class
	
	/*
	 * SequenceSensorPair
	 * used to hold a tuple of objects: String actions and String Sensors
	 */
	public class SequenceSensorPair {
        String characterString = "";
        String sensorString = "";

        public SequenceSensorPair(String chars, String sensors){
            characterString = chars;
            sensorString = sensors;
        }
        public String getCharacterString(){
            return characterString;
        }
        public String getSensorString(){
            return sensorString;
        }
    }//SequenceSensorPairClass

	/**
	 * MarzShortcutAgent
	 * 
	 */
	public MarzShortcutAgent()
	{
		hashFringe = new HashMap<String, MarzShortcutAgent.SuffixNode>();

		// Create an empty root node and split it to create an initial fringe
		// that has a node for each letter in the alphabet
		SuffixNode initNode = new SuffixNode();
		hashFringe.put("", initNode);
		this.activeNode = initNode;

	}// ctor

	/**
	 * exploreEnviroment
	 * 
	 * @see Agent#exploreEnvironment()
	 */
	@Override
	public void exploreEnvironment()
	{

		while (memory.length() < MAX_EPISODES && Successes <= NUM_GOALS)
		{

			// Erase worst node in the hashFringe once we hit our Constant limit
			while (hashFringe.size() > NODE_LIST_SIZE)
			{
				SuffixNode worst = findWorstNodeToTry();
				hashFringe.remove(worst.suffix);
			}// if

            //If the next sequence matches the active node, try it
			if (nextSeqToTry.endsWith(activeNode.suffix))
			{
				debugPrintln("Trying Sequence: " + nextSeqToTry);

				if (Successes <= NUM_GOALS)
				{
					trySeq();

                    //check to see if another node would be better now
                    SuffixNode newBestNode = findBestNodeToTry();

                    if (newBestNode != activeNode) {
                        activeNode.queueSeq = 1;
                        activeNode = newBestNode;

                        // Use the new active node's queue sequence if it exists
                        if (activeNode.queueSeq > 1)
                        {
                            lastPermutationIndex = activeNode.queueSeq;
                            activeNode.queueSeq = 1;
                        }// if
                    }

				}// if

			}// if
            
			else  //sequence's suffix did not match active node
			{
                //If this non-active node doesn't have a queueSeq yet, set it
				SuffixNode node = findNodeForPath(nextSeqToTry);
				if ((node != null) && (node.queueSeq == 1))
				{
                    node.queueSeq = lastPermutationIndex - 1;
				}// if
			}// else
			
			nextSeqToTry = nextPermutation();
			

		}// while

	}// exploreEnviroment

    /**
     * findNodeForPath
     *
     * locates the node in the hashFringe that matches a given path
     *
     * @param path  the path to search with
     *
     * @return the node or null if there is no match
     */
    public SuffixNode findNodeForPath(String path) {
        int charIndex = path.length() - 1;
        String key = "";
        while (! hashFringe.containsKey(key))
        {
            if (charIndex == -1)
            {
                //Example of how this result can be reached:
                //  given path is "ac" and fringe has keys "aac", "bac" and "cac"
                return null;
            }// if

            key = path.charAt(charIndex) + key;
            charIndex--;
        }// while

        //If this non-active node doesn't have a queueSeq yet, set it
        return hashFringe.get(key);

    }//findNodeForPath

    /**
     * findNodeForIndex
     *
     * locates the node in the hashFringe that matches a subsequence of
     * episodicMemory that *ends* with the episode at the given index
     *
     * CAVEAT:  does not check for invalid index!
     *
     * @param index  start the search here
     *
     * @return the node or null if there is no match
     */
    public SuffixNode findNodeForIndex(int index) {
        String key = "";
        while (! hashFringe.containsKey(key))
        {
            Episode ep = episodicMemory.get(index);

            //if we back into the previous goal without finding a key then there is no match
            if ((key.length() > 0) && (ep.sensorValue.GOAL_SENSOR)) return null;
            
            key = ep.command + key;
            index--;

            //don't fall off the end of the memory
            if (index < 0) return null;
        }// while

        //If this non-active node doesn't have a queueSeq yet, set it
        return hashFringe.get(key);

    }//findNodeForIndex

    
    /**
     * divyIndexes
     *
     * is a helper method for splitNode.  It divies up a parent's
     * successIndexList or failsIndexList among the children.
     *
     * @param parent     the parent node
     * @param children   an array of SuffixNode indexed by the child's new letter
     * @param success    indicates whether to divy successes or fails
     */
    protected void divyIndexes(SuffixNode parent, SuffixNode[] children, boolean success)
    {
        //Extract the needed lists
        ArrayList<Integer> parentList = success ? parent.successIndexList : parent.failsIndexList;
        ArrayList<ArrayList<Integer>> childLists = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < alphabet.length; i++)
		{
            childLists.add( success ? children[i].successIndexList : children[i].failsIndexList );
        }

        //divy
		for (Integer indexObj : parentList)
		{
			int index = indexObj.intValue() - 1;  //the -1 because child adds a letter

            //If we fall off the back of the epmem then it can't be matched
            if (index < 0)
			{
				continue;
			}// if

            //If we've backed into the previous goal then we can't match either
			if ((parent.suffix.length() > 0) && (episodicMemory.get(index).sensorValue.GOAL_SENSOR))
			{
                continue;
            }

			int childIdx = memory.charAt(index) - 'a';
            childLists.get(childIdx).add(new Integer(index));
            
		}// for
        
    }//divyIndexes
    
	/**
	 * splitNode
	 * 
	 * Add new alphabet.length number of new nodes to fringe by replacing the
	 * current active node with a new node that prepends each letter to the
	 * active node's suffix. success/fail values and similar are recalculated
	 * using the parent node's data.
	 * 
	 * SIDE EFFECT: the active node is removed from the fringe PREREQ: the
	 * active node's values are up to date
	 * 
	 */
	public void splitNode()
	{
		String parentSuffix = this.activeNode.suffix;
		debugPrintln("NODE TO BE SPLIT: " + activeNode);

		// Create the initial child nodes
		SuffixNode[] children = new SuffixNode[alphabet.length];
		for (int i = 0; i < alphabet.length; i++)
		{
			children[i] = new SuffixNode();
			children[i].suffix = alphabet[i] + parentSuffix;
			children[i].g = activeNode.g + 1;
			children[i].indexOfLastEpisodeTried = memory.length() - 1;
			children[i].parentFailRate = activeNode.failRate;
		}// for

        //Divy the successes and failures among the children
        divyIndexes(activeNode, children, true);
        divyIndexes(activeNode, children, false);

		// Recalculate the children's heuristics
		for (int i = 0; i < alphabet.length; i++)
		{
            //if the child's suffix has never been tried, then it's too soon:
            //abort this split!
            if (children[i].failsIndexList.size() == 0) return;
        }//for        
        
        //Ready to commit:  add the children to the fringe and remove the parent
		for (int i = 0; i < alphabet.length; i++)
		{
			hashFringe.put(children[i].suffix, children[i]);
		}// for
		hashFringe.remove(activeNode.suffix);

        // //%%%REMOVE THIS!
        // if (hashFringe.size() >= 4)
        // {
        //     System.out.println("DONE!!");
        //     System.out.println("active: " + activeNode);
        //     for(SuffixNode node : hashFringe.values())
        //     {
        //         System.out.println(node);
        //         System.out.print("fail: ");
        //         for(Integer i : node.failsIndexList)
        //         {
        //             System.out.print(i + ",");
        //         }
        //         System.out.print("    success: ");
        //         for(Integer i : node.successIndexList)
        //         {
        //             System.out.print(i + ",");
        //         }
        //         System.out.println();
        //     }
        //     System.exit(0);
        // }

	}// splitNode

	/**
	 * findBestNodeToTry
	 * 
	 * finds node with lowest heuristic
	 */
	public SuffixNode findBestNodeToTry()
	{

		SuffixNode[] nodes = (SuffixNode[]) hashFringe.values().toArray(
				new SuffixNode[hashFringe.size()]);
		assert (nodes.length > 0);

		double theBEASTLIESTCombo = nodes[0].f;
		SuffixNode bestNode = nodes[0];
		for (SuffixNode node : nodes)
		{
            node.updateHeuristic();
            
			if (node.f < theBEASTLIESTCombo)
			{
				theBEASTLIESTCombo = node.f;
				bestNode = node;
			}// if
		}// for
		
		//Marz shortcut -- check for possible candidate
		SequenceSensorPair memCandidate = matchMemSeq();//returns potential sequence to try
        SuffixNode candidateNode = null;
        if (memCandidate != null){
            String candidateSuffix = memCandidate.getCharacterString();
            //find the suffix node which matches or  ends with candidateSuffix
            Set<String> keys = hashFringe.keySet();
            List<String> list = new ArrayList<>(keys);
            Collections.sort(list, (o1, o2) -> o1.length() < o2.length() ? 1 : o1.length() > o2.length() ? -1 : 0);
            String addToSuccess = "";
            for(String nodeName: list) {
                if (nextSeqToTry.endsWith(nodeName)) {
                    addToSuccess = nodeName;
                    break;
                }
            }
            if(!addToSuccess.equals("")) {
                candidateNode= hashFringe.get(addToSuccess);
            }
            //continue back in the sequence until a match is found
            else
            {
                for(int i = sensoryMemory.length()-1; i >= 0; i--)
                {
                    if(Character.toString(sensoryMemory.charAt(i)).equals("|")){
                        String name = nextSeqToTry;

                        for(int j = i-2; j >=0;j--)
                        {
                            name = sensoryMemory.charAt(i) + name;
                            if(hashFringe.containsKey(name))
                            {
                                candidateNode = hashFringe.get(name);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            if (candidateNode!= null){
                //recalculating memSinceGoal to get the length
                int index = sensoryMemory.length();
                String memSinceGoal = "";
                while(true) {
                    if (index <= 0) {
                        break;
                    }
                    index--;
                    if (!(sensoryMemory.charAt(index) == ' ')) {
                        memSinceGoal = Character.toString(sensoryMemory.charAt(index)) + memSinceGoal;
                    }
                    else{
                        break;
                    }
                }
                double F_Weight = 1.2*(1-2*Successes/(NUM_GOALS+1)); //modify this to change weight on node
                double candidatef = (double)candidateSuffix.length() /((double)memSinceGoal.length()/2 + (double)candidateSuffix.length());
                if (F_Weight*candidatef < (activeNode.failRate) ) {
                    System.out.println("Candidate f: " + candidatef + "  " + candidateSuffix + " active f: " + (activeNode.failRate) + " " + nextSeqToTry);
                    bestNode = candidateNode;
                    newSuffixVal = candidateSuffix; //save the actual sequence to try in global val
                    newSuffixValSensors = memCandidate.getSensorString();
                }
                System.out.println(completeMemory);
                System.out.println(sensoryMemory);
            }
        }


		return bestNode;

	}// findBestNodeToTry

	/**
	 * findWorstNode
	 * 
	 * finds node with largest heuristic
	 * 
	 */
	public SuffixNode findWorstNodeToTry()
	{
		SuffixNode[] nodes = (SuffixNode[]) hashFringe.values().toArray(
				new SuffixNode[hashFringe.size()]);
		assert (nodes.length > 0);

		double theBEASTLIESTCombo = nodes[0].f;
		SuffixNode worstNode = nodes[0];
		for (SuffixNode node : nodes)
		{
			if (node.f > theBEASTLIESTCombo)
			{
				theBEASTLIESTCombo = node.f;
				worstNode = node;
			}// if
		}// for

		return worstNode;

	}// findWorstNodeToTry

	/**
	 * trySeq
	 * 
	 * Tries nextSeqToTry until it fails Splits node when MIN_TRIES is reached
	 */
	public void trySeq()
	{

        // Try the sequence until it fails
        String result = "";
        int suffixLength = 0;
        do
        {
            //if there is a suffixVal worth trying, try it instead of nextSeqToTry
            if (!(newSuffixVal.equals(""))) {
                result = tryPathSensors(newSuffixVal);
                System.out.println(newSuffixVal);
                suffixLength = newSuffixVal.length();

            }
            //default
            else {
                result = tryPath(nextSeqToTry);
            }


            // Update the active node's success/fail lists and related based
            // upon whether we reached the goal or not. Reaching the goal
            // before the suffix is reached is treated as neither a fail nor
            // success for heuristic purposes. However, it is still an overall
            // success so the path will be repeated in this loop.
            if (result.equals("FAIL") )
            {
               if (!newSuffixVal.equals("")){
                   activeNode.failsIndexList.add(new Integer(this.memory.length() - suffixLength));
               }
               else {
                   activeNode.failsIndexList.add(new Integer(this.memory.length()
                           - activeNode.suffix.length()));
               }
            }// if
            else if (result.equals("PARTIAL_FAIL")){
                //assign no failures for now
            }

            else // possible success
            {
                int unusedLen;
                //adjusted suffix used
                if (!newSuffixVal.equals("")){
                    unusedLen = suffixLength - result.length();
                    //since the active node was based off of our specific algorithm, find a new one
                    activeNode = findBestNodeToTry(); //this will reslt newSuffixVal if applicable

                }
                //nextseq to try used
                else {
                    unusedLen = nextSeqToTry.length() - result.length();

                    //if the last step of the sequence hits the goal that's a success
                    lastSuccessfulSequence = nextSeqToTry;
                }

                if (unusedLen == 0)
                {
                    //adjusted sequence used
                    if (!newSuffixVal.equals("")) {
                        activeNode.successIndexList
                                .add(new Integer(this.memory.length() + unusedLen
                                        - suffixLength));
                    }
                    //nextSeqTo try used
                    else {
                        activeNode.successIndexList
                                .add(new Integer(this.memory.length() + unusedLen
                                        - activeNode.suffix.length()));
                    }

                    activeNode.goalFound = true;
                }// if
                else  //found the goal before sequence finished
                {
                    //For the activeNode, this is neither a success nor a
                    //failure.  So, nothing is recorded on that node. However,
                    //it is a success for the node that matches the success.  So
                    //we give credit for that here.
                    SuffixNode node = findNodeForIndex(episodicMemory.size() - 1);
                    if (node != null) {
                        int index = this.memory.length() - node.suffix.length();
                        node.successIndexList.add(new Integer(index));

                    }//if  (another node can take credit for this success)

                }//else (reached goal too soon)

            }// else (possible success)

            newSuffixVal = "";

            activeNode.tries++;


        } while (!(result.equals("FAIL") || result.equals("PARTIAL_FAIL")) && memory.length() < MAX_EPISODES
                && Successes <= NUM_GOALS);

        // The active node is split once it's found a successful sequence but
        // that sequence eventually failed.
        if (activeNode.goalFound)
        {
            splitNode();
            activeNode = findBestNodeToTry();

            // Use the new active node's queue sequence if it exists
            if (activeNode.queueSeq > 1)
            {
                lastPermutationIndex = activeNode.queueSeq;
                activeNode.queueSeq = 1;
            }// if

        }// if

    }// trySeq

	
	/**
     *matchMemSeq - returns a string sequence IF:
     *  1. the current sensor mem since last goal matches somewhere previously in mem
     * returns a SequenceSensor Pair of <String actions, String sensors>
     */
    public SequenceSensorPair matchMemSeq(){
            int index = sensoryMemory.length();
            String memSinceGoal = "";
          
            //collect the sensoryMemory sequence since the last goal
            while(true) {
                if (index <= 0) {
                    break;
                }
                index--;
                //skip if we found ourself, or if we ran into the last goal
                if (!(sensoryMemory.charAt(index) == ' ')) {
                    memSinceGoal = Character.toString(sensoryMemory.charAt(index)) + memSinceGoal;
                }
                else{
                    break;
                }
            }
            //System.out.println(memSinceGoal);
            //find matches among prefix frontier nodes in hashfringe
            Pattern pattern = Pattern.compile("(?=(" + memSinceGoal +"))." );
            Matcher matcher = pattern.matcher(sensoryMemory);
            
            //arraylist to hold all possible sequences (one for each match in memory)
            ArrayList<SequenceSensorPair> candidateSequences = new ArrayList<SequenceSensorPair>();
            while (matcher.find()) {
                int currentIndex = matcher.start() + memSinceGoal.length();
                String candidateSuffix = "";
                String candidateSuffixSensors = "";
                //we found ourself
                if(sensoryMemory.substring(currentIndex).equals(" ")){
                    continue;
                }
                while (currentIndex < sensoryMemory.length()){
                   //break when we hit a goal
                	if (sensoryMemory.charAt(currentIndex) == '|') {
                        //if (prefixRoot.suffixHash.containsKey(candidateSuffix)){
                        //candidateNodes.add(prefixRoot.suffixHash.get(candidateSuffix));
                        candidateSequences.add(new SequenceSensorPair(candidateSuffix, candidateSuffixSensors));
                        //}
                        break;
                    }
                    candidateSuffix = candidateSuffix + Character.toString(sensoryMemory.charAt(currentIndex));
                    candidateSuffixSensors = candidateSuffixSensors + Character.toString(sensoryMemory.charAt(currentIndex +1));
                    currentIndex+=2; //to account for sensors & actions

                }

            }
            //return the shortest candidate sequence
            SequenceSensorPair returnString = null;
            for (SequenceSensorPair i : candidateSequences){
                if (returnString != null) {
                    if (i.getCharacterString().length() < returnString.getCharacterString().length()) {
                        returnString = i;
                    }
                }
                else{
                    returnString = i;
                }
            }
            return returnString;
    }
	
    
    /*
     * tryPathSensors
     * 
     * the equivalent of try path in agent but matches sensors 
     * implements "bail on fail" if the sensors do not match
     */
    public String tryPathSensors(String pathToTry) {
        Sensors sensorsWhichResultFromTryingPath;
        // Enter each character in the path
        for (int i = 0; i < pathToTry.length(); i++) {
        	sensorsWhichResultFromTryingPath = env.tick(pathToTry.charAt(i));
            
            //update memories based on new state we are in
            episodicMemory.add(new Episode(pathToTry.charAt(i), sensorsWhichResultFromTryingPath));
            memory = memory + pathToTry.charAt(i);
            sensoryMemory = sensoryMemory +pathToTry.charAt(i) +  episodicMemory.get(currIndex).sensorValue.sensorRepresentation(); 
            System.out.println(sensoryMemory);
            completeMemory = completeMemory + pathToTry.charAt(i) + episodicMemory.get(currIndex).sensorValue.sensorRepresentation()
            			+ "(" + sensorsWhichResultFromTryingPath.STATE_NUMBER + ")";
            currIndex++;
            if (sensorsWhichResultFromTryingPath.GOAL_SENSOR) {
                Successes++;
                debugPrintln("Success after " + (i + 1) + " steps.");
                //update memory
                //take out the last sensor val before the goal 
                sensoryMemory = sensoryMemory.substring(0,sensoryMemory.length()-1) + " | ";
                completeMemory = completeMemory + " | ";
                int firstStateVal = env.reset();
                	int oddOrEven  = 0; //odd
                	if (firstStateVal % 2 == 0) oddOrEven = 1; //if even
                	completeMemory = completeMemory + Integer.toString(oddOrEven)+ "(" + firstStateVal + ")";
                	sensoryMemory = sensoryMemory + Integer.toString(oddOrEven);
                return pathToTry.substring(0,i+1);

            }
            env.reset();
            
            //bail if sensors do not match
            String currentSensor = Character.toString(newSuffixValSensors.charAt(i));
            if (!(( episodicMemory.get(currIndex-1).sensorValue.EVEN_SENSOR && currentSensor.equals("1")) || (! episodicMemory.get(currIndex-1).sensorValue.EVEN_SENSOR && currentSensor.equals("0")))){
                return  "PARTIAL_FAIL";
            }
        }
        // If we make it through the entire loop, the path was unsuccessful
        return "FAIL";
    }//tryPath


	
	
	/*
	 * Timing Scripts
	 * 
	 * TBD: REMOVE - PROFILING long startTime = System.currentTimeMillis();
	 * 
	 * TBD: REMOVE - PROFILING long endTime = System.currentTimeMillis();
	 * this.totalTime += endTime - startTime;
	 */

	/**
	 * getIndexOfSuffix
	 * 
	 * returns an list of the indexes into the string where a particular
	 * subsequence (suffix) occurs after a given starting index. In other words,
	 * it's like a mass indexOf().
	 * 
	 * CAVEAT: caller is responsible for passing in reasonable values
	 */
	public ArrayList<Integer> getIndexOfSuffix(String memoryStr,
			int startIndex, String suffix)
	{
		ArrayList<Integer> indexOfSuffix = new ArrayList<Integer>();

		int index = memoryStr.indexOf(suffix, startIndex);
		while (index >= 0)
		{
			indexOfSuffix.add(index);
			startIndex += index + 1;
			if (startIndex >= memoryStr.length())
			{
				break;
			}// if

			index = memory.indexOf(memoryStr, startIndex);
		}// while

		return indexOfSuffix;

	}// getIndexOfSuffix

	/**
	 * nextPermutation
	 * 
	 * converts queueSeq int into a String
	 * 
	 */
	public String nextPermutation(int index)
	{
		if (index <= 0)
		{
			throw new IndexOutOfBoundsException(
					"index must be a positive number.  Has your next permutation index overflowed?");
		}// if
		if (index <= alphabet.length)
		{
			return Character.toString(alphabet[index - 1]);
		}// if

		StringBuffer sb = new StringBuffer();
		while (index > 0)
		{
			sb.insert(0, alphabet[--index % alphabet.length]);
			index /= alphabet.length;
		}// while

		return sb.toString();

	}// nextPermutation

	/**
	 * nextPermutation
	 * 
	 * increments nextSeqToTry
	 */
	public String nextPermutation()
	{
		lastPermutationIndex++;
		return nextPermutation(lastPermutationIndex);
	}// nextPermutation

	/**
	 * tryGenLearningCurves
	 * 
	 * creates a .csv file containing learning curves of several successive
	 * agents
	 */
	public static void tryGenLearningCurves()
	{
		double sumOfAvgSteps = 0.0;
		double currentBaseline = 0.0;

		try
		{

            String fname = "AIReport_" + makeNowString() + ".csv";
            FileWriter csv = new FileWriter(fname);
			
			for (int i = 1; i <= NUM_MACHINES; ++i)
			{

				System.out.println("Starting on Machine " + i + " of "
						+ NUM_MACHINES);
				MarzShortcutAgent gilligan = new MarzShortcutAgent();

				if (Agent.debug)
					gilligan.env.printStateMachineGraph();
				System.out.println("Average Universal Sequence (Cheating): "
						+ gilligan.env.shortestBlindPathToGoal());
				System.out.println("Average Solution Length (Cheating): "
						+ gilligan.env.avgStepsToGoalWithPath(gilligan.env
								.shortestBlindPathToGoal()));

				String path = gilligan.env.shortestPathToGoal(); // will's
				sumOfAvgSteps += gilligan.env.avgStepsToGoalWithPath(path);
				currentBaseline = sumOfAvgSteps / (i + 1);

				gilligan.exploreEnvironment();
				gilligan.recordLearningCurve(csv);
                
				System.out.println("Done with machine " + i);
                System.out.println("\tlast successful sequence: " + gilligan.lastSuccessfulSequence + " solves " + gilligan.env.numStatesSolvedBy(gilligan.lastSuccessfulSequence) + "/" + StateMachineEnvironment.NUM_STATES + " states");
				System.out.println("\tactive node: " + gilligan.activeNode);
                System.out.println();
			}// for
			recordAverage(csv);
			recordBaseline(csv, currentBaseline);
			csv.close();
		}// try
		catch (IOException e)
		{
			System.out.println("tryAllCombos: Could not create file, what "
					+ "a noob...");
			System.exit(-1);
		}// catch

	}// tryGenLearningCurves

	/**
	 * recordLearningCurve
	 * 
	 * @param csv
	 */
	protected void recordLearningCurve(FileWriter csv)
	{
		try
		{
			
			csv.append(episodicMemory.size() + ",");
			csv.flush();
			int prevGoalPoint = 0; // which episode I last reached the goal at
			for (int i = 0; i < episodicMemory.size(); ++i)
			{
				Episode ep = episodicMemory.get(i);
				if (ep.sensorValue.GOAL_SENSOR)
				{
					csv.append(i - prevGoalPoint + ",");
					csv.flush();
					prevGoalPoint = i;
					System.out.println(sensoryMemory);
				}// if
			}// for

			csv.append("\n");
			csv.flush();
		}// try
		catch (IOException e)
		{
			System.out.println("recordLearningCurve: Could not write to given "
					+ "csv file.");
			System.exit(-1);
		}// catch

	}// recordLearningCurve

	public static void main(String[] args)
	{

		// TBD: REMOVE - PROFILING
		MarzShortcutAgent.overallStartTime = System.currentTimeMillis();

		Date date = new Date();
		System.out.println("Start: " + date.toString());

		// JLabel jUserName = new
		// JLabel("Email to be Sent From (UP email only)");
		// JTextField userName = new JTextField();
		// JLabel jPassword = new JLabel("Password");
		// JTextField password = new JPasswordField();
		// Object[] ob = { jUserName, userName, jPassword, password };
		// int result = JOptionPane.showConfirmDialog(null, ob,
		// "Please input password for JOptionPane showConfirmDialog",
		// JOptionPane.OK_CANCEL_OPTION);
		//
		// String from = userName.getText();
		// String pass = password.getText();
		// Address[] addresses = null;
		// if (result == JOptionPane.OK_OPTION) {
		// from = userName.getText();
		// pass = password.getText();
		// // Here is some validation code
		//
		// JFrame frame2 = new JFrame("Emails to Send To");
		// String to = JOptionPane
		// .showInputDialog(frame2,
		// "What's email are you sending to (separate emails using spaces)?");
		// String[] token = to.split(" ");
		//
		// addresses = new Address[token.length];
		// try {
		// for (int i = 0; i < token.length; i++) {
		// addresses[i] = new InternetAddress(token[i]);
		// }
		// } catch (AddressException e) {
		// e.printStackTrace();
		// System.err.println("ERROR ON EMAIL EXCEPTION");
		// }
		// }

		tryGenLearningCurves();
		Date eDate = new Date();

		// if (result == JOptionPane.OK_OPTION) {
		// SendAttachmentInEmail email = new SendAttachmentInEmail();
		//
		// email.sendEmail(from, pass, addresses, G_WEIGHT, MIN_TRIES);
		// }

		System.out.println("End: " + eDate.toString());

		// TBD: REMOVE - PROFILING
		long overallTotalTime = System.currentTimeMillis()
				- MarzShortcutAgent.overallStartTime;
		System.out.println("TOTAL TIME SPENT: " + overallTotalTime + " ms");
		double percent = 100.0 * (double) MarzShortcutAgent.totalTime
				/ (double) overallTotalTime;
		System.out.println("Portion spent: " + MarzShortcutAgent.totalTime + " ms = "
				+ percent + "%");

		System.exit(0);
	}// main

}// MarzShortcutAgent