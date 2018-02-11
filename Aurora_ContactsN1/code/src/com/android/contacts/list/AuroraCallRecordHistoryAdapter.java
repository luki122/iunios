/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.list;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Audio.Media;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraTextView;
import aurora.widget.AuroraListView;
import aurora.app.AuroraAlertDialog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import com.aurora.android.contacts.AuroraStorageManager;

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.activities.AuroraCallRecordHistoryActivity;
import com.privacymanage.service.AuroraPrivacyUtils;


public class AuroraCallRecordHistoryAdapter extends BaseAdapter {
	
	private Context mContext;
	
	private HashMap<String, AuroraCallRecord> mCheckedItem = new HashMap<String, AuroraCallRecord>();
	private boolean mCheckBoxEnable = false;
	private boolean mNeedAnim = false;
	
	private ArrayList<AuroraCallRecord> mRecords = new ArrayList<AuroraCallRecord>();
	
	public AuroraCallRecordHistoryAdapter(Context context, ArrayList<AuroraCallRecord> records) {
		mContext = context;
		mRecords = records;
	}
	
	public void setRecords(ArrayList<AuroraCallRecord> records) {
		mRecords = records;
	}
	
	public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }
    
    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }
    
    public void setNeedAnim(boolean flag) {
        mNeedAnim = flag;
    }
    
    public boolean getNeedAnim() {
        return mNeedAnim;
    }
    
     
    private boolean mAuroraListDelet = false;
    
    public void setAuroraListDelet(boolean flag) {
        mAuroraListDelet = flag;
    }
    
     
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

            setNeedAnim(false);
            super.handleMessage(msg);
        }

    };

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mRecords.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mRecords.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		View v = (View) LayoutInflater.from(mContext).inflate(
                com.aurora.R.layout.aurora_slid_listview, null);
        RelativeLayout mainUi = (RelativeLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_front);
        View item = LayoutInflater.from(mContext).inflate(
				R.layout.aurora_call_record_history_item, parent, false);
        RelativeLayout layout = (RelativeLayout)item.findViewById(R.id.item);
		AuroraTextView nameTv = (AuroraTextView)item.findViewById(R.id.name);
		AuroraTextView dateTv = (AuroraTextView)item.findViewById(R.id.date);
		ImageView detail = (ImageView)item.findViewById(R.id.detail);
        mainUi.addView(item, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        LinearLayout deleteUi = (LinearLayout) v
                .findViewById(com.aurora.R.id.aurora_listview_back);
        ViewGroup.LayoutParams param = deleteUi.getLayoutParams();
        param.width = mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_list_item_delete_back_width);
        deleteUi.setLayoutParams(param);
        
        final AuroraCheckBox checkBox = (AuroraCheckBox) v.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        LinearLayout contentUi = (LinearLayout) v.findViewById(com.aurora.R.id.content);
        AuroraListView.auroraGetAuroraStateListDrawableFromIndex(contentUi, position);
        
        if (getCheckBoxEnable()) {
        	boolean checked = false;
        	if (mCheckedItem != null
                    && mCheckedItem.containsKey(String.valueOf(position))) {
                checked = true;
            }
        	
        	if (getNeedAnim()) {
                AuroraListView.auroraStartCheckBoxAppearingAnim(layout, checkBox);
            } else {
                AuroraListView.auroraSetCheckBoxVisible(layout, checkBox, true);
            }
            checkBox.setChecked(checked);
        } else {
        	if (checkBox != null) {
                if (getNeedAnim()) {
                    AuroraListView.auroraStartCheckBoxDisappearingAnim(layout, checkBox);
                } else {
                    AuroraListView.auroraSetCheckBoxVisible(layout, checkBox, false);
                }
            }
        }
		
		final AuroraCallRecord record = mRecords.get(position);
		
		nameTv.setText(record.getName());
		String date = formatDate(record.getEndTime());
		dateTv.setText(date);
		
		detail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showFileDetail(record);
			}
		});
		
		if (getNeedAnim()) {
            mHandler.sendMessage(mHandler.obtainMessage());
        }
		
		return v;
	}
	
	private void showFileDetail(AuroraCallRecord record) {
		if (record == null) {
			return;
		}
		
		AuroraAlertDialog dialog = null;
		
		if (null == dialog) {
	    	String path = record.getPath();
	    	if (path == null) {
				return;
			}
	    	
			File file = new File(path);
			long size = 0, pointSize = 0;
			String sizeStr = null;
			
			boolean mIsPrivacyRecord = false;
			if (ContactsApplication.sIsAuroraPrivacySupport && AuroraPrivacyUtils.mCurrentAccountId > 0
					&& AuroraPrivacyUtils.mCurrentAccountHomePath != null) {
				mIsPrivacyRecord = path.contains(AuroraPrivacyUtils.mCurrentAccountHomePath) ? true : false;
			}
			
			if (file.exists()) {
				size = file.length() / 1024;
				pointSize = file.length() % 1024;
				sizeStr = String.valueOf(size) + "." + String.valueOf(pointSize).substring(0, 1);
			} else {
				return;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(record.getEndTime() + record.getDruation());
			
			String message = mContext.getString(R.string.call_record_message_time);
			message += date;
			message += "\n";
			message += mContext.getString(R.string.call_record_message_size);
			message += sizeStr;
			message += "KB";
			if (!mIsPrivacyRecord) {
				message += "\n";
				message += mContext.getString(R.string.call_record_message_path);
				message += record.getFileName();
			}
			
			dialog = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.call_record_detail_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                	dialog.dismiss();
                                	dialog = null;
                                }
                            }).create();
        }

		dialog.show();
	}
	
	public String getName(int position) {
		return mRecords.get(position).getName();
	}
	
	public String getPath(int position) {
		return mRecords.get(position).getPath();
	}
	
	public void setCheckedItem(String position, AuroraCallRecord acr) {
		if (mCheckedItem == null) {
            mCheckedItem = new HashMap<String, AuroraCallRecord>();
        }
		
		if (!mCheckedItem.containsKey(position)) {
			mCheckedItem.put(position, acr);
		}
	}
	
	public HashMap<String, AuroraCallRecord> getCheckedItem() {
		return mCheckedItem;
	}
	
    public void removeCheckedItem(String position) {
    	if (mCheckedItem.containsKey(position)) {
    		mCheckedItem.remove(position);
    	}
	}
    
    public void clearCheckedItem() {
    	mCheckedItem.clear();
	}
    
    private String formatDate (long date) {
    	if (date == 0) {
    		return "";
    	}
    	
		CharSequence dateText = DateUtils.getRelativeTimeSpanString(
				date, System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
		String result = dateText.toString().replaceAll(" ", "");
		result  = replaceDateString(result);
		return result;
	}
	
	String replaceDateString(String src) {
		if (src == null) {
			return null;
		}
		
		String from[] = {"十一", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
		String to[] = {"11", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"} ;
		for (int i=0; i < from.length; i++) { 
			src = src.replaceAll(from[i], to[i]); 
		}
		
		return src; 
	}
    
//    private static class ViewHolder {
//		AuroraTextView nameTv;
//		AuroraTextView dateTv;
//		ImageView detail;
//        int position;
//	}
    
    public static class AuroraCallRecord {
		private String mPath;
		private long mDruation;
		private long mEndTime;
		private String mType;
		private String mName;     // contact name
		private String mFileName; // file name
		
		public void setPath(String path) {
			mPath = path;
		}
		public String getPath() {
			return mPath;
		}
		
		public void setDruation(long druation) {
			mDruation = druation;
		}
		public long getDruation() {
			return mDruation;
		}
		
		public void setEndTime(long time) {
			mEndTime = time;
		}
		public long getEndTime() {
			return mEndTime;
		}
		
		public void setName(String name) {
			mName = name;
		}
		public String getName() {
			return mName;
		}
		
		public void setFileName(String name) {
			mFileName = name;
		}
		public String getFileName() {
			return mFileName;
		}
		
		public void setMimeType(String type) {
			mType = type;
		}
		public String getMimeType() {
			return mType;
		}
		
		@Override
		public String toString() {
			return "Path = " + mPath + "\n Druation = " + mDruation + "   mEndTime = " + mEndTime + " mName = " + mName;
		}
	}
}
