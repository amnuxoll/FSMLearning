package framework;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestRunTest {

    // constructor Tests
    @Test
    public void testConstructorNullAgentThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(null, new TestEnvironmentDescription(), 1, new TestRandomizer()));
    }

    @Test
    public void testConstructorNullEnvironmentDescriptionThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(new TestAgent(), null, 1, new TestRandomizer()));
    }

    @Test
    public void testConstructorNumberOfGoalsToFindLessThan1ThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(new TestAgent(), new TestEnvironmentDescription(), 0, new TestRandomizer()));
    }

    @Test
    public void testConstructorNullRandomizerThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new TestRun(new TestAgent(), new TestEnvironmentDescription(), 1, null));
    }

    // execute Tests
    @Test
    public void testExecuteInitializesAgentWithMoves()
    {
        TestAgent agent = new TestAgent();
        TestEnvironmentDescription environmentDescription = new TestEnvironmentDescription();
        TestRun testRun = new TestRun(agent, environmentDescription, 1, new TestRandomizer());
        testRun.execute();
        assertArrayEquals(environmentDescription.getMoves(), agent.moves);
    }

    @Test
    public void testExecuteMarshalsCallsBetweenAgentAndEnvironmentSingleGoalWithResultWriter()
    {
        TestAgent agent = new TestAgent();
        TestEnvironmentDescription environment = new TestEnvironmentDescription();
        TestGoalListener goalListener = new TestGoalListener();
        TestRun testRun = new TestRun(agent, environment, 1, new TestRandomizer());
        testRun.addGoalListener(goalListener);
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
        assertArrayEquals(expectedResultWriterLogs, goalListener.logStatements.toArray());
    }

    @Test
    public void testExecuteMarshalsCallsBetweenAgentAndEnvironmentMultipleGoalsWithResultWriter()
    {
        TestAgent agent = new TestAgent();
        TestEnvironmentDescription environment = new TestEnvironmentDescription();
        TestGoalListener goalListener = new TestGoalListener();
        TestRun testRun = new TestRun(agent, environment, 3, new TestRandomizer());
        testRun.addGoalListener(goalListener);
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
        assertArrayEquals(expectedResultWriterLogs, goalListener.logStatements.toArray());
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

    private  class TestEnvironmentDescription implements IEnvironmentDescription
    {
        private Move lastMove;
        @Override
        public Move[] getMoves() {
            return new Move[] { new Move("a"), new Move("b"), new Move("c") };
        }

        @Override
        public int transition(int currentState, Move move) {
            this.lastMove = move;
            return 0;
        }

        @Override
        public boolean isGoalState(int state) {
            return this.lastMove.getName() == "c";
        }

        @Override
        public int getNumStates() {
            return 0;
        }

        @Override
        public void applySensors(int lastState, Move move, int currentState, SensorData sensorData) {
            sensorData.setSensor(this.lastMove.getName(), this.lastMove.getName());
        }
    }

    private class TestGoalListener implements IGoalListener
    {
        public ArrayList<String> logStatements = new ArrayList<>();

        @Override
        public void goalReceived(GoalEvent event) {
            logStatements.add(event.getStepCountToGoal() + ",");
        }
    }

    private class TestRandomizer  implements IRandomizer {

        @Override
        public int getRandomNumber(int ceiling) {
            return 0;
        }
    }
}
