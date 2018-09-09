package agents.marz;

import java.util.HashMap;

public class SuffixTree {

    /** hash table of all nodes on the fringe of our search */
    private HashMap<Sequence, SuffixNode> hashFringe = new HashMap<Sequence, SuffixNode>();

    /**
     * findBestNodeToTry
     *
     * finds node with lowest heuristic
     */
    public SuffixNode findBestNodeToTry()
    {
        SuffixNode[] nodes = this.hashFringe.values().toArray(new SuffixNode[0]);
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
        SuffixNode[] nodes = this.hashFringe.values().toArray(new SuffixNode[0]);
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

    public SuffixNode findBestMatch(Sequence sequence)
    {
        int charIndex = sequence.getLength() - 1;
        Sequence subsequence = sequence.getSubsequence(charIndex--);
        while (! hashFringe.containsKey(subsequence))
        {
            if (charIndex == -1)
            {
                //Example of how this result can be reached:
                //  given path is "ac" and fringe has keys "aac", "bac" and "cac"
                return null;
            }// if

            subsequence = sequence.getSubsequence(charIndex--);
        }// while

        return hashFringe.get(subsequence);
    }

    public boolean containsSequence(Sequence sequence)
    {
        return this.hashFringe.containsKey(sequence);
    }

    public void addSuffixNode(SuffixNode node)
    {
        this.clearHashFringe();
        this.hashFringe.put(node.suffix, node);
    }

    public void removeSuffixNode(SuffixNode node)
    {
        this.hashFringe.remove(node);
    }

    private final static int NODE_LIST_SIZE = 10000;
    private void clearHashFringe()
    {
        // Erase worst node in the hashFringe once we hit our Constant limit
        while (hashFringe.size() > NODE_LIST_SIZE)
        {
            SuffixNode worst = this.findWorstNodeToTry();
            hashFringe.remove(worst.suffix);
        }// if
    }
}
