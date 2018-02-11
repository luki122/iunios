/**
 * Vulcan created this file in 2014年10月9日 上午10:27:38 .
 */
package com.privacymanage.activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.privacymanage.utils.LogUtils;
import com.privacymanage.view.UserGuidePager;
import com.privacymanage.view.ViewPager.OnPageChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author vulcan
 */
public class UserGuide extends FounderPage implements OnPageChangeListener {
    public static final boolean FLYING_ENABLE = true;
    public static final String LAUNCH_MODE_KEY = "arg0";
    public static final int LAUNCH_MODE_HELP = 0;
    public static final int LAUNCH_MODE_CREATE_DONE = 1;
    public static final int LAUNCH_MODE_SHOW_PAGE_HOW_EXIT = 2;

    public static int PAGE_INDEX_HOW_ENTER = 0;
    public static int PAGE_INDEX_HOW_PROTECT = 1;
    public static int PAGE_INDEX_HOW_EXIT = 2;
    public static int PAGE_INDEX_HOW_DELETE = 3;
    public static int PAGE_INDEX_HOW_GETBACK_PASSWORD = 4;
    public static final int PAGE_TOTAL = 5;

    public static final int PAGE_VELOCITY = 10;

    //private boolean mEnableMannualAnimator = false;
    //public  static boolean mMannualAnimatorSwitch = false;


    private Handler mMainHandler = new Handler();
    private int mCurrentPage = 0;
    private float mOffsetInCurPage = 0.0f;
    private boolean nextButtonFlag = true;

    //all the fragments in the help page
    protected final Fragment[] mFragmentListHelpPage = {
            new HowEnterFrag(),
            new HowProtectFrag(),
            new HowExitFrag(),
            new HowDeleteFrag(),
            new HowGetbackFrag()
    };

    protected final Fragment[] mFragmentListCreateDone = {
            new HowProtectFrag(),
            new HowEnterFrag(),
            new HowExitFrag()
    };
    protected Fragment mUserGuideFragment[] = mFragmentListHelpPage;

    final PageOffsetDetector mPageOffsetDetector = new PageOffsetDetector();
    protected PageIndicator mPageIndicator = null;

    //default type is LAUNCH_MODE_HELP
    protected int mLaunchMode = LAUNCH_MODE_HELP;


    //timer used to animations
    final Timer mCinemaTimer = new Timer();
    final TimerTask mCinemaTimerTask = new TimerTask() {

        @Override
        public void run() {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCinemaMan.onCinemaTick();
                }
            });
        }
    };

    /**
     *
     */
    public UserGuide() {
        mLayoutResId = ResIdMan.LAYOUT_FILE_USER_GUIDE;
        mActionBarIsEmpty = true;
    }

    /**
     * Vulcan created this method in 2014年10月25日 下午4:48:27 .
     *
     * @param firstPageIndex
     * @param number
     */
    protected void showFragment(int pageIndex) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.show(mUserGuideFragment[pageIndex]);
        ft.commit();
        return;
    }

    /**
     * Vulcan created this method in 2014年10月27日 上午9:23:44 .
     *
     * @param pageIndex
     */
    protected void addNShowFragment(int pageIndex) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(mUserGuideFragment[pageIndex], mUserGuideFragment[pageIndex].toString());
        ft.show(mUserGuideFragment[pageIndex]);
        ft.commit();
        return;
    }

    /**
     * Vulcan created this method in 2014年10月27日 上午9:24:15 .
     *
     * @param pageIndex
     */
    protected void removeFragment(int pageIndex) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(mUserGuideFragment[pageIndex]);
        ft.commit();
        return;
    }

    /**
     * Vulcan created this method in 2014年10月25日 下午5:02:47 .
     *
     * @param pageIndex
     */
    protected void hideFragment(int pageIndex) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(mUserGuideFragment[pageIndex]);
        ft.commit();
        return;
    }

    /**
     * Vulcan created this method in 2014年10月11日 上午11:54:13 .
     */
    protected void startPrivacySpace() {
        LogUtils.printWithLogCat("vbtnanim", "stack = " + LogUtils.StackToString(new Throwable()));
        UnderlyingWrapper.openPrivacyAccount(AccountCreateInfo.mAccountRef);
        Intent intentPrivacyManageActivity = new Intent(this, PrivacyManageActivity.class);
        startActivity(intentPrivacyManageActivity);
        return;
    }

    /**
     * Vulcan created this method in 2014年12月16日 下午4:56:36 .
     */
    protected void startDialPad() {
        Intent intentDialPad = new Intent();
        intentDialPad.setAction(Intent.ACTION_DIAL);
        intentDialPad.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentDialPad.setClassName("com.android.contacts", "com.android.contacts.activities.AuroraDialActivity");
        startActivity(intentDialPad);
        overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter, com.aurora.R.anim.aurora_activity_open_exit);
        //overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter,com.aurora.R.anim.aurora_activity_close_exit);
        return;
    }

    public class MyPagerAdapter extends PagerAdapter {

        List<View> mPageList = new ArrayList<View>();

        /**
         * Vulcan created this method in 2014年10月23日 上午10:35:13 .
         *
         * @param pageList
         */
        void createHelpPages(List<View> pageList) {
            pageList.clear();

            //page how enter
            View v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_NULL, null);
            pageList.add(v);

            //page how protect
            v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_NULL, null);
            pageList.add(v);

            // page how exit
            v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_HOW_EXIT, null);
            pageList.add(v);

            //page how delete
            v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_NULL, null);
            pageList.add(v);

            //page how get back password
            v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_NULL, null);
            pageList.add(v);
        }

        /**
         * Vulcan created this method in 2014年10月23日 上午10:36:07 .
         *
         * @param pageList
         */
        void createDonePages(List<View> pageList) {
            pageList.clear();

            //page how enter
            View v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_NULL, null);
            pageList.add(v);

            //page how protect
            v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_NULL, null);
            pageList.add(v);

            //page how exit
            v = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_VIEW_PAGE_HOW_EXIT, null);
            pageList.add(v);
        }

        public MyPagerAdapter() {
            mLaunchMode = loadCallerType();

            if (mLaunchMode == LAUNCH_MODE_HELP) {
                createHelpPages(mPageList);
            } else {
                createDonePages(mPageList);
            }
        }

        @Override
        public int getCount() {                                                                 //获得size
            return mPageList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View view, int position, Object object)                       //销毁Item
        {
            hideFragment(position);
            ((UserGuidePager) view).removeView(mPageList.get(position));
        }

        @Override
        public Object instantiateItem(View view, int position)                                //实例化Item
        {
            showFragment(position);

            ((UserGuidePager) view).addView(mPageList.get(position), 0);

            return mPageList.get(position);
        }

    }

    /**
     * Vulcan created this method in 2014年10月22日 上午11:30:44 .
     */
    protected void setupAnimationFrag(int launchMode) {

        //default is help page
        mUserGuideFragment = mFragmentListHelpPage;
        if (launchMode == LAUNCH_MODE_CREATE_DONE
                || launchMode == LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
            mUserGuideFragment = mFragmentListCreateDone;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        for (int nn = 0; nn < this.mUserGuideFragment.length; nn++) {
            ft.add(ResIdMan.LAYOUT_VIEW_ANIMATIONS_FRAMELAYOUT, mUserGuideFragment[nn]);
            //ft.show(mUserGuideFragment[nn]);
        }
        ft.commit();
    }


    /**
     * Vulcan created this method in 2014年10月24日 下午6:17:01 .
     *
     * @param callerType
     * @return
     */
    long getLastPageIndex(int callerType) {
        if (callerType == LAUNCH_MODE_HELP) {
            return 4;
        } else {
            return 2;
        }
    }

    /**
     * Vulcan created this method in 2014年10月24日 下午6:17:25 .
     *
     * @return
     */
    int getFirstPageIndex() {
        return 0;
    }

    /**
     * Vulcan created this method in 2014年10月23日 上午10:47:49 .
     */
    int loadCallerType() {
        int launchMode = LAUNCH_MODE_HELP;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            launchMode = bundle.getInt(LAUNCH_MODE_KEY, LAUNCH_MODE_HELP);
        }
        return launchMode;
    }

    /* (non-Javadoc)
     * @see aurora.app.AuroraActivity#onCreate(android.os.Bundle)
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //setContentView(ResIdMan.LAYOUT_FILE_USER_GUIDE);

        int loadedLaunchMode = loadCallerType();

        mLaunchMode = loadedLaunchMode;

        LogUtils.printWithLogCat("vma", "onCreate: mLaunchMode = " + mLaunchMode);

        setupButtonsListener(loadedLaunchMode);

        loadPageIndex(loadedLaunchMode);

        //mPageMainCinema = createPageMainCinema(mLaunchMode);
        //mPageTextInCinema = createPageTextInCinema(mLaunchMode);
        //mPageTextOutCinema = createPageTextOutCinema(mLaunchMode);

        setupAnimationFrag(loadedLaunchMode);

        setupUserGuidePager(loadedLaunchMode);

        setupPageIndicator(loadedLaunchMode);

        mCinemaTimer.scheduleAtFixedRate(mCinemaTimerTask, 0, Cinema.CINEMA_FRAME_PERIOD);
        //mCinemaTimer.schedule(mCinemaTimerTask, 0, Cinema.CINEMA_FRAME_PERIOD);

        //super.onCreate(savedInstanceState);

/*		new Handler().postDelayed(new Runnable() {
            public void run() {
				if (!mOnLayoutCreatedCalled) {
					onLayoutCreated();
					mOnLayoutCreatedCalled = true;
				}
			}
		}, 300);*/

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        if (UserGuidePager.SCROLL_STATE_DRAGGING == arg0) {
            LogUtils.printWithLogCat("ctime", "onPageScrollStateChanged: SCROLL_STATE_DRAGGING " + mCurrentPage);
        } else if (UserGuidePager.SCROLL_STATE_IDLE == arg0) {
            LogUtils.printWithLogCat("ctime", "onPageScrollStateChanged: SCROLL_STATE_IDLE" + mCurrentPage);
        } else if (UserGuidePager.SCROLL_STATE_SETTLING == arg0) {
            LogUtils.printWithLogCat("ctime", "onPageScrollStateChanged: SCROLL_STATE_SETTLING" + mCurrentPage);
        }

		/*
        if (mLaunchMode == LAUNCH_MODE_CREATE_DONE
				&& UserGuidePager.SCROLL_STATE_DRAGGING == arg0
				&& (mCurrentPage == PAGE_INDEX_HOW_EXIT || mCurrentPage == PAGE_INDEX_HOW_PROTECT)) {
			showButtonToPrivacySpace();
		}
		
		if (mLaunchMode == LAUNCH_MODE_CREATE_DONE
				&& UserGuidePager.SCROLL_STATE_IDLE == arg0
				&& mCurrentPage == PAGE_INDEX_HOW_EXIT) {
			enableButtonToPrivacySpace();
		}

		if (UserGuidePager.SCROLL_STATE_IDLE == arg0
				&& mCurrentPage != PAGE_INDEX_HOW_EXIT) {
			disableButtonToPrivacySpace();
		}
		*/

        return;
    }

    /**
     * Vulcan created this method in 2014年10月31日 下午3:28:14 .
     */
    public void enableButtonToPrivacySpace() {
        Button btn = (Button) findViewById(ResIdMan.BUTTON_NEXT_STEP);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startPrivacySpace();
            }

        });
    }

    /**
     * Vulcan created this method in 2014年10月31日 下午3:50:57 .
     */
    public void showButtonToPrivacySpace() {
        Button btn = (Button) findViewById(ResIdMan.BUTTON_NEXT_STEP);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(null);
    }

    /**
     * Vulcan created this method in 2014年10月31日 下午3:28:52 .
     */
    public void disableButtonToPrivacySpace() {
        Button btn = (Button) findViewById(ResIdMan.BUTTON_NEXT_STEP);
        btn.setVisibility(View.GONE);
        btn.setOnClickListener(null);
    }

    /**
     * Vulcan created this method in 2014年12月16日 下午4:20:39 .
     */
    protected void setEnableButtonNextStep(boolean shouldEnable) {
        if (shouldEnable) {
            Button btn = (Button) findViewById(ResIdMan.BUTTON_NEXT_STEP);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUserGuidePager.setCurrentItem(PAGE_INDEX_HOW_ENTER, true, PAGE_VELOCITY);
                }
            });
            btn.setVisibility(View.VISIBLE);
            if (nextButtonFlag) {
                btn.setClickable(false);
                nextButtonFlag = false;
            }
        } else {
            Button btn = (Button) findViewById(ResIdMan.BUTTON_NEXT_STEP);
            btn.setVisibility(View.GONE);
        }
    }

    /**
     * Vulcan created this method in 2014年12月16日 下午4:33:44 .
     *
     * @param shouldEnable
     */
    protected void setEnableButtonToDialPad(boolean shouldEnable) {
        if (shouldEnable) {
            Button btn = (Button) findViewById(ResIdMan.BUTTON_TO_DIALING_PAD);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDialPad();
                }
            });
            btn.setVisibility(View.VISIBLE);
        } else {
            Button btn = (Button) findViewById(ResIdMan.BUTTON_TO_DIALING_PAD);
            btn.setVisibility(View.GONE);
        }
    }

    /**
     * Vulcan created this method in 2014年12月16日 下午5:40:41 .
     *
     * @param shouldEnable
     */
    protected void setEnableButtonISee(boolean shouldEnable) {
        if (shouldEnable) {
            Button btn = (Button) findViewById(ResIdMan.BUTTON_I_SEE);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            btn.setVisibility(View.VISIBLE);
        } else {
            Button btn = (Button) findViewById(ResIdMan.BUTTON_I_SEE);
            btn.setVisibility(View.GONE);
        }
    }


    /**
     * Vulcan created this method in 2014年10月24日 下午4:36:40 .
     *
     * @param thisPage
     * @param thisOffset
     */
    protected void continuePlayBackward(int thisPage, float thisOffset) {
        int nextPage = thisPage + 1;

        setCurrentPlayTimeInPage(thisPage, thisOffset);
        LogUtils.printWithLogCat("v10", String.format("continuePlayBackward: page = [%d][%f]", thisPage, thisOffset));

        //1.start the main Cinema of this page
        //2.text from left to center(pause other text cinema)\
        //3.hide the main cinema of next page
        mCinemaMan.startPageMainCinema(thisPage);
        mCinemaMan.startPageTextCinemaLTC(thisPage);
        mCinemaMan.hidePage(nextPage);
        return;
    }

    /**
     * Vulcan created this method in 2014年10月24日 下午4:40:02 .
     *
     * @param thisPage
     * @param thisOffset
     */
    protected void continuePlayForward(int thisPage, float thisOffset) {
        int nextPage = thisPage + 1;
        LogUtils.printWithLogCat("v10", String.format("continuePlayForward: page = [%d][%f]", thisPage, thisOffset));
        setCurrentPlayTimeInPage(thisPage, thisOffset);

        /**
         * 1.start main cinema of next page
         * 2.next page's text from right to center
         * 3.hide the main cinema of this page
         */
        mCinemaMan.startPageMainCinema(nextPage);
        mCinemaMan.startPageTextCinemaRTC(nextPage);
        mCinemaMan.hidePage(thisPage);

        return;
    }

    /**
     * Playing direction is forward if the page number increases.
     * Playing direction is backward if the page number decreases.
     * Vulcan created this method in 2014年10月24日 下午3:00:14 .
     *
     * @param thisPage
     * @param thisOffset
     */
    protected void continuePlay(int thisPage, float thisOffset, boolean forward) {
        if (forward) {
            continuePlayForward(thisPage, thisOffset);
        } else {
            continuePlayBackward(thisPage, thisOffset);
        }
    }

    /**
     * Vulcan created this method in 2014年10月22日 下午4:10:38 .
     *
     * @param page
     * @param offset_in_page
     */
    protected void setCurrentPlayTimeInPage(int page, float offset_in_page) {

        for (int p = getFirstPageIndex(); p < page; p++) {
            //mPageMainCinema[page].setCurrentPlayProgress(0);mPageMainCinema[page].pause();
            //mPageTextOutCinema[page].setCurrentPlayProgress(100);mPageTextOutCinema[page].pause();
            //mPageTextInCinema[page].setCurrentPlayProgress(0);mPageTextInCinema[page].pause();
            mCinemaMan.hidePage(p);
        }

        for (int p = page + 2; p < getLastPageIndex(mLaunchMode); p++) {
            //mPageMainCinema[page].setCurrentPlayProgress(0);mPageMainCinema[page].pause();
            //mPageTextOutCinema[page].setCurrentPlayProgress(100);mPageTextOutCinema[page].pause();
            //mPageTextInCinema[page].setCurrentPlayProgress(0);mPageTextInCinema[page].pause();
            mCinemaMan.hidePage(p);
        }


        float finalOffset1;
        float finalOffset2;
        long percent;

        LogUtils.printWithLogCat("vfling", String.format("setCurrentPlayTimeInPage: page = %d,offset = %f", page, offset_in_page));

        //exit the old animation
        if (offset_in_page >= 0.0f && offset_in_page <= 0.5f) {
            if (offset_in_page <= 0.0f) {
                finalOffset1 = 0.0f;
            } else if (offset_in_page >= 0.5f) {
                finalOffset1 = 0.5f;
            } else {
                finalOffset1 = offset_in_page;
            }
            //percent = (long)(2.0f * finalOffset1 * 100.0f);
            percent = (long) (finalOffset1 * 200.0f);
            mCinemaMan.setPageMainCinemePlayProgress(page, 100 - percent);
            mCinemaMan.setPageTextCinemaPlayProgressCTL(page, percent);
            mCinemaMan.setPageMainCinemePlayProgress(page + 1, 0);
            mCinemaMan.setPageTextCinemaPlayProgressRTC(page + 1, 0);
            mCinemaMan.hidePage(page + 1);
        } else if (offset_in_page > 0.5f && offset_in_page <= 1.0f) {
            //play the new animation
            if (offset_in_page <= 0.5f) {
                finalOffset2 = 0.5f;
            } else if (offset_in_page >= 1.0f) {
                finalOffset2 = 1.0f;
            } else {
                finalOffset2 = offset_in_page;
            }
            //1.0~0.5
            //0.0~0.5
            //percent = (long)((2.0f * finalOffset2 - 1.0f) * 100.0f);
            percent = (long) ((1.0f - finalOffset2) * 200.0f);
            mCinemaMan.setPageMainCinemePlayProgress(page + 1, 100 - percent);
            mCinemaMan.setPageTextCinemaPlayProgressCTR(page + 1, percent);
            mCinemaMan.setPageMainCinemePlayProgress(page, 0);
            mCinemaMan.setPageTextCinemaPlayProgressCTL(page, 100);
            mCinemaMan.hidePage(page);
        }


    }


    @Override
    public void onPageScrolled(int page, float offset_in_page, int arg2) {
        LogUtils.printWithLogCat("ctime", "onPageScrolled: page = " + ((float) page + offset_in_page));

        if (mLaunchMode == LAUNCH_MODE_CREATE_DONE) {
            final float exactPos = page + offset_in_page;
            if (exactPos >= 0.0f && exactPos < 0.5f) {
                setEnableButtonNextStep(true);
                setEnableButtonToDialPad(false);
                setEnableButtonISee(false);
            } else if (exactPos > 0.5f && exactPos < 1.5f) {
                setEnableButtonNextStep(false);
                setEnableButtonToDialPad(true);
                setEnableButtonISee(false);
            } else if (exactPos > 1.5f && exactPos < 2.5f) {
                setEnableButtonNextStep(false);
                setEnableButtonToDialPad(false);
                setEnableButtonISee(true);
            }
        }


        if (mCinemaMan.cinemaIsCreated() && !mCinemaMan.cinemaIsPlaying()) {
            setCurrentPlayTimeInPage(page, offset_in_page);
        } else {
            // LogUtils.printWithLogCat("vfling",
            // "onPageScrolled: mEnableMannualAnimator = " +
            // mMannualAnimatorSwitch);
        }

/*		if(mCinemaMan.cinemaIsCreated()) {
				setCurrentPlayTimeInPage(page, offset_in_page);
			}
			else {
				//LogUtils.printWithLogCat("vfling", "onPageScrolled: mEnableMannualAnimator = "  + mMannualAnimatorSwitch);
			}*/

        int bypassDirection = mPageOffsetDetector.getHalfBypassDirection(page, offset_in_page);
        if (bypassDirection == PageOffsetDetector.BYPASS_DIRECTION_RIGHT) {
            //hideFragment(page);
            //showFragment(page + 1);
            Log.d("vperform", "onPageScrolled: right");
        } else if (bypassDirection == PageOffsetDetector.BYPASS_DIRECTION_LEFT) {
            //hideFragment(page + 1);
            //showFragment(page);
            Log.d("vperform", "onPageScrolled: left");
        }
        mCurrentPage = page;
        mOffsetInCurPage = offset_in_page;
		
		

/*
		if(mEnableMannualAnimator) {
			
			//exit the old animation
			if(page == PAGE_INDEX_HOW_ENTER) {
				float finalOffset = 0;
				
				if(offset_in_page <= 0.0f) {
					finalOffset = 0.0f;
				}
				else if(offset_in_page >= 0.5f) {
					finalOffset = 0.5f;
				}
				else {
					finalOffset = offset_in_page;
				}
				
				long percent = (long)((1.0f - 2.0f * finalOffset ) * 100.0f);
				mCinemaHowEnter.setCurrentPlayProgress(percent);
				mCinemaHowEnterTextToLeft.setCurrentPlayProgress(100 - percent);
			}
			
			//play the new animation
			if(page == PAGE_INDEX_HOW_ENTER) {
				float finalOffset = 0;
				
				if(offset_in_page <= 0.5f) {
					finalOffset = 0.5f;
				}
				else if(offset_in_page >= 1.0f) {
					finalOffset = 1.0f;
				}
				else {
					finalOffset = offset_in_page;
				}
				
				long percent = (long)((2.0f * finalOffset - 1.0f) * 100.0f);
				
				mCinemaHowProtect.setCurrentPlayProgress(percent);
				mCinemaHowProtectRightTextIn.setCurrentPlayProgress(percent);
			}
		}
*/


        return;
    }

    /**
     * Vulcan created this method in 2014年10月14日 上午9:09:28 .
     */
    private void onPageHowExitSelected() {

        //AuroraButton abToPrivacySpace = (AuroraButton) findViewById(ResIdMan.BUTTON_TO_PRIVACY_SPACE);

        return;
    }

    /**
     * Vulcan created this method in 2014年10月14日 上午9:09:28 .
     */
    private void onPageHowProtectSelected() {

        return;
    }

    /**
     * Vulcan created this method in 2014年10月20日 下午1:57:43 .
     *
     * @param as
     */
    @SuppressWarnings("unused")
    private void printAnimSet(AnimationSet as) {
        ArrayList<Animation> animList = new ArrayList<Animation>();
        animList.addAll(as.getAnimations());
        for (Animation anim : animList) {
            if (anim instanceof RotateAnimation) {

                ((RotateAnimation) anim).setFillEnabled(true);
                ((RotateAnimation) anim).setFillAfter(true);
                ((RotateAnimation) anim).setFillBefore(true);

                ((RotateAnimation) anim).setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ImageView ivHandset = (ImageView) findViewById(ResIdMan.IMAGEVIEW_HANDSET);
                        ivHandset.getRotation();
                        Log.d("vanim", String.format("printAnimSet: pivotX = %f,pivotY = %f", ivHandset.getPivotX(), ivHandset.getPivotY()));

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                });
            }
        }
        return;
    }

    /**
     * Vulcan created this method in 2014年10月14日 上午9:09:28 .
     */
    private void onPageHowEnterSelected() {

        //playAnimHowEnter();
        LogUtils.printWithLogCat("vanim", String.format("onPageHowEnterSelected"));
        //mCinemaHowEnter.setCurrentPlayProgress(100);
        return;
    }

    @Override
    public void onPageSelected(int pageSelected) {
        setupButtonsListener(mLaunchMode);

        LogUtils.printWithLogCat("ctime", String.format("onPageSelected: pageSelected = %d", pageSelected));
        if (pageSelected == PAGE_INDEX_HOW_EXIT) {
            onPageHowExitSelected();
        } else if (pageSelected == PAGE_INDEX_HOW_PROTECT) {
            onPageHowProtectSelected();
        } else if (pageSelected == PAGE_INDEX_HOW_ENTER) {
            onPageHowEnterSelected();
        }
        if (mPageIndicator == null) {
            mPageIndicator = (PageIndicator) findViewById(ResIdMan.PAGEINDICATOR_FOR_USER_GUIDE);
        }
        mPageIndicator.switchTo(pageSelected);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        //ActivityMan.killAllActivities();
        //super.onBackPressed();

        if (mLaunchMode == LAUNCH_MODE_HELP
                || mLaunchMode == LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
            super.onBackPressed();
        } else if (mLaunchMode == LAUNCH_MODE_CREATE_DONE) {
            int curPage = mUserGuidePager.getCurrentItem();
            if (curPage <= 0) {
                super.onBackPressed();
            } else {
                mUserGuidePager.setCurrentItem(curPage - 1, true, PAGE_VELOCITY);
            }
        }


        return;
    }


    private class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {

            if (!FLYING_ENABLE) {
                return true;
            }

            LogUtils.printWithLogCat("vf", "onFling: velocityX = " + velocityX);
            if (velocityX < -1500 && mCurrentPage < getLastPageIndex(mLaunchMode)
                    && mOffsetInCurPage > 0.6f) {
                continuePlay(mCurrentPage, mOffsetInCurPage, true);
            } else if (velocityX > 1500 && mCurrentPage >= getFirstPageIndex()
                    && mOffsetInCurPage > 0.0f
                    && mOffsetInCurPage < 0.4f) {
                continuePlay(mCurrentPage, mOffsetInCurPage, false);
            }

            return true;
        }
    }

    private class PageOffsetDetector {
        public final static int BYPASS_DIRECTION_RIGHT = 1;
        public final static int BYPASS_DIRECTION_LEFT = -1;
        public final static int BYPASS_DIRECTION_NONE = 0;

        public boolean mSavedOffsetBeyondHalf = false;
        public int mSavedPageIndex = 0;
        //public float mSavedOffsetInPage = 0;

        public int getHalfBypassDirection(int newPage, float newOffset) {
            boolean bOffsetBeyondHalf = false;
            int bypassDirection = 0;
            if (newOffset >= 0.5f) {
                bOffsetBeyondHalf = true;
            } else {
                bOffsetBeyondHalf = false;
            }

            if (newPage != mSavedPageIndex) {
                bypassDirection = BYPASS_DIRECTION_NONE;
            } else if (mSavedOffsetBeyondHalf == bOffsetBeyondHalf) {
                bypassDirection = BYPASS_DIRECTION_NONE;
            } else if (bOffsetBeyondHalf) {
                bypassDirection = BYPASS_DIRECTION_RIGHT;
            } else {
                bypassDirection = BYPASS_DIRECTION_LEFT;
            }

            mSavedOffsetBeyondHalf = bOffsetBeyondHalf;
            mSavedPageIndex = newPage;
            //mSavedOffsetInPage = newOffset;
            return bypassDirection;
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        mCinemaTimer.cancel();
        super.onDestroy();
    }

    /**
     * Vulcan created this method in 2014年10月31日 下午5:27:39 .
     */
    @Override
    public void onLayoutCreated() {
        //1.The animation to be played should be played just after the animation between activities.
        //2.The duration of the animation between activities is 400ms. We start our animation a little before that animation ends.
        final int PLAY_DELAY_BECAUSE_NEW_ACTIVITY = 300;
        LogUtils.printWithLogCat("ctime", "onLayoutCreated: user guide's layout is ok");
        mCinemaMan.create(mLaunchMode);
        //mCinemaMan.startPageCinema(PAGE_INDEX_HOW_ENTER);

        if (mLaunchMode == LAUNCH_MODE_HELP) {
            mCinemaMan.setPageMainCinemePlayProgress(0, Cinema.PLAY_PROGRESS_MAX);
            mCinemaMan.setPageTextCinemaPlayProgress(0, Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER, 100);
            mCinemaMan.playAnimHowEnter();
        } else if (mLaunchMode == LAUNCH_MODE_CREATE_DONE) {
/*			mCinemaMan.setPageMainCinemePlayProgress(0, Cinema.PLAY_PROGRESS_MIN);
			mCinemaMan.setPageTextCinemaPlayProgressRTC(0,Cinema.PLAY_PROGRESS_MIN);
			mCinemaMan.startPageCinema(0);*/
            mCinemaMan.setPageMainCinemePlayProgress(0, Cinema.PLAY_PROGRESS_MIN);
            mCinemaMan.setPageTextCinemaPlayProgressRTC(0, Cinema.PLAY_PROGRESS_MIN);


            Runnable r = new Runnable() {
                public void run() {
                    mCinemaMan.startPageCinema(0, true);
                }
            };
            mMainHandler.postDelayed(r, PLAY_DELAY_BECAUSE_NEW_ACTIVITY);
        } else if (mLaunchMode == LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
            //mCinemaMan.setPageMainCinemePlayProgress(PAGE_INDEX_HOW_PROTECT, Cinema.PLAY_PROGRESS_MAX);
            //mCinemaMan.setPageTextCinemaPlayProgress(PAGE_INDEX_HOW_PROTECT, Cinema.TEXT_CINEMA_INDEX_RIGHT_TO_CENTER, 100);
            mCinemaMan.setPageMainCinemePlayProgress(2, Cinema.PLAY_PROGRESS_MIN);
            mCinemaMan.setPageTextCinemaPlayProgressRTC(2, Cinema.PLAY_PROGRESS_MIN);
            Runnable r = new Runnable() {
                public void run() {
                    mCinemaMan.startPageCinema(2);
                }
            };
            mMainHandler.postDelayed(r, PLAY_DELAY_BECAUSE_NEW_ACTIVITY);//The duration of the animation between activities is 400ms. We start our animation a little before that animation ends.
        }

    }

    private final CinemaMan mCinemaMan = new CinemaMan(this);

    /* (non-Javadoc)
     * @see android.app.Activity#onWindowFocusChanged(boolean)
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        LogUtils.printWithLogCat("onLayoutCreated", "onWindowFocusChanged: user guide's focus is changed");
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean isPrivateActivity() {
        if (mLaunchMode == LAUNCH_MODE_HELP
                || mLaunchMode == LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Vulcan created this method in 2014年12月16日 下午2:45:27 .
     */
    protected void loadPageIndex(int launchMode) {
        if (LAUNCH_MODE_HELP == launchMode) {
            PAGE_INDEX_HOW_ENTER = 0;
            PAGE_INDEX_HOW_PROTECT = 1;
            PAGE_INDEX_HOW_EXIT = 2;
            PAGE_INDEX_HOW_DELETE = 3;
            PAGE_INDEX_HOW_GETBACK_PASSWORD = 4;
        } else {
            PAGE_INDEX_HOW_PROTECT = 0;
            PAGE_INDEX_HOW_ENTER = 1;
            PAGE_INDEX_HOW_EXIT = 2;
        }
        return;
    }

    /**
     * Vulcan created this method in 2014年12月16日 下午4:18:39 .
     *
     * @param launchMode
     */
    protected void setupButtonsListener(int launchMode) {
        if (launchMode == LAUNCH_MODE_HELP) {
            setEnableButtonNextStep(false);
            setEnableButtonToDialPad(false);
            setEnableButtonISee(false);
        } else if (launchMode == LAUNCH_MODE_CREATE_DONE) {
            setEnableButtonNextStep(true);
            setEnableButtonToDialPad(false);
            setEnableButtonISee(false);
        } else if (launchMode == LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
            setEnableButtonNextStep(false);
            setEnableButtonToDialPad(false);
            setEnableButtonISee(true);
        } else {
            setEnableButtonNextStep(false);
            setEnableButtonToDialPad(false);
            setEnableButtonISee(false);
        }
        return;
    }

    /**
     * Vulcan created this method in 2014年12月17日 下午2:11:11 .
     *
     * @param launchMode
     */
    protected void setupPageIndicator(int launchMode) {
        mPageIndicator = (PageIndicator) findViewById(ResIdMan.PAGEINDICATOR_FOR_USER_GUIDE);
        if (mLaunchMode == LAUNCH_MODE_HELP) {
            mPageIndicator.changeLength(5);
            mPageIndicator.switchTo(0);
        } else {
            mPageIndicator.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Vulcan created this method in 2014年12月17日 下午2:12:51 .
     *
     * @param launchMode
     */
    @SuppressLint("ClickableViewAccessibility")
    protected void setupUserGuidePager(int launchMode) {
        mUserGuidePager = (UserGuidePager) findViewById(ResIdMan.VIEWPAGE_USER_GUIDE);

        final GestureDetector gdViewPager = new GestureDetector(this, new GestureListener(), new Handler());
        final UserGuidePager vpUserGuide = (UserGuidePager) findViewById(ResIdMan.VIEWPAGE_USER_GUIDE);
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter();
        vpUserGuide.setAdapter(myPagerAdapter);
        vpUserGuide.setOnPageChangeListener(this);
        vpUserGuide.setOffscreenPageLimit(1);
        vpUserGuide.setFlagEnableTouchEvent(launchMode == LAUNCH_MODE_HELP);
        vpUserGuide.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //LogUtils.printWithLogCat("vbtnanim", "onClick: test");
                gdViewPager.onTouchEvent(event);
                //Button btn = (Button)UserGuide.this.findViewById(ResIdMan.BUTTON_TO_PRIVACY_SPACE);
                //btn.dispatchTouchEvent(event);
				
/*				if((event.getAction() & MotionEventCompat.ACTION_MASK) == MotionEvent.ACTION_UP) {
					vpUserGuide.onTouchEvent(event);
					if(mOffsetInCurPage > 0.5f) {
						Log.d("vbound","onTouch: to right, next page is " + (mCurrentPage + 1));
						vpUserGuide.setCurrentItem(mCurrentPage + 1, true);
					}
					else {
						Log.d("vbound","onTouch: to left, next page is " + mCurrentPage);
						vpUserGuide.setCurrentItem(mCurrentPage, true);
					}
				}*/

                return false;
            }

        });
		
/*		if(launchMode == LAUNCH_MODE_SHOW_PAGE_HOW_EXIT) {
			vpUserGuide.setCurrentItem(PAGE_INDEX_HOW_EXIT, true, PAGE_VELOCITY);
		}*/

    }

    private UserGuidePager mUserGuidePager;

}
