package com.android.systemui.recent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.systemui.R;
import android.content.res.Configuration;
public class AuroraRubbishView extends ImageView{
	
	private Bitmap [] rubbishBitmaps;
	private Bitmap mDrawBit;
	private int [] rubbishSources = new int []{
			R.drawable.recents_rubbish_anim_000,
			R.drawable.recents_rubbish_anim_001,
			R.drawable.recents_rubbish_anim_002,
			R.drawable.recents_rubbish_anim_003,
			R.drawable.recents_rubbish_anim_004,
			R.drawable.recents_rubbish_anim_005,
			R.drawable.recents_rubbish_anim_006,
			R.drawable.recents_rubbish_anim_007,
			R.drawable.recents_rubbish_anim_008,
			R.drawable.recents_rubbish_anim_009,
			R.drawable.recents_rubbish_anim_010,
			R.drawable.recents_rubbish_anim_011,
			R.drawable.recents_rubbish_anim_012,
			R.drawable.recents_rubbish_anim_013,
			R.drawable.recents_rubbish_anim_014,
			R.drawable.recents_rubbish_anim_015,
			R.drawable.recents_rubbish_anim_016,
			R.drawable.recents_rubbish_anim_017,
			R.drawable.recents_rubbish_anim_018,
			R.drawable.recents_rubbish_anim_019,
			R.drawable.recents_rubbish_anim_020,
			R.drawable.recents_rubbish_anim_021,
			R.drawable.recents_rubbish_anim_022,
			R.drawable.recents_rubbish_anim_023,
			};

	public AuroraRubbishView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	private void initRubbishResource(){
		for(int i =0;i< rubbishSources.length;i++){
			rubbishBitmaps[i]= BitmapFactory.decodeResource(getResources(),rubbishSources[i]);
		}
		mDrawBit = rubbishBitmaps[0];
	}

	private int screenOrientation = getScreenOrientation();

	private int getScreenOrientation(){
		return getResources().getConfiguration().orientation;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		android.util.Log.e("AuroraRubbishView", "Rubbish View onConfigurationChanged new: " + newConfig.orientation + " old is: " + newConfig.orientation);
		if(newConfig.orientation != screenOrientation){
			// is screen orientation change, we should reload level res
			screenOrientation = newConfig.orientation;
			initRubbishResource();
		}
		super.onConfigurationChanged(newConfig);
	}
	public AuroraRubbishView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		rubbishBitmaps = new Bitmap [24];
		initRubbishResource();
		mDrawBit = rubbishBitmaps[0];
	}

	public AuroraRubbishView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void rotate(float deltaX){
		deltaX = Math.min(deltaX, 270);
		int index = 0;
		try {
			index = 23 - (int)Math.abs(deltaX)/8;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			index = 23;
		}
//		recentsRubbish.setImageLevel(index);
		if(index < 0){
			index = 0;
		}
		if(index > 23){
			index = 23;
		}
		mDrawBit = rubbishBitmaps[index];
		invalidate();
	}
	
	public void rest(){
		mDrawBit = rubbishBitmaps[0];
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if(mDrawBit!=null){
			canvas.drawBitmap(mDrawBit, 0, 0, null);
		}
		
	}
	
}
