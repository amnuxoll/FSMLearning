package framework;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileResultWriterProvider implements IResultWriterProvider {
    private final static String outputRootDirectory = Paths.get(System.getProperty("user.home"), "fsm_output").toString();

    private String timestampDirectory;

    public FileResultWriterProvider()
    {
        Date myDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = sdf.format(myDate);
        this.timestampDirectory = Paths.get(FileResultWriterProvider.outputRootDirectory, dateString).toString();
    }

    @Override
    public IResultWriter getResultWriter(String agent) {
        if (agent == null)
            throw new IllegalArgumentException("agent cannot be null");
        if (agent == "")
            throw new IllegalArgumentException("agent cannot be empty");
        return new FileResultWriter(Paths.get(this.timestampDirectory,  agent + ".csv").toString());
    }
}
