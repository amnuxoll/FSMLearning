package environments.meta;

import environments.fsm.FSMDescription;
import environments.fsm.FSMDescriptionProvider;
import framework.*;

import java.util.ArrayList;
import java.util.LinkedList;

public class MetaEnvironmentDescription implements IEnvironmentDescription {
    private IEnvironmentDescriptionProvider environmentDescriptionProvider;
    private IEnvironmentDescription currDescription;
    private IRandomizer randomizer;

    //how many transitions the agent took to reach the goal
    private LinkedList<Integer> successQueue= new LinkedList<Integer>();

    //number of moves since last goal
    private int transitionCounter= 0;
    private MetaConfiguration config;

    public MetaEnvironmentDescription
            (IEnvironmentDescriptionProvider environmentDescriptionProvider, IRandomizer randomizer, MetaConfiguration config){

        if(environmentDescriptionProvider == null){
            throw new IllegalArgumentException("environmentDescriptionProvider cannot be null");
        }
        if(randomizer == null){
            throw new IllegalArgumentException("randomizer cannot be null");
        }
        if(config == null){
            throw new IllegalArgumentException("config cannot be null");
        }

        this.environmentDescriptionProvider= environmentDescriptionProvider;
        this.randomizer= randomizer;

        this.currDescription=
                environmentDescriptionProvider.getEnvironmentDescription(randomizer);

        this.config= config;
    }

    @Override
    public Move[] getMoves() {
        return currDescription.getMoves();
    }

    /**
     * query to determine result of a move
     * @param currentState The state to transition from.
     * @param move The move to make from the current state.
     * @return The state resulting from the move from currentState
     */
    @Override
    public int transition(int currentState, Move move) {
        //check for illegal arg
        if(move == null){
            throw new IllegalArgumentException("move cannot be null");
        }
        if(currentState < 0 || currentState >= currDescription.getNumStates()) {
            throw new IllegalArgumentException("currentState out of range");
        }

        transitionCounter++;
        return currDescription.transition(currentState,move);
    }

    @Override
    public boolean isGoalState(int state) {
        boolean isGoal= currDescription.isGoalState(state);

        if(isGoal){
            //makeNewDescription relies on transitionCounter,
            //so do this first
            makeNewDescription();
            transitionCounter= 0;
        }

        return isGoal;
    }

    @Override
    public int getNumStates() {
        return currDescription.getNumStates();
    }

    @Override
    public void applySensors(int state, SensorData sensorData) {
        if(sensorData == null) throw new IllegalArgumentException("sensor data cannot be null.");
        if(state <0 || state >= currDescription.getNumStates()) throw new IllegalArgumentException("state number out of range.");

        currDescription.applySensors(state,sensorData);
    }

    /**
     * takes the average of the last successQueueSize transition counts
     * and update the fsm description is the target threshold is reached
     */
    private void makeNewDescription(){
        successQueue.add(transitionCounter);
        while(successQueue.size() > config.getSuccessQueueMaxSize()){
            successQueue.remove();
        }

        int average= averageEnqueuedSuccesses();

        //if the average is less than our threshold and we have collected enough data
        if(average < config.getStepThreshold() &&
            successQueue.size() >= config.getSuccessQueueMaxSize()){

            //make a new environment
            currDescription=
                    environmentDescriptionProvider.getEnvironmentDescription(randomizer);
            //and clear the queue
            successQueue.clear();
        }
    }

    /**
     *
     * @return the average of the number of moves to goal in the successQueue
     *          or -1 if the queue is empty
     */
    public int averageEnqueuedSuccesses(){
        if(successQueue.size() == 0) return -1;

        int sum= 0;
        for(Integer tc : successQueue){
            sum+= tc;
        }
        return sum/successQueue.size();
    }

    /**
     *
     * @return number of moves since last goal
     */
    public int getTransitionCounter() {
        return transitionCounter;
    }

    /**
     *
     * @return the last several number of moves it took to reach the goal
     */
    public LinkedList<Integer> getSuccessQueue() {
        return successQueue;
    }
}