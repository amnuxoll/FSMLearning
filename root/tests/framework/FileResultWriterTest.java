package framework;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileResultWriterTest {

    @Test
    public void testConstructorNullNameThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new FileResultWriter(null));
    }


    @Test
    public void testConstructorEmptyNameThrowsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new FileResultWriter(""));
    }

    @Test
    public void testGetFileName()
    {
        String fileName = "output.csv";
        try {
            FileResultWriter writer = new FileResultWriter(fileName);
            assertEquals(fileName, writer.getFileName());
        }
        finally {
            try {
                new File(fileName).delete();
            } catch (Exception ex){

            }
        }
    }

    @Test
    public void testLogStepsToGoalSingleStep()
    {
        String fileName = "output.csv";
        try {
            FileResultWriter writer = new FileResultWriter(fileName);
            writer.logStepsToGoal(13);
            try {
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                assertEquals(1, lines.size());
                String firstLine = lines.get(0);
                assertEquals("13,", firstLine);
            } catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        } finally {
            try {
                new File(fileName).delete();
            } catch (Exception ex){

            }
        }
    }

    @Test
    public void testLogStepsToGoalMultipleSteps()
    {
        String fileName = "output.csv";
        try {
            FileResultWriter writer = new FileResultWriter(fileName);
            writer.logStepsToGoal(13);
            writer.logStepsToGoal(7);
            writer.logStepsToGoal(2);
            writer.logStepsToGoal(15);
            try {
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                assertEquals(1, lines.size());
                String firstLine = lines.get(0);
                assertEquals("13,7,2,15,", firstLine);
            } catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        } finally {
            try {
                new File(fileName).delete();
            } catch (Exception ex){

            }
        }
    }

    @Test
    public void testBeginNewRunSingleRun()
    {
        String fileName = "output.csv";
        try {
            FileResultWriter writer = new FileResultWriter(fileName);
            writer.beginNewRun();
            try {
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                assertEquals(1, lines.size());
                String firstLine = lines.get(0);
                assertEquals("", firstLine);
            } catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        } finally {
            try {
                new File(fileName).delete();
            } catch (Exception ex){

            }
        }
    }

    @Test
    public void testBeginNewRunMultipleRuns()
    {
        String fileName = "output.csv";
        try {
            FileResultWriter writer = new FileResultWriter(fileName);
            writer.beginNewRun();
            writer.beginNewRun();
            writer.beginNewRun();
            writer.beginNewRun();
            try {
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                assertEquals(4, lines.size());
                for (String line : lines)
                {
                    assertEquals("", line);
                }
            } catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        } finally {
            try {
                new File(fileName).delete();
            } catch (Exception ex){

            }
        }
    }

    @Test
    public void testCompleteFinalizesStandardReportRun()
    {
        String fileName = "output.csv";
        try {
            FileResultWriter writer = new FileResultWriter(fileName);
            for (int runs = 0; runs < 10; runs++)
            {
                writer.beginNewRun();
                for (int goals = 0; goals < 15; goals++)
                {
                    writer.logStepsToGoal(goals);
                }
            }
            writer.complete();
            try {
                List<String> expected = Arrays.asList(
                        "",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,",
                        "sum(A2:A11),sum(B2:B11),sum(C2:C11),sum(D2:D11),sum(E2:E11),sum(F2:F11),sum(G2:G11),sum(H2:H11),sum(I2:I11),sum(J2:J11),sum(K2:K11),sum(L2:L11),sum(M2:M11),sum(N2:N11),sum(O2:O11),",
                        ",,,sum(A12:G12),sum(B12:H12),sum(C12:I12),sum(D12:J12),sum(E12:K12),sum(F12:L12),sum(G12:M12),sum(H12:N12),sum(I12:O12),,,,"
                );
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                assertArrayEquals(expected.toArray(), lines.toArray());
            } catch (IOException ex)
            {
                fail(ex.getMessage());
            }
        } finally {
            try {
                new File(fileName).delete();
            } catch (Exception ex){

            }
        }
    }

    // TODO edge cases around condition where smoothing lines get added
    // TODO edge cases around news with different numbers of goals found.
}
