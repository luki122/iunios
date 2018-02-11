package com.aurora.setupwizard.adapter;

import java.util.List;

import com.aurora.setupwizard.domain.LanguageInfo;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.aurora.setupwizard.R;

public class LanguageAdapter extends BaseAdapter {

	private List<LanguageInfo> infos = null;
	private Context mContext;

	public LanguageAdapter(Context context, List<LanguageInfo> infos) {
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
	public LanguageInfo getItem(int arg0) {
		return infos.get(arg0);
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
			view = View.inflate(mContext, R.layout.language_item, null);
			holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
			holder.rb_btn = (RadioButton) view.findViewById(R.id.rb_btn);
			view.setTag(holder);
			;
		} else {
			holder = (ViewHolder) view.getTag();
		}

		// final RadioButton radio=(RadioButton) view.findViewById(R.id.rb_btn);
		// holder.rdBtn = radio;

		final LanguageInfo info = infos.get(position);
		if (info.getName().trim().equals("English (United States)".trim())) {
			holder.tv_name.setAlpha(0.4f);
		} else {
			holder.tv_name.setAlpha(1f);
		}
		holder.tv_name.setText(info.getName());
		holder.rb_btn.setChecked(info.isSelect());

		final RadioButton radio = holder.rb_btn;
		final int p = position;

		if (info.getName().trim().equals("English (United States)".trim())) {
			holder.rb_btn.setEnabled(false);
			holder.tv_name.setText("English (Not available for now)");
		} else {
			holder.rb_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for (LanguageInfo languageInfo : infos) {
						languageInfo.setSelect(false);
					}
					info.setSelect(radio.isChecked());
					//noti(info.getId());
				}
			});

		}

		return view;
	}

	public void noti(int position) {
		notifyDataSetChanged();

		if (mListener != null) {
			mListener.changer(position);
		}
	}

	public void setRBListener(RBListener listener) {
		mListener = listener;
	}

	class ViewHolder {
		TextView tv_name;
		RadioButton rb_btn;
	}

	public RBListener mListener;

	public interface RBListener {
		void changer(int position);
	}

}
