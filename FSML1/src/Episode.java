/**
 * Episode
 *
 * Represents an episode in the agents episodic memory
 */
public class Episode { 
	
	public char command;     //what the agent did
	public Sensors sensorValue; //what the agent sensed
	public int currentState;

	public Episode(char cmd, Sensors sensor, int currState) {
		command = cmd;
		sensorValue = sensor;
		currentState = currState;

	}

    public String toString() {
        return "[Cmd: "+command+"| Sensor: "+sensorValue+"]";
    }
}
