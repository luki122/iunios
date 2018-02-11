package com.aurora.mms.ui;
// Aurora xuyong 2015-10-08 created for aurora's new feature
import java.io.File;

import com.android.mms.R;
import com.android.mms.ui.MessageListItem;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import aurora.widget.AuroraCheckBox;

public class AuroraHorizontalListAdapter extends CursorAdapter{

	protected LayoutInflater mInflater;
	private Handler mHandler;
	
	public AuroraHorizontalListAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
		mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
	}

	public AuroraHorizontalListAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
		// TODO Auto-generated constructor stub
		mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
	}

	public AuroraHorizontalListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
		mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	@Override
	public void bindView(View childView, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		if (cursor == null) {
			return;
		}
		if (childView instanceof AuroraHotizontalListItem) {
			AuroraHotizontalListItem item = (AuroraHotizontalListItem)childView;
			item.setHandler(mHandler);
			item.bind(cursor);
		}
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		mCursor.moveToPosition(position);
        if (convertView == null) {
        	view = newView(mContext, mCursor, parent);
        } else {
        	view = convertView;
        }
        bindView(view, mContext, mCursor);
		return view;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mInflater.inflate(R.layout.aurora_attach_image_item, parent, false);
	}

}
