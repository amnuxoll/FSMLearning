package framework;

class Environment {
    private int currentState;

    protected IEnvironmentDescription environmentDescription;
    private IRandomizer randomizer;

    public Environment(IEnvironmentDescription environmentDescription, IRandomizer randomizer)
    {
        if (environmentDescription == null)
            throw new IllegalArgumentException("environmentDescription cannot be null");
        if (randomizer == null)
            throw new IllegalArgumentException("randomizer cannot be null");
        this.currentState = 0;
        this.environmentDescription = environmentDescription;
        this.randomizer = randomizer;
    }

    public Move[] getMoves() {
        return this.environmentDescription.getMoves();
    }

    public SensorData tick(Move move) {
        if (move == null)
            throw new IllegalArgumentException("move cannot be null");
        this.currentState = this.environmentDescription.transition(this.currentState, move);
        boolean hitGoal = this.environmentDescription.isGoalState(this.currentState);
        SensorData sensorData = new SensorData(hitGoal);
        this.environmentDescription.applySensors(this.currentState, sensorData);
        return sensorData;
    }

    public void reset() {
        int nonGoalStateCount = this.environmentDescription.getNumStates();
        this.currentState = this.randomizer.getRandomNumber(nonGoalStateCount);
    }

    public int getCurrentState()
    {
        return this.currentState;
    }
}