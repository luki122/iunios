package adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aurora.weatherforecast.R;

import datas.CityListShowData;
import datas.WeatherWarningInfo;

public class WarnDetailAdapter extends BaseAdapter {

	private List<WeatherWarningInfo> datas;
	private LayoutInflater inflater;
	private CityListShowData cityListShowData;
	private Context context;
	private HashMap<Integer, WarnDetailAdapter.Holder> viewsMap;
	private boolean isRunAnimator = true;
	private String weatherDate;
	public WarnDetailAdapter(Context context,int index,HashMap<Integer, WarnDetailAdapter.Holder> viewsMap,List<WeatherWarningInfo> datas,String weatherDate){
		this.context = context;
		this.weatherDate = weatherDate;
		cityListShowData = CityListShowData.getInstance(context);
		this.datas = datas;
		this.inflater = LayoutInflater.from(context);
		this.viewsMap = viewsMap;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void disableRunAnimator(){
		isRunAnimator = false;
	}
	
	public static class Holder{
		public TextView tv_warninfo_title,tv_warninfo_content,tv_warninfo_time;
		public View empty_view;
	}
	private SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private String getTime(){
		try {
			Date time=format.parse(weatherDate);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			String month=calendar.get(Calendar.MONTH)+1+"";
			String day=calendar.get(Calendar.DAY_OF_MONTH)+"";
			String HHmm=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);
			return context.getString(R.string.tv_warn_publishtime, month,day,HHmm);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder=null;
		if(convertView==null)
		{
			holder = new Holder();
			convertView=inflater.inflate(R.layout.item_warn_detail, null);
			holder.tv_warninfo_title = (TextView)convertView.findViewById(R.id.tv_warninfo_title);
			holder.tv_warninfo_content = (TextView)convertView.findViewById(R.id.tv_warninfo_content);
			holder.tv_warninfo_time = (TextView)convertView.findViewById(R.id.tv_warninfo_time);
			holder.empty_view = convertView.findViewById(R.id.empty_view);
			convertView.setTag(holder);
		}else{
			holder = (Holder) convertView.getTag();
		}
		
		if(position==0)
		{
			holder.empty_view.setVisibility(View.VISIBLE);
		}else{
			holder.empty_view.setVisibility(View.GONE);
		}
		WeatherWarningInfo data = datas.get(position);
		holder.tv_warninfo_title.setText(data.getTitle());
		holder.tv_warninfo_content.setText(data.getDetail());
		holder.tv_warninfo_time.setText(getTime());
		if(isRunAnimator)
		{
			holder.tv_warninfo_title.setAlpha(0);
			holder.tv_warninfo_content.setAlpha(0);
			holder.tv_warninfo_time.setAlpha(0);
		}else{
			holder.tv_warninfo_title.setAlpha(0.8f);
			holder.tv_warninfo_content.setAlpha(0.7f);
			holder.tv_warninfo_time.setAlpha(0.5f);
		}
		viewsMap.put(position, holder);
		return convertView;
	}

}
