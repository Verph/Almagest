package almagest.client.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector2d;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.blocks.ABlocks;
import almagest.client.blocks.CelestialBodyBlock;
import almagest.client.data.CelestialObjectTypes;
import almagest.client.data.CelestialObjectTypes.*;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.Color;
import almagest.util.ColorUtils;
import almagest.util.PlanetHelpers;

import static almagest.client.RenderHelpers.*;

@SuppressWarnings("null")
public class CelestialObject implements ICelestialObject
{
    public final ClientLevel level;
    public final CelestialObjectHandler handler;
    public final CelestialObjectTypes.CelestialData body;
    public final Player player;
    public final RandomSource random;

    public final Optional<CelestialData> parentBody;
    public volatile List<CelestialObject> parentObjects = new ArrayList<>();
    public final boolean isStar;
    public final boolean hasCustomModel;
    public final boolean applyColor;
    public final int modelVariant;

    public final CelestialObjectTypes type;
    public final String name;
    public final String bodyDescription;
    public final String textureName;
    public final String textureNameFlat;
    public final int textureVariant;
    public final BlockState blockModel;
    public final boolean renderStarBody;
    public final ResourceLocation ringTexture;

    public final double rotationPeriod;
    public final double obliquity;
    public double diameter;

    public final double period;
    public final double semiMajorAxis;
    public final double periapsis;
    public final double eccentricity;
    public double inclination;
    public final double ascendingNode;
    public final double argOfPeriapsis;
    public final double longOfPeriapsis;
    public final double meanLongitude;
    public Vec3 obliquityRotation = Vec3.ZERO;

    public final boolean hasTrail;
    public String trailName = "Unknown";
    public int population = 0;
    public List<MeteoroidObject> meteoroids = new ArrayList<>();

    public final Color color;
    public final int packedLight;
    public final double orbitLineWidthFactor;
    public final int orbitLineSegments;

    public boolean isObserver;
    public boolean isSystemCenterObject;

    public double elapsedTime;
    public Vec3 posAU = Vec3.ZERO;
    public Vec3 pos = Vec3.ZERO;
    public Vec3 oldPos = Vec3.ZERO;
    public List<List<Vec3>> orbitPositions;
    public double angleAroundSun = 0.0D;
    public double rotationCompletion = 0.0D;
    public Vector2d dayNightCycle = new Vector2d(0.0D, 0.0D);
    public double orbitLineWidth;
    public double season;
    public double timeOfDay;
    public double nodalPrecession;
    public boolean shouldRender;
    public boolean shouldRenderOrbits;
    public Matrix4f orientationMatrix = new Matrix4f();
    public Matrix4f transformationMatrix = new Matrix4f();

    public CelestialObject(ClientLevel level, Player player, CelestialObjectHandler handler, CelestialObjectTypes.CelestialData body)
    {
        this.level = level;
        this.handler = handler;
        this.body = body;
        this.player = player;
        this.random = RandomSource.create(body.getId());

        this.parentBody = body.getParent();
        this.isStar = body.isStar();
        this.hasCustomModel = body.hasCustomModel();
        this.modelVariant = this.hasCustomModel ? 0 : random.nextIntBetweenInclusive(CelestialBodyBlock.MIN, CelestialBodyBlock.MAX);
        this.applyColor = !this.hasCustomModel;

        this.type = body.type();
        this.name = body.getNames().isEmpty() ? "Unknown" : body.getNames().get(0);
        this.bodyDescription = body.getBody().getBodyDescription();
        this.renderStarBody = body.getStar() != null && body.getStar().isAParent();
        this.ringTexture = this.body.hasRing() ? AHelpers.identifier("textures/block/" + body.type().getSerializedName() + "s/rings/" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_") + ".png") : null;

        this.diameter = body.getDiameter();
        this.rotationPeriod = body.getBody().getRotationPeriod() * Config.COMMON.planetDayFactor.get();
        this.obliquity = body.getBody().getObliquity();

        this.textureName = this.hasCustomModel ? this.name.toLowerCase(Locale.ROOT) : this.type.getSerializedName();
        this.textureNameFlat = "textures/" + (this.name.equalsIgnoreCase("sun") ? "planets/sun" : this.hasCustomModel ? this.type.getSerializedName() + "/" + this.name.toLowerCase(Locale.ROOT) : this.type.getSerializedName() + "/default") + ".png";
        this.textureVariant = AHelpers.lerpBodyDiameter(diameter);
        this.blockModel = this.body.isBarycenter() ? null : ABlocks.CELESTIAL_BODY.get().defaultBlockState().trySetValue(CelestialBodyBlock.CELESTIAL_BODY, this.textureName).trySetValue(CelestialBodyBlock.VARIANT, this.modelVariant).trySetValue(CelestialBodyBlock.ALTERNATIVE, this.name.equalsIgnoreCase("luna") ? Config.COMMON.toggleEasterEggMoon.get() : false);

        Orbit orbitParam = body.getOrbit();
        this.period = orbitParam.getPeriod();
        this.semiMajorAxis = orbitParam.getSemiMajorAxis() * body.getOrbitDistanceFactor();
        this.periapsis = orbitParam.getPeriapsis() * body.getOrbitDistanceFactor();
        this.eccentricity = orbitParam.getEccentricity();
        this.ascendingNode = orbitParam.getAscendingNode();
        this.argOfPeriapsis = orbitParam.getArgOfPeriapsis();
        this.longOfPeriapsis = orbitParam.getLongOfPeriapsis();
        this.meanLongitude = orbitParam.getMeanLongitude();

        double parentObliquity = (this.parentBody.isPresent() ? this.parentBody.get().getBody().getObliquity() : 0.0D);
        this.inclination = body.getOrbit().getInclination();
        this.obliquityRotation = this.hasCustomModel ? new Vec3(parentObliquity, 0.0D, 0.0D) : new Vec3(random.nextDouble() * Math.PI * 2.0D, random.nextDouble() * Math.PI * 2.0D, random.nextDouble() * Math.PI * 2.0D);

        this.color = isStar ? ColorUtils.tEffToRGB(body.getTemperature()) : new Color(1.0F, 1.0F, 1.0F, 1.0F);
        this.packedLight = isStar ? LightTexture.FULL_BRIGHT : LightTexture.FULL_SKY;
        this.orbitLineWidthFactor = type.getOrbitWidthFactor();
        this.orbitLineSegments = orbitParam.getSegments();

        this.orientationMatrix = new Matrix4f()
            .rotate(Axis.XP.rotationDegrees((float) this.obliquity));

        this.transformationMatrix = new Matrix4f()
            .translate((float) pos.x, (float) pos.y, (float) pos.z)
            .mul(orientationMatrix)
            .scale((float) diameter)
            .translate(-0.5F, -0.5F, -0.5F);

        Trail trailParam = body.getTrail();
        this.hasTrail = trailParam.getHasTrail();
        if (this.hasTrail)
        {
            this.trailName = trailParam.getName();
            this.population = trailParam.getAdjustedPopulation();
            List<MeteoroidObject> m = new ArrayList<>();
            for (int i = 0; i < this.population; i++)
            {
                MeteoroidObject meteoroid = new MeteoroidObject(handler, body, this, i);
                m.add(i, meteoroid);
            }
            this.meteoroids = m;
        }
    }

    public void setParentObjects(List<CelestialObject> parentObjects)
    {
        this.parentObjects = parentObjects;
    }

    public void tick()
    {
        if (CelestialObjectHandler.observerObject == null) return;

        this.diameter = body.getDiameter();
        this.isObserver = CelestialObjectHandler.isObserver(this);

        Vec3 vec0 = oldPos.multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 vec1 = pos.normalize();
        this.angleAroundSun = Math.toDegrees(AHelpers.calculateAngleBetweenVectors(vec0.x(), vec0.z(), vec1.x(), vec1.z()));

        double elapsedTimeTicks = PlanetHelpers.getElapsedTime(CelestialObjectHandler.dayTime, rotationPeriod);
        double adjObliquity = ((elapsedTime / period) * 2.0D - 1.0D) * obliquity;
        double adjRotationPeriod = (elapsedTimeTicks / rotationPeriod) * Level.TICKS_PER_DAY;
        this.rotationCompletion = AHelpers.modulo(((angleAroundSun / 360.0D) * Level.TICKS_PER_DAY) + adjRotationPeriod, Level.TICKS_PER_DAY);
        this.dayNightCycle = PlanetHelpers.getDayCycle(adjObliquity, rotationCompletion);

        this.orbitLineWidth = this.orbitLineSize();
        this.oldPos = this.pos;
        this.shouldRender = this.shouldRender();
        this.shouldRenderOrbits = this.renderOrbit();

        this.orientationMatrix = new Matrix4f()
            .rotate(Axis.XP.rotationDegrees((float) this.obliquity));

        this.transformationMatrix = new Matrix4f()
            .translate((float) pos.x, (float) pos.y, (float) pos.z)
            .mul(orientationMatrix)
            .scale((float) diameter)
            .translate(-0.5F, -0.5F, -0.5F);
    }

    public void renderOrbit(BufferBuilder builder, Camera camera, float partialTicks)
    {
        if (this.isObserver && !Config.COMMON.togglePlayerOffset.get())
            return;

        if (this.shouldRender &&
            this.shouldRenderOrbits &&
            this.orbitPositions != null &&
            !this.orbitPositions.isEmpty() &&
            this.pos != null)
        {
            drawOrbit(builder, camera, partialTicks, this, true);
        }
    }


    public void renderBody(BufferBuilder builder, Camera camera, float partialTicks)
    {
        if (this.isObserver && !Config.COMMON.togglePlayerOffset.get())
            return;

        if (this.shouldRender)
        {
            if (this.blockModel != null && pos != null && AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, pos, CelestialObjectHandler.fov))
            {
                drawBody(builder, camera, partialTicks, this);
            }
            for (MeteoroidObject meteoroid : this.meteoroids)
            {
                meteoroid.render(builder, camera, partialTicks);
            }
        }
    }

    public boolean shouldRender()
    {
        switch (type)
        {
            case PLANET, BARYCENTER_PLANET:
                return Config.COMMON.renderPlanets.get();
            case MOON, DWARFMOON:
                return Config.COMMON.renderMoons.get();
            case DWARFPLANET, BARYCENTER:
                return Config.COMMON.renderMinorPlanets.get();
            case COMET:
                return Config.COMMON.renderComets.get();
            case ASTEROID:
                return Config.COMMON.renderMeteorShowers.get();
            case STAR:
                return this.renderStarBody;
            default:
                return this.hasTrail && Config.COMMON.renderMeteorShowers.get();
        }
    }

    public boolean displayNames()
    {
        switch (type)
        {
            case PLANET, BARYCENTER_PLANET:
                return Config.COMMON.displayPlanetNames.get();
            case MOON, DWARFMOON:
                return Config.COMMON.displayMoonNames.get();
            case DWARFPLANET, BARYCENTER:
                return Config.COMMON.displayMinorPlanetNames.get();
            case COMET:
                return Config.COMMON.displayCometNames.get();
            case ASTEROID:
                return Config.COMMON.displayMeteorShowerNames.get();
            case STAR:
                return Config.COMMON.displayStarNames.get();
            default:
                return false;
        }
    }

    public boolean renderOrbit()
    {
        double size = this.body.getRadius();
        switch (type)
        {
            case PLANET:
                return Config.COMMON.showPlanetOrbits.get() && size >= Config.COMMON.planetMinSizeForOrbitLine.get();
            case BARYCENTER_PLANET:
                return Config.COMMON.showPlanetOrbits.get();
            case MOON:
                return Config.COMMON.showMoonOrbits.get() && size >= Config.COMMON.moonMinSizeForOrbitLine.get();
            case DWARFMOON:
                return Config.COMMON.showMoonOrbits.get() && size >= Config.COMMON.moonMinSizeForOrbitLine.get();
            case DWARFPLANET:
                return Config.COMMON.showMinorPlanetOrbits.get() && size >= Config.COMMON.minorPlanetMinSizeForOrbitLine.get();
            case BARYCENTER:
                return Config.COMMON.showMinorPlanetOrbits.get();
            case COMET:
                return Config.COMMON.showCometOrbits.get() && size >= Config.COMMON.cometMinSizeForOrbitLine.get();
            case ASTEROID:
                return Config.COMMON.showMeteorShowerOrbits.get();
            default:
                return false;
        }
    }

    public double orbitLineSize()
    {
        switch (type)
        {
            case PLANET, BARYCENTER_PLANET:
                return Config.COMMON.planetOrbitLineWidth.get();
            case MOON, DWARFMOON:
                return Config.COMMON.moonOrbitLineWidth.get();
            case DWARFPLANET, BARYCENTER:
                return Config.COMMON.minorPlanetOrbitLineWidth.get();
            case COMET:
                return Config.COMMON.cometOrbitLineWidth.get();
            case ASTEROID:
                return Config.COMMON.meteorShowerOrbitLineWidth.get();
            default:
                return 0.0F;
        }
    }

    @Override
    public BlockState getBlockModel()
    {
        return this.blockModel;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public CelestialObjectTypes getType()
    {
        return this.type;
    }

    @Override
    public Color getColor()
    {
        return this.color;
    }

    @Override
    public double getEccentricity()
    {
        return this.eccentricity;
    }

    @Override
    public double getInclination()
    {
        return this.inclination;
    }

    @Override
    public double getObliquity()
    {
        return this.obliquity;
    }

    @Override
    public double getRotationCompletion()
    {
        return this.rotationCompletion;
    }

    @Override
    public Vec3 getPos()
    {
        return this.pos;
    }

    @Override
    public List<List<Vec3>> getOrbitPositions()
    {
        return this.orbitPositions;
    }

    @Override
    public int getOrbitLineSegments()
    {
        return this.orbitLineSegments;
    }

    @Override
    public double getOrbitLineWidth()
    {
        return this.orbitLineWidth;
    }

    @Override
    public double getDiameter()
    {
        return this.diameter;
    }

    @Override
    public String getTextureName()
    {
        return this.textureName;
    }

    @Override
    public String getTextureNameFlat()
    {
        return this.textureNameFlat;
    }

    @Override
    public int getModelVariant()
    {
        return this.modelVariant;
    }

    @Override
    public int getPackedLight()
    {
        return this.packedLight;
    }

    @Override
    public Vector2d getDayNightCycle()
    {
        return this.dayNightCycle;
    }

    @Override
    public CelestialObjectHandler getHandler()
    {
        return this.handler;
    }

    @Override
    public boolean getApplyColor()
    {
        return this.applyColor;
    }

    @Override
    public Matrix4f getTransformationMatrix()
    {
        return this.transformationMatrix;
    }

    @Override
    public Matrix4f getOrientationMatrix()
    {
        return this.orientationMatrix;
    }

    public class MeteoroidObject implements ICelestialObject
    {
        public final long id;
        public final CelestialObjectHandler handler;
        public final CelestialObject mainBody;
        public final CelestialObjectTypes type;
        public final String textureName;
        public final String textureNameFlat;

        public final RandomSource random;
        public final Color color;
        public final int modelVariant;
        public final double diameter;
        public final double semiMajorAxis;
        public final double periapsis;
        public final double period;
        public final Vec3 obliquityRotation;
        public final BlockState blockModel;
        public final boolean applyColor;

        public double elapsedTime;
        public Vec3 pos = Vec3.ZERO;
        public Matrix4f orientationMatrix = new Matrix4f();
        public Matrix4f transformationMatrix = new Matrix4f();
        public List<Matrix4f> transformationMatrixOrbit = new ArrayList<Matrix4f>();

        public MeteoroidObject(CelestialObjectHandler handler, CelestialObjectTypes.CelestialData body, CelestialObject mainBody, long id)
        {
            this.id = id;
            this.handler = handler;
            this.mainBody = mainBody;
            this.type = CelestialObjectTypes.ASTEROID;
            this.textureName = type.getSerializedName();
            this.textureNameFlat = "textures/celestials/asteroid_small.png";

            Trail trailParam = body.getTrail();
            this.random = RandomSource.create(id);

            float randomColor = (random.nextFloat() * 0.5F) + 0.5F;
            double universeScale = Config.COMMON.universeScale.get();

            this.color = new Color(randomColor, randomColor, randomColor, 1.0F);
            double size = trailParam.getRandomSize(this.random, 100.0D, 1.0D);
            double mass = PlanetHelpers.getMassInEarthMasses(size * 0.5D, PlanetHelpers.getDensityFromType(type));
            this.diameter = size * universeScale;
            this.modelVariant = random.nextIntBetweenInclusive(CelestialBodyBlock.MIN, CelestialBodyBlock.MAX);
            this.semiMajorAxis = body.getOrbit().getSemiMajorAxis() * trailParam.getOrbitDistanceFactor(this.random.nextDouble()) * body.getOrbitDistanceFactor();
            this.periapsis = body.getOrbit().getPeriapsis() * trailParam.getOrbitDistanceFactor(this.random.nextDouble());
            this.period = PlanetHelpers.getOrbitalPeriod(mainBody.body.getMass(), mass, this.semiMajorAxis);
            this.obliquityRotation = new Vec3(this.random.nextGaussian() * 0.3D, this.random.nextGaussian() * 0.3D, this.random.nextGaussian() * 0.3D);
            this.blockModel = ABlocks.CELESTIAL_BODY.get().defaultBlockState().trySetValue(CelestialBodyBlock.CELESTIAL_BODY, this.textureName).trySetValue(CelestialBodyBlock.VARIANT, this.modelVariant).trySetValue(CelestialBodyBlock.ALTERNATIVE, Config.COMMON.toggleEasterEggMoon.get());
            this.applyColor = true;

            Matrix4f orbitFrame = new Matrix4f()
                .rotate(Axis.ZP.rotation((float) this.mainBody.ascendingNode))
                .rotate(Axis.XP.rotation((float) this.mainBody.inclination));

            Matrix4f tilt = new Matrix4f()
                .rotate(Axis.XP.rotation((float) this.mainBody.obliquity));

            Matrix4f planetOrientation = new Matrix4f()
                .mul(orbitFrame)
                .mul(tilt);

            Matrix4f observerFrame = new Matrix4f(CelestialObjectHandler.skyboxRotationMatrix);

            this.orientationMatrix = new Matrix4f()
                .mul(observerFrame)
                .mul(planetOrientation);

            this.transformationMatrix = new Matrix4f()
                .translate((float) pos.x, (float) pos.y, (float) pos.z)
                .mul(orientationMatrix)
                .scale((float) diameter)
                .translate(-0.5F, -0.5F, -0.5F);
        }

        public void tick()
        {
            Matrix4f orbitFrame = new Matrix4f()
                .rotate(Axis.ZP.rotation((float) this.mainBody.ascendingNode))
                .rotate(Axis.XP.rotation((float) this.mainBody.inclination));

            Matrix4f tilt = new Matrix4f()
                .rotate(Axis.XP.rotation((float) this.mainBody.obliquity));

            Matrix4f planetOrientation = new Matrix4f()
                .mul(orbitFrame)
                .mul(tilt);

            Matrix4f observerFrame = new Matrix4f(CelestialObjectHandler.skyboxRotationMatrix);

            this.orientationMatrix = new Matrix4f()
                .mul(observerFrame)
                .mul(planetOrientation);

            this.transformationMatrix = new Matrix4f()
                .translate((float) pos.x, (float) pos.y, (float) pos.z)
                .mul(orientationMatrix)
                .scale((float) diameter)
                .translate(-0.5F, -0.5F, -0.5F);
        }

        public void render(BufferBuilder builder, Camera camera, float partialTicks)
        {
            if (pos != null && AHelpers.isWithinAngle(CelestialObjectHandler.lookAngle, pos, CelestialObjectHandler.fovLim))
            {
                drawBody(builder, camera, partialTicks, this);
            }
        }

        @Override
        public boolean getApplyColor()
        {
            return this.applyColor;
        }

        @Override
        public BlockState getBlockModel()
        {
            return this.blockModel;
        }

        @Override
        public String getName()
        {
            return mainBody.name + ": meteroid " + this.id;
        }

        @Override
        public CelestialObjectTypes getType()
        {
            return this.type;
        }

        @Override
        public Color getColor()
        {
            return this.color;
        }

        @Override
        public double getEccentricity()
        {
            return this.mainBody.eccentricity;
        }

        @Override
        public double getInclination()
        {
            return this.mainBody.inclination;
        }

        @Override
        public double getObliquity()
        {
            return this.mainBody.obliquity;
        }

        @Override
        public double getRotationCompletion()
        {
            return this.mainBody.rotationCompletion;
        }

        @Override
        public Vec3 getPos()
        {
            return this.pos;
        }

        @Override
        public List<List<Vec3>> getOrbitPositions()
        {
            return this.mainBody.orbitPositions;
        }

        @Override
        public int getOrbitLineSegments()
        {
            return this.mainBody.orbitLineSegments;
        }

        @Override
        public double getOrbitLineWidth()
        {
            return this.mainBody.orbitLineWidth;
        }

        @Override
        public double getDiameter()
        {
            return this.diameter;
        }

        @Override
        public String getTextureName()
        {
            return this.textureName;
        }

        @Override
        public String getTextureNameFlat()
        {
            return this.textureNameFlat;
        }

        @Override
        public int getModelVariant()
        {
            return this.modelVariant;
        }

        @Override
        public int getPackedLight()
        {
            return this.mainBody.packedLight;
        }

        @Override
        public Vector2d getDayNightCycle()
        {
            return this.mainBody.dayNightCycle;
        }

        @Override
        public CelestialObjectHandler getHandler()
        {
            return this.handler;
        }

        @Override
        public Matrix4f getTransformationMatrix()
        {
            return this.transformationMatrix;
        }

        @Override
        public Matrix4f getOrientationMatrix()
        {
            return this.orientationMatrix;
        }
    }
}
