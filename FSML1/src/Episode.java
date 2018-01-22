/**
 * Episode
 *
 * Represents an episode in the agents episodic memory
 */
public class Episode { 
	
	public char command;     //what the agent did
	public int sensorValue[];  //what the agent sensed

	public Episode(char cmd, int[] sensors) {
		command = cmd;
		for(int i = 0; i < sensors.length; i++)
		{
			sensorValue[i] = sensors[i];
		}

	}

    public String toString() {
        return "[Cmd: "+command+"| Sensor: "+sensorValue+"]";
    }
}
