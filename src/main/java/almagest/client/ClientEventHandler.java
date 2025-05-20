package almagest.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import almagest.client.blocks.ABlocks;
import almagest.client.data.CometDataManager;
import almagest.client.data.ConstellationDataManager;
import almagest.client.data.GalaxyDataManager;
import almagest.client.data.MeteorSwarmDataManager;
import almagest.client.data.MinorPlanetDataManager;
import almagest.client.data.MoonDataManager;
import almagest.client.data.NebulaDataManager;
import almagest.client.data.PlanetDataManager;
import almagest.client.data.StarDataManager;
import almagest.util.AHelpers;

public class ClientEventHandler
{
    public static boolean HAS_INITIALIZED_CACHES = false;

    public static void init(IEventBus bus)
    {
        final IEventBus eBus = MinecraftForge.EVENT_BUS;

        bus.addListener(ClientEventHandler::clientSetup);
        //bus.addListener(ClientEventHandler::registerClientReloadListeners);
        bus.addListener(ClientEventHandler::registerKeyBindings);
        bus.addListener(ParticleRegistry::registerParticles);
        eBus.addListener(ClientEventHandler::onKeyEvent);
        eBus.addListener(ClientEventHandler::initCaches);
    }

    public static void clientSetup(FMLClientSetupEvent event)
    {
        final RenderType translucent = RenderType.translucent();
        ItemBlockRenderTypes.setRenderLayer(ABlocks.CELESTIAL_BODY.get(), translucent);
        ItemBlockRenderTypes.setRenderLayer(ABlocks.DAY_CYCLE.get(), translucent);
    }

    /*public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(StarDataManager.STAR_DATA);
        event.registerReloadListener(ConstellationDataManager.CONSTELLATION_DATA);
        event.registerReloadListener(GalaxyDataManager.GALAXY_DATA);
        event.registerReloadListener(MinorPlanetDataManager.MINOR_PLANET_DATA);
        event.registerReloadListener(MoonDataManager.MOON_DATA);
        event.registerReloadListener(NebulaDataManager.NEBULA_DATA);
        event.registerReloadListener(PlanetDataManager.PLANET_DATA);
        event.registerReloadListener(CometDataManager.COMET_DATA);
        event.registerReloadListener(MeteorSwarmDataManager.METEOR_DATA);
    }*/

    public static void initCaches(LevelEvent.Load event)
    {
        if (!HAS_INITIALIZED_CACHES)
        {
            StarDataManager.initCaches();
            PlanetDataManager.initCaches();
            MinorPlanetDataManager.initCaches();
            MoonDataManager.initCaches();
            CometDataManager.initCaches();
            MeteorSwarmDataManager.initCaches();
            HAS_INITIALIZED_CACHES = true;
        }
    }

    public static void clearCaches(LevelEvent.Unload event)
    {
        if (HAS_INITIALIZED_CACHES)
        {
            StarDataManager.clearCaches();
            PlanetDataManager.clearCaches();
            NebulaDataManager.clearCaches();
            GalaxyDataManager.clearCaches();
            MeteorSwarmDataManager.clearCaches();
            ConstellationDataManager.clearCaches();
            HAS_INITIALIZED_CACHES = false;
        }
    }

    public static void registerKeyBindings(RegisterKeyMappingsEvent event)
    {
        event.register(AHelpers.RELOAD_STAR_DATA);
    }

    public static void onKeyEvent(InputEvent.Key event)
    {
        if (AHelpers.RELOAD_STAR_DATA.isDown())
        {
            PlanetDataManager.POSITION_CACHE.clear();
            GalaxyDataManager.POSITION_CACHE.clear();
            NebulaDataManager.POSITION_CACHE.clear();
            StarDataManager.POSITION_CACHE.clear();
            StarDataManager.POSITION_ADJ_CACHE.clear();
            RenderEventHandler.HAS_INITIALIZED_CELESTIAL_OBJECTS = false;
        }
    }
}
