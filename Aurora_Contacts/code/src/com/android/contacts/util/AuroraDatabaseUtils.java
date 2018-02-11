package com.android.contacts.util;
import android.content.ContentValues;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.util.Log;

public class AuroraDatabaseUtils {
    public static void cursorRowToContentValues(Cursor cursor, ContentValues values) {
        AbstractWindowedCursor awc =
                (cursor instanceof AbstractWindowedCursor) ? (AbstractWindowedCursor) cursor : null;

        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            if (awc != null && awc.isBlob(i)) {
                values.put(columns[i], cursor.getBlob(i));
            } else if (awc == null && cursor.getType(i) == Cursor.FIELD_TYPE_BLOB) {
                values.put(columns[i], cursor.getBlob(i));
            }else {
                values.put(columns[i], cursor.getString(i));
            }
        }
    }
}