package com.android.auroramusic.widget;

import com.android.auroramusic.util.LogUtil;
import com.android.music.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;

public class AuroraSearchView extends RelativeLayout {

	private static final String TAG ="AuroraSearchView";
	private LinearLayout.LayoutParams mLayoutParams;
	private OnClickListener mOnClickListener;
	
	public AuroraSearchView(Context context) {
		this(context, null);

	}

	public AuroraSearchView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public AuroraSearchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.layout_search, this);
		setClickable(true);
//		findViewById(R.id.aurora_id_baidu_logo).setVisibility(View.VISIBLE);
		findViewById(R.id.et_search).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if(mOnClickListener!=null){
					mOnClickListener.onClick(arg0);
				}
			}
		});
	}

	
	
	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener=l;
	}

	public void setParams(LinearLayout.LayoutParams params) {
		mLayoutParams=params;
		setLayoutParams(params);
	}

	public void setTopMargin(int top){
		if(mLayoutParams!=null){
			mLayoutParams.topMargin=top;
			setLayoutParams(mLayoutParams);
		}
	}
	
	public int getTopMargin(){
		int top=0;
		if(mLayoutParams!=null){
			top=mLayoutParams.topMargin;
		}
		return top;
	}
}
