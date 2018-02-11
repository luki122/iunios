package com.android.gallery3d.filtershow.category;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import aurora.widget.AuroraAnimationImageView;

public class AuroraCategoryAdapter extends ArrayAdapter<AuroraAction> {
	
	private int mItemWidth = ListView.LayoutParams.MATCH_PARENT;
	private int mItemHeight;
	
	private OnClickListener mOnClickListener;
	private View mContainer;

	public AuroraCategoryAdapter(Context context) {
		this(context, 0);
	}
	
	public AuroraCategoryAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		// TODO Auto-generated constructor stub
		mItemHeight = (int) (context.getResources().getDisplayMetrics().density * 100);
		
	}
	
	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

	@Override
	public void add(AuroraAction action) {
		// TODO Auto-generated method stub
		super.add(action);
		action.setAdapter(this);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		/*
		if (convertView == null) {
            convertView = new AuroraAnimationImageView(getContext(), null);
        }
		AuroraAnimationImageView view = (AuroraAnimationImageView) convertView;
		view.setBackgroundColor(Color.RED);
        //view.setOrientation(mOrientation);
		view.setOnClickListener(mOnClickListener);
        AuroraAction action = getItem(position);
        action.setPosition(position);
        view.setTag(action);
        int width = mItemWidth;
        int height = mItemHeight;
        Log.i("SQF_LOG", "getView: [w,h]" + width + " " + height);
        //view.setLayoutParams(new ListView.LayoutParams(width, height));
        view.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        //view.invalidate();
        return view;
        */
		return null;
	}
	
	
	public void setContainer(View container) {
        mContainer = container;
    }
	
    public void setItemHeight(int height) {
        mItemHeight = height;
    }

    public void setItemWidth(int width) {
        mItemWidth = width;
    }
}
