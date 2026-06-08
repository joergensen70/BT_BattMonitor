package com.gddai.lioncheck.utils;

/* JADX INFO: loaded from: classes.dex */
public class TrigonViewPoint {
    public Point p1;
    public Point p2;
    public Point p3;

    public Point getP1() {
        return this.p1;
    }

    public void setP1(Point point) {
        this.p1 = point;
    }

    public Point getP2() {
        return this.p2;
    }

    public void setP2(Point point) {
        this.p2 = point;
    }

    public Point getP3() {
        return this.p3;
    }

    public void setP3(Point point) {
        this.p3 = point;
    }

    public TrigonViewPoint(Point point, Point point2, Point point3) {
        this.p1 = point;
        this.p2 = point2;
        this.p3 = point3;
    }
}
