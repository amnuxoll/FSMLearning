package framework;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestRunTest {

    // constructor Tests
    @Test
    public void testConstructorNullAgentThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(null, new TestEnvironment(), 1, new TestResultWriter()));
    }

    @Test
    public void testConstructorNullEnvironmentThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(new TestAgent(), null, 1, new TestResultWriter()));
    }

    @Test
    public void testConstructorNumGoalsLessThan1ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(new TestAgent(), new TestEnvironment(), 0, new TestResultWriter()));
    }

    @Test
    public void testConstructorNullResultWriterThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(new TestAgent(), new TestEnvironment(), 1, null));
    }

    // execute Tests
    @Test
    public void testExecuteInitializesAgentWithMoves()
    {
        TestAgent agent = new TestAgent();
        TestEnvironment environment = new TestEnvironment();
        TestRun testRun = new TestRun(agent, environment, 1, new TestResultWriter());
        testRun.execute();
        assertArrayEquals(environment.getMoves(), agent.moves);
    }

    @Test
    public void testExecute()
    {
        TestAgent agent = new TestAgent();
        TestEnvironment environment = new TestEnvironment();
        TestRun testRun = new TestRun(agent, environment, 1, new TestResultWriter());
        testRun.execute();
        assertArrayEquals(environment.getMoves(), agent.moves);
    }

    @Test
    public void testExecuteMarshalsCallsBetweenAgentAndEnvironmentSingleGoal()
    {
        TestAgent agent = new TestAgent();
        TestEnvironment environment = new TestEnvironment();
        TestRun testRun = new TestRun(agent, environment, 1, new TestResultWriter());
        testRun.execute();

        SensorData sensorA = new SensorData(false);
        sensorA.setSensor("a", "a");
        SensorData sensorB = new SensorData(false);
        sensorB.setSensor("b", "b");
        SensorData sensorC = new SensorData(true);
        sensorC.setSensor("c", "c");

        Episode[] expectedEpisodicMemory = new Episode[]
        {
                new Episode(null, new Move("a")),
                new Episode(sensorA, new Move("b")),
                new Episode(sensorB, new Move("c"))
        };

        assertArrayEquals(expectedEpisodicMemory, agent.episodes.toArray());
        assertArrayEquals(expectedEpisodicMemory, environment.episodes.toArray());
        assertEquals(1, environment.resetCount);
    }

    @Test
    public void testExecuteMarshalsCallsBetweenAgentAndEnvironmentMultipleGoals()
    {
        TestAgent agent = new TestAgent();
        TestEnvironment environment = new TestEnvironment();
        TestRun testRun = new TestRun(agent, environment, 3, new TestResultWriter());
        testRun.execute();

        SensorData sensorA = new SensorData(false);
        sensorA.setSensor("a", "a");
        SensorData sensorB = new SensorData(false);
        sensorB.setSensor("b", "b");
        SensorData sensorC = new SensorData(true);
        sensorC.setSensor("c", "c");

        Episode[] expectedEpisodicMemory = new Episode[]
                {
                        new Episode(null, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c")),
                        new Episode(sensorC, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c")),
                        new Episode(sensorC, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c"))
                };

        assertArrayEquals(expectedEpisodicMemory, agent.episodes.toArray());
        assertArrayEquals(expectedEpisodicMemory, environment.episodes.toArray());
        assertEquals(3, environment.resetCount);
    }

    @Test
    public void testExecuteMarshalsCallsBetweenAgentAndEnvironmentSingleGoalWithResultWriter()
    {
        TestAgent agent = new TestAgent();
        TestEnvironment environment = new TestEnvironment();
        TestResultWriter resultWriter = new TestResultWriter();
        TestRun testRun = new TestRun(agent, environment, 1, resultWriter);
        testRun.execute();

        SensorData sensorA = new SensorData(false);
        sensorA.setSensor("a", "a");
        SensorData sensorB = new SensorData(false);
        sensorB.setSensor("b", "b");
        SensorData sensorC = new SensorData(true);
        sensorC.setSensor("c", "c");

        Episode[] expectedEpisodicMemory = new Episode[]
                {
                        new Episode(null, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c"))
                };
        String[] expectedResultWriterLogs = new String[]
                {
                  "3,"
                };

        assertArrayEquals(expectedEpisodicMemory, agent.episodes.toArray());
        assertArrayEquals(expectedEpisodicMemory, environment.episodes.toArray());
        assertEquals(1, environment.resetCount);
        assertArrayEquals(expectedResultWriterLogs, resultWriter.logStatements.toArray());
    }

    @Test
    public void testExecuteMarshalsCallsBetweenAgentAndEnvironmentMultipleGoalsWithResultWriter()
    {
        TestAgent agent = new TestAgent();
        TestEnvironment environment = new TestEnvironment();
        TestResultWriter resultWriter = new TestResultWriter();
        TestRun testRun = new TestRun(agent, environment, 3, resultWriter);
        testRun.execute();

        SensorData sensorA = new SensorData(false);
        sensorA.setSensor("a", "a");
        SensorData sensorB = new SensorData(false);
        sensorB.setSensor("b", "b");
        SensorData sensorC = new SensorData(true);
        sensorC.setSensor("c", "c");

        Episode[] expectedEpisodicMemory = new Episode[]
                {
                        new Episode(null, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c")),
                        new Episode(sensorC, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c")),
                        new Episode(sensorC, new Move("a")),
                        new Episode(sensorA, new Move("b")),
                        new Episode(sensorB, new Move("c"))
                };
        String[] expectedResultWriterLogs = new String[]
                {
                        "3,",
                        "3,",
                        "3,"
                };

        assertArrayEquals(expectedEpisodicMemory, agent.episodes.toArray());
        assertArrayEquals(expectedEpisodicMemory, environment.episodes.toArray());
        assertEquals(3, environment.resetCount);
        assertArrayEquals(expectedResultWriterLogs, resultWriter.logStatements.toArray());
    }

    private class TestAgent implements IAgent
    {

        public Move[] moves;

        public ArrayList<Episode> episodes = new ArrayList<>();

        private int moveIndex = 0;

        @Override
        public void setMoves(Move[] moves) {
            this.moves = moves;
        }

        @Override
        public Move getNextMove(SensorData sensorData) {
            Move move = this.moves[this.moveIndex++];
            Episode episode = new Episode(sensorData, move);
            this.episodes.add(episode);
            if (this.moveIndex >= this.moves.length)
                this.moveIndex = 0;
            return move;
        }
    }

    private  class TestEnvironment implements IEnvironment
    {
        public int resetCount = 0;

        public ArrayList<Episode> episodes = new ArrayList<>();

        private SensorData lastSensorData = null;

        @Override
        public Move[] getMoves() {
            return new Move[] { new Move("a"), new Move("b"), new Move("c") };
        }

        @Override
        public SensorData tick(Move move) {
            Episode episode = new Episode(this.lastSensorData, move);
            this.episodes.add(episode);
            this.lastSensorData = new SensorData(move.getName() == "c");
            this.lastSensorData.setSensor(move.getName(), move.getName());
            return this.lastSensorData;
        }

        @Override
        public void reset() {
            this.resetCount++;
        }
    }

    private class TestResultWriter implements IResultWriter
    {
        public ArrayList<String> logStatements = new ArrayList<>();

        @Override
        public void logStepsToGoal(int stepsToGoal) {
            logStatements.add(stepsToGoal + ",");
        }

        @Override
        public void beginNewRun() {

        }

        @Override
        public void complete() {

        }
    }
}
