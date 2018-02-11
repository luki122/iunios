package com.aurora.reject;

import java.util.ArrayList;
import java.util.List;

import com.aurora.reject.adapter.BlackNameAdapter;
import com.aurora.reject.util.RejectApplication;
import com.aurora.reject.util.TotalCount;
import com.aurora.reject.util.YuloreUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

public class AuroraManuallyAddActivity extends AuroraActivity {

	private static Uri uri = Uri.parse("content://com.android.contacts/black");
	private ContentResolver mContentResolver;
	private AsyncQueryHandler mQueryHandler;
	private List<String> list = new ArrayList<String>();
	private AuroraActionBar mActionBar;
	private AuroraCheckBox phone, sms;
	private EditText number, name;
	private TextWatcher PhoneTextWatcher;
	private String add_number;
	private String add_name;
	private String contactName;
	private Bundle bundle;
	private String targetId;
	private String type;
	private boolean mIsAddFromOther = false;
	private static final int DIALOG_REFRESH = 1;
	private static final int AURORA_CONFIRM = 100;
	private boolean isShowing = false;
	private AuroraProgressDialog dialog;
	private String lable = null;
	private int userMark = -1;
	private String mNumberOrig = null;
	private String mNameOrig = null;
    // Aurora xuyong 2016-03-30 deleted for bug #21890 start
	//private String bef = null;
	// Aurora xuyong 2016-03-30 deleted for bug #21890 end
    private boolean touch = false;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// removeDialog(DIALOG_REFRESH);
			if (dialog != null) {
				dialog.dismiss();
			}
			isShowing = false;
			if (bundle == null || mIsAddFromOther) {
				ContentResolver cr = getContentResolver();
				ContentValues cv = new ContentValues();
				cv.put("isblack", 1);
				// if(lable!=null){
				cv.put("lable", lable);
				cv.put("user_mark", userMark);
				// }
				cv.put("number", add_number);
				cv.put("black_name", add_name);
				if (phone.isChecked() && sms.isChecked()) {
					cv.put("reject", 3);
				} else if (!phone.isChecked() && sms.isChecked()) {
					cv.put("reject", 2);
				} else if (phone.isChecked() && !sms.isChecked()) {
					cv.put("reject", 1);
				}
				Uri uris = cr.insert(uri, cv);
				System.out.println(uri.toString());
				new TotalCount(AuroraManuallyAddActivity.this, "180", "addbl",
						1).CountData();
			} else {
				ContentResolver cr = getContentResolver();
				ContentValues cv = new ContentValues();
				cv.put("isblack", 1);
				// if(lable!=null){
				cv.put("lable", lable);
				cv.put("user_mark", userMark);
				// }
				cv.put("number", add_number);
				cv.put("black_name", add_name);
				if (phone.isChecked() && sms.isChecked()) {
					cv.put("reject", 3);
				} else if (!phone.isChecked() && sms.isChecked()) {
					cv.put("reject", 2);
				} else if (phone.isChecked() && !sms.isChecked()) {
					cv.put("reject", 1);
				}
				int update = cr.update(uri, cv, "_ID=?",
						new String[] { targetId });
				System.out.println("updated" + ":" + update);
			}
			touch = false;
			AuroraManuallyAddActivity.this.finish();

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.manually_add, AuroraActionBar.Type.Normal);

		mContentResolver = getContentResolver();
		mQueryHandler = new QueryHandler(mContentResolver, this);
		startQuery();
		number = (AuroraEditText) findViewById(R.id.number);
		// number.set
		name = (AuroraEditText) findViewById(R.id.name);
		phone = (AuroraCheckBox) findViewById(R.id.phone);
		phone.setChecked(true);
		sms = (AuroraCheckBox) findViewById(R.id.sms);
		sms.setChecked(true);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		bundle = getIntent().getExtras();
		if (bundle != null) {
			System.out.println("bundle...................................");
			mNumberOrig = bundle.getString("add_number");
			// Aurora xuyong 2015-07-01 modified for bug #13978 start
			if (mNumberOrig.length() > 20) {
				mNumberOrig = mNumberOrig.substring(0, 20);
				// Aurora xuyong 2015-07-01 modified for bug #13978 end
			}
			number.setText(mNumberOrig);
			number.setSelection(mNumberOrig.length());
			mNameOrig = bundle.getString("add_name");
			if (mNameOrig != null && !"".equals(mNameOrig)) {
				// Aurora xuyong 2015-07-01 modified for bug #13978 start
				if (mNameOrig.length() > 20) {
					mNameOrig = mNameOrig.substring(0, 20);
					// Aurora xuyong 2015-07-01 modified for bug #13978 end
				}
				name.setText(mNameOrig);
			}
			targetId = bundle.getString("targetId");
			mIsAddFromOther = bundle.getBoolean("add");
			type = bundle.getString("type");
		} else {
			System.out
					.println("bundle is null..................................");
		}

		number.setFilters(new InputFilter[] { new InputFilter.LengthFilter(15) });
		name.setFilters(new InputFilter[] { new InputFilter.LengthFilter(15) });

		// if (type != null) {
		// if (Integer.parseInt(type) == 3) {
		// sms.setChecked(true);
		// phone.setChecked(true);
		//
		// } else if (Integer.parseInt(type) == 2) {
		// sms.setChecked(true);
		// phone.setChecked(false);
		// } else {
		// sms.setChecked(false);
		// phone.setChecked(true);
		// }
		// }

		phone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!sms.isChecked() && !phone.isChecked()) {
					phone.setChecked(true);
					Toast.makeText(
							AuroraManuallyAddActivity.this,
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.at_least_one),
							Toast.LENGTH_LONG).show();
				} else {
					phone.auroraSetIsAnimNeeded(true);
				}

			}
		});

		sms.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!phone.isChecked() && !sms.isChecked()) {
					sms.setChecked(true);
					Toast.makeText(
							AuroraManuallyAddActivity.this,
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.at_least_one),
							Toast.LENGTH_LONG).show();
				} else {
					sms.auroraSetIsAnimNeeded(true);
				}
			}
		});

		mActionBar = getAuroraActionBar();
		mActionBar.setTitle(R.string.manually);
		mActionBar.addItem(AuroraActionBarItem.Type.Done, AURORA_CONFIRM);
		mActionBar
				.setOnAuroraActionBarListener(auroraActionBarItemClickListener);

		PhoneTextWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				add_number = number.getText().toString().replace("-", "")
						.replace(" ", "");
				if (add_number != null || !"".equals(add_number)) {
                    // Aurora xuyong 2016-03-30 deleted for bug #21890 start
					/*if (contactName == null) {
						bef = name.getText().toString();
					}*/
                    // Aurora xuyong 2016-03-30 deleted for bug #21890 end
					contactName = getContactNameByPhoneNumber(
							AuroraManuallyAddActivity.this, add_number);
					if (contactName != null) {
						name.setText(contactName);
					} else {
                        // Aurora xuyong 2016-03-30 deleted for bug #21890 start
						/*if (bef != null || !"".equals(bef)) {
							name.setText(bef);
						} else {*/
                        // Aurora xuyong 2016-03-30 deleted for bug #21890 end
							name.setText("");
                        // Aurora xuyong 2016-03-30 deleted for bug #21890 start
						//}
                        // Aurora xuyong 2016-03-30 deleted for bug #21890 end

					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		};

		number.addTextChangedListener(PhoneTextWatcher);

	}

	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case AURORA_CONFIRM:
				doSaveAction();
			}
		}
	};

	// @Override
	// protected Dialog onCreateDialog(int id) {
	// switch (id) {
	// case DIALOG_REFRESH: {
	// if (dialog != null) {
	// removeDialog(DIALOG_REFRESH);
	// }
	// dialog = new AuroraProgressDialog(this);
	// dialog.setIndeterminate(true);
	// dialog.setCancelable(false);
	// dialog.setMessage(getResources().getString(R.string.aurora_save_title));
	// return dialog;
	// }
	// }
	// return null;
	// }
	private void showDialog() {
		if (dialog != null) {
			dialog.dismiss();
		}
		dialog = new AuroraProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.aurora_save_title));
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		doCanelAction();
	}
	
	protected void doCanelAction() {

		// TODO Auto-generated method stub
		if ((number.getText().toString() == null || "".equals(number.getText()
				.toString()))
				&& (name.getText().toString() == null || "".equals(name
						.getText().toString()))
				&& phone.isChecked()
				&& sms.isChecked() && !mIsAddFromOther) {
			AuroraManuallyAddActivity.this.finish();
		} else {
			AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
					AuroraManuallyAddActivity.this)
					.setTitle(
//							AuroraManuallyAddActivity.this.getResources()
//									.getString(R.string.give_up))
//					.setMessage(
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.is_give_up))
					.setPositiveButton(
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.aurora_exit_confirm),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									AuroraManuallyAddActivity.this.finish();
								}

							})
					.setNegativeButton(
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
			dialogs.setCanceledOnTouchOutside(false);

		}
	}

	protected void doSaveAction() {

		// TODO Auto-generated method stub
		if (touch) {
			return;
		}
		touch = true;
		add_number = number.getText().toString().replace("-", "")
				.replace(" ", "");
		add_name = name.getText().toString().replace("-", "").replace(" ", "");
		if (add_number != null && !"".equals(add_number)) {
			boolean isDigit = false;
			for (int i = 0; i < add_number.length(); i++) {
				if (Character.isDigit(add_number.charAt(i))) {
					isDigit = true;
				}
			}
			if (add_number.indexOf('+', 1) > 0) {
				isDigit = false;
			}
			if (!isDigit) {
				Toast.makeText(
						AuroraManuallyAddActivity.this,
						AuroraManuallyAddActivity.this.getResources()
								.getString(R.string.format), Toast.LENGTH_LONG)
						.show();
				touch = false;
				return;
			}
			if (bundle == null || mIsAddFromOther) {
				List<String> blaclist = RejectApplication.getInstance()
						.getBlackList();
				if (blaclist == null || !blaclist.contains(add_number)) {
					lable = YuloreUtil.getUserMark(
							AuroraManuallyAddActivity.this, add_number);

					if (lable == null) {
						showDialog();
						isShowing = true;
						new Thread() {
							public void run() {
								try {
									lable = YuloreUtil.getMarkContent(
											add_number,
											AuroraManuallyAddActivity.this);
									userMark = YuloreUtil.getMarkNumber(
											AuroraManuallyAddActivity.this,
											add_number);
									handler.obtainMessage().sendToTarget();
								} catch (Exception e) {
									e.printStackTrace();
									System.out.println("e.printStackTrace();");
								}

							};
						}.start();
					} else {
						userMark = -1;
						handler.obtainMessage().sendToTarget();
					}
				} else {
					Toast.makeText(
							AuroraManuallyAddActivity.this,
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.black_number_exist),
							Toast.LENGTH_LONG).show();
					touch = false;
					return;
				}

			} else {
				List<String> lists = RejectApplication.getInstance()
						.getBlackList();
				String str = null;
				lists.remove(mNumberOrig);
				if (mNumberOrig.startsWith("+86")) {
					str = mNumberOrig.substring(3);
					lists.remove(str);
				} else {
					str = "+86" + mNumberOrig;
					lists.remove(str);
				}
				if ((mNumberOrig != null && !mNumberOrig.equals(add_number) && lists
						.contains(add_number))) {
					Toast.makeText(
							AuroraManuallyAddActivity.this,
							AuroraManuallyAddActivity.this.getResources()
									.getString(R.string.black_number_exist),
							Toast.LENGTH_LONG).show();
					touch = false;
					return;
				}

				if (mNumberOrig != null
						|| !RejectApplication.getInstance().getBlackList()
								.contains(add_number)) {
					lable = YuloreUtil.getUserMark(
							AuroraManuallyAddActivity.this, add_number);

					if (lable == null) {
						showDialog();
						isShowing = true;
						new Thread() {
							public void run() {
								try {
									lable = YuloreUtil.getMarkContent(
											add_number,
											AuroraManuallyAddActivity.this);
									userMark = YuloreUtil.getMarkNumber(
											AuroraManuallyAddActivity.this,
											add_number);
									handler.obtainMessage().sendToTarget();
								} catch (Exception e) {
									// TODO: handle exception
									e.printStackTrace();
									System.out.println("e.printStackTrace();");
								}

							};
						}.start();

					} else {
						userMark = -1;
						handler.obtainMessage().sendToTarget();
					}
				}
			}
		} else {
			Toast.makeText(
					AuroraManuallyAddActivity.this,
					AuroraManuallyAddActivity.this.getResources().getString(
							R.string.no_number), Toast.LENGTH_LONG).show();
			touch = false;
			return;
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	private String getContactNameByPhoneNumber(Context context, String address) {
		if (TextUtils.isEmpty(address) || address.length() <= 3) {
			return "";
		}
		// aurora add by liguangyu for 16467
		Intent intent = new Intent("com.android.action.LAUNCH_CONTACTS_LIST");
		context.sendBroadcast(intent);
		// aurora add by liguangyu for 16467

		String[] projection = { ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };
        // Aurora xuyong 2016-03-30 modified for bug #21890 start
		/*Builder builder = Phone.CONTENT_FILTER_URI.buildUpon();
		builder.appendPath(address);*/
		Cursor cursor = context.getContentResolver().query(
			ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
			projection,
			ContactsContract.CommonDataKinds.Phone.NUMBER + " = '"
			+ address + "'", null, null);
		/*Cursor cursor = context.getContentResolver().query(builder.build(),
				projection, null, null, null);*/
        // Aurora xuyong 2016-03-30 modified for bug #21890 end
		if (cursor == null) {
			return null;
		}
		try {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				int nameFieldColumnIndex = cursor
						.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
				String name = cursor.getString(nameFieldColumnIndex);
				return name;
			}
			return null;
		} finally {
			cursor.close();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_BACK: {

			// TODO Auto-generated method stub
			if ((number.getText().toString() == null || "".equals(number
					.getText().toString()))
					&& (name.getText().toString() == null || "".equals(name
							.getText().toString()))
					&& phone.isChecked()
					&& sms.isChecked() && !mIsAddFromOther) {
				AuroraManuallyAddActivity.this.finish();
			} else {
				AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
						AuroraManuallyAddActivity.this)
						/*.setTitle(
								AuroraManuallyAddActivity.this.getResources()
										.getString(R.string.give_up))*/
						.setTitle(
								AuroraManuallyAddActivity.this.getResources()
										.getString(R.string.is_give_up))
						.setPositiveButton(
								AuroraManuallyAddActivity.this.getResources()
										.getString(R.string.aurora_exit_confirm),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										AuroraManuallyAddActivity.this.finish();
									}

								})
						.setNegativeButton(
								AuroraManuallyAddActivity.this.getResources()
										.getString(R.string.cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
				dialogs.setCanceledOnTouchOutside(false);

			}

			break;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startQuery() {
		// showDialog(DIALOG_REFRESH);
		mQueryHandler.startQuery(0, null, uri, null,
				"isblack=1 and reject in (1,2,3)", null, "_id desc");
	}

	private class QueryHandler extends AsyncQueryHandler {
		private final Context context;

		public QueryHandler(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			// removeDialog(DIALOG_REFRESH);

			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					list.clear();
				} else {
					list.clear();
					do {
						String number = cursor.getString(cursor
								.getColumnIndex("number"));
						if (number == null) {
							continue;
						}
						String numberE164 = PhoneNumberUtils
								.formatNumberToE164(number,
										AuroraBlackNameActivity
												.getCurrentCountryIso(context));

						list.add(number);

						if (numberE164 != null && !number.equals(numberE164)) {
							list.add(numberE164);
							continue;
						}

						try { // modify in the future
							if (numberE164 != null
									&& numberE164.equals(number)
									&& AuroraBlackNameActivity
											.getCurrentCountryIso(context)
											.equals("CN")
									&& number.startsWith("+86")) {
								numberE164 = number.substring(3,
										number.length());
								list.add(numberE164);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} while (cursor.moveToNext());
				}
				RejectApplication.getInstance().setBlackList(list);
				System.out.println(list.size()
						+ "                  list.size() ");
			}
			if (cursor != null) {
				cursor.close();
			}

		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onUpdateComplete(token, cookie, result);
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			// TODO Auto-generated method stub
			super.onInsertComplete(token, cookie, uri);
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			// TODO Auto-generated method stub
			super.onDeleteComplete(token, cookie, result);
			System.out.println("删除完毕" + result);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
