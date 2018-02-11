package com.gionee.mms.online.transmisson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

import com.gionee.mms.online.OnlineUtils;
import com.gionee.mms.online.LogUtils;
import com.gionee.mms.online.activity.RecommendInformation;
import com.gionee.mms.online.data.InformationColumns;
import com.gionee.mms.online.data.MyContentProvider;

public class NetTransmitManager {

    private static final String TAG_NET1 = "NetTransmit";
    public static final String URL_DOWNLOAD_COLUMN_DATA = "http://olsms.gionee.com/olsms/categories.do";                /**获取 在线短信 分类名**/
    public static final String URL_DOWNLOAD_INFORMATION_DATA = "http://olsms.gionee.com/olsms/listmessages.do";         /**获取 在线短信 分类下具体内容**/
    public static final String URL_DOWNLOAD_COLUMN_DATA_TEST = "http://test1.gionee.com/olsms/categories.do";           /**获取 在线短信 分类名   用于测试**/
    public static final String URL_DOWNLOAD_INFORMATION_DATA_TEST = "http://test1.gionee.com/olsms/listmessages.do";    /**获取 在线短信 分类下具体内容   用于测试**/
    public static final String URL_DOWNLOAD_STATEMENT = "http://test1.gionee.com/olsms/disclaimer.jsp";

    public static final int MAXCOUNT = 20; // the maximum quantity can be
                                           // download every time
    public static final int MaxTableCount = 60000; // this variable means the
                                                   // max item in table
                                                   // information
                                                   // which can be considered
                                                   // to 20M memory capacity
    public static final int DELETECOUNT = 100; // the maximum quantity when need
                                               // free space every time
    public static final int REQUESTTIMEOUT = 60 * 1000;
    public static final int SOTIMEOUT = 10 * 1000;

    // gionee zhouyj 2012-12-10 add for CR00741182 start
    private static final String LOADING_FAILD = "loading_faild";
    // gionee zhouyj 2012-12-10 add for CR00741182 end

    private Context mContext;
    private Handler mHandler;
    private DownloadListener mListener;

    public NetTransmitManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    public void setDownloadListener(DownloadListener listener) {
        mListener = listener;
    }

    public void applyInformationService(final int id, final int max,
            final int min, final boolean isEmpty) {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName() + "   id=" + id +
                "max=" + max + ",min=" + min + ",isEmpty=" + isEmpty);
        final Runnable ApplyData = new Runnable() {

            public void run() {
                String res = "";
                int msg = 0;
                try {
                    if (!isEmpty) {
                        res = updateData(id, max, 0);
                        msg = RecommendInformation.MMS_ONLINE_NO_MORE_INFORMATION;
                        if (res == null || res.length() <= 0) {
                            res = updateData(id, min, 1);
                        }
                    } else {
                        mHandler.sendEmptyMessage(RecommendInformation.MMS_ONLINE_ONLOADING);
                        res = updateData(id, max, 0);
                        msg = RecommendInformation.MMS_ONLINE_DISPLAY_LOADING_FAILD;
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    sendMessage(
                            RecommendInformation.MMS_ONLINE_REQUEST_TIME_OUT,
                            isEmpty);
                    return;
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                    sendMessage(
                            RecommendInformation.MMS_ONLINE_REQUEST_TIME_OUT,
                            isEmpty);
                    return;
                } catch (ConnectTimeoutException e) {
                    e.printStackTrace();
                    sendMessage(
                            RecommendInformation.MMS_ONLINE_REQUEST_TIME_OUT,
                            isEmpty);
                    return;
                } catch (IOException e) {
                    sendMessage(
                            RecommendInformation.MMS_ONLINE_DISPLAY_LOADING_FAILD,
                            isEmpty);
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(
                            RecommendInformation.MMS_ONLINE_DISPLAY_LOADING_FAILD,
                            isEmpty);
                    return;
                }
                if (!(res == null || res.length() <= 0)) {
                    try {
                        // gionee zhouyj 2012-12-10 add for CR00741182 start
                        if (LOADING_FAILD.equals(res)) {
                            mHandler.sendEmptyMessage(RecommendInformation.MMS_ONLINE_DISPLAY_LOADING_FAILD);
                            return;
                        }
                        // gionee zhouyj 2012-12-10 add for CR00741182 end
                        JSONArray mJsonArray = new JSONArray(res);
                        if (isCanInsertInformation()) {
                            insertInformationdata(mJsonArray, id);
                            LogUtils.log(TAG_NET1, "no need delete");
                        } else {
                            LogUtils.log(TAG_NET1, "need delete");
                            freeMemorySpace();
                            insertInformationdata(mJsonArray, id);
                        }
                    } catch (JSONException e) {
                        e.getCause();
                        e.printStackTrace();
                        return;
                    }
                } else if (res == null || res.length() <= 0) {
                    mHandler.sendEmptyMessage(msg);
                }
            }
        };
        Thread DownloadData = new Thread(ApplyData);
        DownloadData.start();
    }

    private void sendMessage(int msgId, Boolean isEmpty) {
        if (isEmpty) {
            mHandler.sendEmptyMessage(RecommendInformation.MMS_ONLINE_NO_NET);
        } else {
            mHandler.sendEmptyMessage(msgId);
        }
    }

    public boolean isHasTestFile() {
        File file = Environment.getExternalStorageDirectory();
        File testfile = new File(file + "/", "mmsinline1234567890");
        return testfile.exists();
    }

    public String updateData(final int id, final int refid, final int dir)
            throws Exception {
        String res = "";
        StringBuffer urlChange = null;
        if (isHasTestFile()) {
            urlChange = new StringBuffer(URL_DOWNLOAD_INFORMATION_DATA_TEST);
        } else {
            urlChange = new StringBuffer(URL_DOWNLOAD_INFORMATION_DATA);
        }

        urlChange.append("?cat=" + id);
        urlChange.append("&refid=" + refid);
        urlChange.append("&dir=" + dir);
        urlChange.append("&cnt=" + MAXCOUNT);
        urlChange.append("&" + addtExtraInfo());
        // String url = URL_DOWNLOAD_INFORMATION_DATA +"?cat="+id;
        LogUtils.log(TAG_NET1, LogUtils.getThreadName() + "urlChange ="
                + urlChange);
        HttpGet httpRequest = new HttpGet(urlChange.toString());
        httpRequest.addHeader("Accept-Encoding", "gzip");
        HttpClient httpClient = getHttpClient();

        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            LogUtils.log(TAG_NET1,
                    "httpResponse.getStatusLine().getStatusCode()"
                            + httpResponse.getStatusLine().getStatusCode());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {

                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream is = httpEntity.getContent();
                // int count = is.available();

                int length = (int) httpEntity.getContentLength();

                LogUtils.log(TAG_NET1, LogUtils.getThreadName() + " length = "
                        + length);

                if (length > 0
                        && httpEntity.getContentType() != null
                        && httpEntity.getContentEncoding() != null
                        && httpEntity.getContentEncoding().getValue()
                                .contains("gzip")) {
                    GZIPInputStream gzipInput = new GZIPInputStream(
                            new BufferedInputStream(is));
                    // int length = 1024;
                    StringBuffer sb = new StringBuffer(length);
                    InputStreamReader inputStreamReader = new InputStreamReader(
                            gzipInput, "UTF_8");
                    char buffer[] = new char[length];
                    int count;
                    while ((count = inputStreamReader.read(buffer, 0, length)) > 0) {
                        sb.append(buffer, 0, count);
                    }
                    res = sb.toString();
                    LogUtils.log(TAG_NET1, LogUtils.getThreadName() + "res = "
                            + res);
                    inputStreamReader.close();
                    gzipInput.close();
                    is.close();
                    return res;
                    // gionee zhouyj 2012-12-10 add for CR00741182 start
                } else if (length > 0
                        && httpEntity.getContentEncoding() == null) {
                    return LOADING_FAILD;
                    // gionee zhouyj 2012-12-10 add for CR00741182 end
                } else {
                    LogUtils.log(TAG_NET1, LogUtils.getThreadName() + "res = "
                            + res);
                    return res;
                }

            }
        } catch (Exception e) {
            LogUtils.loge(TAG_NET1, LogUtils.getThreadName() + e.getMessage(),
                    e);
            e.printStackTrace();
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return res;
    }

    public void applyColumnService() {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName());
        final Runnable ApplyData = new Runnable() {
            public void run() {
                LogUtils.log(TAG_NET1, LogUtils.getThreadName());
                String res = updateColumnData();
                if (res != null && res.length() > 0) {
                    Date currentData = new Date();
                    SimpleDateFormat formattime = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat formatday = new SimpleDateFormat(
                            "yyyy-MM-dd");
                    String datatime = formattime.format(currentData);
                    String dataday = formatday.format(currentData);
                    SharedPreferences Data = mContext.getSharedPreferences(
                            "data", Context.MODE_WORLD_WRITEABLE);
                    Editor editor = Data.edit();
                    editor.putString("datatime", datatime);
                    editor.putString("dataday", dataday);
                    editor.commit();
                }
                if (mListener != null) {
                    mListener.onLoadStateChanged(false);
                }
            }
        };
        new Thread(ApplyData).start();
        if (mListener != null) {
            mListener.onLoadStateChanged(true);
        }
    }

    public String updateColumnData() {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName());
        String res = "";
        String url = "";
        if (isHasTestFile()) {
            url = URL_DOWNLOAD_COLUMN_DATA_TEST;
        } else {
            url = URL_DOWNLOAD_COLUMN_DATA;
        }
        url += "?" + addtExtraInfo();
        HttpGet httpRequest = new HttpGet(url);
        HttpClient httpClient = getHttpClient();
        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            LogUtils.log(TAG_NET1, "url ?" + url);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                LogUtils.log(TAG_NET1, "getStatusCode() == 200");
                HttpEntity httpEntity = httpResponse.getEntity();
                int length = (int) httpEntity.getContentLength();

                if (length > 0) {
                    StringBuffer stringBuffer = new StringBuffer(length);

                    InputStreamReader inputStreamReader = new InputStreamReader(httpEntity.getContent(),
                            "UTF_8");
                    char buffer[] = new char[length];
                    int count;
                    while ((count = inputStreamReader.read(buffer, 0, length)) > 0) {
                        stringBuffer.append(buffer, 0, count);

                        res = stringBuffer.toString();
                        LogUtils.log(TAG_NET1, "res" + res);
                        JSONArray mJsonArray = new JSONArray(res);
                        insertColumndata(mJsonArray);
                    }
                    inputStreamReader.close();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            LogUtils.log(TAG_NET1, "JSONException ");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return res;
    }

    private String addtExtraInfo() {
        return "model=" + OnlineUtils.getModelName() + "&rom="
                + OnlineUtils.getRomVersion() + "&imei="
                + OnlineUtils.getIMEI(mContext) + "&mms_version=" + OnlineUtils.getMmsVersion(mContext);
    }

    public void applyStatement() {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName());
        final Runnable LoadStatement = new Runnable() {

            @Override
            public void run() {
                String res = downloadStatement();
                LogUtils.log(TAG_NET1, LogUtils.getThreadName() + res);
                if (res != null) {
                    Long time = System.currentTimeMillis();

                    SharedPreferences Data = mContext.getSharedPreferences(
                            "data", Context.MODE_WORLD_WRITEABLE);
                    Editor editor = Data.edit();
                    editor.putString("statement", res);
                    editor.putLong("loadStatementTime", time);
                    editor.commit();
                }
                if (mListener != null) {
                    mListener.onLoadStateChanged(false);
                }
            }
        };
        Thread DownloadStatement = new Thread(LoadStatement);
        DownloadStatement.start();
        if (mListener != null) {
            mListener.onLoadStateChanged(true);
        }
    }

    public String downloadStatement() {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName());
        String res = "";
        HttpGet httpRequest = new HttpGet(URL_DOWNLOAD_STATEMENT);
        HttpClient httpClient = getHttpClient();
        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            LogUtils.log(TAG_NET1, "url ?" + URL_DOWNLOAD_STATEMENT);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                LogUtils.log(TAG_NET1, "getStatusCode() == 200");
                HttpEntity httpEntity = httpResponse.getEntity();
                res = EntityUtils.toString(httpEntity, "UTF_8");
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public void insertColumndata(JSONArray jsonArray) {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName());
        ContentValues values = new ContentValues();
        int length = jsonArray.length();
        LogUtils.log(TAG_NET1, "length" + length);
        final ContentResolver cr = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        // if (null != cr) {
        ops.add(ContentProviderOperation
                .newDelete(MyContentProvider.COLUMN_URI).build());

        // }
        if (length > 0) {
            try {
            for (int i = 0; i < length; i++) {
                LogUtils.log(TAG_NET1, "insertColumndata");

                values.put("cat_id", jsonArray.getJSONObject(i).getInt("id"));
                values.put("name", jsonArray.getJSONObject(i).getString("name"));
                values.put("count", jsonArray.getJSONObject(i).getInt("count"));

                ops.add(ContentProviderOperation
                        .newInsert(MyContentProvider.COLUMN_URI)
                        .withValues(values).build());
                values.clear();
            }
            } catch (JSONException jsone) {
                Log.e(TAG_NET1, "insertColumndata   jsone = " + jsone.toString());
            }

        }
        try {
            cr.applyBatch(MyContentProvider.AUTHORITY, ops);
            if (mListener != null) {
                mListener.onColumnStateChanged(false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public HttpClient getHttpClient() {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, REQUESTTIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SOTIMEOUT);
        HttpClient HttpClient = new DefaultHttpClient(httpParams);
        return HttpClient;
    }

    public synchronized void insertInformationdata(JSONArray jsonArray, int id)
            throws JSONException {
        LogUtils.log(TAG_NET1, LogUtils.getThreadName());
        ContentValues values = new ContentValues();
        int length = jsonArray.length();
        LogUtils.log(TAG_NET1, "information count =" + length);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        if (length > 0) {

            for (int i = 0; i < length; i++) {
                values.put("catrgory_id", id);
                values.put("msg_id", jsonArray.getJSONObject(i).getInt("id"));
                String message = jsonArray.getJSONObject(i).getString("msg");
                values.put("msg", message);
                ops.add(ContentProviderOperation
                        .newInsert(MyContentProvider.INFORMATION_URI)
                        .withValues(values).build());
                values.clear();
            }
        }

        try {
            mContext.getContentResolver().applyBatch(
                    MyContentProvider.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public boolean isCanInsertInformation() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(MyContentProvider.INFORMATION_URI, null, null,
                null, null);
        if (null != cur && cur.getCount() > 0) {
            cur.moveToFirst();
            int minid = cur.getInt(cur.getColumnIndex(BaseColumns._ID));
            cur.moveToLast();
            int maxid = cur.getInt(cur.getColumnIndex(BaseColumns._ID));
            LogUtils.log(TAG_NET1, "maxid   " + maxid);
            cur.close();
            if (maxid - minid >= MaxTableCount) {
                LogUtils.log(TAG_NET1, "maxid 1  " + maxid);
                return false;
            } else {
                LogUtils.log(TAG_NET1, "maxid  2 " + maxid);
                return true;
            }
        } else {
            LogUtils.log(TAG_NET1, "maxid 3 ");
            return true;
        }

    }

    public void freeMemorySpace() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(MyContentProvider.INFORMATION_URI, null, null,
                null, null);
        if (cur != null && cur.getCount() > 0) {
            cur.moveToFirst();
            int minid = cur.getInt(cur.getColumnIndex(BaseColumns._ID));
            cur.close();
            cr.delete(MyContentProvider.INFORMATION_URI, InformationColumns._ID
                    + " <?",
                    new String[] { String.valueOf(minid + DELETECOUNT) });
        }
    }

    public boolean isColumnTableEmpty() {

        final ContentResolver cr = mContext.getContentResolver();
        boolean isEmpty = true;
        if (cr != null) {
            Cursor cur = cr.query(MyContentProvider.COLUMN_URI, null, null,
                    null, null);
            try {
                if (cur != null && cur.getCount() > 0) {
                    isEmpty = false;
                } else {
                    isEmpty = true;
                }

                if (mListener != null) {
                    mListener.onColumnStateChanged(isEmpty);
                }
            } catch (Exception e) {
            } finally {
                if ((null != cur) && !(cur.isClosed())) {
                    cur.close();
                }
            }
        }
        return isEmpty;
    }
    
    public void postTapCategory (int catId) {
        HttpClient client = getHttpClient();
        HttpPost post = new HttpPost();
        String uri = "";
        if (isHasTestFile()) {
            uri = URL_DOWNLOAD_COLUMN_DATA_TEST;
        } else {
            uri = URL_DOWNLOAD_COLUMN_DATA;
        }
        uri += addtExtraInfo() + catId;
    }
    
    public static interface DownloadListener {

        public void onLoadStateChanged(boolean state);

        public void onColumnStateChanged(boolean state);
    }
}
