package environments.fsm;

import framework.Move;
import framework.SensorData;

public class SensorFiniteStateMachine extends FiniteStateMachine {


    public SensorFiniteStateMachine(int alphabetSize, int numStates) {
        super(alphabetSize, numStates);
    }

    @Override
    public SensorData tick(Move move)
    {
        SensorData sensorData = super.tick(move);
        // apply additional sensor data here
        this.applyEvenOddSensor(sensorData);
        return sensorData;
    }

    private void applyEvenOddSensor(SensorData sensorData)
    {
        sensorData.setSensor("Even", this.currentState % 2 == 0);
    }
}
