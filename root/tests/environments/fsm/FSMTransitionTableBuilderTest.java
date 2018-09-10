package environments.fsm;

import framework.IRandomizer;
import framework.Move;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSMTransitionTableBuilderTest {

    // buildTransitionTable Tests
    @Test
    public void constructorAlphabetSizeLessThanOneThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FSMTransitionTableBuilder(0, 1, new TestRandomizer()));
    }

    @Test
    public void constructorNumStatesLessThanOneThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FSMTransitionTableBuilder(1, 0, new TestRandomizer()));
    }

    @Test
    public void constructorNullRandomizerThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new FSMTransitionTableBuilder(1, 1, null));
    }

    // getTransitionTable Tests
    @Test
    public void buildTransitionTableSingleTransitionSingleState() {
        FSMTransitionTableBuilder builder = new FSMTransitionTableBuilder(1, 1, new TestRandomizer());
        HashMap[] transitionTable = builder.getTransitionTable();
        assertEquals(1, transitionTable.length);
        HashMap<Move, Integer> goalTransitions = transitionTable[0];
        this.validateGoalTransitions(1, 0, goalTransitions);
    }

    @Test
    public void buildTransitionTableMultipleTransitionSingleState() {
        FSMTransitionTableBuilder builder = new FSMTransitionTableBuilder(13, 1, new TestRandomizer());
        HashMap[] transitionTable = builder.getTransitionTable();
        assertEquals(1, transitionTable.length);
        HashMap<Move, Integer> goalTransitions = transitionTable[0];
        this.validateGoalTransitions(13, 0, goalTransitions);
    }

    private void validateGoalTransitions(int expectedMoveCount, int goalState, HashMap<Move, Integer> transitions) {
        assertEquals(expectedMoveCount, transitions.size());
        for (int state : transitions.values()) {
            assertEquals(goalState, state);
        }
    }

    private class TestRandomizer implements IRandomizer {
        private Random random = new Random();

        @Override
        public int getRandomNumber(int ceiling) {
            return this.random.nextInt(ceiling);
        }
    }
}
