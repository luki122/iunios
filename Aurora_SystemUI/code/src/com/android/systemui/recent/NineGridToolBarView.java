package com.android.systemui.recent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.recent.ToolBarView.ActiviyCallback;
import com.android.systemui.recent.utils.Configurable;

import android.content.res.Configuration;
/**
 * [SystemUI] Support "Toolbar".
 * 滑动view，加载两页设置图标
 */
public class NineGridToolBarView extends LinearLayout {
	private NineGridQuickSettingSwitchFirstPanel mNineGridQuickSettingSwitchFirstPanel;
	private NineGridQuickSettingSwitchThirdPanel mNineGridQuickSettingSwitchThirdPanel;
	private NineGridQuickSettingSwitchSecondPanel mNineGridQuickSettingSwitchSecondPanel;
	private AuroraBrightnessView  mAuroraBrightnessView;
    private boolean mIsUpdated = false;
    
	public NineGridToolBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initToolBarMargins();
	}

	@Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mNineGridQuickSettingSwitchFirstPanel = (NineGridQuickSettingSwitchFirstPanel)findViewById(R.id.quick_setting_linefirst);
        mNineGridQuickSettingSwitchFirstPanel.setToolBar(this);
        mNineGridQuickSettingSwitchFirstPanel.buildIconViews();
        
        mNineGridQuickSettingSwitchThirdPanel = (NineGridQuickSettingSwitchThirdPanel)findViewById(R.id.quick_setting_linethree);
        mNineGridQuickSettingSwitchThirdPanel.setToolBar(this);
        mNineGridQuickSettingSwitchThirdPanel.buildIconViews();
//		mNineGridQuickSettingSwitchPanel.setSilentCallback(		new SilentCallBcak(){
//		public void setVibrateState(Context context,boolean enable){
////			mNineGridQuickSettingSwitchPanel.setVibrateState(context,enable);
//		}
//		});
        mNineGridQuickSettingSwitchSecondPanel = (NineGridQuickSettingSwitchSecondPanel)findViewById(R.id.quick_setting_linesecond);
        mNineGridQuickSettingSwitchSecondPanel.setToolBar(this);
        mNineGridQuickSettingSwitchSecondPanel.buildIconViews();
        mNineGridQuickSettingSwitchSecondPanel.setSilentCallback(		new SilentCallBcak(){
    		public void setVibrateState(Context context,boolean enable){
    			mNineGridQuickSettingSwitchSecondPanel.setVibrateState(context,enable);
    		}
    	});
        
        mAuroraBrightnessView = (AuroraBrightnessView)findViewById(R.id.quick_setting_brightness);
		reLayoutChildViews();
	}
	

	// Aurora <Steve.Tang> 2014-07-30 add method to contorl init panels. start
	public void updateViewState(boolean visiable){
		if(visiable){
			initConfigurationPanels();
			setUpdatable(true);
		} else {
			setUpdatable(false);
		}

	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		reLayoutChildViews();
	}

	private int getScreenState(){
		return getResources().getConfiguration().orientation;
	}

	private boolean isOrientationPortrait(){
		int orientation = getScreenState();
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}

	private void reLayoutChildViews(){

		int secMargin = toolBarSecondMarginTop;
		int thirdMargin = toolBarThirdMarginTop;
		int briMarginTop = toolBarBriMarginTop;

		if(!isOrientationPortrait()){
			secMargin = toolBarSecondMarginTopLand;
			thirdMargin = toolBarThirdMarginTopLand;
			briMarginTop = toolBarBriMarginTopLand;
		}

		LinearLayout.LayoutParams senondToolBarParam = (LinearLayout.LayoutParams)mNineGridQuickSettingSwitchSecondPanel.getLayoutParams();
		senondToolBarParam.setMargins(0, secMargin, 0, 0);
		mNineGridQuickSettingSwitchSecondPanel.setLayoutParams(senondToolBarParam);

		LinearLayout.LayoutParams thirdToolBarParam = (LinearLayout.LayoutParams)mNineGridQuickSettingSwitchThirdPanel.getLayoutParams();
		thirdToolBarParam.setMargins(0, thirdMargin, 0, 0);
		mNineGridQuickSettingSwitchThirdPanel.setLayoutParams(thirdToolBarParam);

		LinearLayout.LayoutParams briToolBarParam = (LinearLayout.LayoutParams)mAuroraBrightnessView.getLayoutParams();
		briToolBarParam.setMargins(0, briMarginTop, 0, 0);
		mAuroraBrightnessView.setLayoutParams(briToolBarParam);
	}

	private int toolBarSecondMarginTop;
	private int toolBarThirdMarginTop;
	private int toolBarBriMarginTop;

	private int toolBarSecondMarginTopLand;
	private int toolBarThirdMarginTopLand;
	private int toolBarBriMarginTopLand;

	private void initToolBarMargins(){
		toolBarSecondMarginTop = getResources().getDimensionPixelSize(R.dimen.quick_settings_nine_two_margintop);
		toolBarThirdMarginTop = getResources().getDimensionPixelSize(R.dimen.quick_settings_nine_three_margintop);
		toolBarBriMarginTop = getResources().getDimensionPixelSize(R.dimen.quick_settings_nine_bri_margintop);

		toolBarSecondMarginTopLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_nine_two_margintop_land);
		toolBarThirdMarginTopLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_nine_three_margintop_land);
		toolBarBriMarginTopLand = getResources().getDimensionPixelSize(R.dimen.quick_settings_nine_bri_margintop_land);
	}

	private void initConfigurationChildPanels() {
		initConfigurationPanels();
	}

	private void setUpdatable(boolean updatable){
		mNineGridQuickSettingSwitchFirstPanel.setUpdates(updatable);
        mNineGridQuickSettingSwitchThirdPanel.setUpdates(updatable);
        mNineGridQuickSettingSwitchSecondPanel.setUpdates(updatable);
	}
	// Aurora <Steve.Tang> 2014-07-30 add method to contorl init panels. end

	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        /*postDelayed(new Runnable() {
            @Override
            public void run() {
                setUpdate();
                mQuickSettingSwitchPanel.setUpdates(true);
                mQuickSettingSwitchSecondPanel.setUpdates(true);
                initConfigurationPanels();
            }
        }, 200);*/
        mNineGridQuickSettingSwitchFirstPanel.setUpdates(true);
        mNineGridQuickSettingSwitchThirdPanel.setUpdates(true);
        mNineGridQuickSettingSwitchSecondPanel.setUpdates(true);
       
        initConfigurationPanels();
    }
	
	private void initConfigurationPanels() {
        for (int i = 0; i < this.getChildCount(); i++) {
            ViewGroup childView = (ViewGroup) this.getChildAt(i);
            if (childView instanceof Configurable) {
                ((Configurable) childView).initConfigurationState();
				// Aurora <Steve.Tang> 2014-08-04 also update config name view as panel init. start
				((Configurable) childView).updateResources();
				// Aurora <Steve.Tang> 2014-08-04 also update config name view as panel init. end
            }
        }
        this.invalidate();
    }
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		/*postDelayed(new Runnable() {
            @Override
            public void run() {
            	mQuickSettingSwitchPanel.setUpdates(false);
            	mQuickSettingSwitchSecondPanel.setUpdates(false);
            }
        }, 200);*/
		mNineGridQuickSettingSwitchFirstPanel.setUpdates(false);
		mNineGridQuickSettingSwitchThirdPanel.setUpdates(false);
		mNineGridQuickSettingSwitchSecondPanel.setUpdates(false);
		
	}
	
	public void updateResources(){
        if(!mIsUpdated){
              return;
        }    
        mNineGridQuickSettingSwitchFirstPanel.updateResources();
        mNineGridQuickSettingSwitchThirdPanel.updateResources();
        mNineGridQuickSettingSwitchSecondPanel.updateResources();
        
    }
	
	interface SilentCallBcak{
		public void setVibrateState(Context context,boolean enable);
	}
	
	interface ActiviyCallback{
		public void finishActivity();
	}
	
	public void enlargeTouchRegion() {
		mNineGridQuickSettingSwitchFirstPanel.enlargeTouchRegion();
		mNineGridQuickSettingSwitchThirdPanel.enlargeTouchRegion();
		mNineGridQuickSettingSwitchSecondPanel.enlargeTouchRegion();
    }
	
	public void setActivityCB(ActiviyCallback cb){
		if(null != mNineGridQuickSettingSwitchFirstPanel) mNineGridQuickSettingSwitchFirstPanel.setActiviyCallback(cb);
		if(null != mNineGridQuickSettingSwitchThirdPanel) mNineGridQuickSettingSwitchThirdPanel.setActiviyCallback(cb);
		if(null != mNineGridQuickSettingSwitchSecondPanel) mNineGridQuickSettingSwitchSecondPanel.setActiviyCallback(cb);
//		if(null != mAuroraBrightnessView) mAuroraBrightnessView.setActiviyCallback(cb);
	}
	
}
