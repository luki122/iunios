package com.android.contacts.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.group.AuroraGroupDetailFragment;
import com.android.contacts.interactions.GroupDeletionDialogFragment;
import com.android.contacts.list.AuroraGroupDetailAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountType;
import com.android.contacts.util.Constants;
import com.android.contacts.util.WeakAsyncTask;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gionee.provider.GnContactsContract;
import gionee.provider.GnContactsContract.CommonDataKinds;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.Groups;
import gionee.provider.GnContactsContract.CommonDataKinds.GroupMembership;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraMenuItem;
import aurora.app.AuroraProgressDialog;
import aurora.app.AuroraAlertDialog;
import com.mediatek.contacts.list.AuroraContactListMultiChoiceActivity;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;

/**
 * aurora:wangth 20130913 add
 */

public class AuroraGroupDetailActivity extends ContactsActivity 
        implements OnSearchViewQuitListener {
    
    private static final String TAG = "AuroraGroupDetailActivity";
    
    private static final int AURORA_GROUP_DETAIL_MORE = 1;
    
    private static String mSelectAllStr;
    private static String mUnSelectAllStr;
    
    private Context mContext;
    private Uri mGroupUri;
    private long mGroupId;
    private String mGroupName;
    private Uri mGroupRingtoneUri;
    private String mGroupRingtoneName;
    
    private static AuroraActionBar mActionBar;
    private static AuroraMenu mAuroraMenu;
    
    private AuroraAlertDialog mDeleteConDialog;
    private static int removeCount = 0;
    private static int addCount = 0;
    private static boolean mIsAdding = false;
    
    private static final int AURORA_ADD_MEMBER = 1;
    private static final int AURORA_EDIT_GROUP = 2;
    
    private static AuroraGroupDetailFragment mFragment;
    
    private ContactsUtils.AuroraContactsProgressDialog mDeleteProgressDialog;
    private static final int START_DELETE = 0;
    private static final int END_DELETE = 1;
    
    private ContactListFilter mFilter;
    
    private final Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what) {
            case START_DELETE: {
                if (null != AuroraGroupDetailActivity.this && !isFinishing()) {
                    if (null == mDeleteProgressDialog) {
                        mDeleteProgressDialog = new ContactsUtils.AuroraContactsProgressDialog(mContext, AuroraProgressDialog.THEME_AMIGO_FULLSCREEN);
                    }
                    mDeleteProgressDialog.setIndeterminate(true);
                    mDeleteProgressDialog.setTitle(R.string.aurora_removeing);
                    mDeleteProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    try {
                        mDeleteProgressDialog.show();
                    } catch (Exception e) {
                        
                    }
                }
                
                break;
            }
            
            case END_DELETE: {
                if (null != AuroraGroupDetailActivity.this && !isFinishing() 
                        && null != mDeleteProgressDialog && mDeleteProgressDialog.isShowing()) {
                    try {
                        mDeleteProgressDialog.dismiss();
                        mDeleteProgressDialog = null;
                    } catch (Exception e) {
                        
                    }
                }
                
                if (null != mFragment) {
                    mFragment.changeToNormalMode(false);
                }
                break;
            }
            }
            
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setTheme(R.style.GN_PeopleTheme_light);
        super.onCreate(savedState);

        mContext = AuroraGroupDetailActivity.this;
        mSelectAllStr = mContext.getResources().getString(R.string.select_all);
        mUnSelectAllStr = mContext.getResources().getString(R.string.unselect_all);
        
        mGroupUri = getIntent().getData();
        if (mGroupUri != null) {
            mGroupId = Long.parseLong(mGroupUri.getLastPathSegment());
        }
        
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            mGroupName = extras.getString(AuroraGroupEditorActivity.EXTRA_GROUP_NAME);
            mGroupRingtoneName = extras.getString(AuroraGroupEditorActivity.EXTRA_RINGTONE);
            mFilter = extras.getParcelable(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER);
        }
        
        setAuroraContentView(R.layout.aurora_group_detail_activity,
                AuroraActionBar.Type.Normal);
        addSearchviewInwindowLayout();
        
        mActionBar = getAuroraActionBar();
        setTitle(mGroupName);
        mAuroraMenu = mActionBar.getActionBarMenu();
        mFragment = (AuroraGroupDetailFragment)getFragmentManager().findFragmentById(R.id.group_detail_fragment);
        
        if (mGroupUri != null) {
            addAuroraActionBarItem(AuroraActionBarItem.Type.More, AURORA_GROUP_DETAIL_MORE);
            mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
            setAuroraMenuCallBack(auroraMenuCallBack);
            setAuroraMenuItems(R.menu.aurora_group_detail);
            mActionBar.initActionBottomBarMenu(R.menu.aurora_remove, 1);
            
            if (mFilter != null && mFilter.accountName != null && !mFilter.accountName.equals("Phone")) {
                removeAuroraMenuItemById(R.id.menu_edit_group);
                removeAuroraMenuItemById(R.id.menu_delete_group);
            }
            
            showLeftRight();
            setOnSearchViewQuitListener(this);
        }
    }
    
    private void showLeftRight() {
        if (mActionBar.getSelectLeftButton() != null ) {
            mActionBar.getSelectLeftButton().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mActionBar.setShowBottomBarMenu(false);
                    mActionBar.showActionBarDashBoard();
                    
                    if (mFragment.getRemoveMemberMode()) {
                        mFragment.changeToNormalMode(true);
                    }
                }
            });
        }
        
        if (mActionBar.getSelectRightButton() != null ) {
            mActionBar.getSelectRightButton().setOnClickListener(new OnClickListener() {

                @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub

                            String selectStr = ((TextView) (mActionBar.getSelectRightButton())).getText().toString();
                            if (selectStr.equals(mSelectAllStr)) {
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
                                mFragment.onSelectAll(true);
                            } else if (selectStr.equals(mUnSelectAllStr)) {
                                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
                                mFragment.onSelectAll(false);
                            }
                        }
            });
        }
    }
    
    public boolean quit() {
        if (mFragment.getRemoveMemberMode()) {
            mActionBar.setShowBottomBarMenu(true);
            mActionBar.showActionBottomeBarMenu();
        }
        
        mFragment.getListView().auroraSetNeedSlideDelete(true);
        return true;
    }
    
    public static void setTitle(String str) {
        if (str == null) {
            mActionBar.setTitle(R.string.aurora_group_title);
        }
            
        mActionBar.setTitle(str);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        mIsAdding = false;
    }
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case AURORA_GROUP_DETAIL_MORE:
                showAuroraMenu();
                break;
            default:
                break;
            }
        }
    };
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU: {
                if (mFragment.mSearchViewHasFocus) {
                    return true;
                }
                
                if (mFragment.getRemoveMemberMode()) {
                    return true;
                }
                
                break;
            }
            
            case KeyEvent.KEYCODE_BACK: {
                try {
                    boolean deleteIsShow = mFragment.getListView().auroraIsRubbishOut();
                    if (deleteIsShow) {
                        mFragment.getListView().auroraSetRubbishBack();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (mActionBar != null && 
                        (mActionBar.auroraIsExitEditModeAnimRunning() || mActionBar.auroraIsEntryEditModeAnimRunning())) {
                    return true;
                }
                
                if (mFragment.getRemoveMemberMode()) {
                    try {
                        Thread.sleep(300);
                        
                        if (isSearchviewLayoutShow()) {
                            hideSearchviewLayout();
                            mActionBar.setShowBottomBarMenu(true);
                            mActionBar.showActionBottomeBarMenu();
                        } else {
                            mActionBar.setShowBottomBarMenu(false);
                            mActionBar.showActionBarDashBoard();
                            mFragment.changeToNormalMode(true);
                        }
                        
                        mFragment.getListView().auroraSetNeedSlideDelete(true);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                break;
            }
            
            default: {
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            case R.id.menu_add_member: {
                if (mContext == null) {
                    return;
                }
                
                if (mIsAdding) {
                    ContactsUtils.toastManager(mContext, R.string.aurora_auto_recording);
                    return;
                }
                
                Intent intent = new Intent(AuroraGroupDetailActivity.this,
                        AuroraContactListMultiChoiceActivity.class);
                intent.putExtra(AuroraGroupEditorActivity.EXTRA_GROUP_NAME, mContext.getResources().getString(R.string.aurora_group_no_group));
                intent.putExtra(
                        MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO,
                        "noGroupId/" + mGroupId);
                intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER, mFilter);
                startActivityForResult(intent, AURORA_ADD_MEMBER);
                break;
            }
            
            case R.id.menu_send_mms: {
                new SendGroupSmsTask(AuroraGroupDetailActivity.this).execute(mGroupName);
                break;
            }
            
            case R.id.menu_edit_group: {
                mGroupUri = mGroupUri.buildUpon().appendPath(String.valueOf(-1)).build();
                
                if (mFragment == null || mFragment.getAdapter() == null) {
                    break;
                }
                
                final Intent intent = new Intent(AuroraGroupDetailActivity.this, AuroraGroupEditorActivity.class);
                int SimId = -1;
                int SlotId = Integer.parseInt(mGroupUri.getLastPathSegment().toString());
                String grpId = mGroupUri.getPathSegments().get(1).toString(); 
                Log.i(TAG, grpId+"--------grpId");
                Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon().appendPath(grpId).build();  
                Log.i(TAG, uri.toString()+"--------groupUri.getPath();");
                
                int count = mFragment.getAdapter().getCount();
                long[] ids = new long[count];
                for (int i = 0; i < count; i++) {
                    ids[i] = Long.valueOf(mFragment.getAdapter().getRawcontactId(i));
                }
                
                intent.setData(uri);
                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra("SLOT_ID", SlotId );
                intent.putExtra("SIM_ID", SimId);
                intent.putExtra(AuroraGroupEditorActivity.EXTRA_GROUP_NAME, mGroupName);
                intent.putExtra(AuroraGroupEditorActivity.EXTRA_RINGTONE, mGroupRingtoneName);
                intent.putExtra("update_rawcontact_id", ids);
                
                startActivityForResult(intent, AURORA_EDIT_GROUP);
                break;
            }
            
            case R.id.menu_delete_group: {
                GroupDeletionDialogFragment.show(getFragmentManager(), mGroupId, mGroupName,
                        true, -1, -1);
                break;
            }
            
            case R.id.menu_delete: {
                removeGroupMembers();
//                mFragment.changeToNormalMode();
                break;
            }

            default:
                break;
            }
        }
    };
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
            case AURORA_ADD_MEMBER: {
                Bundle extra = data.getExtras();
                
                long[] contactsIds = extra.getLongArray(AuroraContactListMultiChoiceActivity.EXTRA_ID_ARRAY);
                int[] simIndexIds = extra.getIntArray(AuroraContactListMultiChoiceActivity.EXTRA_SIM_INDEX_ARRAY);
                addCount = contactsIds.length;
                if (null != contactsIds) {
                    addGroupMembers(contactsIds, simIndexIds);
                }
                
                break;
            }
            
            case AURORA_EDIT_GROUP: {
                Bundle extra = data.getExtras();
                if (extra != null) {
                    mGroupName = extra.getString(AuroraGroupEditorActivity.EXTRA_GROUP_NAME);
                    mGroupRingtoneName = extra.getString(AuroraGroupEditorActivity.EXTRA_RINGTONE);
                    setTitle(mGroupName);
                }
                
                break;
            }
            }
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        int slotId = intent.getIntExtra("mSlotId", -1);
        boolean dismissDialog = intent.getBooleanExtra("dismissDialog", false);
        Log.i(TAG, "mSlotId = " + slotId);
        Log.i(TAG, "dismissDialog = " + dismissDialog);
        String action = intent.getAction();
        Log.i(TAG, "action = " + action);
        
        if(dismissDialog){
            if ("deleteGroupMember".equals(action)) {
                mHandler.sendEmptyMessage(END_DELETE);
                Toast.makeText(mContext, ContactsApplication.getInstance().
                        getResources().getString(R.string.aurora_remove_group_member_toast, 
                                mGroupName, removeCount), Toast.LENGTH_SHORT).show();
            } else if ("addGroupMember".equals(action)) {
                mIsAdding = false;
                Toast.makeText(mContext, ContactsApplication.getInstance().
                        getResources().getString(R.string.aurora_add_group_member_toast, 
                                addCount, mGroupName), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    protected void addGroupMembers(long[] ids, int[] simIndexIds) {
        if (null == ids) {
            return;
        }
        
//        int len = ids.length;
//        if (len > ContactsApplication.MAX_BATCH_HANDLE_NUM) {
//            ids = subIdsIfNeed(ids);
//            Toast.makeText(mContext, 
//                    String.format(getString(R.string.gn_max_add_num), ContactsApplication.MAX_BATCH_HANDLE_NUM), 
//                    Toast.LENGTH_LONG).show();
//        }
        
        Intent saveIntent = ContactSaveService.createGroupUpdateIntent(mContext, mGroupId, null, 
                ids, null, getClass(), "addGroupMember", null, -1, simIndexIds, null);
        startService(saveIntent);
        
        Log.d(TAG, "mGroupRingtoneUri = " + mGroupRingtoneUri);
        if (mGroupRingtoneName != null) {
            mGroupRingtoneUri = Uri.parse(mGroupRingtoneName);
            if (mGroupRingtoneUri != null) {
                handleRingtonePicked(mGroupRingtoneUri);
            }
        }
        
        mIsAdding = true;
    }
    
    protected void removeGroupMembers() {
        int selectedCount = mFragment.getAdapter().getCheckedItem().size();
        String message = ContactsApplication.getInstance().
                getResources().getString(R.string.aurora_remove_group_member_message_multi, 
                mGroupName, selectedCount);
        if (null == mDeleteConDialog) {
            mDeleteConDialog = new AuroraAlertDialog.Builder(this,
                    AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                    .setTitle(R.string.gn_remove)
                    .setMessage(message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    reallyRemoveGroupMembers();
                                }
                            }).create();
        }
        
        mDeleteConDialog.setMessage(message);
        mDeleteConDialog.show();
    }
    
    protected void reallyRemoveGroupMembers() {
        if (mDeleteConDialog != null) {
            mDeleteConDialog.dismiss();
            mDeleteConDialog = null;
        }
        
        mHandler.sendEmptyMessage(START_DELETE);
        int selectedCount = mFragment.getAdapter().getCheckedItem().size();
        removeCount = selectedCount;
        long[] checkedIds = new long[selectedCount];
        int index = 0;
        Set<Long> contactsIds = mFragment.getAdapter().getCheckedItem().keySet();
        for (Long id : contactsIds) {
            checkedIds[index++] = id;
        }
        
        long[] idArray = new long[selectedCount];
        int[] simIndexIds = new int[selectedCount];
        for (int position = 0, curArray = 0; position < selectedCount && curArray < selectedCount; ++ position) {
            int contactId = (int)checkedIds[position];
            int rawContactId = ContactsUtils.getRawContactId(mContext, contactId);
            
            idArray[curArray] = rawContactId;
            simIndexIds[curArray] = -1;
            Log.d(TAG, "idArray[" + curArray + "] = " + idArray[curArray]);
            ++curArray;
        }
        
        Intent saveIntent = ContactSaveService.createGroupUpdateIntent(mContext, mGroupId, null, 
                null, idArray, getClass(), "deleteGroupMember", null, -1, null, simIndexIds);
        startService(saveIntent);
    }
    
    protected long[] subIdsIfNeed(long[] ids) {
        if (null == ids || ids.length < ContactsApplication.MAX_BATCH_HANDLE_NUM) {
            return ids;
        }
        
        return Arrays.copyOfRange(ids, 0, ContactsApplication.MAX_BATCH_HANDLE_NUM);
    }
    
    private void handleRingtonePicked(Uri pickedUri) {
        String customRingtone = pickedUri.toString();
        String selection = "( name_raw_contact_id IN ( "
            + "SELECT raw_contact_id FROM view_data"
            + " WHERE ("
            + Data.MIMETYPE + " = '" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
            + "' AND data1=" + mGroupId
            + ")))";
        
        Intent intent = ContactSaveService.createBatchSetRingtone(
                mContext, selection, customRingtone);
        
        mContext.startService(intent);
    }
    
    private class SendGroupSmsTask extends
            WeakAsyncTask<String, Void, String, Activity> {
        private WeakReference<AuroraProgressDialog> mProgress;

        public SendGroupSmsTask(Activity target) {
            super(target);
        }

        @Override
        protected void onPreExecute(Activity target) {
            mProgress = new WeakReference<AuroraProgressDialog>(
                    AuroraProgressDialog.show(target, null,
                            target.getText(R.string.please_wait), true));
        }

        @Override
        protected String doInBackground(final Activity target, String... group) {
            return getSmsAddressFromGroup(target.getBaseContext(), mGroupId);
        }

        @Override
        protected void onPostExecute(final Activity target, String address) {
            AuroraProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            if (address == null || address.length() == 0) {
                Toast.makeText(target, R.string.no_valid_number_in_group,
                        Toast.LENGTH_SHORT).show();
            } else {
                String[] list = address.split(";");
                if (list.length > 1) {
                    Toast.makeText(target, list[1], Toast.LENGTH_SHORT).show();
                }
                address = list[0];
                if (address == null || address.length() == 0) {
                    return;
                }
                
                String[] numbers = address.split(",");
                if (numbers != null && numbers.length > 100) {
                    ContactsUtils.toastManager(mContext, R.string.aurora_group_mms_more_toast);
                    return;
                }
                
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.fromParts(Constants.SCHEME_SMSTO, address,
                        null));
                startActivity(intent);
                Log.d(TAG, "startActivity goto mms.");
            }
        }

        public String getSmsAddressFromGroup(Context context, long groupId) {
            Log.d(TAG, "groupId:" + groupId);
            StringBuilder builder = new StringBuilder();
            ContentResolver resolver = context.getContentResolver();
            Cursor contactCursor = resolver.query(
                    Data.CONTENT_URI,
                    new String[] { Data.CONTACT_ID },
                    Data.MIMETYPE + "=? AND " + GroupMembership.GROUP_ROW_ID
                            + "=?",
                    new String[] { GroupMembership.CONTENT_ITEM_TYPE,
                            String.valueOf(groupId) }, null);
            Log.d(TAG, "contactCusor count:" + contactCursor.getCount());
            StringBuilder ids = new StringBuilder();
            HashSet<Long> allContacts = new HashSet<Long>();
            if (contactCursor != null) {
                while (contactCursor.moveToNext()) {
                    Long contactId = contactCursor.getLong(0);
                    if (!allContacts.contains(contactId)) {
                        ids.append(contactId).append(",");
                        allContacts.add(contactId);
                    }
                }
                contactCursor.close();
            }
            StringBuilder where = new StringBuilder();
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.length() - 1);
                where.append(Data.CONTACT_ID + " IN (");
                where.append(ids.toString());
                where.append(")");
            } else {
                return "";
            }
            where.append(" AND ");
            where.append(Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'");
            Log.i(TAG, "getSmsAddressFromGroup where " + where);

            Cursor cursor = resolver.query(Data.CONTENT_URI, new String[] {
                    Data.DATA1, Phone.TYPE, Data.CONTACT_ID },
                    where.toString(), null, Data.CONTACT_ID + " ASC ");
            if (cursor != null) {
                long candidateContactId = -1;
                int candidateType = -1;
                String candidateAddress = "";
                while (cursor.moveToNext()) {
                    Long id = cursor.getLong(2);
                    if (allContacts.contains(id))
                        allContacts.remove(id);
                    int type = cursor.getInt(1);
                    String number = cursor.getString(0);
                    int numIndex = number.indexOf(",");
                    int tempIndex = -1;
                    if ((tempIndex = number.indexOf(";")) >= 0) {
                        if (numIndex < 0) {
                            numIndex = tempIndex;
                        } else {
                            numIndex = numIndex < tempIndex ? numIndex
                                    : tempIndex;
                        }
                    }
                    if (numIndex == 0) {
                        continue;
                    } else if (numIndex > 0) {
                        number = number.substring(0, numIndex);
                    }

                    if (candidateContactId == -1) {
                        candidateContactId = id;
                        candidateType = type;
                        candidateAddress = number;
                    } else {
                        if (candidateContactId != id) {
                            if (candidateAddress != null
                                    && candidateAddress.length() > 0) {
                                if (builder.length() > 0) {
                                    builder.append(",");
                                }
                                builder.append(candidateAddress);
                            }
                            candidateContactId = id;
                            candidateType = type;
                            candidateAddress = number;
                        } else {
                            if (candidateType != Phone.TYPE_MOBILE
                                    && type == Phone.TYPE_MOBILE) {
                                candidateContactId = id;
                                candidateType = type;
                                candidateAddress = number;
                            }
                        }
                    }
                    if (cursor.isLast()) {
                        if (candidateAddress != null
                                && candidateAddress.length() > 0) {
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(candidateAddress);
                        }
                    }

                }
                cursor.close();
            }
            Log.i(TAG, "[getSmsAddressFromGroup]address:" + builder);

            ids = new StringBuilder();
            where = new StringBuilder();
            List<String> noNumberContactList = new ArrayList<String>();
            if (allContacts.size() > 0) {
                Long[] allContactsArray = allContacts.toArray(new Long[0]);
                for (Long id : allContactsArray) {
                    if (ids.length() > 0) {
                        ids.append(",");
                    }
                    ids.append(id.toString());
                }
            }
            if (ids.length() > 0) {
                where.append(Data.CONTACT_ID + " IN(");
                where.append(ids.toString());
                where.append(")");
            } else {
                return builder.toString();
            }
            where.append(" AND ");
            where.append(Data.MIMETYPE + "='"
                    + StructuredName.CONTENT_ITEM_TYPE + "'");
            Log.i(TAG,
                    "[getSmsAddressFromGroup]query no name cursor selection:"
                            + where.toString());

            Cursor cursor2 = resolver.query(Data.CONTENT_URI,
                    new String[] { Data.DATA1 }, where.toString(), null,
                    Data.CONTACT_ID + " ASC ");

            if (cursor2 != null) {
                while (cursor2.moveToNext()) {
                    noNumberContactList.add(cursor2.getString(0));
                }
                cursor2.close();
            }
            String str = "";
            if (noNumberContactList.size() == 1) {
                str = context.getString(R.string.send_groupsms_no_number_1,
                        noNumberContactList.get(0));
            } else if (noNumberContactList.size() == 2) {
                str = context.getString(R.string.send_groupsms_no_number_2,
                        noNumberContactList.get(0), noNumberContactList.get(1));
            } else if (noNumberContactList.size() > 2) {
                str = context.getString(R.string.send_groupsms_no_number_more,
                        noNumberContactList.get(0),
                        String.valueOf(noNumberContactList.size() - 1));
            }
            String result = builder.toString();
            Log.i(TAG, "[getSmsAddressFromGroup]result:" + result);
            if (str != null && str.length() > 0) {
                return result + ";" + str;
            } else
                return result;
        }
    }
    
    public void updateSelectedItemsView(int allCount) {
        if (mFragment == null || mFragment.getAdapter() == null) {
            return;
        }
        
        try {
            int count = allCount;
            int curArray = mFragment.getAdapter().getCheckedItem().size();
            Log.d(TAG, "curArray = " + curArray + "  count = " + count);
            
            if (curArray >= count) {
                ((TextView) (mActionBar.getSelectRightButton())).setText(mUnSelectAllStr);
            } else {
                ((TextView) (mActionBar.getSelectRightButton())).setText(mSelectAllStr);
            }
            
            if (curArray > 0) {
                setBottomMenuEnable(true);
            } else {
                setBottomMenuEnable(false);
            }
            
            if (mGroupUri != null) {
                if (count == 0) {
                    removeAuroraMenuItemById(R.id.menu_send_mms);
                } else {
                    addAuroraMenuItemById(R.id.menu_send_mms);
                }
            }
            
            setRightBtnTv(curArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setBottomMenuEnable(boolean flag) {
        AuroraMenu auroraMenu = mActionBar.getAuroraActionBottomBarMenu();
        if (auroraMenu != null) {
            auroraMenu.setBottomMenuItemEnable(1, flag);
        }
    }

    public void setRightBtnTv (int checkCount) {
        Button conBut = getSearchViewRightButton();
        if (conBut != null) {
            String str = mContext.getResources().getString(R.string.cancel);
            if (mFragment.getRemoveMemberMode()) {
                str = mContext.getResources().getString(R.string.aurora_search_continue);
//                if (checkCount > 0) {
//                    str = mContext.getResources().getString(R.string.aurora_search_continue_mutil, checkCount);
//                }
            }
            
            conBut.setText(str);
        }
    }
    
    // aurora qiaohu 20141023 bug9200 start
    public String getGroupName() {
    	return mGroupName;
    }
    // aurora qiaohu 20141023 bug9200 end
}
