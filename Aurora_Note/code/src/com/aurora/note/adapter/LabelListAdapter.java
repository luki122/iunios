/**
 * 
 */
package com.aurora.note.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import aurora.app.AuroraAlertDialog;

import com.aurora.note.NoteMainActivity;
import com.aurora.note.R;
import com.aurora.note.activity.LabelTabActivity2;
import com.aurora.note.bean.LabelResult;
import com.aurora.note.db.LabelAdapter;
import com.aurora.note.util.Globals;

import java.util.List;

/**
 * 标签列表Adapter
 * @author JimXia
 * @date 2014-4-10 下午4:06:05
 */
public class LabelListAdapter extends BaseAdapter {
	private List<LabelResult> mLabelList;
	private Context mContext;
	private NoteMainActivity mActivity;
	private LabelAdapter mLabelDb;
	private AuroraAlertDialog mDeleteLabelDialog;

	public LabelListAdapter(Context context, List<LabelResult> list, NoteMainActivity activity) {
		mLabelList = list;
		mContext = context;
		mActivity = activity;
		mLabelDb = new LabelAdapter(context);
		mLabelDb.open();
	}

	@Override
	public int getCount() {
		return mLabelList.size();
	}

	@Override
	public Object getItem(int position) {
		return mLabelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.label_list_item, parent, false);
			holder = new ViewHolder();
			holder.labelTv = (TextView) convertView.findViewById(R.id.label);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		LabelResult result = mLabelList.get(position);
		final String labelContent = result.getContent();
		holder.labelTv.setText(labelContent);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Bundle bl = new Bundle();
				bl.putString("labelname", labelContent);

				Intent intent = new Intent(mContext, LabelTabActivity2/*LabelTabActivity*/.class);
				intent.putExtras(bl);
				mActivity.startActivityForResult(intent, Globals.REQUEST_CODE_MODIFY_LABEL);
			}
		});

		final int labelId = result.getId();
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				mDeleteLabelDialog = new AuroraAlertDialog.Builder(mActivity)
						// .setTitle(R.string.dialog_delete_label_title)
						.setMessage(R.string.note_delete_label_notice)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								mLabelDb.deleteDataById(String.valueOf(labelId));
								mActivity.initLabelAdapter();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.create();
				mDeleteLabelDialog.show();

				return false;
			}
		});

		return convertView;
	}
	
	private static class ViewHolder {
		TextView labelTv;
	}
}