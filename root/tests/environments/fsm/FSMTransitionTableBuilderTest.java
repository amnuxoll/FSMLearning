package environments.fsm;

import framework.Move;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSMTransitionTableBuilderTest {

    // buildTransitionTable Tests
    @Test
    public void buildTransitionTableAlphabetSizeLessThanOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> FSMTransitionTableBuilder.buildTransitionTable(0, 1));
    }

    @Test
    public void buildTransitionTableNumStatesLessThanOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> FSMTransitionTableBuilder.buildTransitionTable(1, 0));
    }

    @Test
    public void buildTransitionTableSingleTransitionSingleState()
    {
        HashMap[] transitionTable = FSMTransitionTableBuilder.buildTransitionTable(1, 1);
        assertEquals(1, transitionTable.length);
        HashMap<Move, Integer> goalTransitions = transitionTable[0];
        this.validateGoalTransitions(1, 0, goalTransitions);
    }

    @Test
    public void buildTransitionTableMultipleTransitionSingleState()
    {
        HashMap[] transitionTable = FSMTransitionTableBuilder.buildTransitionTable(13, 1);
        assertEquals(1, transitionTable.length);
        HashMap<Move, Integer> goalTransitions = transitionTable[0];
        this.validateGoalTransitions(13, 0, goalTransitions);
    }

    private void validateGoalTransitions(int expectedMoveCount, int goalState, HashMap<Move, Integer> transitions)
    {
        assertEquals(expectedMoveCount, transitions.size());
        for (int state : transitions.values())
        {
            assertEquals(goalState, state);
        }
    }
}
