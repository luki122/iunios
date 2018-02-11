package com.aurora.reject.adapter;

import com.aurora.reject.R;
import com.aurora.reject.util.SelectionManager;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView.RecyclerListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

public class SmsAdapter extends CursorAdapter implements RecyclerListener {
	private int auroraItemHegiht;
	private static Uri uri = Uri.parse("content://com.android.contacts/black");

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

	public SmsAdapter(Context context, Cursor c) {
		super(context, c);
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {

		MessageHolder mHolder = (MessageHolder) arg0.getTag();
		AuroraCheckBox mCheckBox = (AuroraCheckBox) arg0
				.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		mHolder.content.setText(arg2.getString(arg2.getColumnIndex("body")));
		String address = arg2.getString(arg2.getColumnIndex("address"));
		String name = arg2.getString(arg2.getColumnIndex("name"));
		// if(address.startsWith("+86")){
		// name=getBlackNameByPhoneNumber(context, address.substring(3));
		// if(name==null||"".equals(name)){
		// name=getBlackNameByPhoneNumbers(context, address.substring(3));
		// }
		// }else{
		// name=getBlackNameByPhoneNumber(context, address);
		// if(name==null||"".equals(name)){
		// name=getBlackNameByPhoneNumbers(context, address);
		// }
		// }
		if (arg2.getInt(arg2.getColumnIndex("ismms")) == 1) {
			mHolder.attachment.setVisibility(View.VISIBLE);
		} else {
			mHolder.attachment.setVisibility(View.GONE);
		}

		count = arg2.getInt(arg2.getColumnIndex("count"));
		if (name == null || "".equals(name)) {
			if (count > 1) {
				mHolder.title.setText(address + "(" + count + ")");
			} else {
				mHolder.title.setText(address);
			}

		} else {
			if (count > 1) {
				mHolder.title.setText(name + "(" + count + ")");
			} else {
				mHolder.title.setText(name);
			}
		}
//		if (arg2.getInt(arg2.getColumnIndex("read")) == 1) {
//			mHolder.title.setTextColor(context.getResources().getColor(R.color.primary_text_color));
//		} else {
//			mHolder.title.setTextColor(context.getResources().getColor(R.color.unread_text_color));
//		}
		mHolder.date.setText(formatAuroraTimeStampString(context,
				Long.parseLong(arg2.getString(arg2.getColumnIndex("date"))),
				true));

		if (mIsShowCheckBox) {

			if (mAllShowCheckBox == 1) {
				mHolder.cb.auroraSetChecked(true, true);

			} else if (mAllShowCheckBox == 2) {
				mHolder.cb.auroraSetChecked(false, true);
			}
			if (mHolder.cb.getAlpha() == 0.0f) {
//				context.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
				ObjectAnimator oa = ObjectAnimator.ofFloat(mHolder.date,
						"translationX", -66);
				oa.setDuration(200).start();
				if (mNeedAnim) {
					aurora.widget.AuroraListView
							.auroraStartCheckBoxAppearingAnim(mHolder.front,
									mHolder.cb, true);// 启动进入编辑状态动画
				} else {
					aurora.widget.AuroraListView.auroraSetCheckBoxVisible(
							mHolder.front, mHolder.cb, true);
				}
			}
			if (mSelectionManager != null) {
				mHolder.cb.setChecked(mSelectionManager.isSelected(arg2
						.getPosition()));
			}
		} else {

			if (mHolder.cb.getAlpha() == 1.0f) {
				ObjectAnimator oa = ObjectAnimator.ofFloat(mHolder.date,
						"translationX", 0);
				oa.setDuration(200).start();
				aurora.widget.AuroraListView
						.auroraStartCheckBoxDisappearingAnim(mHolder.front,
								mHolder.cb);// 启动退出编辑状态动画
				mHolder.cb.setChecked(false);
			}

		}

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View view = (View) mInflater.inflate(
				com.aurora.R.layout.aurora_slid_listview, null, false);
		RelativeLayout front = (RelativeLayout) view
				.findViewById(com.aurora.R.id.aurora_listview_front);
		front.addView(mInflater.inflate(R.layout.sms_list_item, null));

		MessageHolder mHolder = new MessageHolder(view);
		view.setTag(mHolder);
		// auroraItemHegiht = view.getHeight();
		return view;
	}

	public static String formatAuroraTimeStampString(Context context,
			long when, boolean isConv) {
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
				int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT
						| DateUtils.FORMAT_ABBREV_ALL
						| DateUtils.FORMAT_CAP_AMPM;
				format_flags |= DateUtils.FORMAT_SHOW_TIME;
				sRet = DateUtils.formatDateTime(context, when, format_flags);
			} else {
				if (isConv) {
					sRet = DateFormat.format("MM-dd", when).toString();
				} else {
					sRet = DateFormat.format("yyyy-MM-dd  kk:mm", when)
							.toString();
				}
			}
		}
		return sRet;
	}

	// public static String getContactNameByPhoneNumber(Context context,
	// String address) {
	// String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
	// ContactsContract.CommonDataKinds.Phone.NUMBER };
	// Cursor cursor = context.getContentResolver().query(
	// ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	// projection,
	// ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"
	// + address + "'", null, null);
	// if (cursor == null) {
	// return null;
	// }
	// for (int i = 0; i < cursor.getCount(); i++) {
	// cursor.moveToPosition(i);
	// int nameFieldColumnIndex = cursor
	// .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
	// String name = cursor.getString(nameFieldColumnIndex);
	// return name;
	// }
	// return null;
	// }
	public static String getBlackNameByPhoneNumber(Context context,
			String address) {
		Cursor cursor = context.getContentResolver().query(uri, null,
				"number='" + address + "'", null, null);
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

	private class MessageHolder {
		private TextView title;
		private ImageView attachment;
		private TextView content;
		private TextView date;
		private AuroraCheckBox cb;
		private RelativeLayout front;

		private MessageHolder(View view) {
			this.title = (TextView) view.findViewById(R.id.sms_title);
			this.attachment = (ImageView) view.findViewById(R.id.mms);

			this.content = (TextView) view.findViewById(R.id.sms_content);
			this.date = (TextView) view.findViewById(R.id.sms_date);

			this.front = (RelativeLayout) view
					.findViewById(com.aurora.R.id.aurora_listview_front);
			this.cb = (AuroraCheckBox) view.findViewById(R.id.sms_checkbox);
		}
	}

}
