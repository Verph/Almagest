package almagest.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(value = GameRenderer.class, priority = 100)
public interface GameRendererAccessor
{
    @Invoker("getFov")
    double getFov(Camera camera, float partialTicks, boolean useFovSetting);
}
