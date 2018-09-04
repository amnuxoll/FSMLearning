package environments.fsm;

import framework.IEnvironmentDescription;
import framework.Move;
import framework.SensorData;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

public class FSMDescription implements IEnvironmentDescription {

    private HashMap<Move, Integer>[] transitionTable;

    private Move[] moves;

    private EnumSet<Sensor> sensorsToInclude;

    public FSMDescription(HashMap<Move, Integer>[] transitionTable)
    {
        this(transitionTable, EnumSet.noneOf(Sensor.class));
    }

    public FSMDescription(HashMap<Move, Integer>[] transitionTable, EnumSet<Sensor> sensorsToInclude)
    {
        if (transitionTable == null)
            throw new IllegalArgumentException("transitionTable cannot be null");
        if (transitionTable.length == 0)
            throw new IllegalArgumentException("transitionTable cannot be empty");
        if (sensorsToInclude == null)
            throw new IllegalArgumentException("sensorsToInclude cannot be null");
        this.transitionTable = transitionTable;
        this.sensorsToInclude = sensorsToInclude;
        Set<Move> moveSet = this.transitionTable[0].keySet();
        this.moves = moveSet.toArray(new Move[0]);
        for (int state = 0; state < this.transitionTable.length; state++)
        {
            moveSet = this.transitionTable[state].keySet();
            if (this.moves.length != moveSet.size())
                throw new IllegalArgumentException("transitionTable is not valid for FSM. All transitions must exist for each state.");
            for (Move move : this.moves)
            {
                if (!moveSet.contains(move))
                    throw new IllegalArgumentException("transitionTable is not valid for FSM. All transition moves must exist for each state.");
            }
        }
    }

    public EnumSet<Sensor> getSensorsToInclude() {
        return this.sensorsToInclude;
    }

    @Override
    public Move[] getMoves() {
        return this.moves;
    }

    @Override
    public int transition(int currentState, Move move) {
        if (currentState < 0)
            throw new IllegalArgumentException("currentState cannot be less than 0");
        if (currentState >= this.transitionTable.length)
            throw new IllegalArgumentException("currentState does not exist");
        if (move == null)
            throw new IllegalArgumentException("move cannot be null");
        HashMap<Move, Integer> transitions = this.transitionTable[currentState];
        if (!transitions.containsKey(move))
            throw new IllegalArgumentException("move is invalid for this environment");
        return transitions.get(move);
    }

    @Override
    public boolean isGoalState(int state) {
        return state == (this.transitionTable.length - 1);
    }

    @Override
    public int getNumStates() {
        return this.transitionTable.length;
    }

    @Override
    public void applySensors(int state, SensorData sensorData) {
        if (sensorData == null)
            throw new IllegalArgumentException("sensorData cannot be null");
        if (this.sensorsToInclude.contains(Sensor.EVEN_ODD))
            this.applyEvenOddSensor(state, sensorData);
    }

    private void applyEvenOddSensor(int state, SensorData sensorData)
    {
        sensorData.setSensor("Even", state % 2 == 0);
    }

    public enum Sensor {
        EVEN_ODD,
        NOISE;

        public static final EnumSet<Sensor> ALL_SENSORS = EnumSet.allOf(Sensor.class);
    }
}
