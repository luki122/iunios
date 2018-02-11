package com.aurora.worldtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;

import com.android.db.AlarmAddUpHelp;
import com.android.deskclock.AnalogClock;
import com.android.deskclock.Log;
import com.android.deskclock.R;
import com.aurora.worldtime.City;
import com.aurora.worldtime.WorldTimeAnimationDrawable.OnFrameAnimationCompleteListener;

import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
//Gionee baorui 2012-12-19 modify for CR00733082 begin
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import aurora.app.AuroraAlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.view.ContextMenu;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
//Gionee baorui 2012-12-19 modify for CR00733082 end
//Gionee <baorui><2013-03-23> modify for CR00783443 begin
import java.io.File;
import java.io.InputStream;
//Gionee <baorui><2013-03-23> modify for CR00783443 end
/**
 * worldtime main activity,show the worldtime
 * 
 */
public class WorldTimeFragment extends Fragment {
    //private static final String TAG = "WorldTimeFragment";
    private AuroraActivity mActivity;
    private View mRootView;
    private AuroraListView mListView;
//    private RelativeLayout mAddCity;
    // Gionee baorui 2012-12-19 modify for CR00733082 begin
    // private ImageView mAddCity;
    // private ImageView mSortCity;
    private static final int DELETEITEM = Menu.FIRST;
    // Gionee baorui 2012-12-19 modify for CR00733082 end
    private LayoutInflater mLayoutInflater;
    private AuroraWorldTimeListAdapter mListAdapter;
    
    private LinearLayout noWorldtimeView;

    private static int KEY = -1;

    SharedPreferences mSharedPreferences;
    private static int UPDATE_ID;// record the ListView click Item id

    List<City> mSearchList = new ArrayList<City>();//the data of all city
    List<City> mShowList = new ArrayList<City>();//the data of show city
    
    // Gionee <baorui><2013-03-23> modify for CR00783443 begin
    private File mXmlFile = null;
    // Gionee <baorui><2013-03-23> modify for CR00783443 end
    
    private TextView nativetimeText;
    private AnalogClock mAnalogClock;
    private static final int WORLDTIME_ENTERANIM = 30;
    private Handler mHandler;
    private TextView dividerTextView;
    
    private WorldTimeEnterFrameAnimation mEnterFrameAnimation;
    private WorldTimeExitFrameAnimation mExitFrameAnimation;
    private static final int[] mAnalogClockBackId = new int[]{
    	R.drawable.worldtime01, R.drawable.worldtime02, 
    	R.drawable.worldtime03, R.drawable.worldtime04, 
    	R.drawable.worldtime05, R.drawable.worldtime06,  
    	R.drawable.worldtime07, R.drawable.worldtime08, 
    	R.drawable.worldtime09, R.drawable.worldtime10, 
    	R.drawable.worldtime11, R.drawable.worldtime12, 
    	R.drawable.worldtime13, R.drawable.worldtime14,
    	R.drawable.worldtime15, R.drawable.worldtime16,
    	R.drawable.worldtime17, R.drawable.worldtime18,
    	R.drawable.worldtime19, R.drawable.worldtime20,
    	R.drawable.worldtime21, R.drawable.worldtime22,
    	R.drawable.worldtime23, R.drawable.worldtime24,
    	R.drawable.worldtime25, R.drawable.worldtime26,
    	R.drawable.worldtime27, R.drawable.worldtime28,
    	R.drawable.worldtime29, R.drawable.worldtime30,
    };
    private static final int[] mAnalogClockBackId2 = new int[]{
    	R.drawable.worldtimeexit01, R.drawable.worldtimeexit02, 
    	R.drawable.worldtimeexit03, R.drawable.worldtimeexit04, 
    	R.drawable.worldtimeexit05, R.drawable.worldtimeexit06,  
    	R.drawable.worldtimeexit07, R.drawable.worldtimeexit08, 
    	R.drawable.worldtimeexit09, R.drawable.worldtimeexit10, 
    	R.drawable.worldtimeexit11, R.drawable.worldtimeexit12, 
    	R.drawable.worldtimeexit13, R.drawable.worldtimeexit14,
    	R.drawable.worldtimeexit15, R.drawable.worldtimeexit16,
    	R.drawable.worldtimeexit17, R.drawable.worldtimeexit18,
    	R.drawable.worldtimeexit19, R.drawable.worldtimeexit20,
    	R.drawable.worldtimeexit21, R.drawable.worldtimeexit22,
    	R.drawable.worldtimeexit23, R.drawable.worldtimeexit24,
    	R.drawable.worldtimeexit25, R.drawable.worldtimeexit26,
    	R.drawable.worldtimeexit27, R.drawable.worldtimeexit28,
    	R.drawable.worldtimeexit29, R.drawable.worldtimeexit30,
    };
    
    public boolean isSortOver = false;
    
    
	/*
	 * register receiver
	 */
	private void worldtimeFragmentRegisterReceiverForTime() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIME_CHANGED);

		mActivity.registerReceiver(mIntentReceiver, filter, null, null);
	}

	private void worldtimeFragmentUnregisterReceiverForTime() {
		mActivity.unregisterReceiver(mIntentReceiver);
	}
	
	/*
	 * receiver broadcast , refresh UI
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e("--WorldTimeFragment BroadcastReceiver action = -------" + action);
	        mSearchList = getWorldTimeZones(mActivity);
	        WorldTimeUtils.setmWorldTimeSearchList(mSearchList);
	        List<City> showList = getIdNameBySharedPreference();
	        WorldTimeUtils.setmWorldTimeShowList(showList);
	        
	        SortSearchList( );
	        
	        //updateData();
		}
	};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d("--------------onCreateView()---------------");
        // mActivity = getActivity();

		mRootView = inflater.inflate(R.layout.world_time_view_white, container, false);

        updateLayout();
        return mRootView;
    }
    
    /**
     * 
     */
    private void initWorldTimeAnim() {
    	mEnterFrameAnimation = new WorldTimeEnterFrameAnimation(mAnalogClock, mAnalogClockBackId);
    	mEnterFrameAnimation.setAnimationImageListener(new AnimationEndListener() {
			
			@Override
			public void onAnimationEnd() {
				// TODO Auto-generated method stub
				//mAnalogClock.setDialNotNeed(false);
			}
		});
    	mExitFrameAnimation = new WorldTimeExitFrameAnimation(mAnalogClock, mAnalogClockBackId2);
    	mExitFrameAnimation.setAnimationImageListener(new AnimationEndListener() {
			
			@Override
			public void onAnimationEnd() {
				// TODO Auto-generated method stub
				if (mOnAnimationCompleteListener != null) {
					mOnAnimationCompleteListener.onWorldTimeFragmentAnimationComplete();
				}
			}
		});
    }

    /**
     * initialize activity
     */
    private void updateLayout() {
    	
    	mHandler = new Handler();
    	
    	mAnalogClock = (AnalogClock)mRootView.findViewById(R.id.gn_analog_appwidget);
    	nativetimeText = (TextView)mRootView.findViewById(R.id.nativetimetext);
    	
    	initWorldTimeAnim();
        
		mListView = (AuroraListView) mRootView.findViewById(R.id.lv_worldtime);
		mListView.auroraEnableSelector(false);    //cjs add 
		mListView.setSelector(R.drawable.aurora_listview_selector);  //cjs add
		dividerTextView=(TextView) mRootView.findViewById(R.id.first_divider);	
		
        noWorldtimeView = (LinearLayout) mRootView.findViewById(R.id.worldtime_list_empty);
        // Gionee baorui 2012-12-19 modify for CR00733082 begin
        // mAddCity=(ImageView) mRootView.findViewById(R.id.iv_world_time_add_city);
        // mSortCity=(ImageView) mRootView.findViewById(R.id.iv_world_time_sort);
        // Gionee baorui 2012-12-19 modify for CR00733082 end
        setListViewAdapter();
        // Gionee baorui 2012-12-19 modify for CR00733082 begin
        /*
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                Intent intent = new Intent(mActivity, AllWorldTimeDelete.class);
                intent.putExtra("position", arg2);
                startActivity(intent);
                return true;
            }
        });
        */
        //mListView.setOnCreateContextMenuListener(this);
        // Gionee baorui 2012-12-19 modify for CR00733082 end

        // Gionee <baorui><2013-08-05> modify for CR00844785 begin
        //mListView.registerFragmentForContextMenu(this);
        // Gionee <baorui><2013-08-05> modify for CR00844785 end
        mListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				 switch (scrollState) {  
	             case OnScrollListener.SCROLL_STATE_IDLE: //  
	            	 dividerTextView.setVisibility(View.VISIBLE);
	                
	                 System.out.println("停止...");  
	                 break;  
	             case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:  
	            	 dividerTextView.setVisibility(View.INVISIBLE);
               
	                 System.out.println("正在滑动...");  
	                 break;  
	             case OnScrollListener.SCROLL_STATE_FLING:  
	            	 dividerTextView.setVisibility(View.INVISIBLE);
	              
	                 System.out.println("开始滚动...");  
	   
	                 break;  
	             }  
				
			}@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				 
				
			}
		});

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
            	if ( !isSortOver ) {
            		return;
            	}
                Intent intent = new Intent(mActivity, WorldTimeSearchActivity.class);
                startActivityForResult(intent, Constants.TIME_ZONE_UPDATE);
                UPDATE_ID = arg2;
            }
        });
        mListView.auroraSetNeedSlideDelete(true);
      
       mListView.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {
			
			@Override
			public void auroraPrepareDraged(int position) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void auroraOnClick(int position) {
				// TODO Auto-generated method stub
				auroraJudgeIfDeleteCity(position);
			}
			
			@Override
			public void auroraDragedUnSuccess(int position) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void auroraDragedSuccess(int position) {
				// TODO Auto-generated method stub
				
			}
		});
        
        startAnalogClockAnim();
        
    }
    
    /**
     * @param positon 弹出对话框判断是否删除城市
     */
    private void auroraJudgeIfDeleteCity( final int position ) {
        // Confirm that the alarm will be deleted.
        new AuroraAlertDialog.Builder(mActivity, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                /*.setTitle(getString(R.string.delete_city))*/
                .setTitle(getString(R.string.delete_the_city))
                // .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.alarm_delete_sure2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                    	auroraDeleteCity( position );
                    }
                }).setNegativeButton(R.string.alarm_alert_dismiss_text2, null).show();
    }
    
    /**
     * @param positon 删除城市
     */
    private void auroraDeleteCity(int positon) {
    	
    	mListView.auroraSetAuroraItemState(0);
    	
        Editor editor = mSharedPreferences.edit();
        editor.remove(Constants.KEY);
        for (int i = 0; i < mShowList.size(); i++) {
            editor.remove(Constants.KEY_ID + i);
        }
        mShowList.remove(mShowList.get(positon));

        editor.putInt(Constants.KEY, mShowList.size() - 1);

        for (int i = 0; i < mShowList.size(); i++) {
            String key = Constants.KEY_ID + i;
            editor.putString(key, mShowList.get(i).getId());
        }
        Log.d(mShowList.toString());
        updateData();

        editor.commit();
        // Gionee <baorui><2013-06-17> modify for CR00796729 begin
        // setHasOptionsMenu(true);
        if (mShowList.size() == 0) {
            mActivity.invalidateOptionsMenu();
        }
        // Gionee <baorui><2013-06-17> modify for CR00796729 end
    }

    /**
     * parser the xml to get the worldtime message
     * 
     */
    private static List<City> getWorldTimeZones(Context context) {
        final List<City> myData = new ArrayList<City>();
        final long date = Calendar.getInstance().getTimeInMillis();
        try {
            XmlResourceParser xrp = context.getResources().getXml(
                    R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(Constants.XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addListItem(myData, id, displayName, date);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {

        } catch (java.io.IOException ioe) {

        }

        return myData;
    }

    /*
     * put the  data in the list
     * */
    private static void addListItem(List<City> myData, String id,
            String displayName, long date) {
        City city = new City();
        city.setId(id);
        city.setName(displayName);
        final TimeZone tz = TimeZone.getTimeZone(id);
        final int offset = tz.getOffset(date);
        final int p = Math.abs(offset);
        final StringBuilder name = new StringBuilder();
        name.append("GMT");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / (Constants.HOURS_1));
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);
        city.setGmt(name.toString());
        city.setOffset(offset);
        myData.add(city);
    }

    /*
     * set ListView Adapter
     */
    private void setListViewAdapter() {

        mListView.setAdapter(mListAdapter);
        setTextViewVisibility();

    }

    /*
     * register the BroadCastReceiver
     * */
    private void registerAdapter() {

        mShowList = WorldTimeUtils.getmWorldTimeShowList();
        mListAdapter = new AuroraWorldTimeListAdapter(
                WorldTimeFragment.this.getActivity(), mLayoutInflater, 0,
                mShowList);
        mListAdapter.registerReceiverForTime();
        worldtimeFragmentRegisterReceiverForTime();
    }

    
    /*
     * no city isVisibility
     * */
    private void setTextViewVisibility() {
        if (mShowList.size() != 0) {
            noWorldtimeView.setVisibility(View.GONE);
            dividerTextView.setVisibility(View.VISIBLE);
            // Gionee baorui 2012-12-19 modify for CR00733082 begin
            // mSortCity.setEnabled(true);
            // Gionee baorui 2012-12-19 modify for CR00733082 end
        } else {
            noWorldtimeView.setVisibility(View.VISIBLE);
            dividerTextView.setVisibility(View.GONE);
            // Gionee baorui 2012-12-19 modify for CR00733082 begin
            // mSortCity.setEnabled(false);
            // Gionee baorui 2012-12-19 modify for CR00733082 end
        }
    }

    @Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}

	/*
     * be used for updating the data of adapter
     * */
    private void updateData() {
        mShowList = WorldTimeUtils.getmWorldTimeShowList();
        mListAdapter.updateWorldTiemDada(mShowList);
        setTextViewVisibility();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deskClockApplication = (DeskClockApplication) mActivity.getApplication();
        mSearchList = getWorldTimeZones(mActivity);
        WorldTimeUtils.setmWorldTimeSearchList(mSearchList);

        mLayoutInflater = LayoutInflater.from(mActivity);
        mSharedPreferences = mActivity.getSharedPreferences(Constants.CITY_NAME,
                AuroraActivity.MODE_PRIVATE);
        // Gionee <baorui><2013-03-23> modify for CR00783443 begin
        gnAddDefaultCity();
        // Gionee <baorui><2013-03-23> modify for CR00783443 end
        KEY = mSharedPreferences.getInt("key", -1);

        List<City> showList = getIdNameBySharedPreference();
        WorldTimeUtils.setmWorldTimeShowList(showList);

        registerAdapter();
        Log.d("--------------onCreate()---------------");
        
        SortSearchList( );
    }
    
    private void SortSearchList( ) {
    	isSortOver = false;
        
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//aurora add by tangjun 2014.1.4 start
				Collections.sort(mSearchList, new Comparator() {

					@Override
					public int compare(final Object lhs, final Object rhs) {
						City s1 = (City) lhs;
						City s2 = (City) rhs;
						
						return UsefulUtils.getSpell(s1.getName()).compareTo(UsefulUtils.getSpell(s2.getName()));
					}
					
				});
				//aurora add by tangjun 2014.1.4 end
				
				isSortOver = true;
			}
		}).start();

    }

    /*
     * get id from SharedPreference and get the name by id
     */
    private List<City> getIdNameBySharedPreference() {
        List<City> list = new ArrayList<City>();
        mSearchList = WorldTimeUtils.getmWorldTimeSearchList();
        if (KEY >= 0) {
            for (int i = 0; i <= KEY; i++) {
                String id = mSharedPreferences.getString(Constants.KEY_ID + i, "");
                String name = getNameById(id, mSearchList);
                String gmt = getGmtById(id, mSearchList);
                // Gionee baorui 2013-01-30 modify for CR00769144 begin
                // name = splitCityName(name);
                if (name != null) {
                    name = splitCityName(name);
                }
                // Gionee baorui 2013-01-30 modify for CR00769144 end
                City city = new City();
                city.setId(id);
                city.setName(name);
                city.setGmt(gmt);
                list.add(city);
            }
        }
        return list;
    }

    /*
     * get the name by id
     */
    private String getNameById(String id, List<City> showList) {
        String name = null;
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).getId().equals(id)) {
                name = showList.get(i).getName();
            }
        }
        return name;
    }
    
    /*
     * get the gmt by id
     */
    private String getGmtById(String id, List<City> showList) {
        String gmt = null;
        for (int i = 0; i < showList.size(); i++) {
            if (showList.get(i).getId().equals(id)) {
            	gmt = showList.get(i).getGmt();
            }
        }
        return gmt;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        mActivity = (AuroraActivity) activity;
        
        try {
        	mOnAnimationCompleteListener = (onWorldTimeAnimationCompleteListener) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement onWorldTimeAnimationCompleteListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
		
        KEY = mSharedPreferences.getInt("key", -1);
        updateData();
        Log.e("--------------onResume()---------------"); 
        //startAnalogClockAnim();
        
		//Log.e("WorldTimeFragment onResume");
		worldtimeAuroraListviewOnResume( );
    }
    
    @Override
    public void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	
    	worldtimeAuroraListviewOnPause( );
    }
    
	public AuroraListView getWorldTimeListView( ) {
		return mListView;
	}
    
	/**
	 * 必须调用auroraListview添加的函数 aurora add by tangjun 2014.2.18
	 */
	public void worldtimeAuroraListviewOnPause( ) {
		if ( mListView != null ) {
			Log.e("--------------worldtimeAuroraListviewOnPause---------------");
			mListView.auroraOnPause();
		}
	}
	
	/**
	 * 必须调用auroraListview添加的函数 aurora add by tangjun 2014.2.18
	 */
	public void worldtimeAuroraListviewOnResume( ) {
		if ( mListView != null && mShowList.size() != 0 ) {
			Log.e("--------------worldtimeAuroraListviewOnResume---------------");
			mListView.auroraOnResume();
		}
	}
    
    public void startAnalogClockAnim( ) {
    	Log.e("--------------startAnalogClockAnim()---------------"); 
    	
    	mListView.setVisibility(View.VISIBLE);
    	dividerTextView.setVisibility(View.VISIBLE);
    	mListView.clearAnimation();
    	nativetimeText.clearAnimation();
    	
    	mEnterFrameAnimation.start();

    	//Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.worldtimelistview_enter);
    	//mListView.startAnimation(animation);
    	
    	Animation animation2 = AnimationUtils.loadAnimation(mActivity, R.anim.worldtimelistviewitem_enter);
	    LayoutAnimationController lac = new LayoutAnimationController(animation2);
	    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
	    lac.setDelay(0.2f);
	    mListView.setLayoutAnimation(lac);
	    mListView.startLayoutAnimation();
    	
	    Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.nativetimetext_enter);
    	nativetimeText.startAnimation(animation);
    }
    
    public interface onWorldTimeAnimationCompleteListener {
        void onWorldTimeFragmentAnimationComplete();
    }
    
    private onWorldTimeAnimationCompleteListener mOnAnimationCompleteListener = null;
    
    public boolean endAnalogClockAnim( ) {
    	if(mActivity == null)
    	{
    		return false;
    	}
    	Log.e("--------------endAnalogClockAnim()---------------");
    	
    	if ( mEnterFrameAnimation != null ) {
    		mEnterFrameAnimation.stopHandler();
    	}
    	
    	if ( mExitFrameAnimation != null ) {
    		mExitFrameAnimation.start();
    	}
    	 //修复4429号bug
    	try {
    		
    		mListView.clearAnimation();
    		nativetimeText.clearAnimation();
    	} catch (Exception e) {
    		Log.e("-----endAnalogClockAnim NullPointerException-----------");
    		e.printStackTrace();
    		//throw new NullPointerException("NullPointerException");
    	}   
    	
    	Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.nativetimetext_exit);
    	nativetimeText.startAnimation(animation);
    	
    	//animation = AnimationUtils.loadAnimation(mActivity, R.anim.worldtimelistview_exit);
    	//mListView.startAnimation(animation);
    	animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				dividerTextView.setVisibility(View.GONE);
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub	
				mListView.clearAnimation();
				mListView.setVisibility(View.GONE);
			
			}
		});
    	
    	Animation animation2 = AnimationUtils.loadAnimation(mActivity, R.anim.worldtimelistviewitem_exit);
	    LayoutAnimationController lac = new LayoutAnimationController(animation2);
	    lac.setOrder(LayoutAnimationController.ORDER_REVERSE);
	    lac.setDelay(0.2f);
	    mListView.setLayoutAnimation(lac);
	    mListView.startLayoutAnimation();
    	
    	return true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListAdapter.unregisterReceiverForTime();
        worldtimeFragmentUnregisterReceiverForTime();
        Log.d("--------------onDestroy()---------------");
    }

    /*
     * split the city name in '()'
     */
    private String splitCityName(String name) {
        if (name.contains("(")) {
            return name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"));
        } else {
            return name;
        }

    }

    /*
     * set the city name and id to the list of GNApplication
     */
    private void addCityToList(String cityName, String id, String cityGmt) {
        City city = new City();
        city.setId(id);
        city.setName(cityName);
        city.setGmt(cityGmt);
        mShowList.add(city);
        WorldTimeUtils.setmWorldTimeShowList(mShowList);
    }

    /*
     * update the city name and id from the list of GNApplication
     */
    private void updateCityToList(String cityName, String id, String cityGmt, int position) {
        City city = new City();
        city.setId(id);
        city.setName(cityName);
        city.setGmt(cityGmt);
        mShowList.set(position, city);
        WorldTimeUtils.setmWorldTimeShowList(mShowList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.RESULT) {
            if (requestCode == Constants.TIME_ZONE_SEARCH) {
            	AlarmAddUpHelp.getInstance(mActivity).add(AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_WCLOCK);//添加一次世界时钟，添加一次
            	//add city to list and sharedPreferences
                // Gionee baorui 2013-01-30 modify for CR00769144 begin
                KEY = mSharedPreferences.getInt("key", -1);
                // Gionee baorui 2013-01-30 modify for CR00769144 end
                KEY++;
                String cityName = data.getStringExtra("name");
                String cityId = data.getStringExtra("cityId");
                String cityGmt = data.getStringExtra("gmt");
                cityName = splitCityName(cityName);
                Editor editor = mSharedPreferences.edit();
                String key = Constants.KEY_ID + KEY;
                editor.putString(key, cityId);
                editor.putInt("key", KEY);
                editor.commit();

                // Gionee <baorui><2013-06-17> modify for CR00796729 begin
                if (mShowList.size() == 0) {
                    mActivity.invalidateOptionsMenu();
                }
                // Gionee <baorui><2013-06-17> modify for CR00796729 end

                addCityToList(cityName, cityId, cityGmt);
                updateData();
            }
            if (requestCode == Constants.TIME_ZONE_UPDATE) {
            	//update city from list and sharedPreferences
                String names = data.getStringExtra("name");
                String ids = data.getStringExtra("cityId");
                String cityGmt = data.getStringExtra("gmt");
                names = splitCityName(names);
                Editor editor = mSharedPreferences.edit();
                String key = Constants.KEY_ID + UPDATE_ID;
                editor.putString(key, ids);
                editor.commit();

                // Gionee <baorui><2013-06-17> modify for CR00796729 begin
                if (mShowList.size() == 0) {
                    mActivity.invalidateOptionsMenu();
                }
                // Gionee <baorui><2013-06-17> modify for CR00796729 end

                updateCityToList(names, ids, cityGmt, UPDATE_ID);
                updateData();

            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        menu.setHeaderTitle(R.string.option);
        menu.add(0, DELETEITEM, 0, R.string.remove_alarm);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;

        if (-1 == id) {
            super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case DELETEITEM: {
                // Confirm that the alarm will be deleted.
                new AuroraAlertDialog.Builder(mActivity, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                        .setTitle(getString(R.string.delete_city))
                        .setMessage(getString(R.string.delete_the_city))
                        // .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.alarm_delete_sure, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                // Gionee <baorui><2013-05-31> modify for CR00796729 begin
                                // setHasOptionsMenu(false);
                                // Gionee <baorui><2013-05-31> modify for CR00796729 end
                                Editor editor = mSharedPreferences.edit();
                                editor.remove(Constants.KEY);
                                for (int i = 0; i < mShowList.size(); i++) {
                                    editor.remove(Constants.KEY_ID + i);
                                }
                                mShowList.remove(mShowList.get(id));

                                editor.putInt(Constants.KEY, mShowList.size() - 1);

                                for (int i = 0; i < mShowList.size(); i++) {
                                    String key = Constants.KEY_ID + i;
                                    editor.putString(key, mShowList.get(i).getId());
                                }
                                Log.d(mShowList.toString());
                                updateData();

                                editor.commit();
                                // Gionee <baorui><2013-06-17> modify for CR00796729 begin
                                // setHasOptionsMenu(true);
                                if (mShowList.size() == 0) {
                                    mActivity.invalidateOptionsMenu();
                                }
                                // Gionee <baorui><2013-06-17> modify for CR00796729 end
                            }
                        }).setNegativeButton(R.string.alarm_alert_dismiss_text, null).show();
                return true;
            }
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
    // Gionee baorui 2012-12-19 modify for CR00733082 end

    // Gionee <baorui><2013-03-23> modify for CR00783443 begin
    private void gnAddDefaultCity() {
        String path = "/data/data/com.android.deskclock/shared_prefs/city_name.xml";
        if (mXmlFile == null) {
            mXmlFile = new File(path);
        }

        if (!mXmlFile.exists()) {
            Log.d("add default city begin");
            if (KEY != -1) {
                KEY = -1;
            }

            KEY++;
            String cityId = "Asia/Shanghai";
            String key = Constants.KEY_ID + KEY;

            Editor editor = mSharedPreferences.edit();
            editor.putString(key, cityId);
            editor.putInt("key", KEY);

            KEY++;
            cityId = "America/New_York";
            key = Constants.KEY_ID + KEY;

            editor.putString(key, cityId);
            editor.putInt("key", KEY);
            
            KEY++;
            cityId = "Europe/London";
            key = Constants.KEY_ID + KEY;

            editor.putString(key, cityId);
            editor.putInt("key", KEY);
            
            KEY++;
            cityId = "Australia/Sydney";
            key = Constants.KEY_ID + KEY;

            editor.putString(key, cityId);
            editor.putInt("key", KEY);
            
            KEY++;
            cityId = "Asia/Dubai";
            key = Constants.KEY_ID + KEY;

            editor.putString(key, cityId);
            editor.putInt("key", KEY);

            editor.commit();
            Log.d("add default city end");
        }
    }
    // Gionee <baorui><2013-03-23> modify for CR00783443 end

    // Gionee <baorui><2013-05-31> modify for CR00796729 begin
    public List<City> getMShowList() {
        return mShowList;
    }
    // Gionee <baorui><2013-05-31> modify for CR00796729 end
}
