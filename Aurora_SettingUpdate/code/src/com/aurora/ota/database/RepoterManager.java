
package com.aurora.ota.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.aurora.ota.database.DataBaseCreator.RepoterColumns;
import com.aurora.ota.database.DataBaseCreator.Tables;
import com.aurora.ota.location.LocationInfo;
import com.aurora.ota.reporter.APKinfoUtil;
import com.aurora.ota.reporter.Constants;
import com.aurora.ota.reporter.ReporterItem;
import com.aurora.ota.reporter.ReporterService;

import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class RepoterManager {
    private static RepoterManager instance = null;
    private DataBaseHandler handler;
    private Context mContext;
    private SharedPreferences pre;
    private SharedPreferences.Editor editor;

    public enum ReportType {
        Update, Normal, Delay;
    }

    private ReportType mReportType = ReportType.Normal;

    private RepoterManager(Context context) {
        mContext = context;
        handler = new DataBaseHandler(context);
        pre = context.getSharedPreferences(Constants.NAME_FOR_SRORE_REPORT, Constants.MODE_PRIVATE);
        editor = pre.edit();

    }

    public void setReportType(ReportType type) {
        mReportType = type;
    }

    public ReportType getReportType() {
        return mReportType;
    }

    public String getOSVersion() {
        return pre.getString(Constants.KEY_VERSION, "");
    }

    public void storeOSVersion() {
        editor.putString(Constants.KEY_VERSION, getVersion()).commit();
    }

    public void storeReportDate(int date) {
        editor.putInt(Constants.KEY_REPORT_DATE, date).commit();
    }

    public int getReportDate() {
        return pre.getInt(Constants.KEY_REPORT_DATE, 0);
    }
    
    public void storeUsageTime(long duration) {
        editor.putLong(Constants.KEY_DURATION, duration).commit();
    }

    public long getUsageTime() {
        return pre.getLong(Constants.KEY_DURATION, 0L);
    }

    public void storeTodayReported(boolean value) {
        editor.putBoolean(Constants.KEY_TODAY_REPORTED, value).commit();
    }

    public boolean getTodayReported() {
        return pre.getBoolean(Constants.KEY_TODAY_REPORTED, false);
    }
    
    public LocationInfo getHistoryLocation(){
        LocationInfo info = new LocationInfo();
        String ip = getHistoryIp();
        String province  = pre.getString(Constants.KEY_PROVINCE, "");
        String city = pre.getString(Constants.KEY_CITY, "");
        String country = pre.getString(Constants.KEY_COUNTRY, "");
        info.setCountry(country);
        info.setProvince(province);
        info.setCity(city);
        info.setTime(pre.getString(Constants.KEY_LOCATION_TIME, ""));
        info.setIp(ip);
        return info;
    }
    
    public void saveLocation( LocationInfo info){
        if(info == null){
            return;
        }
        editor.putString(Constants.KEY_COUNTRY, info.getCoutry()).commit();
        editor.putString(Constants.KEY_PROVINCE, info.getProvince()).commit();
        editor.putString(Constants.KEY_CITY, info.getCity()).commit();
        editor.putString(Constants.KEY_LOCATION_TIME, System.currentTimeMillis()+"");
        Constants.NEED_LOCATION = false;
    }
    
    public void saveIp(String ip){
        if(TextUtils.isEmpty(ip)){
            return;
        }
        editor.putString(Constants.KEY_IP, ip).commit();
        
    }
    
    public String getHistoryIp(){
        return pre.getString(Constants.KEY_IP, "");
    }
    
    public int[] getPhoneSize(){
        int height = pre.getInt(Constants.KEY_PHONE_HEIGHT, 1920);
        int width = pre.getInt(Constants.KEY_PHONE_WIDTH, 1080);
        int[] size = new int[2];
        size[0] = height;
        size[1] = width;
        return size;
    }
    public void savePhoneSize(int width,int height){
        editor.putInt(Constants.KEY_PHONE_HEIGHT, height).commit();
        editor.putInt(Constants.KEY_PHONE_WIDTH, width).commit();
    }
    

    private String getVersion() {
        return Util.getGioneeVersion();
    }

    public static synchronized RepoterManager getInstance(Context context) {
        if (instance == null) {
            instance = new RepoterManager(context);
        }
        return instance;
    }

    public void storeTime(String key) {
        editor.putLong(key, System.currentTimeMillis());
        editor.commit();
    }

    public long getTime(String key) {
        return pre.getLong(key, 0L);
    }

    public void storeBootNumber(String key) {
        int number = pre.getInt(key, 0);
        number += 1;
        editor.putInt(key, number);
        editor.commit();
    }

    public void storeBootNumber(String key, int number) {
        editor.putInt(key, number);
        editor.commit();
    }

    public int getBootNumber(String key) {
        return pre.getInt(key, 0);
    }

    private static ReporterItem createItem(Context context) {
        ReporterItem item = new ReporterItem();
        long currentTime = System.currentTimeMillis();
        String imei = "";
        try {
            imei = APKinfoUtil.getUniqueID(context);
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        item = new ReporterItem();
        item.setApkVersion(Util.getInternalVersion());
        item.setmChanel(APKinfoUtil.getChanel(context));
        item.setAppName(APKinfoUtil.getMobilModel() + "");
        item.setMobileModel(APKinfoUtil.getMobilModel() + "");
        item.setImei(imei);
        item.setMobileNumber(" ");
        item.setStartupTime(currentTime + "");
        item.setReported(0);
        item.setLocation("");
        return item;
    }

    /**
     * if item already inserted into database ,do nothing,else insert and send
     * broadcast to report it;
     * 
     * @param resolver
     * @param item
     */
    public synchronized void insert(ContentResolver resolver, ReporterItem item) {
        if (resolver == null) {
            throw new NullPointerException("ContentResolver is null");
        }
        if (item == null) {
            throw new NullPointerException("ReporterItem is null");
        }
        Uri uri = null;
        uri = insertInternal(resolver, item);
        if (uri != null) {
            Bundle b = new Bundle();
            b.putParcelable(Repoters.Columns.KEY_ITEM, uri);
            Intent intent = new Intent(ReporterService.ACTION_REPORT_ITEM);
            intent.putExtra(Repoters.Columns.KEY_ITEM, b);
           LogUtils.log("RepoterManager", "sendBroadcast insert uri:"+uri.toString()+"\n"+" item:"+item.toString());
            mContext.sendBroadcast(intent);
        }

    }

    private Uri insertInternal(ContentResolver resolver, ReporterItem item) {
        Uri uri = resolver.insert(Repoters.Columns.CONTENT_URI, getValue(item));
        return uri;
    }

    private ContentValues getValue(ReporterItem item) {
        ContentValues values = new ContentValues();
        values.put(Repoters.Columns.KEY_APP_VERSION, item.getApkVersion());
        values.put(Repoters.Columns.KEY_APP_NAME, item.getAppName());
        values.put(Repoters.Columns.KEY_IMEI, item.getImei());
        values.put(Repoters.Columns.KEY_CHANEL, item.getmChanel());
        values.put(Repoters.Columns.KEY_MOBILE_MODEL, item.getMobileModel());
        values.put(Repoters.Columns.KEY_MOBILE_NUMBER, item.getMobileNumber());
        values.put(Repoters.Columns.KEY_REGISTER_USER_ID, item.getRegisterUserId());
        values.put(Repoters.Columns.KEY_REPORTED, item.getReported());
        values.put(Repoters.Columns.KEY_SHUT_DOWN_TIME, item.getShutdownTime());
        values.put(Repoters.Columns.KEY_CREATE_ITEM_TIME, item.getCreatItemTime());
        values.put(Repoters.Columns.KEY_STATUS, item.getStatus());
        values.put(Repoters.Columns.KEY_PHONE_SIZE, item.getPhoneWidth()+Constants.SPLITE+item.getPhoneHeight());
        values.put(Repoters.Columns.KEY_LOCATION, item.getLocation());
       
        values.put(Repoters.Columns.KEY_APP_NUM, item.getAppNum());
        values.put(Repoters.Columns.KEY_BOOT_TIME,  item.getStartupTime());
        LogUtils.log("1122", "getValue  duration : " + item.getDuration() + "  Key : " + Repoters.Columns.KEY_DURATION_TIME);
        values.put(Repoters.Columns.KEY_DURATION_TIME, item.getDuration());
        
        return values;
    }

    public Cursor query(ContentResolver resolver, Uri uri) {
        return resolver.query(uri, null, null, null, null);
    }

    synchronized void update(ContentResolver resolver, ReporterItem item) {

    }

    /**
     * @hide
     * @param id
     * @return
     */
    public synchronized int delete(int id) {

        return handler.delete(id);
    }

    /**
     * @hide deleted items
     * @param fromId
     * @param toID
     * @return
     */
    public int delete(int fromId, int toID) {

        return handler.delete(fromId, toID);
    }

    /**
     * @hide delete items
     * @param ids
     * @return
     */
    public int delete(Integer... ids) {

        return handler.delete(ids);
    }

    /**
     * @hide get all item from database not limited count
     * @return
     */
    public List<ReporterItem> queryList() {

        return handler.queryList();
    }

    /**
     * @hide get Reporter items from database with limit count
     * @param count limited count
     * @return
     */
    public List<ReporterItem> queryList(int count) {

        return handler.queryList();
    }

    /**
     * @hide
     * @param id
     * @return
     */
    public ReporterItem queryItem(int id) {

        return handler.queryItem(id);
    }

    /**
     * @hide close the database
     */
    public void closeDatabase() {
        handler.closeDatabase();
    }

}
