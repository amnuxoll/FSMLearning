import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//import javax.mail.Address;
//import javax.mail.internet.AddressException;
//import javax.mail.internet.InternetAddress;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPasswordField;
//import javax.swing.JTextField;

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
	public static final int MIN_TRIES = 10;

	// the likeliness to jump back to another node
	// (should be in the range (0.0 - 1.0)
	public static final double G_WEIGHT = 0.001;

	// max size of list of nodes
	public static final int NODE_LIST_SIZE = 1000;

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

	/*
	 * Tracked the amount of tries performed by the active node, done for
	 * splitting purposes. Reset to 0 at time of choosing new activeNode.
	 */
	int triesDoneBeforeSplit = 0;

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
		public int queueSeq; // if this node becomes active, start with this
								// permutation
		public double heuristic;// the current overall potential of this suffix
		public int g; // distance from root (ala A* search)

		/*
		 * indices into episodicMemory of successful/failed sequences with this
		 * suffix
		 */
		public ArrayList<Integer> successIndexList;
		public ArrayList<Integer> failsIndexList;

		/*
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
		public SuffixNode() {
			this.suffix = "";
			this.queueSeq = 1;
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
			output += "(" + nextPermutation(queueSeq) + ")";

			int failedTries = failsIndexList.size();
			int tries = failedTries + successIndexList.size();

			output = output + ":" + failedTries + "/" + tries;
			output = output + "=" + heuristic;
			return output;
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
				activeNode = findBestNodeToTry();
			}
			
			//Erase worst node in the hashFringe once we hit our Constant limit
			if (hashFringe.size() > NODE_LIST_SIZE) {
				SuffixNode worst = findWorstNodeToTry();
				hashFringe.remove(worst.suffix);
			}

			if (nextSeqToTry.endsWith(activeNode.suffix)) {
				debugPrintln("Trying Sequence: " + nextSeqToTry);

				if (Successes <= NUM_GOALS) {
					trySeq();
				}

			} else {

				// int charIndex = nextSeqToTry.length() - 2;
				// String key = "" + nextSeqToTry.charAt(charIndex + 1);
				// while (!hashFringe.containsKey(key)) {
				// key = nextSeqToTry.charAt(charIndex) + key;
				// charIndex--;
				// if (charIndex == 0) break; //should never happen
				// }
				// SuffixNode node = hashFringe.get(key);
				// if (node.queueSeq == 1)
				// {
				// node.queueSeq = lastPermutationIndex - 1;
				// }

				// TODO: FIX THIS!! See code above might fix it?
				for (Map.Entry<String, SuffixNode> entry : hashFringe
						.entrySet()) {

					if (nextSeqToTry.endsWith(entry.getKey())) {
						SuffixNode node = hashFringe.get(entry.getKey());
						if (node.queueSeq == 1) {
							node.queueSeq = lastPermutationIndex - 1;
						}
						break;
					}

				}// for
			}// else

			nextSeqToTry = nextPermutation();

		}// while

	}// exploreEnviroment

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
	public void splitNode() {
		String parentSuffix = this.activeNode.suffix;
		debugPrintln("NODE TO BE SPLIT: " + activeNode);

		// Create the initial child nodes
		SuffixNode[] children = new SuffixNode[alphabet.length];
		for (int i = 0; i < alphabet.length; i++) {
			children[i] = new SuffixNode();
			children[i].suffix = alphabet[i] + parentSuffix;
			children[i].g = activeNode.g + 1;
			children[i].indexOfLastEpisodeTried = memory.length() - 1;

			hashFringe.put(children[i].suffix, children[i]);
		}

		// Divy up the parent's success list
		for (Integer indexObj : activeNode.successIndexList) {
			int index = indexObj.intValue() - 1;
			if (index < 0)
				continue;

			int childIdx = memory.charAt(index) - 'a';
			children[childIdx].successIndexList.add(new Integer(index));
		}

		// Divy up the parent's fails lists
		for (Integer indexObj : activeNode.failsIndexList) {
			int index = indexObj.intValue() - 1;
			if (index < 0)
				continue;

			int childIdx = memory.charAt(index) - 'a';
			if (episodicMemory.get(index).sensorValue == GOAL) {
				children[childIdx].successIndexList.add(new Integer(index));
			} else {
				children[childIdx].failsIndexList.add(new Integer(index));
			}
		}

		// Recalculate the children's heuristics
		for (int i = 0; i < alphabet.length; i++) {

			updateHeuristic(children[i]);
		}

		hashFringe.remove(activeNode.suffix);

	}// splitNode

	/*
	 * updateHeuristic
	 */
	public void updateHeuristic(SuffixNode theNode) {
		// Recalculate this node's heuristics

		if (theNode.successIndexList.size() + theNode.failsIndexList.size() == 0) {
			theNode.heuristic = ((double) theNode.g * (double) G_WEIGHT);
		} else {
			theNode.heuristic = (((double) theNode.failsIndexList.size() / (double) (theNode.successIndexList
					.size() + (double) theNode.failsIndexList.size())) + ((double) theNode.g * (double) G_WEIGHT));
		}

	}

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

		SuffixNode[] nodes = (SuffixNode[]) hashFringe.values().toArray(
				new SuffixNode[hashFringe.size()]);
		assert (nodes.length > 0);

		double theBEASTLIESTCombo = nodes[0].heuristic;
		SuffixNode bestNode = nodes[0];
		for (SuffixNode node : nodes) {
			if (node.heuristic < theBEASTLIESTCombo) {
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
		SuffixNode[] nodes = (SuffixNode[]) hashFringe.values().toArray(
				new SuffixNode[hashFringe.size()]);
		assert (nodes.length > 0);

		double theBEASTLIESTCombo = nodes[0].heuristic;
		SuffixNode worstNode = nodes[0];
		for (SuffixNode node : nodes) {
			if (node.heuristic > theBEASTLIESTCombo) {
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

		// System.out.println("Successes: " + Successes);
		// System.out.println("TRYING: " + nextSeqToTry);

		// Try the sequence until it fails
		String result = "";
		do {
			// System.out.println("Successes: " + Successes);

			result = tryPath(nextSeqToTry);

			// Update the active node's success/fail lists and related based
			// upon whether we reached the goal or not. Reaching the goal
			// before the suffix is reached is treated as neither a fail nor
			// success for heuristic purposes. However, it is still an overall
			// success so the path will be repeated in this loop.
			if (result.equals("FAIL")) {
				activeNode.failsIndexList.add(new Integer(this.memory.length()
						- activeNode.suffix.length()));
				updateHeuristic(activeNode);
			} else { // possible success
				int unusedLen = nextSeqToTry.length() - result.length();

				// Did we reach the suffix?
				if (unusedLen < activeNode.suffix.length()) {
					activeNode.successIndexList
							.add(new Integer(this.memory.length() + unusedLen
									- activeNode.suffix.length()));
					updateHeuristic(activeNode);
				}

				// If it succeeded before the suffix it's neither a success nor
				// a
				// failure
			}// else

			triesDoneBeforeSplit++;

		} while (!result.equals("FAIL") && memory.length() < MAX_EPISODES
				&& Successes <= NUM_GOALS);

		// Check for split of active node
		if (triesDoneBeforeSplit >= MIN_TRIES && memory.length() < MAX_EPISODES
				&& Successes <= NUM_GOALS) {
			splitNode();
			activeNode = findBestNodeToTry();
			triesDoneBeforeSplit = 0;

			debugPrintln("New active node: " + activeNode);

			// Use the new active node's queue sequence if it exists
			if (activeNode.queueSeq > 1) {
				lastPermutationIndex = activeNode.queueSeq;
			}

		}

		// }// if

	}// trySeq

	/**
	 * Timing Scripts
	 * 
	 * TBD: REMOVE - PROFILING long startTime = System.currentTimeMillis();
	 * 
	 * TBD: REMOVE - PROFILING long endTime = System.currentTimeMillis();
	 * this.totalTime += endTime - startTime;
	 */

	/**
	 * updateTries
	 * 
	 * Updates the tries, failedTries, successIndex and failedIndex data in the
	 * activeNode to be current with all entries added to episodicMemory since
	 * the last update
	 */
	public void updateTries() {

		// Find all new instances of the active node's suffix
		int startIndex = activeNode.indexOfLastEpisodeTried
				- activeNode.suffix.length() + 1;
		ArrayList<Integer> newIndexes = getIndexOfSuffix(this.memory,
				startIndex, activeNode.suffix);

		// Categorize each instance as a success or failure
		for (Integer indexObj : newIndexes) {
			int index = indexObj.intValue();
			boolean fail = true;
			for (int i = 0; i < activeNode.suffix.length(); ++i) {
				if (episodicMemory.get(index + i).sensorValue == GOAL) {
					fail = false;
					break;
				}
			}

			if (fail) {
				activeNode.failsIndexList.add(indexObj);
			} else {
				activeNode.successIndexList.add(indexObj);
			}
		}

		activeNode.indexOfLastEpisodeTried = memory.length() - 1;
	}// updateTries

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
			int startIndex, String suffix) {
		ArrayList<Integer> indexOfSuffix = new ArrayList<Integer>();

		int index = memoryStr.indexOf(suffix, startIndex);
		while (index >= 0) {
			indexOfSuffix.add(index);
			startIndex += index + 1;
			if (startIndex >= memoryStr.length())
				break;

			index = memory.indexOf(memoryStr, startIndex);
		}

		return indexOfSuffix;

	}// getIndexOfSuffix

	/** TODO: Comment please */
	public String nextPermutation(int index) {
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
	 * nextPermutation
	 * 
	 * @return
	 */
	public String nextPermutation() {
		lastPermutationIndex++;
		return nextPermutation(lastPermutationIndex);
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
				- MaRzAgent.overallStartTime;
		System.out.println("TOTAL TIME SPENT: " + overallTotalTime + " ms");
		double percent = 100.0 * (double) MaRzAgent.totalTime
				/ (double) overallTotalTime;
		System.out.println("Portion spent: " + MaRzAgent.totalTime + " ms = "
				+ percent + "%");
		System.exit(0);
	}

}