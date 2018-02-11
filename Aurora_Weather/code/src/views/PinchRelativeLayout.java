package views;

import java.util.HashMap;

import android.content.Context;
import android.filterfw.geometry.Point;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import com.aurora.utils.DensityUtil;;
public class PinchRelativeLayout extends RelativeLayout {

	public PinchRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		// TODO Auto-generated constructor stub
	}

	public PinchRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PinchRelativeLayout(Context context) {
		super(context);
		init(context);
	}


	private void init(Context context){
		DISTANCE=DensityUtil.dip2px(context, DISTANCE);
		
	}
	
	public static interface IPinchDoListener{
		void exit();
		void enter();
	}
	
	private IPinchDoListener mIPinchDoListener;
	public void setIPinchDoListener(IPinchDoListener iPinchDoListener){
		this.mIPinchDoListener=iPinchDoListener;
	}
	
	private void excutePinchDoListener(boolean enter){
		if(mIPinchDoListener==null)
			return;
		if(enter)
		{
			mIPinchDoListener.enter();
		}else{
			mIPinchDoListener.exit();
		}
	}
	
	
	private float getDistance(float x,float y,float x1,float y1){
		return FloatMath.sqrt(((x-x1)*(x-x1)+(y-y1)*(y-y1)));
	}
	
	private final int VALIDE_ID=-1;
	private int pointOneId=VALIDE_ID,pointTwoId=VALIDE_ID;
	private boolean isTwoPoint=false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		 super.onTouchEvent(event);
		switch (event.getAction()&MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			return true;
		case MotionEvent.ACTION_UP:
			getUpPointLocation(event);
			operate(pointOneDistance, pointTwoDistance);
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			return true;
		case MotionEvent.ACTION_POINTER_UP:
			getUpPointLocation(event);
			return isIntercept(event);
		case MotionEvent.ACTION_MOVE:
			return isIntercept(event);
		}
		return false;
	}
	
	private float DISTANCE=30F;
	private void getUpPointLocation(MotionEvent event){
		int nowIndexId=event.getPointerId(event.getActionIndex());
		if(nowIndexId==pointOneId)
		{
			pointOneUpX=event.getX(event.getActionIndex());
			pointOneUpY=event.getY(event.getActionIndex());
			pointTwoDistance=getDistance(pointOneUpX, pointOneUpY, pointTwoUpX, pointTwoUpY);
		}else if(nowIndexId==pointTwoId)
		{
			pointTwoUpX=event.getX(event.getActionIndex());
			pointTwoUpY=event.getY(event.getActionIndex());
			pointTwoDistance=getDistance(pointOneUpX, pointOneUpY, pointTwoUpX, pointTwoUpY);
		}
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getAction()&MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			pointOneDownX=event.getX(event.getActionIndex());
			pointOneDownY=event.getY(event.getActionIndex());
			pointOneId=event.getPointerId(event.getActionIndex());
			return false;
		case MotionEvent.ACTION_UP:
			return false;
		case MotionEvent.ACTION_POINTER_DOWN:
			if(event.getPointerCount()==2)
			{
			  isTwoPoint=true;
			  pointTwoDownX=event.getX(event.getActionIndex());
			  pointTwoDownY=event.getY(event.getActionIndex());
			  pointTwoId=event.getPointerId(event.getActionIndex());
			  pointOneDistance=getDistance(pointOneDownX, pointOneDownY, pointTwoDownX, pointTwoDownY);
			}
			return true;
		case MotionEvent.ACTION_POINTER_UP:
			return true;
		case MotionEvent.ACTION_MOVE:
			return isIntercept(event);
		}
		return false;
	}
	
	private void operate(double pointOneDistance,double pointTwoDistance){
		double del=pointOneDistance-pointTwoDistance;
		if (isTwoPoint) {
			if (del > DISTANCE) {
				excutePinchDoListener(true);
			} else if (del < -DISTANCE) {
				excutePinchDoListener(false);
			}
		}
		this.isTwoPoint=false;
		this.pointOneDistance=0;
		this.pointTwoDistance=0;
		this.pointTwoDownX=0;
		this.pointTwoUpX=0;
		this.pointOneDownX=0;
		this.pointOneUpX=0;
		this.pointOneId=VALIDE_ID;
		this.pointTwoId=VALIDE_ID;
	}
	
	private double pointOneDistance,pointTwoDistance;
	
	private float pointOneDownX,pointOneDownY,pointTwoDownX,pointTwoDownY;
	private float pointOneUpX,pointOneUpY,pointTwoUpX,pointTwoUpY;
	private boolean isIntercept(MotionEvent ev){
		return ev.getPointerCount()==2;
	}
}
