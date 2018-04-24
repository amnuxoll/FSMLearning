package testMarzfromOldSource;
/*
 * Sensors
 * 
 * holds sensor information
 * 
 * sensors can be commented out in the copy constructor so agent's don't
 * 	"see" those sensors. Since the sensors (except the goal) are first set to 
 * 	null the .equals method will still apply.
 * 
 */
public class Sensors {
	Boolean GOAL_SENSOR;
	Boolean EVEN_SENSOR; //1 is even, 0 is odd
	Integer STATE_NUMBER;
	
	public Sensors(){
		GOAL_SENSOR = false;
		EVEN_SENSOR = null;
		STATE_NUMBER = null;
	}
	public Sensors(Sensors sensorToCopyFrom){
		if (sensorToCopyFrom.GOAL_SENSOR) this.GOAL_SENSOR = true;
		else this.GOAL_SENSOR = false;
		if (sensorToCopyFrom.EVEN_SENSOR) this.EVEN_SENSOR = true;
		else this.EVEN_SENSOR = false;
		this.STATE_NUMBER = (int)sensorToCopyFrom.STATE_NUMBER;
	}

	public boolean Equals(Sensors matchTheseSensors){
		boolean equal = true;
		if (!(this.GOAL_SENSOR == matchTheseSensors.GOAL_SENSOR)){
			equal = false;
		}
		if (!(this.EVEN_SENSOR == matchTheseSensors.EVEN_SENSOR)){
			equal = false;
		}
		/*//taken out because we just want state number for debug
		 if (!(this.STATE_NUMBER == matchTheseSensors.STATE_NUMBER)){
			equal = false;
		}
		 */
		return equal;
	}
	public String sensorRepresentation(){
		if (this.EVEN_SENSOR) return "1";
		return "0";
	}
	public String toString(){
		String returnStr = this.sensorRepresentation() + "(" + Integer.toString(STATE_NUMBER) + ")";
		if (GOAL_SENSOR){
			returnStr = returnStr + " | ";
		}
		return returnStr;
	}
}
