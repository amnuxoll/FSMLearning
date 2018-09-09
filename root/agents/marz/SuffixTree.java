package agents.marz;

import java.util.*;

public class SuffixTree<TSuffixNode extends SuffixNodeBase<TSuffixNode>> {

    /** hash table of all nodes on the fringe of our search */
    private HashMap<Sequence, TSuffixNode> hashFringe = new HashMap<Sequence, TSuffixNode>();

    private int maxSize;

    public SuffixTree(int maxSize, TSuffixNode rootNode)
    {
        if (maxSize < 1)
            throw new IllegalArgumentException("maxSize must be greater than 0");
        if (rootNode == null)
            throw new IllegalArgumentException("rootNode cannot be null");
        this.maxSize = maxSize;
        this.addNode(rootNode);
    }

    public boolean splitSuffix(Sequence sequence)
    {
        if (sequence == null)
            throw new IllegalArgumentException("sequence cannot be null");
        TSuffixNode node = this.hashFringe.get(sequence);
        if (node == null)
            return false;
        TSuffixNode[] children = node.split();

        if (children == null)
            return false;

        //Ready to commit:  add the children to the fringe and remove the parent
        for (int i = 0; i < children.length; i++)
        {
            this.addNode(children[i]);
        }// for

        this.hashFringe.remove(node.getSuffix());
        return true;
    }

    /**
     * findBestNodeToTry
     *
     * finds node with lowest heuristic
     */
    public TSuffixNode findBestNodeToTry()
    {
        double bestWeight = Double.MAX_VALUE;
        TSuffixNode bestNode = null;
        for (TSuffixNode node : this.hashFringe.values())
        {
            double nodeWeight = node.getWeight();
            if (nodeWeight < bestWeight)
            {
                bestWeight = nodeWeight;
                bestNode = node;
            }
        }
        return bestNode;
    }// findBestNodeToTry

    public TSuffixNode findBestMatch(Sequence sequence)
    {
        Sequence bestMatch = null;
        int index = 0;
        Sequence subsequence;
        do {
            subsequence = sequence.getSubsequence(index++);
            for (Sequence suffixKey : this.hashFringe.keySet())
            {
                if (subsequence.endsWith(suffixKey) && (bestMatch == null || suffixKey.getLength() >  bestMatch.getLength()))
                    bestMatch = suffixKey;
            }
        } while (index < sequence.getLength());

        if (bestMatch == null)
            return null;
        return this.hashFringe.get(bestMatch);
    }

    public boolean containsSuffix(Sequence suffix)
    {
        if (suffix == null)
            throw new IllegalArgumentException("suffix cannot be null");
        return this.hashFringe.containsKey(suffix);
    }

    private void addNode(TSuffixNode node)
    {
        // Erase worst node in the hashFringe once we hit our Constant limit
        while (hashFringe.size() > this.maxSize)
        {
            Sequence worstSequence = this.findWorstNodeToTry();
            hashFringe.remove(worstSequence);
        }// if
        this.hashFringe.put(node.getSuffix(), node);
    }

    private Sequence findWorstNodeToTry()
    {
        double worstWeight = Double.MIN_VALUE;
        Sequence worstSequence = null;
        for (Sequence sequence : this.hashFringe.keySet())
        {
            TSuffixNode node = this.hashFringe.get(sequence);
            double nodeWeight = node.getWeight();
            if (nodeWeight > worstWeight)
            {
                worstWeight = nodeWeight;
                worstSequence = sequence;
            }
        }
        return worstSequence;
    }// findWorstNodeToTry

}
