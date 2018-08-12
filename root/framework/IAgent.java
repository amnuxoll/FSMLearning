package framework;

public interface IAgent {
    void setMoves(Move[] moves);
    Move getNextMove(SensorData sensorData);
}
