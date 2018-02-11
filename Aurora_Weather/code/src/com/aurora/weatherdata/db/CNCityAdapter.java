package com.aurora.weatherdata.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherdata.util.Log;
import com.aurora.weatherdata.util.SystemUtils;

import java.util.ArrayList;
import java.util.List;

public class CNCityAdapter extends CityAdapter {

    public static final String TAG = "CityDataAdapter";
    public static final String TABLE_NAME = "city_data";// 数据库表名

    public static final String ID = "_id"; // 表属性ID
    public static final String CITY_NAME = "cityName";
    public static final String UP_CITY = "upCity";
    public static final String PROVINCE_NAME = "province";
    public static final String PROVINCE_PINYIN = "provincePY";
    public static final String PROVINCE_PINYIN_SN = "provincePYSN";
    public static final String CITY_PINYIN = "pinYin";
    public static final String CITY_PINYIN_SN = "pinYinSn";

    private DBOpenHelper mDBOpenHelper;
    private SQLiteDatabase mDb;
    private Context mContext;

    public CNCityAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * 空间不够存储的时候设为只读
     *
     * @throws android.database.sqlite.SQLiteException
     */

    public void open() throws SQLiteException {
        mDBOpenHelper = new DBOpenHelper(mContext);
        try {
            mDb = mDBOpenHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            mDb = mDBOpenHelper.getReadableDatabase();
        }
    }

    /**
     * 调用SQLiteDatabase对象的close()方法关闭数据库
     */
    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    public void beginTransaction() {
        mDb.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mDb.setTransactionSuccessful();        //设置事务处理成功，不设置会自动回滚不提交
    }

    public void endTransaction() {
        mDb.endTransaction();        //处理完成
    }

    /**
     * 保存缓存数据(多条)
     *
     * @param
     */
    public void insert(List<CityItem> cityItems) {
        mDb.beginTransaction();
        int i = 0;
        try {
            for (CityItem cityItem : cityItems) {
                i++;
                insert(cityItem);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.toString() + "the error index is = " + i);
        } finally {
            mDb.endTransaction();
            Log.i("Test", "db is finished");
        }
    }

    /**
     * 保存缓存数据(单条)
     *
     * @param
     */
    public void insert(CityItem cityItem) {
        ContentValues value = new ContentValues();

        value.put(ID, cityItem.getId());
        value.put(CITY_NAME, cityItem.getCityName());
        value.put(UP_CITY, cityItem.getUpCity());
        value.put(PROVINCE_NAME, cityItem.getProvince());
        value.put(CITY_PINYIN, cityItem.getPinyin());
        value.put(CITY_PINYIN_SN, cityItem.getPinyinSn());

        mDb.insert(TABLE_NAME, null, value);
    }

    /**
     * 删除数据库相应记录
     *
     * @param
     */
    public void delete(int cityId) {
        if (mDb != null) {
            mDb.delete(TABLE_NAME,
                    CNCityAdapter.ID + "=?",
                    new String[]{cityId + ""});
        }
    }

    /**
     * 通过城市ID,返回一个CityInfo对象, 如果找不到则返回null
     *
     * @param
     * @return
     */
    public CityItem getCityFromId(int cityId) {
        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.ID + "=?",
                    new String[]{cityId + ""}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                CityItem cityInfo = new CityItem();
                cityInfo.setId(cursor.getInt(0));
                int a = cursor.getColumnCount();
                cityInfo.setCityName(cursor.getString(1));
                cityInfo.setUpCity(cursor.getString(2));
                cityInfo.setProvince(cursor.getString(3));
                cityInfo.setPinyin(cursor.getString(6));
                cityInfo.setPinyinSn(cursor.getString(7));
                cursor.close();
                cursor = null;
                return cityInfo;
            }
            cursor.close();
        }
        return null;
    }

    /**
     * 通过城市拼音,返回一个CityInfo列表, 如果找不到则返回null
     *
     * @param pinYin
     * @return
     */
    public List<CityItem> getCityFromPY(String pinYin) {
        List<CityItem> cityList = new ArrayList<CityItem>();
        String likePY = SystemUtils.getLikeStr(pinYin);

        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_PINYIN + " like ? ",
                    new String[]{likePY}, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;
            while (cursor.moveToNext()) {
                CityItem cityInfo = new CityItem();
                cityInfo.setId(cursor.getInt(0));
                cityInfo.setCityName(cursor.getString(1));
                cityInfo.setUpCity(cursor.getString(2));
                cityInfo.setProvince(cursor.getString(3));
                cityInfo.setPinyin(cursor.getString(6));
                cityInfo.setPinyinSn(cursor.getString(7));
                cityList.add(cityInfo);
            }
            cursor.close();
            cursor = null;
        }

        return cityList;
    }

    /**
     * 通过城市pinyin,返回一个Cursor对象, 如果找不到则返回null
     *
     * @param pinYin
     * @return
     */
    public Cursor getCityCursorFromPY(String pinYin) {
        String likePY = SystemUtils.getLikeStr(pinYin);

        Log.i(TAG, pinYin);
        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_PINYIN + " like ? ",
                    new String[]{likePY}, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;
            else
                return cursor;
        }

        return null;
    }

    /**
     * 通过城市名称,返回一个CityItem列表, 如果找不到则返回null
     *
     * @param pinYin
     * @return
     */
    public List<CityItem> getCityFromHZ(String pinYin) {
        List<CityItem> cityList = new ArrayList<CityItem>();
        String likePY = SystemUtils.getLikeStr(pinYin);

        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_PINYIN + " like ? ",
                    new String[]{likePY}, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;
            while (cursor.moveToNext()) {
                CityItem cityInfo = new CityItem();
                cityInfo.setId(cursor.getInt(0));
                cityInfo.setCityName(cursor.getString(1));
                cityInfo.setUpCity(cursor.getString(2));
                cityInfo.setProvince(cursor.getString(3));
                cityInfo.setPinyin(cursor.getString(6));
                cityInfo.setPinyinSn(cursor.getString(7));
                cityList.add(cityInfo);
            }
            cursor.close();
            cursor = null;
        }

        return cityList;
    }

    /**
     * 通过城市名称,返回一个Cursor对象, 如果找不到则返回null
     *
     * @param hz
     * @return
     */
    public Cursor getCityCursorFromHZ(String hz) {
        String likeHZ = SystemUtils.getLikeStr(hz);

        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_NAME + " like ? ",
                    new String[]{likeHZ}, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;
            else
                return cursor;
        }

        return null;
    }

    /**
     * 通过城市名称,返回一个List<string>对象, 如果找不到则返回null
     *
     * @param hz
     * @return
     */
    public List<CityItem> getCityListFromHZ(List<CityItem> cityList, String hz) {
        String likeHZ = SystemUtils.getLikeStr(hz);
        cityList.clear();

        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_NAME + " like ? ",
                    new String[]{likeHZ}, null, null, CNCityAdapter.CITY_PINYIN_SN + " asc ");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    CityItem cityItem = new CityItem();
                    cityItem.setId(cursor.getInt(0));
                    cityItem.setCityName(cursor.getString(3) + "-" + cursor.getString(1));
                    cityItem.setCityFlag(true);
                    cityList.add(cityItem);
                }
            }

            cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.PROVINCE_NAME + " like ? ",
                    new String[]{hz + "%"}, null, null, CNCityAdapter.CITY_PINYIN_SN + " asc ");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                	if(cursor.getString(3).equals(cursor.getString(1))) {
                		continue;
                	}
                    CityItem cityItem = new CityItem();
                    cityItem.setId(cursor.getInt(0));
                    cityItem.setCityName(cursor.getString(3) + "-" + cursor.getString(1));
                    cityItem.setCityFlag(false);
                    cityList.add(cityItem);
                }
            }

            cursor.close();
            cursor = null;
            return cityList;
        }

        return null;
    }

    /**
     * 通过城市名称,返回一个List<string>对象, 如果找不到则返回null
     *
     * @param
     * @return
     */
    public List<CityItem> getCityListFromPY(List<CityItem> cityList, String pinYin) {
        cityList.clear();

        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_PINYIN_SN + " like ? ",
                    new String[]{pinYin + "%"}, null, null, CNCityAdapter.CITY_PINYIN_SN + " asc ");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    CityItem cityItem = new CityItem();
                    cityItem.setId(cursor.getInt(0));
                    cityItem.setCityName(cursor.getString(3) + "-" + cursor.getString(1));
                    cityItem.setCityFlag(true);
                    cityList.add(cityItem);
                }
            }

            cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.CITY_PINYIN + " like ? ",
                    new String[]{pinYin + "%"}, null, null, CNCityAdapter.CITY_PINYIN_SN + " asc ");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                	CityItem cityItem = new CityItem();
                	cityItem.setId(cursor.getInt(0));
                	cityItem.setCityName(cursor.getString(3) + "-" + cursor.getString(1));
                    cityItem.setCityFlag(true);
                    cityList.add(cityItem);
                }
            }

            cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.PROVINCE_PINYIN_SN + " like ? ",
                    new String[]{pinYin + "%"}, null, null, CNCityAdapter.CITY_PINYIN_SN + " asc ");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                	if(cursor.getString(3).equals(cursor.getString(1))) {
                		continue;
                	}
                	CityItem cityItem = new CityItem();
                	cityItem.setId(cursor.getInt(0));
                	cityItem.setCityName(cursor.getString(3) + "-" + cursor.getString(1));
                    cityItem.setCityFlag(true);
                    cityList.add(cityItem);
                }
            }

            cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.PROVINCE_PINYIN + " like ? ",
                    new String[]{pinYin + "%"}, null, null, CNCityAdapter.CITY_PINYIN_SN + " asc ");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                	if(cursor.getString(3).equals(cursor.getString(1))) {
                		continue;
                	}
                	CityItem cityItem = new CityItem();
                	cityItem.setId(cursor.getInt(0));
                	cityItem.setCityName(cursor.getString(3) + "-" + cursor.getString(1));
                    cityItem.setCityFlag(true);
                    cityList.add(cityItem);
                }
            }

            cursor.close();
            cursor = null;
            return cityList;
        }

        return null;
    }

    /**
     * 通过城市ID,返回一个子城市Cursor对象, 如果找不到则返回null
     *
     * @param
     * @return
     */
    public Cursor getSubCityCursorFromHZ(int cityID) {
        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.ID + " like ? ",
                    new String[]{cityID + ""}, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;
            else
                return cursor;
        }

        return null;
    }

    /**
     * 通过城市ID,返回一个CityItem列表, 如果找不到则返回null
     *
     * @param
     * @return
     */
    public List<CityItem> getSubCityFromID(int cityID) {
        List<CityItem> cityList = null;

        if (mDb != null) {
            Cursor cursor = mDb.query(TABLE_NAME,
                    null,
                    CNCityAdapter.ID + "=?",
                    new String[]{cityID + ""}, null, null, null);

            if (cursor == null || cursor.getCount() == 0)
                return null;
            while (cursor.moveToNext()) {
                CityItem cityInfo = new CityItem();
                cityInfo.setId(cursor.getInt(0));
                cityInfo.setCityName(cursor.getString(1));
                cityInfo.setUpCity(cursor.getString(2));
                cityInfo.setProvince(cursor.getString(3));
                cityInfo.setPinyin(cursor.getString(6));
                cityInfo.setPinyinSn(cursor.getString(7));
                cityList.add(cityInfo);
            }
            cursor.close();
            cursor = null;
        }

        return cityList;
    }

    /**
     * 删除所有数据库记录
     */
    public void deleteAll() {
        if (mDb != null) {
            mDb.delete(TABLE_NAME, null, null);
        }
    }

}
