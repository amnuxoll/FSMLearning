package agents.marz;

import framework.Move;

import java.util.*;

public class SequenceGenerator {
    private Move[] moves;
    private HashMap<Move, Integer> moveIndex = new HashMap<>();

    public SequenceGenerator(Move[] moves)
    {
        if (moves == null)
            throw new IllegalArgumentException("moves cannot be null.");
        this.moves = moves;
        for (int i = 0; i < this.moves.length; i++)
        {
            this.moveIndex.put(this.moves[i], i);
        }
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

    public int getCanonicalIndex(Sequence sequence)
    {
        if (sequence == null)
            throw new IllegalArgumentException("sequence cannot be null");
        ArrayList<Move> moves = new ArrayList<>(Arrays.asList(sequence.getMoves()));
        Collections.reverse(moves);
        double total = 0;
        for (int i = 0; i < moves.size(); i++)
        {
            int index = this.moveIndex.get(moves.get(i)) + 1;
            total += Math.pow(this.moves.length, i) * index;
        }
        return (int)total;
    }
}
