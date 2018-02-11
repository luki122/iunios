package views;

import java.util.ArrayList;
import java.util.List;

import views.TemperatureView.Data;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.aurora.weatherforecast.R;

@SuppressLint("NewApi")
public class LineView extends View {
	private List<Data> tempDate = new ArrayList<TemperatureView.Data>();
    private Bitmap bigBitmap,littleBitmap;
	
	public LineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LineView(Context context) {
		super(context);
		init();
	}

	private void init(){
		linePaint = new Paint();
		setBackgroundColor(Color.TRANSPARENT);
		linePaint.setColor(Color.parseColor("#88ffffff"));
		linePaint.setStrokeWidth(2);
		bigBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.bigpoint);
		littleBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.littlepoine);
		setAnim();
	}
	
	private int flag = 0;
	private final int ANIM_IN = 1;
	private final int ANIM_OUT = 2;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		switch (flag) {
		case ANIM_IN:
			drawLineIn(canvas);
			break;
		case ANIM_OUT:
			drawLineOut(canvas);
			break;
		default:
			break;
		}
	}

	public void setDate(List<TemperatureView.Data> tempDate) {
		this.tempDate.clear();
		this.tempDate.addAll(tempDate);
		counts = new int[tempDate.size()];
	}

	public void startAnimIn() {
		aniIn.start();
	}

	public void startAnimOut() {
		animOut.start();
	}

	private boolean[] isOKs;

	private ObjectAnimator aniIn, animOut;

	private void setAnim() {
		aniIn = ObjectAnimator.ofInt(this, "alpha", 0, 255).setDuration(500);
		animOut = ObjectAnimator.ofInt(this, "alpha", 255, 0)
				.setDuration(500);
		aniIn.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				flag = ANIM_IN;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
			}
		});
		animOut.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				flag = ANIM_OUT;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {

			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
			}
		});
	}

	public void setAlpha(int alpha) {
		linePaint.setAlpha(alpha);
		invalidate();
	}

	private Paint linePaint;
	private final int V = 2;
	private int[] counts;

	private void drawLineIn(Canvas canvas) {
		for (int i = 1; i < tempDate.size() - 1; i++) {
			float v = (tempDate.get(i).endPy - tempDate.get(i).py) / V;
			if(i==1)
			{
				canvas.drawBitmap(bigBitmap, tempDate.get(i).px-bigBitmap.getWidth()/2, tempDate.get(i).py-bigBitmap.getHeight()/2, linePaint);
			}else{
				canvas.drawBitmap(littleBitmap, tempDate.get(i).px-littleBitmap.getWidth()/2, tempDate.get(i).py-littleBitmap.getHeight()/2, linePaint);
			}
			if (counts[i - 1] == V) {
				canvas.drawLine(tempDate.get(i).px, tempDate.get(i).py,
						tempDate.get(i).px, tempDate.get(i).endPy, linePaint);
			}
			while (counts[i - 1] < V) {
				counts[i - 1]++;
				float endy = tempDate.get(i).py
						+ v
						* counts[i - 1];
				canvas.drawLine(tempDate.get(i).px, tempDate.get(i).py,
						tempDate.get(i).px, endy, linePaint);
				if (Math.abs(endy - tempDate.get(i).endPy) < 0.1) {
					counts[i - 1]=V;
					break;
				} else {
					invalidate();
					i = tempDate.size();
					break;
				}
			}
		}
	}

	private void drawLineOut(Canvas canvas) {
		for (int i = tempDate.size() - 2; i > 0; i--) {
			int firstIndex = tempDate.size() - 1 - i;
			float v = (tempDate.get(firstIndex).endPy - tempDate
					.get(firstIndex).py) / V;
			if(i==1)
			{
				canvas.drawBitmap(bigBitmap, tempDate.get(i).px-bigBitmap.getWidth()/2, tempDate.get(i).py-bigBitmap.getHeight()/2, linePaint);
			}else{
				canvas.drawBitmap(littleBitmap, tempDate.get(i).px-littleBitmap.getWidth()/2, tempDate.get(i).py-littleBitmap.getHeight()/2, linePaint);
			}
			if(firstIndex==1||counts[firstIndex - 2]==0)
			{
			while (counts[firstIndex - 1] > 0) {
				counts[firstIndex - 1]--;
				float endy = tempDate.get(firstIndex).py + v * counts[firstIndex - 1];
				canvas.drawLine(tempDate.get(firstIndex).px,
						tempDate.get(firstIndex).py,
						tempDate.get(firstIndex).px, endy, linePaint);
				if (counts[firstIndex - 1]==0) {
					invalidate();
					break;
				} else {
					invalidate();
					break;
				}
			}
			}
			if (i>0&&counts[i - 1] == V) {
				canvas.drawLine(tempDate.get(i).px, tempDate.get(i).py,
						tempDate.get(i).px, tempDate.get(i).endPy, linePaint);
			}
		}
	}
}
