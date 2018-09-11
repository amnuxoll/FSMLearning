package environments.meta;

import environments.fsm.FSMDescription;
import framework.IRandomizer;
import framework.Move;
import framework.Randomizer;
import framework.Services;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FSMDescriptionTweakerTest {
    @Test
    public void constructor(){
        assertThrows(IllegalArgumentException.class,
                () -> new FSMDescriptionTweaker(2,3,null,1));
        assertThrows(IllegalArgumentException.class,
                () -> new FSMDescriptionTweaker(0,3, FSMDescription.Sensor.ALL_SENSORS,1));
        assertThrows(IllegalArgumentException.class,
                () -> new FSMDescriptionTweaker(2,0, FSMDescription.Sensor.ALL_SENSORS,1));
        assertThrows(IllegalArgumentException.class,
                () -> new FSMDescriptionTweaker(2,3, FSMDescription.Sensor.ALL_SENSORS,-1));
    }

    @Test
    public void getEnvironmentDescription(){
        Services.register(IRandomizer.class,new Randomizer());
        FSMDescriptionTweaker tweaker= new FSMDescriptionTweaker(2, 3, FSMDescription.Sensor.ALL_SENSORS,1);
        FSMTransitionCounterDescription description1= (FSMTransitionCounterDescription)tweaker.getEnvironmentDescription();

        //test that the description has the correct properties
        assertEquals(2, description1.getMoves().length);
        assertEquals(3, description1.getNumStates());
    }

    @Test
    public void getEnvironmentDescriptionNewDescription(){
        Services.register(IRandomizer.class,new Randomizer());
        FSMDescriptionTweaker tweaker= new FSMDescriptionTweaker(8, 15, FSMDescription.Sensor.ALL_SENSORS,1);
        FSMTransitionCounterDescription description1= (FSMTransitionCounterDescription)tweaker.getEnvironmentDescription();

        //test that the description has the correct properties
        assertEquals(8, description1.getMoves().length);
        assertEquals(15, description1.getNumStates());

        FSMTransitionCounterDescription description2= (FSMTransitionCounterDescription)tweaker.getEnvironmentDescription();

        //check that they have sme moves and states
        assertArrayEquals(description1.getMoves(),description2.getMoves());
        assertEquals(description1.getNumStates(),description2.getNumStates());

        //check that there are two differences between the transitions of d1 and d2
        int numDiff= 0;
        for(int state=0;state<description1.getNumStates();state++){
            for(Move move : description1.getMoves()) {
                int trans1 = description1.transition(state, move);
                int trans2 = description2.transition(state, move);

                if(trans1 != trans2) numDiff++;
            }
        }

        assertEquals(2,numDiff);
    }
}
