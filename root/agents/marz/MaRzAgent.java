package agents.marz;

import framework.*;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import javax.xml.transform.Templates;

import static java.lang.Math.floor;
import static java.lang.Math.log;
//test comment

/**
 * MaRzAgent Class
 *
 * @author Christian Rodriguez
 * @author Giselle Marston
 * @author Andrew Nuxoll
 * @version 3.0
 *
 */
public class MaRzAgent implements IAgent
{
	private static final int NODE_LIST_SIZE = 10000;

	/*---==== MEMBER VARIABLES ===---*/

//	/** hash table of all nodes on the fringe of our search */
//	HashMap<Sequence, SuffixNode> hashFringe = new HashMap<Sequence, SuffixNode>();

	/** this is the node we're currently using to search with */
	SuffixNode activeNode = null;

	/**
	 * each permutation has a number associated with it. This is used to track
	 * the last permutation the agent tried.
	 */
	int lastPermutationIndex = 0;// set to 1 because we hard coded the first
	// permutation to be 'a'

	/**
	 * the next sequence to consider testing (typically generated via
	 * lastPermutationIndex
	 */
	Sequence currentSequence = null; // 'a' is always safe because of how

	/**
	 * the last sequence that was successful (used for reporting and not
	 * required for the algorithm)
	 */
	Sequence lastSuccessfulSequence = currentSequence;

	private SequenceGenerator sequenceGenerator;


	//Instance Variables
	protected Move[] alphabet;
	protected ArrayList<Episode> episodicMemory = new ArrayList<Episode>();
	protected int currIndex = 0;

	/** Number of episodes per run */
	public static final int MAX_EPISODES = 2000000;
	public static final int NUM_GOALS = 1000;
	/** Number of state machines to test a given constant combo with */
	public static final int NUM_MACHINES = 50 ;

	/** Turn this on to print debugging messages */
	public static boolean debug = false;
	/** println for debug messages only */
	public static void debugPrintln(String s) { if (debug) System.out.println(s); }

	/**
	 * MaRzAgent
	 *
	 */
	public MaRzAgent()
	{
	}// ctor

	private SuffixTree suffixTree = new SuffixTree();

	@Override
	public void initialize(Move[] moves)
	{
		this.alphabet = moves;
		this.sequenceGenerator = new SequenceGenerator(this.alphabet);
		this.currentSequence = this.nextPermutation();
		this.activeNode = new SuffixNode();
		this.activeNode.suffix = this.currentSequence;
		this.suffixTree.addSuffixNode(this.activeNode);
	}

	@Override
	public Move getNextMove(SensorData sensorData)
	{
		if (episodicMemory.size() > 0)
			episodicMemory.get(episodicMemory.size() - 1).setSensorData(sensorData);
		if (sensorData == null)
		{
			// Very beginning of time so we need to select our very first sequence
//			this.currentSequence = this.nextPermutation();
		}
		else if (sensorData.isGoal())
		{
			this.markSuccess(this.currentSequence.hasNext());
			// if the sequence succeeded then try again!
			this.currentSequence.reset();
		}
		else if (!this.currentSequence.hasNext())
		{
			this.markFailure();
			this.updateNextSequenceToTry();
		}
		Move nextMove = this.currentSequence.next();
		episodicMemory.add(new Episode(nextMove));
		return nextMove;
	}

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
		Sequence sequence = Sequence.EMPTY;
		while (!this.suffixTree.containsSequence(sequence))
		{
			Episode ep = episodicMemory.get(index);

			//if we back into the previous goal without finding a key then there is no match
			if ((sequence.getLength() > 0) && (ep.getSensorData().isGoal()))
				return null;

			sequence = sequence.buildChildSequence(ep.getMove());
			index--;

			//don't fall off the end of the memory
			if (index < 0)
				return null;
		}// while

		return this.suffixTree.findBestMatch(sequence);

	}//findNodeForIndex

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
		SuffixNode[] children = this.splitActiveNode();

		// Recalculate the children's heuristics
		for (int i = 0; i < this.alphabet.length; i++)
		{
			//if the child's suffix has never been tried, then it's too soon:
			//abort this split!
			if (children[i].failsIndexList.size() == 0) return;
		}//for

		//Ready to commit:  add the children to the fringe and remove the parent
		for (int i = 0; i < alphabet.length; i++)
		{
			this.suffixTree.addSuffixNode(children[i]);
		}// for
		this.suffixTree.removeSuffixNode(activeNode);
	}// splitNode

	private void markFailure() {
		this.activeNode.tries++;
		this.activeNode.failsIndexList.add(this.episodicMemory.size() - this.activeNode.suffix.getLength());
		// The active node is split once it's found a successful sequence but
		// that sequence eventually failed.
		if (this.activeNode.goalFound) {
			this.splitNode();
			SuffixNode newBestNode = this.suffixTree.findBestNodeToTry();
			this.setActiveNode(newBestNode);
		}// if
	}

	private void markSuccess(boolean isPartialSuccess)
	{
		this.activeNode.tries++;
		this.lastSuccessfulSequence = this.currentSequence;
		if (isPartialSuccess)
		{
			SuffixNode node = this.findNodeForIndex(this.episodicMemory.size() - 1);
			if (node != null) {
				int index = this.episodicMemory.size() - node.suffix.getLength();
				node.successIndexList.add(index);
			}
		} else {
			this.activeNode.successIndexList.add(this.episodicMemory.size() - this.activeNode.suffix.getLength());
			this.activeNode.goalFound = true;
		}
	}

	/**
	 * nextPermutation
	 *
	 * increments nextSeqToTry
	 */
	public Sequence nextPermutation()
	{
		this.lastPermutationIndex++;
		return this.sequenceGenerator.nextPermutation(lastPermutationIndex);
	}// nextPermutation

	private void setActiveNode(SuffixNode newActiveNode)
	{
		this.activeNode = newActiveNode;
		// Use the new active node's queue sequence if it exists
		if (this.activeNode.queueSeq > 1) {
			this.lastPermutationIndex = this.activeNode.queueSeq;
			this.activeNode.queueSeq = 1;
		}// if
	}

	private void updateNextSequenceToTry()
	{
		if (this.currentSequence.endsWith(this.activeNode.suffix))
		{
			//check to see if another node would be better now
			SuffixNode newBestNode = this.suffixTree.findBestNodeToTry();

			if (newBestNode != this.activeNode) {
				this.activeNode.queueSeq = 1;
				this.setActiveNode(newBestNode);
			}
		}// if
		else  //sequence's suffix did not match active node
		{
			//If this non-active node doesn't have a queueSeq yet, set it
			SuffixNode node = this.suffixTree.findBestMatch(currentSequence);
			if ((node != null) && (node.queueSeq == 1))
			{
				node.queueSeq = lastPermutationIndex - 1;
			}// if
		}// else

		currentSequence = nextPermutation();
	}

	public SuffixNode[] splitActiveNode()
	{
		HashMap<Move, ArrayList<Integer>> childSuccesses = new HashMap<>();
		HashMap<Move, ArrayList<Integer>> childFailures = new HashMap<>();
		// Create the initial child nodes
		SuffixNode[] children = new SuffixNode[this.alphabet.length];
		for (int i = 0; i < this.alphabet.length; i++)
		{
			Move move = this.alphabet[i];
			children[i] = new SuffixNode();
			children[i].suffix = this.activeNode.suffix.buildChildSequence(move);
			children[i].g = this.activeNode.g + 1;
			children[i].parentFailRate = this.activeNode.failRate;
			childSuccesses.put(move, children[i].successIndexList);
			childFailures.put(move, children[i].failsIndexList);
		}// for

		//Divy the successes and failures among the children
		divyIndexes(childSuccesses, this.activeNode.successIndexList);
		divyIndexes(childFailures, this.activeNode.failsIndexList);

		return children;
	}

	protected void divyIndexes(HashMap<Move, ArrayList<Integer>> childLists, ArrayList<Integer> parentList)
	{
		for (Integer indexObj : parentList) {
			int index = indexObj.intValue() - 1;  //the -1 because child adds a letter

			//If we fall off the back of the epmem then it can't be matched
			//OR If we've backed into the previous goal then we can't match either
			if (index < 0 || ((this.activeNode.suffix.getLength() > 0) && this.episodicMemory.get(index).getSensorData().isGoal())) {
				continue;
			}// if

			Move move = episodicMemory.get(index).getMove();
			childLists.get(move).add(new Integer(index));
		}// for
	}//divyIndexes

}// MaRzAgent
