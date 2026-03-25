package almagest.client.particle;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2d;

import almagest.client.CelestialObjectHandler;
import almagest.client.data.CelestialObjectTypes;
import almagest.util.Color;

public interface ICelestialObject
{
    public BlockState getBlockModel();

    public String getName();

    public CelestialObjectTypes getType();

    public CelestialObjectHandler getHandler();

    public Color getColor();

    public double getEccentricity();

    public double getInclination();

    public double getObliquity();

    public double getRotationCompletion();

    public Vec3 getPos();

    public List<List<Vec3>> getOrbitPositions();

    public int getOrbitLineSegments();

    public double getOrbitLineWidth();

    public double getDiameter();

    public String getTextureName();

    public String getTextureNameFlat();

    public int getModelVariant();

    public int getPackedLight();

    public Vector2d getDayNightCycle();

    public boolean getApplyColor();

    public Matrix4f getTransformMatrix();

    public Matrix4f getOrientationMatrix();
}
