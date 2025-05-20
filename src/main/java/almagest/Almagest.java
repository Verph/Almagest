package almagest;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import almagest.client.CelestialObjectHandler;
import almagest.client.ClientEventHandler;
import almagest.client.RenderEventHandler;
import almagest.client.blocks.ABlocks;
import almagest.client.items.AItems;
import almagest.config.Config;
import almagest.util.AHelpers;

@Mod(Almagest.MOD_ID)
public class Almagest
{
    public static final String MOD_ID = "almagest";
    public static final String MOD_NAME = "Almagest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Almagest()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        CelestialObjectHandler.initData();
        Config.init();
        AHelpers.initTrigTables();

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            ClientEventHandler.init(bus);
            RenderEventHandler.init();
            AItems.ITEMS.register(bus);
            ABlocks.BLOCKS.register(bus);
        }
    }
}