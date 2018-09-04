package framework;

public class TestSuiteConfiguration {
    private int numberOfIterations;
    private int numberOfGoals;
    private boolean trueRandom;

    public TestSuiteConfiguration(int numberOfIterations, int numberOfGoals, boolean trueRandom)
    {
        if (numberOfIterations < 1)
            throw new IllegalArgumentException("numberOfIterations cannot be less than 1.");
        if (numberOfGoals < 1)
            throw new IllegalArgumentException("numberOfGoals cannot be less than 1.");
        this.numberOfIterations = numberOfIterations;
        this.numberOfGoals = numberOfGoals;
        this.trueRandom = trueRandom;
    }

    public int getNumberOfIterations()
    {
        return this.numberOfIterations;
    }

    public int getNumberOfGoals()
    {
        return this.numberOfGoals;
    }

    public boolean getTrueRandom()
    {
        return this.trueRandom;
    }
}
