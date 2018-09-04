package environments.fsm;

import framework.Move;

import java.util.HashMap;

public class FSMTransitionTableBuilder {

    public static HashMap<Move, Integer>[] buildTransitionTable(int alphabetSize, int numStates)
    {
        if (alphabetSize < 1)
            throw new IllegalArgumentException("alphabetSize cannot be less than 1");
        if (numStates < 1)
            throw new IllegalArgumentException("numStates cannot be less than 1");
        Move[] moves = new Move[alphabetSize];
        for(int i = 0; i < moves.length; ++i) {
            char next = (char)('a' + i);
            moves[i] = new Move(next  + "");
        }
        HashMap[] transitionTable = new HashMap[numStates];
        // All goal state transitions should loop back to the goal state
        HashMap<Move, Integer> goalStateTransitions = new HashMap<>();
        for (Move move : moves)
        {
            goalStateTransitions.put(move, numStates - 1);
        }
        transitionTable[numStates - 1] = goalStateTransitions;
        return transitionTable;
    }

//    public TransitionData(int alphabetSize, int numStates)
//    {
//        this.moves = new Move[alphabetSize];
//        for(int i = 0; i < this.moves.length; ++i) {
//            char next = (char)('a' + i);
//            this.moves[i] = new Move(next  + "");
//        }
//        this.transitionTable = new HashMap[numStates];
//        // All goal state transitions should loop back to the goal state
//        HashMap<Move, Integer> goalStateTransitions = new HashMap<>();
//        for (Move move : this.moves)
//        {
//            goalStateTransitions.put(move, this.goalState);
//        }
//        this.transitionTable[this.goalState] = goalStateTransitions;
//        int maxTransitionsToGoal = (int)(this.transitionTable.length * this.moves.length * 0.04);
//        if (maxTransitionsToGoal == 0)
//            maxTransitionsToGoal = 1;
//        this.pickTransitions(this.goalState, this.random.nextInt(maxTransitionsToGoal) + 1);
//    }

//    private void pickTransitions(int initGoal, int numOfTransitions)
//    {
//        int row = -1;
//        int col = -1;
//        for(int i = 0; i<numOfTransitions; i++)
//        {
//            //check to see if table is full
//            if(this.transitionsDone == ((this.transitionTable.length-1)*this.moves.length))
//                return;
//            row = this.randomizer.getRandomNumber(this.transitionTable.length);
//            col = this.randomizer.getRandomNumber(this.moves.length);
//
//            if (this.transitionTable[row] != null && this.transitionTable[row].containsKey(this.moves[col]))
//            {
//                i--;
//                continue;
//            }
//            HashMap<Move, Integer> rowTransitions = this.transitionTable[row];
//            if (rowTransitions == null)
//                this.transitionTable[row] = rowTransitions = new HashMap<>();
//            rowTransitions.put(this.moves[col], initGoal);
//            this.transitionsDone++;
//        }
//        pickTransitions(row, 1);
//
//    }
}
