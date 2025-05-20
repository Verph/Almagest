package almagest.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.entity.player.Player;

import almagest.Almagest;
import almagest.client.data.CometDataManager;
import almagest.client.data.CometDataManager.CometData;
import almagest.client.data.ConstellationDataManager;
import almagest.client.data.ConstellationDataManager.ConstellationData;
import almagest.client.data.GalaxyDataManager;
import almagest.client.data.GalaxyDataManager.GalaxyData;
import almagest.client.data.MeteorSwarmDataManager;
import almagest.client.data.MeteorSwarmDataManager.MeteorData;
import almagest.client.data.MinorPlanetDataManager;
import almagest.client.data.MinorPlanetDataManager.MinorPlanetData;
import almagest.client.data.MoonDataManager;
import almagest.client.data.MoonDataManager.MoonData;
import almagest.client.data.NebulaDataManager;
import almagest.client.data.NebulaDataManager.NebulaData;
import almagest.client.data.PlanetDataManager;
import almagest.client.data.PlanetDataManager.PlanetData;
import almagest.client.data.StarDataManager;
import almagest.client.data.StarDataManager.StarData;
import almagest.client.particle.CelestialObject;
import almagest.client.particle.Comet;
import almagest.client.particle.Constellations;
import almagest.client.particle.Galaxy;
import almagest.client.particle.Meteor;
import almagest.client.particle.MinorPlanet;
import almagest.client.particle.Moon;
import almagest.client.particle.Nebula;
import almagest.client.particle.Planet;
import almagest.client.particle.Skybox;
import almagest.client.particle.Star;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.data.PlanetDataManager.*;

@SuppressWarnings("null")
public class CelestialObjectHandler
{
    public static final List<String> HAS_CUSTOM_MODELS = new ArrayList<>();
    public static CelestialObject CELESTIAL_OBJECTS_PARENT;
    public static final List<CelestialObject> CELESTIAL_OBJECTS = new ArrayList<>();
    public static final ConcurrentMap<Integer, Star> STAR_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Planet> PLANET_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, MinorPlanet> MINOR_PLANET_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Moon> MOON_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Comet> COMET_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Constellations> CONSTELLATIONS_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Meteor> METEOR_SHOWER_OBJECTS_CACHE = new ConcurrentHashMap<>();
    public static final ConcurrentMap<Integer, Meteor> METEOR_SHOWER_CHILD_OBJECTS_CACHE = new ConcurrentHashMap<>();

    public static void initData()
    {
        Almagest.LOGGER.info("Loading JSON stellar data");

        StarDataManager starData = new StarDataManager();
        ConstellationDataManager constellationData = new ConstellationDataManager();
        GalaxyDataManager galaxyData = new GalaxyDataManager();
        MinorPlanetDataManager minorPlanetData = new MinorPlanetDataManager();
        MoonDataManager moonData = new MoonDataManager();
        NebulaDataManager nebulaData = new NebulaDataManager();
        PlanetDataManager planetData = new PlanetDataManager();
        CometDataManager cometData = new CometDataManager();
        MeteorSwarmDataManager meteorData = new MeteorSwarmDataManager();

        StarDataManager.STAR_DATA = starData;
        ConstellationDataManager.CONSTELLATION_DATA = constellationData;
        GalaxyDataManager.GALAXY_DATA = galaxyData;
        MinorPlanetDataManager.MINOR_PLANET_DATA = minorPlanetData;
        MoonDataManager.MOON_DATA = moonData;
        NebulaDataManager.NEBULA_DATA = nebulaData;
        PlanetDataManager.PLANET_DATA = planetData;
        CometDataManager.COMET_DATA = cometData;
        MeteorSwarmDataManager.METEOR_DATA = meteorData;

        getCelestialBodyList(planetData.get(), minorPlanetData.get(), moonData.get(), cometData.get(), nebulaData.get(), galaxyData.get());
    }

    public static void getCelestialBodyList(List<PlanetData> planets, List<MinorPlanetData> minorPlanets, List<MoonData> moons, List<CometData> comets, List<NebulaData> nebulae, List<GalaxyData> galaxies)
    {
        List<String> entries = new ArrayList<>(10000000);
        if (!planets.isEmpty())
        {
            for (PlanetData planet : planets)
            {
                if (planet.customModel())
                {
                    entries.add(AHelpers.replaceChars(planet.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT)));
                }
            }
        }
        if (!minorPlanets.isEmpty())
        {
            for (MinorPlanetData minorPlanet : minorPlanets)
            {
                if (minorPlanet.customModel())
                {
                    entries.add(AHelpers.replaceChars(minorPlanet.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT)));
                }
            }
        }
        if (!moons.isEmpty())
        {
            for (MoonData moon : moons)
            {
                if (moon.customModel())
                {
                    entries.add(AHelpers.replaceChars(moon.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT)));
                }
            }
        }
        if (!comets.isEmpty())
        {
            for (CometData comet : comets)
            {
                if (comet.customModel())
                {
                    entries.add(AHelpers.replaceChars(comet.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT)));
                }
            }
        }
        if (!nebulae.isEmpty())
        {
            for (NebulaData nebula : nebulae)
            {
                String name = nebula.getAlphaNameOrDefault().toLowerCase(Locale.ROOT);
                if (name.equals("unknown") || !nebula.customModel()) continue;
                entries.add(AHelpers.replaceChars(name));
            }
        }
        if (!galaxies.isEmpty())
        {
            for (GalaxyData galaxy : galaxies)
            {
                String name = galaxy.names().getAlphaNameOrDefault().toLowerCase(Locale.ROOT);
                if (name.equals("unknown") || !galaxy.customModel()) continue;
                entries.add(AHelpers.replaceChars(name));
            }
        }
        if (entries.isEmpty())
        {
            entries.addAll(List.of("mercury", "venus", "earth", "mars"));
        }
        entries.addAll(List.of("skybox", "comet"));
        HAS_CUSTOM_MODELS.addAll(entries);
    }

    public static void initializeCelestialObjects(ClientLevel level, Player player, Minecraft instance)
    {
        Almagest.LOGGER.info("Initializing celestial objects!");

        for (CelestialObject celestialObject : CELESTIAL_OBJECTS)
        {
            celestialObject.remove();
        }
        CELESTIAL_OBJECTS.clear();

        if (level != null && player != null)
        {
            final ParticleEngine particleEngine = instance.particleEngine;

            CELESTIAL_OBJECTS_PARENT = new CelestialObject(level, player);
            particleEngine.add(CELESTIAL_OBJECTS_PARENT);

            Almagest.LOGGER.info("Adding skybox!");
            Skybox skybox = new Skybox(level, player);
            particleEngine.add(skybox);
            CELESTIAL_OBJECTS.add(skybox);

            Almagest.LOGGER.info("Adding planets!");
            List<PlanetData> planets = PlanetDataManager.PLANET_DATA.get();
            if (!planets.isEmpty())
            {
                for (PlanetData planet : planets)
                {
                    Planet planetParticle = new Planet(level, planet, player);
                    particleEngine.add(planetParticle);
                    CELESTIAL_OBJECTS.add(planetParticle);
                }
            }

            Almagest.LOGGER.info("Adding minor planets!");
            List<MinorPlanetData> minorPlanets = MinorPlanetDataManager.MINOR_PLANET_DATA.get();
            if (!minorPlanets.isEmpty())
            {
                for (MinorPlanetData minorPlanet : minorPlanets)
                {
                    MinorPlanet minorPlanetParticle = new MinorPlanet(level, minorPlanet, player);
                    particleEngine.add(minorPlanetParticle);
                    CELESTIAL_OBJECTS.add(minorPlanetParticle);
                }
            }

            Almagest.LOGGER.info("Adding moons!");
            List<MoonData> moons = MoonDataManager.MOON_DATA.get();
            if (!moons.isEmpty())
            {
                for (MoonData moon : moons)
                {
                    if (PARENT_CACHE.get(moon.getID()).equals(0)) continue;

                    Moon moonParticle = new Moon(level, moon, player);
                    particleEngine.add(moonParticle);
                    CELESTIAL_OBJECTS.add(moonParticle);
                }
            }

            Almagest.LOGGER.info("Adding stars!");
            List<StarData> stars = StarDataManager.STAR_DATA.get();
            if (!stars.isEmpty())
            {
                for (StarData star : stars)
                {
                    int id = star.id();
                    if (id == 0) continue;
                    Star starParticle = new Star(level, star, player);
                    particleEngine.add(starParticle);
                    CELESTIAL_OBJECTS.add(starParticle);
                    STAR_OBJECTS_CACHE.put(id, starParticle);
                }
            }

            Almagest.LOGGER.info("Adding comets!");
            List<CometData> comets = CometDataManager.COMET_DATA.get();
            if (!comets.isEmpty())
            {
                for (CometData comet : comets)
                {
                    if (comet.orbitalParameters().semimajorAxis() <= 0.0D) continue;
                    Comet cometParticle = new Comet(level, comet, player);
                    particleEngine.add(cometParticle);
                    CELESTIAL_OBJECTS.add(cometParticle);
                }
            }

            Almagest.LOGGER.info("Adding meteor showers!");
            List<MeteorData> swarms = MeteorSwarmDataManager.METEOR_DATA.get();
            if (!swarms.isEmpty())
            {
                for (MeteorData swarm : swarms)
                {
                    int swarmPopulation = (int) Math.floor(MeteorSwarmDataManager.POPULATION_CACHE.get(swarm.getID()) * Config.COMMON.meteorShowerPopulationFactor.get());
                    if (swarm.attributes().semimajorAxis() <= 0.0D || swarmPopulation <= 0) continue;

                    for (int i = 0; i < swarmPopulation; i++)
                    {
                        Meteor meteorParticle = new Meteor(level, swarm, player, i);
                        particleEngine.add(meteorParticle);
                        CELESTIAL_OBJECTS.add(meteorParticle);
                    }
                }
            }

            Almagest.LOGGER.info("Adding nebulas!");
            List<NebulaData> nebulas = NebulaDataManager.NEBULA_DATA.get();
            if (!nebulas.isEmpty())
            {
                for (NebulaData nebula : nebulas)
                {
                    if (nebula.getAlphaNameOrDefault().equals("Unknown") || nebula.texture().equals("Unknown")) continue;
                    Nebula nebulaParticle = new Nebula(level, nebula, player);
                    particleEngine.add(nebulaParticle);
                    CELESTIAL_OBJECTS.add(nebulaParticle);
                }
            }

            Almagest.LOGGER.info("Adding galaxies!");
            List<GalaxyData> galaxies = GalaxyDataManager.GALAXY_DATA.get();
            if (!galaxies.isEmpty())
            {
                for (GalaxyData galaxy : galaxies)
                {
                    if (galaxy.names().getAlphaNameOrDefault().equals("Unknown") || galaxy.texture().equals("Unknown")) continue;
                    Galaxy galaxyParticle = new Galaxy(level, galaxy, player);
                    particleEngine.add(galaxyParticle);
                    CELESTIAL_OBJECTS.add(galaxyParticle);
                }
            }

            Almagest.LOGGER.info("Adding constellations!");
            List<ConstellationData> constellations = ConstellationDataManager.CONSTELLATION_DATA.get();

            if (!constellations.isEmpty())
            {
                for (ConstellationData constellation : constellations)
                {
                    int id = constellation.id();
                    String name = constellation.name();
                    List<List<StarData>> pairs = constellation.getPairs();

                    if (!pairs.isEmpty())
                    {
                        Constellations constellationLines = new Constellations(level, id, name, pairs, player);
                        particleEngine.add(constellationLines);
                        CELESTIAL_OBJECTS.add(constellationLines);
                    }
                }
            }
        }
    }
}
