package experiments;

import agents.nsm.NSMAgentProvider;
import environments.fsm.FSMDescription;
import environments.fsm.FSMDescriptionProvider;
import environments.meta.MetaConfiguration;
import environments.meta.MetaEnvironmentDescriptionProvider;
import framework.*;

import java.util.EnumSet;

public class Runner {

    private static TestSuiteConfiguration FullTest = new TestSuiteConfiguration(
            50,
            1000,
            true
    );

    private static TestSuiteConfiguration QuickTest = new TestSuiteConfiguration(
            10,
            100,
            true
    );

    private static TestSuite Suite1 = new TestSuite(
            Runner.FullTest,
            new FileResultWriterProvider(),
            new FSMDescriptionProvider(2, 5, EnumSet.of(FSMDescription.Sensor.EVEN_ODD)),
            new IAgentProvider[] {
                    new NSMAgentProvider()
            }
    );

    private static TestSuite Suite2 = new TestSuite(
            Runner.FullTest,
            new FileResultWriterProvider(),
            new MetaEnvironmentDescriptionProvider(new FSMDescriptionProvider(2, 5, EnumSet.of(FSMDescription.Sensor.EVEN_ODD)), MetaConfiguration.DEFAULT),
            new IAgentProvider[] {
                    new NSMAgentProvider()
            }
    );

    public static void main(String[] args)
    {
        Runner.Suite1.run();
    }

}
