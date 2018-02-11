package com.android.auroramusic.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.auroramusic.util.Globals;
import com.android.music.R;

public class AuroraFoldListAdapter extends BaseAdapter {

	private static final String TAG = "AuroraFoldListAdapter";
	private LayoutInflater mInflater;
	private List<String> datas = new ArrayList<String>();
	private Context mContext;
	public boolean hasDeleted = false;
	private HashMap<String, Integer> defaultSize = new HashMap<String, Integer>();
	private boolean isRetain = false;
	private onRetainClickListener monRetainClickListener;

	public AuroraFoldListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	public AuroraFoldListAdapter(Context context, onRetainClickListener listener) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		monRetainClickListener = listener;
		if (listener != null) {
			isRetain = true;
		}
	}

	public void addFoldData(List<String> list) {
		if (list == null) {
			return;
		}
		datas = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {

		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {
		return datas.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public void deleteItem(int postion) {
		datas.remove(postion);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {

		HoldView holdView;
		if (arg1 == null) {
			holdView = new HoldView();
			arg1 = mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			View myView = mInflater.inflate(R.layout.aurora_fold_item, null);
			// 获取添加内容的对象
			RelativeLayout mainUi = (RelativeLayout) arg1.findViewById(com.aurora.R.id.aurora_listview_front);

			// 将要显示的内容添加到mainUi中去
			mainUi.addView(myView, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			// 垃圾桶层
			LinearLayout listBack = (LinearLayout) arg1.findViewById(com.aurora.R.id.aurora_listview_back);
			TextView rubbish = new TextView(mContext);
			rubbish.setGravity(Gravity.CENTER);
			rubbish.setTextColor(Color.parseColor("#ffffff"));
			rubbish.setText(mContext.getString(R.string.aurora_ignore));
			listBack.addView(rubbish, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			View rubishView = arg1.findViewById(com.aurora.R.id.aurora_rubbish);
			if (rubishView != null) {
				rubishView.setVisibility(View.GONE);
			}
			// 设置间距
			RelativeLayout rl_control_padding = (RelativeLayout) arg1.findViewById(com.aurora.R.id.control_padding);
			rl_control_padding.setPadding(0, 0, 0, 0);
			holdView.title = (TextView) myView.findViewById(R.id.aurora_title);
			holdView.summary = (TextView) myView.findViewById(R.id.aurora_summary);
			holdView.retain = (Button) myView.findViewById(R.id.aurora_retain_button);
			arg1.setTag(holdView);
		} else {
			holdView = (HoldView) arg1.getTag();
		}
		arg1.setClickable(false);
		if (hasDeleted) {
			ViewGroup.LayoutParams vl = arg1.getLayoutParams();
			if (null != vl) {
				vl.height = mContext.getResources().getDimensionPixelSize(R.dimen.aurora_song_item_height);
			}
			arg1.findViewById(com.aurora.R.id.content).setAlpha(255);
			arg1.setAlpha(255);
		}

		final String itemStr = datas.get(arg0);
		String title = itemStr.substring(itemStr.lastIndexOf("/") + 1);
		holdView.title.setText(title);
		if (isRetain) {
			holdView.retain.setVisibility(View.VISIBLE);
			holdView.retain.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					if (monRetainClickListener != null) {
						monRetainClickListener.onRetainClick(itemStr);
					}
				}
			});
		} else {
			holdView.retain.setVisibility(View.GONE);
		}

		new GetFoldSizeTask(holdView.summary, itemStr).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return arg1;
	}

	class HoldView {
		TextView title;
		TextView summary;
		Button retain;
	}

	class GetFoldSizeTask extends AsyncTask<Void, Void, Integer> {

		private TextView mTextView;
		private String path;

		public GetFoldSizeTask(TextView view, String path) {
			this.mTextView = view;
			this.path = path;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			StringBuilder where = new StringBuilder();
			where.append(Globals.QUERY_SONG_FILTER_1);
			where.append(" and " + MediaStore.Audio.Media.DATA + " like \"" + path + "%\"");

			Cursor cursor = mContext.getContentResolver().query(uri, new String[] { MediaStore.Audio.Media.DATA }, where.toString(), null, null);
			int count = cursor == null ? 0 : cursor.getCount();
			if (cursor.moveToFirst()) {
				do {
					String path1 = cursor.getString(0);
					path1 = path1.substring(0, path1.lastIndexOf("/"));

					// 防止取重复
					if (!path1.equals(path)) {
						count--;
					}
				} while (cursor.moveToNext());
			}
			if (cursor != null) {
				cursor.close();
			}
			return count;
		}

		@Override
		protected void onPostExecute(Integer result) {
			mTextView.setText(mContext.getString(R.string.song_size, result) + path + "/");
			defaultSize.put(path, result);
		}

		@Override
		protected void onPreExecute() {
			Integer size = defaultSize.get(path);
			if (size == null) {
				size = 0;
			}

			mTextView.setText(mContext.getString(R.string.song_size, size) + path + "/");
		}
	}

	public interface onRetainClickListener {
		public void onRetainClick(String path);
	}

}
