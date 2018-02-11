package com.android.gallery3d.filtershow.category;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.imageshow.MasterImage;
import com.android.gallery3d.filtershow.pipeline.ImagePreset;
import com.android.gallery3d.filtershow.pipeline.RenderingRequest;
import com.android.gallery3d.filtershow.pipeline.RenderingRequestCaller;

public class AuroraAction implements RenderingRequestCaller {
	
	public enum AuroraActionType {
		MAIN_MENU_CROP,
		MAIN_MENU_ROTATE,
		MAIN_MENU_FILTERS,
		MAIN_MENU_BEAUTY_SHOTS,//SQF ADDED ON 2014.12.25
	}
	
	private FilterShowActivity mContext;
	private FilterRepresentation mFilterRepresentation;
	
	private ArrayAdapter mAdapter;
	
	private int mPosition;

	private int mDrawableResId;
	
	private AuroraEffectView mView;
	
	private boolean mHasFilterRepresentation = false;
	private AuroraActionType mType;
	
	public AuroraAction(FilterShowActivity context, FilterRepresentation filterRepresentation) {
		this.mContext = context;
		this.mFilterRepresentation = filterRepresentation;
		mHasFilterRepresentation = filterRepresentation != null ? true : false;
	}
	
	public void setAdapter(ArrayAdapter adapter) {
		this.mAdapter = adapter;
	}
	
	public void setType(AuroraActionType type) {
		mType = type;
	}
	
	public void setPosition(int pos) {
		this.mPosition = pos;
	}
	
	public FilterRepresentation getFilterRepresentation() {
		return mFilterRepresentation;
	}
	
	public void setAuroraEffectView(AuroraEffectView v) {
		mView = v;
	}

	@Override
	public void available(RenderingRequest request) {
		// TODO Auto-generated method stub
		Bitmap bitmap = request.getBitmap();
		if(bitmap == null) return;
		mView.setBitmap(bitmap);
		/*
		if(mView != null && mPosition == 0) {//Original
			mView.setSelected(true);
		}
		*/
	}
	
    public void postNewIconRenderRequest(int w, int h) {
        if (mFilterRepresentation != null) {
            //ImagePreset currentPreset = MasterImage.getImage().getCurrentPreset();
            //ImagePreset preset = new ImagePreset(currentPreset);
            
            ImagePreset preset = new ImagePreset();
            preset.addFilter(mFilterRepresentation);
            RenderingRequest.postIconRequest(mContext, w, h, preset, this);
        }
    }

	
}
