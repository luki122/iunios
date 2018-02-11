package adapters;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.CityListShowItem;
/**
 * 城市显示页面列表的adapter
 * @author j
 *
 */
@SuppressLint("NewApi")
public class CityListAdapter extends BaseAdapter {
	public static final String TTF_FROMAT="/system/fonts/Roboto-Thin.ttf";
    private Typeface tf;
    private List<CityListShowItem> datas;
    private LayoutInflater inflater;
    private HashMap<Integer, View> viewsMap;
    private Context context;
    private boolean isExistLocalCity;
    private CityListShowData mCityListShowData;
    public CityListAdapter(Context context,HashMap<Integer, View> viewsMap,List<CityListShowItem> datas){
    	this.viewsMap=viewsMap;
    	mCityListShowData=CityListShowData.getInstance(context);
    	this.datas=datas;
    	this.inflater=LayoutInflater.from(context);
    	this.context=context;
    	tf=Typeface.createFromFile(TTF_FROMAT);
    	isExistLocalCity=mCityListShowData.isExistLocalCity();
    }
    
    private View createItem(){
    	View retView=inflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
    	View divider=retView.findViewById(com.aurora.R.id.aurora_listview_divider);
    	divider.setVisibility(View.VISIBLE);
    	divider.setAlpha(0.5f);
		RelativeLayout front = (RelativeLayout) retView.findViewById(com.aurora.R.id.aurora_listview_front);
		View content = inflater.inflate(R.layout.item_city_list, null);
		front.addView(content);
		return retView;
    }
    private Holder createHolder(View view){
    	Holder holder=new Holder();
		holder.cityName=(TextView)view.findViewById(R.id.item_city_list_city_name);
		holder.cityTemperature=(TextView)view.findViewById(R.id.tv_temperature);
		holder.cityTemperature.setTypeface(tf);
		holder.cityWeatherThumb=(ImageView)view.findViewById(R.id.item_city_list_city_right_circle_view);
		holder.cityHighLowTemperature=(TextView)view.findViewById(R.id.item_city_list_city_terperature);
		holder.iv_temperature_point=(ImageView)view.findViewById(R.id.iv_temperature_point);
		return holder;
    }
    
   
    private void setData(int position,Holder holder){
    	CityListShowItem item=datas.get(position);
		holder.cityName.setText(item.getCityName());
		String currentTemp=item.getCurTemp();
		if(!currentTemp.equals(context.getString(R.string.default_temperature)))
		{
			holder.iv_temperature_point.setVisibility(View.VISIBLE);
			holder.cityTemperature.setVisibility(View.VISIBLE);
		}else{
			holder.iv_temperature_point.setVisibility(View.INVISIBLE);
			holder.cityTemperature.setVisibility(View.INVISIBLE);
		}
		holder.cityTemperature.setText(currentTemp);
		if(item.getLowAndHighTempStr()!=null&&!item.getLowAndHighTempStr().equals(""))
		{
			holder.cityHighLowTemperature.setText(item.getLowAndHighTempStr());
		}
		holder.cityWeatherThumb.setImageResource(item.getResId());
    }
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return isExistLocalCity?(datas.size()-1<0?0:datas.size()-1):datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return isExistLocalCity?datas.get(position+1):datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return isExistLocalCity?position+1:position;
	}

	class Holder{
		TextView cityName,cityTemperature,cityHighLowTemperature;
		ImageView cityWeatherThumb,iv_temperature_point;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder=null;
		if(convertView==null)
		{
			convertView=createItem();
			holder=createHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder=(Holder) convertView.getTag();
		}
		RelativeLayout front = (RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front);
		convertView.findViewById(com.aurora.R.id.content).setAlpha(1);
		convertView.findViewById(com.aurora.R.id.control_padding).setPadding(0, 0, 0, 0);
		setData(isExistLocalCity?position+1:position, holder);
		viewsMap.put(isExistLocalCity?position+1:position, convertView);
		return convertView;
	}

}
