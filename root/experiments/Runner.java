package experiments;

import framework.TestRun;

import java.util.ArrayList;

public class Runner {

    private static TestRun[] ExperimentSet1 = new TestRun[]
            {
                   // new TestRun()
            };

    public static void main(String[] args)
    {
        for (TestRun testRun : Runner.ExperimentSet1)
        {
            testRun.execute();
        }
    }
}
