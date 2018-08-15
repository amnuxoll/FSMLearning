package framework;

public class ConsoleResultWriter implements IResultWriter {
    @Override
    public void logStepsToGoal(int goalNumber, int stepsToGoal) {
        System.out.println("Goal " + goalNumber + " completed in " + stepsToGoal + " steps.");
    }
}
