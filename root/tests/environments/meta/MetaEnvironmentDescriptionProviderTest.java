package environments.meta;

import framework.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetaEnvironmentDescriptionProviderTest {
    @Test
    public void contstructor(){
        assertThrows(IllegalArgumentException.class,
                () -> new MetaEnvironmentDescriptionProvider(new TestEnvironmentDescriptionProvider(),null));
        assertThrows(IllegalArgumentException.class,
                () -> new MetaEnvironmentDescriptionProvider(null,MetaConfiguration.DEFAULT));
    }
    @Test
    public void getEvironmentDesctiption(){

    }

    /**
     *  Mock Classes
     *
     */

    private class TestEnvironmentDescriptionProvider implements IEnvironmentDescriptionProvider {
        public int numGenerated= 0;

        @Override
        public IEnvironmentDescription getEnvironmentDescription(IRandomizer randomizer) {
            numGenerated++;
            return new TestEnvironmentDescription();
        }

    }

    private class TestEnvironmentDescription implements  IEnvironmentDescription{

        @Override
        public Move[] getMoves() {
            Move[] moves= {
                    new Move("a"),
                    new Move("b")
            };
            return moves;
        }

        @Override
        public int transition(int currentState, Move move) {
            return 42;
        }

        @Override
        public boolean isGoalState(int state) {
            return state == 13;
        }

        @Override
        public int getNumStates() {
            return 3;
        }

        @Override
        public void applySensors(int lastState, Move move, int currState, SensorData sensorData) {
            sensorData.setSensor("sensei", new Integer(2));
        }
    }

    private class TestRandomizer implements IRandomizer{

        @Override
        public int getRandomNumber(int ceiling) {
            return 0;
        }
    }
}
