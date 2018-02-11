package com.android.settings.feedback;

import java.util.List;

import com.android.settings.*;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

public class addFeedBackAdapter<Bitmap> extends ArrayAdapter<Bitmap> {
	
	private static final String TAG="gd";
	private ViewGroup parentView;
	private int CurrentPosition=-1;
	private final Bitmap defaultItemBitmap;

	public addFeedBackAdapter(Context context, List<Bitmap> objects, Bitmap bitmap) {
		super(context, 0,objects);
		defaultItemBitmap=bitmap;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FeedBackGridItemCache feedBackGridItemCache = null;
		if(convertView==null)
		{
			parentView=parent;
			feedBackGridItemCache=new FeedBackGridItemCache();
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater(); 
			convertView = inflater.inflate(R.layout.griditem, parent, false);
			feedBackGridItemCache.picCache=(ImageView) convertView.findViewById(R.id.grid_item_pic);
		    feedBackGridItemCache.mImageButton=(ImageButton)convertView.findViewById(R.id.delbtn);
		    feedBackGridItemCache.mImageButton.setOnClickListener(onClickListener);
			convertView.setTag(feedBackGridItemCache);
		}else
		{
			feedBackGridItemCache=(FeedBackGridItemCache) convertView.getTag();
		}
		Log.d(TAG, "Count="+getCount());
		if(position>getCount())
		{
			return convertView;
		}
		
		if(getItem(position).equals(defaultItemBitmap))
		{
			/*feedBackGridItemCache.picCache.setImageBitmap((android.graphics.Bitmap) defaultItemBitmap);*/
			feedBackGridItemCache.picCache.setImageResource(R.drawable.add_default_pic);
			/*feedBackGridItemCache.picCache.setBackgroundResource(R.drawable.add_default_pic);*/
			feedBackGridItemCache.mImageButton.setVisibility(View.INVISIBLE	);
			return convertView;
		}
		
		feedBackGridItemCache.mImageButton.setTag(getItem(position)+"@chioce_flag");
		feedBackGridItemCache.mImageButton.setVisibility(View.VISIBLE);
		feedBackGridItemCache.picCache.setTag(getItem(position)+"@pic_dis");
		feedBackGridItemCache.picCache.setImageBitmap((android.graphics.Bitmap) getItem(position));
		return convertView;
	}
	
	
	public void dealClickEvent(int position)
	{
		CurrentPosition=position;
	    ImageButton imageBtnFlag=(ImageButton) parentView.findViewWithTag(getItem(position)+"@chioce_flag");
	    if(imageBtnFlag!=null)
	    {
	    	imageBtnFlag.setVisibility(View.VISIBLE);
	    }
	}
	
	private boolean isChoice(int position)
	{
		if(CurrentPosition==-1)
		{
			return false;
		}
		return (getItem(CurrentPosition)==getItem(position));
	}
	
	private OnClickListener onClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			for(int index=0;index<getCount();index++)
			{
				if(v.getTag().equals(getItem(index)+"@chioce_flag"))
				{
					Log.d(TAG, "  getTag="+v.getTag());
					remove(getItem(index));
				}
			}
			if(!isAddDefaultPic())
		    {
            	add(defaultItemBitmap);
            }
			notifyDataSetInvalidated();
		}
	};
	
	
	private  boolean isAddDefaultPic()
	{
		for(int position=0;position<getCount();position++)
        {
        	if(getItem(position).equals(defaultItemBitmap))
        	{
                return true;
        	}
        }
		return false;
	}
	
	public class FeedBackGridItemCache {
		public ImageView picCache;
		public ImageButton mImageButton;
	}

}
