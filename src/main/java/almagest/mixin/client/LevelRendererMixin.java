package almagest.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexBuffer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;

@Mixin(value = LevelRenderer.class, priority = 999999)
public abstract class LevelRendererMixin
{
	/*@Inject(method = "renderLevel", at = @At("TAIL"), cancellable = true)
    private void inject$renderSky(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci)
    {
        FogRenderer.setupNoFog();
        CelestialDataManager.SKYBOX.values().stream().forEach(skybox -> skybox.render(camera, partialTick));
    }*/

    @Redirect(
        method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V"
        )
    )
    private void renderSky$skipVanillaStars(VertexBuffer buffer, Matrix4f lastPose, Matrix4f projectionMatrix, ShaderInstance shaderInstance) {}

    @Redirect(
        method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"
        )
    )
    private float renderSky$redirectRainLevel(ClientLevel level, float partialTick)
    {
        return 1.0F;
    }
}
