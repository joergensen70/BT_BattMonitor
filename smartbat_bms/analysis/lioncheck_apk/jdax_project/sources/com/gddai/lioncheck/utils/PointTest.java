package com.gddai.lioncheck.utils;

/* JADX INFO: loaded from: classes.dex */
public class PointTest {
    public static void main(String[] strArr) {
        TrigonViewPoint trigonViewPoint = new TrigonViewPoint(new Point(4.0f, 4.0f), new Point(0.0f, 0.0f), new Point(8.0f, 0.0f));
        System.out.println(PointUtils.isInside(new Point(2.0f, 2.0f), trigonViewPoint) + "new Point(2,2)");
        System.out.println(PointUtils.isInside(new Point(2.0f, 3.0f), trigonViewPoint) + "new Point(2,3)");
        System.out.println(PointUtils.isInside(new Point(3.0f, 2.0f), trigonViewPoint) + "new Point(3,2)");
        System.out.println(PointUtils.isInside(new Point(8.0f, 0.0f), trigonViewPoint) + "new Point(8,0)");
        System.out.println(PointUtils.isInside(new Point(-1.0f, -1.0f), trigonViewPoint) + "new Point(-1,-1)");
    }
}
