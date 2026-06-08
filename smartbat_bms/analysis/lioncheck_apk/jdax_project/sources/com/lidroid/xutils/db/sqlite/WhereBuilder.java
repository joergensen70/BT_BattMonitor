package com.lidroid.xutils.db.sqlite;

import android.text.TextUtils;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.table.ColumnUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class WhereBuilder {
    private final List<String> whereItems = new ArrayList();

    private WhereBuilder() {
    }

    public static WhereBuilder b() {
        return new WhereBuilder();
    }

    public static WhereBuilder b(String str, String str2, Object obj) {
        WhereBuilder whereBuilder = new WhereBuilder();
        whereBuilder.appendCondition(null, str, str2, obj);
        return whereBuilder;
    }

    public WhereBuilder and(String str, String str2, Object obj) {
        appendCondition(this.whereItems.size() == 0 ? null : "AND", str, str2, obj);
        return this;
    }

    public WhereBuilder or(String str, String str2, Object obj) {
        appendCondition(this.whereItems.size() == 0 ? null : "OR", str, str2, obj);
        return this;
    }

    public WhereBuilder expr(String str) {
        this.whereItems.add(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + str);
        return this;
    }

    public WhereBuilder expr(String str, String str2, Object obj) {
        appendCondition(null, str, str2, obj);
        return this;
    }

    public int getWhereItemSize() {
        return this.whereItems.size();
    }

    public String toString() {
        if (this.whereItems.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = this.whereItems.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
        }
        return sb.toString();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v10 */
    /* JADX WARN: Type inference failed for: r2v2, types: [java.util.ArrayList] */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.lang.Iterable] */
    /* JADX WARN: Type inference failed for: r2v5, types: [java.lang.Iterable] */
    /* JADX WARN: Type inference failed for: r2v6, types: [java.util.ArrayList] */
    /* JADX WARN: Type inference failed for: r2v7, types: [java.lang.Iterable] */
    /* JADX WARN: Type inference failed for: r2v9, types: [java.lang.Iterable] */
    private void appendCondition(String str, String str2, String str3, Object obj) {
        StringBuilder sb = new StringBuilder();
        if (this.whereItems.size() > 0) {
            sb.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        }
        if (!TextUtils.isEmpty(str)) {
            sb.append(str + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        }
        sb.append(str2);
        if ("!=".equals(str3)) {
            str3 = "<>";
        } else if ("==".equals(str3)) {
            str3 = "=";
        }
        if (obj == null) {
            if ("=".equals(str3)) {
                sb.append(" IS NULL");
            } else if ("<>".equals(str3)) {
                sb.append(" IS NOT NULL");
            } else {
                sb.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + str3 + " NULL");
            }
        } else {
            sb.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + str3 + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            int i = 0;
            ?? arrayList = 0;
            ?? arrayList2 = 0;
            if ("IN".equalsIgnoreCase(str3)) {
                if (obj instanceof Iterable) {
                    arrayList2 = (Iterable) obj;
                } else if (obj.getClass().isArray()) {
                    arrayList2 = new ArrayList();
                    int length = Array.getLength(obj);
                    while (i < length) {
                        arrayList2.add(Array.get(obj, i));
                        i++;
                    }
                }
                if (arrayList2 != 0) {
                    StringBuffer stringBuffer = new StringBuffer("(");
                    Iterator it = arrayList2.iterator();
                    while (it.hasNext()) {
                        Object objConvert2DbColumnValueIfNeeded = ColumnUtils.convert2DbColumnValueIfNeeded(it.next());
                        if (ColumnDbType.TEXT.equals(ColumnConverterFactory.getDbColumnType(objConvert2DbColumnValueIfNeeded.getClass()))) {
                            String string = objConvert2DbColumnValueIfNeeded.toString();
                            if (string.indexOf(39) != -1) {
                                string = string.replace("'", "''");
                            }
                            stringBuffer.append("'" + string + "'");
                        } else {
                            stringBuffer.append(objConvert2DbColumnValueIfNeeded);
                        }
                        stringBuffer.append(",");
                    }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    stringBuffer.append(")");
                    sb.append(stringBuffer.toString());
                } else {
                    throw new IllegalArgumentException("value must be an Array or an Iterable.");
                }
            } else if ("BETWEEN".equalsIgnoreCase(str3)) {
                if (obj instanceof Iterable) {
                    arrayList = (Iterable) obj;
                } else if (obj.getClass().isArray()) {
                    arrayList = new ArrayList();
                    int length2 = Array.getLength(obj);
                    while (i < length2) {
                        arrayList.add(Array.get(obj, i));
                        i++;
                    }
                }
                if (arrayList != 0) {
                    Iterator it2 = arrayList.iterator();
                    if (!it2.hasNext()) {
                        throw new IllegalArgumentException("value must have tow items.");
                    }
                    Object next = it2.next();
                    if (!it2.hasNext()) {
                        throw new IllegalArgumentException("value must have tow items.");
                    }
                    Object next2 = it2.next();
                    Object objConvert2DbColumnValueIfNeeded2 = ColumnUtils.convert2DbColumnValueIfNeeded(next);
                    Object objConvert2DbColumnValueIfNeeded3 = ColumnUtils.convert2DbColumnValueIfNeeded(next2);
                    if (ColumnDbType.TEXT.equals(ColumnConverterFactory.getDbColumnType(objConvert2DbColumnValueIfNeeded2.getClass()))) {
                        String string2 = objConvert2DbColumnValueIfNeeded2.toString();
                        if (string2.indexOf(39) != -1) {
                            string2 = string2.replace("'", "''");
                        }
                        String string3 = objConvert2DbColumnValueIfNeeded3.toString();
                        if (string3.indexOf(39) != -1) {
                            string3 = string3.replace("'", "''");
                        }
                        sb.append("'" + string2 + "'");
                        sb.append(" AND ");
                        sb.append("'" + string3 + "'");
                    } else {
                        sb.append(objConvert2DbColumnValueIfNeeded2);
                        sb.append(" AND ");
                        sb.append(objConvert2DbColumnValueIfNeeded3);
                    }
                } else {
                    throw new IllegalArgumentException("value must be an Array or an Iterable.");
                }
            } else {
                Object objConvert2DbColumnValueIfNeeded4 = ColumnUtils.convert2DbColumnValueIfNeeded(obj);
                if (ColumnDbType.TEXT.equals(ColumnConverterFactory.getDbColumnType(objConvert2DbColumnValueIfNeeded4.getClass()))) {
                    String string4 = objConvert2DbColumnValueIfNeeded4.toString();
                    if (string4.indexOf(39) != -1) {
                        string4 = string4.replace("'", "''");
                    }
                    sb.append("'" + string4 + "'");
                } else {
                    sb.append(objConvert2DbColumnValueIfNeeded4);
                }
            }
        }
        this.whereItems.add(sb.toString());
    }
}
