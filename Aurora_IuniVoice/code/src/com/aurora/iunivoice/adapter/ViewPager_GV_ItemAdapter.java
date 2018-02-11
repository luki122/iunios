package com.aurora.iunivoice.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.utils.DefaultUtil;
import com.aurora.iunivoice.utils.ImageLoaderHelper;

public class ViewPager_GV_ItemAdapter extends BaseAdapter {

	private List<String> list_info;
	private Context context;
	/** ViewPager页码 */
	private int index;
	/** 根据屏幕大小计算得到的每页item个数 */
	private int pageItemCount;
	/** 传进来的List的总长度 */
	private int totalSize;

	/** 当前页item的实际个数 */
	// private int itemRealNum;
	@SuppressWarnings("unchecked")
	public ViewPager_GV_ItemAdapter(Context context, List<?> list) {
		this.context = context;
		this.list_info = (List<String>) list;
	}

	public ViewPager_GV_ItemAdapter(Context context, List<?> list, int index, int pageItemCount) {
		this.context = context;
		this.index = index;
		this.pageItemCount = pageItemCount;
		list_info = new ArrayList<String>();
		totalSize = list.size();
		// itemRealNum=list.size()-index*pageItemCount;
		// 当前页的item对应的实体在List<?>中的其实下标
		int list_index = index * pageItemCount;
		for (int i = list_index; i < list.size(); i++) {
			list_info.add((String) list.get(i));
		}

	}

	@Override
	public int getCount() {
		int size = totalSize / pageItemCount;
		if (index == size)
			return totalSize - pageItemCount * index;
		else
			return pageItemCount;
		// return itemRealNum;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Holder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_twitter_selected_photo, null);
			holder = new Holder();
			holder.iv_img = (ImageView) convertView.findViewById(R.id.iv_selected_img);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		String url  = list_info.get(position);
	    ImageLoaderHelper.disPlay(url, holder.iv_img, DefaultUtil.getDefaultImageDrawable(context));
		
		return convertView;
	}

	private static class Holder {
		ImageView iv_img;
	}
	
}
