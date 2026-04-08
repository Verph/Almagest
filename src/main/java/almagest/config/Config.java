package almagest.config;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import net.neoforged.neoforge.common.ModConfigSpec;

import static almagest.Almagest.*;

public class Config
{
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static
    {
        Pair<Common, ModConfigSpec> commonPair =
            new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }

    public static class Common
    {
        public final ModConfigSpec.BooleanValue printListAllCelestialObjects;
        public final ModConfigSpec.BooleanValue ignoreFOV;
        public final ModConfigSpec.DoubleValue farPlaneClippingDistance;
        public final ModConfigSpec.LongValue manualTimeControl;
        public final ModConfigSpec.IntValue axisTimeIndex;
        public final ModConfigSpec.IntValue axisIndex;
        public final ModConfigSpec.IntValue axisRotationIndex;
        public final ModConfigSpec.IntValue axisTimeRotationIndex;
        public final ModConfigSpec.DoubleValue axisRotation;
        public final ModConfigSpec.IntValue axisRotationIndex2;
        public final ModConfigSpec.DoubleValue axisRotation2;
        public final ModConfigSpec.IntValue axisRotationIndex3;
        public final ModConfigSpec.DoubleValue axisRotation3;
        public final ModConfigSpec.BooleanValue drawEclipticPlane;
        public final ModConfigSpec.BooleanValue drawCelestialEquator;
        public final ModConfigSpec.BooleanValue drawGalacticPlane;
        public final ModConfigSpec.ConfigValue<String> manualObserverBody;
        public final ModConfigSpec.BooleanValue enableManualSeason;
        public final ModConfigSpec.DoubleValue manualSeasonPercentage;
        public final ModConfigSpec.BooleanValue enableManualTimeOfDay;
        public final ModConfigSpec.DoubleValue manualTimeOfDayPercentage;

        public final ModConfigSpec.BooleanValue enableLensFlare;
        public final ModConfigSpec.DoubleValue lensFlareIntensity;
        public final ModConfigSpec.DoubleValue lensFlareSize;
        public final ModConfigSpec.BooleanValue togglePlayerOffset;
        public final ModConfigSpec.DoubleValue playerYOffset;
        public final ModConfigSpec.DoubleValue seasonOffsetTicks;
        public final ModConfigSpec.DoubleValue daysPerYear;
        public final ModConfigSpec.DoubleValue equatorLatitude;
        public final ModConfigSpec.DoubleValue distanceToPoles;
        public final ModConfigSpec.BooleanValue toggleFastTrigMath;
        public final ModConfigSpec.IntValue eccentricAnomalyIterations;
        public final ModConfigSpec.DoubleValue fovBuffer;
        public final ModConfigSpec.IntValue skyboxAxisIndex;
        public final ModConfigSpec.DoubleValue skyboxAxisRotation;
        public final ModConfigSpec.IntValue skyboxAxisIndex2;
        public final ModConfigSpec.DoubleValue skyboxAxisRotation2;
        public final ModConfigSpec.IntValue skyboxAxisIndex3;
        public final ModConfigSpec.DoubleValue skyboxAxisRotation3;
        public final ModConfigSpec.DoubleValue skyboxXRotation;
        public final ModConfigSpec.DoubleValue skyboxYRotation;
        public final ModConfigSpec.DoubleValue skyboxZRotation;
        public final ModConfigSpec.DoubleValue universeScale;
        public final ModConfigSpec.DoubleValue auScale;
        public final ModConfigSpec.IntValue starVBOSize;
        public final ModConfigSpec.IntValue starAsyncBatchSize;
        public final ModConfigSpec.IntValue subBucketSize;

        public final ModConfigSpec.BooleanValue renderSkybox;
        public final ModConfigSpec.DoubleValue skyboxApparentMagnitude;
        public final ModConfigSpec.DoubleValue skyboxBrightness;
        public final ModConfigSpec.DoubleValue skyboxDistance;

        public final ModConfigSpec.BooleanValue renderPlanets;
        public final ModConfigSpec.DoubleValue planetOrbitFactor;
        public final ModConfigSpec.DoubleValue planetDayFactor;
        public final ModConfigSpec.DoubleValue planetSeasonalIntensity;
        public final ModConfigSpec.DoubleValue planetDiameterFactor;
        public final ModConfigSpec.DoubleValue sunDiameterFactor;
        public final ModConfigSpec.DoubleValue planetDistanceFactor;
        public final ModConfigSpec.BooleanValue displayPlanetNames;
        public final ModConfigSpec.DoubleValue planetDisplayNameAngleThreshold;
        public final ModConfigSpec.BooleanValue showPlanetOrbits;
        public final ModConfigSpec.DoubleValue planetOrbitLineWidth;
        public final ModConfigSpec.DoubleValue planetMinSizeForOrbitLine;
        public final ModConfigSpec.IntValue planetAxisIndex;
        public final ModConfigSpec.DoubleValue planetAxisRotation;
        public final ModConfigSpec.DoubleValue planetOrbitColorRed;
        public final ModConfigSpec.DoubleValue planetOrbitColorGreen;
        public final ModConfigSpec.DoubleValue planetOrbitColorBlue;

        public final ModConfigSpec.IntValue planetUVIndexLeft;
        public final ModConfigSpec.IntValue planetUVIndexFront;
        public final ModConfigSpec.IntValue planetUVIndexRight;
        public final ModConfigSpec.IntValue planetUVIndexBack;

        public final ModConfigSpec.BooleanValue renderMinorPlanets;
        public final ModConfigSpec.DoubleValue minorPlanetOrbitFactor;
        public final ModConfigSpec.DoubleValue minorPlanetDayFactor;
        public final ModConfigSpec.DoubleValue minorPlanetSeasonalIntensity;
        public final ModConfigSpec.DoubleValue minorPlanetDiameterFactor;
        public final ModConfigSpec.DoubleValue minorPlanetDistanceFactor;
        public final ModConfigSpec.BooleanValue displayMinorPlanetNames;
        public final ModConfigSpec.DoubleValue minorPlanetDisplayNameAngleThreshold;
        public final ModConfigSpec.BooleanValue showMinorPlanetOrbits;
        public final ModConfigSpec.DoubleValue minorPlanetOrbitLineWidth;
        public final ModConfigSpec.DoubleValue minorPlanetMinSizeForOrbitLine;
        public final ModConfigSpec.DoubleValue minorPlanetMinSizeForRender;
        public final ModConfigSpec.DoubleValue minorPlanetOrbitColorRed;
        public final ModConfigSpec.DoubleValue minorPlanetOrbitColorGreen;
        public final ModConfigSpec.DoubleValue minorPlanetOrbitColorBlue;

        public final ModConfigSpec.BooleanValue renderComets;
        public final ModConfigSpec.DoubleValue cometOrbitFactor;
        public final ModConfigSpec.DoubleValue cometDayFactor;
        public final ModConfigSpec.DoubleValue cometSeasonalIntensity;
        public final ModConfigSpec.DoubleValue cometDiameterFactor;
        public final ModConfigSpec.DoubleValue cometDistanceFactor;
        public final ModConfigSpec.BooleanValue displayCometNames;
        public final ModConfigSpec.DoubleValue cometDisplayNameAngleThreshold;
        public final ModConfigSpec.BooleanValue showCometOrbits;
        public final ModConfigSpec.DoubleValue cometOrbitLineWidth;
        public final ModConfigSpec.DoubleValue cometMinSizeForOrbitLine;
        public final ModConfigSpec.DoubleValue cometMinSizeForRender;
        public final ModConfigSpec.DoubleValue cometOrbitColorRed;
        public final ModConfigSpec.DoubleValue cometOrbitColorGreen;
        public final ModConfigSpec.DoubleValue cometOrbitColorBlue;

        public final ModConfigSpec.BooleanValue renderMoons;
        public final ModConfigSpec.BooleanValue toggleEasterEggMoon;
        public final ModConfigSpec.DoubleValue moonOrbitFactor;
        public final ModConfigSpec.DoubleValue moonDayFactor;
        public final ModConfigSpec.DoubleValue moonSeasonalIntensity;
        public final ModConfigSpec.DoubleValue moonDiameterFactor;
        public final ModConfigSpec.DoubleValue moonDistanceFactor;
        public final ModConfigSpec.BooleanValue displayMoonNames;
        public final ModConfigSpec.DoubleValue moonDisplayNameAngleThreshold;
        public final ModConfigSpec.BooleanValue showMoonOrbits;
        public final ModConfigSpec.DoubleValue moonOrbitLineWidth;
        public final ModConfigSpec.DoubleValue moonMinSizeForOrbitLine;
        public final ModConfigSpec.DoubleValue moonMinSizeForRender;
        public final ModConfigSpec.DoubleValue moonOrbitColorRed;
        public final ModConfigSpec.DoubleValue moonOrbitColorGreen;
        public final ModConfigSpec.DoubleValue moonOrbitColorBlue;

        public final ModConfigSpec.BooleanValue renderStars;
        public final ModConfigSpec.BooleanValue skipGaia;
        public final ModConfigSpec.IntValue maxGaiaIndex;
        public final ModConfigSpec.BooleanValue displayStarNames;
        public final ModConfigSpec.DoubleValue minApparentMagnitudeToLoad;
        public final ModConfigSpec.IntValue maxStarsToLoad;
        public final ModConfigSpec.DoubleValue starDistanceAdd;
        public final ModConfigSpec.DoubleValue starDistanceMult;
        public final ModConfigSpec.DoubleValue starTwinkleFrequency;
        public final ModConfigSpec.DoubleValue starDisplayNameAngleThreshold;
        public final ModConfigSpec.DoubleValue starUpdateFrequency;
        public final ModConfigSpec.DoubleValue starUpdateExponent;
        public final ModConfigSpec.DoubleValue minApparentMagnitude;
        public final ModConfigSpec.DoubleValue maxApparentMagnitudeSpyglass;
        public final ModConfigSpec.DoubleValue minAngularSize;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.DoubleValue gamma;
        public final ModConfigSpec.DoubleValue exposure;
        public final ModConfigSpec.DoubleValue slope;

        public final ModConfigSpec.BooleanValue renderNebulas;
        public final ModConfigSpec.DoubleValue nebulaDistanceAdd;
        public final ModConfigSpec.DoubleValue nebulaDistanceMult;
        public final ModConfigSpec.BooleanValue displayNebulaNames;
        public final ModConfigSpec.DoubleValue nebulaDisplayNameAngleThreshold;
        public final ModConfigSpec.DoubleValue nebulaSize;

        public final ModConfigSpec.BooleanValue renderGalaxies;
        public final ModConfigSpec.DoubleValue galaxyDistanceAdd;
        public final ModConfigSpec.DoubleValue galaxyDistanceMult;
        public final ModConfigSpec.BooleanValue displayGalaxyNames;
        public final ModConfigSpec.DoubleValue galaxyDisplayNameAngleThreshold;
        public final ModConfigSpec.DoubleValue galaxySize;

        public final ModConfigSpec.BooleanValue renderMeteorShowers;
        public final ModConfigSpec.DoubleValue meteorShowerDistanceFactor;
        public final ModConfigSpec.DoubleValue meteorShowerDiameterFactor;
        public final ModConfigSpec.DoubleValue meteorShowerPopulationFactor;
        public final ModConfigSpec.BooleanValue showMeteorShowerOrbits;
        public final ModConfigSpec.DoubleValue meteorShowerOrbitLineWidth;
        public final ModConfigSpec.DoubleValue meteorShowerOrbitColorRed;
        public final ModConfigSpec.DoubleValue meteorShowerOrbitColorGreen;
        public final ModConfigSpec.DoubleValue meteorShowerOrbitColorBlue;

        public final ModConfigSpec.BooleanValue drawConstellations;
        public final ModConfigSpec.BooleanValue drawAllConstellations;
        public final ModConfigSpec.BooleanValue displayConstellationNames;
        public final ModConfigSpec.DoubleValue constellationDisplayNameAngleThreshold;
        public final ModConfigSpec.DoubleValue constellationDrawAngleThreshold;
        public final ModConfigSpec.DoubleValue constellationLineDistance;
        public final ModConfigSpec.DoubleValue constellationLineWidth;
        public final ModConfigSpec.DoubleValue constellationsRed;
        public final ModConfigSpec.DoubleValue constellationsGreen;
        public final ModConfigSpec.DoubleValue constellationsBlue;

        public final ModConfigSpec.BooleanValue displayMeteorShowerNames;
        public final ModConfigSpec.DoubleValue meteorShowerDisplayNameAngleThreshold;

        public Common(ModConfigSpec.Builder innerBuilder)
        {
            Function<String, ModConfigSpec.Builder> builder = name -> innerBuilder.translation(MOD_ID + ".config.common." + name);

            printListAllCelestialObjects = builder.apply("printListAllCelestialObjects").comment("If lists of all celestial objects should be printed to the log. This includes, stars, planets etc.").define("printListAllCelestialObjects", false);
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
            universeScale = builder.apply("universeScale").comment("Universe scale factor. Setting to 1 is equal to 1 km/block, where 1 block is 1 meter.").defineInRange("universeScale", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            auScale = builder.apply("auScale")
                .comment(
                    "Scale factor for converting astronomical units (AU) into Minecraft blocks.\n" +
                    "This controls the visual size of the entire universe.\n\n" +
                    " - 1 AU is the average distance from Earth to the Sun.\n" +
                    " - The game performs all orbital calculations internally in AU.\n" +
                    " - This value determines how many Minecraft blocks represent 1 AU.\n\n" +
                    "Examples:\n" +
                    " - 1.0  = 1 AU equals 1 block (extremely tiny universe)\n" +
                    " - 1000 = 1 AU equals 1,000 blocks\n" +
                    " - 10000 = Recommended default. 1 AU equals 10,000 blocks.\n\n" +
                    "Larger values make planets and orbits appear farther apart.\n" +
                    "Smaller values compress the universe into a tighter space.\n"
                )
                .defineInRange("auScale", 1000.0D, 1.0D, Double.MAX_VALUE);
            manualObserverBody = builder.apply("manualObserverBody")
                .comment("Manually override the observer body. Leave empty to use the dimension name.")
                .define("manualObserverBody", "");
            enableManualSeason = builder.apply("enableManualSeason")
                .comment("If true, seasons are manually controlled using manualSeasonPercentage instead of orbital position.")
                .define("enableManualSeason", false);
            manualSeasonPercentage = builder.apply("manualSeasonPercentage")
                .comment("Manual season control as a percentage of the year (0.0 = winter solstice, 0.25 = spring equinox, 0.5 = summer solstice, 0.75 = autumn equinox).")
                .defineInRange("manualSeasonPercentage", 0.0D, 0.0D, 1.0D);
            enableManualTimeOfDay = builder.apply("enableManualTimeOfDay")
                .comment("If true, time of day is manually controlled using manualTimeOfDayPercentage instead of world time.")
                .define("enableManualTimeOfDay", false);
            manualTimeOfDayPercentage = builder.apply("manualTimeOfDayPercentage")
                .comment("Manual time of day as a percentage (0.0 = midnight, 0.25 = sunrise, 0.5 = noon, 0.75 = sunset).")
                .defineInRange("manualTimeOfDayPercentage", 0.0D, 0.0D, 1.0D);


            drawEclipticPlane = builder.apply("drawEclipticPlane").comment("If the ecliptic plane of the current observing body should be rendered.").define("drawEclipticPlane", false);
            drawCelestialEquator = builder.apply("drawCelestialEquator").comment("If the celestial equator plane of the current observing body should be rendered.").define("drawCelestialEquator", false);
            drawGalacticPlane = builder.apply("drawGalacticPlane").comment("If the galactic plane should be rendered.").define("drawGalacticPlane", false);

            innerBuilder.push("deep space objects");
            renderSkybox = builder.apply("renderSkybox").comment("Render the skybox?").define("renderSkybox", true);
            skyboxApparentMagnitude = builder.apply("skyboxApparentMagnitude").comment("Apparent magnitude of the skybox.").defineInRange("skyboxApparentMagnitude", 5.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            skyboxBrightness = builder.apply("skyboxBrightness").comment("Skybox alpha brightness.").defineInRange("skyboxBrightness", 1.0D, 0.0D, Double.MAX_VALUE);
            skyboxDistance = builder.apply("skyboxDistance").comment("Skybox distance.").defineInRange("skyboxDistance", 1800000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            innerBuilder.pop();

            innerBuilder.push("planets");
            renderPlanets = builder.apply("renderPlanets").comment("Render the planets?").define("renderPlanets", true);
            planetOrbitFactor = builder.apply("planetOrbitFactor").comment("Planet orbital period factor. Higher -> longer orbital periods.").defineInRange("planetOrbitFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            planetDayFactor = builder.apply("planetDayFactor").comment("Planet rotation period factor. Higher -> longer days.").defineInRange("planetDayFactor", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            planetSeasonalIntensity = builder.apply("planetSeasonalIntensity").comment("Planet rotation period factor. Higher -> longer days.").defineInRange("planetSeasonalIntensity", 1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            planetDiameterFactor = builder.apply("planetDiameterFactor").comment("Planet diameter factor.").defineInRange("planetDiameterFactor", 0.002D, -Double.MAX_VALUE, Double.MAX_VALUE);
            sunDiameterFactor = builder.apply("sunDiameterFactor").comment("Sun diameter factor.").defineInRange("sunDiameterFactor", 1000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            planetDistanceFactor = builder.apply("planetDistanceFactor").comment("Planet distance factor.").defineInRange("planetDistanceFactor", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);
            displayPlanetNames = builder.apply("displayPlanetNames").comment("Display names of planets?").define("displayPlanetNames", true);
            planetDisplayNameAngleThreshold = builder.apply("planetDisplayNameAngleThreshold").comment("Max angle between cursor and planet for its name to display.").defineInRange("planetDisplayNameAngleThreshold", 8.0D, 0.0D, Double.MAX_VALUE);
            showPlanetOrbits = builder.apply("showPlanetOrbits").comment("Show orbits of the planets?").define("showPlanetOrbits", false);
            planetOrbitLineWidth = builder.apply("planetOrbitLineWidth").comment("Orbit line width of the planets.").defineInRange("planetOrbitLineWidth", 35D, 0.0D, Double.MAX_VALUE);
            planetMinSizeForOrbitLine = builder.apply("planetMinSizeForOrbitLine").comment("Minimum size for planets to show their orbital paths.").defineInRange("planetMinSizeForOrbitLine", 100.0D, 0.0D, Double.MAX_VALUE);
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

                renderStars = builder.apply("renderStars")
                    .comment(
                        "Render stars?\n" +
                        "Disable to hide all star rendering."
                    )
                    .define("renderStars", true);

                skipGaia = builder.apply("skipGaia")
                    .comment(
                        "Skip loading the Gaia star dataset.\n" +
                        "Useful if you want only custom or non‑Gaia stars."
                    )
                    .define("skipGaia", true);

                maxGaiaIndex = builder.apply("maxGaiaIndex")
                    .comment(
                        "Maximum Gaia index depth to load.\n" +
                        "Gaia files are sorted by apparent magnitude (brightest first).\n" +
                        "Each index adds ~250,000 additional stars.\n" +
                        "Index 0 contains the brightest stars in the dataset."
                    )
                    .defineInRange("maxGaiaIndex", 0, 0, Integer.MAX_VALUE);

                displayStarNames = builder.apply("displayStarNames")
                    .comment(
                        "Display the names of stars when looking at them.\n" +
                        "Only applies when scoping."
                    )
                    .define("displayStarNames", true);

                minApparentMagnitudeToLoad = builder.apply("minApparentMagnitudeToLoad")
                    .comment(
                        "Minimum apparent magnitude required for a star to be loaded.\n" +
                        "Higher values include dimmer stars.\n" +
                        "Set to 0 to load none, or -1 to load all stars."
                    )
                    .defineInRange("minApparentMagnitudeToLoad", -1.0D, -Double.MAX_VALUE, Double.MAX_VALUE);

                maxStarsToLoad = builder.apply("maxStarsToLoad")
                    .comment(
                        "Maximum number of stars to load and initialize.\n" +
                        "Set to 0 to load none, or -1 to load all available stars."
                    )
                    .defineInRange("maxStarsToLoad", -1, -1, Integer.MAX_VALUE);

                starDistanceAdd = builder.apply("starDistanceAdd")
                    .comment(
                        "Additive distance offset applied to all stars.\n" +
                        "Used to push stars farther away or bring them closer."
                    )
                    .defineInRange("starDistanceAdd", 10000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);

                starDistanceMult = builder.apply("starDistanceMult")
                    .comment(
                        "Multiplicative distance factor applied to all stars.\n" +
                        "Useful for scaling the perceived depth of the sky."
                    )
                    .defineInRange("starDistanceMult", 0.0D, -Double.MAX_VALUE, Double.MAX_VALUE);

                starTwinkleFrequency = builder.apply("starTwinkleFrequency")
                    .comment(
                        "How fast stars twinkle.\n" +
                        "Set to 0 to disable twinkling entirely."
                    )
                    .defineInRange("starTwinkleFrequency", 0.001D, 0, Double.MAX_VALUE);

                starDisplayNameAngleThreshold = builder.apply("starDisplayNameAngleThreshold")
                    .comment(
                        "Maximum angular distance between the cursor and a star\n" +
                        "for its name to appear on screen."
                    )
                    .defineInRange("starDisplayNameAngleThreshold", 0.07D, 0.0D, Double.MAX_VALUE);

                starUpdateFrequency = builder.apply("starUpdateFrequency")
                    .comment(
                        "Base update interval for star ticking.\n" +
                        "Measured in render ticks (10 render ticks = 1 game tick).\n" +
                        "Higher values mean slower updates.\n" +
                        "\n" +
                        "If set to 0, stars update every render tick, equivalent to 10x normal game tick rate.\n" +
                        "The final update rate per brightness bucket is: frequency * (bucketIndex + 1)^starUpdateExponent."
                    )
                    .defineInRange("starUpdateFrequency", 1, -Double.MAX_VALUE, Double.MAX_VALUE);

                starUpdateExponent = builder.apply("starUpdateExponent")
                    .comment(
                        "Controls how strongly update frequency scales with star brightness.\n" +
                        "1.0 = linear (default, current behavior).\n" +
                        "0.0 = completely flat (all stars update equally).\n" +
                        "0.1-0.7 = gently flattened curve.\n" +
                        "2.0+ = very steep curve."
                    )
                    .defineInRange("starUpdateExponent", 1.25D, -Double.MAX_VALUE, Double.MAX_VALUE);

                scale = builder.apply("scale")
                    .comment(
                        "Base scale factor controlling the overall size of stars.\n" +
                        "Higher values make all stars appear larger."
                    )
                    .defineInRange("scale", 30.0D, 0.0D, Double.MAX_VALUE);

                gamma = builder.apply("gamma")
                    .comment(
                        "Perceptual compression factor.\n" +
                        "Lower values reduce the size contrast between bright and faint stars."
                    )
                    .defineInRange("gamma", 0.5D, 0.0D, Double.MAX_VALUE);

                exposure = builder.apply("exposure")
                    .comment(
                        "Exposure multiplier.\n" +
                        "Increases the size of all stars uniformly, simulating a brighter sky."
                    )
                    .defineInRange("exposure", 3.5D, -Double.MAX_VALUE, Double.MAX_VALUE);

                slope = builder.apply("slope")
                    .comment(
                        "Slope factor controlling how quickly star size changes with magnitude.\n" +
                        "Higher values exaggerate brightness differences."
                    )
                    .defineInRange("slope", 0.8D, -Double.MAX_VALUE, Double.MAX_VALUE);

                minApparentMagnitude = builder.apply("minApparentMagnitude")
                    .comment(
                        "Minimum apparent magnitude for stars to render.\n" +
                        "Lower values correspond to brighter stars."
                    )
                    .defineInRange("minApparentMagnitude", 6.5D, -Double.MAX_VALUE, Double.MAX_VALUE);

                maxApparentMagnitudeSpyglass = builder.apply("maxApparentMagnitudeSpyglass")
                    .comment(
                        "Maximum apparent magnitude for stars when using a spyglass.\n" +
                        "Lower values correspond to brighter stars."
                    )
                    .defineInRange("maxApparentMagnitudeSpyglass", 1000.0D, -Double.MAX_VALUE, Double.MAX_VALUE);

                minAngularSize = builder.apply("minApparentSize")
                    .comment(
                        "Minimum apparent angular size for stars to render.\n" +
                        "Lower values allow smaller (fainter) stars to appear."
                    )
                    .defineInRange("minApparentSize", 0.0D, -Double.MAX_VALUE, Double.MAX_VALUE);

                starVBOSize = builder.apply("starVBOSize")
                    .comment(
                        "Maximum number of stars stored in a single Vertex Buffer Object (VBO).\n" +
                        "Higher values reduce the number of VBOs and draw calls,\n" +
                        "but increase GPU memory usage per VBO.\n" +
                        "Lower values reduce memory spikes but increase draw calls."
                    )
                    .defineInRange("starVBOSize", 5000, 1, Integer.MAX_VALUE);

                starAsyncBatchSize = builder.apply("starAsyncBatchSize")
                    .comment(
                        "How many stars to process per asynchronous batch.\n" +
                        "Larger batches reduce scheduling overhead but may cause frame spikes.\n" +
                        "Recommended range: 5000–50000.\n" +
                        "Extremely large values may cause crashes or OS soft‑locks."
                    )
                    .defineInRange("starAsyncBatchSize", 15000, 1000, Integer.MAX_VALUE);

                subBucketSize = builder.apply("subBucketSize")
                    .comment(
                        "Maximum number of stars stored in a single sub‑bucket.\n" +
                        "Smaller values increase update staggering and reduce frame spikes.\n" +
                        "Larger values reduce memory overhead but may cause occasional stutters.\n" +
                        "Recommended range: 2000–20000."
                    )
                    .defineInRange("subBucketSize", 10000, 1, Integer.MAX_VALUE);

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
                skyboxAxisIndex2 = builder.apply("skyboxAxisIndex2").comment("Skybox axis index.").defineInRange("skyboxAxisIndex2", 0, 0, 5);
                skyboxAxisRotation2 = builder.apply("skyboxAxisRotation2").comment("Skybox axis rotation.").defineInRange("skyboxAxisRotation2", 10, -Double.MAX_VALUE, Double.MAX_VALUE);
                skyboxAxisIndex3 = builder.apply("skyboxAxisIndex3").comment("Skybox axis index.").defineInRange("skyboxAxisIndex3", 0, 0, 5);
                skyboxAxisRotation3 = builder.apply("skyboxAxisRotation3").comment("Skybox axis rotation.").defineInRange("skyboxAxisRotation3", 10, -Double.MAX_VALUE, Double.MAX_VALUE);
                skyboxXRotation = builder.apply("skyboxXRotation").comment("Skybox x rotation.").defineInRange("skyboxXRotation", 0, -360.0D, 360.0D);
                skyboxYRotation = builder.apply("skyboxYRotation").comment("Skybox y rotation.").defineInRange("skyboxYRotation", 0, -360.0D, 360.0D);
                skyboxZRotation = builder.apply("skyboxZRotation").comment("Skybox z rotation.").defineInRange("skyboxZRotation", 0, -360.0D, 360.0D);

            innerBuilder.pop();
        }
    }
}