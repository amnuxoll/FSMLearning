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
    int STATENUM_SENSOR;

    //constructor - sets all sensors to -1 initially.
    public Sensors() {

        GOAL_SENSOR = false;
        EVEN_SENSOR = false;
        NEWSTATE_SENSOR = false;
        ISLOOP_SENSOR = false;
        STATENUM_SENSOR = -1;
    }

    //copy constructor
    public Sensors( Sensors cpySensor){
        this.GOAL_SENSOR = cpySensor.GOAL_SENSOR;
        this.EVEN_SENSOR = cpySensor.EVEN_SENSOR;
        this.NEWSTATE_SENSOR = cpySensor.NEWSTATE_SENSOR;
        this.ISLOOP_SENSOR = cpySensor.ISLOOP_SENSOR;
        this.STATENUM_SENSOR = cpySensor.STATENUM_SENSOR;
    }

    /**
     * updateSensors
     *
     * changes the correct sensor value based on input
     * @param sensorInput: the sensor to be changed
     * @param sensorValue: the value the sensor will be changed to
     *
     */
    public void updateSensors( String sensorInput, boolean sensorValue){
        if (sensorInput.equals("GOAL_SENSOR")){
            GOAL_SENSOR = sensorValue;
        }
        else if (sensorInput.equals("EVEN_SENSOR")){
            EVEN_SENSOR = sensorValue;
        }
        else if (sensorInput.equals("NEWSTATE_SENSOR")){
            NEWSTATE_SENSOR = sensorValue;
        }
        else if (sensorInput.equals("ISLOOP_SENSOR")){
            ISLOOP_SENSOR = sensorValue;
        }
        else{//warning message if sensor name does not match existing sensor
            System.out.println("Warning, incorrect sensor name: " + sensorInput);
        }
    }

    /**
     * UpdateSensors -- overload for integer sensor values
     * @param sensorValue overloaded for integer sensor values
     */
    public void udpateSensors(  String sensorInput, int sensorValue){

        if (sensorInput.equals("STATENUM_SENSOR")) {
                STATENUM_SENSOR = sensorValue;
            }

            else {//warning message if sensor name does not match existing sensor
                System.out.println("Warning, incorrect sensor name: " + sensorInput);
            }
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
        if (compareSensor.STATENUM_SENSOR != this.STATENUM_SENSOR){
            return false;
        }

        return true;
    }

}
