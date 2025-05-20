package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joml.Quaternionf;

import com.google.gson.JsonElement;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.client.RenderEventHandler;
import almagest.client.data.ConstellationDataManager.ConstellationData;
import almagest.client.data.MinorPlanetDataManager.MinorPlanetData;
import almagest.client.data.MoonDataManager.MoonData;
import almagest.client.data.PlanetDataManager.PlanetData;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

import static almagest.client.data.PlanetDataManager.*;

public class StarDataManager
{
    public static StarDataManager STAR_DATA;
    public final List<StarData> stars;

    public static final ConcurrentMap<Integer, Vec3> SPECTRAL_TYPE_RGB = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> STAR_COLOR_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> POSITION_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Vec3> POSITION_ADJ_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, ChatFormatting> COLOR_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Boolean> IS_IN_CONSTELLATION_CACHE = new ConcurrentHashMap<>();

    public StarDataManager()
    {
        this.stars = initData("stellar_data", "stars");
    }

    public List<StarData> initData(String directory, String name)
    {
        List<StarData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<StarDataList> dataResult = StarDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(starDataList -> {
            data.addAll(starDataList.stars());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read star data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        Almagest.LOGGER.info("Loading dataset");
        stars.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            Almagest.LOGGER.info("1");
            DataResult<StarDataList> dataResult = StarDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            Almagest.LOGGER.info("2");

            Optional<StarDataList> starDataResult = dataResult.result();
            Almagest.LOGGER.info("3");
            if (starDataResult.isPresent())
            {
                Almagest.LOGGER.info("4");
                StarDataList starDataList = dataResult.result().get();
                if (starDataList.replace())
                {
                    stars.clear();
                }
                stars.addAll(starDataList.stars());
            }
            Almagest.LOGGER.info("5");
        }

        stars.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<StarDataList> dataResult = StarDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(starDataList -> {
                if (starDataList.replace()) {
                    stars.clear();
                }
                stars.addAll(starDataList.stars());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read stellar data {}", partialResult.message());
            });
        }
    }*/

    public static void clearCaches()
    {
        SPECTRAL_TYPE_RGB.clear();
        STAR_COLOR_CACHE.clear();
        POSITION_CACHE.clear();
        POSITION_ADJ_CACHE.clear();
        COLOR_CACHE.clear();
        IS_IN_CONSTELLATION_CACHE.clear();
    }

    public static void initCaches()
    {
        Almagest.LOGGER.info("Initializing star data caches.");
        initColorCache();
        List<StarData> stars = StarDataManager.STAR_DATA.get();
        if (!stars.isEmpty())
        {
            for (StarData star : stars)
            {
                star.isInConstellation();
            }
        }
    }

    public static void initColorCache()
    {
        SPECTRAL_TYPE_RGB.put("O".hashCode(), new Vec3(0.61D, 0.69D, 1.00D)); // Blue
        SPECTRAL_TYPE_RGB.put("B".hashCode(), new Vec3(0.67D, 0.75D, 1.00D)); // Blue-white
        SPECTRAL_TYPE_RGB.put("A".hashCode(), new Vec3(0.79D, 0.84D, 1.00D)); // White
        SPECTRAL_TYPE_RGB.put("F".hashCode(), new Vec3(0.97D, 0.97D, 1.00D)); // Yellow-white
        SPECTRAL_TYPE_RGB.put("G".hashCode(), new Vec3(1.00D, 0.96D, 0.92D)); // Yellow (Sun-like)
        SPECTRAL_TYPE_RGB.put("K".hashCode(), new Vec3(1.00D, 0.82D, 0.63D)); // Orange
        SPECTRAL_TYPE_RGB.put("M".hashCode(), new Vec3(1.00D, 0.80D, 0.44D)); // Red
    }

    public List<StarData> get()
    { 
        return stars;
    }

    public Optional<StarData> get(String name)
    { 
        return stars.stream()
                .filter(star -> !name.equals("Unknown") && !star.names().getAlphaNameOrDefault().equals("Unknown") && (star.names().alphanumericName().equals(name) || star.names().name().equals(name) || star.names().hdName().equals(name)))
                .findFirst();
    }

    public Optional<StarData> get(int id)
    { 
        return stars.stream()
                .filter(star -> star.id() == id)
                .findFirst();
    }

    public Optional<Vec3> getPos(String name)
    {
        return stars.stream()
                .filter(star -> !name.equals("Unknown") && !star.names().getAlphaNameOrDefault().equals("Unknown") && (star.names().alphanumericName().equals(name) || star.names().name().equals(name) || star.names().hdName().equals(name)))
                .map(StarData::getPos)
                .findFirst();
    }

    public Optional<Vec3> getAdjustedPos(String name)
    {
        return stars.stream()
                .filter(star -> !name.equals("Unknown") && !star.names().getAlphaNameOrDefault().equals("Unknown") && (star.names().alphanumericName().equals(name) || star.names().name().equals(name) || star.names().hdName().equals(name)))
                .map(star -> star.getAdjustedPos(false))
                .findFirst();
    }

    public Optional<Double> getDistanceFromEarth(String name)
    {
        return stars.stream()
                .filter(star -> !name.equals("Unknown") && !star.names().getAlphaNameOrDefault().equals("Unknown") && (star.names().alphanumericName().equals(name) || star.names().name().equals(name) || star.names().hdName().equals(name)))
                .map(star -> star.getPrimaryAttributes().distanceFromEarth)
                .findFirst();
    }

    public Optional<Double> getDistanceFromSun(String name)
    {
        return stars.stream()
                .filter(star -> !name.equals("Unknown") && !star.names().getAlphaNameOrDefault().equals("Unknown") && (star.names().alphanumericName().equals(name) || star.names().name().equals(name) || star.names().hdName().equals(name)))
                .map(star -> star.getPrimaryAttributes().distanceFromSun)
                .findFirst();
    }

    /**
     * List of star data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param stars List of stars.
     */
    public record StarDataList(boolean replace, List<StarData> stars)
    {
        public static final Codec<StarDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(StarDataList::replace),
                StarData.CODEC.listOf().fieldOf("stars").forGetter(StarDataList::stars)
        ).apply(instance, StarDataList::new));
    }

    /**
     * Data for stars.
     *
     * @param id Unique identifier for the star.
     * @param names Names associated with the star.
     * @param pos Position coordinates of the star (e.g., x, y, z).
     * @param color The color in RGB.
     * @param satellites List of satellites orbiting the star.
     * @param systemMembers List of other members in the star system.
     * @param primaryAttributes Primary attributes of the star.
     * @param secondaryAttributes Secondary attributes of the star.
     */
    public record StarData(int id, Names names, List<Double> pos, List<Double> color, List<String> satellites, List<String> systemMembers, PrimaryAttributes primaryAttributes, SecondaryAttributes secondaryAttributes)
    {
        public static final Codec<StarData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(StarData::id),
            Names.CODEC.fieldOf("Names").forGetter(StarData::names),
            Codec.DOUBLE.listOf().optionalFieldOf("Pos", List.of(0.0, 0.0, 0.0)).forGetter(StarData::pos),
            Codec.DOUBLE.listOf().optionalFieldOf("Color", List.of(0.0, 0.0, 0.0)).forGetter(StarData::color),
            Codec.STRING.listOf().optionalFieldOf("Satellites", List.of("Unknown")).forGetter(StarData::satellites),
            Codec.STRING.listOf().optionalFieldOf("SystemMembers", List.of()).forGetter(StarData::systemMembers),
            PrimaryAttributes.CODEC.fieldOf("PrimaryAttributes").forGetter(StarData::primaryAttributes),
            SecondaryAttributes.CODEC.fieldOf("SecondaryAttributes").forGetter(StarData::secondaryAttributes)
        ).apply(instance, StarData::new));

        public int getID()
        {
            return this.id();
        }

        public List<String> getSatellites()
        {
            return this.satellites;
        }

        public List<String> getSystemMembers()
        {
            return this.systemMembers;
        }

        public PrimaryAttributes getPrimaryAttributes()
        {
            return this.primaryAttributes;
        }

        public SecondaryAttributes getSecondaryAttributes()
        {
            return this.secondaryAttributes;
        }

        public double getDistanceOrRandom()
        {
            double distance = this.primaryAttributes().distanceFromEarth();
            if (distance > 0.0D)
            {
                return this.primaryAttributes().getDistanceFromEarth();
            }
            return ((RandomSource.create(this.id()).nextDouble() * 1000.0D) + 2.0D + Config.COMMON.starDistanceAdd.get()) * Config.COMMON.starDistanceMult.get();
        }

        public double getDeclinationOrRandom()
        {
            double declination = this.primaryAttributes().declination();
            if (declination >= -Math.PI / 2.0D)
            {
                return declination;
            }
            return Math.toRadians((RandomSource.create(this.id()).nextDouble() * 180.0D) - 90.0D);
        }

        public double getRightAscensionOrRandom()
        {
            double rightAscension = this.primaryAttributes().rightAscension();
            if (rightAscension >= 0.0D)
            {
                return rightAscension;
            }
            return Math.toRadians(RandomSource.create(this.id()).nextDouble() * 360.0D);
        }

        public Vec3 getPos()
        {
            double x = this.pos().get(0);
            double y = this.pos().get(1);
            double z = this.pos().get(2);
            Vec3 pos = new Vec3(x, y, z);
            if (pos.length() <= 0.0D)
            {
                pos = AHelpers.calculatePos(getDistanceOrRandom(), getDeclinationOrRandom(), getRightAscensionOrRandom());
            }
            Vec3 posAdd = pos.normalize().scale(Config.COMMON.starDistanceAdd.get());
            return pos.add(posAdd).scale(Config.COMMON.starDistanceMult.get());
        }

        public Vec3 getCachedPos()
        {
            final int id = this.id();
            if (POSITION_CACHE.containsKey(id))
            {
                return POSITION_CACHE.get(id);
            }
            Vec3 pos = getPos();
            POSITION_CACHE.put(id, pos);
            return pos;
        }

        public Vec3 getAdjustedPos(boolean updateCache)
        {
            double i = OBSERVER_INCLINATION;
            double s = OBSERVER_SEASON;
            double l = RenderEventHandler.latitude;
            double d = OBSERVER_TIME_OF_DAY;

            Vec3 pos = getCachedPos();
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

        public void isInConstellation()
        {
            final int id = this.id();
            List<ConstellationData> constellations = ConstellationDataManager.CONSTELLATION_DATA.get();

            if (!constellations.isEmpty())
            {
                for (ConstellationData constellation : constellations)
                {
                    List<List<StarData>> pairs = constellation.getPairs();
                    if (!pairs.isEmpty())
                    {
                        for (List<StarData> pair : pairs)
                        {
                            if (pair.contains(this))
                            {
                                IS_IN_CONSTELLATION_CACHE.putIfAbsent(id, true);
                            }
                        }
                    }
                }
            }
            IS_IN_CONSTELLATION_CACHE.putIfAbsent(id, false);
        }

        public Vec3 getColor()
        {
            final int id = this.id();
            if (STAR_COLOR_CACHE.containsKey(id))
            {
                return STAR_COLOR_CACHE.get(id);
            }
            double r = this.color.get(0);
            double g = this.color.get(1);
            double b = this.color.get(2);
            Vec3 color = Vec3.ZERO;
            if (r + g + b == 0.0D)
            {
                PrimaryAttributes att = this.primaryAttributes();
                color = color.add(getStarColor(att.absoluteMagnitude(), att.apparentMagnitude(), att.distanceFromEarth()));
            }
            else
            {
                color = color.add(r, g, b);
            }
            STAR_COLOR_CACHE.putIfAbsent(id, color);
            return color;
        }

        public ChatFormatting getCachedChatColor()
        {
            final int id = this.id();
            if (COLOR_CACHE.containsKey(id))
            {
                return COLOR_CACHE.get(id);
            }
            ChatFormatting color = getChatColor();
            COLOR_CACHE.put(id, color);
            return color;
        }

        public ChatFormatting getChatColor()
        {
            char spectralClass = this.secondaryAttributes().spectralClass().charAt(0);
            switch (spectralClass)
            {
                case 'O':
                    return ChatFormatting.DARK_BLUE;
                case 'B':
                    return ChatFormatting.BLUE;
                case 'A':
                    return ChatFormatting.AQUA;
                case 'F':
                    return ChatFormatting.WHITE;
                case 'G':
                    return ChatFormatting.YELLOW;
                case 'K':
                    return ChatFormatting.GOLD;
                case 'M':
                    return ChatFormatting.RED;
                case 'L':
                    return ChatFormatting.DARK_RED;
                case 'T':
                    return ChatFormatting.LIGHT_PURPLE;
                case 'Y':
                    return ChatFormatting.DARK_PURPLE;
                case 'C':
                    return ChatFormatting.DARK_GRAY;
                default:
                    return ChatFormatting.YELLOW;
            }
        }

        public double getApparentSize()
        {
            double minSize = 0;
            double maxSize = 1;
            double minMagnitude = -1;
            double maxMagnitude = 10;
            double easing = AHelpers.cubicEaseInNorm(this.getPrimaryAttributes().apparentMagnitude(), minSize, maxSize, minMagnitude, maxMagnitude, 4.0D);
            return Mth.clamp(1.0D - easing, 0.0001D, 1.0D);
        }

        public boolean isCurrentObserverSystem()
        {
            if (!this.satellites().isEmpty())
            {
                List<PlanetData> planets = PlanetDataManager.PLANET_DATA.get();
                if (!planets.isEmpty())
                {
                    for (PlanetData body : planets)
                    {
                        if (this.satellites().contains(body.names().getAlphaNameOrDefault()) || body.observer())
                        {
                            return true;
                        }
                    }
                }
                List<MinorPlanetData> minorPlanets = MinorPlanetDataManager.MINOR_PLANET_DATA.get();
                if (!minorPlanets.isEmpty())
                {
                    for (MinorPlanetData body : minorPlanets)
                    {
                        if (this.satellites().contains(body.names().getAlphaNameOrDefault()) || body.observer())
                        {
                            return true;
                        }
                    }
                }
                List<MoonData> moons = MoonDataManager.MOON_DATA.get();
                if (!moons.isEmpty())
                {
                    for (MoonData body : moons)
                    {
                        if (this.satellites().contains(body.names().getAlphaNameOrDefault()) || body.observer())
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * Names associated with a star.
     *
     * @param alphanumericName The alphanumeric name of the star.
     * @param name The common name of the star.
     * @param hdName The HD (Henry Draper) catalog name of the star.
     */
    public record Names(String alphanumericName, String name, String hdName)
    {
        public static final Codec<Names> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("AlphanumericName", "Unknown").forGetter(Names::alphanumericName),
            Codec.STRING.optionalFieldOf("Name", "Unknown").forGetter(Names::name),
            Codec.STRING.optionalFieldOf("HDName", "Unknown").forGetter(Names::hdName)
        ).apply(instance, Names::new));

        public String getAlphaNameOrDefault()
        {
            return this.alphanumericName.equals("Unknown") ? (!this.name.equals("Unknown") ? this.name : this.hdName) : this.alphanumericName;
        }
    }

    /**
     * Primary attributes of a star.
     *
     * @param absoluteMagnitude The absolute magnitude (dimensionless).
     * @param apparentMagnitude The apparent magnitude (dimensionless).
     * @param age The age in billions of years.
     * @param effectiveTemperature The effective temperature in Kelvin.
     * @param gravity The surface gravity in m/s².
     * @param luminosity The luminosity in watts (W).
     * @param density The density in g/cm³.
     * @param diameter The diameter in kilometers.
     * @param mass The mass in kg.
     * @param radius The radius in kilometers.
     * @param volume The volume in m³.
     * @param rightAscension The right ascension in degrees.
     * @param declination The declination in degrees.
     * @param distanceFromEarth The distance from Earth in light years.
     * @param distanceFromSun The distance from the Sun in light years.
     * @param eccentricity The orbital eccentricity (dimensionless).
     */
    public record PrimaryAttributes(double absoluteMagnitude, double apparentMagnitude, double age, double effectiveTemperature, double gravity, double luminosity, double density, double diameter, double mass, double radius, double volume, double rightAscension, double declination, double distanceFromEarth, double distanceFromSun, double eccentricity)
    {
        public static final Codec<PrimaryAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("AbsoluteMagnitude", 1000.0D).forGetter(PrimaryAttributes::absoluteMagnitude),
            Codec.DOUBLE.optionalFieldOf("ApparentMagnitude", 1000.0).forGetter(PrimaryAttributes::apparentMagnitude),
            Codec.DOUBLE.optionalFieldOf("Age", 0.0).forGetter(PrimaryAttributes::age),
            Codec.DOUBLE.optionalFieldOf("EffectiveTemperature", 0.0).forGetter(PrimaryAttributes::effectiveTemperature),
            Codec.DOUBLE.optionalFieldOf("Gravity", 0.0).forGetter(PrimaryAttributes::gravity),
            Codec.DOUBLE.optionalFieldOf("Luminosity", 0.0).forGetter(PrimaryAttributes::luminosity),
            Codec.DOUBLE.optionalFieldOf("Density", 0.0).forGetter(PrimaryAttributes::density),
            Codec.DOUBLE.optionalFieldOf("Diameter", 0.0).forGetter(PrimaryAttributes::diameter),
            Codec.DOUBLE.optionalFieldOf("Mass", 0.0).forGetter(PrimaryAttributes::mass),
            Codec.DOUBLE.optionalFieldOf("Radius", 0.0).forGetter(PrimaryAttributes::radius),
            Codec.DOUBLE.optionalFieldOf("Volume", 0.0).forGetter(PrimaryAttributes::volume),
            Codec.DOUBLE.optionalFieldOf("RightAscension", -1000.0).forGetter(PrimaryAttributes::rightAscension),
            Codec.DOUBLE.optionalFieldOf("Declination", -1000.0).forGetter(PrimaryAttributes::declination),
            Codec.DOUBLE.optionalFieldOf("DistanceFromEarth", -1000.0).forGetter(PrimaryAttributes::distanceFromEarth),
            Codec.DOUBLE.optionalFieldOf("DistanceFromSun", -1000.0).forGetter(PrimaryAttributes::distanceFromSun),
            Codec.DOUBLE.optionalFieldOf("Eccentricity", 0.0).forGetter(PrimaryAttributes::eccentricity)
        ).apply(instance, PrimaryAttributes::new));

        public double getDistanceFromEarth()
        {
            return (distanceFromEarth + Config.COMMON.starDistanceAdd.get()) * Config.COMMON.starDistanceMult.get();
        }

        public double getDistanceFromSun()
        {
            return (distanceFromSun + Config.COMMON.starDistanceAdd.get()) * Config.COMMON.starDistanceMult.get();
        }
    }

    /**
     * Secondary attributes of a star.
     *
     * @param constellation The constellation the star belongs to (e.g., Sagittarius).
     * @param spectralClass The spectral class of the star (e.g., G2V).
     * @param starEndState The end state of the star (e.g., white dwarf, neutron star).
     * @param starType The type of the star (e.g., main sequence, giant).
     */
    public record SecondaryAttributes(String constellation, String spectralClass, String starEndState, String starType)
    {
        public static final Codec<SecondaryAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("Constellation", "Unknown").forGetter(SecondaryAttributes::constellation),
            Codec.STRING.optionalFieldOf("SpectralClass", "Unknown").forGetter(SecondaryAttributes::spectralClass),
            Codec.STRING.optionalFieldOf("StarEndState", "Unknown").forGetter(SecondaryAttributes::starEndState),
            Codec.STRING.optionalFieldOf("StarType", "Unknown").forGetter(SecondaryAttributes::starType)
        ).apply(instance, SecondaryAttributes::new));
    }

    public static Vec3 getStarColor(double absoluteMagnitude, double apparentMagnitude, double distanceInParsecs)
    {
        if (absoluteMagnitude < 1000.0D)
        {
            int spectralType = estimateSpectralTypeAbsMag(absoluteMagnitude).hashCode();
            return SPECTRAL_TYPE_RGB.getOrDefault(spectralType, new Vec3(1.0D, 0.9749091894822445D, 0.9698780157224179D));
        }
        else if (absoluteMagnitude >= 1000.0D && distanceInParsecs > -1000.0D)
        {
            double absMag = apparentMagnitude - 5 * Math.log10(distanceInParsecs) + 5;
            int spectralType = estimateSpectralTypeAbsMag(absMag).hashCode();
            return SPECTRAL_TYPE_RGB.getOrDefault(spectralType, new Vec3(1.0D, 0.9749091894822445D, 0.9698780157224179D));
        }
        int spectralType = estimateSpectralTypeAppMag(apparentMagnitude).hashCode();
        return SPECTRAL_TYPE_RGB.getOrDefault(spectralType, new Vec3(1.0D, 0.9749091894822445D, 0.9698780157224179D));
    }

    public static String estimateSpectralTypeAbsMag(double absoluteMagnitude)
    {
        if (absoluteMagnitude < -5)
        {
            return "O"; // Blue
        }
        else if (absoluteMagnitude < 0)
        {
            return "B"; // Blue-white
        }
        else if (absoluteMagnitude < 5)
        {
            return "A"; // White
        }
        else if (absoluteMagnitude < 10)
        {
            return "F"; // Yellow-white
        }
        else if (absoluteMagnitude < 15)
        {
            return "G"; // Yellow (Sun-like)
        }
        else if (absoluteMagnitude < 20)
        {
            return "K"; // Orange
        }
        return "M"; // Red
    }

    public static String estimateSpectralTypeAppMag(double apparentMagnitude)
    {
        if (apparentMagnitude < 0)
        {
            return "O"; // Blue
        }
        else if (apparentMagnitude < 1)
        {
            return "B"; // Blue-white
        }
        else if (apparentMagnitude < 2)
        {
            return "A"; // White
        }
        else if (apparentMagnitude < 3)
        {
            return "F"; // Yellow-white
        }
        else if (apparentMagnitude < 4)
        {
            return "G"; // Yellow (Sun-like)
        }
        else if (apparentMagnitude < 5)
        {
            return "K"; // Orange
        }
        return "M"; // Red
    }

    public static Vec3 getRotation(Vec3 pos)
    {
        final double rotation = Math.toRadians(Config.COMMON.axisRotation.get());
        switch (Config.COMMON.axisRotationIndex.get())
        {
            case 1: 
                return rotY(rotation, pos);
            case 2: 
                return rotZ(rotation, pos);
            default:
                return rotX(rotation, pos);
        }
    }

    public static Vec3 getRotation2(Vec3 pos)
    {
        final double rotation = Math.toRadians(Config.COMMON.axisRotation2.get());
        switch (Config.COMMON.axisRotationIndex2.get())
        {
            case 1: 
                return rotY(rotation, pos);
            case 2: 
                return rotZ(rotation, pos);
            default:
                return rotX(rotation, pos);
        }
    }

    public static Vec3 getRotation3(Vec3 pos)
    {
        final double rotation = Math.toRadians(Config.COMMON.axisRotation3.get());
        switch (Config.COMMON.axisRotationIndex3.get())
        {
            case 1: 
                return rotY(rotation, pos);
            case 2: 
                return rotZ(rotation, pos);
            default:
                return rotX(rotation, pos);
        }
    }

    public static Quaternionf getRotation()
    {
        final float rotation = Config.COMMON.axisRotation.get().floatValue();
        switch (Config.COMMON.axisRotationIndex.get())
        {
            case 1:
                return Axis.YP.rotationDegrees(rotation);
            case 2:
                return Axis.ZP.rotationDegrees(rotation);
            case 3:
                return Axis.XN.rotationDegrees(rotation);
            case 4:
                return Axis.YN.rotationDegrees(rotation);
            case 5:
                return Axis.ZN.rotationDegrees(rotation);
            default:
                return Axis.XP.rotationDegrees(rotation);
        }
    }

    public static Quaternionf getRotation2()
    {
        final float rotation = Config.COMMON.axisRotation2.get().floatValue();
        switch (Config.COMMON.axisRotationIndex2.get())
        {
            case 1:
                return Axis.YP.rotationDegrees(rotation);
            case 2:
                return Axis.ZP.rotationDegrees(rotation);
            default:
                return Axis.XP.rotationDegrees(rotation);
        }
    }

    public static Quaternionf getRotation3()
    {
        final float rotation = Config.COMMON.axisRotation3.get().floatValue();
        switch (Config.COMMON.axisRotationIndex3.get())
        {
            case 1:
                return Axis.YP.rotationDegrees(rotation);
            case 2:
                return Axis.ZP.rotationDegrees(rotation);
            default:
                return Axis.XP.rotationDegrees(rotation);
        }
    }
}
