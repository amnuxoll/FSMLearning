package framework;

import java.util.function.Supplier;

public class TestSuite {

    private int numberOfIterations;
    private int numberOfGoals;
    private Supplier<IAgent> agentSupplier;
    private Supplier<IEnvironment> environmentSupplier;
    private ITestRunSupplier testRunSupplier;
    private IResultWriter resultWriter;

    public TestSuite(int numberOfIterations, int numberOfGoals, IResultWriter resultWriter, Supplier<IAgent> agentSupplier, Supplier<IEnvironment> environmentSupplier, ITestRunSupplier testRunSupplier) {
        if (numberOfIterations < 1)
            throw new IllegalArgumentException("numberOfIterations must be greater than 0.");
        if (numberOfGoals < 1)
            throw new IllegalArgumentException("numberOfGoals must be greater than 0.");
        if (resultWriter == null)
            throw new IllegalArgumentException("resultWriter cannot be null.");
        if (agentSupplier == null)
            throw new IllegalArgumentException("agentSupplier cannot be null.");
        if (environmentSupplier == null)
            throw new IllegalArgumentException("environmentSupplier cannot be null.");
        if (testRunSupplier == null)
            throw new IllegalArgumentException("testRunSupplier cannot be null.");
        this.numberOfIterations = numberOfIterations;
        this.numberOfGoals = numberOfGoals;
        this.resultWriter = resultWriter;
        this.agentSupplier = agentSupplier;
        this.environmentSupplier = environmentSupplier;
        this.testRunSupplier = testRunSupplier;
    }

    public void run() {
        for (int i = 0; i < this.numberOfIterations; i++) {
            this.resultWriter.beginNewRun();
            IAgent agent = this.agentSupplier.get();
            IEnvironment environment = this.environmentSupplier.get();
            ITestRun testRun = this.testRunSupplier.GetTestRun(agent, environment, this.numberOfGoals, this.resultWriter);
            testRun.execute();
        }
        this.resultWriter.complete();
    }
}
