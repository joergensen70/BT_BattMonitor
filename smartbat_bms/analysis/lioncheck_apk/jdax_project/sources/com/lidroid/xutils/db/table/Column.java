package com.lidroid.xutils.db.table;

import android.database.Cursor;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.util.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/* JADX INFO: loaded from: classes.dex */
public class Column {
    protected final ColumnConverter columnConverter;
    protected final Field columnField;
    protected final String columnName;
    private final Object defaultValue;
    protected final Method getMethod;
    private int index = -1;
    protected final Method setMethod;
    private Table table;

    Column(Class<?> cls, Field field) {
        this.columnField = field;
        ColumnConverter columnConverter = ColumnConverterFactory.getColumnConverter(field.getType());
        this.columnConverter = columnConverter;
        this.columnName = ColumnUtils.getColumnNameByField(field);
        if (columnConverter != null) {
            this.defaultValue = columnConverter.getFieldValue(ColumnUtils.getColumnDefaultValue(field));
        } else {
            this.defaultValue = null;
        }
        this.getMethod = ColumnUtils.getColumnGetMethod(cls, field);
        this.setMethod = ColumnUtils.getColumnSetMethod(cls, field);
    }

    public void setValue2Entity(Object obj, Cursor cursor, int i) {
        this.index = i;
        Object fieldValue = this.columnConverter.getFieldValue(cursor, i);
        if (fieldValue == null && this.defaultValue == null) {
            return;
        }
        Method method = this.setMethod;
        if (method != null) {
            if (fieldValue == null) {
                try {
                    fieldValue = this.defaultValue;
                } catch (Throwable th) {
                    LogUtils.e(th.getMessage(), th);
                    return;
                }
            }
            method.invoke(obj, fieldValue);
            return;
        }
        try {
            this.columnField.setAccessible(true);
            Field field = this.columnField;
            if (fieldValue == null) {
                fieldValue = this.defaultValue;
            }
            field.set(obj, fieldValue);
        } catch (Throwable th2) {
            LogUtils.e(th2.getMessage(), th2);
        }
    }

    public Object getColumnValue(Object obj) {
        return this.columnConverter.fieldValue2ColumnValue(getFieldValue(obj));
    }

    public Object getFieldValue(Object obj) {
        if (obj == null) {
            return null;
        }
        Method method = this.getMethod;
        if (method != null) {
            try {
                return method.invoke(obj, new Object[0]);
            } catch (Throwable th) {
                LogUtils.e(th.getMessage(), th);
                return null;
            }
        }
        try {
            this.columnField.setAccessible(true);
            return this.columnField.get(obj);
        } catch (Throwable th2) {
            LogUtils.e(th2.getMessage(), th2);
            return null;
        }
    }

    public Table getTable() {
        return this.table;
    }

    void setTable(Table table) {
        this.table = table;
    }

    public int getIndex() {
        return this.index;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public Field getColumnField() {
        return this.columnField;
    }

    public ColumnConverter getColumnConverter() {
        return this.columnConverter;
    }

    public ColumnDbType getColumnDbType() {
        return this.columnConverter.getColumnDbType();
    }
}
