package environments.fsm;

import framework.IEnvironmentDescription;
import framework.IEnvironmentDescriptionProvider;
import framework.IRandomizer;
import framework.Move;

import java.util.EnumSet;
import java.util.HashMap;

public class FSMDescriptionProvider implements IEnvironmentDescriptionProvider {

    private int alphabetSize;
    private int numStates;
    private EnumSet<FSMDescription.Sensor> sensorsToInclude;

    public FSMDescriptionProvider(int alphabetSize, int numStates, EnumSet<FSMDescription.Sensor> sensorsToInclude)
    {
        if (alphabetSize < 1)
            throw new IllegalArgumentException("alphabetSize cannot be less than 1");
        if (numStates < 1)
            throw new IllegalArgumentException("numStates cannot be less than 1");
        if (sensorsToInclude == null)
            throw new IllegalArgumentException("sensorsToInclude cannot be null");
       this.alphabetSize = alphabetSize;
       this.numStates = numStates;
       this.sensorsToInclude = sensorsToInclude;
    }

    @Override
    public IEnvironmentDescription getEnvironmentDescription(IRandomizer randomizer) {
        if (randomizer == null)
            throw new IllegalArgumentException("randomizer cannot be null");
        FSMTransitionTableBuilder builder = new FSMTransitionTableBuilder(this.alphabetSize, this.numStates, randomizer);
        HashMap<Move, Integer>[] transitionTable = builder.getTransitionTable();
        return new FSMDescription(transitionTable, this.sensorsToInclude);
    }
}
