package almagest.client.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import almagest.Almagest;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.DataHelpers;

import static almagest.client.data.PlanetDataManager.*;

public class CometDataManager
{
    public static CometDataManager COMET_DATA;
    public final List<CometData> comets;

    public CometDataManager()
    {
        this.comets = initData("stellar_data", "comets");
    }

    public List<CometData> initData(String directory, String name)
    {
        List<CometData> data = new ArrayList<>();
        Map.Entry<ResourceLocation, JsonElement> entry = DataHelpers.scanDirectoryForFile(directory, name);
        DataResult<CometDataList> dataResult = CometDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
        dataResult.get().ifLeft(cometDataList -> {
            data.addAll(cometDataList.comets());
        }).ifRight((partialResult) -> {
            Almagest.LOGGER.warn("Failed to read comet data {}", partialResult.message());
        });
        return data;
    }

    /*@SuppressWarnings("null")
    @Override
    public void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
    {
        comets.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet())
        {
            DataResult<CometDataList> dataResult = CometDataList.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
            dataResult.get().ifLeft(cometDataList -> {
                if (cometDataList.replace()) {
                    comets.clear();
                }
                comets.addAll(cometDataList.comets());
            }).ifRight((partialResult) -> {
                Almagest.LOGGER.warn("Failed to read comet data {}", partialResult.message());
            });
        }
    }*/

    public static void initCaches()
    {
        Almagest.LOGGER.info("Initializing comet data caches.");
        List<CometData> bodies = CometDataManager.COMET_DATA.get();
        if (!bodies.isEmpty())
        {
            for (CometData body : bodies)
            {
                body.getCachedParent();
                body.getAlbedo();
                body.getDiameter();
                body.getMass();
                body.getEccentricity();
                body.getInclination();
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

    public List<CometData> get()
    { 
        return comets;
    }

    public Optional<CometData> get(String name)
    { 
        return comets.stream()
                .filter(comet -> !name.equals("Unknown") && !comet.names().getAlphaNameOrDefault().equals("Unknown") && (comet.names().alphanumericName().equals(name) || comet.names().name().equals(name)))
                .findFirst();
    }

    /**
     * List of comets data.
     *
     * @param replace Indicates whether to replace the existing list.
     * @param comets List of comets.
     */
    public record CometDataList(boolean replace, List<CometData> comets)
    {
        public static final Codec<CometDataList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(CometDataList::replace),
                CometData.CODEC.listOf().fieldOf("comets").forGetter(CometDataList::comets)
        ).apply(instance, CometDataList::new));
    }

    /**
     * Data for comets.
     *
     * @param id Unique identifier for the comets.
     * @param names Names associated with the comets.
     * @param attributes Attributes of the comets.
     * @param orbitalParameters Orbital parameters of the comets.
     * @param texture The texture of the comets's surface.
     */
    public record CometData(int id, Names names, Attributes attributes, OrbitalParameters orbitalParameters, String texture, boolean customModel)
    {
        public static final Codec<CometData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("ID", 0).forGetter(CometData::id),
            Names.CODEC.fieldOf("Names").forGetter(CometData::names),
            Attributes.CODEC.fieldOf("Attributes").forGetter(CometData::attributes),
            OrbitalParameters.CODEC.fieldOf("OrbitalParameters").forGetter(CometData::orbitalParameters),
            Codec.STRING.optionalFieldOf("Texture", "Unknown").forGetter(CometData::texture),
            Codec.BOOL.optionalFieldOf("CustomModel", false).forGetter(CometData::customModel)
        ).apply(instance, CometData::new));

        public int getID()
        {
            return this.id() + PlanetDataManager.PLANET_DATA.get().size() + MinorPlanetDataManager.MINOR_PLANET_DATA.get().size() + MoonDataManager.MOON_DATA.get().size();
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
            if (diameter >= 500.0D)
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

        public void getCachedParent()
        {
            int id = this.getID();
            /*final Set<Character> charsToRemove = Set.of(' ', '/');
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
            PARENT_CACHE.put(id, 0);
        }

        public void getAlbedo()
        {
            ALBEDO_CACHE.put(this.getID(), 0.06D);
        }

        public void getDiameter()
        {
            DIAMETER_CACHE.put(this.getID(), this.attributes().getAdjustedDiameter());
        }

        public void getMass()
        {
            MASS_CACHE.put(this.getID(), this.attributes().mass());
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
     * Names associated with a comet.
     *
     * @param alphanumericName The alphanumeric name of the comet.
     * @param name The common name of the comet.
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
     * Attributes of a comet.
     *
     * @param diameter The diameter in kilometers.
     * @param volume The volume in m³.
     * @param mass The mass in kilograms.
     * @param rotationPeriod The rotation period in hours.
     * @param inclination The inclination in degrees.
     */
    public record Attributes(double diameter, double volume, double mass, double rotationPeriod, double inclination)
    {
        public static final Codec<Attributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Diameter", 0.0).forGetter(Attributes::diameter),
            Codec.DOUBLE.optionalFieldOf("Volume", 0.0).forGetter(Attributes::volume),
            Codec.DOUBLE.optionalFieldOf("Mass", 0.0).forGetter(Attributes::mass),
            Codec.DOUBLE.optionalFieldOf("RotationPeriod", 0.0).forGetter(Attributes::rotationPeriod),
            Codec.DOUBLE.optionalFieldOf("Inclination", 0.0).forGetter(Attributes::inclination)
        ).apply(instance, Attributes::new));

        public double getAdjustedDayDuration()
        {
            return AHelpers.convertDaysToTicks(this.rotationPeriod()) * Config.COMMON.cometDayFactor.get();
        }

        public double getAdjustedDiameter()
        {
            return this.diameter() * Config.COMMON.cometDiameterFactor.get();
        }
    }

    /**
     * Orbital parameters of a comet.
     *
     * @param eccentricity The orbital eccentricity (dimensionless).
     * @param ascendingNodeLongitude The longitude of the ascending node in degrees.
     * @param orbitPeriod The orbital period in julian years.
     * @param apoapsis The apoapsis distance in kilometers.
     * @param periapsis The periapsis distance in kilometers.
     * @param apoapsisLongitude The longitude of the apoapsis in degrees.
     * @param periapsisLongitude The longitude of the periapsis in degrees.
     * @param semimajorAxis The semi-major axis in astronomical units (AU).
     */
    public record OrbitalParameters(double eccentricity, double ascendingNodeLongitude, double orbitPeriod, double apoapsis, double periapsis, double apoapsisLongitude, double periapsisLongitude, double semimajorAxis)
    {
        public static final Codec<OrbitalParameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("Eccentricity", 0.0).forGetter(OrbitalParameters::eccentricity),
            Codec.DOUBLE.optionalFieldOf("AscendingNodeLongitude", 0.0).forGetter(OrbitalParameters::ascendingNodeLongitude),
            Codec.DOUBLE.optionalFieldOf("OrbitPeriod", 0.0).forGetter(OrbitalParameters::orbitPeriod),
            Codec.DOUBLE.optionalFieldOf("Apoapsis", 0.0).forGetter(OrbitalParameters::apoapsis),
            Codec.DOUBLE.optionalFieldOf("Periapsis", 0.0).forGetter(OrbitalParameters::periapsis),
            Codec.DOUBLE.optionalFieldOf("ApoapsisLongitude", 0.0).forGetter(OrbitalParameters::apoapsisLongitude),
            Codec.DOUBLE.optionalFieldOf("PeriapsisLongitude", 0.0).forGetter(OrbitalParameters::periapsisLongitude),
            Codec.DOUBLE.optionalFieldOf("SemimajorAxis", 0.0).forGetter(OrbitalParameters::semimajorAxis)
        ).apply(instance, OrbitalParameters::new));

        public double getAdjustedOrbitalPeriod()
        {
            return AHelpers.convertDaysToTicks(this.orbitPeriod()) * Config.COMMON.cometOrbitFactor.get();
        }

        public double getAdjustedSemimajorAxis()
        {
            return this.semimajorAxis() * Config.COMMON.cometDistanceFactor.get();
        }
    }
}