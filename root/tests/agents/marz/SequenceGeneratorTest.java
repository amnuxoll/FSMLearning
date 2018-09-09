package agents.marz;

import framework.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SequenceGeneratorTest {

    // constructor Tests
    @Test
    public void constructorNullMovesThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new SequenceGenerator(null));
    }

    // nextPermutation Tests
    @Test
    public void nextPermutationIndexLessThanZeroThrowsException()
    {
        SequenceGenerator sequenceGenerator = new SequenceGenerator(new Move[0]);
        assertThrows(IndexOutOfBoundsException.class, () -> sequenceGenerator.nextPermutation(0));
    }

    @Test
    public void nextPermutationNoMovesAlwaysYieldsEmptySequence()
    {
        SequenceGenerator sequenceGenerator = new SequenceGenerator(new Move[0]);
        assertEquals(Sequence.EMPTY, sequenceGenerator.nextPermutation(1));
        assertEquals(Sequence.EMPTY, sequenceGenerator.nextPermutation(3));
        assertEquals(Sequence.EMPTY, sequenceGenerator.nextPermutation(42));
    }

    @Test
    public void nextPermutationPermutesSingleMove()
    {
        Move a = new Move("a");
        SequenceGenerator sequenceGenerator = new SequenceGenerator(new Move[] { a });
        assertEquals(new Sequence(new Move[] { a }), sequenceGenerator.nextPermutation(1));
        assertEquals(new Sequence(new Move[] { a, a }), sequenceGenerator.nextPermutation(2));
        assertEquals(new Sequence(new Move[] { a, a, a }), sequenceGenerator.nextPermutation(3));
        assertEquals(new Sequence(new Move[] { a, a, a, a, a, a, a, a }), sequenceGenerator.nextPermutation(8));
    }

    @Test
    public void nextPermutationPermutesTwoMoves()
    {
        Move a = new Move("a");
        Move b = new Move("b");
        SequenceGenerator sequenceGenerator = new SequenceGenerator(new Move[] { a, b });
        assertEquals(new Sequence(new Move[] { a }), sequenceGenerator.nextPermutation(1));
        assertEquals(new Sequence(new Move[] { b }), sequenceGenerator.nextPermutation(2));
        assertEquals(new Sequence(new Move[] { a, a }), sequenceGenerator.nextPermutation(3));
        assertEquals(new Sequence(new Move[] { a, b}), sequenceGenerator.nextPermutation(4));
        assertEquals(new Sequence(new Move[] { b, a}), sequenceGenerator.nextPermutation(5));
        assertEquals(new Sequence(new Move[] { b, b}), sequenceGenerator.nextPermutation(6));
        assertEquals(new Sequence(new Move[] { a, a, a}), sequenceGenerator.nextPermutation(7));
        assertEquals(new Sequence(new Move[] { a, a, b}), sequenceGenerator.nextPermutation(8));
        assertEquals(new Sequence(new Move[] { a, b, a}), sequenceGenerator.nextPermutation(9));
        assertEquals(new Sequence(new Move[] { a, b, b}), sequenceGenerator.nextPermutation(10));
        assertEquals(new Sequence(new Move[] { b, a, a}), sequenceGenerator.nextPermutation(11));
        assertEquals(new Sequence(new Move[] { b, a, b}), sequenceGenerator.nextPermutation(12));
        assertEquals(new Sequence(new Move[] { b, b, a}), sequenceGenerator.nextPermutation(13));
        assertEquals(new Sequence(new Move[] { b, b, b}), sequenceGenerator.nextPermutation(14));
    }
}
