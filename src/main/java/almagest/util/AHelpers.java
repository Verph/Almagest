package almagest.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.Nullable;
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

import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.world.ChunkGeneratorExtension;

import almagest.Almagest;
import almagest.client.RenderEventHandler;
import almagest.config.Config;
import almagest.mixin.client.GameRendererAccessor;

@SuppressWarnings("null")
public class AHelpers
{
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final String TEXT_STAR = Almagest.MOD_ID + ".tooltip.looking_at_star";
    public static final String TEXT_CONSTELLATION = Almagest.MOD_ID + ".tooltip.looking_at_constellation";
    public static final KeyMapping RELOAD_STAR_DATA = new KeyMapping(Almagest.MOD_ID + ".key.reload_stellar_data", KeyConflictContext.UNIVERSAL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, Almagest.MOD_NAME);
    public static final double AU_KM_RATIO = 149597870.691D;
    public static final ResourceLocation WHITE = AHelpers.identifier("textures/white.png");
    public static final Vec3 ZERO_VEC = new Vec3(0.0D, 0.0D, 0.0D);

    public static final double GRAVITATIONAL_CONSTANT = 6.67430e-11D;

    public static final int TABLE_SIZE = 10000000;
    public static final double STEP = 2.0 / TABLE_SIZE;
    public static final double[] acosTable = new double[TABLE_SIZE + 1];
    public static final double[] asinTable = new double[TABLE_SIZE + 1];
    public static final double[] atanTable = new double[TABLE_SIZE + 1];

    public static void initTrigTables()
    {
        for (int i = 0; i <= TABLE_SIZE; i++)
        {
            double x = -1.0 + i * STEP;
            acosTable[i] = Math.acos(x);
            double y = -1.0 + i * STEP;
            asinTable[i] = Math.asin(y);
            double z = -1.0 + i * STEP;
            atanTable[i] = Math.atan(z);
        }
    }

    public static double fastAcos(double x)
    {
        if (x < -1.0D || x > 1.0D)
        {
            throw new IllegalArgumentException("Input must be in range [-1, 1]");
        }
        int index = (int) ((x + 1.0) / STEP);
        return acosTable[index];
    }

    public static double fastAsin(double x)
    {
        if (x < -1.0D || x > 1.0D)
        {
            throw new IllegalArgumentException("Input must be in range [-1, 1]");
        }
        int index = (int) ((x + 1.0) / STEP);
        return asinTable[index];
    }

    public static double fastAtan(double x)
    {
        if (x < -1.0D || x > 1.0D)
        {
            if (x > 1.0D)
            {
                return Math.PI / 2 - fastAtan(1.0 / x);
            }
            else if (x < -1.0D)
            {
                return -Math.PI / 2 - fastAtan(1.0 / x);
            }
        }
        int index = (int) ((x + 1.0) / STEP);
        return atanTable[index];
    }

    public static ResourceLocation identifier(String name)
    {
        return resourceLocation(Almagest.MOD_ID, name);
    }

    public static ResourceLocation resourceLocation(String name)
    {
        return new ResourceLocation(name);
    }

    public static ResourceLocation resourceLocation(String domain, String path)
    {
        return new ResourceLocation(domain, path);
    }

    public static ArtifactVersion getVersion()
    {
        return ModList.get().getModContainerById(Almagest.MOD_ID).get().getModInfo().getVersion();
    }

    public static <T extends Block> RegistryObject<T> registerBlock(DeferredRegister<Block> blocks, DeferredRegister<Item> items, String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        final RegistryObject<T> block = blocks.register(actualName, blockSupplier);
        if (blockItemFactory != null)
        {
            items.register(actualName, () -> blockItemFactory.apply(block.get()));
        }
        return block;
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

    public static boolean isWithinFOV(Vec3 direction, Vec3 vectorToCheck, double fov)
    {
        if (Config.COMMON.ignoreFOV.get())
        {
            return true;
        }
        double dotProduct = direction.normalize().dot(vectorToCheck.normalize());
        double halfFOV = Math.toRadians(fov);
        double cosHalfFOV = Math.cos(halfFOV);
        return dotProduct >= cosHalfFOV;
    }

    public static boolean isWithinFOV(Vector3f direction, Vector3f vectorToCheck, double fov)
    {
        if (Config.COMMON.ignoreFOV.get())
        {
            return true;
        }
        double dotProduct = direction.normalize().dot(vectorToCheck.normalize());
        double halfFOV = Math.toRadians(fov);
        double cosHalfFOV = Math.cos(halfFOV);
        return dotProduct >= cosHalfFOV;
    }

    public static double getAngleBetween(Vec3 vector1, Vec3 vector2)
    {
        double dotProduct = vector1.dot(vector2);
        double magnitude1 = vector1.length();
        double magnitude2 = vector2.length();

        double cosineOfAngle = dotProduct / (magnitude1 * magnitude2);
        return Math.toDegrees(Config.COMMON.toggleFastTrigMath.get() ? fastAcos(cosineOfAngle) : Math.acos(cosineOfAngle));
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

    public static double getAngleBetween(Vector3f vector1, Vector3f vector2)
    {
        double dotProduct = dot(vector1, vector2);
        double magnitude1 = vector1.length();
        double magnitude2 = vector2.length();

        double cosineOfAngle = dotProduct / (magnitude1 * magnitude2);
        return Math.toDegrees(Config.COMMON.toggleFastTrigMath.get() ? fastAcos(cosineOfAngle) : Math.acos(cosineOfAngle));
    }

    public static boolean isWithinAngle(Vector3f vector1, Vector3f vector2, double angle)
    {
        if (Config.COMMON.ignoreFOV.get())
        {
            return true;
        }
        return getAngleBetween(vector1, vector2) <= angle;
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

    public static double lerpAlpha(double distance, double diameter)
    {
        double minDistance = diameter;
        double maxDistance = Math.pow(minDistance + 1.0D, 2.0D);

        if (distance <= minDistance)
        {
            return 0.0D;
        }
        else if (distance >= maxDistance)
        {
            return 1.0D;
        }
        return (distance - minDistance) / (maxDistance - minDistance);
    }

    public static double getDayTimeAngle(Level level)
    {
        return Math.toRadians(-level.getTimeOfDay(Minecraft.getInstance().getPartialTick()) * 360.0D);
    }

    /**
     * Converts distance from AU to kilometers.
     *
     * @param distanceAU The distance in astronomical units (AU).
     * @return The distance in kilometers.
     */
    public static double convertAUtoKM(double distanceAU)
    {
        return distanceAU * AU_KM_RATIO;
    }

    /**
     * Converts distance from kilometers to AU.
     *
     * @param distanceKM The distance in kilometers.
     * @return The distance in astronomical units (AU).
     */
    public static double convertKMtoAU(double distanceKM)
    {
        return distanceKM / AU_KM_RATIO;
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
        double result = number % divisor;
        return (result < 0) ? result + divisor : result;
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
    public static double getFOV(Camera camera, float partialTicks)
    {
        return getFOV(camera, partialTicks, Config.COMMON.fovBuffer.get());
    }

    /**
     * Calculates the camera FOV taking modifiers, such as sprinting, into account.
     */
    public static double getFOV(Camera camera, float partialTicks, double buffer)
    {
        Minecraft minecraft = Minecraft.getInstance();
        GameRenderer gameRenderer = minecraft.gameRenderer;
        GameRendererAccessor accessor = (GameRendererAccessor) gameRenderer;
        return (accessor.getFov(camera, partialTicks, true) + buffer);
    }

    public static double getFieldOfViewModifier(Player player)
    {
        if (player instanceof AbstractClientPlayer client)
        {
            return client.getFieldOfViewModifier();
        }
        return 1.0D;
    }

    /**
     * Calculates the apparent size of an object in degrees.
     *
     * @param diameter The actual diameter of the object.
     * @param distance The distance from the observer to the object.
     * @return The apparent size in degrees.
     */
    public static double getApparentSize(double diameter, double distance)
    {
        if (distance <= 0.0D)
        {
            return 180.0D;
        }
        double size = diameter / (2.0D * distance);
        return Math.toDegrees(2.0D * (Config.COMMON.toggleFastTrigMath.get() ? fastAtan(size) : Math.atan(size)));
    }

    /**
     * Calculates the apparent size of an object in degrees.
     *
     * @param object The object in question.
     * @param observer The observer.
     * @param diameter The actual diameter of the object.
     * @param minSize The minimum size of the object in degrees as seen from the observer.
     * @return The apparent size in degrees.
     */
    public static boolean getApparentSize(Vec3 object, Player observer, double diameter, double minSize)
    {
        double distance = observer.getEyePosition().distanceTo(object);
        if (distance < 1D)
        {
            return true;
        }
        double size = diameter / (2.0D * distance);
        return Math.toDegrees(2.0D * (Config.COMMON.toggleFastTrigMath.get() ? fastAtan(size) : Math.atan(size))) >= minSize * RenderEventHandler.fieldOfViewModifier;
    }

    /**
     * Calculates the apparent size of an object in degrees without calling {@link #getFieldOfViewModifier(Player player)}.
     *
     * @param object The object in question.
     * @param observer The observer.
     * @param diameter The actual diameter of the object.
     * @param minSize The minimum size of the object in degrees as seen from the observer.
     * @return The apparent size in degrees.
     */
    public static boolean getApparentSizeClient(Vec3 object, Player observer, double diameter, double minSize)
    {
        double distance = observer.getEyePosition().distanceTo(object);
        if (distance < 1D)
        {
            return true;
        }
        double size = diameter / (2.0D * distance);
        return Math.toDegrees(2.0D * (Config.COMMON.toggleFastTrigMath.get() ? fastAtan(size) : Math.atan(size))) >= minSize * RenderEventHandler.fieldOfViewModifier;
    }

    public static boolean isUsingSpyglass()
    {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.options.getCameraType().isFirstPerson() && RenderEventHandler.isScoping;
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

    public static double minApparentMagnitude(Player player)
    {
        final double minAppMag = Config.COMMON.minApparentMagnitude.get();
        return !RenderEventHandler.isScoping ? minAppMag : Math.min(minAppMag * Math.max(2.0D - RenderEventHandler.fieldOfViewModifier, 1.0D), Config.COMMON.maxApparentMagnitudeSpyglass.get());
    }

    public static float getClearness()
    {
        return 1.0F - getLevel().getRainLevel(0.0F);
    }

    public static double getVisibility()
    {
        ClientLevel level = getLevel();
        double starBrightness = level.getStarBrightness(0.0F);
        return getClearness() * Mth.clamp(starBrightness * 2.0D, 0.5D, 1.0D);
    }

    public static double getDeepskyVisibility()
    {
        double v = (2.0D * Math.max(0.0D, getVisibility() - 0.5D * (1.0D - 0.0D * getSpaceMultiplier())));
        double b = Math.pow(v, 6.0D);
        if (isUsingSpyglass())
        {
            b /= 1.61D;
        }
        return Mth.clamp(b / 2.0D - 0.08D, 0.0D, 1.0D);
    }

    public static boolean isInDome()
    {
        Minecraft client = Minecraft.getInstance();
        double y = client.player.getY();
        if (!(y < 73.0D) && !(y > 84.0D))
        {
            double x = client.player.getX();
            double z = client.player.getZ();
            double dx = Math.abs(x - 20531.5D);
            double dy = Math.abs(y - 74.5D);
            double dz = Math.abs(z - 403.5D);
            double dist = dx * dx + dy * dy + dz * dz;
            return dist < 52.5625D;
        }
        else
        {
            return false;
        }
    }

    public static double getSubSpaceMultiplier()
    {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = getLevel();
        double y = client.player.getY();
        double top = level.getMaxBuildHeight();
        double bottom = level.getMinBuildHeight();
        double height = top - bottom;
        return Mth.clamp((y - bottom) / height, 0.0D, 1.0D);
    }

    public static double getSpaceMultiplier()
    {
        Minecraft client = Minecraft.getInstance();
        if (isInDome())
        {
            return 1.0F;
        }
        else
        {
            double start = Config.COMMON.spaceStartAltitude.get();
            double y = client.player.getY();
            double h = Math.max(0.0D, (y - start) / Config.COMMON.spaceHeight.get());
            return Mth.clamp(Math.sqrt(h), 0.0D, 1.0D);
        }
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

    public static Vector3d toVector3d(Vec3 vec)
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
        return getMatrixPlane(toVector3d(v0), toVector3d(v1), toVector3d(v2));
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
        double clampedValue = Math.max(lowerBound, Math.min(upperBound, skewedNormal));
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

    public static double hemisphereScale(ServerLevelAccessor level)
    {
        return AHelpers.getMaybeChunkExtension(level).settings().temperatureScale() * 0.5D;
    }

    public static double equator(ServerLevelAccessor level)
    {
        return hemisphereScale(level);
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
            blocks90 = AHelpers.hemisphereScale(level.getServer().overworld().getLevel()) * 2.0D;
        }
        return blocks90 + offset;
    }

    public static double angleFromPole(Level level, Minecraft minecraft, double offset)
    {
        return angleFromPole(level, minecraft.player.getZ(), offset);
    }

    public static double angleFromPole(Level level, double playerLatitude, double offset)
    {
        double equator = getEquator(level);
        double blocks90 = getDistanceToPole(level, offset);
        double angle = 90.0D * (playerLatitude - equator) / blocks90;
        return -(modulo(angle + 180.0D, 360.0D) - 180.0D);
    }

    public static double angleFromPrimeMeridian(Level level, double playerLongitude, double offset)
    {
        double primeMeridian = 0.0D;
        double blocks90 = 20000.0D;
        double angle = 90.0D * (playerLongitude - primeMeridian) / blocks90;
        return modulo(angle + 180.0D, 360.0D) - 180.0D;
    }

    public static double calculateAngleBetweenVectors(double x1, double y1, double x2, double y2)
    {
        double magnitudeV1 = Math.sqrt(x1 * x1 + y1 * y1);
        double magnitudeV2 = Math.sqrt(x2 * x2 + y2 * y2);

        double nx1 = x1 / magnitudeV1;
        double ny1 = y1 / magnitudeV1;

        double nx2 = x2 / magnitudeV2;
        double ny2 = y2 / magnitudeV2;

        double dotProduct = nx1 * nx2 + ny1 * ny2;
        double crossProduct = nx1 * ny2 - ny1 * nx2;

        double angleRadians = Math.atan2(crossProduct, dotProduct);

        if (angleRadians < 0.0D)
        {
            angleRadians += 2.0D * Math.PI;
        }
        return angleRadians;
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
}
