package com.aurora.reject.adapter;
import com.aurora.reject.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.RecyclerListener;
import android.widget.TextView;

public class MarkAdapter extends CursorAdapter implements RecyclerListener {
	private int auroraItemHegiht;
	private LayoutInflater mInflater;
	private Context context;
	
	public MarkAdapter(Context context, Cursor c) {
		super(context, c);
		this.context=context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		// TODO Auto-generated method stub

	}
	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		LayoutParams lp = arg0.getLayoutParams();
		if(lp != null)
		{
			lp.height = auroraItemHegiht;
		
			arg0.setLayoutParams(lp);
			
			arg0.findViewById(com.aurora.R.id.content).setAlpha(1.0f);
		}
		System.out.println("auroraItemHegiht="+auroraItemHegiht);
		RelativeLayout front = (RelativeLayout)arg0.findViewById(com.aurora.R.id.aurora_listview_front);
        TextView content=(TextView)((LinearLayout)(front.getChildAt(0))).getChildAt(0);
        content.setText(arg2.getString(arg2.getColumnIndex("lable")));

        LinearLayout deleteUi = (LinearLayout) arg0
                .findViewById(com.aurora.R.id.aurora_listview_back);
        ViewGroup.LayoutParams param = deleteUi.getLayoutParams();
        param.width = arg1.getResources().getDimensionPixelSize(
                R.dimen.aurora_list_item_delete_back_width);
        deleteUi.setLayoutParams(param);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View view = (View) mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null, false);
		RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		front.addView(mInflater.inflate(R.layout.mark_list_item, null));
		auroraItemHegiht = view.getHeight();
		return view;
	}

}
