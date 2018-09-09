package agents.marz;

import framework.Move;

import java.util.ArrayList;
import java.util.List;

public class SequenceGenerator {
    private Move[] moves;

    public SequenceGenerator(Move[] moves)
    {
        if (moves == null)
            throw new IllegalArgumentException("moves cannot be null.");
        this.moves = moves;
    }

    public Sequence nextPermutation(int index)
    {
        if (index <= 0) {
            throw new IndexOutOfBoundsException("index must be a positive number.  Has your next permutation index overflowed?");
        }// if

        if (this.moves.length == 0)
            return Sequence.EMPTY;

        List<Move> nextSequence = new ArrayList<>();
        if (index <= this.moves.length) {
            nextSequence.add(this.moves[index - 1]);
        }// if
        else {
            while (index > 0) {
                nextSequence.add(0, this.moves[--index % this.moves.length]);
                index /= this.moves.length;
            }// while
        }

        return new Sequence(nextSequence.toArray(new Move[0]));
    }// nextPermutation
}
