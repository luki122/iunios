/*
 *
 * Copyright (C) 2011 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.importexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.MmsApp;
import android.opengl.Visibility;
import android.widget.ImageView;
import com.android.mms.data.Conversation;

public class MultiExportSmsAdapter extends CursorAdapter {
    private static final String TAG = "MultiExportSmsAdapter";
    private OnContentChangedListener mOnContentChangedListener = null;
    private ExportSmsAdapterCallBack mCallBack = null;
    private ArrayList<String> mData  = new ArrayList<String>();
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private ArrayList<String> mThreadIds = new ArrayList<String>();
    private Map<Integer, String> mAddresses = new HashMap<Integer, String>();
    private QueryHandler mQueryHandler = null;
    private static final int QUERY_COMPLETE_TOKEN = 1;
    private static int aurora_min_match = 7;//MIN_MATCH = SystemProperties.getInt("ro.gn.match.numberlength", 7);
    private ArrayList<String> mHasEncryption = new ArrayList<String>();

    public MultiExportSmsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mQueryHandler = new QueryHandler(mContext.getContentResolver());
    }

    public ArrayList<String> getmData() {
        return mData;
    }

    public void setmData(Cursor cursor) {
        if (null != cursor && cursor.getCount() > 0) {
            mData.clear();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                mData.add(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.BaseMmsColumns.THREAD_ID))));
            }
        }
    }

    public Map<Integer, String> getmAddresses() {
        return mAddresses;
    }

    public void setmAddresses(Map<Integer, String> mAddresses) {
        this.mAddresses = mAddresses;
    }

    public interface OnContentChangedListener {
        void onContentChanged(MultiExportSmsAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }

    public boolean isAllSelected() {
        return mData.size() == mThreadIds.size();
    }

    public int getSelectedNumber() {
        return mThreadIds.size();
    }

    public void setSelectedAll() {
        mThreadIds.clear();
        for (int i=0;i<mData.size();i++) {
            mThreadIds.add(mData.get(i));
        }
        callBack(mThreadIds);
    }

    public void cancelSelectedAll() {
        mThreadIds.clear();
        callBack(mThreadIds);
    }

    @Override
    protected void onContentChanged() {
        Log.d(TAG, "onContentChanged.................");
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }

    private void startQuery(int id, TextView view) {
        mQueryHandler.startQuery(QUERY_COMPLETE_TOKEN, view, ConfigConstantUtils.IMPORT_EXPORT_SMS_QUERY_URI, new String[]{Sms.ADDRESS},
                                    Sms.THREAD_ID + " = " + id + " and " + Sms.TYPE + " != " + Telephony.TextBasedSmsColumns.MESSAGE_TYPE_DRAFT
                                    + ") GROUP BY (" + Sms.ADDRESS, null, null);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // TODO Auto-generated method stub
        view.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Sms.THREAD_ID)));
        if (MmsApp.mEncryption) {
            Conversation conv = Conversation.get(
                    context.getApplicationContext(), view.getId(), false);
            ImageView imageView = (ImageView) view.findViewById(R.id.ecyImg);
            /*if (MmsApp.mDarkStyle) {
                imageView.setImageResource(R.drawable.gn_ic_encryptionimage_dark);
            }*/
            if (conv.getEncryption()) {
                mHasEncryption.add(String.valueOf(view.getId()));
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        if (mThreadIds.contains(String.valueOf(view.getId()))) {
            ((CheckBox) view.findViewById(R.id.selBtn)).setChecked(true);
        } else {
            ((CheckBox) view.findViewById(R.id.selBtn)).setChecked(false);
        }

        // gionee zhouyj 2012-05-28 modify for CR00607938 start
        startQuery(view.getId(), ((TextView) view.findViewById(R.id.contact)));
        // gionee zhouyj 2012-05-28 modify for CR00607938 end
        TextView textView = (TextView) view.findViewById(R.id.count);
        textView.setText(" (" + String.valueOf(cursor.getInt(3)) + ")");
        textView.setVisibility(View.VISIBLE);
        ((CheckBox) view.findViewById(R.id.selBtn)).setClickable(false);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (((CheckBox) v.findViewById(R.id.selBtn)).isChecked()) {
                    ((CheckBox) v.findViewById(R.id.selBtn)).setChecked(false);
                    mThreadIds.remove(String.valueOf(v.getId()));
                } else {
                    ((CheckBox) v.findViewById(R.id.selBtn)).setChecked(true);
                    if (!mThreadIds.contains(String.valueOf(v.getId()))) {
                        mThreadIds.add(String.valueOf(v.getId()));
                    }
                }
                callBack(mThreadIds);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup group) {
        // TODO Auto-generated method stub
        return mInflater.inflate(R.layout.gn_import_export_item, null);
    }

    public interface ExportSmsAdapterCallBack {
        public void onExportSmsAdapterCallBack(ArrayList<String> threadIds);
    }

    public void setExportSmsAdapterCallBack(ExportSmsAdapterCallBack callBack) {
        mCallBack = callBack;
    }

    private void callBack(ArrayList<String> threadIds) {
        if (null != mCallBack) {
            mCallBack.onExportSmsAdapterCallBack(threadIds);
        }
    }

    private final class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver arg0) {
            super(arg0);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case QUERY_COMPLETE_TOKEN:
                    try{
                        //Gionee <guoyx> <2013-05-27> modify for CR00819048 begin
                        //if (cursor.getCount() > 1) {
                        if (cursor.getCount() > 0) {
                        //Gionee <guoyx> <2013-05-27> modify for CR00819048 end
                            String address = "";
                            ArrayList<String> addressList = new ArrayList<String>();
                            cursor.moveToPosition(-1);
                            boolean exists = false;
                            while (cursor.moveToNext()) {
                                exists = false;
                                address = cursor.getString(cursor.getColumnIndexOrThrow(Sms.ADDRESS));
                                // add by wangth 20140403 begin
                                if (address != null && (address.startsWith("1") || address.startsWith("+861"))) {
                                    aurora_min_match = 11;
                                } else {
                                    aurora_min_match = 7;
                                }
                                // add by wangth 20140403 end
                                address = address.length() > aurora_min_match ? address.substring(address.length() - aurora_min_match) : address;
                                for (int j=0;j<addressList.size();j++) {
                                    String compareAddress = addressList.get(j).length() > aurora_min_match ? addressList.get(j).substring(addressList.get(j).length() - aurora_min_match) : addressList.get(j);
                                    if (address.equals(compareAddress)) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    addressList.add(cursor.getString(cursor.getColumnIndexOrThrow(Sms.ADDRESS)));
                                }
                            }
                            StringBuffer buffer = new StringBuffer();
                            for (int i=0;i<addressList.size();i++) {
                                buffer.append(Contact.get(addressList.get(i), false).getName() + ",");
                            }
                            ((TextView)cookie).setText(buffer.toString().substring(0, buffer.toString().length()-1));
                            addressList.clear();
                            addressList = null;
                        }
                        //Gionee <guoyx> <2013-05-27> delete for CR00819048 begin
//                        else {
//                            cursor.moveToFirst();
//                            String address = cursor.getString(cursor.getColumnIndexOrThrow(Sms.ADDRESS));
//                            ((TextView)cookie).setText(Contact.get(address, false).getName());
//                        }
                        //Gionee <guoyx> <2013-05-27> delete for CR00819048 end
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    } finally {
                        if (null != cursor) {
                            cursor.close();
                            cursor = null;
                        }
                    }

                    break;

                default:
                    break;
            }
        }
    }

    public boolean checkEncryption() {
        // TODO Auto-generated method stub
        for (int i=0;i<mHasEncryption.size();i++) {
            if (mThreadIds.contains(mHasEncryption.get(i))) {
                return true;
            }
        }
        return false;
    }
}

