package com.android.contacts.activities;

import java.util.List;

import com.android.contacts.ContactsActivity;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.group.GroupBrowseListFragment.OnGroupBrowserActionListener;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountType;
import com.mediatek.contacts.list.MultiContactsPickerBaseFragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.RawContacts;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

/**
 * aurora:wangth 20130911 add
 */
public class AuroraGroupBrowserActivity extends ContactsActivity {

    private static final String TAG = "AuroraGroupBrowserActivity";
    
    private GroupBrowseListFragment mGroupsFragment;
    
    private static final int AURORA_NEW_GROUP = 1;
    
    public AuroraGroupBrowserActivity() {
    }

    @Override
    protected void onCreate(Bundle savedState) {
    	ContactsApplication.sendSimContactBroad();
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedState);
        
//        setTheme(R.style.GN_PeopleTheme_light);
        
        setAuroraContentView(R.layout.group_browser_activity,
                AuroraActionBar.Type.Normal);
        
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.aurora_group_title);
        final Context context = this;
        new Thread() {
            public void run() {
                String selection = RawContacts.ACCOUNT_NAME + "='Phone' AND " 
                        + RawContacts.ACCOUNT_TYPE + "='Local Phone Account' AND "
                        + RawContacts.DELETED + "=0";
                Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, 
                        new String[]{RawContacts.CONTACT_ID}, selection, null, null);
                int contactId = -1;
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        contactId = cursor.getInt(0);
                    }
                    cursor.close();
                }
                
                Cursor c = context.getContentResolver().query(Contacts.CONTENT_URI, 
                        null, Contacts._ID + "=" + contactId + " AND " + Contacts.IN_VISIBLE_GROUP + "=1", null, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        try {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    addAuroraActionBarItem(AuroraActionBarItem.Type.Add, AURORA_NEW_GROUP);
                                }
                            });
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    c.close();
                }
            }
        }.start();
        actionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        
        Fragment frag = getFragmentManager().findFragmentById(R.id.list_fragment);
        if (null != frag && frag instanceof GroupBrowseListFragment) {
            mGroupsFragment = (GroupBrowseListFragment) frag;
        } else if (mGroupsFragment == null) {
            mGroupsFragment = new GroupBrowseListFragment();
            getFragmentManager().beginTransaction().
                add(R.id.list_fragment, mGroupsFragment).commitAllowingStateLoss();
        }
        
        mGroupsFragment.setListener(new GroupBrowserActionListener());
    }
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case AURORA_NEW_GROUP:
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.contacts", 
                        "com.android.contacts.activities.AuroraGroupEditorActivity"));
                intent.setAction(Intent.ACTION_INSERT);
                startActivity(intent);
                break;
            default:
                break;
            }
        }
    };
    
    private final class GroupBrowserActionListener implements OnGroupBrowserActionListener {

        @Override
        public void onViewGroupAction(Uri groupUri) {
            Log.i(TAG, "groupUri is " + groupUri.toString());
            List uriList = groupUri.getPathSegments();
            final Uri newGroupUri = Uri.parse("content://com.android.contacts")
                    .buildUpon().appendPath(uriList.get(0).toString())
                    .appendPath(uriList.get(1).toString()).build();
            Log.i(TAG, "newUri is " + newGroupUri);
            
            String accountName = null;
            String accountType = null;
            String dataSet = null;
            String groupName = "";
            String ringtone = "";
            Uri mGroupRingtoneUri = null;
            
            Cursor c = ContactsApplication.getInstance().getContentResolver().query(
                    Groups.CONTENT_URI, 
                    new String[]{"account_name", "account_type", "data_set",  Groups.TITLE, "group_ringtone"}, 
                    Groups._ID + " = " + newGroupUri.getLastPathSegment(), 
                    null, 
                    null);
            
            if (null == c) {
                return;
            }
            
            if (c.moveToFirst()) {
                accountName = c.getString(0);
                accountType = c.getString(1);
                dataSet = c.getString(2);
                groupName = c.getString(3);
                ringtone = c.getString(4);
                if (null != ringtone) {
                    mGroupRingtoneUri = Uri.parse(ringtone);
                }
            }
            
            c.close();
            
            Intent intent = new Intent(AuroraGroupBrowserActivity.this,
                    AuroraGroupDetailActivity.class);
            intent.setData(newGroupUri);
            intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER_EXINFO, 
                    "withGroupId/" + newGroupUri.getLastPathSegment());
            intent.putExtra(AuroraGroupEditorActivity.EXTRA_GROUP_NAME, groupName);
            if (mGroupRingtoneUri != null) {
                intent.putExtra(AuroraGroupEditorActivity.EXTRA_RINGTONE, mGroupRingtoneUri.toString());
            }
            intent.putExtra(MultiContactsPickerBaseFragment.EXTRA_ACCOUNT_FILTER, 
                    ContactListFilter
                    .createAccountFilter(accountType, accountName, dataSet, null));
            
            startActivity(intent);
        }
    }
    
}
