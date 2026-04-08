package almagest.util;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2d;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import almagest.client.CelestialObjectHandler;
import almagest.client.data.CelestialObjectTypes;
import almagest.client.data.CelestialObjectTypes.Season;
import almagest.client.particle.CelestialObject;
import almagest.config.Config;

public class PlanetHelpers
{
    public static final Object2ObjectMap<CelestialObject, OrbitCache> ORBIT_CACHE = new Object2ObjectOpenHashMap<>();
    public static final int REFERENCE_SEGMENTS = 32;
    public static final int MAX_SEGMENTS = 256;
    public static final double AU_TO_MC = Config.COMMON.auScale.get();

    public static List<List<Vec3>> updateOrbitPositions(CelestialObject obj, Vec3 playerOffsetMC, double semiMajorAxis, double periapsis, double period, double elapsedTime, CelestialObject observer)
    {
        if (obj == CelestialObjectHandler.systemCenterObject)
            return updateOrbitPositionsForSystemCenter(observer, playerOffsetMC);

        int segments = getOrbitSegments(semiMajorAxis);
        List<List<Vec3>> result = new ArrayList<>(segments);

        double a = obj.semiMajorAxis;
        double e = obj.eccentricity;
        double i = obj.inclination;
        double O = obj.ascendingNode;
        double w = obj.argOfPeriapsis;

        double vNow = getTrueAnomaly(obj, period, elapsedTime, e, a, periapsis);

        for (int s = 0; s < segments; s++)
        {
            double v1 = vNow + (double)s / segments * (2.0 * Math.PI);
            double v2 = vNow + (double)(s + 1) / segments * (2.0 * Math.PI);

            Vec3 p1AU = getOrbitPointAU(a, e, i, O, w, v1, obj);
            Vec3 p2AU = getOrbitPointAU(a, e, i, O, w, v2, obj);

            Vec3 g1AU = toGeocentric(p1AU, observer.posAU);
            Vec3 g2AU = toGeocentric(p2AU, observer.posAU);

            Vec3 f1AU = applyObserverFrame(g1AU, observer);
            Vec3 f2AU = applyObserverFrame(g2AU, observer);

            Vec3 f1MC = scaleAUToMC(f1AU).add(playerOffsetMC);
            Vec3 f2MC = scaleAUToMC(f2AU).add(playerOffsetMC);

            List<Vec3> pair = new ArrayList<>(2);
            pair.add(f1MC);
            pair.add(f2MC);
            result.add(pair);
        }

        return result;
    }

    public static List<List<Vec3>> updateOrbitPositionsForSystemCenter(CelestialObject observer, Vec3 playerOffsetMC)
    {
        int segments = getOrbitSegments(observer.posAU.length());
        List<List<Vec3>> result = new ArrayList<>(segments);

        double a = observer.semiMajorAxis;
        double e = observer.eccentricity;
        double i = observer.inclination;
        double O = observer.ascendingNode;
        double w = observer.argOfPeriapsis;

        double vNow = getTrueAnomaly(observer, observer.period, observer.elapsedTime, e, a, observer.periapsis);

        for (int s = 0; s < segments; s++)
        {
            double v1 = vNow + (double)s / segments * (2.0 * Math.PI);
            double v2 = vNow + (double)(s + 1) / segments * (2.0 * Math.PI);

            Vec3 obs1 = getOrbitPointAU(a, e, i, O, w, v1, observer);
            Vec3 obs2 = getOrbitPointAU(a, e, i, O, w, v2, observer);

            Vec3 sun1AU = obs1.scale(-1);
            Vec3 sun2AU = obs2.scale(-1);

            Vec3 g1AU = sun1AU.subtract(observer.posAU);
            Vec3 g2AU = sun2AU.subtract(observer.posAU);

            Vec3 f1AU = applyObserverFrame(g1AU, observer);
            Vec3 f2AU = applyObserverFrame(g2AU, observer);

            Vec3 f1MC = scaleAUToMC(f1AU).add(playerOffsetMC);
            Vec3 f2MC = scaleAUToMC(f2AU).add(playerOffsetMC);

            List<Vec3> pair = new ArrayList<>(2);
            pair.add(f1MC);
            pair.add(f2MC);
            result.add(pair);
        }

        return result;
    }

    public static Vec3 getOrbitPointAU(double a, double e, double i, double O, double w, double v, CelestialObject obj)
    {
        double r = a * (1.0 - e * e) / (1.0 + e * Math.cos(v));

        double xOrb = r * Math.cos(v);
        double yOrb = r * Math.sin(v);
        Vec3 pos = new Vec3(xOrb, yOrb, 0);

        pos = rotZ(O, pos);
        pos = rotX(i, pos);
        pos = rotZ(w, pos);

        return pos.add(getParentOffset(obj));
    }

    /**
     * Computes the periapsis distance (closest approach) for elliptical, parabolic,
     * or hyperbolic orbits.
     *
     * Units:
     *   - semiMajorAxis     - kilometers (km)
     *   - eccentricity      - dimensionless
     *   - angularMomentum   - km²/s   (only used for parabolic orbits)
     *   - mu                - km³/s²  (standard gravitational parameter GM)
     *
     * Returns:
     *   - periapsis distance in kilometers (km)
     *
     * Notes:
     *   - Elliptical: 0 < e < 1      -> q = a(1 − e)
     *   - Parabolic:  e ≈ 1          -> q = h² / μ
     *   - Hyperbolic: e > 1          -> q = |a|(e − 1)
     */
    public static double calculatePeriapsis(double semiMajorAxis, double eccentricity, double angularMomentum, double mu)
    {
        double e = Math.abs(eccentricity);

        // Elliptical orbit (0 < e < 1)
        if (e < 1.0D - 1e-9)
        {
            return semiMajorAxis * (1.0D - eccentricity);
        }

        // Parabolic orbit (e ≈ 1)
        if (Math.abs(e - 1.0D) < 1e-9)
        {
            return (angularMomentum * angularMomentum) / mu;
        }

        // Hyperbolic orbit (e > 1)
        if (e > 1.0D + 1e-9)
        {
            return Math.abs(semiMajorAxis) * (eccentricity - 1.0D);
        }

        return 0.0D;
    }

    /**
     * Computes surface gravity in m/s^2 using Newtonian gravity: g = G * M / R^2.
     *
     * @param massKg   Body mass in kilograms
     * @param radiusKm Body radius in kilometers
     * @return Surface gravity in m/s^2
     */
    public static double calculateSurfaceGravity(double massKg, double radiusKm)
    {
        double Rm = radiusKm * Nature.KM_TO_M;
        return (Nature.GRAVITATIONAL_CONSTANT * massKg) / (Rm * Rm);
    }

    /**
     * Computes the number of orbit segments based on semi-major axis (AU).
     * Ensures smooth orbits by scaling segment count with orbital size.
     */
    public static int getOrbitSegments(double semiMajorAxis)
    {
        if (semiMajorAxis <= 0.0D)
            return REFERENCE_SEGMENTS;

        double segments = REFERENCE_SEGMENTS * semiMajorAxis;

        int seg = (int)Math.ceil(segments);
        seg = Mth.clamp(seg, REFERENCE_SEGMENTS, MAX_SEGMENTS);

        return seg;
    }

    public static int getOrbitSegments(double semiMajorAxis, double eccentricity)
    {
        eccentricity = Mth.clamp(eccentricity, 0.0D, 0.999999D);
        semiMajorAxis = Math.max(semiMajorAxis, 1e-9);

        double semiMinorAxis = semiMajorAxis * Math.sqrt(1.0 - eccentricity * eccentricity);

        if (!Double.isFinite(semiMinorAxis))
        {
            return REFERENCE_SEGMENTS;
        }

        double h = Math.pow(semiMajorAxis - semiMinorAxis, 2.0) / Math.pow(semiMajorAxis + semiMinorAxis, 2.0);

        if (!Double.isFinite(h) || h >= 1.333333)
        {
            return REFERENCE_SEGMENTS;
        }

        double perimeter = Math.PI * (semiMajorAxis + semiMinorAxis) * (1.0 + (3.0 * h) / (10.0 + Math.sqrt(4.0 - 3.0 * h)));

        if (!Double.isFinite(perimeter))
        {
            return REFERENCE_SEGMENTS;
        }

        int seg = (int)Math.ceil(perimeter / REFERENCE_SEGMENTS) + REFERENCE_SEGMENTS;

        seg = Mth.clamp(seg, REFERENCE_SEGMENTS, MAX_SEGMENTS);

        return seg;
    }

    /**
     * @return The density in g/cm^3
     */
    public static double getDensityFromType(CelestialObjectTypes type)
    {
        switch (type)
        {
            case COMET:
                return 0.67D;
            case ASTEROID:
                return 3.34D;
            case DWARFMOON:
                return 1.71D;
            case DWARFPLANET:
                return 2.52D;
            case MOON:
                return 3.01D;
            case PLANET:
                return 5.03D;
            case STAR:
                return 1.41D;
            default:
                return 3.93D;
        }
    }

    /**
     * Estimates the mass of an object in kg.
     *
     * @param radiusKm Radius in kilometers
     * @param densityGcm3 Density in grams per cubic centimeter g/cm^3
     * @return Mass in kg
     */
    public static double getMass(double radiusKm, double densityGcm3)
    {
        // Convert radius to centimeters
        double radiusCm = radiusKm * 1E5D;

        // Volume of a sphere: V = (4/3) * π * r³
        double volumeCm3 = (4.0D / 3.0D) * Math.PI * Math.pow(radiusCm, 3.0D);

        // Mass in grams: mass = density * volume
        double massGrams = densityGcm3 * volumeCm3;

        // Convert grams to kilograms
        return massGrams * 1e-3D;
    }

    /**
     * Estimates the mass of an object in Earth masses.
     *
     * @param radiusKm Radius in kilometers
     * @param densityGcm3 Density in grams per cubic centimeter
     * @return Mass in Earth masses
     */
    public static double getMassInEarthMasses(double radiusKm, double densityGcm3)
    {
        // Convert to Earth masses
        return getMass(radiusKm, densityGcm3) / Nature.EARTH_MASS_KG;
    }

    /**
     * Gets the ResourceLocation for the given obliquity and hour from the DAY_CYCLE_TEXTURE_CACHE.
     *
     * @param obliquity The obliquity in degrees, which will be rounded to the nearest 2.5-degree interval from -180 to 180.
     * @param hour      The hour in the range of 0 to 24000, which will be rounded to the nearest valid hour interval.
     * @return The 'vector' containing the corresponding time and obliquity, where x is obliquity and y is the hour.
     */
    public static Vector2d getDayCycle(double obliquity, double hour)
    {
        double roundedObliquity = Math.round(obliquity / 2.5D) * 2.5D;
        double normalizedHour = AHelpers.modulo(hour, (double) Level.TICKS_PER_DAY);
        double roundedHour = AHelpers.modulo(Math.round(normalizedHour / ((double) Level.TICKS_PER_DAY / 48.0D)), 48.0D);
        return new Vector2d(roundedObliquity, roundedHour);
    }

    /**
     * Calculates the orbital period (years).
     * @param a The semi-major axis (AU).
     * @return The orbital period in years.
     */
    public static double getOrbitalPeriod(double a)
    {
        return Math.sqrt(Math.pow(a, 3.0D));
    }

    /**
     * Computes the orbital period of an object using Kepler's Third Law.
     *
     * Units:
     *   - massParent  - kilograms (kg)
     *   - massObject  - kilograms (kg)
     *   - semiMajorAxis - meters (m)
     *
     * Returns:
     *   - orbital period in days
     *
     * Formula:
     *   T = 2π * sqrt(a³ / μ)
     *
     * Where:
     *   μ = G * (massParent + massObject)
     *   G = 6.67430e−11 m³/(kg·s²)
     */
    public static double getOrbitalPeriod(double massParent, double massObject, double semiMajorAxis)
    {
        double mu = Nature.GRAVITATIONAL_CONSTANT * (massParent + massObject);
        double periodSeconds = 2.0D * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3.0D) / mu);
        return periodSeconds / 86400.0D;
    }

    /**
     * Calculates the mean longitude of the perihelion and ascending node.
     * @return The mean longitude.
     */
    public static double getMeanLongitude(double ascendingNode, double periapsis)
    {
        return (2.0D * Math.PI * (ascendingNode + periapsis)) / 360.0D;
    }

    public static class OrbitCache
    {
        public double meanAnomalyFactor;     // k
        public double eccentricityFactor;    // sqrt((1+e)/(1-e))
        public double hyperbolicFactor;      // sqrt((e+1)/(e-1))

        public boolean isElliptical;
        public boolean isParabolic;
        public boolean isHyperbolic;
    }

    public static OrbitCache buildOrbitCache(CelestialObject o)
    {
        OrbitCache c = new OrbitCache();

        double e = o.eccentricity;
        double a = o.semiMajorAxis;
        double q = o.periapsis;
        double P = o.period;
        double mu = Nature.GRAVITATIONAL_CONSTANT;

        if (Math.abs(e - 1.0D) < 1e-9)
        {
            c.isParabolic = true;
            c.meanAnomalyFactor = Math.sqrt(mu / (2.0D * Math.pow(q, 3.0D)));
        }
        else if (e < 1.0D)
        {
            c.isElliptical = true;
            c.meanAnomalyFactor = (2.0D * Math.PI) / P;
            c.eccentricityFactor = Math.sqrt((1.0D + e) / (1.0D - e));
        }
        else
        {
            c.isHyperbolic = true;
            c.meanAnomalyFactor = Math.sqrt(mu / Math.pow(Math.abs(a), 3.0D));
            c.hyperbolicFactor = Math.sqrt((e + 1.0D) / (e - 1.0D));
        }

        return c;
    }

    /**
     * Calculates the mean anomaly for elliptical, parabolic, or hyperbolic orbits.
     *
     * Uses cached per-object constants to avoid repeated sqrt/pow/divisions.
     *
     * @param o The celestial object (used for cache lookup)
     * @param P The orbital period (unused after caching, but kept for compatibility)
     * @param T The elapsed time
     * @param e The eccentricity
     * @param a The semi-major axis
     * @param q The periapsis distance
     * @return The mean anomaly (M, Mp, or Mh)
     */
    public static double getMeanAnomaly(CelestialObject o, double P, double T, double e, double a, double q)
    {
        if (e < 0.0D) return 0.0D;

        OrbitCache c = ORBIT_CACHE.get(o);
        if (c == null)
        {
            c = buildOrbitCache(o);
            ORBIT_CACHE.put(o, c);
        }

        return c.meanAnomalyFactor * T;
    }

    /**
     * Calculates the eccentric anomaly for elliptical orbits.
     * @param P The orbital period.
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param a The semi-major axis (positive for elliptical orbits).
     * @param q The periapsis distance q (for parabolic orbits, ignored here).
     * @return The eccentric anomaly.
     */
    public static double getEccentricAnomaly(CelestialObject o, double P, double T, double e, double a, double q)
    {
        return getEccentricAnomaly(o, P, T, e, a, q, Config.COMMON.eccentricAnomalyIterations.get());
    }

    /**
     * Calculates the eccentric anomaly for elliptical orbits with a given iteration count.
     */
    public static double getEccentricAnomaly(CelestialObject o, double P, double T, double e, double a, double q, int iterations)
    {
        if (e >= 1.0D || e < 0.0D || P <= 0.0D || a <= 0.0D) return 0.0D;

        double M = getMeanAnomaly(o, P, T, e, a, q);
        double E = M;

        for (int j = 0; j < iterations; j++)
        {
            E = M + e * Math.sin(E);
        }

        return E;
    }

    /**
     * Calculates the hyperbolic anomaly for hyperbolic orbits.
     * @param P The orbital period (used for consistency with mean anomaly).
     * @param a The semi-major axis (negative for hyperbolic orbits).
     * @param T The elapsed time.
     * @param e The eccentricity (> 1).
     * @param q The periapsis distance q (for parabolic orbits, ignored here).
     * @param iterations The number of iterations for solving the hyperbolic Kepler equation.
     * @return The hyperbolic anomaly.
     */
    public static double getHyperbolicAnomaly(CelestialObject o, double P, double a, double T, double e, double q, int iterations)
    {
        if (e <= 1.0D || a >= 0.0D) return 0.0D;

        double Mh = getMeanAnomaly(o, P, T, e, a, q);
        double H = Mh;

        for (int j = 0; j < iterations; j++)
        {
            H = (Mh + e * FastMath.sinh(H)) / (e * FastMath.cosh(H) - 1.0D);
        }

        return H;
    }

    /**
     * Calculates the true anomaly for elliptical, parabolic, or hyperbolic orbits.
     * @param P The orbital period (elliptical).
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param a The semi-major axis (positive for elliptical, negative for hyperbolic, ignored for parabolic).
     * @param q The periapsis distance q (for parabolic orbits).
     * @return The true anomaly in radians.
     */
    public static double getTrueAnomaly(CelestialObject o, double P, double T, double e, double a, double q)
    {
        OrbitCache c = ORBIT_CACHE.get(o);
        if (c == null)
        {
            c = buildOrbitCache(o);
            ORBIT_CACHE.put(o, c);
        }

        double M = c.meanAnomalyFactor * T;

        if (c.isParabolic)
        {
            double value = Math.cbrt(1.5D * M);
            return 2.0D * FastMath.atanFast(value);
        }

        if (c.isElliptical)
        {
            double E = getEccentricAnomaly(o, P, T, e, a, q);
            double value = c.eccentricityFactor * Math.tan(E / 2.0D);
            return 2.0D * FastMath.atanFast(value);
        }

        if (c.isHyperbolic)
        {
            double H = getHyperbolicAnomaly(o, P, a, T, e, q, Config.COMMON.eccentricAnomalyIterations.get());
            double value = c.hyperbolicFactor * FastMath.tanh(H / 2.0D);
            return 2.0D * FastMath.atanFast(value);
        }

        return 0.0D;
    }

    /**
     * Calculates the heliocentric distance for elliptical, parabolic, or hyperbolic orbits.
     * Distances are in AU if a and q are in AU.
     * @param P The orbital period (elliptical) or periapsis distance q (parabolic).
     * @param T The elapsed time.
     * @param e The eccentricity.
     * @param a The semi-major axis (positive for elliptical, negative for hyperbolic, ignored for parabolic).
     * @param q The periapsis distance q (for parabolic orbits).
     * @return The heliocentric distance (same units as a/q, typically AU).
     */
    public static double getHeliocentricDistance(CelestialObject o, double P, double T, double e, double a, double q)
    {
        if (e < 0.0D) return 0.0D;

        double v = getTrueAnomaly(o, P, T, e, a, q);

        // Parabolic (e = 1)
        if (Math.abs(e - 1.0D) < 1E-6D)
        {
            return (2.0D * q) / (1.0D + Math.cos(v));
        }
        // Elliptical (e < 1)
        else if (e < 1.0D)
        {
            if (P <= 0.0D) return 0.0D;
            return a * (1.0D - e * e) / (1.0D + e * Math.cos(v));
        }
        // Hyperbolic (e > 1)
        else
        {
            if (a >= 0.0D) return 0.0D;
            return a * (e * e - 1.0D) / (1.0D + e * Math.cos(v));
        }
    }

    public static Vec3 rotX(double angle, Vec3 v)
    {
        if (angle == 0.0D) return v;
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Vec3(
            v.x,
            v.y * c - v.z * s,
            v.y * s + v.z * c
        );
    }

    public static Vec3 rotY(double angle, Vec3 v)
    {
        if (angle == 0.0D) return v;
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Vec3(
            v.x * c + v.z * s,
            v.y,
            -v.x * s + v.z * c
        );
    }

    public static Vec3 rotZ(double angle, Vec3 v)
    {
        if (angle == 0.0D) return v;
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Vec3(
            v.x * c - v.y * s,
            v.x * s + v.y * c,
            v.z
        );
    }

    public static Vec3 rotXY(Vec3 v, double sinY, double cosY, double sinX, double cosX)
    {
        // --- Rotate around X first ---
        double y1 = v.y * cosX - v.z * sinX;
        double z1 = v.y * sinX + v.z * cosX;
        double x1 = v.x;

        // --- Then rotate around Y ---
        double x2 = x1 * cosY + z1 * sinY;
        double z2 = -x1 * sinY + z1 * cosY;

        return new Vec3(x2, y1, z2);
    }

    public static Vec3 rotYX(Vec3 v, double sinY, double cosY, double sinX, double cosX)
    {
        // --- Rotate around Y first ---
        double x1 = v.x * cosY + v.z * sinY;
        double z1 = -v.x * sinY + v.z * cosY;
        double y1 = v.y;

        // --- Then rotate around X ---
        double y2 = y1 * cosX - z1 * sinX;
        double z2 = y1 * sinX + z1 * cosX;

        return new Vec3(x1, y2, z2);
    }

    /**
     * Calculates the elapsed time within an orbital period.
     * @param t The time.
     * @param P The orbital period.
     * @return The elapsed time within the orbital period.
     */
    public static double getElapsedTime(double t, double P)
    {
        double elapsedTime = AHelpers.modulo(t, P);
        return Double.isNaN(elapsedTime) ? 0.0D : elapsedTime;
    }

    /**
     * Calculates the time of day as an angle in radians.
     * @param t The time.
     * @param r The duration of a day (same units as t).
     * @return The time of day in radians.
     */
    public static double getTimeOfDay(double t, double r)
    {
        return getTimeOfDay(t, r, false);
    }

    /**
     * Calculates the time of day as an angle in radians.
     * Supports manual override via config percentage (0.0–1.0).
     *
     * @param t The time.
     * @param r The duration of a day (same units as t).
     * @param overrideEnabled Whether manual override should be used.
     * @return The time of day in radians.
     */
    public static double getTimeOfDay(double t, double r, boolean overrideEnabled)
    {
        if (overrideEnabled)
        {
            double pct = Config.COMMON.manualTimeOfDayPercentage.get();
            pct = Mth.clamp(pct, 0.0D, 1.0D);

            double degrees = pct * 360.0D + 90.0D;
            double radians = Math.toRadians(degrees);

            return Double.isNaN(radians) ? 0.0D : radians;
        }

        if (r <= 0.0D) return 0.0D;

        double phase = AHelpers.modulo(t, r) / r;
        double degrees = phase * 360.0D + 90.0D;
        double radians = Math.toRadians(degrees);

        return Double.isNaN(radians) ? 0.0D : radians;
    }

    public static Vec3 toGeocentric(Vec3 heliocentricAU, Vec3 observerAU)
    {
        return heliocentricAU.subtract(observerAU);
    }

    public static Vec3 getParentOffset(CelestialObject obj)
    {
        Vec3 sum = Vec3.ZERO;

        for (CelestialObject parent : obj.parentObjects)
        {
            sum = sum.add(parent.posAU);
        }

        return sum;
    }

    public static Vec3 scaleAUToMC(Vec3 v)
    {
        return v.scale(AU_TO_MC);
    }

    /**
     * Calculates the elapsed nodal precession angle in radians.
     * @param time The time.
     * @param period The orbital period.
     * @param nodalPrecession The nodal precession period in "orbits per cycle" (or equivalent).
     * @return The nodal precession angle in radians.
     */
    public static double getElapsedNodalPrecession(double time, double period, double nodalPrecession)
    {
        if (nodalPrecession == 0.0D || period == 0.0D) return 0.0D;

        double cycles = (time / period) / nodalPrecession;
        return cycles * (2.0D * Math.PI);
    }

    /**
     * Calculates the seasonal obliquity/rotation.
     * @param object The celestial body.
     * @param time The time.
     * @return The seasonal rotation.
     */
    public static double getSeason(CelestialObject object, long time)
    {
        return getSeason(object, time, false);
    }

    /**
     * Calculates the seasonal obliquity/rotation.
     * Supports manual override via config percentage (0.0–1.0).
     *
     * @param object The celestial body.
     * @param time   The time.
     * @return The seasonal rotation.
     */
    public static double getSeason(CelestialObject object, long time, boolean overrideEnabled)
    {
        Season season = object.body.getSeason();
        double period = object.period;

        if (overrideEnabled)
        {
            double overridePct = Config.COMMON.manualSeasonPercentage.get();
            double phase = overridePct * (2.0D * Math.PI);
            double obVar = getObliquityVariation(object, season, time, period);
            double seasonOffset = Math.toRadians(
                obVar *
                Math.cos(phase + season.getSeasonOffset() + Math.PI) *
                Config.COMMON.planetSeasonalIntensity.get()
            );
            return !Double.isNaN(seasonOffset) ? seasonOffset : 0.0D;
        }

        double offset = season.getWinterSolsticeOffset() > 0.0D
            ? AHelpers.modulo(season.getWinterSolsticeOffset() / period, period)
            : 0.0D;

        double seasonOffset = Math.toRadians(
            getObliquityVariation(object, season, time, period) *
            Math.cos(
                (object.elapsedTime / period) * 2.0D * Math.PI +
                offset +
                season.getSeasonOffset() +
                Math.PI
            ) *
            Config.COMMON.planetSeasonalIntensity.get()
        );

        return !Double.isNaN(seasonOffset) ? seasonOffset : 0.0D;
    }

    /**
     * Calculates the obliquity variation, including the Milankovitch cycle.
     * @param time The time.
     * @param period The orbital period.
     * @return The obliquity variation.
     */
    public static double getObliquityVariation(CelestialObject object, Season season, long time, double period)
    {
        return getObliquityVariation(object, season, time, period, season.getMilankovitchCycle(), season.getMinObliquity(), season.getMaxObliquity());
    }

    /**
     * Calculates the obliquity variation, including the Milankovitch cycle.
     * @param time The time.
     * @param period The orbital period.
     * @param milankovitchCycle The period of the Milankovitch cycle.
     * @param minObliquity The minimum obliquity.
     * @param maxObliquity The maximum obliquity.
     * @return The obliquity variation.
     */
    public static double getObliquityVariation(CelestialObject object, Season season, long time, double period, double milankovitchCycle, double minObliquity, double maxObliquity)
    {
        double meanObliquity = season.getMeanObliquity() == 0.0D ? object.obliquity : season.getMeanObliquity();
        return season.getAmplitudeObliquity() * Math.sin((2.0D * Math.PI * time) / (period * milankovitchCycle)) + meanObliquity;
    }

    /**
     * Calculates the obliquity amplitude.
     * @param minObliquity The minimum obliquity.
     * @param maxObliquity The maximum obliquity.
     * @return The obliquity amplitude.
     */
    public static double getObliquityAmplitude(double minObliquity, double maxObliquity)
    {
        return (maxObliquity - minObliquity) * 0.5D;
    }

    /**
     * Calculates the obliquity mean.
     * @param minObliquity The minimum obliquity.
     * @param maxObliquity The maximum obliquity.
     * @return The obliquity mean.
     */
    public static double getMeanObliquity(double minObliquity, double maxObliquity)
    {
        return (minObliquity + maxObliquity) / 2.0D;
    }

    public static Vec3 sphericalRot(double angle, Vec3 vector)
    {
        if (angle == 0.0D) return vector;

        double r = vector.length();
        if (r == 0.0D) return vector;

        double phi = FastMath.atan2(vector.z(), vector.x());
        double value = Mth.clamp(vector.y() / r, -1.0D, 1.0D);
        double theta = FastMath.acos(value);

        theta = Mth.clamp(theta + angle, 0.0D, Math.PI);
        double rsintheta = r * Math.sin(theta);

        double x = rsintheta * Math.cos(phi);
        double y = r * Math.cos(theta);
        double z = rsintheta * Math.sin(phi);

        return new Vec3(x, y, z);
    }

    public static Vec3 applyObserverFrame(Vec3 v, CelestialObject observer)
    {
        double sinY = CelestialObjectHandler.skyYawSinY;
        double cosY = CelestialObjectHandler.skyYawCosY;
        double sinX = CelestialObjectHandler.skyPitchSinX;
        double cosX = CelestialObjectHandler.skyPitchCosX;

        return rotYX(v, sinY, cosY, sinX, cosX);
    }

    public static Vec3 applyRotations(CelestialObject object, Vec3 v, boolean useSeasonOffset)
    {
        v = rotY(object.nodalPrecession, v);

        if (useSeasonOffset)
        {
            v = sphericalRot(CelestialObjectHandler.observerObject.season, v);
        }

        double sinY = CelestialObjectHandler.skyYawSinY;
        double cosY = CelestialObjectHandler.skyYawCosY;
        double sinX = CelestialObjectHandler.skyPitchSinX;
        double cosX = CelestialObjectHandler.skyPitchCosX;

        return rotYX(v, sinY, cosY, sinX, cosX);
    }

    public static Vec3 getPosHeliocentric(CelestialObject obj, double elapsedTime)
    {
        double a = obj.semiMajorAxis;
        double e = obj.eccentricity;
        double i = obj.inclination;
        double O = obj.ascendingNode;
        double w = obj.argOfPeriapsis;
        double P = obj.period;
        double q = obj.periapsis;

        double r = getHeliocentricDistance(obj, P, elapsedTime, e, a, q);
        double v = getTrueAnomaly(obj, P, elapsedTime, e, a, q);

        double xOrb = r * Math.cos(v);
        double yOrb = r * Math.sin(v);
        Vec3 pos = new Vec3(xOrb, yOrb, 0);

        pos = rotZ(O, pos);
        pos = rotX(i, pos);
        pos = rotZ(w, pos);

        return pos;
    }

    public static Vec3 getPosWithParents(CelestialObject obj, double elapsedTime)
    {
        Vec3 pos = getPosHeliocentric(obj, elapsedTime);
        for (CelestialObject parent : obj.parentObjects)
        {
            pos = pos.add(parent.posAU);
        }
        return pos;
    }

    /**
     * Calculates the position.
     * @param object The celestial object.
     * @param semiMajorAxis The semi-major axis of the object.
     * @param T The global time.
     * @param useSeasonOffset If there should be applied rotations (such as longitude/latitude effects, seasons etc to the position).
     * @return A list containing two Vec3 positions: [0] final position after all rotations, [1] initial heliocentric position.
     */
    public static Vec3 getPos(CelestialObject object, double semiMajorAxis, double periapsis, double elapsedTime, boolean useSeasonOffset)
    {
        double P = object.period;
        double e = object.eccentricity;
        double O = object.ascendingNode;
        double W = object.argOfPeriapsis;
        double a = semiMajorAxis;
        double q = periapsis;
        double i = object.inclination;

        double rs = getHeliocentricDistance(object, P, elapsedTime, e, a, q);
        double v  = getTrueAnomaly(object, P, elapsedTime, e, a, q);

        double Wv = W + v;
        double cosO = Math.cos(O);
        double sinO = Math.sin(O);
        double cosWv = Math.cos(Wv);
        double sinWv = Math.sin(Wv);
        double cosI = Math.cos(i);
        double sinI = Math.sin(i);

        double sinWvcosI = sinWv * cosI;

        double x = (cosO * cosWv - sinO * sinWvcosI) * rs;
        double y = (sinWv * sinI) * rs;
        double z = (sinO * cosWv + cosO * sinWvcosI) * rs;

        Vec3 pos = new Vec3(x, y, z);

        if (object.obliquityRotation != Vec3.ZERO)
        {
            pos = rotZ(object.obliquityRotation.z(),
                rotX(object.obliquityRotation.x(),
                rotY(object.obliquityRotation.y(), pos)));
        }

        return applyRotations(object, pos, useSeasonOffset);
    }

    /**
     * The player, or the observing body, is essentially at the center of the world, i.e. the vec zero.
     * The sun revolves around us, while the other planets revolve around the sun.
     * This essentially means the sun has the vector position of where the Earth should've been, because
     * its orbit is just the mirrored of the Earths.
     * @param object The object in question.
     * @param semiMajorAxis The semi-major axis of the object.
     * @param elapsedTime The elapsed orbit of the object.
     * @param useSeasonOffset If the pos should be offset by the season of the observer body.
     * @return Gets the position of the current planet irt. its parent based on the Tychonic model and Kepler's laws.
     */
    public static Vec3 getPosObserver(CelestialObject object, double semiMajorAxis, double periapsis, double elapsedTime, boolean useSeasonOffset, boolean invertSemiMajorAxis)
    {
        Vec3 parentSum = Vec3.ZERO;

        for (CelestialObject parent : object.parentObjects)
        {
            Vec3 parentPos = PlanetHelpers.getPos(
                parent,
                invertSemiMajorAxis ? -parent.semiMajorAxis : parent.semiMajorAxis,
                invertSemiMajorAxis ? -parent.periapsis : parent.periapsis,
                parent.elapsedTime,
                useSeasonOffset
            );
            parentSum = parentSum.add(parentPos);
        }

        Vec3 selfPos = PlanetHelpers.getPos(
            object,
            invertSemiMajorAxis ? -semiMajorAxis : semiMajorAxis,
            invertSemiMajorAxis ? -periapsis : periapsis,
            elapsedTime,
            useSeasonOffset
        );

        return selfPos.add(parentSum);
    }
}