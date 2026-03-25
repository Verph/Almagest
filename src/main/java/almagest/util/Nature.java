package almagest.util;

import almagest.config.Config;
import net.minecraft.world.level.Level;

/**
 * Constants in nature, mostly to convert units.
 */
public class Nature
{
    /*
     * ======= MISCELLANEOUS CONSTANTS =======
     */
    /** W/m^2K^4 */
    public static final double STEFAN_BOLTZMANN = 5.67e-8D;
    /** Km */
    public static final double SOLAR_RADIUS = 696340.0D;
    /** Watts */
    public static final double SOLAR_LUMINOSITY = 3.828e26D;
    /** Kg */
    public static final double SOLAR_MASS_KG = 1.98847e30D;
    /** Kelvin */
    public static final double SOLAR_TEFF = 5772.0D;
    /** Kg */
    public static final double EARTH_MASS_KG = 5.972e24D;
    /** m^3/kg/s^2 */
    public static final double GRAVITATIONAL_CONSTANT = 6.67430e-11D;
    /** km^3/kg/s^2 */
    public static final double GRAVITATIONAL_CONSTANT_KM = 6.67430e-20D;

    /*
     * ======= ANGLE UNITS =======
     */
    /** Degrees to radians **/
    public static final double TO_RAD = Math.PI / 180D;
    /** Radians to degrees **/
    public static final double TO_DEG = 180D / Math.PI;
    /** Degrees to arc-seconds **/
    public static final double DEG_TO_ARCSEC = 3600D;
    /** Arc-seconds to degrees **/
    public static final double ARCSEC_TO_DEG = 1D / DEG_TO_ARCSEC;
    /** Arc-seconds to radians **/
    public static final double ARCSEC_TO_RAD = ARCSEC_TO_DEG * TO_RAD;
    /** Degrees to milliarcseconds **/
    public static final double DEG_TO_MILLARCSEC = DEG_TO_ARCSEC * 1000;
    /** Milliarcseconds to degrees **/
    public static final double MILLARCSEC_TO_DEG = 1D / DEG_TO_MILLARCSEC;
    /** Milliarcseconds to radians **/
    public static final double MILLARCSEC_TO_RAD = MILLARCSEC_TO_DEG * TO_RAD;
    /** Radians to milliarcseconds **/
    public static final double RAD_TO_MILLARCSEC = TO_DEG * DEG_TO_MILLARCSEC;
    /** Milliarcseconds to arc-seconds **/
    public static final double MILLIARCSEC_TO_ARCSEC = 1D / 1000D;
    /** Arc-seconds to milliarcseconds **/
    public static final double ARCSEC_TO_MILLIARCSEC = 1000D;
    /** Arc-seconds per year to kilometers per second **/
    public static final double ARCSEC_PER_YEAR_TO_KMS = 4.74047D;

    /*
     * ======= DISTANCE UNITS =======
     */
    /** Parsecs to light years **/
    public static final double PC_TO_LY = 3.2615637769443D;
    /** Light years to parsecs **/
    public static final double LY_TO_PC = 1.0D / PC_TO_LY;
    /** Parsecs to kilometres **/
    public static final double PC_TO_KM = 3.08567758149137e13D;
    /** Kilometres to parsecs **/
    public static final double KM_TO_PC = 1.0D / PC_TO_KM;
    /** Parsecs to metres **/
    public static final double PC_TO_M = PC_TO_KM * 1000.0D;
    /** Metres to parsecs **/
    public static final double M_TO_PC = 1.0D / PC_TO_M;
    /** Astronomical units to kilometres **/
    public static final double AU_TO_KM = 149597870.691D;
    /** Astronomical units to megametres **/
    public static final double AU_TO_MM = AU_TO_KM * 0.001D;
    /** Astronomical units to gigametres **/
    public static final double AU_TO_GM = AU_TO_MM * 0.001D;
    /** Kilometres to astronomical units **/
    public static final double KM_TO_AU = 1.0D / AU_TO_KM;
    /** Megametres to astronomical units **/
    public static final double MM_TO_AU = 1.0D / AU_TO_MM;
    /** Gigametres to astronomical units **/
    public static final double GM_TO_AU = 1.0D / AU_TO_GM;
    /** Light years to kilometers **/
    public static final double LY_TO_KM = 9.46073e12D;
    /** Kilometers to light years **/
    public static final double KM_TO_LY = 1.0D / LY_TO_KM;
    /** Metres to kilometres **/
    public static final double M_TO_KM = 0.001D;
    /** Kilometres to metres **/
    public static final double KM_TO_M = 1000.0D;
    /** Kilometres to megametres **/
    public static final double KM_TO_MM = 1.0D / 1000.0D;
    /** Astronomical units to metres **/
    public static final double AU_TO_M = AU_TO_KM * KM_TO_M;
    /** Metres to megametres **/
    public static final double M_TO_MM = 1.0D / 1000000.0D;
    /** Megametres to metres **/
    public static final double MM_TO_M = 1000000.0D;
    /** Metres to gigametres **/
    public static final double M_TO_GM = 1.0D / 1000000000.0D;
    /** Gigametres to metres **/
    public static final double GM_TO_M = 1000000000.0D;
    /** Kilometres to gigametres **/
    public static final double KM_TO_GM = 1.0D / 1000000.0D;
    /** Gigametres to kilometres **/
    public static final double GM_TO_KM = 1000000.0D;

    /*
     * ======= TIME UNITS =======
     */
    /** Seconds to milliseconds **/
    public static final double S_TO_MS = 1000.0D;
    /** Milliseconds to seconds **/
    public static final double MS_TO_S = 1.0D / S_TO_MS;
    /** Milliseconds to nanoseconds **/
    public static final double MS_TO_NS = 1.0e6D;
    /** Seconds to nanoseconds **/
    public static final double S_TO_NS = S_TO_MS * MS_TO_NS;
    /** Hours to seconds **/
    public static final double H_TO_S = 3600.0D;
    /** Seconds to hours **/
    public static final double S_TO_H = 1.0D / H_TO_S;
    /** Hours to milliseconds **/
    public static final double H_TO_MS = H_TO_S * 1000.0D;
    /** Milliseconds to hours **/
    public static final double MS_TO_H = 1.0D / H_TO_MS;
    /** Days to seconds **/
    public static final double D_TO_S = 86400.0D;
    /** Seconds to days **/
    public static final double S_TO_D = 1.0D / D_TO_S;
    /** Days to milliseconds **/
    public static final double D_TO_MS = D_TO_S * 1000.0D;
    /** Milliseconds to days **/
    public static final double MS_TO_D = 1.0D / D_TO_MS;
    /** Days to nanoseconds **/
    public static final double D_TO_NS = D_TO_S * 1.0e9D;
    /** Nanoseconds to days **/
    public static final double NS_TO_D = 1.0D / D_TO_NS;
    /** Years to seconds **/
    public static final double Y_TO_S = 31557600.0D;
    /** Seconds to years **/
    public static final double S_TO_Y = 1.0D / Y_TO_S;
    /** Years to milliseconds **/
    public static final double Y_TO_MS = Y_TO_S * 1000.0D;
    /** Milliseconds to year **/
    public static final double MS_TO_Y = 1.0D / Y_TO_MS;
    /** Minutes to seconds **/
    public static final double MIN_TO_S = 60.0D;
    /** Hours to ticks **/
    public static final double H_TO_TICKS = 1000.0D;
    /** Years to ticks **/
    public static final double Y_TO_TICKS = Level.TICKS_PER_DAY * Config.COMMON.daysPerYear.get();

    /*
     * ======= SPEEDS =======
     */
    /**
     * Speed of light in m/s
     */
    public static final double C = 299792458D;
}