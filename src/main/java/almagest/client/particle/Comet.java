package almagest.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderEventHandler;
import almagest.client.data.CometDataManager.CometData;
import almagest.client.data.PlanetDataManager;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;
import static almagest.client.data.PlanetDataManager.*;

import java.util.Locale;

@SuppressWarnings("null")
public class Comet extends CelestialObject
{
    public final BufferBuilder builder;
    public final int id;
    public final ResourceLocation texture;
    public final ResourceLocation textureGlow;
    public final ResourceLocation textureGlowFlat;
    public final ResourceLocation textureCometTail;
    public final CometData comet;
    public final String name;
    public final String nameAdj;
    public final int parent;

    public final double diameter;
    public final double orbit;
    public final double eccentricity;
    public final double ascendingNode;
    public final double periapsis;
    public final double semimajorAxis;
    public final double meanLongitude;
    public final double inclination;
    public final Vec3 obliquityRotation;

    public final double realDiameter;
    public final int segments;
    public final int glow;
    public final Player player;
    public final boolean isObserver;

    public final Vec3 color;
    public final Vec3 colorOrbit;
    public Vec3 parentPos;
    public Vec3 pos;
    public Vec3 sunPos;
    public Vec3 direction;
    public Vec3 directionToSun;
    public Vec3 P1;
    public Vec3 P2;
    public Vec3 P3;
    public long time;

    public Comet(ClientLevel level, CometData comet, Player player)
    {
        super(level, player);

        this.setPos(0.0D, 0.0D, 0.0D);
        this.builder = Tesselator.getInstance().getBuilder();
        this.comet = comet;
        this.name = comet.names().getAlphaNameOrDefault();
        this.nameAdj = AHelpers.replaceChars(this.name.toLowerCase(Locale.ROOT));
        this.id = comet.getID();
        this.parent = PARENT_CACHE.getOrDefault(id, 0).intValue();

        this.texture = AHelpers.identifier(TEXTURE_CACHE.get(id) + ".png");
        this.textureGlow = AHelpers.identifier("textures/celestials/glow_cube.png");
        this.textureGlowFlat = AHelpers.identifier(!TEXTURE_CACHE.get(id).equals("Unknown") || DIAMETER_CACHE.get(id).doubleValue() / Config.COMMON.cometDiameterFactor.get() >= 1000.0D ? "textures/celestials/glow.png" : TEXTURE_CACHE.get(id) + "_glow.png");
        this.textureCometTail = AHelpers.identifier("textures/celestials/comet.png");

        this.color = new Vec3(0.9D, 0.9D, 0.9D);
        this.colorOrbit = new Vec3(Config.COMMON.cometOrbitColorRed.get(), Config.COMMON.cometOrbitColorGreen.get(), Config.COMMON.cometOrbitColorBlue.get());
        this.time = RenderEventHandler.time;

        this.diameter = DIAMETER_CACHE.get(id).doubleValue();
        this.orbit = ORBIT_PERIOD_CACHE.get(id).doubleValue();
        this.eccentricity = ECCENTRICITY_CACHE.get(id).doubleValue();
        this.ascendingNode = ASCENDING_NODE_LONGITUDE_CACHE.get(id).doubleValue();
        this.periapsis = PERIAPSIS_LONGITUDE_CACHE.get(id).doubleValue();
        this.semimajorAxis = SEMIMAJOR_AXIS_CACHE.get(id).doubleValue();
        this.meanLongitude = MEAN_LONGITUDE_CACHE.get(id).doubleValue();
        this.inclination = INCLINATION_CACHE.get(id).doubleValue();
        this.obliquityRotation = new Vec3(-(PARENT_CACHE.get(id) == 0 ? 0.0D : Math.toRadians(OBLIQUITY_CACHE.getOrDefault(PARENT_CACHE.get(id), 0.0D))), 0.0D, 0.0D);

        this.realDiameter = DIAMETER_CACHE.get(id).doubleValue() / Config.COMMON.cometDiameterFactor.get();
        this.segments = (int) Math.round(50 + Math.abs(this.semimajorAxis * 0.1D));
        this.isObserver = PlanetDataManager.isObserver(id);

        double elapsedTime = PlanetDataManager.getElapsedTime(time, orbit);

        this.pos = PlanetDataManager.getPos(id, level, time, orbit, elapsedTime, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, 0.0D, obliquityRotation, true, true, true, false);
        this.sunPos = PlanetDataManager.getCachedPos(0, level, time, false, false);

        Vec3 futurePos = PlanetDataManager.getPos(id, level, time, orbit, PlanetDataManager.getElapsedTime(time + 10, orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, 0.0D, obliquityRotation, false, true, true);
        this.direction = futurePos.subtract(this.pos);
        this.directionToSun = sunPos.subtract(this.pos);

        this.quadSize = (float) diameter;
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = 1.0F;
        this.glow = LightTexture.FULL_BRIGHT;
        this.player = player;
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderComets.get() || realDiameter < Config.COMMON.cometMinSizeForRender.get()) return;
        super.tick();

        this.time = RenderEventHandler.time;
        double elapsedTime = PlanetDataManager.getElapsedTime(time, orbit);
        this.pos = PlanetDataManager.getPos(id, level, time, orbit, elapsedTime, eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, 0.0D, obliquityRotation, true, true, true, false);
        this.parentPos = POSITION_CACHE.getOrDefault(this.parent, AHelpers.ZERO_VEC);

        Vec3 futurePos = PlanetDataManager.getPos(id, level, time, orbit, PlanetDataManager.getElapsedTime(time + 10, orbit), eccentricity, ascendingNode, periapsis, semimajorAxis, meanLongitude, inclination, 0.0D, obliquityRotation, false, true, true);
        this.direction = futurePos.subtract(this.pos);
        this.directionToSun = sunPos.subtract(this.pos);

        this.sunPos = PlanetDataManager.getCachedPos(0, level, time, false, false);

        if (!isObserver)
        {
            if (Config.COMMON.displayCometNames.get() && RenderEventHandler.gameTime % 20 == 0)
            {
                float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
                if (alpha > 0.0F && player != null && RenderEventHandler.isScoping && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov))
                {
                    if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, Config.COMMON.cometDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier, false))
                    {
                        player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), true);
                    }
                }
            }
        }
        CelestialObjectHandler.COMET_OBJECTS_CACHE.put(id, this);
        if (!this.isAlive())
        {
            CelestialObjectHandler.COMET_OBJECTS_CACHE.remove(id);
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderComets.get() || realDiameter < Config.COMMON.cometMinSizeForRender.get()) return;

        float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
        if (alpha > 0.0F && pos != null)
        {
            if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov))
            {
                drawCometTailBody(builder, camera, partialTicks, this.texture, pos, sunPos, this.direction, this.directionToSun, this.quadSize, glow);
                if (!isObserver && AHelpers.getApparentSizeClient(pos, player, this.getQuadSize(partialTicks), Config.COMMON.minApparentSize.get()))
                {
                    drawBodyFlat(builder, camera, partialTicks, texture, pos, color, alpha, this.quadSize, this.roll, this.oRoll, glow);
                    drawBodyFlat(builder, camera, partialTicks, textureGlowFlat, pos, color, alpha, this.quadSize, this.roll, this.oRoll, glow);
                }
            }
        }
    }
}