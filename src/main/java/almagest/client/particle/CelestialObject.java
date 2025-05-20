package almagest.client.particle;

import java.util.Map;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderEventHandler;
import almagest.client.RenderHelpers;
import almagest.config.Config;
import almagest.mixin.client.ParticleAccessor;
import almagest.util.AHelpers;

@SuppressWarnings("null")
public class CelestialObject extends TextureSheetParticle
{
    public final ClientLevel level;
    public final Player player;
    public final BufferBuilder builder;

    public CelestialObject(ClientLevel level, Player player)
    {
        super(level, 0.0D, 0.0D, 0.0D);
        this.level = level;
        this.player = player;
        this.builder = Tesselator.getInstance().getBuilder();
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.setBoundingBox(IForgeBlockEntity.INFINITE_EXTENT_AABB);
    }

    @Override
    public void tick()
    {
        if (AHelpers.RELOAD_STAR_DATA.isDown())
        {
            for (CelestialObject celestialObject : CelestialObjectHandler.CELESTIAL_OBJECTS)
            {
                celestialObject.remove();
            }
            CelestialObjectHandler.CELESTIAL_OBJECTS.clear();
            RenderEventHandler.HAS_INITIALIZED_CELESTIAL_OBJECTS = false;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        for (Map.Entry<Integer, Planet> entry : CelestialObjectHandler.PLANET_OBJECTS_CACHE.entrySet())
        {
            Planet object = entry.getValue();
            double orbitLineWidth = Config.COMMON.planetOrbitLineWidth.get();
            if (!object.isSun && Config.COMMON.showPlanetOrbits.get() && orbitLineWidth > 0.0D)
            {
                RenderHelpers.drawOrbit(builder, camera, partialTicks, level, orbitLineWidth, object.parentPos, object.colorOrbit, object.segments, object.id, object.time, object.orbit, object.eccentricity, object.ascendingNode, object.periapsis, object.semimajorAxis, object.meanLongitude, object.inclination, object.wobble, object.obliquityRotation);
            }
        }
        for (Map.Entry<Integer, MinorPlanet> entry : CelestialObjectHandler.MINOR_PLANET_OBJECTS_CACHE.entrySet())
        {
            MinorPlanet object = entry.getValue();
            double orbitLineWidth = Config.COMMON.minorPlanetOrbitLineWidth.get();
            if (Config.COMMON.showMinorPlanetOrbits.get() && orbitLineWidth > 0.0D && object.realDiameter >= Config.COMMON.minorPlanetMinSizeForOrbitLine.get())
            {
                RenderHelpers.drawOrbit(builder, camera, partialTicks, level, orbitLineWidth, object.parentPos, object.colorOrbit, object.segments, object.id, object.time, object.orbit, object.eccentricity, object.ascendingNode, object.periapsis, object.semimajorAxis, object.meanLongitude, object.inclination, object.wobble, object.obliquityRotation);
            }
        }
        for (Map.Entry<Integer, Moon> entry : CelestialObjectHandler.MOON_OBJECTS_CACHE.entrySet())
        {
            Moon object = entry.getValue();
            double orbitLineWidth = Config.COMMON.moonOrbitLineWidth.get();
            if (Config.COMMON.showMoonOrbits.get() && orbitLineWidth > 0.0D && object.realDiameter >= Config.COMMON.moonMinSizeForOrbitLine.get())
            {
                RenderHelpers.drawOrbit(builder, camera, partialTicks, level, orbitLineWidth, object.parentPos, object.colorOrbit, object.segments, object.id, object.time, object.orbit, object.eccentricity, object.ascendingNode, object.periapsis, object.semimajorAxis, object.meanLongitude, object.inclination, object.wobble, object.obliquityRotation);
            }
        }
        for (Map.Entry<Integer, Comet> entry : CelestialObjectHandler.COMET_OBJECTS_CACHE.entrySet())
        {
            Comet object = entry.getValue();
            double orbitLineWidth = Config.COMMON.cometOrbitLineWidth.get();
            if (Config.COMMON.showCometOrbits.get() && orbitLineWidth > 0.0D && object.realDiameter >= Config.COMMON.cometMinSizeForOrbitLine.get())
            {
                RenderHelpers.drawOrbit(builder, camera, partialTicks, level, orbitLineWidth, object.parentPos, object.colorOrbit, object.segments, object.id, object.time, object.orbit, object.eccentricity, object.ascendingNode, object.periapsis, object.semimajorAxis, object.meanLongitude, object.inclination, 0.0D, object.obliquityRotation);
            }
        }
        for (Map.Entry<Integer, Meteor> entry : CelestialObjectHandler.METEOR_SHOWER_OBJECTS_CACHE.entrySet())
        {
            Meteor object = entry.getValue();
            double orbitLineWidth = Config.COMMON.meteorShowerOrbitLineWidth.get();
            if (object.meteorNumber == 0 && Config.COMMON.showMeteorShowerOrbits.get() && orbitLineWidth > 0.0D)
            {
                RenderHelpers.drawOrbitNoFade(builder, camera, partialTicks, level, orbitLineWidth, object.parentPos, object.colorOrbit, object.segments, object.id, object.time, object.parentOrbit, object.parentEccentricity, object.parentAscendingNode, object.parentPeriapsis, object.parentSemimajorAxis, object.parentMeanLongitude, object.parentInclination, 0.0D, AHelpers.ZERO_VEC);
            }
        }
        if (Config.COMMON.drawConstellations.get())
        {
            for (Map.Entry<Integer, Constellations> entry : CelestialObjectHandler.CONSTELLATIONS_OBJECTS_CACHE.entrySet())
            {
                Constellations constellation = entry.getValue();
                float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * constellation.alpha;
                if (alpha > 0.0F)
                {
                    RenderHelpers.drawLines(builder, camera, partialTicks, player, constellation.pairs, constellation.alpha);
                }
            }
        }
        BufferUploader.drawWithShader(builder.end());

        if (Config.COMMON.renderStars.get())
        {
            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

            for (Map.Entry<Integer, Star> entry : CelestialObjectHandler.STAR_OBJECTS_CACHE.entrySet())
            {
                Star star = entry.getValue();
                if (star.alphaAdj > 0.0F && AHelpers.getApparentSizeClient(star.pos, player, star.alphaAdj, Config.COMMON.minApparentSize.get()) && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, star.pos, RenderEventHandler.fov))
                {
                    RenderHelpers.drawStar(builder, camera, partialTicks, poseStack, star.texture, star.pos, star.color, star.randomRotation, star.quadSizeAdj, star.roll, star.oRoll, star.alphaAdj, star.fancyTexture, star.v0, star.v1);
                }
            }
            poseStack.popPose();
            BufferUploader.drawWithShader(builder.end());
        }
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull()
    {
        return false;
    }

    @Override
    public void setBoundingBox(AABB bb)
    {
        ((ParticleAccessor) this).setBB(IForgeBlockEntity.INFINITE_EXTENT_AABB);
    }
}
