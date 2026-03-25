package almagest.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import almagest.client.blocks.ABlocks;
import almagest.util.AHelpers;

public class ClientEventHandler
{
    public static void init(IEventBus bus)
    {
        //bus.addListener(ClientEventHandler::onClientSetup);
        bus.addListener(ClientEventHandler::onRegisterKeyBindings);
        //bus.addListener(ClientEventHandler::onUpdateStarSizes);
    }

    @SuppressWarnings("deprecation")
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        ItemBlockRenderTypes.setRenderLayer(ABlocks.CELESTIAL_BODY.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ABlocks.DAY_CYCLE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ABlocks.STAR.get(), RenderType.translucent());
    }

    public static void onRegisterKeyBindings(RegisterKeyMappingsEvent event)
    {
        event.register(AHelpers.RELOAD_STAR_DATA);
    }

    /*public static void onUpdateStarSizes(ModConfigEvent.Reloading event)
    {
        if (event.getConfig().getSpec() == Config.COMMON)
        {
            CelestialDataManager.STAR_OBJECTS_BY_ID.values().parallelStream().forEach(Star::updateAdjustedSize);
        }
    }*/
}
