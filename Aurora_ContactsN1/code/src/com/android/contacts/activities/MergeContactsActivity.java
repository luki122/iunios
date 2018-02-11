package com.android.contacts.activities;

import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.activities.ManualCombineByNumberActivity.CombineItemsAdapter;
import com.android.contacts.util.MergeContacts;
import com.android.contacts.util.MergeContacts.CombineItemData;
import com.android.contacts.util.MergeContacts.LetStopException;
import com.android.contacts.util.MergeContacts.RawContactItem;
import com.android.contacts.util.MergeContacts.SameContactItems;

import android.app.ActivityManager;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import aurora.app.AuroraListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraProgressDialog;

public class MergeContactsActivity extends AuroraListActivity {
	protected SameContactItems mSameAllRecord;
	private AuroraProgressDialog dialog;
	protected AuroraListView mListView;

	protected AuroraProgressDialog mMergingProgressDialog;
	protected MergeContacts mMergeContacts;

	protected int mDeletedCount;
	protected DialogInterface.OnClickListener mEmptyClickListener = null;
    
	private ContactPhotoManager mPhotoManager;
    private boolean  isFinished;
	protected Handler mMergeProgress = new Handler() {
		public void handleMessage(Message msg) {
			if (null == this || isFinishing())
				return;
			
			switch (msg.what) {
			case 0: {
				mMergingProgressDialog.setIndeterminate(false);
				mMergingProgressDialog.setMax(msg.arg1);
				break;
			}
			case 1: {
				mDeletedCount += msg.arg1;
				mMergingProgressDialog.setProgress(mDeletedCount);
				break;
			}
			case 2: {
				if (mMergingProgressDialog != null) {
					mMergingProgressDialog.dismiss();
				}
				isFinished = (msg.arg1 == 1);

				showDialog();
			}

			default:
				break;
			}
		}
	};
	
	
	
	

	private void showDialog() {
		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
				MergeContactsActivity.this)
				.setTitle(
						MergeContactsActivity.this.getResources().getString(
								R.string.gn_mergeContactsConfirmation_title))
				.setMessage(
						MergeContactsActivity.this.getResources().getString(
								R.string.gn_mergeContacts_continue))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								
									Intent intent = new Intent(
											MergeContactsActivity.this,
											ManualCombineByNumberActivity.class);
									startActivity(intent);
								
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								finish();
							}
						}).show();
		dialogs.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		dialogs.setCanceledOnTouchOutside(false);

	}

	protected void onCreate(Bundle savedInstanceState) {	
        
		super.onCreate(savedInstanceState);
		setAuroraContentView(R.layout.gn_merge_contacts,
                AuroraActionBar.Type.Normal);

		mSameAllRecord = MergableQueryActivity.getSameContectItems();
		
		AuroraActionBar ab = getAuroraActionBar();


			String title = getString(R.string.gn_menu_merge_contacts);
			ab.setTitle(title);

		
		if (mPhotoManager == null) {
            mPhotoManager = ContactPhotoManager.getInstance(this);            
        }
        

		showDialogs();
		showDuplicate(this);
	}
	
	
	public List<RawContactItem> fliterNeedToDelete(SameContactItems same) {
		if (null == same || same.size() <= 0)
			return null;

		List<RawContactItem> needToDelete = new ArrayList<RawContactItem>(
				(same.size() * 2));

		for (ArrayList<RawContactItem> item : same) {

			for (int i = 1, s = item.size(); i < s; i++) {
				needToDelete.add(item.get(i));
			}
		}

		return needToDelete;
	}
	
	
	protected void showDuplicate(final Context context) {
		new AsyncTask<Integer, Integer, Integer>() {

			@Override
			protected Integer doInBackground(Integer... params) {

				if (null == mSameAllRecord || mSameAllRecord.size() <= 0)
					return 0;

				List<RawContactItem> needToDelete = fliterNeedToDelete(mSameAllRecord);

				if (null == needToDelete)
					return 0;
				int size = needToDelete.size();
				if (size <= 0)
					return 0;

				StringBuilder sb = new StringBuilder("_id in (");
				for (int i = 0; i < size;i++) {
					sb.append(needToDelete.get(i).mRawContactId).append(",");
				}

				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				String where = sb.toString();
				int delCount = context.getContentResolver().delete(
						RawContacts.CONTENT_URI, where, null);
				publishProgress(0);

				return delCount;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				int ret = values[0];
				switch (ret) {
				case 0:
					if (dialog != null) {
						dialog.dismiss();
					}
					showDialog();
					break;
				default:
					break;
				}

			};
		}.execute();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class ContactItemView extends LinearLayout {
		public ImageView mIcon;
		public TextView mTitle;
		public TextView mNote;

		public ContactItemView(Context context) {
			super(context);
			initView();
		}

		public ContactItemView(Context context, AttributeSet attrs) {
			super(context, attrs);
			initView();
		}

		protected void initView() {
			inflate(getContext(), R.layout.gn_contacts_mergable_list_item, this);
			mIcon = (ImageView) findViewById(R.id.icon);
			mTitle = (TextView) findViewById(R.id.title);
			mNote = (TextView) findViewById(R.id.note);
			
			mIcon.setVisibility(View.GONE);
		}

	}

	protected BaseAdapter mAdapter = new BaseAdapter() {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ContactItemView civ = null;
			if (null != convertView) {
				civ = (ContactItemView) convertView;
			} else {
				civ = new ContactItemView(MergeContactsActivity.this);
			}

			RawContactItem item = mSameAllRecord.get(position).get(0);

			civ.mTitle.setText(item.mDisplayName);
			civ.mNote.setText("(" + mSameAllRecord.get(position).size() + ")");
			
			if (mPhotoManager != null) {
				mPhotoManager.loadPhoto(civ.mIcon, item.mPhotoId, false, false);
			}

			return civ;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return (null != mSameAllRecord ? mSameAllRecord.size() : 0);
		}
	};

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.merge_contacts_btn: {			
			startMerge();
			break;
		}

		default:
			break;
		}

	}

	protected void onPause() {
	    super.onPause();
	    
	    if(null != mMergeContacts) {
	    	ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    	ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
	    	/*if (!cn.getClassName().equals(this.getClass().getName())
	    			&& !cn.getClassName().equals("com.android.phone.InCallScreen")) {
	    		quitMerge();
	    	}*/
	    	if (!cn.getClassName().equals(this.getClass().getName())) {
	    		quitMerge();
	    	}
	    }	    
	}
	
	protected void startMerge() {
		mMergingProgressDialog = new AuroraProgressDialog(this, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
		mMergingProgressDialog.setMessage(getString(R.string.gn_mergingSameContacts_msg));
		mMergingProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mMergingProgressDialog.setMax(1);
		mMergingProgressDialog.setIndeterminate(true);
		mMergingProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
				getString(android.R.string.cancel),
				new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
		mMergingProgressDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				stopMerge();

			}
		});

		mMergingProgressDialog.show();

		new Thread() {
			public void run() {
				mMergeContacts = new MergeContacts();
				mMergeContacts.mergeContacts(MergeContactsActivity.this,
						mSameAllRecord, mMergeProgress);
			}
		}.start();
	}
	
	protected void stopMerge() {
		if (null != mMergeContacts) {
			mMergeContacts.stop();
		}
	}
	
	protected void quitMerge() {
		if (null != mMergeContacts) {
			mMergeContacts.stop();
			Toast.makeText(this, R.string.gn_merge_stop_title, Toast.LENGTH_SHORT).show();
		}
		finish();
	}
	
	private final int MENU_ID_START_MERGE = 1;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*menu.add(0, MENU_ID_START_MERGE, 0, R.string.gn_start_merge)
		.setIcon(R.drawable.gn_ic_menu_merge_contacts)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;*/
		
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case MENU_ID_START_MERGE:
				startMerge();
				return true;
				
			// gionee xuhz 20120524 add for CR00596957 start
			case android.R.id.home:
	
					onBackPressed();
					return true;
				
			// gionee xuhz 20120524 add for CR00596957 end
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	 private void showDialogs(){
			if (dialog == null) {
				dialog = new AuroraProgressDialog(this);
				dialog.setIndeterminate(true);
				dialog.setCancelable(false);
				dialog.setMessage(getResources().getString(R.string.gn_combine_contacts_merge_contact));
			}
			dialog.show();
		}
}
