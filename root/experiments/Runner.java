package experiments;

import agents.marz.MaRzAgent;
import environments.fsm.FiniteStateMachine;
import framework.*;

import java.net.URI;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Runner {

    public static void main(String[] args)
    {
        Runner.Suite1.run();
    }

    private static TestSuite Suite1 = new TestSuite(
            50,1000,
            new FileResultWriter(Runner.getOutputFileName("Suite1")),
            () -> new MaRzAgent(),
            () -> new FiniteStateMachine(3, 30),
            TestRun::new
            );

    private static String getOutputFileName(String testSuiteDirectory)
    {
        Date myDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = sdf.format(myDate);
        String outputDirectory = Paths.get(System.getProperty("user.home"), "fsm_output").toString();
        return Paths.get(outputDirectory, testSuiteDirectory, dateString + ".csv").toString();
    }
}
