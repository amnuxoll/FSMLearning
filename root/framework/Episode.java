package framework;

public class Episode {

    private Move move;

    private SensorData sensorData;

    public Episode(SensorData sensorData, Move move) throws IllegalArgumentException
    {
        if (move == null)
            throw new IllegalArgumentException("move cannot be null");
        this.sensorData = sensorData;
        this.move = move;
    }

    public Move getMove()
    {
        return this.move;
    }

    public SensorData getSensorData() {
        return this.sensorData;
    }

    public boolean isFirstEpisode()
    {
        return this.sensorData == null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Episode)) {
            return false;
        }
        Episode episode = (Episode) o;
        if (this.sensorData == null && episode.sensorData != null)
            return false;
        if (this.sensorData != null && episode.sensorData == null)
            return false;
        if (this.sensorData != null && !this.sensorData.equals(episode.sensorData))
            return false;
        if (!this.move.equals(episode.move))
            return false;
        return true;
    }
}
