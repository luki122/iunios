/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.util.HapticFeedback;

import android.app.StatusBarManager;

import com.android.contacts.ContactsApplication;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2.IAuroraDialpadFragment;
import com.android.contacts.dialpad.AuroraDialpadFragmentV2.updateDiapadButtonListener;
import com.android.contacts.list.OnContactsUnavailableActionListener;
import com.android.contacts.list.ProviderStatusLoader;
import com.android.contacts.list.ProviderStatusLoader.ProviderStatusListener;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.widget.AuroraTabHost;
import com.android.contacts.widget.AuroraTabHost.TabChangeAnimation;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.ContactsFeatureConstants;

import gionee.app.GnStatusBarManager;
// import gionee.provider.GnSettings;

import aurora.app.AuroraActivity;
import aurora.provider.AuroraSettings;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraMenu;
import android.app.Notification;
import android.app.NotificationManager;

//aurora change liguangyu 20131113 for #864 start
public class AuroraDialtactsActivityV2  extends AuroraActivity implements updateDiapadButtonListener {
//aurora change liguangyu 20131113 for #864 end
    //aurora modify liguangyu 20140106 for bug #1746 start
    private static final int FLING_MIN_VERTICAL_DISTANCE = 150;//350;
    //aurora modify liguangyu 20140106 for bug #1746 end
    private static final int FLING_MAX_HORIZONTAL_DISTANCE = 160;
    
    //Gionee:huangzy 20121023 add for CR00686812 start
    private static final int FLING_MAX_VERTICAL_DISTANCE = 160;
    private static final int FLING_MIN_HORIZONTAL_DISTANCE = 120;
    //Gionee:huangzy 20121023 add for CR00686812 end
    
    /**
     * Copied from PhoneApp. See comments in Phone app for more detail.
     */
    public static final String EXTRA_CALL_ORIGIN = "com.android.phone.CALL_ORIGIN";
    public static final String CALL_ORIGIN_DIALTACTS = "com.android.contacts.activities.DialtactsActivity";
    
    private AuroraDialpadFragmentV2 mGnDialpadFragment;
    private boolean mIsPressing = false;

   // aurora change zhouxiaobing 20130918 start
    private ImageView button2;
    private AuroraDialActivityV1 parent;
   //aurora change zhouxiaobing 20130918 end
    // gionee xuhz 20120810 add for CR00672799 start
    private HapticFeedback mHaptic = new HapticFeedback();
    // gionee xuhz 20120810 add for CR00672799 end
    
    public AuroraDialtactsActivityV2() {
    }
    
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.add(this);
        }
        
        final Intent intent = getIntent();
        fixIntent(intent);
        
        setAuroraContentView(R.layout.aurora_dialtacts_activity_layout, AuroraActionBar.Type.Empty);
    	Fragment frag = getFragmentManager().findFragmentById(R.id.dial_pad_frag);
        if (null != frag && frag instanceof AuroraDialpadFragmentV2) {
            mGnDialpadFragment = (AuroraDialpadFragmentV2) frag;
        } else if (mGnDialpadFragment == null) {
            mGnDialpadFragment = new AuroraDialpadFragmentV2();
            getFragmentManager().beginTransaction().
            	add(/*contentId*/android.R.id.content, mGnDialpadFragment).commitAllowingStateLoss();//aurora changes zhouxiaobing 20130914
        }

		mGnDialpadFragment.setIAuroraDialpadFragment(myIAuroraDialpadFragment);
		mGnDialpadFragment.initYellowPages();
        // gionee xuhz 20120810 add for CR00672799 start
        try {
            mHaptic.init(this,
                         getResources().getBoolean(R.bool.config_enable_dialer_key_vibration));
        } catch (Resources.NotFoundException nfe) {
             Log.e("AuroraDialtactsActivityV2", "Vibrate control bool missing.", nfe);
        }
        
    	if(getParent() == null) {
  	       final FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) mGnDialpadFragment.getView().getLayoutParams();
            layoutParams.bottomMargin = 0;
            mGnDialpadFragment.getView().setLayoutParams(layoutParams);
    	}
        
    }
    
    private IAuroraDialpadFragment myIAuroraDialpadFragment = new IAuroraDialpadFragment() {
		
		@Override
		public Uri getReallyUri() {
			// TODO Auto-generated method stub
			parent=(AuroraDialActivityV1)getParent();	
			if(parent != null) {
				return parent.getReallyUri();
			} else {
				return null;
			}

		}
	};

    @Override
    protected void onResume() {
        super.onResume();
        
        
        mHaptic.checkSystemSetting();
        
        // aurora change zhouxiaobing 20130918 start
        parent=(AuroraDialActivityV1)this.getParent();
        if(button2!=null){
			if(ContactsApplication.sIsAuroraYuloreSupport){
	        	if(!mGnDialpadFragment.isYellowPagesShowing()){
	        		 button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);
	        	}else{
	        		 button2.setImageResource(R.drawable.aurora_tab_dial_upx);
	        	}
			} else {
	        	if(!mGnDialpadFragment.isDialpadShowing()){
	        		 button2.setImageResource(R.drawable.aurora_tab_dial_upx);
	        	}else{
	        		 button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
	        	}
			}
        }
		if (parent != null && button2 == null) {
			button2 = parent.getTabsicon();
			if (button2 != null) {				
				button2.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						TabHost th = parent.getTabHost();
						if (th.getCurrentTab() == 1) {
							if(ContactsApplication.sIsAuroraYuloreSupport){
								if (!mGnDialpadFragment.isYellowPagesShowing()) {
									mGnDialpadFragment.showYellowPages( true);
									if (button2 != null)
									    button2.setImageResource(R.drawable.aurora_tab_dial_upx);
								} else {
									mGnDialpadFragment.hideYellowPages( true);
									if (button2 != null)
									    button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);
								}
							} else {
								if (!mGnDialpadFragment.isDialpadShowing()) {
									mGnDialpadFragment.showDialpad(true, true);
									if (button2 != null)
									    button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
								} else {
									mGnDialpadFragment.showDialpad(false, true);
									if (button2 != null)
									    button2.setImageResource(R.drawable.aurora_tab_dial_upx);
								}
							}
						} else {
							th.setCurrentTab(1);
						}
					}
				});
			}
		}
        // aurora change zhouxiaobing 20130918 end

		SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_WHITE, this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause()");
        
    }
    
    @Override
    	protected void onStop() {
    		// TODO Auto-generated method stub
    		super.onStop();
    		System.out.println("onStop()");
    	}
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.remove(this);
        }    
    }
    
    @Override
    protected void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        fixIntent(newIntent);
        
        if (mGnDialpadFragment != null) {
            mGnDialpadFragment.configureScreenFromIntent(newIntent);
        }
    }
    
    private void fixIntent(Intent intent) {
        // This should be cleaned up: the call key used to send an Intent
        // that just said to go to the recent calls list.  It now sends this
        // abstract action, but this class hasn't been rewritten to deal with it.
        if (Intent.ACTION_CALL_BUTTON.equals(intent.getAction())) {
            intent.setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE);
            intent.putExtra("call_key", true);
            setIntent(intent);
        }
    }
    
    private GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
        
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
        	if(ContactsApplication.sIsAuroraYuloreSupport){
        		
        	} else {
            	//Gionee:huangzy 20130319 modify for CR00786102 start
            	//Gionee:huangzy 20120727 modify for CR00652430 start
                if (null != mGnDialpadFragment &&
                		mGnDialpadFragment.isDialpadShowing() &&
                		mGnDialpadFragment.getDigitsTextLen() > 0) {
                	
                	Rect rect = mGnDialpadFragment.getHideDialpadTouchRect();
                	if (null != e1 && null != rect && rect.contains((int)e1.getX(), (int)e1.getY())) {
                		mGnDialpadFragment.showDialpad(false, true);
                //aurora change zhouxiaobing 20130918
                		if (button2 != null)
                		    button2.setImageResource(R.drawable.aurora_tab_dial_upx);
                		return true;
                	}
                }
            	//Gionee:huangzy 20120727 modify for CR00652430 end
                //Gionee:huangzy 20130319 modify for CR00786102 end
        	}
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
        	if(ContactsApplication.sIsAuroraYuloreSupport){
        		
        	} else {
                if (null != mGnDialpadFragment) {
                	//Gionee:xuhz 20121014 add for CR00686812 start
                    if (null != e1 && null != e2) {
                    	Rect rect = mGnDialpadFragment.getKeyBoardFlipperRect();
                        boolean isDialpadShowing = mGnDialpadFragment.isDialpadShowing();
                        int x1 = (int)e1.getX();
                        int y1 = (int)e1.getY();
                        int x2 = (int)e2.getX();
                        int y2 = (int)e2.getY();
                        int distanceX = x2 - x1;
                        int distanceY = y2 - y1;
                        
                        if (ContactsApplication.sIsGnQwertDialpadSupport) {
                            if (Math.abs(distanceY) < FLING_MAX_VERTICAL_DISTANCE &&
                            		rect.contains(x1, y1) && rect.contains(x2, y2)) {
                            	if (isDialpadShowing) {
                                	if (distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
                                        return true;
                                    } else if (-distanceX > FLING_MIN_HORIZONTAL_DISTANCE) {
                                        return true;
                                    }
                            	}
                            }
                        }
                        
                        rect = mGnDialpadFragment.getDialpadFlipperRect();
                        if (Math.abs(distanceX) < FLING_MAX_HORIZONTAL_DISTANCE/* && rect.contains(x1, y1)*/) {//aurora change zhouxiaobing 20130918
                            if (Math.abs(distanceY) > FLING_MIN_VERTICAL_DISTANCE && isDialpadShowing
                                    && mGnDialpadFragment.getDigitsTextLen() > 0) {
                                mGnDialpadFragment.showDialpad(false, true);
                                if (button2 != null)
                                    button2.setImageResource(R.drawable.aurora_tab_dial_upx);//aurora change zhouxiaobing 20130918
                                return true;
                            }
                        }
                    }
                    //Gionee:xuhz 20121014 add for CR00686812 end
            	}
        	}
            
            return false;
        }
    });
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = mGnDialpadFragment.onKeyDown(keyCode, event);
        if (handled) {
            return true;
        }
        
        //aurora change liguangyu 201311128 start
        if (keyCode == KeyEvent.KEYCODE_MENU) {
    	  	if (event.getSource() != 99) {
                addMenu(AuroraMenu.FIRST, getString(R.string.call_settings), new OnMenuItemClickLisener() {
                    public void onItemClick(View menu) {
                    	startActivity(DialtactsActivity.getCallSettingsIntent());
                    }
                });
                
                addMenu(AuroraMenu.FIRST + 1, getString(R.string.aurora_call_record), new OnMenuItemClickLisener() {
                    public void onItemClick(View menu) {
                        startActivity(new Intent(ContactsApplication.getInstance().getApplicationContext(), AuroraCallRecordActivity.class));
                    }
                });
                
                removeMenuById(AuroraMenu.FIRST + 2);
        	} else {	
                addMenu(AuroraMenu.FIRST, getString(R.string.menu_newContact), new OnMenuItemClickLisener() {
                    public void onItemClick(View menu) {
                    	startActivity(IntentFactory.newCreateContactIntent(mGnDialpadFragment.getDigitsText()));
                    }
                });
                addMenu(AuroraMenu.FIRST + 1, getString(R.string.aurora_menu_add_exist_contact), new OnMenuItemClickLisener() {
                    public void onItemClick(View menu) {
                    	startActivity(IntentFactory.newInsert2ExistContactIntent(mGnDialpadFragment.getDigitsText()));
                    }
                });
        	}
        	showCustomMenu();
    	  	return true;
        }
        //aurora change liguangyu 201311128 end 
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean isGestureDetected = mGestureDetector.onTouchEvent(ev); 
        if(isGestureDetected) {
            ev.setAction(MotionEvent.ACTION_CANCEL);
        }
        
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mIsPressing = true;
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mIsPressing = false;
            break;

        default:
            break;
        }
        
        return super.dispatchTouchEvent(ev);
    }
    
    public boolean isPressing() {
        return mIsPressing;
    }
    
    
    public void showDialpad(boolean show, boolean withAnim) {
        if (null != mGnDialpadFragment) {
            mGnDialpadFragment.showDialpad(show, withAnim);
        }
    }
    
    public void showAllDialpad(boolean show) {
        if (null != mGnDialpadFragment) {
            mGnDialpadFragment.showAllDialpad(show);
        }
    }
    
    public boolean isDialpadShowing() {
        return (null != mGnDialpadFragment) ?
            mGnDialpadFragment.isDialpadShowing() : false;
    }
    
    
    //aurora add liguangyu 20131113 for #864 start
    public void updateDialpadButton(boolean show) {
    	if (button2 != null) {
    		if(ContactsApplication.sIsAuroraYuloreSupport){
        		if (show) {
        			button2.setImageResource(R.drawable.aurora_tab_dial_upx);				
        		} else {
             		 button2.setImageResource(R.drawable.aurora_tab_yulore_normalx);		
        		}
    		} else {
        		if (show) {
        			button2.setImageResource(R.drawable.aurora_tab_dial_normalx);
        		} else {
        			button2.setImageResource(R.drawable.aurora_tab_dial_upx);						
        		}
    		}
    	}
    }
    //aurora add liguangyu 20131113 for #864 end
    
	
    public void animationBeforeSetTab() {
    	mGnDialpadFragment.animationBeforeSetTab();
    }
    
    public void animationAfterSetTab() {
    	mGnDialpadFragment.animationAfterSetTab();
    }
    
	@Override
	  public void onBackPressed() {
		 AuroraDialActivityV1 activityparent = (AuroraDialActivityV1) getParent();
	   	 if(activityparent != null) {
//	   		 activityparent.moveTaskToBack(true);
	   		 super.onBackPressed();
	   	 } else {
//		     super.onBackPressed();
	   		 finish();
	     	 overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
	   	 }
	  }
}
