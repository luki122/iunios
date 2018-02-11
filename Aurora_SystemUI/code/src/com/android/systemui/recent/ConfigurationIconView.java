package com.android.systemui.recent;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

/**
 * [SystemUI] Support "Toolbar".
 * 用于配置快捷设置的图标，由图标和文字组成
 */
public class ConfigurationIconView extends LinearLayout {
    private static final String TAG = "ConfigurationIconView";

    private TextView mConfigName;
    private ImageView mConfigImage;
    private FrameLayout mConfigIconLayout;
	private View mClickView;
    private ImageView mSwitchIngGifView;
    
    private Context mContext;

    public ConfigurationIconView(Context context) {
        this(context, null);
    }

    public ConfigurationIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mConfigName = (TextView) findViewById(R.id.config_name);
        mConfigImage = (ImageView) findViewById(R.id.config_icon);
        mConfigIconLayout = (FrameLayout) findViewById(R.id.config_icon_view);
		mClickView =  findViewById(R.id.click_view);
    }

    public void setClickListener(View.OnClickListener l) {
        if (l != null) {
        	mClickView.setOnClickListener(l);
        }
    }

    public void setLongClickListener(View.OnLongClickListener l){
        if (l != null) {
            mClickView.setOnLongClickListener(l);
        }
    }

    public void setTagForIcon(Object obj) {
    	mConfigImage.setTag(obj);
    }
    
    public void setConfigName(int resId) {
    	mConfigName.setText(resId);
    }
    
    public void setConfigDrawable(int resId) {
    	mConfigImage.setImageResource(resId);
    }
    
    public ImageView getConfigView() {
    	return mConfigImage;
    }
    
    public TextView getConfigNameView(){
        return mConfigName;
    }
    
    private void initSwitchingGifView() {
    	if (mSwitchIngGifView == null) {
    		ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mSwitchIngGifView = new ImageView(mContext);
            //mSwitchIngGifView.setBackgroundResource(R.drawable.panel_switch_btn);
            mConfigIconLayout.addView(mSwitchIngGifView, 0);
            mSwitchIngGifView.setVisibility(GONE);
    	}
    }
    
    public void enlargeTouchRegion() {
    	Rect bounds = new Rect();
    	bounds.left = 0;
        bounds.right = this.getMeasuredWidth();
        bounds.top = 0;
        bounds.bottom = this.getMeasuredHeight();
        TouchDelegate touchDelegate = new TouchDelegate(bounds, mConfigImage);
        ConfigurationIconView.this.setTouchDelegate(touchDelegate);
    }
    
    public ImageView getSwitchingGifView() {
    	if (mSwitchIngGifView == null) {
    		initSwitchingGifView();
    	}
    	return mSwitchIngGifView;
    }
}