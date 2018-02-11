package com.aurora.iunivoice.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.aurora.iunivoice.R;

public class MenuPopupWindow extends PopupWindow {

	private View popupView;
	
	private ListView lv_menus;
	
	private MenuAdapter menuAdapter;
	
	private OnBottomMenuClickListener bottomMenuClickListener;
	private ArrayList<String> menus = new ArrayList<String>();
	public MenuPopupWindow(Context context){
		popupView = LayoutInflater.from(context).inflate(R.layout.menu_pop_layout,null);
		setContentView(popupView);
		lv_menus = (ListView) popupView.findViewById(R.id.lv_menus);
		setBackgroundDrawable(new BitmapDrawable());
		setAnimationStyle(R.style.bottom_menu_style);
		menuAdapter = new MenuAdapter(menus, context);
		lv_menus.setAdapter(menuAdapter);
		lv_menus.setOnItemClickListener(menuItemClickListener);
		setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
		popupView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					if(isShowing());
					{
						dismiss();
					}
					break;
				}
				return true;
			}
		});
	}
	
	public void setOnBottomMenuClickListener(OnBottomMenuClickListener onBottomMenuClickListener){
		this.bottomMenuClickListener = onBottomMenuClickListener;
	}
	
	private OnItemClickListener menuItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			dismiss();
			if(bottomMenuClickListener != null)
			{
				bottomMenuClickListener.onMenuClick(position, menus.get(position));
			}
		}
	};
	
	public static interface OnBottomMenuClickListener{
		void onMenuClick(int position,String menuText);
	}
	
	public void addMenu(String menuText){
		menus.add(menuText);
		menuAdapter.notifyDataSetChanged();
	}
	
}

class MenuAdapter extends BaseAdapter{
	private ArrayList<String> menus;
	private Context context;
	private LayoutInflater inflater;
	public MenuAdapter(ArrayList<String> menus, Context context) {
		super();
		this.menus = menus;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return menus.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return menus.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.menu_text, null);
		}
		((TextView)convertView.findViewById(R.id.tv_menu_text)).setText(menus.get(position));
		return convertView;
	}
}
