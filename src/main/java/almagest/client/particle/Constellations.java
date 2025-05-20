package almagest.client.particle;

import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderEventHandler;
import almagest.client.data.StarDataManager.StarData;
import almagest.config.Config;
import almagest.util.AHelpers;

@SuppressWarnings("null")
public class Constellations extends CelestialObject
{
    public final BufferBuilder builder;
    public final int id;
    public final String name;
    public final List<List<StarData>> pairs;
    public final Player player;

    public Constellations(ClientLevel level, int id, String name, List<List<StarData>> pairs, Player player)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.id = id;
        this.name = name;
        this.pairs = pairs;
        this.alpha = Config.COMMON.drawAllConstellations.get() ? 1.0F : 0.0F;
        this.player = player;
    }

    @Override
    public void tick()
    {
        boolean drawAllConstellations = Config.COMMON.drawAllConstellations.get();

        if (Config.COMMON.drawConstellations.get())
        {
            super.tick();
            float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness;
            if (alpha > 0.0F)
            {
                double maxAngle = Config.COMMON.constellationDisplayNameAngleThreshold.get() * RenderEventHandler.fieldOfViewModifier;

                for (List<StarData> pair : pairs)
                {
                    for (StarData star : pair)
                    {
                        Vec3 starPos = star.getCachedAdjPos(false);
                        if (this.alpha < 1.0F)
                        {
                            if (drawAllConstellations)
                            {
                                this.setAlpha(Mth.clamp(this.alpha + 0.01F, 0.0F, 1.0F));
                            }
                            else if (!drawAllConstellations && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, starPos, maxAngle, false))
                            {
                                this.setAlpha(Mth.clamp(this.alpha + 0.01F, 0.0F, 1.0F));
                            }
                        }
                        if (Config.COMMON.displayConstellationNames.get() && !RenderEventHandler.isScoping && AHelpers.isWithinAngle(RenderEventHandler.lookAngle, starPos, maxAngle, false))
                        {
                            player.displayClientMessage(Component.translatable(name).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC), true);
                        }
                    }
                }
            }
        }
        if (this.alpha > 0.00F && !drawAllConstellations)
        {
            this.setAlpha(Mth.clamp(this.alpha - 0.01F, 0.0F, 1.0F));
        }
        CelestialObjectHandler.CONSTELLATIONS_OBJECTS_CACHE.put(id, this);
        if (!this.isAlive())
        {
            CelestialObjectHandler.CONSTELLATIONS_OBJECTS_CACHE.remove(id);
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {}
}