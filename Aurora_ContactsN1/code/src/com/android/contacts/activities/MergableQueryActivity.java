package com.android.contacts.activities;

import java.lang.reflect.Field;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.util.MergeContacts;
import com.android.contacts.util.MergeContacts.LetStopException;
import com.android.contacts.util.MergeContacts.SameContactItems;
import com.android.contacts.vcard.NotificationImportExportListener;
import com.mediatek.contacts.list.service.MultiChoiceService;

import android.app.Activity;
import android.app.ActivityManager;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraProgressDialog;

public class MergableQueryActivity extends AuroraActivity {
	public static String KEY_RESULT_MERGABLE = "result_mergable";

	protected MergeContacts mMergeContacts;
	private SameContactItems mSameRawAll;
	// i don't use the bundle, cause when there are a huge records, it does't work	
	private static SameContactItems mSameContactAll;
	
	protected ContactsUtils.AuroraContactsProgressDialog mQueryProgressDialog;
	private int progress_max;//aurora add zhouxiaobing 20131211
	protected Handler mPublishProgress = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				mQueryProgressDialog.setProgress(msg.arg1*100/progress_max);
				break;
			case 0:
				mQueryProgressDialog.setMax(msg.arg1);
				mQueryProgressDialog.setIndeterminate(false);//aurora change zhouxiaobing 20131212
				progress_max=msg.arg1;
				break;

			default:
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {

        
	    super.onCreate(savedInstanceState);
	    
	    if (MultiChoiceService.isProcessing(MultiChoiceService.TYPE_DELETE) ||
	            MultiChoiceService.isProcessing(MultiChoiceService.TYPE_COPY) ||
	            NotificationImportExportListener.isImporting()) {
            Toast.makeText(this, R.string.phone_book_busy,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        showMergingProgess();
	}

	protected void showMergingProgess() {
		mQueryProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(this, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
		mQueryProgressDialog.setTitle(getString(R.string.gn_searchContactsForMerge_msg));			
		mQueryProgressDialog.setIndeterminate(true);
		mQueryProgressDialog.setMax(1);
		mQueryProgressDialog.setProgressStyle(AuroraProgressDialog.STYLE_SPINNER);
		mQueryProgressDialog.setOnCancelListener(new OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				stopQuery();
			}
		});
		mQueryProgressDialog.setProgress(0);
		mQueryProgressDialog.show();		
		
		try {
            Field fieldNumber = AuroraProgressDialog.class.getDeclaredField("mProgressNumber");
            fieldNumber.setAccessible(true);
            ((View)(fieldNumber.get(mQueryProgressDialog))).setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		new AsyncTask<Integer, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Integer... params) {
				
				try {
					Context context = MergableQueryActivity.this;
					mMergeContacts = new MergeContacts();
					mSameRawAll = mMergeContacts.querySameRawContact(context);
					if (null != mSameRawAll && mSameRawAll.size() > 0) {
						mSameContactAll = mMergeContacts.querySameContact(context, mSameRawAll, mPublishProgress);
					}
				} catch (LetStopException e) {
					e.printStackTrace();
				}

				return null;
			}

			protected void onPostExecute(Integer result) {
				if (mMergeContacts.isLetStop()) {
					finish();
					return;
				}
				 if(null != mQueryProgressDialog && mQueryProgressDialog.isShowing() && 
							MergableQueryActivity.this != null && !MergableQueryActivity.this.isFinishing()){
						mQueryProgressDialog.dismiss();
					}

				if (null != mSameContactAll && mSameContactAll.size() > 0 && !mMergeContacts.isLetStop()) {
					
						
					    String contentFormat=MergableQueryActivity.this.getResources().getString(R.string.gn_mergeble);
						String sFinalContent = String.format(contentFormat, mSameContactAll.size()); 
						AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(MergableQueryActivity.this)
		                .setTitle(MergableQueryActivity.this.getResources().getString(R.string.gn_mergeContactsConfirmation_title))
		                .setMessage(sFinalContent)
		                .setPositiveButton(android.R.string.ok,
		                    new DialogInterface.OnClickListener() {
		                        @Override
		                        public void onClick(DialogInterface dialog, int whichButton) {
		                        	 dialog.dismiss();
		                        	 goMergeContactsActivity();
		                        }
		                    }
		                )
		                .setNegativeButton(android.R.string.cancel,   
		                		new DialogInterface.OnClickListener() {
		                    @Override
		                    public void onClick(DialogInterface dialog, int whichButton) {
		                    	dialog.dismiss();
		                    	mMergeContacts.stop();
	                            finish();
		                    }
		                }).show();
						 dialogs.setOnDismissListener(new DialogInterface.OnDismissListener() {
                             public void onDismiss(DialogInterface dialog) {
                                 mMergeContacts.stop();
                                 finish();
                             }
                          });
						 dialogs.setCanceledOnTouchOutside(false);
				} else {
				   
				    

                      	AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(MergableQueryActivity.this)
    	                .setTitle(MergableQueryActivity.this.getResources().getString(R.string.gn_mergeContactsConfirmation_title))
    	                .setMessage(MergableQueryActivity.this.getResources().getString(R.string.gn_mergeContacts_manual))
    	                .setPositiveButton(android.R.string.ok,
    	                    new DialogInterface.OnClickListener() {
    	                        @Override
    	                        public void onClick(DialogInterface dialog, int whichButton) {
    	                        	 dialog.dismiss();
//    	                             startActivity(new Intent(MergableQueryActivity.this, ManualCombineQueryActivity.class));
    	                         	 startActivity(new Intent(MergableQueryActivity.this, ManualCombineByNumberActivity.class));
    	                             mMergeContacts.stop();
    	                             finish();
    	                        }
    	                    }
    	                )
    	                .setNegativeButton(android.R.string.cancel,   
    	                		new DialogInterface.OnClickListener() {
    	                    @Override
    	                    public void onClick(DialogInterface dialog, int whichButton) {
    	                    	dialog.dismiss();
    	                    	mMergeContacts.stop();
 	                            finish();
    	                    }
    	                }).show();
    					 dialogs.setCanceledOnTouchOutside(false);
    					 dialogs.setOnDismissListener(new DialogInterface.OnDismissListener() {
                             public void onDismiss(DialogInterface dialog) {
                                 mMergeContacts.stop();
                                 finish();
                             }
                          });
                    
				}
			}
		}.execute();
	}

	protected void goMergeContactsActivity() {
		finish();
		Intent intent = new Intent(this, MergeContactsActivity.class);
//		 Bundle bundle = new Bundle();
//		 bundle.putSerializable(KEY_RESULT_MERGABLE, mSameAllRecord);
//		 intent.putExtras(bundle);

		startActivity(intent);
	}

	protected void onPause() {
		super.onPause();
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		if (!cn.getClassName().equals(this.getClass().getName())) {
			stopQuery();
		}
	}
	
	protected void stopQuery() {
		if (null != mMergeContacts) {
			mMergeContacts.stop();
			Toast.makeText(this, R.string.gn_mergable_query_stop_msg, Toast.LENGTH_SHORT).show();
		}
		finish();
	}
	
	// only one time
	public static SameContactItems getSameContectItems() {
		SameContactItems tmp = mSameContactAll;
		mSameContactAll = null;
		return tmp;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mSameContactAll = null;
	}
}
