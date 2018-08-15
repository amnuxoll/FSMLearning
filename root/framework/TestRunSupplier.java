package framework;

@FunctionalInterface
public interface TestRunSupplier {
    ITestRun GetTestRun(IAgent agent, IEnvironment environment, int numGoals, IResultWriter resultWriter);
}
