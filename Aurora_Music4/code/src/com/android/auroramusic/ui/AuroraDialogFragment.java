package com.android.auroramusic.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import aurora.app.AuroraAlertDialog;

import com.android.auroramusic.util.DisplayUtil;
import com.android.music.R;
import com.aurora.internal.R.id;

public class AuroraDialogFragment extends DialogFragment {

	private static final String TAG = "AuroraDialogFragment";
	private Context mContext;
	private static int mleftoff = 0;

	private static String[] mShareStr = { "微信", "朋友圈", "微博", "其他", };
	private static Integer[] mImageIds = { R.drawable.aurora_share_wx, R.drawable.aurora_share_friends, R.drawable.aurora_share_xlwb, R.drawable.aurora_share_other, };

	private static AuroraDilogCallBack mCallback = null;

	public interface AuroraDilogCallBack {
		public void onFinishDialogFragment(int ret);
	}

	public AuroraDialogFragment(Context context) {
		this.mContext = context;
		if (mleftoff == 0) {
			mleftoff = DisplayUtil.dip2px(context, 26f);
		}
	}

	public static AuroraDialogFragment newInstance(Context context, final int messageId) {
		final AuroraDialogFragment frag = new AuroraDialogFragment(context);
		final Bundle args = new Bundle(2);
		args.putInt("messageId", messageId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final int messageId = getArguments().getInt("messageId");

		View mView = LayoutInflater.from(mContext).inflate(R.layout.aurora_dialogfragment, null);
		final GridView mGridView = (GridView) mView.findViewById(R.id.my_dialog_gridview);

		List<Map<String, Object>> mList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < mImageIds.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("PIC", mImageIds[i]);
			map.put("TITLE", mShareStr[i]);
			mList.add(map);
		}

		final BaseAdapter mAdapter = new SimpleAdapter(mContext, (List<Map<String, Object>>) mList, R.layout.aurora_dialog_girditem, new String[] { "PIC", "TITLE" }, new int[] { R.id.girdview_icon,
				R.id.girdview_text });

		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (mCallback != null) {
					mCallback.onFinishDialogFragment(position);
				}
				dismiss();
			}
		});

		AuroraAlertDialog mDialog = new AuroraAlertDialog.Builder(getActivity()).setTitle(messageId).setCancelable(true).setView(mView, mleftoff, 0, mleftoff, 0).setTitleDividerVisible(true)
				.setNegativeButton(R.string.songlist_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						dismiss();
					}
				}).create();

		mDialog.setCanceledOnTouchOutside(true);

		return mDialog;
	}

	public static void registerItemClickCallback(AuroraDilogCallBack callback) {
		if (mCallback != null && callback != null) {
			return;
		}
		mCallback = callback;
	}

}
