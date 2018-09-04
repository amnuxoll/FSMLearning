package framework;

import java.util.Random;

/**
 *
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public class Randomizer implements IRandomizer {
    private Random random;

    public Randomizer(boolean trueRandom)
    {
        if (trueRandom)
            this.random = new Random(System.currentTimeMillis());
        else
            this.random = new Random(10);
    }

    @Override
    public int getRandomNumber(int ceiling) {
        return this.random.nextInt(ceiling);
    }
}
