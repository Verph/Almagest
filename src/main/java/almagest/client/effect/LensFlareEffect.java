package almagest.client.effect;

import java.util.stream.IntStream;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import almagest.client.particle.CelestialObject;
import almagest.util.AHelpers;

@SuppressWarnings("null")
public class LensFlareEffect
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

    public static final ResourceLocation BLINDNESS_OVERLAY =
        AHelpers.identifier("textures/environment/lense_flare/sun_blindness.png");

    public static void renderLensFlare(Camera camera, float partialTicks, CelestialObject celestialObject)
    {
        if (!celestialObject.isStar)
        {
            return;
        }

        // Apparent magnitude gating
        double appMag = AHelpers.getApparentMagnitudeForSpectator(
            celestialObject.player.position(),
            celestialObject.pos,
            celestialObject.body.getStar().getAbsoluteMagnitude()
        );
        if (appMag >= -14.0D)
        {
            return;
        }

        float magT = Mth.clamp((float)((-14.0D - appMag) / (-14.0D - (-28.0D))), 0.0F, 1.0F);
        float sizeScale = Mth.lerp(magT, 0.1F, 0.5F);
        float baseOverlayOpacity = Mth.lerp(magT, 0.0F, 0.3F);

        Vec3 cameraPos = camera.getPosition();
        Vec3 starPos = celestialObject.pos;
        Vec3 toStar = starPos.subtract(cameraPos);
        double distToStar = toStar.length();
        if (distToStar <= 0.0001D)
        {
            return;
        }

        Vec3 dir = toStar.scale(1.0D / distToStar);

        // Use angle between view direction and star direction to modulate opacity
        float dot = (float) camera.getLookVector().dot(dir.toVector3f().normalize());
        if (dot <= 0.0F)
        {
            return; // behind the camera
        }

        float centerDistance = Mth.sqrt(1.0F - dot * dot); // sin(theta)
        if (centerDistance > 0.7071F)
        {
            return;
        }

        float opacityScale = Mth.clamp(1.0F - centerDistance / 0.7071F, 0.0F, 1.0F);
        float flareOpacity = 0.8F * opacityScale;
        float overlayOpacity = baseOverlayOpacity * opacityScale;

        int packedLight = LightTexture.FULL_BRIGHT;

        // Blindness overlay (still screen-space style if you want it)
        if (overlayOpacity > 0.0F)
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, BLINDNESS_OVERLAY);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, overlayOpacity);

            Matrix4f ortho = new Matrix4f().setOrtho(0, 1, 1, 0, -1, 1);
            RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.getModelViewStack().identity();
            RenderSystem.applyModelViewMatrix();

            BufferBuilder builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.PARTICLE
            );
            builder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 0.0F).setColor(1F, 1F, 1F, overlayOpacity);
            builder.addVertex(0.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F).setColor(1F, 1F, 1F, overlayOpacity);
            builder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F).setColor(1F, 1F, 1F, overlayOpacity);
            builder.addVertex(1.0F, 0.0F, 0.0F).setUv(1.0F, 0.0F).setColor(1F, 1F, 1F, overlayOpacity);
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }

        if (flareOpacity <= 0.0F)
        {
            RenderSystem.disableBlend();
            return;
        }

        // Now render the flares as true particles in world space
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getParticleShader);

        Quaternionf camRot = camera.rotation();
        Vector3f right = new Vector3f(1, 0, 0).rotate(camRot);
        Vector3f up = new Vector3f(0, 1, 0).rotate(camRot);

        // Base distance in front of the camera along the star direction
        float baseDist = (float) Mth.clamp(distToStar, 4.0D, 64.0D);

        for (int i = 0; i < FLARES.length; i++)
        {
            float flareSize = FLARE_SIZES[i] * sizeScale;
            float influence = FLARE_INFLUENCES[i];

            // Position along the line camera -> star
            float flareDist = baseDist * (1.0F + influence);
            if (flareDist <= 0.0F)
            {
                continue;
            }

            Vec3 flarePosVec = cameraPos.add(dir.scale(flareDist));
            Vector3f center = new Vector3f(
                (float) flarePosVec.x(),
                (float) flarePosVec.y(),
                (float) flarePosVec.z()
            );

            float half = flareSize * 0.5F;

            Vector3f p0 = new Vector3f(center)
                .add(right.x * -half + up.x * -half,
                     right.y * -half + up.y * -half,
                     right.z * -half + up.z * -half);
            Vector3f p1 = new Vector3f(center)
                .add(right.x * -half + up.x * half,
                     right.y * -half + up.y * half,
                     right.z * -half + up.z * half);
            Vector3f p2 = new Vector3f(center)
                .add(right.x * half + up.x * half,
                     right.y * half + up.y * half,
                     right.z * half + up.z * half);
            Vector3f p3 = new Vector3f(center)
                .add(right.x * half + up.x * -half,
                     right.y * half + up.y * -half,
                     right.z * half + up.z * -half);

            RenderSystem.setShaderTexture(0, FLARES[i]);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, flareOpacity);

            BufferBuilder builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.PARTICLE
            );

            builder.addVertex(p0.x, p0.y, p0.z).setUv(0.0F, 0.0F).setColor(1F, 1F, 1F, flareOpacity).setLight(packedLight);
            builder.addVertex(p1.x, p1.y, p1.z).setUv(0.0F, 1.0F).setColor(1F, 1F, 1F, flareOpacity).setLight(packedLight);
            builder.addVertex(p2.x, p2.y, p2.z).setUv(1.0F, 1.0F).setColor(1F, 1F, 1F, flareOpacity).setLight(packedLight);
            builder.addVertex(p3.x, p3.y, p3.z).setUv(1.0F, 0.0F).setColor(1F, 1F, 1F, flareOpacity).setLight(packedLight);

            BufferUploader.drawWithShader(builder.buildOrThrow());
        }

        RenderSystem.disableBlend();
    }
}
