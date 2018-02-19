/**
 * Sensor class -
 *
 *  object which contains all sensors values
 *
 *@authors Emily Peterson & Andrew Ripple
 */


public class Sensors {
    Boolean GOAL_SENSOR; //currently on but handled uniquely bc its the goal
    Boolean EVEN_SENSOR; //currently on
    Boolean NEWSTATE_SENSOR;
    Boolean ISLOOP_SENSOR;
    Boolean ISNOISE_SENSOR;
    Integer STATENUM_SENSOR;
    Integer DISTANCE_SENSOR;
    Integer LIMITEDSTATENUM_SENSOR;
    Integer MODBUCKETS_SENSOR;


    //constructor - sets all sensors to -1 initially.
    public Sensors() {

        GOAL_SENSOR = false;
        EVEN_SENSOR = null;
        NEWSTATE_SENSOR = null;
        ISLOOP_SENSOR = null;
        ISNOISE_SENSOR = null;
        STATENUM_SENSOR = null;
        DISTANCE_SENSOR = null;
        MODBUCKETS_SENSOR = null;
        LIMITEDSTATENUM_SENSOR = null;
    }

    //copy constructor
    public Sensors( Sensors cpySensor){
        this.GOAL_SENSOR = cpySensor.GOAL_SENSOR;
        this.EVEN_SENSOR = cpySensor.EVEN_SENSOR;
        this.NEWSTATE_SENSOR = cpySensor.NEWSTATE_SENSOR;
        this.ISLOOP_SENSOR = cpySensor.ISLOOP_SENSOR;
        this.ISNOISE_SENSOR = cpySensor.ISNOISE_SENSOR;
        this.STATENUM_SENSOR = cpySensor.STATENUM_SENSOR;
        this.LIMITEDSTATENUM_SENSOR = cpySensor.LIMITEDSTATENUM_SENSOR;
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
        if (compareSensor.LIMITEDSTATENUM_SENSOR != this.LIMITEDSTATENUM_SENSOR){
            return false;
        }
        if (compareSensor.MODBUCKETS_SENSOR != this.MODBUCKETS_SENSOR){
            return false;
        }

        return true;
    }

    /**
     * equalsOrNull
     *
     * Returns true if sensors or equal or one sensor is null which we treat as a wildcard.
     * Null sensors match any sensors.
     */
    public boolean equalsOrNull(Sensors compareSensor){

        if (compareSensor.GOAL_SENSOR != this.GOAL_SENSOR ){ //if the sensors are unequal
            //do nothing if one or both are null
            if (compareSensor.GOAL_SENSOR == null || this.GOAL_SENSOR == null){}
            else { // neither are null, return false
                return false;
            }
        }
        if (compareSensor.EVEN_SENSOR != this.EVEN_SENSOR){
            if (compareSensor.EVEN_SENSOR == null || this.EVEN_SENSOR == null){ }
            else {
                return false;
            }
        }
        if (compareSensor.NEWSTATE_SENSOR != this.NEWSTATE_SENSOR){
            if (compareSensor.NEWSTATE_SENSOR == null || this.NEWSTATE_SENSOR == null){ }
            else {
                return false;
            }
        }
        if (compareSensor.ISLOOP_SENSOR != this.ISLOOP_SENSOR){
            if (compareSensor.ISLOOP_SENSOR == null || this.ISLOOP_SENSOR == null){ }
            else {
                return false;
            }
        }
        if (compareSensor.ISNOISE_SENSOR != this.ISNOISE_SENSOR)
        {
            if (compareSensor.ISNOISE_SENSOR == null || this.ISNOISE_SENSOR == null){ }
            else {
                return false;
            }
        }
        if (compareSensor.STATENUM_SENSOR != this.STATENUM_SENSOR){
            if (compareSensor.STATENUM_SENSOR == null || this.STATENUM_SENSOR == null){ }
            else {
                return false;
            }
        }
        if (compareSensor.LIMITEDSTATENUM_SENSOR != this.LIMITEDSTATENUM_SENSOR){
            if (compareSensor.LIMITEDSTATENUM_SENSOR == null || this.LIMITEDSTATENUM_SENSOR == null){ }
            else {
                return false;
            }
        }
        if (compareSensor.MODBUCKETS_SENSOR != this.MODBUCKETS_SENSOR){
            if (compareSensor.MODBUCKETS_SENSOR == null || this.MODBUCKETS_SENSOR == null){ }
            else {
                return false;
            }
        }

        return true;
    }


    /**
     *sensorRepresentation
     *
     * returns a String representation of the sensor object
     * even sensor as 1 for true, 0 for false
     */
    public String sensorRepresentation(){
        if (EVEN_SENSOR == true){
            return "1";
        }
        else{
            return "0";
        }
    }
}
