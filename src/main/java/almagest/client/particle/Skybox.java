package almagest.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import almagest.client.CelestialObjectHandler;
import almagest.client.blocks.ABlocks;
import almagest.client.blocks.CelestialBodyBlock;
import almagest.client.data.CelestialObjectTypes.StarData;
import almagest.config.Config;
import almagest.util.Color;

import static almagest.client.RenderHelpers.*;

@SuppressWarnings("null")
public class Skybox
{
    public final ClientLevel level;
    public final Player player;
    public final CelestialObjectHandler handler;
    public BlockState model;
    public Color color;

    public Skybox(ClientLevel level, Player player, CelestialObjectHandler handler)
    {
        this.level = level;
        this.player = player;
        this.handler = handler;
        this.model = ABlocks.CELESTIAL_BODY.get().defaultBlockState().trySetValue(CelestialBodyBlock.CELESTIAL_BODY, "skybox");
        this.color = new Color(1.0F, 1.0F, 1.0F, 0.0F);
    }

    public void render(BufferBuilder builder, Camera camera, float partialTicks)
    {
        if (!Config.COMMON.renderSkybox.get() || CelestialObjectHandler.skyboxRotationMatrix == null) return;
        this.color = color.set((float) Mth.clamp(StarData.getStarAlpha(Config.COMMON.skyboxApparentMagnitude.get()) * Config.COMMON.skyboxBrightness.get(), 0, 1.0F));
        drawSkybox(builder, camera, partialTicks, this);
    }

    public BlockState getBlockModel()
    {
        return this.model;
    }
}