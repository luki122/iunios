package com.aurora.change.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.DialogInterface.OnDismissListener;

import com.aurora.change.AuroraChangeApp;
import com.aurora.change.R;
import aurora.app.AuroraAlertDialog;
import com.aurora.change.activities.WallpaperLocalActivity;
import com.aurora.change.cache.ImageLoader;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.imagecache.ImageResizer;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.receiver.ChangeReceiver;
import com.aurora.change.utils.CommonLog;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.LogFactory;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;
import aurora.widget.AuroraCheckBox;

public class WallpaperLocalAdapter extends BaseAdapter {

    private static final String TAG = "WallpaperLocalAdapter";
    private static CommonLog log = LogFactory.createLog(TAG);
    private List<PictureGroupInfo> mGroupInfos = null;
    private List<List<PictureInfo>> mPictureInfos = null;
    private Context mContext = null;
    private LayoutInflater mInflater;
    private static final int SINGLE_TYPE = 1;
    private static final int MULT_TYPE = 2;
    private HashMap<String, Boolean> mRadiostates;// 用于记录每个RadioButton的状态，并保证只可选一个
    private ListView mListView;
    //private ImageLoader mImageLoader;
    //private boolean mIsLoadding = false;
    private ImageResizer mImageResizer;
    private ListCheckStateChangeCallback mStateChangeCallback;
    private int mSize = 0;
    private boolean isImageFailed = false;
    private boolean isGroupExit = false;
    
    //shigq add start
    private AuroraAlertDialog mNextDayDialog = null;
    private boolean groupChanged = false;
    //shigq add end

    public WallpaperLocalAdapter(Context context, List<PictureGroupInfo> groupInfos,
            List<List<PictureInfo>> pictureInfos) {
        this.mGroupInfos = groupInfos;
        this.mPictureInfos = pictureInfos;
        this.mContext = context;
        mInflater = ( LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mImageLoader = new ImageLoader(context);
        initCurrentGroup();
        mSize = pictureInfos.size();
    }

    @Override
    public int getCount() {
        return mGroupInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mGroupInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

	 // Aurora liugj 2014-09-23 modified for getView optimize start
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int itemType = getItemType(mGroupInfos.get(position));
        ViewHolder holder = null;
        
        if (convertView != null) {
    		holder = (ViewHolder) convertView.getTag();
		}
        if (itemType == SINGLE_TYPE) {
			View singleView = convertView;
			if (singleView == null || (holder != null && holder.mViewType != SINGLE_TYPE)) {
				singleView = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
		        RelativeLayout cont = ( RelativeLayout )singleView.findViewById(com.aurora.R.id.control_padding);
		        cont.setPadding(0, 0, 0, 0);
		        RelativeLayout front = ( RelativeLayout ) singleView.findViewById(com.aurora.R.id.aurora_listview_front);
		        LinearLayout back = ( LinearLayout ) singleView.findViewById(com.aurora.R.id.aurora_listview_back);
		        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		        float ds = dm.density;
		        int width = ( int ) (72 * ds);
		        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
		        lp.gravity = Gravity.BOTTOM;
		        lp.bottomMargin = ( int ) (10 * ds);
		        
				View view = mInflater.inflate(R.layout.local_wallpaper_list_single_item, null);
				holder = new ViewHolder();
                holder.mLockpaperTitleColor = ( ImageView ) view
                        .findViewById(R.id.lockpaper_item_title_color);
                holder.mLockpaperTitleName = ( TextView ) view
                        .findViewById(R.id.lockpaper_item_title_name);
                holder.mLockpaperTitleNum = ( TextView ) view.findViewById(R.id.lockpaper_item_title_num);
                holder.mLockpaperSettings = ( RadioButton ) view
                        .findViewById(R.id.lockpaper_item_settings);
                holder.mLockpaperPreImg1 = ( ImageView ) view.findViewById(R.id.lockpaper_pre_img1);
                holder.mLockpaperImgError = ( TextView ) view.findViewById(R.id.lockpaper_pre_null);
                holder.mViewType = SINGLE_TYPE;
                lp.height = ( int ) (145 * ds);
                front.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                back.setLayoutParams(lp);
                singleView.setTag(holder);
			}
			convertView = singleView;
		}else if (itemType == MULT_TYPE) {
			View multiView = convertView;
			if (multiView == null || (holder != null && holder.mViewType != MULT_TYPE)) {
				multiView = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
		        RelativeLayout cont = ( RelativeLayout )multiView.findViewById(com.aurora.R.id.control_padding);
		        cont.setPadding(0, 0, 0, 0);
		        RelativeLayout front = ( RelativeLayout ) multiView.findViewById(com.aurora.R.id.aurora_listview_front);
		        LinearLayout back = ( LinearLayout ) multiView.findViewById(com.aurora.R.id.aurora_listview_back);
		        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		        float ds = dm.density;
		        int width = ( int ) (72 * ds);
		        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
		        lp.gravity = Gravity.BOTTOM;
		        lp.bottomMargin = ( int ) (10 * ds);
				
		        View view = mInflater.inflate(R.layout.local_wallpaper_list_mult_item, null);
				holder = new ViewHolder();
                holder.mLockpaperTitleColor = ( ImageView ) view
                        .findViewById(R.id.lockpaper_item_title_color);
                holder.mLockpaperTitleName = ( TextView ) view
                        .findViewById(R.id.lockpaper_item_title_name);
                holder.mLockpaperTitleNum = ( TextView ) view.findViewById(R.id.lockpaper_item_title_num);
                holder.mLockpaperSettings = ( RadioButton ) view
                        .findViewById(R.id.lockpaper_item_settings);
                holder.mLockpaperPreImg1 = ( ImageView ) view.findViewById(R.id.lockpaper_pre_img1);
                holder.mLockpaperPreImg2 = ( ImageView ) view.findViewById(R.id.lockpaper_pre_img2);
                holder.mLockpaperLl3 = ( LinearLayout ) view.findViewById(R.id.lockpaper_pre_ll3);
                holder.mLockpaperPreImg3 = ( ImageView ) view.findViewById(R.id.lockpaper_pre_img3);
                holder.mLockpaperPreImg4 = ( ImageView ) view.findViewById(R.id.lockpaper_pre_img4);
                holder.mLockpaperPreImg5 = ( ImageView ) view.findViewById(R.id.lockpaper_pre_img5);
                holder.mLockpaperImgError = ( TextView ) view.findViewById(R.id.lockpaper_pre_null);
                holder.mViewType = MULT_TYPE;
                lp.height = ( int ) (296 * ds);
                front.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                back.setLayoutParams(lp);
                multiView.setTag(holder);
			}
			convertView = multiView;
			switch (mGroupInfos.get(position).getCount()) {
            case 2:
                holder.mLockpaperPreImg1.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg2.setVisibility(View.VISIBLE);
                holder.mLockpaperLl3.setVisibility(View.GONE);
                break;
            case 3:
                holder.mLockpaperPreImg1.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg2.setVisibility(View.VISIBLE);
                holder.mLockpaperLl3.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg3.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg4.setVisibility(View.GONE);
                holder.mLockpaperPreImg5.setVisibility(View.GONE);
                break;
            case 4:
                holder.mLockpaperPreImg1.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg2.setVisibility(View.VISIBLE);
                holder.mLockpaperLl3.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg3.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg4.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg5.setVisibility(View.GONE);
                break;
            case 5:
                holder.mLockpaperPreImg1.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg2.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg3.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg4.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg5.setVisibility(View.VISIBLE);
                holder.mLockpaperLl3.setVisibility(View.VISIBLE);
                break;
            default:
                holder.mLockpaperPreImg1.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg2.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg3.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg4.setVisibility(View.VISIBLE);
                holder.mLockpaperPreImg5.setVisibility(View.VISIBLE);
                holder.mLockpaperLl3.setVisibility(View.VISIBLE);
                break;
			}
		}
        String groupName = mGroupInfos.get(position).getDisplay_name();
        if (mContext.getResources().getConfiguration().locale.getCountry().equals("CN")) {
        	if (groupName.startsWith("Custom Series")) {
        		groupName = groupName.replace("Custom Series", mContext.getString(R.string.wallpaper_crop_custom_name));
			}else {
				groupName = groupName.replace("Custom series", mContext.getString(R.string.wallpaper_crop_custom_name));
			}
		}else {
			if (groupName.startsWith("Custom Series")) {
        		groupName = groupName.replace("Custom Series", mContext.getString(R.string.wallpaper_crop_custom_name));
			}else {
				groupName = groupName.replace("自定义系列", mContext.getString(R.string.wallpaper_crop_custom_name));
			}
		}
        holder.mLockpaperTitleName.setText(groupName);
        
        //shigq add start
//        holder.mLockpaperTitleNum.setText(mGroupInfos.get(position).getCount() + "" + mContext.getResources().getText(R.string.group_num_type));
        if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(groupName)) {
        	holder.mLockpaperTitleNum.setText(mContext.getResources().getText(R.string.nextday_wallpaper_name_logo));
		} else {
			holder.mLockpaperTitleNum.setText(mGroupInfos.get(position).getCount() + "" + mContext.getResources().getText(R.string.group_num_type));
		}
        //shigq add end
        
        setTitleColors(holder.mLockpaperTitleColor, holder.mLockpaperTitleName, mGroupInfos.get(position));
        holder.mLockpaperSettings.setId(position);
		 // Aurora liugj 2014-09-11 modified for using system lockpaper be deleted start
        if (!isGroupExit && mGroupInfos.get(position).getDisplay_name().equals(Consts.DEFAULT_LOCKPAPER_GROUP)) {
			mRadiostates.put(String.valueOf(position), true);
			String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, Consts.DEFAULT_LOCKPAPER_GROUP);
            
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//          FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
            FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
            //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
		}
		 // Aurora liugj 2014-09-11 modified for using system lockpaper be deleted end

		 // Aurora liugj 2014-09-18 modified for bug-8006 start
        //final RadioButton tempRb = holder.mLockpaperSettings;
        // 当RadioButton被选中时，将其状态记录进States中，并更新其他RadioButton的状态使它们不被选中
        holder.mLockpaperSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	//shigq add start
                // 重置，确保最多只有一项被选中
                /*for (String key : mRadiostates.keySet()) {
                    mRadiostates.put(key, false);
                }*/
//                mRadiostates.put(String.valueOf(position), true/*tempRb.isChecked()*/);
                //notifyDataSetChanged();
            	
            	String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
//                boolean isChecked = !current_group.equals(mGroupInfos.get(position).getDisplay_name());
                
                if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(mGroupInfos.get(position).getDisplay_name()) &&
                										!mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(current_group)) {
                	View tipsLayout = mInflater.inflate(R.layout.nextday_setting_dialog_tips_layout, null);
                	
                	AuroraCheckBox mCheckBox = (AuroraCheckBox) tipsLayout.findViewById(R.id.nextday_setting_dialog_tips_checkbox);
                	mCheckBox.setChecked(true);
                	DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_WIFI_NETWORK_SETTINGS, true);
                	mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
							// TODO Auto-generated method stub
							if (!isChecked) {
								DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_WIFI_NETWORK_SETTINGS, isChecked);
							}
						}
					});
                	
                	groupChanged = false;
                	
                	mNextDayDialog = new AuroraAlertDialog.Builder(mContext)
                	.setTitle(mContext.getResources().getString(R.string.nextday_wallpaper_setting_dialog_title))
                	.setMessage(mContext.getResources().getString(R.string.nextday_wallpaper_setting_dialog_message))
                	.setView(tipsLayout)
                	.setCancelable(true)
                	.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	//go back and set radiostate as false
                        	mRadiostates.put(String.valueOf(position), false);
                        	if (mStateChangeCallback != null) {
                                String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
                                boolean isChecked = !current_group.equals(mGroupInfos.get(position).getDisplay_name());
                                
                                if (isChecked) {
                    				notifyDataSetChanged();
                    			}
                            }
                        }
                    })
                	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        	groupChanged = true;
                        	for (String key : mRadiostates.keySet()) {
                                mRadiostates.put(key, false);
                            }
                        	mRadiostates.put(String.valueOf(position), true);
                        	stateChangedCallBack(position);
                        }
                    }).show();
                	
                	mNextDayDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface arg0) {
							// TODO Auto-generated method stub
							Log.d("Wallpaper_DEBUG", "onDismiss----------------------------");
							mNextDayDialog = null;
							if (!groupChanged) {
								//go back and set radiostate as false
	                        	mRadiostates.put(String.valueOf(position), false);
	                        	if (mStateChangeCallback != null) {
	                                String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
	                                boolean isChecked = !current_group.equals(mGroupInfos.get(position).getDisplay_name());
	                                
	                                if (isChecked) {
	                    				notifyDataSetChanged();
	                    			}
	                            }
							}
						}
					});
                	                    
                } else {
                	for (String key : mRadiostates.keySet()) {
                        mRadiostates.put(key, false);
                    }
                	DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_WIFI_NETWORK_SETTINGS, false);
                	mRadiostates.put(String.valueOf(position), true);
                	stateChangedCallBack(position);
                }
            }
        });
        boolean res = mRadiostates.get(String.valueOf(position));
        /*if (mRadiostates.get(String.valueOf(position)) == false) {
            res = false;
            mRadiostates.put(String.valueOf(position), false);
        } else {
            res = true;
            Log.d(ChangeReceiver.LOCK_TAG, TAG + " CURRENT_LOCKPAPER_GROUP = "+mGroupInfos.get(position).getDisplay_name());
            DataOperation.getInstance(mContext).putString(Consts.CURRENT_LOCKPAPER_GROUP,
                    mGroupInfos.get(position).getDisplay_name());
        }*/
		  // Aurora liugj 2014-09-18 modified for bug-8006 end
        holder.mLockpaperSettings.setChecked(res);
        if (res || (mGroupInfos.get(position).getSystem_flag() == 1)) {
            View v = ( View ) convertView.findViewById(com.aurora.R.id.aurora_item_sliding_switch);
            v.setVisibility(View.VISIBLE);
		  // Aurora liugj 2014-09-26 modified for bug-8756 start
        }else {
        	View v = ( View ) convertView.findViewById(com.aurora.R.id.aurora_item_sliding_switch);
            v.setVisibility(View.GONE);
		}
		// Aurora liugj 2014-09-26 modified for bug-8756 end
        
        int pictureSize = mPictureInfos.get(position).size();
        if (pictureSize > 0) {
        	//shigq add start
//        	setImagePre(holder.mLockpaperPreImg1, mPictureInfos.get(position).get(0), position);
            int count = mGroupInfos.get(position).getCount();
            
        	if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(mGroupInfos.get(position).getDisplay_name())) {
        		if (count == 3 && pictureSize == 3) {
					setImagePre(holder.mLockpaperPreImg1, mPictureInfos.get(position).get(2), position);
				} else {
					setImagePre(holder.mLockpaperPreImg1, mPictureInfos.get(position).get(0), position);
				}
        		
			} else {
				setImagePre(holder.mLockpaperPreImg1, mPictureInfos.get(position).get(0), position);
			}
            //shigq add end
        	
            //int pictureSize = mPictureInfos.get(position).size();
            if (count == 2 && pictureSize == 2) {
                setImagePre(holder.mLockpaperPreImg2, mPictureInfos.get(position).get(1), position);
            } else if (count == 3 && pictureSize == 3) {
            	
            	//shigq add start
                /*setImagePre(holder.mLockpaperPreImg2, mPictureInfos.get(position).get(1), position);
                setImagePre(holder.mLockpaperPreImg3, mPictureInfos.get(position).get(2), position);*/
            	if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(mGroupInfos.get(position).getDisplay_name())) {
            		setImagePre(holder.mLockpaperPreImg2, mPictureInfos.get(position).get(1), position);
                    setImagePre(holder.mLockpaperPreImg3, mPictureInfos.get(position).get(0), position);
            	} else {
            		setImagePre(holder.mLockpaperPreImg2, mPictureInfos.get(position).get(1), position);
                    setImagePre(holder.mLockpaperPreImg3, mPictureInfos.get(position).get(2), position);
            	}
            	
            } else if (count == 4 && pictureSize == 4) {
                setImagePre(holder.mLockpaperPreImg2, mPictureInfos.get(position).get(1), position);
                setImagePre(holder.mLockpaperPreImg3, mPictureInfos.get(position).get(2), position);
                setImagePre(holder.mLockpaperPreImg4, mPictureInfos.get(position).get(3), position);
            } else if (count >= 5 && pictureSize >= 5) {
                setImagePre(holder.mLockpaperPreImg2, mPictureInfos.get(position).get(1), position);
                setImagePre(holder.mLockpaperPreImg3, mPictureInfos.get(position).get(2), position);
                setImagePre(holder.mLockpaperPreImg4, mPictureInfos.get(position).get(3), position);
                setImagePre(holder.mLockpaperPreImg5, mPictureInfos.get(position).get(4), position);
            }
            holder.mLockpaperImgError.setVisibility(View.GONE);
        } else {
            DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
            
            //shigq add start
            DbControl mDbControl = new DbControl(mContext);
            PictureGroupInfo groupInfo = mDbControl.queryGroupByName(Consts.DEFAULT_LOCKPAPER_GROUP);
            if (groupInfo.getIsTimeBlack() == 0) {
            	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "false");
			} else {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "true");
			}
            if (groupInfo.getIsStatusBarBlack() == 0) {
            	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "false");
			} else {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "true");
			}
            mDbControl.close();
            //shigq add end
            
            holder.mLockpaperImgError.setVisibility(View.VISIBLE);
        }
        if (isImageFailed) {
        	holder.mLockpaperSettings.setEnabled(false);
        	isImageFailed = false;
		}else {
			holder.mLockpaperSettings.setEnabled(true);
		}
        return convertView;
    }
	 // Aurora liugj 2014-09-23 modified for getView optimize end

    class ViewHolder {
        ImageView mLockpaperTitleColor;
        TextView mLockpaperTitleName;
        TextView mLockpaperTitleNum;
        RadioButton mLockpaperSettings;
        ImageView mLockpaperPreImg1;
        ImageView mLockpaperPreImg2;
        LinearLayout mLockpaperLl3;
        ImageView mLockpaperPreImg3;
        ImageView mLockpaperPreImg4;
        ImageView mLockpaperPreImg5;
        TextView mLockpaperImgError;
        int mViewType;
    }

    private static int getItemType(PictureGroupInfo groupInfo) {
        if (groupInfo.getCount() > 1) {
            return MULT_TYPE;
        } else {
            return SINGLE_TYPE;
        }
    }

    public HashMap<String, Boolean> getGroupState(){
        return mRadiostates;
    }

    private void initCurrentGroup() {
        mRadiostates = new HashMap<String, Boolean>(getCount());
        
        //shigq add start
//        String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
        String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, null);
    	if (current_group == null) {
    		DbControl mDbControl = new DbControl(mContext);
        	PictureGroupInfo groupInfo = mDbControl.queryDefaultGroup();
    		
    		if (groupInfo != null) {
        		current_group = groupInfo.getDisplay_name();
    		} else {
    			current_group = Consts.DEFAULT_LOCKPAPER_GROUP;
    		}
    		mDbControl.close();
		}
        //shigq add end
        
        for (int i = 0; i < getCount(); i++) {
        	if (current_group.equals(mGroupInfos.get(i).getDisplay_name())) {
				isGroupExit = true;
			}
            mRadiostates.put(i + "", current_group.equals(mGroupInfos.get(i).getDisplay_name()));
        }
    }

    public void setImageResizer(ImageResizer imageResizer) {
        mImageResizer = imageResizer;
    }
    
    private void setTitleColors(ImageView iv, TextView tv, PictureGroupInfo groupInfo){
        if ("Timely".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.timely_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.timely_color));
        } else if ("City".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.city_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.city_color));
        } else if ("Cat".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.cat_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.cat_color));
        } else if ("Color".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.color_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.color_color));
        } else if ("Dream".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.dream_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.dream_color));
		  // Aurora liugj 2015-03-11 modified for new lockpapers start
        } else if ("Green".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.green_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.green_color));
		  } else if ("Cute".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.cute_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.cute_color));
        } else if ("Fascinating".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.fascinating_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.fascinating_color));
        } else if ("MissPuff".equals(groupInfo.getDisplay_name())) {
            iv.setBackgroundColor(mContext.getResources().getColor(R.color.misspuff_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.misspuff_color));
		  // Aurora liugj 2015-03-11 modified for new lockpapers end
        } else {
            /*iv.setBackgroundColor(mContext.getResources().getColor(R.color.default_title_color));
            tv.setTextColor(mContext.getResources().getColor(R.color.default_title_color));*/
        	
        	if (groupInfo.getThemeColor() != null) {
    			iv.setBackgroundColor(Color.parseColor(groupInfo.getThemeColor()));
    			tv.setTextColor(Color.parseColor(groupInfo.getThemeColor()));
    		} else {
    			iv.setBackgroundColor(mContext.getResources().getColor(R.color.default_title_color));
                tv.setTextColor(mContext.getResources().getColor(R.color.default_title_color));
    		}
        }
    	//shigq add end
        
    }

    private void setImagePre(ImageView iv, PictureInfo info, int position) {
//        Log.d("wallpaper", "zll big_icon=" + info.getBigIcon());
        String url = "";
        url = String.valueOf(info.getBigIcon());
        if (!checkFileAvailable(url)) {
			// Aurora liugj 2014-11-13 modified for find file failed show error start
        	//iv.setBackgroundResource(R.drawable.item_load_failed);
        	iv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_load_failed));
			// Aurora liugj 2014-11-13 modified for find file failed show error end
        	File file = new File(url);
        	if (file != null) {
        		Log.d("liugj", file.getParentFile() + " checkFileAvailable = "+file.listFiles());
        		if (file.getParentFile() == null || (file.getParentFile() != null && (!file.getParentFile().exists() || file.getParentFile().listFiles().length == 0))) {
    				isImageFailed = true;
    			}
			}
		}else {
			mImageResizer.loadImage(url, iv);
		}
//        Bitmap bitmap = mImageLoader.getmeMemoryCache().getBitmapFromCache(url);
//        if (bitmap != null) {
//            iv.setImageBitmap(bitmap);
//        }
//        iv.setImageResource(R.drawable.loading);
//        mImageLoader.DisplayImage(url, iv, mIsLoadding);
//        notifyDataSetChanged();
        
    }

    /**
     * 检查文件是否有效
     * @param path 完整文件路径
     * @return boolean 是否有效
     */
    private boolean checkFileAvailable(String path) {
        boolean exist = false;
        try {
            File file = new File(path);
            if (file != null && file.exists()) {
                exist = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("liugj", TAG + " checkFileAvailable path = "+path + " === "+exist);
        return exist;
    }
    
    private void clearList() {
    	if (mGroupInfos != null) {
    		mGroupInfos.clear();
    		mGroupInfos = null;
		}
        if (mPictureInfos != null) {
        	mPictureInfos.clear();
        	mPictureInfos = null;
		}
    }

    private void clearAll() {
    	clearList();
    	if (mRadiostates != null) {
			mRadiostates.clear();
			mRadiostates = null;
		}
    }

    public void clearCache() {
        /*if (mImageLoader != null) {
            mImageLoader.clearCache();
        }*/
    	clearAll();
        if (mImageResizer != null) {
            // mImageResizer.clearCache();
            mImageResizer.clearMemoryCache();
            mImageResizer.closeCache();
        }
    }

    public void onPause() {
        if (mImageResizer != null) {
            mImageResizer.setExitTasksEarly(true);
            mImageResizer.flushCache();
        }
    }

    public void onResume() {
        DbControl mDbControl = new DbControl(mContext);
//        mGroupInfos = mDbControl.queryAllGroupInfos();
        mGroupInfos = mDbControl.queryAllGroupInfosOrderBy();
        mPictureInfos = mDbControl.queryAllPictureInfosOrderBy();
        initCurrentGroup();
        mDbControl.close();
        if (mImageResizer != null) {
			   // Aurora liugj 2014-09-18 modified for bug-8006 start
            /*if (mSize != mGroupInfos.size()) {
                mImageResizer.clearMemoryCache();
            }*/
				// Aurora liugj 2014-09-18 modified for bug-8006 end
            mImageResizer.setExitTasksEarly(false);
            notifyDataSetChanged();
        }
    }

    /*public void setLoadFlag(boolean isLoad) {
        this.mIsLoadding = isLoad;
    }*/

    public void setListView(ListView listView) {
        this.mListView = listView;
    }

    public void setCheckStateChangeCallback(ListCheckStateChangeCallback callback) {
        mStateChangeCallback = callback;
    }

    public interface ListCheckStateChangeCallback {
        void onCheckStateChange(String currentGroup, boolean isChecked);
    }
    
    //shigq add start
    public void stateChangedCallBack(int position) {
    	if (mStateChangeCallback != null) {
            String current_group = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
            Log.d(ChangeReceiver.LOCK_TAG, TAG + current_group+"== CURRENT_LOCKPAPER_GROUP = "+mGroupInfos.get(position).getDisplay_name());
            boolean isChecked = !current_group.equals(mGroupInfos.get(position).getDisplay_name());
            Log.d("Wallpaper_DEBUG", "current_group = "+current_group+" and target group = "+mGroupInfos.get(position).getDisplay_name());
            
            if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(mGroupInfos.get(position).getDisplay_name())) {
            	mStateChangeCallback.onCheckStateChange("NextDay", isChecked);
            	
			} else {
				mStateChangeCallback.onCheckStateChange(mGroupInfos.get(position).getDisplay_name(), isChecked);
			}
            
            Log.d("Wallpaper_DEBUG", "isCheck = "+isChecked);
            if (isChecked) {
				notifyDataSetChanged();
			}
            DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, mGroupInfos.get(position).getDisplay_name());
            if (!mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(mGroupInfos.get(position).getDisplay_name())) {
            	if (mGroupInfos.get(position).getIsTimeBlack() == 0) {
                	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "false");
    			} else {
    				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "true");
    			}
                if (mGroupInfos.get(position).getIsStatusBarBlack() == 0) {
                	DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "false");
    			} else {
    				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "true");
    			}
                
			} else {
				ArrayList<NextDayPictureInfo> mPictureList = ((AuroraChangeApp) mContext.getApplicationContext()).getNextDayPictureInfoList();
				if (mPictureList != null && mPictureList.size() != 0) {
					NextDayPictureInfo pictureInfo = mPictureList.get(0);
					
					if ("white".equals(pictureInfo.getPictureTimeColor())) {
						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "false");
					} else {
						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_TIME_BLACK, "true");
					}
					if ("white".equals(pictureInfo.getPictureStatusColor())) {
						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "false");
					} else {
						DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP_STATUS_BLACK, "true");
					}
				}
			}
        }
    }
    //shigq add end
        
}
