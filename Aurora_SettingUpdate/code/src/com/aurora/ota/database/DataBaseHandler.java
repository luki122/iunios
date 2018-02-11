
package com.aurora.ota.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.ota.database.DataBaseCreator.RepoterColumns;
import com.aurora.ota.database.DataBaseCreator.Tables;
import com.aurora.ota.reporter.Constants;
import com.aurora.ota.reporter.ReporterItem;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler implements ReporterKey {
    private DatabaseModifier modifier;
    private Context mContext;
    private static final int REPORTED=1;

    public DataBaseHandler(Context context) {
        mContext = context;
        modifier = new Modifier(context);

    }

    private static final String[] Columns = {
            RepoterColumns.ID,
            RepoterColumns.KEY_APP_VERSION,
            RepoterColumns.KEY_APP_NAME,
            RepoterColumns.KEY_IMEI,
            RepoterColumns.KEY_CHANEL,
            RepoterColumns.KEY_MOBILE_MODEL,
            RepoterColumns.KEY_MOBILE_NUMBER,
            RepoterColumns.KEY_REGISTER_USER_ID,
            RepoterColumns.KEY_SHUT_DOWN_TIME,
            RepoterColumns.KEY_CREATE_ITEM_TIME,
            RepoterColumns.KEY_STATUS,
            RepoterColumns.KEY_REPORTED,
            RepoterColumns.KEY_PHONE_SIZE,
            RepoterColumns.KEY_LOCATION,
            RepoterColumns.KEY_APP_NUM,
            RepoterColumns.KEY_BOOT_TIME,
            RepoterColumns.KEY_DURATION_TIME,

    };

    /**
     * insert item into database
     * @param item
     * @return
     */
    public synchronized long insert(ReporterItem item) {
        ContentValues value = new ContentValues();
        if (item != null) {
            value.put(KEY_APP_VERSION, item.getApkVersion());
            value.put(KEY_APP_NAME, item.getAppName());
            value.put(KEY_IMEI, item.getImei());
            value.put(KEY_CHANEL, item.getmChanel());
            value.put(KEY_MOBILE_MODEL, item.getMobileModel());
            value.put(KEY_MOBILE_NUMBER, item.getMobileNumber());
            value.put(KEY_REGISTER_USER_ID, item.getRegisterUserId());
            value.put(KEY_SHUT_DOWN_TIME, item.getShutdownTime());
            value.put(KEY_CREATE_ITEM_TIME, item.getStartupTime());
            value.put(KEY_STATUS, item.getStatus());
            value.put(KEY_REPORTED, item.getReported());//RepoterColumns.KEY_PHONE_SIZE,
            value.put(KEY_PHONE_SIZE, item.getPhoneWidth()+Constants.SPLITE+item.getPhoneHeight());
            value.put(KEY_LOCATION, item.getLocation());
        }
        if (value.size() > 0) {
            return modifier.insert(Tables.DB_TABLE, null, value);
        }
        return 0;
    }

    /**
     * delete item
     * @param id
     * @return
     */
    public synchronized int delete(int id) {
        String selection = RepoterColumns.ID + " = " + id+" AND "+RepoterColumns.KEY_REPORTED+" = "+REPORTED;
        return modifier.delete(Tables.DB_TABLE, selection, null);
    }

    /**
     * deleted items
     * @param fromId
     * @param toID
     * @return
     */
    public int delete(int fromId, int toID) {
        for (int i = fromId; i <= toID; i++) {
            delete(i);
        }
        return 0;
    }

    /**
     * delete items 
     * @param ids
     * @return
     */
    public int delete(Integer... ids) {
        for (Integer id : ids) {
            delete(id);
        }
        return 0;
    }

    /**
     * get all item from database not limited count
     * @return
     */
    public synchronized List<ReporterItem> queryList() {
        Cursor c =
                modifier.query(Tables.DB_TABLE, Columns, null, null, null, null, RepoterColumns.ID);
        List<ReporterItem> items = new ArrayList<ReporterItem>();
        ReporterItem item = null;
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                item = getCursorItem(c);
                items.add(item);
            }
        }
        c.close();
        return items;
    }

    /**
     * get Reporter items from database with limit count
     * @param count limited count
     * @return
     */
    public synchronized List<ReporterItem> queryList(int count) {
        Cursor c =
                modifier.query(Tables.DB_TABLE, Columns, null, null, null, null, null);
        List<ReporterItem> items = new ArrayList<ReporterItem>();
        ReporterItem item = null;
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                item = getCursorItem(c);
                items.add(item);
            }
        }
        c.close();
        return items;
    }

    /**
     * get String from cursor
     * @param c
     * @param columnName
     * @return
     */
    private String getCursorString(Cursor c, String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }
    /**
     * get int from cursor
     * @param c
     * @param columnName
     * @return
     */
    private int getCursorInt(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }
    
    /**
     * query one item by id
     * @param id
     * @return
     */
    public  synchronized ReporterItem queryItem(int id) {
        String selection = RepoterColumns.ID + " = " + id;
        Cursor c =
                modifier.query(Tables.DB_TABLE, Columns, selection, null, null, null, null);
        ReporterItem item = null;
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                item = getCursorItem(c);
            }
        }
        c.close();
        return item;
    }

    /**
     * get the Item in cursor
     * @param c
     * @return
     */
    private ReporterItem getCursorItem(Cursor c) {
    	 String width = "1080";
        String height = "1920";
        ReporterItem item = new ReporterItem();
        item.setApkVersion(getCursorString(c, KEY_APP_VERSION));
        item.setAppName(getCursorString(c, KEY_APP_NAME));
        item.setImei(getCursorString(c, KEY_IMEI));
        item.setmChanel((getCursorString(c, KEY_CHANEL)));
        item.setMobileModel(getCursorString(c, KEY_MOBILE_MODEL));
        item.setMobileNumber(getCursorString(c, KEY_MOBILE_NUMBER));
        item.setRegisterUserId(getCursorString(c, KEY_REGISTER_USER_ID));
        item.setShutdownTime(getCursorString(c, KEY_SHUT_DOWN_TIME));
        item.setStartupTime(getCursorString(c, KEY_BOOT_TIME));
        item.setStatus(getCursorInt(c, KEY_STATUS));
        item.setReported(getCursorInt(c, KEY_REPORTED));
        item.setId(getCursorInt(c, RepoterColumns.ID));
        //KEY_PHONE_SIZE
     String phoneSize = getCursorString(c,RepoterColumns.KEY_PHONE_SIZE);
      if(!TextUtils.isEmpty(phoneSize)){
          String[] size = phoneSize.split(Constants.SPLITE);
          if(size.length == 2){
              height = size[0];
             width = size[1];
          }
      }
      item.setPhoneHeight(height);
      item.setPhoneWidth(width);

        item.setLocation(getCursorString(c, RepoterColumns.KEY_LOCATION));
        item.setAppNum(getCursorInt(c, RepoterColumns.KEY_APP_NUM));
        item.setCreatItemTime(getCursorString(c, RepoterColumns.KEY_CREATE_ITEM_TIME));
        item.setDuration(getCursorString(c, RepoterColumns.KEY_DURATION_TIME));

        return item;
    }

    /**
     * update item
     * @param item
     * @return
     */
    public synchronized int update(ReporterItem item) {
        ContentValues value = new ContentValues();
        if (item != null) {
            value.put(KEY_APP_VERSION, item.getApkVersion());
            value.put(KEY_APP_NAME, item.getAppName());
            value.put(KEY_IMEI, item.getImei());
            value.put(KEY_CHANEL, item.getmChanel());
            value.put(KEY_MOBILE_MODEL, item.getMobileModel());
            value.put(KEY_MOBILE_NUMBER, item.getMobileNumber());
            value.put(KEY_REGISTER_USER_ID, item.getRegisterUserId());
            value.put(KEY_SHUT_DOWN_TIME, item.getShutdownTime());
            value.put(KEY_CREATE_ITEM_TIME, item.getCreatItemTime());
            value.put(KEY_STATUS, item.getStatus());
            value.put(KEY_REPORTED, item.getReported());
            value.put(KEY_PHONE_SIZE, item.getPhoneWidth()+Constants.SPLITE+item.getPhoneHeight());
            value.put(KEY_LOCATION, item.getLocation());
            value.put(KEY_APP_NUM, item.getAppNum());
            value.put(KEY_BOOT_TIME, item.getStartupTime());
            value.put(KEY_DURATION_TIME, item.getDuration());
            
            
            int id = item.getId();
            String[] whereArgs = {String.valueOf(id)};
            String whereClause = RepoterColumns.ID + " = "+id;  
            Log.e("luofu", "id:"+id);
            return modifier.update(Tables.DB_TABLE, value, whereClause, null);
        }
        
        return 0;
    }

    /**
     * close the database
     */
    public void closeDatabase() {
        modifier.closeDatabase();
    }

}
