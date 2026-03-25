package almagest.client.renderer;

import java.io.IOException;

import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import almagest.util.AHelpers;

public class AShaders
{
    public static ShaderInstance STAR_SHADER;

    public static void init(IEventBus bus)
    {
        bus.addListener(AShaders::registerShaders);
    }

    public static void registerShaders(RegisterShadersEvent event)
    {
        try
        {
            ShaderInstance shader = new ShaderInstance(
                event.getResourceProvider(),
                AHelpers.identifier("star"),
                ARenderTypes.STAR_VERTEX_FORMAT
            );
            event.registerShader(shader, s -> STAR_SHADER = s);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load star shader", e);
        }
    }
}
