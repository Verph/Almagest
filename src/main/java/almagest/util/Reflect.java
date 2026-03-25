package almagest.util;

import java.lang.reflect.Field;

public class Reflect
{
    @SuppressWarnings("unchecked")
    public static <T> T get(Object instance, String fieldName)
    {
        try
        {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(instance);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to reflect field: " + fieldName, e);
        }
    }

    public static void set(Object instance, String fieldName, Object value)
    {
        try
        {
            Field f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(instance, value);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to reflect field: " + fieldName, e);
        }
    }
}
