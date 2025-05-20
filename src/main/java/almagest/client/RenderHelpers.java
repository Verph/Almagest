package almagest.client;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.model.data.ModelData;

import almagest.client.blocks.ABlocks;
import almagest.client.blocks.CelestialBodyBlock;
import almagest.client.blocks.DayCycleBlock;
import almagest.client.data.PlanetDataManager;
import almagest.client.data.StarDataManager.StarData;
import almagest.config.Config;
import almagest.mixin.client.BlockRenderDispatcherAccessor;
import almagest.util.AHelpers;

import static almagest.client.data.PlanetDataManager.*;

@SuppressWarnings({"null", "deprecation"})
public class RenderHelpers
{
    public static final ResourceLocation WHITE = AHelpers.identifier("textures/particles/white.png");
    public static final ResourceLocation CUBE_DEBUG = AHelpers.identifier("textures/cube.png");

    public static final void noTransparency() {RenderSystem.disableBlend();}
    public static final void additiveTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);}
    public static final void lightningTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);}
    public static final void glintTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);}
    public static final void crumblingTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);}
    public static final void translucentTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);}

    public static void drawRing(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, double obliquity, double rotationTime, double minRingRadius, double maxRingRadius, double alphaMod, int glow)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        poseStack.translate(pos.x(), pos.y(), pos.z());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) obliquity));
        poseStack.mulPose(Axis.YN.rotationDegrees((float) rotationTime));

        float minRadius = (float) minRingRadius;
        float maxRadius = (float) maxRingRadius;
        int segments = 20;

        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float alpha = (float) alphaMod;
        Color color = new Color(r, g, b, alpha);

        for (int i = 0; i < segments; i++)
        {
            double angle1 = 2.0D * Math.PI * i / segments;
            double angle2 = 2.0D * Math.PI * (i + 1) / segments;

            float x1Inner = (float) (minRadius * Math.cos(angle1));
            float z1Inner = (float) (minRadius * Math.sin(angle1));
            float x2Inner = (float) (minRadius * Math.cos(angle2));
            float z2Inner = (float) (minRadius * Math.sin(angle2));

            float x1Outer = (float) (maxRadius * Math.cos(angle1));
            float z1Outer = (float) (maxRadius * Math.sin(angle1));
            float x2Outer = (float) (maxRadius * Math.cos(angle2));
            float z2Outer = (float) (maxRadius * Math.sin(angle2));

            renderTexturedVertex(poseStack, builder, color, x1Inner, 0.0F, z1Inner, glow, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
            renderTexturedVertex(poseStack, builder, color, x2Inner, 0.0F, z2Inner, glow, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F);
            renderTexturedVertex(poseStack, builder, color, x2Outer, 0.0F, z2Outer, glow, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F);
            renderTexturedVertex(poseStack, builder, color, x1Outer, 0.0F, z1Outer, glow, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F);
        }

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawBodyFlat(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, Vec3 color, double alphaMod, float quadSize, float roll, float oRoll, int glow)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        poseStack.translate(pos.x(), pos.y(), pos.z());
        float size = quadSize / 2.0F;

        Quaternionf quaternion;
        if (roll == 0.0F)
        {
            quaternion = camera.rotation();
        }
        else
        {
            quaternion = new Quaternionf(camera.rotation());
            quaternion.rotateZ(Mth.lerp(partialTicks, oRoll, roll));
        }

        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; ++i)
        {
            Vector3f vector3 = vertices[i];
            vector3.rotate(quaternion);
            vector3.mul(size);
        }

        float r = (float) color.x();
        float g = (float) color.y();
        float b = (float) color.z();
        float alpha = (float) alphaMod;

        renderTexturedQuad(builder, poseStack, vertices, r, g, b, alpha, glow);

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawGlow(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, Vec3 color, double diameter, float quadSize, float roll, float oRoll, int glow)
    {
        /*PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderHelpers.translucentTransparency();
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.depthMask(true);
        RenderSystem.0(GL13.GL_TEXTURE3);
        RenderSystem.setShaderTexture(GL13.GL_TEXTURE3, texture);

        float r = (float) color.x();
        float g = (float) color.y();
        float b = (float) color.z();
        float a = 1.0F;

        Vec3 cameraPos = camera.getPosition();
        Vec3 direction = pos.subtract(cameraPos).normalize();
        Vec3 offset = direction.scale(quadSize / 2.0D);
        Vec3 pos1 = pos.add(offset);
        poseStack.translate(pos1.x(), pos1.y(), pos1.z());

        double distanceToPlanet = pos.distanceTo(cameraPos);
        float size = (float) Mth.clamp(Mth.lerp((distanceToPlanet - 5.0D * diameter) / (50.0D * diameter - 5.0D * diameter), 0.0D, 5.0D * quadSize), 0.0D, 5.0D * quadSize) / 2.0F;

        Quaternionf quaternion;
        if (roll == 0.0F)
        {
            quaternion = camera.rotation();
        }
        else
        {
            quaternion = new Quaternionf(camera.rotation());
            quaternion.rotateZ(Mth.lerp(partialTicks, oRoll, roll));
        }

        Vector3f[] vertices = new Vector3f[]{
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; ++i)
        {
            Vector3f vector3 = vertices[i];
            vector3.rotate(quaternion);
            vector3.mul(size);
        }

        renderTexturedQuadDouble(buffer, poseStack, vertices, r, g, b, a, glow);

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);*/
    }

    public static void drawBody(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, Vec3 color, double obliquity, double rotationTime, double inclination, Vector2d dayNightCycle, double alphaMod, float quadSize, int glow, float sizeFactor)
    {
        RenderHelpers.noTransparency();
        drawCelestialBody(builder, camera, partialTicks, texture, name, pos, color, obliquity, rotationTime, inclination, dayNightCycle, alphaMod, quadSize, glow, sizeFactor);
    }

    public static void drawBody(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, Vec3 color, double obliquity, double rotationTime, double inclination, Vector2d dayNightCycle, double alphaMod, float quadSize, int glow)
    {
        RenderHelpers.noTransparency();
        drawCelestialBody(builder, camera, partialTicks, texture, name, pos, color, obliquity, rotationTime, inclination, dayNightCycle, alphaMod, quadSize, glow, 2.0F);
    }

    public static void drawClouds(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, Vec3 color, double obliquity, double rotationTime, double inclination, Vector2d dayNightCycle, double alphaMod, float quadSize, int glow)
    {
        RenderHelpers.translucentTransparency();
        drawCelestialBody(builder, camera, partialTicks, texture, name, pos, color, obliquity, rotationTime, inclination, dayNightCycle, alphaMod, quadSize, glow, 1.995F);
    }

    public static void drawDayNightCycle(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, Vec3 color, double obliquity, double rotationTime, double inclination, Vector2d dayNightCycle, double alphaMod, float quadSize, int glow)
    {
        RenderHelpers.translucentTransparency();
        drawCelestialBody(builder, camera, partialTicks, texture, name, pos, color, obliquity, rotationTime, inclination, dayNightCycle, alphaMod, quadSize, glow, 1.985F);
    }

    public static void drawCelestialBody(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, Vec3 color, double obliquity, double rotationTime, double inclination, Vector2d dayNightCycle, double alphaMod, float quadSize, int glow, float sizeFactor)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        //poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        //poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

        poseStack.pushPose();

        final RenderType translucent = RenderType.translucent();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) inclination));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) obliquity));
        poseStack.mulPose(Axis.YN.rotationDegrees((float) rotationTime));
        poseStack.translate(pos.x(), pos.y(), pos.z());

        float size = quadSize / sizeFactor;
        poseStack.translate(-size, -size, -size);
        poseStack.scale(quadSize, quadSize, quadSize);

        poseStack.pushPose();
        renderBlockModel(builder, ABlocks.CELESTIAL_BODY.get().defaultBlockState().setValue(CelestialBodyBlock.CELESTIAL_BODY, name).setValue(CelestialBodyBlock.ALTERNATIVE, Config.COMMON.toggleEasterEggMoon.get()), poseStack, glow, translucent, 1.0F);
        poseStack.popPose();
        poseStack.pushPose();
        renderBlockModel(builder, ABlocks.DAY_CYCLE.get().defaultBlockState().setValue(DayCycleBlock.TIME, (int) dayNightCycle.y()).setValue(DayCycleBlock.OBLIQUITY, (int) Math.round((dayNightCycle.x() + 180.0F) / 2.5F)), poseStack, glow, translucent, 1.0F);
        poseStack.popPose();

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawCelestialBodyNoOverlay(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, float quadSize, int glow)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        //poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        //poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

        poseStack.pushPose();

        final RenderType translucent = RenderType.translucent();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        poseStack.translate(pos.x(), pos.y(), pos.z());

        float size = quadSize / 2.0F;
        poseStack.translate(-size, -size, -size);
        poseStack.scale(quadSize, quadSize, quadSize);

        poseStack.pushPose();
        renderBlockModel(builder, ABlocks.CELESTIAL_BODY.get().defaultBlockState().setValue(CelestialBodyBlock.CELESTIAL_BODY, name), poseStack, glow, translucent, 1.0F);
        poseStack.popPose();

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawCometTailBody(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, Vec3 sunPos, Vec3 direction, Vec3 directionToSun, float quadSize, int glow)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        final RenderType translucent = RenderType.translucent();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        double distanceToSunAU = sunPos.distanceTo(pos) / Config.COMMON.planetDistanceFactor.get();
        float distanceFactor = (float) (1.0D - AHelpers.cubicEaseInNorm(distanceToSunAU, 0.0D, 1.0D, 2.5D, 5.0D, 2.0D));
        float outerQuadSize = quadSize * distanceFactor * 10.0F;
        float size = (float) (outerQuadSize / 2.0F);

        poseStack.translate(pos.x(), pos.y(), pos.z());
        poseStack.translate(-size, -size, -size);
        poseStack.scale(outerQuadSize, outerQuadSize, outerQuadSize);

        /*Quaterniond rotation = new Quaterniond();
        rotation.rotateTo(AHelpers.toVector3d(direction), AHelpers.toVector3d(directionToSun));
        poseStack.mulPose(AHelpers.toQuaternionf(rotation));*/

        poseStack.pushPose();
        renderBlockModel(builder, ABlocks.CELESTIAL_BODY.get().defaultBlockState().setValue(CelestialBodyBlock.CELESTIAL_BODY, "comet"), poseStack, glow, translucent, 1.0F);
        poseStack.popPose();

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawWhiteOverlay(BufferBuilder builder, Camera camera, float partialTicks, Vec3 pos, Vec3 color, double diameter, double alphaMod, float quadSize, double obliquity, int glow)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, WHITE);

        Vec3 pos1 = pos.subtract(pos.normalize().scale(diameter * 20.0D));
        poseStack.translate(pos1.x(), pos1.y(), pos1.z());

        float size = quadSize / 1.4F;

        Quaternionf quaternion = new Quaternionf(camera.rotation());
        quaternion.rotateZ((float) obliquity);

        Vector3f[] vertices = new Vector3f[] {
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

        float r = (float) (color.x() * alphaMod);
        float g = (float) (color.y() * alphaMod);
        float b = (float) (color.z() * alphaMod);
        float a = (float) alphaMod;

        renderTexturedQuadDouble(builder, poseStack, vertices, r, g, b, a, glow);

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawOrbit(BufferBuilder builder, Camera camera, float partialTicks, Level level, double configMod, Vec3 parentPos, Vec3 color, double segments, int body, long time, double orbit, double eccentricity, double ascendingNode, double periapsis, double semimajorAxis, double meanLongitude, double inclination, double wobble, Vec3 obliquityRotation)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, WHITE);

        double t = PlanetDataManager.getElapsedTime(time, orbit);
        float r = (float) color.x();
        float g = (float) color.y();
        float b = (float) color.z();
        double fov = RenderEventHandler.fov;
        float size = (float) (configMod * RenderEventHandler.fieldOfViewModifier);

        for (double i = 0; i < segments; i++)
        {
            poseStack.pushPose();
            double i1 = (i / segments);
            double i2 = ((i + 1) / segments);
            Vec3 vec1 = PlanetDataManager.getPos(body, level, time, orbit, AHelpers.modulo(t - (i1 * orbit), orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, false, true, true);
            Vec3 vec2 = PlanetDataManager.getPos(body, level, time, orbit, AHelpers.modulo(t - (i2 * orbit), orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, false, true, true);

            if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, vec1, fov) || AHelpers.isWithinAngle(RenderEventHandler.lookAngle, vec2, fov))
            {
                float a = (float) (0.1D + 0.9D * i1);

                Vec3 direction = vec2.subtract(vec1).normalize();
                poseStack.translate(vec1.x(), vec1.y(), vec1.z());
                poseStack.mulPose(getRotationToAlign(direction, new Vec3(1.0F, 0.0F, 0.0F)));
                float length = (float) vec1.distanceTo(vec2);

                renderTexturedCuboid(poseStack, builder, LightTexture.FULL_SKY, new Color(r, g, b, a), 0.0F, -size, -size, length, size, size, false, false);
            }
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawOrbitNoFade(BufferBuilder builder, Camera camera, float partialTicks, Level level, double configMod, Vec3 parentPos, Vec3 color, double segments, int body, long time, double orbit, double eccentricity, double ascendingNode, double periapsis, double semimajorAxis, double meanLongitude, double inclination, double wobble, Vec3 obliquityRotation)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, WHITE);

        double t = PlanetDataManager.getElapsedTime(time, orbit);
        float r = (float) color.x();
        float g = (float) color.y();
        float b = (float) color.z();
        float a = 1.0F;
        double fov = RenderEventHandler.fov;
        float size = (float) (configMod * RenderEventHandler.fieldOfViewModifier);

        for (double i = 0; i < segments; i++)
        {
            poseStack.pushPose();
            double i1 = (i / segments);
            double i2 = ((i + 1) / segments);
            Vec3 vec1 = PlanetDataManager.getPos(body, level, time, orbit, AHelpers.modulo(t - (i1 * orbit), orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, false, true, true);
            Vec3 vec2 = PlanetDataManager.getPos(body, level, time, orbit, AHelpers.modulo(t - (i2 * orbit), orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, false, true, true);

            if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, vec1, fov) || AHelpers.isWithinAngle(RenderEventHandler.lookAngle, vec2, fov))
            {
                Vec3 direction = vec2.subtract(vec1).normalize();
                poseStack.translate(vec1.x(), vec1.y(), vec1.z());
                poseStack.mulPose(getRotationToAlign(direction, new Vec3(1.0F, 0.0F, 0.0F)));
                float length = (float) vec1.distanceTo(vec2);

                renderTexturedCuboid(poseStack, builder, LightTexture.FULL_SKY, new Color(r, g, b, a), 0.0F, -size, -size, length, size, size, false, false);
            }
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawLines(BufferBuilder builder, Camera camera, float partialTicks, Player player, List<List<StarData>> pairs, float alpha)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, WHITE);

        float r = Config.COMMON.constellationsRed.get().floatValue();
        float g = Config.COMMON.constellationsGreen.get().floatValue();
        float b = Config.COMMON.constellationsBlue.get().floatValue();

        RenderSystem.setShaderColor(r, g, b, alpha);

        double fov = RenderEventHandler.fov;
        double distance = Config.COMMON.constellationLineDistance.get();
        double modifier = RenderEventHandler.fieldOfViewModifier;
        float width = (float) (Config.COMMON.constellationLineWidth.get().floatValue() * (!RenderEventHandler.isScoping ? modifier : modifier + 0.2F));

        for (List<StarData> stars : pairs)
        {
            poseStack.pushPose();
            Vec3 vec1 = stars.get(0).getCachedAdjPos(false).normalize().scale(distance);
            Vec3 vec2 = stars.get(1).getCachedAdjPos(false).normalize().scale(distance);

            if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, vec1, fov) || AHelpers.isWithinAngle(RenderEventHandler.lookAngle, vec2, fov))
            {
                Vec3 direction = vec2.subtract(vec1).normalize();
                poseStack.translate(vec1.x(), vec1.y(), vec1.z());
                poseStack.mulPose(getRotationToAlign(direction, new Vec3(1.0F, 0.0F, 0.0F)));
                float length = (float) vec1.distanceTo(vec2);

                renderTexturedCuboid(poseStack, builder, LightTexture.FULL_SKY, new Color(r, g, b, alpha), 0.0F, -width, -width, length, width, width, false, false);
            }
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawCometTail(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, Vec3 sunPos, Vec3 direction, Vec3 directionToSun, float quadSize, int glow)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        double distanceToSunAU = sunPos.distanceTo(pos) / Config.COMMON.planetDistanceFactor.get();
        float distanceFactor = (float) (1.0D - AHelpers.cubicEaseInNorm(distanceToSunAU, 0.0D, 1.0D, 2.5D, 5.0D, 2.0D));

        float outerQuadSize = quadSize * distanceFactor * 4096.0F;
        float size = (float) (outerQuadSize / quadSize);

        Quaterniond rotation = new Quaterniond();
        rotation.rotateTo(AHelpers.toVector3d(direction), AHelpers.toVector3d(directionToSun));

        //poseStack.mulPose(AHelpers.toQuaternionf(rotation));
        poseStack.translate(pos.x(), pos.y(), pos.z());

        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; ++i)
        {
            Vector3f vector3 = vertices[i];
            //vector3.mulDirection(normalPlane);
            vector3.rotate(AHelpers.toQuaternionf(rotation));
            vector3.mul(size);
        }

        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float a = 1.0F * distanceFactor;

        renderTexturedQuadDouble(builder, poseStack, vertices, r, g, b, a, glow);

        /*Quaternionf axisRotation = new Quaternionf();
        axisRotation.rotateAxis((float) Math.toRadians(Config.COMMON.skyboxAxisRotation.get()), directionToSun.toVector3f());
        for (int i = 0; i < 4; ++i)
        {
            Vector3f vector3 = vertices[i];
            vector3.rotate(axisRotation);
        }
        renderTexturedQuadDouble(buffer, poseStack, vertices, r, g, b, a, glow);*/

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawStellarObject(BufferBuilder builder, Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, float quadSize, float roll, float oRoll, float alpha)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        poseStack.translate(pos.x(), pos.y(), pos.z());

        Quaternionf quaternion;
        if (roll == 0.0F)
        {
            quaternion = camera.rotation();
        }
        else
        {
            quaternion = new Quaternionf(camera.rotation());
            quaternion.rotateZ(Mth.lerp(partialTicks, oRoll, roll));
        }

        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; ++i)
        {
            Vector3f vector3 = vertices[i];
            vector3.rotate(quaternion);
            vector3.mul(quadSize);
        }

        float r = 1.0F * alpha;
        float g = 1.0F * alpha;
        float b = 1.0F * alpha;

        renderTexturedQuad(builder, poseStack, vertices, r, g, b, alpha, LightTexture.FULL_BRIGHT);

        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawStar(BufferBuilder builder, Camera camera, float partialTicks, PoseStack poseStack, ResourceLocation texture, Vec3 pos, Vec3 color, double randomRotation, float quadSize, float roll, float oRoll, float alpha, boolean fancyTexture, float v0, float v1)
    {
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);

        poseStack.translate(pos.x(), pos.y(), pos.z());

        Quaternionf quaternion;
        if (roll == 0.0F)
        {
            quaternion = camera.rotation();
        }
        else
        {
            quaternion = new Quaternionf(camera.rotation());
            quaternion.rotateZ(Mth.lerp(partialTicks, oRoll, roll));
        }

        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0F, -1.0F, 0.0F),
            new Vector3f(-1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, 1.0F, 0.0F),
            new Vector3f(1.0F, -1.0F, 0.0F)
        };

        for (int i = 0; i < 4; ++i)
        {
            Vector3f vector3 = vertices[i];
            vector3.rotate(Axis.ZP.rotationDegrees((float) randomRotation));
            vector3.rotate(quaternion);
            vector3.mul(quadSize);
        }

        float r = (float) color.x() * alpha;
        float g = (float) color.y() * alpha;
        float b = (float) color.z() * alpha;

        renderTexturedQuad(builder, poseStack, vertices, r, g, b, alpha, v0, v1, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawSkybox(BufferBuilder builder, Camera camera, float partialTicks, List<StarData> stars, ResourceLocation texture, float timeOfDay, double minMagnitude, float roll, float oRoll)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        //poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        //poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        double k = Math.pow(2.512D, (minMagnitude - 5.2D) / 2.4D);
        float h = (float) (k * Config.COMMON.DSObrightness.get() * 0.2D);
        float v = (float) AHelpers.getDeepskyVisibility();
        float a = Mth.clamp(v * h, 0.0F, 1.0F);
        float quadSize = Config.COMMON.skyboxDistance.get().floatValue();
        float size = quadSize / 2.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, a);

        //rotatePoseStack(poseStack);

        float i = (float) OBSERVER_INCLINATION;
        float s = (float) OBSERVER_SEASON;
        float l = (float) RenderEventHandler.latitude;
        float d = (float) OBSERVER_TIME_OF_DAY;

        poseStack.mulPose(Axis.ZP.rotation(d));
        poseStack.mulPose(Axis.XP.rotationDegrees(270.0F));
        poseStack.mulPose(Axis.ZP.rotation(i));
        poseStack.mulPose(Axis.XP.rotation(-l + s));

        Vec3 camPos = camera.getPosition();
        poseStack.translate(camPos.x(), camPos.y(), camPos.z());
        poseStack.translate(-size, -size, -size);
        poseStack.scale(quadSize, quadSize, quadSize);

        poseStack.pushPose();
        renderBlockModel(builder, ABlocks.CELESTIAL_BODY.get().defaultBlockState().setValue(CelestialBodyBlock.CELESTIAL_BODY, "skybox"), poseStack, LightTexture.FULL_SKY, RenderType.translucent(), a);
        poseStack.popPose();

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.end());
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderTexturedCuboid(PoseStack poseStack, VertexConsumer buffer, int packedLight, Color color, AABB bounds, boolean individualFaces, boolean insideOut)
    {
        renderTexturedCuboid(poseStack, buffer, packedLight, color, (float) bounds.minX, (float) bounds.minY, (float) bounds.minZ, (float) bounds.maxX, (float) bounds.maxY, (float) bounds.maxZ, individualFaces, insideOut);
    }

    public static void renderTexturedCuboid(PoseStack poseStack, VertexConsumer buffer, int packedLight, Color color, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, boolean individualFaces, boolean insideOut)
    {
        renderTexturedCuboid(poseStack, buffer, packedLight, color, minX, minY, minZ, maxX, maxY, maxZ, 16f * (maxX - minX), 16f * (maxY - minY), 16f * (maxZ - minZ), individualFaces, insideOut);
    }

    public static void renderTexturedCuboid(PoseStack poseStack, VertexConsumer buffer, int packedLight, Color color, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float xPixels, float yPixels, float zPixels, boolean individualFaces, boolean insideOut)
    {
        float[][] xV = individualFaces ? getXVerticesEW(minX, minY, minZ, maxX, maxY, maxZ, insideOut) : getXVertices(minX, minY, minZ, maxX, maxY, maxZ);
        float[][] yV = individualFaces ? getYVerticesUD(minX, minY, minZ, maxX, maxY, maxZ, insideOut) : getYVertices(minX, minY, minZ, maxX, maxY, maxZ);
        float[][] zV = individualFaces ? getZVerticesNS(minX, minY, minZ, maxX, maxY, maxZ, insideOut) : getZVertices(minX, minY, minZ, maxX, maxY, maxZ);
        renderTexturedQuads(poseStack, buffer, packedLight, color, xV, zPixels, yPixels, 1.0F, 0.0F, 0.0F);
        renderTexturedQuads(poseStack, buffer, packedLight, color, yV, zPixels, xPixels, 0.0F, 1.0F, 0.0F);
        renderTexturedQuads(poseStack, buffer, packedLight, color, zV, xPixels, yPixels, 0.0F, 0.0F, 1.0F);
    }

    public static void renderTexturedQuads(PoseStack poseStack, VertexConsumer buffer, int packedLight, Color color, float[][] vertices, float uSize, float vSize, float normalX, float normalY, float normalZ)
    {
        for (float[] v : vertices)
        {
            renderTexturedVertex(poseStack, buffer, color, v[0], v[1], v[2], packedLight, v[3], v[4], v[5] * normalX, v[5] * normalY, v[5] * normalZ);
        }
    }

    public static void renderTexturedVertex(PoseStack poseStack, VertexConsumer buffer, Color color, float x, float y, float z, int packedLight, float u, float v, float normalX, float normalY, float normalZ)
    {
        renderTexturedVertex(poseStack, buffer, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), x, y, z, packedLight, u, v, normalX, normalY, normalZ);
    }

    public static void renderTexturedVertex(PoseStack poseStack, VertexConsumer buffer, float r, float g, float b, float a, float x, float y, float z, int packedLight, float u, float v, float normalX, float normalY, float normalZ)
    {
        buffer.vertex(poseStack.last().pose(), x, y, z)
            .uv(u, v)
            .color(r, g, b, a)
            .uv2(packedLight)
            .normal(poseStack.last().normal(), normalX, normalY, normalZ)
            .endVertex();
    }

    public static void renderTexturedVertexBlock(PoseStack poseStack, VertexConsumer buffer, float r, float g, float b, float a, float x, float y, float z, int packedLight, int combinedOverlay, float u, float v, float normalX, float normalY, float normalZ)
    {
        buffer.vertex(poseStack.last().pose(), x, y, z)
            .color(r, g, b, a)
            .uv(u, v)
            .uv2(packedLight)
            .normal(poseStack.last().normal(), normalX, normalY, normalZ)
            .overlayCoords(combinedOverlay)
            .endVertex();
    }

    public static float[][] getXVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][] {
            {minX, minY, minZ, 0.0F, 1.0F, 1.0F}, // +X
            {minX, minY, maxZ, 1.0F, 1.0F, 1.0F},
            {minX, maxY, maxZ, 1.0F, 0.0F, 1.0F},
            {minX, maxY, minZ, 0.0F, 0.0F, 1.0F},

            {maxX, minY, maxZ, 1.0F, 0.0F, -1.0F}, // -X
            {maxX, minY, minZ, 0.0F, 0.0F, -1.0F},
            {maxX, maxY, minZ, 0.0F, 1.0F, -1.0F},
            {maxX, maxY, maxZ, 1.0F, 1.0F, -1.0F}
        };
    }

    public static float[][] getYVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][] {
            {minX, maxY, minZ, 0.0F, 1.0F, 1.0F}, // +Y
            {minX, maxY, maxZ, 1.0F, 1.0F, 1.0F},
            {maxX, maxY, maxZ, 1.0F, 0.0F, 1.0F},
            {maxX, maxY, minZ, 0.0F, 0.0F, 1.0F},

            {minX, minY, maxZ, 1.0F, 0.0F, -1.0F}, // -Y
            {minX, minY, minZ, 0.0F, 0.0F, -1.0F},
            {maxX, minY, minZ, 0.0F, 1.0F, -1.0F},
            {maxX, minY, maxZ, 1.0F, 1.0F, -1.0F}
        };
    }

    public static float[][] getZVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][] {
            {maxX, minY, minZ, 0.0F, 1.0F, 1.0F}, // +Z
            {minX, minY, minZ, 1.0F, 1.0F, 1.0F},
            {minX, maxY, minZ, 1.0F, 0.0F, 1.0F},
            {maxX, maxY, minZ, 0.0F, 0.0F, 1.0F},

            {minX, minY, maxZ, 1.0F, 0.0F, -1.0F}, // -Z
            {maxX, minY, maxZ, 0.0F, 0.0F, -1.0F},
            {maxX, maxY, maxZ, 0.0F, 1.0F, -1.0F},
            {minX, maxY, maxZ, 1.0F, 1.0F, -1.0F}
        };
    }

    public static float[][] getXVerticesEW(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, boolean insideOut)
    {
        if (insideOut)
        {
            return new float[][] {
                {minX, minY, minZ, 0.25F, 1.0F, 1.0F}, // +X
                {minX, minY, maxZ, 0.00F, 1.0F, 1.0F},
                {minX, maxY, maxZ, 0.00F, 0.5F, 1.0F},
                {minX, maxY, minZ, 0.25F, 0.5F, 1.0F},

                {maxX, minY, maxZ, 0.75F, 1.0F, -1.0F}, // -X
                {maxX, minY, minZ, 0.50F, 1.0F, -1.0F},
                {maxX, maxY, minZ, 0.50F, 0.5F, -1.0F},
                {maxX, maxY, maxZ, 0.75F, 0.5F, -1.0F}
            };
        }
        return new float[][] {
            {minX, minY, minZ, 0.25F, 1.0F, 1.0F}, // +X
            {minX, minY, maxZ, 0.00F, 1.0F, 1.0F},
            {minX, maxY, maxZ, 0.00F, 0.5F, 1.0F},
            {minX, maxY, minZ, 0.25F, 0.5F, 1.0F},

            {maxX, minY, maxZ, 0.75F, 1.0F, -1.0F}, // -X
            {maxX, minY, minZ, 0.50F, 1.0F, -1.0F},
            {maxX, maxY, minZ, 0.50F, 0.5F, -1.0F},
            {maxX, maxY, maxZ, 0.75F, 0.5F, -1.0F}
        };
    }

    public static float[][] getYVerticesUD(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, boolean insideOut)
    {
        if (insideOut)
        {
            return new float[][] {
                {minX, maxY, minZ, 0.25F, 0.5F, 1.0F}, // +Y
                {minX, maxY, maxZ, 0.25F, 0.0F, 1.0F},
                {maxX, maxY, maxZ, 0.50F, 0.0F, 1.0F},
                {maxX, maxY, minZ, 0.50F, 0.5F, 1.0F},

                {minX, minY, maxZ, 0.50F, 0.5F, -1.0F}, // -Y
                {minX, minY, minZ, 0.50F, 0.0F, -1.0F},
                {maxX, minY, minZ, 0.75F, 0.0F, -1.0F},
                {maxX, minY, maxZ, 0.75F, 0.5F, -1.0F}
            };
        }
        return new float[][] {
            {minX, maxY, minZ, 0.25F, 0.5F, 1.0F}, // +Y
            {minX, maxY, maxZ, 0.25F, 0.0F, 1.0F},
            {maxX, maxY, maxZ, 0.50F, 0.0F, 1.0F},
            {maxX, maxY, minZ, 0.50F, 0.5F, 1.0F},

            {minX, minY, maxZ, 0.50F, 0.5F, -1.0F}, // -Y
            {minX, minY, minZ, 0.50F, 0.0F, -1.0F},
            {maxX, minY, minZ, 0.75F, 0.0F, -1.0F},
            {maxX, minY, maxZ, 0.75F, 0.5F, -1.0F}
        };
    }

    public static float[][] getZVerticesNS(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, boolean insideOut)
    {
        if (insideOut)
        {
            return new float[][] {
                {maxX, minY, minZ, 0.50F, 1.0F, 1.0F}, // +Z
                {minX, minY, minZ, 0.25F, 1.0F, 1.0F},
                {minX, maxY, minZ, 0.25F, 0.5F, 1.0F},
                {maxX, maxY, minZ, 0.50F, 0.5F, 1.0F},

                {minX, minY, maxZ, 1.00F, 1.0F, -1.0F}, // -Z
                {maxX, minY, maxZ, 0.75F, 1.0F, -1.0F},
                {maxX, maxY, maxZ, 0.75F, 0.5F, -1.0F},
                {minX, maxY, maxZ, 1.00F, 0.5F, -1.0F}
            };
        }
        return new float[][] {
            {maxX, minY, minZ, 0.50F, 1.0F, 1.0F}, // +Z
            {minX, minY, minZ, 0.25F, 1.0F, 1.0F},
            {minX, maxY, minZ, 0.25F, 0.5F, 1.0F},
            {maxX, maxY, minZ, 0.50F, 0.5F, 1.0F},

            {minX, minY, maxZ, 1.00F, 1.0F, -1.0F}, // -Z
            {maxX, minY, maxZ, 0.75F, 1.0F, -1.0F},
            {maxX, maxY, maxZ, 0.75F, 0.5F, -1.0F},
            {minX, maxY, maxZ, 1.00F, 0.5F, -1.0F}
        };
    }

    public static void renderTexturedQuad(VertexConsumer buffer, PoseStack poseStack, int glow, float r, float g, float b, float a, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4)
    {
        Color color = new Color(r, g, b, a);
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        renderTexturedVertex(poseStack, buffer, color, x1, y1, z1, glow, u0, v1, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, x2, y2, z2, glow, u0, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, x3, y3, z3, glow, u1, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, x4, y4, z4, glow, u1, v1, 0.0F, 1.0F, 0.0F);
    }

    public static void renderTexturedQuad(VertexConsumer buffer, PoseStack poseStack, Vector3f[] vertices, float r, float g, float b, float a, int glow)
    {
        renderTexturedQuad(buffer, poseStack, vertices, r, g, b, a, 0.0F, 1.0F, glow);
    }

    public static void renderTexturedQuad(VertexConsumer buffer, PoseStack poseStack, Vector3f[] vertices, float r, float g, float b, float a, float v0, float v1, int glow)
    {
        Color color = new Color(r, g, b, a);
        float u0 = 0.0F;
        float u1 = 1.0F;

        renderTexturedVertex(poseStack, buffer, color, vertices[0].x(), vertices[0].y(), vertices[0].z(), glow, u0, v1, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[1].x(), vertices[1].y(), vertices[1].z(), glow, u0, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[2].x(), vertices[2].y(), vertices[2].z(), glow, u1, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[3].x(), vertices[3].y(), vertices[3].z(), glow, u1, v1, 0.0F, 1.0F, 0.0F);
    }

    public static void renderTexturedQuadDouble(VertexConsumer buffer, PoseStack poseStack, Vector3f[] vertices, float r, float g, float b, float a, int glow)
    {
        Color color = new Color(r, g, b, a);
        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        renderTexturedVertex(poseStack, buffer, color, vertices[0].x(), vertices[0].y(), vertices[0].z(), glow, u0, v1, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[1].x(), vertices[1].y(), vertices[1].z(), glow, u0, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[2].x(), vertices[2].y(), vertices[2].z(), glow, u1, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[3].x(), vertices[3].y(), vertices[3].z(), glow, u1, v1, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[3].x(), vertices[3].y(), vertices[3].z(), glow, u1, v1, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[2].x(), vertices[2].y(), vertices[2].z(), glow, u1, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[1].x(), vertices[1].y(), vertices[1].z(), glow, u0, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(poseStack, buffer, color, vertices[0].x(), vertices[0].y(), vertices[0].z(), glow, u0, v1, 0.0F, 1.0F, 0.0F);
    }

    public static Quaternionf getRotationToAlign(Vec3 target, Vec3 forward)
    {
        Vec3 targetNorm = target.normalize();
        Vec3 forwardNorm = forward.normalize();
        Vec3 axis = forwardNorm.cross(targetNorm);

        // Handle case when the axis vector is zero (target and forward are parallel)
        if (axis.length() < 1e-6)
        {
            // Return identity quaternion if vectors are parallel or anti-parallel
            return forwardNorm.dot(targetNorm) > 0
                ? new Quaternionf() // Identity quaternion for same direction
                : new Quaternionf(0, 1, 0, 0); // 180-degree flip for opposite direction
        }

        axis = axis.normalize(); // Normalize axis for rotation
        double angle = Math.acos(forwardNorm.dot(targetNorm)); // Compute angle between vectors

        // Construct quaternion
        float sinHalfAngle = (float) Math.sin(angle / 2.0);
        return new Quaternionf(
            (float) axis.x() * sinHalfAngle,
            (float) axis.y() * sinHalfAngle,
            (float) axis.z() * sinHalfAngle,
            (float) Math.cos(angle / 2.0)
        );
    }

    public static void renderBlockModel(BlockState state, PoseStack poseStack, int packedLight, RenderType renderType)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.getBlockRenderer().renderSingleBlock(state, poseStack, mc.renderBuffers().bufferSource(), packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
    }

    public static void renderBlockModel(VertexConsumer buffer, BlockState state, PoseStack poseStack, int packedLight, RenderType renderType, float alpha)
    {
        renderBlockModel(buffer, state, poseStack, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType, alpha);
    }

    public static void renderBlockModel(VertexConsumer buffer, BlockState state, PoseStack poseStack, int packedLight, int packedOverlay, ModelData modelData, RenderType renderType, float alpha)
    {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher renderer = mc.getBlockRenderer();
        BlockRenderDispatcherAccessor accessor = (BlockRenderDispatcherAccessor) renderer;
        if (state.getRenderShape() == RenderShape.MODEL)
        {
            // Get the transformation matrix for vertex positions and normals
            PoseStack.Pose pose = poseStack.last();
            Matrix4f positionMatrix = pose.pose();
            Matrix3f normalMatrix = pose.normal();

            BakedModel model = renderer.getBlockModel(state);
            int colors = accessor.getBlockColors().getColor(state, (BlockAndTintGetter) null, (BlockPos) null, 0);
            float red = (float)(colors >> 16 & 255) / 255.0F;
            float green = (float)(colors >> 8 & 255) / 255.0F;
            float blue = (float)(colors & 255) / 255.0F;

            for (RenderType type : model.getRenderTypes(state, RandomSource.create(42), modelData))
            {
                renderModel(buffer, positionMatrix, normalMatrix, state, model, modelData, type, red, green, blue, alpha, packedLight, packedOverlay);
            }
        }
    }

    public static void renderModel(VertexConsumer consumer, Matrix4f positionMatrix, Matrix3f normalMatrix, @Nullable BlockState state, BakedModel model, ModelData modelData, RenderType renderType, float red, float green, float blue, float alpha, int packedLight, int packedOverlay)
    {
        RandomSource random = RandomSource.create();
        long seed = 42L;
        for (Direction direction : Direction.values())
        {
            random.setSeed(seed);
            renderQuads(consumer, positionMatrix, normalMatrix, model.getQuads(state, direction, random, modelData, renderType), red, green, blue, alpha, packedLight, packedOverlay);
        }
        random.setSeed(seed);
        renderQuads(consumer, positionMatrix, normalMatrix, model.getQuads(state, (Direction)null, random, modelData, renderType), red, green, blue, alpha, packedLight, packedOverlay);
    }

    public static void renderQuads(VertexConsumer consumer, Matrix4f positionMatrix, Matrix3f normalMatrix, List<BakedQuad> quads, float red, float green, float blue, float alpha, int light, int overlay)
    {
        for (BakedQuad quad : quads)
        {
            // Get vertex data
            int[] vertexData = quad.getVertices();
            int vertexCount = vertexData.length / 8; // 8 integers per vertex (position, color, UV, light, normal)

            // Determine vertex color
            float finalRed = 1.0F;
            float finalGreen = 1.0F;
            float finalBlue = 1.0F;
            float finalAlpha = alpha;

            // Check if the quad is tinted
            if (quad.isTinted())
            {
                // Multiply the provided RGB with the tint color
                finalRed *= red;
                finalGreen *= green;
                finalBlue *= blue;
            }

            for (int i = 0; i < vertexCount; i++)
            {
                int offset = i * 8;

                // Extract position (x, y, z)
                float x = Float.intBitsToFloat(vertexData[offset]);
                float y = Float.intBitsToFloat(vertexData[offset + 1]);
                float z = Float.intBitsToFloat(vertexData[offset + 2]);

                // Extract UV coordinates
                float u = Float.intBitsToFloat(vertexData[offset + 4]);
                float v = Float.intBitsToFloat(vertexData[offset + 5]);

                // Extract normal
                int normalInt = vertexData[offset + 7];
                float nx = ((byte) (normalInt & 0xFF)) / 127.0f;
                float ny = ((byte) ((normalInt >> 8) & 0xFF)) / 127.0f;
                float nz = ((byte) ((normalInt >> 16) & 0xFF)) / 127.0f;

                // Transform position
                Vector4f pos = new Vector4f(x, y, z, 1.0f);
                pos.mul(positionMatrix);

                // Transform normal
                Vector3f normal = new Vector3f(nx, ny, nz);
                normal.mul(normalMatrix);

                // Emit vertex to the VertexConsumer with final color
                consumer.vertex(pos.x(), pos.y(), pos.z())
                            .color(finalRed, finalGreen, finalBlue, finalAlpha) // Apply tinted or provided RGB
                            .uv(u, v)
                            .uv2(light)
                            .overlayCoords(overlay)
                            .normal(normal.x(), normal.y(), normal.z())
                            .endVertex();
            }
        }
    }

    public static Vector3d getNormal()
    {
        switch (Config.COMMON.axisIndex.get())
        {
            case 1:
                return new Vector3d(0.0F, 1.0F, 0.0F);
            case 2:
                return new Vector3d(0.0F, 0.0F, 1.0F);
            case 3:
                return new Vector3d(-1.0F, 0.0F, 0.0F);
            case 4:
                return new Vector3d(0.0F, -1.0F, 0.0F);
            case 5:
                return new Vector3d(0.0F, 0.0F, -1.0F);
            default:
                return new Vector3d(1.0F, 0.0F, 0.0F);
        }
    }

    public static Quaternionf getRotation(float rotation)
    {
        switch (Config.COMMON.skyboxAxisIndex.get())
        {
            case 1:
                return Axis.YP.rotation(rotation);
            case 2:
                return Axis.ZP.rotation(rotation);
            case 3:
                return Axis.XN.rotation(rotation);
            case 4:
                return Axis.YN.rotation(rotation);
            case 5:
                return Axis.ZN.rotation(rotation);
            default:
                return Axis.XP.rotation(rotation);
        }
    }

    public static void rotatePoseStack(PoseStack poseStack)
    {
        float x = (float) Math.toRadians(Config.COMMON.skyboxXRotation.get());
        float y = (float) Math.toRadians(Config.COMMON.skyboxZRotation.get());
        float z = (float) Math.toRadians(Config.COMMON.skyboxYRotation.get());
        switch (Config.COMMON.skyboxAxisIndex.get())
        {
            case 0:
                poseStack.mulPose(Axis.XP.rotation(x));
                poseStack.mulPose(Axis.ZP.rotation(z));
                poseStack.mulPose(Axis.YP.rotation(y));
            case 1:
                poseStack.mulPose(Axis.YP.rotation(y));
                poseStack.mulPose(Axis.XP.rotation(x));
                poseStack.mulPose(Axis.ZP.rotation(z));
            case 2:
                poseStack.mulPose(Axis.YP.rotation(y));
                poseStack.mulPose(Axis.ZP.rotation(z));
                poseStack.mulPose(Axis.XP.rotation(x));
            case 3:
                poseStack.mulPose(Axis.ZP.rotation(z));
                poseStack.mulPose(Axis.XP.rotation(x));
                poseStack.mulPose(Axis.YP.rotation(y));
            case 4:
                poseStack.mulPose(Axis.ZP.rotation(z));
                poseStack.mulPose(Axis.YP.rotation(y));
                poseStack.mulPose(Axis.XP.rotation(x));
            default:
                poseStack.mulPose(Axis.XP.rotation(x));
                poseStack.mulPose(Axis.YP.rotation(y));
                poseStack.mulPose(Axis.ZP.rotation(z));
        }
    }

    public static Vector4f switchAxes(Vector4f vec)
    {
        float x = vec.x();
        float y = vec.y();
        float z = vec.z();
        float w = vec.w();
        switch (Config.COMMON.skyboxAxisIndex.get())
        {
            case 0:
                return new Vector4f(x, z, y, w);
            case 1:
                return new Vector4f(y, x, z, w);
            case 2:
                return new Vector4f(y, z, x, w);
            case 3:
                return new Vector4f(z, x, y, w);
            case 4:
                return new Vector4f(z, y, x, w);
            default:
                return new Vector4f(x, y, z, w);
        }
    }

    public static Vector3f switchAxes(Vector3f vec)
    {
        float x = vec.x();
        float y = vec.y();
        float z = vec.z();
        switch (Config.COMMON.skyboxAxisIndex.get())
        {
            case 0:
                return new Vector3f(x, z, y);
            case 1:
                return new Vector3f(y, x, z);
            case 2:
                return new Vector3f(y, z, x);
            case 3:
                return new Vector3f(z, x, y);
            case 4:
                return new Vector3f(z, y, x);
            default:
                return new Vector3f(x, y, z);
        }
    }
}
