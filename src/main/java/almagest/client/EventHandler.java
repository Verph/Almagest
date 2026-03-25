package almagest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

import almagest.client.data.CelestialDataManager;
import almagest.client.renderer.AVertexBuffers;

@SuppressWarnings("null")
public class EventHandler
{
    public static void init()
    {
        final IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(EventHandler::onLevelLoad);
        bus.addListener(EventHandler::onLevelUnload);
        bus.addListener(EventHandler::onClientLogout);
    }

    public static void onLevelLoad(ClientTickEvent.Post event)
    {
        if (!CelestialDataManager.HAS_INITIALIZED_CELESTIAL_OBJECTS)
        {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (mc.level != null && player != null)
            {
                CelestialDataManager.initObjects(mc.level, player, mc);
            }
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event)
    {
        //CelestialDataManager.clearCaches();
        CelestialDataManager.HAS_INITIALIZED_CELESTIAL_OBJECTS = false;
        AVertexBuffers.clearStarVBOs();
    }

    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event)
    {
        AVertexBuffers.clearStarVBOs();
    }
}