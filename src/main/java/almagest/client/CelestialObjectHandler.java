package almagest.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.client.data.CelestialDataManager;
import almagest.client.data.CelestialObjectTypes.Atmosphere;
import almagest.client.data.CelestialObjectTypes.CelestialData;
import almagest.client.data.CelestialObjectTypes.StarData;
import almagest.client.effect.LensFlareEffect;
import almagest.client.particle.CelestialObject;
import almagest.client.particle.Constellations;
import almagest.client.particle.Star;
import almagest.client.renderer.AVertexBuffers;
import almagest.config.Config;
import almagest.mixin.client.ParticleAccessor;
import almagest.util.AHelpers;
import almagest.util.Color;
import almagest.util.FastMath;
import almagest.util.PlanetHelpers;

import static almagest.client.data.CelestialDataManager.*;

@SuppressWarnings("unchecked")
public class CelestialObjectHandler extends TextureSheetParticle
{
    private static CelestialObjectHandler INSTANCE;

    public static final int ECLIPTIC_SEGMENTS = 48;
    public static final Vec3[] EQUATOR_UNIT = new Vec3[ECLIPTIC_SEGMENTS];
    public static final Vec3[] GALACTIC_UNIT = new Vec3[ECLIPTIC_SEGMENTS];
    public static final double[] SIN_LAMBDA = new double[ECLIPTIC_SEGMENTS];
    public static final double[] COS_LAMBDA = new double[ECLIPTIC_SEGMENTS];
    public static Vec3 U_NGP, V_NGP;

    public static int adjustChunkIndex = 0;
    public static int refreshChunkIndex = 0;
    public static long lastAdjustTime = 0;
    public static final long ADJUST_INTERVAL_MS = 1000;

    public final ClientLevel level;
    public final Player player;

    public static final Minecraft mc = Minecraft.getInstance();
    public static final GameRenderer gameRenderer = mc.gameRenderer;

    public static boolean firstTick = true;
    public static String observer;
    public static CelestialObject observerObject;
    public static CelestialObject systemCenterObject;
    public static CelestialData observerBody;
    public static Matrix4f skyboxRotationMatrix = new Matrix4f();
    public static Camera camera;
    public static Matrix4f projectionMatrix = new Matrix4f();
    public static Matrix4f modelViewMatrix = new Matrix4f();
    public static double lineScale;

    public static float partialTicks;
    public static float starBrightness;
    public static float rainLevel;
    public static double[] fovLimits = new double[]{0.0D, 0.0D};
    public static double screenAspect;
    public static double fieldOfViewModifier;
    public static double fovOld;
    public static double fov;
    public static double fovLim;
    public static double fovNorm;
    public static double fovNormInv;
    public static Vector3f fovForward = new Vector3f();
    public static Vector3f fovRight = new Vector3f();
    public static Vector3f fovUp = new Vector3f();

    public static long dayTime;
    public static double dayRotationDeg;
    public static double dayRotationRad;
    public static double latitudeDeg;
    public static double longitudeDeg;
    public static double latitude;
    public static double longitude;
    public static double atmosphereFactor;
    public static double minApparentMagnitude;
    public static boolean isScoping;
    public static Vec3 lookAngle = Vec3.ZERO;
    public static Vec3 playerPos = Vec3.ZERO;
    public static Quaternionf camRotation = new Quaternionf();
    public static Vector3f camRight = new Vector3f();
    public static Vector3f camUp = new Vector3f();
    public static Vector3f camForward = new Vector3f();

    public static double skyYaw;
    public static double skyPitch;
    public static double skyYawSinY;
    public static double skyYawCosY;
    public static double skyPitchSinX;
    public static double skyPitchCosX;

    public static int renderTickCounter;

    public static List<Vec3> eclipticPlane = new ArrayList<>();
    public static List<Vec3> celestialEquator = new ArrayList<>();
    public static List<Vec3> galacticPlane = new ArrayList<>();

    public static boolean hasObserverChanged = true;
    public static boolean changedFOV = true;
    public static Constellations lastFocusedConstellation;

    public static final Long2ObjectMap<CelestialObject> STAR_OBJECTS_MAP = new Long2ObjectOpenHashMap<>();

    public static final int MIN_MAG_BUCKET = -1;
    public static final int NUM_MAG_BUCKETS = 30;
    public static final int NUM_BUCKETS = (NUM_MAG_BUCKETS - MIN_MAG_BUCKET + 1);
    public static final ObjectArrayList<Star>[][] STAR_BUCKETS_ALL_SUB = new ObjectArrayList[NUM_BUCKETS][];
    public static final ObjectOpenHashSet<Star>[][] STAR_BUCKETS_DRAW_SUB = new ObjectOpenHashSet[NUM_BUCKETS][];
    public static final boolean[][] DIRTY_SUB_BUCKETS = new boolean[NUM_BUCKETS][];
    public static final int[] SUB_BUCKET_CURSOR = new int[NUM_BUCKETS];

    static
    {
        for (int b = 0; b < NUM_BUCKETS; b++)
        {
            STAR_BUCKETS_ALL_SUB[b] = new ObjectArrayList[1];
            STAR_BUCKETS_DRAW_SUB[b] = new ObjectOpenHashSet[1];
            DIRTY_SUB_BUCKETS[b] = new boolean[1];
            STAR_BUCKETS_ALL_SUB[b][0] = new ObjectArrayList<>();
            STAR_BUCKETS_DRAW_SUB[b][0] = new ObjectOpenHashSet<>();
        }
    }

    public static boolean starConfigValuesChanged = true;
    public static double lastScale = Double.NaN;
    public static double lastGamma = Double.NaN;
    public static double lastSlope = Double.NaN;
    public static double lastExposure = Double.NaN;
    public static double starColorUpperBound;
    public static double starColorLowerBound;

    public static List<Star> pendingInitStars = new ArrayList<>();
    public static int pendingInitIndex = 0;
    public static boolean initializingStars = false;
    public static long startTimeStars = 0;
    public static boolean initializedCelestialStars = false;

    public CelestialObjectHandler(ClientLevel level, Player player)
    {
        super(level, 0.0D, 0.0D, 0.0D);
        this.level = level;
        this.player = player;

        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.setBoundingBox(AABB.INFINITE);
        initEclipticPrecalc();
    }

    /**
     * Gets the singleton instance, creating it if necessary
     */
    public static CelestialObjectHandler getInstance(ClientLevel level, Player player)
    {
        if (INSTANCE == null || INSTANCE.level != level || INSTANCE.player != player)
        {
            INSTANCE = new CelestialObjectHandler(level, player);
        }
        return INSTANCE;
    }

    /**
     * Gets the singleton instance (use only after initialization)
     */
    public static CelestialObjectHandler getInstance()
    {
        if (INSTANCE == null)
        {
            throw new IllegalStateException("CelestialObjectHandler not initialized! Call getInstance(level, player) first.");
        }
        return INSTANCE;
    }

    @Override
    public void tick()
    {
        if (level == null) return;

        updateCommonParam();

        if (observerObject == null || systemCenterObject == null) return;

        observerObject.elapsedTime = PlanetHelpers.getElapsedTime(dayTime, observerObject.period);
        observerObject.timeOfDay = PlanetHelpers.getTimeOfDay(dayTime, observerObject.rotationPeriod, Config.COMMON.enableManualTimeOfDay.get());
        observerObject.season = PlanetHelpers.getSeason(observerObject, dayTime, Config.COMMON.enableManualSeason.get());

        observerObject.posAU = PlanetHelpers.getPosWithParents(
            observerObject,
            observerObject.elapsedTime
        );

        final Vec3 playerOffsetMC =
            Config.COMMON.togglePlayerOffset.get()
                ? new Vec3(0.0D, Config.COMMON.playerYOffset.get(), 0.0D)
                    .subtract(CelestialObjectHandler.playerPos.scale(10.0D))
                : Vec3.ZERO;

        CELESTIAL_OBJECTS_BY_ID.values().parallelStream().forEach(obj -> {
            obj.elapsedTime = PlanetHelpers.getElapsedTime(dayTime, obj.period);
            obj.timeOfDay = PlanetHelpers.getTimeOfDay(dayTime, observerObject.rotationPeriod);
            obj.nodalPrecession = PlanetHelpers.getElapsedNodalPrecession(
                dayTime, obj.period, obj.body.getOrbit().getNodalPrecession()
            );

            Vec3 heliocentricAU = PlanetHelpers.getPosWithParents(obj, obj.elapsedTime);
            obj.posAU = heliocentricAU;

            Vec3 geocentricAU = PlanetHelpers.toGeocentric(heliocentricAU, observerObject.posAU);
            Vec3 skyAU = PlanetHelpers.applyObserverFrame(geocentricAU, observerObject);
            Vec3 skyMC = PlanetHelpers.scaleAUToMC(skyAU);

            obj.pos = skyMC.add(playerOffsetMC);

            if (obj.hasTrail && Config.COMMON.renderMeteorShowers.get())
            {
                obj.meteoroids.parallelStream().forEach(m -> {
                    m.elapsedTime = PlanetHelpers.getElapsedTime(dayTime, m.period);

                    Vec3 mHelioAU = PlanetHelpers.getPosWithParents(m.mainBody, m.elapsedTime);
                    Vec3 mGeoAU = PlanetHelpers.toGeocentric(mHelioAU, observerObject.posAU);
                    Vec3 mSkyAU = PlanetHelpers.applyObserverFrame(mGeoAU, observerObject);

                    m.pos = PlanetHelpers.scaleAUToMC(mSkyAU);
                    m.tick();
                });
            }

            if (obj.shouldRenderOrbits)
            {
                obj.orbitPositions = PlanetHelpers.updateOrbitPositions(
                    obj,
                    playerOffsetMC,
                    obj.semiMajorAxis,
                    obj.periapsis,
                    obj.period,
                    obj.elapsedTime,
                    observerObject
                );
            }

            obj.tick();
        });

        Vec3 lookDir = CelestialObjectHandler.lookAngle.normalize();

        CelestialObject bestObject = CELESTIAL_OBJECTS_BY_ID.values().stream()
            .filter(obj -> obj.displayNames() && (!obj.isObserver || Config.COMMON.togglePlayerOffset.get()))
            .filter(obj -> AHelpers.isWithinAngle(lookDir, obj.pos, CelestialObjectHandler.fovLim))
            .filter(obj -> AHelpers.isWithinAngle(
                    lookDir,
                    obj.pos,
                    Config.COMMON.planetDisplayNameAngleThreshold.get() * CelestialObjectHandler.fovNorm,
                    false))
            .max((a, b) -> {
                double da = a.pos.length();
                double db = b.pos.length();
                if (da <= 0.0D && db <= 0.0D) return 0;
                if (da <= 0.0D) return -1;
                if (db <= 0.0D) return 1;

                double angA = a.diameter / da;
                double angB = b.diameter / db;
                return Double.compare(angA, angB);
            })
            .orElse(null);

        if (bestObject != null && player != null && CelestialObjectHandler.isScoping)
        {
            player.displayClientMessage(
                Component.translatable(bestObject.name)
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC),
                true
            );
        }

        if (CelestialDataManager.allStarsLoaded && !initializingStars)
        {
            pendingInitStars = new ArrayList<>(STAR_OBJECTS_BY_ID.values());
            pendingInitIndex = 0;
            initializingStars = true;
            CelestialDataManager.allStarsLoaded = false;

            Almagest.LOGGER.info("Beginning batched star initialization: {} stars", pendingInitStars.size());
        }

        initStarObjects();
        updateConstellations();

        fovOld = fov;
        firstTick = false;
    }

    public void initStarObjects()
    {
        if (!initializedCelestialStars)
        {
            long i = 0;
            for (CelestialObject obj : CELESTIAL_OBJECTS_BY_ID.values())
            {
                if (obj.isStar)
                {
                    STAR_OBJECTS_MAP.put(i, obj);
                    i++;
                }
            }
            initializedCelestialStars = true;
        }

        if (initializingStars)
        {
            if (startTimeStars == 0)
            {
                Almagest.LOGGER.debug("Initializing stars");
                startTimeStars = System.nanoTime();
            }

            int batchSize = Config.COMMON.starAsyncBatchSize.get();
            int end = Math.min(pendingInitIndex + batchSize, pendingInitStars.size());

            Almagest.LOGGER.info(
                "Initializing stars: batch {} -> {} of {} (batch size {})",
                pendingInitIndex, end, pendingInitStars.size(), batchSize
            );

            for (int i = pendingInitIndex; i < end; i++)
            {
                Star star = pendingInitStars.get(i);
                if (star == null)
                    continue;

                int bucket = getMagnitudeBucket(star, star.apparentMagnitude);

                addStarToSubBucket(bucket, star);

                star.tick();
                star.update();
            }

            pendingInitIndex = end;

            if (pendingInitIndex >= pendingInitStars.size())
            {
                initializingStars = false;
                pendingInitStars.clear();
                logDuration("Task", startTimeStars);

                Almagest.LOGGER.info("Finished batched star initialization.");
            }
        }
    }

    public void updateObserverObjects()
    {
        String override = Config.COMMON.manualObserverBody.get();
        String normalizedOverride = override == null ? "" : override.trim().toLowerCase(Locale.ROOT);

        String fallbackObserver = level.dimension().equals(Level.OVERWORLD)
            ? "earth"
            : level.dimension().location().getPath().toLowerCase(Locale.ROOT);

        String requestedObserver = normalizedOverride.isEmpty()
            ? fallbackObserver
            : normalizedOverride;

        Optional<CelestialObject> requestedObject = getCelestialObject(requestedObserver);

        CelestialObject finalObserverObject = requestedObject.orElseGet(() ->
            getCelestialObject(fallbackObserver).orElse(null)
        );

        if (finalObserverObject == null)
        {
            return;
        }

        if (CelestialObjectHandler.observerObject != finalObserverObject)
        {
            CelestialObjectHandler.observerObject = finalObserverObject;
            CelestialObjectHandler.observer = finalObserverObject.body.getNames().get(0).toLowerCase(Locale.ROOT);

            CelestialObject systemCenter =
                finalObserverObject.parentObjects.isEmpty()
                    ? finalObserverObject
                    : finalObserverObject.parentObjects.get(finalObserverObject.parentObjects.size() - 1);

            systemCenter.isSystemCenterObject = true;
            CelestialObjectHandler.systemCenterObject = systemCenter;
            CelestialObjectHandler.observerBody = finalObserverObject.body;

            hasObserverChanged = true;
        }
        else
        {
            hasObserverChanged = false;
        }
    }

    public void updateCommonParam()
    {
        final long manualTime = Config.COMMON.manualTimeControl.get();

        partialTicks = DeltaTracker.ONE.getGameTimeDeltaPartialTick(false);

        starBrightness = level.getStarBrightness(partialTicks) * 2.0F;
        rainLevel = level.getRainLevel(partialTicks);
        dayTime = manualTime > -1 ? manualTime : level.getDayTime();
        dayRotationDeg = 360.0D - level.getTimeOfDay(partialTicks) * 360.0D;
        dayRotationRad = Math.toRadians(dayRotationDeg);
        playerPos = player.position();

        isScoping = player.isScoping();
        minApparentMagnitude = AHelpers.minApparentMagnitude();

        updateCoordinates();
        updateFOV();
        updateSkyboxParam();
        updateObserverObjects();
        updateEclipticPositions();
        updateAtmosphereFactor();
    }

    public void updateCoordinates()
    {
        double northPoleZ = -10000.0;
        double southPoleZ =  30000.0;

        double equatorZ  = (northPoleZ + southPoleZ) * 0.5;
        double blocks90  = (southPoleZ - equatorZ);
        double blocks180 = 20000.0;

        double latitudeDeg = 90.0 * (equatorZ - playerPos.z()) / blocks90;
        latitudeDeg = Mth.clamp(latitudeDeg, -90.0, 90.0) - 90.0D;

        double longitudeDeg = 180.0 * (playerPos.x()) / blocks180;
        longitudeDeg = ((longitudeDeg + 180.0) % 360.0) - 180.0;

        double latitude  = Math.toRadians(latitudeDeg);
        double longitude = Math.toRadians(longitudeDeg);

        double rotDay = observerObject.timeOfDay;
        double rotTilt = observerObject.obliquity;
        double rotSeason = observerObject.season;
        double rotIncl = observerObject.inclination;

        skyYaw = rotDay + longitude;
        skyPitch = rotIncl + rotTilt + rotSeason + latitude;

        skyYawSinY = Math.sin(skyYaw);
        skyYawCosY = Math.cos(skyYaw);
        skyPitchSinX = Math.sin(skyPitch);
        skyPitchCosX = Math.cos(skyPitch);
    }

    public void updateFOV()
    {
        if (camera != null)
        {
            fovForward = camera.getLookVector().normalize();
            Vector3f worldUp = new Vector3f(0, 1, 0);
            if (Math.abs(fovForward.dot(worldUp)) > 0.999f)
            {
                float yaw = camera.getYRot() * ((float) Math.PI / 180f);
                worldUp = new Vector3f(-Mth.sin(yaw), 0, Mth.cos(yaw));
            }
            fovRight = new Vector3f(worldUp).cross(fovForward).normalize();
            fovUp = new Vector3f(fovForward).cross(fovRight).normalize();
        }

        fovLimits = AHelpers.computeFovLimits();
        fieldOfViewModifier = AHelpers.getFieldOfViewModifier(player);
        fov = AHelpers.getFOV(fieldOfViewModifier);
        double fovH = fov;
        screenAspect = (double) mc.getWindow().getWidth() / mc.getWindow().getHeight();
        double fovV = Math.toDegrees(2.0 * FastMath.atan(Math.tan(Math.toRadians(fovH / 2.0)) / screenAspect));
        double fovDiag = Math.toDegrees(
            2.0 * FastMath.atan(Math.sqrt(
                Math.pow(Math.tan(Math.toRadians(fovH / 2.0)), 2) +
                Math.pow(Math.tan(Math.toRadians(fovV / 2.0)), 2)
            ))
        );
        fovLim = Math.max(fovDiag * 0.5, 3.0) + Config.COMMON.fovBuffer.get() + 15.0D;
        fovNorm = fov / 70.0D;
        fovNormInv = 70.0D / fov;
    }

    public void updateSkyboxParam()
    {
        double halfPi = Math.PI * 0.5D;
        Vec3 starX = PlanetHelpers.rotYX(AHelpers.getCelestialVector(0.0D, 0.0D), skyYawSinY, skyYawCosY, skyPitchSinX, skyPitchCosX);
        Vec3 starY = PlanetHelpers.rotYX(AHelpers.getCelestialVector(halfPi, 0.0D), skyYawSinY, skyYawCosY, skyPitchSinX, skyPitchCosX);
        Vec3 starZ = PlanetHelpers.rotYX(AHelpers.getCelestialVector(0.0D, halfPi), skyYawSinY, skyYawCosY, skyPitchSinX, skyPitchCosX);

        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.set(
            (float) starX.x(), (float) starX.y(), (float) starX.z(), 0.0F,
            (float) starY.x(), (float) starY.y(), (float) starY.z(), 0.0F,
            (float) starZ.x(), (float) starZ.y(), (float) starZ.z(), 0.0F,
            0.0F, 0.0F, 0.0F, 1.0F
        );
        skyboxRotationMatrix = rotationMatrix;
    }

    public static void updateConstellations()
    {
        if (!Config.COMMON.drawConstellations.get()) return;

        Collection<Constellations> constellations = CONSTELLATIONS_BY_ID.values();
        double bufferAngle = Config.COMMON.constellationDisplayNameAngleThreshold.get() * CelestialObjectHandler.fov;
        Vec3 lookDir = CelestialObjectHandler.lookAngle.normalize();

        Constellations candidate = null;
        double bestAngle = Double.MAX_VALUE;

        for (Constellations c : constellations)
        {
            Vec3 centroid = c.computeCentroid();
            if (centroid.lengthSqr() == 0.0) continue;

            double maxRadius = c.computeMaxAngularRadius(centroid);
            double angleToCentroid = FastMath.acos(centroid.normalize().dot(lookDir));

            boolean insideShape = angleToCentroid <= maxRadius;
            boolean withinBuffer = angleToCentroid <= maxRadius + bufferAngle;

            if (insideShape || withinBuffer)
            {
                if (angleToCentroid < bestAngle)
                {
                    bestAngle = angleToCentroid;
                    candidate = c;
                }
            }
        }

        if (candidate != null)
        {
            lastFocusedConstellation = candidate;
        }

        for (Constellations c : constellations)
        {
            c.tick(lastFocusedConstellation == c);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera cam, float partialTicks)
    {
        camera = cam;
        camRotation = cam.rotation();
        lookAngle = AHelpers.vec3(cam.getLookVector());
        Vector3f right = new Vector3f(1, 0, 0).rotate(camRotation);
        Vector3f up = new Vector3f(0, 1, 0).rotate(camRotation);
        Vector3f forward = new Vector3f(0, 0, 1).rotate(camRotation);
        camRight = right;
        camUp = up;
        camForward = forward;
        lineScale = Math.tan(Math.toRadians(fov / 2.0)) / Math.tan(Math.toRadians(70.0D / 2.0));

        if (observerObject == null || firstTick) return;

        this.offsetHandler();
        this.tickStars();

        double scale = Config.COMMON.scale.get();
        double gamma = Config.COMMON.gamma.get();
        double slope = Config.COMMON.slope.get();
        double exposure = Config.COMMON.exposure.get();
        boolean changed = scale != lastScale || gamma != lastGamma || slope != lastSlope || exposure != lastExposure;

        if (changed)
        {
            starConfigValuesChanged = true;
            lastScale = scale;
            lastGamma = gamma;
            lastSlope = slope;
            lastExposure = exposure;
        }

        final Tesselator tess = Tesselator.getInstance();

        if (Config.COMMON.renderSkybox.get())
        {
            SKYBOX.values().stream().forEach(skybox -> {
                final BufferBuilder skyboxBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

                RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
                RenderSystem.enableBlend();
                RenderHelpers.translucentTransparency();
                RenderSystem.depthMask(false);
                RenderSystem.enableDepthTest();
                RenderSystem.disableCull();

                skybox.render(skyboxBuilder, camera, partialTicks);

                MeshData mesh = skyboxBuilder.build();
                if (mesh != null)
                {
                    BufferUploader.drawWithShader(mesh);
                }
            });
        }

        if (Config.COMMON.renderPlanets.get() || Config.COMMON.renderMoons.get() || Config.COMMON.renderMinorPlanets.get() || Config.COMMON.renderComets.get())
        {
            final BufferBuilder celestialObjectBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

            RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
            RenderHelpers.noTransparency();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();

            CELESTIAL_OBJECTS_BY_ID.values().stream().forEach(celestialObject -> celestialObject.renderBody(celestialObjectBuilder, camera, partialTicks));

            MeshData mesh = celestialObjectBuilder.build();
            if (mesh != null)
            {
                BufferUploader.drawWithShader(mesh);
            }

            CELESTIAL_OBJECTS_BY_ID.values().stream().forEach(celestialObject -> {if (celestialObject.body.hasRing()) RenderHelpers.drawRing(tess, celestialObject);});

            STAR_OBJECTS_MAP.values().stream().forEach(celestialObject -> {if (celestialObject.isStar) {LensFlareEffect.renderLensFlare(camera, partialTicks, celestialObject);}});
        }

        if (Config.COMMON.renderStars.get())
        {
            AVertexBuffers.rebuildStarBuckets(tess);
            AVertexBuffers.renderStarBuckets();
        }

        if (Config.COMMON.drawConstellations.get())
        {
            final BufferBuilder constellationBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderHelpers.translucentTransparency();
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, RenderHelpers.WHITE);

            CONSTELLATIONS_BY_ID.values().stream().forEach(constellation -> constellation.render(constellationBuilder, camera, partialTicks));

            MeshData mesh = constellationBuilder.build();
            if (mesh != null)
            {
                BufferUploader.drawWithShader(mesh);
            }
        }

        if (Config.COMMON.showPlanetOrbits.get() || Config.COMMON.showMoonOrbits.get() || Config.COMMON.showMinorPlanetOrbits.get() || Config.COMMON.showCometOrbits.get() || Config.COMMON.showMeteorShowerOrbits.get())
        {
            final BufferBuilder celestialObjectOrbitBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderHelpers.translucentTransparency();
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, RenderHelpers.WHITE);

            CELESTIAL_OBJECTS_BY_ID.values().stream().forEach(celestialObject -> celestialObject.renderOrbit(celestialObjectOrbitBuilder, camera, partialTicks));

            MeshData mesh = celestialObjectOrbitBuilder.build();
            if (mesh != null)
            {
                BufferUploader.drawWithShader(mesh);
            }
        }

        float alpha = (float) StarData.getStarAlpha(0.0D);
        if (alpha > 0.0F)
        {
            if (Config.COMMON.drawEclipticPlane.get())
            {
                final BufferBuilder eclipticPlaneBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                RenderHelpers.drawLines(eclipticPlaneBuilder, camera, Config.COMMON.constellationLineWidth.get(), eclipticPlane, new Color(0.145F, 0.878F, 1.0F, alpha));

                MeshData mesh = eclipticPlaneBuilder.build();
                if (mesh != null)
                {
                    BufferUploader.drawWithShader(mesh);
                }
            }
            if (Config.COMMON.drawCelestialEquator.get())
            {
                final BufferBuilder celestialEquatorBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                RenderHelpers.drawLines(celestialEquatorBuilder, camera, Config.COMMON.constellationLineWidth.get(), celestialEquator, new Color(1.0F, 0.459F, 0.145F, alpha));

                MeshData mesh = celestialEquatorBuilder.build();
                if (mesh != null)
                {
                    BufferUploader.drawWithShader(mesh);
                }
            }
            if (Config.COMMON.drawGalacticPlane.get())
            {
                final BufferBuilder galacticPlaneBuilder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                RenderHelpers.drawLines(galacticPlaneBuilder, camera, Config.COMMON.constellationLineWidth.get(), galacticPlane, new Color(1.0F, 0.898F, 0.145F, alpha));

                MeshData mesh = galacticPlaneBuilder.build();
                if (mesh != null)
                {
                    BufferUploader.drawWithShader(mesh);
                }
            }
        }
    }

    public void offsetHandler()
    {
        double offsetDistance = 0.5D;
        Vec3 offsetPos = player.getEyePosition(partialTicks).add(player.getLookAngle().scale(offsetDistance));
        this.setPos(offsetPos.x(), offsetPos.y(), offsetPos.z());
    }

    public static void addStarToSubBucket(int bucket, Star star)
    {
        ObjectArrayList<Star>[] subs = STAR_BUCKETS_ALL_SUB[bucket];
        int last = subs.length - 1;

        if (subs[last].size() >= Config.COMMON.subBucketSize.get())
        {
            STAR_BUCKETS_ALL_SUB[bucket] = Arrays.copyOf(subs, subs.length + 1);
            STAR_BUCKETS_DRAW_SUB[bucket] = Arrays.copyOf(STAR_BUCKETS_DRAW_SUB[bucket], subs.length + 1);
            DIRTY_SUB_BUCKETS[bucket] = Arrays.copyOf(DIRTY_SUB_BUCKETS[bucket], subs.length + 1);

            last++;

            STAR_BUCKETS_ALL_SUB[bucket][last] = new ObjectArrayList<>();
            STAR_BUCKETS_DRAW_SUB[bucket][last] = new ObjectOpenHashSet<>();
            DIRTY_SUB_BUCKETS[bucket][last] = true;
        }

        STAR_BUCKETS_ALL_SUB[bucket][last].add(star);
        DIRTY_SUB_BUCKETS[bucket][last] = true;
    }

    public void tickStars()
    {
        renderTickCounter++;

        double scale    = Config.COMMON.scale.get();
        double gamma    = Config.COMMON.gamma.get();
        double slope    = Config.COMMON.slope.get();
        double exposure = Config.COMMON.exposure.get();

        starColorUpperBound = StarData.calculateSize(-1.0D, scale, gamma, slope, exposure);
        starColorLowerBound = StarData.calculateSize(6.0D,  scale, gamma, slope, exposure);

        double freq = Config.COMMON.starUpdateFrequency.get();
        double exp = Config.COMMON.starUpdateExponent.get();

        for (int bucket = 0; bucket < NUM_MAG_BUCKETS; bucket++)
        {
            ObjectArrayList<Star>[] subBuckets = STAR_BUCKETS_ALL_SUB[bucket];
            int subCount = subBuckets.length;
            if (subCount == 0) continue;

            int interval = (int) Math.max(1, freq * Math.pow(bucket + 1, exp));
            if (renderTickCounter % interval != 0) continue;

            int sub = SUB_BUCKET_CURSOR[bucket];
            if (sub >= subCount) sub = 0;

            ObjectArrayList<Star> stars = subBuckets[sub];
            if (stars.isEmpty())
            {
                SUB_BUCKET_CURSOR[bucket] = (sub + 1) % subCount;
                continue;
            }

            double bucketMag = (bucket + MIN_MAG_BUCKET) + 0.5D;

            float bucketAlpha = (float) StarData.getStarAlpha(
                CelestialObjectHandler.observerBody,
                player.getY(),
                bucketMag,
                CelestialObjectHandler.starBrightness,
                CelestialObjectHandler.rainLevel
            );

            ObjectArrayList<StarUpdate> updates = new ObjectArrayList<>();

            for (Star star : stars)
            {
                if (star == null) continue;

                star.alpha = bucketAlpha;
                star.tick();
                star.update();
                star.shouldDraw = bucketAlpha > 0.0F && !star.isParentToObserver && star.isInView && star.render;

                int newBucket = getMagnitudeBucket(star, AHelpers.absoluteToApparentMagnitude(star.distance, star.absoluteMagnitude));

                updates.add(new StarUpdate(star, newBucket, star.shouldDraw));
            }

            for (StarUpdate u : updates)
            {
                Star star = u.star;

                int oldBucket = bucket;
                int newBucket = u.newBucket;

                int oldSub = sub;

                if (STAR_BUCKETS_DRAW_SUB[oldBucket][oldSub].remove(star))
                    DIRTY_SUB_BUCKETS[oldBucket][oldSub] = true;

                if (newBucket != oldBucket)
                {
                    STAR_BUCKETS_ALL_SUB[oldBucket][oldSub].remove(star);
                    DIRTY_SUB_BUCKETS[oldBucket][oldSub] = true;

                    ObjectArrayList<Star>[] newSubs = STAR_BUCKETS_ALL_SUB[newBucket];
                    int last = newSubs.length - 1;

                    if (newSubs[last].size() >= Config.COMMON.subBucketSize.get())
                    {
                        STAR_BUCKETS_ALL_SUB[newBucket] =
                            Arrays.copyOf(newSubs, newSubs.length + 1);
                        STAR_BUCKETS_DRAW_SUB[newBucket] =
                            Arrays.copyOf(STAR_BUCKETS_DRAW_SUB[newBucket], newSubs.length + 1);
                        DIRTY_SUB_BUCKETS[newBucket] =
                            Arrays.copyOf(DIRTY_SUB_BUCKETS[newBucket], newSubs.length + 1);

                        last++;

                        STAR_BUCKETS_ALL_SUB[newBucket][last] = new ObjectArrayList<>();
                        STAR_BUCKETS_DRAW_SUB[newBucket][last] = new ObjectOpenHashSet<>();
                        DIRTY_SUB_BUCKETS[newBucket][last] = true;
                    }

                    STAR_BUCKETS_ALL_SUB[newBucket][last].add(star);
                    DIRTY_SUB_BUCKETS[newBucket][last] = true;

                    if (u.keepInBucket)
                    {
                        if (STAR_BUCKETS_DRAW_SUB[newBucket][last].add(star))
                            DIRTY_SUB_BUCKETS[newBucket][last] = true;
                    }
                }
                else
                {
                    if (u.keepInBucket)
                    {
                        if (STAR_BUCKETS_DRAW_SUB[oldBucket][oldSub].add(star))
                            DIRTY_SUB_BUCKETS[oldBucket][oldSub] = true;
                    }
                }
            }

            SUB_BUCKET_CURSOR[bucket] = (sub + 1) % subCount;
        }
    }

    public static int getMagnitudeBucket(Star star, double mag)
    {
        if (star.isInConstellation || mag < -1.0D) return 0;
        int bucket = (int)Math.floor(mag) - MIN_MAG_BUCKET;
        return Math.min(Math.max(bucket, 0), NUM_MAG_BUCKETS - 1);
    }

    public static final class StarUpdate
    {
        final Star star;
        final int newBucket;
        final boolean keepInBucket;

        StarUpdate(Star star, int newBucket, boolean keepInBucket)
        {
            this.star = star;
            this.newBucket = newBucket;
            this.keepInBucket = keepInBucket;
        }
    }

    public static void initEclipticPrecalc()
    {
        for (int k = 0; k < ECLIPTIC_SEGMENTS; k++)
        {
            double lambda = Math.toRadians(k * (360.0 / ECLIPTIC_SEGMENTS));
            SIN_LAMBDA[k] = Math.sin(lambda);
            COS_LAMBDA[k] = Math.cos(lambda);
        }

        double raNGP = Math.toRadians(192.85948D);
        double decNGP = Math.toRadians(27.12825D);

        Vec3 ngp = new Vec3(
            Math.cos(decNGP) * Math.cos(raNGP),
            Math.cos(decNGP) * Math.sin(raNGP),
            Math.sin(decNGP)
        );

        Vec3 ref = new Vec3(0, 0, 1);
        U_NGP = ngp.cross(ref).normalize();
        V_NGP = ngp.cross(U_NGP).normalize();

        for (int k = 0; k < ECLIPTIC_SEGMENTS; k++)
        {
            double lambda = Math.toRadians(k * (360.0 / ECLIPTIC_SEGMENTS));

            EQUATOR_UNIT[k] = AHelpers.getCelestialVector(1.0, 0.0, lambda);

            Vec3 g = U_NGP.scale(COS_LAMBDA[k]).add(V_NGP.scale(SIN_LAMBDA[k]));
            double raG = FastMath.atan2Fast(g.y, g.x);
            if (raG < 0) raG += Math.PI * 2;
            double decG = FastMath.asinFast(g.z);

            GALACTIC_UNIT[k] = AHelpers.getCelestialVector(1.0, decG, raG);
        }
    }

    public void updateEclipticPositions()
    {
        if (observerObject == null) return;

        eclipticPlane.clear();
        celestialEquator.clear();
        galacticPlane.clear();

        double O  = observerObject.ascendingNode;
        double I  = observerObject.inclination;
        double ob = observerObject.obliquity;

        double distance = Config.COMMON.constellationLineDistance.get() * 1.01;

        for (int k = 0; k < ECLIPTIC_SEGMENTS; k++)
        {
            double lambda = Math.toRadians(k * (360.0 / ECLIPTIC_SEGMENTS));
            Vec3 base = new Vec3(Math.cos(lambda), Math.sin(lambda), 0.0);

            Vec3 ecl = PlanetHelpers.rotZ(O, base);
            ecl = PlanetHelpers.rotX(I, ecl);
            ecl = PlanetHelpers.applyObserverFrame(ecl, observerObject);
            eclipticPlane.add(ecl.scale(distance));

            Vec3 eq = PlanetHelpers.rotZ(O, base);
            eq = PlanetHelpers.rotX(I, eq);
            eq = PlanetHelpers.rotX(ob, eq);
            eq = PlanetHelpers.applyObserverFrame(eq, observerObject);
            celestialEquator.add(eq.scale(distance));

            Vec3 gal = GALACTIC_UNIT[k];
            gal = PlanetHelpers.applyObserverFrame(gal, observerObject);
            galacticPlane.add(gal.scale(distance));
        }
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void setBoundingBox(AABB bb)
    {
        ((ParticleAccessor) this).setBB(AABB.INFINITE);
    }

    public static boolean isObserver(CelestialObject celestialObject)
    {
        if (CelestialObjectHandler.observerObject == null)
        {
            return false;
        }
        return celestialObject.equals(CelestialObjectHandler.observerObject);
    }

    public static void updateAtmosphereFactor()
    {
        if (observerBody == null)
        {
            atmosphereFactor = 0.0D;
            return;
        }

        Atmosphere atmosphere = observerBody.getAtmosphere();

        // No atmosphere object OR height <= 0 -> no atmospheric effects
        if (atmosphere == null || atmosphere.getHeight() <= 0.0D)
        {
            atmosphereFactor = 0.0D;
            return;
        }

        double surfaceHeight = observerBody.getSurfaceHeight();
        double atmosphereHeight = atmosphere.getHeight();

        // Fade atmosphere influence starting at 10% of atmosphere height
        double atmosphereFadeStart = atmosphereHeight * 0.1D;
        double relAlt = playerPos.y() - surfaceHeight;

        if (relAlt < atmosphereFadeStart)
        {
            atmosphereFactor = 1.0D;
        }
        else
        {
            double fadeRange = atmosphereHeight - atmosphereFadeStart;

            // Prevent division by zero (extremely thin atmospheres)
            if (fadeRange <= 0.0D)
            {
                atmosphereFactor = 0.0D;
                return;
            }

            double t = 1.0D - ((relAlt - atmosphereFadeStart) / fadeRange);
            atmosphereFactor = Mth.clamp(t, 0.0D, 1.0D);
        }
    }
}
