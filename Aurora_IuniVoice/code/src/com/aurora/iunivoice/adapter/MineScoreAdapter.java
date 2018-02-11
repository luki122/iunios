package com.aurora.iunivoice.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aurora.datauiapi.data.bean.UserInfoExtcredits;
import com.aurora.iunivoice.R;

public class MineScoreAdapter extends BaseAdapter {

	private ArrayList<UserInfoExtcredits> datas;
	private Context context;
	private LayoutInflater inflater;
	public MineScoreAdapter(ArrayList<UserInfoExtcredits> datas,Context context){
		this.datas = datas;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return datas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		
	    Holder holder = null;
	    if(arg1 == null)
	    {
	    	arg1 = inflater.inflate(R.layout.item_score, null);
	    	holder = new Holder();
	    	holder.title = (TextView) arg1.findViewById(R.id.title);
	    	holder.value = (TextView) arg1.findViewById(R.id.value);
	    	arg1.setTag(holder);
	    }else{
	    	holder = (Holder) arg1.getTag();
	    }
	    holder.title.setText(datas.get(arg0).getTitle());
	    holder.value.setText(datas.get(arg0).getValue()+datas.get(arg0).getUnit());
		return arg1;
	}
	
	class Holder{
		TextView title,value;
	}

}
