package adapters;

import java.util.List;

import com.aurora.weatherdata.bean.CityItem;
import com.aurora.weatherforecast.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchCityAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<CityItem> cityList;
	
	public SearchCityAdapter(Context mContext,List<CityItem> cityList) {
		this.mContext = mContext;
		this.cityList = cityList;
	}

	@Override
	public int getCount() {
		return cityList.size();
	}

	@Override
	public Object getItem(int position) {
		return cityList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class ViewHolder {
		TextView tvSearchedCity;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.searchcityitem, null);
			holder.tvSearchedCity = (TextView)convertView.findViewById(R.id.tv_searchcity);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvSearchedCity.setText(cityList.get(position).getCityName());
		
		return convertView;
	}

}
