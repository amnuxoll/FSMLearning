package experiments;

import agents.marz.MaRzAgent;
import environments.fsm.FiniteStateMachine;
import framework.ConsoleResultWriter;
import framework.IResultWriter;
import framework.TestRun;
import agents.nsm.*;
import framework.TestSuite;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class Runner {

    private static TestSuite Suite1 = new TestSuite(
            50,1000,
            new ConsoleResultWriter(),
            () -> new MaRzAgent(),
            () -> new FiniteStateMachine(3, 30),
            TestRun::new
            );

    public static void main(String[] args)
    {
        Runner.Suite1.run();
    }
}
