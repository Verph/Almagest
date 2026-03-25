package almagest.client.particle;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.RenderHelpers;
import almagest.client.data.CelestialObjectTypes.ConstellationData;
import almagest.client.data.CelestialObjectTypes.StarData;
import almagest.config.Config;
import almagest.util.Color;
import almagest.util.FastMath;

@SuppressWarnings("null")
public class Constellations
{
    public static final float ALPHA_CHANGE = 0.005F;

    public final ClientLevel level;
    public final Player player;
    public final CelestialObjectHandler handler;
    public final ConstellationData constellation;

    public final long id;
    public final String name;
    public volatile List<List<Star>> pairs = new ArrayList<>();
    public float alpha;
    public float starAlpha;
    public Color color;

    public final List<LineSegment> renderSegments = new ArrayList<>();
    public float lineWidth;

    public Constellations(ClientLevel level, Player player, CelestialObjectHandler handler, ConstellationData constellation)
    {
        this.level = level;
        this.player = player;
        this.handler = handler;
        this.constellation = constellation;

        this.id = constellation.getId();
        this.name = constellation.getName();
        this.alpha = Config.COMMON.drawAllConstellations.get() ? 1.0F : 0.0F;
        this.color = constellation.getColor();
        this.pairs = constellation.getStarPairs();
    }

    public void tick(boolean isFocused)
    {
        this.updateFocus(isFocused);

        this.starAlpha = (float) StarData.getStarAlpha(1.0D);

        renderSegments.clear();

        if (Config.COMMON.drawConstellations.get())
        {
            double distance = Config.COMMON.constellationLineDistance.get();
            for (List<Star> stars : pairs)
            {
                if (stars.size() < 2) continue;

                Vec3 vec1 = stars.get(0).pos.normalize().scale(distance);
                Vec3 vec2 = stars.get(1).pos.normalize().scale(distance);
                Vec3 direction = vec2.subtract(vec1).normalize();
                Quaternionf rotation = RenderHelpers.getRotationToAlign(direction, new Vec3(1.0F, 0.0F, 0.0F));
                float length = (float)vec1.distanceTo(vec2);
                renderSegments.add(new LineSegment(vec1, length, rotation));
            }
        }
    }

    public void setStarPairs(List<List<Star>> pairs)
    {
        this.pairs = pairs;
    }

    public void render(BufferBuilder builder, Camera camera, float partialTicks)
    {
        if (Config.COMMON.drawConstellations.get() && this.alpha > 0.0F && this.starAlpha > 0.01F)
        {
            RenderHelpers.drawConstellation(builder, camera, partialTicks, this);
        }
    }

    public void updateFocus(boolean isFocused)
    {
        boolean focusedOrDrawAll = Config.COMMON.drawAllConstellations.get() || isFocused;

        if (focusedOrDrawAll)
        {
            if (this.alpha < 1.0F)
            {
                this.alpha = Mth.clamp(this.alpha + ALPHA_CHANGE, 0.0F, 1.0F);
            }
            if (isFocused && Config.COMMON.displayConstellationNames.get() && !CelestialObjectHandler.isScoping)
            {
                player.displayClientMessage(
                    Component.translatable(name).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
                    true
                );
            }
        }
        else
        {
            if (this.alpha > 0.0F)
            {
                this.alpha = Mth.clamp(this.alpha - ALPHA_CHANGE, 0.0F, 1.0F);
            }
        }

        this.color = new Color(color.red(), color.green(), color.blue(), this.alpha);
    }

    public Vec3 computeCentroid()
    {
        double x = 0, y = 0, z = 0;
        int count = 0;
        for (List<Star> pair : pairs)
        {
            for (Star s : pair)
            {
                x += s.pos.x();
                y += s.pos.y();
                z += s.pos.z();
                count++;
            }
        }
        if (count == 0) return Vec3.ZERO;
        return new Vec3(x / count, y / count, z / count);
    }

    public double computeMaxAngularRadius(Vec3 centroid)
    {
        if (pairs.isEmpty() || centroid.lengthSqr() == 0.0) return 0.0;
        double maxAngle = 0.0;
        Vec3 normCentroid = centroid.normalize();
        for (List<Star> pair : pairs)
        {
            for (Star s : pair)
            {
                double angle = FastMath.acos(normCentroid.dot(s.pos.normalize()));
                if (angle > maxAngle) maxAngle = angle;
            }
        }
        return maxAngle;
    }

    public static class LineSegment
    {
        public final Vec3 start;
        public final float length;
        public final Quaternionf rotation;

        public LineSegment(Vec3 start, float length, Quaternionf rotation)
        {
            this.start = start;
            this.length = length;
            this.rotation = rotation;
        }
    }
}