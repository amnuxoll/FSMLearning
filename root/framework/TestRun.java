package framework;

class TestRun {

    private IAgent agent;
    private IEnvironmentDescription environmentDescription;
    private int numberOfGoalsToFind;
    private IResultWriter resultWriter;
    private IRandomizer randomizer;

    public TestRun(IAgent agent, IEnvironmentDescription environmentDescription, int numberOfGoalsToFind, IResultWriter resultWriter, IRandomizer randomizer) throws IllegalArgumentException
    {
        if (agent == null)
            throw new IllegalArgumentException("agent cannot be null");
        if (environmentDescription == null)
            throw new IllegalArgumentException("environmentDescription cannot be null");
        if (numberOfGoalsToFind < 1)
            throw new IllegalArgumentException("numberOfGoalsToFind cannot be less than 1");
        if (resultWriter == null)
            throw new IllegalArgumentException("resultWriter cannot be null");
        if (randomizer == null)
            throw new IllegalArgumentException("randomizer cannot be null");
        this.agent = agent;
        this.environmentDescription = environmentDescription;
        this.numberOfGoalsToFind = numberOfGoalsToFind;
        this.resultWriter = resultWriter;
        this.randomizer = randomizer;
    }

    public void execute()
    {
        try {
            int goalCount = 0;
            int moveCount = 0;
            this.agent.setMoves(this.environmentDescription.getMoves());
            Environment environment = new Environment(this.environmentDescription, this.randomizer);
            SensorData sensorData = null;
            do {
                Move move = this.agent.getNextMove(sensorData);
                sensorData = environment.tick(move);
                moveCount++;
                if (sensorData.isGoal()) {
                    resultWriter.logStepsToGoal(moveCount);
                    goalCount++;
                    moveCount = 0;
                    environment.reset();
                }
            } while (goalCount < this.numberOfGoalsToFind);
        }
        catch (Exception ex)
        {

        }
    }

}
