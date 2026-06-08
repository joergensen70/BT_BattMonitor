package com.fasterxml.jackson.databind;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class PropertyMetadata implements Serializable {
    private static final long serialVersionUID = -1;
    protected final String _description;
    protected final Integer _index;
    protected final Boolean _required;
    public static final PropertyMetadata STD_REQUIRED = new PropertyMetadata(Boolean.TRUE, null, null);
    public static final PropertyMetadata STD_OPTIONAL = new PropertyMetadata(Boolean.FALSE, null, null);
    public static final PropertyMetadata STD_REQUIRED_OR_OPTIONAL = new PropertyMetadata(null, null, null);

    @Deprecated
    protected PropertyMetadata(Boolean bool, String str) {
        this(bool, str, null);
    }

    protected PropertyMetadata(Boolean bool, String str, Integer num) {
        this._required = bool;
        this._description = str;
        this._index = num;
    }

    @Deprecated
    public static PropertyMetadata construct(boolean z, String str) {
        return construct(z, str, null);
    }

    public static PropertyMetadata construct(boolean z, String str, Integer num) {
        PropertyMetadata propertyMetadataWithDescription = z ? STD_REQUIRED : STD_OPTIONAL;
        if (str != null) {
            propertyMetadataWithDescription = propertyMetadataWithDescription.withDescription(str);
        }
        return num != null ? propertyMetadataWithDescription.withIndex(num) : propertyMetadataWithDescription;
    }

    protected Object readResolve() {
        if (this._description != null || this._index != null) {
            return this;
        }
        Boolean bool = this._required;
        if (bool == null) {
            return STD_REQUIRED_OR_OPTIONAL;
        }
        return bool.booleanValue() ? STD_REQUIRED : STD_OPTIONAL;
    }

    public PropertyMetadata withDescription(String str) {
        return new PropertyMetadata(this._required, str, this._index);
    }

    public PropertyMetadata withIndex(Integer num) {
        return new PropertyMetadata(this._required, this._description, num);
    }

    public PropertyMetadata withRequired(Boolean bool) {
        Boolean bool2;
        return (bool != null ? (bool2 = this._required) == null || bool2.booleanValue() != bool.booleanValue() : this._required != null) ? new PropertyMetadata(bool, this._description, this._index) : this;
    }

    public String getDescription() {
        return this._description;
    }

    public boolean isRequired() {
        Boolean bool = this._required;
        return bool != null && bool.booleanValue();
    }

    public Boolean getRequired() {
        return this._required;
    }

    public Integer getIndex() {
        return this._index;
    }

    public boolean hasIndex() {
        return this._index != null;
    }
}
