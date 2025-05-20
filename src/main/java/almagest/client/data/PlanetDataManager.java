package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joml.Vector2d;

import com.google.gson.JsonElement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

import almagest.Almagest;
import almagest.client.RenderEventHandler;
import almagest.client.data.MinorPlanetDataManager.MinorPlanetData;
import almagest.client.data.MoonDataManager.MoonData;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

public class PlanetDataManager
{
    //SimpleJsonResourceReloadListener
    public static PlanetDataManager PLANET_DATA;
    public final List<PlanetData> planets;

    public static final ConcurrentMap<Integer, Double> ALBEDO_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> ATMOSPHERIC_SCALE_HEIGHT_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> DIAMETER_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> MASS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> OBLIQUITY_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> ROTATION_PERIOD_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> INCLINATION_CACHE = new ConcurrentHashMap<>();

    public static final ConcurrentMap<Integer, Double> ECCENTRICITY_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> ASCENDING_NODE_LONGITUDE_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> PERIAPSIS_LONGITUDE_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> ORBIT_PERIOD_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> SEMIMAJOR_AXIS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> NODAL_PRECESSION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> RING_MIN_RADIUS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> RING_MAX_RADIUS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> MEAN_LONGITUDE_CACHE = new ConcurrentHashMap<>();

    public static final ConcurrentMap<Double, ConcurrentMap<Double, ResourceLocation>> DAY_CYCLE_TEXTURE_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> TIME_OF_DAY_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> SEASON_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> RANDOM_ROTATION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Boolean> OBSERVING_BODY_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, List<Integer>> SATELLITES_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Integer> PARENT_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> POSITION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, String> TEXTURE_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Double> ORBITAL_ANGLE_CACHE = new ConcurrentHashMap<>();

    public static volatile int OBSERVER;
    public static volatile PlanetData OBSERVER_BODY;
    public static volatile double OBSERVER_INCLINATION;
    public static volatile double OBSERVER_SEASON;
    public static volatile double OBSERVER_TIME_OF_DAY;

    public static final double WINTER_SOLSTICE_TICKS_OFFSET = 262992.0D;

    public PlanetDataManager()
    {
        this.planets = initData("stellar_data", "planets");
    }

    public List<PlanetData> initData(String directory, String name)
    {
        List<PlanetData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<PlanetDataList> dataResult = PlanetDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(planetDataList -> {
            data.addAll(planetDataList.planets());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read planet data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        planets.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<PlanetDataList> dataResult = PlanetDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(planetDataList -> {
                if (planetDataList.replace()) {
                    planets.clear();
                }
                planets.addAll(planetDataList.planets());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read planet data {}", partialResult.message());
            });
        }
    }*/

    public static void clearCaches()
    {
        ALBEDO_CACHE.clear();
        ATMOSPHERIC_SCALE_HEIGHT_CACHE.clear();
        DIAMETER_CACHE.clear();
        MASS_CACHE.clear();
        OBLIQUITY_CACHE.clear();
        ROTATION_PERIOD_CACHE.clear();
        INCLINATION_CACHE.clear();
        ECCENTRICITY_CACHE.clear();
        ASCENDING_NODE_LONGITUDE_CACHE.clear();
        PERIAPSIS_LONGITUDE_CACHE.clear();
        ORBIT_PERIOD_CACHE.clear();
        SEMIMAJOR_AXIS_CACHE.clear();
        NODAL_PRECESSION_CACHE.clear();
        RING_MIN_RADIUS_CACHE.clear();
        RING_MAX_RADIUS_CACHE.clear();
        MEAN_LONGITUDE_CACHE.clear();
        DAY_CYCLE_TEXTURE_CACHE.clear();
        TIME_OF_DAY_CACHE.clear();
        SEASON_CACHE.clear();
        RANDOM_ROTATION_CACHE.clear();
        OBSERVING_BODY_CACHE.clear();
        SATELLITES_CACHE.clear();
        PARENT_CACHE.clear();
        POSITION_CACHE.clear();
        TEXTURE_CACHE.clear();
        ORBITAL_ANGLE_CACHE.clear();
        RANDOM_ROTATION_CACHE.clear();
        RANDOM_ROTATION_CACHE.clear();
        RANDOM_ROTATION_CACHE.clear();
        RANDOM_ROTATION_CACHE.clear();
    }

    public static void initCaches()
    {
        Almagest.LOGGER.info("Initializing planet data caches.");
        initBakedDayCycleTextures();
        List<PlanetData> bodies = PlanetDataManager.PLANET_DATA.get();
        if (!bodies.isEmpty())
        {
            for (PlanetData body : bodies)
            {
                body.isObserver();
                body.getCachedParent();
                body.getCachedSatellites();
                body.getAlbedo();
                body.getAtmosphericScaleHeight();
                body.getDiameter();
                body.getMass();
                body.getObliquity();
                body.getEccentricity();
                body.getInclination();
                body.getNodalPrecession();
                body.getOrbitPeriod();
                body.getAscendingNodeLongitude();
                body.getPeriapsisLongitude();
                body.getRingMaxRadius();
                body.getRingMinRadius();
                body.getRotationPeriod();
                body.getSemimajorAxis();
                body.getTexture();
                body.getMeanLongitude();
            }
            OBSERVER = getObserver();
            OBSERVER_BODY = getObserverBody();
        }
    }

    public static void updateObserverParam(ClientTickEvent event)
    {
        long time = RenderEventHandler.time;
        OBSERVER_INCLINATION = INCLINATION_CACHE.getOrDefault(OBSERVER, 0.0D);
        OBSERVER_SEASON = PlanetDataManager.getCachedSeason(OBSERVER, time, false);
        OBSERVER_TIME_OF_DAY = PlanetDataManager.getCachedTimeOfDay(OBSERVER, time, ROTATION_PERIOD_CACHE.getOrDefault(OBSERVER, Level.TICKS_PER_DAY * Config.COMMON.planetDayFactor.get()).doubleValue(), false);
    }

    public static void initBakedDayCycleTextures()
    {
        for (double obliquity = -180.0D; obliquity <= 180.0D; obliquity += 2.5D)
        {
            DAY_CYCLE_TEXTURE_CACHE.computeIfAbsent(obliquity, k -> new ConcurrentHashMap<>());
            for (double hour = 0.0D; hour < 48.0D; hour++)
            {
                String obliquityStr = (obliquity == (int) obliquity) ? String.format("%.0f", obliquity) : String.format("%.1f", obliquity);
                String hourStr = (hour == (int) hour) ? String.format("%.0f", hour) : String.format("%.1f", hour);
                DAY_CYCLE_TEXTURE_CACHE.get(obliquity).put(hour * 0.5D, AHelpers.identifier("textures/day_cycle/" + obliquityStr + "/" + hourStr + ".png"));
            }
        }
    }

    /**
     * Gets the ResourceLocation for the given obliquity and hour from the DAY_CYCLE_TEXTURE_CACHE.
     *
     * @param obliquity The obliquity in degrees, which will be rounded to the nearest 2.5-degree interval from -180 to 180.
     * @param hour      The hour in the range of 0 to 24000, which will be rounded to the nearest valid hour interval.
     * @return The texture for the corresponding time and obliquity if present, otherwise defaults to 0th hour of 0th obliquity.
     */
    public static ResourceLocation getDayCycleTexture(double obliquity, double hour)
    {
        double roundedObliquity = Math.round(obliquity / 2.5D) * 2.5D;
        double normalizedHour = AHelpers.modulo(hour, (double) Level.TICKS_PER_DAY);
        double roundedHour = AHelpers.modulo(Math.round(normalizedHour / ((double) Level.TICKS_PER_DAY / 48.0D)), 48.0D);

        ConcurrentMap<Double, ResourceLocation> hourMap = DAY_CYCLE_TEXTURE_CACHE.get(roundedObliquity);
        if (hourMap != null)
        {
            if (hourMap.get(roundedHour * 0.5D) != null)
            {
                return hourMap.get(roundedHour * 0.5D);
            }
        }
        ResourceLocation texture = DAY_CYCLE_TEXTURE_CACHE.get(0.0D).get(0.0D);
        return texture;
    }

    /**
     * Gets the ResourceLocation for the given obliquity and hour from the DAY_CYCLE_TEXTURE_CACHE.
     *
     * @param obliquity The obliquity in degrees, which will be rounded to the nearest 2.5-degree interval from -180 to 180.
     * @param hour      The hour in the range of 0 to 24000, which will be rounded to the nearest valid hour interval.
     * @return The 'vector' containing the corresponding time and obliquity, where x is obliquity and y is the hour.
     */
    public static Vector2d getDayCycle(double obliquity, double hour)
    {
        double roundedObliquity = Math.round(obliquity / 2.5D) * 2.5D;
        double normalizedHour = AHelpers.modulo(hour, (double) Level.TICKS_PER_DAY);
        double roundedHour = AHelpers.modulo(Math.round(normalizedHour / ((double) Level.TICKS_PER_DAY / 48.0D)), 48.0D);
        return new Vector2d(roundedObliquity, roundedHour);
    }

    public List<PlanetData> get()
    { 
        return planets;
    }

    public static Optional<PlanetData> get(String name)
    {
        return PlanetDataManager.PLANET_DATA.get().stream()
                .filter(body -> !body.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT).equals("unknown") && (body.names().alphanumericName().toLowerCase(Locale.ROOT).equals(name) || body.names().name().toLowerCase(Locale.ROOT).equals(name)))
                .findFirst();
    }

    public static Optional<PlanetData> get(int id)
    { 
        return PlanetDataManager.PLANET_DATA.get().stream()
                .filter(body -> body.getID() == id)
                .findFirst();
    }

    /**
     * Gets the ID of the first key in OBSERVING_BODY_CACHE that has the boolean value of true.
     *
     * @return The ID of the first key with a true value, or Earth (ID: 3) if no such key exists.
     */
    public static int getObserver()
    {
        for (Entry<Integer, Boolean> entry : OBSERVING_BODY_CACHE.entrySet())
        {
            if (entry.getValue())
            {
                return entry.getKey();
            }
        }
        return 3;
    }

    public static PlanetData getObserverBody()
    {
        return get(OBSERVER).get();
    }

    public static boolean isSun(int id)
    {
        return id == 0;
    }

    public static boolean isEarth(int id)
    {
        return id == 3;
    }

    public static boolean isObserver(int id)
    {
        return id == OBSERVER;
    }

    /**
     * List of planet data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param planets List of planets.
     */
    public record PlanetDataList(boolean replace, List<PlanetData> planets)
    {
        public static final Codec<PlanetDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(PlanetDataList::replace),
                PlanetData.CODEC.listOf().fieldOf("planets").forGetter(PlanetDataList::planets)
        ).apply(instance, PlanetDataList::new));
    }

    /**
     * Data for planets.
     *
     * @param id Unique identifier for the planet.
     * @param names Names associated with the planet.
     * @param color The color in RGB.
     * @param apparentMotion The apparent motion of the planet (e.g., prograde, retrograde).
     * @param satellites List of satellites orbiting the planet.
     * @param attributes Attributes of the planet.
     * @param orbitalParameters Orbital parameters of the planet.
     * @param texture The texture of the planet's surface.
     * @param observer If this is the observer body.
     * @param parent The name of the parent body.
     * @param isSun If this planet is the sun.
     */
    public record PlanetData(int id, Names names, List<Double> color, String apparentMotion, List<String> satellites, Attributes attributes, OrbitalParameters orbitalParameters, String texture, boolean customModel, boolean observer, String parent, boolean isSun)
    {
        public static final Codec<PlanetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(PlanetData::id),
            Names.CODEC.fieldOf("Names").forGetter(PlanetData::names),
            Codec.DOUBLE.listOf().optionalFieldOf("Color", List.of(0.8D, 0.8D, 0.8D)).forGetter(PlanetData::color),
            Codec.STRING.optionalFieldOf("RetrogradeApparentMotionQuery", "prograde").forGetter(PlanetData::apparentMotion),
            Codec.STRING.listOf().optionalFieldOf("Satellites", List.of()).forGetter(PlanetData::satellites),
            Attributes.CODEC.fieldOf("Attributes").forGetter(PlanetData::attributes),
            OrbitalParameters.CODEC.fieldOf("OrbitalParameters").forGetter(PlanetData::orbitalParameters),
            Codec.STRING.optionalFieldOf("Texture", "Unknown").forGetter(PlanetData::texture),
            Codec.BOOL.optionalFieldOf("CustomModel", false).forGetter(PlanetData::customModel),
            Codec.BOOL.optionalFieldOf("Observer", false).forGetter(PlanetData::observer),
            Codec.STRING.optionalFieldOf("Parent", "Unknown").forGetter(PlanetData::parent),
            Codec.BOOL.optionalFieldOf("Sun", false).forGetter(PlanetData::isSun)
        ).apply(instance, PlanetData::new));

        public int getID()
        {
            return this.id();
        }

        public List<String> getSatellites()
        {
            return this.satellites();
        }

        public Vec3 getColor()
        {
            double r = this.color.get(0);
            double g = this.color.get(1);
            double b = this.color.get(2);
            return new Vec3(r, g, b);
        }

        public double getAscendingNodeLongitudeOrDefault()
        {
            double ascendingNode = this.orbitalParameters().ascendingNodeLongitude();
            return ascendingNode != 0.0D ? ascendingNode : (RandomSource.create(this.getID()).nextDouble() * 2.0D - 1.0D) * 360.0D;
        }

        public double getPeriapsisLongitudeOrDefault()
        {
            double periapsis = this.orbitalParameters().periapsisLongitude();
            return periapsis != 0.0D ? periapsis : (RandomSource.create(this.getID()).nextDouble() * 2.0D - 1.0D) * 360.0D;
        }

        /**
         * This method returns the texture resource location for a celestial object based on its attributes.
         * It first checks if the texture name is known. If it is, it returns the corresponding texture file.
         * If the texture name is "Unknown", it checks various attributes of the celestial object:
         * - If the atmospheric scale height is 1.0 or greater, it returns a terrestrial texture with an atmosphere.
         * - If the diameter is 1000 or greater, it returns a terrestrial texture.
         * - If the diameter is 700 or greater, it returns a rough terrestrial texture.
         * - If the diameter is 500 or greater, it returns a spheroid asteroid texture.
         * - If the diameter is 300 or greater, it returns an irregular asteroid texture.
         * - If the diameter is 200 or greater, it returns a large asteroid texture.
         * - If the diameter is 100 or greater, it returns a medium asteroid texture.
         * - If none of the above conditions are met, it returns a small asteroid texture.
         */
        public void getTexture()
        {
            double diameter = this.attributes().diameter();
            if (!this.texture().toLowerCase(Locale.ROOT).equals("unknown"))
            {
                String textureName = "textures/block/planets/" + this.texture().toLowerCase(Locale.ROOT);
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (this.attributes().atmosphericScaleHeight() > 0.0D)
            {
                String textureName = "textures/celestials/terrestrial_atmosphere";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 1500.0D)
            {
                String textureName = "textures/celestials/terrestrial_smooth";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 1000.0D)
            {
                String textureName = "textures/celestials/terrestrial_rough";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 800.0D)
            {
                String textureName = "textures/celestials/asteroid_spheroid";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 600.0D)
            {
                String textureName = "textures/celestials/asteroid_irregular";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 400.0D)
            {
                String textureName = "textures/celestials/asteroid_large";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 200.0D)
            {
                String textureName = "textures/celestials/asteroid_medium";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            String textureName = "textures/celestials/asteroid_small";
            TEXTURE_CACHE.put(this.getID(), textureName);
            return;
        }

        public void isObserver()
        {
            OBSERVING_BODY_CACHE.put(this.getID(), this.observer());
        }

        public void getCachedSatellites()
        {
            int id = this.getID();
            List<Integer> satellites = new ArrayList<>();
            List<String> satelliteNames = this.satellites();
            for (String satellite : satelliteNames)
            {
                if (PlanetDataManager.get(satellite).isPresent())
                {
                    satellites.add(PlanetDataManager.get(satellite).get().getID());
                }
                else if (MinorPlanetDataManager.get(satellite).isPresent())
                {
                    satellites.add(MinorPlanetDataManager.get(satellite).get().getID());
                }
                else if (MoonDataManager.get(satellite).isPresent())
                {
                    satellites.add(MoonDataManager.get(satellite).get().getID());
                }
            }
            SATELLITES_CACHE.put(id, satellites);
        }

        public void getCachedParent()
        {
            int id = this.getID();
            final Set<Character> charsToRemove = Set.of(' ', '/');
            final String name = AHelpers.removeChars(this.names().getAlphaNameOrDefault(), charsToRemove);

            List<PlanetData> planets = PlanetDataManager.PLANET_DATA.get();
            if (!planets.isEmpty())
            {
                for (PlanetData body : planets)
                {
                    List<String> satellites = body.satellites().stream().map(s -> AHelpers.removeChars(s, charsToRemove)).toList();
                    if (!body.satellites().isEmpty() && satellites.contains(name))
                    {
                        PARENT_CACHE.put(id, body.getID());
                        return;
                    }
                }
            }
            List<MinorPlanetData> minorPlanets = MinorPlanetDataManager.MINOR_PLANET_DATA.get();
            if (!minorPlanets.isEmpty())
            {
                for (MinorPlanetData body : minorPlanets)
                {
                    List<String> satellites = body.satellites().stream().map(s -> AHelpers.removeChars(s, charsToRemove)).toList();
                    if (!body.satellites().isEmpty() && satellites.contains(name))
                    {
                        PARENT_CACHE.put(id, body.getID());
                        return;
                    }
                }
            }
            List<MoonData> moons = MoonDataManager.MOON_DATA.get();
            if (!moons.isEmpty())
            {
                for (MoonData body : moons)
                {
                    List<String> satellites = body.satellites().stream().map(s -> AHelpers.removeChars(s, charsToRemove)).toList();
                    if (!body.satellites().isEmpty() && satellites.contains(name))
                    {
                        PARENT_CACHE.put(id, body.getID());
                        return;
                    }
                }
            }
            PARENT_CACHE.put(id, 0);
        }

        public void getAlbedo()
        {
            ALBEDO_CACHE.put(this.getID(), this.attributes().albedo());
        }

        public void getAtmosphericScaleHeight()
        {
            ATMOSPHERIC_SCALE_HEIGHT_CACHE.put(this.getID(), this.attributes().atmosphericScaleHeight());
        }

        public void getDiameter()
        {
            DIAMETER_CACHE.put(this.getID(), this.attributes().getAdjustedDiameter());
        }

        public void getMass()
        {
            MASS_CACHE.put(this.getID(), this.attributes().mass());
        }

        public void getObliquity()
        {
            OBLIQUITY_CACHE.put(this.getID(), this.attributes().obliquity());
        }

        public void getRotationPeriod()
        {
            ROTATION_PERIOD_CACHE.put(this.getID(), this.attributes().getAdjustedDayDuration());
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
            ECCENTRICITY_CACHE.put(this.getID(), this.orbitalParameters().eccentricity());
        }

        public void getOrbitPeriod()
        {
            ORBIT_PERIOD_CACHE.put(this.getID(), this.orbitalParameters().getAdjustedOrbitalPeriod());
        }

        public void getSemimajorAxis()
        {
            SEMIMAJOR_AXIS_CACHE.put(this.getID(), this.orbitalParameters().getAdjustedSemimajorAxis());
        }

        public void getNodalPrecession()
        {
            NODAL_PRECESSION_CACHE.put(this.getID(), this.orbitalParameters().nodalPrecession());
        }

        public void getRingMinRadius()
        {
            RING_MIN_RADIUS_CACHE.put(this.getID(), this.orbitalParameters().getAdjustedRingMinRadius());
        }

        public void getRingMaxRadius()
        {
            RING_MAX_RADIUS_CACHE.put(this.getID(), this.orbitalParameters().getAdjustedRingMaxRadius());
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
     * Names associated with a planet.
     *
     * @param alphanumericName The alphanumeric name of the planet.
     * @param name The common name of the planet.
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

    /**
     * Attributes of a celestial body.
     *
     * @param albedo The albedo (dimensionless).
     * @param atmosphericScaleHeight The atmospheric scale height in kilometers.
     * @param density The density in g/cm³.
     * @param diameter The diameter in kilometers.
     * @param volume The volume in m³.
     * @param equatorialRadius The equatorial radius in kilometers.
     * @param minimumTemperature The minimum temperature in degrees Celsius.
     * @param averageTemperature The average temperature in degrees Celsius.
     * @param maximumTemperature The maximum temperature in degrees Celsius.
     * @param effectiveTemperature The effective temperature in Kelvin.
     * @param gravity The surface gravity in m/s².
     * @param mass The mass in kilograms.
     * @param oblateness The oblateness (dimensionless).
     * @param obliquity The obliquity in degrees.
     * @param rotationPeriod The rotation period in hours.
     * @param inclination The inclination in degrees.
     */
    public record Attributes(double albedo, double atmosphericScaleHeight, double density, double diameter, double volume, double equatorialRadius, double minimumTemperature, double averageTemperature, double maximumTemperature, double effectiveTemperature, double gravity, double mass, double oblateness, double obliquity, double rotationPeriod, double inclination)
    {
        public static final Codec<Attributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Albedo", 0.3D).forGetter(Attributes::albedo),
            Codec.DOUBLE.optionalFieldOf("AtmosphericScaleHeight", 0.0).forGetter(Attributes::atmosphericScaleHeight),
            Codec.DOUBLE.optionalFieldOf("Density", 0.0).forGetter(Attributes::density),
            Codec.DOUBLE.optionalFieldOf("Diameter", 0.0).forGetter(Attributes::diameter),
            Codec.DOUBLE.optionalFieldOf("Volume", 0.0).forGetter(Attributes::volume),
            Codec.DOUBLE.optionalFieldOf("EquatorialRadius", 0.0).forGetter(Attributes::equatorialRadius),
            Codec.DOUBLE.optionalFieldOf("MinimumTemperature", 0.0).forGetter(Attributes::minimumTemperature),
            Codec.DOUBLE.optionalFieldOf("AverageTemperature", 0.0).forGetter(Attributes::averageTemperature),
            Codec.DOUBLE.optionalFieldOf("MaximumTemperature", 0.0).forGetter(Attributes::maximumTemperature),
            Codec.DOUBLE.optionalFieldOf("EffectiveTemperature", 0.0).forGetter(Attributes::effectiveTemperature),
            Codec.DOUBLE.optionalFieldOf("Gravity", 0.0).forGetter(Attributes::gravity),
            Codec.DOUBLE.optionalFieldOf("Mass", 0.0).forGetter(Attributes::mass),
            Codec.DOUBLE.optionalFieldOf("Oblateness", 0.0).forGetter(Attributes::oblateness),
            Codec.DOUBLE.optionalFieldOf("Obliquity", 0.0).forGetter(Attributes::obliquity),
            Codec.DOUBLE.optionalFieldOf("RotationPeriod", 0.0).forGetter(Attributes::rotationPeriod),
            Codec.DOUBLE.optionalFieldOf("Inclination", 0.0).forGetter(Attributes::inclination)
        ).apply(instance, Attributes::new));

        public double getAdjustedDayDuration()
        {
            return AHelpers.convertHoursToTicks(this.rotationPeriod()) * Config.COMMON.planetDayFactor.get();
        }

        public double getAdjustedDiameter()
        {
            return this.diameter() * Config.COMMON.planetDiameterFactor.get();
        }
    }

    /**
     * Orbital parameters of a celestial body.
     *
     * @param eccentricity The orbital eccentricity (dimensionless).
     * @param ascendingNodeLongitude The longitude of the ascending node in degrees.
     * @param orbitPeriod The orbital period in days.
     * @param apoapsis The apoapsis distance in kilometers.
     * @param periapsis The periapsis distance in kilometers.
     * @param periapsisLongitude The longitude of the periapsis in degrees.
     * @param semimajorAxis The semi-major axis in astronomical units (AU).
     * @param nodalPrecession The nodal precession rate in radians per second.
     * @param ringMinRadius The minimum radius of the ring of the planet.
     * @param ringMaxRadius The maximum radius of the ring of the planet.
     */
    public record OrbitalParameters(double eccentricity, double ascendingNodeLongitude, double orbitPeriod, double apoapsis, double periapsis, double periapsisLongitude, double semimajorAxis, double nodalPrecession, double ringMinRadius, double ringMaxRadius)
    {
        public static final Codec<OrbitalParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Eccentricity", 0.0).forGetter(OrbitalParameters::eccentricity),
            Codec.DOUBLE.optionalFieldOf("AscendingNodeLongitude", 0.0).forGetter(OrbitalParameters::ascendingNodeLongitude),
            Codec.DOUBLE.optionalFieldOf("OrbitPeriod", 0.0).forGetter(OrbitalParameters::orbitPeriod),
            Codec.DOUBLE.optionalFieldOf("Apoapsis", 0.0).forGetter(OrbitalParameters::apoapsis),
            Codec.DOUBLE.optionalFieldOf("Periapsis", 0.0).forGetter(OrbitalParameters::periapsis),
            Codec.DOUBLE.optionalFieldOf("PeriapsisLongitude", 0.0).forGetter(OrbitalParameters::periapsisLongitude),
            Codec.DOUBLE.optionalFieldOf("SemimajorAxis", 0.0).forGetter(OrbitalParameters::semimajorAxis),
            Codec.DOUBLE.optionalFieldOf("NodalPrecession", 0.0).forGetter(OrbitalParameters::nodalPrecession),
            Codec.DOUBLE.optionalFieldOf("RingMinRadius", 0.0).forGetter(OrbitalParameters::ringMinRadius),
            Codec.DOUBLE.optionalFieldOf("RingMaxRadius", 0.0).forGetter(OrbitalParameters::ringMaxRadius)
        ).apply(instance, OrbitalParameters::new));

        public double getAdjustedRingMinRadius()
        {
            return this.ringMinRadius() * Config.COMMON.planetDiameterFactor.get();
        }

        public double getAdjustedRingMaxRadius()
        {
            return this.ringMaxRadius() * Config.COMMON.planetDiameterFactor.get();
        }

        public double getAdjustedOrbitalPeriod()
        {
            return AHelpers.convertDaysToTicks(this.orbitPeriod()) * Config.COMMON.planetOrbitFactor.get();
        }

        public double getAdjustedSemimajorAxis()
        {
            return this.semimajorAxis() * Config.COMMON.planetDistanceFactor.get();
        }
    }

    /**
     * Calculates the orbital period (years).
     * @param a The semi-major axis (AU).
     * @return The orbital period in years.
     */
    public static double getOrbitalPeriod(double a)
    {
        return Math.sqrt(Math.pow(a, 3.0D));
    }

    /**
     * Estimates the orbital period of an object.
     *
     * @param massParent The mass of the parent object in kilograms.
     * @param massObject The mass of the orbiting object in kilograms.
     * @param semiMajorAxis The semi-major axis of the orbit in meters.
     * @return The orbital period in days.
     */
    public static double getOrbitalPeriod(double massParent, double massObject, double semiMajorAxis)
    {
        // Calculate the sum of the masses
        double sumMasses = massParent + massObject;

        // Calculate the orbital period using Kepler's Third Law
        return (Math.sqrt((4 * Math.PI * Math.PI * Math.pow(semiMajorAxis, 3)) / (AHelpers.GRAVITATIONAL_CONSTANT * sumMasses))) / (60 * 60 * 24);
    }

    /**
     * Calculates the mean longitude of the perihelion and ascending node.
     * @return The mean longitude.
     */
    public static double getCachedMeanLongitude(int id)
    {
        if (MEAN_LONGITUDE_CACHE.containsKey(id))
        {
            return MEAN_LONGITUDE_CACHE.get(id);
        }
        double periapsisLongitude = PERIAPSIS_LONGITUDE_CACHE.get(id);
        double ascendingNodeLongitude = ASCENDING_NODE_LONGITUDE_CACHE.get(id);
        double meanLongitude = (2.0D * Math.PI * (periapsisLongitude + ascendingNodeLongitude)) / 360.0D;
        return MEAN_LONGITUDE_CACHE.put(id, meanLongitude);
    }

    /**
     * Kepler's 1st law; the mean anomaly.
     * Calculates the mean anomaly.
     * @param P The orbital period.
     * @param t The time.
     * @return The mean anomaly.
     */
    public static double getMeanAnomaly(double P, double t)
    {
        return ((2 * Math.PI) / P) * t;
    }

    /**
     * Kepler's 2nd law; the eccentric anomaly.
     * Calculates the eccentric anomaly.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @return The eccentric anomaly.
     */
    public static double getEccentricAnomaly(double P, double T, double e)
    {
        return getEccentricAnomaly(P, T, e, Config.COMMON.eccentricAnomalyIterations.get());
    }

    /**
     * Kepler's 2nd law; the eccentric anomaly.
     * Calculates the eccentric anomaly based on the value of the True Anomaly.
     * @param e The eccentricity.
     * @param v The True Anomaly.
     * @return The eccentric anomaly.
     */
    public static double getEccentricAnomaly(double e, double v)
    {
        return Math.atan2(Math.sqrt(1.0D - Math.pow(e, 2.0D)) * Math.sin(v), e + Math.cos(v));
    }

    /**
     * Kepler's 2nd law; the eccentric anomaly.
     * Calculates the eccentric anomaly.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param i The amount of iterations - higher value increases precision.
     * @return The eccentric anomaly.
     */
    public static double getEccentricAnomaly(double P, double T, double e, int i)
    {
        double M = getMeanAnomaly(P, T);
        double E = M;
        for (int j = 0; j < i; j++)
        {
            E = M + e * Math.sin(E);
        }
        return E;
    }

    /**
     * Kepler's 3rd law; the true anomaly.
     * Calculates the true anomaly.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @return The true anomaly.
     */
    public static double getTrueAnomaly(double P, double T, double e)
    {
        /*double value = Math.sqrt(((1.0D + e) * Math.pow(Math.tan(getEccentricAnomaly(P, T, e) / 2.0D), 2.0D)) / (1.0D - e));
        double angle = 2.0D * (Config.COMMON.toggleFastTrigMath.get() ? AHelpers.fastAtan(value) : Math.atan(value));
        return (T <= P / 2.0D) ? angle : -angle;*/

        double beta = e / (1.0D + Math.sqrt(1.0D - Math.pow(e, 2.0D)));
        double E = getEccentricAnomaly(P, T, e);
        double value = (beta * Math.sin(E)) / (1.0D - beta * Math.cos(E));
        double angle = 2.0D * (Config.COMMON.toggleFastTrigMath.get() ? AHelpers.fastAtan(value) : Math.atan(value));
        return E + angle;
    }

    /**
     * Kepler's 4th law; the heliocentric distance.
     * Calculates the radius from the sun.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param a The semi-major axis.
     * @return The radius from the sun.
     */
    public static double getHeliocentricDistance(double P, double T, double e, double a)
    {
        return a * (1.0D - e * Math.cos(getEccentricAnomaly(P, T, e)));
    }

    /**
     * Kepler's 4th law; the heliocentric distance.
     * Calculates the radius from the sun.
     * @param e The eccentricity.
     * @param a The semi-major axis.
     * @param v The True Anomaly.
     * @return The radius from the sun.
     */
    public static double getHeliocentricDistance(double e, double a, double v)
    {
        return a * (1.0D - e * Math.cos(getEccentricAnomaly(e, v)));
    }

    /**
     * Rotates a vector around the X-axis.
     * @param r The angle in radians.
     * @param V The vector to be rotated.
     * @return The rotated vector.
     */
    public static Vec3 rotX(double r, Vec3 V)
    {
        if (r == 0.0D) return V;
        /*if (AHelpers.modulo(r, 360.0D) == 0.0D)
        {
            return V;
        }*/
        double cos = Math.cos(r);
        double sin = Math.sin(r);
        double y = V.y * cos + V.z * sin;
        double z = V.z * cos - V.y * sin;
        return new Vec3(V.x, y, z);
    }

    /**
     * Rotates a vector around the Y-axis.
     * @param r The angle in radians.
     * @param V The vector to be rotated.
     * @return The rotated vector.
     */
    public static Vec3 rotY(double r, Vec3 V)
    {
        if (r == 0.0D) return V;
        /*if (AHelpers.modulo(r, 360.0D) == 0.0D)
        {
            return V;
        }*/
        double cos = Math.cos(r);
        double sin = Math.sin(r);
        double x = V.x * cos + V.z * sin;
        double z = V.z * cos - V.x * sin;
        return new Vec3(x, V.y, z);
    }

    /**
     * Rotates a vector around the Z-axis.
     * @param r The angle in radians.
     * @param V The vector to be rotated.
     * @return The rotated vector.
     */
    public static Vec3 rotZ(double r, Vec3 V)
    {
        if (r == 0.0D) return V;
        /*if (AHelpers.modulo(r, 360.0D) == 0.0D)
        {
            return V;
        }*/
        double cos = Math.cos(r);
        double sin = Math.sin(r);
        double x = V.x * cos + V.y * sin;
        double y = V.y * cos - V.x * sin;
        return new Vec3(x, y, V.z);
    }

    /**
     * Calculates the nodal precession.
     * @param t The time.
     * @param P The orbital period.
     * @param nodalPrecession The nodal precession.
     * @return The nodal precession.
     */
    public static double getAdjustedNodalPrecession(double t, double P, double nodalPrecession)
    {
        return (t / P) / nodalPrecession;
    }

    /**
     * Calculates the elapsed time within an orbital period.
     * @param t The time.
     * @param P The orbital period.
     * @return The elapsed time within the orbital period.
     */
    public static double getElapsedTime(double t, double P)
    {
        return AHelpers.modulo(t, P);
    }

    /**
     * Calculates the time of day.
     * @param t The time.
     * @param r The duration of a day.
     * @return The time of day.
     */
    public static double getCachedTimeOfDay(int id, double t, double r, boolean updateCache)
    {
        if (!updateCache && TIME_OF_DAY_CACHE.containsKey(id))
        {
            return TIME_OF_DAY_CACHE.getOrDefault(id, 0.0D);
        }
        double timeOfDay = (r <= 0.0D ? r : Math.toRadians(((AHelpers.modulo(t, r) / r) * 360.0D) + 90.0D)) + RenderEventHandler.longitude;
        TIME_OF_DAY_CACHE.put(id, timeOfDay);
        return timeOfDay;
    }

    /**
     * Calculates the seasonal obliquity/rotation.
     * @param t The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param updateCache If the cache should be updated.
     * @param useSeasonOffset If season rotation offset should be used at all.
     * @return The seasonal rotation.
     */
    public static double getSeason(int id, long t, boolean updateCache, boolean useSeasonOffset)
    {
        return useSeasonOffset ? PlanetDataManager.getCachedSeason(id, t, isObserver(id)) : 0.0D;
    }

    /**
     * Calculates the seasonal obliquity/rotation.
     * @param t The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @return The seasonal rotation.
     */
    public static double getCachedSeason(int id, long t, boolean updateCache)
    {
        if (!updateCache && SEASON_CACHE.containsKey(OBSERVER))
        {
            return SEASON_CACHE.getOrDefault(OBSERVER, 0.0D);
        }
        double P = ORBIT_PERIOD_CACHE.getOrDefault(id, 0.0D);
        double T = getElapsedTime(t, P);
        double season = Math.toRadians(getCachedObliquityVariation(OBSERVER, t, P, isEarth(OBSERVER), updateCache) * Math.cos((T / P) * 2.0D * Math.PI + (WINTER_SOLSTICE_TICKS_OFFSET / P) + Config.COMMON.seasonOffsetTicks.get() + Math.PI) * Config.COMMON.planetSeasonalIntensity.get()); // plus pi or no pi? Breaks some things!
        SEASON_CACHE.put(OBSERVER, season);
        return season;
    }

    /**
     * Calculates the obliquity variation, including the Milankovitch cycle.
     * @param t The time.
     * @param P The orbital period.
     * @return The obliquity variation.
     */
    public static double getCachedObliquityVariation(int id, long t, double P, boolean isEarth, boolean updateCache)
    {
        if (!updateCache && OBLIQUITY_CACHE.containsKey(id))
        {
            return OBLIQUITY_CACHE.get(id);
        }
        double obliquity = isEarth ? getObliquityVariation(t, P, 41000.0D, 22.1D, 24.5D) : OBLIQUITY_CACHE.get(id);
        OBLIQUITY_CACHE.put(id, obliquity);
        return obliquity;
    }

    /**
     * Calculates the obliquity variation, including the Milankovitch cycle.
     * @param t The time.
     * @param P The orbital period.
     * @param milankovitchCycle The period of the Milankovitch cycle.
     * @param minObliquity The minimum obliquity.
     * @param maxObliquity The maximum obliquity.
     * @param amplitude The amplitude of the obliquity.
     * @return The obliquity variation.
     */
    public static double getObliquityVariation(long t, double P, double milankovitchCycle, double minObliquity, double maxObliquity)
    {
        return getObliquityAmplitude(minObliquity, maxObliquity) * Math.sin((2.0D * Math.PI * t) / (P * milankovitchCycle)) + getMeanObliquity(minObliquity, maxObliquity);
    }

    public static double getObliquityAmplitude(double minObliquity, double maxObliquity)
    {
        return (maxObliquity - minObliquity) * 0.5D;
    }

    public static double getMeanObliquity(double minObliquity, double maxObliquity)
    {
        return (minObliquity + maxObliquity) / 2.0D;
    }

    public static Vec3 sphericalRot(double angle, Vec3 vector)
    {
        if (angle == 0.0D) return vector;

        // Convert point to spherical coordinates
        double r = vector.length();
        double phi = Math.atan2(vector.z(), vector.x()); // Azimuthal angle
        double value = vector.y() / r;
        double theta = (Config.COMMON.toggleFastTrigMath.get() ? AHelpers.fastAcos(value) : Math.acos(value)); // Polar angle

        // Adjust theta by the tilt angle (keeping it in the range 0 to π)
        theta = Math.min(Math.PI, Math.max(0, theta + angle));
        double rsintheta = r * Math.sin(theta);

        // Convert back to Cartesian coordinates
        double x = rsintheta * Math.cos(phi);
        double y = r * Math.cos(theta);
        double z = rsintheta * Math.sin(phi);

        return new Vec3(x, y, z);
    }

    /**
     * Calculates the position.
     * @param l The latitude of the player between -90 and 90 degrees (radians).
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param s The season rotation of the planet (radians).
     * @param r The season offset rotation of the planet. Only relevant for the sun. (radians).
     * @param d The day rotation of the planet (radians).
     * @param rot The XYZ-axis rotation offsets of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @param useRawPos If the pos should be post-processed with players latitude, seasonal rotation etc.
     * @return The vector position.
     */
    public static Vec3 getPos(Level level, int id, double l, double P, double T, double e, double O, double W, double a, double p, double i, double w, double s, double r, double d, Vec3 rot, boolean updateCache, boolean useRawPos)
    {
        double rs = getHeliocentricDistance(P, T, e, a);
        double v = getTrueAnomaly(P, T, e);

        double Wv = W + v;
        double cosO = Math.cos(O);
        double sinO = Math.sin(O);
        double cosWv = Math.cos(Wv);
        double sinWv = Math.sin(Wv);
        double cosI = Math.cos(i);
        double sinI = Math.sin(i);
        double sinWvcosI = sinWv * cosI;

        double x = (cosO * cosWv - sinO * sinWvcosI) * rs;
        double y = (sinWv * sinI) * rs;
        double z = (sinO * cosWv + cosO * sinWvcosI) * rs;

        Vec3 pos = new Vec3(x, y, z);

        if (useRawPos)
        {
            return pos;
        }

        /*
         * Calculate and cache current angle around the sun
         */
        if (RenderEventHandler.gameTime % 20 == 0)
        {
            Vec3 vec0 = POSITION_CACHE.getOrDefault(0, new Vec3(0.0D, 0.0D, 0.0D)).multiply(1.0D, 0.0D, 1.0D).normalize();
            Vec3 vec1 = pos.normalize();
            double angle = Math.toDegrees(AHelpers.calculateAngleBetweenVectors(vec0.x(), vec0.z(), vec1.x(), vec1.z()));
            if (updateCache)
            {
                ORBITAL_ANGLE_CACHE.put(id, angle);
            }
        }

        pos = rotZ(i, pos);
        if (rot != Vec3.ZERO)
        {
            pos = rotZ(rot.z(), rotY(rot.y(), rotX(rot.x(), pos)));
        }
        return applyRotations(pos, l, s, r, d);
    }

    /**
     * Applies rotations to the input vector.
     * @param l The latitude of the player between -90 and 90 degrees (radians).
     * @param s The season rotation of the planet (radians).
     * @param r The season offset rotation of the planet. Only relevant for the sun. (radians).
     * @param d The day rotation of the planet (radians).
     * @return The rotated vector.
     */
    public static Vec3 applyRotations(Vec3 input, double l, double s, double r, double d)
    {
        Vec3 vec = sphericalRot(r, input);
        vec = rotY(d, vec);
        vec = rotX(r, vec);
        vec = rotX(-l + s, vec);
        return vec;
    }

    /**
     * Calculates the position.
     * @param l The latitude of the player between -90 and 90 degrees (radians).
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param s The season rotation of the planet (radians).
     * @param r The season offset rotation of the planet. Only relevant for the sun. (radians).
     * @param d The day rotation of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @param useRawPos If the pos should be post-processed with players latitude, seasonal rotation etc.
     * @return The vector position.
     */
    public static Vec3 getPos(Level level, int id, double l, double P, double T, double e, double O, double W, double a, double p, double i, double w, double s, double r, double d, boolean updateCache, boolean useRawPos)
    {
        return getPos(level, id, l, P, T, e, O, W, a, p, i, w, s, r, d, Vec3.ZERO, updateCache, useRawPos);
    }

    /**
     * Calculates the position.
     * @param l The latitude of the player between -90 and 90 degrees (radians).
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param s The season rotation of the planet (radians).
     * @param d The day rotation of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @param useRawPos If the pos should be post-processed with players latitude, seasonal rotation etc.
     * @return The vector position.
     */
    public static Vec3 getPos(Level level, int id, double l, double P, double T, double e, double O, double W, double a, double p, double i, double w, double s, double d, boolean updateCache, boolean useRawPos)
    {
        return getPos(level, id, l, P, T, e, O, W, a, p, i, w, s, 0.0D, d, updateCache, useRawPos);
    }

    /**
     * Calculates the position.
     * @param l The latitude of the player between -90 and 90 degrees (radians).
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param s The season rotation of the planet (radians).
     * @param d The day rotation of the planet (radians).
     * @param rot The XYZ-axis rotation offsets of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @param useRawPos If the pos should be post-processed with players latitude, seasonal rotation etc.
     * @return The vector position.
     */
    public static Vec3 getPos(Level level, int id, double l, double P, double T, double e, double O, double W, double a, double p, double i, double w, double s, double d, Vec3 rot, boolean updateCache, boolean useRawPos)
    {
        return getPos(level, id, l, P, T, e, O, W, a, p, i, w, s, 0.0D, d, rot, updateCache, useRawPos);
    }

    /**
     * @param id The ID value of the body.
     * @param level The level.
     * @param time The time.
     * @param updateCache If the pos cache should be updated.
     * @param playerOffset If the pos should be offset by player position.
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getCachedPos(int id, Level level, long time, boolean updateCache, boolean playerOffset)
    {
        if (POSITION_CACHE.containsKey(id))
        {
            return POSITION_CACHE.get(id);
        }

        double P = ORBIT_PERIOD_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double T = PlanetDataManager.getElapsedTime(time, P);
        double e = ECCENTRICITY_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double O = ASCENDING_NODE_LONGITUDE_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double W = PERIAPSIS_LONGITUDE_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double a = SEMIMAJOR_AXIS_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double p = MEAN_LONGITUDE_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double i = INCLINATION_CACHE.getOrDefault(id, 0.0D).doubleValue();
        double w = Math.toRadians(PlanetDataManager.getAdjustedNodalPrecession(time, P, NODAL_PRECESSION_CACHE.getOrDefault(id, 0.0D).doubleValue()));
        Vec3 offset = RANDOM_ROTATION_CACHE.getOrDefault(id, Vec3.ZERO);

        Vec3 pos = getPos(id, level, time, P, T, e, O, W, a, p, i, w, offset, updateCache, playerOffset, true, false);
        if (updateCache)
        {
            POSITION_CACHE.put(id, pos);
        }
        return pos;
    }

    /**
     * @param id The ID value of the body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, Vec3.ZERO, true, true, true, false);
    }

    /**
     * @param planet This body.
     * @param parent The parent body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param updateCache If the pos cache should be updated.
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, boolean updateCache)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, Vec3.ZERO, updateCache, true, true, false);
    }

    /**
     * @param planet This body.
     * @param parent The parent body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param updateCache If the pos cache should be updated.
     * @param playerOffset If the pos should be offset by player position.
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, boolean updateCache, boolean playerOffset)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, Vec3.ZERO, updateCache, playerOffset, true, false);
    }

    /**
     * @param planet This body.
     * @param parent The parent body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param updateCache If the pos cache should be updated.
     * @param playerOffset If the pos should be offset by player position.
     * @param useSeasonOffset If the pos should be offset by the season of the observer body.
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, boolean updateCache, boolean playerOffset, boolean useSeasonOffset)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, Vec3.ZERO, updateCache, playerOffset, useSeasonOffset, false);
    }

    /**
     * @param id The ID value of the body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param rot The XYZ-axis rotation offsets of the planet (radians).
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, Vec3 rot)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, rot, true, true, true, false);
    }

    /**
     * @param planet This body.
     * @param parent The parent body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param rot The XYZ-axis rotation offsets of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, Vec3 rot, boolean updateCache)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, rot, updateCache, true, true, false);
    }

    /**
     * @param planet This body.
     * @param parent The parent body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param rot The XYZ-axis rotation offsets of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @param playerOffset If the pos should be offset by player position.
     * @param useSeasonOffset If the pos should be offset by the season of the observer body.
     * @return Gets the planet's position plus their parent position based on the Tychonic model.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, Vec3 rot, boolean updateCache, boolean playerOffset, boolean useSeasonOffset)
    {
        return getPos(id, level, time, P, T, e, O, W, a, p, i, w, rot, updateCache, playerOffset, useSeasonOffset, false);
    }

    /**
     * The player, or the observing body, is essentially at the center of the world, i.e. the vec zero.
     * The sun revolves around us, while the other planets revolve around the sun.
     * This essentially means the sun has the vector position of where the Earth should've been, because
     * its orbit is just the mirrored of the Earths.
     * @param id The ID value of the body.
     * @param time The time.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param O The longitude of the ascending node.
     * @param W The longitude of the periapsis.
     * @param a The semi-major axis.
     * @param p The mean longitude.
     * @param i The inclination (radians).
     * @param w The wobble or the adjusted nodal precession (radians).
     * @param rot The XYZ-axis rotation offsets of the planet (radians).
     * @param updateCache If the pos cache should be updated.
     * @param playerOffset If the pos should be offset by player position.
     * @param useSeasonOffset If the pos should be offset by the season of the observer body.
     * @param useRawPos If the pos should be post-processed with players latitude, seasonal rotation etc.
     * @return Gets the position of the current planet irt. its parent based on the Tychonic model and Kepler's laws.
     */
    public static Vec3 getPos(int id, Level level, long time, double P, double T, double e, double O, double W, double a, double p, double i, double w, Vec3 rot, boolean updateCache, boolean playerOffset, boolean useSeasonOffset, boolean useRawPos)
    {
        final double season = getSeason(id, time, isObserver(id), useSeasonOffset);
        final double latitude = RenderEventHandler.latitude;
        final double timeOfDay = PlanetDataManager.getCachedTimeOfDay(OBSERVER, time, ROTATION_PERIOD_CACHE.get(OBSERVER).doubleValue(), isObserver(id));

        if (isSun(id))
        {
            final double seasonOffset = -(season * ((Math.sin((2.0D * Math.PI * (RenderEventHandler.latitudeDeg - 45.0D)) / 180.D) + 1.0D) * 0.5D));
            Vec3 pos = PlanetDataManager.getPos(level, id, latitude, P, T, e, O, W, a, p, i, w, season, seasonOffset, timeOfDay, rot, updateCache, useRawPos);
            if (Config.COMMON.togglePlayerOffset.get() && playerOffset)
            {
                pos = pos.add(0.0D, Config.COMMON.playerYOffset.get(), 0.0D).subtract(RenderEventHandler.playerPos.scale(10.0D));
            }
            if (updateCache)
            {
                POSITION_CACHE.put(id, pos);
            }
            return pos;
        }

        Vec3 parentPos = PlanetDataManager.getCachedPos(PARENT_CACHE.get(id), level, time, false, playerOffset);
        Vec3 pos = parentPos.add(PlanetDataManager.getPos(level, id, latitude, P, T, e, O, W, a, p, i, w, season, timeOfDay, rot, updateCache, useRawPos));
        if (updateCache)
        {
            POSITION_CACHE.put(id, pos);
        }
        return pos;
    }
}