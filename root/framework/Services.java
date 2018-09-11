package framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Services {
    private static HashMap<Class, Object> services = new HashMap<>();

    public static <TServiceInterface, TService extends TServiceInterface> void register(Class<TServiceInterface> type, TService instance)
    {
        Services.services.put(type, instance);
    }

    public static <TServiceInterface> TServiceInterface retrieve(Class<TServiceInterface> type)
    {
        return (TServiceInterface) Services.services.get(type);
    }
}
