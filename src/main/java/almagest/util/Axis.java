package almagest.util;

import org.joml.Quaterniond;
import org.joml.Vector3d;

@FunctionalInterface
public interface Axis
{
    Axis XN = (radians) -> {
        return (new Quaterniond()).rotationX(-radians);
    };
    Axis XP = (radians) -> {
        return (new Quaterniond()).rotationX(radians);
    };
    Axis YN = (radians) -> {
        return (new Quaterniond()).rotationY(-radians);
    };
    Axis YP = (radians) -> {
        return (new Quaterniond()).rotationY(radians);
    };
    Axis ZN = (radians) -> {
        return (new Quaterniond()).rotationZ(-radians);
    };
    Axis ZP = (radians) -> {
        return (new Quaterniond()).rotationZ(radians);
    };

    static Axis of(Vector3d axis)
    {
        return (radians) -> {
            return (new Quaterniond()).rotateAxis(radians, axis);
        };
    }

    Quaterniond rotation(double radians);

    default Quaterniond rotationDegrees(double degrees)
    {
        return this.rotation(degrees * (Math.PI / 180D));
    }
}