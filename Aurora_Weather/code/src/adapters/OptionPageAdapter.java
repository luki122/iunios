package adapters;

import java.util.List;

import views.WeatherMainView;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class OptionPageAdapter extends PagerAdapter {

	private List<WeatherMainView> pageViewsList;
	public OptionPageAdapter(List<WeatherMainView> views){
		this.pageViewsList=views;
	}
	
	@Override
	public void destroyItem(ViewGroup v, int position, Object arg2) {
		v.removeView((View)arg2);
	}

	@Override
	public int getCount() {
		return pageViewsList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup v, int position) {
		((ViewPager) v).addView(pageViewsList.get(position));
		return pageViewsList.get(position);
	}
	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}
}
