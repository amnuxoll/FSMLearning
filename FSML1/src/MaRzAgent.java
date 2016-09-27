import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * MaRzAgent Class
 * 
 * @author Christian Rodriguez
 * @author Giselle Marston
 * @version 1.0
 * @date 9/12/2016
 * 
 *       NOTE: Not Finished
 */
public class MaRzAgent extends Agent {

	/*---====CONSTANTS====---*/

	// minimum tries before a suffix node is expanded
	public static final int MIN_TRIES = 100;

	// the likeliness to jump back to another node
	// (should be in the range (0.0 - 1.0)
	public static final double G_WEIGHT = 0.2;

	// max size of list of nodes
	public static final int NODE_LIST_SIZE = 1000;

	/*---==== MEMBER VARIABLES ===---*/

	/** hash table of all nodes on the fringe of our search */
	HashMap<String, SuffixNode> hashFringe;

	/** this is the node we're currently using to search with */
	SuffixNode activeNode = null;

    /** each permutation has a number associated with it.  This is used to track
	  * the last permutation the agent tried.
      */
	int lastPermutationIndex = 0;

	/** the next sequence to consider testing (typically generated via
     * lastPermutationIndex */
	String nextSeqToTry = "a"; // 'a' is always safe because of how

	/**
	 * to print a status message every N milliseconds we need to track time
	 * elapsed
	 */
	long timeOfLastStatus = 0;

	/** for profiling: log total time spent in various code */
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
		/*--==Instance Variables==--*/
		public String suffix;   
		public String queueSeq; // if this node becomes active, start with this sequence
		public double heuristic;// the current overall potential of this suffix
		public int g;           // distance from root (ala A* search)

        /* indices into episodicMemory of successful/failed sequences with this suffix */
		public ArrayList<Integer> successIndexList;  
		public ArrayList<Integer> failsIndexList;

        /* the length of episodicMemory the last time the above lists were updated */
		public int indexOfLastEpisodeTried;

		/**
		 * SuffixNode default ctor inits variables for a root node.
         * 
         * NOTE: If creating a non-root node (@see #splitNode) these values will
		 * need to be initialized properly.  It can't be done in ctor without
		 * creating inefficiencies.
		 * 
		 */
		public SuffixNode() {
			this.suffix = "";
			this.queueSeq = "";
			this.heuristic = 0.0;
			this.g = 0;
			this.indexOfLastEpisodeTried = 0;
			this.successIndexList = new ArrayList<Integer>();
			this.failsIndexList = new ArrayList<Integer>();
		}// ctor


		/*
		 * toString
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String output = suffix;
			if (queueSeq != null) {
				output += "(" + queueSeq + ")";
			}

            int failedTries = failsIndexList.size();
            int tries = failedTries + successIndexList.size();
            
			return output + ":" + failedTries + "/" + tries;
		}
	}// SuffixNode Class

	/**
	 * MaRzAgent
	 * 
	 */
	public MaRzAgent() {
		hashFringe = new HashMap<String, MaRzAgent.SuffixNode>();
		
		// Create an empty root node and split it to create an initial fringe
		// that has a node for each letter in the alphabet
		SuffixNode initNode = new SuffixNode();
		hashFringe.put("", initNode);
        this.activeNode = initNode;
        splitNode();

	}// ctor

	/*
	 * exploreEnviroment
	 * 
	 * @see Agent#exploreEnvironment()
	 */
	@Override
	public void exploreEnvironment() {

		while (memory.length() < MAX_EPISODES && Successes <= NUM_GOALS) {

			// Initial node gets special treatment
			if (activeNode.suffix.length() == 0) {
				nextSeqToTry = "a";
				splitNode();
			}

			if (hashFringe.size() > NODE_LIST_SIZE) {
				SuffixNode worst = findWorstNodeToTry();
				hashFringe.remove(worst.suffix);
			}

			// System.out.println("FRINGE SIZE: " + hashFringe.size());

			if (nextSeqToTry.endsWith(activeNode.suffix)) {
				debugPrintln("Trying Sequence: " + nextSeqToTry);

				trySeq();

			} else {
				activeNode = hashFringe.get(this.nextSeqToTry);
				if (activeNode.queueSeq.equals("")) {
					activeNode.queueSeq = nextSeqToTry;
					debugPrintln("\nTrying Sequence: " + nextSeqToTry);
					// System.out.println("WE START HERE");
					trySeq();
				}
			}
			nextSeqToTry = nextPermutation();

		}// while

	}// exploreEnviroment

    /*a useful code snippet to save for later */
    /*
			double heuristic = 0.0;
			if (activeNode.successIndexList.size() + activeNode.failsIndexList.size() == 0) {
				heuristic = (activeNode.g * G_WEIGHT);
			} else {
				heuristic = (activeNode.failsIndexList.size() / (activeNode.successIndexList.size() + activeNode.failsIndexList.size()))
						+ (activeNode.g * G_WEIGHT);
			}
    */
    
	/**
	 * splitNode
	 * 
	 * Add new alphabet.length number of new nodes to fringe by replacing the
	 * current active node with a new node that prepends each letter to the
	 * active node's suffix.  success/fail values and similar are recalculated
	 * using the parent node's data.
     *
     * SIDE EFFECT:  the active node is removed from the fringe
     * PREREQ:  the active node's values are up to date
	 * 
	 */
	public void splitNode() {
        String parentSuffix = this.activeNode.suffix;
		debugPrintln("NODE TO BE SPLIT: " + parentSuffix);

        //Create the initial child nodes
        SuffixNode[] children = new SuffixNode[alphabet.length];
		for (int i = 0; i < alphabet.length; i++) {
            children[i] = new SuffixNode();
			children[i].suffix = alphabet[i] + parentSuffix;
			children[i].g = activeNode.g + 1;
			children[i].indexOfLastEpisodeTried = memory.length() - 1;

            hashFringe.put(children[i].suffix, children[i]);
        }

        //Divy up the parent's success list
        for(Integer indexObj : activeNode.successIndexList) {
            int index = indexObj.intValue() - 1;
            if (index < 0) continue;

            int childIdx = memory.charAt(index) - 'a';
            children[childIdx].successIndexList.add(new Integer(index));
        }

        //Divy up the parent's fails lists
        for(Integer indexObj : activeNode.failsIndexList) {
            int index = indexObj.intValue() - 1;
            if (index < 0) continue;

            int childIdx = memory.charAt(index) - 'a';
            if (episodicMemory.get(index).sensorValue == GOAL) {
                children[childIdx].successIndexList.add(new Integer(index));
            }
            else {
                children[childIdx].failsIndexList.add(new Integer(index));
            }
        }

        //Recalulate the children's heuristics
		for (int i = 0; i < alphabet.length; i++) {
            if (children[i].successIndexList.size() + children[i].failsIndexList.size() == 0) {
                children[i].heuristic = (children[i].g * G_WEIGHT);
            } else {
                children[i].heuristic = (children[i].failsIndexList.size()
                                         / (children[i].successIndexList.size()
                                            + children[i].failsIndexList.size()))
                                        + (children[i].g * G_WEIGHT);
            }
        }

	}// splitNode

	/**
	 * findBestNodeToTry
	 * 
	 * finds node with lowest heuristic
	 * 
	 * 9/7/16
	 * http://stackoverflow.com/questions/11420920/search-a-hashmap-in-an-
	 * arraylist-of-hashmap
	 * 
	 */

	public SuffixNode findBestNodeToTry() {
        SuffixNode[] nodes = (SuffixNode[])hashFringe.values().toArray();
        assert(nodes.length > 0);
        
        double theBEASTLIESTCombo = nodes[0].heuristic;
        SuffixNode bestNode = nodes[0];
        for(SuffixNode node : nodes)
        {
            if (node.heuristic > theBEASTLIESTCombo)
            {
                theBEASTLIESTCombo = node.heuristic;
                bestNode = node;
            }
        }
        
		return bestNode;

	}// findBestNodeToTry

	/**
	 * findWorstNode
	 * 
	 * finds node with smallest heuristic
	 * 
	 */

	public SuffixNode findWorstNodeToTry() {
        SuffixNode[] nodes = (SuffixNode[])hashFringe.values().toArray();
        assert(nodes.length > 0);
        
        double theBEASTLIESTCombo = nodes[0].heuristic;
        SuffixNode worstNode = nodes[0];
        for(SuffixNode node : nodes)
        {
            if (node.heuristic < theBEASTLIESTCombo)
            {
                theBEASTLIESTCombo = node.heuristic;
                worstNode = node;
            }
        }
        
		return worstNode;

	}// findWorstNodeToTry

	/**
	 * trySeq
	 * 
	 */
	public void trySeq() {

		// TBD: DEBUGGING
		long timeSince = System.currentTimeMillis() - timeOfLastStatus;
		if (timeSince > 500) {
			System.out.println("Successes: " + Successes);
			System.out.println("TRYING: " + nextSeqToTry);
			this.timeOfLastStatus = System.currentTimeMillis();
		}

        //Try the sequence until it fails
        String result = "";
        do
        {
            result = tryPath(nextSeqToTry);

            // Update the active node's success/fail lists and related based
            // upon whether we reached the goal or not.  Reaching the goal
            // before the suffix is reached is treated as neither a fail nor
            // success for heuristic purposes.  However, it is still an overall
            // success so the path will be repeated in this loop.
            if (result.equals("FAIL")) {
                activeNode.failsIndexList.add(new Integer(this.memory.length() - activeNode.suffix.length()));
            }
            else { //possible success
                int unusedLen = nextSeqToTry.length() - result.length();

                //Did we reach the suffix?
                if (unusedLen < activeNode.suffix.length()) {
                    activeNode.successIndexList.add(new Integer(this.memory.length() + unusedLen - activeNode.suffix.length()));
                }

                //If it succeeded before the suffix it's neither a success nor a
                //failure
            }//else

            //TODO:  Also update
            // the heuristic

		} while (!result.equals("FAIL"));

        //Check for split of active node
        int tries = activeNode.failsIndexList.size() + activeNode.successIndexList.size();
		if (tries >= MIN_TRIES) {
			splitNode();

			activeNode = findBestNodeToTry();

            //Use the new active node's queue sequence if it exists
			if (!activeNode.queueSeq.equals("")) {
				nextSeqToTry = activeNode.queueSeq;
			}
		}//if

	}// trySeq
	
	
	/**
	 * Timing Scripts
	 * 
	 * 	TBD: REMOVE - PROFILING
	 *	long startTime = System.currentTimeMillis();
	 *
	 *	TBD: REMOVE - PROFILING
	 *	long endTime = System.currentTimeMillis();
	 *	this.totalTime += endTime - startTime;
	 */
	
	

	/**
	 * updateTries
	 *
     * Updates the tries, failedTries, successIndex and failedIndex data in the
     * activeNode to be current with all entries added to episodicMemory since
     * the last update
	 */
	public void updateTries() {

        //Find all new instances of the active node's suffix
        int startIndex = activeNode.indexOfLastEpisodeTried - activeNode.suffix.length() + 1;
        ArrayList<Integer> newIndexes =
            getIndexOfSuffix(this.memory, startIndex, activeNode.suffix);

        //Categorize each instance as a success or failure
        for(Integer indexObj : newIndexes) {
            int index = indexObj.intValue();
            boolean fail = true;
            for(int i = 0; i < activeNode.suffix.length(); ++i) {
                if (episodicMemory.get(index + i).sensorValue == GOAL) {
                    fail = false;
                    break;
                }
            }

            if (fail) {
                activeNode.failsIndexList.add(indexObj);
            }
            else {
                activeNode.successIndexList.add(indexObj);
            }
        }

        activeNode.indexOfLastEpisodeTried = memory.length() - 1;
	}// updateTries

	/**
	 * getIndexOfSuffix
	 *
     * returns an list of the indexes into the string where a particular
     * subsequence (suffix) occurs after a given starting index.  In other
     * words, it's like a mass indexOf().
     *
     * CAVEAT:  caller is responsible for passing in reasonable values
	 */
	public ArrayList<Integer> getIndexOfSuffix(String memoryStr, int startIndex, String suffix) {
		ArrayList<Integer> indexOfSuffix = new ArrayList<Integer>();

        
		int index = memoryStr.indexOf(suffix, startIndex);
		while (index >= 0) {
			indexOfSuffix.add(index);
            startIndex += index + 1;
            if (startIndex >= memoryStr.length()) break;
            
			index = memory.indexOf(memoryStr, startIndex);
		}

		return indexOfSuffix;

	}// getIndexOfSuffix

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
			System.out
					.println("recordLearningCurve: Could not write to given csv file.");
			System.exit(-1);
		}

	}// recordLearningCurve

	public static void main(String[] args) {

		// TBD: REMOVE - PROFILING
		MaRzAgent.overallStartTime = System.currentTimeMillis();

		Date date = new Date();
		System.out.println("Start: " + date.toString());
		tryGenLearningCurves();
		Date eDate = new Date();
		System.out.println("End: " + eDate.toString());

		// TBD: REMOVE - PROFILING
		long overallTotalTime = System.currentTimeMillis()
				- MaRzAgent.overallStartTime;
		System.out.println("TOTAL TIME SPENT: " + overallTotalTime + " ms");
		double percent = 100.0 * (double) MaRzAgent.totalTime
				/ (double) overallTotalTime;
		System.out.println("Portion spent: " + MaRzAgent.totalTime + " ms = "
				+ percent + "%");
	}

}
