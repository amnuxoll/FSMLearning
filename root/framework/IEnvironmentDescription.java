package framework;

public interface IEnvironmentDescription {

    Move[] getMoves();

    int transition(int currentState, Move move);

    boolean isGoalState(int state);

    int getNumStates();

    void applySensors(int state, SensorData sensorData);
}
