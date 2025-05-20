package almagest.config;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.dries007.tfc.util.Helpers;

import static almagest.Almagest.*;

public class Config
{
    public static final Config COMMON = register(ModConfig.Type.COMMON, Config::new);

    public static void init() {}

    private static <C> C register(ModConfig.Type type, Function<ForgeConfigSpec.Builder, C> factory)
    {
        Pair<C, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(factory);
        if (!Helpers.BOOTSTRAP_ENVIRONMENT) ModLoadingContext.get().registerConfig(type, specPair.getRight());
        return specPair.getLeft();
    }

    public final ForgeConfigSpec.BooleanValue ignoreFOV;
    public final ForgeConfigSpec.DoubleValue farPlaneClippingDistance;
    public final ForgeConfigSpec.LongValue manualTimeControl;
    public final ForgeConfigSpec.IntValue axisTimeIndex;
    public final ForgeConfigSpec.IntValue axisIndex;
    public final ForgeConfigSpec.IntValue axisRotationIndex;
    public final ForgeConfigSpec.IntValue axisTimeRotationIndex;
    public final ForgeConfigSpec.DoubleValue axisRotation;
    public final ForgeConfigSpec.IntValue axisRotationIndex2;
    public final ForgeConfigSpec.DoubleValue axisRotation2;
    public final ForgeConfigSpec.IntValue axisRotationIndex3;
    public final ForgeConfigSpec.DoubleValue axisRotation3;
    public final ForgeConfigSpec.BooleanValue enableLensFlare;
    public final ForgeConfigSpec.DoubleValue lensFlareIntensity;
    public final ForgeConfigSpec.DoubleValue lensFlareSize;
    public final ForgeConfigSpec.BooleanValue togglePlayerOffset;
    public final ForgeConfigSpec.DoubleValue playerYOffset;
    public final ForgeConfigSpec.DoubleValue seasonOffsetTicks;
    public final ForgeConfigSpec.DoubleValue daysPerYear;
    public final ForgeConfigSpec.DoubleValue equatorLatitude;
    public final ForgeConfigSpec.DoubleValue distanceToPoles;
    public final ForgeConfigSpec.BooleanValue toggleFastTrigMath;
    public final ForgeConfigSpec.IntValue eccentricAnomalyIterations;
    public final ForgeConfigSpec.DoubleValue fovBuffer;
    public final ForgeConfigSpec.IntValue skyboxAxisIndex;
    public final ForgeConfigSpec.DoubleValue skyboxAxisRotation;
    public final ForgeConfigSpec.DoubleValue skyboxXRotation;
    public final ForgeConfigSpec.DoubleValue skyboxYRotation;
    public final ForgeConfigSpec.DoubleValue skyboxZRotation;

    public final ForgeConfigSpec.DoubleValue spaceStartAltitude;
    public final ForgeConfigSpec.DoubleValue spaceHeight;
    public final ForgeConfigSpec.DoubleValue minApparentMagnitude;
    public final ForgeConfigSpec.DoubleValue maxApparentMagnitudeSpyglass;
    public final ForgeConfigSpec.DoubleValue minApparentSize;

    public final ForgeConfigSpec.BooleanValue renderSkybox;
    public final ForgeConfigSpec.DoubleValue DSObrightness;
    public final ForgeConfigSpec.DoubleValue skyboxDistance;

    public final ForgeConfigSpec.BooleanValue renderPlanets;
    public final ForgeConfigSpec.DoubleValue planetOrbitFactor;
    public final ForgeConfigSpec.DoubleValue planetDayFactor;
    public final ForgeConfigSpec.DoubleValue planetSeasonalIntensity;
    public final ForgeConfigSpec.DoubleValue planetDiameterFactor;
    public final ForgeConfigSpec.DoubleValue planetDistanceFactor;
    public final ForgeConfigSpec.BooleanValue displayPlanetNames;
    public final ForgeConfigSpec.DoubleValue planetDisplayNameAngleThreshold;
    public final ForgeConfigSpec.BooleanValue showPlanetOrbits;
    public final ForgeConfigSpec.DoubleValue planetOrbitLineWidth;
    public final ForgeConfigSpec.IntValue planetAxisIndex;
    public final ForgeConfigSpec.DoubleValue planetAxisRotation;
    public final ForgeConfigSpec.DoubleValue planetOrbitColorRed;
    public final ForgeConfigSpec.DoubleValue planetOrbitColorGreen;
    public final ForgeConfigSpec.DoubleValue planetOrbitColorBlue;

    public final ForgeConfigSpec.IntValue planetUVIndexLeft;
    public final ForgeConfigSpec.IntValue planetUVIndexFront;
    public final ForgeConfigSpec.IntValue planetUVIndexRight;
    public final ForgeConfigSpec.IntValue planetUVIndexBack;

    public final ForgeConfigSpec.BooleanValue renderMinorPlanets;
    public final ForgeConfigSpec.DoubleValue minorPlanetOrbitFactor;
    public final ForgeConfigSpec.DoubleValue minorPlanetDayFactor;
    public final ForgeConfigSpec.DoubleValue minorPlanetSeasonalIntensity;
    public final ForgeConfigSpec.DoubleValue minorPlanetDiameterFactor;
    public final ForgeConfigSpec.DoubleValue minorPlanetDistanceFactor;
    public final ForgeConfigSpec.BooleanValue displayMinorPlanetNames;
    public final ForgeConfigSpec.DoubleValue minorPlanetDisplayNameAngleThreshold;
    public final ForgeConfigSpec.BooleanValue showMinorPlanetOrbits;
    public final ForgeConfigSpec.DoubleValue minorPlanetOrbitLineWidth;
    public final ForgeConfigSpec.DoubleValue minorPlanetMinSizeForOrbitLine;
    public final ForgeConfigSpec.DoubleValue minorPlanetMinSizeForRender;
    public final ForgeConfigSpec.DoubleValue minorPlanetOrbitColorRed;
    public final ForgeConfigSpec.DoubleValue minorPlanetOrbitColorGreen;
    public final ForgeConfigSpec.DoubleValue minorPlanetOrbitColorBlue;

    public final ForgeConfigSpec.BooleanValue renderComets;
    public final ForgeConfigSpec.DoubleValue cometOrbitFactor;
    public final ForgeConfigSpec.DoubleValue cometDayFactor;
    public final ForgeConfigSpec.DoubleValue cometSeasonalIntensity;
    public final ForgeConfigSpec.DoubleValue cometDiameterFactor;
    public final ForgeConfigSpec.DoubleValue cometDistanceFactor;
    public final ForgeConfigSpec.BooleanValue displayCometNames;
    public final ForgeConfigSpec.DoubleValue cometDisplayNameAngleThreshold;
    public final ForgeConfigSpec.BooleanValue showCometOrbits;
    public final ForgeConfigSpec.DoubleValue cometOrbitLineWidth;
    public final ForgeConfigSpec.DoubleValue cometMinSizeForOrbitLine;
    public final ForgeConfigSpec.DoubleValue cometMinSizeForRender;
    public final ForgeConfigSpec.DoubleValue cometOrbitColorRed;
    public final ForgeConfigSpec.DoubleValue cometOrbitColorGreen;
    public final ForgeConfigSpec.DoubleValue cometOrbitColorBlue;

    public final ForgeConfigSpec.BooleanValue renderMoons;
    public final ForgeConfigSpec.BooleanValue toggleEasterEggMoon;
    public final ForgeConfigSpec.DoubleValue moonOrbitFactor;
    public final ForgeConfigSpec.DoubleValue moonDayFactor;
    public final ForgeConfigSpec.DoubleValue moonSeasonalIntensity;
    public final ForgeConfigSpec.DoubleValue moonDiameterFactor;
    public final ForgeConfigSpec.DoubleValue moonDistanceFactor;
    public final ForgeConfigSpec.BooleanValue displayMoonNames;
    public final ForgeConfigSpec.DoubleValue moonDisplayNameAngleThreshold;
    public final ForgeConfigSpec.BooleanValue showMoonOrbits;
    public final ForgeConfigSpec.DoubleValue moonOrbitLineWidth;
    public final ForgeConfigSpec.DoubleValue moonMinSizeForOrbitLine;
    public final ForgeConfigSpec.DoubleValue moonMinSizeForRender;
    public final ForgeConfigSpec.DoubleValue moonOrbitColorRed;
    public final ForgeConfigSpec.DoubleValue moonOrbitColorGreen;
    public final ForgeConfigSpec.DoubleValue moonOrbitColorBlue;

    public final ForgeConfigSpec.BooleanValue renderStars;
    public final ForgeConfigSpec.BooleanValue useFancyStarTexture;
    public final ForgeConfigSpec.BooleanValue useLargeStarDataset;
    public final ForgeConfigSpec.BooleanValue displayStarNames;
    public final ForgeConfigSpec.DoubleValue starDistanceAdd;
    public final ForgeConfigSpec.DoubleValue starDistanceMult;
    public final ForgeConfigSpec.DoubleValue starSize;
    public final ForgeConfigSpec.IntValue starTwinkleFrequency;
    public final ForgeConfigSpec.DoubleValue starDisplayNameAngleThreshold;
    public final ForgeConfigSpec.IntValue starUpdateFrequency;

    public final ForgeConfigSpec.BooleanValue renderNebulas;
    public final ForgeConfigSpec.DoubleValue nebulaDistanceAdd;
    public final ForgeConfigSpec.DoubleValue nebulaDistanceMult;
    public final ForgeConfigSpec.BooleanValue displayNebulaNames;
    public final ForgeConfigSpec.DoubleValue nebulaDisplayNameAngleThreshold;
    public final ForgeConfigSpec.DoubleValue nebulaSize;

    public final ForgeConfigSpec.BooleanValue renderGalaxies;
    public final ForgeConfigSpec.DoubleValue galaxyDistanceAdd;
    public final ForgeConfigSpec.DoubleValue galaxyDistanceMult;
    public final ForgeConfigSpec.BooleanValue displayGalaxyNames;
    public final ForgeConfigSpec.DoubleValue galaxyDisplayNameAngleThreshold;
    public final ForgeConfigSpec.DoubleValue galaxySize;

    public final ForgeConfigSpec.BooleanValue renderMeteorShowers;
    public final ForgeConfigSpec.DoubleValue meteorShowerDistanceFactor;
    public final ForgeConfigSpec.DoubleValue meteorShowerDiameterFactor;
    public final ForgeConfigSpec.DoubleValue meteorShowerPopulationFactor;
    public final ForgeConfigSpec.BooleanValue showMeteorShowerOrbits;
    public final ForgeConfigSpec.DoubleValue meteorShowerOrbitLineWidth;
    public final ForgeConfigSpec.DoubleValue meteorShowerOrbitColorRed;
    public final ForgeConfigSpec.DoubleValue meteorShowerOrbitColorGreen;
    public final ForgeConfigSpec.DoubleValue meteorShowerOrbitColorBlue;

    public final ForgeConfigSpec.BooleanValue drawConstellations;
    public final ForgeConfigSpec.BooleanValue drawAllConstellations;
    public final ForgeConfigSpec.BooleanValue displayConstellationNames;
    public final ForgeConfigSpec.DoubleValue constellationDisplayNameAngleThreshold;
    public final ForgeConfigSpec.DoubleValue constellationDrawAngleThreshold;
    public final ForgeConfigSpec.DoubleValue constellationLineDistance;
    public final ForgeConfigSpec.DoubleValue constellationLineWidth;
    public final ForgeConfigSpec.DoubleValue constellationsRed;
    public final ForgeConfigSpec.DoubleValue constellationsGreen;
    public final ForgeConfigSpec.DoubleValue constellationsBlue;

    public final ForgeConfigSpec.BooleanValue displayMeteorShowerNames;
    public final ForgeConfigSpec.DoubleValue meteorShowerDisplayNameAngleThreshold;

    Config(ForgeConfigSpec.Builder innerBuilder)
    {
        Function<String, ForgeConfigSpec.Builder> builder = name -> innerBuilder.translation(MOD_ID + ".config.common." + name);

        ignoreFOV = builder.apply("ignoreFOV").comment("If elements should be rendered regardless of being in view.").define("ignoreFOV", false);
        farPlaneClippingDistance = builder.apply("farPlaneClippingDistance").comment("Far plane clipping distance.").defineInRange("farPlaneClippingDistance", 1.0E38D, 0.0D, Float.MAX_VALUE);
        manualTimeControl = builder.apply("manualTimeControl").comment("Manual control for time. If set to -1, then time will pass as normal. At time = 0 all celestial objects will be at the periapsis irt. their parent object.").defineInRange("manualTimeControl", -1, -1, Long.MAX_VALUE);
        enableLensFlare = builder.apply("enableLensFlare").comment("Toggle lens flare. True = enabled.").define("enableLensFlare", false);
        lensFlareIntensity = builder.apply("lensFlareIntensity").comment("Lens flare intensity.").defineInRange("lensFlareIntensity", 1.0D, 0.0D, 1.0D);
        lensFlareSize = builder.apply("lensFlareSize").comment("Lens flare size.").defineInRange("lensFlareSize", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        togglePlayerOffset = builder.apply("togglePlayerOffset").comment("Toggle if the sun (and thus all its satellites) should be offset irt. the player position? True = enabled.").define("togglePlayerOffset", false);
        playerYOffset = builder.apply("playerYOffset").comment("Player altitude offset.").defineInRange("playerYOffset", 0.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        seasonOffsetTicks = builder.apply("seasonOffsetTicks").comment("Season offset in ticks.").defineInRange("seasonOffsetTicks", 0.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        daysPerYear = builder.apply("daysPerYear").comment("Default days per year.").defineInRange("daysPerYear", 365.25635535, -Double.MAX_VALUE, Double.MAX_VALUE);
        equatorLatitude = builder.apply("equatorLatitude").comment("Latitude of the equator in blocks.").defineInRange("equatorLatitude", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        distanceToPoles = builder.apply("distanceToPoles").comment("Distance to the poles from equator in blocks.").defineInRange("distanceToPoles", 20000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        toggleFastTrigMath = builder.apply("toggleFastTrigMath").comment("Toggle whether to use trigonometry look-up tables for faster logic or actually calculate it. True = fast.").define("toggleFastTrigMath", true);
        eccentricAnomalyIterations = builder.apply("eccentricAnomalyIterations").comment("The amount of iterations to refine the eccentric anomaly.").defineInRange("eccentricAnomalyIterations", 4, 1, Integer.MAX_VALUE);
        fovBuffer = builder.apply("fovBuffer").comment("Extra degrees of FOV to add to the calculations.").defineInRange("fovBuffer", -12.0D, -Double.MAX_VALUE, Double.MAX_VALUE);

        spaceStartAltitude = builder.apply("spaceStartAltitude").comment("The altitude at which space begins.").defineInRange("spaceStartAltitude", 512.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        spaceHeight = builder.apply("spaceHeight").comment("The altitude at which you've reached space.").defineInRange("spaceHeight", 2048.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        minApparentMagnitude = builder.apply("minApparentMagnitude").comment("Minimum apparent magnitude for stars and other objects to render. Lower value indicates a brighter object.").defineInRange("minApparentMagnitude", 6.5D, -Double.MAX_VALUE, Double.MAX_VALUE);
        maxApparentMagnitudeSpyglass = builder.apply("maxApparentMagnitudeSpyglass").comment("Maximum apparent magnitude for stars and other objects to render if using a spyglass. Lower value indicates a brighter object.").defineInRange("maxApparentMagnitudeSpyglass", 10.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        minApparentSize = builder.apply("minApparentSize").comment("Minimum apparent size for objects to render. Lower value for lower threshold.").defineInRange("minApparentSize", 0.01D, -Double.MAX_VALUE, Double.MAX_VALUE);

        innerBuilder.push("deep space objects");
        renderSkybox = builder.apply("renderSkybox").comment("Render the Milky Way skybox?").define("renderSkybox", true);
        DSObrightness = builder.apply("DSObrightness").comment("Brightness factor of deep space objects (DSO).").defineInRange("DSObrightness", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        skyboxDistance = builder.apply("skyboxDistance").comment("Skybox distance (Milky Way).").defineInRange("skyboxDistance", 1800000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        innerBuilder.pop();

        innerBuilder.push("planets");
        renderPlanets = builder.apply("renderPlanets").comment("Render the planets?").define("renderPlanets", true);
        planetOrbitFactor = builder.apply("planetOrbitFactor").comment("Planet orbital period factor. Higher -> longer orbital periods.").defineInRange("planetOrbitFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        planetDayFactor = builder.apply("planetDayFactor").comment("Planet rotation period factor. Higher -> longer days.").defineInRange("planetDayFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        planetSeasonalIntensity = builder.apply("planetSeasonalIntensity").comment("Planet rotation period factor. Higher -> longer days.").defineInRange("planetSeasonalIntensity", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        planetDiameterFactor = builder.apply("planetDiameterFactor").comment("Planet diameter factor.").defineInRange("planetDiameterFactor", 0.002D, -Double.MAX_VALUE, Double.MAX_VALUE);
        planetDistanceFactor = builder.apply("planetDistanceFactor").comment("Planet distance factor.").defineInRange("planetDistanceFactor", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        displayPlanetNames = builder.apply("displayPlanetNames").comment("Display names of planets?").define("displayPlanetNames", true);
        planetDisplayNameAngleThreshold = builder.apply("planetDisplayNameAngleThreshold").comment("Max angle between cursor and planet for its name to display.").defineInRange("planetDisplayNameAngleThreshold", 8.0D, 0.0D, Double.MAX_VALUE);
        showPlanetOrbits = builder.apply("showPlanetOrbits").comment("Show orbits of the planets?").define("showPlanetOrbits", false);
        planetOrbitLineWidth = builder.apply("planetOrbitLineWidth").comment("Orbit line width of the planets.").defineInRange("planetOrbitLineWidth", 35D, 0.0D, Double.MAX_VALUE);
            innerBuilder.push("planet orbit line RGB color");
            planetOrbitColorRed = builder.apply("planetOrbitColorRed").comment("r").defineInRange("planetOrbitColorRed", 0.1D, 0.0D, 1.0D);
            planetOrbitColorGreen = builder.apply("planetOrbitColorGreen").comment("g").defineInRange("planetOrbitColorGreen", 1.0D, 0.0D, 1.0D);
            planetOrbitColorBlue = builder.apply("planetOrbitColorBlue").comment("b").defineInRange("planetOrbitColorBlue", 0.0D, 0.0D, 1.0D);
            innerBuilder.pop();
        innerBuilder.pop();

        innerBuilder.push("minor planets");
        renderMinorPlanets = builder.apply("renderMinorPlanets").comment("Render minor planets?").define("renderMinorPlanets", true);
        minorPlanetOrbitFactor = builder.apply("minorPlanetOrbitFactor").comment("Minor planet orbital period factor. Higher -> longer orbital periods.").defineInRange("minorPlanetOrbitFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        minorPlanetDayFactor = builder.apply("minorPlanetDayFactor").comment("Minor planet rotation period factor. Higher -> longer days.").defineInRange("minorPlanetDayFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        minorPlanetSeasonalIntensity = builder.apply("minorPlanetSeasonalIntensity").comment("Minor planet rotation period factor. Higher -> longer days.").defineInRange("minorPlanetSeasonalIntensity", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        minorPlanetDiameterFactor = builder.apply("minorPlanetDiameterFactor").comment("Minor planet diameter factor.").defineInRange("minorPlanetDiameterFactor", 0.02D, -Double.MAX_VALUE, Double.MAX_VALUE);
        minorPlanetDistanceFactor = builder.apply("minorPlanetDistanceFactor").comment("Minor planet distance factor.").defineInRange("minorPlanetDistanceFactor", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        displayMinorPlanetNames = builder.apply("displayMinorPlanetNames").comment("Display names of minor planet?").define("displayMinorPlanetNames", true);
        minorPlanetDisplayNameAngleThreshold = builder.apply("minorPlanetDisplayNameAngleThreshold").comment("Max angle between cursor and minor planet for its name to display.").defineInRange("minorPlanetDisplayNameAngleThreshold", 8.0D, 0.0D, Double.MAX_VALUE);
        showMinorPlanetOrbits = builder.apply("showMinorPlanetOrbits").comment("Show orbits of the minor planets and asteroids?").define("showMinorPlanetOrbits", false);
        minorPlanetOrbitLineWidth = builder.apply("minorPlanetOrbitLineWidth").comment("Orbit line width of the minor planets.").defineInRange("minorPlanetOrbitLineWidth", 20D, 0.0D, Double.MAX_VALUE);
        minorPlanetMinSizeForOrbitLine = builder.apply("minorPlanetMinSizeForOrbitLine").comment("Minimum size for minor planets to show their orbital paths.").defineInRange("minorPlanetMinSizeForOrbitLine", 60.0D, 0.0D, Double.MAX_VALUE);
        minorPlanetMinSizeForRender = builder.apply("minorPlanetMinSizeForRender").comment("Minimum size for minor planets to render.").defineInRange("minorPlanetMinSizeForRender", 60.0D, 0.0D, Double.MAX_VALUE);
            innerBuilder.push("minor planet orbit line RGB color");
            minorPlanetOrbitColorRed = builder.apply("minorPlanetOrbitColorRed").comment("r").defineInRange("minorPlanetOrbitColorRed", 1.0D, 0.0D, 1.0D);
            minorPlanetOrbitColorGreen = builder.apply("minorPlanetOrbitColorGreen").comment("g").defineInRange("minorPlanetOrbitColorGreen", 0.85D, 0.0D, 1.0D);
            minorPlanetOrbitColorBlue = builder.apply("minorPlanetOrbitColorBlue").comment("b").defineInRange("minorPlanetOrbitColorBlue", 0.0D, 0.0D, 1.0D);
            innerBuilder.pop();
        innerBuilder.pop();

        innerBuilder.push("comets");
        renderComets = builder.apply("renderComets").comment("Render comets?").define("renderComets", false);
        cometOrbitFactor = builder.apply("cometOrbitFactor").comment("Comet orbital period factor. Higher -> longer orbital periods.").defineInRange("cometOrbitFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        cometDayFactor = builder.apply("cometDayFactor").comment("Comet rotation period factor. Higher -> longer days.").defineInRange("cometDayFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        cometSeasonalIntensity = builder.apply("cometSeasonalIntensity").comment("Comet rotation period factor. Higher -> longer days.").defineInRange("cometSeasonalIntensity", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        cometDiameterFactor = builder.apply("cometDiameterFactor").comment("Comet diameter factor.").defineInRange("cometDiameterFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        cometDistanceFactor = builder.apply("cometDistanceFactor").comment("Comet distance factor.").defineInRange("cometDistanceFactor", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        displayCometNames = builder.apply("displayCometNames").comment("Display names of comet?").define("displayCometNames", true);
        cometDisplayNameAngleThreshold = builder.apply("cometDisplayNameAngleThreshold").comment("Max angle between cursor and comet for its name to display.").defineInRange("cometDisplayNameAngleThreshold", 10.0D, 0.0D, Double.MAX_VALUE);
        showCometOrbits = builder.apply("showCometOrbits").comment("Show orbits of comets?").define("showCometOrbits", false);
        cometOrbitLineWidth = builder.apply("cometOrbitLineWidth").comment("Orbit line width of the comets.").defineInRange("cometOrbitLineWidth", 5.0D, 0.0D, Double.MAX_VALUE);
        cometMinSizeForOrbitLine = builder.apply("cometMinSizeForOrbitLine").comment("Minimum size for comets to show their orbital paths.").defineInRange("cometMinSizeForOrbitLine", 1.0D, 0.0D, Double.MAX_VALUE);
        cometMinSizeForRender = builder.apply("cometMinSizeForRender").comment("Minimum size for comets to render.").defineInRange("cometMinSizeForRender", 1.0D, 0.0D, Double.MAX_VALUE);
            innerBuilder.push("comet orbit line RGB color");
            cometOrbitColorRed = builder.apply("cometOrbitColorRed").comment("r").defineInRange("cometOrbitColorRed", 0.0D, 0.0D, 1.0D);
            cometOrbitColorGreen = builder.apply("cometOrbitColorGreen").comment("g").defineInRange("cometOrbitColorGreen", 0.15D, 0.0D, 1.0D);
            cometOrbitColorBlue = builder.apply("cometOrbitColorBlue").comment("b").defineInRange("cometOrbitColorBlue", 1.0D, 0.0D, 1.0D);
            innerBuilder.pop();
        innerBuilder.pop();

        innerBuilder.push("moons");
        renderMoons = builder.apply("renderMoons").comment("Render moons?").define("renderMoons", true);
        toggleEasterEggMoon = builder.apply("toggleEasterEggMoon").comment("Use alternative texture for the moon?").define("toggleEasterEggMoon", false);
        moonOrbitFactor = builder.apply("moonOrbitFactor").comment("Moon orbital period factor. Higher -> longer orbital periods.").defineInRange("moonOrbitFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        moonDayFactor = builder.apply("moonDayFactor").comment("Moon rotation period factor. Higher -> longer days.").defineInRange("moonDayFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        moonSeasonalIntensity = builder.apply("moonSeasonalIntensity").comment("Moon rotation period factor. Higher -> longer days.").defineInRange("moonSeasonalIntensity", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        moonDiameterFactor = builder.apply("moonDiameterFactor").comment("Moon diameter factor.").defineInRange("moonDiameterFactor", 0.05D, -Double.MAX_VALUE, Double.MAX_VALUE);
        moonDistanceFactor = builder.apply("moonDistanceFactor").comment("Moon distance factor.").defineInRange("moonDistanceFactor", 600000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        displayMoonNames = builder.apply("displayMoonNames").comment("Display names of moons?").define("displayMoonNames", true);
        moonDisplayNameAngleThreshold = builder.apply("moonDisplayNameAngleThreshold").comment("Max angle between cursor and moon for its name to display.").defineInRange("moonDisplayNameAngleThreshold", 4.0D, 0.0D, Double.MAX_VALUE);
        showMoonOrbits = builder.apply("showMoonOrbits").comment("Show orbits of moons?").define("showMoonOrbits", false);
        moonOrbitLineWidth = builder.apply("moonOrbitLineWidth").comment("Orbit line width of the moons.").defineInRange("moonOrbitLineWidth", 15.0D, 0.0D, Double.MAX_VALUE);
        moonMinSizeForOrbitLine = builder.apply("moonMinSizeForOrbitLine").comment("Minimum size for moons to show their orbital paths.").defineInRange("moonMinSizeForOrbitLine", 215.0D, 0.0D, Double.MAX_VALUE);
        moonMinSizeForRender = builder.apply("moonMinSizeForRender").comment("Minimum size for moons to render.").defineInRange("moonMinSizeForRender", 215.0D, 0.0D, Double.MAX_VALUE);
            innerBuilder.push("moon orbit line RGB color");
            moonOrbitColorRed = builder.apply("moonOrbitColorRed").comment("r").defineInRange("moonOrbitColorRed", 0.3D, 0.0D, 1.0D);
            moonOrbitColorGreen = builder.apply("moonOrbitColorGreen").comment("g").defineInRange("moonOrbitColorGreen", 1.0D, 0.0D, 1.0D);
            moonOrbitColorBlue = builder.apply("moonOrbitColorBlue").comment("b").defineInRange("moonOrbitColorBlue", 0.0D, 0.0D, 1.0D);
            innerBuilder.pop();
        innerBuilder.pop();

        innerBuilder.push("stars");
        renderStars = builder.apply("renderStars").comment("Render stars?").define("renderStars", true);
        useFancyStarTexture = builder.apply("useFancyStarTexture").comment("Use fancy star textures?").define("useFancyStarTexture", false);
        useLargeStarDataset = builder.apply("useLargeStarDataset").comment("Use the huge star dataset?").define("useLargeStarDataset", true);
        displayStarNames = builder.apply("displayStarNames").comment("Display names of stars?").define("displayStarNames", true);
        starDistanceAdd = builder.apply("starDistanceAdd").comment("Star distance addition.").defineInRange("starDistanceAdd", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        starDistanceMult = builder.apply("starDistanceMult").comment("Star distance multiplier.").defineInRange("starDistanceMult", 50.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        starSize = builder.apply("starSize").comment("Star size.").defineInRange("starSize", 4000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        starTwinkleFrequency = builder.apply("starTwinkleFrequency").comment("How fast the stars twinkle. Set to 0 to disable.").defineInRange("starTwinkleFrequency", 2000, 0, Integer.MAX_VALUE);
        starDisplayNameAngleThreshold = builder.apply("starDisplayNameAngleThreshold").comment("Max angle between cursor and star for its name to display.").defineInRange("starDisplayNameAngleThreshold", 0.1D, 0.0D, Double.MAX_VALUE);
        starUpdateFrequency = builder.apply("starUpdateFrequency").comment("How fast and often stars should update. Lower = faster.").defineInRange("starUpdateFrequency", 1, 1, Integer.MAX_VALUE);
        innerBuilder.pop();

        innerBuilder.push("nebulas");
        renderNebulas = builder.apply("renderNebulas").comment("Render nebulas?").define("renderNebulas", true);
        nebulaDistanceAdd = builder.apply("nebulaDistanceAdd").comment("Nebula distance addition.").defineInRange("nebulaDistanceAdd", 1000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        nebulaDistanceMult = builder.apply("nebulaDistanceMult").comment("Nebula distance multiplier.").defineInRange("nebulaDistanceMult", 1000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        displayNebulaNames = builder.apply("displayNebulaNames").comment("Display names of nebulae?").define("displayNebulaNames", true);
        nebulaDisplayNameAngleThreshold = builder.apply("nebulaDisplayNameAngleThreshold").comment("Max angle between cursor and nebula for its name to display.").defineInRange("nebulaDisplayNameAngleThreshold", 2.0D, 0.0D, Double.MAX_VALUE);
        nebulaSize = builder.apply("nebulaSize").comment("Nebula size.").defineInRange("nebulaSize", 50000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        innerBuilder.pop();

        innerBuilder.push("galaxys");
        renderGalaxies = builder.apply("renderGalaxies").comment("Render galaxies?").define("renderGalaxies", true);
        galaxyDistanceAdd = builder.apply("galaxyDistanceAdd").comment("Galaxy distance addition.").defineInRange("galaxyDistanceAdd", 15.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        galaxyDistanceMult = builder.apply("galaxyDistanceMult").comment("Galaxy distance multiplier.").defineInRange("galaxyDistanceMult", 50000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        displayGalaxyNames = builder.apply("displayGalaxyNames").comment("Display names of galaxies?").define("displayGalaxyNames", true);
        galaxyDisplayNameAngleThreshold = builder.apply("galaxyDisplayNameAngleThreshold").comment("Max angle between cursor and galaxy for its name to display.").defineInRange("galaxyDisplayNameAngleThreshold", 3.0D, 0.0D, Double.MAX_VALUE);
        galaxySize = builder.apply("galaxySize").comment("Galaxy size.").defineInRange("galaxySize", 100000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        innerBuilder.pop();

        innerBuilder.push("meteor showers");
        renderMeteorShowers = builder.apply("renderMeteorShowers").comment("Render meteor showers?").define("renderMeteorShowers", false);
        meteorShowerDistanceFactor = builder.apply("meteorShowerDistanceFactor").comment("Meteor showers distance factor.").defineInRange("meteorShowerDistanceFactor", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        meteorShowerDiameterFactor = builder.apply("meteorShowerDiameterFactor").comment("Meteor showers diameter factor.").defineInRange("meteorShowerDiameterFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
        meteorShowerPopulationFactor = builder.apply("meteorShowerPopulationFactor").comment("Meteor showers population factor.").defineInRange("meteorShowerPopulationFactor", 0.05D, 0.0D, Double.MAX_VALUE);
        displayMeteorShowerNames = builder.apply("displayMeteorShowerNames").comment("Display names of meteor showers?").define("displayMeteorShowerNames", true);
        meteorShowerDisplayNameAngleThreshold = builder.apply("meteorShowerDisplayNameAngleThreshold").comment("Max angle between cursor and meteor shower members for its name to display.").defineInRange("meteorShowerDisplayNameAngleThreshold", 0.5D, 0.0D, Double.MAX_VALUE);
        showMeteorShowerOrbits = builder.apply("showMeteorShowerOrbits").comment("Show orbits of meteor showers?").define("showMeteorShowerOrbits", false);
        meteorShowerOrbitLineWidth = builder.apply("meteorShowerOrbitLineWidth").comment("Orbit line width of the meteor showers.").defineInRange("meteorShowerOrbitLineWidth", 15.0D, 0.0D, Double.MAX_VALUE);
            innerBuilder.push("meteor shower orbit line RGB color");
            meteorShowerOrbitColorRed = builder.apply("meteorShowerOrbitColorRed").comment("r").defineInRange("meteorShowerOrbitColorRed", 0.0D, 0.0D, 1.0D);
            meteorShowerOrbitColorGreen = builder.apply("meteorShowerOrbitColorGreen").comment("g").defineInRange("meteorShowerOrbitColorGreen", 0.8D, 0.0D, 1.0D);
            meteorShowerOrbitColorBlue = builder.apply("meteorShowerOrbitColorBlue").comment("b").defineInRange("meteorShowerOrbitColorBlue", 1.0D, 0.0D, 1.0D);
            innerBuilder.pop();
        innerBuilder.pop();

        innerBuilder.push("constellations");
        drawConstellations = builder.apply("drawConstellations").comment("Draw constellations?").define("drawConstellations", true);
        drawAllConstellations = builder.apply("drawAllConstellations").comment("Draw all constellations at once?").define("drawAllConstellations", false);
        displayConstellationNames = builder.apply("displayConstellationNames").comment("Display names of constellations?").define("displayConstellationNames", true);
        constellationDisplayNameAngleThreshold = builder.apply("constellationDisplayNameAngleThreshold").comment("Max angle between cursor and constellations for their names to display.").defineInRange("constellationDisplayNameAngleThreshold", 6.0D, 0.0D, Double.MAX_VALUE);
        constellationDrawAngleThreshold = builder.apply("constellationDrawAngleThreshold").comment("Max angle between cursor and star in constellation for its lines to become opaque.").defineInRange("constellationDrawAngleThreshold", 20.0D, 0.0D, Double.MAX_VALUE);
        constellationLineDistance = builder.apply("constellationLineDistance").comment("Constellation line distance from viewer.").defineInRange("constellationLineDistance", 10000.0D, 0.0D, Double.MAX_VALUE);
        constellationLineWidth = builder.apply("constellationLineWidth").comment("Constellation line width.").defineInRange("constellationLineWidth", 10.0D, 0.0D, Double.MAX_VALUE);
            innerBuilder.push("constellation line RGB color");
            constellationsRed = builder.apply("constellationsRed").comment("Constellation red color").defineInRange("constellationsRed", 0.1D, 0.0D, 1.0D);
            constellationsGreen = builder.apply("constellationsGreen").comment("Constellation green color").defineInRange("constellationsGreen", 0.6D, 0.0D, 1.0D);
            constellationsBlue = builder.apply("constellationsBlue").comment("Constellation blue color").defineInRange("constellationsBlue", 0.2D, 0.0D, 1.0D);
            innerBuilder.pop();
        innerBuilder.pop();

        innerBuilder.push("debug");
        axisTimeIndex = builder.apply("axisTimeIndex").comment("Axis time index.").defineInRange("axisTimeIndex", 0, 0, 5);
        axisIndex = builder.apply("axisIndex").comment("Axis index.").defineInRange("axisIndex", 0, 0, 5);
        axisRotationIndex = builder.apply("axisRotationIndex").comment("Axis rotation index.").defineInRange("axisRotationIndex", 0, 0, 5);
        axisTimeRotationIndex = builder.apply("axisTimeRotationIndex").comment("Axis time rotation index.").defineInRange("axisTimeRotationIndex", 0, 0, 2);
        axisRotation = builder.apply("axisRotation").comment("Axis rotation.").defineInRange("axisRotation", 0, -360.0D, 360.0D);
        axisRotationIndex2 = builder.apply("axisRotationIndex2").comment("Axis rotation index 2.").defineInRange("axisRotationIndex2", 0, 0, 5);
        axisRotation2 = builder.apply("axisRotation2").comment("Axis rotation 2.").defineInRange("axisRotation2", 0, -360.0D, 360.0D);
        axisRotationIndex3 = builder.apply("axisRotationIndex3").comment("Axis rotation index 3.").defineInRange("axisRotationIndex3", 0, 0, 5);
        axisRotation3 = builder.apply("axisRotation3").comment("Axis rotation 3.").defineInRange("axisRotation3", 0, -360.0D, 360.0D);
        planetAxisIndex = builder.apply("planetAxisIndex").comment("Axis index.").defineInRange("planetAxisIndex", 0, 0, 5);
        planetAxisRotation = builder.apply("planetAxisRotation").comment("Axis rotation.").defineInRange("planetAxisRotation", 0, -360.0D, 360.0D);
        planetUVIndexLeft = builder.apply("planetUVIndexLeft").comment("UV index.").defineInRange("planetUVIndexLeft", 0, 0, 3);
        planetUVIndexFront = builder.apply("planetUVIndexFront").comment("UV index.").defineInRange("planetUVIndexFront", 0, 0, 3);
        planetUVIndexRight = builder.apply("planetUVIndexRight").comment("UV index.").defineInRange("planetUVIndexRight", 0, 0, 3);
        planetUVIndexBack = builder.apply("planetUVIndexBack").comment("UV index.").defineInRange("planetUVIndexBack", 0, 0, 3);
        skyboxAxisIndex = builder.apply("skyboxAxisIndex").comment("Skybox axis index.").defineInRange("skyboxAxisIndex", 0, 0, 5);
        skyboxAxisRotation = builder.apply("skyboxAxisRotation").comment("Skybox axis rotation.").defineInRange("skyboxAxisRotation", 10, -Double.MAX_VALUE, Double.MAX_VALUE);
        skyboxXRotation = builder.apply("skyboxXRotation").comment("Skybox x rotation.").defineInRange("skyboxXRotation", 0, -360.0D, 360.0D);
        skyboxYRotation = builder.apply("skyboxYRotation").comment("Skybox y rotation.").defineInRange("skyboxYRotation", 0, -360.0D, 360.0D);
        skyboxZRotation = builder.apply("skyboxZRotation").comment("Skybox z rotation.").defineInRange("skyboxZRotation", 0, -360.0D, 360.0D);
        innerBuilder.pop();

        //innerBuilder.pop();
    }
}