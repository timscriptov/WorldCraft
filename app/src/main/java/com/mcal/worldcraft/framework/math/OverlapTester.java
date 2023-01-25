package com.mcal.worldcraft.framework.math;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class OverlapTester {
    public static boolean overlapCircles(@NonNull Circle c1, @NonNull Circle c2) {
        float distance = c1.center.distSquared(c2.center);
        float radiusSum = c1.radius + c2.radius;
        return distance <= radiusSum * radiusSum;
    }

    @Contract(pure = true)
    public static boolean overlapRectangles(@NonNull Rectangle r1, @NonNull Rectangle r2) {
        return r1.lowerLeft.x < r2.lowerLeft.x + r2.width && r1.lowerLeft.x + r1.width > r2.lowerLeft.x && r1.lowerLeft.y < r2.lowerLeft.y + r2.height && r1.lowerLeft.y + r1.height > r2.lowerLeft.y;
    }

    public static boolean overlapCircleRectangle(@NonNull Circle c, @NonNull Rectangle r) {
        float closestX = c.center.x;
        float closestY = c.center.y;
        if (c.center.x < r.lowerLeft.x) {
            closestX = r.lowerLeft.x;
        } else if (c.center.x > r.lowerLeft.x + r.width) {
            closestX = r.lowerLeft.x + r.width;
        }
        if (c.center.y < r.lowerLeft.y) {
            closestY = r.lowerLeft.y;
        } else if (c.center.y > r.lowerLeft.y + r.height) {
            closestY = r.lowerLeft.y + r.height;
        }
        return c.center.distSquared(closestX, closestY) < c.radius * c.radius;
    }

    public static boolean overlapSpheres(@NonNull Sphere s1, @NonNull Sphere s2) {
        float distance = s1.center.distSquared(s2.center);
        float radiusSum = s1.radius + s2.radius;
        return distance <= radiusSum * radiusSum;
    }

    public static boolean pointInSphere(@NonNull Sphere c, Vector3 p) {
        return c.center.distSquared(p) < c.radius * c.radius;
    }

    public static boolean pointInSphere(@NonNull Sphere c, float x, float y, float z) {
        return c.center.distSquared(x, y, z) < c.radius * c.radius;
    }

    public static boolean pointInCircle(@NonNull Circle c, Vector2 p) {
        return c.center.distSquared(p) < c.radius * c.radius;
    }

    public static boolean pointInCircle(@NonNull Circle c, float x, float y) {
        return c.center.distSquared(x, y) < c.radius * c.radius;
    }

    @Contract(pure = true)
    public static boolean pointInRectangle(@NonNull Rectangle r, @NonNull Vector2 p) {
        return r.lowerLeft.x <= p.x && r.lowerLeft.x + r.width >= p.x && r.lowerLeft.y <= p.y && r.lowerLeft.y + r.height >= p.y;
    }

    @Contract(pure = true)
    public static boolean pointInRectangle(@NonNull Rectangle r, float x, float y) {
        return r.lowerLeft.x <= x && r.lowerLeft.x + r.width >= x && r.lowerLeft.y <= y && r.lowerLeft.y + r.height >= y;
    }
}
