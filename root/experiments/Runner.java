package experiments;

import agents.marz.MaRzAgentProvider;
import agents.marz.nodes.SuffixNodeProvider;
import agents.nsm.NSMAgentProvider;
import environments.fsm.FSMDescription;
import environments.fsm.FSMDescriptionProvider;
import environments.meta.FSMDescriptionTweaker;
import environments.meta.MetaConfiguration;
import environments.meta.MetaEnvironmentDescriptionProvider;
import framework.*;

import java.util.EnumSet;

public class Runner {

    private static TestSuite MaRzFSM = new TestSuite(
            TestSuiteConfiguration.MEDIUM,
            new FileResultWriterProvider(),
            new FSMDescriptionProvider(3, 15, FSMDescription.Sensor.NO_SENSORS),
            new IAgentProvider[] {
                    new MaRzAgentProvider<>(new SuffixNodeProvider())
            }
    );

    private static TestSuite MaRzMeta = new TestSuite(
            TestSuiteConfiguration.QUICK,
            new FileResultWriterProvider(),
            new MetaEnvironmentDescriptionProvider(
                    new FSMDescriptionTweaker(3,15,FSMDescription.Sensor.NO_SENSORS,1),
                    new MetaConfiguration(12)),
            new IAgentProvider[] {
                    new MaRzAgentProvider<>(new SuffixNodeProvider())
            }
    );

    private static TestSuite Suite2 = new TestSuite(
            TestSuiteConfiguration.FULL,
            new FileResultWriterProvider(),
            new MetaEnvironmentDescriptionProvider(new FSMDescriptionProvider(2, 5, EnumSet.of(FSMDescription.Sensor.EVEN_ODD)), MetaConfiguration.DEFAULT),
            new IAgentProvider[] {
                    new NSMAgentProvider()
            }
    );

    public static void main(String[] args) {
        /**
         * args should be:
         * 0: tweak point
         * 1: numSwaps
         */

        int tweakPoint= Integer.parseInt(args[0]);
        int numSwaps= Integer.parseInt(args[1]);

        try {
            Services.register(IRandomizer.class, new Randomizer());
            TestSuiteConfiguration testConfig= new TestSuiteConfiguration(100,5000);
            MetaEnvironmentDescriptionProvider provider=
                    new MetaEnvironmentDescriptionProvider(
                            new FSMDescriptionTweaker(3,30,FSMDescription.Sensor.NO_SENSORS,numSwaps),
                            new MetaConfiguration(tweakPoint)
                    );
            TestSuite suite=
                    new TestSuite(testConfig,
                            new FileResultWriterProvider(),
                            provider,
                            new IAgentProvider[]{new MaRzAgentProvider<>(new SuffixNodeProvider())}
                            );

            suite.run();
        } catch (Exception ex)
        {
            System.out.println("Runner failed with exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
