package com.aurora.voiceassistant.view;

import com.aurora.voiceassistant.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

public class CustomMenu extends PopupWindow {

	private Context mContext;
	private FrameLayout mFrameLayout;
	private ListView mListView;
	private String[] titles;
	private List<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
	private SimpleAdapter adapter;
	private LayoutInflater inflater;

	public CustomMenu(Context context) {
		this(context, null);
	}

	public CustomMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initDatas();
		initViewInWin();
		this.setFocusable(true);
		this.setAnimationStyle(R.style.PopupAnimation);
		this.setOutsideTouchable(true);
		this.setBackgroundDrawable(new BitmapDrawable());
		this.setContentView(mFrameLayout);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
	}

	public void initViewInWin() {
		inflater = (LayoutInflater) mContext
				.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		mFrameLayout = (FrameLayout) inflater.inflate(R.layout.vs_custom_menu,
				null);
		mFrameLayout.setFocusable(true);
		mFrameLayout.setFocusableInTouchMode(true);
		mFrameLayout.setOnKeyListener(new LayouOnKeyEvent());
		mListView = (ListView) mFrameLayout.findViewById(R.id.custom_menu_list);
		this.setOnDismissListener(new OnCustemMenuDismissListener());
		mListView.setAdapter(adapter);
	}

	public void initDatas() {
		titles = mContext.getResources().getStringArray(R.array.vs_custom_menu_items);
		for (int i = 0; i < titles.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("title", titles[i]);
			itemList.add(map);
		}
		adapter = new SimpleAdapter(mContext, itemList, R.layout.vs_menu_item,
				new String[] { "title" }, new int[] { R.id.item_title });
	}

	class LayouOnKeyEvent implements OnKeyListener {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& keyCode == KeyEvent.KEYCODE_MENU) {
				if (CustomMenu.this.isShowing()) {
					CustomMenu.this.dismiss();
				}
			}
			return false;
		}

	}

	class OnCustemMenuDismissListener implements OnDismissListener {
		@Override
		public void onDismiss() {
			Log.e("iuni-ht", "----------------------ishowing----------------------------");
			mIDissMissBackGround.onCallBack();
		}

	}

	IDissMissBackGround mIDissMissBackGround;

	public void setCallBack(IDissMissBackGround i) {
		mIDissMissBackGround = i;
	}

	interface IDissMissBackGround {
		void onCallBack();
	}

	public void setCustomMenuItemListener(OnItemClickListener l) {
		mListView.setOnItemClickListener(l);
	}

}
