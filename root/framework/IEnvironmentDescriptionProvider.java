package framework;

/**
 * An IEnvironmentDescriptionProvider generates new {@link IEnvironmentDescription} for consecutive test runs.
 * @author Zachary Paul Faltersack
 * @version 0.95
 */
public interface IEnvironmentDescriptionProvider {
    /**
     * Get a new {@link IEnvironmentDescription}.
     * @param randomizer A {@link IRandomizer} that can be used to get random data for environment description generation.
     * @return The new {@link IEnvironmentDescription}.
     */
    IEnvironmentDescription getEnvironmentDescription(IRandomizer randomizer);
}
