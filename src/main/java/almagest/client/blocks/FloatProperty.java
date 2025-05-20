package almagest.client.blocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("null")
public class FloatProperty extends Property<Float>
{
    public final ImmutableSet<Float> values;
    public final float min;
    public final float max;

    public FloatProperty(String name, float min, float max, float interval)
    {
        super(name, Float.class);
        if (max <= min)
        {
            throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
        }
        else
        {
            this.min = min;
            this.max = max;
            Set<Float> set = Sets.newHashSet();

            for (float i = min; i <= max; i += interval)
            {
                set.add(i);
            }

            this.values = ImmutableSet.copyOf(set);
        }
    }

    @Override
    public Collection<Float> getPossibleValues()
    {
        return this.values;
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other instanceof FloatProperty floatProperty && super.equals(other))
        {
            return this.values.equals(floatProperty.values);
        }
        return false;
    }

    @Override
    public int generateHashCode()
    {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public static FloatProperty create(String name, float min, float max)
    {
        return create(name, min, max, 1.0F);
    }

    public static FloatProperty create(String name, float min, float max, float interval)
    {
        return new FloatProperty(name, min, max, interval);
    }

    @Override
    public Optional<Float> getValue(String value)
    {
        try
        {
            Float flt = Float.valueOf(value);
            return flt >= this.min && flt <= this.max ? Optional.of(flt) : Optional.empty();
        }
        catch (NumberFormatException numberformatexception)
        {
            return Optional.empty();
        }
    }

    @Override
    public String getName(Float value)
    {
        return value.toString();
    }
}