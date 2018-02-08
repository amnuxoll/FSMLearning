/**
 * Sensor class -
 *
 *  object which contains all sensors values
 *
 *@authors Emily Peterson & Andrew Ripple
 */


public class Sensors {
    boolean GOAL_SENSOR;
    boolean EVEN_SENSOR;
    boolean NEWSTATE_SENSOR;
    boolean ISLOOP_SENSOR;
    boolean ISNOISE_SENSOR;
    int STATENUM_SENSOR;
    int DISTANCE_SENSOR;
    int MODBUCKETS_SENSOR;


    //constructor - sets all sensors to -1 initially.
    public Sensors() {

        GOAL_SENSOR = false;
        EVEN_SENSOR = false;
        NEWSTATE_SENSOR = false;
        ISLOOP_SENSOR = false;
        ISNOISE_SENSOR = false;
        STATENUM_SENSOR = -1;
        DISTANCE_SENSOR = -1;
        MODBUCKETS_SENSOR = -1;
    }

    //copy constructor
    public Sensors( Sensors cpySensor){
        this.GOAL_SENSOR = cpySensor.GOAL_SENSOR;
        //this.EVEN_SENSOR = cpySensor.EVEN_SENSOR;
       // this.NEWSTATE_SENSOR = cpySensor.NEWSTATE_SENSOR;
        //this.ISLOOP_SENSOR = cpySensor.ISLOOP_SENSOR;
        //this.ISNOISE_SENSOR = cpySensor.ISNOISE_SENSOR;
       // this.STATENUM_SENSOR = cpySensor.STATENUM_SENSOR;
        this.MODBUCKETS_SENSOR = cpySensor.MODBUCKETS_SENSOR;
    }

    /**
     * equals
     *
     * equals method for two sensors. Returns true if all sensor values are the same.
     */
    public boolean equals(Sensors compareSensor){

        if (compareSensor.GOAL_SENSOR != this.GOAL_SENSOR){
            return false;
        }
        if (compareSensor.EVEN_SENSOR != this.EVEN_SENSOR){
            return false;
        }
        if (compareSensor.NEWSTATE_SENSOR != this.NEWSTATE_SENSOR){
            return false;
        }
        if (compareSensor.ISLOOP_SENSOR != this.ISLOOP_SENSOR){
            return false;
        }
        if (compareSensor.ISNOISE_SENSOR != this.ISNOISE_SENSOR)
        {
            return false;
        }
        if (compareSensor.STATENUM_SENSOR != this.STATENUM_SENSOR){
            return false;
        }
        if (compareSensor.MODBUCKETS_SENSOR != this.MODBUCKETS_SENSOR){
            return false;
        }

        return true;
    }

}
