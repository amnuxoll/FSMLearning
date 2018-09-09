package framework;

/**
 *
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public class TestSuiteConfiguration {
    private int numberOfIterations;
    private int numberOfGoals;
    private boolean trueRandom;

    public static final TestSuiteConfiguration QUICK = new TestSuiteConfiguration(10, 100, true);
    public static final TestSuiteConfiguration MEDIUM = new TestSuiteConfiguration(25, 500, true);
    public static final TestSuiteConfiguration FULL = new TestSuiteConfiguration(50, 1000, true);

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
