package almagest.client.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import almagest.Almagest;
import almagest.client.CelestialObjectHandler;
import almagest.client.data.CelestialObjectTypes.Body.*;
import almagest.client.particle.CelestialObject;
import almagest.client.particle.Star;
import almagest.config.Config;
import almagest.util.AHelpers;
import almagest.util.Color;
import almagest.util.ColorUtils;
import almagest.util.FastMath;
import almagest.util.Nature;
import almagest.util.PlanetHelpers;

public enum CelestialObjectTypes
{
    BARYCENTER(new Color(Config.COMMON.minorPlanetOrbitColorRed.get().floatValue(), Config.COMMON.minorPlanetOrbitColorGreen.get().floatValue(), Config.COMMON.minorPlanetOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.minorPlanetOrbitLineWidth.get()),
    BARYCENTER_PLANET(new Color(Config.COMMON.planetOrbitColorRed.get().floatValue(), Config.COMMON.planetOrbitColorGreen.get().floatValue(), Config.COMMON.planetOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.planetOrbitLineWidth.get()),
    STAR(),
    PLANET(new Color(Config.COMMON.planetOrbitColorRed.get().floatValue(), Config.COMMON.planetOrbitColorGreen.get().floatValue(), Config.COMMON.planetOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.planetOrbitLineWidth.get()),
    DWARFPLANET(new Color(Config.COMMON.minorPlanetOrbitColorRed.get().floatValue(), Config.COMMON.minorPlanetOrbitColorGreen.get().floatValue(), Config.COMMON.minorPlanetOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.minorPlanetOrbitLineWidth.get()),
    MOON(new Color(Config.COMMON.moonOrbitColorRed.get().floatValue(), Config.COMMON.moonOrbitColorGreen.get().floatValue(), Config.COMMON.moonOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.moonOrbitLineWidth.get()),
    DWARFMOON(new Color(Config.COMMON.moonOrbitColorRed.get().floatValue(), Config.COMMON.moonOrbitColorGreen.get().floatValue(), Config.COMMON.moonOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.moonOrbitLineWidth.get()),
    ASTEROID(new Color(Config.COMMON.meteorShowerOrbitColorRed.get().floatValue(), Config.COMMON.meteorShowerOrbitColorGreen.get().floatValue(), Config.COMMON.meteorShowerOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.meteorShowerOrbitLineWidth.get()),
    COMET(new Color(Config.COMMON.cometOrbitColorRed.get().floatValue(), Config.COMMON.cometOrbitColorGreen.get().floatValue(), Config.COMMON.cometOrbitColorBlue.get().floatValue(), 1.0F), Config.COMMON.cometOrbitLineWidth.get()),
    NEBULA(),
    GALAXY(),
    CONSTELLATION();

    public static final CelestialObjectTypes[] VALUES = values();

    public final String serializedName;
    public final Color orbitColor;
    public final double orbitWidthFactor;

    CelestialObjectTypes(Color orbitColor, double orbitWidthFactor)
    {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.orbitColor = orbitColor;
        this.orbitWidthFactor = orbitWidthFactor;
    }

    CelestialObjectTypes()
    {
        this(new Color(1.0F, 1.0F, 1.0F, 1.0F), 0.0F);
    }

    public String getSerializedName()
    {
        return serializedName;
    }

    public Color getOrbitColor()
    {
        return orbitColor;
    }

    public double getOrbitWidthFactor()
    {
        return orbitWidthFactor;
    }

    public static Optional<CelestialObjectTypes> fromString(String typeName)
    {
        return Arrays.stream(VALUES)
            .filter(t -> t.serializedName.equalsIgnoreCase(typeName))
            .findFirst();
    }

    public class CelestialData
    {
        public static final Object2ObjectMap<CelestialData, List<StarData>> PARENT_STARS_CACHE = new Object2ObjectOpenHashMap<>();
        public static final Object2ObjectMap<CelestialData, List<CelestialData>> PARENT_CELESTIAL_DATA_CACHE = new Object2ObjectOpenHashMap<>();
        public static final Object2ObjectMap<CelestialData, List<CelestialObject>> PARENT_CELESTIAL_OBJECTS_CACHE = new Object2ObjectOpenHashMap<>();

        private final long id;
        private final CelestialObjectTypes type;
        private final List<String> names;
        private final boolean hasModel;
        private final boolean hasRing;
        private final double mass;
        private final double radius;
        private final double gravity;
        private final double temperature;
        private final double surfaceHeight;
        private final Color color;
        private final String parentBody;
        private StarData star;
        private final Trail trail;
        private final Atmosphere atmosphere;
        private final Body body;
        private final Ring ring;
        private final Life life;
        private final Orbit orbit;
        private final Season season;

        public CelestialData(long id, CelestialObjectTypes type, List<String> names, boolean hasModel, boolean hasRing, double mass, double radius, double gravity, double temperature, double surfaceHeight, Color color, String parentBody, StarData star, Trail trail, Atmosphere atmosphere, Body body, Ring ring, Life life, Orbit orbit, Season season)
        {
            this.id = id;
            this.type = type != null ? type : CelestialObjectTypes.PLANET;
            this.names = names != null ? new ArrayList<>(names) : new ArrayList<>();
            this.hasModel = hasModel;
            this.hasRing = hasRing;
            this.mass = mass;
            this.radius = radius;
            this.gravity = gravity;
            this.temperature = temperature;
            this.surfaceHeight = surfaceHeight;
            this.color = color;
            this.parentBody = parentBody != null ? parentBody : "";
            this.star = star;
            this.trail = trail != null ? trail : new Trail(false, "Unknown", 0.0, 10000);
            this.atmosphere = atmosphere != null ? atmosphere : new Atmosphere(0.0, 0.0, 0.0, 0.0, 0.0);
            this.body = body != null ? body : new Body(
                0.142,
                2.91695,
                0.0,
                0.0,
                Body.BodyTemperature.UNKNOWN,
                Body.BodySurface.UNKNOWN,
                Body.BodySize.UNKNOWN,
                Body.BodyClass.UNKNOWN
            );
            this.ring = ring != null ? ring : new Ring(
                0.0D,
                0.0D,
                0.0D
            );
            this.life = life != null ? life : new Life(
                new ArrayList<>(List.of(Life.LifeClass.UNKNOWN)),
                new ArrayList<>(List.of(Life.LifeType.UNKNOWN)),
                new ArrayList<>(List.of(Life.LifeBiome.UNKNOWN))
            );
            this.orbit = orbit != null ? orbit : new Orbit(
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                20
            );
            this.season = season != null ? season : new Season(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public long getId()
        {
            return id;
        }

        public CelestialObjectTypes type()
        {
            return type;
        }

        public List<String> getNames()
        {
            return names;
        }

        public boolean hasModel()
        {
            return hasModel;
        }

        public boolean hasRing()
        {
            return hasRing;
        }

        public double getMass()
        {
            return mass;
        }

        public double getRadius()
        {
            return radius;
        }

        public double getGravity()
        {
            return gravity;
        }

        public double getTemperature()
        {
            return temperature;
        }

        public double getSurfaceHeight()
        {
            return surfaceHeight;
        }

        public String getParentBody()
        {
            return parentBody;
        }

        public StarData getStar()
        {
            return star;
        }

        public Trail getTrail()
        {
            return trail;
        }

        public Atmosphere getAtmosphere()
        {
            return atmosphere;
        }

        public Body getBody()
        {
            return body;
        }

        public Ring getRing()
        {
            return ring;
        }

        public Life getLife()
        {
            return life;
        }

        public Orbit getOrbit()
        {
            return orbit;
        }

        public Season getSeason()
        {
            return season;
        }

        public double getOrbitDistanceFactor()
        {
            switch (this.type)
            {
                case MOON, DWARFMOON:
                    return Config.COMMON.moonDistanceFactor.get();
                case DWARFPLANET:
                    return Config.COMMON.minorPlanetDistanceFactor.get();
                case COMET:
                    return Config.COMMON.cometDistanceFactor.get();
                case ASTEROID:
                    return Config.COMMON.meteorShowerDistanceFactor.get();
                default:
                    return Config.COMMON.planetDistanceFactor.get();
            }
        }

        public double getDiameterFactor()
        {
            switch (this.type)
            {
                case MOON, DWARFMOON:
                    return Config.COMMON.moonDiameterFactor.get();
                case DWARFPLANET:
                    return Config.COMMON.minorPlanetDiameterFactor.get();
                case COMET:
                    return Config.COMMON.cometDiameterFactor.get();
                case ASTEROID:
                    return Config.COMMON.meteorShowerDiameterFactor.get();
                case STAR, NEBULA, GALAXY:
                    return Nature.SOLAR_RADIUS * Config.COMMON.sunDiameterFactor.get();
                default:
                    return Config.COMMON.planetDiameterFactor.get();
            }
        }

        public void setStarData(StarData newStar)
        {
            this.star = newStar;
        }

        public Vec3 getObliquityRotation()
        {
            if (this.getParentBody().equalsIgnoreCase("unknown"))
            {
                return new Vec3(0.0D, 0.0D, 0.0D);
            }
            double xRot = 0.0D;
            Optional<CelestialData> parentBody = this.getParent();
            if (parentBody.isPresent())
            {
                xRot = -Math.toRadians(parentBody.get().getBody().getObliquity());
            }
            return new Vec3(xRot, 0.0D, 0.0D);
        }

        /**
         * @return The diameter of the body in gigameters, adjusted for planet diameter factor (config) or solar size (if a star).
         */
        public double getDiameter()
        {
            return this.radius * 2.0D * getDiameterFactor() * Config.COMMON.universeScale.get();
        }

        public double getOrbitLineWidth(double observerDistance, double scaleFactor)
        {
            double radiusAdj = this.getRadius();
            double referenceRadius = 1.0D; // Earth's radius in km
            //double referenceDistance = 1000.0D;
            double baseWidth = 1.0D; // Minimum line width (meters)
            //double maxWidth = 100.0D; // Maximum line width (meters)
            // Base width based on planet radius
            double baseLineWidth = baseWidth + scaleFactor * (radiusAdj / referenceRadius);
            // Scale by distance to maintain constant apparent size
            double adjustedWidth = baseLineWidth * observerDistance;
            return Math.max(adjustedWidth, baseWidth);
        }

        public boolean hasCustomModel()
        {
            return this.getNames().stream().anyMatch(name -> {
                List<String> matchedNames = new ArrayList<>();
                boolean hasMatch = CelestialDataManager.getBlockStateData().stream().anyMatch(modelName -> {
                    boolean matches = name.equalsIgnoreCase(modelName);
                    if (matches)
                    {
                        matchedNames.add(modelName);
                    }
                    return matches;
                });
                if (hasMatch && Config.COMMON.printListAllCelestialObjects.get())
                {
                    Almagest.LOGGER.debug("{}: Matched with names: {}", this.getNames().get(0), matchedNames.toString());
                }
                return hasMatch;
            });
        }

        public boolean isMoon()
        {
            return this.type().equals(CelestialObjectTypes.DWARFMOON) || this.type().equals(CelestialObjectTypes.MOON);
        }

        public boolean isStar()
        {
            return this.type().equals(CelestialObjectTypes.STAR) || this.type().equals(CelestialObjectTypes.NEBULA) || this.type().equals(CelestialObjectTypes.GALAXY);
        }

        public boolean isBarycenter()
        {
            return this.type().equals(CelestialObjectTypes.BARYCENTER) || this.type().equals(CelestialObjectTypes.BARYCENTER_PLANET);
        }

        public boolean isPlanet()
        {
            return this.type().equals(CelestialObjectTypes.PLANET);
        }

        public Color getColor()
        {
            double[] color = this.color.toArray();
            double sum = 0;
            for (double value : color)
            {
                sum += value;
            }

            if (sum <= 0.0D)
            {
                return this.type().equals(STAR) ? ColorUtils.tEffToRGB(this.getTemperature()) : new Color(1.0D, 1.0D, 1.0D, 1.0D);
            }
            return ColorUtils.getColor(color);
        }

        public Optional<StarData> getStarEquivalent()
        {
            return this.getNames().isEmpty() || this.getNames().stream().anyMatch(name -> name.equalsIgnoreCase("unknown")) ? Optional.empty() : CelestialDataManager.getStarData(this.getNames());
        }

        public Optional<CelestialData> getParent()
        {
            return this.getParentBody().equalsIgnoreCase("unknown") ? Optional.empty() : CelestialDataManager.getPlanetData(this.getParentBody());
        }

        public boolean isDirectParentToObserver()
        {
            if (CelestialObjectHandler.observerObject == null)
            {
                return false;
            }
            CelestialData observer = CelestialObjectHandler.observerObject.body;
            return observer.getParentBody() == null || observer.getParentBody().equalsIgnoreCase("unknown") ? false : this.getNames().stream().anyMatch(obj -> obj.equalsIgnoreCase(CelestialObjectHandler.observerObject.body.getParentBody()));
        }

        public List<StarData> getParentStars()
        {
            List<StarData> cached = PARENT_STARS_CACHE.get(this);
            if (cached != null)
            {
                return cached;
            }

            List<StarData> result = new ArrayList<>();
            Set<String> visited = new HashSet<>();

            collectParentStars(this.parentBody, result, visited);

            PARENT_STARS_CACHE.put(this, result);
            return result;
        }

        public void collectParentStars(String parent, List<StarData> out, Set<String> visited)
        {
            if (parent == null || parent.equalsIgnoreCase("unknown"))
            {
                return;
            }

            String key = parent.toLowerCase(Locale.ROOT);
            if (!visited.add(key))
            {
                return;
            }

            Optional<StarData> star = CelestialDataManager.getStarData(parent);
            if (star.isPresent())
            {
                out.add(star.get());
                return;
            }

            Optional<CelestialData> body = CelestialDataManager.getPlanetData(parent);
            body.ifPresent(b -> collectParentStars(b.parentBody, out, visited));
        }

        public List<CelestialObject> getParentObjects()
        {
            if (PARENT_CELESTIAL_OBJECTS_CACHE.containsKey(this))
            {
                return PARENT_CELESTIAL_OBJECTS_CACHE.getOrDefault(this, new ArrayList<>());
            }
            List<CelestialData> parentBodiesData = getParents();
            List<CelestialObject> parentBodies = new ArrayList<>();
            List<String> parentBodyNames = new ArrayList<>();
            for (CelestialData data : parentBodiesData)
            {
                parentBodyNames.add(data.getNames().get(0));
                Optional<CelestialObject> body = CelestialDataManager.getCelestialObject(data);
                if (body.isPresent())
                {
                    parentBodies.add(body.get());
                }
            }
            if (Config.COMMON.printListAllCelestialObjects.get())
            {
                Almagest.LOGGER.debug("{}: Parent bodies found: {}", this.getNames().get(0), parentBodyNames);
            }
            PARENT_CELESTIAL_OBJECTS_CACHE.put(this, parentBodies);
            return parentBodies;
        }

        public List<CelestialData> getParents()
        {
            return PARENT_CELESTIAL_DATA_CACHE.computeIfAbsent(this, key -> {
                List<CelestialData> parentBodies = new ArrayList<>();
                Set<String> visited = new HashSet<>();
                collectParentBodies(((CelestialData) key).getParentBody(), parentBodies, visited);
                return parentBodies;
            });
        }

        public void collectParentBodies(String parentName, List<CelestialData> parentBodies, Set<String> visited)
        {
            if (parentName == null || visited.stream().anyMatch(name -> name.equalsIgnoreCase(parentName)) || parentName.equalsIgnoreCase("unknown"))
            {
                return;
            }
            visited.add(parentName.toLowerCase(Locale.ROOT));

            // Check if parent is a valid celestial object
            Optional<CelestialData> celestialData = CelestialDataManager.getPlanetData(parentName);
            if (celestialData.isPresent())
            {
                parentBodies.add(celestialData.get());

                // Bodies may have parent bodies (e.g., binary systems)
                collectParentBodies(celestialData.get().getParentBody(), parentBodies, visited);
            }
        }

        /**
         * Calculates the equilibrium temperature of the planet based on the luminosity of its parent stars
         * and the planet's orbital parameters and albedo.
         * @return The equilibrium temperature in Kelvin.
         */
        public double getEquilibriumTemperature()
        {
            List<StarData> parentStars = getParentStars();
            if (parentStars.isEmpty())
            {
                return 0.0D; // No parent stars, return default temperature
            }

            double totalFlux = 0.0D;
            for (StarData star : parentStars)
            {
                // Calculate luminosity (in solar units) from StarData
                double luminosity = star.getLuminosity(); // Already in solar units

                // Get distance to the star (semi-major axis in km)
                double distance = (this.getOrbit().getSemiMajorAxis() / Config.COMMON.universeScale.get()) * Nature.GM_TO_AU * Nature.AU_TO_KM;

                // Convert distance to meters for calculations
                distance *= 1000.0D;

                // Calculate flux received by the planet (W/m²)
                // Flux = L / (4 * π * d²), where L is in watts (luminosity * solar luminosity constant)
                double flux = (luminosity * Nature.SOLAR_LUMINOSITY) / (4.0D * Math.PI * distance * distance);
                totalFlux += flux;
            }

            // Equilibrium temperature: T = [(1 - A) * F / (4 * σ)]^(1/4)
            // A = albedo, F = total flux, σ = Stefan-Boltzmann constant
            double albedo = this.getBody().getAlbedo();
            double sigma = Nature.STEFAN_BOLTZMANN;
            double equilibriumTemp = Math.pow((1.0D - albedo) * totalFlux / (4.0D * sigma), 0.25D);

            return equilibriumTemp;
        }

        /**
         * Estimates the surface temperature of the planet, accounting for greenhouse effects
         * using the atmosphere's greenhouse parameter (in degrees Celsius).
         * @return The estimated surface temperature in Kelvin.
         */
        public double getSurfaceTemperature()
        {
            if (this.isStar())
            {
                return this.getStarEquivalent().isPresent() ? this.getStarEquivalent().get().getEffectiveTemperature() : this.getTemperature() > 0.0D ? this.getTemperature() : 6000.0D;
            }
            double equilibriumTemp = getEquilibriumTemperature();
            double greenhouseEffect = this.getAtmosphere().getGreenhouse(); // Greenhouse effect in Celsius

            // Convert greenhouse effect to Kelvin and add to equilibrium temperature
            return equilibriumTemp + greenhouseEffect;
        }

        public BodyTemperature getBodyTemperature()
        {
            if (this.isStar())
            {
                return BodyTemperature.UNKNOWN;
            }
            double temp = getSurfaceTemperature();
            if (temp <= 0.0D) return BodyTemperature.UNKNOWN;
            if (temp < 100.0D) return BodyTemperature.FRIGID;    // Below -173°C
            if (temp < 200.0D) return BodyTemperature.COLD;      // -173°C to -73°C
            if (temp < 250.0D) return BodyTemperature.COOL;      // -73°C to -23°C
            if (temp < 288.0D) return BodyTemperature.TEMPERATE; // -23°C to 15°C
            if (temp < 323.0D) return BodyTemperature.WARM;      // 15°C to 50°C
            if (temp < 373.0D) return BodyTemperature.HOT;       // 50°C to 100°C
            return BodyTemperature.TORRID;                       // Above 100°C
        }

        public BodyClass getBodyClassRough()
        {
            if (this.isStar())
            {
                return BodyClass.UNKNOWN;
            }
            double radius = this.getRadius();                           // Radius in km
            double earthRadius = 6378.14D;                              // Earth's radius in km
            if (radius <= 0.0D) return BodyClass.UNKNOWN;
            if (radius < 500.0D) return BodyClass.ASTEROID;             // < 500 km (typical for asteroids)
            if (radius < 2.0D * earthRadius) return BodyClass.TERRA;    // 500–12742 km (terrestrial planets)
            if (radius < 10.0D * earthRadius) return BodyClass.NEPTUNE; // 12742–63710 km (ice giants)
            return BodyClass.JUPITER;                                   // > 63710 km (gas giants)
        }

        public BodySize getBodySizePrefix()
        {
            if (this.isStar())
            {
                return BodySize.UNKNOWN;
            }
            double radius = this.getRadius();                           // Radius in km
            double earthRadius = 6378.14D;                              // Earth's radius in km
            if (radius <= 0.0D) return BodySize.UNKNOWN;
            if (radius < 0.1D * earthRadius) return BodySize.MICRO;     // < 637.1 km
            if (radius < 0.5D * earthRadius) return BodySize.MINI;      // 637.1–3185.5 km
            if (radius < 0.8D * earthRadius) return BodySize.SUB;       // 3185.5–5096.8 km
            if (radius < 2.0D * earthRadius) return BodySize.STANDARD;  // 5096.8–12742 km
            if (radius < 5.0D * earthRadius) return BodySize.SUPER;     // 12742–31855 km
            return BodySize.MEGA;                                       // > 31855 km
        }
    }

    public class Trail
    {
        private final boolean hasTrail;
        private final String name;
        private final double density;
        private final int population;

        public Trail(boolean hasTrail, String name, double density, int population)
        {
            this.hasTrail = hasTrail;
            this.name = name != null ? name : "Unknown";
            this.density = density;
            this.population = population;
        }

        public boolean getHasTrail()
        {
            return hasTrail;
        }

        public String getName()
        {
            return name;
        }

        public double getDensity()
        {
            return density;
        }

        public int getPopulation()
        {
            return population;
        }

        public double getRandomSize(RandomSource random, double range, double min)
        {
            return (random.nextDouble() * (range - min)) + min;
        }

        public double getOrbitDistanceFactor(double x)
        {
            return getOrbitDistanceFactor(x, 1.0D, 3.0D, 0.25D, 2.0D);
        }

        /**
         * Computes a nonlinear orbit distance factor based on the input value x.
         * 
         * The result is shaped by a skewed power curve, favoring values near 1 and bounded between a minimum and maximum.
         * As x increases from 0 to 1:
         * - The formula emphasizes values near 1 using a power transformation: a + b · (1 - x^c)^d
         * - For x > 0.9, the value decays exponentially: the output is multiplied by exp(-(x - 0.9))
         *   This enforces a steep drop-off past the threshold to suppress higher values.
         *
         * @param x Normalized input value in the range [0, 1]
         * @param a Base offset (e.g. 0.7) — lower bound of the output
         * @param b Scale factor for how far output stretches above 'a'
         * @param c Exponent that controls steepness of curvature (higher = sharper drop)
         * @param d Power applied to transformed value to add nonlinearity
         * @return A skewed, bounded double value favoring 1, decaying rapidly past x = 0.9
         */
        public double getOrbitDistanceFactor(double x, double a, double b, double c, double d)
        {
            double y = a + b * Math.pow(1.0D - Math.pow(x, c), d);
            return x > 0.9D ? y : y * Math.exp(-(x - 0.9D));
        }

        /**
         * Calculates the adjusted population to maintain constant density regardless of universe scale factor.
         * The population is scaled based on the ratio of the current universe scale factor to the default scale factor,
         * ensuring the density (population per unit volume) remains consistent.
         * 
         * @return The adjusted population value.
         */
        public int getAdjustedPopulation()
        {
            return (int) Math.round(this.getPopulation() * Math.pow(Config.COMMON.universeScale.get() / 1.0D, 3.0D) * this.getDensity());
        }
    }

    public class Atmosphere
    {
        private final double height;
        private final double greenhouse;
        private final double pressure;
        private final double density;
        private final double opacity;

        // Constants for physics-based scale height
        private static final double EARTH_BLOCK_TO_M = 30.0D; // 1 block ≈ 30 m
        private static final double EARTH_SURFACE_PRESSURE_PA = 101325.0D; // 1 atm in Pa

        public Atmosphere(double height, double greenhouse, double pressure, double density, double opacity)
        {
            this.height = height;
            this.greenhouse = greenhouse;
            this.pressure = pressure;
            this.density = density;
            this.opacity = opacity;
        }

        public double getHeight()
        {
            return height;
        }

        public double getGreenhouse()
        {
            return greenhouse;
        }

        public double getPressure()
        {
            return pressure;
        }

        public double getDensity()
        {
            return density;
        }

        public double getOpacity()
        {
            return opacity;
        }

        /**
         * Returns the pressure scale height (in blocks) without using molar mass.
         * Uses H ≈ p0 / (ρ0 * g).
         *
         * @param gravity surface gravity in m/s²
         * @return scale height in blocks
         */
        public double getPressureScaleHeight(double gravity)
        {
            double p0 = pressure * EARTH_SURFACE_PRESSURE_PA; // convert atm → Pa
            double rho0 = density; // kg/m³
            double H_meters = p0 / (rho0 * gravity);
            return H_meters / EARTH_BLOCK_TO_M; // meters → blocks
        }

        /**
         * Returns the normalized atmospheric density relative to Earth (1.2929 kg/m³).
         */
        public double getNormalizedDensity()
        {
            return density / 1.2929D;
        }

        /**
         * Returns the effective transmission factor at a given altitude.
         * Uses exponential decay of pressure with altitude.
         *
         * @param altitude the current Y position
         * @param gravity surface gravity in m/s²
         * @return transmission factor between 0.0D and 1.0D
         */
        public double getTransmission(double altitude, double gravity, double surfaceHeight)
        {
            double relAlt = Math.max(0.0D, altitude - surfaceHeight);
            double scaleHeightBlocks = getPressureScaleHeight(gravity);
            double pressureFactor = Math.exp(-relAlt / scaleHeightBlocks);
            return Math.exp(-opacity * getNormalizedDensity() * pressureFactor);
        }

        /**
         * Calculates the dynamic faintest visible stellar magnitude under current atmospheric conditions.
         *
         * <p>This method models how faint a star can be and still remain visible to the naked eye,
         * depending on the atmosphere’s transmission at a given altitude and gravity. It accounts for
         * extreme cases:
         *
         * <ul>
         *   <li><b>Vacuum (transmission → 1.0):</b> No limiting faint magnitude; in principle, infinitely faint
         *       stars could be seen (limited only by rendering budget).</li>
         *   <li><b>Earth-like atmosphere:</b> At typical surface transmission, the faint limit is around 6.5,
         *       matching the naked-eye limit under dark skies.</li>
         *   <li><b>Dense/opaque atmosphere (transmission → 0.0):</b> The faint limit collapses below –30,
         *       ensuring that even the Sun (–26.7) is blocked if the atmosphere is completely opaque.</li>
         * </ul>
         *
         * <p>The function is continuous: as transmission decreases, the faint limit smoothly decreases;
         * as transmission increases, the faint limit smoothly rises toward infinity.
         *
         * @param altitude the current Y position (in blocks). Higher altitude generally increases
         *                 transmission and raises the faint limit.
         * @param gravity  the surface gravity in m/s², which influences the pressure scale height
         *                 and thus how quickly the atmosphere thins with altitude.
         * @return the maximum visible apparent magnitude (faintest star visible).
         *         Lower values mean only extremely bright stars (negative magnitudes) are visible;
         *         higher values mean progressively fainter stars can be seen. In vacuum, this tends
         *         toward infinity.
         */
        public double getDynamicMaxMagnitude(double altitude, double gravity, double surfaceHeight)
        {
            double transmission = getTransmission(altitude, gravity, surfaceHeight);

            // If atmosphere is fully opaque, set faint limit below the Sun’s magnitude
            double minLimit = -30.0D; // ensures even super bright stars like the Sun are blocked
            double maxLimit = Double.MAX_VALUE; // vacuum → no limit

            // Smooth interpolation: as transmission rises, faint limit grows
            if (transmission <= 0.0D) return minLimit;
            if (transmission >= 0.9999D) return maxLimit;

            // Exponential growth of faint limit with transmission:
            // - At low transmission, faint limit stays near minLimit.
            // - At Earth-like transmission, faint limit ~6.5.
            // - As transmission → 1.0, faint limit rises rapidly toward infinity.
            return minLimit + (6.5D - minLimit) * transmission + 20.0D * (transmission / (1.0D - transmission));
        }
    }

    public class Body
    {
        private final double albedo;
        private final double rotationPeriod;
        private final double precession;
        private final double obliquity;
        private BodyTemperature temperature;
        private BodySurface surface;
        private BodySize size;
        private BodyClass bodyClass;

        public Body(double albedo, double rotationPeriod, double precession, double obliquity, BodyTemperature temperature, BodySurface surface, BodySize size, BodyClass bodyClass)
        {
            this.albedo = albedo;
            this.rotationPeriod = rotationPeriod;
            this.precession = precession;
            this.obliquity = obliquity;
            this.temperature = temperature != null ? temperature : BodyTemperature.UNKNOWN;
            this.surface = surface != null ? surface : BodySurface.UNKNOWN;
            this.size = size != null ? size : BodySize.UNKNOWN;
            this.bodyClass = bodyClass != null ? bodyClass : BodyClass.UNKNOWN;
        }

        public double getAlbedo()
        {
            return albedo;
        }

        public double getRotationPeriod()
        {
            return rotationPeriod;
        }

        public double getPrecession()
        {
            return precession;
        }

        public double getObliquity()
        {
            return obliquity;
        }

        public BodyTemperature getBodyTemperature()
        {
            return temperature;
        }

        public BodySurface getSurface()
        {
            return surface;
        }

        public BodySize getBodySize()
        {
            return size;
        }

        public BodyClass getBodyClass()
        {
            return bodyClass;
        }

        public void setBodyTemperature(BodyTemperature newData)
        {
            this.temperature = newData;
        }

        public void setBodySurface(BodySurface newData)
        {
            this.surface = newData;
        }

        public void setBodySize(BodySize newData)
        {
            this.size = newData;
        }

        public void setBodyClass(BodyClass newData)
        {
            this.bodyClass = newData;
        }

        public static List<String> getAllCombinations()
        {
            List<String> combinations = new ArrayList<>();

            for (BodyTemperature temp : BodyTemperature.VALUES)
            {
                if (temp == BodyTemperature.UNKNOWN) continue;

                for (BodySurface surface : BodySurface.VALUES)
                {
                    if (surface == BodySurface.UNKNOWN) continue;

                    for (BodySize size : BodySize.VALUES)
                    {
                        if (size == BodySize.UNKNOWN) continue;

                        for (BodyClass bodyClass : BodyClass.VALUES)
                        {
                            if (bodyClass == BodyClass.UNKNOWN) continue;

                            String combination = temp.getSerializedName() + "_" + surface.getSerializedName() + "_" + size.getSerializedName() + "_" + bodyClass.getSerializedName();
                            combinations.add(combination);
                        }
                    }
                }
            }
            return combinations;
        }

        public String getTemperatureString()
        {
            BodyTemperature temp = this.getBodyTemperature();
            return temp == BodyTemperature.UNKNOWN ? "" : temp.getSerializedName();
        }

        public String getSurfaceString()
        {
            BodySurface surf = this.getSurface();
            return surf == BodySurface.UNKNOWN ? "" : surf.getSerializedName();
        }

        public String getSizePrefix()
        {
            BodySize sz = this.getBodySize();
            return (sz == BodySize.UNKNOWN || sz == BodySize.STANDARD) ? "" : sz.getSerializedName();
        }

        public String getBodyClassString()
        {
            BodyClass bc = this.getBodyClass();
            return bc == BodyClass.UNKNOWN ? "" : bc.getSerializedName();
        }

        public String getBodyDescription()
        {
            StringBuilder sb = new StringBuilder();

            if (this.getBodyTemperature() != BodyTemperature.UNKNOWN)
            {
                sb.append(AHelpers.capitalize(this.getBodyTemperature().getSerializedName())).append(" ");
            }

            if (this.getSurface() != BodySurface.UNKNOWN)
            {
                sb.append(AHelpers.capitalize(this.getSurface().getSerializedName())).append(" ");
            }

            if (this.getBodySize() != BodySize.UNKNOWN && this.getBodySize() != BodySize.STANDARD)
            {
                sb.append(AHelpers.capitalize(this.getBodySize().getSerializedName())).append(" ");
            }

            if (this.getBodyClass() != BodyClass.UNKNOWN)
            {
                sb.append(AHelpers.capitalize(this.getBodyClass().getSerializedName()));
            }

            String description = sb.toString().trim();
            return description.isEmpty() ? "Unknown Body Type" : description;
        }

        public enum BodyTemperature
        {
            TORRID,
            HOT,
            WARM,
            TEMPERATE,
            COOL,
            COLD,
            FRIGID,
            UNKNOWN;

            private final String serializedName;
            public static final BodyTemperature[] VALUES = values();

            BodyTemperature()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static BodyTemperature fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null body temperature type, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (BodyTemperature type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown body temperature type '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown body temperature type '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }

        public enum BodySurface
        {
            AIRLESS,
            ARID,
            LACUSTRINE,
            MARINE,
            OCEANIC,
            SUPEROCEANIC,
            UNKNOWN;

            private final String serializedName;
            public static final BodySurface[] VALUES = values();

            BodySurface()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static BodySurface fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null body surface type, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (BodySurface type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown body surface type '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown body surface type '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }

        public enum BodySize
        {
            MICRO,
            MINI,
            SUB,
            STANDARD,
            SUPER,
            MEGA,
            UNKNOWN;

            private final String serializedName;
            public static final BodySize[] VALUES = values();

            BodySize()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static BodySize fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null body size, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (BodySize type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown body size '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown body size '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }

        public enum BodyClass
        {
            FERRIA,
            CARBONIA,
            TERRA,
            AQUARIA,
            NEPTUNE,
            JUPITER,
            ASTEROID,
            UNKNOWN;

            private final String serializedName;
            public static final BodyClass[] VALUES = values();

            BodyClass()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static BodyClass fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null body class, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (BodyClass type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown body class '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown body class '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }
    }

    public class Ring
    {
        private final double innerRadius;
        private final double outerRadius;
        private final double thickness;

        public Ring(double innerRadius, double outerRadius, double thickness)
        {
            this.innerRadius = innerRadius;
            this.outerRadius = outerRadius;
            this.thickness = thickness;
        }

        public double getInnerRadius()
        {
            return innerRadius;
        }

        public double getOuterRadius()
        {
            return this.outerRadius;
        }

        public double getThickness()
        {
            return thickness;
        }
    }

    public class Life
    {
        private final List<LifeClass> lifeClass;
        private final List<LifeType> type;
        private final List<LifeBiome> biome;

        public Life(List<LifeClass> lifeClass, List<LifeType> type, List<LifeBiome> biome)
        {
            this.lifeClass = lifeClass != null ? new ArrayList<>(lifeClass) : new ArrayList<>(List.of(LifeClass.UNKNOWN));
            this.type = type != null ? new ArrayList<>(type) : new ArrayList<>(List.of(LifeType.UNKNOWN));
            this.biome = biome != null ? new ArrayList<>(biome) : new ArrayList<>(List.of(LifeBiome.UNKNOWN));
        }

        public List<LifeClass> getLifeClass()
        {
            return new ArrayList<>(lifeClass);
        }

        public List<LifeType> getType()
        {
            return new ArrayList<>(type);
        }

        public List<LifeBiome> getBiome()
        {
            return new ArrayList<>(biome);
        }

        public enum LifeClass
        {
            NONE,
            ORGANIC,
            EXOTIC,
            UNKNOWN;

            private final String serializedName;
            public static final LifeClass[] VALUES = values();

            LifeClass()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static LifeClass fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null life class, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (LifeClass type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown life class '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown life class '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }

        public enum LifeType
        {
            NONE,
            UNICELLULAR,
            MULTICELLULAR,
            UNKNOWN;

            private final String serializedName;
            public static final LifeType[] VALUES = values();

            LifeType()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static LifeType fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null life type, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (LifeType type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown life type '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown life type '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }

        public enum LifeBiome
        {
            NONE,
            ANY,
            SUBGLACIAL,
            AERIAL,
            MARINE,
            TERRESTRIAL,
            UNKNOWN;

            private final String serializedName;
            public static final LifeBiome[] VALUES = values();

            LifeBiome()
            {
                this.serializedName = name().toLowerCase(Locale.ROOT);
            }

            public String getSerializedName()
            {
                return serializedName;
            }

            public static LifeBiome fromString(String name)
            {
                if (name == null)
                {
                    Almagest.LOGGER.debug("Null life biome, defaulting to " + UNKNOWN.getSerializedName());
                    return UNKNOWN;
                }
                try
                {
                    for (LifeBiome type : VALUES)
                    {
                        if (type.serializedName.equals(name.toLowerCase(Locale.ROOT)))
                        {
                            return type;
                        }
                    }
                    Almagest.LOGGER.debug("Unknown life biome '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
                catch (Exception e)
                {
                    Almagest.LOGGER.debug("Unknown life biome '" + name + "', defaulting to " + UNKNOWN);
                    return UNKNOWN;
                }
            }
        }
    }

    public class Orbit
    {
        private double period;
        private double semiMajorAxis;
        private double periapsis;
        private final double eccentricity;
        private final double inclination;
        private final double ascendingNode;
        private final double argOfPeriapsis;
        private final double longOfPeriapsis;
        private final double meanLongitude;
        private final double nodalPrecession;
        private final double orbitLineSize;
        private int segments;

        public Orbit(double period, double semiMajorAxis, double periapsis, double eccentricity, double inclination, double ascendingNode, double argOfPeriapsis, double longOfPeriapsis, double meanLongitude, double nodalPrecession, double orbitLineSize, int segments)
        {
            this.period = period;
            this.semiMajorAxis = semiMajorAxis;
            this.periapsis = periapsis;
            this.eccentricity = eccentricity;
            this.inclination = inclination;
            this.ascendingNode = ascendingNode;
            this.argOfPeriapsis = argOfPeriapsis;
            this.longOfPeriapsis = longOfPeriapsis;
            this.meanLongitude = meanLongitude;
            this.nodalPrecession = nodalPrecession;
            this.orbitLineSize = orbitLineSize;
            this.segments = segments;
        }

        public double getPeriod()
        {
            return period;
        }

        public double getSemiMajorAxis()
        {
            return semiMajorAxis;
        }

        public double getPeriapsis()
        {
            return periapsis;
        }

        public double getEccentricity()
        {
            return eccentricity;
        }

        public double getInclination()
        {
            return inclination;
        }

        public double getAscendingNode()
        {
            return ascendingNode;
        }

        public double getArgOfPeriapsis()
        {
            return argOfPeriapsis;
        }

        public double getLongOfPeriapsis()
        {
            return longOfPeriapsis;
        }

        public double getMeanLongitude()
        {
            return meanLongitude;
        }

        public double getNodalPrecession()
        {
            return nodalPrecession;
        }

        public double getOrbitLineSize()
        {
            return orbitLineSize;
        }

        public int getSegments()
        {
            return segments;
        }

        public void setSemiMajorAxis(double semiMajorAxis)
        {
            this.semiMajorAxis = semiMajorAxis;
        }

        public void setPeriapsis(double periapsis)
        {
            this.periapsis = periapsis;
        }

        public void setPeriod(double period)
        {
            this.period = period;
        }

        public void setSegments(int segments)
        {
            this.segments = segments;
        }
    }

    public class Season
    {
        private final double minObliquity;
        private final double maxObliquity;
        private final double amplitudeObliquity;
        private final double meanObliquity;
        private final double milankovitchCycle;
        private final double seasonOffset;
        private final double winterSolsticeOffset;

        public Season(double minObliquity, double maxObliquity, double amplitudeObliquity, double meanObliquity, double milankovitchCycle, double seasonOffset, double winterSolsticeOffset)
        {
            this.minObliquity = minObliquity;
            this.maxObliquity = maxObliquity;
            this.amplitudeObliquity = amplitudeObliquity;
            this.meanObliquity = meanObliquity;
            this.milankovitchCycle = milankovitchCycle;
            this.seasonOffset = seasonOffset;
            this.winterSolsticeOffset = winterSolsticeOffset;
        }

        public double getMinObliquity()
        {
            return minObliquity;
        }

        public double getMaxObliquity()
        {
            return maxObliquity;
        }

        public double getAmplitudeObliquity()
        {
            return amplitudeObliquity;
        }

        public double getMeanObliquity()
        {
            return meanObliquity;
        }

        public double getMilankovitchCycle()
        {
            return milankovitchCycle;
        }

        public double getSeasonOffset()
        {
            return seasonOffset;
        }

        public double getWinterSolsticeOffset()
        {
            return winterSolsticeOffset;
        }
    }

    public class StarData
    {
        public static final int TWINKLE_TABLE_SIZE = 2048;
        public static final float[] TWINKLE_SIN = new float[TWINKLE_TABLE_SIZE];

        static
        {
            for (int i = 0; i < TWINKLE_TABLE_SIZE; i++)
            {
                TWINKLE_SIN[i] = (float)Math.sin((i / (float)TWINKLE_TABLE_SIZE) * FastMath.TWO_PI);
            }
        }

        private final long id;
        private CelestialData type;
        private final List<Double> coords;
        private final double parallax;
        private final double distance;
        private final double rightAscension;
        private final double declination;
        private final double apparentMagnitude;
        private final double absoluteMagnitude;
        private final double effectiveTemperature;
        private final double metallicity;
        private final double luminosity;
        private final double gravity;
        private final double age;
        private final String spectralType;

        public StarData(long id, CelestialData type, List<Double> coords, double parallax, double distance, double rightAscension, double declination, double apparentMagnitude, double absoluteMagnitude, double effectiveTemperature, double metallicity, double luminosity, double gravity, double age, String spectralType)
        {
            this.id = id;
            this.type = type;
            this.coords = coords != null ? new ArrayList<>(coords) : new ArrayList<>(List.of(0.0, 0.0, 0.0));
            this.parallax = parallax;
            this.distance = distance;
            this.rightAscension = rightAscension;
            this.declination = declination;
            this.apparentMagnitude = apparentMagnitude;
            this.absoluteMagnitude = absoluteMagnitude;
            this.effectiveTemperature = effectiveTemperature;
            this.metallicity = metallicity;
            this.luminosity = luminosity;
            this.gravity = gravity;
            this.age = age;
            this.spectralType = spectralType != null ? spectralType : "";
        }

        public long getId()
        {
            return id;
        }

        public CelestialData getType()
        {
            return type;
        }

        public List<Double> getCoords()
        {
            return new ArrayList<>(coords);
        }

        public double getParallax()
        {
            return parallax;
        }

        public double getDistance()
        {
            return distance;
        }

        public double getRightAscension()
        {
            return rightAscension;
        }

        public double getDeclination()
        {
            return declination;
        }

        public double getApparentMagnitude()
        {
            return apparentMagnitude;
        }

        public double getAbsoluteMagnitude()
        {
            return absoluteMagnitude;
        }

        public double getEffectiveTemperature()
        {
            return effectiveTemperature;
        }

        public double getMetallicity()
        {
            return metallicity;
        }

        public double getLuminosity()
        {
            return luminosity;
        }

        public double getGravity()
        {
            return gravity;
        }

        public double getAge()
        {
            return age;
        }

        public String getSpectralType()
        {
            return spectralType;
        }

        public void setCelestialData(CelestialData newType)
        {
            this.type = newType;
        }

        public List<String> getAllNames()
        {
            List<String> allNames = new ArrayList<>();
            allNames.addAll(this.type.getNames());
            allNames.add(String.valueOf(id));
            if (allNames.isEmpty())
            {
                allNames.add("Unknown");
            }
            return allNames;
        }

        public boolean isAParent()
        {
            for (CelestialData data : CelestialDataManager.CELESTIAL_DATA_BY_ID.values())
            {
                if (this.getAllNames().stream().anyMatch(name -> name.equalsIgnoreCase(data.getParentBody())))
                {
                    return true;
                }
            }
            return false;
        }

        public boolean isParentToObserver()
        {
            CelestialObject observer = CelestialObjectHandler.observerObject;
            if (observer == null || observer.body == null)
            {
                return false;
            }

            List<String> parentStarNames = new ArrayList<>();
            observer.body.getParentStars().parallelStream().forEach(parentStar -> {
                parentStarNames.addAll(parentStar.getAllNames());
            });

            return AHelpers.hasMatchesIgnoreCase(this.getAllNames(), parentStarNames);
        }

        public double calculateLuminosity()
        {
            double luminosity = 4.0 * Math.PI * Math.pow(this.getType().getRadius() * Nature.KM_TO_M, 2.0) * Nature.STEFAN_BOLTZMANN * Math.pow(this.effectiveTemperature, 4.0);
            double luminositySolarUnits = luminosity / Nature.SOLAR_LUMINOSITY;
            return luminositySolarUnits;
        }

        public Vec3 getPos()
        {
            double distance = this.getDistance();
            double declination = Math.toRadians(this.getDeclination());
            double rightAscension = Math.toRadians(this.getRightAscension() * 15.0);

            double z = distance * Math.cos(declination) * Math.sin(rightAscension);
            double y = distance * Math.sin(declination);
            double x = distance * Math.cos(declination) * Math.cos(rightAscension);
            return new Vec3(z, y, x);
        }

        public Vec3 getAdjustedPos(Vec3 basePos)
        {
            return PlanetHelpers.rotYX(basePos, CelestialObjectHandler.skyYawSinY, CelestialObjectHandler.skyYawCosY, CelestialObjectHandler.skyPitchSinX, CelestialObjectHandler.skyPitchCosX);
        }

        public boolean isNameUnknown()
        {
            List<String> names = this.getAllNames();
            return names == null || names.isEmpty() || names.stream().anyMatch(name -> name.equalsIgnoreCase("unknown"));
        }

        public Optional<ConstellationData> getConstellation()
        {
            Optional<ConstellationData> constellation = Optional.empty();
            if (this.isNameUnknown())
            {
                return constellation;
            }

            Object2ObjectMap<StarData, ConstellationData> constellations = CelestialDataManager.STAR_DATA_TO_CONSTELLATIONS;
            if (constellations.containsKey(this))
            {
                constellation = Optional.of(constellations.get(this));
            }
            return constellation;
        }

        public Color getColor()
        {
            return ColorUtils.tEffToRGB(this.effectiveTemperature);
        }

        public ChatFormatting getChatColor()
        {
            char spectralType = !this.spectralType.isEmpty() ? this.spectralType.charAt(0) : ' ';
            switch (spectralType)
            {
                case 'O':
                    return ChatFormatting.DARK_BLUE;
                case 'B':
                    return ChatFormatting.BLUE;
                case 'A':
                    return ChatFormatting.AQUA;
                case 'F':
                    return ChatFormatting.WHITE;
                case 'G':
                    return ChatFormatting.YELLOW;
                case 'K':
                    return ChatFormatting.GOLD;
                case 'M':
                    return ChatFormatting.RED;
                case 'L':
                    return ChatFormatting.DARK_RED;
                case 'T':
                    return ChatFormatting.LIGHT_PURPLE;
                case 'Y':
                    return ChatFormatting.DARK_PURPLE;
                case 'C':
                    return ChatFormatting.DARK_GRAY;
                default:
                    return ChatFormatting.RED;
            }
        }

        public static double getAtmosphericTwinkle(Star star)
        {
            if (star.visualSize <= Config.COMMON.minAngularSize.get() * 1.25D || !star.isInView || CelestialObjectHandler.atmosphereFactor <= 0.001D)
            {
                return 1.0D;
            }

            star.twinklePhase += star.twinkleChangeSpeed;
            if (star.twinklePhase >= 1.0f) star.twinklePhase -= 1.0f;

            int idx = ((int)(star.twinklePhase * TWINKLE_TABLE_SIZE) + star.twinkleStartIndex) & (TWINKLE_TABLE_SIZE - 1);
            float tw = TWINKLE_SIN[idx] * 0.075f;

            return 1.0D + tw * CelestialObjectHandler.atmosphereFactor;
        }

        /**
         * Calculates fictional visual size of a star based on apparent magnitude,
         * slope, exposure, scale, and perceptual compression.
         *
         * @param appMag    Apparent magnitude of the star
         * @param scale     Base scaling factor
         * @param gamma     Perceptual compression factor
         * @param slope     Slope multiplier for magnitude
         * @param exposure  Global exposure offset
         * @return          Fictional size value for rendering
         */
        public static double getApparentSize(double appMag)
        {
            return calculateSize(Math.max(-0.25D, appMag), Config.COMMON.scale.get(), Config.COMMON.gamma.get(), Config.COMMON.slope.get(), Config.COMMON.exposure.get());
        }

        /**
         * Calculates fictional visual size of a star based on apparent magnitude,
         * slope, exposure, scale, and perceptual compression.
         *
         * @param appMag    Apparent magnitude of the star
         * @param scale     Base scaling factor
         * @param gamma     Perceptual compression factor
         * @param slope     Slope multiplier for magnitude
         * @param exposure  Global exposure offset
         * @return          Fictional size value for rendering
         */
        public static double calculateSize(double appMag, double scale, double gamma, double slope, double exposure)
        {
            double adjustedMag = slope * appMag - exposure;
            double brightness = Math.pow(10, -adjustedMag / 2.5D);
            return scale * Math.pow(brightness, gamma);
        }

        /**
         * Returns the adjusted colour of the star based on its apparent visual size (in degrees).
         * - Stars >= 0.1° keep their true colour.
         * - Stars <= 0.02° are fully white.
         * - Between 0.02° and 0.1°, the colour is linearly blended toward white.
         *
         * @param size  Size of the star in degrees
         * @param alpha Alpha value of the star
         * @return Adjusted Color
         */
        public Color getAdjustedColorBySize(double size, float alpha, Color baseColor)
        {
            Color trueColor = baseColor.set(alpha);
            Color white = new Color(1.0F, 1.0F, 1.0F, alpha);

            double lowerBound = CelestialObjectHandler.starColorLowerBound;
            double upperBound = CelestialObjectHandler.starColorUpperBound;

            if (lowerBound > upperBound)
            {
                double tmp = lowerBound;
                lowerBound = upperBound;
                upperBound = tmp;
            }

            if (size >= upperBound)
            {
                return trueColor;
            }

            if (size <= lowerBound)
            {
                return white;
            }

            float t = (float)((size - lowerBound) / (upperBound - lowerBound));
            t = Mth.clamp(t, 0.0F, 1.0F);

            return new Color(white).lerp(trueColor, t);
        }

        /**
         * Calculates the angular size of an object in degrees.
         *
         * @param size     the physical size of the object in meters (must be positive)
         * @param distance the distance to the object in meters (must be positive)
         * @return the angular size in degrees
         * @throws IllegalArgumentException if size or distance is non-positive
         */
        public static double getAngularSize(double size, double distance)
        {
            if (size <= 0.0D || distance <= 0.0D)
            {
                return 0.0D;
            }
            // Calculate angular size in radians: θ = 2 * arctan(size / (2 * distance))
            double angularSizeRadians = 2.0D * FastMath.atan(size / (2.0D * distance));
            // Convert to degrees: degrees = radians * (180 / π)
            return Math.toDegrees(angularSizeRadians);
        }

        /**
         * Estimates the visual (apparent) size of an object in degrees, adjusted for the player's
         * field of view (FOV) relative to a normalized FOV of 70 degrees.
         *
         * @param size     the physical size of the object in meters (must be positive)
         * @param distance the distance to the object in meters (must be positive)
         * @param playerFOV the player's field of view in degrees (must be positive)
         * @return the adjusted visual size in degrees
         * @throws IllegalArgumentException if size, distance, or fov is non-positive
         */
        public static double getVisualSize(double size, double distance)
        {
            return getAngularSize(size, distance) * CelestialObjectHandler.fovNormInv;
        }

        public static double getStarAlpha(double apparentMagnitude)
        {
            return getStarAlpha(CelestialObjectHandler.observerBody, CelestialObjectHandler.playerPos.y(), apparentMagnitude, CelestialObjectHandler.starBrightness, CelestialObjectHandler.rainLevel);
        }

        /**
         * Computes the effective alpha (visibility) of a star given environmental and player conditions.
         *
         * <p>This method models how clearly a star should appear to the player by combining multiple
         * influences:
         *
         * <ul>
         *   <li><b>Atmosphere factor:</b> Stars are most suppressed at the surface and fade in as altitude
         *       increases. Atmospheric influence begins to decrease around ~200 blocks above the surface
         *       and vanishes completely at the atmosphere edge.</li>
         *   <li><b>Transmission:</b> Based on atmospheric density, opacity, and gravity, using exponential
         *       decay of pressure with altitude.</li>
         *   <li><b>Magnitude visibility:</b> Logistic fade-in depending on apparent magnitude relative to
         *       the dynamic faint limit. Bright stars appear earlier; faint stars fade in more slowly.</li>
         *   <li><b>Rainfall attenuation:</b> Rain strongly reduces visibility near the surface, but its
         *       effect fades out starting ~200 blocks above the surface.</li>
         *   <li><b>Day/night factor (starBrightness):</b> A value of 0 indicates daytime; values above 0
         *       indicate night, increasing toward full darkness. Daylight reduces visibility strongly near
         *       the surface, but its influence fades with altitude and vanishes in space.</li>
         * </ul>
         *
         * <p>At high altitude or in space, rainfall and daylight suppression have no effect, leaving only
         * magnitude and transmission to determine visibility.
         *
         * @param data              celestial body data (atmosphere, gravity, surface height).
         * @param altitude          current Y position of the player.
         * @param apparentMagnitude star’s apparent magnitude (brightness).
         * @param starBrightness    daylight brightness factor (0 = day, >0 = night).
         * @param rainLevel         normalized rainfall intensity [0,1].
         * @return alpha value in [0.0, 1.0], representing star visibility.
         */
        public static double getStarAlpha(CelestialData data, double altitude, double apparentMagnitude, double starBrightness, double rainLevel)
        {
            Atmosphere atmosphere = data.getAtmosphere();

            if (atmosphere == null || atmosphere.getHeight() <= 0.0D)
            {
                return 1.0D;
            }

            double gravity = data.getGravity();
            double surfaceHeight = data.getSurfaceHeight();
            double atmosphereHeight = atmosphere.getHeight();

            double threshold = 0.05D;
            double brightness = Mth.clamp((starBrightness - threshold) / (1.0D - threshold), 0.0D, 1.0D);

            double atmosphereFadeStart = atmosphereHeight * 0.1D;
            double relAlt = altitude - surfaceHeight;
            double atmosphereFactor = relAlt < atmosphereFadeStart ? 1.0D : Mth.clamp(1.0D - ((relAlt - atmosphereFadeStart) / (atmosphereHeight - atmosphereFadeStart)), 0.0D, 1.0D);

            double dynamicMaxMag = atmosphere.getDynamicMaxMagnitude(altitude, gravity, surfaceHeight);
            double magFactor = magnitudeVisibility(apparentMagnitude, dynamicMaxMag, brightness);

            double transmission = atmosphere.getTransmission(altitude, gravity, surfaceHeight);

            double visibility = Mth.lerp(transmission, 1.0D, magFactor);

            double rainFadeFactor = Mth.clamp((altitude - surfaceHeight) / atmosphereFadeStart, 0.0D, 1.0D);
            double rainFactor = Mth.clamp(1.0D - (rainLevel * (1.0D - rainFadeFactor)), 0.0D, 1.0D);

            double nightVisibility = smoothstep(0.0D, 0.2D, brightness);
            double daylightInfluence = 1.0D - nightVisibility;
            double dayFactor = 1.0D - (daylightInfluence * atmosphereFactor);

            double alpha = visibility * rainFactor * dayFactor;
            double clamped = Mth.clamp(alpha, 0.0D, 1.0D);

            return clamped < 0.01D ? 0.0D : clamped;
        }

        /**
         * Dynamic visibility weighting:
         * - Bright stars (low mag) reach full visibility earlier in the night.
         * - Faint stars (high mag) fade in more slowly, but all reach full visibility at midnight.
         * - Atmosphere (dynamicMaxMag) controls faint cutoff.
         */
        public static double magnitudeVisibility(double apparentMagnitude, double dynamicMaxMag, double starBrightness)
        {
            // Normalize magnitude into [0,1] relative to faint limit
            // Effective fade-in threshold: faint stars require higher brightness to appear
            // Bright stars (magNorm ~0) → threshold near 0
            // Faint stars (magNorm ~1) → threshold near 1
            double magNorm = Mth.clamp(apparentMagnitude / dynamicMaxMag, 0.0D, 1.0D);

            // Compare starBrightness against threshold with a smooth logistic
            double k = 8.0D; // steepness of fade-in curve
            double logistic = Mth.clamp((1.0D / (1.0D + Math.exp(-k * (starBrightness - magNorm)))) * 1.1D - 0.05D, 0.0D, 1.0D);

            return logistic;
        }

        /**
         * Smooth Hermite interpolation between 0.0D and 1.0D.
         */
        public static double smoothstep(double edge0, double edge1, double x)
        {
            double t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0D, 1.0D);
            return t * t * (3.0D - 2.0D * t);
        }

        public static double easeInOutCubic(double t)
        {
            return t < 0.5D
                ? 4.0D * t * t * t
                : 1.0D - Math.pow(-2.0D * t + 2.0D, 3.0D) / 2.0D;
        }
    }

    public class ConstellationData
    {
        private final long id;
        private final CelestialObjectTypes type;
        private final String name;
        private final boolean zodiac;
        private final List<List<String>> pairs;
        private final Color color;

        public ConstellationData(long id, CelestialObjectTypes type, String name, boolean zodiac, List<List<String>> pairs, Color color)
        {
            this.id = id;
            this.type = type != null ? type : CelestialObjectTypes.STAR;
            this.name = name != null ? name : "Unknown";
            this.zodiac = zodiac;
            this.pairs = pairs != null ? pairs : new ArrayList<>();
            this.color = color != null ? color : new Color(
                Config.COMMON.constellationsRed.get(),
                Config.COMMON.constellationsGreen.get(),
                Config.COMMON.constellationsBlue.get(),
                1.0D
            );
        }

        public long getId()
        {
            return id;
        }

        public CelestialObjectTypes getType()
        {
            return type;
        }

        public String getName()
        {
            return name;
        }

        public boolean isZodiac()
        {
            return zodiac;
        }

        public List<List<String>> getPairs()
        {
            return pairs;
        }

        public Color getColor()
        {
            return this.zodiac ? this.color.mul(2.0F, 2.0F, 3.0F, 1.0F) : this.color;
        }
    }
}
