package agents.marz;

import framework.Move;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.*;

public class Sequence {
    private Move[] moves;

    public static final Sequence EMPTY = new Sequence(new Move[0]);

    public Sequence(Move[] moves)
    {
        if (moves == null)
            throw new IllegalArgumentException("moves cannot be null");
        this.moves = moves;
    }

    public boolean endsWith(Sequence sequence)
    {
        if (sequence == null)
            throw new IllegalArgumentException("sequence cannot be null.");
        if (sequence.moves.length > this.moves.length)
            return false;
        if (sequence.moves.length == 0)
            return true;
        List<Move> myMoves = new ArrayList<>(Arrays.asList(this.moves));
        List<Move> otherMoves = new ArrayList<>(Arrays.asList(sequence.moves));
        Collections.reverse(myMoves);
        Collections.reverse(otherMoves);
        for (int i = 0; i < otherMoves.size(); i++)
        {
            if (!myMoves.get(i).equals(otherMoves.get(i)))
                return false;
        }
        return true;
    }

    public Move[] getMoves()
    {
        return this.moves;
    }

    public Sequence getSubsequence(int startIndex)
    {
        if (startIndex < 0)
            throw new IllegalArgumentException("startIndex cannot be less than 0");
        if (startIndex >= this.moves.length)
            return Sequence.EMPTY;
        Move[] subsequence = Arrays.copyOfRange(this.moves, startIndex, this.moves.length);
        return new Sequence(subsequence);
    }

    public Sequence take(int length)
    {
        if (length < 0)
            throw new IllegalArgumentException("length cannot be less than zero.");
        if (length > this.moves.length)
            throw new IllegalArgumentException("length is too large.");
        if (length == 0)
            return Sequence.EMPTY;
        Move[] subsequence = Arrays.copyOfRange(this.moves, 0, length);
        return new Sequence(subsequence);
    }

    public int getLength()
    {
        return this.moves.length;
    }

    public Sequence buildChildSequence(Move newMove)
    {
        if (newMove == null)
            throw new IllegalArgumentException("newMove cannot be null");
        List<Move> childMoves = new LinkedList<>(Arrays.asList(this.moves));
        childMoves .add(0, newMove);
        return new Sequence(childMoves.toArray(new Move[0]));
    }

    private int currentIndex = -1;

    public boolean hasNext()
    {
        return this.currentIndex < (this.moves.length - 1);
    }

    public Move next()
    {
        if (!this.hasNext())
            throw new InvalidStateException("Sequence has no next Move.");
        this.currentIndex++;
        return this.moves[this.currentIndex];
    }

    public void reset()
    {
        this.currentIndex = -1;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Sequence)) {
            return false;
        }
        Sequence sequence = (Sequence) o;
        if (this.moves.length != sequence.moves.length)
            return false;
        for (int i = 0; i < this.moves.length; i++)
        {
            if (!this.moves[i].equals(sequence.moves[i]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(this.moves);
    }

    @Override
    public String toString()
    {
        String representation = "";
        for (Move move : this.moves)
        {
            representation += move.toString();
        }
        return representation;
    }
}
