package almagest.util;

import org.joml.Matrix3f;
import org.joml.Matrix4d;
import org.joml.Matrix4dStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class MatrixHelpers
{
    public static Matrix4dStack toMatrix4dStack(Matrix4fStack src)
    {
        Matrix4f[] srcMats = Reflect.get(src, "mats");
        int curr = Reflect.get(src, "curr");
        Matrix4dStack dst = new Matrix4dStack(srcMats.length + 1);
        Matrix4d[] dstMats = Reflect.get(dst, "mats");

        dst.set(new Matrix4d(src));

        for (int i = 0; i < curr; i++)
        {
            dstMats[i].set(new Matrix4d(srcMats[i]));
        }

        Reflect.set(dst, "curr", curr);

        return dst;
    }

    public static Matrix4fStack toMatrix4fStack(Matrix4dStack src)
    {
        Matrix4d[] srcMats = Reflect.get(src, "mats");
        int curr = Reflect.get(src, "curr");
        Matrix4fStack dst = new Matrix4fStack(srcMats.length + 1);
        Matrix4f[] dstMats = Reflect.get(dst, "mats");

        dst.set(new Matrix4f(src));

        for (int i = 0; i < curr; i++)
        {
            dstMats[i].set(new Matrix4f(srcMats[i]));
        }

        Reflect.set(dst, "curr", curr);

        return dst;
    }

    public static Matrix4d toMatrix4d(Matrix4f src)
    {
        return new Matrix4d(src);
    }

    public static Matrix4f toMatrix4f(Matrix4d src)
    {
        return new Matrix4f(src);
    }

    public static PoseStack toPoseStack(Matrix4fStack src)
    {
        PoseStack poseStack = new PoseStack();
        Matrix4f pose = new Matrix4f(src);
        Matrix3f normal = new Matrix3f(pose);

        normal.invert();
        normal.transpose();
        poseStack.last().pose().set(pose);
        poseStack.last().normal().set(normal);

        return poseStack;
    }

    public static PoseStack toPoseStack(Matrix4dStack src)
    {
        PoseStack poseStack = new PoseStack();
        Matrix4f pose = new Matrix4f(src);
        Matrix3f normal = new Matrix3f(pose);

        normal.invert();
        normal.transpose();
        poseStack.last().pose().set(pose);
        poseStack.last().normal().set(normal);

        return poseStack;
    }
}
