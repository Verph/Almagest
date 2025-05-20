package almagest.client.particle;

import java.util.Locale;

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

import almagest.client.RenderEventHandler;
import almagest.client.data.GalaxyDataManager.GalaxyData;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;

@SuppressWarnings("null")
public class Galaxy extends CelestialObject
{
    public final BufferBuilder builder;
    public final ResourceLocation texture;
    public final GalaxyData galaxy;
    public final Player player;
    public final String name;
    public final String nameAdj;
    public final double diameter;
    public final double distanceFromEarth;
    public final double apparentSize;
    public Vec3 pos;

    public Galaxy(ClientLevel level, GalaxyData galaxy, Player player)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.galaxy = galaxy;
        this.texture = galaxy.getTexture();
        this.diameter = galaxy.diameter();
        this.distanceFromEarth = galaxy.distanceFromEarth();
        this.apparentSize = AHelpers.getApparentSize(diameter, distanceFromEarth);
        this.name = galaxy.names().getAlphaNameOrDefault();
        this.nameAdj = AHelpers.replaceChars(this.name.toLowerCase(Locale.ROOT));

        this.quadSize = (float) AHelpers.getApparentSize(galaxy.diameter(), galaxy.distanceFromEarth()) + Config.COMMON.galaxySize.get().floatValue();
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.player = player;
        this.pos = galaxy.getAdjustedPos(false);
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderGalaxies.get() || galaxy.texture().equals("Unknown") || galaxy.apparentMagnitude() > AHelpers.minApparentMagnitude(player)) return;

        super.tick();
        this.pos = galaxy.getAdjustedPos(false);

        if (Config.COMMON.displayGalaxyNames.get() && RenderEventHandler.gameTime % 20 == 0)
        {
            float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
            if (alpha > 0.0F && player != null && RenderEventHandler.isScoping && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, this.pos, RenderEventHandler.fov))
            {
                if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, this.pos, Config.COMMON.galaxyDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier, false))
                {
                    player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), true);
                }
            }
        }
    }

    @Override
    public float getQuadSize(float scaleFactor)
    {
        return (float) apparentSize + Config.COMMON.galaxySize.get().floatValue();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderGalaxies.get()) return;

        float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
        if (texture != null && galaxy.apparentMagnitude() <= AHelpers.minApparentMagnitude(player) && alpha > 0.0F && AHelpers.getApparentSizeClient(this.pos, player, this.getQuadSize(partialTicks), Config.COMMON.minApparentSize.get()))
        {
            if (this.pos != null && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, RenderEventHandler.fov))
            {
                if (galaxy.customModel())
                {
                    drawCelestialBodyNoOverlay(builder, camera, partialTicks, this.texture, this.nameAdj, this.pos, quadSize, LightTexture.FULL_BRIGHT);
                }
                else
                {
                    drawStellarObject(builder, camera, partialTicks, texture, this.pos, quadSize, this.roll, this.oRoll, alpha);
                }
            }
        }
    }
}
