package almagest.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import static almagest.Almagest.*;

public class DataHelpers
{
    public static final Gson GSON = new GsonBuilder().setLenient().create();
    public static final String ASSETS_PATH = "assets/" + MOD_ID + "/";

    /**
     * Scans the mods directory for the mod's JAR file, loads a specified JSON file
     * from the given directory, and returns it as a Map.Entry with ResourceLocation and JsonElement.
     *
     * @param directory The directory under assets/almagest/ (e.g., "stellar_data/")
     * @param jsonName  Name of the JSON file to read (without .json extension)
     * @return Map.Entry containing the ResourceLocation and JsonElement, or null if not found
     */
    public static Map.Entry<ResourceLocation, JsonElement> scanDirectoryForFile(String directory, String jsonName)
    {
        Map<ResourceLocation, JsonElement> jsonMap = new HashMap<>();
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
     * Finds the first JAR file in the specified directory containing the search string
     * in its name and lists its contents for debugging.
     *
     * @param directoryPath Path to the directory to search
     * @param searchString  String to search for in JAR file names
     * @return Path of the found JAR file, or null if none found
     * @throws IOException If there's an error accessing files
     */
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
                LOGGER.error("JSON file not found in JAR: {}", fullPath);
                throw new IOException("JSON file not found: " + fullPath);
            }

            try (InputStream inputStream = jar.getInputStream(entry); InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            {
                // Log the first few bytes of the file for debugging
                /*byte[] firstBytes = new byte[10000];
                try (InputStream debugStream = jar.getInputStream(entry)) {
                    int bytesRead = debugStream.read(firstBytes);
                    LOGGER.debug("First {} bytes of {}: {}", bytesRead, fullPath, new String(firstBytes, 0, bytesRead, StandardCharsets.UTF_8));
                }*/

                // Use JsonReader with lenient parsing
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(true);
                try
                {
                    JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                    // Create ResourceLocation for the map key (without .json extension)
                    String fileNameWithoutExtension = jsonFileName.substring(0, jsonFileName.lastIndexOf('.'));
                    ResourceLocation resourceLocation = AHelpers.identifier(directory + fileNameWithoutExtension);
                    map.put(resourceLocation, jsonElement);
                    LOGGER.info("Successfully parsed JSON file: {}", fullPath);
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
}