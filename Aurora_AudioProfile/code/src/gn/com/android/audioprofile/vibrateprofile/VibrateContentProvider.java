package gn.com.android.audioprofile.vibrateprofile;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import gn.com.android.audioprofile.R;

public class VibrateContentProvider extends ContentProvider {

//    private static final UriMatcher sMatcher;
//    static{
//            sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//            sMatcher.addURI(VibrateProfileActivity.AUTOHORITY,RuiXin.TNAME, RuiXin.ITEM);
//            sMatcher.
//
//    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub
        String fileName = null;
        String menuName = null;
        if (uri.equals(VibrateProfileActivity.CONTENT_URI_1)) {
            fileName = "01.htxt";
            menuName = getContext().getString(R.string.vibrate_basecall);
        } else if (uri.equals(VibrateProfileActivity.CONTENT_URI_2)) {
            fileName = "02.htxt";
            menuName = getContext().getString(R.string.vibrate_heartbeat);
        } else if (uri.equals(VibrateProfileActivity.CONTENT_URI_3)) {
            fileName = "03.htxt";
            menuName = getContext().getString(R.string.vibrate_jinglebell);
        } else if (uri.equals(VibrateProfileActivity.CONTENT_URI_4)) {
            fileName = "04.htxt";
            menuName = getContext().getString(R.string.vibrate_ticktock);
        } else {
            fileName = "01.htxt";
            menuName = getContext().getString(R.string.vibrate_basecall);
        }

        String columnName[] = {"file_name", "menu_name"};
        MatrixCursor mCursor = new MatrixCursor(columnName);
        mCursor.addRow(new Object[] {fileName, menuName});
        return mCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
