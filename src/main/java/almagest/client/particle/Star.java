package almagest.client.particle;

import org.joml.Vector3f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.data.CelestialObjectTypes;
import almagest.client.data.CelestialObjectTypes.CelestialData;
import almagest.client.data.CelestialObjectTypes.ConstellationData;
import almagest.client.data.CelestialObjectTypes.StarData;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.Color;

@SuppressWarnings("null")
public class Star
{
    public static final ResourceLocation STAR = AHelpers.identifier("textures/celestials/star.png");
    public static final ResourceLocation STAR_SIMPLE = AHelpers.identifier("textures/celestials/star_simple.png");

    public static final int SPRITE_COUNT = 5;
    public static final int SPRITE_SIZE = 15;
    public static final float SPRITE_HEIGHT = 1.0F / SPRITE_COUNT;

    public final ClientLevel level;
    public final Player player;
    public final CelestialObjectHandler handler;
    public final StarData star;
    public final CelestialData type;

    public final long id;
    public final String name;
    public final RandomSource random;
    public ResourceLocation texture;
    public final double randomRotation;
    public final float randomCos;
    public final float randomSin;
    public volatile double adjustedSize;
    public volatile double apparentMagnitude;
    public volatile double apparentMagnitudeOld;
    public final double absoluteMagnitude;
    public final double diameter;
    public double distance = 0.0D;
    public double distanceOld = Double.NaN;
    public ConstellationData constellation;
    public boolean isInConstellation;
    public final ChatFormatting style;
    public final Vec3 cartesianPos;
    public Vec3 pos = Vec3.ZERO;
    public final Color baseColor;
    public final int[] baseColorsRGBA;
    public Color color;
    public int[] colorsRGBA;
    public int spriteIndex;
    public boolean isParentToObserver;
    public boolean shouldDraw;
    public boolean isInView;
    public boolean render;

    public double size = 1.0D;
    public float alpha;
    public double visualSize;

    public final int twinkleStartIndex;
    public float twinklePhase = 0.0F;
    public final double twinkleStartSize;
    public final double twinkleChangeSpeed;

    public float p0x = 0.0F;
    public float p0y = 0.0F;
    public float p0z = 0.0F;
    public float p1x = 0.0F;
    public float p1y = 0.0F;
    public float p1z = 0.0F;
    public float p2x = 0.0F;
    public float p2y = 0.0F;
    public float p2z = 0.0F;
    public float p3x = 0.0F;
    public float p3y = 0.0F;
    public float p3z = 0.0F;
    public int[] c = new int[]{};

    public Star(ClientLevel level, Player player, CelestialObjectHandler handler, CelestialObjectTypes.StarData star)
    {
        this.level = level;
        this.handler = handler;
        this.star = star;
        this.type = star.getType();
        this.player = player;

        this.id = star.getId();
        this.name = type.getNames().get(0);
        this.random = RandomSource.create(this.id);
        this.randomRotation = Math.toRadians(random.nextDouble() * 360.0D);
        this.randomCos = Mth.cos((float) this.randomRotation);
        this.randomSin = Mth.sin((float) this.randomRotation);

        this.diameter = type.getRadius() * 2.0D;
        this.distance = star.getDistance();
        this.baseColor = star.getColor();
        this.baseColorsRGBA = baseColor.toArrayRGBA();
        this.color = baseColor;
        this.colorsRGBA = baseColorsRGBA;

        this.absoluteMagnitude = this.star.getAbsoluteMagnitude();

        Vec3 raw = star.getPos();
        Vec3 dir = raw.normalize();
        this.cartesianPos = dir.scale(raw.length() * Config.COMMON.starDistanceMult.get() + Config.COMMON.starDistanceAdd.get());
        this.pos = star.getAdjustedPos(this.cartesianPos);

        this.apparentMagnitude = AHelpers.absoluteToApparentMagnitude(this.distance, this.absoluteMagnitude);
        this.style = star.getChatColor();
        this.spriteIndex = 0;
        this.isParentToObserver = star.isParentToObserver();

        this.twinkleStartSize = random.nextDouble() * 0.4D - 0.2D;
        this.twinkleChangeSpeed = random.nextDouble() * Config.COMMON.starTwinkleFrequency.get();
        float startPhase = (float)(twinkleStartSize * 0.5D + 0.5D);
        this.twinkleStartIndex = (int)(startPhase * StarData.TWINKLE_TABLE_SIZE) & (StarData.TWINKLE_TABLE_SIZE - 1);
    }

    public void setConstellation(ConstellationData constellation)
    {
        this.constellation = constellation;
        this.isInConstellation = true;
    }

    public void tick()
    {
        if (this.distance != this.distanceOld)
        {
            this.apparentMagnitude = AHelpers.absoluteToApparentMagnitude(this.distance, this.absoluteMagnitude);
            this.distanceOld = this.distance;
        }

        if (this.alpha <= 0.0F || !Config.COMMON.renderStars.get())
        {
            this.shouldDraw = false;
            return;
        }

        if (CelestialObjectHandler.hasObserverChanged)
        {
            this.isParentToObserver = star.isParentToObserver();
        }

        this.pos = star.getAdjustedPos(this.cartesianPos);
        this.updateAdjustedSize();
        this.color = star.getAdjustedColorBySize(this.visualSize, this.alpha, this.baseColor);
        this.colorsRGBA = this.color.toArrayRGBA();
        this.apparentMagnitudeOld = apparentMagnitude;

        this.render = (this.isInConstellation && Config.COMMON.drawConstellations.get()) || (this.visualSize > Config.COMMON.minAngularSize.get() && this.apparentMagnitude <= CelestialObjectHandler.minApparentMagnitude);

        if (!render)
        {
            this.shouldDraw = false;
            return;
        }

        this.isInView = AHelpers.isWithinFov(this.pos);

        if (this.shouldDraw && Config.COMMON.displayStarNames.get() && CelestialObjectHandler.isScoping && !this.isParentToObserver)
        {
            if (AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, pos, Config.COMMON.starDisplayNameAngleThreshold.get(), false))
            {
                player.displayClientMessage(Component.translatable(name).withStyle(style, ChatFormatting.ITALIC), true);
            }
        }
    }

    public void updateAdjustedSize()
    {
        if (this.apparentMagnitudeOld != this.apparentMagnitude || CelestialObjectHandler.starConfigValuesChanged)
        {
            this.adjustedSize = StarData.getApparentSize(this.apparentMagnitude);
        }
        this.visualSize = this.adjustedSize * CelestialObjectHandler.fovNormInv;
        this.size = this.adjustedSize * StarData.getAtmosphericTwinkle(this);
    }

    public void update()
    {
        float half = (float) this.size;

        float cos = this.randomCos;
        float sin = this.randomSin;

        float x0 = -half, y0 = -half;
        float x1 =  half, y1 = -half;
        float x2 =  half, y2 =  half;
        float x3 = -half, y3 =  half;

        float rx0 = x0 * cos - y0 * sin;
        float ry0 = x0 * sin + y0 * cos;

        float rx1 = x1 * cos - y1 * sin;
        float ry1 = x1 * sin + y1 * cos;

        float rx2 = x2 * cos - y2 * sin;
        float ry2 = x2 * sin + y2 * cos;

        float rx3 = x3 * cos - y3 * sin;
        float ry3 = x3 * sin + y3 * cos;

        Vector3f right = CelestialObjectHandler.camRight;
        Vector3f up = CelestialObjectHandler.camUp;

        float cx = (float) this.pos.x;
        float cy = (float) this.pos.y;
        float cz = (float) this.pos.z - 0.0005f;

        p0x = cx + right.x * rx0 + up.x * ry0;
        p0y = cy + right.y * rx0 + up.y * ry0;
        p0z = cz + right.z * rx0 + up.z * ry0;
        p1x = cx + right.x * rx1 + up.x * ry1;
        p1y = cy + right.y * rx1 + up.y * ry1;
        p1z = cz + right.z * rx1 + up.z * ry1;
        p2x = cx + right.x * rx2 + up.x * ry2;
        p2y = cy + right.y * rx2 + up.y * ry2;
        p2z = cz + right.z * rx2 + up.z * ry2;
        p3x = cx + right.x * rx3 + up.x * ry3;
        p3y = cy + right.y * rx3 + up.y * ry3;
        p3z = cz + right.z * rx3 + up.z * ry3;
        c = this.colorsRGBA;
    }
}
