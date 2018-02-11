package com.aurora.community.view;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.community.R;
import com.aurora.community.interfaces.IAppActionBar;

public class ActionBarLayout extends RelativeLayout implements IAppActionBar {

	private Context mContext;
	private int lastAddItemId = 0;
	private OnActionBarItemClickListener mOnActionBarItemClickListener;
	private int actionBarItemMargin = 0,actionBarItemPaddingTopBottom;
	private int actionBarPaddingSide = 0;
	
	private ArrayList<View> actionBarItems = new ArrayList<View>();
	
	private ImageButton backItem;
	private TextView titleView;
	
	private boolean isBackEnable = true;
	
	
	@SuppressLint("NewApi")
	public ActionBarLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupActionBar(context);
	}

	public ActionBarLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupActionBar(context);
	}

	public ActionBarLayout(Context context) {
		super(context);
		setupActionBar(context);
	}

	
	
	private void setupActionBar(Context context){
		mContext = context;
		setAttribute();
		addHomeView();
	}
	
	private LinearLayout homeView;
	
	private void addHomeView(){
		homeView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.action_home_layout, null);
		backItem = (ImageButton) homeView.findViewById(R.id.btn_back);
		homeView.setId(BACK_ITEM_ID);
		homeView.setOnClickListener(itemClickListener);
		titleView = (TextView) homeView.findViewById(R.id.tv_title);
		RelativeLayout.LayoutParams lp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(CENTER_VERTICAL);
		addView(homeView, lp);
	}
	
	private void setAttribute(){
		setGravity(Gravity.CENTER_VERTICAL);
		actionBarItemMargin = (int) mContext.getResources().getDimension(R.dimen.action_bar_margin_in_item);
		actionBarItemPaddingTopBottom = (int) mContext.getResources().getDimension(R.dimen.action_bar_padding_top_bottom);
		actionBarPaddingSide = (int)mContext.getResources().getDimension(R.dimen.action_bar_paddingleft);
	}
	
	@Override
	public void enableBackItem(boolean enable){
		isBackEnable = enable;
		backItem.setVisibility(enable ? View.VISIBLE:View.GONE);
		homeView.setOnClickListener(enable ? itemClickListener : null);
		homeView.setFocusable(enable);
		homeView.setClickable(enable);
	}
	@Override
	public void setBackItemRes(int imageRes){
		backItem.setImageResource(imageRes);
	}
	
	@Override
	public void setTitleRes(int res){
		titleView.setText(res);
	}
	@Override
	public void setTitleText(String title){
		titleView.setText(title);
	}
	@Override
	public void setTitleSize(float size){
		titleView.setTextSize(size);
	}
	@Override
	public void setTitleColor(int color){
		titleView.setTextColor(color);
	}
	@Override
	public void addActionBarItem(int imageRes,int Id){
		if(getActionBarItem(Id) != null)
		{
			return;
		}
		RelativeLayout.LayoutParams lp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(CENTER_VERTICAL);
		if(lastAddItemId == 0)
		{
		   lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}else{
		   lp.addRule(RelativeLayout.LEFT_OF, lastAddItemId);
		}
		lastAddItemId = Id;
		ImageButton item = new ImageButton(mContext);
		item.setBackgroundDrawable(null);
		item.setPadding(actionBarItemMargin, actionBarItemPaddingTopBottom, actionBarItemMargin, actionBarItemPaddingTopBottom);
		item.setLayoutParams(lp);
		item.setImageResource(imageRes);
		item.setId(Id);
		item.setOnClickListener(itemClickListener);
		addView(item);
		actionBarItems.add(item);
	}
	@Override
	public void changeActionBarItemImageRes(int imageRes,int Id){
		View temp = null;
		for(int i = 0;i< actionBarItems.size();i++)
		{
			temp = actionBarItems.get(i);
			if(temp.getId() == Id)
			{
				((ImageButton)temp).setImageResource(imageRes);
				break;
			}
		}
	}
	@Override
    public void addActionBarItem(String itemText,int Id){
		
		if(getActionBarItem(Id) != null)
		{
			return;
		}
		
    	RelativeLayout.LayoutParams lp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(CENTER_VERTICAL);
		if(lastAddItemId == 0)
		{
		   lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}else{
		   lp.addRule(RelativeLayout.LEFT_OF, lastAddItemId);
		}
		lastAddItemId = Id;
		final TextView item = (TextView) LayoutInflater.from(mContext).inflate(R.layout.actionbar_item_text, null);
		item.setLayoutParams(lp);
		item.setText(itemText);
		item.setId(Id);
		addView(item);
		actionBarItems.add(item);
		item.setOnClickListener(itemClickListener);
    }
	
	@Override
    public void removeActionBarItem(int Id){
    	View temp = null;
    	for(int i = 0;i < actionBarItems.size();i++)
    	{
    		temp = actionBarItems.get(i);
    		if(temp.getId() == Id)
    		{
    			removeView(temp);
    			if(i != actionBarItems.size()-1 && i != 0)
    			{
    				RelativeLayout.LayoutParams lp = (LayoutParams) actionBarItems.get(i+1).getLayoutParams();
    				lp.addRule(LEFT_OF, actionBarItems.get(i-1).getId());
    				actionBarItems.get(i+1).setLayoutParams(lp);
    			}
    			actionBarItems.remove(temp);
    			if(actionBarItems.size() > 0)
    			{
    				lastAddItemId = actionBarItems.get(actionBarItems.size()-1).getId();
    			}else{
    				lastAddItemId = 0;
    			}
    			break;
    		}
    	}
    }
	
	
	private OnClickListener itemClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if(mOnActionBarItemClickListener != null)
			{
				if(v.getId() == BACK_ITEM_ID)
				{
					if(!isBackEnable)
					{
						return;
					}
				}
				
				mOnActionBarItemClickListener.onClick(v, v.getId());
			}
			
		}
	};
	
	
	public void setOnActionBarItemClickListener(OnActionBarItemClickListener listener){
		
		this.mOnActionBarItemClickListener = listener;
		
	}
	
	public static interface OnActionBarItemClickListener{
		void onClick(View view,int id);
	}

	@Override
	public View getActionBarItem(int itemId) {
		
		for(int i = 0;i < actionBarItems.size();i++)
		{
			if(actionBarItems.get(i).getId() == itemId)
			{
				return actionBarItems.get(i);
			}
		}
		return null;
	}

	@Override
	public void setActionBarBg(int res) {
		// TODO Auto-generated method stub
		setBackgroundResource(res);
	}
	
}
