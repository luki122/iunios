package cn.com.xy.sms.sdk.ui.popu.widget;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.xy.sms.sdk.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class SelectDataAdapter extends BaseAdapter {

	public HashMap<String, Boolean> mCheckedStates;
	private JSONArray mDataSourceJsonArray = null;
	private String mDefaultSelectedStation;
	private LayoutInflater mLayoutInflater;

	public SelectDataAdapter(Context context, JSONArray dataSourceJsonArray,
			String defaultSelectedStation) {
		this.mDataSourceJsonArray = dataSourceJsonArray;
		this.mDefaultSelectedStation = defaultSelectedStation;
		this.mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mCheckedStates = new HashMap<String, Boolean>();

		int j = -1;
		for (int i = 0; i < mDataSourceJsonArray.length(); i++) {
			JSONObject jsonObject = mDataSourceJsonArray.optJSONObject(i);
			if (null == jsonObject) {
				continue;
			}
			String name = jsonObject.optString("name");
			if (name.equals(mDefaultSelectedStation)) {
				j = i;
			}
			if (j == i) {
				mCheckedStates.put(String.valueOf(i), true);
			} else {
				mCheckedStates.put(String.valueOf(i), false);
			}

			if (j == -1 && i == mDataSourceJsonArray.length() - 1) {
				mCheckedStates.put(String.valueOf(i), true);
			}
		}

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataSourceJsonArray.length();
	}

	@Override
	public Object getItem(int arg0) {
		JSONObject jsonObject = null;
		try {
			jsonObject = mDataSourceJsonArray.getJSONObject(arg0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int arg0, View convertView, ViewGroup arg2) {

		ViewHolder mViewHolder = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(
					R.layout.duoqu_list_items_contents, null);
			mViewHolder = new ViewHolder();
			mViewHolder.mItemRadioButton = (RadioButton) convertView
					.findViewById(R.id.item_rb);
			mViewHolder.mItemTextView = (TextView) convertView
					.findViewById(R.id.item_text);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		Object item = getItem(arg0);
		if (item != null) {
			mViewHolder.mItemTextView.setText(((JSONObject) item)
					.optString("name"));
		}

		mViewHolder.mItemRadioButton
				.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						for (String key : mCheckedStates.keySet()) {
							mCheckedStates.put(key, false);

						}
						mCheckedStates.put(String.valueOf(arg0),
								((RadioButton) v).isChecked());
						SelectDataAdapter.this.notifyDataSetChanged();
					}
				});
		convertView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				for (String key : mCheckedStates.keySet()) {
					mCheckedStates.put(key, false);

				}
				mCheckedStates.put(String.valueOf(arg0), true);
				SelectDataAdapter.this.notifyDataSetChanged();
			}
		});

		boolean res = false;
		if (mCheckedStates.get(String.valueOf(arg0)) == null
				|| mCheckedStates.get(String.valueOf(arg0)) == false) {
			res = false;
			mCheckedStates.put(String.valueOf(arg0), false);
		} else
			res = true;

		mViewHolder.mItemRadioButton.setChecked(res);
		return convertView;
	}

}

class ViewHolder {
	protected TextView mItemTextView;
	protected RadioButton mItemRadioButton;
}
