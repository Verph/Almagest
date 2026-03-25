package almagest.util;

public class MutableVec3
{
    public double x, y, z;

    public MutableVec3 set(double x, double y, double z)
    {
        this.x = x; this.y = y; this.z = z;
        return this;
    }

    public MutableVec3 normalizeInPlace()
    {
        double d = Math.sqrt(x*x + y*y + z*z);
        if (d != 0) { x /= d; y /= d; z /= d; }
        return this;
    }

    public MutableVec3 scaleInPlace(double s)
    {
        x *= s; y *= s; z *= s;
        return this;
    }
}
