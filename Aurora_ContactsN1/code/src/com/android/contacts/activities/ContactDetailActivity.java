/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.contacts.activities;

import com.android.contacts.ContactLoader;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.AuroraContactDetailFragment;
import com.android.contacts.detail.AuroraContactDetailLayoutController;
import com.android.contacts.detail.ContactLoaderFragment;
import com.android.contacts.detail.ContactLoaderFragment.ContactLoaderFragmentListener;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.PhoneCapabilityTester;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;

import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.SubContactsUtils;

import android.os.SystemProperties;

import java.util.List;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
// Aurora xuyong 2015-07-13 added for bug #14161 start
import com.mediatek.contacts.simcontact.SimCardUtils;
// Aurora xuyong 2015-07-13 added for bug #14161 end 
import com.privacymanage.data.AidlAccountData;
import com.privacymanage.service.AuroraPrivacyUtils;
import com.android.contacts.ContactsUtils;
import com.android.contacts.calllog.PhoneNumberHelper;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Entity.NamedContentValues;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.android.contacts.util.Constants;
import com.android.contacts.util.IntentFactory;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import aurora.app.AuroraAlertDialog;
import aurora.provider.AuroraSettings;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;

import android.graphics.Bitmap;


public class ContactDetailActivity extends ContactsActivity {
    private static final String TAG = "ContactDetailActivity";

    /**
     * Boolean intent key that specifies whether pressing the "up" affordance in this activity
     * should cause it to finish itself or launch an intent to bring the user back to a specific
     * parent activity - the {@link PeopleActivity}.
     */
    public static final String INTENT_KEY_FINISH_ACTIVITY_ON_UP_SELECTED =
            "finishActivityOnUpSelected";

    private ContactLoader.Result mContactData;
    private Uri mLookupUri;
    // edit by chenlong 81249
    private Uri simOrPhoneUri;
    private boolean mFinishActivityOnUpSelected;

    private AuroraContactDetailLayoutController mContactDetailLayoutController;
    private ContactLoaderFragment mLoaderFragment;

    private Handler mHandler = new Handler();
    public StatusBarManager mStatusBarMgr;
    
    private static final int AURORA_CONTACT_DETAIL_MORE = 1;
    
    private boolean mIsPrivacyContact = false;
    private long mCurrentPrivacyAccountId = 0;
    
    @Override
    public void onCreate(Bundle savedState) {
        
        super.onCreate(savedState);
        
        Intent i = new Intent("com.android.action.LAUNCH_CONTACTS_LIST");
        sendBroadcast(i);
        
        mContext = ContactDetailActivity.this;
        
        Bundle extras = getIntent().getExtras();
        if (ContactsApplication.sIsAuroraPrivacySupport && null != extras) {
        	mIsPrivacyContact = extras.getBoolean("is_privacy_contact");
        	mCurrentPrivacyAccountId = AuroraPrivacyUtils.mCurrentAccountId;
        }


        mFinishActivityOnUpSelected = getIntent().getBooleanExtra(
                INTENT_KEY_FINISH_ACTIVITY_ON_UP_SELECTED, false);
		// aurora <ukiliu> <2013-9-25> add for auroro ui begin
        setAuroraContentView(R.layout.contact_detail_activity,
                AuroraActionBar.Type.Normal); 
        AuroraActionBar actionBar = getAuroraActionBar();
        
        addAuroraActionBarItem(AuroraActionBarItem.Type.More, AURORA_CONTACT_DETAIL_MORE);
        actionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        setAuroraMenuCallBack(auroraMenuCallBack);
        if (mIsPrivacyContact) {
        	setAuroraMenuItems(R.menu.aurora_privacy_contact_detail);
        } else {
        	setAuroraMenuItems(R.menu.aurora_contact_detail);
        }
        
        actionBar.setTitle(R.string.viewContactTitle);
	    // aurora <ukiliu> <2013-9-25> add for auroro ui end

        mContactDetailLayoutController = new AuroraContactDetailLayoutController(this, savedState,
                getFragmentManager(), findViewById(R.id.contact_detail_container),
                mContactDetailFragmentListener);

        // We want the UP affordance but no app icon.
        // Setting HOME_AS_UP, SHOW_TITLE and clearing SHOW_HOME does the trick.
        // edit by chenlong 81249
        simOrPhoneUri = getIntent().getData();
        //Gionee <huangzy> <2013-05-07> modify for CR00809120 start
        /*Log.i(TAG, getIntent().getData().toString());*/
        if (null != getIntent().getData()) {
        	Log.i(TAG, getIntent().getData().toString());
        }
        
        if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
            ContactsApplication.mPrivacyActivityList.add(this);
        }
    }
    
	// aurora <ukiliu> <2013-9-25> add for auroro ui begin
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.menu_contact_edit: {
            	if (mLoaderFragmentListener != null) mLoaderFragmentListener.onEditRequested(mLookupUri);
                break; 
            }
            
            case R.id.menu_contact_delete: {
            	if (mLoaderFragmentListener != null) mLoaderFragmentListener.onDeleteRequested(mLookupUri);           	
            	break;
            }
            
            case R.id.menu_contact_share: {
                if (mContactData == null) break;
                final String lookupKey = mContactData.getLookupKey();
                Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                // aurora ukiliu 2013-10-10 modify for aurora-design begin
                intent.setClass(ContactDetailActivity.this, com.android.contacts.ShareContactViaSMS.class);
                intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra("contactId", String.valueOf(mContactData.getContactId()));
                intent.putExtra("LOOKUPURIS", String.valueOf(mContactData.getLookupUri()));
                // aurora ukiliu 2013-10-10 modify for aurora-design end
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                }
                break;
            }
            
            case R.id.menu_remove_black: {
            	removeFromBlack(false);
            	break;
            }
            
            case R.id.menu_remove_all_black: {
            	removeFromBlack(true);
            	break;
            }
            
//            case R.id.menu_contact_qrcode: {
//            	showQrcodeImage();
//            	break;
//            }

            default:
                break;
            }
        }
    };
    
    private void removeFromBlack(boolean flag) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
		final AuroraCheckBox checkBox = (AuroraCheckBox)view.findViewById(R.id.check_box);
		checkBox.setChecked(true);
		
		TextView message = (TextView) view.findViewById(R.id.textView1);
		if (flag) {
			message.setText(getResources().getString(R.string.confirm_remove_all));
		}

		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(mContext)
				.setTitle(getResources().getString(R.string.black_remove))
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								boolean recoveryLogs = checkBox.isChecked();
								int isblack = 0;
								if (!recoveryLogs) {
									isblack = -1;
								}
								
								ContentValues values = new ContentValues();
								Log.d(TAG, "mThisRejectedList = " + mThisRejectedList);
								if (mThisRejectedList.size() < 1) {
									getBlackList();
								}
								
								for (String number : mThisRejectedList) {
									values.put("isblack", isblack);
									values.put("number", number);
									values.put("reject", 0);
									try {
										int count = mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), 
						            			values, GNContactsUtils.getPhoneNumberEqualString(number), null);
										if (count == 0) {
											count = mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), 
							            			values, "number='" + number + "'", null);
										}
										Log.d(TAG, "number = " + number + "   isblack = " + isblack + "  delete = " + count);
									} catch (Exception e) {
										e.printStackTrace();
									}
									values.clear();
								}
								
								mRejectedCount = 0;
								mRejectedList.clear();
					        	mThisRejectedList.clear();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).show();
	}
    
    // aurora ukiliu 2013-11-16 add for BUG #626 begin 
    private boolean doDeleteContact(long rawContactId) {
    	
    	int resultcode = getContentResolver().delete(
                RawContacts.CONTENT_URI, 
                RawContacts._ID + " = " + rawContactId + " and deleted = 0", 
                null);
    	if (resultcode > 0) {
    		return true;
    	}
    	return false;
    }
    // aurora ukiliu 2013-11-16 add for BUG #626 end
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case AURORA_CONTACT_DETAIL_MORE:
                showAuroraMenu();
                break;
            default:
                break;
            }
        }
    };
	// aurora <ukiliu> <2013-9-25> add for auroro ui end
    
    public static ArrayList<String> mRejectedList = new ArrayList<String>();
    public static ArrayList<String> mThisRejectedList = new ArrayList<String>();
    public static int mRejectedCount = 0;
    private static Context mContext;
    private void loadPhoneRejected() {
    	final long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), Long.parseLong(simOrPhoneUri.getLastPathSegment()));
        new Thread(){
            @Override
            public void run() {
            	getBlackList();
            }
        }.start();
    }
    
    private void getBlackList() {
    	Cursor c = mContext.getContentResolver().query(Uri.parse("content://com.android.contacts/black"),
                new String[]{"number"}, "isblack>0", null, null);
        
        if (null != c && c.moveToFirst()) {
            try {
            	mRejectedList.clear();
            	do {
            		String number = c.getString(0);
            		if (number == null) {
            			continue;
            		}
            		
            		String numberE164 = PhoneNumberUtils.formatNumberToE164(
			                number, ContactsUtils.getCurrentCountryIso(mContext));
            		
            		mRejectedList.add(number);
            		if (numberE164 != null && !number.equals(numberE164) && !mRejectedList.contains(numberE164)) {
            			mRejectedList.add(numberE164);
					}
				} while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
//            } finally {
//                c.close();
            }   
        }
        if(c != null) {
        	c.close();
        }
    }
    

    @Override
    public void onAttachFragment(Fragment fragment) {
         if (fragment instanceof ContactLoaderFragment) {
            mLoaderFragment = (ContactLoaderFragment) fragment;
            mLoaderFragment.setListener(mLoaderFragmentListener);
            mLoaderFragment.loadUri(getIntent().getData());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // First check if the {@link ContactLoaderFragment} can handle the key
        if (mLoaderFragment != null && mLoaderFragment.handleKeyDown(keyCode)) return true;

        // Otherwise find the correct fragment to handle the event
        FragmentKeyListener mCurrentFragment = mContactDetailLayoutController.getCurrentPage();
        if (mCurrentFragment != null && mCurrentFragment.handleKeyDown(keyCode)) return true;

        // In the last case, give the key event to the superclass.
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mContactDetailLayoutController != null) {
            mContactDetailLayoutController.onSaveInstanceState(outState);
        }
    }

    private final ContactLoaderFragmentListener mLoaderFragmentListener =
            new ContactLoaderFragmentListener() {
        @Override
        public void onContactNotFound() {
            finish();
        }

        @Override
        public void onDetailsLoaded(final ContactLoader.Result result) {
            if (result == null) {
                return;
            }
            // Since {@link FragmentTransaction}s cannot be done in the onLoadFinished() of the
            // {@link LoaderCallbacks}, then post this {@link Runnable} to the {@link Handler}
            // on the main thread to execute later.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // If the activity is destroyed (or will be destroyed soon), don't update the UI
                    if (isFinishing()) {
                        return;
                    }
                    mContactData = result;
                    mLookupUri = result.getLookupUri();
                    invalidateOptionsMenu();
                    mContactDetailLayoutController.setContactData(mContactData);
                }
            });
        }

        @Override
        public void onEditRequested(Uri contactLookupUri) {
            if (mContactData == null) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_EDIT, contactLookupUri); 
            //Gionee:huangzy 20130401 modify for CR00792013 start
            intent.addCategory(IntentFactory.GN_CATEGORY);
            //Gionee:huangzy 20130401 modify for CR00792013 end
            intent.putExtra(
                    ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
            // aurora <wangth> <2014-1-3> add for aurora begin
            intent.putExtra("mSimId", (long)mContactData.getIndicate());
            intent.putExtra("is_privacy_contact", mIsPrivacyContact);
            // aurora <wangth> <2014-1-3> add for aurora end
            // Don't finish the detail activity after launching the editor because when the
            // editor is done, we will still want to show the updated contact details using
            // this activity.
            startActivity(intent);
        }

        @Override
        public void onDeleteRequested(Uri contactUri) {
        	if (null == mContactData || null == contactUri) {
        		return;
        	}
        	
            if (mContactData.getIndicate() < 0) {
            	// aurora ukiliu 2013-11-16 modify for BUG #626 begin 
//            	ContactDeletionInteraction.start(ContactDetailActivity.this, contactUri, true);
            	final CharSequence name = ContactDetailDisplayUtils.getDisplayName(ContactsApplication.getInstance(), mContactData);
            	String message = ContactsApplication.getInstance().getResources().getString(R.string.aurora_delete_one_contact_message, name);
            	String title = ContactsApplication.getInstance().getResources().getString(R.string.delete);
            	final long rawContactId = mContactData.getNameRawContactId();
            	final long contactId = mContactData.getContactId();
            	
            	View dialogView = null;
            	AuroraCheckBox black_remove = null;
            	if (mIsPrivacyContact) {
            		title = ContactsApplication.getInstance().getResources().getString(R.string.gn_remove);
            		
            		dialogView = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
            		TextView messageView = (TextView)dialogView.findViewById(R.id.textView1);
            		messageView.setText(mContext.getString(R.string.aurora_remove_one_privacy_contact_message, name));
            		black_remove = (AuroraCheckBox)dialogView.findViewById(R.id.check_box);
            		black_remove.setText(mContext.getString(R.string.aurora_remove_privacy_contact_check_box_text));
            		black_remove.setChecked(false);
            	}
            	
            	final AuroraCheckBox checked = black_remove;
            	
            	AuroraAlertDialog mDialog = new AuroraAlertDialog.Builder(ContactDetailActivity.this,
						AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
						.setTitle(title)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(
											DialogInterface dialog,
											int whichButton) {
										if (mIsPrivacyContact) {
				            				if (checked.isChecked()) {
				                    			mContext.getContentResolver().delete(
				                                        RawContacts.CONTENT_URI, 
				                                        RawContacts._ID + "=?" + " and deleted=0 and is_privacy=" + mCurrentPrivacyAccountId, 
				                                        new String[] {String.valueOf(rawContactId)});
				            				} else {
				            					ContentValues values = new ContentValues();
				                        		values.put("is_privacy", 0);
				                        		
				                    			mContext.getContentResolver().update(RawContacts.CONTENT_URI, values, 
				                                		RawContacts.CONTACT_ID + "=" + contactId, null);
				                                mContext.getContentResolver().update(Data.CONTENT_URI, values, 
				                                        Data.CONTACT_ID + "=" + contactId, null);
				                                values.clear();
				            				}
				            				
				            				finish();
				            				
				            				return;
				            			}
										
										CharSequence toastChar = ContactsApplication.getInstance().
					                            getResources().getString(R.string.aurora_delete_one_contact_toast, 
					                            		name);
										
					            		if (doDeleteContact(rawContactId)) {
					            			toastChar = ContactsApplication.getInstance().
						                            getResources().getString(R.string.aurora_delete_one_contact_toast, 
						                            		name);
					            			Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
					                                toastChar, Toast.LENGTH_SHORT).show();
                                    ContactsApplication.getInstance().sendSyncBroad();
					            			finish();
					            		} else {
					            			toastChar = ContactsApplication.getInstance().
						                            getResources().getString(R.string.notifier_fail_delete_title);
					            			Toast.makeText(ContactsApplication.getInstance().getApplicationContext(),
					                                toastChar, Toast.LENGTH_SHORT).show();
					            		}					            		
									}
								}).create();
            	
            	if (mIsPrivacyContact) {
            		mDialog.setView(dialogView);
            	} else {
            		mDialog.setMessage(message);
            	}
            	
            	mDialog.show();
            	// aurora ukiliu 2013-11-16 modify for BUG #626 end
            } else {
				if (GNContactsUtils.isContactsSimProcess()) {//aurora change zhouxiaobing 20140707 for simcontacts
					Toast.makeText(
							mContext,
							R.string.aurora_sim_not_ready,
							Toast.LENGTH_SHORT).show();
					return;
				}
				
                int simIndex = mContactData.getSimIndex();
                // Aurora xuyong 2015-07-13 modified for bug #14161 start
                Uri simUri = 
                		ContactsUtils.getSimContactsUri((int)mContactData.getIndicate(),
                        		SimCardUtils.isSimUsimType(mContactData.getIndicate()));                
                // Aurora xuyong 2015-07-13 modified for bug #14161 end
                // qc begin
                if (GNContactsUtils.isOnlyQcContactsSupport() && GNContactsUtils.isMultiSimEnabled() && contactUri != null) {
                    int slotId = GNContactsUtils.querySlotIdByLookupUri(getApplicationContext(), contactUri);
                    simUri = GNContactsUtils.getQcSimUri(slotId);
                }
                // qc end
                Log.v(TAG, "name="+mContactData.getDisplayName()+"PhoneticName="+mContactData.getPhoneticName());
                String name = mContactData.getDisplayName();
                //if(name.equalsIgnoreCase(getPhoneNumber()))
                //	name="";
                //String s="鳚";
            	//name=name.replace(' ', s.toCharArray()[0]);//aurora add zhouxiaobing 20140305 for delete space
            	// Aurora xuyong 2015-07-13 modified for bug #14161 start
                // Aurora xuyong 2015-07-13 modified for bug #14161 start
                simUri = ContactsUtils.getSimContactsUri((int)mContactData.getIndicate(),
                		SimCardUtils.isSimUsimType(mContactData.getIndicate()));
               
                // Aurora xuyong 2015-07-13 modified for bug #14161 end
                // Aurora xuyong 2015-07-13 modified for bug #14161 end
//                ContactDeletionInteraction.start(ContactDetailActivity.this, contactUri, true, simUri, ("index = " + simIndex));   
                ContactsLog.log("detail contact delete index = " + simIndex);
                ContactDeletionInteraction.start(ContactDetailActivity.this, contactUri, true, simUri, ("index = " + simIndex), name);   
            }
        }
    };  


    private final AuroraContactDetailFragment.Listener mContactDetailFragmentListener =
            new AuroraContactDetailFragment.Listener() {
        @Override
        public void onItemClicked(Intent intent) {
            if (intent == null) {
                return;
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No activity found for intent: " + intent);
            }
        }

        @Override
        public void onCreateRawContactRequested(
                ArrayList<ContentValues> values, AccountWithDataSet account) {
            Toast.makeText(ContactDetailActivity.this, R.string.toast_making_personal_copy,
                    Toast.LENGTH_LONG).show();
            Intent serviceIntent = ContactSaveService.createNewRawContactIntent(
                    ContactDetailActivity.this, values, account,
                    ContactDetailActivity.class, Intent.ACTION_VIEW);
            startService(serviceIntent);

        }
    };

    /**
     * This interface should be implemented by {@link Fragment}s within this
     * activity so that the activity can determine whether the currently
     * displayed view is handling the key event or not.
     */
    public interface FragmentKeyListener {
        /**
         * Returns true if the key down event will be handled by the implementing class, or false
         * otherwise.
         */
        public boolean handleKeyDown(int keyCode);
    }

    
    /*
     * Bug Fix by Mediatek Begin.
     *   CR ID: ALPS00116397
     */
    public boolean isMe() {
    	AuroraContactDetailFragment detailFragment =  mContactDetailLayoutController.getDetailFragment();
        if (detailFragment != null) {
            return detailFragment.isMe();
        }
        return false;
    }
    /*
     * New Feature  by Mediatek End.
    */
       
	
    
    //Gionee:huangzy 20121102 add for CR00692598 start
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	   if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivacyContact) {
               ContactsApplication.mPrivacyActivityList.remove(this);
           }
    }
    //Gionee:huangzy 20121102 add for CR00692598 end
    
//    private void showQrcodeImage() {
//    	String qrcode = getQrcodeString();
//    	Log.d(TAG, "showQrcodeImage = " + qrcode);
//    	  Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
//          hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//          //图像数据转换，使用了矩阵转换
//          try {
//	          BitMatrix bitMatrix = new QRCodeWriter().encode(qrcode, BarcodeFormat.QR_CODE, 400,400, hints);
//	          int[] pixels = new int[400 * 400];
//	          //下面这里按照二维码的算法，逐个生成二维码的图片，
//	          //两个for循环是图片横列扫描的结果
//	          for (int y = 0; y < 400; y++)
//	          {
//	              for (int x = 0; x < 400; x++)
//	              {
//	                  if (bitMatrix.get(x, y))
//	                  {
//	                      pixels[y * 400 + x] = 0xff000000;
//	                  }
//	                  else
//	                  {
//	                      pixels[y * 400 + x] = 0xffffffff;
//	                  }
//	              }
//	          }
//	          //生成二维码图片的格式，使用ARGB_8888
//	          Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
//	          bitmap.setPixels(pixels, 0, 400, 0, 0, 400, 400);
//	          
//	          
////	          Toast toast = Toast.makeText(mContext, "带图片的Toast", Toast.LENGTH_LONG); 
////	          toast.setGravity(Gravity.CENTER, 0, 0);    
////	          LinearLayout toastView = (LinearLayout) toast.getView();    
////	          ImageView imageCodeProject = new ImageView(mContext);  
////	          imageCodeProject.setImageBitmap(bitmap);   
////	          toastView.addView(imageCodeProject, 0);    	          
////	          toast.show();
//	          
//	          if(myDialog != null) {
//	        	  myDialog.dismiss();
//	        	  myDialog = null;
//	          }
//	          myDialog = new AuroraAlertDialog.Builder(this).create();  
//              myDialog.show();  
//              myDialog.getWindow().setContentView(R.layout.aurora_qrcode_dialog);  
//              myDialog.setContentView(R.layout.aurora_qrcode_dialog);
//              ImageView v = (ImageView)myDialog.getWindow().findViewById(R.id.qrcode_image); 
//              v.setImageBitmap(bitmap);
//              myDialog.show();
//	          
//          } catch(Exception e) {
//        	  e.printStackTrace();
//          }
//          
//    }
//    
//    private AuroraAlertDialog myDialog = null; 
//    
//    private String getQrcodeString() {
////    	 VCardComposer composer = new VCardComposer(this, VCardConfig.getVCardTypeFromString(getResources().getString(R.string.config_export_vcard_type)), true);
//    	VCardComposer composer = new VCardComposer(this, VCardConfig.VCARD_TYPE_V30_GENERIC, true);    	
//    	    final Uri contentUriForRawContactsEntity = RawContactsEntity.CONTENT_URI.buildUpon()
//                    .appendQueryParameter(RawContactsEntity.FOR_EXPORT_ONLY, "1")
//                    .build();
//    	   if (!composer.init(simOrPhoneUri, null,
//                   null, null, null, contentUriForRawContactsEntity)) {
//    		   final String errorReason = composer.getErrorReason();
//    		   final String translatedErrorReason =
//                       translateComposerError(errorReason);
//    		   return "";
//    	   }
//    	   
//    	   if (!composer.isAfterLast()) {
//    		   return composer.createOneEntry();
//    	   }
//    	   
//    	   return "";
//    
//    }
//    
//    private String translateComposerError(String errorMessage) {
//        final Resources resources = getResources();
//        if (VCardComposer.FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO.equals(errorMessage)) {
//            return resources.getString(R.string.composer_failed_to_get_database_infomation);
//        } else if (VCardComposer.FAILURE_REASON_NO_ENTRY.equals(errorMessage)) {
//            return resources.getString(R.string.composer_has_no_exportable_contact);
//        } else if (VCardComposer.FAILURE_REASON_NOT_INITIALIZED.equals(errorMessage)) {
//            return resources.getString(R.string.composer_not_initialized);
//        } else {
//            return errorMessage;
//        }
//    }
}
