package environments.meta;

import framework.IEnvironmentDescription;
import framework.IEnvironmentDescriptionProvider;
import framework.IRandomizer;

public class MetaEnvironmentDescriptionProvider implements IEnvironmentDescriptionProvider {
    private IEnvironmentDescriptionProvider environmentDescriptionProvider;
    private MetaConfiguration config;

    public MetaEnvironmentDescriptionProvider
            (IEnvironmentDescriptionProvider environmentDescriptionProvider, MetaConfiguration config){
        if(environmentDescriptionProvider == null){
            throw new IllegalArgumentException("environmentDescriptionProvider cannot be null");
        }
        if(config == null){
            throw new IllegalArgumentException("config cannot be null");
        }

        this.environmentDescriptionProvider= environmentDescriptionProvider;
        this.config= config;
    }

    @Override
    public IEnvironmentDescription getEnvironmentDescription(IRandomizer randomizer) {
        if(randomizer == null){
            throw new IllegalArgumentException("randomizer cannot be null");
        }
        return new MetaEnvironmentDescription(environmentDescriptionProvider, randomizer, config);
    }
}
