package com.fasterxml.jackson.annotation;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public abstract class ObjectIdGenerator<T> implements Serializable {
    public abstract boolean canUseFor(ObjectIdGenerator<?> objectIdGenerator);

    public abstract ObjectIdGenerator<T> forScope(Class<?> cls);

    public abstract T generateId(Object obj);

    public abstract Class<?> getScope();

    public abstract IdKey key(Object obj);

    public abstract ObjectIdGenerator<T> newForSerialization(Object obj);

    public static final class IdKey implements Serializable {
        private static final long serialVersionUID = 1;
        private final int hashCode;
        public final Object key;
        public final Class<?> scope;
        public final Class<?> type;

        public IdKey(Class<?> cls, Class<?> cls2, Object obj) {
            this.type = cls;
            this.scope = cls2;
            this.key = obj;
            int iHashCode = obj.hashCode() + cls.getName().hashCode();
            this.hashCode = cls2 != null ? iHashCode ^ cls2.getName().hashCode() : iHashCode;
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            IdKey idKey = (IdKey) obj;
            return idKey.key.equals(this.key) && idKey.type == this.type && idKey.scope == this.scope;
        }
    }
}
