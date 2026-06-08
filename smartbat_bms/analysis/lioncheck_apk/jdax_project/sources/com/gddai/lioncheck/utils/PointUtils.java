package com.gddai.lioncheck.utils;

/* JADX INFO: loaded from: classes.dex */
public class PointUtils {
    public static int onTouchPoint(Point point, TrigonViewPoint trigonViewPoint, TrigonViewPoint trigonViewPoint2, TrigonViewPoint trigonViewPoint3, TrigonViewPoint trigonViewPoint4) {
        if (isInside(point, trigonViewPoint)) {
            return 0;
        }
        if (isInside(point, trigonViewPoint2)) {
            return 1;
        }
        if (isInside(point, trigonViewPoint3)) {
            return 2;
        }
        return isInside(point, trigonViewPoint4) ? 3 : -1;
    }

    public static boolean isInside(Point point, TrigonViewPoint trigonViewPoint) {
        return isInsideX(point, trigonViewPoint.p1, trigonViewPoint.p2) >= 0.0f && isInsideX(point, trigonViewPoint.p1, trigonViewPoint.p3) <= 0.0f && isInsideY(point, trigonViewPoint.p2, trigonViewPoint.p3) <= 0.0f;
    }

    public static float isInsideX(Point point, Point point2, Point point3) {
        float f = point2.y - point3.y;
        float f2 = point2.y - point.y;
        if (f == 0.0f) {
            f = 1.0f;
            f2 = 1.0f;
        }
        return point.x - (point2.x - ((f2 * (point2.x - point3.x)) / f));
    }

    public static float isInsideY(Point point, Point point2, Point point3) {
        float f = point2.x - point3.x;
        float f2 = point2.x - point.x;
        if (f == 0.0f) {
            f = 1.0f;
            f2 = 1.0f;
        }
        return point.y - (point2.y - ((f2 * (point2.y - point3.y)) / f));
    }
}
