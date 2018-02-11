package com.aurora.reject.adapter;

import com.aurora.reject.R;
import com.aurora.reject.util.RejectApplication;
import com.aurora.reject.util.SelectionManager;
import com.aurora.reject.util.YuloreUtil;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.Toast;
import aurora.widget.AuroraCheckBox;

public class PhoneAdapter extends CursorAdapter implements RecyclerListener {
	private static final String TAG = "PhoneAdapter";
	private int auroraItemHegiht;
	private static Uri uri = Uri
			.parse("content://com.android.contacts/black");
	private String name;
	private LayoutInflater mInflater;
	private Context context;
	private boolean mIsShowCheckBox = false;
	private SelectionManager<Integer> mSelectionManager;
	public int mAllShowCheckBox = 0;
	public boolean mNeedAnim = false;
	private int count;
	
	public void updateAllCheckBox(int allShow) {
		this.mAllShowCheckBox = allShow;
	}
	public void setCheckBoxAnim(boolean needAnim) {
		this.mNeedAnim = needAnim;
	}
	public void showCheckBox(boolean show) {
        mIsShowCheckBox = show;
    }
	public void setSelectionManager(SelectionManager<Integer> selectionManager) {
        mSelectionManager = selectionManager;
    }
	public PhoneAdapter(Context context, Cursor c) {
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
		final RelativeLayout front = (RelativeLayout)arg0.findViewById(com.aurora.R.id.aurora_listview_front);
        final AuroraCheckBox mCheckBox = (AuroraCheckBox) arg0.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
        final TextView title=(TextView)((RelativeLayout)(front.getChildAt(0))).getChildAt(0);
        final TextView content=(TextView)((RelativeLayout)(front.getChildAt(0))).getChildAt(1);
        final TextView date=(TextView) ((LinearLayout)((RelativeLayout)(front.getChildAt(0))).getChildAt(2)).getChildAt(0);
        final ImageView detail=(ImageView) ((LinearLayout)((RelativeLayout)(front.getChildAt(0))).getChildAt(2)).getChildAt(1);
        
        final String number = arg2.getString(arg2.getColumnIndex("number"));
        name=arg2.getString(arg2.getColumnIndex("name"));
//        if(name==null||"".equals(name)){
//        	System.out.println(number);
//        	name=getBlackNameByPhoneNumbers(context, number);
//        	System.out.println("name=null");
//        }
        detail.setTag(name);
        detail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Intent intent = new Intent();
				String name = (String) v.getTag();
				Log.e(TAG, "name = " + name + "  number = " + number);
				intent.setClassName("com.android.contacts",
						"com.android.contacts.AuroraCallDetailActivity");
				intent.putExtra("number", number);
				intent.putExtra("black_name", name);
				intent.putExtra("reject_detail", true);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

				String userMark = YuloreUtil.getUserMark(context, number);
				String markContent = YuloreUtil.getMarkContent(number, context);
				int markCount = YuloreUtil.getMarkNumber(context, number);
				intent.putExtra("user-mark", userMark);
				intent.putExtra("mark-content", markContent);
				intent.putExtra("mark-count", markCount);

				try {
					context.startActivity(intent);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
        if(RejectApplication.getInstance().isSelectMode==2){
        	LayoutTransition transitioner = new LayoutTransition();
	        ObjectAnimator disappear=ObjectAnimator.ofFloat(detail, "Alpha", 1f, 0f);	       
	        disappear.setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING) + 150);
			disappear.start();
		}else {
			detail.setVisibility(View.VISIBLE);
			LayoutTransition transitioner = new LayoutTransition();
		    ObjectAnimator appear=ObjectAnimator.ofFloat(detail, "Alpha", 0f, 1f);	       
		    appear.setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING) + 150);
		    appear.start();
		}
        
        count=arg2.getInt(arg2.getColumnIndex("count"));
        if(name==null||"".equals(name)){
        	if(count>1){
        		title.setText(number+"("+count+")");
        	}else{
        		title.setText(number);
        	}
        	String lable=arg2.getString(arg2.getColumnIndex("lable"));
//        	if(lable==null){
//        		lable=getLableByPhoneNumbers(context, arg2.getString(arg2.getColumnIndex("number")));
//        	}
			if(lable==null||"".equals(lable)){
				String area=arg2.getString(arg2.getColumnIndex("area"));
				if(area==null||"".equals(area)){
					content.setText(arg1.getResources().getString(R.string.mars)); 
				}else{
					content.setText(area);
				}
			}else {
				content.setText(lable);
			}
        	
        	
        }else{
        	if(count>1){
        		title.setText(name+"("+count+")");
        	}else{
        		title.setText(name);
        	}
        	
        	content.setText(number);
        }
        
        CharSequence dateText = DateUtils.getRelativeTimeSpanString(
        		Long.parseLong(arg2.getString(arg2.getColumnIndex("date"))), System.currentTimeMillis(),
    			DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
    	String dates = dateText.toString().replaceAll(" ", "");   
    	date.setText(replaceString(dates));

    	if (mIsShowCheckBox) {
        	if (mAllShowCheckBox == 1) {
				mCheckBox.auroraSetChecked(true, true);
//				return;
			}else if (mAllShowCheckBox == 2) {
				mCheckBox.auroraSetChecked(false, true);
//				return;
			}
        	if (mCheckBox.getAlpha() == 0.0f) {
        		if (mNeedAnim) {
            		aurora.widget.AuroraListView.auroraStartCheckBoxAppearingAnim(front, mCheckBox, true);//启动进入编辑状态动画
        		}else {
        			aurora.widget.AuroraListView.auroraSetCheckBoxVisible(front, mCheckBox, true);
				}
    		}
        	if (mSelectionManager != null) {
        		mCheckBox.setChecked(mSelectionManager.isSelected(arg2.getPosition()));
			}
        } else {
        	if (mCheckBox.getAlpha() == 1.0f){
        		aurora.widget.AuroraListView.auroraStartCheckBoxDisappearingAnim(front, mCheckBox);//启动退出编辑状态动画
                mCheckBox.setChecked(false);
        	}
           
        }
		
    

	}

	
	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View view = (View) mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null, false);
		RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
		front.addView(mInflater.inflate(R.layout.call_log_list_item, null));
		auroraItemHegiht = view.getHeight();
		return view;
	}
	
	
//	public static String getContactNameByPhoneNumber(Context context,
//			String address) {
//		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
//				ContactsContract.CommonDataKinds.Phone.NUMBER };
//		Cursor cursor = context.getContentResolver().query(
//				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//				projection,
//				ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"
//						+ address + "'", null, null);
//		if (cursor == null) {
//			return null;
//		}
//		for (int i = 0; i < cursor.getCount(); i++) {
//			cursor.moveToPosition(i);
//			int nameFieldColumnIndex = cursor
//					.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
//			String name = cursor.getString(nameFieldColumnIndex);
//			return name;
//		}
//		return null;
//	}
	
	public static String getBlackNameByPhoneNumber(Context context,String address){
		Cursor cursor = context.getContentResolver().query(uri, null, "number='"+address+"'", null, null);
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}
		
		return null;
	}
	
	public static String getBlackNameByPhoneNumbers(Context context,
			String address) {

		Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI,
				null, "number='" + address + "'" + " and reject=1", null, null);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor
						.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}

		return null;
	}
	
	
	public static String getLableByPhoneNumber(Context context, String address) {
		Cursor cursor = context.getContentResolver().query(uri, null,
				"number='" + address + "'", null, null);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("lable"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}

		return null;
	}
	
	public static String getLableByPhoneNumbers(Context context,String address){
		Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, "number='"+address+"'"+" and reject=1", null, null);
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("mark"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}
		
		return null;
	}

	
	
	public static String formatAuroraTimeStampString(Context context, long when, boolean isConv) {
    	Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();
        String sRet;
                
        if (then.year != now.year) {
        	if (isConv) {
                sRet = DateFormat.format("yyyy-MM-dd", when).toString();
        	} else {
        		sRet = DateFormat.format("yyyy-MM-dd  kk:mm", when).toString();
        	}
        } else {            
            if (then.yearDay == now.yearDay) {
            	int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                        DateUtils.FORMAT_ABBREV_ALL |
                        DateUtils.FORMAT_CAP_AMPM;
                format_flags |= DateUtils.FORMAT_SHOW_TIME;
                sRet = DateUtils.formatDateTime(context, when, format_flags);
            } else {
            	if (isConv) {
            		sRet = DateFormat.format("MM-dd", when).toString();
            	} else {
                    sRet = DateFormat.format("yyyy-MM-dd  kk:mm", when).toString();
                }
            }
        }
        return sRet;
    }
	
	
	
	
	private static String from[]={"十一","一","二","三","四","五","六","七","八","九","十"};
	private static String to[] = {"11","1","2","3","4","5","6","7","8","9","10"} ;
	
	public String replaceString(String src) 
	{
		if(src == null) {
			return null;
		} 
		for(int i=0; i <from.length;i++) 
		{ 
			src = src.replaceAll(from[i],to[i]); 
		} 
		return src; 
	} 

	
	


	
}
