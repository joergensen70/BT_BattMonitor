package com.lidroid.xutils.db.converter;

import android.database.Cursor;
import com.lidroid.xutils.db.sqlite.ColumnDbType;

/* JADX INFO: loaded from: classes.dex */
public class ByteArrayColumnConverter implements ColumnConverter<byte[]> {
    @Override // com.lidroid.xutils.db.converter.ColumnConverter
    public Object fieldValue2ColumnValue(byte[] bArr) {
        return bArr;
    }

    @Override // com.lidroid.xutils.db.converter.ColumnConverter
    public byte[] getFieldValue(String str) {
        return null;
    }

    @Override // com.lidroid.xutils.db.converter.ColumnConverter
    public byte[] getFieldValue(Cursor cursor, int i) {
        if (cursor.isNull(i)) {
            return null;
        }
        return cursor.getBlob(i);
    }

    @Override // com.lidroid.xutils.db.converter.ColumnConverter
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.BLOB;
    }
}
