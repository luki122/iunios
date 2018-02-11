package com.android.auroramusic.widget;

import com.android.music.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class AuroraDotView extends View {

	private static final String TAG = "AuroraDotView";
	private int dotCount=1; //dot 数目
	private int selectDot=0; //当前dot
	private Paint mPaint = new Paint();
	private Bitmap selectDotBitmap,unselectDotBitmap;
	private int dotBesize=0;
	
	public AuroraDotView(Context context) {
		this(context, null);

	}

	public AuroraDotView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public AuroraDotView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		selectDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aurora_dot_select);
		unselectDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aurora_dot_unslect);
		dotBesize = getResources().getDimensionPixelSize(R.dimen.aurora_dot_besize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		int bitmapheight=selectDotBitmap.getHeight();
		int bitmapwidth=selectDotBitmap.getWidth();

		if(dotCount<=0){
			return;
		}
		
		int left = (width-(dotCount*bitmapwidth+(dotCount-1)*dotBesize))/2;
		for(int i=0;i<dotCount;i++){
			if(selectDot==i){
				canvas.drawBitmap(selectDotBitmap, left+i*(dotBesize+bitmapwidth), height-bitmapheight, mPaint);
			}else{
				canvas.drawBitmap(unselectDotBitmap, left+i*(dotBesize+bitmapwidth), height-bitmapheight, mPaint);
			}			
		}
	}

	public void setSelectDot(int position){
		selectDot = position;
		invalidate();
	}
	public void setDotCount(int count){
		if(count==0){
			count=1;
		}
		dotCount=count;
		invalidate();
	}
	
	public void setSelectDrawble(int resId){
		selectDotBitmap = BitmapFactory.decodeResource(getResources(), resId);
	}
	
	public void setUnselectDrawble(int resId){
		unselectDotBitmap = BitmapFactory.decodeResource(getResources(), resId);
	}
	
	public void setDotBesize(int resId){
		dotBesize = getResources().getDimensionPixelSize(resId);
	}
}
