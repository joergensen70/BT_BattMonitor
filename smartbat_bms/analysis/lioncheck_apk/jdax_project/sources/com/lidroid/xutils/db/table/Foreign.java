package com.lidroid.xutils.db.table;

import android.database.Cursor;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.db.sqlite.ForeignLazyLoader;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import java.lang.reflect.Field;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class Foreign extends Column {
    private final ColumnConverter foreignColumnConverter;
    private final String foreignColumnName;

    @Override // com.lidroid.xutils.db.table.Column
    public Object getDefaultValue() {
        return null;
    }

    Foreign(Class<?> cls, Field field) {
        super(cls, field);
        String foreignColumnNameByField = ColumnUtils.getForeignColumnNameByField(field);
        this.foreignColumnName = foreignColumnNameByField;
        this.foreignColumnConverter = ColumnConverterFactory.getColumnConverter(TableUtils.getColumnOrId(getForeignEntityType(), foreignColumnNameByField).columnField.getType());
    }

    public String getForeignColumnName() {
        return this.foreignColumnName;
    }

    public Class<?> getForeignEntityType() {
        return ColumnUtils.getForeignEntityType(this);
    }

    @Override // com.lidroid.xutils.db.table.Column
    public void setValue2Entity(Object obj, Cursor cursor, int i) {
        Object firstFromDb;
        Object fieldValue = this.foreignColumnConverter.getFieldValue(cursor, i);
        if (fieldValue == null) {
            return;
        }
        Class<?> type = this.columnField.getType();
        if (type.equals(ForeignLazyLoader.class)) {
            firstFromDb = new ForeignLazyLoader(this, fieldValue);
        } else if (type.equals(List.class)) {
            try {
                firstFromDb = new ForeignLazyLoader(this, fieldValue).getAllFromDb();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
                firstFromDb = null;
            }
        } else {
            try {
                firstFromDb = new ForeignLazyLoader(this, fieldValue).getFirstFromDb();
            } catch (DbException e2) {
                LogUtils.e(e2.getMessage(), e2);
                firstFromDb = null;
            }
        }
        if (this.setMethod != null) {
            try {
                this.setMethod.invoke(obj, firstFromDb);
                return;
            } catch (Throwable th) {
                LogUtils.e(th.getMessage(), th);
                return;
            }
        }
        try {
            this.columnField.setAccessible(true);
            this.columnField.set(obj, firstFromDb);
        } catch (Throwable th2) {
            LogUtils.e(th2.getMessage(), th2);
        }
    }

    @Override // com.lidroid.xutils.db.table.Column
    public Object getColumnValue(Object obj) {
        Object fieldValue = getFieldValue(obj);
        Object columnValue = null;
        if (fieldValue != null) {
            Class<?> type = this.columnField.getType();
            if (type.equals(ForeignLazyLoader.class)) {
                return ((ForeignLazyLoader) fieldValue).getColumnValue();
            }
            if (type.equals(List.class)) {
                try {
                    List list = (List) fieldValue;
                    if (list.size() > 0) {
                        Column columnOrId = TableUtils.getColumnOrId(ColumnUtils.getForeignEntityType(this), this.foreignColumnName);
                        columnValue = columnOrId.getColumnValue(list.get(0));
                        Table table = getTable();
                        if (table != null && (columnOrId instanceof Id)) {
                            for (Object obj2 : list) {
                                if (columnOrId.getColumnValue(obj2) == null) {
                                    table.db.saveOrUpdate(obj2);
                                }
                            }
                        }
                        return columnOrId.getColumnValue(list.get(0));
                    }
                } catch (Throwable th) {
                    LogUtils.e(th.getMessage(), th);
                }
            } else {
                try {
                    Column columnOrId2 = TableUtils.getColumnOrId(type, this.foreignColumnName);
                    columnValue = columnOrId2.getColumnValue(fieldValue);
                    Table table2 = getTable();
                    if (table2 != null && columnValue == null && (columnOrId2 instanceof Id)) {
                        table2.db.saveOrUpdate(fieldValue);
                    }
                    return columnOrId2.getColumnValue(fieldValue);
                } catch (Throwable th2) {
                    LogUtils.e(th2.getMessage(), th2);
                }
            }
        }
        return columnValue;
    }

    @Override // com.lidroid.xutils.db.table.Column
    public ColumnDbType getColumnDbType() {
        return this.foreignColumnConverter.getColumnDbType();
    }
}
