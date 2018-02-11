package com.android.settings.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import aurora.preference.AuroraSwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import aurora.widget.AuroraSwitch;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


import com.android.settings.R;

public class GnSwitchPreference extends AuroraSwitchPreference {

    private int mUid;
    /**
     * Construct a new AuroraSwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     * @param defStyle Theme attribute defining the default style options
     */
    public GnSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUi();
    }

    /**
     * Construct a new AuroraSwitchPreference with the given style options.
     *
     * @param context The Context that will style this preference
     * @param attrs Style attributes that differ from the default
     */
    public GnSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUi();
    }

    /**
     * Construct a new AuroraSwitchPreference with default style options.
     *
     * @param context The Context that will style this preference
     */
    public GnSwitchPreference(Context context) {
        super(context, null);
        initUi();
    }

    // qy add 
    private void initUi(){
    	setLayoutResource(R.layout.aurora_push_notify_preference_layout);
   
        mUid = 0;
    }
    
    @Override
    protected void onBindView(View view) {
        // Clean listener before invoke AuroraSwitchPreference.onBindView
//        ViewGroup viewGroup= (ViewGroup)view; // del test
//        clearListenerInViewGroup(viewGroup);
        super.onBindView(view);
        
        /* Original Android Code has already disabled image view,but its drawable 
         * seems not disabled,so set an alpha for drawable,make it seems disabled 
         *  */
        // add 2014 04 15
        ImageView imageView = null;
        if(view.getTag() == null){
        	imageView = (ImageView) view.findViewById(android.R.id.icon);
        	view.setTag(imageView);
        }else{
        	imageView = (ImageView)view.getTag();
        }
        
//        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        Drawable icon = imageView.getDrawable();
        if(icon != null)
        {
        	int alpha = imageView.isEnabled() ? 0xFF : 0xAF;
        	icon.setAlpha(alpha);
        }
           
    }

    /**
     * Clear listener in AuroraSwitch for specify ViewGroup.
     *
     * @param viewGroup The ViewGroup that will need to clear the listener.
     */
    private void clearListenerInViewGroup(ViewGroup viewGroup) {
        if (null == viewGroup) {
            return;
        }

        int count = viewGroup.getChildCount();
        for(int n = 0; n < count; ++n) {
            View childView = viewGroup.getChildAt(n);
            if(childView instanceof AuroraSwitch) {
                final AuroraSwitch switchView = (AuroraSwitch) childView;
                switchView.setOnCheckedChangeListener(null);
                return;
            } else if (childView instanceof ViewGroup){
                ViewGroup childGroup = (ViewGroup)childView;
                clearListenerInViewGroup(childGroup);
            }
        }
    }

    public void setPreferenceUid(int uid) {
        mUid = uid;
    }

    public int getPreferenceUid() {
        return mUid;
    }
}
