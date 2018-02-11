package com.aurora.iunivoice.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.bean.EmotionInfo;

public class EmotionAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private List<EmotionInfo> dataList;
	
	public EmotionAdapter(Context context, List<EmotionInfo> list) {
		this.inflater = LayoutInflater.from(context); 
		this.dataList = list;
	}

	@Override
	public int getCount() {
		return dataList == null ? 0 : dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EmotionInfo info = dataList.get(position);
		Holder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_emotion, null);
			holder = new Holder();
			holder.iv_face = (ImageView) convertView.findViewById(R.id.iv_face);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		holder.iv_face.setImageResource(info.getResId());
		
		return convertView;
	}
	
	private static class Holder {
		ImageView iv_face;
	}

}
