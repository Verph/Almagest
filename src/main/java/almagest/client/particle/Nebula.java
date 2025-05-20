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

import almagest.client.RenderEventHandler;
import almagest.client.data.NebulaDataManager.NebulaData;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;

import java.util.Locale;

@SuppressWarnings("null")
public class Nebula extends CelestialObject
{
    public final BufferBuilder builder;
    public final ResourceLocation texture;
    public final NebulaData nebula;
    public final Player player;
    public final String name;
    public final String nameAdj;
    public Vec3 pos;

    public Nebula(ClientLevel level, NebulaData nebula, Player player)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.nebula = nebula;
        this.texture = nebula.getTexture();
        this.name = nebula.alphanumericName();
        this.nameAdj = AHelpers.replaceChars(this.name.toLowerCase(Locale.ROOT));

        this.quadSize = Config.COMMON.nebulaSize.get().floatValue();
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.player = player;
        this.pos = nebula.getAdjustedPos(true);
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderNebulas.get() || nebula.texture().equals("Unknown") || nebula.apparentMagnitude() > AHelpers.minApparentMagnitude(player)) return;

        super.tick();
        this.pos = nebula.getAdjustedPos(false);

        if (Config.COMMON.displayNebulaNames.get() && RenderEventHandler.gameTime % 20 == 0)
        {
            float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
            if (alpha > 0.0F && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, this.pos, RenderEventHandler.fov))
            {
                if (player != null && RenderEventHandler.isScoping)
                {
                    if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, this.pos, Config.COMMON.nebulaDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier, false))
                    {
                        player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), true);
                    }
                }
            }
        }
    }

    @Override
    public float getQuadSize(float scaleFactor)
    {
        return Config.COMMON.nebulaSize.get().floatValue();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderNebulas.get() || nebula.texture().equals("Unknown")) return;

        float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
        if (texture != null && this.pos != null && alpha > 0.0F && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, this.pos, RenderEventHandler.fov) && nebula.apparentMagnitude() <= AHelpers.minApparentMagnitude(player) && AHelpers.getApparentSizeClient(this.pos, player, this.getQuadSize(partialTicks), Config.COMMON.minApparentSize.get()))
        {
            if (nebula.customModel())
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
