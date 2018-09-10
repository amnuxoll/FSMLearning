package agents.marz;

import framework.Episode;
import framework.Move;
import framework.SensorData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.function.Function;


/**
 * SuffixNode
 * Represents a node in a suffix tree.
 *
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public class SuffixNode extends SuffixNodeBase<SuffixNode> {
    // the likeliness to jump back to another node
    // (should be in the range (0.0 - 1.0)
    private static final double G_WEIGHT = 0.05;

    /*--==Instance Variables==--*/
    public int queueSeq; // if this node becomes active, start with this
    // permutation
    private double f; // the current overall potential of this suffix (f = g + h)
    private int g; // distance from root (ala A* search)
    //public int tries; // number of times a sequence with this suffix has been tried
    private double failRate;  //[0.0..1.0] fraction of failed tries
    private double parentFailRate;  //save parent's fail rate to track my progress
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

    private Move[] possibleMoves;
    private Function<Integer, Episode> lookupEpisode;

    /**
     * SuffixNode default ctor inits variables for a root node.
     *
     * NOTE: If creating a non-root node (@see #splitNode) these values will
     * need to be initialized properly. It can't be done in ctor without
     * creating inefficiencies.
     *
     */
    public SuffixNode(Sequence sequence, Move[] possibleMoves, Function<Integer, Episode> lookupEpisode)
    {
        super(sequence);
        this.queueSeq = -1;
        this.f = 0.0;
        this.g = 0;
        //this.indexOfLastEpisodeTried = 0;
        this.successIndexList = new ArrayList<Integer>();
        this.failsIndexList = new ArrayList<Integer>();
        //this.tries = 0;
        this.failRate = 0.0;
        this.parentFailRate = 0.0;
        this.possibleMoves = possibleMoves;
        this.lookupEpisode = lookupEpisode;
    }// ctor

    @Override
    public SuffixNode[] split()
    {
        HashMap<Move, ArrayList<Integer>> childSuccesses = new HashMap<>();
        HashMap<Move, ArrayList<Integer>> childFailures = new HashMap<>();
        // Create the initial child nodes
        SuffixNode[] children = new SuffixNode[this.possibleMoves.length];
        for (int i = 0; i < this.possibleMoves.length; i++)
        {
            Move move = this.possibleMoves[i];
            children[i] = new SuffixNode(this.getSuffix().buildChildSequence(move), this.possibleMoves, this.lookupEpisode);
            children[i].g = this.g + 1;
            children[i].parentFailRate = this.failRate;
            childSuccesses.put(move, children[i].successIndexList);
            childFailures.put(move, children[i].failsIndexList);
        }// for

        //Divy the successes and failures among the children
        divyIndexes(childSuccesses, this.successIndexList);
        divyIndexes(childFailures, this.failsIndexList);

        // Do not split of the children aren't viable
        for (SuffixNode child : children)
        {
            if (child.failsIndexList.size() == 0)
                return null;
        }

        return children;
    }

    /**
     * updateHeuristic
     *
     * Recalculate this node's heuristic value (h) and overall value(f)
     */
    @Override
    protected void updateHeuristic()
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

    private void divyIndexes(HashMap<Move, ArrayList<Integer>> childLists, ArrayList<Integer> parentList)
    {
        for (Integer indexObj : parentList) {
            int index = indexObj.intValue() - 1;  //the -1 because child adds a letter
            //If we fall off the back of the epmem then it can't be matched
            if (index < 0)
                continue;

            Episode episode = this.lookupEpisode.apply(index);
            //If we've backed into the previous goal then we can't match either
            if ((this.getSuffix().getLength() > 0) && episode.getSensorData().isGoal()) {
                continue;
            }// if

            Move move = episode.getMove();
            childLists.get(move).add(new Integer(index));
        }// for
    }//divyIndexes

    /**
     * toString
     *
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        this.updateHeuristic();
        return this.getSuffix().toString() + "_" + this.f;
        //String output = suffix;
//        if (queueSeq > 1)
//        {
//            //output += "(q=" + nextPermutation(queueSeq+1) + ")";
//        }
//
//        int failedTries = failsIndexList.size();
//        int succTries = successIndexList.size();
//        int tries = failedTries + successIndexList.size();
//
//        updateHeuristic();
//        double truncatedG = (int)(g * G_WEIGHT * 100.0) / 100.0;  //trim to 2 decimal places
//        output = output + ":" + truncatedG + "+" + (failedTries) + "/" + (failedTries + succTries);
//        double truncatedHeur = (int)(f * 1000.0) / 1000.0;  //trim to 3 decimal places
//        output = output + "=" + truncatedHeur;
//        return output;
    }
}
