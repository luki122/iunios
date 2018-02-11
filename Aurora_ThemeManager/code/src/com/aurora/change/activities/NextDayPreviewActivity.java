package com.aurora.change.activities;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.animation.Animator.AnimatorListener;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.thememanager.R;
import com.aurora.thememanager.utils.ThemeConfig;
import com.aurora.thememanager.utils.themehelper.ThemeManager;
import com.aurora.change.adapters.NextDayPreViewAdapter;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.model.PictureInfo;
import com.aurora.change.model.ThemeInfo;
import com.aurora.change.receiver.ChangeReceiver;
import com.aurora.change.utils.CommonUtil;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.FileHelper;
import com.aurora.change.utils.ImageLoaderHelper;
import com.aurora.change.utils.NextDayLoadAndProcessTask;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;
import com.aurora.change.view.NextDayTimeLayout;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraCheckBox;

public class NextDayPreviewActivity extends AuroraActivity {
	private Context mContext;
	private ViewPager mViewPager;
	private RelativeLayout mPreviewInfoLayout;
	private RelativeLayout mPreviewControlLayout;
	private NextDayTimeLayout mNextDayTimeLayout;
	private RelativeLayout mPreviewTextLayout;
	private LinearLayout mCheckBoxLayout;
	private ImageView mMaskLayer;
	private NextDayPreViewAdapter mAdapter;
	private ArrayList<NextDayPictureInfo> mPictureList;
	private ImageView toBack;
	private ImageView toSave;
	private ImageView toSet;
	private ImageView toShare;
	private CheckBox toCheck;
	private TextView mCommentCity;
	private TextView mComment;
	private ProgressBar mLoadingProgressBar;
	
	private String mFilePath;
	private String mLoadingType;
	
	private int pos = 0;
	private float mOldX = 0;
    private float mOldY = 0;
    private static int firstHour = -1;
    private static int firstMinute = -1;
    private boolean isHomeBack = false;
    private String width;
	private String height;
    
    private Animator mControlLayoutAnimator;
    private Animator mPreviewTimeLayotAnimator;
    
    private AuroraAlertDialog mAlertDialog = null;
    private boolean mCheckInit = false;
    private ThemeManager mThemeManager;
    Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == Consts.LOCKPAPER_NEXTDAY_UPDATE) {
				Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity-----------------LOCKPAPER_NEXTDAY_UPDATE--------refreshPictureComment");
				NextDayPictureInfo pictureInfo = (NextDayPictureInfo) msg.obj;
				if (pictureInfo == null) {
					pictureInfo = getCurrentPictureInfo();
				}
				refreshPictureComment(pictureInfo);
				
			} else if (msg.what == Consts.LOCKPAPER_NEXTDAY_SHOW_TIPS) {
				CommonUtil.showToast(mContext, getResources().getString(R.string.share_wallpaper_loading_tips));
				
			} else if (msg.what == Consts.LOCKPAPER_NEXTDAY_IS_SAVED_TIPS) {
				CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_completed));
				
			} else if (msg.what == Consts.LOCKPAPER_NEXTDAY_SET_WALLPAPER) {
				String filePath = (String) msg.obj;
				Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity-----------------LOCKPAPER_NEXTDAY_SET_WALLPAPER----filePath = "+filePath);
				setWallpaper(filePath);
			} else if (msg.what == Consts.LOCKPAPER_NEXTDAY_LOAD_COMPLETE) {
				Intent intent = (Intent) msg.obj;
				mContext.startActivity(Intent.createChooser(intent, mContext.getResources().getString(R.string.share_wallpaper)));
			}
		}
    	
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nextday_preview_layout);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mContext = this;
		mThemeManager = ThemeManager.getInstance(ThemeConfig.THEME_TIMES);
		mViewPager = (ViewPager) findViewById(R.id.nextday_wallpaper_preview);
		mPreviewInfoLayout = (RelativeLayout) findViewById(R.id.nextday_wallpaper_preview_info_layout);
		mPreviewControlLayout = (RelativeLayout) findViewById(R.id.nextday_wallpaper_preview_control_layout);
		mNextDayTimeLayout = (NextDayTimeLayout) findViewById(R.id.nextday_time_layout);
		mPreviewTextLayout = (RelativeLayout) findViewById(R.id.nextday_wallpaper_preview_text_layout);
		mCheckBoxLayout = (LinearLayout) findViewById(R.id.to_check_layout);
		mMaskLayer = (ImageView) findViewById(R.id.mask_layer);
		
		toBack = (ImageView) findViewById(R.id.to_back);
		toSave = (ImageView) findViewById(R.id.to_save);
		toSet = (ImageView) findViewById(R.id.to_set);
		toShare = (ImageView) findViewById(R.id.to_share);
		toCheck = (CheckBox) findViewById(R.id.to_check);
		mCommentCity = (TextView) findViewById(R.id.comment_city);
		mComment = (TextView) findViewById(R.id.comment);
		mLoadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
		
//		InitiatePictureList();
		mPictureList = ((ThemeManagerApplication) mContext.getApplicationContext()).getNextDayPictureInfoList();
//		mAdapter = new NextDayPreViewAdapter(mContext, mPictureList, mLoadingProgressBar);
		mAdapter = new NextDayPreViewAdapter(mContext, mHandler, mPictureList, mLoadingProgressBar);
		
		mViewPager.setAdapter(mAdapter);
//		mViewPager.setCurrentItem(Consts.NEXTDAY_PICTURE_SIZE - 1);
		mViewPager.setCurrentItem(mPictureList.size() - 1);
		mViewPager.setOnTouchListener(mTouchListener);
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		
		width = String.valueOf(((ThemeManagerApplication) mContext.getApplicationContext()).getDisplayWidth());
		height = String.valueOf(((ThemeManagerApplication) mContext.getApplicationContext()).getDisplayHeight());
		
		mCheckBoxLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("Wallpaper_DEBUG", "click----------------------");
				mCheckInit = !mCheckInit;
				toCheck.setChecked(mCheckInit);
				DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_SHOW_COMMENTS, mCheckInit);
			}
		});
				
		toCheck.setChecked(DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_SHOW_COMMENTS, false));
		
		mPreviewControlLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//do nothing, to improve the operation of clicking for toCheck
			}
		});
		
		toBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		toSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
				String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
				if (DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_SHOW_COMMENTS, false)) {
					mFilePath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + "_time_comment" + ".jpg";
				} else {
					mFilePath = originalPath;
				}
				
				if (FileHelper.fileIsExist(mFilePath)) {
					mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_NONE;
					CommonUtil.showToast(mContext, mContext.getResources().getString(R.string.nextday_wallpaper_loading_completed));
					return;
					
				} else {
					if (!mFilePath.equals(originalPath) && FileHelper.fileIsExist(originalPath)) {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_INFO;
					} else if (mFilePath.equals(originalPath)) {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE;
					} else {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD;
					}
				}
				
				if (CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.NO_NET) {
					CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_without_network));
					
				} else if (DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_LOADING_ANYWAY, false) || 
						CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.WIFI) {
					NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(mHandler);
					loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, Consts.NEXTDAY_OPERATION_SAVE, mLoadingType, mFilePath);
//					new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SAVE);
					
				} else {
					LayoutInflater mInflater = LayoutInflater.from(mContext);
					View tipsLayout = mInflater.inflate(R.layout.nextday_loading_dialog_tips_layout, null);
	            	
	            	AuroraCheckBox mCheckBox = (AuroraCheckBox) tipsLayout.findViewById(R.id.nextday_loading_dialog_tips_checkbox);
	            	mCheckBox.setChecked(false);
	            	mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
							// TODO Auto-generated method stub
							DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_LOADING_ANYWAY, isChecked);
						}
					});
	            	
	            	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
	            	builder.setTitle(getResources().getString(R.string.nextday_wallpaper_loading_dialog_title));
	            	builder.setMessage(getResources().getString(R.string.nextday_wallpaper_loading_dialog_message));
	            	builder.setView(tipsLayout);
	            	builder.setCancelable(true);
	            	builder.setNegativeButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_cancel), new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	//do nothing
	                    }
	                });
	            	builder.setPositiveButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_confirm), new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
//	                    	new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SAVE);
	                    	NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(mHandler);
	    					loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, Consts.NEXTDAY_OPERATION_SAVE, mLoadingType, mFilePath);
	                    }
	                });
	                builder.show();
				}
			}
		});
		toSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
				
				mFilePath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + "_comment" + ".jpg";
				String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
				
				if (FileHelper.fileIsExist(mFilePath)) {
					mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_NONE;
					
				} else {
					if (FileHelper.fileIsExist(originalPath)) {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_INFO;
					} else {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD;
					}
				}
								
				if (CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.NO_NET) {
					CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_without_network));
					
				} else if (DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_LOADING_ANYWAY, false) || 
						CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.WIFI) {
					NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(mHandler);
					loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, Consts.NEXTDAY_OPERATION_SET, mLoadingType, mFilePath);
//					new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SET);
					
				} else {
//					new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SET);
					
					LayoutInflater mInflater = LayoutInflater.from(mContext);
					View tipsLayout = mInflater.inflate(R.layout.nextday_loading_dialog_tips_layout, null);
	            	
	            	AuroraCheckBox mCheckBox = (AuroraCheckBox) tipsLayout.findViewById(R.id.nextday_loading_dialog_tips_checkbox);
	            	mCheckBox.setChecked(false);
	            	mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
							// TODO Auto-generated method stub
							DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_LOADING_ANYWAY, isChecked);
						}
					});
	            	
	            	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
	            	builder.setTitle(getResources().getString(R.string.nextday_wallpaper_loading_dialog_title));
	            	builder.setMessage(getResources().getString(R.string.nextday_wallpaper_loading_dialog_message));
	            	builder.setView(tipsLayout);
	            	builder.setCancelable(true);
	            	builder.setNegativeButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_cancel), new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	//do nothing
	                    }
	                });
	            	builder.setPositiveButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_confirm), new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
	                    	NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(mHandler);
	    					loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, Consts.NEXTDAY_OPERATION_SET, mLoadingType, mFilePath);
//	                    	new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SET);
	                    }
	                });
	                builder.show();
				}
			}
		});
		toShare.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
				
				String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
				if (DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_SHOW_COMMENTS, false)) {
					mFilePath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + "_time_comment" + ".jpg";
				} else {
					mFilePath = originalPath;
				}
				
				if (FileHelper.fileIsExist(mFilePath)) {
					mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_NONE;
					
				} else {
					if (!mFilePath.equals(originalPath) && FileHelper.fileIsExist(originalPath)) {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_INFO;
					} else if (mFilePath.equals(originalPath)) {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE;
					} else {
						mLoadingType = Consts.NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD;
					}
				}
				
				if (CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.NO_NET) {
					CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_without_network));
					
				} else if (DataOperation.getBooleanPreference(mContext, Consts.NEXTDAY_LOADING_ANYWAY, false) || 
						CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.WIFI) {
					NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(mHandler);
					loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, Consts.NEXTDAY_OPERATION_SHARE, mLoadingType, mFilePath);
//					new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SHARE);
					
				} else {
//					new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SHARE);
					
					LayoutInflater mInflater = LayoutInflater.from(mContext);
					View tipsLayout = mInflater.inflate(R.layout.nextday_loading_dialog_tips_layout, null);
	            	
	            	AuroraCheckBox mCheckBox = (AuroraCheckBox) tipsLayout.findViewById(R.id.nextday_loading_dialog_tips_checkbox);
	            	mCheckBox.setChecked(false);
	            	mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
							// TODO Auto-generated method stub
							DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_LOADING_ANYWAY, isChecked);
						}
					});
	            	
	            	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
	            	builder.setTitle(getResources().getString(R.string.nextday_wallpaper_loading_dialog_title));
	            	builder.setMessage(getResources().getString(R.string.nextday_wallpaper_loading_dialog_message));
	            	builder.setView(tipsLayout);
	            	builder.setCancelable(true);
	            	builder.setNegativeButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_cancel), new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	//do nothing
	                    }
	                });
	            	builder.setPositiveButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_confirm), new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    	NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
	                    	NextDayLoadAndProcessTask loadAndProcessTask = new NextDayLoadAndProcessTask(mHandler);
	    					loadAndProcessTask.execute(mContext, pictureInfo, width + "*" + height, Consts.NEXTDAY_OPERATION_SHARE, mLoadingType, mFilePath);
//	                    	new NextDayLoadingPictureTask().execute(mContext, pictureInfo, mHandler, Consts.NEXTDAY_OPERATION_SHARE);
	                    }
	                });
	                builder.show();
				}
			}
		});
		
		/*toCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean value) {
				// TODO Auto-generated method stub
				DataOperation.setBooleanPreference(mContext, Consts.NEXTDAY_SHOW_COMMENTS, value);
			}
		});*/
		
		
	}
	
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK) {
			Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity---------------------onActivityResult----Activity.RESULT_OK ");
			if (requestCode == 1) {
				Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity-------------------------------onActivityResult----requestCode = 1");
			}
		} else {
			Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity---------------------onActivityResult----failed ");
		}
    }*/
	
	OnTouchListener mTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOldX = x;
                    mOldY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    int dx = Math.abs(( int ) (x - mOldX));
                    int dy = Math.abs(( int ) (y - mOldY));
                    /*if (mTransition == null) {
                    	mTransition = new LayoutTransition();
					}
                    if (dx < 20 && !mTransition.isRunning()) {
                        toggleActionBar();
                        togglePreView();
                    }*/
                    if (dx < 20) {
                    	if (mPreviewControlLayout.getVisibility() == View.GONE) {
							mPreviewControlLayout.setVisibility(View.VISIBLE);
							mMaskLayer.setVisibility(View.VISIBLE);
							toBack.setVisibility(View.VISIBLE);
							
							if (mPreviewInfoLayout.getVisibility() == View.VISIBLE) {
								mPreviewTimeLayotAnimator = getPreviewTimeLayoutAnimator(false, 300);
								mPreviewTimeLayotAnimator.start();
							}
							
							mControlLayoutAnimator = getPreviewControlLayoutAnimator(true, 300);
							mControlLayoutAnimator.start();
							
						} else {
//							mPreviewControlLayout.setVisibility(View.GONE);
//							mMaskLayer.setVisibility(View.GONE);
							toBack.setVisibility(View.GONE);
							
							if (mPreviewInfoLayout.getVisibility() == View.GONE) {
								mPreviewInfoLayout.setVisibility(View.VISIBLE);
								mPreviewTimeLayotAnimator = getPreviewTimeLayoutAnimator(true, 300);
								mPreviewTimeLayotAnimator.start();
							}
							
							mControlLayoutAnimator = getPreviewControlLayoutAnimator(false, 300);
							mControlLayoutAnimator.start();
						}
					}
                    break;

                default:
                    break;
            }
            return false;
        }
    };
	
	OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
        	pos = position;
//        	mNextDayTimeLayout.onPageSelected(position);
        	Log.d("Wallpaper_DEBUG", "----------------onPageSelected = "+position);
//        	NextDayPictureInfo pictureInfo = mPictureList.get(Consts.NEXTDAY_PICTURE_SIZE - 1 - position);
        	NextDayPictureInfo pictureInfo = mPictureList.get(mPictureList.size() - 1 - position);
        	mNextDayTimeLayout.onPageSelected(position, pictureInfo);
        	refreshPictureComment(pictureInfo);
        	
        	
        }
        
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        	mNextDayTimeLayout.onPageScrolled(position, positionOffset, positionOffsetPixels);
//        	Log.d("Wallpaper_DEBUG", "----------------onPageScrolled-------position = "+position);
//        	Log.d("Wallpaper_DEBUG", "----------------onPageScrolled-------positionOffset = "+positionOffset);
//        	Log.d("Wallpaper_DEBUG", "----------------onPageScrolled-------positionOffsetPixels = "+positionOffsetPixels);
//        	Log.d("Wallpaper_DEBUG", "----------------onPageScrolled-------alpha = "+positionOffsetPixels/1080.0f);
        	/*if (positionOffsetPixels != 0) {
//				mNextDayTimeLayout.setAlpha(positionOffsetPixels/1080.0f);
//				mPreviewTextLayout.setAlpha(positionOffsetPixels/1080.0f);
				
				float delta = Math.abs(positionOffset - 0.5f);
				mNextDayTimeLayout.setAlpha((1 - delta)/2);
				mPreviewTextLayout.setAlpha((1 - delta)/2);
			}*/
        	
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        	/*Log.d("Wallpaper_DEBUG", "----------------onPageScrollStateChanged-------state = "+state);
        	if (state == 0) {
        		mNextDayTimeLayout.setAlpha(1.0f);
        		mPreviewTextLayout.setAlpha(1.0f);
			}*/
        }
    };
    
    public NextDayPictureInfo getCurrentPictureInfo() {
    	int index = mViewPager.getCurrentItem();
    	Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity---------------getCurrentPictureInfo-----index = "+index);
//		return mPictureList.get(Consts.NEXTDAY_PICTURE_SIZE - 1 - index);
    	return mPictureList.get(mPictureList.size() - 1 - index);
    }
    
    public void refreshPictureComment(NextDayPictureInfo pictureInfo) {
    	if (pictureInfo.getPictureCommentCity() != null) {
    		mPreviewTextLayout.setVisibility(View.VISIBLE);
    		mCommentCity.setText(pictureInfo.getPictureCommentCity());
		} else {
			mPreviewTextLayout.setVisibility(View.GONE);
		}
    	if (pictureInfo.getPictureComment() != null) {
    		mComment.setText(pictureInfo.getPictureComment());
		}
    }
    
    private void setWallpaper(final String filePath) {
    	AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
    	builder.setTitle(getResources().getString(R.string.set_wallpaper_tips));
    	builder.setMessage(getResources().getString(R.string.set_wallpaper_content));
    	builder.setCancelable(true);
    	builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	//do nothing
            }
        });
    	builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	/*NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
				new SetWallpaperTask().execute(pictureInfo);*/
            	new ToSetWallpaperTask().execute(filePath);
            }
        });
        builder.show();
    }
        
    private class LoadImageTask extends AsyncTask<Object, Object, Boolean> {
    	private NextDayPictureInfo pictureInfo;
//		private String url;
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "LoadImageTask---------------doInBackground");
//			url = (String) params[0];
			pictureInfo = (NextDayPictureInfo) params[0];
			String url = pictureInfo.getPictureOriginalUrl();
			
			String path = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
			File file = new File(path);
			if (file.exists()) return true;
			
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Wallpaper_DEBUG", "LoadImageTask---------------doInBackground--------Exception = "+e);
			}
			
			if (bitmap == null) return false;
			Log.d("Wallpaper_DEBUG", "LoadImageTask---------------doInBackground--------bitmap = "+bitmap);
//			String path = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
			return FileHelper.writeImage(bitmap, path, 100);
		}
		
		protected void onPostExecute(Boolean result) {
     		Log.d("Wallpaper_DEBUG", "LoadImageTask---------------onPostExecute--------result = "+result);
     		
     		if (result) {
     			CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_completed));
			} else {
				CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_failed));
			}
     	}
	}
    
    private class LoadAndShareImageTask extends AsyncTask<Object, Object, Boolean> {
    	private NextDayPictureInfo pictureInfo;
//    	private boolean isLoading;
    	private boolean isShare = false;
    	private boolean isSaved = false;
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "LoadOrShareImageTask---------------doInBackground");
			pictureInfo = (NextDayPictureInfo) params[0];
//			isLoading = (Boolean) params[1];
			isShare = (Boolean) params[1];
			
			String url = pictureInfo.getPictureOriginalUrl();
			
			String path = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
			File file = new File(path);
			if (file.exists()) {
				isSaved = true;
				
				Message msgMessage = Message.obtain();
				msgMessage.what = Consts.LOCKPAPER_NEXTDAY_IS_SAVED_TIPS;
				mHandler.sendMessage(msgMessage);
				
				return true;
			}
			
			Message msgMessage = Message.obtain();
			msgMessage.what = Consts.LOCKPAPER_NEXTDAY_SHOW_TIPS;
			mHandler.sendMessage(msgMessage);
			
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Wallpaper_DEBUG", "LoadOrShareImageTask---------------doInBackground--------Exception = "+e);
			}
			
			if (bitmap == null) return false;
			Log.d("Wallpaper_DEBUG", "LoadOrShareImageTask---------------doInBackground--------bitmap = "+bitmap);
			
			return FileHelper.writeImage(bitmap, path, 100);
		}
		
		protected void onPostExecute(Boolean result) {
     		Log.d("Wallpaper_DEBUG", "LoadOrShareImageTask---------------onPostExecute--------result = "+result);
     		
     		String path = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
			File file = new File(path);
			
     		if (isShare && file.exists()) {
     			Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_SUBJECT, "share");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                
//				startActivity(Intent.createChooser(intent, getResources().getString(R.string.vs_barcode_share_to)));
				startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.share_wallpaper)), 1);
				
			} else if (!isShare && !isSaved) {
	     		if (result) {
	     			CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_completed));
				} else {
					CommonUtil.showToast(mContext, getResources().getString(R.string.nextday_wallpaper_loading_failed));
				}
			}
     	}
	}
    
    private class ToSetWallpaperTask extends AsyncTask<Object, Object, Boolean> {
    	private String filePath;
    	private String mGroupName;
    	private NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
    	
    	@Override
		protected Boolean doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "ToSetWallpaperTask---------------doInBackground");
			filePath = (String) params[0];
			File file = new File(filePath);
			if (!file.exists()) return false;
						
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath));
				
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Wallpaper_DEBUG", "SetWallpaperTask---------------doInBackground--------Exception = "+e);
			}
			if (bitmap == null) return false;
			
			mGroupName = getGroupName();
			StringBuffer path = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
			path.append(mGroupName).append("/").append("data01").append(".png");
			boolean bool = FileHelper.writeImage(bitmap, path.toString(), 100);
			if (!bool) return false;
						
			creatNewThemeAndSetWallpaper(pictureInfo, "data01", path.toString());
			return true;
		}
		
		protected void onPostExecute(Boolean result) {
     		Log.d("Wallpaper_DEBUG", "ToSetWallpaperTask---------------onPostExecute--------result = "+result);
     		
     		if (result) {
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, mGroupName);
				
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
                
                String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, mGroupName);
                
                //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 start
//                boolean res = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
                boolean res = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH, mContext);
                //M:shigq send broadcast to notify the wallpaper has been changed for Android5.0 end
                
                CommonUtil.showToast(mContext, getResources().getString(R.string.wallpaper_set_success));
                
//                finish();
                
			} else {
				CommonUtil.showToast(mContext, getResources().getString(R.string.set_wallpaper_failed));
			}
     	}

    }
    
    /*private class SetWallpaperTask extends AsyncTask<Object, Object, Boolean> {
    	private NextDayPictureInfo pictureInfo;
    	private String mGroupName;
    	
    	@Override
		protected Boolean doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "SetWallpaperTask---------------doInBackground");
			pictureInfo = (NextDayPictureInfo) params[0];
			String url = pictureInfo.getPictureOriginalUrl();
			
			Bitmap bitmap = null;
			try {
				String path = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
				File file = new File(path);
				if (file.exists()) {
					bitmap = BitmapFactory.decodeStream(new FileInputStream(path));
				} else {
					bitmap = BitmapFactory.decodeStream(new URL(url).openStream());
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("Wallpaper_DEBUG", "SetWallpaperTask---------------doInBackground--------Exception = "+e);
			}
			if (bitmap == null) return false;
			
			mGroupName = getGroupName();
			StringBuffer path = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
			path.append(mGroupName).append("/").append("data01").append(".png");
			boolean bool = FileHelper.writeImage(bitmap, path.toString(), 100);
			if (!bool) return false;
			
			creatNewThemeAndSetWallpaper("data01", path.toString());
			return true;
		}
		
		protected void onPostExecute(Boolean result) {
     		Log.d("Wallpaper_DEBUG", "SetWallpaperTask---------------onPostExecute--------result = "+result);
     		
     		if (result) {
     			CommonUtil.showToast(mContext, getResources().getString(R.string.wallpaper_set_success));
				DataOperation.setStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, mGroupName);
                String currentPath = WallpaperUtil.getCurrentLockPaperPath(mContext, mGroupName);
                boolean res = FileHelper.copyFile(currentPath, Consts.LOCKSCREEN_WALLPAPER_PATH);
                
                finish();
                
			} else {
//				Toast.makeText(mContext, "save faled!", Toast.LENGTH_LONG).show();
			}
     	}

    }*/
    
    public String getGroupName() {
        DbControl control = new DbControl(mContext);
        List<PictureGroupInfo> groupInfos = control.queryAllGroupInfos();
        StringBuffer group_name = new StringBuffer(Consts.DEFAULT_LOCKPAPER_FILE_NAME);
        int id = 1;
        if (groupInfos != null && groupInfos.size() != 0) {
            id = groupInfos.get(groupInfos.size() - 1).getId() + 1;
        }
        if (id < 10) {
            group_name.append("0").append(id);
        } else {
        	group_name.append(id);
        }
        control.close();
        return group_name.toString();
    }
    
    public void creatNewThemeAndSetWallpaper(NextDayPictureInfo pictureInfo, String file, String path) {
    	String mGroupName = getGroupName();
		StringBuffer filePath = new StringBuffer(Consts.DEFAULT_SDCARD_LOCKSCREEN_WALLPAPER_PATH);
		filePath.append(mGroupName).append("/").append(Consts.LOCKPAPER_SET_FILE);
		
		ThemeInfo mThemeInfo = new ThemeInfo();
		mThemeInfo.name = mGroupName;
		/*String defaultGroup = Consts.DEFAULT_LOCKPAPER_GROUP;
		Log.d("Wallpaper_DEBUG", "creatNewThemeAndSetWallpaper==============defaultGroup = "+defaultGroup);
		if (defaultGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_1) || defaultGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
    		mThemeInfo.timeBlack = "true";
		}*/
		if ("white".equals(pictureInfo.getPictureTimeColor())) {
			mThemeInfo.timeBlack = "false";
			mThemeInfo.statusBarBlack = "false";
		} else {
			mThemeInfo.timeBlack = "true";
			mThemeInfo.statusBarBlack = "true";
		}
		
		String fileString = WallpaperConfigUtil.creatWallpaperConfigurationXmlFile(filePath.toString(), mThemeInfo);
		Log.d("Wallpaper_DEBUG", "creatNewThemeAndSetWallpaper==============creatWallpaperConfigurationXmlFile========fileString = "+fileString);
		
		DbControl control = new DbControl(mContext);
		updatePictrueGroupDatabase(control, mThemeInfo, 1);
		updatePictrueDatabase(control, mGroupName, file, path);
		
		control.refreshDb();
		control.close();
    }
    
    public void updatePictrueGroupDatabase(DbControl control, ThemeInfo mThemeInfo, int group_count) {
        PictureGroupInfo groupInfo = new PictureGroupInfo();
        groupInfo.setDisplay_name(mThemeInfo.name);
        groupInfo.setThemeColor(mThemeInfo.nameColor);
        groupInfo.setIsDefaultTheme("false".equals(mThemeInfo.isDefault)? 0 : 1);
        groupInfo.setIsTimeBlack("false".equals(mThemeInfo.timeBlack)? 0 : 1);
        groupInfo.setIsStatusBarBlack("false".equals(mThemeInfo.statusBarBlack)? 0 : 1);
        groupInfo.setCount(group_count);
        control.insertPictureGroup(groupInfo, false);
        mThemeManager.setTimeWallpaperUnApplied(this);
    }

    public void updatePictrueDatabase(DbControl control, String group_name, String pictureTitle, String path) {
        PictureGroupInfo belong_group = control.queryGroupByName(group_name);
        int belong_id = belong_group.getId();
        PictureInfo pictureInfo = new PictureInfo();
        pictureInfo.setBelongGroup(belong_id);
        pictureInfo.setIdentify(pictureTitle);
        pictureInfo.setBigIcon(path);
        control.insertPicture(pictureInfo);
    }
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
    	Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity----------------onSaveInstanceState");
    	isHomeBack = true;
    }
    
    @Override
    protected void onResume() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        super.onResume();
        NextDayPictureInfo pictureInfo = getCurrentPictureInfo();
        mNextDayTimeLayout.updateClock(pictureInfo);
        mNextDayTimeLayout.refresh(0, pictureInfo);
        
        if (isHomeBack && CommonUtil.getNetWorkType(mContext) == CommonUtil.NetWorkType.MOBILE_ONLY) {
        	isHomeBack = false;
        	if (mAlertDialog != null && mAlertDialog.isShowing()) {
        		mAlertDialog.dismiss();
        		mAlertDialog = null;
			}
        	
        	mAlertDialog = new AuroraAlertDialog.Builder(mContext)
        	.setTitle(getResources().getString(R.string.nextday_wallpaper_loading_dialog_title))
        	.setMessage(getResources().getString(R.string.nextday_wallpaper_loading_dialog_mobile_message))
        	.setNegativeButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_mobile_cancel), 
        																			new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	((ThemeManagerApplication) mContext.getApplicationContext()).setIsMobileData(false);
                }
            })
        	.setPositiveButton(getResources().getString(R.string.nextday_wallpaper_loading_dialog_mobile_confirm), 
        																			new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	((ThemeManagerApplication) mContext.getApplicationContext()).setIsMobileData(true);
                }
            })
            .show();
		}
        
        refreshPictureComment(pictureInfo);
        Log.d("Wallpaper_DEBUG", "NextDayPreviewActivity---------------onResume--------pictureInfo = "+pictureInfo);
    }
    
    
    
    /*ObjectAnimator tran = ObjectAnimator.ofFloat(greeting, "translationY",starty, starty / 2);
	tran.setDuration(850);
	tran.setInterpolator(new OvershootInterpolator(1.5f));*/
    
	private Animator getPreviewControlLayoutAnimator(boolean direction, int duration) {
		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator mControlLayoutAnimator = null;
		ObjectAnimator mMaskAlphaAnimator = null;
		if (direction) {
			mControlLayoutAnimator = new ObjectAnimator().ofFloat(mPreviewControlLayout, "translationY", 330f, 0f);
			mControlLayoutAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
			
			mMaskAlphaAnimator = new ObjectAnimator().ofFloat(mMaskLayer, "alpha", 0.0f, 1.0f);
			
		} else {
			mControlLayoutAnimator = new ObjectAnimator().ofFloat(mPreviewControlLayout, "translationY", 0f, 330f);
			mMaskAlphaAnimator = new ObjectAnimator().ofFloat(mMaskLayer, "alpha", 1.0f, 0.0f);
		}
		
		// 设置fallAnim动画的持续时间
		mControlLayoutAnimator.setDuration(duration);
		mControlLayoutAnimator.addListener(mControlLayoutAnimatorListener);
		
		mMaskAlphaAnimator.setDuration(duration);
		
		animatorSet.playTogether(mControlLayoutAnimator, mMaskAlphaAnimator);
		
		return animatorSet;
	}
	
	private AnimatorListener mControlLayoutAnimatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator arg0) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "mControlLayoutAnimatorListener---------onAnimationStart");
		}
		@Override
		public void onAnimationRepeat(Animator arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onAnimationEnd(Animator arg0) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "mControlLayoutAnimatorListener---------onAnimationEnd");
			if (mPreviewControlLayout.getTranslationY() == 0) {
				mPreviewControlLayout.setVisibility(View.VISIBLE);
				
				mMaskLayer.setVisibility(View.VISIBLE);
				
			} else {
				mPreviewControlLayout.setVisibility(View.GONE);
				
				mMaskLayer.setVisibility(View.GONE);
			}
		}
		@Override
		public void onAnimationCancel(Animator arg0) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "mControlLayoutAnimatorListener---------onAnimationCancel");
		}
	};
	
	private Animator getPreviewTimeLayoutAnimator(boolean direction, int duration) {
		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator mTimeLayoutYAnimator = null;
		ObjectAnimator mTimeLayoutAlphaAnimator = null;
		ObjectAnimator mTextLayoutAlphaAnimator = null;
		if (direction) {
			mTimeLayoutYAnimator = new ObjectAnimator().ofFloat(mNextDayTimeLayout, "translationY", -100f, 0f);
			mTimeLayoutAlphaAnimator = new ObjectAnimator().ofFloat(mNextDayTimeLayout, "alpha", 0.0f, 1.0f);
			
			mTextLayoutAlphaAnimator = new ObjectAnimator().ofFloat(mPreviewTextLayout, "alpha", 0.0f, 1.0f);
			
		} else {
			mTimeLayoutYAnimator = new ObjectAnimator().ofFloat(mNextDayTimeLayout, "translationY", 0f, -100f);
			mTimeLayoutAlphaAnimator = new ObjectAnimator().ofFloat(mNextDayTimeLayout, "alpha", 1.0f, 0.0f);
			
			mTextLayoutAlphaAnimator = new ObjectAnimator().ofFloat(mPreviewTextLayout, "alpha", 1.0f, 0.0f);
		}
		
		// 设置fallAnim动画的持续时间
		mTimeLayoutYAnimator.setDuration(duration);
		mTimeLayoutYAnimator.addListener(mTimeLayoutAnimatorListener);
		
		animatorSet.playTogether(mTimeLayoutYAnimator, mTimeLayoutAlphaAnimator, mTextLayoutAlphaAnimator);
		
		return animatorSet;
	}
	
	private AnimatorListener mTimeLayoutAnimatorListener = new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator arg0) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "mTimeLayoutAnimatorListener---------onAnimationStart");
		}
		@Override
		public void onAnimationRepeat(Animator arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onAnimationEnd(Animator arg0) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "mTimeLayoutAnimatorListener---------onAnimationEnd");
			if (mNextDayTimeLayout.getTranslationY() == 0) {
//				mNextDayTimeLayout.setVisibility(View.VISIBLE);
				
			} else {
//				mNextDayTimeLayout.setVisibility(View.GONE);
				mPreviewInfoLayout.setVisibility(View.GONE);
			}
		}
		@Override
		public void onAnimationCancel(Animator arg0) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "mTimeLayoutAnimatorListener---------onAnimationCancel");
		}
	};
    
}
