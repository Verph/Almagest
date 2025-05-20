package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

import static almagest.client.data.PlanetDataManager.*;

public class MeteorSwarmDataManager
{
    public static MeteorSwarmDataManager METEOR_DATA;
    public final List<MeteorData> meteors;

    public static final ConcurrentMap<Integer, ConcurrentMap<Integer, Double>> METEOR_SEMIMAJOR_AXIS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, ConcurrentMap<Integer, Double>> METEOR_DIAMETER_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, ConcurrentMap<Integer, Double>> METEOR_ORBIT_PERIOD_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, ConcurrentMap<Integer, Vec3>> METEOR_RANDOM_ROTATION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, ConcurrentMap<Integer, String>> METEOR_TEXTURE_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> DENSITY_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Integer> POPULATION_CACHE = new ConcurrentHashMap<>();

    public MeteorSwarmDataManager()
    {
        this.meteors = initData("stellar_data", "meteors");
    }

    public List<MeteorData> initData(String directory, String name)
    {
        List<MeteorData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<MeteorDataList> dataResult = MeteorDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(meteorDataList -> {
            data.addAll(meteorDataList.meteors());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read meteor swarm data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        meteors.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<MeteorDataList> dataResult = MeteorDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(meteorDataList -> {
                if (meteorDataList.replace()) {
                    meteors.clear();
                }
                meteors.addAll(meteorDataList.meteors());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read meteor shower data {}", partialResult.message());
            });
        }
    }*/

    public static void clearCaches()
    {
        METEOR_SEMIMAJOR_AXIS_CACHE.clear();
        METEOR_DIAMETER_CACHE.clear();
        METEOR_ORBIT_PERIOD_CACHE.clear();
        METEOR_RANDOM_ROTATION_CACHE.clear();
        METEOR_TEXTURE_CACHE.clear();
        DENSITY_CACHE.clear();
        POPULATION_CACHE.clear();
    }

    public static void initCaches()
    {
        Almagest.LOGGER.info("Initializing meteor shower data caches.");
        List<MeteorData> bodies = MeteorSwarmDataManager.METEOR_DATA.get();
        if (!bodies.isEmpty())
        {
            for (MeteorData body : bodies)
            {
                body.getCachedParent();
                body.getEccentricity();
                body.getInclination();
                body.getAscendingNodeLongitude();
                body.getPeriapsisLongitude();
                body.getSemimajorAxis();
                body.getOrbitalPeriod();
                body.getMeanLongitude();
                body.getCachedDensity();
                body.getCachedPopulation();

                int showerPopulation = (int) Math.floor(POPULATION_CACHE.get(body.getID()) * Config.COMMON.meteorShowerPopulationFactor.get());
                if (body.attributes().semimajorAxis() > 0.0D && showerPopulation > 0)
                {
                    for (int i = 0; i < showerPopulation; i++)
                    {
                        RandomSource random = RandomSource.create(i);
                        body.getCachedAdjustedSemimajorAxis(random, i);
                        body.getCachedDiameter(random, i);
                        body.getCachedOrbitPeriod(random, i);
                        body.getTexture(i);
                        body.getRandomOffset(random, i);
                    }
                }
            }
        }
    }

    public List<MeteorData> get()
    { 
        return meteors;
    }

    public static Optional<MeteorData> get(String name)
    { 
        return MeteorSwarmDataManager.METEOR_DATA.get().stream()
                .filter(body -> body.names().name().equals(name))
                .findFirst();
    }

    public static Optional<MeteorData> get(int id)
    { 
        return MeteorSwarmDataManager.METEOR_DATA.get().stream()
                .filter(body -> body.getID() == id)
                .findFirst();
    }

    /**
     * List of meteors data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param meteors List of meteors.
     */
    public record MeteorDataList(boolean replace, List<MeteorData> meteors)
    {
        public static final Codec<MeteorDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(MeteorDataList::replace),
                MeteorData.CODEC.listOf().fieldOf("meteors").forGetter(MeteorDataList::meteors)
        ).apply(instance, MeteorDataList::new));
    }

    /**
     * Data for meteors.
     *
     * @param id Unique identifier for the meteors.
     * @param names Names associated with the meteors.
     * @param attributes Attributes of the meteors.
     */
    public record MeteorData(int id, Names names, Attributes attributes)
    {
        public static final Codec<MeteorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(MeteorData::id),
            Names.CODEC.fieldOf("Names").forGetter(MeteorData::names),
            Attributes.CODEC.fieldOf("Attributes").forGetter(MeteorData::attributes)
        ).apply(instance, MeteorData::new));

        public int getID()
        {
            return this.id() + PlanetDataManager.PLANET_DATA.get().size() + MinorPlanetDataManager.MINOR_PLANET_DATA.get().size() + MoonDataManager.MOON_DATA.get().size() + CometDataManager.COMET_DATA.get().size();
        }

        public Attributes getAttributes()
        {
            return this.attributes();
        }

        public void getCachedDensity()
        {
            final int id = this.getID();
            if (this.attributes().numberDensity() <= 0.0D)
            {
                DENSITY_CACHE.put(id, 5.0D + (RandomSource.create(id).nextGaussian() + 1.0D) * 50.0D);
            }
            DENSITY_CACHE.put(id, this.attributes().numberDensity());
        }

        public void getCachedPopulation()
        {
            final int id = this.getID();
            POPULATION_CACHE.put(id, (int) Math.round(20.0D * DENSITY_CACHE.get(id)));
        }

        public void getCachedAdjustedSemimajorAxis(RandomSource random, int meteor)
        {
            final int id = this.getID();
            METEOR_SEMIMAJOR_AXIS_CACHE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
            METEOR_SEMIMAJOR_AXIS_CACHE.get(id).put(meteor, this.attributes().semimajorAxis() * AHelpers.nextSkewedGaussian(random, 1.0D, 2.0D, -0.7D, -1.0D, 3.0D) * Config.COMMON.meteorShowerDistanceFactor.get());
        }

        public void getCachedDiameter(RandomSource random, int meteor)
        {
            final int id = this.getID();
            METEOR_DIAMETER_CACHE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
            METEOR_DIAMETER_CACHE.get(id).put(meteor, AHelpers.nextSkewedGaussian(random, 0.5D, 0.5D, -0.7D, 0.01D, 1.0D) * Config.COMMON.meteorShowerDiameterFactor.get());
        }

        public void getCachedOrbitPeriod(RandomSource random, int meteor)
        {
            final int id = this.getID();
            METEOR_ORBIT_PERIOD_CACHE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
            METEOR_ORBIT_PERIOD_CACHE.get(id).put(meteor, AHelpers.convertYearsToTicks(PlanetDataManager.getOrbitalPeriod(METEOR_SEMIMAJOR_AXIS_CACHE.get(id).get(meteor)) * Config.COMMON.planetOrbitFactor.get()));
        }

        public void getRandomOffset(RandomSource random, int meteor)
        {
            final int id = this.getID();
            double x = Math.toRadians(random.nextGaussian() * 3.0D);
            double y = Math.toRadians(random.nextGaussian() * 3.0D);
            double z = Math.toRadians(random.nextGaussian() * 3.0D);
            METEOR_RANDOM_ROTATION_CACHE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
            METEOR_RANDOM_ROTATION_CACHE.get(id).put(meteor, new Vec3(x, y, z));
        }

        public void getTexture(int meteor)
        {
            final int id = this.getID();
            double diameter = METEOR_DIAMETER_CACHE.get(id).get(meteor);
            String textureName;
            if (diameter >= 0.9D)
            {
                textureName = "textures/celestials/asteroid_spheroid";
            }
            else if (diameter >= 0.8D)
            {
                textureName = "textures/celestials/asteroid_irregular";
            }
            else if (diameter >= 0.65D)
            {
                textureName = "textures/celestials/asteroid_large";
            }
            else if (diameter >= 0.4D)
            {
                textureName = "textures/celestials/asteroid_medium";
            }
            else
            {
                textureName = "textures/celestials/asteroid_small";
            }
            METEOR_TEXTURE_CACHE.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
            METEOR_TEXTURE_CACHE.get(id).put(meteor, textureName);
        }

        public double getAscendingNodeLongitudeOrDefault()
        {
            double ascendingNode = this.attributes().ascendingNodeLongitude();
            return ascendingNode != 0.0D ? ascendingNode : (RandomSource.create(this.getID()).nextDouble() * 2.0D - 1.0D) * 360.0D;
        }

        public double getPeriapsisLongitudeOrDefault()
        {
            double periapsis = this.attributes().periapsisLongitude();
            return periapsis != 0.0D ? periapsis : (RandomSource.create(this.getID()).nextDouble() * 2.0D - 1.0D) * 360.0D;
        }

        public void getCachedParent()
        {
            PARENT_CACHE.put(this.getID(), 0);
        }

        public void getInclination()
        {
            INCLINATION_CACHE.put(this.getID(), Math.toRadians(this.attributes().inclination()));
        }

        public void getAscendingNodeLongitude()
        {
            ASCENDING_NODE_LONGITUDE_CACHE.put(this.getID(), this.getAscendingNodeLongitudeOrDefault());
        }

        public void getPeriapsisLongitude()
        {
            PERIAPSIS_LONGITUDE_CACHE.put(this.getID(), this.getPeriapsisLongitudeOrDefault());
        }

        public void getEccentricity()
        {
            ECCENTRICITY_CACHE.put(this.getID(), this.attributes().eccentricity());
        }

        public void getSemimajorAxis()
        {
            SEMIMAJOR_AXIS_CACHE.put(this.getID(), this.attributes().semimajorAxis() * Config.COMMON.meteorShowerDistanceFactor.get());
        }

        public void getOrbitalPeriod()
        {
            ORBIT_PERIOD_CACHE.put(this.getID(), AHelpers.convertYearsToTicks(PlanetDataManager.getOrbitalPeriod(SEMIMAJOR_AXIS_CACHE.get(this.getID())) * Config.COMMON.planetOrbitFactor.get()));
        }

        public void getMeanLongitude()
        {
            final int id = this.getID();
            double periapsisLongitude = PERIAPSIS_LONGITUDE_CACHE.get(id).doubleValue();
            double ascendingNodeLongitude = ASCENDING_NODE_LONGITUDE_CACHE.get(id).doubleValue();
            double meanLongitude = (2.0D * Math.PI * (periapsisLongitude + ascendingNodeLongitude)) / 360.0D;
            MEAN_LONGITUDE_CACHE.put(id, meanLongitude);
        }
    }

    /**
     * Names associated with a meteor.
     *
     * @param name The common name of the meteor.
     */
    public record Names(String name)
    {
        public static final Codec<Names> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("Name", "Unknown").forGetter(Names::name)
        ).apply(instance, Names::new));
    }

    /**
     * Attributes of a meteor.
     *
     * @param diameter The diameter in kilometers.
     * @param volume The volume in m³.
     * @param mass The mass in kilograms.
     * @param rotationPeriod The rotation period in hours.
     * @param inclination The inclination in degrees.
     */
    public record Attributes(double inclination, double eccentricity, double ascendingNodeLongitude, double periapsisLongitude, double semimajorAxis, double diurnalDriftDeclination, double diurnalDriftRightAscension, double geocentricVelocity, double numberDensity, double populationIndex, double radiantAltitude, double radiantAzimuth, double radiantDeclination, double radiantRightAscension, double radiantSolarLongitude)
    {
        public static final Codec<Attributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Inclination", 0.0).forGetter(Attributes::inclination),
            Codec.DOUBLE.optionalFieldOf("Eccentricity", 0.0).forGetter(Attributes::eccentricity),
            Codec.DOUBLE.optionalFieldOf("AscendingNodeLongitude", 0.0).forGetter(Attributes::ascendingNodeLongitude),
            Codec.DOUBLE.optionalFieldOf("PeriapsisLongitude", 0.0).forGetter(Attributes::periapsisLongitude),
            Codec.DOUBLE.optionalFieldOf("SemimajorAxis", 0.0).forGetter(Attributes::semimajorAxis),
            Codec.DOUBLE.optionalFieldOf("DiurnalDriftDeclination", 0.0).forGetter(Attributes::diurnalDriftDeclination),
            Codec.DOUBLE.optionalFieldOf("DiurnalDriftRightAscension", 0.0).forGetter(Attributes::diurnalDriftRightAscension),
            Codec.DOUBLE.optionalFieldOf("GeocentricVelocity", 0.0).forGetter(Attributes::geocentricVelocity),
            Codec.DOUBLE.optionalFieldOf("NumberDensity", 0.0).forGetter(Attributes::numberDensity),
            Codec.DOUBLE.optionalFieldOf("PopulationIndex", 0.0).forGetter(Attributes::populationIndex),
            Codec.DOUBLE.optionalFieldOf("RadiantAltitude", 0.0).forGetter(Attributes::radiantAltitude),
            Codec.DOUBLE.optionalFieldOf("RadiantAzimuth", 0.0).forGetter(Attributes::radiantAzimuth),
            Codec.DOUBLE.optionalFieldOf("RadiantDeclination", 0.0).forGetter(Attributes::radiantDeclination),
            Codec.DOUBLE.optionalFieldOf("RadiantRightAscension", 0.0).forGetter(Attributes::radiantRightAscension),
            Codec.DOUBLE.optionalFieldOf("RadiantSolarLongitude", 0.0).forGetter(Attributes::radiantSolarLongitude)
        ).apply(instance, Attributes::new));
    }
}