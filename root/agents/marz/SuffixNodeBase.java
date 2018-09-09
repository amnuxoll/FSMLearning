package agents.marz;

/**
 * SuffixNodeBase
 * Represents a node in a suffix tree.
 *
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public abstract class SuffixNodeBase<TNodeType extends SuffixNodeBase<TNodeType>> {
    private Sequence suffix;
    private double f; // the current overall potential of this suffix (f = g + h)

    /**
     * Create an instance of a SuffixNodeBase
     * @param suffix The suffix for this node.
     */
    public SuffixNodeBase(Sequence suffix)
    {
        this.suffix = suffix;
    }

    /**
     * Gets the suffix for this node.
     * @return The sequence that contains the suffix of this node.
     */
    public Sequence getSuffix() {
        return this.suffix;
    }

    /**
     * Gets the weight of this node.
     * @return The weight value as a double of this node.
     */
    public double getWeight()
    {
        this.updateHeuristic();
        return this.f;
    }

    /**
     * Splits this node out into its children.
     * @return The set of child nodes.
     */
    public abstract TNodeType[] split();

    /**
     * Recalculates the weight of this Node.
     */
    protected abstract void updateHeuristic();

}
