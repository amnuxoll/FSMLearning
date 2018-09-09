package framework;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
class TestRun {

    private IAgent agent;
    private IEnvironmentDescription environmentDescription;
    private int numberOfGoalsToFind;
    private IRandomizer randomizer;

    private List<IGoalListener> goalListeners = new ArrayList();

    public TestRun(IAgent agent, IEnvironmentDescription environmentDescription, int numberOfGoalsToFind, IRandomizer randomizer) throws IllegalArgumentException
    {
        if (agent == null)
            throw new IllegalArgumentException("agent cannot be null");
        if (environmentDescription == null)
            throw new IllegalArgumentException("environmentDescription cannot be null");
        if (numberOfGoalsToFind < 1)
            throw new IllegalArgumentException("numberOfGoalsToFind cannot be less than 1");
        if (randomizer == null)
            throw new IllegalArgumentException("randomizer cannot be null");
        this.agent = agent;
        this.environmentDescription = environmentDescription;
        this.numberOfGoalsToFind = numberOfGoalsToFind;
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
                //System.out.print(move + " -> " + sensorData + "; ");
                moveCount++;
                if (sensorData.isGoal()) {
                    this.fireGoalEvent(moveCount);
                    goalCount++;
                    moveCount = 0;
                    environment.reset();
                }
            } while (goalCount < this.numberOfGoalsToFind);
        }
        catch (Exception ex)
        {
            System.out.println("TestRun failed with exception: " + ex.getMessage());
        }
    }

    public synchronized void addGoalListener(IGoalListener listener)
    {
        this.goalListeners.add(listener);
    }

    public synchronized void removeGoalListener(IGoalListener listener)
    {
        this.goalListeners.remove(listener);
    }

    private synchronized void fireGoalEvent(int stepsToGoal)
    {
        GoalEvent goal = new GoalEvent(this, stepsToGoal);
        for (IGoalListener listener : this.goalListeners)
        {
            listener.goalReceived(goal);
        }
    }
}
