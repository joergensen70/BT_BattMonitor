package com.lidroid.xutils.db.sqlite;

import com.lidroid.xutils.db.table.TableUtils;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class Selector {
    protected Class<?> entityType;
    protected int limit = 0;
    protected int offset = 0;
    protected List<OrderBy> orderByList;
    protected String tableName;
    protected WhereBuilder whereBuilder;

    private Selector(Class<?> cls) {
        this.entityType = cls;
        this.tableName = TableUtils.getTableName(cls);
    }

    public static Selector from(Class<?> cls) {
        return new Selector(cls);
    }

    public Selector where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    public Selector where(String str, String str2, Object obj) {
        this.whereBuilder = WhereBuilder.b(str, str2, obj);
        return this;
    }

    public Selector and(String str, String str2, Object obj) {
        this.whereBuilder.and(str, str2, obj);
        return this;
    }

    public Selector and(WhereBuilder whereBuilder) {
        this.whereBuilder.expr("AND (" + whereBuilder.toString() + ")");
        return this;
    }

    public Selector or(String str, String str2, Object obj) {
        this.whereBuilder.or(str, str2, obj);
        return this;
    }

    public Selector or(WhereBuilder whereBuilder) {
        this.whereBuilder.expr("OR (" + whereBuilder.toString() + ")");
        return this;
    }

    public Selector expr(String str) {
        if (this.whereBuilder == null) {
            this.whereBuilder = WhereBuilder.b();
        }
        this.whereBuilder.expr(str);
        return this;
    }

    public Selector expr(String str, String str2, Object obj) {
        if (this.whereBuilder == null) {
            this.whereBuilder = WhereBuilder.b();
        }
        this.whereBuilder.expr(str, str2, obj);
        return this;
    }

    public DbModelSelector groupBy(String str) {
        return new DbModelSelector(this, str);
    }

    public DbModelSelector select(String... strArr) {
        return new DbModelSelector(this, strArr);
    }

    public Selector orderBy(String str) {
        if (this.orderByList == null) {
            this.orderByList = new ArrayList(2);
        }
        this.orderByList.add(new OrderBy(str));
        return this;
    }

    public Selector orderBy(String str, boolean z) {
        if (this.orderByList == null) {
            this.orderByList = new ArrayList(2);
        }
        this.orderByList.add(new OrderBy(str, z));
        return this;
    }

    public Selector limit(int i) {
        this.limit = i;
        return this;
    }

    public Selector offset(int i) {
        this.offset = i;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(this.tableName);
        WhereBuilder whereBuilder = this.whereBuilder;
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            sb.append(" WHERE ").append(this.whereBuilder.toString());
        }
        if (this.orderByList != null) {
            for (int i = 0; i < this.orderByList.size(); i++) {
                sb.append(" ORDER BY ").append(this.orderByList.get(i).toString());
            }
        }
        if (this.limit > 0) {
            sb.append(" LIMIT ").append(this.limit);
            sb.append(" OFFSET ").append(this.offset);
        }
        return sb.toString();
    }

    public Class<?> getEntityType() {
        return this.entityType;
    }

    protected class OrderBy {
        private String columnName;
        private boolean desc;

        public OrderBy(String str) {
            this.columnName = str;
        }

        public OrderBy(String str, boolean z) {
            this.columnName = str;
            this.desc = z;
        }

        public String toString() {
            return this.columnName + (this.desc ? " DESC" : " ASC");
        }
    }
}
