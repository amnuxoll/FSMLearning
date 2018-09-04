package environments.fsm;

import framework.IRandomizer;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class FSMDescriptionProviderTest {

    // constructor Tests
    @Test
    public void constructorAlphabetSizeLessThanOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new FSMDescriptionProvider(0, 1, FSMDescription.Sensor.ALL_SENSORS));
    }

    @Test
    public void constructorNumberOfStatesLessThanOneThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new FSMDescriptionProvider(1, 0, FSMDescription.Sensor.ALL_SENSORS));
    }

    @Test
    public void constructorNullEnumSetThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new FSMDescriptionProvider(1, 1, null));
    }

    // getEnvironmentDescription Tests
    @Test
    public void getEnvironmentDescriptionNullRandomizerThrowsException()
    {
        FSMDescriptionProvider descriptionProvider = new FSMDescriptionProvider(1, 1, FSMDescription.Sensor.ALL_SENSORS);
        assertThrows(IllegalArgumentException.class, () -> descriptionProvider.getEnvironmentDescription(null));
    }

    @Test
    public void getEnvironmentDescriptionHeedsConfiguration1()
    {
        FSMDescriptionProvider descriptionProvider = new FSMDescriptionProvider(1, 1, FSMDescription.Sensor.ALL_SENSORS);
        FSMDescription description = (FSMDescription)descriptionProvider.getEnvironmentDescription(new TestRandomizer());
        assertEquals(1, description.getMoves().length);
        assertEquals(1, description.getNumStates());
        assertEquals(FSMDescription.Sensor.ALL_SENSORS, description.getSensorsToInclude());
    }

    @Test
    public void getEnvironmentDescriptionHeedsConfiguration2()
    {
        FSMDescriptionProvider descriptionProvider = new FSMDescriptionProvider(13, 42, EnumSet.of(FSMDescription.Sensor.EVEN_ODD));
        FSMDescription description = (FSMDescription)descriptionProvider.getEnvironmentDescription(new TestRandomizer());
        assertEquals(13, description.getMoves().length);
        assertEquals(42, description.getNumStates());
        assertEquals(EnumSet.of(FSMDescription.Sensor.EVEN_ODD), description.getSensorsToInclude());
    }

    private class TestRandomizer implements IRandomizer
    {
        private Random random = new Random();

        @Override
        public int getRandomNumber(int ceiling) {
            return this.random.nextInt(ceiling);
        }
    }
}
