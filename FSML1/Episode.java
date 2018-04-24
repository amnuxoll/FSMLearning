
/**
 * Episode
 *
 * Represents an episode in the agents episodic memory
 */
public class Episode { 
	
	public char command;     //what the agent did
	public Sensors sensorValue;  //what the agent sensed

	public Episode(char cmd, Sensors sensor) {
		command = cmd;
		sensorValue = new Sensors(sensor); //deep copy of sensors we are given

	}

    public String toString() {
        return "[Cmd: "+command+"| Sensor: "+sensorValue+"]";
    }
}
