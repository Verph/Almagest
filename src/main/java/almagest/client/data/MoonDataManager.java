package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.client.data.MinorPlanetDataManager.MinorPlanetData;
import almagest.client.data.PlanetDataManager.PlanetData;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

import static almagest.client.data.PlanetDataManager.*;

public class MoonDataManager
{
    public static MoonDataManager MOON_DATA;
    public final List<MoonData> moons;

    public MoonDataManager()
    {
        this.moons = initData("stellar_data", "moons");
    }

    public List<MoonData> initData(String directory, String name)
    {
        List<MoonData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<MoonDataList> dataResult = MoonDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(moonDataList -> {
            data.addAll(moonDataList.moons());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read moon data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        moons.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<MoonDataList> dataResult = MoonDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(moonDataList -> {
                if (moonDataList.replace()) {
                    moons.clear();
                }
                moons.addAll(moonDataList.moons());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read moon data {}", partialResult.message());
            });
        }
    }*/

    public static void initCaches()
    {
        Almagest.LOGGER.info("Initializing moon data caches.");
        List<MoonData> bodies = MoonDataManager.MOON_DATA.get();
        if (!bodies.isEmpty())
        {
            for (MoonData body : bodies)
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
                body.getAscendingNodeLongitude();
                body.getPeriapsisLongitude();
                body.getRotationPeriod();
                body.getSemimajorAxis();
                body.getOrbitPeriod();
                body.getTexture();
                body.getMeanLongitude();
            }
        }
    }

    public List<MoonData> get()
    { 
        return moons;
    }

    public static Optional<MoonData> get(String name)
    { 
        return MoonDataManager.MOON_DATA.get().stream()
                .filter(body -> !body.names().getAlphaNameOrDefault().equals("Unknown") && (body.names().alphanumericName().equals(name) || body.names().name().equals(name)))
                .findFirst();
    }

    public static Optional<MoonData> get(int id)
    { 
        return MoonDataManager.MOON_DATA.get().stream()
                .filter(body -> body.getID() == id)
                .findFirst();
    }

    /**
     * List of moons data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param moons List of moons.
     */
    public record MoonDataList(boolean replace, List<MoonData> moons)
    {
        public static final Codec<MoonDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(MoonDataList::replace),
                MoonData.CODEC.listOf().fieldOf("moons").forGetter(MoonDataList::moons)
        ).apply(instance, MoonDataList::new));
    }

    /**
     * Data for moons.
     *
     * @param id Unique identifier for the moons.
     * @param names Names associated with the moons.
     * @param color The color in RGB.
     * @param nearestPlanet The nearest planet to the moons.
     * @param apparentMotion The apparent motion of the moons (e.g., prograde, retrograde).
     * @param satellites List of satellites orbiting the moons.
     * @param attributes Attributes of the moons.
     * @param orbitalParameters Orbital parameters of the moons.
     * @param texture The texture of the moons's surface.
     * @param observer If this is the observer body.
     * @param parent The name of the parent body.
     */
    public record MoonData(int id, Names names, List<Double> color, String nearestPlanet, String apparentMotion, List<String> satellites, Attributes attributes, OrbitalParameters orbitalParameters, String texture, boolean customModel, boolean observer, String parent)
    {
        public static final Codec<MoonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(MoonData::id),
            Names.CODEC.fieldOf("Names").forGetter(MoonData::names),
            Codec.DOUBLE.listOf().optionalFieldOf("Color", List.of(0.8D, 0.8D, 0.8D)).forGetter(MoonData::color),
            Codec.STRING.optionalFieldOf("NearestPlanet", "Unknown").forGetter(MoonData::nearestPlanet),
            Codec.STRING.optionalFieldOf("RetrogradeApparentMotionQuery", "prograde").forGetter(MoonData::apparentMotion),
            Codec.STRING.listOf().optionalFieldOf("Satellites", List.of()).forGetter(MoonData::satellites),
            Attributes.CODEC.fieldOf("Attributes").forGetter(MoonData::attributes),
            OrbitalParameters.CODEC.fieldOf("OrbitalParameters").forGetter(MoonData::orbitalParameters),
            Codec.STRING.optionalFieldOf("Texture", "Unknown").forGetter(MoonData::texture),
            Codec.BOOL.optionalFieldOf("CustomModel", false).forGetter(MoonData::customModel),
            Codec.BOOL.optionalFieldOf("Observer", false).forGetter(MoonData::observer),
            Codec.STRING.optionalFieldOf("Parent", "Unknown").forGetter(MoonData::parent)
        ).apply(instance, MoonData::new));

        public int getID()
        {
            return this.id() + PlanetDataManager.PLANET_DATA.get().size() + MinorPlanetDataManager.MINOR_PLANET_DATA.get().size();
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
            return (RandomSource.create(this.getID()).nextDouble() * 2.0D - 1.0D) * 360.0D;
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
                String textureName = "textures/block/moons/" + this.texture().toLowerCase(Locale.ROOT);
                if (Config.COMMON.toggleEasterEggMoon.get() && this.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT).equals("moon"))
                {
                    textureName = "textures/block/moons/moon_majora_0";
                }
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
            PARENT_CACHE.put(id, PlanetDataManager.get(this.nearestPlanet().toLowerCase(Locale.ROOT)).orElse(PlanetDataManager.get(0).get()).getID());
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

        public void getSemimajorAxis()
        {
            SEMIMAJOR_AXIS_CACHE.put(this.getID(), this.orbitalParameters().getAdjustedSemimajorAxis());
        }

        public void getNodalPrecession()
        {
            NODAL_PRECESSION_CACHE.put(this.getID(), this.orbitalParameters().nodalPrecession());
        }

        public void getOrbitPeriod()
        {
            final int id = this.getID();
            ORBIT_PERIOD_CACHE.put(id, AHelpers.convertDaysToTicks(this.orbitalParameters().orbitPeriod()) * Config.COMMON.moonOrbitFactor.get());
            //ORBIT_PERIOD_CACHE.put(id, AHelpers.convertYearsToTicks(PlanetDataManager.getOrbitalPeriod(PARENT_CACHE.get(id).doubleValue(), MASS_CACHE.get(id).doubleValue(), SEMIMAJOR_AXIS_CACHE.get(id).doubleValue())) * Config.COMMON.moonOrbitFactor.get());
        }

        public void getMeanLongitude()
        {
            final int id = this.getID();
            double periapsisLongitude = PERIAPSIS_LONGITUDE_CACHE.getOrDefault(id, 0.0D).doubleValue();
            double ascendingNodeLongitude = ASCENDING_NODE_LONGITUDE_CACHE.getOrDefault(id, 0.0D).doubleValue();
            double meanLongitude = (2.0D * Math.PI * (periapsisLongitude + ascendingNodeLongitude)) / 360.0D;
            MEAN_LONGITUDE_CACHE.put(id, meanLongitude);
        }
    }

    /**
     * Names associated with a moon.
     *
     * @param alphanumericName The alphanumeric name of the moon.
     * @param name The common name of the moon.
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
            return AHelpers.convertDaysToTicks(this.rotationPeriod()) * Config.COMMON.moonDayFactor.get();
        }

        public double getAdjustedDiameter()
        {
            return this.diameter() * Config.COMMON.moonDiameterFactor.get();
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
     * @param semimajorAxis The semi-major axis in astronomical units (AU).
     * @param nodalPrecession The nodal precession rate in radians per second.
     */
    public record OrbitalParameters(double eccentricity, double ascendingNodeLongitude, double orbitPeriod, double apoapsis, double periapsis, double semimajorAxis, double nodalPrecession)
    {
        public static final Codec<OrbitalParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Eccentricity", 0.0).forGetter(OrbitalParameters::eccentricity),
            Codec.DOUBLE.optionalFieldOf("AscendingNodeLongitude", 0.0).forGetter(OrbitalParameters::ascendingNodeLongitude),
            Codec.DOUBLE.optionalFieldOf("OrbitPeriod", 0.0).forGetter(OrbitalParameters::orbitPeriod),
            Codec.DOUBLE.optionalFieldOf("Apoapsis", 0.0).forGetter(OrbitalParameters::apoapsis),
            Codec.DOUBLE.optionalFieldOf("Periapsis", 0.0).forGetter(OrbitalParameters::periapsis),
            Codec.DOUBLE.optionalFieldOf("SemimajorAxis", 0.0).forGetter(OrbitalParameters::semimajorAxis),
            Codec.DOUBLE.optionalFieldOf("NodalPrecession", 0.0).forGetter(OrbitalParameters::nodalPrecession)
        ).apply(instance, OrbitalParameters::new));

        public double getAdjustedOrbitalPeriod()
        {
            return Math.max(AHelpers.convertDaysToTicks(this.orbitPeriod()) * Config.COMMON.moonOrbitFactor.get(), 0.1D);
        }

        public double getAdjustedSemimajorAxis()
        {
            double semimajorAxis = this.semimajorAxis();
            if (semimajorAxis > 1.0D)
            {
                semimajorAxis = AHelpers.convertKMtoAU(semimajorAxis);
            }
            return semimajorAxis * Config.COMMON.moonDistanceFactor.get();
        }
    }
}
