package com.aurora.reject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.location.CountryDetector;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;

import com.aurora.reject.adapter.BlackNameAdapter;
import com.aurora.reject.util.AuroraBatch;
import com.aurora.reject.util.RejectApplication;
import com.aurora.reject.util.SelectionManager;
import com.aurora.reject.util.TotalCount;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import android.text.TextUtils;

public class AuroraBlackNameActivity extends AuroraActivity implements OnItemClickListener,
		OnItemLongClickListener {
	private static String TAG= "AuroraBlackNameActivity";
	private boolean isInBatchDel=false;
	private boolean blackBatch=false;
	private boolean touch=false;
	private Cursor pcursor;	
	private List<Integer> lists;
	private String num="";
	private ContentResolver cr;
	private ContentValues cv;
	private List<String> nums;
	private List<String> names;
	private List<Integer> rejects;
	private boolean isShowing=false;
	private String targetNumber;
	private String targetName;
	private String targetId;
	private String type;
	private static Uri uri = Uri
			.parse("content://com.android.contacts/black");
	private static final int ACTION_BTN_ADD = 1;
	private AuroraActionBar mActionBar;
	private ContentResolver mContentResolver;
	private AsyncQueryHandler mQueryHandler;
	private static final int SHOW_BUSY = 0;
	private static final int SHOW_LIST = 1;
	private static final int SHOW_EMPTY = 2;
	private int mState;
	private static final int DIALOG_REFRESH = 1;
	private AuroraProgressDialog mDialog;
	private AuroraListView black_name_list;
	private TextView black_name_empty;
	private BlackNameAdapter mListAdapter = null;
    private boolean isInSlideDelete=false;
	private boolean isbatch = false;
	private AuroraBatch<Integer> auroraBatch;
	private int mSelectCount = 0;
	private Map<Integer, Integer> mThreadsMap = new HashMap<Integer, Integer>();
//	private Cursor listCursor;
	private List<String> list=new ArrayList<String>();
	private StringBuilder mBlackNumbers = new StringBuilder();
	private ContentObserver changeObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfUpdate) {
			if(!blackBatch){
				if(!isInSlideDelete){
					startQuery();
				}
			}
			Log.i(TAG,"onChange.................................");
		}
	};
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.arg1==0){
				isShowing=true;
				showDialog();
				Log.i(TAG,"isShowing=true");
			}else{
				Log.i(TAG,"isShowing=false");
				touch=false;
				blackBatch=false;
				startQuery();
				if(auroraBatch != null) {
					auroraBatch.leaveSelectionMode();
				}
			}
			
		
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.black_name_list,
				AuroraActionBar.Type.Normal);
		mActionBar = getAuroraActionBar();
		addAuroraActionBarItem(AuroraActionBarItem.Type.Add, ACTION_BTN_ADD);
		mActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
		mActionBar.setTitle(getResources().getString(R.string.black));

		

		black_name_list = (AuroraListView) findViewById(R.id.black_name_list);
		black_name_list.setOnItemClickListener(this);
		black_name_list.setOnItemLongClickListener(this);
		// 打开滑动删除功能
		black_name_list.auroraSetNeedSlideDelete(true);
		black_name_list
				.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {

					@Override
					public void auroraOnClick(final int position) {// 点击了垃圾桶的响应事件
//						 View view=LayoutInflater.from(AuroraBlackNameActivity.this).inflate(R.layout.black_remove, null);
//						  final AuroraCheckBox black_remove=(AuroraCheckBox)view.findViewById(R.id.black_remove);
//						  black_remove.setChecked(true);

						if (mDialog != null) {
							mDialog.dismiss();
						}
						isShowing = true;
						isInSlideDelete = true;
						showDialog();
						black_name_list.auroraDeleteSelectedItemAnim();
						black_name_list
								.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {

									@Override
									public void auroraDeleteItem(View arg0,
											int arg1) {
										// TODO Auto-generated method stub
										new Thread() {
											public void run() {
												Cursor pcursor = (Cursor) black_name_list
														.getItemAtPosition(position);
												if (pcursor == null) {
													return;
												}
												String num = pcursor.getString(pcursor
														.getColumnIndex("number"));
												ContentResolver cr = getContentResolver();
												ContentValues cv = new ContentValues();

//												if (black_remove.isChecked()) {
													cv.put("isblack", 0);

//												} else {
//													cv.put("isblack", -1);
//												}
												cv.put("number", num);
												cv.put("reject",
														pcursor.getInt(pcursor
																.getColumnIndex("reject")));
												cv.put("black_name",
														pcursor.getString(pcursor
																.getColumnIndex("black_name")));
												int uri2 = cr.update(
														Uri.parse("content://com.android.contacts/black"),
														cv, "number=?",
														new String[] { num });
												Log.i(TAG,"updated"
														+ ":" + uri2);
												startQuery();
												new TotalCount(
														AuroraBlackNameActivity.this,
														"180", "deletebl", 1)
														.CountData();
											};
										}.start();
									}
								});                     	
	                        
//						  AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(AuroraBlackNameActivity.this)
//			                .setTitle(AuroraBlackNameActivity.this.getResources().getString(R.string.black_remove))
//			                .setView(view)
//			                .setPositiveButton(android.R.string.ok,
//			                    new DialogInterface.OnClickListener() {
//			                        @Override
//			                        public void onClick(DialogInterface dialog, int whichButton) {
//			                        	dialog.dismiss();
//			                        	isShowing=true;
//			                        	isInSlideDelete=true;
//										showDialog();
//			                        	black_name_list.auroraDeleteSelectedItemAnim();
//			                        	black_name_list.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {
//											
//											@Override
//											public void auroraDeleteItem(View arg0, int arg1) {
//												// TODO Auto-generated method stub
//												new Thread(){
//					                        		public void run() {
//					                        			Cursor pcursor = (Cursor) black_name_list
//							    								.getItemAtPosition(position);
//							    						if (pcursor == null) {
//							    							return;
//							    						}
//							    						String num = pcursor.getString(pcursor.getColumnIndex("number"));
//							                        	ContentResolver cr = getContentResolver();
//						                				ContentValues cv = new ContentValues();
//						                				
//							                        	if(black_remove.isChecked()){
//							                				cv.put("isblack", 0);
//							                				
//							                        	}else{
//							                        		cv.put("isblack", -1);
//							                        	}
//							                        	cv.put("number", num);
//							                        	cv.put("reject", pcursor.getInt(pcursor.getColumnIndex("reject")));
//							                        	cv.put("black_name", pcursor.getString(pcursor.getColumnIndex("black_name")));
//							                        	int uri2 = cr.update(Uri
//						                						.parse("content://com.android.contacts/black"), cv, "number=?", new String[] { num });
//						                				Log.i(TAG,"updated" + ":" + uri2);
//						                				startQuery();
//						                				new TotalCount(AuroraBlackNameActivity.this, "180", "deletebl", 1).CountData();
//					                        		};
//					                        	}.start();
//											}
//										});                        	
//			                        }
//			                    }
//			                )
//			                .setNegativeButton(android.R.string.cancel,   
//			                		new DialogInterface.OnClickListener() {
//			                    @Override
//			                    public void onClick(DialogInterface dialog, int whichButton) {
//			                    	dialog.dismiss();
//			                    }
//			                }).show();
//						  dialogs.setCanceledOnTouchOutside(false);
					}

					// 准备滑动删除的响应事件，滑动之前允许用户做一些初始化操作
					@Override
					public void auroraPrepareDraged(int position) {

					}

					// 成功拖出垃圾桶之后的响应事件
					@Override
					public void auroraDragedSuccess(int position) {

					}

					// 进行了拖动垃圾桶操作，但是没有成功，比如只拖动了一点点
					@Override
					public void auroraDragedUnSuccess(int position) {

					}
				});
		black_name_list.setRecyclerListener(mListAdapter);

		black_name_empty = (TextView) findViewById(R.id.black_name_empty);

		mContentResolver = getContentResolver();
		mQueryHandler = new QueryHandler(mContentResolver, this);
//		startQuery();
		mContentResolver.registerContentObserver(uri, true, changeObserver);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(!isInBatchDel){
			blackBatch=false;
			startQuery();
			Log.i(TAG,"startQuery();");
		}
		
		if(black_name_list!=null){
			black_name_list.auroraOnResume();
		}

	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		blackBatch=true;
		if(black_name_list!=null){
			black_name_list.auroraOnPause();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mContentResolver.unregisterContentObserver(changeObserver);
		if(mListAdapter != null) {
			mListAdapter.changeCursor(null);
		}
	}

	private void startQuery() {
//		showDialog(DIALOG_REFRESH);
		mQueryHandler.startQuery(0, null, uri, null, "isblack=1 and reject in (1,2,3)", null, "_id desc");

	}

	private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case ACTION_BTN_ADD:
				setAuroraSystemMenuCallBack(auroraMenuCallBack);
				setAuroraMenuItems(R.menu.black_name);
				AuroraBlackNameActivity.this.showAuroraMenu(AuroraBlackNameActivity.this.getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT,
						0, 0);
				break;
			default:
				break;
			}
		}
	};

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int arg0) {
			// TODO Auto-generated method stub
			switch (arg0) {
			case R.id.del:
				 if(touch){
  					return;
  				}
  				touch=true;
				delAllSelected();
				
				break;
			case R.id.contact:
				final Intent contactIntent = new Intent();
				contactIntent.setClassName("com.android.contacts", "com.android.contacts.activities.AuroraSimContactListActivity");
				contactIntent.putExtra("blackname_select", true);
				if (!TextUtils.isEmpty(mBlackNumbers)) {
					contactIntent.putExtra("blacknumbers", mBlackNumbers.toString().substring(0, mBlackNumbers.toString().length() - 1));
				}
				try {
					startActivity(contactIntent);
				} catch (ActivityNotFoundException a) {
				    a.printStackTrace();
				}
				
				break;
			case R.id.phone:
				final Intent callLogIntent = new Intent("com.aurora.action.blackname.select.calllog");
				if (!TextUtils.isEmpty(mBlackNumbers)) {
					callLogIntent.putExtra("blacknumbers", mBlackNumbers.toString().substring(0, mBlackNumbers.toString().length() - 1));
				}
				startActivity(callLogIntent);
				break;
			case R.id.sms:
				final Intent SmsIntent = new Intent();
				SmsIntent.setClassName("com.android.mms", "com.aurora.mms.ui.AuroraRejConvOperActivity");
				SmsIntent.putExtra("isFromReject", true);
				try {
					startActivity(SmsIntent);
				} catch (ActivityNotFoundException a) {
				    a.printStackTrace();
				}
				break;
			case R.id.manually:
				Intent intent = new Intent(getApplicationContext(),
						AuroraManuallyAddActivity.class);
				startActivity(intent);
				break;
				 // SIMInfo simInfo = SIMInfoWrapper.getDefault().getSimInfoById(simId);
			case R.id.sim_one_black_call:
				Intent intent1 = AuroraTelephoneManager.getCallNumberIntent(targetNumber, 0);
                startActivity(intent1);
				break;
			case R.id.sim_two_black_call:
				Intent intent2 = AuroraTelephoneManager.getCallNumberIntent(targetNumber, 1);
                startActivity(intent2);
				break;
			case R.id.black_call:
				Intent intents = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + targetNumber));
                startActivity(intents);
				break;
			case R.id.black_sms:
				  Uri uri = Uri.parse("smsto:"+targetNumber);            
				  Intent it = new Intent(Intent.ACTION_SENDTO, uri);                  
				  startActivity(it);  
				break;
			case R.id.black_edit:
                Intent intentss=new Intent(AuroraBlackNameActivity.this, AuroraManuallyAddActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("targetId", targetId);
                bundle.putString("type", type);
                bundle.putString("add_number", targetNumber); 
                if(targetName!=null){
                	bundle.putString("add_name", targetName);
                }else{
                	bundle.putString("add_name", "");
                }
                intentss.putExtras(bundle); 
                AuroraBlackNameActivity.this.startActivity(intentss);
				break;
			case R.id.black_remove:
				  View view=LayoutInflater.from(AuroraBlackNameActivity.this).inflate(R.layout.black_remove, null);
				  final AuroraCheckBox black_remove=(AuroraCheckBox)view.findViewById(R.id.black_remove);
				  black_remove.setChecked(true);
				  AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(AuroraBlackNameActivity.this)
	                .setTitle(AuroraBlackNameActivity.this.getResources().getString(R.string.black_remove))
	                .setView(view)
	                .setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	ContentResolver cr = getContentResolver();
                				ContentValues cv = new ContentValues();
	                        	if(black_remove.isChecked()){
	                				cv.put("isblack", 0);
	                				
	                        	}else{
	                        		cv.put("isblack", -1);
	                        	}
	                        	cv.put("number", targetNumber);
	                        	cv.put("reject", Integer.parseInt(type));
	                        	cv.put("black_name", targetName);
	                        	int uri2 = cr.update(Uri
                						.parse("content://com.android.contacts/black"), cv, "number=?", new String[] { targetNumber });
                				Log.i(TAG,"updated" + ":" + uri2);
	                        	 dialog.dismiss();
	                        	
	                        }
	                    }
	                )
	                .setNegativeButton(android.R.string.cancel,   
	                		new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	dialog.dismiss();
	                    }
	                }).show();
				  dialogs.setCanceledOnTouchOutside(false);
				break;

			}

		}
	};
	
	private void delAllSelected(){
	
		View view=LayoutInflater.from(AuroraBlackNameActivity.this).inflate(R.layout.black_remove, null);
		  final AuroraCheckBox black_remove=(AuroraCheckBox)view.findViewById(R.id.black_remove);
		  black_remove.setChecked(true);
		  String title = null;
		  int selectedCount = auroraBatch.getSelected().size();
		  if (selectedCount == 1) {
			  title = this.getResources().getString(R.string.aurora_remove_one_bn);
		  } else {
			  if (selectedCount < mListAdapter.getCount()) {
				  title = this.getResources().getString(R.string.aurora_remove_multi_bn, selectedCount);
			  } else {
				  title = this.getResources().getString(R.string.aurora_remove_all_bn);
			  }
		  }
		  AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(AuroraBlackNameActivity.this)
          .setTitle(title)
          .setView(view)
          .setPositiveButton(R.string.aurora_remove_confirm,
              new DialogInterface.OnClickListener() {
                  @Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								new Thread(){
									public void run() {
										isInBatchDel=true;
										blackBatch=true;
										Message message=handler.obtainMessage();
										message.arg1=0;
									    message.sendToTarget();
										lists = auroraBatch.getSelected();
										nums=new ArrayList<String>();
										names=new ArrayList<String>();
										rejects=new ArrayList<Integer>();
										new TotalCount(AuroraBlackNameActivity.this, "180", "deletebl", lists.size()).CountData();
										for (int i = 0; i < lists.size(); i++) {
											pcursor = (Cursor) black_name_list
													.getItemAtPosition(lists.get(i));
											nums.add(pcursor.getString(pcursor.getColumnIndex("number")));
											names.add(pcursor.getString(pcursor.getColumnIndex("black_name")));
											rejects.add(pcursor.getInt(pcursor.getColumnIndex("reject")));
											
										}
									   
										for (int i = 0; i < lists.size(); i++) {
											cr = getContentResolver();
											cv = new ContentValues();
											if (black_remove.isChecked()) {
												cv.put("isblack", 0);

											} else {
												cv.put("isblack", -1);
											}
											Log.i(TAG,"i="+i);
											
											num = nums.get(i);
											cv.put("number", num);
											cv.put("black_name",
													names.get(i));
											cv.put("reject",rejects.get(i));

											int uri2 = cr.update(
													Uri.parse("content://com.android.contacts/black"),
													cv, "number=?",
													new String[] { num });
											Log.i(TAG,"updated" + ":" + uri2);

										}
										 message=handler.obtainMessage();
									     message.arg1=1;
									     message.sendToTarget();
									};
								}.start();
						
								

							}
              }
          )
          .setNegativeButton(android.R.string.cancel,   
          		new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int whichButton) {
              	dialog.dismiss();
              	touch=false;
              }
          }).show();
		  dialogs.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if(keyCode == KeyEvent.KEYCODE_BACK){
						touch=false;
					}
					return false;
				}
			});
		  dialogs.setCanceledOnTouchOutside(false);
	}
	
	public static final String getCurrentCountryIso(Context context) {
        CountryDetector detector =
                (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
        return detector.detectCountry().getCountryIso();
    }

	private class QueryHandler extends AsyncQueryHandler {
		private final Context context;

		public QueryHandler(ContentResolver cr, Context context) {
			super(cr);
			this.context = context;
		}

		
		//todo lgy
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
//			removeDialog(DIALOG_REFRESH);
//			listCursor=cursor;
//			new Thread(){
//				public void run() {};
//			}.start();
			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					updateState(SHOW_EMPTY);	
				} else if (mListAdapter == null) {
					mListAdapter = new BlackNameAdapter(context, cursor);
					black_name_list.setAdapter(mListAdapter);
					updateState(SHOW_LIST);
				} else {
					mListAdapter.changeCursor(cursor);
					updateState(SHOW_LIST);
					mListAdapter.notifyDataSetChanged();
				}
			} else {
				if (mListAdapter != null) {
					mListAdapter.changeCursor(null);
					mListAdapter.notifyDataSetChanged();
				}
				updateState(SHOW_EMPTY);
			}
//			removeDialog(DIALOG_REFRESH);
			isInSlideDelete=false;
			if(mDialog!=null){
				mDialog.dismiss();
				isInBatchDel=false;
			}
			isShowing=false;
			if(cursor!=null){
				if(!cursor.moveToFirst()){
					list.clear();
					mBlackNumbers = null;
					cursor.close();
				}else{
					list.clear();
					mBlackNumbers = new StringBuilder();
					do{
						String number = cursor.getString(cursor.getColumnIndex("number"));
						if (number == null) {
							continue;
						}
						String numberE164 = PhoneNumberUtils.formatNumberToE164(
				                number, getCurrentCountryIso(context));
						Log.i(TAG,"numberE164="+numberE164);
						list.add(number);
						mBlackNumbers.append("'");
						mBlackNumbers.append(number);
						mBlackNumbers.append("',");
						
						if (numberE164 != null && !number.equals(numberE164)) {
							list.add(numberE164);
							mBlackNumbers.append("'");
							mBlackNumbers.append(numberE164);
							mBlackNumbers.append("',");
							continue;
						}
						
						try { // modify in the future
				        	if (numberE164 != null && numberE164.equals(number)
				        			&& getCurrentCountryIso(context).equals("CN") && number.startsWith("+86")) {
				            	numberE164 = number.substring(3, number.length());
				            	list.add(numberE164);
								mBlackNumbers.append("'");
								mBlackNumbers.append(numberE164);
								mBlackNumbers.append("',");
				            }
				        } catch (Exception e) {
				        	e.printStackTrace();
				        }
						
					}while(cursor.moveToNext());
					cursor.moveToFirst();
				}
				RejectApplication.getInstance().setBlackList(list);
				Log.i(TAG,list.size()+"                  list.size() ");
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
			Log.i(TAG,"删除完毕" + result);
		}

	}

	private void initThreadsMap() {
		if (mListAdapter == null || mListAdapter.getCount() == 0) {
			return;
		}
		Cursor cursor = mListAdapter.getCursor();
		cursor.moveToPosition(-1);
		int i = 0;
		while (cursor.moveToNext()) {
			if (mThreadsMap.get(i) == null) {
				mThreadsMap.put(i++, i - 1);
			}
		}
	}

	private void updateState(int state) {
		if (mState == state) {
			return;
		}

		mState = state;
		switch (state) {
		case SHOW_LIST:
			black_name_empty.setVisibility(View.GONE);
			black_name_list.setVisibility(View.VISIBLE);
			break;
		case SHOW_EMPTY:
			black_name_list.setVisibility(View.GONE);
			black_name_empty.setVisibility(View.VISIBLE);
			break;
		case SHOW_BUSY:
			black_name_list.setVisibility(View.GONE);
			black_name_empty.setVisibility(View.GONE);
//			showDialog(DIALOG_REFRESH);
			break;
		}
	}

	// showDialog（）回调此方法
//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//		case DIALOG_REFRESH: {
//			if (dialog != null) {
//				removeDialog(DIALOG_REFRESH);
//			}
//			dialog = new AuroraProgressDialog(this);
//			dialog.setIndeterminate(true);
//			dialog.setCancelable(false);
//			dialog.setMessage(getResources().getString(R.string.dels));
//			return dialog;
//		}
//		}
//		return null;
//	}
	
	private void showDialog(){
		if (mDialog == null) {
			mDialog = new AuroraProgressDialog(this);
			mDialog.setIndeterminate(true);
			mDialog.setCancelable(false);
			mDialog.setMessage(getResources().getString(R.string.removing));
		}
		mDialog.show();
	}

	// launchMode
	// 为singleTask的时候，通过Intent启到一个Activity,如果系统已经存在一个实例，系统就会将请求发送到这个实例上，但这个时候，系统就不会再调用通常情况下我们处理请求数据的onCreate方法，而是调用onNewIntent方法
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		if (auroraBatch == null) {
			isbatch = true;
			setAuroraBottomBarMenuCallBack(auroraMenuCallBack); // 必须写在布局前
			mActionBar.initActionBottomBarMenu(R.menu.aurora_list_menu_remove,
					1);
			mActionBar.showActionBarDashBoard();
			initThreadsMap();
			initAuroraBatch();
			auroraBatch.enterSelectionMode(false, position);
			return true;
		}
		return false;

	}

	private void initAuroraBatch() {
		auroraBatch = new AuroraBatch<Integer>(mActionBar, this) {
			@Override
			public void enterSelectionMode(boolean autoLeave,
					Integer itemPressing) {
				black_name_list.auroraSetNeedSlideDelete(false);
				black_name_list.auroraEnableSelector(false);// Item点击效果的设置
				mListAdapter.showCheckBox(true);
				mListAdapter.setCheckBoxAnim(true);
				super.enterSelectionMode(autoLeave, itemPressing);
				mListAdapter.notifyDataSetChanged();
			}

			@Override
			public Set getDataSet() {
				Set<Integer> dataSet = new HashSet<Integer>(mThreadsMap.size());
				for (int i = 0; i < mThreadsMap.size(); i++)
					dataSet.add(mThreadsMap.get(i));
				Log.i(TAG,dataSet.toString()
						+ "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				return dataSet;
			}

			@Override
			public void leaveSelectionMode() {
				mActionBar.setShowBottomBarMenu(false);
				mActionBar.showActionBarDashBoard();
				isbatch = false;
				if (black_name_list != null) {
					black_name_list.auroraSetNeedSlideDelete(true);
					black_name_list.auroraEnableSelector(true);
				}
				if (mListAdapter != null) {
					mListAdapter.showCheckBox(false);
					mListAdapter.setCheckBoxAnim(false);
					mListAdapter.updateAllCheckBox(0);
				}
				
				if (auroraBatch != null) {
					auroraBatch.destroyAction();
				}
				if (mListAdapter != null) {
					mListAdapter.notifyDataSetChanged();
				}
				if (auroraBatch != null) {
					auroraBatch = null;
				}
				if (mThreadsMap != null) {
					mThreadsMap.clear();
				}

			}

			@Override
			public void updateUi() {
				mSelectCount = null != getSelected() ? getSelected().size() : 0;
				mActionBar.getAuroraActionBottomBarMenu()
						.setBottomMenuItemEnable(0,
								mSelectCount == 0 ? false : true);
			}

			@Override
			public void updateListView(int allShow) {
				mListAdapter.updateAllCheckBox(allShow);
				mListAdapter.notifyDataSetChanged();
			}

			@Override
			public void bindToAdapter(SelectionManager<Integer> selectionManager) {
				mListAdapter.setSelectionManager(selectionManager);
			}
		};

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (null != auroraBatch && auroraBatch.isInSelectionMode()) {
			AuroraCheckBox mCheckBox = (AuroraCheckBox) view
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			if (mCheckBox == null) {
				return;
			}
			boolean isChecked = mCheckBox.isChecked();
			mCheckBox.auroraSetChecked(!isChecked, true);
			auroraBatch.getSelectionManger().toggle(position);
		} else {
//			setAuroraSystemMenuCallBack(auroraMenuCallBack);
//			if(RejectApplication.getInstance().MTK_GEMINI_SUPPORT){
//				if(SimUtils.isShowDoubleButton(AuroraBlackNameActivity.this)){
//					setAuroraMenuItems(R.menu.black_name_menu_mul);
//				}else{
//					setAuroraMenuItems(R.menu.black_name_menu);
//				}
//			}else{
//				setAuroraMenuItems(R.menu.black_name_menu);
//			}
			showDialogMenu();
//			showAuroraMenu();
			Cursor pcursor = (Cursor) black_name_list
					.getItemAtPosition(position);
			if (pcursor == null) {
				return;
			}
			targetNumber = pcursor.getString(pcursor.getColumnIndex("number"));
			targetName= pcursor.getString(pcursor.getColumnIndex("black_name"));
			targetId=pcursor.getString(pcursor.getColumnIndex("_id"));
			type=pcursor.getString(pcursor.getColumnIndex("reject"));
		}

	}

	private void showDialogMenu() {
		if (RejectApplication.getInstance().MTK_GEMINI_SUPPORT) {
			if (SimUtils.isShowDoubleButton(AuroraBlackNameActivity.this)) {
				AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
						AuroraBlackNameActivity.this).setItems(
						R.array.black_name_menu_mul, new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0,
									int position) {
								switch (position) {
								case 0:
									Intent intent1 = AuroraTelephoneManager
											.getCallNumberIntent(targetNumber,
													0);
									startActivity(intent1);
									break;
								case 1:
									Intent intent2 = AuroraTelephoneManager
											.getCallNumberIntent(targetNumber,
													1);
									startActivity(intent2);
									break;
								case 2:
									Uri uri = Uri
											.parse("smsto:" + targetNumber);
									Intent it = new Intent(
											Intent.ACTION_SENDTO, uri);
									startActivity(it);
									break;
								case 3:
									Intent intentss = new Intent(
											AuroraBlackNameActivity.this,
											AuroraManuallyAddActivity.class);
									Bundle bundle = new Bundle();
									bundle.putString("targetId", targetId);
									bundle.putString("type", type);
									bundle.putString("add_number", targetNumber);
									if (targetName != null) {
										bundle.putString("add_name", targetName);
									} else {
										bundle.putString("add_name", "");
									}
									intentss.putExtras(bundle);
									AuroraBlackNameActivity.this
											.startActivity(intentss);
									break;
								case 4:
									View view = LayoutInflater.from(
											AuroraBlackNameActivity.this)
											.inflate(R.layout.black_remove,
													null);
									final AuroraCheckBox black_remove = (AuroraCheckBox) view
											.findViewById(R.id.black_remove);
									black_remove.setChecked(true);
									AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
											AuroraBlackNameActivity.this)
											.setTitle(
													AuroraBlackNameActivity.this
															.getResources()
															.getString(
																	R.string.confirm_remove))
											.setView(view)
											.setPositiveButton(
													android.R.string.ok,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int whichButton) {
															ContentResolver cr = getContentResolver();
															ContentValues cv = new ContentValues();
															if (black_remove
																	.isChecked()) {
																cv.put("isblack",
																		0);

															} else {
																cv.put("isblack",
																		-1);
															}
															cv.put("number",
																	targetNumber);
															cv.put("reject",
																	Integer.parseInt(type));
															cv.put("black_name",
																	targetName);
															int uri2 = cr.update(
																	Uri.parse("content://com.android.contacts/black"),
																	cv,
																	"number=?",
																	new String[] { targetNumber });
															System.out
																	.println("updated"
																			+ ":"
																			+ uri2);
															dialog.dismiss();

														}
													})
											.setNegativeButton(
													android.R.string.cancel,
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int whichButton) {
															dialog.dismiss();
														}
													}).show();
									dialogs.setCanceledOnTouchOutside(false);
									break;
								default:
									break;
								}
							}
						}).show();
				return;
			}
		}
		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
				AuroraBlackNameActivity.this).setItems(R.array.black_name_menu,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						switch (position) {
						case 0:
							Intent intents = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel:" + targetNumber));
							startActivity(intents);
							break;
						case 1:
							Uri uri = Uri.parse("smsto:" + targetNumber);
							Intent it = new Intent(Intent.ACTION_SENDTO, uri);
							startActivity(it);
							break;
						case 2:
							Intent intentss = new Intent(
									AuroraBlackNameActivity.this,
									AuroraManuallyAddActivity.class);
							Bundle bundle = new Bundle();
							bundle.putString("targetId", targetId);
							bundle.putString("type", type);
							bundle.putString("add_number", targetNumber);
							if (targetName != null) {
								bundle.putString("add_name", targetName);
							} else {
								bundle.putString("add_name", "");
							}
							intentss.putExtras(bundle);
							AuroraBlackNameActivity.this
									.startActivity(intentss);
							break;
						case 3:
							View view = LayoutInflater.from(
									AuroraBlackNameActivity.this).inflate(
									R.layout.black_remove, null);
							final AuroraCheckBox black_remove = (AuroraCheckBox) view
									.findViewById(R.id.black_remove);
							black_remove.setChecked(true);
							AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
									AuroraBlackNameActivity.this)
									.setTitle(
											AuroraBlackNameActivity.this
													.getResources()
													.getString(
															R.string.confirm_remove))
									.setView(view)
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													ContentResolver cr = getContentResolver();
													ContentValues cv = new ContentValues();
													if (black_remove
															.isChecked()) {
														cv.put("isblack", 0);

													} else {
														cv.put("isblack", -1);
													}
													cv.put("number",
															targetNumber);
													cv.put("reject", Integer
															.parseInt(type));
													cv.put("black_name",
															targetName);
													int uri2 = cr.update(
															Uri.parse("content://com.android.contacts/black"),
															cv,
															"number=?",
															new String[] { targetNumber });
													System.out
															.println("updated"
																	+ ":"
																	+ uri2);
													dialog.dismiss();

												}
											})
									.setNegativeButton(
											android.R.string.cancel,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													dialog.dismiss();
												}
											}).show();
							dialogs.setCanceledOnTouchOutside(false);
							break;
						default:
							break;
						}
					}
				}).show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			touch=false;
			if(isbatch&&mActionBar.auroraIsEntryEditModeAnimRunning()){
				return true;
			}
			if (isbatch) {
				auroraBatch.leaveSelectionMode();
				return true;
			}
			if (black_name_list.auroraIsRubbishOut()) {
				black_name_list.auroraSetRubbishBack();
				return true;
			}
			if(isShowing){
				return true;
			}
			break;
		case KeyEvent.KEYCODE_MENU: {

			return true;

		}
		}

		return super.onKeyDown(keyCode, event);
	}

}
