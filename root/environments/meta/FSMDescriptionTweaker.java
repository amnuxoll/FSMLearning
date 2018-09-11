package environments.meta;

import environments.fsm.FSMDescription;
import environments.fsm.FSMTransitionTableBuilder;
import framework.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

/**
 * Class FSMDescriptionTweaker gives new FSM environments and 'tweaks' them by randomly swapping two moves in the transition table
 *
 * @author Patrick Maloney
 * @author Harry Thoma
 * @version September 2018
 */
public class FSMDescriptionTweaker implements IEnvironmentDescriptionProvider {

    private HashMap<Move, Integer>[] table;
    private FSMTransitionCounterDescription lastDescription = null;
    private HashMap<Move, Integer>[] sensorTable;
    private EnumSet<FSMDescription.Sensor> sensorsToInclude;

    public FSMDescriptionTweaker(int alphaSize, int numStates, EnumSet<FSMDescription.Sensor> sensorsToInclude,
                                 Randomizer randomizer) {
        FSMTransitionTableBuilder builder = new FSMTransitionTableBuilder(alphaSize, numStates, randomizer);
        table = builder.getTransitionTable();
    }


    @Override
    public IEnvironmentDescription getEnvironmentDescription(IRandomizer randomizer) {
        FSMTransitionCounterDescription newDescription;
        if(lastDescription == null) {
            newDescription = new FSMTransitionCounterDescription(table, sensorsToInclude);

        }
        else {
            tweakTable(1, randomizer, lastDescription.getSensorTable());
            newDescription = new FSMTransitionCounterDescription(table, sensorsToInclude, sensorTable);
        }
        lastDescription = newDescription;
        return newDescription;
    }

    /**
     * randomly changes two moves in the transition table
     * @param numChanges
     * @param randomizer
     */
    private void tweakTable(int numChanges, IRandomizer randomizer, HashMap<Move, Integer>[] sensorTable) {
        for(int i = 0;i<numChanges; i++) {
            int stateToSwitch = randomizer.getRandomNumber(table.length); //pick which state whose moves will be swapped
            Set<Move> moves = table[stateToSwitch].keySet();
            Move[] moveArray = (Move[]) moves.toArray();

            //pick two moves from a state's possible moves to exchange
            int selectedMove1 = randomizer.getRandomNumber(moveArray.length);
            int selectedMove2 = randomizer.getRandomNumber(moveArray.length);

            //save value to temp and put new values in swapped places
            Integer temp = table[stateToSwitch].get(moveArray[selectedMove1]);
            table[stateToSwitch].put(moveArray[selectedMove1], table[stateToSwitch].get(moveArray[selectedMove2]));
            table[stateToSwitch].put(moveArray[selectedMove2], temp);

            sensorTable[stateToSwitch].put(moveArray[selectedMove1], 0);
            sensorTable[stateToSwitch].put(moveArray[selectedMove2], 0);
        }
    }
}
