package com.android.auroramusic.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

import com.android.auroramusic.ui.AuroraArtistBrowserActivity;
import com.android.auroramusic.ui.AuroraTrackBrowserActivity;
import com.android.auroramusic.util.Artist;
import com.android.auroramusic.widget.StickyListHeadersAdapter;
import com.android.music.R;

public class AuroraArtistListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	static class ViewHold {
		TextView tv_artistName;
		// TextView tv_trackCount;
		TextView tv_albumCount;
		RelativeLayout front;
		AuroraCheckBox cb;
		View header;
	}

	private int mItemHeight = 0;
	private int mSubHeight = 0;
	public boolean hasDeleted = false;
	private ArrayList<Artist> mList;
	private Context mContext;
	private static final int MSG_NEED_IN = 200001;
	private static final int MSG_NEED_OUT = 200002;
	private boolean mNeedout = false;
	private boolean mNeedin = false;
	private boolean mEditMode = false;
	private int mCurrentPosition = -1;
	private static final int WAIT_TIME = 50;
	private HashMap<Integer, Boolean> mCheckedMap = new HashMap<Integer, Boolean>();
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NEED_IN:
				mNeedin = false;
				break;
			case MSG_NEED_OUT:
				mNeedout = false;
				break;
			default:
				break;
			}
		};
	};

	public AuroraArtistListAdapter(Context context, ArrayList<Artist> list) {
		mList = list;
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mList != null)
			return mList.size();
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		if (mList != null)
			return mList.get(arg0);
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		final ViewHold vh;
		final int position = arg0;
		if (arg1 == null) {
			View artistView = LayoutInflater.from(mContext).inflate(R.layout.aurora_artistlist_item, null);
			arg1 = LayoutInflater.from(mContext).inflate(com.aurora.R.layout.aurora_slid_listview, null);
			((RelativeLayout) arg1.findViewById(com.aurora.R.id.aurora_listview_front)).addView(artistView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			RelativeLayout rl_control_padding = (RelativeLayout) arg1.findViewById(com.aurora.R.id.control_padding);
			rl_control_padding.setPadding(0, 0, (int) mContext.getResources().getDimension(com.aurora.R.dimen.checkbox_margin_right_in_listview_with_index), 0);
			vh = new ViewHold();
			vh.front = ((RelativeLayout) arg1.findViewById(com.aurora.R.id.aurora_listview_front));
			vh.tv_artistName = (TextView) artistView.findViewById(R.id.artist_name);
			vh.tv_albumCount = (TextView) artistView.findViewById(R.id.album_count);
			vh.cb = (AuroraCheckBox) arg1.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
//			RelativeLayout.LayoutParams rlp = new LayoutParams(LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.song_itemheight));
//			rlp.addRule(RelativeLayout.CENTER_VERTICAL);
//			rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//			rlp.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.aurora_checkbox_margin), 0);
//			((View) vh.cb).setLayoutParams(rlp);
			arg1.setTag(vh);
		} else
			vh = (ViewHold) arg1.getTag();
		vh.tv_artistName.setText(mList.get(arg0).mArtistName);
		vh.tv_albumCount.setText(mContext.getResources().getString(R.string.num_songs_and_album, mList.get(arg0).mAlbumNumber, mList.get(arg0).mSongNumber));

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(arg0);

		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (arg0 == getPositionForSection(section)) {
			char c = mList.get(position).mPinyin.charAt(0);
			if (c > 90 || c < 65) {
				c = '#';
			}
			vh.header = LayoutInflater.from(mContext).inflate(R.layout.aurora_artistheader_layout, null);
			((TextView) vh.header.findViewById(R.id.tv_artist_header)).setText(String.valueOf(c));
			if (arg1.findViewById(R.id.tv_artist_header) != null)
				((ViewGroup) arg1).removeViewAt(0);
			((ViewGroup) arg1).addView(vh.header, 0);
			vh.header.setVisibility(View.VISIBLE);
			if (hasDeleted) {
				ViewGroup.LayoutParams vl = arg1.getLayoutParams();
				if (null != vl) {
					vl.height = mItemHeight;
				}
				arg1.findViewById(com.aurora.R.id.content).setAlpha(255);
			}
		} else {
			if (arg1.findViewById(R.id.tv_artist_header) != null)
				((ViewGroup) arg1).removeViewAt(0);
			if (hasDeleted) {
				ViewGroup.LayoutParams vl = arg1.getLayoutParams();
				if (null != vl) {
					vl.height = mSubHeight;
				}
				arg1.findViewById(com.aurora.R.id.content).setAlpha(255);
			}
		}
		if (mNeedin) {
			AuroraListView.auroraStartCheckBoxAppearingAnim(vh.front, vh.cb);
		} else if (!mNeedin && mEditMode) {
			AuroraListView.auroraSetCheckBoxVisible(vh.front, vh.cb, true);
		}
		if (mNeedout) {
			AuroraListView.auroraStartCheckBoxDisappearingAnim(vh.front, vh.cb);
		} else if (!mEditMode && !mNeedout) {
			AuroraListView.auroraSetCheckBoxVisible(vh.front, vh.cb, false);
		}
		if (mEditMode) {
			arg1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!((AuroraCheckBox) vh.cb).isChecked()) {
						((AuroraCheckBox) vh.cb).auroraSetChecked(true, true);
						mCheckedMap.put(position, true);
						((AuroraArtistBrowserActivity) mContext).changeMenuState();
					} else {

						((AuroraCheckBox) vh.cb).auroraSetChecked(false, true);
						mCheckedMap.remove(position);
						((AuroraArtistBrowserActivity) mContext).changeMenuState();
					}
				}
			});
			arg1.setBackgroundResource(android.R.color.transparent);
		} else {
			arg1.setClickable(false);
			arg1.setBackgroundResource(R.drawable.aurora_playlist_item_clicked);
		}
		Boolean checked = mCheckedMap.get(position);
		if (checked != null && checked && mEditMode) {
			((AuroraCheckBox) vh.cb).setChecked(true);
		} else {
			((AuroraCheckBox) vh.cb).setChecked(false);
		}
		return arg1;
	}

	public int getPositionForSection(int section) {
		for (int i = 0; i < mList.size(); i++) {
			char c = mList.get(i).mPinyin.charAt(0);
			if (c > section && section != 35) {
				return -1;
			}
			if (c == section || mList.get(i).mPinyin.charAt(0) < 65 || mList.get(i).mPinyin.charAt(0) > 90) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return mList.get(position).mPinyin.charAt(0);
	}

	public void setSelected(int position) {
		mCurrentPosition = position;
	}

	public void setNeedout() {
		mNeedout = true;
		mHandler.sendEmptyMessageDelayed(MSG_NEED_OUT, WAIT_TIME);
	}

	public void setNeedin(int position) {
		mNeedin = true;
		mCheckedMap.clear();
		if (position - 1 >= 0)
			position -= 1;
		mCheckedMap.put(position, true);
		mHandler.sendEmptyMessageDelayed(MSG_NEED_IN, WAIT_TIME);
	}

	public void selectAll() {
		for (int i = 0; i < mList.size(); i++) {
			mCheckedMap.put(i, true);
		}
	}

	public void changeEidtMode(boolean flag) {
		mEditMode = flag;
	}

	public boolean getEidtMode() {
		return mEditMode;
	}

	public void selectAllNot() {
		mCheckedMap.clear();
	}

	public int getCheckedCount() {
		return mCheckedMap.size();
	}

	public HashMap getmCheckedMap() {
		return mCheckedMap;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(R.layout.aurora_artistheader_layout, parent, false);
		}
		TextView labelText = (TextView) convertView.findViewById(R.id.tv_artist_header);
		char c = mList.get(position).mPinyin.charAt(0);
		if (c > 90 || c < 65) {
			c = '#';
		}
		labelText.setText(String.valueOf(c));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		// TODO Auto-generated method stub
		String labelText = mList.get(position).mPinyin;
		if (!TextUtils.isEmpty(labelText)) {
			return labelText.charAt(0);
		} else {
			return 0;
		}
	}

	public boolean isItemChecked(int positon) {
		if (mCheckedMap.get(positon) == null)
			return false;
		return mCheckedMap.get(positon);
	}

	public void setItemChecked(int position, boolean flag) {
		mCheckedMap.put(position, flag);
	}

	public int getItemHeight() {
		return mItemHeight;
	}

	public void setItemHeight(int height, int headerHeight) {
		mItemHeight = height;
		mSubHeight = headerHeight - headerHeight;
	}
}
