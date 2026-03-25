package almagest.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.lwjgl.glfw.GLFW;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;

import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import net.dries007.tfc.world.ChunkGeneratorExtension;

import almagest.Almagest;
import almagest.client.CelestialObjectHandler;
import almagest.config.Config;

import static almagest.Almagest.MOD_ID;

@SuppressWarnings("null")
public class AHelpers
{
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final String TEXT_STAR = MOD_ID + ".tooltip.looking_at_star";
    public static final String TEXT_CONSTELLATION = MOD_ID + ".tooltip.looking_at_constellation";
    public static final KeyMapping RELOAD_STAR_DATA = new KeyMapping(MOD_ID + ".key.reload_stellar_data", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, Almagest.MOD_NAME);
    public static final Vec3 ZERO_VEC = new Vec3(0.0D, 0.0D, 0.0D);

    /**
     * @return A {@link ResourceLocation} with the {@code tfc} namespace.
     */
    public static ResourceLocation identifier(String name)
    {
        return resourceLocation(MOD_ID, name);
    }

    /**
     * @return A {@link ResourceLocation} with the {@code minecraft} namespace.
     */
    public static ResourceLocation identifierMC(String name)
    {
        return resourceLocation("minecraft", name);
    }

    /**
     * @return A {@link ResourceLocation} with an inferred namespace. If present, the namespace will be used, otherwise
     * {@code minecraft} will be used.
     */
    public static ResourceLocation resourceLocation(String name)
    {
        return ResourceLocation.parse(name);
    }

    /**
     * @return A {@link ResourceLocation} with an explicit namespace and path.
     */
    public static ResourceLocation resourceLocation(String domain, String path)
    {
        return ResourceLocation.fromNamespaceAndPath(domain, path);
    }

    public static ArtifactVersion getVersion()
    {
        return ModList.get().getModContainerById(MOD_ID).get().getModInfo().getVersion();
    }

    public static Path getPath(String string)
    {
        Path dir = Paths.get(FMLPaths.MODSDIR.get().toString(), "Almagest-" + SharedConstants.getCurrentVersion().getName() + "-" + AHelpers.getVersion().toString() + ".jar", "assets", string);
        return new File(dir.toString()).toPath();
    }

    public static JsonObject readJsonFile(String filePath)
    {
        try (FileReader reader = new FileReader(filePath))
        {
            JsonElement element = JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject())
            {
                return element.getAsJsonObject();
            }
        }
        catch (IOException | JsonSyntaxException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static double cubicEaseInNorm(double t, double min, double max, double lowerBound, double upperBound, double power)
    {
        double range = max - min;
        double normalizedT = (t - lowerBound) / (upperBound - lowerBound);
        if (normalizedT <= 0) return min;
        if (normalizedT >= 1) return max;
        return min + range * Math.pow(normalizedT, power);
    }

    public static boolean isWithinFov(Vec3 pos)
    {
        if (Config.COMMON.ignoreFOV.get())
            return true;

        Vector3f s = pos.normalize().toVector3f();
        double x = s.dot(CelestialObjectHandler.fovRight);
        double y = s.dot(CelestialObjectHandler.fovUp);
        double z = s.dot(CelestialObjectHandler.fovForward);

        if (z <= 0)
            return false;

        double[] fovs = CelestialObjectHandler.fovLimits;
        double buffer = (Config.COMMON.fovBuffer.get() + 8.0D) + (CelestialObjectHandler.screenAspect - 1.0D) * 12.0D;

        double tanH = Math.tan(Math.toRadians(fovs[0] + buffer));
        double tanV = Math.tan(Math.toRadians(fovs[1] + buffer));

        return Math.abs(x) <= z * tanH && Math.abs(y) <= z * tanV;
    }

    public static boolean isWithinFov2(Vec3 pos)
    {
        if (Config.COMMON.ignoreFOV.get())
            return true;

        double[] fovs = CelestialObjectHandler.fovLimits;
        double buffer = (Config.COMMON.fovBuffer.get() + 8.0D) + (CelestialObjectHandler.screenAspect - 1.0D) * 12.0D;

        Vector3f s = pos.normalize().toVector3f();
        double x = s.dot(CelestialObjectHandler.fovRight);
        double y = s.dot(CelestialObjectHandler.fovUp);
        double z = s.dot(CelestialObjectHandler.fovForward);

        if (z <= 0)
            return false;

        double horizontalAngle = Math.toDegrees(FastMath.atan2Fast(x, z));
        double verticalAngle = Math.toDegrees(FastMath.atan2Fast(y, z));

        return Math.abs(horizontalAngle) <= fovs[0] + buffer && Math.abs(verticalAngle) <= fovs[1] + buffer;
    }

    public static double[] computeFovLimits()
    {
        if (Config.COMMON.ignoreFOV.get())
        {
            return CelestialObjectHandler.fovLimits;
        }

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        double aspect = (double) window.getWidth() / window.getHeight();
        double fovH = CelestialObjectHandler.fov;
        double fovV = Math.toDegrees(2.0 * FastMath.atanFast(Math.tan(Math.toRadians(fovH / 2.0)) / aspect));

        return new double[]{fovH * 0.5D, fovV * 0.5D};
    }

    public static boolean isWithinAngle(Vec3 vector1, Vec3 vector2, double angle)
    {
        if (Config.COMMON.ignoreFOV.get())
        {
            return true;
        }
        return getAngleBetween(vector1, vector2) <= angle;
    }

    public static boolean isWithinAngle(Vec3 vector1, Vec3 vector2, double angle, boolean ignoreFOV)
    {
        if (Config.COMMON.ignoreFOV.get() && ignoreFOV)
        {
            return true;
        }
        return getAngleBetween(vector1, vector2) <= angle;
    }

    public static boolean isWithinAngle(Vector3f vector1, Vector3f vector2, double angle)
    {
        if (Config.COMMON.ignoreFOV.get())
        {
            return true;
        }
        return getAngleBetween(vector1, vector2) <= angle;
    }

    public static double getAngleBetween(Vec3 v1, Vec3 v2)
    {
        double dot = v1.dot(v2);
        double mag = v1.length() * v2.length();

        if (mag == 0.0D)
        {
            return 0.0D;
        }

        return FastMath.RAD_TO_DEG * FastMath.acos(dot / mag);
    }

    public static double getAngleBetween(Vector3f v1, Vector3f v2)
    {
        double dot = v1.dot(v2);
        double mag = v1.length() * v2.length();

        if (mag == 0.0D)
        {
            return 0.0D;
        }

        return FastMath.RAD_TO_DEG * FastMath.acos(dot / mag);
    }

    public static double dot(Vector3f vector1, Vector3f vector2)
    {
       return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z;
    }

    public static Vec3 xRot(Vec3 input, double pitch)
    {
        double f = Math.cos(pitch);
        double f1 = Math.sin(pitch);
        double d0 = input.x;
        double d1 = input.y * f + input.z * f1;
        double d2 = input.z * f - input.y * f1;
        return new Vec3(d0, d1, d2);
    }

    public static Vec3 yRot(Vec3 input, double yaw)
    {
        double f = Math.cos(yaw);
        double f1 = Math.sin(yaw);
        double d0 = input.x * f + input.z * f1;
        double d1 = input.y;
        double d2 = input.z * f - input.x * f1;
        return new Vec3(d0, d1, d2);
    }

    public static Vec3 zRot(Vec3 input, double roll)
    {
        double f = Math.cos(roll);
        double f1 = Math.sin(roll);
        double d0 = input.x * f + input.y * f1;
        double d1 = input.y * f - input.x * f1;
        double d2 = input.z;
        return new Vec3(d0, d1, d2);
    }

    public static double getDayTimeAngle(Level level)
    {
        return Math.toRadians(-level.getTimeOfDay(CelestialObjectHandler.partialTicks) * 360.0D);
    }

    /**
     * Converts distance from AU to kilometers.
     *
     * @param distanceAU The distance in astronomical units (AU).
     * @return The distance in kilometers.
     */
    public static double convertAUtoKM(double distanceAU)
    {
        return distanceAU * Nature.AU_TO_KM;
    }

    /**
     * Converts distance from kilometers to AU.
     *
     * @param distanceKM The distance in kilometers.
     * @return The distance in astronomical units (AU).
     */
    public static double convertKMtoAU(double distanceKM)
    {
        return distanceKM / Nature.AU_TO_KM;
    }

    /**
     * Converts real years to Minecraft ticks.
     *
     * @param years The years.
     * @return The tick equivalent.
     */
    public static double convertYearsToTicks(double years)
    {
        //return years * 60.0D * 60.0D * 24.0D * Config.COMMON.daysPerYear.get() * 20.0D;
        return years * Level.TICKS_PER_DAY * Config.COMMON.daysPerYear.get();
    }

    /**
     * Converts real days to Minecraft ticks.
     *
     * @param days The days.
     * @return The tick equivalent.
     */
    public static double convertDaysToTicks(double days)
    {
        //return days * 60.0D * 60.0D * 24.0D * 20.0D;
        return days * Level.TICKS_PER_DAY;
    }

    /**
     * Converts real hours to Minecraft ticks.
     *
     * @param hours The hours.
     * @return The tick equivalent.
     */
    public static double convertHoursToTicks(double hours)
    {
        //return hours * 60.0D * 60.0D * 20.0D;
        return hours * 1000.0D;
    }

    /**
     * Converts real minutes to Minecraft ticks.
     *
     * @param minutes The minutes.
     * @return The tick equivalent.
     */
    public static double convertMinutesToTicks(double minutes)
    {
        //return minutes * 60.0D * 20.0D;
        return minutes * (1000.0D / 60.0D);
    }

    /**
     * Converts real seconds to Minecraft ticks.
     *
     * @param seconds The seconds.
     * @return The tick equivalent.
     */
    public static double convertSecondsToTicks(double seconds)
    {
        //return seconds * 20.0D;
        return seconds * (1000.0D / 3600.0D);
    }

    public static double modulo(double number, double divisor)
    {
        return ((number % divisor) + divisor) % divisor;
    }

    public static float nearValue(float from, float to, float delta, float adjustmentRate)
    {
        return approach(Mth.lerp(delta, from, to), to, adjustmentRate);
    }

    public static float approach(float value, float target, float adjustmentRate)
    {
        if (value > target)
        {
            return Math.max(value - adjustmentRate, target);
        }
        if (value < target)
        {
            return Math.min(value + adjustmentRate, target);
        }
        return value;
    }

    public static double nearValue(double from, double to, double delta, double adjustmentRate)
    {
        return approach(Mth.lerp(delta, from, to), to, adjustmentRate);
    }

    public static double approach(double value, double target, double adjustmentRate)
    {
        if (value > target)
        {
            return Math.max(value - adjustmentRate, target);
        }
        if (value < target)
        {
            return Math.min(value + adjustmentRate, target);
        }
        return value;
    }

    /**
     * Calculates the camera FOV taking modifiers, such as sprinting, into account.
     */
    public static double getFOV(double fieldOfViewModifier)
    {
        return fieldOfViewModifier * Minecraft.getInstance().options.fov().get();
    }

    public static double getFieldOfViewModifier(Player player)
    {
        if (player instanceof AbstractClientPlayer client)
        {
            return client.getFieldOfViewModifier();
        }
        return Minecraft.getInstance().player.getFieldOfViewModifier();
    }

    public static boolean isUsingSpyglass()
    {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.options.getCameraType().isFirstPerson() && CelestialObjectHandler.isScoping;
    }

    public static ClientLevel getLevel()
    {
        Minecraft client = Minecraft.getInstance();
        return client.level;
    }

    public static long getLevelTime()
    {
        ClientLevel level = getLevel();
        return level == null ? 0L : level.dayTime();
    }

    public static double minApparentMagnitude()
    {
        final double minAppMag = Config.COMMON.minApparentMagnitude.get();
        return !CelestialObjectHandler.isScoping ? minAppMag : Math.min(minAppMag * Math.max(2.0D - CelestialObjectHandler.fovNorm, 1.0D), Config.COMMON.maxApparentMagnitudeSpyglass.get());
    }

    /**
     * Computes the apparent magnitude of the current focus object as seen from the camera
     * position.
     *
     * @return The magnitude.
     */
    public static double getApparentMagnitudeForSpectator(Vec3 playerPos, Vec3 starPos, double absMag)
    {
        return 5.0D * Math.log10(starPos.distanceTo(playerPos)) - 5.0D + absMag;
    }

    /**
     * Converts an apparent magnitude to an absolute magnitude given the distance in parsecs.
     *
     * @param distPc The distance to the star in parsecs.
     * @param appMag The apparent magnitude.
     *
     * @return The absolute magnitude.
     */
    public static double apparentToAbsoluteMagnitude(double distPc, double appMag)
    {
        final double v = 5.0D * Math.log10(distPc <= 0.0D ? 10.0D : distPc);
        return appMag - v + 5.0D;
    }

    /**
     * Converts an absolute magnitude to an apparent magnitude at the given distance in parsecs.
     *
     * @param distPc The distance to the star in parsecs.
     * @param absMag The absolute magnitude.
     *
     * @return The apparent magnitude at the given distance.
     */
    public static double absoluteToApparentMagnitude(double distPc, double absMag)
    {
        return -5.0D + 5.0D * Math.log10(distPc <= 0.0D ? 10.0D : distPc) + absMag;
    }

    /**
     * Computes the pseudo-size of a star from the absolute magnitude.
     *
     * @param absMag The absolute magnitude of the star.
     *
     * @return The pseudo-size of this star, mainly used for rendering purposes.
     * It has no physical meaning and has no relation to the actual physical size of the star.
     */
    public static double absoluteMagnitudeToPseudoSize(double absMag)
    {
        // Pseudo-luminosity. Usually L = L0 * 10^(-0.4*Mbol). We omit M0 and approximate Mbol = M
        double pseudoL = Math.pow(10D, -0.4D * absMag);
        double sizeFactor = Nature.PC_TO_M * Config.COMMON.universeScale.get() * 0.15D;
        return Math.min((Math.pow(pseudoL, 0.5D) * sizeFactor), 1e10D);
    }

    /**
     * Get the spectral type from the effective temperature for main sequence stars.
     * More info: <a href="https://sites.uni.edu/morgans/astro/course/Notes/section2/spectraltemps.html">see here</a>.
     */
    public static String getSpectralType(final double tEff)
    {
        initSpectralTypeTable();

        int n = spectralTypes.size();
        for (int i = n - 1; i >= 0; i--)
        {
            var p = spectralTypes.get(i);
            if (tEff <= p.getFirst())
            {
                return p.getSecond();
            }
        }
        return "Unknown";
    }

    public static ArrayList<Pair<Double, String>> spectralTypes;

    /**
     * Initializes the spectral type table for main sequence stars.
     * More info: <a href="https://sites.uni.edu/morgans/astro/course/Notes/section2/spectraltemps.html">see here</a>.
     */
    public static void initSpectralTypeTable()
    {
        if (spectralTypes == null)
        {
            spectralTypes = new ArrayList<>();
            addToSpTable(54000.0D, "O5");
            addToSpTable(45000.0D, "O6");
            addToSpTable(43300.0D, "O7");
            addToSpTable(40600.0D, "O8");
            addToSpTable(37800.0D, "O9");
            addToSpTable(29200.0D, "B0");
            addToSpTable(23000.0D, "B1");
            addToSpTable(21000.0D, "B2");
            addToSpTable(17600.0D, "B3");
            addToSpTable(15200.0D, "B5");
            addToSpTable(14300.0D, "B6");
            addToSpTable(13500.0D, "B7");
            addToSpTable(12300.0D, "B8");
            addToSpTable(11400.0D, "B9");
            addToSpTable(9600.0D, "A0");
            addToSpTable(9330.0D, "A1");
            addToSpTable(9040.0D, "A2");
            addToSpTable(8750.0D, "A3");
            addToSpTable(8480.0D, "A4");
            addToSpTable(8310.0D, "A5");
            addToSpTable(7920.0D, "A7");
            addToSpTable(7350.0D, "F0");
            addToSpTable(7050.0D, "F2");
            addToSpTable(6850.0D, "F3");
            addToSpTable(6700.0D, "F5");
            addToSpTable(6550.0D, "F6");
            addToSpTable(6400.0D, "F7");
            addToSpTable(6300.0D, "F8");
            addToSpTable(6050.0D, "G0");
            addToSpTable(5930.0D, "G1");
            addToSpTable(5800.0D, "G2");
            addToSpTable(5660.0D, "G5");
            addToSpTable(5440.0D, "G8");
            addToSpTable(5240.0D, "K0");
            addToSpTable(5110.0D, "K1");
            addToSpTable(4960.0D, "K2");
            addToSpTable(4800.0D, "K3");
            addToSpTable(4600.0D, "K4");
            addToSpTable(4400.0D, "K5");
            addToSpTable(4000.0D, "K7");
            addToSpTable(3750.0D, "M0");
            addToSpTable(3700.0D, "M1");
            addToSpTable(3600.0D, "M2");
            addToSpTable(3500.0D, "M3");
            addToSpTable(3400.0D, "M4");
            addToSpTable(3200.0D, "M5");
            addToSpTable(3100.0D, "M6");
            addToSpTable(2900.0D, "M7");
            addToSpTable(2700.0D, "M8");
            addToSpTable(2600.0D, "L0");
            addToSpTable(2200.0D, "L3");
            addToSpTable(1500.0D, "L8");
            addToSpTable(1400.0D, "T2");
            addToSpTable(1000.0D, "T6");
            addToSpTable(800.0D, "T8");
        }
    }

    public static void addToSpTable(double tEff, String spType)
    {
        if (spectralTypes != null)
        {
            spectralTypes.add(new Pair<>(tEff, spType));
        }
    }

    public static int lerpBodyDiameter(double diameter)
    {
        float upperBound = 1000.0F; // Upper bound (Near Ceres diameter in km of 939.4)
        float lowerBound = 20.0F; // Lower bound (Near Phobos diameter in km of 22.2)
        double delta = (diameter - lowerBound) / (upperBound - lowerBound);
        return (int) Math.round(Mth.clamp(delta * 5.0F, 0.0F, 5.0F));
    }


    public static Vec3 calculatePos(double distance, double declination, double rightAscension)
    {
        double k = distance * Math.cos(declination);
        double x = k * Math.cos(rightAscension);
        double y = k * Math.sin(rightAscension);
        double z = distance * Math.sin(declination);
        return new Vec3(x, y, z);
    }

    public static Vec3 getOrbitalPlane(Vec3 v0, Vec3 v1, Vec3 v2)
    {
        Vec3 p0 = v1.subtract(v0);
        Vec3 p1 = v2.subtract(v0);
        return p0.cross(p1).normalize();
    }

    public static Vec3 vec3(Vector3f vec)
    {
        return new Vec3(vec.x(), vec.y(), vec.z());
    }

    public static Vector3d vector3d(Vec3 vec)
    {
        return new Vector3d(vec.x(), vec.y(), vec.z());
    }

    public static Quaternionf toQuaternionf(Quaterniond quaterniond)
    {
        return new Quaternionf(
            (float) quaterniond.x(),
            (float) quaterniond.y(),
            (float) quaterniond.z(),
            (float) quaterniond.w()
        );
    }

    public static Matrix4d getMatrixPlane(Vec3 v0, Vec3 v1, Vec3 v2)
    {
        return getMatrixPlane(vector3d(v0), vector3d(v1), vector3d(v2));
    }

    public static Matrix4d getMatrixPlane(Vector3d v0, Vector3d v1, Vector3d v2)
    {
        Vector3d pos0 = v0.normalize();
        Vector3d pos1 = v1.normalize();
        Vector3d pos2 = v2.normalize();

        Vector3d vX = new Vector3d();
        Vector3d vY = new Vector3d();
        Vector3d vZ = new Vector3d();

        pos1.sub(pos0, vX).normalize();
        pos2.sub(pos0, vY).cross(vX).normalize();
        vX.cross(vY, vZ).normalize();

        Matrix4d rotation = new Matrix4d();
        rotation.set(
            vX.x(), vX.y(), vX.z(), 0.0D,
            vY.x(), vY.y(), vY.z(), 0.0D,
            vZ.x(), vZ.y(), vZ.z(), 0.0D,
            0.0D, 0.0D, 0.0D, 1.0D
        );

        return rotation;
    }

    /**
     * Generates a skewed normal distribution value.
     *
     * @param mean The mean of the distribution.
     * @param stddev The standard deviation of the distribution.
     * @param skewness The skewness parameter. Positive for right skew, negative for left skew.
     * @return A skewed normal distribution value.
     */
    public static double nextSkewedGaussian(RandomSource random, double mean, double stddev, double skewness)
    {
        return nextSkewedGaussian(random, mean, stddev, skewness, 0.0D, 1.0D);
    }

    /**
     * Generates a skewed normal distribution value with bounds.
     *
     * @param mean The mean of the distribution.
     * @param stddev The standard deviation of the distribution.
     * @param skewness The skewness parameter. Positive for right skew, negative for left skew.
     * @param lowerBound The lower bound of the value.
     * @param upperBound The upper bound of the value.
     * @return A skewed normal distribution value within the bounds.
     */
    public static double nextSkewedGaussian(RandomSource random, double mean, double stddev, double skewness, double lowerBound, double upperBound)
    {
        double standardNormal = random.nextGaussian();

        // Apply skewness transformation
        double skewedNormal = mean + stddev * (standardNormal + skewness * (Math.pow(standardNormal, 2.0D) - 1.0D));

        // Normalize and clamp the value within bounds
        double clampedValue = Mth.clamp(skewedNormal, lowerBound, upperBound);
        double normalizedValue = (clampedValue - lowerBound) / (upperBound - lowerBound);

        // Adjust to fit the [0, 1] range and increase likelihood near 0
        return Math.pow(normalizedValue, 1.0D + skewness);
    }

    public static ChunkGeneratorExtension getMaybeChunkExtension(ServerLevelAccessor maybeLevel)
    {
        if (maybeLevel != null && maybeLevel instanceof ServerLevel server)
        {
            return (ChunkGeneratorExtension) server.getChunkSource().getGenerator();
        }
        return null;
    }

    public static double getHemisphereScale(ServerLevelAccessor level)
    {
        return AHelpers.getMaybeChunkExtension(level).settings().temperatureScale() * 0.5D;
    }

    public static double getVerticalHemisphereScale(ServerLevelAccessor level)
    {
        return AHelpers.getMaybeChunkExtension(level).settings().rainfallScale() * 0.5D;
    }

    public static double equator(ServerLevelAccessor level)
    {
        return getHemisphereScale(level);
    }

    public static double getEquator(Level level)
    {
        double equator = Config.COMMON.equatorLatitude.get();
        if (ModList.get().isLoaded("tfc") && level.getServer() != null && level.getServer().overworld().getLevel() != null && level.dimension() == Level.OVERWORLD)
        {
            equator = equator(level.getServer().overworld().getLevel());
        }
        return equator;
    }

    public static double getDistanceToPole(Level level, double offset)
    {
        double blocks90 = Config.COMMON.distanceToPoles.get();
        if (ModList.get().isLoaded("tfc") && level.getServer() != null && level.getServer().overworld().getLevel() != null && level.dimension() == Level.OVERWORLD)
        {
            blocks90 = AHelpers.getHemisphereScale(level.getServer().overworld().getLevel()) * 2.0D;
        }
        return blocks90 + offset;
    }

    public static double getDistanceTo180thMeridian(Level level, double offset)
    {
        double blocks90 = 20000.0D;
        if (ModList.get().isLoaded("tfc") && level.getServer() != null && level.getServer().overworld().getLevel() != null && level.dimension() == Level.OVERWORLD)
        {
            blocks90 = AHelpers.getVerticalHemisphereScale(level.getServer().overworld().getLevel()) * 2.0D;
        }
        return blocks90 + offset;
    }

    public static double angleFromPole(Level level, Minecraft minecraft, double offset)
    {
        return angleFromPole(level, minecraft.player.getZ(), offset);
    }

    /*public static double angleFromPole(Level level, double playerLatitude, double offset)
    {
        double equator = getEquator(level);
        double blocks90 = getDistanceToPole(level, offset);
        double angle = 90.0D * (playerLatitude - equator) / blocks90;
        return -(modulo(angle + 180.0D, 360.0D) - 180.0D);
    }

    public static double angleFromPrimeMeridian(Level level, double playerLongitude, double offset)
    {
        double primeMeridian = 0.0D;
        double blocks90 = getDistanceTo180thMeridian(level, offset);
        double angle = 90.0D * (playerLongitude - primeMeridian) / blocks90;
        return modulo(angle + 180.0D, 360.0D) - 180.0D;
    }*/

    public static double angleFromPole(Level level, double playerLatitude, double offset)
    {
        double equator = getEquator(level);
        double blocks90 = getDistanceToPole(level, offset);
        double angle = 90.0D * (playerLatitude - equator) / blocks90;
        return Mth.clamp(angle, -90.0D, 90.0D);
    }

    public static double angleFromPrimeMeridian(Level level, double playerLongitude, double offset)
    {
        double primeMeridian = 0.0D;
        double blocks180 = getDistanceTo180thMeridian(level, offset);
        double angle = 180.0D * (playerLongitude - primeMeridian) / blocks180;
        return modulo(angle + 180.0D, 360.0D) - 180.0D;
    }

    public static double calculateAngleBetweenVectors(double x1, double y1, double x2, double y2)
    {
        double dot = x1 * x2 + y1 * y2;
        double cross = x1 * y2 - y1 * x2;

        double angle = FastMath.atan2(cross, dot);

        if (angle < 0.0D)
        {
            angle += FastMath.TWO_PI;
        }

        return angle;
    }

    /**
     * Removes specified characters from a given string, because using {@code String.replace()} directly is sloooow!
     *
     * @param str The input string.
     * @param charsToRemove The set of characters to remove.
     * @return The cleaned string.
     */
    public static String removeChars(String str, Set<Character> charsToRemove)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray())
        {
            if (!charsToRemove.contains(c))
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Vec3 getMidpoint(Vec3 vec1, Vec3 vec2)
    {
        double x_m = (vec1.x() + vec2.x()) / 2.0;
        double y_m = (vec1.y() + vec2.y()) / 2.0;
        double z_m = (vec1.z() + vec2.z()) / 2.0;
        return new Vec3(x_m, y_m, z_m);
    }

    public static double interpolate(double input, double t, double target)
    {
        return input + t * (target - input);
    }

    public static Vec3 interpolateVector(Vec3 vec, double t)
    {
        return interpolateVector(vec, t, 1.0D);
    }

    public static Vec3 interpolateVector(Vec3 vec, double t, double target)
    {
        double x = vec.x() + t * (target - vec.x());
        double y = vec.y() + t * (target - vec.y());
        double z = vec.z() + t * (target - vec.z());
        return new Vec3(x, y, z);
    }

    public static Vector4d interpolateVector(Vector4d vec, double t)
    {
        return interpolateVector(vec, t, 1.0D);
    }

    public static Vector4d interpolateVector(Vector4d vec, double t, double target)
    {
        double x = vec.x() + t * (target - vec.x());
        double y = vec.y() + t * (target - vec.y());
        double z = vec.z() + t * (target - vec.z());
        double w = vec.w() + t * (target - vec.w());
        return new Vector4d(x, y, z, w);
    }

    public static String replaceChars(String input)
    {
        return input.replace(" ", "_").replace("(", "").replace(")", "").replace("{", "").replace("}", "").replace("[", "").replace("]", "");
    }

    public static String toTitleCase(String input)
    {
        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;
        for (char c : input.toCharArray())
        {
            if (Character.isSpaceChar(c))
            {
                nextTitleCase = true;
            }
            else if (nextTitleCase)
            {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    /**
     * Checks if two lists of strings have any common elements, ignoring case.
     *
     * @param list1 The first list of strings.
     * @param list2 The second list of strings.
     * @return true if there is at least one common element (case-insensitive), false otherwise.
     */
    public static boolean hasMatchesIgnoreCase(List<String> list1, List<String> list2)
    {
        // Handle null or empty lists
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty())
        {
            return false;
        }

        // Convert list1 to a HashSet with lowercase strings for O(1) lookup
        Set<String> set = new HashSet<>();
        for (String str : list1)
        {
            if (str != null)
            {
                set.add(str.toLowerCase());
            }
        }

        // Check if any element in list2 (converted to lowercase) exists in the set
        for (String str : list2)
        {
            if (str != null && set.contains(str.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Prints a list of elements with a configurable number of elements per row.
     *
     * @param <T>            The type of elements in the list.
     * @param elements       The list of elements to print.
     * @param elementsPerRow The number of elements to display per row.
     * @param label          A label describing the elements (e.g., "Added items").
     */
    public static <T> void printListPerRow(List<T> elements, int elementsPerRow, String label)
    {
        if (elements == null || elements.isEmpty())
        {
            Almagest.LOGGER.info("No " + label.toLowerCase() + " to display.");
            return;
        }

        if (elementsPerRow <= 0)
        {
            Almagest.LOGGER.info("Invalid elements per row; defaulting to single-line output: " + elements);
            return;
        }

        StringBuilder output = new StringBuilder();
        output.append(label).append(":\n");

        for (int i = 0; i < elements.size(); i += elementsPerRow)
        {
            int endIndex = Math.min(i + elementsPerRow, elements.size());
            List<T> rowElements = elements.subList(i, endIndex);

            // Join elements directly with commas, no padding
            String row = rowElements.stream().map(Object::toString).collect(Collectors.joining(", "));

            output.append(row).append("\n");
        }

        Almagest.LOGGER.info(output.toString());
    }

    /**
     * Prints a list of lists of Vec3 elements, with each inner list displayed as a row.
     *
     * @param vectorLists    The list of lists containing Vec3 elements to print.
     * @param label          A label describing the elements (e.g., "Vector groups").
     */
    public static void printVec3ListPerRow(List<List<Vec3>> vectorLists, String label)
    {
        if (vectorLists == null || vectorLists.isEmpty())
        {
            Almagest.LOGGER.info("No " + label.toLowerCase() + " to display.");
            return;
        }

        // Calculate the maximum length of the string representation of any Vec3 element
        int maxLength = vectorLists.parallelStream()
            .flatMap(List::stream)
            .map(Object::toString)
            .mapToInt(String::length)
            .max()
            .orElse(10); // Default to 10 if no elements (though checked above)

        // Add a small buffer (e.g., 2 spaces) for readable spacing
        String formatString = "%-" + (maxLength + 2) + "s";

        StringBuilder output = new StringBuilder();
        output.append(label).append(":\n");

        // Iterate through each inner list (row)
        for (List<Vec3> rowElements : vectorLists)
        {
            if (rowElements == null || rowElements.isEmpty())
            {
                output.append("[Empty row]\n");
                continue;
            }
            // Format each Vec3 element in the row
            String row = rowElements.stream()
                .map(element -> String.format(formatString, element.toString()))
                .collect(Collectors.joining(", "));
            output.append(row).append("\n");
        }

        Almagest.LOGGER.info(output.toString());
    }

    public static Vec3 getCelestialVector(double declination, double rightAscension)
    {
        return getCelestialVector(1.0D, declination, rightAscension);
    }

    public static Vec3 getCelestialVector(double distance, double declination, double rightAscension)
    {
        double z = distance * Math.cos(declination) * Math.sin(rightAscension);
        double y = distance * Math.sin(declination);
        double x = distance * Math.cos(declination) * Math.cos(rightAscension);
        return new Vec3(z, y, x);
    }

    public static String capitalize(String input)
    {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase(Locale.ROOT) + input.substring(1).toLowerCase(Locale.ROOT);
    }
}
