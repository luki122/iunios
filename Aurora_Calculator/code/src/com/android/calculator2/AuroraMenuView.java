package com.android.calculator2;

import java.util.List;

import com.android.calculator2.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

// Aurora liugj 2014-01-21 created for aurora's new feature
public class AuroraMenuView extends LinearLayout {
	private Context context;

	public AuroraMenuView(Context context) {
		super(context);
		this.context=context;
		// TODO Auto-generated constructor stub
	}
	
	/**
     * 根据菜单列表进行整个菜单的布局
     * @param items 菜单列表
     */
    public void layoutMenu(List<AuroraMenuItem> items) {
    	int count = items.size();
    	if (count == 1) {
			TextView singleView = (TextView) getItemView(items.get(0));
			singleView.setBackgroundResource(R.drawable.aurora_text_toolbar_single);
			singleView.setPadding(dip2px( context,15), 0, dip2px( context,15), 0);
			addView(singleView);
		}else if (count == 3) {
			TextView leftView = (TextView) getItemView(items.get(0));
			leftView.setBackgroundResource(R.drawable.aurora_text_toolbar_left);
			leftView.setPadding(dip2px( context,17), 0, dip2px( context,13), 0);
			addView(leftView, 0);
			TextView centerView = (TextView) getItemView(items.get(1));
			centerView.setBackgroundResource(R.drawable.aurora_text_toolbar_center);
			centerView.setPadding(dip2px( context,13), 0, dip2px( context,13), 0);
			addView(centerView, 1);
			TextView rightView = (TextView) getItemView(items.get(2));
			rightView.setBackgroundResource(R.drawable.aurora_text_toolbar_right);
			rightView.setPadding(dip2px( context,13), 0, dip2px( context,17), 0);
			addView(rightView, 2);
		}else if (count == 2) {
			TextView leftView = (TextView) getItemView(items.get(0));
			leftView.setBackgroundResource(R.drawable.aurora_text_toolbar_left);
			addView(leftView, 0);
			leftView.setPadding(dip2px( context,17), 0, dip2px( context,13), 0);
			TextView rightView = (TextView) getItemView(items.get(1));
			rightView.setBackgroundResource(R.drawable.aurora_text_toolbar_right);
			addView(rightView, 1);
			rightView.setPadding(dip2px( context,13), 0, dip2px( context,17), 0);
		}else {
			
		}
    }
    public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
    
    /**
     * 生成菜单项View
     * @param item 菜单项
     * @return 给定菜单项的View
     */
    private View getItemView(final AuroraMenuItem item) {
        Context context = getContext();
        TextView itemView = new TextView(context);
        itemView.setGravity(Gravity.CENTER);
        itemView.setTextSize(16);
        itemView.setTextColor(Color.WHITE);
        itemView.setId(item.getItemId());
        itemView.setText(item.getTitle());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuroraMenuItem.OnItemClickListener listener = item.getOnClickListener();
                if (listener != null) {
                    listener.onClick(item);
                }
            }
        });
        
        return itemView;
    }
}
