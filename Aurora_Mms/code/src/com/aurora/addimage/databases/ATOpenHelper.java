package com.aurora.addimage.databases;
// Aurora xuyong 2015-10-15 created for aurora's new feature
import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ATOpenHelper extends SQLiteOpenHelper {
    
    public static final String DB_NAME = "thumbnails.db";
    private static final int DB_VERSION = 1;
    
    private static final String AT_TABLE_NAME = "imagethumb";
    
    private static final String ID                  = "_id";
    //path
    private static final String PATH            = "path";
    //image byte array
    private static final String IMAGE_CONTENT         = "content";
    
    private String[] selectColumn = new String[] {
    		IMAGE_CONTENT
    };
    
    public ATOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        // TODO Auto-generated constructor stub
    }

    private void createATtable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + AT_TABLE_NAME + " ("
                          + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                          + PATH + " TEXT NOT NULL, "
                          + IMAGE_CONTENT + " BLOB NOT NULL"
                          + ");";
        db.execSQL(sql);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        createATtable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        String sql = "DROP TABLE IF EXISTS " + AT_TABLE_NAME;  
        db.execSQL(sql);  
        onCreate(db);  
    }
    
    private void closeDb(SQLiteDatabase db) {
        if (db != null) {
            db.close();
            db = null;
        }
    }
    
    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
    }
    
    private void close(SQLiteDatabase db, Cursor cusror) {
        closeDb(db);
        closeCursor(cusror);
    }
    
    public boolean existPath(String path) {
    	Cursor result = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        String selection = PATH + " = ? ";
        String[] selectionArgs = new String[] {
        		path
        };
        try {
            result = db.query(AT_TABLE_NAME, selectColumn, selection, selectionArgs, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (result != null && result.getCount() > 0) {
            	close(db, result);;
                return true;
            }
        }
        return false;
    }
    
    public Bitmap getBitmapByPath(String path) {
    	Cursor result = null;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        String selection = PATH + " = ? ";
        String[] selectionArgs = new String[] {
        		path
        };
        try {
            result = db.query(AT_TABLE_NAME, selectColumn, selection, selectionArgs, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (result != null && result.moveToFirst()) {
                byte[] content = result.getBlob(0);
                close(db, result);
                return BitmapFactory.decodeByteArray(content, 0, content.length);
            }
        }
        return null;
    }
    
    public void insert(String path, Bitmap bitmap) {
    	SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            db = this.getReadableDatabase();
        }
        ContentValues values = new ContentValues();
        values.put(PATH, path);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();   
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);  
        values.put(IMAGE_CONTENT, os.toByteArray());
        try {
            db.insert(AT_TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDb(db);
        }
    }
}
