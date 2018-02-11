package com.mediatek.contacts.list;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.mediatek.contacts.util.ContactsGroupUtils;
import com.mediatek.contacts.util.ContactsGroupUtils.ContactsGroupArrayData;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.util.WeakAsyncTask;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;

import android.accounts.Account;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import aurora.app.AuroraProgressDialog;

public class ContactsGroupMultiPickerFragment extends MultiContactsPickerBaseFragment {

    private static final String TAG = ContactsGroupMultiPickerFragment.class.getSimpleName();
    public static final boolean DEBUG = true;
    private static HashMap<Long, ContactsGroupUtils.ContactsGroupArrayData> selectedContactsMap = 
         new HashMap<Long, ContactsGroupUtils.ContactsGroupArrayData>();

    private String fromUgroupName = null;
    private int mSlotId = 0;
    private long fromPgroupId = 0;
    private String mAccountName;
    static AuroraProgressDialog mPogressDialog;
    private MoveGroupTask mMoveGroupTask;
    private boolean mCancel = false;
    private Account mAccount = null;

    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 150;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Intent intent = this.getArguments().getParcelable(FRAGMENT_ARGS);
        fromUgroupName = intent.getStringExtra("mGroupName");
        mAccountName = intent.getStringExtra("mAccountName"); 
        mSlotId = intent.getIntExtra("mSlotId", -1);
        fromPgroupId = intent.getLongExtra("mGroupId", -1);
        mAccount = intent.getParcelableExtra("account");
        if (DEBUG) {
            Log.i(TAG, "[onCreate]fromUgroupName:" + fromUgroupName
                    + "|fromPgroupId:" + fromPgroupId
                    + "|mSlotId:" + mSlotId + "|mAccountName:" + mAccountName); 
        }

        showFilterHeader(false);
    }

    @Override
    public void onOptionAction() {
        if (getListView().getCheckedItemCount() == 0) {
            Toast.makeText(this.getContext(), R.string.multichoice_no_select_alert,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        selectedContactsMap.clear();
        ContactsGroupMultiPickerAdapter adapter = (ContactsGroupMultiPickerAdapter) this.getAdapter();
        int count = getListView().getCount();
        int simIndex = 0;
        int indexSimOrPhone = 0;
        for (int position = 0; position < count; ++position) {
            if (getListView().isItemChecked(position)) {
                long contactId = adapter.getContactID(position);
                if(DEBUG)Log.d(TAG, "contactId = " + contactId);
                simIndex = adapter.getSimIndex(position);
                // ToDo to Check the method called
                indexSimOrPhone = adapter.getContactIndicator(position);
                adapter.getContactID(position);
                selectedContactsMap.put(contactId,
                        new ContactsGroupUtils.ContactsGroupArrayData()
                                .initData(simIndex, indexSimOrPhone));
            }
        }
        if (DEBUG) {
            Log.i(TAG, "[onOptionAction]selectedContactsMap size"
                    + selectedContactsMap.size());
        }
        MoveDialog moveDialog = new MoveDialog();
        moveDialog.setArguments(this.getArguments());
        moveDialog.show(this.getFragmentManager(), "moveGroup");
    }

    @Override
    protected ContactListAdapter createListAdapter() {
        ContactsGroupMultiPickerAdapter adapter = new ContactsGroupMultiPickerAdapter(
                getActivity(), getListView());
        adapter.setFilter(ContactListFilter
                .createFilterWithType(ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS));
        adapter.setSectionHeaderDisplayEnabled(true);
        adapter.setDisplayPhotos(true);
        adapter.setQuickContactEnabled(false);
        adapter.setEmptyListEnabled(true);

        adapter.setGroupTitle(fromUgroupName);
        
        adapter.setGroupAccount(mAccount);

        return adapter;
    }

    
	public class MoveGroupTask extends
			WeakAsyncTask<String, Void, Boolean, Activity> {
		private WeakReference<AuroraProgressDialog> mProgress;
		
		public MoveGroupTask(Activity target) {
			super(target);
		}
		
		@Override
		protected void onPreExecute(final Activity target) {
			//Gionee:huangzy 20120815 add for CR00664358 start
			final int size = selectedContactsMap.size() ;
			Log.i("James", "size  = " + size);
			if (size > ContactsApplication.MAX_BATCH_HANDLE_NUM) {
				Toast.makeText(getActivity(), 
						String.format(getActivity().getString(R.string.gn_max_move_num), ContactsApplication.MAX_BATCH_HANDLE_NUM),
						Toast.LENGTH_LONG).show();
				
				HashMap<Long, ContactsGroupUtils.ContactsGroupArrayData> tmpContactsMap = 
			         new HashMap<Long, ContactsGroupUtils.ContactsGroupArrayData>();
				
				Set<Entry<Long, ContactsGroupArrayData>> entrySet = selectedContactsMap.entrySet();
				int count = 1;
				for (Entry<Long, ContactsGroupArrayData> set : entrySet) {
					tmpContactsMap.put(set.getKey(), set.getValue());
					++count;
					if (count > ContactsApplication.MAX_BATCH_HANDLE_NUM) {
						selectedContactsMap.clear();
						selectedContactsMap = tmpContactsMap;
						break;
					}
				}
			}
			//Gionee:huangzy 20120815 add for CR00664358 end
			
			mPogressDialog = AuroraProgressDialog.show(target, null, target
					.getText(R.string.moving_group_members), false, true);
			mPogressDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							mMoveGroupTask.cancel(true);
							target.finish();
						}
					});
			mPogressDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {

						public void onCancel(DialogInterface dialog) {
							mCancel = true;
							boolean cancel = mMoveGroupTask.cancel(true);
							Log.i(TAG, cancel + "------------cancel");
							target.finish();

						}
					});
			mProgress = new WeakReference<AuroraProgressDialog>(mPogressDialog);
			super.onPreExecute(target);
		}

		@Override
		protected Boolean doInBackground(Activity target,
				String... params) {   

			String fromGroupName = params[0];
			String toGroupName = params[1];
			long fromGroupId = Long.parseLong(params[2]);
			long toGroupId = Long.parseLong(params[3]);
			int slot = Integer.parseInt(params[4]);

			if (DEBUG) {
				Log.i(TAG, "fromGroupName:" + fromGroupName + "|toGroupName:"
						+ toGroupName + "|fromGroupId:" + fromGroupId
						+ "|toGroupId:" + toGroupId + "|slot:" + slot
						+ "|selectedContactsMap size:"
						+ selectedContactsMap.size());
			}
			doMove(target.getContentResolver(), fromGroupName, slot,
					toGroupName, selectedContactsMap, fromGroupId, toGroupId);
			return false;
		}

		@Override
		protected void onCancelled() {
			mCancel = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(final Activity target, Boolean error) {
			final AuroraProgressDialog progress = mProgress.get();
			int toast;
			if (!target.isFinishing() && progress != null
					&& progress.isShowing()) {
				progress.dismiss();
			}
			super.onPostExecute(target, error);
			if (error) {
				toast = R.string.removing_group_members_fail;
			} else {
				toast = R.string.moving_group_members_sucess;
			}
			if (!error)
				Toast.makeText(target, toast, Toast.LENGTH_LONG).show();
			selectedContactsMap.clear();
			target.finish();
		}
	}

    public class MoveDialog extends DialogFragment {

        private Activity mContext = null;
        private String accountName = null;
        private int slotId = -1;
        private long originalGroupId = -1;
        private String originalGroupName = null;
        private long targetGroupId = -1;
        private String targetGroupName = null;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mContext = activity;
            mMoveGroupTask = new MoveGroupTask(mContext);
        }

        public void onCreate(Bundle savedState) {
            super.onCreate(savedState);
            Intent intent = this.getArguments().getParcelable(FRAGMENT_ARGS);

            originalGroupName = intent.getStringExtra("mGroupName");
            accountName = intent.getStringExtra("mAccountName");
            slotId = intent.getIntExtra("mSlotId", -1);
            originalGroupId = intent.getLongExtra("mGroupId", -1);

            Log.d(TAG, "[MoveDialog#onCreate]originalGroupName:"
                    + originalGroupName + "|originalGroupId:" + originalGroupId
                    + "|accountName:" + accountName + "|slotId:" + slotId);

            if (savedState != null) {
                targetGroupName = savedState.getString("target_group_name");
                targetGroupId = savedState.getLong("target_group_id", -1);
                Log.d(TAG, "[MoveDialog#onCreate]targetGroupName:"
                        + targetGroupName + "|targetGroupId:"
                        + String.valueOf(targetGroupId));
            }

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (!TextUtils.isEmpty(targetGroupName))
                outState.putString("target_group_name", targetGroupName);
            outState.putLong("target_group_id", targetGroupId);
            outState.putString("mGroupName", originalGroupName);
            outState.putString("mAccountName", accountName);
            outState.putInt("mSlotId", slotId);
            outState.putLong("mGroupId", originalGroupId);
            super.onSaveInstanceState(outState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Cursor cursor = mContext.getContentResolver().query(
                    Groups.CONTENT_URI,
                    new String[] { Groups._ID, Groups.TITLE },
                    Groups.DELETED + "=0 " + "AND " + Groups.ACCOUNT_NAME
                    + "= '" + accountName + "' AND " + Groups._ID
                    + " !=" + originalGroupId, null, Groups.TITLE);
            final ArrayList<Long> idList = new ArrayList<Long>();
            final ArrayList<String> titleList = new ArrayList<String>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    idList.add(cursor.getLong(0));
                    String title = cursor.getString(1);
                    titleList.add(title);
                }
                cursor.close();
            }
            
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this.getActivity(),
            		AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
            builder.setTitle(R.string.move_contacts_to).setTitleDividerVisible(true);
//            builder.setIcon(R.drawable.ic_dialog_alert_holo_light);
            
            CharSequence[] list = titleList.toArray(new String[0]);
            builder.setSingleChoiceItems(list, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            long id = idList.get(which);
                            String title = titleList.get(which);
                            Log.d(TAG, "[onClick]Move to title:" + title + " ||id: " + id);
                            targetGroupId = id;
                            targetGroupName = title;
                        }
                    });
            
            builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    if (targetGroupId > 0) { 
                    	mMoveGroupTask.execute(
                                originalGroupName, 
                                targetGroupName, 
                                String.valueOf(originalGroupId), 
                                String.valueOf(targetGroupId), 
                                String.valueOf(slotId));
                    } else {
                        if (mContext != null) {
                            Toast.makeText(mContext, R.string.multichoice_no_select_alert, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            return builder.create();
        }
    }
    private void doMove(ContentResolver resolver, String fromUgroupName, int slotId,
			String toUgroupName, HashMap<Long, ContactsGroupArrayData> selectedContactsMap,
			long fromPgroupId, long toPgroupId) {

		int fromUgroupId = -1;
		int toUgroupId = -1;
		if (slotId >= 0) {
	      try {
	          fromUgroupId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, fromUgroupName);
	          toUgroupId = ContactsGroupUtils.USIMGroup.hasExistGroup(slotId, toUgroupName);
	         } catch (RemoteException e) {
	             //log
	             fromUgroupId = -1;
	             toUgroupId = -1;
	         }
		}

		Cursor c = resolver.query(Data.CONTENT_URI,
                new String[]{Data.CONTACT_ID}, 
                Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID + "=?", 
                new String[]{GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(toPgroupId)}, null);
		
		HashSet<Long> set = new HashSet<Long>();
		while (c != null && c.moveToNext()) {
			long contactId = c.getLong(0);
			Log.i(TAG, contactId+"--------contactId");
			set.add(contactId);
		}
		if (c != null)
			c.close();

		ContentValues values = new ContentValues();
		StringBuilder idBuilder = new StringBuilder();
		StringBuilder idBuilderDel = new StringBuilder();
		// USIM group begin
		boolean isInTargetGroup = false;
		// USIM group end
		Iterator<Entry<Long, ContactsGroupArrayData>> iter = selectedContactsMap.entrySet()
				.iterator();
		int moveCount = 0;

		while (iter.hasNext()&& !mCancel) {
			Log.i(TAG, mCancel+"----------mCancel---------");
			// Gionee:wangth 20120808 modify for CR00671830 begin
			/*
			Entry<Long, ContactsGroupArrayData> entry = iter.next();
			*/
			Entry<Long, ContactsGroupArrayData> entry = null;
			try {
			    entry = iter.next();
			} catch (Exception e) {
			    e.printStackTrace();
			}
			
			if (entry == null) {
			    return;
			}
			// Gionee:wangth 20120808 modify for CR00671830 end
			long id = entry.getKey();
			Log.i(TAG, id+"--------entry.getKey()");
			// USIM Group begin
			isInTargetGroup = set.contains(id);
			
			
			int tsimId = entry.getValue().getmSimIndexPhoneOrSim();
			
			if(DEBUG)Log.i(TAG, "contactsId--------------"+id);
			if(DEBUG)Log.i(TAG, "mSimIndexPhoneOrSim--------------"+tsimId);
			if(DEBUG)Log.i(TAG, "mSimIndex--------------"+entry.getValue().getmSimIndex());
			if (tsimId > 0
					&& !ContactsGroupUtils.moveUSIMGroupMember(entry.getValue(), slotId,
							 isInTargetGroup, fromUgroupId, toUgroupId)) {
				// failed to move USIM contacts from one group to another
				Log.d(TAG,"Failed to move USIM contacts from one group to another");
				continue;
			}
			
			if (isInTargetGroup) { // mark as need to be delete later
				if (idBuilderDel.length() > 0) {
					idBuilderDel.append(",");
				}
				idBuilderDel.append(id);
			} else { // mark as need to be update later
				if (idBuilder.length() > 0) {
					idBuilder.append(",");
				}
				idBuilder.append(id);
			}
			// USIM Group end
			moveCount++;
			if (moveCount > MAX_OP_COUNT_IN_ONE_BATCH) {
				int count = 0;
				if (idBuilder.length() > 0) {
					String where = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
							idBuilder.toString()).replace("%2",
							String.valueOf(fromPgroupId));
					Log.i(TAG, "[doMove]where: " + where);
					values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID,
							toPgroupId);

					count = resolver.update(GnContactsContract.Data.CONTENT_URI,
							values, where, null);
					idBuilder.setLength(0);
				}
				Log.i(TAG, "[doMove]move data count:" + count);
				count = 0;
				if (idBuilderDel.length() > 0) {
					String whereDel = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
							idBuilderDel.toString());
					whereDel = whereDel.replace("%2", String
							.valueOf(fromPgroupId));
					Log.i(TAG, "[doMove]whereDel: " + whereDel);
					count = resolver.delete(GnContactsContract.Data.CONTENT_URI,
							whereDel, null);
					Log.i(TAG, "[doMove]delete repeat contact:"
							+ whereDel.toString());
					idBuilderDel.setLength(0);
				}
				Log.i(TAG, "[doMove]delete repeat data count:" + count);
				moveCount = 0;
			}
		}
		int count = 0;
		if (idBuilder.length() > 0) {
			String where = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
					idBuilder.toString()).replace("%2",
					String.valueOf(fromPgroupId));
			Log.i(TAG, "[doMove]End where: " + where);
			values
					.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID,
							toPgroupId);

			count = resolver.update(GnContactsContract.Data.CONTENT_URI, values,
					where, null);
			idBuilder.setLength(0);
		}
		Log.i(TAG, "[doMove]End move data count:" + count);
		count = 0;
		if (idBuilderDel.length() > 0) {
			String whereDel = ContactsGroupUtils.SELECTION_MOVE_GROUP_DATA.replace("%1",
					idBuilderDel.toString());
			whereDel = whereDel.replace("%2", String.valueOf(fromPgroupId));
			Log.i(TAG, "[doMove]End whereDel: " + whereDel);
			count = resolver.delete(GnContactsContract.Data.CONTENT_URI,
					whereDel, null);
			idBuilderDel.setLength(0);
		}
		Log.i(TAG, "[doMove]End delete repeat data count:" + count);
	}
}
