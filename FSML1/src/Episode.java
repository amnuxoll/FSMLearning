/**
 * Episode
 *
 * Represents an episode in the agents episodic memory
 */
public class Episode { 
	
	public char command;     //what the agent did
	public Sensors sensorValue = new Sensors();  //what the agent sensed

	public Episode(char cmd, Sensors sensor) {
		command = cmd;
		Sensors sensorValue = sensor;

	}

    public String toString() {
        return "[Cmd: "+command+"| Sensor: "+sensorValue+"]";
    }
}
