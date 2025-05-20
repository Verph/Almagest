package almagest.client.particle;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import almagest.client.data.StarDataManager;
import almagest.client.data.StarDataManager.StarData;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.RenderHelpers.*;

@SuppressWarnings("null")
public class Skybox extends CelestialObject
{
    public final BufferBuilder builder;
    public final ResourceLocation texture;
    public final Player player;
    public final List<StarData> stars = new ArrayList<>();

    public Skybox(ClientLevel level, Player player)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.quadSize = 1.0F;
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.texture = AHelpers.identifier("textures/block/skybox/milky_way.png");
        this.player = player;

        stars.add(StarDataManager.STAR_DATA.get(177).get());
        stars.add(StarDataManager.STAR_DATA.get(196).get());
        stars.add(StarDataManager.STAR_DATA.get(72).get());
    }

    @Override
    public void tick()
    {
        if (!Config.COMMON.renderSkybox.get()) return;
        super.tick();
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderSkybox.get()) return;

        drawSkybox(builder, camera, partialTicks, stars, texture, level.getTimeOfDay(partialTicks), AHelpers.minApparentMagnitude(player), this.roll, this.oRoll);
    }
}