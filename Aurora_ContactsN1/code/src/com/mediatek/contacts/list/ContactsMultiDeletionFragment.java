
package com.mediatek.contacts.list;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.contacts.R;

import com.android.contacts.ContactsUtils;
import com.android.internal.telephony.ITelephony;
import com.android.contacts.R;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.list.service.MultiChoiceHandlerListener;
import com.mediatek.contacts.list.service.MultiChoiceRequest;
import com.mediatek.contacts.list.service.MultiChoiceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import android.os.ServiceManager;

public class ContactsMultiDeletionFragment extends MultiContactsPickerBaseFragment {

    public static final String TAG = "ContactsMultiDeletion";
    public static final boolean DEBUG = true;

    private SendRequestHandler mRequestHandler = null;
    private HandlerThread mHandlerThread = null;

    private DeleteRequestConnection mConnection = null;

    private int mRetryCount = 20;

    @Override
    public void onOptionAction() {

        if (getListView().getCheckedItemCount() == 0) {
            Toast.makeText(this.getContext(), R.string.multichoice_no_select_alert,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //Gionee <wangth><2013-04-24> add for CR00790944 begin
        if (getListView().getCheckedItemCount() > 2000) {
            Toast.makeText(this.getContext(), R.string.gn_delete_contacts_limit,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //Gionee <wangth><2013-04-24> add for CR00790944 end

		ConfirmDialog cDialog = new ConfirmDialog();
		cDialog.setTargetFragment(this, 0);
		cDialog.setArguments(this.getArguments());
		cDialog.show(this.getFragmentManager(), "cDialog");
	}

	public static class ConfirmDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this
					.getActivity(), AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
					.setTitle(
					R.string.multichoice_delete_confirm_title).setMessage(
					R.string.multichoice_delete_confirm_message)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									final ContactsMultiDeletionFragment target = (ContactsMultiDeletionFragment) getTargetFragment();
									if (target != null) {
										target.handleDelete();
									}
								}
							});
			return builder.create();

		}
	}

    private void handleDelete() {
        startDeleteService();

        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
            mRequestHandler = new SendRequestHandler(mHandlerThread.getLooper());
        }

        List<MultiChoiceRequest> requests = new ArrayList<MultiChoiceRequest>();

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
        int count = getListView().getCount();
        for (int position = 0; position < count; ++position) {
            if (getListView().isItemChecked(position)) {
                requests.add(new MultiChoiceRequest(adapter.getContactIndicator(position), adapter
                        .getSimIndex(position), adapter.getContactID(position), adapter
                        .getContactDisplayName(position)));
            }
        }

        /*
         * Bug Fix by Mediatek Begin.
         * 
         * CR ID: ALPS00233127
         */
        if (requests.size() > 0) {
            mRequestHandler.sendMessage(mRequestHandler.obtainMessage(SendRequestHandler.MSG_REQUEST, requests));
        } else {
            mRequestHandler.sendMessage(mRequestHandler.obtainMessage(SendRequestHandler.MSG_END));
        }
        /*
         * Bug Fix by Mediatek End.
         */
    }

    private class DeleteRequestConnection implements ServiceConnection {
        private MultiChoiceService mService;

        public boolean sendDeleteRequest(final List<MultiChoiceRequest> requests) {
            Log.d(TAG, "Send an delete request");
            if (mService == null) {
                Log.i(TAG, "mService is not ready");
                return false;
            }
            mService.handleDeleteRequest(requests, new MultiChoiceHandlerListener(mService));
            return true;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected");
            mService = ((MultiChoiceService.MyBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnected from MultiChoiceService");
        }
    }

    private class SendRequestHandler extends Handler {

        public static final int MSG_REQUEST = 100;
        public static final int MSG_END = 200;

        public SendRequestHandler(Looper looper) {
            super(looper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REQUEST) {
                if (!mConnection.sendDeleteRequest((List<MultiChoiceRequest>) msg.obj)) {
                    if (mRetryCount-- > 0) {
                        sendMessageDelayed(obtainMessage(msg.what, msg.obj), 500);
                    } else {
                        sendMessage(obtainMessage(MSG_END));
                    }
                } else {
                    sendMessage(obtainMessage(MSG_END));
                }
                return;
            } else if (msg.what == MSG_END) {
                destroyMyself();
                return;
            }
            super.handleMessage(msg);
        }

    }

    void startDeleteService() {
        mConnection = new DeleteRequestConnection();

        Log.i(TAG, "Bind to MultiChoiceService.");
        // We don't want the service finishes itself just after this connection.
        Intent intent = new Intent(this.getActivity(), MultiChoiceService.class);
        getContext().startService(intent);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    void destroyMyself() {
        getContext().unbindService(mConnection);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    // Gionee:xuhz 20130105 add for CR00757469 start
    public void onOptionShareAction() {
        
        Activity activity = getActivity();

        int selectedCount = this.getListView().getCheckedItemCount();

        if (selectedCount == 0) {
            return;
//            activity.setResult(Activity.RESULT_CANCELED, null);
//            activity.finish();
        }

        final Intent retIntent = new Intent();
        if (null == retIntent) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        int curArray = 0;
        String[] idArrayUri = new String[selectedCount];
        if (null == idArrayUri) {
            activity.setResult(Activity.RESULT_CANCELED, null);
            activity.finish();
            return;
        }

        MultiContactsBasePickerAdapter adapter = (MultiContactsBasePickerAdapter) this.getAdapter();
        int itemCount = getListView().getCount();
        
        // Gionee:wangth 20130402 modify for CR00792214 begin
        /*
        if (selectedCount > 5000) {
            Toast.makeText(getContext(), R.string.share_contacts_limit, Toast.LENGTH_LONG).show();
        */
        if (selectedCount > 2000) {
            Toast.makeText(getContext(), R.string.gn_share_contacts_limit, Toast.LENGTH_LONG).show();
            // Gionee:wangth 20130402 modify for CR00792214 end
            return;
        }
        
        for (int position = 0; position < itemCount; ++position) {
            if (getListView().isItemChecked(position)) {
                idArrayUri[curArray++] = adapter.getContactLookUpKey(position);
                if (curArray > selectedCount) {
                    break;
                }
            }
        }

        retIntent.putExtra(resultIntentExtraName, idArrayUri);
        doShareVisibleContacts("Multi_Contact",null,idArrayUri);
        activity.setResult(Activity.RESULT_OK, retIntent);
        activity.finish();
    }
    
    private void doShareVisibleContacts(String type, Uri uri ,String[] idArrayUriLookUp) {
        if (idArrayUriLookUp == null || idArrayUriLookUp.length == 0) {
            return;
        }

//        final String lookupKey = mContactData.getLookupKey();
        StringBuilder uriListBuilder = new StringBuilder();
        int index = 0;
        for (int i = 0; i < idArrayUriLookUp.length; i++) {
            if (index != 0) {
                uriListBuilder.append(":");    
            }
            // find lookup key
            uriListBuilder.append(idArrayUriLookUp[i]);
            index++;
        }
        
//        Log.i(TAG, "-----------------uriListBuilder is " + uriListBuilder.toString());
        Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI, Uri.encode(uriListBuilder.toString()));
        final Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setDataAndType(shareUri, Contacts.CONTENT_VCARD_TYPE);
        intent.setType(Contacts.CONTENT_VCARD_TYPE);
//        intent.putExtra("contactId", String.valueOf(mContactData.getContactId()));
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        intent.putExtra("LOOKUPURIS", uriListBuilder.toString());

        // Launch chooser to share contact via
        final CharSequence chooseTitle = getText(R.string.share_via);
        final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

        try {
            startActivity(chooseIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.share_error, Toast.LENGTH_SHORT).show();
        }
    }
    // Gionee:xuhz 20130105 add for CR00757469 end
}
