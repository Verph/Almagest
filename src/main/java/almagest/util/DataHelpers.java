package almagest.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import almagest.Almagest;
import almagest.client.blocks.CelestialBodyBlock;
import almagest.config.Config;

import static almagest.Almagest.*;

public class DataHelpers
{
    public static final Gson GSON = new GsonBuilder().setLenient().create();
    public static final String ASSETS_PATH = "assets/" + MOD_ID + "/";
    public static final String CUSTOM_DATA_DIR = FMLPaths.CONFIGDIR.get().resolve(MOD_ID).resolve("custom_data").toString();

    /**
     * Scans the mods directory for the mod's JAR file, loads a specified JSON file
     * from the given directory, and returns it as a Map.Entry with ResourceLocation and JsonElement.
     *
     * @param directory The directory under assets/almagest/ (e.g., "stellar_data/")
     * @param jsonName  Name of the JSON file to read (without .json extension)
     * @return Map.Entry containing the ResourceLocation and JsonElement, or null if not found
     */
    public static ConcurrentMap.Entry<ResourceLocation, JsonElement> scanDirectoryForFile(String directory, String jsonName)
    {
        ConcurrentMap<ResourceLocation, JsonElement> jsonMap = new ConcurrentHashMap<>();
        String jsonFileName = jsonName + ".json";
        String fullPath = ASSETS_PATH + directory + "/" + jsonFileName;

        LOGGER.debug("Attempting to load JSON file: {}", fullPath);

        try
        {
            // Find the mod's JAR file
            String jarPath = accessFirstMatchingJar(FMLPaths.MODSDIR.get().toString(), MOD_ID);

            if (jarPath == null)
            {
                LOGGER.warn("No matching JAR file found for mod ID: {}", MOD_ID);
                return null;
            }

            LOGGER.debug("Found JAR file: {}", jarPath);

            // Read the JSON file from the JAR
            readJsonAsJsonElement(jarPath, directory + "/", jsonFileName, jsonMap);

            if (jsonMap.isEmpty())
            {
                LOGGER.warn("No JSON data loaded for file: {}", fullPath);
                return null;
            }

            // Return the first (and only) entry in the map
            return jsonMap.entrySet().iterator().next();
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load JSON file {}: {}", fullPath, e.getMessage());
            throw new RuntimeException("Error loading JSON file: " + fullPath, e);
        }
    }

    /**
     * Scans the mod's JAR file for all JSON files within a specified directory
     * and returns them as a list of Map.Entry<ResourceLocation, JsonElement>.
     *
     * @param directory The directory under assets/almagest/ (e.g., "stellar_data/")
     * @return List of entries containing ResourceLocation and JsonElement, or empty list if none found
     */
    public static List<ConcurrentMap.Entry<ResourceLocation, JsonElement>> scanDirectoryForAllJson(String directory)
    {
        List<ConcurrentMap.Entry<ResourceLocation, JsonElement>> entries = new ArrayList<>();
        ConcurrentMap<ResourceLocation, JsonElement> jsonMap = new ConcurrentHashMap<>();
        String fullPath = ASSETS_PATH + directory + "/";

        LOGGER.debug("Scanning directory for JSON files: {}", fullPath);

        try
        {
            String jarPath = accessFirstMatchingJar(FMLPaths.MODSDIR.get().toString(), MOD_ID);

            if (jarPath == null)
            {
                LOGGER.warn("No matching JAR file found for mod ID: {}", MOD_ID);
                return entries;
            }

            LOGGER.debug("Found JAR file: {}", jarPath);

            // Scan and read all JSON files in the directory
            readAllJsonFromDirectory(jarPath, directory, jsonMap);

            if (jsonMap.isEmpty())
            {
                LOGGER.warn("No JSON files found in directory: {}", fullPath);
                return entries;
            }

            entries.addAll(jsonMap.entrySet());
            return entries;
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load JSON files from directory {}: {}", fullPath, e.getMessage());
            throw new RuntimeException("Error loading JSON files: " + fullPath, e);
        }
    }

    /**
     * Finds the first JAR file in the specified directory containing the search string
     * in its name and lists its contents for debugging.
     *
     * @param directoryPath Path to the directory to search
     * @param searchString  String to search for in JAR file names
     * @return Path of the found JAR file, or null if none found
     * @throws IOException If there's an error accessing files
     */
    @SuppressWarnings("unused")
    public static String accessFirstMatchingJar(String directoryPath, String searchString) throws IOException
    {
        File directory = new File(directoryPath);

        // Verify directory exists and is a directory
        if (!directory.exists() || !directory.isDirectory())
        {
            LOGGER.error("Invalid directory path: {}", directoryPath);
            throw new IOException("Invalid directory path: " + directoryPath);
        }

        // Get all JAR files matching the search string
        File[] files = directory.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".jar") &&
            name.toLowerCase().contains(searchString.toLowerCase())
        );

        // If no matching JAR files found
        if (files == null || files.length == 0)
        {
            LOGGER.warn("No JAR files found in {} containing '{}'", directoryPath, searchString);
            return null;
        }

        // Take the first matching JAR file
        File jarFile = files[0];
        LOGGER.debug("Selected JAR file: {}", jarFile.getAbsolutePath());

        try (JarFile jar = new JarFile(jarFile))
        {
            // Log contents for debugging
            /*Enumeration<JarEntry> entries = jar.entries();
            LOGGER.debug("Contents of {}:", jarFile.getName());
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                LOGGER.debug("  {}", entry.getName());
            }*/

            return jarFile.getAbsolutePath();
        }
    }

    /**
     * Reads a specified JSON file from the given directory within the JAR file
     * and adds it to a Map as a JsonElement with a ResourceLocation key.
     *
     * @param jarPath       Path to the JAR file
     * @param directory     Directory under assets/almagest/ (e.g., "stellar_data/")
     * @param jsonFileName  Name of the JSON file to read (including .json extension)
     * @param map           Map to store the ResourceLocation and JsonElement
     * @throws IOException If there's an error accessing the JAR or file
     */
    public static void readJsonAsJsonElement(String jarPath, String directory, String jsonFileName, Map<ResourceLocation, JsonElement> map) throws IOException
    {
        File jarFile = new File(jarPath);

        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar"))
        {
            LOGGER.error("Invalid JAR file path: {}", jarPath);
            throw new IOException("Invalid JAR file path: " + jarPath);
        }

        String fullPath = ASSETS_PATH + directory + jsonFileName;
        LOGGER.debug("Reading JSON file: {}", fullPath);

        try (JarFile jar = new JarFile(jarFile))
        {
            JarEntry entry = jar.getJarEntry(fullPath);

            if (entry == null)
            {
                // Try checking for .gz version
                entry = jar.getJarEntry(fullPath + ".gz");
                if (entry == null)
                {
                    LOGGER.error("JSON file not found in JAR: {}", fullPath);
                    throw new IOException("JSON file not found: " + fullPath);
                }
            }

            try (InputStream inputStream = jar.getInputStream(entry);
                InputStream decompressedStream = jsonFileName.endsWith(".gz") ? new GZIPInputStream(inputStream) : inputStream;
                InputStreamReader reader = new InputStreamReader(decompressedStream, StandardCharsets.UTF_8))
            {
                // Use JsonReader with lenient parsing
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(true);
                try
                {
                    JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                    // Create ResourceLocation for the map key (without .json extension)
                    String fileNameWithoutExtension = jsonFileName.replace(".gz", "").replace(".json", "");
                    ResourceLocation resourceLocation = AHelpers.identifier(directory + fileNameWithoutExtension);
                    map.put(resourceLocation, jsonElement);
                    LOGGER.debug("Successfully parsed JSON file: {}", fullPath);
                }
                catch (JsonParseException e)
                {
                    LOGGER.error("Failed to parse JSON file {}: {}", fullPath, e.getMessage());
                    // Log file content for debugging
                    try (InputStream debugStream = jar.getInputStream(entry))
                    {
                        String content = new String(debugStream.readAllBytes(), StandardCharsets.UTF_8);
                        LOGGER.debug("File content: {}", content);
                    }
                    throw new IOException("Invalid JSON in file: " + fullPath, e);
                }
            }
        }
    }

    public static void readAllJsonFromDirectory(String jarPath, String directory, Map<ResourceLocation, JsonElement> map) throws IOException
    {
        File jarFile = new File(jarPath);

        if (!jarFile.exists() || !jarFile.getName().endsWith(".jar"))
        {
            LOGGER.error("Invalid JAR file path: {}", jarPath);
            throw new IOException("Invalid JAR file path: " + jarPath);
        }

        // Normalize directory to ensure it doesn't end with a slash
        String normalizedDirectory = directory;
        if (normalizedDirectory.endsWith("/"))
        {
            normalizedDirectory = normalizedDirectory.substring(0, normalizedDirectory.length() - 1);
        }
        String fullDirPath = ASSETS_PATH + normalizedDirectory + "/";
        LOGGER.debug("Scanning for JSON files in: {} (directory parameter: {})", fullDirPath, directory);
        LOGGER.debug("Max Gaia index from config: {}", Config.COMMON.maxGaiaIndex.get());

        try (JarFile jar = new JarFile(jarFile))
        {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(fullDirPath) && (name.endsWith(".json") || name.endsWith(".json.gz")))
                {
                    // Extract jsonFileName, removing all text before and including the last '/'
                    String jsonFileName = name.substring(fullDirPath.length());
                    if (jsonFileName.contains("/"))
                    {
                        jsonFileName = jsonFileName.substring(jsonFileName.lastIndexOf("/") + 1);
                    }
                    LOGGER.debug("Found JSON file: {} (full path: {})", jsonFileName, name);

                    // Check if the file name contains "gaia" (case-insensitive) and skip it if configured
                    if (Config.COMMON.skipGaia.get() && jsonFileName.toLowerCase(Locale.ROOT).contains("gaia"))
                    {
                        LOGGER.debug("Skipping {} JSON file due to skipGaia config", jsonFileName);
                        continue;
                    }

                    // Handle Gaia index check
                    String lowerCaseFileName = jsonFileName.toLowerCase(Locale.ROOT);
                    if (jsonFileName.toLowerCase(Locale.ROOT).contains("gaia") && lowerCaseFileName.startsWith("gaia_") && (lowerCaseFileName.endsWith(".json") || lowerCaseFileName.endsWith(".json.gz")))
                    {
                        // Extract the part between "gaia_" and ".json" or ".json.gz"
                        String indexPart = lowerCaseFileName.substring("gaia_".length(), lowerCaseFileName.endsWith(".json.gz") ? lowerCaseFileName.length() - ".json.gz".length() : lowerCaseFileName.length() - ".json".length());
                        try
                        {
                            int fileIndex = Integer.parseInt(indexPart);
                            if (fileIndex > Config.COMMON.maxGaiaIndex.get())
                            {
                                LOGGER.debug("Skipping {} (index: {}) as it exceeds maxGaiaIndex: {}", jsonFileName, fileIndex, Config.COMMON.maxGaiaIndex.get());
                                continue;
                            }
                            LOGGER.debug("Processing {} (index: {}) as it is within maxGaiaIndex: {}", jsonFileName, fileIndex, Config.COMMON.maxGaiaIndex.get());
                        }
                        catch (NumberFormatException e)
                        {
                            LOGGER.debug("Processing {} as it has no valid index (e.g., gaia.json)", jsonFileName);
                        }
                    }

                    try (InputStream inputStream = jar.getInputStream(entry);
                         InputStream decompressedStream = name.endsWith(".gz") ? new GZIPInputStream(inputStream) : inputStream;
                         InputStreamReader reader = new InputStreamReader(decompressedStream, StandardCharsets.UTF_8))
                    {
                        JsonReader jsonReader = new JsonReader(reader);
                        jsonReader.setLenient(true);

                        try
                        {
                            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                            String fileNameWithoutExtension = jsonFileName.replace(".gz", "").replace(".json", "");
                            ResourceLocation resourceLocation = AHelpers.identifier(normalizedDirectory + "/" + fileNameWithoutExtension);
                            map.put(resourceLocation, jsonElement);
                            LOGGER.debug("Successfully loaded: {}", name);
                        }
                        catch (JsonParseException e)
                        {
                            LOGGER.warn("Skipping invalid JSON file: {} due to parsing error: {}", name, e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static List<String> getUniqueBodyNamesFromBlockState(JsonElement celestialBlockStates)
    {
        Set<String> bodyNames = new HashSet<>();

        if (celestialBlockStates.isJsonObject() && celestialBlockStates.getAsJsonObject().has("multipart"))
        {
            JsonElement multipart = celestialBlockStates.getAsJsonObject().get("multipart");
            if (multipart.isJsonArray())
            {
                for (JsonElement entry : multipart.getAsJsonArray())
                {
                    if (entry.isJsonObject() && entry.getAsJsonObject().has("when"))
                    {
                        JsonElement when = entry.getAsJsonObject().get("when");
                        String bodyProperty = CelestialBodyBlock.CELESTIAL_BODY.getName();
                        if (when.isJsonObject() && when.getAsJsonObject().has(bodyProperty))
                        {
                            JsonElement body = when.getAsJsonObject().get(bodyProperty);
                            if (body.isJsonPrimitive() && body.getAsJsonPrimitive().isString())
                            {
                                bodyNames.add(body.getAsString());
                            }
                        }
                    }
                }
            }
            else
            {
                Almagest.LOGGER.warn("Invalid 'multipart' format in celestial_bodies.json: not an array");
            }
        }
        else
        {
            Almagest.LOGGER.warn("No 'multipart' field found in celestial_bodies.json");
        }

        if (!bodyNames.isEmpty())
        {
            Almagest.LOGGER.debug("Found the following list of bodies: " + bodyNames.toString());
        }
        else
        {
            Almagest.LOGGER.debug("Didn't find any bodies.");
        }
        return new ArrayList<>(bodyNames);
    }

    public static File ensureCustomDataDir()
    {
        File dir = new File(CUSTOM_DATA_DIR);
        if (!dir.exists())
        {
            boolean created = dir.mkdirs();
            if (created)
            {
                Almagest.LOGGER.info("Created custom data directory: {}", CUSTOM_DATA_DIR);
            }
            else
            {
                Almagest.LOGGER.warn("Failed to create custom data directory: {}", CUSTOM_DATA_DIR);
            }
        }
        return dir;
    }

    public static List<ConcurrentMap.Entry<ResourceLocation, JsonElement>> scanCustomData()
    {
        List<ConcurrentMap.Entry<ResourceLocation, JsonElement>> entries = new ArrayList<>();
        File dir = ensureCustomDataDir();

        File[] files = dir.listFiles((d, name) ->
            name.toLowerCase(Locale.ROOT).endsWith(".json") ||
            name.toLowerCase(Locale.ROOT).endsWith(".json.gz")
        );

        if (files == null || files.length == 0)
        {
            Almagest.LOGGER.info("No custom data JSON files found in {}", CUSTOM_DATA_DIR);
            return entries;
        }

        for (File file : files)
        {
            try (InputStream inputStream = new FileInputStream(file);
                InputStream decompressedStream = file.getName().endsWith(".gz")
                    ? new GZIPInputStream(inputStream)
                    : inputStream;
                InputStreamReader reader = new InputStreamReader(decompressedStream, StandardCharsets.UTF_8))
            {

                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(true);

                JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                String fileNameWithoutExtension = file.getName()
                    .replace(".gz", "")
                    .replace(".json", "");
                ResourceLocation resourceLocation = AHelpers.identifier("custom_data/" + fileNameWithoutExtension);

                ConcurrentMap<ResourceLocation, JsonElement> map = new ConcurrentHashMap<>();
                map.put(resourceLocation, jsonElement);
                entries.add(map.entrySet().iterator().next());

                Almagest.LOGGER.info("Loaded custom data JSON: {}", file.getName());
            }
            catch (Exception e)
            {
                Almagest.LOGGER.error("Failed to parse custom data JSON {}: {}", file.getName(), e.getMessage());
            }
        }
        return entries;
    }
}