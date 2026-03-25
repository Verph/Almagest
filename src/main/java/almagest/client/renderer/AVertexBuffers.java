package almagest.client.renderer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderHelpers;
import almagest.client.particle.Star;
import almagest.config.Config;

public class AVertexBuffers
{
    public static final VertexBuffer[][] STAR_BUCKET_VBOS   = new VertexBuffer[CelestialObjectHandler.NUM_BUCKETS][];
    public static final int[][]          STAR_BUCKET_COUNTS = new int[CelestialObjectHandler.NUM_BUCKETS][];

    public static void rebuildStarBuckets(Tesselator tess)
    {
        final int maxStarsPerVBO = Config.COMMON.starVBOSize.get();

        for (int bucket = CelestialObjectHandler.NUM_BUCKETS - 1; bucket >= 0; bucket--)
        {
            ObjectOpenHashSet<Star>[] drawSubs = CelestialObjectHandler.STAR_BUCKETS_DRAW_SUB[bucket];
            boolean[] dirtySubs = CelestialObjectHandler.DIRTY_SUB_BUCKETS[bucket];

            if (drawSubs == null || dirtySubs == null)
                continue;

            int subCount = drawSubs.length;
            if (subCount == 0)
                continue;

            if (STAR_BUCKET_VBOS[bucket] == null || STAR_BUCKET_VBOS[bucket].length != subCount)
            {
                if (STAR_BUCKET_VBOS[bucket] != null)
                {
                    for (VertexBuffer vbo : STAR_BUCKET_VBOS[bucket])
                        if (vbo != null) vbo.close();
                }

                STAR_BUCKET_VBOS[bucket]   = new VertexBuffer[subCount];
                STAR_BUCKET_COUNTS[bucket] = new int[subCount];
            }

            for (int sub = 0; sub < subCount; sub++)
            {
                if (!dirtySubs[sub])
                    continue;

                dirtySubs[sub] = false;

                ObjectOpenHashSet<Star> stars = drawSubs[sub];
                if (stars == null || stars.isEmpty())
                {
                    if (STAR_BUCKET_VBOS[bucket][sub] != null)
                    {
                        STAR_BUCKET_VBOS[bucket][sub].close();
                        STAR_BUCKET_VBOS[bucket][sub] = null;
                    }

                    STAR_BUCKET_COUNTS[bucket][sub] = 0;
                    continue;
                }

                Star[] starArray = stars.toArray(new Star[0]);
                int totalStars   = starArray.length;

                int vboCount = (totalStars + maxStarsPerVBO - 1) / maxStarsPerVBO;

                if (vboCount != 1)
                    vboCount = 1;

                BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

                int drawn = 0;
                for (int i = 0; i < totalStars; i++)
                {
                    Star star = starArray[i];
                    if (star == null || !star.shouldDraw) continue;

                    RenderHelpers.drawStar(buf, star);
                    drawn++;
                }

                MeshData mesh = buf.build();

                if (mesh == null || drawn == 0)
                {
                    if (STAR_BUCKET_VBOS[bucket][sub] != null)
                    {
                        STAR_BUCKET_VBOS[bucket][sub].close();
                        STAR_BUCKET_VBOS[bucket][sub] = null;
                    }

                    STAR_BUCKET_COUNTS[bucket][sub] = 0;
                    continue;
                }

                VertexBuffer vbo = STAR_BUCKET_VBOS[bucket][sub];
                if (vbo == null)
                {
                    vbo = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                    STAR_BUCKET_VBOS[bucket][sub] = vbo;
                }

                vbo.bind();
                vbo.upload(mesh);
                VertexBuffer.unbind();

                STAR_BUCKET_COUNTS[bucket][sub] = drawn;
            }
        }
    }

    public static void clearStarVBOs()
    {
        for (int bucket = 0; bucket < STAR_BUCKET_VBOS.length; bucket++)
        {
            VertexBuffer[] sub = STAR_BUCKET_VBOS[bucket];
            if (sub == null) continue;

            for (VertexBuffer vbo : sub)
            {
                if (vbo != null) vbo.close();
            }

            STAR_BUCKET_VBOS[bucket]   = null;
            STAR_BUCKET_COUNTS[bucket] = null;
        }
    }

    public static void renderStarBuckets()
    {
        ShaderInstance shader = GameRenderer.getParticleShader();
        if (shader == null) return;

        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.enableBlend();
        RenderHelpers.translucentTransparency();
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, RenderHelpers.STAR);

        for (int bucket = 0; bucket < CelestialObjectHandler.NUM_BUCKETS; bucket++)
        {
            VertexBuffer[] subVbos = STAR_BUCKET_VBOS[bucket];
            if (subVbos == null) continue;

            for (VertexBuffer vbo : subVbos)
            {
                if (vbo == null) continue;

                vbo.bind();
                vbo.drawWithShader(
                    RenderSystem.getModelViewMatrix(),
                    RenderSystem.getProjectionMatrix(),
                    shader
                );
            }
        }

        VertexBuffer.unbind();
    }
}