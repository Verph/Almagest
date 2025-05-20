package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

public class MinorPlanetDataManager
{
    public static MinorPlanetDataManager MINOR_PLANET_DATA;
    public final List<MinorPlanetData> minorPlanets;

    public MinorPlanetDataManager()
    {
        this.minorPlanets = initData("stellar_data", "minor_planets");
    }

    public List<MinorPlanetData> initData(String directory, String name)
    {
        List<MinorPlanetData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<MinorPlanetDataList> dataResult = MinorPlanetDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(minorplanetDataList -> {
            data.addAll(minorplanetDataList.minorPlanets());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read minor planet data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        minorPlanets.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<MinorPlanetDataList> dataResult = MinorPlanetDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(planetDataList -> {
                if (planetDataList.replace()) {
                    minorPlanets.clear();
                }
                minorPlanets.addAll(planetDataList.minorPlanets());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read minor planet data {}", partialResult.message());
            });
        }
    }*/

    public static void initCaches()
    {
        Almagest.LOGGER.info("Initializing minor planet data caches.");
        List<MinorPlanetData> bodies = MinorPlanetDataManager.MINOR_PLANET_DATA.get();
        if (!bodies.isEmpty())
        {
            for (MinorPlanetData body : bodies)
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
                body.getRotationPeriod();
                body.getSemimajorAxis();
                body.getTexture();
                body.getMeanLongitude();
            }
        }
    }

    public List<MinorPlanetData> get()
    { 
        return minorPlanets;
    }

    public static Optional<MinorPlanetData> get(String name)
    { 
        return MinorPlanetDataManager.MINOR_PLANET_DATA.get().stream()
                .filter(body -> !body.names().getAlphaNameOrDefault().equals("Unknown") && (body.names().alphanumericName().equals(name) || body.names().name().equals(name)))
                .findFirst();
    }

    public static Optional<MinorPlanetData> get(int id)
    { 
        return MinorPlanetDataManager.MINOR_PLANET_DATA.get().stream()
                .filter(body -> body.getID() == id)
                .findFirst();
    }

    /**
     * List of minor planet data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param minorPlanets List of minor planets.
     */
    public record MinorPlanetDataList(boolean replace, List<MinorPlanetData> minorPlanets)
    {
        public static final Codec<MinorPlanetDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(MinorPlanetDataList::replace),
                MinorPlanetData.CODEC.listOf().fieldOf("minor_planets").forGetter(MinorPlanetDataList::minorPlanets)
        ).apply(instance, MinorPlanetDataList::new));
    }

    /**
     * Data for minor planets.
     *
     * @param id Unique identifier for the minor planet.
     * @param names Names associated with the minor planet.
     * @param color The color in RGB.
     * @param satellites List of satellites orbiting the minor planet.
     * @param attributes Attributes of the minor planet.
     * @param orbitalParameters Orbital parameters of the minor planet.
     * @param texture The texture of the minor planet's surface.
     * @param observer If this is the observer body.
     * @param parent The name of the parent body.
     */
    public record MinorPlanetData(int id, Names names, List<Double> color, List<String> satellites, Attributes attributes, OrbitalParameters orbitalParameters, String texture, boolean customModel, boolean observer, String parent)
    {
        public static final Codec<MinorPlanetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(MinorPlanetData::id),
            Names.CODEC.fieldOf("Names").forGetter(MinorPlanetData::names),
            Codec.DOUBLE.listOf().optionalFieldOf("Color", List.of(0.0, 0.0, 0.0)).forGetter(MinorPlanetData::color),
            Codec.STRING.listOf().optionalFieldOf("Satellites", List.of()).forGetter(MinorPlanetData::satellites),
            Attributes.CODEC.fieldOf("Attributes").forGetter(MinorPlanetData::attributes),
            OrbitalParameters.CODEC.fieldOf("OrbitalParameters").forGetter(MinorPlanetData::orbitalParameters),
            Codec.STRING.optionalFieldOf("Texture", "Unknown").forGetter(MinorPlanetData::texture),
            Codec.BOOL.optionalFieldOf("CustomModel", false).forGetter(MinorPlanetData::customModel),
            Codec.BOOL.optionalFieldOf("Observer", false).forGetter(MinorPlanetData::observer),
            Codec.STRING.optionalFieldOf("Parent", "Unknown").forGetter(MinorPlanetData::parent)
        ).apply(instance, MinorPlanetData::new));

        public int getID()
        {
            return this.id() + PlanetDataManager.PLANET_DATA.get().size();
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
            if (r * g * b <= 0.0D)
            {
                return new Vec3(0.6D, 0.6D, 0.6D);
            }
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

        public void getTexture()
        {
            double diameter = this.attributes().diameter();
            if (!this.texture().toLowerCase(Locale.ROOT).equals("unknown"))
            {
                String textureName = "textures/block/minor_planets/" + this.texture().toLowerCase(Locale.ROOT);
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (this.attributes().atmosphericScaleHeight() > 0.0D)
            {
                String textureName = "textures/celestials/terrestrial_atmosphere";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 1200.0D)
            {
                String textureName = "textures/celestials/terrestrial_smooth";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 800.0D)
            {
                String textureName = "textures/celestials/terrestrial_rough";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 500.0D)
            {
                String textureName = "textures/celestials/asteroid_spheroid";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 350.0D)
            {
                String textureName = "textures/celestials/asteroid_irregular";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 200.0D)
            {
                String textureName = "textures/celestials/asteroid_large";
                TEXTURE_CACHE.put(this.getID(), textureName);
                return;
            }
            else if (diameter >= 100.0D)
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
            SATELLITES_CACHE.put(this.getID(), satellites);
        }

        public void getCachedParent()
        {
            /*int id = this.getID();
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
            }*/
            PARENT_CACHE.put(this.getID(), 0);
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
     * Names associated with a minor planet.
     *
     * @param alphanumericName The alphanumeric name of the minor planet.
     * @param name The common name of the minor planet.
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
    public record Attributes(double albedo, double atmosphericScaleHeight, double density, double diameter, double volume, double minimumTemperature, double averageTemperature, double maximumTemperature, double effectiveTemperature, double gravity, double mass, double oblateness, double obliquity, double rotationPeriod, double inclination)
    {
        public static final Codec<Attributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Albedo", 0.2D).forGetter(Attributes::albedo),
            Codec.DOUBLE.optionalFieldOf("AtmosphericScaleHeight", 0.0).forGetter(Attributes::atmosphericScaleHeight),
            Codec.DOUBLE.optionalFieldOf("Density", 0.0).forGetter(Attributes::density),
            Codec.DOUBLE.optionalFieldOf("Diameter", 0.0).forGetter(Attributes::diameter),
            Codec.DOUBLE.optionalFieldOf("Volume", 0.0).forGetter(Attributes::volume),
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
            return AHelpers.convertDaysToTicks(this.rotationPeriod()) * Config.COMMON.minorPlanetDayFactor.get();
        }

        public double getAdjustedDiameter()
        {
            return this.diameter() * Config.COMMON.minorPlanetDiameterFactor.get();
        }
    }

    /**
     * Orbital parameters of a celestial body.
     *
     * @param eccentricity The orbital eccentricity (dimensionless).
     * @param ascendingNodeLongitude The longitude of the ascending node in degrees.
     * @param orbitPeriod The orbital period in julian years.
     * @param apoapsis The apoapsis distance in kilometers.
     * @param periapsis The periapsis distance in kilometers.
     * @param apoapsisLongitude The longitude of the apoapsis in degrees.
     * @param periapsisLongitude The longitude of the periapsis in degrees.
     * @param semimajorAxis The semi-major axis in astronomical units (AU).
     * @param nodalPrecession The nodal precession rate in radians per second.
     */
    public record OrbitalParameters(double eccentricity, double ascendingNodeLongitude, double orbitPeriod, double apoapsis, double periapsis, double apoapsisLongitude, double periapsisLongitude, double semimajorAxis, double nodalPrecession)
    {
        public static final Codec<OrbitalParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Eccentricity", 0.0).forGetter(OrbitalParameters::eccentricity),
            Codec.DOUBLE.optionalFieldOf("AscendingNodeLongitude", 0.0).forGetter(OrbitalParameters::ascendingNodeLongitude),
            Codec.DOUBLE.optionalFieldOf("OrbitPeriod", 0.0).forGetter(OrbitalParameters::orbitPeriod),
            Codec.DOUBLE.optionalFieldOf("Apoapsis", 0.0).forGetter(OrbitalParameters::apoapsis),
            Codec.DOUBLE.optionalFieldOf("Periapsis", 0.0).forGetter(OrbitalParameters::periapsis),
            Codec.DOUBLE.optionalFieldOf("ApoapsisLongitude", 0.0).forGetter(OrbitalParameters::apoapsisLongitude),
            Codec.DOUBLE.optionalFieldOf("PeriapsisLongitude", 0.0).forGetter(OrbitalParameters::periapsisLongitude),
            Codec.DOUBLE.optionalFieldOf("SemimajorAxis", 0.0).forGetter(OrbitalParameters::semimajorAxis),
            Codec.DOUBLE.optionalFieldOf("NodalPrecession", 0.0).forGetter(OrbitalParameters::nodalPrecession)
        ).apply(instance, OrbitalParameters::new));

        public double getAdjustedOrbitalPeriod()
        {
            return AHelpers.convertDaysToTicks(this.orbitPeriod()) * Config.COMMON.minorPlanetOrbitFactor.get();
        }

        public double getAdjustedSemimajorAxis()
        {
            return this.semimajorAxis() * Config.COMMON.minorPlanetDistanceFactor.get();
        }
    }
}