package agents.marz;

import framework.Episode;
import framework.Move;
import framework.SensorData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;


public class SuffixNode {
    // the likeliness to jump back to another node
    // (should be in the range (0.0 - 1.0)
    public static double G_WEIGHT = 0.05;

    /*--==Instance Variables==--*/
    public Sequence suffix;
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
     * SuffixNode default ctor inits variables for a root node.
     *
     * NOTE: If creating a non-root node (@see #splitNode) these values will
     * need to be initialized properly. It can't be done in ctor without
     * creating inefficiencies.
     *
     */
    public SuffixNode()
    {
        this.suffix = Sequence.EMPTY;
        this.queueSeq = 1;
        this.f = 0.0;
        this.g = 0;
        //this.indexOfLastEpisodeTried = 0;
        this.successIndexList = new ArrayList<Integer>();
        this.failsIndexList = new ArrayList<Integer>();
        this.tries = 0;
        this.failRate = 0.0;
        this.parentFailRate = 0.0;

    }// ctor

    /**
     * toString
     *
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "";
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

}
