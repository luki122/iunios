package com.aurora.voiceassistant.view;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.aurora.voiceassistant.model.*;
import com.aurora.voiceassistant.R;

import android.R.integer;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraAnimationImageView;

public class ViewimageActivity extends Activity implements OnPageChangeListener 
{
	private final int MSG_TYEP_LD_ERR = 0;
	private final int MSG_TYEP_LD_OK = 1;
	private final int MSG_TYEP_ANIM_TRANS_START = 2;
	private final int MSG_TYEP_ANIM_TRANS_STOP = 3;
	private final int MSG_TYEP_ANIM_LOADING_START = 4;
	private final int MSG_TYEP_ANIM_LOADING_STOP = 5;
	private final int MSG_TYEP_ANIM_RELOAD = 6;
	
	private int curIndex = 0;
	private int size = 0;
	private ArrayList<RspXmlPicItem> rspXmlpicItemList=null;
	private Context context;
	
	private ViewPager viewPager;
	
	private Bitmap[] bitMaps;
	
	private ImageView[] mImageViews;
	
	private LinearLayout mTitleLayout;
	private RelativeLayout mBottomLayout;
	private boolean mIsDisp = false;
	private Timer mCheckDispTimer;
	private TextView mTietleText;
	private AnimationDrawable drawloadingAnim;
	private ImageView  LoadingAnim;
	private String path;
	private boolean initFlag = false;
	private boolean exitFlag = false;
	//private Thread reloadThread = null;
	private final String TAG = "VS-VI";
	
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vs_viewimg_layout);
		
		context = this;
		LoadingAnim = (ImageView)findViewById(R.id.imageview_anim);
		
		
		
		LoadingAnim.setBackgroundResource(R.anim.vs_imgview_loading_anim);
		drawloadingAnim = (AnimationDrawable) LoadingAnim.getBackground();
		drawloadingAnim.start();		
	
		curIndex = getIntent().getIntExtra("index", 0);
		path = getIntent().getStringExtra("path");
		rspXmlpicItemList = getIntent().getParcelableArrayListExtra("list");
		size = rspXmlpicItemList.size();
		size = (size>9)? 9:size;
		
		
		bitMaps = new Bitmap[size];
		
		
		mImageViews = new ImageView[size];
		for(int j=0; j<mImageViews.length; j++)
		{
			ImageView imageView = new ImageView(this);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	      
			imageView.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					if(View.VISIBLE == mTitleLayout.getVisibility())
					{
						Message msg = Message.obtain();
						msg.what = MSG_TYEP_ANIM_TRANS_STOP;
						handler.sendMessage(msg);
					}
					else
					{
						
						Message msg = Message.obtain();
						msg.what = MSG_TYEP_ANIM_TRANS_START;
						handler.sendMessage(msg);
						
						mCheckDispTimer = new Timer();
						mCheckDispTimer.schedule(new TimerTask() 
						{
							@Override
							public void run() 
							{
								Message msg = Message.obtain();
								msg.what = MSG_TYEP_ANIM_TRANS_STOP;
								handler.sendMessage(msg);
							}
						},5000);
					}
				}
			});
			
			mImageViews[j] = imageView;
		}
		
		
		
		mTitleLayout = (LinearLayout)findViewById(R.id.title_bar);
		mBottomLayout = (RelativeLayout)findViewById(R.id.imgview_bottom);
		mTietleText =(TextView)findViewById(R.id.imgview_title_text);
		setBarText(curIndex);
		
		ImageView back = (ImageView)findViewById(R.id.imgview_title_back);
		back.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				ViewimageActivity.this.finish();
			}
		});
		
		AuroraAnimationImageView save = (AuroraAnimationImageView)findViewById(R.id.save_btn);
		save.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				if(null != bitMaps[curIndex])
				{
					SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss"); 
					Date   curDate   =   new   Date(System.currentTimeMillis());     
					String   name   =   "vs"+formatter.format(curDate)+".png";
					
					boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
					if(sdCardExist) 
					{ 
						File sdcardDir=Environment.getExternalStorageDirectory();
						String path = sdcardDir.toString()+"/voiceAssistant/";
						
						Tools tools = new Tools();
						boolean flag = tools.saveImage(bitMaps[curIndex], path, name);
						if(flag)
						{
							Toast toast = Toast.makeText(context,context.getResources().getString(R.string.vs_savesec), Toast.LENGTH_SHORT); 
							toast.setGravity(Gravity.CENTER, 0, 0); 
							toast.show();
						}
						else
						{
							Toast toast = Toast.makeText(context,context.getResources().getString(R.string.vs_savefal), Toast.LENGTH_SHORT); 
							toast.setGravity(Gravity.CENTER, 0, 0); 
							toast.show();
						}
					}
					else
					{
						Toast toast = Toast.makeText(context,context.getResources().getString(R.string.vs_savesec_nocard), Toast.LENGTH_SHORT); 
						toast.setGravity(Gravity.CENTER, 0, 0); 
						toast.show();
					}
				}
			}
		});
		
	
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager.setAdapter(new pagerAdapter());
		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem(curIndex);
		
		
		Thread lt = new Thread(new lRunnable(curIndex));
		lt.start();
		
		initFlag = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return true;
	}
	
	@Override
	protected void onStop() 
	{
		exitFlag = true;
		super.onStop();
	}
	@Override
	protected void onStart() 
	{
		exitFlag = false;
		super.onStart();
	}
	@Override
	protected void onDestroy() 
	{
		if(null != mCheckDispTimer)
		{
			mCheckDispTimer.cancel();
			mCheckDispTimer = null;
		}
		int count = bitMaps.length -1;
		while(count >= 0)
		{
			if(null != bitMaps[count] && !bitMaps[count].isRecycled())
			{
				bitMaps[count].recycle();
			}
			count--;
		}
		
		super.onDestroy();
	}
	
	class pagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount() 
		{
			return size;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) 
		{
			((ViewPager)container).removeView(mImageViews[position % mImageViews.length]);
		}

		@Override
		public Object instantiateItem(View container, int position) 
		{
			((ViewPager)container).addView(mImageViews[position % mImageViews.length], 0);
			return mImageViews[position % mImageViews.length];
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) 
	{
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) 
	{
		
	}

	@Override
	public void onPageSelected(int arg0) 
	{
		if(!initFlag) return;
		
		
		curIndex = arg0 % mImageViews.length;
		setBarText(curIndex);
		
		Message msg = Message.obtain();
		
		
		if(null == bitMaps[arg0])
		{
			LoadingAnim.setVisibility(View.VISIBLE);
			LoadingAnim.setBackgroundResource(R.anim.vs_imgview_loading_anim);
			drawloadingAnim = (AnimationDrawable) LoadingAnim.getBackground();
			drawloadingAnim.start();
			
			if(!exitFlag)
			{
				msg.arg1 = curIndex;
				msg.what = MSG_TYEP_ANIM_RELOAD;
				handler.sendMessage(msg);
			}
			
		}
		else
		{
			if(null != drawloadingAnim && drawloadingAnim.isRunning())
			{
				msg.what = MSG_TYEP_ANIM_LOADING_STOP;
				handler.sendMessage(msg);
			}
		}
	}
	
	private void setBarText(int selectItems)
	{
		if(null == mTietleText) return ;
		
		String text = String.valueOf(selectItems+1)+"/"+size;
		mTietleText.setText(text);
	}

	private class lRunnable implements Runnable
	{
		private int index;
		lRunnable(int index)
		{
			this.index = index;
		}
		
		public void run()
		{
			Bitmap bitmap = loadImageFromTemp(index);
			
			Message msg = Message.obtain();
			
			if(null != bitmap)
			{
				bitMaps[index] = bitmap;
				msg.what = MSG_TYEP_LD_OK;
			}
			else
			{
				msg.what = MSG_TYEP_LD_ERR;
			}
			
			msg.arg1 = index;
			handler.sendMessage(msg);
		}
	}
	
	private Handler handler = new Handler() 
	{
		public void handleMessage(android.os.Message msg) 
		{
			
			switch(msg.what)
			{
				case MSG_TYEP_LD_OK:
					{
						RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_LD_OK");
						
						int index = msg.arg1;
						
						if(null == bitMaps[index])
						{
							if(null != LoadingAnim)
							{
								LoadingAnim.setBackgroundResource(R.drawable.vs_imeview_loding_fail);
							}
							return;
						}
						mImageViews[index].setImageBitmap(bitMaps[index]);
						mImageViews[index].invalidate();
						
						
						if(index == curIndex && null != drawloadingAnim && drawloadingAnim.isRunning())
						{
							drawloadingAnim.stop();
							drawloadingAnim = null;
							if(null != LoadingAnim)
							{
								LoadingAnim.setVisibility(View.INVISIBLE);
							}
						}
					}
					break;
				case MSG_TYEP_LD_ERR:
					{
						RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_LD_ERR");
						
						int index = msg.arg1;
						if(index == curIndex && !exitFlag)
						{
							Timer timer = new Timer();
							timer.schedule(new TimerTask() {
								
								@Override
								public void run() 
								{
									new lRunnable(curIndex).run();
								}
							},1000);
						}
					}
					break;
				case MSG_TYEP_ANIM_RELOAD:
					
					if(!exitFlag)
					{
						RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_ANIM_RELOAD");
						int index = msg.arg1;
						Thread lt = new Thread(new lRunnable(index));
						lt.start();
					}
					break;
				case MSG_TYEP_ANIM_TRANS_STOP:
					RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_ANIM_TRANS_STOP");
					
					if(mIsDisp && View.VISIBLE == mTitleLayout.getVisibility())
					{
						animTranslation(false);
						mIsDisp = false;
					}
					
					if(null != mCheckDispTimer)
					{
						mCheckDispTimer.cancel();
						mCheckDispTimer = null;
					}
					break;
				case MSG_TYEP_ANIM_TRANS_START:
					RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_ANIM_TRANS_START");
					
					mTitleLayout.setVisibility(View.VISIBLE);
					mBottomLayout.setVisibility(View.VISIBLE);
					animTranslation(true);
					mIsDisp = true;
					break;
				case MSG_TYEP_ANIM_LOADING_START:
					
					RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_ANIM_LOADING_START");
					
					LoadingAnim.setVisibility(View.VISIBLE);
					LoadingAnim.setBackgroundResource(R.anim.vs_imgview_loading_anim);
					drawloadingAnim = (AnimationDrawable) LoadingAnim.getBackground();
					drawloadingAnim.start();
					break;
				case MSG_TYEP_ANIM_LOADING_STOP:
					
					RkLog.i(TAG,"ViewimageActivity.handler: MSG_TYEP_ANIM_LOADING_STOP");
					
					drawloadingAnim.stop();
					drawloadingAnim = null;
					LoadingAnim.setVisibility(View.INVISIBLE);
					break;
			}
		}
	};
	
	private void animTranslation(boolean in)
	{		
		int offsetTitle = (int)getApplicationContext().getResources().getDimension(R.dimen.vs_offset_title);
		int offsetBottom = (int)getApplicationContext().getResources().getDimension(R.dimen.vs_offset_bottom);
		
		ValueAnimator titleAnim = null;
		ValueAnimator bottomAnim = null;
		
		if(in)
		{
			titleAnim = ObjectAnimator.ofFloat(mTitleLayout, "translationY",offsetTitle);
			bottomAnim = ObjectAnimator.ofFloat(mBottomLayout, "translationY",-offsetBottom);
		}
		else
		{
			titleAnim = ObjectAnimator.ofFloat(mTitleLayout, "translationY",0);
			bottomAnim = ObjectAnimator.ofFloat(mBottomLayout, "translationY",0);
			titleAnim.addListener(translateAnimListener);
		}
		titleAnim.setDuration(500);
		bottomAnim.setDuration(500);
		titleAnim.setInterpolator(new DecelerateInterpolator());
		bottomAnim.setInterpolator(new DecelerateInterpolator());
		
		titleAnim.start();
		bottomAnim.start();
	}
	
	private AnimatorListener translateAnimListener = new AnimatorListener() 
	{
		
		@Override
		public void onAnimationStart(Animator animation) 
		{
			
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) 
		{
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) 
		{
			mTitleLayout.setVisibility(View.GONE);
			mBottomLayout.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private Bitmap loadImageFromTemp(int index)
    {
		Bitmap result = null;
		
		String   name   =   "vs"+String.valueOf(index)+".png";
		
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在 
		if(!sdCardExist) 
		{
			RkLog.e(TAG,"ViewimageActivity.loadImageFromTemp: sdcard exist!!");
		}
		
		File sdcardDir=Environment.getExternalStorageDirectory();
		String picPath = sdcardDir.toString()+"/"+path+"/"+name;
		
		RkLog.i(TAG,"ViewimageActivity.loadImageFromTemp: picPath="+picPath);
		
		Tools tools = new Tools();
		result = tools.loadImage(picPath);
		if(null == result)
		{
			RkLog.i(TAG,"ViewimageActivity.loadImageFromTemp: NULL index ="+index);
			RspXmlPicItem item =  rspXmlpicItemList.get(index);
			String url = (String)item.getImagelink();
			try {
				result = BitmapFactory.decodeStream(new URL(url).openStream());
			} catch (Exception e) {
				RkLog.i(TAG,"ViewimageActivity.loadImageFromTemp: NULL index ="+e);
			}
		}
		
		return result;
    }
}