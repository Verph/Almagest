package almagest.client.particle;

import java.util.Locale;

import org.joml.Vector2d;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderEventHandler;
import almagest.client.data.MinorPlanetDataManager.MinorPlanetData;
import almagest.client.data.PlanetDataManager;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;
import static almagest.client.data.PlanetDataManager.*;

@SuppressWarnings("null")
public class MinorPlanet extends CelestialObject
{
    public final BufferBuilder builder;
    public final int id;
    public final ResourceLocation texture;
    public final ResourceLocation textureGlow;
    public final ResourceLocation textureGlowFlat;
    public Vector2d dayNightCycle;
    public final MinorPlanetData planet;
    public final String name;
    public final String nameAdj;
    public final int parent;
    public final int body;
    public final Vec3 color;
    public final Vec3 colorOrbit;
    public double alphaMod;
    public double alphaModInv;

    public final double albedo;
    public final double diameter;
    public final double obliquity;
    public final double orbit;
    public final double eccentricity;
    public final double ascendingNode;
    public final double periapsis;
    public final double rotationPeriod;
    public final double semimajorAxis;
    public final double meanLongitude;
    public final double inclination;
    public final double nodalPrecession;
    public double wobble;
    public final Vec3 obliquityRotation;

    public final double realDiameter;
    public final int segments;
    public final int glow;
    public final Player player;
    public final boolean isObserver;

    public Vec3 parentPos;
    public Vec3 pos;
    public Vec3 P1;
    public Vec3 P2;
    public Vec3 P3;
    public double rotationTime;
    public long time;

    public MinorPlanet(ClientLevel level, MinorPlanetData planet, Player player)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();

        this.setPos(0.0D, 0.0D, 0.0D);
        this.planet = planet;
        this.name = planet.names().getAlphaNameOrDefault();
        this.nameAdj = AHelpers.replaceChars(this.name.toLowerCase(Locale.ROOT));
        this.id = planet.getID();
        this.parent = PARENT_CACHE.getOrDefault(id, 0).intValue();
        this.body = OBSERVING_BODY_CACHE.get(id).booleanValue() ? 0 : id;

        this.color = planet.getColor();
        this.colorOrbit = new Vec3(Config.COMMON.minorPlanetOrbitColorRed.get(), Config.COMMON.minorPlanetOrbitColorGreen.get(), Config.COMMON.minorPlanetOrbitColorBlue.get());
        this.alphaMod = 1.0D;
        this.alphaModInv = 1.0D - this.alphaMod;

        this.texture = AHelpers.identifier(TEXTURE_CACHE.get(id) + ".png");
        this.textureGlow = AHelpers.identifier("textures/celestials/glow_cube.png");
        this.textureGlowFlat = AHelpers.identifier(!TEXTURE_CACHE.get(id).equals("Unknown") || DIAMETER_CACHE.get(id).doubleValue() / Config.COMMON.moonDiameterFactor.get() >= 1000.0D ? "textures/celestials/glow.png" : TEXTURE_CACHE.get(id) + "_glow.png");

        this.time = RenderEventHandler.time;

        this.albedo = ALBEDO_CACHE.get(id).doubleValue();
        this.diameter = DIAMETER_CACHE.get(id).doubleValue();
        this.obliquity = OBLIQUITY_CACHE.get(id).doubleValue();
        this.orbit = ORBIT_PERIOD_CACHE.get(id).doubleValue();
        this.eccentricity = ECCENTRICITY_CACHE.get(id).doubleValue();
        this.ascendingNode = ASCENDING_NODE_LONGITUDE_CACHE.get(id).doubleValue();
        this.periapsis = PERIAPSIS_LONGITUDE_CACHE.get(id).doubleValue();
        this.rotationPeriod = ROTATION_PERIOD_CACHE.get(id).doubleValue();
        this.semimajorAxis = SEMIMAJOR_AXIS_CACHE.get(id).doubleValue();
        this.meanLongitude = MEAN_LONGITUDE_CACHE.get(id).doubleValue();
        this.inclination = INCLINATION_CACHE.get(id).doubleValue();
        this.nodalPrecession = NODAL_PRECESSION_CACHE.get(id).doubleValue();
        this.wobble = PlanetDataManager.getAdjustedNodalPrecession(time, orbit, nodalPrecession);
        this.obliquityRotation = new Vec3(-(PARENT_CACHE.get(id) == 0 ? 0.0D : Math.toRadians(OBLIQUITY_CACHE.getOrDefault(PARENT_CACHE.get(id), 0.0D))), 0.0D, 0.0D);

        this.realDiameter = DIAMETER_CACHE.get(id).doubleValue() / Config.COMMON.moonDiameterFactor.get();
        this.segments = (int) Math.round(50 + Math.abs(planet.orbitalParameters().semimajorAxis() * 10.0D));
        this.isObserver = PlanetDataManager.isObserver(id);

        double elapsedTime = PlanetDataManager.getElapsedTime(time, orbit);

        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, elapsedTime, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, true, true, true, false);

        double elapsedTimeTicks = PlanetDataManager.getElapsedTime(time, rotationPeriod);
        this.rotationTime = (elapsedTimeTicks / rotationPeriod) * 360.0D;

        double adjObliquity = ((elapsedTime / orbit) * 2.0D - 1.0D) * obliquity; //FIX
        this.dayNightCycle = getDayCycle(adjObliquity, elapsedTimeTicks);

        this.quadSize = (float) diameter;
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = 1.0F;
        this.glow = Mth.clamp(Mth.floor(LightTexture.FULL_BRIGHT * (albedo * 2.0D)), 0, LightTexture.FULL_BRIGHT);
        this.player = player;
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderMinorPlanets.get() || realDiameter < Config.COMMON.minorPlanetMinSizeForRender.get()) return;
        super.tick();

        this.time = RenderEventHandler.time;
        double elapsedTime = PlanetDataManager.getElapsedTime(time, orbit);
        this.wobble = PlanetDataManager.getAdjustedNodalPrecession(time, orbit, nodalPrecession);
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, elapsedTime, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, true, true, true, false);
        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);

        double elapsedTimeTicks = PlanetDataManager.getElapsedTime(time, rotationPeriod);
        this.rotationTime = (elapsedTimeTicks / rotationPeriod) * 360.0D;

        // full alpha at night (1.0)
        this.alphaMod = RenderEventHandler.isScoping ? 1.0D : RenderEventHandler.starBrightness;
        this.alphaModInv = 1.0D - alphaMod;

        if (!isObserver && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov))
        {
            if (Config.COMMON.displayMinorPlanetNames.get() && RenderEventHandler.gameTime % 20 == 0)
            {
                if (player != null && RenderEventHandler.isScoping)
                {
                    if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, Config.COMMON.minorPlanetDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier, false))
                    {
                        player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), true);
                    }
                }
            }
        }
        if (RenderEventHandler.gameTime % 200 == 0)
        {
            double adjObliquity = ((elapsedTime / orbit) * 2.0D - 1.0D) * obliquity;
            double adjRotationPeriod = (elapsedTimeTicks / rotationPeriod) * Level.TICKS_PER_DAY;
            double time = AHelpers.modulo(((PlanetDataManager.ORBITAL_ANGLE_CACHE.getOrDefault(id, 0.0D) / 360.0D) * Level.TICKS_PER_DAY) + adjRotationPeriod, Level.TICKS_PER_DAY);
            this.dayNightCycle = getDayCycle(adjObliquity, time);
        }
        CelestialObjectHandler.MINOR_PLANET_OBJECTS_CACHE.put(id, this);
        if (!this.isAlive())
        {
            CelestialObjectHandler.MINOR_PLANET_OBJECTS_CACHE.remove(id);
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderMinorPlanets.get() || realDiameter < Config.COMMON.minorPlanetMinSizeForRender.get()) return;

        float a = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
        if (a > 0.0F && pos != null)
        {
            if (!isObserver && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov) && AHelpers.getApparentSizeClient(pos, player, this.getQuadSize(partialTicks), Config.COMMON.minApparentSize.get()))
            {
                if (planet.texture().equals("Unknown"))
                {
                    drawBodyFlat(builder, camera, partialTicks, texture, pos, this.color, this.alphaMod, this.quadSize, this.roll, this.oRoll, glow);
                    drawBodyFlat(builder, camera, partialTicks, textureGlowFlat, pos, this.color, this.alphaMod, this.quadSize, this.roll, this.oRoll, glow);
                }
                else if (planet.customModel())
                {
                    //drawGlow(builder, camera, partialTicks, textureGlow, pos, this.color, diameter, this.quadSize, this.roll, this.oRoll, glow);
                    drawBody(builder, camera, partialTicks, this.texture, this.nameAdj, pos, this.color, obliquity, rotationTime, inclination, this.dayNightCycle, this.alphaMod, this.quadSize, glow);
                }
            }
        }
    }
}