import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PlayFileLogger {

    private Agent agent;

    private String playfileName = "agent.playfile";

    public PlayFileLogger(Agent agentToLog)
    {
        this.agent = agentToLog;
    }

    public void logNewSequenceAttempt(String sequence)
    {
        this.writeToLog("AGENT: " + this.agent.toDOT());
        this.writeToLog("SEQUENCE: " + sequence);
    }

    public void logEnvironmentState(Sensors sensors)
    {
        this.writeToLog("ENVIRONMENT: " + this.agent.env.toDOT());
        this.writeToLog("SENSORS: " + sensors.sensorRepresentation());
    }

    public void logMessage(String message)
    {
        this.writeToLog("MESSAGE: " + message);
    }

    private void writeToLog(String itemToWrite)
    {
        BufferedWriter bw = null;

        try {
            // APPEND MODE SET HERE
            bw = new BufferedWriter(new FileWriter(this.playfileName, true));
            bw.write(itemToWrite);
            bw.newLine();
            bw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {                       // always close the file
            if (bw != null) try {
                bw.close();
            } catch (IOException ioe2) {
                // just ignore it
            }
        } // end try/catch/finally
    }
}
