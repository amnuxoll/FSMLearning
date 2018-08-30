package experiments;

import agents.marz.MaRzAgent;
import environments.fsm.FiniteStateMachine;
import framework.*;

import java.net.URI;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Runner {

    private static String outputDirectory = Paths.get(System.getProperty("user.home"), "fsm_output").toString();

    private static TestSuite Suite1 = new TestSuite(
            50,1000,
            new FileResultWriter(Runner.getOutputFilePath("testSuite1")),
            () -> new MaRzAgent(),
            () -> new FiniteStateMachine(3, 30),
            TestRun::new
            );

    public static void main(String[] args)
    {
        Runner.Suite1.run();
    }

    private static String getOutputFilePath(String testSuiteDirectory)
    {
        Date myDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = sdf.format(myDate);
        return Paths.get(outputDirectory, testSuiteDirectory, dateString + ".csv").toString();
    }
}
