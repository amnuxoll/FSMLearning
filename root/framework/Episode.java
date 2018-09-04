package framework;

/**
 * An Episode describes a pairing of a {@link SensorData} and {@link Move} where the move was selected
 * as a result of the sensor data.
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public class Episode {
    private Move move;
    private SensorData sensorData;

    /**
     * Create an Episode.
     * @param sensorData The {@link SensorData} associated with the episode.
     * @param move The {@link Move} associated with the episode.
     */
    public Episode(SensorData sensorData, Move move) {
        if (move == null)
            throw new IllegalArgumentException("move cannot be null");
        this.sensorData = sensorData;
        this.move = move;
    }

    /**
     * Get the move for this episode.
     * @return The {@link Move} of the episode.
     */
    public Move getMove() {
        return this.move;
    }

    /**
     * Get the sensor data for this episode.
     * @return The {@link SensorData} of the episode.
     */
    public SensorData getSensorData() {
        return this.sensorData;
    }

    /**
     * Get whether or not this episode is the first in memory.
     * @return true if no {@link SensorData} accompanies the {@link Move}; otherwise false.
     */
    public boolean isFirstEpisode()
    {
        return this.sensorData == null;
    }

    /**
     * Determine if another object equals this {@link Episode}.
     * @param o The other object to compare with.
     * @return true if the objects are equal; otherwise false.
     */
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

    // TODO -- hashCode()
}
