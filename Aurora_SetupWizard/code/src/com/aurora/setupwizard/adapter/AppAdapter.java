package com.aurora.setupwizard.adapter;

import java.util.List;

import com.aurora.setupwizard.domain.ApkInfo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.setupwizard.R;

public class AppAdapter extends BaseAdapter {

	private List<ApkInfo> infos = null;
	private Context mContext;

	public AppAdapter(Context context, List<ApkInfo> infos) {
		this.mContext = context;
		this.infos = infos;
	}

	@Override
	public int getCount() {
		if (infos != null) {
			return infos.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int posion) {
		return infos.get(posion);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			view = View.inflate(mContext, R.layout.app_recommend_item, null);
			holder.cb = (CheckBox) view.findViewById(R.id.cb_select);
			holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
			holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
			holder.tv_resion = (TextView) view.findViewById(R.id.tv_app_reson);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.iv_icon.setImageDrawable(infos.get(position).getApkIcon());
		holder.tv_name.setText(infos.get(position).getApkName());
		holder.tv_resion.setText(infos.get(position).getApkDescription());

		holder.cb.setChecked(infos.get(position).isCheck());
		return view;
	}

	class ViewHolder {
		CheckBox cb;
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_resion;
	}

}
