package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.JsonElement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.client.RenderEventHandler;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

import static almagest.client.data.PlanetDataManager.*;

public class GalaxyDataManager
{
    public static GalaxyDataManager GALAXY_DATA;
    public final List<GalaxyData> galaxies;

    public static final ConcurrentMap<Integer, Vec3> POSITION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> POSITION_ADJ_CACHE = new ConcurrentHashMap<>();

    public GalaxyDataManager()
    {
        this.galaxies = initData("stellar_data", "galaxies");
    }

    public List<GalaxyData> initData(String directory, String name)
    {
        List<GalaxyData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<GalaxyDataList> dataResult = GalaxyDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(galaxyDataList -> {
            data.addAll(galaxyDataList.galaxies());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read galaxy data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        galaxies.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<GalaxyDataList> dataResult = GalaxyDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(planetDataList -> {
                if (planetDataList.replace()) {
                    galaxies.clear();
                }
                galaxies.addAll(planetDataList.galaxies());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read galaxy data {}", partialResult.message());
            });
        }
    }*/

    public static void clearCaches()
    {
        POSITION_CACHE.clear();
        POSITION_ADJ_CACHE.clear();
    }

    public List<GalaxyData> get()
    { 
        return galaxies;
    }

    public Optional<GalaxyData> get(String name)
    { 
        return galaxies.stream()
                .filter(galaxy -> !name.equals("Unknown") && !galaxy.names().getAlphaNameOrDefault().equals("Unknown") && (galaxy.names().alphanumericName().equals(name) || galaxy.names().name().equals(name)))
                .findFirst();
    }

    /**
     * List of galaxies data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param galaxies List of galaxies.
     */
    public record GalaxyDataList(boolean replace, List<GalaxyData> galaxies)
    {
        public static final Codec<GalaxyDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(GalaxyDataList::replace),
                GalaxyData.CODEC.listOf().fieldOf("galaxies").forGetter(GalaxyDataList::galaxies)
        ).apply(instance, GalaxyDataList::new));
    }

    /**
     * Data for galaxies.
     *
     * @param id Unique identifier for the galaxy.
     * @param alphanumericName The alphanumeric name of the galaxy.
     * @param name The common name of the galaxy.
     * @param constellation The constellation the galaxy is located in.
     * @param diameter The diameter in light years (LY).
     * @param radius The radius in light years (LY).
     * @param rightAscension The right ascension in degrees.
     * @param declination The declination in degrees.
     * @param distanceFromEarth The distance from Earth in megaparsecs.
     * @param distanceFromMilkyWay The distance from the Milky Way in megaparsecs.
     * @param distanceFromSun The distance from the Sun in megaparsecs.
     * @param redshift The redshift (dimensionless).
     * @param pos The position coordinates of the galaxy (e.g., x, y, z).
     */
    public record GalaxyData(int id, Names names, String constellation, double apparentMagnitude, double diameter, double radius, double rightAscension, double declination, double distanceFromEarth, double distanceFromMilkyWay, double distanceFromSun, double redshift, List<Double> pos, String texture, boolean customModel)
    {
        public static final Codec<GalaxyData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(GalaxyData::id),
            Names.CODEC.fieldOf("Names").forGetter(GalaxyData::names),
            Codec.STRING.optionalFieldOf("Constellation", "Unknown").forGetter(GalaxyData::constellation),
            Codec.DOUBLE.optionalFieldOf("ApparentMagnitude", 5.0).forGetter(GalaxyData::apparentMagnitude),
            Codec.DOUBLE.optionalFieldOf("Diameter", 3000.0).forGetter(GalaxyData::diameter),
            Codec.DOUBLE.optionalFieldOf("Radius", 0.0).forGetter(GalaxyData::radius),
            Codec.DOUBLE.optionalFieldOf("RightAscension", 0.0).forGetter(GalaxyData::rightAscension),
            Codec.DOUBLE.optionalFieldOf("Declination", 0.0).forGetter(GalaxyData::declination),
            Codec.DOUBLE.optionalFieldOf("DistanceFromEarth", 500.0).forGetter(GalaxyData::distanceFromEarth),
            Codec.DOUBLE.optionalFieldOf("DistanceFromMilkyWay", 500.0).forGetter(GalaxyData::distanceFromMilkyWay),
            Codec.DOUBLE.optionalFieldOf("DistanceFromSun", 500.0).forGetter(GalaxyData::distanceFromSun),
            Codec.DOUBLE.optionalFieldOf("Redshift", 0.0).forGetter(GalaxyData::redshift),
            Codec.DOUBLE.listOf().optionalFieldOf("Pos", List.of(0.0, 0.0, 0.0)).forGetter(GalaxyData::pos),
            Codec.STRING.optionalFieldOf("Texture", "Unknown").forGetter(GalaxyData::texture),
            Codec.BOOL.optionalFieldOf("CustomModel", false).forGetter(GalaxyData::customModel)
        ).apply(instance, GalaxyData::new));

        public int getID()
        {
            return this.id();
        }

        public Vec3 getPos()
        {
            double x = this.pos().get(0);
            double y = this.pos().get(1);
            double z = this.pos().get(2);
            Vec3 pos = new Vec3(x, y, z);
            Vec3 posAdd = pos.normalize().scale(Config.COMMON.nebulaDistanceAdd.get());
            return pos.add(posAdd).scale(Config.COMMON.nebulaDistanceMult.get());
        }

        public Vec3 getCachedPos()
        {
            final int id = this.id();
            if (POSITION_CACHE.containsKey(id))
            {
                return POSITION_CACHE.get(id);
            }
            //Vec3 pos = AHelpers.yRot(getPos(), Math.toRadians(27.0D));
            Vec3 pos = getPos();
            POSITION_CACHE.put(id, pos);
            return pos;
        }

        public Vec3 getAdjustedPos(boolean updateCache)
        {
            double i = OBSERVER_INCLINATION;
            double s = OBSERVER_SEASON;
            double l = RenderEventHandler.latitude;
            double d = OBSERVER_TIME_OF_DAY + 1.22173047639603065D;

            Vec3 pos = getCachedPos();
            pos = StarDataManager.getRotation(pos);
            pos = StarDataManager.getRotation2(pos);
            pos = StarDataManager.getRotation3(pos);
            pos = rotZ(d, pos);
            pos = rotX(Math.toRadians(270.0D), pos);
            pos = rotZ(i, pos);
            pos = rotX(-l + s, pos);
            if (updateCache)
            {
                POSITION_ADJ_CACHE.put(this.id(), pos);
            }
            return pos;
        }

        public Vec3 getCachedAdjPos(boolean updateCache)
        {
            final int id = this.id();
            if (POSITION_ADJ_CACHE.containsKey(id))
            {
                return POSITION_ADJ_CACHE.get(id);
            }
            return getAdjustedPos(updateCache);
        }

        public ResourceLocation getTexture()
        {
            return AHelpers.identifier("textures/galaxies/" + this.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT).replace(" ", "_").replace("+", "_") + ".png");
        }
    }

    /**
     * Names associated with a galaxy.
     *
     * @param alphanumericName The alphanumeric name of the galaxy.
     * @param name The common name of the galaxy.
     */
    public record Names(String alphanumericName, String name)
    {
        public static final Codec<Names> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("AlphanumericName", "Unknown").forGetter(Names::alphanumericName),
            Codec.STRING.optionalFieldOf("Name", "Unknown").forGetter(Names::name)
        ).apply(instance, Names::new));

        public String getAlphaNameOrDefault()
        {
            return this.alphanumericName.equals("Unknown") ? this.name : this.alphanumericName;
        }
    }
}
