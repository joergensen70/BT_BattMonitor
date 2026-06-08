package com.fasterxml.jackson.databind.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public final class ContainerBuilder {
    private static final int MAX_BUF = 1000;
    private Object[] b;
    private List<Object> list;
    private Map<String, Object> map;
    private int start;
    private int tail;

    public ContainerBuilder(int i) {
        this.b = new Object[i & (-2)];
    }

    public boolean canReuse() {
        return this.list == null && this.map == null;
    }

    public int bufferLength() {
        return this.b.length;
    }

    public int start() {
        if (this.list != null || this.map != null) {
            throw new IllegalStateException();
        }
        int i = this.start;
        this.start = this.tail;
        return i;
    }

    public int startList(Object obj) {
        if (this.list != null || this.map != null) {
            throw new IllegalStateException();
        }
        int i = this.start;
        this.start = this.tail;
        add(obj);
        return i;
    }

    public int startMap(String str, Object obj) {
        if (this.list != null || this.map != null) {
            throw new IllegalStateException();
        }
        int i = this.start;
        this.start = this.tail;
        put(str, obj);
        return i;
    }

    public void add(Object obj) {
        List<Object> list = this.list;
        if (list != null) {
            list.add(obj);
            return;
        }
        int i = this.tail;
        Object[] objArr = this.b;
        if (i >= objArr.length) {
            _expandList(obj);
        } else {
            this.tail = i + 1;
            objArr[i] = obj;
        }
    }

    public void put(String str, Object obj) {
        Map<String, Object> map = this.map;
        if (map != null) {
            map.put(str, obj);
            return;
        }
        int i = this.tail;
        int i2 = i + 2;
        Object[] objArr = this.b;
        if (i2 > objArr.length) {
            _expandMap(str, obj);
            return;
        }
        int i3 = i + 1;
        this.tail = i3;
        objArr[i] = str;
        this.tail = i + 2;
        objArr[i3] = obj;
    }

    public List<Object> finishList(int i) {
        List<Object> list_buildList = this.list;
        if (list_buildList == null) {
            list_buildList = _buildList(true);
        } else {
            this.list = null;
        }
        this.start = i;
        return list_buildList;
    }

    public Object[] finishArray(int i) {
        Object[] array;
        List<Object> list = this.list;
        if (list == null) {
            array = Arrays.copyOfRange(this.b, this.start, this.tail);
        } else {
            array = list.toArray(new Object[this.tail - this.start]);
            this.list = null;
        }
        this.start = i;
        return array;
    }

    public <T> Object[] finishArray(int i, Class<T> cls) {
        int i2 = this.tail - this.start;
        Object[] array = (Object[]) Array.newInstance((Class<?>) cls, i2);
        List<Object> list = this.list;
        if (list == null) {
            System.arraycopy(this.b, this.start, array, 0, i2);
        } else {
            array = list.toArray(array);
            this.list = null;
        }
        this.start = i;
        return array;
    }

    public Map<String, Object> finishMap(int i) {
        Map<String, Object> map_buildMap = this.map;
        if (map_buildMap == null) {
            map_buildMap = _buildMap(true);
        } else {
            this.map = null;
        }
        this.start = i;
        return map_buildMap;
    }

    private void _expandList(Object obj) {
        Object[] objArr = this.b;
        if (objArr.length < 1000) {
            Object[] objArrCopyOf = Arrays.copyOf(objArr, objArr.length << 1);
            this.b = objArrCopyOf;
            int i = this.tail;
            this.tail = i + 1;
            objArrCopyOf[i] = obj;
            return;
        }
        List<Object> list_buildList = _buildList(false);
        this.list = list_buildList;
        list_buildList.add(obj);
    }

    /* JADX WARN: Removed duplicated region for block: B:9:0x000f A[PHI: r3
  0x000f: PHI (r3v6 int) = (r3v1 int), (r3v8 int) binds: [B:8:0x000d, B:5:0x0008] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private java.util.List<java.lang.Object> _buildList(boolean r3) {
        /*
            r2 = this;
            int r0 = r2.tail
            int r1 = r2.start
            int r0 = r0 - r1
            if (r3 == 0) goto Lb
            r3 = 2
            if (r0 >= r3) goto L1b
            goto Lf
        Lb:
            r3 = 20
            if (r0 >= r3) goto L11
        Lf:
            r0 = r3
            goto L1b
        L11:
            r3 = 1000(0x3e8, float:1.401E-42)
            if (r0 >= r3) goto L18
            int r3 = r0 >> 1
            goto L1a
        L18:
            int r3 = r0 >> 2
        L1a:
            int r0 = r0 + r3
        L1b:
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>(r0)
            int r0 = r2.start
        L22:
            int r1 = r2.tail
            if (r0 >= r1) goto L30
            java.lang.Object[] r1 = r2.b
            r1 = r1[r0]
            r3.add(r1)
            int r0 = r0 + 1
            goto L22
        L30:
            int r0 = r2.start
            r2.tail = r0
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.util.ContainerBuilder._buildList(boolean):java.util.List");
    }

    private void _expandMap(String str, Object obj) {
        Object[] objArr = this.b;
        if (objArr.length < 1000) {
            Object[] objArrCopyOf = Arrays.copyOf(objArr, objArr.length << 1);
            this.b = objArrCopyOf;
            int i = this.tail;
            int i2 = i + 1;
            this.tail = i2;
            objArrCopyOf[i] = str;
            this.tail = i + 2;
            objArrCopyOf[i2] = obj;
            return;
        }
        Map<String, Object> map_buildMap = _buildMap(false);
        this.map = map_buildMap;
        map_buildMap.put(str, obj);
    }

    /* JADX WARN: Removed duplicated region for block: B:16:0x0024  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private java.util.Map<java.lang.String, java.lang.Object> _buildMap(boolean r5) {
        /*
            r4 = this;
            int r0 = r4.tail
            int r1 = r4.start
            int r0 = r0 - r1
            int r1 = r0 >> 1
            if (r5 == 0) goto L19
            r5 = 3
            if (r1 > r5) goto Le
            r5 = 4
            goto L2a
        Le:
            r5 = 40
            if (r1 > r5) goto L13
            goto L24
        L13:
            int r5 = r0 >> 3
            int r0 = r0 >> 5
            int r5 = r5 + r0
            goto L29
        L19:
            r5 = 10
            if (r1 >= r5) goto L20
            r5 = 16
            goto L2a
        L20:
            r5 = 1000(0x3e8, float:1.401E-42)
            if (r1 >= r5) goto L27
        L24:
            int r5 = r0 >> 2
            goto L29
        L27:
            int r5 = r1 / 3
        L29:
            int r5 = r5 + r1
        L2a:
            java.util.LinkedHashMap r0 = new java.util.LinkedHashMap
            r1 = 1061997773(0x3f4ccccd, float:0.8)
            r0.<init>(r5, r1)
            int r5 = r4.start
        L34:
            int r1 = r4.tail
            if (r5 >= r1) goto L48
            java.lang.Object[] r1 = r4.b
            r2 = r1[r5]
            java.lang.String r2 = (java.lang.String) r2
            int r3 = r5 + 1
            r1 = r1[r3]
            r0.put(r2, r1)
            int r5 = r5 + 2
            goto L34
        L48:
            int r5 = r4.start
            r4.tail = r5
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.databind.util.ContainerBuilder._buildMap(boolean):java.util.Map");
    }
}
