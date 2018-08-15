package framework;

public class TestRun implements ITestRun {

    private IAgent agent;

    private IEnvironment environment;

    private IResultWriter resultWriter;

    private int numGoalsToFind;

    public TestRun(IAgent agent, IEnvironment environment, int numGoalsToFind, IResultWriter resultWriter) throws IllegalArgumentException
    {
        this(agent, environment, numGoalsToFind);
        if (resultWriter == null)
            throw new IllegalArgumentException("resultWriter cannot be null");
        this.resultWriter = resultWriter;
    }

    public TestRun(IAgent agent, IEnvironment environment, int numGoalsToFind) throws IllegalArgumentException
    {
        if (agent == null)
            throw new IllegalArgumentException("agent cannot be null");
        if (environment == null)
            throw new IllegalArgumentException("environment cannot be null");
        if (numGoalsToFind < 1)
            throw new IllegalArgumentException("numGoalsToFind cannot be less than 1");
        this.agent = agent;
        this.environment = environment;
        this.numGoalsToFind = numGoalsToFind;
    }

    public void execute()
    {
        try {
            int goalCount = 0;
            int moveCount = 0;
            this.agent.setMoves(this.environment.getMoves());
            SensorData sensorData = null;
            do {
                Move move = this.agent.getNextMove(sensorData);
                sensorData = this.environment.tick(move);
                moveCount++;
                if (sensorData.isGoal()) {
                    if (this.resultWriter != null)
                        this.resultWriter.logStepsToGoal(goalCount, moveCount);
                    goalCount++;
                    moveCount = 0;
                    this.environment.reset();
                }
            } while (goalCount < this.numGoalsToFind);
        }
        catch (Exception ex)
        {

        }
    }

}
