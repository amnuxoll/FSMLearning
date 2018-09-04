package environments.fsm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
    public void getEnvironmentDescriptionHeedsConfiguration()
    {
        fail("need to implement");
    }
}
