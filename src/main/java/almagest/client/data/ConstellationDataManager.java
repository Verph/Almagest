package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

import almagest.Almagest;
import almagest.client.data.StarDataManager.StarData;
import almagest.util.DataHelpers;

public class ConstellationDataManager
{
    public static ConstellationDataManager CONSTELLATION_DATA;
    public final List<ConstellationData> constellations;

    public static final ConcurrentMap<Integer, List<List<StarData>>> CONSTELLATION_PAIRS_CACHE = new ConcurrentHashMap<>();

    public ConstellationDataManager()
    {
        this.constellations = initData("stellar_data", "constellations");
    }

    public List<ConstellationData> initData(String directory, String name)
    {
        List<ConstellationData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<ConstellationDataList> dataResult = ConstellationDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(constellationDataList -> {
            data.addAll(constellationDataList.constellations());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read constellation data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        constellations.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<ConstellationDataList> dataResult = ConstellationDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(constellationDataList -> {
                if (constellationDataList.replace()) {
                    constellations.clear();
                }
                constellations.addAll(constellationDataList.constellations());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read constellation data {}", partialResult.message());
            });
        }
    }*/

    public static void clearCaches()
    {
        CONSTELLATION_PAIRS_CACHE.clear();
    }

    public List<ConstellationData> get()
    { 
        return constellations;
    }

    /**
     * List of constellation data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param constellations List of constellations.
     */
    public record ConstellationDataList(boolean replace, List<ConstellationData> constellations)
    {
        public static final Codec<ConstellationDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(ConstellationDataList::replace),
                ConstellationData.CODEC.listOf().fieldOf("constellations").forGetter(ConstellationDataList::constellations)
        ).apply(instance, ConstellationDataList::new));
    }

    /**
     * Data for constellations.
     *
     * @param id Unique identifier for the constellation.
     * @param name The name of the constellation.
     * @param pairs List of pairs of stars in the constellation.
     */
    public record ConstellationData(int id, String name, List<List<String>> pairs)
    {
        public static final Codec<ConstellationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("ID", 0).forGetter(ConstellationData::id),
                Codec.STRING.optionalFieldOf("Name", "Unknown").forGetter(ConstellationData::name),
                Codec.list(Codec.list(Codec.STRING)).optionalFieldOf("Pairs", List.of()).forGetter(ConstellationData::pairs)
        ).apply(instance, ConstellationData::new));

        public List<List<StarData>> getPairs()
        {
            final int id = this.id();
            if (CONSTELLATION_PAIRS_CACHE.containsKey(id))
            {
                return CONSTELLATION_PAIRS_CACHE.get(id);
            }
            List<List<StarData>> pairs = new ArrayList<>();
            for (List<String> pair : this.pairs())
            {
                List<StarData> starPair = new ArrayList<>();
                for (String starName : pair)
                {
                    StarData star = StarDataManager.STAR_DATA.get(starName).orElse(null);
                    if (star != null)
                    {
                        starPair.add(star);
                    }
                }
                pairs.add(starPair);
            }
            CONSTELLATION_PAIRS_CACHE.put(id, pairs);
            return pairs;
        }
    }
}