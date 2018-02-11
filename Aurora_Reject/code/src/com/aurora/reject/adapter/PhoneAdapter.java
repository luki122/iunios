package com.aurora.reject.adapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gionee.provider.GnTelephony.SIMInfo;

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
import android.view.Gravity;
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
import aurora.widget.AuroraTextView;
import gionee.provider.GnTelephony.SIMInfo;

public class PhoneAdapter extends CursorAdapter implements RecyclerListener {
	private static final String TAG = "PhoneAdapter";
	private int auroraItemHegiht;
	private int itemHeaderHegiht;
	private static Uri uri = Uri.parse("content://com.android.contacts/black");
	private String name;
	private LayoutInflater mInflater;
	private Context context;
	private boolean mIsShowCheckBox = false;
	private SelectionManager<Integer> mSelectionManager;
	public int mAllShowCheckBox = 0;
	public boolean mNeedAnim = false;
	private int count;
	private int[] cardIcons = { R.drawable.svg_dial_card1, R.drawable.svg_dial_card2, R.drawable.aurora_sim_not_found };

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
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void onMovedToScrapHeap(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		CallHolder mHolder = (CallHolder) arg0.getTag();

		final ImageView simView = mHolder.simView;

		final String number = arg2.getString(arg2.getColumnIndex("number"));
		final String area = arg2.getString(arg2.getColumnIndex("area"));
		final long dateTime = Long.parseLong(arg2.getString(arg2
				.getColumnIndex("date")));
		name = arg2.getString(arg2.getColumnIndex("name"));
		String date = formatDateTime(dateTime);
		auroraBindSectionHeaderAndDivider(arg0, date, arg2);
		simView.setTag(name);
		final int simId = arg2.getInt(arg2.getColumnIndex("simId"));
		// int slotId = SIMInfo.getSlotById(context, simId);
		// List<SIMInfo> mInsertedSimInfoList =
		// SIMInfo.getInsertedSIMList(context);
		System.out.println("[bindView]slotId=" + simId);
		if (simId == 0 || simId == 1) {
			mHolder.simView.setImageResource(cardIcons[simId]);
			mHolder.simView.setVisibility(View.VISIBLE);
		} else {
			mHolder.simView.setVisibility(View.GONE);
		}

		// System.out.println("[bindView]slotId0=" +
		// mInsertedSimInfoList.get(0).mSimId);
		// System.out.println("[bindView]slotId1=" +
		// mInsertedSimInfoList.get(1).mSimId);
		// simView.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// final Intent intent = new Intent();
		// String name = (String) v.getTag();
		// Log.e(TAG, "name = " + name + "  number = " + number);
		// intent.setClassName("com.android.contacts",
		// "com.android.contacts.FullCallDetailActivity");
		// intent.putExtra("number", number);
		// intent.putExtra("black_name", name);
		// intent.putExtra("reject_detail", true);
		// intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//
		// String userMark = YuloreUtil.getUserMark(context, number);
		// String markContent = YuloreUtil.getMarkContent(number, context);
		// int markCount = YuloreUtil.getMarkNumber(context, number);
		// intent.putExtra("user-mark", userMark);
		// intent.putExtra("mark-content", markContent);
		// intent.putExtra("mark-count", markCount);
		//
		// try {
		// context.startActivity(intent);
		// } catch (ActivityNotFoundException e) {
		// e.printStackTrace();
		// }
		// }
		// });
		// if (RejectApplication.getInstance().isSelectMode == 2) {
		// LayoutTransition transitioner = new LayoutTransition();
		// ObjectAnimator disappear=ObjectAnimator.ofFloat(simView, "Alpha",
		// 1f, 0f);
		// disappear.setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING)
		// + 150);
		// disappear.start();
		// } else {
		// simView.setVisibility(View.VISIBLE);
		// LayoutTransition transitioner = new LayoutTransition();
		// ObjectAnimator appear=ObjectAnimator.ofFloat(simView, "Alpha",
		// 0f, 1f);
		// appear.setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING)
		// + 150);
		// appear.start();
		// }

		count = arg2.getInt(arg2.getColumnIndex("count"));
		if (count > 1) {
			mHolder.countView.setText("(" + count + ")");
			mHolder.countView.setVisibility(View.VISIBLE);
		} else {
			mHolder.countView.setVisibility(View.GONE);
		}
		if (name == null || "".equals(name)) {
			mHolder.title.setText(number);
		} else {
			mHolder.title.setText(name);
		}

		if (area == null || "".equals(area)) {
			mHolder.content.setText(arg1.getResources().getString(
					R.string.mars));
		} else {
			mHolder.content.setText(area);
		}

		String lable = arg2.getString(arg2.getColumnIndex("lable"));
		// if(lable==null){
		// lable=getLableByPhoneNumbers(context,
		// arg2.getString(arg2.getColumnIndex("number")));
		// }
		if (lable == null || "".equals(lable)) {
			mHolder.markView.setVisibility(View.GONE);
		} else {
			mHolder.markView.setText(" | " + lable);
			mHolder.markView.setVisibility(View.VISIBLE);
		}

		// CharSequence dateText = DateUtils.getRelativeTimeSpanString(
		// Long.parseLong(arg2.getString(arg2.getColumnIndex("date"))),
		// System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
		// DateUtils.FORMAT_ABBREV_RELATIVE);
		// String dates = dateText.toString().replaceAll(" ", "");
		// mHolder.markView.setText(replaceString(dates));

		if (mIsShowCheckBox) {
			if (mAllShowCheckBox == 1) {
				mHolder.cb.auroraSetChecked(true, true);
				// return;
			} else if (mAllShowCheckBox == 2) {
				mHolder.cb.auroraSetChecked(false, true);
				// return;
			}
			if (mHolder.cb.getAlpha() == 0.0f) {
				if (mNeedAnim) {
					aurora.widget.AuroraListView
							.auroraStartCheckBoxAppearingAnim(
									mHolder.front, mHolder.cb, true);// 启动进入编辑状态动画
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
		front.addView(mInflater.inflate(R.layout.call_log_list_item, null));
		CallHolder mHolder = new CallHolder(view);
		// auroraItemHegiht = arg0.getResources().getDimensionPixelSize(
		// R.dimen.twoline_height);
		// itemHeaderHegiht = arg0.getResources().getDimensionPixelSize(
		// R.dimen.item_header_height);
		view.setTag(mHolder);
		return view;
	}

	protected void auroraBindSectionHeaderAndDivider(View view, String date,
			Cursor mCursor) {
		CallHolder mHolder = (CallHolder) view.getTag();
		LinearLayout headerUi = mHolder.head;
		if (mCursor.isFirst()) {
			if (headerUi != null) {
				ViewGroup.LayoutParams params = headerUi.getLayoutParams();
				params.height = context.getResources().getDimensionPixelSize(
						R.dimen.item_header_height);
				System.out.println("[0bindView]params.height=" + params.height);
				AuroraTextView tv = new AuroraTextView(context);
				tv.setText(date);
				int paddingLeft = context.getResources().getDimensionPixelSize(
						R.dimen.activity_horizontal_margin);
				tv.setTextAppearance(context, R.style.calllog_list_header_style);
				tv.setHeight(params.height);
				tv.setPadding(paddingLeft, 0, 0, 0);
				tv.setGravity(Gravity.CENTER_VERTICAL);

				headerUi.setBackgroundColor(context.getResources().getColor(
						R.color.calllog_list_header_background_color));
				headerUi.removeAllViews();
				headerUi.setEnabled(false);
				headerUi.setClickable(false);
				headerUi.addView(tv);
				headerUi.setLayoutParams(params);
				headerUi.setVisibility(View.VISIBLE);
			}
		} else {
			mCursor.moveToPrevious();
			long preDate = Long.parseLong(mCursor.getString(mCursor
					.getColumnIndex("date")));
			mCursor.moveToNext();
			String preDateString = formatDateTime(preDate);
			if (!preDateString.equals(date)) {
				if (headerUi != null) {
					ViewGroup.LayoutParams params = headerUi.getLayoutParams();
					params.height = context.getResources()
							.getDimensionPixelSize(R.dimen.item_header_height);

					AuroraTextView tv = new AuroraTextView(context);
					tv.setText(date);
					int paddingLeft = context.getResources()
							.getDimensionPixelSize(
									R.dimen.activity_horizontal_margin);
					tv.setTextAppearance(context,
							R.style.calllog_list_header_style);
					tv.setHeight(params.height);
					tv.setPadding(paddingLeft, 0, 0, 0);
					tv.setGravity(Gravity.CENTER_VERTICAL);

					headerUi.setBackgroundColor(context
							.getResources()
							.getColor(
									R.color.calllog_list_header_background_color));
					headerUi.removeAllViews();
					headerUi.setEnabled(false);
					headerUi.setClickable(false);
					headerUi.addView(tv);
					headerUi.setLayoutParams(params);
					headerUi.setVisibility(View.VISIBLE);
				}
			} else {
				if (headerUi != null) {
					headerUi.setVisibility(View.GONE);
				}
			}
		}

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

	public static String getLableByPhoneNumbers(Context context, String address) {
		Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI,
				null, "number='" + address + "'" + " and reject=1", null, null);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("mark"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}

		return null;
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

	private static String from[] = { "十一", "一", "二", "三", "四", "五", "六", "七",
			"八", "九", "十" };
	private static String to[] = { "11", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "10" };

	public String replaceString(String src) {
		if (src == null) {
			return null;
		}
		for (int i = 0; i < from.length; i++) {
			src = src.replaceAll(from[i], to[i]);
		}
		return src;
	}

	private class CallHolder {
		TextView title;
		TextView markView;
		TextView content;
		ImageView simView;
		TextView countView;
		RelativeLayout front;
		AuroraCheckBox cb;
		LinearLayout head;

		private CallHolder(View view) {
			this.title = (TextView) view.findViewById(R.id.title);
			this.markView = (TextView) view.findViewById(R.id.mark);

			this.content = (TextView) view.findViewById(R.id.content);
			this.simView = (ImageView) view.findViewById(R.id.sim);
			this.countView = (TextView) view.findViewById(R.id.count);

			this.front = (RelativeLayout) view
					.findViewById(com.aurora.R.id.aurora_listview_front);
			this.cb = (AuroraCheckBox) view
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			this.head = (LinearLayout) view
					.findViewById(com.aurora.R.id.aurora_list_header);
		}
	}

	String formatPattern1 = "今天";
	String formatPattern2 = "昨天";
	String formatPattern3 = "MM月dd日";
	String formatPattern4 = "yyyy年MM月dd日";
	Date date;
	Calendar current;
	Calendar today;
	Calendar yesterday;
	Calendar thisyear;

	/**
	 * 格式化时间
	 * 
	 * @param timeStamp
	 * @return
	 */
	private String formatDateTime(long timeStamp) {
		date = new Date(timeStamp);

		// liyang add:
		current = Calendar.getInstance();// 当前

		today = Calendar.getInstance(); // 今天
		today.set(Calendar.YEAR, current.get(Calendar.YEAR));
		today.set(Calendar.MONTH, current.get(Calendar.MONTH));
		today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
		// Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		yesterday = Calendar.getInstance(); // 昨天
		yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
		yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
		yesterday.set(Calendar.DAY_OF_MONTH,
				current.get(Calendar.DAY_OF_MONTH) - 1);
		yesterday.set(Calendar.HOUR_OF_DAY, 0);
		yesterday.set(Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND, 0);

		thisyear = Calendar.getInstance(); // 今年
		thisyear.set(Calendar.YEAR, current.get(Calendar.YEAR));
		thisyear.set(Calendar.MONTH, 0);
		thisyear.set(Calendar.DAY_OF_MONTH, 0);
		thisyear.set(Calendar.HOUR_OF_DAY, 0);
		thisyear.set(Calendar.MINUTE, 0);
		thisyear.set(Calendar.SECOND, 0);
		// liyang add end.

		current.setTime(date);

		// return new SimpleDateFormat(formatPattern4).format(date);

		if (current.after(today)) {
			return formatPattern1;
		} else if (current.before(today) && current.after(yesterday)) {
			return formatPattern2;
		} else if (current.before(thisyear)) {
			return new SimpleDateFormat(formatPattern4).format(date);
		} else {
			return new SimpleDateFormat(formatPattern3).format(date);
		}
	}

}
