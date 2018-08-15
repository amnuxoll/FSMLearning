package environments.fsm;

import framework.IEnvironment;
import framework.Move;
import framework.SensorData;

import java.util.HashMap;
import java.util.Random;

public class FiniteStateMachine implements IEnvironment {

    private int transitionsDone = 0;
    private Random random = new Random();

    private HashMap<Move, Integer>[] transitionTable;

    private int goalState;

    protected int currentState;

    private Move[] moves;

    public FiniteStateMachine(int alphabetSize, int numStates)
    {
        this.goalState = numStates - 1;
        this.currentState = 0;
        this.moves = new Move[alphabetSize];
        for(int i = 0; i < this.moves.length; ++i) {
            char next = (char)('a' + i);
            this.moves[i] = new Move(next  + "");
        }
        this.transitionTable = new HashMap[numStates];
        // All goal state transitions should loop back to the goal state
        HashMap<Move, Integer> goalStateTransitions = new HashMap<>();
        for (Move move : this.moves)
        {
            goalStateTransitions.put(move, this.goalState);
        }
        this.transitionTable[this.goalState] = goalStateTransitions;
        int maxTransitionsToGoal = (int)(this.transitionTable.length * this.moves.length * 0.04);
        if (maxTransitionsToGoal == 0)
            maxTransitionsToGoal = 1;
        this.pickTransitions(this.goalState, this.random.nextInt(maxTransitionsToGoal) + 1);
    }

    @Override
    public Move[] getMoves() {
        return this.moves;
    }

    @Override
    public SensorData tick(Move move) {
        this.currentState = this.transitionTable[this.currentState].get(move);
        return new SensorData(this.currentState == this.goalState);
    }

    @Override
    public void reset() {
        this.currentState = this.random.nextInt(this.transitionTable.length);
    }

    private void pickTransitions(int initGoal, int numOfTransitions)
    {
        int row = -1;
        int col = -1;
        for(int i = 0; i<numOfTransitions; i++)
        {
            //check to see if table is full
            if(this.transitionsDone == ((this.transitionTable.length-1)*this.moves.length))
                return;
            row = random.nextInt(this.transitionTable.length);
            col = random.nextInt(this.moves.length);

            if (this.transitionTable[row] != null && this.transitionTable[row].containsKey(this.moves[col]))
            {
                i--;
                continue;
            }
            HashMap<Move, Integer> rowTransitions = this.transitionTable[row];
            if (rowTransitions == null)
                this.transitionTable[row] = rowTransitions = new HashMap<>();
            rowTransitions.put(this.moves[col], initGoal);
            this.transitionsDone++;
        }
        pickTransitions(row, 1);

    }
}
