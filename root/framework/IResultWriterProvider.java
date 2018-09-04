package framework;

public interface IResultWriterProvider {
    IResultWriter getResultWriter(String agent);
}
