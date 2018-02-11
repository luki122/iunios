package com.android.contacts.activities;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.model.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.model.BaseAccountType.PhoneActionAltInflater;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.Constants;
import com.android.contacts.util.ImportExportUtil;
import com.android.contacts.util.ImportExportUtil.ResultListener;
import com.android.contacts.util.WeakAsyncTask;
import com.android.contacts.vcard.CancelActivity;
import com.android.contacts.vcard.ExportVCardActivity;
import com.android.contacts.vcard.VCardService;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.list.AuroraContactListMultiChoiceActivity;
import com.mediatek.contacts.list.ContactsIntentResolverEx;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;
import com.android.internal.telephony.ITelephony;


import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.model.LocalPhoneAccountType;
import com.mediatek.contacts.model.SimAccountType;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.StartSIMService;
import com.mediatek.contacts.simcontact.StartSIMService2;
import com.mediatek.contacts.util.ContactsIntent;
import com.mediatek.contacts.util.ErrorCause;
import com.mediatek.contacts.util.OperatorUtils;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraProgressDialog;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.preference.Preference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceActivity;
import com.aurora.android.contacts.AuroraStorageManager;

import com.android.internal.telephony.ITelephony;

import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.preference.AuroraPreferenceGroup;


/**
 * aurora:ukiliu 20130927 add
 */

public class AuroraContactImportExportActivity extends AuroraPreferenceActivity 
		implements AuroraPreference.OnPreferenceClickListener {
    private static final String TAG = "AuroraContactImportExportActivity";
    
    private static final int AURORA_IMPORT_FROM_SIM = 1;
    private static final int AURORA_IMPORT_FROM_SD = 2;
    private static final int AURORA_EXPORT_TO_SIM = 3;
    private static final int AURORA_EXPORT_TO_SD = 4;
    
    private static final int START_EXPORT = 10;
    private static final int REFRESH = 11;
    private static final int END_EXPORT = 12;
    private static final int END_EXPORT_SIM_FULL = 121;
    private static final int END_EXPORT_SIM_ERROR = 122;
    
    private static final int END_REFRESH_SIM_IMPORT = 13;
    private static final int END_IMPORT = 14;
    private static final int START_IMPORT = 15;
    
    private static final int START_REFRESH_SIM_3RD = 16;
    private static final int END_REFRESH_SIM_3RD = 17;
    private static final int ERROR = 18;
    private ContactsUtils.AuroraContactsProgressDialog mRefreshSimContactsDig;
    
    private static String SIM_CONTACTS_LOADED_ACTION = "aurora.sim.contacts.loaded";
    private BroadcastReceiver mReceiver = new SimContactsLoadedBroadcastReceiver();
    private boolean isFinished = false;
    private ArrayList<String> mNamesArray = new ArrayList<String>();
    private ArrayList<String> mPhoneNumberArray = new ArrayList<String>();
    
    public static final int REQUEST_CODE = 111111;
    public static final int RESULT_CODE = 111112;

    private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#]]+[[0-9][*#,]]*";
    private Context mContext;
    
    private View importList;
    private View exportList;
    private static AuroraMenu mAuroraMenu;
    static StorageManager mStorageManager;

    private HandlerThread mHandlerThread = null;
    private List<AccountWithDataSetEx> accounts;
    
    private static AccountWithDataSetEx mPhoneAccount = null;
    private static AccountWithDataSetEx mSimAccount = null;
    private static AccountWithDataSetEx mSim1Account = null;
    private static AccountWithDataSetEx mSim2Account = null;
    
    private static ContactsUtils.AuroraContactsProgressDialog mQueryProgressDialog;
    private static ContactsUtils.AuroraContactsProgressDialog mImportProgressDialog;
    private static boolean mIsImportSimContactFinish = false;
    private static boolean mIsReallyImportSim = false;
    private static int mDismissThreadFlag = 0;
    private static final int DISMISS_PROGDIAL_SLEEP_TIME = 100;
    private static final int DISMISS_PROGDIAL_ALL_TIME = 600;
    
    private AccountWithDataSetEx sourceAccountWithDataSetEx;
    private AccountWithDataSetEx targetAccountWithDataSetEx;
    
    private ContentResolver mResolver;
    
    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 100;
    
    
    private AuroraPreference mSimImportPref;
    private AuroraPreference mSdImportPref;
    private AuroraPreference mSd2ImportPref;
    private AuroraPreference mSimExportPref;
    private AuroraPreference mSdExportPref;
    private AuroraPreference mSd2ExportPref;
    private Handler handler;
    
    private AuroraAlertDialog export_sim_dialog,import_sim_dialog,import_sim_no_dialog;
    
    private boolean mIsImportOnly = false;
    private boolean mSimExportFlag=true;

    private int mImportExportSlot;
    private String select=null;
    
    private Drawable micon;
    private String sumary_text,mtitle;
    
    private static int mSlot = 0;
    private static int QUERY_TIMEOUT = 120;
    
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		mContext = this.getApplicationContext();
		handler = new Handler();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mIsImportOnly = extras.getBoolean("import_only", false);
			if (mIsImportOnly) {
				getAuroraActionBar().setTitle(R.string.aurora_menu_import);
				addPreferencesFromResource(R.xml.aurora_preference_contact_import);
			}
		}

		if (!mIsImportOnly) {
			getAuroraActionBar().setTitle(R.string.aurora_import_export);
			addPreferencesFromResource(R.xml.aurora_preference_contact_import_export);
			mSimExportPref = (AuroraPreference) findPreference("pref_key_export_sim");
			mSdExportPref = (AuroraPreference) findPreference("pref_key_export_sd");
			mSd2ExportPref = (AuroraPreference) findPreference("pref_key_export_sd2");
			mSimExportPref.setOnPreferenceClickListener(this);
			mSdExportPref.setOnPreferenceClickListener(this);
			mSd2ExportPref.setOnPreferenceClickListener(this);

			if (ContactsUtils.mIsIUNIDeviceOnly) {
				if ((FeatureOption.MTK_GEMINI_SUPPORT && (simStateReady(0) || simStateReady(1)))
						|| (!FeatureOption.MTK_GEMINI_SUPPORT && simStateReady())) {
					mSimExportPref.setEnabled(true);
					mSimExportPref.setSelectable(true);
					ImportExportUtil.getInstance().setPhoneListner(new ResultListener() {
						@Override
						public void onResult(Integer result) {
							if (result == 0) {
								mSimExportPref.setEnabled(false);
								mSimExportPref.setSelectable(false);
								mSimExportFlag = false;
							}
							
						}
					});
					ImportExportUtil.getInstance().startQueryPhoneContactsTask();
				} else {
					mSimExportPref.setEnabled(false);
					mSimExportPref.setSelectable(false);
				}
			} else {
				((AuroraPreferenceGroup) (getPreferenceScreen()
						.getPreference(1))).removePreference(mSimExportPref);
			}

			if (AuroraStorageManager.getInstance(this).getInternalStoragePath() != null) {
				mSdExportPref.setEnabled(true);
				mSdExportPref.setSelectable(true);
			} else {
				mSdExportPref.setEnabled(false);
				mSdExportPref.setSelectable(false);
			}

			if (AuroraStorageManager.getInstance(this).getExternalStoragePath() != null) {
				if (findPreference("pref_key_export_sd2") == null)
					((AuroraPreferenceGroup) (getPreferenceScreen()
							.getPreference(0))).addPreference(mSd2ExportPref);
			} else {
				((AuroraPreferenceGroup) (getPreferenceScreen()
						.getPreference(1))).removePreference(mSd2ExportPref);
			}
		}

		mSimImportPref = (AuroraPreference) findPreference("pref_key_import_sim");
		mSdImportPref = (AuroraPreference) findPreference("pref_key_import_sd");
		mSd2ImportPref = (AuroraPreference) findPreference("pref_key_import_sd2");
		mSimImportPref.setOnPreferenceClickListener(this);
		if ((FeatureOption.MTK_GEMINI_SUPPORT && (simStateReady(0) || simStateReady(1)))
				|| (!FeatureOption.MTK_GEMINI_SUPPORT && simStateReady())) {
			mSimImportPref.setEnabled(true);
			mSimImportPref.setSelectable(true);

			if (ContactsUtils.mIsIUNIDeviceOnly) {
				ImportExportUtil.getInstance().setSimListner(new ResultListener() {
					@Override
					public void onResult(Integer result) {
						if (result == 0) {
							mSimImportPref.setEnabled(false);
							mSimImportPref.setSelectable(false);
							if (!mSimExportFlag) {
								mSdExportPref.setEnabled(false);
								mSdExportPref.setSelectable(false);
							}
						}
						
					}
				});
				ImportExportUtil.getInstance().startQuerySimContactsTask();
			}
		} else {
			mSimImportPref.setEnabled(false);
			mSimImportPref.setSelectable(false);
		}

		mSdImportPref.setOnPreferenceClickListener(this);
		mSd2ImportPref.setOnPreferenceClickListener(this);// aurora add
															// zhouxiaobing
															// 20131211

		if (AuroraStorageManager.getInstance(this).getInternalStoragePath() != null) {
			mSdImportPref.setEnabled(true);
			mSdImportPref.setSelectable(true);
		} else {
			mSdImportPref.setEnabled(false);
			mSdImportPref.setSelectable(false);
		}

		if (AuroraStorageManager.getInstance(this).getExternalStoragePath() != null) {
			if (findPreference("pref_key_import_sd2") == null)
				((AuroraPreferenceGroup) (getPreferenceScreen()
						.getPreference(0))).addPreference(mSd2ImportPref);// aurora
																			// add
																			// zhouxiaobing
																			// 20140422
		} else {
			((AuroraPreferenceGroup) (getPreferenceScreen().getPreference(0)))
					.removePreference(mSd2ImportPref);
		}

		accounts = loadAccountFilters(mContext);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);// aurora
																									// add
																									// zhouxiaobing
																									// 20131205

		IntentFilter loadIntentFilter = new IntentFilter(
				SIM_CONTACTS_LOADED_ACTION);
		this.registerReceiver(mReceiver, loadIntentFilter);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
    
	
	private static List<AccountWithDataSetEx> loadAccountFilters(Context context) {
        List<AccountWithDataSetEx> accountsEx = new ArrayList<AccountWithDataSetEx>();
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(context);
        List<AccountWithDataSet> accounts = accountTypes.getAccounts(false);
        for (AccountWithDataSet account : accounts) {
            AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
            if (accountType.isExtension() && !account.hasData(context)) {
                // Hide extensions with no raw_contacts.
                continue;
            }

            int slot = 0;
            if (account instanceof AccountWithDataSetEx) {
                slot = ((AccountWithDataSetEx) account).getSlotId();
            }
            if (account.type.equals(LocalPhoneAccountType.ACCOUNT_TYPE)) {
            	mPhoneAccount = new AccountWithDataSetEx(account.name, account.type, slot);
            } else if (account.name.startsWith("SIM") || account.name.startsWith("USIM") || account.name.startsWith("UIM")) {
            	mSimAccount = new AccountWithDataSetEx(account.name, account.type, slot);
            }
            
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
            	if (slot == 0) {
            		mSim1Account = mSimAccount;
            	} else if (slot == 1) {
            		mSim2Account = mSimAccount;
            	}
            }
            
            accountsEx.add(new AccountWithDataSetEx(account.name, account.type, slot));
        }

        return accountsEx;
    }
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            Log.v(TAG, "requestCode="+requestCode+"resultCode="+resultCode);
            if (resultCode == Activity.RESULT_OK) {
            	switch (requestCode) {
            	case AURORA_EXPORT_TO_SIM: {
            		Bundle extra = data.getExtras();
            		long[] contactsIds = extra.getLongArray(AuroraContactListMultiChoiceActivity.EXTRA_ID_ARRAY);
	                 
	                if (null != contactsIds) {
	                	doExport(AURORA_EXPORT_TO_SIM, extra);
	                }
                    //finish();
	                finishToFirstActivity();//aurora change zhouxiaobing 20121220
                    break;
            	}
            	case AURORA_EXPORT_TO_SD:{
            		
            		Bundle extra = data.getExtras();
            		long[] contactsIds = extra.getLongArray(AuroraContactListMultiChoiceActivity.EXTRA_ID_ARRAY);
	                 
	                if (null != contactsIds) {
	                	doExport(AURORA_EXPORT_TO_SD,extra);
	                }
            		
	                //finish();
	                finishToFirstActivity();//aurora change zhouxiaobing 20121220
                    break;
            	}
            	case REQUEST_CODE:{
            		//finish();
            		finishToFirstActivity();//aurora change zhouxiaobing 20121220
            	}
            	}
            }
    }
	
	private void showSimContacts(int slotid) {
		if (ContactsUtils.mIsIUNIDeviceOnly) {
			mHandler.sendMessage(mHandler.obtainMessage(END_REFRESH_SIM_IMPORT));
		} else {
			mSlot = slotid;
			mHandler.sendMessage(mHandler.obtainMessage(START_REFRESH_SIM_3RD));
		}
    }
	
    void doExport(int i, Bundle extra) {

		long[] rawcontactsIds = extra.getLongArray(AuroraContactListMultiChoiceActivity.EXTRA_ID_ARRAY);
		long[] contactsIds = extra.getLongArray(AuroraContactListMultiChoiceActivity.EXTRA_CONTACT_ID_ARRAY);
        int[] simIndexIds = extra.getIntArray(AuroraContactListMultiChoiceActivity.EXTRA_SIM_INDEX_ARRAY);
    	if (i == AURORA_EXPORT_TO_SIM) {
    		// Process sim data, sim id or slot
            
            Log.d(TAG, "[copyContactsToSim]AccountName:" + targetAccountWithDataSetEx.name
                    + "|accountType:" + targetAccountWithDataSetEx.type);
            int dstSlotId = targetAccountWithDataSetEx.getSlotId();
            //qc modify begin      
            long dstSimId = ContactsUtils.getSubIdbySlot(dstSlotId);
            // qc modify end
            Log.d(TAG, "[copyContactsToSim]dstSlotId:" + dstSlotId + "|dstSimId:" + dstSimId);
            boolean isTargetUsim = SimCardUtils.isSimUsimType((int)dstSimId);
            String dstSimType = isTargetUsim ? "USIM" : "SIM";
            Log.d(TAG, "[copyContactsToSim]dstSimType:" + dstSimType);
            mResolver = this.getContentResolver();

    		ExportThread exportThread = null;
            if (null != exportThread && exportThread.isAlive()) {
                return;
            }
            
            sendMessage(mHandler, START_EXPORT, 0, 1);
            
            if (null == exportThread) {
                exportThread = new ExportThread(contactsIds, dstSlotId, dstSimId);
            }
            exportThread.start();
            
    	} else if (i == AURORA_EXPORT_TO_SD) {
    		Log.e(TAG,"contactsIds.::"+contactsIds.length);

        	int count = rawcontactsIds.length;
        	StringBuilder exportSelection = new StringBuilder();
            exportSelection.append(Contacts._ID + " IN (");

            int curIndex = 0;
            for (int position = 0; position < count; ++position) {
                    long contactId = contactsIds[position];
                    Log.d(TAG, "contactId = " + contactId);
                    if (curIndex++ != 0) {
                        exportSelection.append("," + contactId);
                    } else {
                        exportSelection.append(contactId);
                    }

            }

            exportSelection.append(")");
            Log.d(TAG, "doExportVCardToSDCard exportSelection is " + exportSelection.toString());
            Intent exportIntent = new Intent(this, ExportVCardActivity.class);
            exportIntent.putExtra("exportselection", exportSelection.toString());
            startActivityForResult(exportIntent, AuroraContactImportExportActivity.REQUEST_CODE);
            
    	}
    	
    }
    
    //aurora add zhouxiaobing 20131203 start
    public void exporttoSim() {
        int dstSlotId = targetAccountWithDataSetEx.getSlotId();
        //qc modify begin
        Log.v(TAG, "exporttoSim1");
        long dstSimId = ContactsUtils.getSubIdbySlot(dstSlotId);
        // qc modify end
        long contactsIds[]=null;
        boolean isTargetUsim = SimCardUtils.isSimUsimType((int)dstSimId);
        String dstSimType = isTargetUsim ? "USIM" : "SIM";
        mResolver = this.getContentResolver();
        Cursor cursor=getContentResolver().query(RawContacts.CONTENT_URI, null,RawContacts.INDICATE_PHONE_SIM+"< 0 AND deleted < 1", null, null);
        Log.v(TAG, "cursor="+cursor.getCount());
        if(cursor!=null) {
			cursor.moveToFirst();
			contactsIds=new long[cursor.getCount()];
			Log.v(TAG, "cursor count="+cursor.getCount());
			for(int i=0; i<contactsIds.length; i++) {
				contactsIds[i]=cursor.getInt(cursor.getColumnIndexOrThrow("contact_id"));
				cursor.moveToNext();
			}
			cursor.close();
		}
        ExportThread exportThread = null;
        if (null != exportThread && exportThread.isAlive()) {
            return;
        }
        
        sendMessage(mHandler, START_EXPORT, 0, 1);
        Toast.makeText(mContext, R.string.aurora_start_daochu, Toast.LENGTH_SHORT).show();
        if (null == exportThread) {
            exportThread = new ExportThread(contactsIds, dstSlotId, dstSimId);
        }
        exportThread.start();
    	
    }
    //aurora add zhouxiaobing 20131203 end
    private static int export_tosim_count;
    public  static boolean is_importexporting_sim=false;
    
    private class ExportThread extends Thread {
        private long[] adArray;
        private int dstSlotId;
        private long dstSimId;
        public ExportThread(long[] adArray, int dstSlotId, long dstSimId) {
            this.adArray = adArray;
            this.dstSlotId = dstSlotId;//aurora change zhouxiaobing 20140421
            if (FeatureOption.MTK_GEMINI_SUPPORT)
               this.dstSimId = dstSimId;//aurora change zhouxiaobing 20140421
            else
               this.dstSimId = 1;
        }
        
        
        @Override
        public void run() {
            int successfulItems = 0;
            int totalItems = adArray.length;
            boolean isSimStorageFull = false;
            final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

            int iccRecordsSize = 0;
            int iccRecordSizeCapacity = 0;
            int needImportCount = 0;
            int nSuccessCount = 0;
            
          is_importexporting_sim=true;
          ArrayList<String> numberArray = new ArrayList<String>();
          ArrayList<String> additionalNumberArray = new ArrayList<String>();
          ArrayList<String> emailArray = new ArrayList<String>();
          String targetName = null;
          
          boolean isTargetUsim = SimCardUtils.isSimUsimType((int)dstSimId);
          int max_count=0;
          int now_count=0;
          if(FeatureOption.MTK_GEMINI_SUPPORT) {
    		  select=RawContacts.INDICATE_PHONE_SIM+" = "+dstSimId;
    	  } else {
    		  select=RawContacts.INDICATE_PHONE_SIM+" > 0";
    	  }
          
          Cursor simCursor = getContentResolver().query(RawContacts.CONTENT_URI, null, select+" AND deleted < 1", null, null);
          if(simCursor!=null) {
        	  simCursor.moveToFirst();
        	  now_count=simCursor.getCount();
        	  simCursor.close();
          }
          if(isTargetUsim)
        	  max_count=500;
          else
        	  max_count=250;
          String dstSimType = isTargetUsim ? "USIM" : "SIM";
          Log.d(TAG, "[copyContactsToSim]dstSimType:" + dstSimType);      
            int zi=0;
            export_tosim_count=0;
            int max_need_export_count=0;
            for(zi=0;zi<adArray.length;zi++) {  //for (long contactId : adArray) {
            	
            	if(now_count+export_tosim_count >= max_count) {
            		 max_need_export_count+=1;
            		 break;
            	}
            	long contactId=adArray[zi];
            	Uri dataUri = Uri.withAppendedPath(
                        ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                        Contacts.Data.CONTENT_DIRECTORY);
                final String[] projection = new String[] {
                        Contacts._ID, 
                        Contacts.Data.MIMETYPE, 
                        Contacts.Data.DATA1,
                        Contacts.Data.IS_ADDITIONAL_NUMBER
                };
                
                Cursor c = mResolver.query(dataUri, projection, null, null, null);
                
                if (c != null && c.moveToFirst()) {
                    do {
                        String mimeType = c.getString(1);
                        if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // For phone number
                            String number = c.getString(2);
                            //arurora add by liguangyu
                            if(!TextUtils.isEmpty(number)) {
                            	number = number.replaceAll(" ", "");
                            }
                            String isAdditionalNumber = c.getString(3);
                            if (Pattern.matches(SIM_NUM_PATTERN, number)) {//aurora add zhouxiaobing 20140310 for filter invalid number
                            
                            if (isAdditionalNumber != null && isAdditionalNumber.equals("1")) {
                                additionalNumberArray.add(number);
                            } else {
                                numberArray.add(number);
                            }
                            }
                        } else if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                            // For name
                            targetName = c.getString(2);
                        }
/*                        if (isTargetUsim) {
                            if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                                // For email
                                String email = c.getString(2);
                                emailArray.add(email);
                            } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                           
                            }
                        }*///aurora change zhouxiaobing 20140113
                    } while (c.moveToNext());
                }
                if (c != null)
                    c.close();
                // Aurora xuyong 2015-07-13 modified for bug #14161 start
                Uri dstSimUri = ContactsUtils.getSimContactsUri((int)dstSimId, SimCardUtils.isSimUsimType((int)dstSimId));//Uri.parse("content://auroraicc/auroraadn");
                // Aurora xuyong 2015-07-13 modified for bug #14161 end
                                            //aurora change zhouxiaobing 20140421
                Log.v(TAG, "dstSimUri="+dstSimUri+" now_count="+now_count+" export_tosim_count="+export_tosim_count);
                int maxCount = 0;//TextUtils.isEmpty(targetName) ? 0 : 1;aurora change zhouxiaobing 20140113
                if (isTargetUsim) {
                    int numberCount = numberArray.size();
                    int additionalCount = additionalNumberArray.size();
                    int emailCount = emailArray.size();
                    maxCount = (maxCount > numberCount) ? maxCount : numberCount;
                    maxCount = (maxCount > additionalCount) ? maxCount : additionalCount;
                    maxCount = (maxCount > emailCount) ? maxCount : emailCount;
                } else {
                    numberArray.addAll(additionalNumberArray);
                    additionalNumberArray.clear();
                    int numberCount = numberArray.size();
                    maxCount = maxCount > numberCount ? maxCount : numberCount;
                }
                //aurora add zhouxiaobing start 20140217 start
                if(targetName==null||targetName.length()==0)
                {
                	maxCount=0;
                }
               //aurora add zhouxiaobing start 20140217 end
                int sameNameCount = 0;
                ContentValues values = new ContentValues();
                String simTag = null;
                String simNum = null;
                String simAnrNum = null;
                String simEmail = null;

                simTag = sameNameCount > 0 ? (targetName + sameNameCount) : targetName;
                simTag = TextUtils.isEmpty(simTag) ? "" : simTag;
                if ((simTag == null || simTag.isEmpty() || simTag.length() == 0)
                        && numberArray.isEmpty()) {
                    Log.e(TAG, " name and number are empty");
//                    errorCause = ErrorCause.ERROR_UNKNOWN;
                    continue;
                }
                //aurora add by liguangyu for 17039 
                Log.d(TAG, "simTag length is " + simTag.getBytes().length);        
                if (simTag.getBytes().length == simTag.length()) {
                    if (simTag.length() > 14) {
                    	simTag = simTag.substring(0, 14);
                    }
                } else if (simTag.length() > 6) {
                    simTag = simTag.substring(0, 6);                    
                }            

                int subContact = 0;
                max_need_export_count+=maxCount;
                for (int i = 0; i < maxCount; i++) {
                    // Gionee:wangth 20130307 add for CR00778421 begin
//                    if (GNContactsUtils.isOnlyQcContactsSupport() && nSuccessCount >= needImportCount) {
////                        errorCause = ErrorCause.SIM_STORAGE_FULL;
//                        isSimStorageFull = true;
//                        Log.d(TAG, "********* storage full");
//                        break; // SIM card filled up
//                    }
                    // Gionee:wangth 20130307 add for CR00778421 end
                    
                    values.put("tag", simTag);
                    Log.d(TAG, "copyContactsToSim tag is " + simTag);
                    simNum = null;
                    simAnrNum = null;
                    simEmail = null;
                    if (!numberArray.isEmpty()) {
                        simNum = numberArray.remove(0);
                        simNum = TextUtils.isEmpty(simNum) ? "" : simNum.replace("-", "");
                        values.put("number", PhoneNumberUtils.stripSeparators(simNum));
                        Log.d(TAG, "copyContactsToSim number is " + simNum);
                    }

                    if (isTargetUsim) {
                        Log.d(TAG, "copyContactsToSim copy to USIM");
                        if (!additionalNumberArray.isEmpty()) {
                            simAnrNum = additionalNumberArray.remove(0);
                            simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-",
                                    "");
                            // Gionee:wangth 20130306 modify begin
                            /*
                            values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                            */
                            if (GNContactsUtils.isOnlyQcContactsSupport()) {
                                values.put("anrs", PhoneNumberUtils.stripSeparators(simAnrNum));
                            } else {
                                values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                            }
                            // Gionee:wangth 20130306 modify end
                            Log.d(TAG, "copyContactsToSim anr is " + simAnrNum);
                        }
                        // Gionee jialf 20130319 added for CR00785982 start
                        else {
                            if (OperatorUtils.getOptrProperties().equals("OP02")) {
                                if (!numberArray.isEmpty()) {
                                    simAnrNum = numberArray.remove(0);
                                    simAnrNum = TextUtils.isEmpty(simAnrNum) ? "" : simAnrNum.replace("-","");
                                    if (GNContactsUtils.isOnlyQcContactsSupport()) {
                                        values.put("anrs", PhoneNumberUtils.stripSeparators(simAnrNum));
                                    } else {
                                        values.put("anr", PhoneNumberUtils.stripSeparators(simAnrNum));
                                    }
                                    maxCount--;
                                }
                            }
                        }
                        // Gionee jialf 20130319 added for CR00785982 end
                        

                        if (!emailArray.isEmpty()) {
                            simEmail = emailArray.remove(0);
                            simEmail = TextUtils.isEmpty(simEmail) ? "" : simEmail;
                            values.put("emails", simEmail);
                            Log.d(TAG, "copyContactsToSim emails is " + simEmail);
                        }
                    }
                    Log.i(TAG, "Before insert Sim card.");
                    Uri retUri = null;
                    try  {
                    	retUri = mResolver.insert(dstSimUri, values);
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                    Log.i(TAG, "After insert Sim card.");

                    Log.i(TAG, "retUri is " + retUri);
                    if (retUri != null) {
                        List<String> checkUriPathSegs = retUri.getPathSegments();
                        for(String s:checkUriPathSegs)
                        	Log.i(TAG, "s is " + s);
                        
                        if ("error".equals(checkUriPathSegs.get(0))) {
                            String errorCode = checkUriPathSegs.get(1);
                            Log.i(TAG, "error code = " + errorCode);
                            if (true) {
//                                printSimErrorDetails(errorCode);
                            }
//                            errorCause = ErrorCause.ERROR_UNKNOWN;
                            if ("-3".equals(checkUriPathSegs.get(1))) {
//                                errorCause = ErrorCause.SIM_STORAGE_FULL;
                                isSimStorageFull = true;
                                Log.e(TAG, "Fail to insert sim contacts fail"
                                        + " because sim storage is full. zi="+zi);
//                                startnotifySimExport(0, export_tosim_count, adArray.length);//aurora add ukiliu 20150723
                                break;
                            }
                            isSimStorageFull = true;
                        } else {
                            Log.d(TAG, "insertUsimFlag = true zi="+zi);
                            long indexInSim = 502;//ContentUris.parseId(retUri);
                            SubContactsUtils.buildInsertOperation(operationList,
                                    targetAccountWithDataSetEx, simTag, simNum, simEmail,
                                    simAnrNum, mResolver, dstSimId, dstSimType,
                                    indexInSim, null);
                            subContact ++;
                            export_tosim_count++;
                            startnotifySimExport(0,export_tosim_count,adArray.length);//aurora add zhouxiaobing 20131205
                            //successfulItems++;
                        }
                        
                        // Gionee:wangth 20130307 add for CR00778421 begin
                        if (GNContactsUtils.isOnlyQcContactsSupport()) {
                            nSuccessCount++;
                        }
                        // Gionee:wangth 20130307 add for CR00778421 end
                    } else {
//                        errorCause = ErrorCause.ERROR_UNKNOWN;
                    	isSimStorageFull=true;
                    }
                    
                    sendMessage(mHandler, REFRESH, successfulItems, adArray.length);
                    
                    if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                        try {
                            mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                            try {
                            	Thread.sleep(300);
                            } catch (InterruptedException ie) {
                            	ie.printStackTrace();
                            }
                        } catch (android.os.RemoteException e) {
                        } catch (android.content.OperationApplicationException e) {
                        }
                        operationList.clear();
                    }
                }// inner looper
                if (subContact > 0) {
                    successfulItems ++;
                }
                if (isSimStorageFull)
                {
                	//Toast.makeText(mContext, "成功导出"+(zi+1)+"个联系人到sim卡，因sim卡已满，余下联系人未能导出.", Toast.LENGTH_SHORT).show();
                	//sendMessage(mHandler, END_EXPORT, adArray.length, adArray.length);
          //      	break;
                }
                
            }
            
            if (operationList.size() > 0) {
                try {
                    Log.i(TAG, "Before end applyBatch. ");
                    mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                    Log.i(TAG, "After end applyBatch ");
                } catch (android.os.RemoteException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                } catch (android.content.OperationApplicationException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                }
                operationList.clear();
            }
            Log.v(TAG, "zi="+zi+"adArray.length="+adArray.length+"isSimStorageFull="+isSimStorageFull);
			if (export_tosim_count == 0) {
				if (max_need_export_count == 0) {
					if (now_count >= max_count) {
						Log.v(TAG, "logtag1");
						sendMessage(mHandler, END_EXPORT_SIM_FULL, zi,
								adArray.length);
					} else {
						Log.v(TAG, "logtag2");
						sendMessage(mHandler, END_EXPORT_SIM_ERROR, zi, -1);
					}
				} else {
					if (now_count >= max_count) {
						Log.v(TAG, "logtag3");
						sendMessage(mHandler, END_EXPORT_SIM_FULL, zi,
								adArray.length);
					} else {
						Log.v(TAG, "logtag4");
						if(isSimStorageFull){
							sendMessage(mHandler, END_EXPORT_SIM_FULL, zi,
									adArray.length);
						} else {
							sendMessage(mHandler, END_EXPORT_SIM_ERROR, zi,
									adArray.length);
						}
					}
				}

			} else {
				if (now_count + export_tosim_count >= max_count) {
					if (export_tosim_count < max_need_export_count) {
						Log.v(TAG, "logtag5 export_tosim_count="
								+ export_tosim_count + "max_need_export_count="
								+ max_need_export_count);
						sendMessage(mHandler, END_EXPORT, export_tosim_count, 0);
						endnotifySimExport2(0, export_tosim_count);
					} else {
						Log.v(TAG, "logtag6 export_tosim_count="
								+ export_tosim_count + "max_need_export_count="
								+ max_need_export_count);
						sendMessage(mHandler, END_EXPORT, export_tosim_count,
								max_need_export_count);
						endnotifySimExport(0);
					}
				} else {
					if (export_tosim_count < max_need_export_count) {
						Log.v(TAG, "logtag7 export_tosim_count="
								+ export_tosim_count + "max_need_export_count="
								+ max_need_export_count);
						sendMessage(mHandler, END_EXPORT, export_tosim_count,
								-1);
						endnotifySimExport(-1);
					} else {
						Log.v(TAG, "logtag8 export_tosim_count="
								+ export_tosim_count + "max_need_export_count="
								+ max_need_export_count);
						sendMessage(mHandler, END_EXPORT, export_tosim_count,
								max_need_export_count);
						endnotifySimExport(0);
					}
				}

			}
            is_importexporting_sim=false;
        }
    }

    private void sendMessage(Handler handler, int what, int arg1, int arg2) {
        if (null != handler) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            Log.i(TAG,"msg.what::"+msg.what);
            handler.sendMessage(msg);
        }
    }
    
	private Handler mHandler = new Handler() {
	        
	       @Override
	       public void handleMessage(Message msg) {
	            
	           switch(msg.what) {
	           case START_EXPORT: {
	        	   if (null != AuroraContactImportExportActivity.this && !isFinishing()) {
	        		   if (null == mImportProgressDialog) {
	                        mImportProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
	                    }
	                    mImportProgressDialog.setTitle(R.string.aurora_export_contact_to_sim);
	                    mImportProgressDialog.setIndeterminate(true);
	                    mImportProgressDialog.setMax(1);
	                    mImportProgressDialog.setProgress(msg.arg1);
	                    mImportProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	                    try {
	                        mImportProgressDialog.show();
	                    } catch (Exception e) {
	                        
	                    }
	        	   }

	        	   break;
	           }
	           
	           case REFRESH: {
	        	   if (null != AuroraContactImportExportActivity.this && !isFinishing() 
	                        && null != mImportProgressDialog && mImportProgressDialog.isShowing()) {
	                    mImportProgressDialog.setTitle(R.string.aurora_export_contact_to_sim);
	                    mImportProgressDialog.setMax(msg.arg2);
	                    mImportProgressDialog.setProgress(msg.arg1);
	                    mImportProgressDialog.setIndeterminate(false);
	                }
	        	   break;
	           }
	           
	           case END_EXPORT: {
	        	   if (null != AuroraContactImportExportActivity.this && !isFinishing() 
	                        && null != mImportProgressDialog && mImportProgressDialog.isShowing()) {
	        		   try {
	                        mImportProgressDialog.dismiss();
	                    } catch (Exception e) {
	                        
	                    }
	        		   
	                }
	        	   
	        	  if (msg.arg2 == 0) {
	        		  Toast.makeText(mContext, mContext.getString(R.string.aurora_daochu_sim_full2), Toast.LENGTH_SHORT).show();  
	        	  } else if(msg.arg2 == -1) {
	        		  Toast.makeText(mContext, getString(R.string.aurora_daochu_dao_sim,msg.arg1), Toast.LENGTH_SHORT).show();
	        	  } else {
	        		  Toast.makeText(mContext, R.string.aurora_end_daochu, Toast.LENGTH_SHORT).show();
	        	  }
	        	   
	        	   //finish();

	        	   break;
	           }
	           
	           //aurora add zhouxiaobing 20131202 start    
	           case END_REFRESH_SIM_IMPORT: {
	        	    
					if (FeatureOption.MTK_GEMINI_SUPPORT) {
						int[] subId = SubscriptionManager.getSubId(mImportExportSlot);
						select = RawContacts.INDICATE_PHONE_SIM + " = " + subId[0];
					} else {
						select = RawContacts.INDICATE_PHONE_SIM + "> 0";
					}
					msimCursor = getContentResolver().query(
							RawContacts.CONTENT_URI, null,
							select + " AND deleted < 1", null, null);
					Log.e(TAG, "select: "+select +"; mImportSlot: "+mImportExportSlot);
	
					if (msimCursor == null || msimCursor.getCount() == 0) {
						noSimContactsImport();
					} else {
						realyImportSimContacts();
					}
					msimCursor.close();
					msimCursor = null;
	
					break;
				}
	           
	           case END_IMPORT: {
	        	   //msimCursor.close();
	        	   Toast.makeText(AuroraContactImportExportActivity.this, R.string.aurora_end_daoru, Toast.LENGTH_SHORT).show();
	        	   break;
	           }
	           
	           case START_IMPORT: {
	        	   //msimCursor.close();
	        	   Toast.makeText(AuroraContactImportExportActivity.this, R.string.aurora_start_daoru, Toast.LENGTH_SHORT).show();
	        	   break;
	           }
	           
	           case END_EXPORT_SIM_FULL: {
	        	   Toast.makeText(mContext, R.string.aurora_daochu_sim_full, Toast.LENGTH_SHORT).show();
	        	   break;
	           }
	        	   
	           case END_EXPORT_SIM_ERROR: {
	        	   if(msg.arg2==-1) {
	        		   Toast.makeText(mContext, R.string.aurora_no_contacts_to_sim, Toast.LENGTH_SHORT).show();  
	        	   } else {
	        	       Toast.makeText(mContext, R.string.aurora_daochu_sim_error, Toast.LENGTH_SHORT).show();
	        	   }
	        	   break;
	           }
		       //aurora add zhouxiaobing 20131202 end
	           
	           // add for 3rd phone
	           case START_REFRESH_SIM_3RD: {
	        	   startQuery();
	        	   break;
	           }
	           
	           case END_REFRESH_SIM_3RD: {
	        	   if (mRefreshSimContactsDig != null) {
	        		   mRefreshSimContactsDig.dismiss();
	        	   }
	        	   
	        	   if (mNamesArray == null || (mNamesArray != null && mNamesArray.size() < 1)) {
	        		   noSimContactsImport();
	        	   } else {
	        		   realyImportSimContacts();
	        	   }
	        	   
	        	   break;
	           }
	           
	           case ERROR: {
	            	ContactsUtils.toastManager(mContext, R.string.aurora_sim_not_ready);
	            	break;
	           }
	           
	           }
	          
	           super.handleMessage(msg);
	       }
	};
	
	private void startQuery() {
    	// single card only
    	if (!simStateReady(0)) {
    		ContactsUtils.toastManager(mContext, R.string.aurora_sim_not_ready);
    		return;
    	} else {
    		if (mRefreshSimContactsDig == null) {
 			   mRefreshSimContactsDig = new ContactsUtils.AuroraContactsProgressDialog(AuroraContactImportExportActivity.this,
 					   AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
 			   mRefreshSimContactsDig.setTitle(R.string.aurora_loading_sim_contacts_title);
            }
 		    mRefreshSimContactsDig.show();
 		   
 		    new QueryThread().start();
    	}
    }
	
	private class QueryThread extends Thread {
    	private int slot = 0;
    	
    	public QueryThread() {
    		slot = mSlot;
    	}
    	
    	@Override
        public void run() {
    		if (!simStateReady(slot)) {
	    		ContactsUtils.toastManager(mContext, R.string.aurora_sim_not_ready);
	    		finish();
	    		return;
	    	}
    		
    		if (GNContactsUtils.isOnlyQcContactsSupport()) {
    			sendQcSimState(slot);
    		} else {
    			sendMtkSimState(slot);
    		}
			
			int i = 0;
			try {
				while (true) {
					if (isFinished) {
						break;
					}
					
					if (i >= QUERY_TIMEOUT) {
						mHandler.sendEmptyMessage(ERROR);
						break;
					}
					
					Thread.sleep(1000);
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
	
	private void sendQcSimState(int slot) {
//		Bundle args = new Bundle();
//		if (FeatureOption.MTK_GEMINI_SUPPORT) {
//			args.putInt(GNContactsUtils.SUB, slot);
//		}
//		args.putInt(SimContactsService.OPERATION, GNContactsUtils.OP_SIM);
//		args.putInt(SimContactsService.SIM_STATE, GNContactsUtils.SIM_STATE_READY);
//		Intent intent = new Intent(mContext, SimContactsService.class).putExtras(args);
//		mContext.stopService(intent);
//		mContext.startService(intent);
	}
	
	private void sendMtkSimState(int slot) {
		Intent intent = null;
        if (slot == 0) {
            intent = new Intent(mContext, StartSIMService.class);
        } else if (slot == 1) {
            intent = new Intent(mContext, StartSIMService2.class);
        }
        
        intent.putExtra(AbstractStartSIMService.SERVICE_SLOT_KEY, slot);
        intent.putExtra(AbstractStartSIMService.SERVICE_WORK_TYPE, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        mContext.startService(intent);
	}
	
	private class SimContactsLoadedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SIM_CONTACTS_LOADED_ACTION)) {
            	isFinished = true;
            	mNamesArray = intent.getStringArrayListExtra("names");
            	mPhoneNumberArray = intent.getStringArrayListExtra("phones");
            	Log.d(TAG, "SimContactsLoadedBroadcastReceiver loading completed. mNamesArray " + mNamesArray);
            	
            	mHandler.sendEmptyMessage(END_REFRESH_SIM_3RD);
            }
        }
    }
	
	private void noSimContactsImport() {
		if (import_sim_no_dialog == null) {
			import_sim_no_dialog = new AuroraAlertDialog.Builder(AuroraContactImportExportActivity.this)
					.setTitle(mContext.getString(R.string.aurora_menu_import))
					.setMessage(mContext.getString(R.string.aurora_sim_no_contacts))
					.setNegativeButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// msimCursor.close();
									import_sim_no_dialog = null;
								}
							}).create();

			import_sim_no_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							import_sim_no_dialog = null;
						}
					});
			
			import_sim_no_dialog.show();
		}
	}
	
	private void realyImportSimContacts() {
		if (import_sim_dialog == null) {
			int simContactsCount = 0;
			if (!ContactsUtils.mIsIUNIDeviceOnly) {
				simContactsCount = mNamesArray.size();
			} else {
				simContactsCount = msimCursor.getCount();
			}

			import_sim_dialog = new AuroraAlertDialog.Builder(AuroraContactImportExportActivity.this)
					.setTitle(mContext.getString(R.string.aurora_menu_import))
					.setMessage(mContext.getString(R.string.aurora_find_sim_contacts_message, simContactsCount))
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									// msimCursor.close();
									import_sim_dialog = null;
								}
							})
					.setPositiveButton(R.string.aurora_import_title,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									import_sim_dialog = null;
									
									if (!ContactsUtils.mIsIUNIDeviceOnly) {
										new ImportSimContacts3RdThread().start();
									} else {
										msimCursor = getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"},
												select + " AND deleted<1", null,
												null);
										if (msimCursor != null) {
											msimCursor.moveToFirst();
											int len = msimCursor.getCount();
											long ids[] = new long[len];
											for (int i = 0; i < len; i++) {
												ids[i] = msimCursor.getLong(0);
												msimCursor.moveToNext();
											}

											msimCursor.close();
											msimCursor = null;
											new ImportThread(ids).start();
										}
									}

									// finish();
									finishToFirstActivity();
								}
							}).create();

			import_sim_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							import_sim_dialog = null;
						}
					});

			import_sim_dialog.setCanceledOnTouchOutside(false);
			import_sim_dialog.show();
		}
	}
	
	private class ImportSimContacts3RdThread extends Thread {
		public ImportSimContacts3RdThread() {
        }
		
		@Override
        public void run() {
			final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			sendMessage(mHandler, START_IMPORT, mNamesArray.size(), mNamesArray.size());
			is_importexporting_sim = true;
			int successfulItems = 0;
			int i = 0;
			int n = 0;
			
			for (i = 0; i < mNamesArray.size(); i++) {
				int m = 0;
				
				ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
                builder.withValue(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
                builder.withValue(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
                //aurora add by liguangyu
                builder.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
                operationList.add(builder.build());
                m++;
                
                String name = mNamesArray.get(i);
                String phoneNumber = mPhoneNumberArray.get(i);
                Log.d(TAG, "name = " + name + "  phoneNumber = " + phoneNumber);
                if (name == null && phoneNumber == null) {
                	continue;
                }
                
                if (!TextUtils.isEmpty(name)) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, n);
                    builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                    builder.withValue(StructuredName.DISPLAY_NAME, name);
                    operationList.add(builder.build());
                    m++;
                }

                if (!TextUtils.isEmpty(phoneNumber)) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, n);
                    builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    builder.withValue(Phone.NUMBER, phoneNumber);
                    builder.withValue(Data.DATA2, 2);
                    operationList.add(builder.build());
                    m++;
                }
                
                n += m;
                successfulItems++;
                sendMessage(mHandler, REFRESH, successfulItems, mNamesArray.size());
                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                    try {
                        mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                    } catch (android.os.RemoteException e) {
                        e.printStackTrace();
                    } catch (android.content.OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    
                    n = 0;
                    operationList.clear();
                }
                startnotifySimImport(0, successfulItems, mNamesArray.size());
			}
			
			if (operationList.size() > 0) {
                try {
                    mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                } catch (android.os.RemoteException e) {
                	e.printStackTrace();
                } catch (android.content.OperationApplicationException e) {
                	e.printStackTrace();
                }
                
                n = 0;
                operationList.clear();
            }
			
            endnotifySimImport(0);
            sendMessage(mHandler, END_IMPORT, mNamesArray.size(), mNamesArray.size());
			is_importexporting_sim = false;
		}
	}
	
    //aurora add zhouxiaobing 20131202 start
	private Cursor msimCursor;
    private class ImportThread extends Thread {
        private long[] adArray;
        public ImportThread(long[] adArray) {
            this.adArray = adArray;
        }
        
        @Override
        public void run() {
            int successfulItems = 0;
            int totalItems = 0;
            final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            sendMessage(mHandler, START_IMPORT, adArray.length, adArray.length);
            is_importexporting_sim=true;
            for (long id : adArray) {
                Cursor dataCursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
                        DATA_ALLCOLUMNS, Data.RAW_CONTACT_ID + " =? ", 
                        new String[] { String.valueOf(id) }, null);
                if (dataCursor == null || dataCursor.getCount() <= 0) {
                	if(dataCursor != null) {
                		dataCursor.close();
                	}
                    continue;
                }
                Log.v(TAG, "dataCursor="+dataCursor);
                int backRef = operationList.size();
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
                builder.withValue(RawContacts.ACCOUNT_NAME, AccountType.ACCOUNT_NAME_LOCAL_PHONE);
                builder.withValue(RawContacts.ACCOUNT_TYPE, AccountType.ACCOUNT_TYPE_LOCAL_PHONE);
                //aurora add by liguangyu
                builder.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
                operationList.add(builder.build());
                
                dataCursor.moveToPosition(-1);
                String[] columnNames = dataCursor.getColumnNames();
                while (dataCursor.moveToNext()) {
                    String mimeType = dataCursor.getString(dataCursor.getColumnIndex(Data.MIMETYPE));
                    if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        continue;
                    }
                    
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    
                    for (int i = 1; i < columnNames.length; i++) {
                        cursorColumnToBuilder(dataCursor, columnNames, i, builder);
                    }
                    
                    builder.withValueBackReference(Data.RAW_CONTACT_ID, backRef);
                    operationList.add(builder.build());
                }
                
                dataCursor.close();
                successfulItems++;
                Log.v(TAG, "successfulItems="+successfulItems);
                sendMessage(mHandler, REFRESH, successfulItems, adArray.length);
                
                if (operationList.size() > MAX_OP_COUNT_IN_ONE_BATCH) {
                    try {
                        mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                        try {
                        	Thread.sleep(300);
                        } catch (InterruptedException i) {
                        	i.printStackTrace();
                        }
                    } catch (android.os.RemoteException e) {
                        e.printStackTrace();
                    } catch (android.content.OperationApplicationException e) {
                        e.printStackTrace();
                    }
                    operationList.clear();
                }
                startnotifySimImport(0,successfulItems,adArray.length);
            }
            
            if (operationList.size() > 0) {
                try {
                    mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                } catch (android.os.RemoteException e) {
                } catch (android.content.OperationApplicationException e) {
                }
                operationList.clear();
            }
            endnotifySimImport(0);
            sendMessage(mHandler, END_IMPORT, adArray.length, adArray.length);
            is_importexporting_sim=false;
        }
    }
    
    final static String[] DATA_ALLCOLUMNS = new String[] {
        Data._ID,
        Data.MIMETYPE,
        Data.IS_PRIMARY,
        Data.IS_SUPER_PRIMARY,
        Data.DATA1,
        Data.DATA2,
        Data.DATA3,
        Data.DATA4,
        Data.DATA5,
        Data.DATA6,
        Data.DATA7,
        Data.DATA8,
        Data.DATA9,
        Data.DATA10,
        Data.DATA11,
        Data.DATA12,
        Data.DATA13,
        Data.DATA14,
        Data.DATA15,
        Data.SYNC1,
        Data.SYNC2,
        Data.SYNC3,
        Data.SYNC4,
        Data.IS_ADDITIONAL_NUMBER
    };
    
    public static final String DEFAULT_NOTIFICATION_TAG = "SIMServiceProgress";
    private  NotificationManager mNotificationManager;
    
    private void cursorColumnToBuilder(Cursor cursor, String[] columnNames,
            int index, ContentProviderOperation.Builder builder) {
        switch (cursor.getType(index)) {
        case Cursor.FIELD_TYPE_NULL:
            // don't put anything in the content values
            break;
        case Cursor.FIELD_TYPE_INTEGER:
            builder.withValue(columnNames[index], cursor.getLong(index));
            break;
        case Cursor.FIELD_TYPE_STRING:
            builder.withValue(columnNames[index], cursor.getString(index));
            break;
        case Cursor.FIELD_TYPE_BLOB:
            builder.withValue(columnNames[index], cursor.getBlob(index));
            break;
        default:
            throw new IllegalStateException("Invalid or unhandled data type");
        }
    }    
     public static Notification constructProgressNotification(
            Context context, int type, String description,
            int totalCount, int currentCount) {

        int iconid=(type ==0? R.drawable.aurora_stat_sys_download_done : R.drawable.gn_stat_sys_upload_done_static);
//        int stringid=(type ==0? R.string.aurora_start_daoru:R.string.aurora_start_daochu);
        final Notification.Builder builder = new Notification.Builder(context);
        builder.setOngoing(true)
                .setProgress(totalCount, currentCount, totalCount == - 1)
                .setContentTitle(description)
                .setSmallIcon(iconid)
                // Gionee:wangth 20120712 modify for CR00640215 end
                .setContentIntent(/*PendingIntent.getActivity(context, 0, intent, 0)*/null);//aurora change zhouxiaobing 20131205
        if (totalCount > 0) {
            builder.setContentText(context.getString(R.string.percentage,
                    String.valueOf(currentCount * 100 / totalCount)));
        }
        return builder.getNotification();
    }    
     public static Notification constructfinishNotification(
             Context context, int type) {
         if(type!=-1)
         {
         int iconid=(type ==0? R.drawable.aurora_stat_sys_download_done : R.drawable.gn_stat_sys_upload_done_static);
         int stringid=(type ==0? R.string.aurora_end_daoru:R.string.aurora_end_daochu);
         final Notification.Builder builder = new Notification.Builder(context);
         Intent intents=new Intent(context, AuroraPeopleActivity.class);
         intents.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         builder.setAutoCancel(true)
                 .setContentTitle(context.getString(stringid))
                 .setSmallIcon(iconid)
                 // Gionee:wangth 20120712 modify for CR00640215 end
                 .setContentIntent(PendingIntent.getActivity(context, 0, intents, 0));//aurora change zhouxiaobing 20131205
         
         return builder.getNotification();
         }
         else
         {
        	 int iconid=R.drawable.gn_stat_sys_upload_done_static;
             int stringid=R.string.aurora_end_daoru;
             final Notification.Builder builder = new Notification.Builder(context);
             builder.setAutoCancel(true)
                     .setContentTitle(context.getString(stringid))
                     .setSmallIcon(iconid)
                     // Gionee:wangth 20120712 modify for CR00640215 end
                     .setContentIntent(/*PendingIntent.getActivity(context, 0, intent, 0)*/null);//aurora change zhouxiaobing 20131205
             builder.setContentTitle(context.getString(R.string.aurora_daochu_dao_sim,export_tosim_count));
             return builder.getNotification(); 
         }
     }       
     static Notification constructfinishNotification2(
             Context context, int type,int count) {

         int iconid=(type ==0? R.drawable.aurora_stat_sys_download_done : R.drawable.gn_stat_sys_upload_done_static);
         int stringid=R.string.aurora_daochu_sim_full3;//(type ==0? R.string.aurora_end_daoru:R.string.aurora_end_daochu);
         final Notification.Builder builder = new Notification.Builder(context);
         builder.setAutoCancel(true)
                 .setContentTitle(context.getString(stringid))
                 .setContentText(context.getString(R.string.aurora_daochu_dao_sim,count))
                 .setSmallIcon(iconid)
                 // Gionee:wangth 20120712 modify for CR00640215 end
                 .setContentIntent(/*PendingIntent.getActivity(context, 0, intent, 0)*/null);//aurora change zhouxiaobing 20131205
         return builder.getNotification();
     }   
    public void startnotifySimImport(int jobid,int currentCount,int totalCount)
    {
    	String description=mContext.getString(R.string.aurora_start_daoru);
    	final Notification notification = constructProgressNotification(
                mContext.getApplicationContext(), 0, description,
                totalCount, currentCount);
        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobid, notification);   	
    }
    public void endnotifySimImport(int jobid)
    {
    	final Notification notification = constructfinishNotification(
                mContext.getApplicationContext(), 0);
        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobid, notification);     	
    }
    public void startnotifySimExport(int jobid,int currentCount,int totalCount)
    {    	
    	if(currentCount>=totalCount)
    		currentCount=totalCount-1;//aurora zhouxiaobing set for the max_num is can't count;
    	String description=mContext.getString(R.string.aurora_start_daochu);
    	final Notification notification = constructProgressNotification(
                mContext.getApplicationContext(), 1, description,
                totalCount, currentCount);
        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobid, notification);   	
    }
    public void endnotifySimExport(int jobid)
    {
    	final Notification notification;
    	if(jobid==0)
    	{
    		notification= constructfinishNotification(
                mContext.getApplicationContext(), 1);
    	}
    	else
    	{
    		notification= constructfinishNotification(
                    mContext.getApplicationContext(), -1);
    	}
    	
        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, 0, notification);     	
    }    
    public void endnotifySimExport2(int jobid,int count)
    {
    	final Notification notification = constructfinishNotification2(
                mContext.getApplicationContext(), 1,count);
    	Intent intent=new Intent();
    	intent.setClass(this,AuroraPeopleActivity.class);
    	notification.contentIntent=PendingIntent.getActivity(this, 0, intent, 0);
        mNotificationManager.notify(DEFAULT_NOTIFICATION_TAG, jobid, notification);     	
    }    
    
//aurora add zhouxiaobing 20131202 end	
	private static File mFile;
    
    public static String getExternalStorageState() {
        try {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                    .getService("mount"));
            Log.i(TAG,"[getExternalStorageState] mFile : "+mFile);
            return mountService.getVolumeState(mFile
                    .toString());
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }
    }
    
    public File getExternalStorageDirectory(){
        StorageManager mSM = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
        String path = null;
        path = AuroraStorageManager.getInstance(mContext).getSdCardPath(0);
        File file;
        
     
            file = getDirectory(path, "/mnt/sdcard");
        
        Log.i(TAG,"[getExternalStorageDirectory]file.path : "+file.getPath());
        mFile = file;
        return file;
    }
    
    public  File getDirectory(String path, String defaultPath) {
        Log.i("getDirectory","path : "+path);
        return path == null ? new File(defaultPath) : new File(path);
    }

    private boolean checkSDCardAvaliable() {

        getExternalStorageDirectory();
        return (getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED));      
    }
    
	private boolean isSDCardFull() {

        getExternalStorageDirectory();
        String state = getExternalStorageState(); 

               if(Environment.MEDIA_MOUNTED.equals(state)) { 
                   File sdcardDir = getExternalStorageDirectory(); 
                   StatFs sf = new StatFs(sdcardDir.getPath());
                   long availCount = sf.getAvailableBlocks(); 
                   if(availCount>0){
                       return false;
                   } else {
                       return true;
                   }
               } 

        return true;
    }
	
	private boolean checkForSim(int slot) {
		return simStateReady(slot) && isPhoneBookReady(slot);
	}
	
	private boolean isPhoneBookReady(int slot) {
        Log.i(TAG, "isPhoneBookReady " + SimCardUtils.isPhoneBookReady(slot));
        return SimCardUtils.isPhoneBookReady(slot);
    }
	
	public static void importFromVcard(Context mContext) {
		mPhoneAccount = new AccountWithDataSetEx(LocalPhoneAccountType.ACCOUNT_NAME_LOCAL_PHONE, LocalPhoneAccountType.ACCOUNT_TYPE, -1);
    	AccountSelectionUtil.doImportFromSdCard(mContext, mPhoneAccount);
	}

	@Override
	public boolean onPreferenceClick(AuroraPreference mPreference) {
		String key = mPreference.getKey();
		if (key != null) {
			if (is_importexporting_sim || VCardService.mIsstart) {
				Toast.makeText(mContext, R.string.aurora_daochu_daoru_ing, Toast.LENGTH_SHORT).show();
				return false;
			}
			
			if (key.equals("pref_key_import_sim")) {
				sourceAccountWithDataSetEx = mSimAccount;
				targetAccountWithDataSetEx = mPhoneAccount;
				
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					if (simStateReady(0) && simStateReady(1)) {
						try {
							removeMenuById(0);
							removeMenuById(1);
						} catch (Exception e) {
							e.printStackTrace();
						}

						addMenu(0, "SIM2", new OnMenuItemClickLisener() {
							public void onItemClick(View menu) {
								mImportExportSlot = 1;
								importtoSimClick(mImportExportSlot);
							}
						});
						addMenu(1, "SIM1", new OnMenuItemClickLisener() {
							public void onItemClick(View menu) {
								mImportExportSlot = 0;
								importtoSimClick(mImportExportSlot);
							}
						});
						showCustomMenu();
					} else if (simStateReady(1)) {
						mImportExportSlot = 1;
						importtoSimClick(1);
					} else {
						mImportExportSlot = 0;
						importtoSimClick(0);
					}
				} else {
					mImportExportSlot=0;
					importtoSimClick(0);
				}
                               
                //AuroraPeopleActivity.mIsImportSimContact = true;
                //finish();
				return true;
			} else if (key.equals("pref_key_import_sd")) {
	        	sourceAccountWithDataSetEx = null;
				targetAccountWithDataSetEx = mPhoneAccount;
	        	if (checkSDCardAvaliable()) {
	        	    new AuroraAlertDialog.Builder(this)
	                .setMessage(R.string.aurora_ready_searching_vcard)
	                .setTitle(R.string.aurora_import_sd_title)
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int which) {
	                		AccountSelectionUtil.doImportFromSdCard(AuroraContactImportExportActivity.this, targetAccountWithDataSetEx);
	                    }
	                 }).show();
	        		//finish();//aurora add zhouxiaobing 20131203
	        		//AccountSelectionUtil.doImportFromSdCard(this, targetAccountWithDataSetEx);
	        	} else {
	        	
	                
	                new AuroraAlertDialog.Builder(this)
	                .setMessage(R.string.no_sdcard_message)
	                .setTitle(R.string.no_sdcard_title)
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int which) {
	                		finish();
	                    }
	                 }).show();
	                
	                return false;
	        	}
	        	
				return true;
			} else if (key.equals("pref_key_import_sd2")) {
	        	sourceAccountWithDataSetEx = null;
				targetAccountWithDataSetEx = mPhoneAccount;
				
	        	if (checkExternalStorageAvaliable()) {
	        		//finish();//aurora add zhouxiaobing 20131203
	        		AccountSelectionUtil.doImportFromSdCard2(this, targetAccountWithDataSetEx);
	        	} else {
	        
	        		
	                new AuroraAlertDialog.Builder(this)
	                .setMessage(R.string.aurora_no_sd2_insert)
	                .setTitle(R.string.no_sdcard_title)
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int which) {
	                		//finish();
	                    }
	                 }).show();
	                
	                return false;
	        	}				
				return true;
			} else if (key.equals("pref_key_export_sim")) {
				//phone to sim
			    boolean simReady = simStateReady();
                if (!simReady) {
                	Toast.makeText(AuroraContactImportExportActivity.this, R.string.aurora_sim_not_ready_tip, Toast.LENGTH_SHORT).show();
                    return true;
                }
                
				//aurora change zhouxiaobing 20140707 for simcontacts
				if(AbstractStartSIMService.isServiceRunning()) {
					Toast.makeText(AuroraContactImportExportActivity.this, R.string.aurora_sim_not_ready, Toast.LENGTH_SHORT).show();
					return true;
				}
				//aurora change zhouxiaobing 20140707 for simcontacts
				
	        	sourceAccountWithDataSetEx = mPhoneAccount;
				targetAccountWithDataSetEx = mSimAccount;
				if (targetAccountWithDataSetEx != null && sourceAccountWithDataSetEx != null) {
					if (FeatureOption.MTK_GEMINI_SUPPORT) {
						if (simStateReady(0) && simStateReady(1)) {
							try {
								removeMenuById(0);
								removeMenuById(1);
							} catch (Exception e) {
								e.printStackTrace();
							}

							addMenu(0, "SIM2", new OnMenuItemClickLisener() {
								public void onItemClick(View menu) {
									mImportExportSlot = 1;
									targetAccountWithDataSetEx = mSim2Account;
									exporttoSimClick();
								}
							});
							addMenu(1, "SIM1", new OnMenuItemClickLisener() {
								public void onItemClick(View menu) {
									mImportExportSlot = 0;
									targetAccountWithDataSetEx = mSim1Account;
									exporttoSimClick();
								}
							});
							showCustomMenu();
						} else if (simStateReady(1)) {
							mImportExportSlot = 1;
							targetAccountWithDataSetEx = mSim2Account;
							exporttoSimClick();
						} else {
							mImportExportSlot = 0;
							targetAccountWithDataSetEx = mSim1Account;
							exporttoSimClick();
						}
					} else {
						mImportExportSlot = 0;
						exporttoSimClick();
					}
				} else {
	        		Toast.makeText(AuroraContactImportExportActivity.this, 
	        		        R.string.aurora_export_to_sim_error, Toast.LENGTH_SHORT).show();	
	        	}
				
				return true;
			} else if (key.equals("pref_key_export_sd")) {
	        	sourceAccountWithDataSetEx = mPhoneAccount;
				targetAccountWithDataSetEx = null;
	
	        	if (checkSDCardAvaliable()) {
	        		
	        		if(isSDCardFull()){ //SD card is full
	                    Log.i(TAG,"[handleImportExportAction] isSDCardFull");
	                    new AuroraAlertDialog.Builder(this)
	                    .setMessage(R.string.storage_full)
	                    .setTitle(R.string.storage_full)
	                    .setIcon(R.drawable.ic_dialog_alert_holo_light)
	                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                            //finish();
	                        }
	                    })
	                    .show();
	                    return false;
	                }
	        		//check sdcard
	            	if (null != mPhoneAccount) {
	            		//exportToSdCardFrom(mPhoneAccount);//aurora change zhouxiaobing 20131203
	            		Intent exportIntent = new Intent(this, ExportVCardActivity.class);
                        exportIntent.putExtra("exportselection", "");
                        startActivityForResult(exportIntent, AuroraContactImportExportActivity.REQUEST_CODE);
	            	} else {
	            		Log.e(TAG,"null == mPhoneAccount");
	            	}
	        	} else {
	        	
	                //Gionee <xuhz> <2013-07-20> add for CR00824492 end
	                new AuroraAlertDialog.Builder(this)
	                .setMessage(R.string.no_sdcard_message)
	                .setTitle(R.string.no_sdcard_title)
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int which) {
	                		//finish();
	                    }
	                 }).show();
	                return false;
	        	}
	        	

				return true;
			}else if (key.equals("pref_key_export_sd2")) {
	        	sourceAccountWithDataSetEx = mPhoneAccount;
				targetAccountWithDataSetEx = null;
	
	
	        	if (checkExternalStorageAvaliable()) {
	        		
	        		if(isExternalStorageFull()){ //SD card is full
	                    Log.i(TAG,"[handleImportExportAction] isSDCardFull");
	                    new AuroraAlertDialog.Builder(this)
	                    .setMessage(R.string.storage_full)
	                    .setTitle(R.string.storage_full)
	                    .setIcon(R.drawable.ic_dialog_alert_holo_light)
	                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                            //finish();
	                        }
	                    })
	                    .show();
	                    return false;
	                }
	        		//check sdcard
	            	if (null != mPhoneAccount) {
	            		//exportToSdCardFrom(mPhoneAccount);//aurora change zhouxiaobing 20131203
	            		Intent exportIntent = new Intent(this, ExportVCardActivity.class);
                        exportIntent.putExtra("exportselection", "");
                        exportIntent.putExtra("isSdcard2", true);
                        startActivityForResult(exportIntent, AuroraContactImportExportActivity.REQUEST_CODE);
	            	} else {
	            		Log.e(TAG,"null == mPhoneAccount");
	            	}
	        	} else {
	        
	                //Gionee <xuhz> <2013-07-20> add for CR00824492 end
	                new AuroraAlertDialog.Builder(this)
	                .setMessage(R.string.no_sdcard_message)
	                .setTitle(R.string.no_sdcard_title)
	                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int which) {
	                		//finish();
	                    }
	                 }).show();
	                return false;
	        	}
	        	
				
				return true;
			}
			
		}
		return false;
	}
	
	public void importtoSimClick(int slotid) {
		boolean simReady = simStateReady(slotid);
		if (!simReady) {
			Toast.makeText(AuroraContactImportExportActivity.this,
					R.string.aurora_sim_not_ready_tip, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		
		if (GNContactsUtils.isContactsSimProcess()) {
			Toast.makeText(AuroraContactImportExportActivity.this,
					R.string.aurora_sim_not_ready, Toast.LENGTH_SHORT).show();
			return;
		}
		
		showSimContacts(slotid);
	}
	
	public void exporttoSimClick() {
		if (export_sim_dialog == null) {
			export_sim_dialog = new AuroraAlertDialog.Builder(this)
					.setMessage(R.string.aurora_export_to_sim_meg)
					.setTitle(R.string.aurora_menu_export)
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// finish();
									export_sim_dialog = null;
								}
							})
					.setPositiveButton(R.string.aurora_export_title,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									export_sim_dialog = null;
									exporttoSim();
									// finish();
									finishToFirstActivity();// aurora change zhouxiaobing 20121220
								}
							}).create();
			
			export_sim_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							// TODO Auto-generated method stub
							export_sim_dialog = null;
						}
					});
			export_sim_dialog.show();
		}

	}
	
	private void finishToFirstActivity() {
		finishAffinity();
		Intent intent=new Intent();
		intent.setClass(this, AuroraPeopleActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
//		setResult(Activity.RESULT_OK);
//		finish();
	}
	
    private boolean checkInternalStorageAvaliable() {
    	String s = AuroraStorageManager.getInstance(mContext).getInternalStoragePath();
    	if (s == null) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    private boolean checkExternalStorageAvaliable() {
    	String s = AuroraStorageManager.getInstance(mContext).getExternalStoragePath();
    	if (s == null) {
    		return false;
    	} else {
    		return true;
    	}    
    }
    
	private boolean isInternalStorageFull() {
         String s = AuroraStorageManager.getInstance(mContext).getInternalStoragePath();
         if (s != null) {
             StatFs sf = new StatFs(s);
             long availCount = sf.getAvailableBlocks(); 
             if (availCount > 0) {
                 return false;
             } else {
                 return true;
             }        	 
         }
        return true;
    }
	
	private boolean isExternalStorageFull() {
        String s = AuroraStorageManager.getInstance(mContext).getExternalStoragePath();
        if (s != null) {
            StatFs sf = new StatFs(s);
            long availCount = sf.getAvailableBlocks(); 
            if (availCount > 0) {
                return false;
            } else {
                return true;
            }        	 
        }
       return true;
    }  	
    //aurora add zhouxiaobing 20131211 end	
	
	public boolean simStateReady() {

		boolean simReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
				.from(mContext).getSimState());
		return simReady;

	}
	
	public boolean simStateReady(int slotIdx) {
		return SimCardUtils.isSimStateReady(slotIdx);
	}
	
	
	
}
