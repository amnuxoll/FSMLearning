package framework;

@FunctionalInterface
public interface ITestRunSupplier {
    ITestRun GetTestRun(IAgent agent, IEnvironment environment, int numGoals, IResultWriter resultWriter);
}
