package agents.marz;

public abstract class SuffixNodeBase<TNodeType extends SuffixNodeBase<TNodeType>> {
    private Sequence suffix;
    private double f; // the current overall potential of this suffix (f = g + h)

    public SuffixNodeBase(Sequence suffix)
    {
        this.suffix = suffix;
    }

    public Sequence getSuffix() {
        return this.suffix;
    }

    public double getWeight()
    {
        this.updateHeuristic();
        return this.f;
    }

    public abstract TNodeType[] split();

    protected abstract void updateHeuristic();

}
