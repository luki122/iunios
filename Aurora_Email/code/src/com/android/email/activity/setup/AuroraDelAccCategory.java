package com.android.email.activity.setup;
import com.android.email.R;
import android.widget.Button;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import aurora.preference.AuroraPreferenceCategory;
import android.util.Log;

public class AuroraDelAccCategory extends AuroraPreferenceCategory {

    private Button mDelButton = null;
	private OnViewClickListener mOnViewClickListener = null;
	public interface OnViewClickListener {
		void onViewClick();
	}

	public void setClickListener(OnViewClickListener l){
		mOnViewClickListener = l;
	}

    public AuroraDelAccCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AuroraDelAccCategory(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceCategoryStyle);
    }

    public AuroraDelAccCategory(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

  
    	mDelButton = (Button)view.findViewById(R.id.deleteAccount);
		if(null != mDelButton){
			mDelButton.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					if(null != mOnViewClickListener) mOnViewClickListener.onViewClick();
				}
			});
		}

    }


}
