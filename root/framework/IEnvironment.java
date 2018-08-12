package framework;

public interface IEnvironment {
    Move[] getMoves();
    SensorData tick(Move move);
    void reset();
}
