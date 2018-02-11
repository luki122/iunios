//<!-- aurora add zhouxiaobing 20130910 --> for dial launcher
package com.android.contacts.activities;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.widget.AuroraTabHost;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.privacymanage.service.AuroraPrivacyUtils;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import aurora.preference.AuroraPreferenceManager;


@SuppressWarnings("deprecation")
public class AuroraDialActivity extends TabActivity {
	private Intent oldIntent=null;
    private TabWidget tw;
    private AuroraTabHost th;
    private ImageView button2;
    private Uri mRealUri;
    
    private boolean mHasMissedCall = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//        .detectAll()
//        .penaltyLog()
//        .penaltyDeath()
//        .build());
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.aurora_dial_main);
		
		checkMissedCall();
		
		th = (AuroraTabHost)this.getTabHost();
		th.setTabChangeAnimation(myTabChangeAnimation);
		
		Intent intent = getIntent();
		Intent intent1 = new Intent();
		intent1.setClass(this, AuroraCallLogActivity.class);
		
		Intent intent2 = new Intent();
		if (isDialIntent(intent)) {
		    intent2 = intent;
		    setReallyUri(intent);
		}
		intent2.setClass(this, AuroraDialtactsActivityV2.class);
		
		Intent intent3 = new Intent();
		intent3.setClass(this, AuroraPeopleActivity.class);
		
		LayoutInflater inflater = this.getLayoutInflater();
		ImageView button1 = (ImageView)inflater.inflate(R.layout.aurora_dial_tab_imageview, null);
		button1.setImageResource(R.drawable.aurora_tab_calllogx);
		button2=(ImageView)inflater.inflate(R.layout.aurora_dial_tab_imageview, null);
		if(ContactsApplication.sIsAuroraYuloreSupport){
			button2.setImageResource(R.drawable.aurora_tab_dial_upx);
		} else {
			button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
		}
		ImageView button3 = (ImageView)inflater.inflate(R.layout.aurora_dial_tab_imageview, null);
		button3.setImageResource(R.drawable.aurora_tab_contactx);
		
		th.addTab(th.newTabSpec("tab1").setContent(intent1).setIndicator(button1));
		th.addTab(th.newTabSpec("tab2").setContent(intent2).setIndicator(button2));
		th.addTab(th.newTabSpec("tab3").setContent(intent3).setIndicator(button3));
		th.setOnTabChangedListener(mTabChangedListener);
		
		mPrefs = AuroraPreferenceManager.getDefaultSharedPreferences(this);
		mCurrentTab = mPrefs.getInt("currentTab", 1);
		((AuroraTabHost)this.getTabHost()).firstCurrentTab(mCurrentTab);
		
		tw = this.getTabWidget();
		tw.setDividerDrawable(this.getResources().getDrawable(R.drawable.aurora_tab_sep));
		if (GNContactsUtils.isMultiSimEnabled()) {
		    tw.setBackgroundResource(R.drawable.aurora_bar_bg);						
	    }
		SystemUtils.setStatusBarBackgroundTransparent(this);
	}
	
	private TabChangeAnimation myTabChangeAnimation = new TabChangeAnimation() {
		
		@Override
		public void animationBeforeSetTab() {
			Activity currentActivity = getCurrentActivity();
			if (currentActivity != null) {
				if (currentActivity instanceof AuroraCallLogActivity) {
					((AuroraCallLogActivity) currentActivity)
							.animationBeforeSetTab();
				} else if (currentActivity instanceof AuroraDialtactsActivityV2) {
					((AuroraDialtactsActivityV2) currentActivity)
							.animationBeforeSetTab();
				} else if (currentActivity instanceof AuroraPeopleActivity) {
					((AuroraPeopleActivity) currentActivity)
							.animationBeforeSetTab();
				}
			}
		}
		
		@Override
		public void animationAfterSetTab() {
			Activity currentActivity = getCurrentActivity();
			if (currentActivity != null) {
				if (currentActivity instanceof AuroraCallLogActivity) {
					((AuroraCallLogActivity) currentActivity)
							.animationAfterSetTab();
				} else if (currentActivity instanceof AuroraDialtactsActivityV2) {
					((AuroraDialtactsActivityV2) currentActivity)
							.animationAfterSetTab();
				} else if (currentActivity instanceof AuroraPeopleActivity) {
					((AuroraPeopleActivity) currentActivity)
							.animationAfterSetTab();
				}
			}
		}
	};
	
	@Override
	protected void onResume() {
	    // TODO Auto-generated method stub
	    super.onResume();
	    if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
	    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
	    }
		String type = this.getIntent().getType();

		// CallLog.Calls.CONTENT_TYPE
		if (type != null
				&& type.equalsIgnoreCase("vnd.android.cursor.dir/calls")
				|| mHasMissedCall) {
			mIsCalllog = true;
			mHasMissedCall = false;
			th.setCurrentTabNoAnimation(0);
		} else if (isDialIntent(this.getIntent())) {
			mIsCalllog = false;
			th.setCurrentTabNoAnimation(1);
		} else {
			mIsCalllog = false;
			th.setCurrentTabNoAnimation(mCurrentTab);
		}
		
		if(oldIntent!=null){
			setIntent(oldIntent);
		}
		
		
	}

	@Override
    public void onNewIntent(Intent newIntent) {
		oldIntent=getIntent();//bug10983
        if ((newIntent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
	    
        checkMissedCall();
        
        setReallyUri(newIntent);
        setIntent(newIntent);
	}
	
	private void setReallyUri(Intent intent) {
	    if (intent != null) {
	        mRealUri = intent.getData();
	    }
	}
	
	public Uri getReallyUri() {
	    Uri data = mRealUri;
	    mRealUri = null;
        return data;
	}
	
	public void setTabWidgetVisible(final int visible) {
		LayoutTransition transitioner = new LayoutTransition();
		TranslateAnimation animation = null;
		
		if (visible == View.VISIBLE) {
			animation = new TranslateAnimation(0, 0, tw.getHeight(), 0);
		} else {
			animation = new TranslateAnimation(0, 0, 0, tw.getHeight());
		}
		
		animation.setDuration(transitioner
				.getDuration(LayoutTransition.DISAPPEARING));
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				tw.setVisibility(visible);
			}
		});
		
		animation.setInterpolator(new LinearInterpolator());
		tw.startAnimation(animation);
	}
	
	public ImageView getTabsicon() {
		return button2;
	}
	
	private boolean isDialIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action)) {
            return true;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            final Uri data = intent.getData();
            if (data != null && "tel".equals(data.getScheme())) {
                return true;
            }
        }
        
        return false;
	}
	
	//aurora change liguangyu 20131112 for BUG #677 start
	private boolean isCalllogIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            final String data = intent.getType();
            if (data != null && data.equals("vnd.android.cursor.dir/calls")) {
                return true;
            }
        }        
        return false;
	}
	//aurora change liguangyu 20131112 for BUG #677 end
	
	//aurora add liguangyu 201311128 start
    private SharedPreferences mPrefs;
    private boolean mIsCalllog = false;
	private int mCurrentTab;
	
	@Override
    protected void onDestroy() {
		Log.v("AuroraDialActivity", "onDestroy");
        if (!mIsCalllog) {
    		Log.v("AuroraDialActivity", "onDestroy tabIndex= " + this.getTabHost().getCurrentTab());
			Editor editor = mPrefs.edit();
			editor.putInt("currentTab", this.getTabHost().getCurrentTab());
			editor.commit();
        }
        super.onDestroy();
    }
    //aurora add liguangyu 201311128 end
	
	private boolean mFirst = true;
	 
	private TabHost.OnTabChangeListener mTabChangedListener = new TabHost.OnTabChangeListener(){
		public void onTabChanged(String tabId){
			if (mFirst) {
				mFirst = false;
				return;
			}
			int index = Integer.valueOf(tabId.substring(3)) - 1;
			Log.v("AuroraDialActivity", "onTabSelectionChanged tabIndex= " + index);
			if(!mIsCalllog || index != 0) {
				mCurrentTab = index;
			}
			if(index != 0) {
				mIsCalllog = false;
			}
			
			//aurora add liguangyu 20140925 for #8664 start
	        if(!mIsCalllog) {
				Editor editor = mPrefs.edit();
				editor.putInt("currentTab", mCurrentTab);
				editor.commit();
	        }
	        //aurora add liguangyu 20140925 for #8664 end
		}
	};
	
	
    public boolean dispatchKeyEvent(KeyEvent event) {
		Log.v("SHIJIAN", "AuroraDialActivity dispatchKeyEvent  "); 
	    if(!ViewConfiguration.get(this).hasPermanentMenuKey()) {
	    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
	    }
        return super.dispatchKeyEvent(event);
    }
    
    //让父容器TabActivity在不调用saveInstanceState的情况下onDestroy, 为空是一样的效果
    //aurora add liguangyu 20140806 for BUG #7262 start
    protected void onSaveInstanceState(Bundle outState) {
    	
    }
    //aurora add liguangyu 20140806 for BUG #7262 end
    
    private void checkMissedCall() {
    	
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final String[] CALL_LOG_PROJECTION = new String[] { Calls._ID };
				StringBuilder where = new StringBuilder("type=");
				where.append(Calls.MISSED_TYPE);
				where.append(" AND new=1");
				where.append(" AND (privacy_id = "
						+ AuroraPrivacyUtils.getCurrentAccountId()
						+ " or privacy_id = 0)");
				try {
					Cursor missCallCursor = getContentResolver().query(
							Calls.CONTENT_URI, CALL_LOG_PROJECTION,
							where.toString(), null, Calls.DEFAULT_SORT_ORDER);
					if (missCallCursor != null) {
						if (missCallCursor.getCount() > 0) {
							mHasMissedCall = true;
						}
						missCallCursor.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
    }
}
