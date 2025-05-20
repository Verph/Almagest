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
import almagest.client.data.PlanetDataManager;
import almagest.client.data.PlanetDataManager.PlanetData;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;
import static almagest.client.data.PlanetDataManager.*;

@SuppressWarnings("null")
public class Planet extends CelestialObject
{
    public final BufferBuilder builder;
    public final int id;
    public final ResourceLocation texture;
    public final ResourceLocation textureClouds;
    public final ResourceLocation textureRing;
    public final ResourceLocation textureGlow;
    public Vector2d dayNightCycle;
    public final PlanetData planet;
    public final String name;
    public final String nameAdj;
    public final int parent;
    public final int body;
    public final Vec3 color;
    public final Vec3 colorOrbit;
    public Vec3 colorAdj;
    public double alphaMod;
    public double alphaModInv;

    public final double albedo;
    public final double diameter;
    public double obliquity;
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

    public final double minRingRadius;
    public final double maxRingRadius;
    public final double segments;
    public final boolean isSun;
    public final boolean isObserver;

    public final int glow;
    public final Player player;

    public Vec3 pos;
    public Vec3 P1;
    public Vec3 P2;
    public Vec3 P3;
    public Vec3 parentPos;
    public double rotationTime;
    public long time;

    public Planet(ClientLevel level, PlanetData planet, Player player)
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
        this.colorOrbit = new Vec3(Config.COMMON.planetOrbitColorRed.get(), Config.COMMON.planetOrbitColorGreen.get(), Config.COMMON.planetOrbitColorBlue.get());
        this.colorAdj = color;
        this.alphaMod = 1.0D;
        this.alphaModInv = 1.0D - this.alphaMod;

        this.texture = AHelpers.identifier(TEXTURE_CACHE.get(id) + ".png");
        this.textureClouds = AHelpers.identifier(TEXTURE_CACHE.get(id) + "_atmosphere.png");
        this.textureRing = RING_MAX_RADIUS_CACHE.get(id).doubleValue() > 0.0D ? AHelpers.identifier(TEXTURE_CACHE.get(id) + "_ring.png") : null;
        this.textureGlow = AHelpers.identifier("textures/celestials/glow_cube.png");

        this.isSun = PlanetDataManager.isSun(id);
        this.isObserver = PlanetDataManager.isObserver(id);
        this.time = RenderEventHandler.time;

        this.albedo = ALBEDO_CACHE.get(id).doubleValue();
        this.diameter = DIAMETER_CACHE.get(id).doubleValue();
        this.orbit = ORBIT_PERIOD_CACHE.get(id).doubleValue();
        this.eccentricity = ECCENTRICITY_CACHE.get(id).doubleValue();
        this.ascendingNode = ASCENDING_NODE_LONGITUDE_CACHE.get(id).doubleValue();
        this.periapsis = PERIAPSIS_LONGITUDE_CACHE.get(id).doubleValue();
        this.rotationPeriod = ROTATION_PERIOD_CACHE.get(id).doubleValue();
        this.semimajorAxis = isSun ? -SEMIMAJOR_AXIS_CACHE.get(OBSERVER).doubleValue() : SEMIMAJOR_AXIS_CACHE.get(id).doubleValue();
        this.meanLongitude = MEAN_LONGITUDE_CACHE.get(id).doubleValue();
        this.inclination = INCLINATION_CACHE.get(id).doubleValue();
        this.nodalPrecession = NODAL_PRECESSION_CACHE.get(id).doubleValue();
        this.obliquity = PlanetDataManager.getCachedObliquityVariation(body, time, orbit, isEarth(id), isEarth(id));
        this.wobble = PlanetDataManager.getAdjustedNodalPrecession(time, orbit, nodalPrecession);
        this.obliquityRotation = new Vec3(-(PARENT_CACHE.get(id) == 0 ? 0.0D : Math.toRadians(OBLIQUITY_CACHE.getOrDefault(PARENT_CACHE.get(id), 0.0D))), 0.0D, 0.0D);

        this.minRingRadius = RING_MIN_RADIUS_CACHE.get(id).doubleValue() + diameter;
        this.maxRingRadius = (RING_MAX_RADIUS_CACHE.get(id).doubleValue() * 2.0D) + diameter;
        this.segments = (int) Math.round(50 + Math.abs(planet.orbitalParameters().semimajorAxis() * 10.0D));

        double elapsedTime = PlanetDataManager.getElapsedTime(time, orbit);

        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, elapsedTime, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, true, true, true, false);

        double elapsedTimeTicks = PlanetDataManager.getElapsedTime(time, rotationPeriod);
        this.rotationTime = (elapsedTimeTicks / rotationPeriod) * 360.0D;

        double adjObliquity = ((elapsedTime / orbit) * 2.0D - 1.0D) * obliquity; //FIX
        this.dayNightCycle = getDayCycle(adjObliquity, elapsedTimeTicks);

        this.quadSize = (float) diameter * (isSun ? 50.0F : 2.0F);
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = 1.0F;
        this.glow = Mth.clamp(isSun ? LightTexture.FULL_BRIGHT : Mth.floor(LightTexture.FULL_BRIGHT * (albedo * 4.0D)), 0, LightTexture.FULL_BRIGHT);
        this.player = player;
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderPlanets.get()) return;
        super.tick();

        this.time = RenderEventHandler.time;
        double elapsedTime = PlanetDataManager.getElapsedTime(time, orbit);
        this.wobble = PlanetDataManager.getAdjustedNodalPrecession(time, orbit, nodalPrecession);
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, elapsedTime, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, wobble, obliquityRotation, true, true, true, false);
        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);

        double elapsedTimeTicks = PlanetDataManager.getElapsedTime(time, rotationPeriod);
        this.rotationTime = (elapsedTimeTicks / rotationPeriod) * 360.0D;
        this.obliquity = PlanetDataManager.getCachedObliquityVariation(id, time, orbit, isEarth(id), isEarth(id));

        // full alpha at night (1.0)
        if (!isSun)
        {
            this.alphaMod = RenderEventHandler.isScoping ? 1.0D : RenderEventHandler.starBrightness;
            this.alphaModInv = 1.0D - alphaMod;
        }

        if (!isObserver || Config.COMMON.togglePlayerOffset.get())
        {
            if (Config.COMMON.displayPlanetNames.get() && RenderEventHandler.gameTime % 20 == 0)
            {
                if (player != null && RenderEventHandler.isScoping && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov))
                {
                    if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, Config.COMMON.planetDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier, false))
                    {
                        player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC), true);
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

            /*if (name.equals("Mars") || name.equals("Earth") || name.equals("Venus") || name.equals("Mercury"))
            {
                Almagest.LOGGER.info("Orbit completion for " + name + " is: " + (T1 / orbit));
                Almagest.LOGGER.info("Adjusted obliquity for " + name + " is: " + adjObliquity);
                Almagest.LOGGER.info("Time for " + name + " is: " + time);
            }*/
        }
        CelestialObjectHandler.PLANET_OBJECTS_CACHE.put(id, this);
        if (!this.isAlive())
        {
            CelestialObjectHandler.PLANET_OBJECTS_CACHE.remove(id);
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        boolean renderPlanets = isSun ? true : Config.COMMON.renderPlanets.get();
        if (!renderPlanets) return;

        float alpha = (1.0F - RenderEventHandler.rainLevel) * this.alpha;
        if (alpha > 0.0F && pos != null && AHelpers.getApparentSizeClient(pos, player, diameter, Config.COMMON.minApparentSize.get()))
        {
            if ((isObserver && Config.COMMON.togglePlayerOffset.get()) || (!isObserver && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov)))
            {
                if (maxRingRadius > 0.0D && textureRing != null)
                {
                    drawRing(builder, camera, partialTicks, textureRing, pos, obliquity, rotationTime, minRingRadius, maxRingRadius, this.alphaMod, glow);
                }

                if (isSun)
                {
                    drawBodyFlat(builder, camera, partialTicks, texture, pos, this.color, this.alphaMod, this.quadSize, this.roll, this.oRoll, glow);
                }
                else if (!isSun)
                {
                    //drawGlow(builder, camera, partialTicks, textureGlow, pos, this.color, diameter, this.quadSize, this.roll, this.oRoll, glow);
                    drawBody(builder, camera, partialTicks, this.texture, this.nameAdj, pos, this.color, obliquity, rotationTime, inclination, this.dayNightCycle, this.alphaMod, this.quadSize, glow);
                }
            }
        }
    }
}
