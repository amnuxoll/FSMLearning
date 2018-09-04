package framework;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EpisodeTest {

    // constructor Tests
    @Test
    public void testConstructorNullMoveThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Episode(new SensorData(false), null));
    }

    // getMove Tests
    @Test
    public void testGetMove()
    {
        Episode ep = new Episode(new SensorData(false), new Move("move"));
        Move move = ep.getMove();
        assertEquals("move", move.getName());
    }

    // getSensorData Tests
    @Test
    public void testGetSensorData()
    {
        Episode ep = new Episode(new SensorData(false), new Move("move"));
        SensorData sensorData = ep.getSensorData();
        assertFalse(sensorData.isGoal());
    }

    // isFirstEpisodeTests
    @Test
    public void testIsFirstEpisodeNullSensorDataIndicatesStartOfMemory()
    {
        Episode episode = new Episode(null, new Move("move"));
        assertTrue(episode.isFirstEpisode());
    }

    public void testIsFirstEpisodeNonNullSensorDataIndicatesNotStartOfMemory()
    {
        Episode episode = new Episode(new SensorData(false), new Move("move"));
        assertFalse(episode.isFirstEpisode());
    }

    // equals Tests
    @Test
    public void testEqualsAreEqualNullSensorData()
    {
        Episode episode1 = new Episode(null, new Move("move"));
        Episode episode2 = new Episode(null, new Move("move"));
        assertEquals(episode1, episode2);
    }

    @Test
    public void testEqualsAreEqualNotNullSensorData()
    {
        Episode episode1 = new Episode(new SensorData(true), new Move("move"));
        Episode episode2 = new Episode(new SensorData(true), new Move("move"));
        assertEquals(episode1, episode2);
    }

    @Test
    public void testEqualsAreNotEqualDifferentSensorData()
    {
        Episode episode1 = new Episode(new SensorData(false), new Move("move"));
        Episode episode2 = new Episode(new SensorData(true), new Move("move"));
        assertNotEquals(episode1, episode2);
    }

    @Test
    public void testEqualsAreNotEqualDifferentMove()
    {
        Episode episode1 = new Episode(new SensorData(true), new Move("move1"));
        Episode episode2 = new Episode(new SensorData(true), new Move("move2"));
        assertNotEquals(episode1, episode2);
    }

    @Test
    public void testEqualsAreNotEqualLeftHasNullSensorData()
    {
        Episode episode1 = new Episode(null, new Move("move"));
        Episode episode2 = new Episode(new SensorData(true), new Move("move"));
        assertNotEquals(episode1, episode2);
    }

    @Test
    public void testEqualsAreNotEqualRightHasNullSensorData()
    {
        Episode episode1 = new Episode(new SensorData(true), new Move("move"));
        Episode episode2 = new Episode(null, new Move("move"));
        assertNotEquals(episode1, episode2);
    }
}
