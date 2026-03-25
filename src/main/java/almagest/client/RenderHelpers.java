package almagest.client;

import java.util.List;
import java.util.Set;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import almagest.client.blocks.ABlocks;
import almagest.client.blocks.DayCycleBlock;
import almagest.client.particle.CelestialObject;
import almagest.client.particle.Constellations;
import almagest.client.particle.ICelestialObject;
import almagest.client.particle.Skybox;
import almagest.client.particle.Star;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.Color;
import almagest.util.FastMath;
import almagest.util.MutableVec3;
import almagest.util.PlanetHelpers;

@SuppressWarnings("null")
public class RenderHelpers
{
    public static final ResourceLocation WHITE = AHelpers.identifier("textures/particles/white.png");
    public static final ResourceLocation STAR = AHelpers.identifier("textures/block/stars/star.png");
    public static final ResourceLocation CUBE_DEBUG = AHelpers.identifier("textures/cube.png");

    public static final RandomSource RANDOM_42 = RandomSource.create(42L);
    public static final float[] DEFAULT_COLOR_MUL = {1.0F, 1.0F, 1.0F, 1.0F};
    public static final Color WHITE_COLOR = new Color(1.0D, 1.0D, 1.0D, 1.0D);
    public static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

    public static final Vector3f[] QUAD_CACHE = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};

    public static final void noTransparency() {RenderSystem.disableBlend();}
    public static final void additiveTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);}
    public static final void lightningTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);}
    public static final void glintTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);}
    public static final void crumblingTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);}
    public static final void translucentTransparency() {RenderSystem.enableBlend(); RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);}

    public static void drawRing(Tesselator tess, CelestialObject obj)
    {
        BufferBuilder builder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, obj.ringTexture);

        Vec3 p = obj.getPos();
        poseStack.translate((float)p.x, (float)p.y, (float)p.z);
        poseStack.mulPose(obj.getOrientationMatrix());

        Pose pose = poseStack.last();

        float minRadius = (float) obj.body.getRing().getInnerRadius();
        float maxRadius = (float) obj.body.getRing().getOuterRadius();
        int segments = 32;

        int r = 255;
        int g = 255;
        int b = 255;
        int a = 255;

        double step = 2.0 * Math.PI / segments;
        float cosStep = (float) Math.cos(step);
        float sinStep = (float) Math.sin(step);

        float xInner = minRadius;
        float zInner = 0f;
        float xOuter = maxRadius;
        float zOuter = 0f;

        for (int i = 0; i < segments; i++)
        {
            float xInner2 = xInner * cosStep - zInner * sinStep;
            float zInner2 = xInner * sinStep + zInner * cosStep;

            float xOuter2 = xOuter * cosStep - zOuter * sinStep;
            float zOuter2 = xOuter * sinStep + zOuter * cosStep;

            renderTexturedVertex(pose, builder, r, g, b, a, xInner, 0f, zInner, LightTexture.FULL_BRIGHT, 0f, 1f, 0f, 1f, 0f);
            renderTexturedVertex(pose, builder, r, g, b, a, xInner2, 0f, zInner2, LightTexture.FULL_BRIGHT, 0f, 0f, 0f, 1f, 0f);
            renderTexturedVertex(pose, builder, r, g, b, a, xOuter2, 0f, zOuter2, LightTexture.FULL_BRIGHT, 1f, 0f, 0f, 1f, 0f);
            renderTexturedVertex(pose, builder, r, g, b, a, xOuter, 0f, zOuter, LightTexture.FULL_BRIGHT, 1f, 1f, 0f, 1f, 0f);

            xInner = xInner2;
            zInner = zInner2;
            xOuter = xOuter2;
            zOuter = zOuter2;
        }

        poseStack.popPose();
        MeshData mesh = builder.build();
        if (mesh != null)
        {
            BufferUploader.drawWithShader(mesh);
        }
        RenderSystem.disableBlend();
    }

    public static void drawBodyFlat(Camera camera, float partialTicks, ICelestialObject object)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, AHelpers.identifier(object.getTextureNameFlat()));
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        Vec3 pos = object.getPos();
        poseStack.translate(pos.x(), pos.y(), pos.z());
        float diameter = (float) object.getDiameter();
        float size = diameter / 2.0F;

        Quaternionf quaternion = camera.rotation();

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

        int[] color = object.getColor().toArrayRGBA();
        int r = color[0];
        int g = color[1];
        int b = color[2];
        int a = color[3];
        renderTexturedQuad(builder, poseStack.last(), vertices, r, g, b, a, object.getPackedLight());

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawBody(BufferBuilder builder, Camera camera, float partialTicks, ICelestialObject object)
    {
        PoseStack poseStack = new PoseStack();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Vec3 p = object.getPos();
        poseStack.translate((float)p.x, (float)p.y, (float)p.z);
        poseStack.mulPose(object.getOrientationMatrix());

        renderBlockModel(builder, object.getBlockModel(), poseStack, LightTexture.FULL_BRIGHT, RenderType.cutout(), object.getColor());

        RenderSystem.disableBlend();
    }

    public static void drawBodyDayCycle(Camera camera, float partialTicks, ICelestialObject object)
    {
        RenderHelpers.translucentTransparency();
        PoseStack poseStack = new PoseStack(); //MatrixHelpers.toPoseStack(RenderSystem.getModelViewStack());
        RenderSystem.applyModelViewMatrix();
        poseStack.pushPose();

        RenderSystem.depthMask(true);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) object.getInclination()));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) object.getObliquity()));
        poseStack.mulPose(Axis.YN.rotationDegrees((float) object.getRotationCompletion()));
        Vec3 pos = object.getPos();
        poseStack.translate(pos.x(), pos.y(), pos.z());

        float diameter = (float) object.getDiameter();
        float size = diameter / 1.95F;
        poseStack.translate(-size, -size, -size);
        poseStack.scale(diameter, diameter, diameter);

        renderBlockModel(builder, ABlocks.DAY_CYCLE.get().defaultBlockState().setValue(DayCycleBlock.TIME, (int) object.getDayNightCycle().y()).setValue(DayCycleBlock.OBLIQUITY, (int) Math.round((object.getDayNightCycle().x() + 180.0F) / 2.5F)), poseStack, object.getPackedLight(), RenderType.translucentMovingBlock());

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawCelestialBodyNoOverlay(Camera camera, float partialTicks, ResourceLocation texture, String name, Vec3 pos, float quadSize, int glow)
    {
        PoseStack poseStack = new PoseStack(); //MatrixHelpers.toPoseStack(RenderSystem.getModelViewStack());
        RenderSystem.applyModelViewMatrix();
        poseStack.pushPose();

        //RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        poseStack.translate(pos.x(), pos.y(), pos.z());

        float size = quadSize / 2.0F;
        poseStack.translate(-size, -size, -size);
        poseStack.scale(quadSize, quadSize, quadSize);

        poseStack.pushPose();
        //renderBlockModel(ABlocks.CELESTIAL_BODY.get().defaultBlockState().setValue(CelestialBodyBlock.CELESTIAL_BODY, name), poseStack, glow, RenderType.translucentMovingBlock());
        //renderBlockModel(Blocks.DIRT.defaultBlockState(), poseStack, glow, RenderType.translucentMovingBlock());
        poseStack.popPose();

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawCometTailBody(Camera camera, float partialTicks, ResourceLocation texture, Color color, Vec3 pos, Vec3 direction, Vec3 directionToSun, float tailSize, float outerTailSize, int glow)
    {
        PoseStack poseStack = new PoseStack(); //MatrixHelpers.toPoseStack(RenderSystem.getModelViewStack());
        RenderSystem.applyModelViewMatrix();
        poseStack.pushPose();

        //RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        //RenderSystem.setShaderTexture(0, texture);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        poseStack.translate(pos.x(), pos.y(), pos.z());
        poseStack.translate(-tailSize, -tailSize, -tailSize);
        poseStack.scale(outerTailSize, outerTailSize, outerTailSize);

        /*Quaterniond rotation = new Quaterniond();
        rotation.mulPoseTo(AHelpers.toVector3d(direction), AHelpers.toVector3d(directionToSun));
        poseStack.mulPose(AHelpers.toQuaternionf(rotation));*/

        poseStack.pushPose();
        //renderBlockModel(ABlocks.CELESTIAL_BODY.get().defaultBlockState().setValue(CelestialBodyBlock.CELESTIAL_BODY, "comet"), poseStack, glow, RenderType.translucentMovingBlock());
        renderBlockModel(builder, Blocks.DIRT.defaultBlockState(), poseStack, glow, RenderType.translucentMovingBlock());
        poseStack.popPose();

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawOrbit(BufferBuilder builder, Camera camera, float partialTicks, ICelestialObject object, boolean fadeAlpha)
    {
        final List<List<Vec3>> orbitPositions = object.getOrbitPositions();
        if (orbitPositions.isEmpty())
            return;

        final int segCount = orbitPositions.size();
        final Color baseColor = object.getType().getOrbitColor();

        final double fov = CelestialObjectHandler.fovLim;
        final float width = (float)(object.getOrbitLineWidth() * CelestialObjectHandler.lineScale);

        final Vector3f camDir = camera.getLookVector();
        final Vec3 camPos = camera.getPosition();

        final MutableVec3 dir = new MutableVec3();
        final MutableVec3 side = new MutableVec3();

        final Vector3f[] quad = QUAD_CACHE;
        final Pose pose = new PoseStack().last();

        final double fadeInner = 0.0;
        final double fadeOuter = 48.0;

        int idx = 0;
        for (List<Vec3> pair : orbitPositions)
        {
            if (pair.size() < 2)
            {
                idx++;
                continue;
            }

            final Vec3 v1 = pair.get(0);
            final Vec3 v2 = pair.get(1);

            if (!AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, v1, fov) && !AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, v2, fov))
            {
                idx++;
                continue;
            }

            float indexFade = fadeAlpha ? (0.1f + 0.9f * (1.0f - (idx / (float)segCount))) : baseColor.alpha();

            double minX = Math.min(v1.x, v2.x) - width;
            double maxX = Math.max(v1.x, v2.x) + width;
            double minY = Math.min(v1.y, v2.y) - width;
            double maxY = Math.max(v1.y, v2.y) + width;
            double minZ = Math.min(v1.z, v2.z) - width;
            double maxZ = Math.max(v1.z, v2.z) + width;

            double dx = (camPos.x < minX) ? (minX - camPos.x) : (camPos.x > maxX) ? (camPos.x - maxX) : 0.0;
            double dy = (camPos.y < minY) ? (minY - camPos.y) : (camPos.y > maxY) ? (camPos.y - maxY) : 0.0;
            double dz = (camPos.z < minZ) ? (minZ - camPos.z) : (camPos.z > maxZ) ? (camPos.z - maxZ) : 0.0;

            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

            if (dist <= fadeInner)
            {
                idx++;
                continue;
            }

            double t = (dist - fadeInner) / (fadeOuter - fadeInner);
            t = Mth.clamp(t, 0.0, 1.0);
            float distanceFade = (float)t;

            float alpha = indexFade * distanceFade;
            if (alpha <= 0.0f)
            {
                idx++;
                continue;
            }

            int[] colors = baseColor.set(baseColor, alpha).toArrayRGBA();
            int r = colors[0], g = colors[1], b = colors[2], a = colors[3];

            dir.set(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z).normalizeInPlace();

            side.set(
                camDir.y * dir.z - camDir.z * dir.y,
                camDir.z * dir.x - camDir.x * dir.z,
                camDir.x * dir.y - camDir.y * dir.x
            ).normalizeInPlace().scaleInPlace(width);

            final double sx = side.x, sy = side.y, sz = side.z;

            quad[0].set((float)(v1.x + sx), (float)(v1.y + sy), (float)(v1.z + sz));
            quad[3].set((float)(v1.x - sx), (float)(v1.y - sy), (float)(v1.z - sz));
            quad[2].set((float)(v2.x - sx), (float)(v2.y - sy), (float)(v2.z - sz));
            quad[1].set((float)(v2.x + sx), (float)(v2.y + sy), (float)(v2.z + sz));

            RenderHelpers.renderTexturedQuad(builder, pose, quad, r, g, b, a, LightTexture.FULL_BRIGHT);

            idx++;
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    public static void drawConstellation(BufferBuilder builder, Camera camera, float partialTicks, Constellations constellation)
    {
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, WHITE);

        final double fov = CelestialObjectHandler.fovLim;
        final float width = (float)(Config.COMMON.constellationLineWidth.get() * CelestialObjectHandler.lineScale);

        final Vector3f camDir = camera.getLookVector();

        final MutableVec3 dir = new MutableVec3();
        final MutableVec3 side = new MutableVec3();

        final Vector3f[] quad = QUAD_CACHE;

        final Pose pose = new PoseStack().last();

        int[] colors = constellation.color.toArrayRGBA();
        int r = colors[0];
        int g = colors[1];
        int b = colors[2];
        int a = colors[3];

        for (Constellations.LineSegment seg : constellation.renderSegments)
        {
            final Vec3 start = seg.start;

            Vector3f endF = new Vector3f(seg.length, 0, 0);
            endF.rotate(seg.rotation);

            final Vec3 end = new Vec3(
                start.x + endF.x,
                start.y + endF.y,
                start.z + endF.z
            );

            if (!AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, start, fov) && !AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, end, fov))
                continue;

            dir.set(end.x - start.x, end.y - start.y, end.z - start.z).normalizeInPlace();

            side.set(
                camDir.y * dir.z - camDir.z * dir.y,
                camDir.z * dir.x - camDir.x * dir.z,
                camDir.x * dir.y - camDir.y * dir.x
            ).normalizeInPlace().scaleInPlace(width);

            final double sx = side.x, sy = side.y, sz = side.z;

            final double x1 = start.x, y1 = start.y, z1 = start.z;
            final double x2 = end.x,   y2 = end.y,   z2 = end.z;

            quad[0].set((float)(x1 + sx), (float)(y1 + sy), (float)(z1 + sz));
            quad[3].set((float)(x1 - sx), (float)(y1 - sy), (float)(z1 - sz));
            quad[2].set((float)(x2 - sx), (float)(y2 - sy), (float)(z2 - sz));
            quad[1].set((float)(x2 + sx), (float)(y2 + sy), (float)(z2 + sz));

            RenderHelpers.renderTexturedQuad(builder, pose, quad, r, g, b, a, LightTexture.FULL_BRIGHT);
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    public static void drawLines(BufferBuilder builder, Camera camera, double lineWidth, List<Vec3> positions, Color color)
    {
        final int segCount = CelestialObjectHandler.ECLIPTIC_SEGMENTS;

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, WHITE);

        final double fov = CelestialObjectHandler.fovLim;
        final float width = (float)(lineWidth * CelestialObjectHandler.lineScale);

        final Vector3f camDir = camera.getLookVector();

        final MutableVec3 dir = new MutableVec3();
        final MutableVec3 side = new MutableVec3();

        final Vector3f[] quad = QUAD_CACHE;

        final Pose pose = new PoseStack().last();

        int[] colors = color.toArrayRGBA();
        int r = colors[0];
        int g = colors[1];
        int b = colors[2];
        int a = colors[3];

        for (int i = 0; i < segCount; i++)
        {
            final int j = (i + 1 == segCount ? 0 : i + 1);

            final Vec3 v1 = positions.get(i);
            final Vec3 v2 = positions.get(j);

            if (!AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, v1, fov) && !AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, v2, fov))
                continue;

            dir.set(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z).normalizeInPlace();

            side.set(
                camDir.y * dir.z - camDir.z * dir.y,
                camDir.z * dir.x - camDir.x * dir.z,
                camDir.x * dir.y - camDir.y * dir.x
            ).normalizeInPlace().scaleInPlace(width);

            final double sx = side.x, sy = side.y, sz = side.z;

            final double x1 = v1.x, y1 = v1.y, z1 = v1.z;
            final double x2 = v2.x, y2 = v2.y, z2 = v2.z;

            quad[0].set((float)(x1 + sx), (float)(y1 + sy), (float)(z1 + sz));
            quad[3].set((float)(x1 - sx), (float)(y1 - sy), (float)(z1 - sz));
            quad[2].set((float)(x2 - sx), (float)(y2 - sy), (float)(z2 - sz));
            quad[1].set((float)(x2 + sx), (float)(y2 + sy), (float)(z2 + sz));

            RenderHelpers.renderTexturedQuad(builder, pose, quad, r, g, b, a, LightTexture.FULL_BRIGHT);
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    public static void drawStellarObject(Camera camera, float partialTicks, ResourceLocation texture, Vec3 pos, Color color, float quadSize, float alpha)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        poseStack.translate(pos.x(), pos.y(), pos.z());

        Quaternionf quaternion = camera.rotation();

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

        int[] colors = color.toArrayRGBA();
        int r = colors[0];
        int g = colors[1];
        int b = colors[2];
        int a = colors[3];
        renderTexturedQuad(builder, poseStack.last(), vertices, r, g, b, a, LightTexture.FULL_BRIGHT);

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    /*public static void drawStar(BufferBuilder builder, Star star)
    {
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.mulPose(star.transformationMatrix);

        Color color = star.color;
        RenderSystem.setShaderColor(color.red(), color.green(), color.blue(), color.alpha());

        renderCameraFacingQuad(builder, poseStack.last(), (float) star.size, star.colorsRGBA, LightTexture.FULL_BRIGHT);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        poseStack.popPose();
    }*/

    public static void drawStar(VertexConsumer buffer, Star s)
    {
        renderBillboardQuad(
            buffer,
            s.p0x, s.p0y, s.p0z,
            s.p1x, s.p1y, s.p1z,
            s.p2x, s.p2y, s.p2z,
            s.p3x, s.p3y, s.p3z,
            s.c[0], s.c[1], s.c[2], s.c[3],
            0f, 0f, 1f,
            LightTexture.FULL_BRIGHT
        );
    }

    public static void drawSkybox(BufferBuilder builder, Camera camera, float partialTicks, Skybox skybox)
    {
        if (skybox.color.a < 0.01F) return;

        PoseStack poseStack = new PoseStack();
        RenderSystem.applyModelViewMatrix();

        float quadSize = Config.COMMON.skyboxDistance.get().floatValue();
        float size = quadSize / 2.0F;

        RenderSystem.setShaderColor(skybox.color.r, skybox.color.g, skybox.color.b, skybox.color.a);

        poseStack.mulPose(CelestialObjectHandler.skyboxRotationMatrix);
        poseStack.mulPose(Axis.XP.rotationDegrees(270.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        poseStack.translate(size, size, size);
        poseStack.scale(-quadSize, -quadSize, -quadSize);

        renderBlockModel(builder, skybox.getBlockModel(), poseStack, LightTexture.FULL_BRIGHT, RenderType.translucentMovingBlock(), skybox.color);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderQuad(VertexConsumer buffer, Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3, float[] uv, int r,int g,int b,int a, int light, float nx, float ny, float nz)
    {
        float u0 = uv[0], v0u = uv[1];
        float u1 = uv[2], v1u = uv[3];

        renderTexturedVertex(buffer, v0, r, g, b, a, light, u0, v1u, nx, ny, nz);
        renderTexturedVertex(buffer, v1, r, g, b, a, light, u1, v1u, nx, ny, nz);
        renderTexturedVertex(buffer, v2, r, g, b, a, light, u1, v0u, nx, ny, nz);
        renderTexturedVertex(buffer, v3, r, g, b, a, light, u0, v0u, nx, ny, nz);
    }

    public static void renderCameraFacingQuad(VertexConsumer buffer, Pose pose, float size, int[] color, int packedLight)
    {
        Vector3f[] verts = new Vector3f[]
        {
            new Vector3f(-size, -size, 0),
            new Vector3f( size, -size, 0),
            new Vector3f( size,  size, 0),
            new Vector3f(-size,  size, 0)
        };

        Vector3f v0 = verts[0];
        Vector3f v1 = verts[1];
        Vector3f v2 = verts[2];
        Vector3f v3 = verts[3];

        Vector3f normal = pose.transformNormal(0f, 1f, 0f, new Vector3f());
        float nX = normal.x();
        float nY = normal.y();
        float nZ = normal.z();

        int r = color[0];
        int g = color[1];
        int b = color[2];
        int a = color[3];

        renderTexturedVertex(pose, buffer, r, g, b, a, v0.x(), v0.y(), v0.z(), packedLight, 0f, 1f, nX, nY, nZ);
        renderTexturedVertex(pose, buffer, r, g, b, a, v1.x(), v1.y(), v1.z(), packedLight, 0f, 0f, nX, nY, nZ);
        renderTexturedVertex(pose, buffer, r, g, b, a, v2.x(), v2.y(), v2.z(), packedLight, 1f, 0f, nX, nY, nZ);
        renderTexturedVertex(pose, buffer, r, g, b, a, v3.x(), v3.y(), v3.z(), packedLight, 1f, 1f, nX, nY, nZ);
    }

    public static void renderTexturedCuboid(Pose pose, VertexConsumer buffer, int packedLight, int r, int g, int b, int a, float size)
    {
        renderTexturedCuboid(pose, buffer, packedLight, r, g, b, a, -size, -size, -size, size, size, size);
    }

    public static void renderTexturedCuboid(Pose pose, VertexConsumer buffer, int packedLight, int r, int g, int b, int a, float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        renderTexturedQuads(pose, buffer, packedLight, r, g, b, a, getXVertices(minX, minY, minZ, maxX, maxY, maxZ), 1.0F, 0.0F, 0.0F);
        renderTexturedQuads(pose, buffer, packedLight, r, g, b, a, getYVertices(minX, minY, minZ, maxX, maxY, maxZ), 0.0F, 1.0F, 0.0F);
        renderTexturedQuads(pose, buffer, packedLight, r, g, b, a, getZVertices(minX, minY, minZ, maxX, maxY, maxZ), 0.0F, 0.0F, 1.0F);
    }

    public static void renderTexturedQuads(Pose pose, VertexConsumer buffer, int packedLight, int r, int g, int b, int a, float[][] vertices, float normalX, float normalY, float normalZ)
    {
        for (float[] v : vertices)
        {
            renderTexturedVertex(pose, buffer, r, g, b, a, v[0], v[1], v[2], packedLight, v[3], v[4], v[5] * normalX, v[5] * normalY, v[5] * normalZ);
        }
    }

    public static void renderTexturedVertex(Pose pose, VertexConsumer buffer, int r, int g, int b, int a, float x, float y, float z, int packedLight, float u, float v, float normalX, float normalY, float normalZ)
    {
        buffer.addVertex(pose, x, y, z)
            .setUv(u, v)
            .setColor(r, g, b, a)
            .setLight(packedLight)
            .setNormal(pose, normalX, normalY, normalZ);
    }

    public static void renderBillboardQuad(VertexConsumer buffer, Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3, int r, int g, int b, int a, float nX, float nY, float nZ, int packedLight)
    {
        renderTexturedVertex(buffer, v0, r, g, b, a, packedLight, 0f, 1f, nX, nY, nZ);
        renderTexturedVertex(buffer, v1, r, g, b, a, packedLight, 0f, 0f, nX, nY, nZ);
        renderTexturedVertex(buffer, v2, r, g, b, a, packedLight, 1f, 0f, nX, nY, nZ);
        renderTexturedVertex(buffer, v3, r, g, b, a, packedLight, 1f, 1f, nX, nY, nZ);
    }

    public static void renderBillboardQuad(VertexConsumer buffer, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a, float nX, float nY, float nZ, int packedLight)
    {
        renderTexturedVertex(buffer, x0, y0, z0, r, g, b, a, packedLight, 0f, 1f, nX, nY, nZ);
        renderTexturedVertex(buffer, x1, y1, z1, r, g, b, a, packedLight, 0f, 0f, nX, nY, nZ);
        renderTexturedVertex(buffer, x2, y2, z2, r, g, b, a, packedLight, 1f, 0f, nX, nY, nZ);
        renderTexturedVertex(buffer, x3, y3, z3, r, g, b, a, packedLight, 1f, 1f, nX, nY, nZ);
    }

    public static void renderTexturedVertex(VertexConsumer buffer, Vec3 pos, int r, int g, int b, int a, int packedLight, float u, float v, float nX, float nY, float nZ)
    {
        renderTexturedVertex(buffer, (float) pos.x, (float) pos.y, (float) pos.z, r, g, b, a, packedLight, u, v, nX, nY, nZ);
    }

    public static void renderTexturedVertex(VertexConsumer buffer, float x, float y, float z, int r, int g, int b, int a, int packedLight, float u, float v, float nX, float nY, float nZ)
    {
        buffer.addVertex(x, y, z)
            .setUv(u, v)
            .setColor(r, g, b, a)
            .setLight(packedLight)
            .setNormal(nX, nY, nZ);
    }

    public static float[][] getXVertices(float size)
    {
        return getXVertices(-size, -size, -size, size, size, size);
    }

    public static float[][] getYVertices(float size)
    {
        return getYVertices(-size, -size, -size, size, size, size);
    }

    public static float[][] getZVertices(float size)
    {
        return getZVertices(-size, -size, -size, size, size, size);
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

    public static void renderTexturedQuad(VertexConsumer buffer, Pose pose, Vector3f[] vertices, int r, int g, int b, int a, int glow)
    {
        renderTexturedQuad(buffer, pose, vertices, r, g, b, a, 0.0F, 1.0F, glow);
    }

    public static void renderTexturedQuad(VertexConsumer buffer, Pose pose, Vector3f[] vertices, int r, int g, int b, int a, float v0, float v1, int glow)
    {
        float u0 = 0.0F;
        float u1 = 1.0F;

        renderTexturedVertex(pose, buffer, r, g, b, a, vertices[0].x(), vertices[0].y(), vertices[0].z(), glow, u0, v1, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(pose, buffer, r, g, b, a, vertices[1].x(), vertices[1].y(), vertices[1].z(), glow, u0, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(pose, buffer, r, g, b, a, vertices[2].x(), vertices[2].y(), vertices[2].z(), glow, u1, v0, 0.0F, 1.0F, 0.0F);
        renderTexturedVertex(pose, buffer, r, g, b, a, vertices[3].x(), vertices[3].y(), vertices[3].z(), glow, u1, v1, 0.0F, 1.0F, 0.0F);
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
        double angle = FastMath.acos(forwardNorm.dot(targetNorm)); // Compute angle between vectors

        // Construct quaternion
        float sinHalfAngle = (float) Math.sin(angle / 2.0);
        return new Quaternionf(
            (float) axis.x() * sinHalfAngle,
            (float) axis.y() * sinHalfAngle,
            (float) axis.z() * sinHalfAngle,
            (float) Math.cos(angle / 2.0)
        );
    }

    public static void renderBlockModel(VertexConsumer consumer, BlockState state, PoseStack poseStack, int packedLight, RenderType renderType)
    {
        renderBlockModel(consumer, state, poseStack, packedLight, renderType, WHITE_COLOR);
    }

    public static void renderBlockModel(VertexConsumer consumer, BlockState state, PoseStack poseStack, int packedLight, RenderType renderType, Color color)
    {
        renderBlockModel(consumer, state, poseStack.last(), packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType, color, Set.of(Direction.values()));
    }

    public static void renderBlockModel(VertexConsumer consumer, BlockState state, PoseStack poseStack, int packedLight, RenderType renderType, Color color, Set<Direction> renderDirs)
    {
        renderBlockModel(consumer, state, poseStack.last(), packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType, color, renderDirs);
    }

    public static void renderBlockModel(VertexConsumer consumer, BlockState state, Pose pose, int packedLight, RenderType renderType, Color color, Set<Direction> renderDirs)
    {
        renderBlockModel(consumer, state, pose, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType, color, renderDirs);
    }

    public static void renderBlockModel(VertexConsumer consumer, BlockState state, Pose pose, int packedLight, int packedOverlay, ModelData modelData, RenderType renderType, Color color, Set<Direction> renderDirs)
    {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher renderer = mc.getBlockRenderer();
        if (state.getRenderShape() == RenderShape.MODEL)
        {
            BakedModel model = renderer.getBlockModel(state);

            for (RenderType type : model.getRenderTypes(state, RANDOM_42, modelData))
            {
                renderModel(consumer, pose, state, model, modelData, type, color, packedLight, packedOverlay, renderDirs);
            }
        }
    }

    public static void renderModel(VertexConsumer consumer, Pose pose, BlockState state, BakedModel model, ModelData modelData, RenderType renderType, Color color, int packedLight, int packedOverlay, Set<Direction> renderDirs)
    {
        int[] combinedLight = new int[]{packedLight, packedLight, packedLight, packedLight};
        renderQuads(consumer, pose, model.getQuads(state, null, RANDOM_42, modelData, renderType), color, combinedLight, DEFAULT_COLOR_MUL, packedOverlay, renderDirs);
    }

    public static void renderQuads(VertexConsumer consumer, Pose pose, List<BakedQuad> quads, Color color, int[] light, float[] colorMul, int overlay, Set<Direction> renderDirs)
    {
        float r = color.red(), g = color.green(), b = color.blue(), a = color.alpha();
        for (BakedQuad quad : quads)
        {
            if (renderDirs.contains(quad.getDirection()))
            {
                consumer.putBulkData(pose, quad, colorMul, r, g, b, a, light, overlay, false);
            }
        }
    }

    public static void renderModelStar(BufferBuilder consumer, Pose pose, List<BakedQuad> quads, Color color, int packedLight)
    {
        int[] combinedLight = new int[]{packedLight, packedLight, packedLight, packedLight};
        renderStarQuads(consumer, pose, quads, color, combinedLight, DEFAULT_COLOR_MUL, OverlayTexture.NO_OVERLAY);
    }

    public static void renderStarQuads(BufferBuilder consumer, Pose pose, List<BakedQuad> quads, Color color, int[] light, float[] colorMul, int overlay)
    {
        float r = color.red(), g = color.green(), b = color.blue(), a = color.alpha();
        for (BakedQuad quad : quads)
        {
            consumer.putBulkData(pose, quad, colorMul, r, g, b, a, light, overlay, false);
        }
    }

    public static Direction getCfgDirection()
    {
        return Direction.values()[Config.COMMON.axisIndex.get()];
    }

    public static Quaternionf getRotation(float rotation)
    {
        switch (Config.COMMON.skyboxAxisIndex.get())
        {
            case 0:
                return Axis.XP.rotation(rotation);
            case 1:
                return Axis.YP.rotation(rotation);
            case 2:
                return Axis.ZP.rotation(rotation);
            case 3:
                return Axis.XN.rotation(rotation);
            case 4:
                return Axis.YN.rotation(rotation);
            default:
                return Axis.ZN.rotation(rotation);
        }
    }

    public static Quaternionf getRotation1()
    {
        float rotation = (float) Math.toRadians(Config.COMMON.skyboxAxisRotation.get());
        switch (Config.COMMON.skyboxAxisIndex.get())
        {
            case 0:
                return Axis.XP.rotation(rotation);
            case 1:
                return Axis.YP.rotation(rotation);
            case 2:
                return Axis.ZP.rotation(rotation);
            case 3:
                return Axis.XN.rotation(rotation);
            case 4:
                return Axis.YN.rotation(rotation);
            default:
                return Axis.ZN.rotation(rotation);
        }
    }

    public static Quaternionf getRotation2()
    {
        float rotation = (float) Math.toRadians(Config.COMMON.skyboxAxisRotation2.get());
        switch (Config.COMMON.skyboxAxisIndex2.get())
        {
            case 0:
                return Axis.XP.rotation(rotation);
            case 1:
                return Axis.YP.rotation(rotation);
            case 2:
                return Axis.ZP.rotation(rotation);
            case 3:
                return Axis.XN.rotation(rotation);
            case 4:
                return Axis.YN.rotation(rotation);
            default:
                return Axis.ZN.rotation(rotation);
        }
    }

    public static Quaternionf getRotation3()
    {
        float rotation = (float) Math.toRadians(Config.COMMON.skyboxAxisRotation3.get());
        switch (Config.COMMON.skyboxAxisIndex3.get())
        {
            case 0:
                return Axis.XP.rotation(rotation);
            case 1:
                return Axis.YP.rotation(rotation);
            case 2:
                return Axis.ZP.rotation(rotation);
            case 3:
                return Axis.XN.rotation(rotation);
            case 4:
                return Axis.YN.rotation(rotation);
            default:
                return Axis.ZN.rotation(rotation);
        }
    }

    public static void mulPosePoseStack(PoseStack poseStack)
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

    public static Vector3f switchAxes(float x, float y, float z)
    {
        switch (Config.COMMON.axisIndex.get())
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

    public static Vec3 switchAxes(double x, double y, double z)
    {
        switch (Config.COMMON.axisIndex.get())
        {
            case 1:
                return new Vec3(x, z, y);
            case 2:
                return new Vec3(y, x, z);
            case 3:
                return new Vec3(y, z, x);
            case 4:
                return new Vec3(z, x, y);
            case 5:
                return new Vec3(z, y, x);
            default:
                return new Vec3(x, y, z);
        }
    }

    public static Vec3 getRotation(Vec3 vec)
    {
        double rotation = Math.toRadians(Config.COMMON.skyboxAxisRotation2.get());
        switch (Config.COMMON.skyboxAxisIndex2.get())
        {
            case 0:
                return PlanetHelpers.rotX(rotation, vec);
            case 1:
                return PlanetHelpers.rotY(rotation, vec);
            case 2:
                return PlanetHelpers.rotZ(rotation, vec);
            case 3:
                return PlanetHelpers.rotX(-rotation, vec);
            case 4:
                return PlanetHelpers.rotY(-rotation, vec);
            default:
                return PlanetHelpers.rotZ(-rotation, vec);
        }
    }

    public static void rotateZ(Vector3f v, float cos, float sin)
    {
        float x = v.x;
        float y = v.y;
        v.x = x * cos - y * sin;
        v.y = x * sin + y * cos;
    }
}
