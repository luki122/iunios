package views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aurora.utils.DensityUtil;
import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.WeatherData;

public class CircleIndexView extends AbstractWeatherView {
	
	private int mCityIndex = 0;
	private LinearLayout circleIndexLinear;
	private Context mContext;
	private int mLocalPosition=-1;
	
	private int cityIndexPoint_PaddingLeft;
	private int cityIndexPoint_PaddingTop;
	private int currentPosition=0;
	public CircleIndexView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public CircleIndexView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public CircleIndexView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		cityIndexPoint_PaddingLeft = DensityUtil.dip2px(context, 6);
		//cityIndexPoint_PaddingTop = DensityUtil.dip2px(context, 1f);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.circleindex, this, true);
		circleIndexLinear = (LinearLayout)this.findViewById(R.id.circleindexlinear);
		if(mCityListShowData.isExistLocalCity())
		{
			mLocalPosition=0;
		}
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onUpdating() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @ used to show which highlight picture
	 * 
	 * @param index : city index, means current surface is which city
	 */
	public void setCityIndex(int index)
	{
		mCityIndex = index;
	}
	
	/**
	 * 设置当前所在城市的index
	 */
	public void setLocalPosition( int position ) {
		this.mLocalPosition = position;
		if(currentPosition==mLocalPosition)
		{
			((ImageView)circleIndexLinear.getChildAt(0)).setImageResource(R.drawable.localcityindexpoint_highlight);
		}else{
			((ImageView)circleIndexLinear.getChildAt(0)).setImageResource(R.drawable.localcityindexpoint);
		}
	}
	
	/**
	 * 添加一个圆点
	 */
	public void addCirclePointView( ) {
		ImageView childView = new ImageView(mContext);
		if( mLocalPosition == circleIndexLinear.getChildCount() ) {
			childView.setImageResource(R.drawable.localcityindexpoint);
		} else {
			childView.setImageResource(R.drawable.cityindexpoint);
			childView.setPadding(0, cityIndexPoint_PaddingTop, 0, 0);
		}
		if( circleIndexLinear.getChildCount() > 0 ) {
			childView.setPadding(cityIndexPoint_PaddingLeft, childView.getPaddingTop(), 0, 0);
		}
		circleIndexLinear.addView(childView);
	}
	
	public void addCirclePointView( int index ) {
		ImageView childView = new ImageView(mContext);
		if( mLocalPosition == index ) {
			childView.setImageResource(R.drawable.localcityindexpoint);
		} else {
			childView.setImageResource(R.drawable.cityindexpoint);
		}
		if( circleIndexLinear.getChildCount() > 0 ) {
			childView.setPadding(cityIndexPoint_PaddingLeft, childView.getPaddingTop(), 0, 0);
		}
		circleIndexLinear.addView(childView, index);
	}
	
	/**
	 * 删除一个圆点
	 */
	public void removeCirclePointView( int index ) {
		circleIndexLinear.removeViewAt(index);
	}
	
	public void changeCircleState(int index){
		currentPosition=index;
		int childCount=circleIndexLinear.getChildCount();
		boolean isExistLocalCity=mCityListShowData.isExistLocalCity();
		ImageView temp;
		for(int i=0;i<childCount;i++)
		{
			temp=(ImageView)circleIndexLinear.getChildAt(i);
			if(index==i)
			{
				if(index==0&&isExistLocalCity)
				{
					temp.setImageResource(R.drawable.localcityindexpoint_highlight);
				}else{
					temp.setImageResource(R.drawable.cityindexpoint_highlight);
				}
			}else{
				if(i==0&&isExistLocalCity)
				{
					temp.setImageResource(R.drawable.localcityindexpoint);
				}else{
					temp.setImageResource(R.drawable.cityindexpoint);
				}
				
			}
		}
	}
	
	/**
	 * 设置高亮圆点的图片
	 */
	public void setFocusedCircleIndexViewBackGround( int index ) {

		ImageView childView = (ImageView)circleIndexLinear.getChildAt(index);
		if ( childView != null ) {
			if( mLocalPosition == index ) {
				childView.setImageResource(R.drawable.localcityindexpoint_highlight);
			} else {
				childView.setImageResource(R.drawable.cityindexpoint_highlight);
			}
		}
	}
	
	/**
	 * 设置非高亮圆点的图片
	 */
	public void setNormalCircleIndexViewBackGround( int index ) {

		ImageView childView = (ImageView)circleIndexLinear.getChildAt(index);
		if ( childView != null ) {
			if( mLocalPosition == index ) {
				childView.setImageResource(R.drawable.localcityindexpoint);
			} else {
				childView.setImageResource(R.drawable.cityindexpoint);
			}
		}
	}
}