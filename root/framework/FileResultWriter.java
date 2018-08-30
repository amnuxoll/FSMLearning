package framework;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileResultWriter implements IResultWriter {

    private String fileName;

    private FileWriter fileWriter;

    private int currentNumberOfGoals = 0;
    private int maxNumberOfGoals = 0;
    private int numberOfRuns = 0;

    public FileResultWriter(String outputFile)
    {
        if (outputFile == null)
            throw new IllegalArgumentException("outputFile cannot be null");
        if (outputFile == "")
            throw new IllegalArgumentException("outputFile cannot be empty");
        try {
            this.fileName = outputFile;
            File file = new File(outputFile);
            this.fileWriter = new FileWriter(file);
        } catch (IOException ex)
        {
            // do anything here?
        }
    }

    public String getFileName()
    {
        return this.fileName;
    }

    @Override
    public void logStepsToGoal(int stepsToGoal) {
        this.currentNumberOfGoals++;
        try {
            this.fileWriter.write(stepsToGoal + ",");
            this.fileWriter.flush();
        } catch (IOException ex)
        {
            // do anything here?
        }
    }

    @Override
    public void beginNewRun() {
        this.numberOfRuns++;
        this.maxNumberOfGoals = Math.max(this.maxNumberOfGoals, this.currentNumberOfGoals);
        this.currentNumberOfGoals = 0;
        try {
            this.fileWriter.write("\n");
            this.fileWriter.flush();
        } catch (IOException ex)
        {
            // do anything here?
        }
    }

    @Override
    public void complete() {
        try {
            this.fileWriter.write("\n");
            // Write out the basic goal sums
            for (int i = 0; i < this.maxNumberOfGoals; i++)
            {
                char column = (char)('A' + i);
                int startRow = 2;
                int endRow = startRow + this.numberOfRuns - 1;
                this.fileWriter.write("sum(" + column + startRow + ":" + column + endRow + "),");
            }
            this.fileWriter.write("\n");
            this.fileWriter.write(",,,");

            // Write out the smoothing row
            for (int i = 3; i < this.maxNumberOfGoals - 3; i++)
            {
                char leftColumn = (char)('A' + i - 3);
                char rightColumn = (char)('A' + i + 3);
                int row = 2 + this.numberOfRuns;
                this.fileWriter.write("sum(" + leftColumn + row + ":" + rightColumn + row + "),");
            }
            this.fileWriter.write(",,,");
            this.fileWriter.close();
        }catch (IOException ex)
        {
            // do anything here?
        }
    }
}
