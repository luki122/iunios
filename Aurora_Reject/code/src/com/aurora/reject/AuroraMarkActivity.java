package com.aurora.reject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.aurora.reject.adapter.MarkAdapter;
import com.aurora.reject.util.RejectApplication;
import com.aurora.reject.util.TotalCount;
import com.aurora.reject.util.YuloreUtil;

import com.aurora.reject.R;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import gionee.provider.GnContactsContract;
import gionee.provider.GnCallLog.Calls;

public class AuroraMarkActivity extends AuroraActivity implements
		OnItemClickListener {
	private String target="";
	private String targetId="";
	private AuroraActionBar mActionBar;
	private static final int ACTION_BTN_ADD = 1;
	private static Uri uri = Uri.parse("content://com.android.contacts/mark");
	private ContentResolver mContentResolver;
	private AsyncQueryHandler mQueryHandler;
	private static final int SHOW_BUSY = 0;
	private static final int SHOW_LIST = 1;
	private static final int SHOW_EMPTY = 2;
	private int mState;
	private static final int DIALOG_REFRESH = 1;
	private AuroraProgressDialog dialog;
	private AuroraListView mark_list;
	private TextView mark_empty;
	private MarkAdapter mListAdapter = null;
	private Context mContext = null;
	private Cursor listCursor;
	private List<String> list=new ArrayList<String>();
	
	private ContentObserver changeObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfUpdate) {
			startQuery();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.mark_list, AuroraActionBar.Type.Normal);
		mActionBar = getAuroraActionBar();
		addAuroraActionBarItem(AuroraActionBarItem.Type.Add, ACTION_BTN_ADD);
		mActionBar.setOnAuroraActionBarListener(auroActionBarItemClickListener);
		mActionBar.setTitle(getResources().getString(R.string.marks_manage));
		mark_list = (AuroraListView) findViewById(R.id.mark_list);
		mark_list.setOnItemClickListener(this);
		// 打开滑动删除功能
		mark_list.auroraSetNeedSlideDelete(true);
		mark_list
				.auroraSetAuroraBackOnClickListener(new AuroraListView.AuroraBackOnClickListener() {

					@Override
					public void auroraOnClick(final int position) {// 点击了垃圾桶的响应事件
						AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(AuroraMarkActivity.this)
		                .setTitle(AuroraMarkActivity.this.getResources().getString(R.string.del_mark))
		                .setMessage(AuroraMarkActivity.this.getResources().getString(R.string.is_confirm_del))
		                .setPositiveButton(R.string.aurora_del_confirm,
		                    new DialogInterface.OnClickListener() {
		                        @Override
		                        public void onClick(DialogInterface dialog, int whichButton) {
		                        	 dialog.dismiss();
	                        		try {
	                        			mark_list.auroraDeleteSelectedItemAnim();
	                        		} catch (Exception e) {
		     							e.printStackTrace();
		     						}
		                        	 mark_list.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {
										
										@Override
										public void auroraDeleteItem(View arg0, int arg1) {
											// TODO Auto-generated method stub
											 new TotalCount(AuroraMarkActivity.this, "180", "deletemark", 1).CountData();
											 Cursor pcursor = (Cursor) mark_list
					     								.getItemAtPosition(position);
					     						if (pcursor == null) {
					     							return;
					     						}
					     						try {
						     						String lable = pcursor.getString(1);
						     						
						     						System.out.println(lable);
						     						mQueryHandler.startDelete(1, null, uri, "lable=?",
						     								new String[] { lable });
						     						
						     						updateData(lable, null);
					     						} catch (Exception e) {
					     							e.printStackTrace();
					     						}
										}
									});
		                        	 
		                        	
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
						 dialog.setCanceledOnTouchOutside(false);
						
						
						
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
		mark_list.setRecyclerListener(mListAdapter);
		
		mContext = AuroraMarkActivity.this;

		mark_empty = (TextView) findViewById(R.id.mark_empty);

		mContentResolver = getContentResolver();
		mQueryHandler = new QueryHandler(mContentResolver, this);
		startQuery();
		mContentResolver.registerContentObserver(uri, true, changeObserver);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
			if(mark_list!=null){
				mark_list.auroraOnResume();
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
		mQueryHandler.startQuery(0, null, uri, null, "lable is not null and number is null", null, null);

	}
	
	private void showDialogMenu() {
		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
				AuroraMarkActivity.this).setItems(R.array.mark_menu,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						switch (position) {
						case 0:

							View view = LayoutInflater.from(
									AuroraMarkActivity.this).inflate(
									R.layout.dialog_edittext, null);
							final AuroraEditText mark_content = (AuroraEditText) view
									.findViewById(R.id.mark_content);
							mark_content.setText(target);
							mark_content.setSelection(target.length());
							mark_content
									.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
											15) });
							AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
									AuroraMarkActivity.this)
									.setTitle(
											AuroraMarkActivity.this
													.getResources().getString(
															R.string.edit_mark))
									.setView(view)
									.setPositiveButton(
											android.R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													String s = mark_content
															.getText()
															.toString()
															.replace(" ", "");
													if (!s.equals("")) {
														s = mark_content
																.getText()
																.toString();
														ContentResolver cr = getContentResolver();
														ContentValues cv = new ContentValues();
														cv.put("lable", s);
														int uri2 = cr
																.update(uri,
																		cv,
																		"lable=?",
																		new String[] { target });
														System.out
																.println("updated"
																		+ ":"
																		+ uri2);

														updateData(target, s);
													} else {
														Toast.makeText(
																AuroraMarkActivity.this,
																AuroraMarkActivity.this
																		.getResources()
																		.getString(
																				R.string.no_marks),
																Toast.LENGTH_LONG)
																.show();
														return;
													}
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
							dialogs.getWindow()
									.setSoftInputMode(
											WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

							break;
						case 1:

							AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(
									AuroraMarkActivity.this)
									.setTitle(
											AuroraMarkActivity.this
													.getResources().getString(
															R.string.del_mark))
									.setMessage(
											AuroraMarkActivity.this
													.getResources()
													.getString(
															R.string.is_confirm_del))
									.setPositiveButton(
											R.string.aurora_del_confirm,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													dialog.dismiss();
													ContentResolver cr = getContentResolver();
													cr.delete(
															uri,
															"lable=?",
															new String[] { target });

													updateData(target, null);
													new TotalCount(
															AuroraMarkActivity.this,
															"180",
															"deletemark", 1)
															.CountData();
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
							dialog.setCanceledOnTouchOutside(false);

							System.out
									.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

							break;
						default:
							break;
						}

					}
				}).show();
	}

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int arg0) {
			// TODO Auto-generated method stub
			 
			switch (arg0) {
			case R.id.del:
				AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(AuroraMarkActivity.this)
                .setTitle(AuroraMarkActivity.this.getResources().getString(R.string.del_mark))
                .setMessage(AuroraMarkActivity.this.getResources().getString(R.string.is_confirm_del))
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	 dialog.dismiss();
                        	 ContentResolver cr = getContentResolver();
             				 cr.delete(uri, "lable=?", new String[] { target });
             				 
             				updateData(target, null);
             				new TotalCount(AuroraMarkActivity.this, "180", "deletemark", 1).CountData();
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
				 dialog.setCanceledOnTouchOutside(false);
				
			
				System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				break;
			case R.id.edit:
				  View view=LayoutInflater.from(AuroraMarkActivity.this).inflate(R.layout.dialog_edittext, null);
				  final AuroraEditText mark_content=(AuroraEditText) view.findViewById(R.id.mark_content);
				  mark_content.setText(target);
				  mark_content.setSelection(target.length());
				  mark_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
				  AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(AuroraMarkActivity.this)
	                .setTitle(AuroraMarkActivity.this.getResources().getString(R.string.edit_mark))
	                .setView(view)
	                .setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	 String s=mark_content.getText().toString().replace(" ", "");
	                        	 if(!s.equals("")){
	                        		    s=mark_content.getText().toString();
	                        			ContentResolver cr = getContentResolver();
	                    				ContentValues cv = new ContentValues();
	                    				cv.put("lable", s);
	                    				int uri2 = cr.update(uri, cv, "lable=?", new String[] { target });
	                    				System.out.println("updated" + ":" + uri2);
	                    				
	                    				updateData(target, s);
	                        	 }else{
	                        		 Toast.makeText(AuroraMarkActivity.this, AuroraMarkActivity.this.getResources().getString(R.string.no_marks), Toast.LENGTH_LONG).show();
	         						 return;
	                        	 }
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
				  dialogs.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) ;
	              
				break;

			}

		}
	};

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
			listCursor=cursor;
			if(listCursor!=null){
				if(!listCursor.moveToFirst()){
					list.clear();
				}else{
					list.clear();
					do{
						list.add(listCursor.getString(listCursor.getColumnIndex("lable")));
					}while(listCursor.moveToNext());
				}
				
			}
			if (cursor != null) {
				if (!cursor.moveToFirst()) {
					updateState(SHOW_EMPTY);
				} else if (mListAdapter == null) {
					mListAdapter = new MarkAdapter(context, cursor);
					mark_list.setAdapter(mListAdapter);
					updateState(SHOW_LIST);
				} else {
					mListAdapter.changeCursor(cursor);
					updateState(SHOW_LIST);
				}
				// startManagingCursor(cursor);
			} else {
				if (mListAdapter != null) {
					mListAdapter.changeCursor(null);
				}
				updateState(SHOW_EMPTY);
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

	private void updateState(int state) {
		if (mState == state) {
			return;
		}

		mState = state;
		switch (state) {
		case SHOW_LIST:
			mark_empty.setVisibility(View.GONE);
			mark_list.setVisibility(View.VISIBLE);
			break;
		case SHOW_EMPTY:
			mark_list.setVisibility(View.GONE);
			mark_empty.setVisibility(View.VISIBLE);
			break;
		case SHOW_BUSY:
			mark_list.setVisibility(View.GONE);
			mark_empty.setVisibility(View.GONE);
//			showDialog(DIALOG_REFRESH);
			break;
		}
	}

	// showDialog（）回调此方法
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_REFRESH: {
			if (dialog != null && dialog.getContext() != this) {
				removeDialog(DIALOG_REFRESH);
			}
			dialog = new AuroraProgressDialog(this);
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.setMessage(getResources().getString(R.string.refresh));
			return dialog;
		}
		}
		return null;
	}

	// launchMode
	// 为singleTask的时候，通过Intent启到一个Activity,如果系统已经存在一个实例，系统就会将请求发送到这个实例上，但这个时候，系统就不会再调用通常情况下我们处理请求数据的onCreate方法，而是调用onNewIntent方法
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	private OnAuroraActionBarItemClickListener auroActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case ACTION_BTN_ADD:
				  
				  View view=LayoutInflater.from(AuroraMarkActivity.this).inflate(R.layout.dialog_edittext, null);
				  final AuroraEditText mark_content=(AuroraEditText) view.findViewById(R.id.mark_content);
				  mark_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
				  AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(AuroraMarkActivity.this)
	                .setTitle(AuroraMarkActivity.this.getResources().getString(R.string.add_mark))
	                .setView(view)
	                .setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog, int whichButton) {
	                        	
	                        	
	                        	
	                        	 String s=mark_content.getText().toString();
	                        	 if(!s.equals("")){
	                        		 if (s != null && s.replaceAll(" ", "").equals("")) {
	                           			Toast.makeText(mContext, mContext.getResources().getString(R.string.aurora_mark_error), Toast.LENGTH_SHORT).show();
	                           			return;
	                           		 }
	                        		 
	                        		 if(!list.contains(s)){
	 	                        		ContentResolver cr = getContentResolver();
	 	                     			ContentValues cv = new ContentValues();
	 	                     			cv.put("lable", s);
	 	                     			try {
		 	                     			Uri uri2 = cr.insert(uri, cv);
		 	                     			new TotalCount(mContext, "180", "addmark", 1).CountData();
		 	                     			System.out.println(uri2.toString());
	 	                     		   }catch(Exception e){
	 	                  			    	e.printStackTrace();
	 	                  			    }
		 	                     			try{
	 	                     			
	 	                     				 Field field = dialog.getClass()
	 		                  			            .getSuperclass().getDeclaredField(
	 		                  			                     "mShowing" );
	 		                  			    field.setAccessible( true );
	 	                  			    	field.set(dialog, true );
	 	                  			    }catch(Exception e){
	 	                  			    	
	 	                  			    }
	                        		 }else{
	                        			Toast.makeText(AuroraMarkActivity.this, AuroraMarkActivity.this.getResources().getString(R.string.mark_content_exist), Toast.LENGTH_LONG).show();
	                        			//   将mShowing变量设为false，表示对话框已关闭 
	                        			 try{
	                        				 Field field = dialog.getClass()
	     	                  			            .getSuperclass().getDeclaredField(
	     	                  			                     "mShowing" );
	     	                  			    field.setAccessible( true );
	                       			    	field.set(dialog, false );
	                       			    }catch(Exception e){
	                       			    	
	                       			    }
	                        		 }
	                        		 
	                        	 }else{
	                        		 Toast.makeText(AuroraMarkActivity.this, AuroraMarkActivity.this.getResources().getString(R.string.no_content), Toast.LENGTH_LONG).show();
	                        		 try{
	                        			 Field field = dialog.getClass()
	 	                  			            .getSuperclass().getDeclaredField(
	 	                  			                     "mShowing" );
	 	                  			    field.setAccessible( true );
	                       			    	field.set(dialog, false );
	                       			    }catch(Exception e){
	                       			    	
	                       			    }
	                        	 }
	                        	 dialog.dismiss();
	                        }
	                    }
	                )
	                .setNegativeButton(android.R.string.cancel,   
	                		new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	try{
	                    	 Field field = dialog.getClass()
              			            .getSuperclass().getDeclaredField(
              			                     "mShowing" );
              			    field.setAccessible( true );
              			    
              			    	field.set(dialog, true );
              			    }catch(Exception e){
              			    	
              			    }
	                    	
	                    	dialog.dismiss();
	                    }
	                }).show();
				  dialogs.setCanceledOnTouchOutside(false);
				  dialogs.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) ;

				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
//		setAuroraSystemMenuCallBack(auroraMenuCallBack);
//		setAuroraMenuItems(R.menu.mark);
//		showAuroraMenu();
		showDialogMenu();
		
		Cursor pcursor = (Cursor) mark_list
				.getItemAtPosition(position);
		if (pcursor == null) {
			return;
		}
		target = pcursor.getString(pcursor.getColumnIndex("lable"));
		targetId=pcursor.getString(pcursor.getColumnIndex("_id"));
	
		

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mark_list.auroraIsRubbishOut()) {
				mark_list.auroraSetRubbishBack();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_MENU: {
			return true;
		}

		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void updateData(String oldLable, String newLable) {
		final String oldMark = oldLable;
		final String newMark = newLable;
		Log.e("wangth", "oldMark = " + oldMark + " newMark = " + newMark);
		
		new Thread() {
    		public void run() {
    			Cursor callCursor = mContext.getContentResolver().query(Calls.CONTENT_URI,
    					new String[] { "_id", "number"},
    					"mark='" + oldMark + "' and user_mark='-1' and reject in (0,1)",
    					null, null, null);
    		    if (callCursor != null) {
    				if (callCursor.moveToFirst()) {
    					do {
    						ContentValues cv = new ContentValues();
    						int userMark = 0;
    						String number = callCursor.getString(1);
    						Log.e("wangth", "number ========== " + number);
    						
    						if (newMark != null) {
    							YuloreUtil.insertUserMark(mContext, number, newMark);
    							userMark = -1;
    						} else {
    							userMark = 0;
    							YuloreUtil.deleteUserMark(mContext, number);
    						}
    						
    						
    						cv.put("mark", newMark);
    						cv.put("user_mark", userMark);
    						mContext.getContentResolver().update(Calls.CONTENT_URI, cv,
    								"_id=" + callCursor.getString(0), null);
    					} while (callCursor.moveToNext());
    				}

    				callCursor.close();
    			}
    		    
    		    Uri blackUri = Uri.parse("content://com.android.contacts/black");
    		    System.out.println("oldMark="+oldMark);
    		    Cursor blackCursor = mContext.getContentResolver().query(blackUri,
    					new String[] { "_id", "number", "user_mark"},
    					"lable='" + oldMark + "' and user_mark='-1'",
    					null, null, null);
    		    if (blackCursor != null) {
    				if (blackCursor.moveToFirst()) {
    					try {
    						do {
    							ContentValues cv = new ContentValues();
        						int userMark = 0;
        						String number = blackCursor.getString(1);
        						
        						if (newMark != null) {
        							YuloreUtil.insertUserMark(mContext, number, newMark);
        							userMark = -1;
        						} else {
        							userMark = 0;
        							YuloreUtil.deleteUserMark(mContext, number);
        						}
        						
        						cv.put("lable", newMark);
        						cv.put("user_mark", userMark);
    							int i=mContext.getContentResolver().update(blackUri, cv,
    									"_id=" + blackCursor.getString(0), null);
    							System.out.println("i="+i);
    						} while (blackCursor.moveToNext());
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    				}

    				blackCursor.close();
    			}
    		}
		}.start();
	}

}
