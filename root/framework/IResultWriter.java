package framework;

public interface IResultWriter {

    void beginNewRun();

    void logStepsToGoal(int stepsToGoal);

    void complete();
}
