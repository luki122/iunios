package com.aurora.iunivoice.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aurora.iunivoice.R;
import com.aurora.iunivoice.adapter.ViewPager_GV_ItemAdapter;
import com.aurora.iunivoice.adapter.ViewPager_GridView_Adapter;
import com.aurora.iunivoice.utils.DensityUtil;

public class GridViewGallery extends LinearLayout {

	private Context context;
	/** 图片列表 */
	private List<String> list;
	private ViewPager viewPager;
	/** 底部点 */
	private LinearLayout ll_dot;
	private ImageView[] dots;
	/** ViewPager当前页 */
	private int currentIndex;
	/** ViewPager页数 */
	private int viewPager_size;
	/** 默认一页6个item */
	private int pageItemCount = 6;
	/** GridView列数 */
	private int numColumns = 3;

	/** 保存每个页面的GridView视图 */
	private List<View> list_Views;
	
	private onGridViewItemClickListener gridViewItemClickListener;

	public GridViewGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.list = null;
		initView();
	}

	@SuppressWarnings("unchecked")
	public GridViewGallery(Context context, List<?> list) {
		super(context);
		this.context = context; 
		this.list = (List<String>) list;
		initView();
		initDots();
		setAdapter();
	}

	public void setPageItemCount(int pageItemCount) {
		this.pageItemCount = pageItemCount;
	}

	public void setList(List<String> list) {
		this.list = list;
		if (list != null && list.size() != 0) {
			int size = list.size();
			viewPager_size = size % pageItemCount == 0 ?  size / pageItemCount : (size / pageItemCount) + 1;
		}
		
		initDots();
		setAdapter();
	}

	private void setAdapter() {
		list_Views = new ArrayList<View>();
		for (int i = 0; i < viewPager_size; i++) {
			list_Views.add(getViewPagerItem(i));
		}
		viewPager.removeAllViews();
		viewPager.setAdapter(new ViewPager_GridView_Adapter(list_Views));
	}

	private void initView() {
		View view = LayoutInflater.from(context).inflate(R.layout.view_gridview_gallery, null);
		viewPager = (ViewPager) view.findViewById(R.id.vp_img);
		ll_dot = (LinearLayout) view.findViewById(R.id.ll_img_dot);
		addView(view);
	}

	// 初始化底部小圆点
	private void initDots() {

		ll_dot.removeAllViews();
		
		if (viewPager_size == 0) {
			return;
		}
		
		if (viewPager_size > 0) {
			if (1 == viewPager_size) {
				ll_dot.setVisibility(View.INVISIBLE);
			} else if (1 < viewPager_size) {
				ll_dot.setVisibility(View.VISIBLE);
				for (int j = 0; j < viewPager_size; j++) {
					ImageView image = new ImageView(context);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, 10);
					int width = DensityUtil.dip2px(getContext(), 2);
					params.setMargins(width, 0, width, 0);
					image.setImageResource(R.drawable.gridviewgallery_dot_selector);
					ll_dot.addView(image, params);
				}
			}
		}
		if (viewPager_size != 1) {
			dots = new ImageView[viewPager_size];
			for (int i = 0; i < viewPager_size; i++) {
				dots[i] = (ImageView) ll_dot.getChildAt(i);
				dots[i].setSelected(false);
				dots[i].setTag(i);
			}
			currentIndex = 0;
			dots[currentIndex].setSelected(true);
			viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					setCurDot(arg0);
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	/** 当前底部小圆点 */
	private void setCurDot(int positon) {
		if (positon < 0 || positon > viewPager_size - 1 || currentIndex == positon) {
			return;
		}
		for (int i = 0; i < viewPager_size; i++) {
			dots[i].setSelected(false);
		}
		dots[positon].setSelected(true);
		currentIndex = positon;
	}

	private View getViewPagerItem(int index) {
		GridView gridView = new GridView(context);
		gridView.setNumColumns(numColumns);  
		gridView.setVerticalSpacing(DensityUtil.dip2px(context, 4));

		ViewPager_GV_ItemAdapter adapter = new ViewPager_GV_ItemAdapter(context, list, index, pageItemCount);

		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (gridViewItemClickListener != null) {
					gridViewItemClickListener.ongvItemClickListener(view, position + currentIndex * pageItemCount);
				}
			}
		});
		return gridView;
	}
	
	public void setGridViewItemClickListener(
			onGridViewItemClickListener gridViewItemClickListener) {
		this.gridViewItemClickListener = gridViewItemClickListener;
	}

	public interface onGridViewItemClickListener {
		public abstract void ongvItemClickListener(View v, int postion);
	}
	
}
