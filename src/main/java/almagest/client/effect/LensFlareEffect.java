package almagest.client.effect;

import java.util.stream.IntStream;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.RenderEventHandler;
import almagest.client.RenderHelpers;
import almagest.client.data.PlanetDataManager;
import almagest.client.data.PlanetDataManager.PlanetData;
import almagest.config.Config;
import almagest.util.AHelpers;

@SuppressWarnings("null")
public class LensFlareEffect extends TextureSheetParticle
{
    public static final float[] FLARE_SIZES = {
            0.15f, 0.24f, 0.12f, 0.036f, 0.06f,
            0.048f, 0.006f, 0.012f, 0.5f, 0.09f,
            0.036f, 0.09f, 0.06f, 0.05f, 0.6f
    };

    public static final float[] FLARE_INFLUENCES = {
            -1.3f, -2.0f, 0.2f, 0.4f, 0.25f,
            -0.25f, -0.7f, -1.0f, 1.0f, 1.4f,
            -1.31f, -1.2f, -1.5f, -1.55f, -3.0f
    };

    public static final ResourceLocation[] FLARES = IntStream.range(0, FLARE_SIZES.length)
            .mapToObj(i -> AHelpers.identifier("textures/environment/lense_flare/flare" + i + ".png"))
            .toArray(ResourceLocation[]::new);

    public static final ResourceLocation BLINDNESS_OVERLAY = AHelpers.identifier("textures/environment/lense_flare/sun_blindness.png");

    public final Minecraft mc;
    public final Player player;
    public final PlanetData sun;
    public final int id;
    public Vec3 pos;
    public Vec3 normPos;
    public Vec3 normal = new Vec3(0.0D, 1.0D, 0.0D);
    public float actualSunAlpha = 1.0F;
    public double angleToSun;
    public float intensity;
    public float red;
    public float green;
    public float blue;
    public long time;

    public LensFlareEffect(ClientLevel level, Player player)
    {
        super(level, 0.0D, 0.0D, 0.0D);
        this.setPos(0.0D, 0.0D, 0.0D);
        this.mc = Minecraft.getInstance();
        this.player = player;
        this.sun = PlanetDataManager.get(0).get();
        this.id = 0;

        this.time = RenderEventHandler.time;

        this.pos = PlanetDataManager.getCachedPos(0, level, time, false, true);
        this.normPos = pos.normalize().scale(10.0D);
        this.angleToSun = Math.pow(Math.toRadians(AHelpers.getAngleBetween(normPos.normalize(), RenderEventHandler.lookAngle)) + 0.5D, 1.5D);

        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = 1.0F;

        this.red = (float) sun.getColor().x();
        this.green = (float) sun.getColor().y();
        this.blue = (float) sun.getColor().z();
    }

    @Override
    public void tick()
    {
        if (!this.isAlive() || !Config.COMMON.enableLensFlare.get())
        {
            RenderEventHandler.lensFlare = null;
            this.remove();
            this.setAlpha(0.0F);
        }

        this.time = RenderEventHandler.time;

        this.pos = PlanetDataManager.getCachedPos(0, level, time, false, true);
        this.normPos = pos.normalize().scale(10.0D);
        this.intensity = Config.COMMON.lensFlareIntensity.get().floatValue();
        this.actualSunAlpha = Math.min(1.0F, AHelpers.nearValue(actualSunAlpha, (1.0F - level.getRainLevel(mc.getPartialTick())) * (1.0F - RenderEventHandler.starBrightness), 0.1F, 0.01F));
        this.angleToSun = Math.pow(Math.toRadians(AHelpers.getAngleBetween(normPos.normalize(), RenderEventHandler.lookAngle)) + 0.5D, 1.5D);
    }

    @Override
    public void setLifetime(int particleLifeTime)
    {
        this.lifetime = Integer.MAX_VALUE;
    }

    @Override
    public float getQuadSize(float scaleFactor)
    {
        return Config.COMMON.lensFlareSize.get().floatValue();
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        /*if (actualSunAlpha <= 0.0F || intensity <= 0.0F || !Config.COMMON.enableLensFlare.get())
        {
            return;
        }

        if (!AHelpers.isWithinAngle(new Vec3(camera.getLookVector()), sunPos, AHelpers.getFOV(player, 1.0D)))
        {
            return;
        }*/

        /*double angle = calculateAngle(normPos, normal);
        float alpha = 1.0F; //(float) getAlphaByAngle(angle, -5.0D, 5.0D);
        float xSun = (float) normPos.x();
        float ySun = (float) normPos.y();

        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        for (int i = 0; i < FLARE_SIZES.length; i++)
        {
            float flareSizeHalf = FLARE_SIZES[i] * 0.5f;
            float flareCenterX = xSun * FLARE_INFLUENCES[i];
            float flareCenterY = ySun * FLARE_INFLUENCES[i];

            RenderSystem.setShaderColor(red - 0.1F, green - 0.1F, blue - 0.1F, (alpha * i == 8.0F ? 1F : 0.5F) * actualSunAlpha * intensity);
            drawQuad(FLARES[i], normPos,
                    flareCenterX - flareSizeHalf,
                    flareCenterY - flareSizeHalf,
                    flareCenterX + flareSizeHalf,
                    flareCenterY + flareSizeHalf
            );
        }

        float genDist = 1.0F - (xSun * xSun + ySun * ySun);
        float blendingSize = (genDist - 0.1F) * intensity * 250F;

        if (blendingSize > 0)
        {
            float blendingSizeHalf = blendingSize * 0.5F;
            float blendAlpha = Math.min(1.0F, blendingSize / 150F);

            RenderSystem.setShaderColor(red - 0.1F, green - 0.1F, blue - 0.1F, blendAlpha * actualSunAlpha);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            drawQuad(BLINDNESS_OVERLAY, normPos,
                    xSun - blendingSizeHalf,
                    ySun - blendingSizeHalf,
                    xSun + blendingSizeHalf,
                    ySun + blendingSizeHalf
            );
        }*/
        drawFlares(buffer, camera, partialTicks);
        drawBlindnessOverlay(buffer, camera, partialTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }


    public void drawFlares(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        /*float angle = (float) AHelpers.getAngleBetween(new Vec3(camera.getLookVector()), normPos);
        float genDist = (float) AHelpers.cubicEaseInNorm(angle, 0, 1, 180.0D, 0.0D, 20);
        float blendingSize = (genDist - 0.1F) * intensity * 250F;*/

        for (int f = 0; f < FLARE_SIZES.length; f++)
        {
            //float flareSizeHalf = FLARE_SIZES[f] * 0.5f;
            Vec3 flarePos = normPos.xRot((float) (Math.toRadians(FLARE_INFLUENCES[f]) * this.angleToSun)).zRot((float) (Math.toRadians(FLARE_INFLUENCES[f]) * this.angleToSun));

            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();

            RenderHelpers.translucentTransparency();
            RenderSystem.depthMask(true);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            RenderSystem.setShaderTexture(0, FLARES[f]);

            float r = red - 0.1F;
            float g = green - 0.1F;
            float b = blue - 0.1F;
            float a = (alpha * f == 8.0F ? 1F : 0.5F) * actualSunAlpha * intensity;
            RenderSystem.setShaderColor(r, g, b, a);

            poseStack.translate(flarePos.x(), flarePos.y(), flarePos.z());
            Matrix4f matrix = poseStack.last().pose();

            Quaternionf quaternion;
            if (this.roll == 0.0F)
            {
                quaternion = camera.rotation();
            }
            else
            {
                quaternion = new Quaternionf(camera.rotation());
                quaternion.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
            }

            float size = this.getQuadSize(partialTicks) / 1.5F;

            Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-size, -size, 0.0F),
                new Vector3f(size, -size, 0.0F),
                new Vector3f(size, size, 0.0F),
                new Vector3f(-size, size, 0.0F)
            };

            for (int i = 0; i < 4; ++i)
            {
                Vector3f vector3 = vertices[i];
                vector3.rotate(quaternion);
            }

            float u0 = 0.0F;
            float u1 = 1.0F;
            float v0 = 0.0F;
            float v1 = 1.0F;

            builder.vertex(matrix, vertices[0].x(), vertices[0].y(), vertices[0].z()).color(r, g, b, a).uv(u1, v1).endVertex();
            builder.vertex(matrix, vertices[1].x(), vertices[1].y(), vertices[1].z()).color(r, g, b, a).uv(u1, v0).endVertex();
            builder.vertex(matrix, vertices[2].x(), vertices[2].y(), vertices[2].z()).color(r, g, b, a).uv(u0, v0).endVertex();
            builder.vertex(matrix, vertices[3].x(), vertices[3].y(), vertices[3].z()).color(r, g, b, a).uv(u0, v1).endVertex();

            BufferUploader.drawWithShader(builder.end());
            poseStack.popPose();
        }
    }

    public void drawBlindnessOverlay(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        float angle = (float) AHelpers.getAngleBetween(RenderEventHandler.lookAngle, normPos);
        float genDist = (float) AHelpers.cubicEaseInNorm(angle, 0, 1, 180.0D, 0.0D, 10);
        float blendingSize = (genDist - 0.1F) * intensity * 250F;

        if (blendingSize > 0)
        {
            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();

            RenderHelpers.translucentTransparency();
            RenderSystem.depthMask(true);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            RenderSystem.setShaderTexture(0, BLINDNESS_OVERLAY);

            float blendAlpha = Math.min(1.0F, blendingSize / 150F);

            float r = red - 0.1F;
            float g = green - 0.1F;
            float b = blue - 0.1F;
            float a = blendAlpha * actualSunAlpha;
            RenderSystem.setShaderColor(r, g, b, a);

            poseStack.translate(normPos.x(), normPos.y(), normPos.z());
            Matrix4f matrix = poseStack.last().pose();

            Quaternionf quaternion;
            if (this.roll == 0.0F)
            {
                quaternion = camera.rotation();
            }
            else
            {
                quaternion = new Quaternionf(camera.rotation());
                quaternion.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
            }

            float size = this.getQuadSize(partialTicks);

            Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-size, -size, 0.0F),
                new Vector3f(size, -size, 0.0F),
                new Vector3f(size, size, 0.0F),
                new Vector3f(-size, size, 0.0F)
            };

            for (int i = 0; i < 4; ++i)
            {
                Vector3f vector3 = vertices[i];
                vector3.rotate(quaternion);
            }

            float u0 = 0.0F;
            float u1 = 1.0F;
            float v0 = 0.0F;
            float v1 = 1.0F;

            builder.vertex(matrix, vertices[0].x(), vertices[0].y(), vertices[0].z()).color(r, g, b, a).uv(u1, v1).endVertex();
            builder.vertex(matrix, vertices[1].x(), vertices[1].y(), vertices[1].z()).color(r, g, b, a).uv(u1, v0).endVertex();
            builder.vertex(matrix, vertices[2].x(), vertices[2].y(), vertices[2].z()).color(r, g, b, a).uv(u0, v0).endVertex();
            builder.vertex(matrix, vertices[3].x(), vertices[3].y(), vertices[3].z()).color(r, g, b, a).uv(u0, v1).endVertex();

            BufferUploader.drawWithShader(builder.end());
            poseStack.popPose();
        }
    }
}
