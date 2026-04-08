package almagest.client.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import org.joml.Math;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import almagest.Almagest;
import almagest.client.CelestialObjectHandler;
import almagest.client.data.CelestialObjectTypes.Atmosphere;
import almagest.client.data.CelestialObjectTypes.Body;
import almagest.client.data.CelestialObjectTypes.CelestialData;
import almagest.client.data.CelestialObjectTypes.ConstellationData;
import almagest.client.data.CelestialObjectTypes.Life;
import almagest.client.data.CelestialObjectTypes.Orbit;
import almagest.client.data.CelestialObjectTypes.Ring;
import almagest.client.data.CelestialObjectTypes.Season;
import almagest.client.data.CelestialObjectTypes.StarData;
import almagest.client.data.CelestialObjectTypes.Trail;
import almagest.client.particle.CelestialObject;
import almagest.client.particle.Constellations;
import almagest.client.particle.Skybox;
import almagest.client.particle.Star;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.Color;
import almagest.util.ColorUtils;
import almagest.util.DataHelpers;
import almagest.util.Nature;
import almagest.util.PlanetHelpers;

public class CelestialDataManager
{
    public static long globalIdCounter = 0L;
    public static long starIdCounter = 0L;
    public static long planetIdCounter = 0L;
    public static long constellationIdCounter = 0L;
    public static final double DEFAULT_SURFACE_LEVEL = 63.0D;

    public static volatile int starsCreated = 0;
    public static volatile int starsRegistered = 0;
    public static volatile boolean allStarsLoaded = false;

    public static volatile boolean HAS_INITIALIZED_CELESTIAL_DATA = false;
    public static volatile boolean HAS_INITIALIZED_CELESTIAL_OBJECTS = false;
    public static volatile boolean HAS_INITIALIZED_OTHER_PARAMETERS = false;

    public static final ExecutorService STAR_LOADER = Executors.newSingleThreadExecutor(r -> new Thread(r, "Almagest-StarLoader"));

    public static final Long2ObjectMap<CelestialData> CELESTIAL_DATA_BY_ID = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<CelestialObject> CELESTIAL_OBJECTS_BY_ID = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<StarData> STAR_DATA_BY_ID = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<Star> STAR_OBJECTS_BY_ID = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<ConstellationData> CONSTELLATION_DATA_BY_ID = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<Constellations> CONSTELLATIONS_BY_ID = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<Skybox> SKYBOX = new Long2ObjectOpenHashMap<>();

    public static final Object2ObjectMap<ConstellationData, Constellations> CONSTELLATION_OBJECTS = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<StarData, ConstellationData> STAR_DATA_TO_CONSTELLATIONS = new Object2ObjectOpenHashMap<>();
    public static final Set<String> BODY_MODELS = new LinkedHashSet<>();

    public static void init()
    {
        List<ConcurrentMap.Entry<ResourceLocation, JsonElement>> JSON_ENTRIES = DataHelpers.scanDirectoryForAllJson("stellar_data");

        JSON_ENTRIES.addAll(DataHelpers.scanCustomData());

        CELESTIAL_DATA_BY_ID.clear();
        CELESTIAL_OBJECTS_BY_ID.clear();
        STAR_DATA_BY_ID.clear();
        STAR_OBJECTS_BY_ID.clear();
        CONSTELLATION_DATA_BY_ID.clear();
        CONSTELLATIONS_BY_ID.clear();
        SKYBOX.clear();
        CONSTELLATION_OBJECTS.clear();
        STAR_DATA_TO_CONSTELLATIONS.clear();
        BODY_MODELS.clear();

        initData(JSON_ENTRIES);
        JSON_ENTRIES.clear();
        getBlockStateData();
    }

    public static Collection<String> getBlockStateData()
    {
        if (!BODY_MODELS.isEmpty())
        {
            return BODY_MODELS;
        }

        Set<String> bodies = new LinkedHashSet<>();
        CelestialDataManager.CELESTIAL_DATA_BY_ID.values().forEach(object -> {
            String model;
            if (object.hasModel() && !object.getNames().isEmpty())
            {
                model = object.getNames().get(0).toLowerCase(Locale.ROOT).replace(" ", "_").replaceAll("[^a-z0-9/._-]", "_").trim();
            }
            else
            {
                model = object.getBody().getBodyDescription().toLowerCase(Locale.ROOT).replace(" ", "_").replaceAll("[^a-z0-9/._-]", "_").trim();
            }

            if (!model.equals("unknown_body_type"))
            {
                bodies.add(model);
            }
        });
        for (CelestialObjectTypes type : CelestialObjectTypes.values())
        {
            bodies.add(type.getSerializedName());
        }
        //bodies.addAll(Body.getAllCombinations());
        bodies.add("skybox");
        BODY_MODELS.addAll(bodies);

        if (Config.COMMON.printListAllCelestialObjects.get())
        {
            AHelpers.printListPerRow(List.of(bodies), 10, "Added block models");
        }

        return bodies;
    }


    /**
     * Retrieves planet/moon/asteroid data by ID, name, or list of names.
     * <p>
     * Supported key types:
     * <ul>
     *   <li>{@link Long} - direct ID lookup (fastest)</li>
     *   <li>{@link String} - case-insensitive name search</li>
     *   <li>{@link List} - first match from any name in the list</li>
     * </ul>
     * Name lookups scan {@link CelestialData#getNames()} across all entries.
     *
     * @param key the lookup key (ID, name, or list of names)
     * @return Optional containing the {@link CelestialData}, or empty if not found
     */
    public static Optional<CelestialData> getPlanetData(Object key)
    {
        if (key == null)
        {
            Almagest.LOGGER.warn("Input key is null.");
            return Optional.empty();
        }
        if (key instanceof Long id)
        {
            return Optional.ofNullable(CELESTIAL_DATA_BY_ID.get(id.longValue()));
        }
        if (key instanceof String name)
        {
            String normalized = name.trim().toLowerCase(Locale.ROOT);
            return CELESTIAL_DATA_BY_ID.values().stream()
                    .filter(data -> data.getNames().stream()
                            .anyMatch(n -> n.trim().toLowerCase(Locale.ROOT).equals(normalized)))
                    .findFirst();
        }
        if (key instanceof List<?> list)
        {
            List<String> search = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            return CELESTIAL_DATA_BY_ID.values().stream()
                    .filter(data -> data.getNames().stream()
                            .map(n -> n.trim().toLowerCase(Locale.ROOT))
                            .anyMatch(search::contains))
                    .findFirst();
        }
        Almagest.LOGGER.warn("Invalid key type: {}", key.getClass().getSimpleName());
        return Optional.empty();
    }

    /**
     * Retrieves star data by ID, proper name, or catalog designation.
     * <p>
     * Supported key types:
     * <ul>
     *   <li>{@link Long} - direct ID lookup (fastest)</li>
     *   <li>{@link String} - case-insensitive name search</li>
     *   <li>{@link List} - first match from any name in the list</li>
     * </ul>
     * Uses case-insensitive matching against all known names from {@link StarData#getAllNames()}.
     *
     * @param key the lookup key
     * @return Optional containing the {@link StarData}, or empty if not found
     */
    public static Optional<StarData> getStarData(Object key)
    {
        if (key == null)
        {
            Almagest.LOGGER.warn("Input key is null.");
            return Optional.empty();
        }
        if (key instanceof Long id)
        {
            return Optional.ofNullable(STAR_DATA_BY_ID.get(id.longValue()));
        }
        if (key instanceof String name)
        {
            String normalized = name.trim().toLowerCase(Locale.ROOT);
            return STAR_DATA_BY_ID.values().stream()
                    .filter(data -> data.getAllNames().stream()
                            .anyMatch(n -> n.trim().toLowerCase(Locale.ROOT).equals(normalized)))
                    .findFirst();
        }
        if (key instanceof List<?> list)
        {
            List<String> search = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            return STAR_DATA_BY_ID.values().stream()
                    .filter(data -> data.getAllNames().stream()
                            .map(n -> n.trim().toLowerCase(Locale.ROOT))
                            .anyMatch(search::contains))
                    .findFirst();
        }
        Almagest.LOGGER.warn("Invalid key type: {}", key.getClass().getSimpleName());
        return Optional.empty();
    }

    /**
     * Retrieves constellation data by ID or name (case-insensitive).
     * <p>
     * Supported key types:
     * <ul>
     *   <li>{@link Long} - direct ID lookup (fastest)</li>
     *   <li>{@link String} - case-insensitive name search</li>
     *   <li>{@link List} - first match from any name in the list</li>
     * </ul>
     * Constellations have a single canonical name via {@link ConstellationData#getName()}.
     *
     * @param key the lookup key
     * @return Optional containing the {@link ConstellationData}, or empty if not found
     */
    public static Optional<ConstellationData> getConstellationData(Object key)
    {
        if (key == null)
        {
            Almagest.LOGGER.warn("Input key is null.");
            return Optional.empty();
        }
        if (key instanceof Long id)
        {
            return Optional.ofNullable(CONSTELLATION_DATA_BY_ID.get(id.longValue()));
        }
        if (key instanceof String name)
        {
            String normalized = name.trim().toLowerCase(Locale.ROOT);
            return CONSTELLATION_DATA_BY_ID.values().stream()
                    .filter(c -> c.getName().trim().toLowerCase(Locale.ROOT).equals(normalized))
                    .findFirst();
        }
        if (key instanceof List<?> list)
        {
            List<String> search = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            return CONSTELLATION_DATA_BY_ID.values().stream()
                    .filter(c -> search.contains(c.getName().trim().toLowerCase(Locale.ROOT)))
                    .findFirst();
        }
        Almagest.LOGGER.warn("Invalid key type: {}", key.getClass().getSimpleName());
        return Optional.empty();
    }

    /**
     * Retrieves a rendered celestial body (planet, moon, etc.) by its data, ID, or name.
     * <p>
     * Supported key types:
     * <ul>
     *   <li>{@link CelestialData} - direct lookup using the data object's ID</li>
     *   <li>{@link Long} - direct ID lookup</li>
     *   <li>{@link String} - case-insensitive name search</li>
     *   <li>{@link List} - first match from any name in the list</li>
     * </ul>
     * Fast path for {@link CelestialData} or {@link Long} ID. Name lookups scan all objects.
     *
     * @param key the lookup key
     * @return Optional containing the {@link CelestialObject}, or empty if not found
     */
    public static Optional<CelestialObject> getCelestialObject(Object key)
    {
        if (key == null)
        {
            Almagest.LOGGER.warn("Input key is null.");
            return Optional.empty();
        }
        if (key instanceof CelestialData data)
        {
            return Optional.ofNullable(CELESTIAL_OBJECTS_BY_ID.get(data.getId()));
        }
        if (key instanceof Long id)
        {
            return Optional.ofNullable(CELESTIAL_OBJECTS_BY_ID.get(id.longValue()));
        }
        if (key instanceof String name)
        {
            String norm = name.trim().toLowerCase(Locale.ROOT);
            return CELESTIAL_OBJECTS_BY_ID.values().stream()
                    .filter(obj -> obj.body.getNames().stream()
                            .anyMatch(n -> n.trim().toLowerCase(Locale.ROOT).equals(norm)))
                    .findFirst();
        }
        if (key instanceof List<?> list)
        {
            List<String> search = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            return CELESTIAL_OBJECTS_BY_ID.values().stream()
                    .filter(obj -> obj.body.getNames().stream()
                            .map(n -> n.trim().toLowerCase(Locale.ROOT))
                            .anyMatch(search::contains))
                    .findFirst();
        }
        Almagest.LOGGER.warn("Invalid key type: {}", key.getClass().getSimpleName());
        return Optional.empty();
    }

    /**
     * Retrieves a rendered star object by ID or any of its names/designations.
     * <p>
     * Supported key types:
     * <ul>
     *   <li>{@link StarData} - direct lookup using the data object's ID</li>
     *   <li>{@link Long} - direct ID lookup (fastest)</li>
     *   <li>{@link String} - case-insensitive name search</li>
     *   <li>{@link List} - first match from any name in the list</li>
     * </ul>
     * Searches across all names returned by {@code star.getAllNames()}.
     *
     * @param key the lookup key
     * @return Optional containing the {@link Star}, or empty if not found
     */
    public static Optional<Star> getStarObject(Object key)
    {
        if (key == null)
        {
            Almagest.LOGGER.warn("Input key is null.");
            return Optional.empty();
        }
        if (key instanceof StarData data)
        {
            return Optional.ofNullable(STAR_OBJECTS_BY_ID.get(data.getId()));
        }
        if (key instanceof Long id)
        {
            return Optional.ofNullable(STAR_OBJECTS_BY_ID.get(id.longValue()));
        }
        if (key instanceof String name)
        {
            String norm = name.trim().toLowerCase(Locale.ROOT);
            return STAR_OBJECTS_BY_ID.values().stream()
                    .filter(s -> s.star.getAllNames().stream()
                            .anyMatch(n -> n.trim().toLowerCase(Locale.ROOT).equals(norm)))
                    .findFirst();
        }
        if (key instanceof List<?> list)
        {
            List<String> search = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            return STAR_OBJECTS_BY_ID.values().stream()
                    .filter(s -> s.star.getAllNames().stream()
                            .map(n -> n.trim().toLowerCase(Locale.ROOT))
                            .anyMatch(search::contains))
                    .findFirst();
        }
        Almagest.LOGGER.warn("Invalid key type: {}", key.getClass().getSimpleName());
        return Optional.empty();
    }

    /**
     * Retrieves a rendered constellation by ID or name (case-insensitive).
     * <p>
     * Supported key types:
     * <ul>
     *   <li>{@link ConstellationData} - direct lookup using the data object's ID</li>
     *   <li>{@link Long} - direct ID lookup (fastest)</li>
     *   <li>{@link String} - case-insensitive name search</li>
     *   <li>{@link List} - first match from any name in the list</li>
     * </ul>
     * @param key the lookup key
     * @return Optional containing the {@link Constellations} object, or empty if not found
     */
    public static Optional<Constellations> getConstellationObject(Object key)
    {
        if (key == null)
        {
            Almagest.LOGGER.warn("Input key is null.");
            return Optional.empty();
        }
        if (key instanceof ConstellationData data)
        {
            return Optional.ofNullable(CONSTELLATIONS_BY_ID.get(data.getId()));
        }
        if (key instanceof Long id)
        {
            return Optional.ofNullable(CONSTELLATIONS_BY_ID.get(id.longValue()));
        }
        if (key instanceof String name)
        {
            String norm = name.trim().toLowerCase(Locale.ROOT);
            return CONSTELLATIONS_BY_ID.values().stream()
                    .filter(c -> c.constellation.getName().trim().toLowerCase(Locale.ROOT).equals(norm))
                    .findFirst();
        }
        if (key instanceof List<?> list)
        {
            List<String> search = list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();
            return CONSTELLATIONS_BY_ID.values().stream()
                    .filter(c -> search.contains(c.constellation.getName().trim().toLowerCase(Locale.ROOT)))
                    .findFirst();
        }
        Almagest.LOGGER.warn("Invalid key type: {}", key.getClass().getSimpleName());
        return Optional.empty();
    }

    public static void initData(List<ConcurrentMap.Entry<ResourceLocation, JsonElement>> dataEntries)
    {
        Almagest.LOGGER.debug("Initializing all data");
        long startTime = System.nanoTime();

        List<String> celestialObjectNames = new ArrayList<>();
        List<String> starObjectNames = new ArrayList<>();
        List<String> constellationObjectNames = new ArrayList<>();

        for (Entry<ResourceLocation, JsonElement> entry : dataEntries)
        {
            ResourceLocation key = entry.getKey();
            JsonElement json = entry.getValue();
            String fileName = key.getPath();

            JsonArray celestialArray = extractCelestialArray(json, fileName);
            if (celestialArray == null) continue;

            for (JsonElement element : celestialArray)
            {
                JsonObject typeObj = validateTypeObject(element, fileName);
                if (typeObj == null) continue;

                CelestialObjectTypes type = extractType(typeObj, fileName);
                if (type == null) continue;

                JsonObject dataObj = typeObj.getAsJsonObject(type.getSerializedName());
                if (dataObj == null)
                {
                    Almagest.LOGGER.error("Null data object for type " + type + " in " + fileName);
                    continue;
                }

                if (type == CelestialObjectTypes.CONSTELLATION)
                {
                    parseConstellation(dataObj, fileName, constellationObjectNames);
                }
                else
                {
                    parseCelestialOrStar(dataObj, type, fileName, celestialObjectNames, starObjectNames);
                }
            }
        }

        Almagest.LOGGER.debug("Indexing constellation memberships");
        long startTimeConstellations = System.nanoTime();

        Map<String, StarData> STAR_ALIAS_MAP = new HashMap<>(STAR_DATA_BY_ID.size() * 3);
        for (StarData star : STAR_DATA_BY_ID.values())
        {
            for (String alias : star.getAllNames())
            {
                STAR_ALIAS_MAP.put(alias.trim().toLowerCase(Locale.ROOT), star);
            }
        }

        for (ConstellationData constellation : CONSTELLATION_DATA_BY_ID.values())
        {
            for (List<String> pair : constellation.getPairs())
            {
                for (String name : pair)
                {
                    StarData star = STAR_ALIAS_MAP.get(name.trim().toLowerCase(Locale.ROOT));
                    if (star != null)
                    {
                        STAR_DATA_TO_CONSTELLATIONS.put(star, constellation);
                    }
                }
            }
        }

        logDuration("Task", startTimeConstellations);

        //finalizeStarSorting();

        logParsedLists(celestialObjectNames, starObjectNames, constellationObjectNames);
        HAS_INITIALIZED_CELESTIAL_DATA = true;

        logDuration("Data processing", startTime);
    }

    public static JsonArray extractCelestialArray(JsonElement json, String fileName)
    {
        if (!json.isJsonObject())
        {
            Almagest.LOGGER.error("Invalid JSON root in " + fileName);
            return null;
        }

        JsonObject root = json.getAsJsonObject();
        JsonArray arr = root.getAsJsonArray("celestialData");

        if (arr == null && root.has("almagest"))
            arr = root.getAsJsonObject("almagest").getAsJsonArray("celestialData");

        if (arr == null)
        {
            Almagest.LOGGER.error("No celestialData array in " + fileName);
            return null;
        }

        return arr;
    }

    public static JsonObject validateTypeObject(JsonElement element, String fileName)
    {
        if (!element.isJsonObject())
        {
            Almagest.LOGGER.warn("Skipping non-object entry in " + fileName);
            return null;
        }

        JsonObject typeObj = element.getAsJsonObject();
        if (typeObj.size() != 1)
        {
            Almagest.LOGGER.warn("Expected one type key in " + fileName + ", got " + typeObj.keySet());
            return null;
        }

        return typeObj;
    }

    public static void parseConstellation(JsonObject dataObj, String fileName, List<String> namesOut)
    {
        try
        {
            String name = dataObj.has("name") ? dataObj.get("name").getAsString() : "Unknown";
            boolean zodiac = dataObj.has("zodiac") && dataObj.get("zodiac").getAsBoolean();

            List<List<String>> pairs = parseConstellationPairs(dataObj);

            Color color = parseColor(dataObj.getAsJsonArray("color"), true);

            ConstellationData c = CelestialObjectTypes.CONSTELLATION.new ConstellationData(
                constellationIdCounter++, CelestialObjectTypes.CONSTELLATION, name, zodiac, pairs, color
            );

            CONSTELLATION_DATA_BY_ID.put(c.getId(), c);
            namesOut.add(name);
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Failed to parse constellation in " + fileName + ": " + ex.getMessage());
        }
    }

    public static void parseCelestialOrStar(JsonObject dataObj, CelestialObjectTypes type, String fileName, List<String> celestialNames, List<String> starNames)
    {
        try
        {
            List<String> names = parseNames(dataObj);
            Body body = parseBody(dataObj, type);
            Ring ring = parseRing(dataObj, type);
            Orbit orbit = parseOrbit(dataObj, type);
            Atmosphere atmosphere = parseAtmosphere(dataObj, type);
            Life life = parseLife(dataObj, type);
            Season season = parseSeason(dataObj, type);
            Trail trail = parseTrail(dataObj, type);

            double radius = parseDouble(dataObj, "radius", 10);
            double mass = parseMass(dataObj, type, radius);
            double gravity = parseDouble(dataObj, "gravity", PlanetHelpers.calculateSurfaceGravity(mass, radius));
            double temperature = parseDouble(dataObj, "temperature", 0);
            double surfaceHeight = parseDouble(dataObj, "surfaceHeight", DEFAULT_SURFACE_LEVEL);

            Color color = parseColor(dataObj.getAsJsonArray("color"));
            String parentBody = parseString(dataObj, "parentBody", "unknown");

            CelestialData celestial = type.new CelestialData(
                planetIdCounter++, type, names, 
                parseBoolean(dataObj, "hasModel", false),
                parseBoolean(dataObj, "hasRing", false),
                mass, gravity, radius, temperature, surfaceHeight,
                color, parentBody, null, trail, atmosphere, body, ring, life, orbit, season
            );

            StarData starData = parseStarData(dataObj, type, celestial, fileName);

            if (starData != null)
            {
                starData.setCelestialData(celestial);
                if (celestial.isStar())
                {
                    STAR_DATA_BY_ID.put(starData.getId(), starData);
                }
            }
            celestial.setStarData(starData);

            if (!celestial.isStar() || (starData != null && starData.isAParent()))
            {
                CELESTIAL_DATA_BY_ID.put(celestial.getId(), celestial);
            }

            if (!celestial.isStar())
            {
                celestialNames.add(names.isEmpty() ? "No names, Unknown" : names.get(0));
            }
            else
            {
                starNames.add(names.isEmpty() ? "No names, Unknown" : names.get(0));
            }
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Failed to parse celestial entry in " + fileName + ": " + ex.getMessage());
        }
    }

    public static StarData parseStarData(JsonObject dataObj, CelestialObjectTypes type, CelestialData parent, String fileName)
    {
        if (!dataObj.has("star") || !dataObj.get("star").isJsonObject())
            return null;

        try
        {
            JsonObject starObj = dataObj.getAsJsonObject("star");

            List<Double> coords = parseDoubleList(starObj.getAsJsonArray("xyz"), 3, 0.0);

            StarData sd = type.new StarData(
                starIdCounter,
                null,
                coords,
                parseDouble(starObj, "parallax", 0),
                parseDouble(starObj, "distance", 0),
                parseDouble(starObj, "ra", 0),
                parseDouble(starObj, "dec", 0),
                parseDouble(starObj, "appMag", 0),
                parseDouble(starObj, "absMag", 0),
                parseDouble(starObj, "tEff", 0),
                parseDouble(starObj, "metallicity", 0),
                parseDouble(starObj, "luminosity", 0),
                parseDouble(starObj, "gravity", 0),
                parseDouble(starObj, "age", 0),
                parseString(starObj, "spectralType", "Unknown")
            );

            sd.setCelestialData(parent);
            parent.setStarData(sd);

            starIdCounter++;
            return sd;
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Invalid star entry in " + fileName + ": " + ex.getMessage());
            return null;
        }
    }

    public static void finalizeStarSorting()
    {
        if (STAR_DATA_BY_ID.isEmpty()) return;

        int maxStars = Config.COMMON.maxStarsToLoad.get();
        double minMag = Config.COMMON.minApparentMagnitudeToLoad.get();

        List<Long2ObjectMap.Entry<StarData>> sorted = STAR_DATA_BY_ID.long2ObjectEntrySet().stream()
            .filter(e -> e.getValue().getApparentMagnitude() <= minMag)
            .sorted(Comparator.comparingDouble(e -> e.getValue().getApparentMagnitude()))
            .toList();

        STAR_DATA_BY_ID.clear();

        int limit = (maxStars <= -1) ? sorted.size() : Math.min(sorted.size(), maxStars);
        for (int i = 0; i < limit; i++)
        {
            Entry<Long, StarData> e = sorted.get(i);
            STAR_DATA_BY_ID.put(e.getKey().longValue(), e.getValue());
        }
    }

    public static void clearCaches()
    {
        Almagest.LOGGER.debug("Clearing caches...");

        SKYBOX.clear();
        CELESTIAL_OBJECTS_BY_ID.clear();
        STAR_OBJECTS_BY_ID.clear();
        CONSTELLATIONS_BY_ID.clear();
    }

    public static void initObjects(ClientLevel level, Player player, Minecraft instance)
    {
        Almagest.LOGGER.debug("Initializing all celestial objects...");
        player.displayClientMessage(Component.translatable("Loading celestial data! This may take some time.").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);

        long totalStartTime = System.nanoTime();
        final double TO_SECONDS = Nature.S_TO_NS;

        CelestialObjectHandler handler = new CelestialObjectHandler(level, player);

        initializeOrbitParameters();
        initializeCelestialObjects(level, player, handler);
        initializeConstellations(level, player, handler);
        initializeStars(level, player, handler);
        initializeSkybox(level, player, handler);
        initializeParticleEngine(instance);

        logTotalTime(totalStartTime, TO_SECONDS);

        CelestialDataManager.HAS_INITIALIZED_CELESTIAL_OBJECTS = true;
    }

    public static void initializeOrbitParameters()
    {
        if (HAS_INITIALIZED_OTHER_PARAMETERS) return;

        Almagest.LOGGER.debug("Processing planet orbits...");
        long startTime = System.nanoTime();

        CELESTIAL_DATA_BY_ID.values().forEach(data -> {
            Orbit orbit = data.getOrbit();
            double a = orbit.getSemiMajorAxis();
            double e = orbit.getEccentricity();
            double q = orbit.getPeriapsis();
            double P = orbit.getPeriod();
            Optional<CelestialData> parent = data.getParent();

            if (a <= 0.0D && parent.isPresent())
            {
                RandomSource random = RandomSource.create(data.getId());
                double diameter = parent.get().getRadius() * 2.0D;

                a = (1.0D + diameter + random.nextDouble() * 50.0D);

                double newPeriod = PlanetHelpers.getOrbitalPeriod(
                    parent.get().getMass(),
                    data.getMass(),
                    a * Nature.AU_TO_M
                );
                orbit.setPeriod(newPeriod);
                P = newPeriod;
            }

            if (P <= 0.0D)
            {
                P = PlanetHelpers.getOrbitalPeriod(a) * Nature.Y_TO_TICKS;
                orbit.setPeriod(P);
            }

            if (q <= 0.0D)
            {
                q = a * (1.0D - e);
                orbit.setPeriapsis(q);
            }

            orbit.setSegments(PlanetHelpers.getOrbitSegments(a, e));
            orbit.setSemiMajorAxis(a);
            orbit.setPeriapsis(q);

            if (Config.COMMON.printListAllCelestialObjects.get())
            {
                Almagest.LOGGER.debug(
                    "{}: Set semi-major axis={}, periapsis={}, period={}",
                    getObjectName(data), a, q, P
                );
            }
        });

        HAS_INITIALIZED_OTHER_PARAMETERS = true;
        logDuration("Task", startTime);
    }

    public static void initializeCelestialObjects(ClientLevel level, Player player, CelestialObjectHandler handler)
    {
        Almagest.LOGGER.debug("Processing planets and bodies");
        long startTime = System.nanoTime();

        List<CelestialData> dataList = CELESTIAL_DATA_BY_ID.values().stream().toList();

        List<CelestialObject> created =
            dataList.parallelStream()
                .map(data -> {
                    return new CelestialObject(level, player, handler, data);
                })
                .toList();

        List<String> foundPlanets = new ArrayList<>(created.size());

        for (CelestialObject obj : created)
        {
            CELESTIAL_OBJECTS_BY_ID.put(obj.body.getId(), obj);
            foundPlanets.add(obj.name);

            if (isEarth(obj.body.getNames()))
            {
                CelestialObjectHandler.observer = obj.name;
                CelestialObjectHandler.observerObject = obj;
            }

            PlanetHelpers.ORBIT_CACHE.put(obj, PlanetHelpers.buildOrbitCache(obj));
        }

        CELESTIAL_OBJECTS_BY_ID.values().stream().forEach(body -> body.setParentObjects(body.body.getParentObjects()));

        if (Config.COMMON.printListAllCelestialObjects.get())
        {
            AHelpers.printListPerRow(foundPlanets, 10, "Added bodies");
        }

        logDuration("Task", startTime);
    }

    public static void initializeConstellations(ClientLevel level, Player player, CelestialObjectHandler handler)
    {
        Almagest.LOGGER.debug("Processing constellations");
        long startTime = System.nanoTime();

        List<Entry<Long, Constellations>> created =
            CONSTELLATION_DATA_BY_ID.long2ObjectEntrySet()
                .stream()
                .map(entry -> {
                    Constellations constellation = new Constellations(level, player, handler, entry.getValue());
                    CONSTELLATION_OBJECTS.put(entry.getValue(), constellation);
                    return Map.entry(entry.getLongKey(), constellation);
                })
                .toList();

        for (Entry<Long, Constellations> e : created)
        {
            CONSTELLATIONS_BY_ID.put(e.getKey().longValue(), e.getValue());
        }

        logDuration("Task", startTime);
    }

    public static void initializeStars(ClientLevel level, Player player, CelestialObjectHandler handler)
    {
        Almagest.LOGGER.debug("Processing stars");
        int totalEntries = STAR_DATA_BY_ID.size();
        Almagest.LOGGER.debug("Found data entries for '{}' stars.", totalEntries);
        STAR_LOADER.submit(() -> loadStarsAsync(level, player, handler));
    }

    public static void loadStarsAsync(ClientLevel level, Player player, CelestialObjectHandler handler)
    {
        long start = System.nanoTime();
        int total = STAR_DATA_BY_ID.size();

        final int batchSize = Config.COMMON.starAsyncBatchSize.get();
        List<Star> batch = new ArrayList<>(batchSize);

        int processed = 0;

        for (Long2ObjectMap.Entry<StarData> entry : STAR_DATA_BY_ID.long2ObjectEntrySet())
        {
            StarData data = entry.getValue();
            if (data == null) continue;

            Star star = new Star(level, player, handler, data);
            batch.add(star);
            starsCreated++;

            ConstellationData constellation = STAR_DATA_TO_CONSTELLATIONS.get(data);

            if (constellation != null)
            {
                try
                {
                    star.setConstellation(constellation);

                    Constellations renderObj = CONSTELLATION_OBJECTS.get(constellation);
                    if (renderObj != null)
                    {
                        renderObj.addStarToPairs(star);
                    }
                }
                catch (Exception ex)
                {
                    Almagest.LOGGER.error(
                        "Failed to assign constellation '{}' to star '{}': {}",
                        constellation.getName(), star.name, ex.getMessage()
                    );
                }
            }

            if (batch.size() >= batchSize)
            {
                List<Star> toApply = new ArrayList<>(batch);
                batch.clear();

                Minecraft.getInstance().execute(() -> applyStarBatch(toApply, total));
            }

            processed++;
            if (processed % 100000 == 0)
            {
                Almagest.LOGGER.info("Loaded {} / {} stars asynchronously", processed, total);
            }
        }

        if (!batch.isEmpty())
        {
            List<Star> toApply = new ArrayList<>(batch);
            Minecraft.getInstance().execute(() -> applyStarBatch(toApply, total));
        }

        long end = System.nanoTime();
        Almagest.LOGGER.info("Async star loading completed in {} ms", (end - start) / 1000000);
    }

    public static void applyStarBatch(List<Star> stars, int total)
    {
        for (Star star : stars)
        {
            STAR_OBJECTS_BY_ID.put(star.id, star);
            starsRegistered++;
        }
        Almagest.LOGGER.debug("Applied {} / {} stars (total now {})", stars.size(), total, STAR_OBJECTS_BY_ID.size());

        if (!allStarsLoaded && starsRegistered == starsCreated)
        {
            allStarsLoaded = true;
            Almagest.LOGGER.info("Completed loading all stars.");
        }
    }

    public static void initializeSkybox(ClientLevel level, Player player, CelestialObjectHandler handler)
    {
        Almagest.LOGGER.debug("Processing skybox");
        long startTime = System.nanoTime();
        Skybox skybox = new Skybox(level, player, handler);
        SKYBOX.put(0L, skybox);
        logDuration("Task", startTime);
    }

    public static void initializeParticleEngine(Minecraft mc)
    {
        Almagest.LOGGER.debug("Adding objects to particle engine");
        long startTime = System.nanoTime();
        final ParticleEngine particleEngine = mc.particleEngine;
        particleEngine.add(CelestialObjectHandler.getInstance(mc.level, mc.player));
        logDuration("Task", startTime);
    }

    public static String getObjectName(CelestialData data)
    {
        return data.getNames().isEmpty() ? "Unknown " + data.getId() : data.getNames().get(0);
    }

    public static boolean isEarth(List<String> names)
    {
        List<String> normalized = names.stream()
            .filter(Objects::nonNull)
            .map(e -> e.trim().toLowerCase(Locale.ROOT))
            .toList();
        return normalized.stream().anyMatch(name -> name.replace("-", "_").trim().equalsIgnoreCase("earth"));
    }

    public static void logDuration(String operation, long startTime)
    {
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / Nature.S_TO_NS;
        Almagest.LOGGER.debug("{} took {} s", operation, String.format("%.2f", duration));
    }

    public static void logDurationWithDetail(String operation, long startTime, String detail)
    {
        long endTime = System.nanoTime();
        double duration = (endTime - startTime) / Nature.S_TO_NS;
        Almagest.LOGGER.debug("{} took {} s ({})", operation, String.format("%.2f", duration), detail);
    }

    public static void logTotalTime(long totalStartTime, double toSeconds)
    {
        long totalEndTime = System.nanoTime();
        double totalDuration = (totalEndTime - totalStartTime) / toSeconds;
        Almagest.LOGGER.debug("Total initialization time took {} s", String.format("%.2f", totalDuration));
    }

    public static double parseDouble(JsonObject obj, String key, double fallback)
    {
        try
        {
            if (obj.has(key) && obj.get(key).isJsonPrimitive())
            {
                return obj.get(key).getAsDouble();
            }
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Invalid double for key '{}' in object: {}", key, ex.getMessage());
        }
        return fallback;
    }

    public static boolean parseBoolean(JsonObject obj, String key, boolean fallback)
    {
        try
        {
            if (obj.has(key) && obj.get(key).isJsonPrimitive())
            {
                return obj.get(key).getAsBoolean();
            }
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Invalid boolean for key '{}' in object: {}", key, ex.getMessage());
        }
        return fallback;
    }

    public static String parseString(JsonObject obj, String key, String fallback)
    {
        try
        {
            if (obj.has(key) && obj.get(key).isJsonPrimitive())
            {
                return obj.get(key).getAsString().trim();
            }
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Invalid string for key '{}' in object: {}", key, ex.getMessage());
        }
        return fallback;
    }

    public static List<Double> parseDoubleList(JsonArray arr, int expectedSize, double fallbackValue)
    {
        List<Double> list = new ArrayList<>();

        if (arr != null)
        {
            for (JsonElement e : arr)
            {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
                {
                    list.add(e.getAsDouble());
                }
            }
        }

        while (list.size() < expectedSize)
        {
            list.add(fallbackValue);
        }

        return list;
    }

    public static Color parseColor(JsonArray arr)
    {
        return parseColor(arr, false);
    }

    public static Color parseColor(JsonArray arr, boolean isConstellation)
    {
        if (arr == null)
        {
            if (isConstellation)
            {
                return null;
            }
            return ColorUtils.getColor(List.of(1.0, 1.0, 1.0, 1.0));
        }

        List<Double> values = new ArrayList<>();
        for (JsonElement e : arr)
        {
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber())
            {
                values.add(e.getAsDouble());
            }
        }

        if (values.size() == 3)
        {
            values.add(1.0);
        }
        else if (values.size() != 4)
        {
            if (isConstellation)
            {
                return null;
            }
            values = List.of(1.0, 1.0, 1.0, 1.0);
        }

        return ColorUtils.getColor(values);
    }

    public static List<String> parseNames(JsonObject obj)
    {
        List<String> names = new ArrayList<>();

        if (obj.has("names") && obj.get("names").isJsonArray())
        {
            for (JsonElement e : obj.getAsJsonArray("names"))
            {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString())
                {
                    names.add(e.getAsString().trim());
                }
            }
        }

        if (names.isEmpty()) {
            names.add("Unknown");
        }

        return names;
    }

    public static Body parseBody(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("body") || !obj.get("body").isJsonObject()) return null;

        JsonObject b = obj.getAsJsonObject("body");

        return type.new Body(
            parseDouble(b, "albedo", 0.3),
            parseDouble(b, "rotationPeriod", 24) * Nature.H_TO_TICKS,
            parseDouble(b, "precession", 0),
            Math.toRadians(parseDouble(b, "obliquity", 0)),
            Body.BodyTemperature.fromString(parseString(b, "temperature", "UNKNOWN")),
            Body.BodySurface.fromString(parseString(b, "surface", "UNKNOWN")),
            Body.BodySize.fromString(parseString(b, "size", "UNKNOWN")),
            Body.BodyClass.fromString(parseString(b, "class", "UNKNOWN"))
        );
    }

    public static Ring parseRing(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("ring") || !obj.get("ring").isJsonObject()) return null;

        JsonObject b = obj.getAsJsonObject("ring");

        double f = (1.0D / 10000.0D);
        return type.new Ring(
            parseDouble(b, "innerRadius", 0.0D) * f,
            parseDouble(b, "outerRadius", 0.0D) * f,
            parseDouble(b, "thickness", 0.0D) * f
        );
    }

    public static Orbit parseOrbit(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("orbit") || !obj.get("orbit").isJsonObject()) return null;

        JsonObject o = obj.getAsJsonObject("orbit");

        return type.new Orbit(
            parseDouble(o, "period", 0) * Nature.Y_TO_TICKS,
            parseDouble(o, "semiMajorAxis", 0),
            parseDouble(o, "periapsis", 0),
            parseDouble(o, "eccentricity", 0),
            Math.toRadians(parseDouble(o, "inclination", 0)),
            Math.toRadians(parseDouble(o, "ascendingNode", 0)),
            Math.toRadians(parseDouble(o, "argOfPeriapsis", 0)),
            Math.toRadians(parseDouble(o, "longOfPeriapsis", 0)),
            Math.toRadians(parseDouble(o, "meanLongitude", 0)),
            parseDouble(o, "nodalPrecession", 0),
            parseDouble(o, "orbitLineSize", 0),
            (int) parseDouble(o, "segments", 20)
        );
    }

    public static Atmosphere parseAtmosphere(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("atmosphere") || !obj.get("atmosphere").isJsonObject()) return null;

        JsonObject a = obj.getAsJsonObject("atmosphere");

        return type.new Atmosphere(
            parseDouble(a, "height", 0),
            parseDouble(a, "greenhouse", 0),
            parseDouble(a, "pressure", 0),
            parseDouble(a, "density", 0),
            parseDouble(a, "opacity", 0)
        );
    }

    public static Life parseLife(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("life") || !obj.get("life").isJsonObject()) return null;

        JsonObject l = obj.getAsJsonObject("life");

        List<Life.LifeClass> classes = new ArrayList<>();
        List<Life.LifeType> types = new ArrayList<>();
        List<Life.LifeBiome> biomes = new ArrayList<>();

        for (JsonElement e : l.getAsJsonArray("class"))
        {
            classes.add(Life.LifeClass.fromString(e.getAsString()));
        }
        for (JsonElement e : l.getAsJsonArray("type"))
        {
            types.add(Life.LifeType.fromString(e.getAsString()));
        }
        for (JsonElement e : l.getAsJsonArray("biome"))
        {
            biomes.add(Life.LifeBiome.fromString(e.getAsString()));
        }

        return type.new Life(classes, types, biomes);
    }

    public static Season parseSeason(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("season") || !obj.get("season").isJsonObject()) return null;

        JsonObject s = obj.getAsJsonObject("season");

        return type.new Season(
            parseDouble(s, "minObliquity", 0),
            parseDouble(s, "maxObliquity", 0),
            parseDouble(s, "amplitudeObliquity", 0),
            parseDouble(s, "meanObliquity", 0),
            parseDouble(s, "milankovitchCycle", 0),
            parseDouble(s, "seasonOffset", 0),
            parseDouble(s, "winterSolsticeOffset", 0)
        );
    }

    public static Trail parseTrail(JsonObject obj, CelestialObjectTypes type)
    {
        if (!obj.has("trail") || !obj.get("trail").isJsonObject()) return null;

        JsonObject t = obj.getAsJsonObject("trail");

        return type.new Trail(
            parseBoolean(t, "hasTrail", false),
            parseString(t, "name", "Unknown"),
            parseDouble(t, "density", 0),
            (int) parseDouble(t, "population", 10000)
        );
    }

    public static CelestialObjectTypes extractType(JsonObject typeObj, String fileName)
    {
        String typeName = typeObj.keySet().iterator().next();
        Optional<CelestialObjectTypes> typeOpt = CelestialObjectTypes.fromString(typeName);

        if (typeOpt.isEmpty())
        {
            Almagest.LOGGER.error("Invalid celestial type '{}' in {}", typeName, fileName);
            return null;
        }

        return typeOpt.get();
    }

    public static List<List<String>> parseConstellationPairs(JsonObject obj)
    {
        List<List<String>> pairs = new ArrayList<>();

        if (!obj.has("pairs") || !obj.get("pairs").isJsonArray())
        {
            return pairs;
        }

        for (JsonElement elem : obj.getAsJsonArray("pairs"))
        {
            if (!elem.isJsonArray()) continue;

            List<String> pair = new ArrayList<>();
            for (JsonElement starName : elem.getAsJsonArray())
            {
                if (starName.isJsonPrimitive() && starName.getAsJsonPrimitive().isString())
                {
                    pair.add(starName.getAsString().trim());
                }
            }

            if (!pair.isEmpty())
            {
                pairs.add(pair);
            }
        }

        return pairs;
    }

    public static double parseMass(JsonObject obj, CelestialObjectTypes type, double radius)
    {
        try
        {
            if (obj.has("mass") && obj.get("mass").isJsonPrimitive())
            {
                double raw = obj.get("mass").getAsDouble();
                return raw * (type.equals(CelestialObjectTypes.STAR)
                        ? Nature.SOLAR_MASS_KG
                        : Nature.EARTH_MASS_KG);
            }
        }
        catch (Exception ex)
        {
            Almagest.LOGGER.error("Invalid mass value: {}", ex.getMessage());
        }

        return PlanetHelpers.getMass(radius, PlanetHelpers.getDensityFromType(type));
    }

    public static void logParsedLists(List<String> celestialObjectNames, List<String> starObjectNames, List<String> constellationObjectNames)
    {
        if (!Config.COMMON.printListAllCelestialObjects.get())
        {
            return;
        }

        AHelpers.printListPerRow(
            celestialObjectNames,
            20,
            "Parsed CelestialData: Total: " + celestialObjectNames.size()
        );

        AHelpers.printListPerRow(
            starObjectNames,
            20,
            "Parsed StarData: Total: " + starObjectNames.size()
        );

        AHelpers.printListPerRow(
            constellationObjectNames,
            20,
            "Parsed ConstellationData: Total: " + constellationObjectNames.size()
        );
    }
}