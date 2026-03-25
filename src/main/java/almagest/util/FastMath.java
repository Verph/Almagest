package almagest.util;

import almagest.config.Config;
import net.minecraft.util.Mth;

public class FastMath
{
    public static final double TWO_PI = 2.0D * Math.PI;
    public static final double RAD_TO_DEG = 180.0D / Math.PI;

    public static final int TABLE_SIZE = 10240000;
    public static final double STEP = 2.0 / TABLE_SIZE;
    public static final double[] acosTable = new double[TABLE_SIZE + 1];
    public static final double[] asinTable = new double[TABLE_SIZE + 1];
    public static final double[] atanTable = new double[TABLE_SIZE + 1];

    public static final int TABLE_SIZE_FAST = 16384;
    public static final double STEP_FAST = 2.0 / TABLE_SIZE_FAST;
    public static final double[] acosTableFast = new double[TABLE_SIZE_FAST + 1];
    public static final double[] asinTableFast = new double[TABLE_SIZE_FAST + 1];
    public static final double[] atanTableFast = new double[TABLE_SIZE_FAST + 1];

    public static final int HYP_TABLE_SIZE = 4096;
    public static final double HYP_MIN = -5.0;
    public static final double HYP_MAX = 5.0;
    public static final double HYP_STEP = (HYP_MAX - HYP_MIN) / HYP_TABLE_SIZE;

    public static final double[] sinhTable = new double[HYP_TABLE_SIZE + 1];
    public static final double[] coshTable = new double[HYP_TABLE_SIZE + 1];
    public static final double[] tanhTable = new double[HYP_TABLE_SIZE + 1];

    public static void initTrigTables()
    {
        for (int i = 0; i <= TABLE_SIZE; i++)
        {
            double x = -1.0 + i * STEP;
            acosTable[i] = Math.acos(x);
            double y = -1.0 + i * STEP;
            asinTable[i] = Math.asin(y);
            double z = -1.0 + i * STEP;
            atanTable[i] = Math.atan(z);
        }
        for (int i = 0; i <= TABLE_SIZE_FAST; i++)
        {
            double x = -1.0 + i * STEP_FAST;
            acosTableFast[i] = Math.acos(x);
            double y = -1.0 + i * STEP_FAST;
            asinTableFast[i] = Math.asin(y);
            double z = -1.0 + i * STEP_FAST;
            atanTableFast[i] = Math.atan(z);
        }
        initHyperbolicTables();
    }

    public static void initHyperbolicTables()
    {
        for (int i = 0; i <= HYP_TABLE_SIZE; i++)
        {
            double x = HYP_MIN + i * HYP_STEP;
            sinhTable[i] = Math.sinh(x);
            coshTable[i] = Math.cosh(x);
            tanhTable[i] = Math.tanh(x);
        }
    }

    public static double acos(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.acos(x);
        }

        x = Mth.clamp(x, -1.0D, 1.0D);

        int index = (int) ((x + 1.0) / STEP);
        return acosTable[index];
    }

    public static double asin(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.asin(x);
        }

        x = Mth.clamp(x, -1.0D, 1.0D);

        int index = (int) ((x + 1.0) / STEP);
        return asinTable[index];
    }

    public static double atan(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.atan(x);
        }

        // Handle values outside [-1,1] using reciprocal trick
        if (x > 1.0)
        {
            return Math.PI / 2 - atan(1.0 / x);
        }
        else if (x < -1.0)
        {
            return -Math.PI / 2 - atan(1.0 / x);
        }

        int index = (int) ((x + 1.0) / STEP);
        return atanTable[index];
    }

    public static double atan2(double y, double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.atan2(y, x);
        }

        if (x == 0.0)
        {
            return (y > 0.0) ? Math.PI / 2 : (y < 0.0 ? -Math.PI / 2 : 0.0);
        }

        double angle = atan(y / x);

        int quadrant = ((x < 0) ? 2 : 0) | ((y < 0) ? 1 : 0);
        switch (quadrant)
        {
            case 2:  return angle + Math.PI;   // x < 0, y >= 0
            case 3:  return angle - Math.PI;   // x < 0, y < 0
            default: return angle;             // x > 0
        }
    }

    public static double acosFast(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.acos(x);
        }

        x = Mth.clamp(x, -1.0D, 1.0D);

        int index = (int) ((x + 1.0) / STEP_FAST);
        return acosTableFast[index];
    }

    public static double asinFast(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.asin(x);
        }

        x = Mth.clamp(x, -1.0D, 1.0D);

        int index = (int) ((x + 1.0) / STEP_FAST);
        return asinTableFast[index];
    }

    public static double atanFast(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.atan(x);
        }

        // Handle values outside [-1,1] using reciprocal trick
        if (x > 1.0)
        {
            return Math.PI / 2 - atanFast(1.0 / x);
        }
        else if (x < -1.0)
        {
            return -Math.PI / 2 - atanFast(1.0 / x);
        }

        int index = (int) ((x + 1.0) / STEP_FAST);
        return atanTableFast[index];
    }

    public static double atan2Fast(double y, double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
        {
            return Math.atan2(y, x);
        }

        if (x == 0.0)
        {
            return (y > 0.0) ? Math.PI / 2 : (y < 0.0 ? -Math.PI / 2 : 0.0);
        }

        double angle = atanFast(y / x);

        int quadrant = ((x < 0) ? 2 : 0) | ((y < 0) ? 1 : 0);
        switch (quadrant)
        {
            case 2:  return angle + Math.PI;   // x < 0, y >= 0
            case 3:  return angle - Math.PI;   // x < 0, y < 0
            default: return angle;             // x > 0
        }
    }

    public static double sinh(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
            return Math.sinh(x);

        if (x <= HYP_MIN || x >= HYP_MAX)
            return Math.sinh(x);

        double t = (x - HYP_MIN) / HYP_STEP;
        int i = (int)t;
        double frac = t - i;

        return sinhTable[i] * (1 - frac) + sinhTable[i + 1] * frac;
    }

    public static double cosh(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
            return Math.cosh(x);

        if (x <= HYP_MIN || x >= HYP_MAX)
            return Math.cosh(x);

        double t = (x - HYP_MIN) / HYP_STEP;
        int i = (int)t;
        double frac = t - i;

        return coshTable[i] * (1 - frac) + coshTable[i + 1] * frac;
    }

    public static double tanh(double x)
    {
        if (!Config.COMMON.toggleFastTrigMath.get())
            return Math.tanh(x);

        if (x <= HYP_MIN) return -1.0;
        if (x >= HYP_MAX) return 1.0;

        double t = (x - HYP_MIN) / HYP_STEP;
        int i = (int)t;
        double frac = t - i;

        return tanhTable[i] * (1 - frac) + tanhTable[i + 1] * frac;
    }
}
