package com.fasterxml.jackson.databind.cfg;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public abstract class ContextAttributes {
    public abstract Object getAttribute(Object obj);

    public abstract ContextAttributes withPerCallAttribute(Object obj, Object obj2);

    public abstract ContextAttributes withSharedAttribute(Object obj, Object obj2);

    public abstract ContextAttributes withSharedAttributes(Map<Object, Object> map);

    public abstract ContextAttributes withoutSharedAttribute(Object obj);

    public static ContextAttributes getEmpty() {
        return Impl.getEmpty();
    }

    public static class Impl extends ContextAttributes implements Serializable {
        protected static final Impl EMPTY = new Impl(Collections.emptyMap());
        protected static final Object NULL_SURROGATE = new Object();
        private static final long serialVersionUID = 1;
        protected transient Map<Object, Object> _nonShared;
        protected final Map<Object, Object> _shared;

        protected Impl(Map<Object, Object> map) {
            this._shared = map;
            this._nonShared = null;
        }

        protected Impl(Map<Object, Object> map, Map<Object, Object> map2) {
            this._shared = map;
            this._nonShared = map2;
        }

        public static ContextAttributes getEmpty() {
            return EMPTY;
        }

        @Override // com.fasterxml.jackson.databind.cfg.ContextAttributes
        public ContextAttributes withSharedAttribute(Object obj, Object obj2) {
            Map<Object, Object> map_copy;
            if (this == EMPTY) {
                map_copy = new HashMap<>(8);
            } else {
                map_copy = _copy(this._shared);
            }
            map_copy.put(obj, obj2);
            return new Impl(map_copy);
        }

        @Override // com.fasterxml.jackson.databind.cfg.ContextAttributes
        public ContextAttributes withSharedAttributes(Map<Object, Object> map) {
            return new Impl(map);
        }

        @Override // com.fasterxml.jackson.databind.cfg.ContextAttributes
        public ContextAttributes withoutSharedAttribute(Object obj) {
            if (this._shared.isEmpty() || !this._shared.containsKey(obj)) {
                return this;
            }
            if (this._shared.size() == 1) {
                return EMPTY;
            }
            Map<Object, Object> map_copy = _copy(this._shared);
            map_copy.remove(obj);
            return new Impl(map_copy);
        }

        @Override // com.fasterxml.jackson.databind.cfg.ContextAttributes
        public Object getAttribute(Object obj) {
            Object obj2;
            Map<Object, Object> map = this._nonShared;
            if (map != null && (obj2 = map.get(obj)) != null) {
                if (obj2 == NULL_SURROGATE) {
                    return null;
                }
                return obj2;
            }
            return this._shared.get(obj);
        }

        @Override // com.fasterxml.jackson.databind.cfg.ContextAttributes
        public ContextAttributes withPerCallAttribute(Object obj, Object obj2) {
            if (obj2 == null) {
                if (!this._shared.containsKey(obj)) {
                    return this;
                }
                obj2 = NULL_SURROGATE;
            }
            Map<Object, Object> map = this._nonShared;
            if (map == null) {
                return nonSharedInstance(obj, obj2);
            }
            map.put(obj, obj2);
            return this;
        }

        protected ContextAttributes nonSharedInstance(Object obj, Object obj2) {
            HashMap map = new HashMap();
            if (obj2 == null) {
                obj2 = NULL_SURROGATE;
            }
            map.put(obj, obj2);
            return new Impl(this._shared, map);
        }

        private Map<Object, Object> _copy(Map<Object, Object> map) {
            return new HashMap(map);
        }
    }
}
