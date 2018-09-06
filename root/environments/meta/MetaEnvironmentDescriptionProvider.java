package environments.meta;

import framework.IEnvironmentDescription;
import framework.IEnvironmentDescriptionProvider;
import framework.IRandomizer;

public class MetaEnvironmentDescriptionProvider implements IEnvironmentDescriptionProvider {
    private IEnvironmentDescriptionProvider environmentDescriptionProvider;
    private MetaConfiguration config;

    public MetaEnvironmentDescriptionProvider
            (IEnvironmentDescriptionProvider environmentDescriptionProvider, MetaConfiguration config){
        if(environmentDescriptionProvider == null || config == null)
            throw new IllegalArgumentException("Arguments cannot be null");
        this.environmentDescriptionProvider= environmentDescriptionProvider;
        this.config= config;
    }

    @Override
    public IEnvironmentDescription getEnvironmentDescription(IRandomizer randomizer) {
        return new MetaEnvironmentDescription(environmentDescriptionProvider, randomizer, config);
    }
}
