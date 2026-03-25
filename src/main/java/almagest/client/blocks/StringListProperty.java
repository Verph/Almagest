package almagest.client.blocks;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.world.level.block.state.properties.Property;

@SuppressWarnings("null")
public class StringListProperty extends Property<String>
{
   private final Collection<String> values;
   /** Map of names to String values */
   private final Map<String, String> names = Maps.newHashMap();

   protected StringListProperty(String name, Collection<String> values)
   {
      super(name, String.class);
      this.values = values;

      for (String s : values)
      {
         if (this.names.containsKey(s))
         {
            throw new IllegalArgumentException("Multiple values have the same name '" + s + "'");
         }
         this.names.put(s, s);
      }
   }

   public Collection<String> getPossibleValues()
   {
      return this.values;
   }

   public Optional<String> getValue(String value)
   {
      return Optional.ofNullable(this.names.get(value));
   }

   /**
    * @return the name for the given value.
    */
   public String getName(String value)
   {
      return value;
   }

   @Override
   public boolean equals(Object other)
   {
      if (this == other)
      {
         return true;
      }
      else if (other instanceof StringListProperty stringListProperty && super.equals(other))
      {
         return this.values.equals(stringListProperty.values) && this.names.equals(stringListProperty.names);
      }
      return false;
   }

   @Override
   public int generateHashCode()
   {
      int i = super.generateHashCode();
      i = 31 * i + this.values.hashCode();
      return 31 * i + this.names.hashCode();
   }

   /**
    * Create a new StringListProperty with the specified values.
    */
   public static StringListProperty create(String name, String... values)
   {
      return create(name, Lists.newArrayList(values));
   }

   /**
    * Create a new StringListProperty with the specified values.
    */
   public static StringListProperty create(String name, Collection<String> values)
   {
      return new StringListProperty(name, values);
   }

   /**
    * Create a new StringListProperty with all values that match the given Predicate.
    */
   public static StringListProperty create(String name, Collection<String> values, Predicate<String> pFilter)
   {
      return create(name, values.stream().filter(pFilter).toList());
   }
}