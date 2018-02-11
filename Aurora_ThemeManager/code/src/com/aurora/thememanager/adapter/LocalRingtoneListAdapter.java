package com.aurora.thememanager.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.internet.RequestQueue;
import com.aurora.internet.cache.BitmapImageCache;
import com.aurora.internet.cache.DiskCache;
import com.aurora.internet.request.ImageRequest;
import com.aurora.internet.toolbox.ImageLoader;
import com.aurora.internetimage.NetworkImageView;
import com.aurora.thememanager.R;
import com.aurora.thememanager.activity.Action;
import com.aurora.thememanager.adapter.RingtoneListAdapter.OnItemApplyListener;
import com.aurora.thememanager.entities.Theme;
import com.aurora.thememanager.entities.ThemeAudio;
import com.aurora.thememanager.utils.SystemUtils;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.download.DownloadData;
import com.aurora.thememanager.utils.download.DownloadManager;
import com.aurora.thememanager.utils.download.DownloadService;
import com.aurora.thememanager.utils.download.DownloadStatusCallback;
import com.aurora.thememanager.utils.download.FileDownloader;
import com.aurora.thememanager.utils.download.RingtongDownloadService;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.thememanager.utils.themehelper.ThemeOperationCallBack;
import com.aurora.thememanager.utils.themeloader.ImageLoaderImpl;
import com.aurora.thememanager.view.ListViewLoadView;
import com.aurora.thememanager.view.ListViewDelegate.OnListScrollChange;
import com.aurora.thememanager.view.ListViewLoadView.OnLoadViewClick;
import com.aurora.thememanager.widget.DownloadButton;
import com.aurora.thememanager.widget.NetworkRoundedImageView;
import com.aurora.thememanager.widget.ProgressBtn;
import com.aurora.thememanager.widget.ProgressBtn.OnAnimListener;
import com.nostra13.universalimageloader.core.assist.FailReason;

public class LocalRingtoneListAdapter extends BaseAdapter {
	private Context mContext;
	private List<DownloadData> mAudioFilePhone;
	private List<DownloadData> mAudioFileMessage;
	private boolean showListCheckbox = false;
	private int mClickedPosition;
	
	private static final int RINGTONE_TYPE_COUNT = 2;
	
    private static final int LOCAL_RINGTONG_TYPE = 1;
    private static final int LOCAL_RINGTONG_LIST = 2;
    
    private ThemeManager mThemeManager;
	
	ArrayList<Integer> mCheckArrayList = new ArrayList<Integer>();
	
	private static final int MSG_UPDATE_APPLIED_RINGTONE_ITEM = 0;
	

	public LocalRingtoneListAdapter(Context context, List<DownloadData> audioFilePhone, List<DownloadData> audioFileMessage) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mAudioFilePhone = audioFilePhone;
		mAudioFileMessage = audioFileMessage;
		Log.e("101010", "----mAudioFilePhone = ----" + mAudioFilePhone.size());
		Log.e("101010", "----mAudioFileMessage = ----" + mAudioFileMessage.size());
		
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_RINGTONG);
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == MSG_UPDATE_APPLIED_RINGTONE_ITEM){
				notifyDataSetChanged();
				Toast.makeText(mContext, R.string.apply_success, Toast.LENGTH_SHORT).show();
			}
			
		};
	};
	
	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		if (getItemType(position) == LOCAL_RINGTONG_TYPE) {
			return false;
		} else {
			return true;
		}
	}
	
    private int getItemType(int position) {
        if (position == 0 || (mAudioFilePhone.size() > 0 && position == mAudioFilePhone.size() + 1)) {
            return LOCAL_RINGTONG_TYPE;
        } else {
        	return LOCAL_RINGTONG_LIST;
        }
    }

	@Override
	public View getView(int position, View coverview, ViewGroup parent) {
		// TODO Auto-generated method stub
		int itemType = getItemType(position);
		
		//Log.e("101010", "---getView coverview = ---" + coverview);
		
		Holder holder = null;
        if (coverview != null) {
    		holder = (Holder) coverview.getTag();
		} 
        if (itemType == LOCAL_RINGTONG_TYPE) {
        	if (coverview == null || (holder != null && holder.mViewType != LOCAL_RINGTONG_TYPE)) {
				coverview = LayoutInflater.from(mContext).inflate(R.layout.ringtone_list_item_text, null);
				holder = new Holder();
				holder.mRingToneTypeText = (TextView)coverview;
				holder.mViewType = LOCAL_RINGTONG_TYPE;
				coverview.setTag(holder);
			}
			if (position == 0 && mAudioFilePhone.size() > 0) {
				holder.mRingToneTypeText.setText(R.string.phone_ringtone);
			} else {
				holder.mRingToneTypeText.setText(R.string.message_ringtone);
			}
        } else if(itemType == LOCAL_RINGTONG_LIST) {

        	if (coverview == null || (holder != null && holder.mViewType != LOCAL_RINGTONG_LIST)) {
        		holder = new Holder();
        		coverview = LayoutInflater.from(mContext).inflate(com.aurora.R.layout.aurora_slid_listview, parent, false);
        		holder.mFront = (RelativeLayout)coverview.findViewById(com.aurora.R.id.aurora_listview_front);
        		holder.mCheckbox = (CheckBox)coverview.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        		LinearLayout linear = (LinearLayout)coverview.findViewById(com.aurora.R.id.content);	 
        		LayoutInflater.from(mContext).inflate(R.layout.ringtone_local_list_item, holder.mFront);
        		holder.mAudioName = (TextView)coverview.findViewById(R.id.ringtone_name);
        		holder.mDuration = (TextView)coverview.findViewById(R.id.ringtone_duration);
        		holder.mViewType = LOCAL_RINGTONG_LIST;
        		holder.applyBtn = (Button)coverview.findViewById(R.id.applyBtn);
        		holder.divider = coverview.findViewById(R.id.divider);
        		coverview.setTag(holder);
        	}
			final Theme theme;
			final int index;
			if (position <= mAudioFilePhone.size()) {
				index = position - 1;
				theme = (Theme)mAudioFilePhone.get(index);
				if(index == mAudioFilePhone.size() - 1) {
					holder.divider.setVisibility(View.GONE);
				} else {
					holder.divider.setVisibility(View.VISIBLE);
				}
			} else {
				index = position - getIndexForMessage();
				theme = (Theme)mAudioFileMessage.get(index - mAudioFilePhone.size());
				if(index == mAudioFileMessage.size() - 1 + mAudioFilePhone.size()) {
					holder.divider.setVisibility(View.GONE);
				} else {
					holder.divider.setVisibility(View.VISIBLE);
				}
			}
    		holder.applyBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mThemeManager.setCallBack(new ThemeOperationCallBack() {
						
						@Override
						public void onProgressUpdate(int progress) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onCompleted(boolean success, int statusCode) {
							Message msg = new Message();
							msg.what = MSG_UPDATE_APPLIED_RINGTONE_ITEM;
							mHandler.sendMessage(msg);
						}
						
						@Override
						public Context getContext() {
							// TODO Auto-generated method stub
							return mContext;
						}
					});
					Log.e("101010", "----LocalRingtoneListAdapter ringtonetype = ------" + ((ThemeAudio)theme).ringtongType);
					if(((ThemeAudio)theme).ringtongType > 0) {
						if(mOnLocalItemApplyListener != null) {
							mOnLocalItemApplyListener.setLocalItemApply(theme, mThemeManager);
						}
					} else {
						mThemeManager.apply(theme);
					}
				}
			});
        	holder.mAudioName.setText(theme.name);
        	holder.mDuration.setText(theme.author);
        	if(showListCheckbox) {
        		AuroraListView.auroraSetCheckBoxVisible(holder.mFront, holder.mCheckbox, true);
        		if(mCheckArrayList.contains(Integer.valueOf(index))) {
        			holder.mCheckbox.setChecked(true);
        		} else {
        			holder.mCheckbox.setChecked(false);
        		}
        		holder.applyBtn.setVisibility(View.GONE);
        	} else {
        		AuroraListView.auroraSetCheckBoxVisible(holder.mFront, holder.mCheckbox, false);
        		holder.applyBtn.setVisibility(View.VISIBLE);
        	}
        }
		return coverview;
	}
	
	public ArrayList<Integer> getCheckboxCheckList() {
		return mCheckArrayList;
	}
	
	public void setShowListCheckbox(boolean showListCheckbox) {
		this.showListCheckbox = showListCheckbox;
	}
	
	public boolean getShowListCheckbox() {
		return this.showListCheckbox;
	}
	
	@Override
	public DownloadData getItem(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int phoneCount = mAudioFilePhone.size() > 0 ? mAudioFilePhone.size() + 1 : 0;
		int messageCount = mAudioFileMessage.size() > 0 ? mAudioFileMessage.size() + 1 : 0;
		return phoneCount + messageCount;
	}
	
	public int getIndexForMessage() {
		if(mAudioFilePhone.size() > 0) {
			//如果电话铃声数量大于0,则需剔除两项文本
			return 2;
		} else {
			//如果电话铃声数量等于0,则只需剔除一项文本
			return 1;
		}
	}

	private final class Holder {
		TextView mAudioName;
		TextView mDuration;
		RelativeLayout mFront;
		CheckBox mCheckbox;
		
		Button applyBtn;
		
		View divider;
		
		TextView mRingToneTypeText;
		int mViewType;
	}

	@Override
	public long getItemId(int paramInt) {
		// TODO Auto-generated method stub
		return paramInt;
	}
	
	public void updateAdapter(List<DownloadData> audioFilePhone, List<DownloadData> audioFileMessage, boolean resetCheckList) {
		mAudioFilePhone = audioFilePhone;
		mAudioFileMessage = audioFileMessage;
		if(resetCheckList) {
			mCheckArrayList.clear();
		}
		notifyDataSetChanged();
	}
	
	public interface OnLocalItemApplyListener{
		public void setLocalItemApply(Theme theme, ThemeManager mThemeManager);
	}
	private OnLocalItemApplyListener mOnLocalItemApplyListener;
	public void setOnLocalItemApplyListener(OnLocalItemApplyListener onLocalItemApplyListener) {
		mOnLocalItemApplyListener = onLocalItemApplyListener;
	}
}
