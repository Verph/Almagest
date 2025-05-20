package almagest.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderEventHandler;
import almagest.client.data.StarDataManager.*;
import almagest.config.Config;
import almagest.util.AHelpers;

import static almagest.client.data.StarDataManager.*;

@SuppressWarnings("null")
public class Star extends CelestialObject
{
    public final BufferBuilder builder;
    public static final ResourceLocation STAR = AHelpers.identifier("textures/celestials/star.png");
    public static final ResourceLocation STAR_SIMPLE = AHelpers.identifier("textures/celestials/star_simple.png");

    public static final int SPRITE_COUNT = 5;
    public static final int SPRITE_SIZE = 15;
    public static final float SPRITE_HEIGHT = 1.0F / SPRITE_COUNT;

    public final int id;
    public final StarData star;
    public final String name;
    public ResourceLocation texture;
    public final double randomRotation;
    public final double apparentMagnitude;
    public final boolean isInConstellation;
    public long time;
    public final Player player;
    public final ChatFormatting style;
    public Vec3 pos;
    public Vec3 color;
    public int spriteIndex;
    public boolean fancyTexture;

    public float quadSizeAdj;
    public float alphaAdj;
    public float v0;
    public float v1;

    public Star(ClientLevel level, StarData star, Player player)
    {
        super(level, player);
        this.builder = Tesselator.getInstance().getBuilder();
        this.setPos(0.0D, 0.0D, 0.0D);
        this.id = star.id();
        this.star = star;
        this.name = star.names().getAlphaNameOrDefault();
        this.randomRotation = RandomSource.create((long) star.getID()).nextDouble() * 360.0D;
        this.apparentMagnitude = star.getPrimaryAttributes().apparentMagnitude();
        this.isInConstellation = IS_IN_CONSTELLATION_CACHE.getOrDefault(star.id(), false).booleanValue();
        this.color = star.getColor();
        this.fancyTexture = Config.COMMON.useFancyStarTexture.get();
        this.texture = (this.fancyTexture ? STAR : STAR_SIMPLE);

        this.quadSize = (float) (Math.pow(star.getApparentSize(), 3.0D) * Config.COMMON.starSize.get());
        this.gravity = 0.0F;
        this.alpha = 1.0F;
        this.hasPhysics = false;
        this.lifetime = Integer.MAX_VALUE;
        this.time = level.getRandom().nextInt(1000) * (Config.COMMON.starTwinkleFrequency.get() + 1);
        this.player = player;
        this.style = star.getCachedChatColor();
        this.pos = star.getAdjustedPos(isInConstellation);
        this.spriteIndex = 0;
    }

    @Override
    public void tick()
    {
        if (Config.COMMON.renderStars.get() && (isInConstellation || apparentMagnitude <= AHelpers.minApparentMagnitude(player)))
        {
            super.tick();
            long gameTime = RenderEventHandler.gameTime;
            if (gameTime % Config.COMMON.starUpdateFrequency.get() == 0)
            {
                this.pos = star.getAdjustedPos(isInConstellation);
            }

            if (Config.COMMON.starTwinkleFrequency.get() > 0 && Config.COMMON.useFancyStarTexture.get())
            {
                this.time += 1 + level.getRandom().nextInt(Math.max(Mth.floor(star.getPrimaryAttributes().distanceFromEarth() * 0.2D) + 1, 1));
            }
            if (Config.COMMON.displayStarNames.get() && gameTime % 20 == 0)
            {
                float alpha = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
                if (alpha > 0.0F && player != null && RenderEventHandler.isScoping && star.getID() > 0)
                {
                    if (AHelpers.isWithinAngle(RenderEventHandler.lookAngle, pos, Config.COMMON.starDisplayNameAngleThreshold.get(), false))
                    {
                        player.displayClientMessage(Component.translatable(name).withStyle(style, ChatFormatting.ITALIC), true);
                    }
                }
            }

            this.texture = (this.fancyTexture ? STAR : STAR_SIMPLE);
            this.quadSizeAdj = this.getQuadSize(1.0F);
            this.alphaAdj = (1.0F - RenderEventHandler.rainLevel) * RenderEventHandler.starBrightness * this.alpha;
            this.v0 = 0.0F;
            this.v1 = 1.0F;

            if (this.fancyTexture)
            {
                int cycleTime = Config.COMMON.starTwinkleFrequency.get();
                if (cycleTime > 0)
                {
                    int totalCycleTime = cycleTime * (SPRITE_COUNT - 1) * 2;
                    long modTime = time % totalCycleTime;
                    this.spriteIndex = Math.round(modTime / cycleTime);
                    if (this.spriteIndex >= SPRITE_COUNT)
                    {
                        this.spriteIndex = SPRITE_COUNT - (this.spriteIndex - SPRITE_COUNT + 1);
                    }
                }

                this.v0 = this.spriteIndex * SPRITE_HEIGHT;
                this.v1 = v0 + SPRITE_HEIGHT;
            }
            CelestialObjectHandler.STAR_OBJECTS_CACHE.put(id, this);
        }
        if (!this.isAlive())
        {
            CelestialObjectHandler.STAR_OBJECTS_CACHE.remove(id);
            this.remove();
        }
    }

    @Override
    public float getQuadSize(float scaleFactor)
    {
        float fancyTexMod = this.fancyTexture ? 1.0F : 0.15F;
        float modifier = (float) RenderEventHandler.fieldOfViewModifier;
        return quadSize * (!RenderEventHandler.isScoping ? modifier : modifier + 0.2F) * fancyTexMod;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {}
}
