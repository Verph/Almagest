package almagest.client.particle;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderEventHandler;
import almagest.client.data.MeteorSwarmDataManager.MeteorData;
import almagest.client.data.PlanetDataManager;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;
import static almagest.client.data.MeteorSwarmDataManager.*;
import static almagest.client.data.PlanetDataManager.*;

@SuppressWarnings("null")
public class Meteor extends CelestialObject
{
    public final BufferBuilder builder;
    public final int id;
    public final int meteorNumber;
    public final RandomSource random;
    public final ResourceLocation texture;
    public final ResourceLocation textureGlow;
    public final MeteorData meteor;
    public final String name;
    public final int parent;
    public final double diameter;
    public final double orbit;
    public final double eccentricity;
    public final double ascendingNode;
    public final double periapsis;
    public final double semimajorAxis;
    public final double meanLongitude;
    public final double inclination;
    public final double orbitOffset;
    public final Vec3 rotation;
    public final Vec3 obliquityRotation;

    public final int segments;

    public final Vec3 color;
    public final Vec3 colorOrbit;
    public Vec3 pos;
    public Vec3 sunPos;
    public Vec3 parentPos;
    public Vec3 P1;
    public Vec3 P2;
    public Vec3 P3;
    public Vec3 orbitalPlaneNormal;
    public Quaternionf orbitalRotation;
    public final int glow;
    public final Player player;
    public long time;

    public final double parentOrbit;
    public final double parentEccentricity;
    public final double parentAscendingNode;
    public final double parentPeriapsis;
    public final double parentSemimajorAxis;
    public final double parentMeanLongitude;
    public final double parentInclination;

    public Meteor(ClientLevel level, MeteorData meteor, Player player, int meteorNumber)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.meteor = meteor;
        this.name = meteor.names().name();
        this.id = meteor.getID();
        this.meteorNumber = meteorNumber;
        this.parent = PARENT_CACHE.getOrDefault(id, 0).intValue();

        this.random = RandomSource.create(meteorNumber);
        this.time = RenderEventHandler.time;

        this.diameter = METEOR_DIAMETER_CACHE.get(id).getOrDefault(meteorNumber, 1.0D).doubleValue();
        this.eccentricity = ECCENTRICITY_CACHE.get(id).doubleValue();
        this.ascendingNode = ASCENDING_NODE_LONGITUDE_CACHE.get(id).doubleValue();
        this.periapsis = PERIAPSIS_LONGITUDE_CACHE.get(id).doubleValue();
        this.semimajorAxis = METEOR_SEMIMAJOR_AXIS_CACHE.get(id).getOrDefault(meteorNumber, SEMIMAJOR_AXIS_CACHE.get(id)).doubleValue();
        this.orbit = METEOR_ORBIT_PERIOD_CACHE.get(id).getOrDefault(meteorNumber, PlanetDataManager.getOrbitalPeriod(semimajorAxis)).doubleValue();
        this.meanLongitude = MEAN_LONGITUDE_CACHE.get(id).doubleValue();
        this.inclination = INCLINATION_CACHE.get(id).doubleValue();
        this.obliquityRotation = new Vec3(-(PARENT_CACHE.get(id) == 0 ? 0.0D : Math.toRadians(OBLIQUITY_CACHE.getOrDefault(PARENT_CACHE.get(id), 0.0D))), 0.0D, 0.0D);

        this.texture = AHelpers.identifier(METEOR_TEXTURE_CACHE.get(id).get(meteorNumber) + ".png");
        this.textureGlow = AHelpers.identifier(METEOR_TEXTURE_CACHE.get(id).get(meteorNumber) + "_glow.png");
        this.rotation = METEOR_RANDOM_ROTATION_CACHE.get(id).get(meteorNumber).add(obliquityRotation);

        this.color = new Vec3(0.9D, 0.9D, 0.9D);
        this.colorOrbit = new Vec3(Config.COMMON.meteorShowerOrbitColorRed.get(), Config.COMMON.meteorShowerOrbitColorGreen.get(), Config.COMMON.meteorShowerOrbitColorBlue.get());
        this.quadSize = (float) diameter;
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = 1.0F;
        this.glow = Mth.clamp(Math.round(LightTexture.FULL_BRIGHT * 0.5F), 0, LightTexture.FULL_BRIGHT);

        this.orbitOffset = (orbit * random.nextDouble());
        double T1 = PlanetDataManager.getElapsedTime(time + orbitOffset, orbit);
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, T1, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, 0.0D, rotation, false, true, true);
        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);

        this.player = player;

        this.parentOrbit = ORBIT_PERIOD_CACHE.getOrDefault(id, AHelpers.convertYearsToTicks(PlanetDataManager.getOrbitalPeriod(SEMIMAJOR_AXIS_CACHE.get(id)) * Config.COMMON.planetOrbitFactor.get()));
        this.parentEccentricity = ECCENTRICITY_CACHE.getOrDefault(id, meteor.getAttributes().eccentricity());
        this.parentAscendingNode = ASCENDING_NODE_LONGITUDE_CACHE.getOrDefault(id, meteor.getAttributes().ascendingNodeLongitude());
        this.parentPeriapsis = PERIAPSIS_LONGITUDE_CACHE.getOrDefault(id, meteor.getAttributes().periapsisLongitude());
        this.parentSemimajorAxis = SEMIMAJOR_AXIS_CACHE.getOrDefault(id, meteor.getAttributes().semimajorAxis());
        this.parentMeanLongitude = MEAN_LONGITUDE_CACHE.getOrDefault(id, 0.0D);
        this.parentInclination = INCLINATION_CACHE.getOrDefault(id, meteor.getAttributes().inclination());

        this.segments = (int) Math.round(50 + Math.abs(meteor.getAttributes().semimajorAxis() * 10.0D));
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderComets.get()) return;
        super.tick();

        this.time = RenderEventHandler.time;
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, PlanetDataManager.getElapsedTime(time + orbitOffset, orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, 0.0D, rotation, false, true, true);
        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);

        if (Config.COMMON.displayMeteorShowerNames.get() && RenderEventHandler.gameTime % 20 == 0)
        {
            float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
            if (alpha > 0.0F && player != null && RenderEventHandler.isScoping)
            {
                if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, Config.COMMON.meteorShowerDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier, false))
                {
                    player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
                }
            }
        }
        CelestialObjectHandler.METEOR_SHOWER_CHILD_OBJECTS_CACHE.put(meteorNumber, this);
        if (this.meteorNumber == 0)
        {
            CelestialObjectHandler.METEOR_SHOWER_OBJECTS_CACHE.put(id, this);
        }
        if (!this.isAlive())
        {
            CelestialObjectHandler.METEOR_SHOWER_CHILD_OBJECTS_CACHE.remove(meteorNumber);
        }
        if (CelestialObjectHandler.METEOR_SHOWER_CHILD_OBJECTS_CACHE.isEmpty())
        {
            CelestialObjectHandler.METEOR_SHOWER_OBJECTS_CACHE.remove(id);
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderMeteorShowers.get()) return;

        float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
        if (alpha > 0.0F && pos != null && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov) && AHelpers.getApparentSizeClient(pos, player, this.getQuadSize(partialTicks), Config.COMMON.minApparentSize.get()))
        {
            drawBodyFlat(builder, camera, partialTicks, texture, pos, color, alpha, this.quadSize, this.roll, this.oRoll, glow);
            drawBodyFlat(builder, camera, partialTicks, textureGlow, pos, color, alpha, this.quadSize, this.roll, this.oRoll, glow);
        }
    }
}