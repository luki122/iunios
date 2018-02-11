package com.android.mail.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraListView;
import com.android.mail.R;
import com.android.mail.providers.Account;
import com.android.mail.providers.Folder;
import com.android.mail.utils.Utils;
import com.android.mail.providers.UIProvider;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.widget.AdapterView.OnItemClickListener;

import aurora.widget.AuroraActionBar;

public class AuroraSelectMoveToFolderActivity extends AuroraActivity {
	
	public static final String INTENT_KEY_CURRENT_ACCOUNT = "INTENT_KEY_CURRENT_ACCOUNT";
	public static final String INTENT_KEY_CURRENT_FOLDER = "INTENT_KEY_CURRENT_FOLDER";
	
	public static final String INTENT_RESULT_FOLDER = "INTENT_RESULT_FOLDER";
	
	private SeparatedFolderListAdapter mAdapter;
	
	private AuroraListView mListView;	
	private AuroraActionBar mActionBar;
	
	private TextView mActionBarAccountTextView; 
	private Account mAccount; 
	private Folder mCurrentFolder;
	private QueryRunner mRunner;
	
	private static final int MSG_LOAD_FOLDERS_FINISHED = 1;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_LOAD_FOLDERS_FINISHED) {
				mListView.setAdapter(mAdapter);
			}
		}
	};
	

	@Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setAuroraContentView(R.layout.aurora_select_move_to_folder, AuroraActionBar.Type.Normal, false);
        
        
        Intent intent = getIntent();
        mAccount = (Account)intent.getParcelableExtra(INTENT_KEY_CURRENT_ACCOUNT);
        mCurrentFolder = (Folder)intent.getParcelableExtra(INTENT_KEY_CURRENT_FOLDER);
        
        if(mAccount == null) {
        	Log.e("SQF_LOG", "mAccount is null");
        } else {
        	Log.i("SQF_LOG", "mAccount is not null");
        }
        setupViews();   

        mAdapter = new SeparatedFolderListAdapter();
        mRunner = new QueryRunner(getBaseContext());
        
        
        mRunner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	@Override
    public void onBackPressed() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
	private void setupViews() {
		initAuroraActionBar();
		mListView = (AuroraListView)findViewById(R.id.folder_list);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Object object = mAdapter.getItem(position);
				if(object instanceof FolderSelectorAdapter.FolderRow) {
					FolderSelectorAdapter.FolderRow folderRow = (FolderSelectorAdapter.FolderRow)object;
					Intent data = new Intent();
					data.putExtra(INTENT_RESULT_FOLDER, folderRow.getFolder());
					setResult(Activity.RESULT_OK, data);
					finish();
				}
				//Log.i("SQF_LOG", "object class :" + object.getClass().getName());
			}
		});
	}
	
	private void initAuroraActionBar() {
		mActionBar = getAuroraActionBar();
		if(mActionBar == null) {
			return;
		}
		TextView tv = (TextView)mActionBar.getTitleView();
		//tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
		//tv.setTextColor(0xff000000);
		
		if(mAccount != null && !TextUtils.isEmpty(mAccount.name)) {
			//int indexOfAt = mAccount.name.indexOf("@");
			//String account = mAccount.name.substring(0, indexOfAt);
			//mActionBar.setTitle(account);
			mActionBar.setTitle(R.string.aurora_actionbar_menu_move_to);
			//mActionBar.setTitle(mAccount.name);
		}
		//mActionBar.setCustomView(R.layout.select_folder_activity_actionbar_custom_view);
		//mActionBarAccountTextView = (TextView)findViewById(R.id.actionbar_account_text_view);
		//mActionBarAccountTextView.setText(mAccount.name);
    	//View homeBackButton = mActionBar.getHomeButton();
    	//homeBackButton.setVisibility(View.GONE);
	}
	
    //Aurora <SQF> <2014-11-28>  for NEW_UI begin
	void debug(Cursor cursor) {
		int clmCount = cursor.getColumnCount();
		//Log.i("SQF_LOG", "clmCount:" + clmCount);
		while(cursor.moveToNext()) {
			//Log.i("SQF_LOG", " ============================================================== ");
			for(int columnIndex = 0; columnIndex <  clmCount; columnIndex ++) {
				String colName = cursor.getColumnName(columnIndex);
				switch(cursor.getType(columnIndex)) {
				case Cursor.FIELD_TYPE_INTEGER:
					int i = cursor.getInt(columnIndex);
					Log.i("SQF_LOG", cursor.getColumnName(columnIndex) + " " + i);
					/*
					if(colName.equals("_id")) {
						getContentResolver().delete(uri, "_id=?", new String[]{String.valueOf(i)});
					}
					*/
					break;
				case Cursor.FIELD_TYPE_STRING:
					String str = cursor.getString(columnIndex);
					Log.i("SQF_LOG", cursor.getColumnName(columnIndex) + " " + str);
					break;
				case Cursor.FIELD_TYPE_FLOAT:
					float f = cursor.getFloat(columnIndex);
					Log.i("SQF_LOG", cursor.getColumnName(columnIndex) + " " + f);
					break;
				case Cursor.FIELD_TYPE_BLOB:
					Log.i("SQF_LOG", cursor.getColumnName(columnIndex) + " blob");
					break;
				case Cursor.FIELD_TYPE_NULL:
					Log.i("SQF_LOG", cursor.getColumnName(columnIndex) + " null");
					break;
				}
			}
		}
	}
    //Aurora <SQF> <2014-11-28>  for NEW_UI end

	protected void updateAdapterInBackground(Context context) {
        Cursor foldersCursor = null;
        try {
//            foldersCursor = context.getContentResolver().query(
//                    !Utils.isEmpty(mAccount.fullFolderListUri) ? mAccount.fullFolderListUri
//                            : mAccount.folderListUri, UIProvider.FOLDERS_PROJECTION, null,
//                    null, null);
            foldersCursor = context.getContentResolver().query(
                    !Utils.isEmpty(mAccount.allFolderListUri) ? mAccount.allFolderListUri : mAccount.folderListUri, 
                            UIProvider.FOLDERS_PROJECTION, null,null, null);
            
            // TODO(mindyp) : bring this back in UR8 when Email providers
            // will have divided folder sections.
            
            //debug(foldersCursor);//SQF ADDED ON 2014.11.28
            
            final String[] headers = context.getResources().getStringArray(
                    R.array.moveto_folder_sections);
            
            
            // Currently, the number of adapters are assumed to match the
            // number of headers in the string array.
            mAdapter.addSection(new SystemFolderSelectorAdapter(context, foldersCursor,
                    R.layout.single_folders_view, null, mCurrentFolder)); // null originally is headers[0]

            // TODO(mindyp): we currently do not support frequently moved to
            // folders, at headers[1]; need to define what that means.*/
            // TODO(pwestbro): determine if we need to call filterFolders
            mAdapter.addSection(new UserFolderHierarchicalFolderSelectorAdapter(context,
                    AddableFolderSelectorAdapter.filterFolders(foldersCursor, null),
                    R.layout.single_folders_view, null, mCurrentFolder));//null originally is headers[2].
            //mBuilder.setAdapter(mAdapter, SingleFolderSelectionDialog.this);
            
            mHandler.sendEmptyMessage(MSG_LOAD_FOLDERS_FINISHED);
        } finally {
            if (foldersCursor != null) {
                foldersCursor.close();
            }
        }
    }
	
	@Deprecated
    private class QueryRunner extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        private QueryRunner(final Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... v) {
            updateAdapterInBackground(mContext);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //mDialog = mBuilder.create();
            //showInternal();
        }
    }
}
