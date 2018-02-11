package com.android.gallery3d.local.widget;

import com.android.gallery3d.local.tools.ImageLoader;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.android.gallery3d.R;

public class GalleryBaseAdapter extends BaseAdapter {
	
	protected LayoutInflater inflater;
	protected Context mContext;
	protected ImageLoader imageLoader;

	public GalleryBaseAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		this.inflater = LayoutInflater.from(mContext);
		imageLoader = ImageLoader.getInstance(mContext.getApplicationContext());
	}


	private boolean isOperation = false;

	public boolean isOperation() {
		return isOperation;
	}

	public void setOperation(boolean isOperation) {
		this.isOperation = isOperation;
	}
	
	
	private boolean isItemPicAnim;

	public boolean isItemPicAnim() {
		return isItemPicAnim;
	}

	public void setItemPicAnim(boolean isItemPicAnim) {
		this.isItemPicAnim = isItemPicAnim;
	}

	protected void imageViewAnim(ImageView imageView, boolean in) {
		if (in) {
			imageView.setImageResource(R.anim.animation_imageview_in);
		} else {
			imageView.setImageResource(R.anim.animation_imageview_out);
		}
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
		animationDrawable.start();
	}
	

	
	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

}
