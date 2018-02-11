package aurora.lib.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.aurora.lib.R;
public class AuroraLoadingView extends LinearLayout {

	
	private LayoutInflater mInflater;
	
	
	private View mLoadingPoint1,mLoadingPoint2,mLoadingPoint3;
	
	private AuroraLoadingAnimation mAnimation;
	public AuroraLoadingView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public AuroraLoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public AuroraLoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	/**
	 * init loading view
	 */
	private void init(Context context){
		mInflater = LayoutInflater.from(context);
		mInflater.inflate(R.layout.aurora_loading_view_layout,this,true);
		setOrientation(LinearLayout.HORIZONTAL);
		mLoadingPoint1 = findViewById(R.id.aurora_loading_point1);
		mLoadingPoint2 = findViewById(R.id.aurora_loading_point2);
		mLoadingPoint3 = findViewById(R.id.aurora_loading_point3);
		
		mLoadingPoint1.setVisibility(View.INVISIBLE);
		mLoadingPoint1.setScaleX(0);
		mLoadingPoint1.setScaleY(0);
		mLoadingPoint2.setVisibility(View.INVISIBLE);
		mLoadingPoint2.setScaleX(0);
		mLoadingPoint2.setScaleY(0);
		mLoadingPoint3.setVisibility(View.INVISIBLE);
		mLoadingPoint3.setScaleX(0);
		mLoadingPoint3.setScaleY(0);
		mAnimation = new AuroraLoadingAnimation();
		setBackgroundColor(Color.TRANSPARENT);
		mAnimation.setAnimatorSet(mAnimation.setupAnimation(mLoadingPoint1, mLoadingPoint2, mLoadingPoint3));
		
	}
	
	/**
	 * show loading view
	 */
	public void show(){
		mLoadingPoint1.setVisibility(View.VISIBLE);
		mLoadingPoint2.setVisibility(View.VISIBLE);
		mLoadingPoint3.setVisibility(View.VISIBLE);
		if(mAnimation.isRunning()){
			return;
		}
		mAnimation.start();
	}
	
	
	/**
	 * dismiss loading views
	 */
	public void dismiss(){
		if(mAnimation.isRunning()){
			mAnimation.stop();
		}
	}
	
	
	
	
	
	
	
	
	

}
