package framework;

import java.util.EventObject;

class GoalEvent extends EventObject {
    private int stepCountToGoal;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public GoalEvent(Object source, int stepCountToGoal) {
        super(source);
        this.stepCountToGoal = stepCountToGoal;
    }

    public int getStepCountToGoal()
    {
        return this.stepCountToGoal;
    }
}
