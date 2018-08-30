package framework;

import agents.marz.MaRzAgent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSuiteTest {

    // constructor Tests
    @Test
    public void testConstructorNumberOfIterationsLessThanOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class,() -> new TestSuite(0, 1, new TestResultWriter(), () -> null, () -> null, (agent, environment, numGoals, resultWriter) -> null));
    }

    @Test
    public void testConstructorNumberOfGoalsLessThanOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class,() -> new TestSuite(1, 0, new TestResultWriter(), () -> null, () -> null, (agent, environment, numGoals, resultWriter) -> null));
    }

    @Test
    public void testConstructorNullResultWriterSupplierOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class,() -> new TestSuite(1, 1, null, () -> null, () -> null, (agent, environment, numGoals, resultWriter) -> null));
    }

    @Test
    public void testConstructorNullAgentSupplierOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class,() -> new TestSuite(1, 1, new TestResultWriter(), null, () -> null, (agent, environment, numGoals, resultWriter) -> null));
    }

    @Test
    public void testConstructorNullEnvironmentSupplierOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class,() -> new TestSuite(1, 1,new TestResultWriter(), () -> null, null, (agent, environment, numGoals, resultWriter) -> null));
    }

    @Test
    public void testConstructorNullTestRunSupplierOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class,() -> new TestSuite(1, 1, new TestResultWriter(), () -> null, () -> null, null));
    }

    // run Tests
    @Test
    public void testRunInitializesAndExecutesSingleTestCorrectly()
    {
        final AtomicInteger testRunInstanceCount = new AtomicInteger(0);
        TestTestRun testRun = new TestTestRun();
        TestSuite testSuite = new TestSuite(
                1,
                13,
                new TestResultWriter(),
                () -> new TestAgent(),
                () -> new TestEnvironment(),
                (agent, environment, numGoals, resultWriter) -> {
                    testRunInstanceCount.addAndGet(1);
                    assertTrue(agent instanceof TestAgent);
                    assertTrue(environment instanceof TestEnvironment);
                    assertEquals(13, numGoals);
                    assertTrue(resultWriter instanceof TestResultWriter);
                    return testRun;
                });
        testSuite.run();
        assertEquals(1, testRunInstanceCount.get());
        assertEquals(1, testRun.executeCount);
    }

    @Test
    public void testRunInitializesAndExecutesMultipleTestsCorrectly()
    {
        final AtomicInteger testRunInstanceCount = new AtomicInteger(0);
        TestTestRun testRun = new TestTestRun();
        TestSuite testSuite = new TestSuite(
                20,
                13,
                new TestResultWriter(),
                () -> new TestAgent(),
                () -> new TestEnvironment(),
                (agent, environment, numGoals, resultWriter) -> {
                    testRunInstanceCount.addAndGet(1);
                    assertTrue(agent instanceof TestAgent);
                    assertTrue(environment instanceof TestEnvironment);
                    assertEquals(13, numGoals);
                    assertTrue(resultWriter instanceof TestResultWriter);
                    return testRun;
                });
        testSuite.run();
        assertEquals(20, testRunInstanceCount.get());
        assertEquals(20, testRun.executeCount);
    }

    private class TestTestRun implements ITestRun
    {
        public int executeCount = 0;

        @Override
        public void execute() {
            this.executeCount++;
        }
    }

    private class TestAgent implements IAgent
    {

        @Override
        public void setMoves(Move[] moves) {

        }

        @Override
        public Move getNextMove(SensorData sensorData) throws Exception {
            return null;
        }
    }

    private class TestEnvironment implements IEnvironment
    {

        @Override
        public Move[] getMoves() {
            return new Move[0];
        }

        @Override
        public SensorData tick(Move move) {
            return null;
        }

        @Override
        public void reset() {

        }
    }

    private class TestResultWriter implements IResultWriter
    {

        @Override
        public void logStepsToGoal(int stepsToGoal) {

        }

        @Override
        public void beginNewRun() {

        }

        @Override
        public void complete() {

        }
    }
}
