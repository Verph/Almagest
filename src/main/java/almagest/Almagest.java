package almagest;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

import almagest.client.ClientEventHandler;
import almagest.client.EventHandler;
import almagest.client.blocks.ABlocks;
import almagest.client.data.CelestialDataManager;
import almagest.client.items.AItems;
import almagest.client.renderer.AShaders;
import almagest.config.Config;
import almagest.util.FastMath;

@Mod(Almagest.MOD_ID)
public class Almagest
{
    public static final String MOD_ID = "almagest";
    public static final String MOD_NAME = "Almagest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Almagest(ModContainer mod, IEventBus bus)
    {
        mod.registerConfig(ModConfig.Type.STARTUP, Config.COMMON_SPEC);
        FastMath.initTrigTables();

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            AShaders.init(mod.getEventBus());
            CelestialDataManager.init();
            ClientEventHandler.init(bus);
            EventHandler.init();
            ABlocks.BLOCKS.register(bus);
            AItems.ITEMS.register(bus);
        }
    }
}