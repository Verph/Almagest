package almagest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import almagest.Almagest;
import almagest.client.effect.LensFlareEffect;
import almagest.client.particle.CelestialObject;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.data.PlanetDataManager.*;

import static almagest.client.CelestialObjectHandler.*;

@SuppressWarnings("null")
public class RenderEventHandler
{
    /*public static final Vec3 NORMAL_VEC = new Vec3(0.0D, 1.0D, 0.0D);
    public static final double observerRotationPeriod = ROTATION_PERIOD_CACHE.get(getObserver()).doubleValue();*/
    public static volatile boolean HAS_INITIALIZED_CELESTIAL_OBJECTS = false;
    public static volatile Minecraft mc;
    public static volatile float starBrightness;
    public static volatile float rainLevel;
    public static volatile double fieldOfViewModifier;
    public static volatile double fov;
    public static volatile long gameTime;
    public static volatile long time;
    public static volatile double dayRotationDeg;
    public static volatile double dayRotationRad;
    public static volatile double latitudeDeg;
    public static volatile double longitudeDeg;
    public static volatile double latitude;
    public static volatile double longitude;
    public static volatile boolean isScoping;
    public static volatile Vec3 lookAngle = Vec3.ZERO;
    public static volatile Vec3 playerPos = Vec3.ZERO;
    public static volatile LensFlareEffect lensFlare;
    //public static volatile Vec3 solarSystemNormal = NORMAL_VEC;

    public static void init()
    {
        final IEventBus bus = MinecraftForge.EVENT_BUS;

        bus.addListener(RenderEventHandler::loadCelestialObjectHandler);
        bus.addListener(RenderEventHandler::unloadCelestialObjectHandler);
        //bus.addListener(RenderEventHandler::renderLensFlare);
        bus.addListener(RenderEventHandler::updateCommonParam);
    }

    public static void updateCommonParam(ClientTickEvent event)
    {
        mc = Minecraft.getInstance();
        if (mc != null && mc.level != null)
        {
            final long manualTime = Config.COMMON.manualTimeControl.get();

            ClientLevel level = mc.level;
            Player player = mc.player;
            GameRenderer gameRenderer = mc.gameRenderer;
            float partialTicks = mc.getPartialTick();
            starBrightness = level.getStarBrightness(partialTicks);
            rainLevel = level.getRainLevel(partialTicks);
            fieldOfViewModifier = AHelpers.getFieldOfViewModifier(player);
            fov = AHelpers.getFOV(gameRenderer.getMainCamera(), mc.getPartialTick());
            gameTime = level.getGameTime();
            time = manualTime > -1 ? manualTime : level.getDayTime();
            dayRotationDeg = level.getTimeOfDay(partialTicks) * 360.0D;
            dayRotationRad = Math.toRadians(dayRotationDeg);
            lookAngle = player.getLookAngle();
            playerPos = player.position();
            latitudeDeg = AHelpers.angleFromPole(level, playerPos.z(), 0.0D) + 90;
            longitudeDeg = AHelpers.angleFromPrimeMeridian(level, playerPos.x(), 0.0D) - 180.0D;
            latitude = Math.toRadians(latitudeDeg);
            longitude = Math.toRadians(longitudeDeg);
            isScoping = player.isScoping();

            /*final double season = getSeason(OBSERVER, time, true, true);
            final double timeOfDay = getCachedTimeOfDay(OBSERVER, time, observerRotationPeriod, true);
            solarSystemNormal = applyRotations(NORMAL_VEC, latitude, season, 0.0D, timeOfDay);*/

            updateObserverParam(event);
        }
    }

    public static void renderLensFlare(ClientTickEvent event)
    {
        if (mc != null && mc.level != null && mc.player != null && lensFlare == null)
        {
            lensFlare = new LensFlareEffect(mc.level, mc.player);
            mc.particleEngine.add(lensFlare);
        }
    }

    public static void loadCelestialObjectHandler(ClientTickEvent event)
    {
        if (!HAS_INITIALIZED_CELESTIAL_OBJECTS && mc != null)
        {
            Player player = mc.player;

            if (mc.level != null && player != null)
            {
                Almagest.LOGGER.info("Initializing celestial object handler!");
                CelestialObjectHandler.initializeCelestialObjects(mc.level, player, mc);
                HAS_INITIALIZED_CELESTIAL_OBJECTS = true;
            }
        }
    }

    public static void unloadCelestialObjectHandler(LevelEvent.Unload event)
    {
        for (CelestialObject celestialObject : CELESTIAL_OBJECTS)
        {
            celestialObject.remove();
        }
        CELESTIAL_OBJECTS.clear();
        HAS_INITIALIZED_CELESTIAL_OBJECTS = false;
    }
}