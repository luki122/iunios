package com.android.contacts.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.EntityIterator;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import aurora.widget.AuroraListView;
import android.widget.AbsListView.OnScrollListener;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactSaveService.Listener;
import com.android.contacts.R;
import com.android.contacts.model.AccountWithDataSet;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.MergeContacts;
import com.android.contacts.util.MergeContacts.LetStopException;
import com.android.contacts.util.MergeContacts.RawContactItem;
import com.android.contacts.util.MergeContacts.SameContactItems;
import com.android.contacts.widget.CombineItemView;
import com.android.contacts.widget.ContactSimpleInfoView;
// gionee xuhz 20120830 add for CR00678320 start
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.model.ExchangeAccountType;
import com.android.contacts.model.GoogleAccountType;
import com.mediatek.contacts.model.LocalPhoneAccountType;
// gionee xuhz 20120830 add for CR00678320 end
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;


public class ManualCombineQueryActivity extends AuroraActivity implements Listener {    
	private SameContactItems mAllSameNameContacts;
	private CombineItemsAdapter mCombineItemsAdapter;
	private ContentResolver mContentResolver;
	private AuroraListView mListView;
	private MergeContacts mMergeContact = new MergeContacts();
	private ArrayList<RawContactItem> mContacts4Combine;
	private Uri mTmpContactCombined;
	private boolean mTmpContactConfirmed = false;
	// gionee xuhz 20120830 add for CR00678320 start
	private Uri mPhotoUri;
	// gionee xuhz 20120830 add for CR00678320 end
	
	//Gionee <xuhz> <2013-08-15> add for CR00857585 begin
	private ContactPhotoManager mContactPhotoManager;
	//Gionee <xuhz> <2013-08-15> add for CR00857585 end
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Gionee <xuhz> <2013-08-15> add for CR00857585 begin
        if (mContactPhotoManager == null) {
    		mContactPhotoManager = ContactPhotoManager.getInstance(this);
        }
		//Gionee <xuhz> <2013-08-15> add for CR00857585 end
        
		mContentResolver = getContentResolver();
		// aurora <wangth> <2013-9-27> modify for aurora ui begin
        /*
		initView();
		*/
		setAuroraContentView(R.layout.manual_combine,
                AuroraActionBar.Type.Normal);
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.setTitle(R.string.gn_menu_merge_contacts);
		
		mListView = (AuroraListView) findViewById(android.R.id.list);
		// aurora <wangth> <2013-9-27> modify for aurora ui end
		showDuplicate(this);
	}
	
	protected void initView() {
		setContentView(R.layout.manual_combine);
		
		mListView = (AuroraListView) findViewById(android.R.id.list);
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		actionbar.setTitle(R.string.gn_combine_contacts_title);
		
	}
	
	protected void showDuplicate(final Context context)
    {
    	new AsyncTask<Integer, Integer, Integer>()
    	{
    		private final int MAX_INJECT_SIZE = 80;
    		
			@Override
			protected Integer doInBackground(Integer... params)
			{
				try
				{
					mAllSameNameContacts = mMergeContact.querySameRawContact(context);
				} catch (LetStopException e) {
					e.printStackTrace();
				} finally {
					if (mMergeContact.isLetStop() || null == mAllSameNameContacts ||
							mAllSameNameContacts.size() <= 0) {
					    publishProgress(0);
						return null;
					}
				}
				
				for (ArrayList<RawContactItem> sameName : mAllSameNameContacts) {
					if (null != sameName) {
						for (RawContactItem rci : sameName) {
							if (null != rci) {
								rci.mRawCom = null;
							}
						}
					}
				}
				
				mCombineItemsAdapter = new CombineItemsAdapter(context, mAllSameNameContacts);
                publishProgress(1);

				//~~~~~~~~~~~~~~~~~~~~~inject number start~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				final int sizeList = mAllSameNameContacts.size();
				ArrayList<RawContactItem> items = null;
				RawContactItem item = null;
				// gionee xuhz 20120823 modify for CR00678320 start
				final String sel = Data.MIMETYPE +" in ('"+ Phone.CONTENT_ITEM_TYPE +
				"' , '" + Photo.CONTENT_ITEM_TYPE + "')" + 
				" AND " + Data.RAW_CONTACT_ID + " in ("; 
				/*final String sel = Data.MIMETYPE +"='"+ Phone.CONTENT_ITEM_TYPE +
					"' AND " + Data.RAW_CONTACT_ID + " in ("; */
				// gionee xuhz 20120823 modify for CR00678320 end
				StringBuilder sb = new StringBuilder(sel);
				String[] projection = new String[]{"_id", Data.RAW_CONTACT_ID, "data1", "photo_id"};
				int beforeLast = sizeList - 1;
				for (int i = 0, j = 0; i < sizeList; ++i) {
					items = mAllSameNameContacts.get(i);
					int sizeItems = items.size();
					for (int k = 0; k < sizeItems; ++j, ++k) {
						item = items.get(k);
						sb.append(item.mRawContactId).append(",");
					}
					
					if (j >= MAX_INJECT_SIZE || i == beforeLast) {
						sb.replace(sb.length()-1, sb.length(), ")");
						Cursor cursor = mContentResolver.query(Data.CONTENT_URI,
								projection, sb.toString(), null, null);
						if (null != cursor) {
							if (cursor.moveToFirst()) {
								do {
									int rcid = cursor.getInt(1);
									String numbers = mCombineItemsAdapter.mNumberPool.get(rcid); 
									// gionee xuhz 20120823 modify for CR00678320 start
									String tmpNum = cursor.getString(2);
									if (!TextUtils.isEmpty(tmpNum)) {
										if (null == numbers) {
											numbers = tmpNum; 
										} else {
//											numbers = numbers + "\n" + tmpNum;
										}
									}
									// gionee xuhz 20120823 modify for CR00678320 end

									mCombineItemsAdapter.mNumberPool.put(rcid, numbers);
									
									int photoId = cursor.getInt(3);
                                    if (0 != photoId) {
                                        mCombineItemsAdapter.mPhotoPool.put(rcid, photoId);
                                    }
								} while(cursor.moveToNext());
							}
							
							cursor.close();
							
							publishProgress(2);
						}
						
						j = 0;
						sb = new StringBuilder(sel);
					}
				}
				//~~~~~~~~~~~~~~~~~~~~~inject number end~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				
				return null;
			}
			
			@Override
			protected void onProgressUpdate(Integer... values) {
				/*int ret = (null != values && values.length == 1) ? values[0] : -1;*/
				int ret = values[0];
				switch (ret) {
				case 0:
				    //Gionee:huangzy 20120713 add for CR00640627 start
				    finish();
				    //Gionee:huangzy 20120713 add for CR00640627 end
				    break;
				case 1:					
					mListView.setAdapter(mCombineItemsAdapter);
					mListView.setOnScrollListener(mCombineItemsAdapter);
					updataUi();
					break;
				case 2:
					mCombineItemsAdapter.notifyDataSetChanged();
					break;

				default:
					break;
				}
				
			};    		
    	}.execute();
    }
	
	class CombineItemsAdapter extends BaseAdapter implements OnScrollListener {
        private Context mContext;
		private SameContactItems mSameAllRecord;
		public Map<Integer, String> mNumberPool;
		public Map<Integer, Integer> mPhotoPool;
		//Gionee <xuhz> <2013-08-15> delete for CR00857585 begin
		//private final ContactPhotoManager mContactPhotoManager;
		//Gionee <xuhz> <2013-08-15> delete for CR00857585 end
		
		public CombineItemsAdapter(Context context, SameContactItems sameAllRecord) {
			mContext = context;
			mSameAllRecord = sameAllRecord;
			mNumberPool = new HashMap<Integer, String>();
			mPhotoPool = new HashMap<Integer, Integer>();
			//Gionee <xuhz> <2013-08-15> delete for CR00857585 begin
			//mContactPhotoManager = ContactPhotoManager.getInstance(mContext);
			//Gionee <xuhz> <2013-08-15> delete for CR00857585 end
		}
		
		@Override
		public int getCount() {
			return mSameAllRecord.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CombineItemView combineItemView = null;
			if (null != convertView) {
				combineItemView = (CombineItemView) convertView;
				combineItemView.mCombineItemContainer.removeAllViews();
			} else {
				combineItemView = CombineItemView.create(mContext);
				//combineItemView.mCombineItemContainer.setPadding(0, 0, 0, 4);
			}
			
			final ArrayList<RawContactItem> record = mSameAllRecord.get(position);
			ContactSimpleInfoView infoView = null;
			for (RawContactItem r : record) {
				infoView = ContactSimpleInfoView.create(mContext);
				infoView.mNameTv.setText(r.mDisplayName);
				
				// aurora <wangth> <2013-9-27> add for aurora ui begin
				infoView.mHeadIv.setVisibility(View.GONE);
				// aurora <wangth> <2013-9-27> add for aurora ui end
				
				if (null == r.mRawCom) {
					r.mRawCom = mNumberPool.remove(r.mRawContactId);
				}
				
				if (0 == r.mPhotoId) {
                    Integer pid = mPhotoPool.remove(r.mRawContactId);
                    if (null != pid) {
                        r.mPhotoId = pid; 
                    }
                }
				
//				infoView.mNumberTv.setText(r.mRawCom);
				combineItemView.mCombineItemContainer.addView(infoView);
				

			}
			
			combineItemView.mCombineButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					goCombine(record);
				}
			});
			
			return combineItemView;
		}
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
	        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
	            mContactPhotoManager.resume();
	            notifyDataSetChanged();
	        } else {
	            mContactPhotoManager.pause();
	        }
	    }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
        }
	}
	
	protected void goCombine(ArrayList<RawContactItem> items4Combine) {
		if (null == items4Combine || items4Combine.size() < 2) {
			return;
		}
		
		mContacts4Combine = items4Combine;
		
		StringBuilder selection = new StringBuilder("_id in (");
		
		for (RawContactItem item : items4Combine) {
			selection.append(item.mRawContactId).append(",");
		}
		selection.replace(selection.length()-1, selection.length(), ")");
		
		Cursor cursor = getContentResolver().query(
				RawContactsEntity.CONTENT_URI, null, selection.toString(),
				null, null);
		
		if (null != cursor) {
			EntityIterator iterator = RawContacts.newEntityIterator(cursor);
			
			ArrayList<ContentValues> values = new ArrayList<ContentValues>();
			AccountWithDataSet account = null;
			while (iterator.hasNext()) {
				Entity e = iterator.next();
				
				if (null == account) {
				    String type = e.getEntityValues().getAsString("account_type");
				    String name = e.getEntityValues().getAsString("account_name");
				    //Gionee <xuhz> <2013-08-09> modify for CR00850854 begin
				    if (null != type && null != name) {
				    //Gionee <xuhz> <2013-08-09> modify for CR00850854 end
				    	account = new AccountWithDataSet(name, type, null);	
				    }
				}
				ArrayList<NamedContentValues> subs = e.getSubValues();
				for (NamedContentValues s : subs) {
				    values.add(s.values);
				}
			}
			
			//Gionee:huangzy 20120801 modify for CR00651523 start
			for (ContentValues v : values) {
			    v.remove(Data.DATA_VERSION);
			    v.remove(Data.IS_SUPER_PRIMARY);
			    v.remove(Data.IS_PRIMARY);
			    v.remove(Data._ID);			    
			}
			
			// gionee xuhz 20120830 add for CR00678320 start
			mPhotoUri = null;
			// gionee xuhz 20120830 add for CR00678320 end

			for (int i = 0; i < values.size(); i++) {
			    for (int j = i + 1; j < values.size();) {
			    	ContentValues valuesA = values.get(i);
			    	ContentValues valuesB = values.get(j);
			    	
			    	String mimeTypeA = (String) valuesA.get(Data.MIMETYPE);
			    	if (Phone.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeTypeA)) {
			    		String mimeTypeB = (String) valuesB.get(Data.MIMETYPE);
			    		// Gionee <xuhz> <2013-07-29> modify for CR00838797 begin
			    		String aData1 = valuesA.getAsString(Data.DATA1);
			    		if (Phone.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeTypeB) && 
			    				aData1 != null && aData1.equals(valuesB.getAsString(Data.DATA1))) {
			    		// Gionee <xuhz> <2013-07-29> modify for CR00838797 end
			    			valuesB.put(Data.DATA2, valuesA.getAsString(Data.DATA2));
			    		}
			    	}
			    	
	                if (valuesA.equals(valuesB)) {
	                    values.remove(j);
	                } else {
	                    j++;
	                }	                
	            }
			    
				// gionee xuhz 20120830 add for CR00678320 start
			    ContentValues valuesA = values.get(i);
			    String mimeTypeA = (String) valuesA.get(Data.MIMETYPE);
		    	if (Photo.CONTENT_ITEM_TYPE.equalsIgnoreCase(mimeTypeA)) {
		    		Integer data14 = valuesA.getAsInteger(Data.DATA14);
		    		if (mPhotoUri == null) {
			    		if (data14 != null) {
				    		String phontoUri = "content://com.android.contacts/display_photo/" + data14.intValue();
				    		mPhotoUri = Uri.parse(phontoUri);
			    		} else {
			    			values.remove(i);
			    		}
		    		} else {
		    			values.remove(i);
		    		}
		    	}
				// gionee xuhz 20120830 add for CR00678320 end
			}			
			//Gionee:huangzy 20120801 modify for CR00651523 end
			
			iterator.close();
			cursor.close();
			
			startService(ContactSaveService.createNewRawContactIntent(this, values, account, 
                    ManualCombineQueryActivity.class, Intent.ACTION_EDIT));
			
			ContactSaveService.registerListener(this);
		}			
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);	    
	    if (resultCode == RESULT_OK) {
	        mTmpContactConfirmed = true;
	        if (null != mContacts4Combine) {
	            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	            
				// gionee xuhz 20120830 add for CR00678320 start
	            Uri lookUpUri = data.getData();
	            Uri contactUri = Contacts.lookupContact(mContentResolver, lookUpUri);
	            long contactId = ContentUris.parseId(contactUri);
	            Long[] mRawContactIds;
	            ArrayList<Long> rawContactIdsList = AttachPhotoActivity.queryForAllRawContactIds(
	                    mContentResolver, contactId);
	            mRawContactIds = new Long[rawContactIdsList.size()];
	            mRawContactIds = rawContactIdsList.toArray(mRawContactIds);
	            
	            Bitmap photo = null;
	            if (null != mPhotoUri) {
	                try {
	                	InputStream is = mContentResolver.openInputStream(mPhotoUri);
	                    photo = BitmapFactory.decodeStream(is);                    
	                    is.close();
	                } catch (Exception e) {
	                }
	                
	                if (null != photo) {
	                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	                    photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
	                    
	                    final ContentValues imageValues = new ContentValues();
	                    imageValues.put(Photo.PHOTO, stream.toByteArray());
	                    imageValues.put(RawContacts.Data.IS_SUPER_PRIMARY, 1);

	                    // attach the photo to every raw contact
	                    for (Long rawContactId : mRawContactIds) {
	                        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
	                                rawContactId);
	                        Uri rawContactDataUri = Uri.withAppendedPath(rawContactUri,
	                                RawContacts.Data.CONTENT_DIRECTORY);
	                        updatePhoto(imageValues, rawContactDataUri, true);
	                    }
	                }
	            }
				// gionee xuhz 20120830 add for CR00678320 end
	            
	            for (RawContactItem con : mContacts4Combine) {
	                ops.add(ContentProviderOperation.newDelete(
	                        ContentUris.withAppendedId(RawContacts.CONTENT_URI, con.mRawContactId)).build());
	            }
	            try {
	                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
	            } catch (Exception e){
	            }
	            
	            mAllSameNameContacts.remove(mContacts4Combine);
	            
	            //Gionee:huangzy 20120713 add for CR00640627 start
	            if (mAllSameNameContacts.size() <= 0) {
	                finish();
	                return;
	            }
	            //Gionee:huangzy 20120713 add for CR00640627 end
	            updataUi();
	        }
	    } else {
	        deleteTmpContact();
	    }
	}

    @Override
    public void onServiceCompleted(Intent callbackIntent) {
        if (null == callbackIntent) {
            return;
        }        
        
        mTmpContactCombined = callbackIntent.getData();
        Intent intent = new Intent(Intent.ACTION_EDIT, mTmpContactCombined);
        //Gionee:huangzy 20130401 modify for CR00792013 start
        intent.addCategory(IntentFactory.GN_CATEGORY);
        //Gionee:huangzy 20130401 modify for CR00792013 end
        intent.putExtra(ContactEditorActivity.INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true);
        intent.putExtra(ContactEditorActivity.INTENT_KEY_AUTO_SAVE_ON_BACK_PRESS, false);
        startActivityForResult(intent, 1);
    }
    
    private void deleteTmpContact() {
        if (null != mTmpContactCombined) {
            getContentResolver().delete(mTmpContactCombined, null, null);   
        }        
    }
    
    protected void updataUi() {
        mCombineItemsAdapter.notifyDataSetChanged();
//        getActionBar().setSubtitle(String.format(getString(R.string.gn_combine_contacts_sub_title),
//                mCombineItemsAdapter.getCount()));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {            
            case android.R.id.home:
                onBackPressed();
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }
        
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (!mTmpContactConfirmed) {
            deleteTmpContact();   
        }        
    }
    
	// gionee xuhz 20120830 add for CR00678320 start
    /**
     * Inserts a photo on the raw contact.
     * @param values the photo values
     * @param assertAccount if true, will check to verify that no photos exist for Google,
     *     Exchange and unsynced phone account types. These account types only take one picture,
     *     so if one exists, the account will be updated with the new photo.
     */
    private void insertPhoto(ContentValues values, Uri rawContactDataUri,
            boolean assertAccount) {

        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();

        if (assertAccount) {
            // Make sure no pictures exist for Google, Exchange and unsynced phone accounts.
            /*
             * Bug Fix by Mediatek Begin.
             *   Original Android's code:
             *     operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.DATA_SET + " IS NULL AND ("
                            + RawContacts.ACCOUNT_TYPE + " IN (?,?) OR "
                            + RawContacts.ACCOUNT_TYPE + " IS NULL)",
                            new String[] {Photo.CONTENT_ITEM_TYPE, GoogleAccountType.ACCOUNT_TYPE,
                            ExchangeAccountType.ACCOUNT_TYPE})
                            .withExpectedCount(0).build());
             *   CR ID: ALPS00249819
             *   Descriptions: add phone account type in query 
             */
            operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.DATA_SET + " IS NULL AND ("
                            + RawContacts.ACCOUNT_TYPE + " IN (?,?,?) OR "
                            + RawContacts.ACCOUNT_TYPE + " IS NULL)",
                            new String[] {Photo.CONTENT_ITEM_TYPE, GoogleAccountType.ACCOUNT_TYPE,
                            ExchangeAccountType.ACCOUNT_TYPE, LocalPhoneAccountType.ACCOUNT_TYPE})
                            .withExpectedCount(0).build());
            /*
             * Bug Fix by Mediatek End.
             */
        }

        // insert the photo
        values.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        operations.add(ContentProviderOperation.newInsert(rawContactDataUri)
                .withValues(values).build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            // the account doesn't allow multiple photos, so update
            if (assertAccount) {
                updatePhoto(values, rawContactDataUri, false);
            } else {
                throw new IllegalStateException("Problem inserting photo into raw_contacts/data", e);
            }
        }
    }
    
    /**
     * Tries to update the photo on the raw_contact.  If no photo exists, and allowInsert == true,
     * then will try to {@link #updatePhoto(ContentValues, boolean)}
     */
    private void updatePhoto(ContentValues values, Uri rawContactDataUri,
            boolean allowInsert) {
        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();

        values.remove(Photo.MIMETYPE);

        // check that a photo exists
        operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {
                    Photo.CONTENT_ITEM_TYPE
                }).withExpectedCount(1).build());

        // update that photo
        operations.add(ContentProviderOperation.newUpdate(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {Photo.CONTENT_ITEM_TYPE})
                .withValues(values).build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            if (allowInsert) {
                // they deleted the photo between insert and update, so insert one
                insertPhoto(values, rawContactDataUri, false);
            } else {
                throw new IllegalStateException("Problem inserting photo raw_contacts/data", e);
            }
        }
    }
	// gionee xuhz 20120830 add for CR00678320 end
}
