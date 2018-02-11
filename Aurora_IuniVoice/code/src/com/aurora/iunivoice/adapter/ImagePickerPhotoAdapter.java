package com.aurora.iunivoice.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.aurora.datauiapi.data.bean.PhotoInfo;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.picBrowser.RotateImageViewAware;
import com.aurora.iunivoice.utils.DefaultUtil;
import com.aurora.iunivoice.utils.ImageLoaderHelper;

public class ImagePickerPhotoAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private ArrayList<PhotoInfo> list;

	private Context context;

	private ViewHolder holder;

	private GridView grid;

	private ArrayList<PhotoInfo> mSelectedList = new ArrayList<PhotoInfo>();

	public int hasSelect = 1;

	private int leftSpace = 0;

	private int MAX_ITEM = 9;

	public ImagePickerPhotoAdapter(Context c, ArrayList<PhotoInfo> l,
			GridView g, int left) {
		this.context = c;
		this.list = l;
		this.inflater = LayoutInflater.from(context);
		this.grid = g;
		this.leftSpace = MAX_ITEM - left;
		Log.e("linp", "~~~~~~~~~~~~~~~~adapter left = " + left);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.list.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return this.list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public void refreshView(int index) {
		int visiblePos = grid.getFirstVisiblePosition();
		View view = grid.getChildAt(index - visiblePos);
		ViewHolder holder = (ViewHolder) view.getTag();
		PhotoInfo info = list.get(index);
		if (info.isChoose()) {
			final AnimationDrawable frame = (AnimationDrawable) context
					.getResources().getDrawable(
							R.drawable.quick_check_animation);
			holder.item_check.setImageDrawable(frame);
			frame.start();
			mSelectedList.add(info);
		} else {
			if (mSelectedList.contains(info)) {
				mSelectedList.remove(info);
			}
			holder.item_check.setImageDrawable(context.getResources()
					.getDrawable(R.drawable.quick_check_01));
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final PhotoInfo info = list.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater
					.inflate(R.layout.item_image_pick_photo, null);
			holder.item_photo = (ImageView) convertView
					.findViewById(R.id.item_iv_photo);
			holder.item_check = (ImageView) convertView
					.findViewById(R.id.item_iv_selected);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (list.get(position).isChoose()) {
			holder.item_check.setImageResource(R.drawable.quick_check_15);
		} else {
			holder.item_check.setImageResource(R.drawable.quick_check_01);
		}
		ImageLoaderHelper.disPlay(
				info.getPath_file(),
				new RotateImageViewAware(holder.item_photo, info
						.getPath_absolute()), DefaultUtil
						.getDefaultImageDrawable(context));
		// Log.e("jadon", "image path = "+info.getPath_absolute());
		holder.item_check.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (info.isChoose()) {
					if (mSelectedList.contains(info)) {
						mSelectedList.remove(info);
					}
					((ImageView) arg0).setImageDrawable(context.getResources()
							.getDrawable(R.drawable.quick_check_01));
					info.setChoose(false);
					// hasSelect--;
				} else if (mSelectedList.size() < leftSpace) {
					final AnimationDrawable frame = (AnimationDrawable) context
							.getResources().getDrawable(
									R.drawable.quick_check_animation);
					((ImageView) arg0).setImageDrawable(frame);

					frame.start();
					mSelectedList.add(info);
					info.setChoose(true);
					// hasSelect++;
				} else {
					Toast.makeText(context, "最多选择" + MAX_ITEM + "张图片！",
							Toast.LENGTH_SHORT).show();
				}
				Log.e("linp", "~~~~~~~~~~~~~onCLick");
			}
		});
		return convertView;
	}

	class ViewHolder {
		public ImageView item_photo;
		public ImageView item_check;
	}

	public ArrayList<PhotoInfo> getSelectedList() {

		return mSelectedList;
	}

	public void setCurrentLeft(int current) {
		this.leftSpace = MAX_ITEM - current;
	}

	public void clear() {
		mSelectedList.clear();
	}
}
