package almagest.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.GameRenderer;

import almagest.config.Config;

@Mixin(value = GameRenderer.class, priority = 100)
public abstract class GameRendererMixin 
{
	@Inject(method = "getDepthFar", at = @At("RETURN"), cancellable = true)
	private void inject$getDepthFar(CallbackInfoReturnable<Float> cir)
    {
		cir.setReturnValue(Config.COMMON.farPlaneClippingDistance.get().floatValue());
	}
}