package com.android.contacts.util;

import com.android.contacts.ContactsApplication;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import gionee.provider.GnContactsContract.RawContacts;

public class ImportExportUtil {

	private static final String TAG = "ImportExportUtil";

	private static ImportExportUtil instance = null;

	public QuerySimContactsTask simContatcts = null;
	public QueryPhoneContactsTask phoneContatcts = null;

	public boolean simQuerying = false;
	public boolean phoneQuerying = false;

	ResultListener simListener;
	ResultListener phoneListener;

	public static ImportExportUtil getInstance() {
		if (instance == null) {
			instance = new ImportExportUtil();
		}
		return instance;
	}

	public void setSimListner(ResultListener listner) {
		simListener = listner;
	}

	public void setPhoneListner(ResultListener listner) {
		phoneListener = listner;
	}

	public void startQueryPhoneContactsTask() {
		if (!phoneQuerying) {
			if (phoneContatcts == null) {
				phoneContatcts = new QueryPhoneContactsTask();
				phoneContatcts.execute();
			}
		}
	}

	public void startQuerySimContactsTask() {
		if (!simQuerying) {
			if (simContatcts == null) {
				simContatcts = new QuerySimContactsTask();
				simContatcts.execute();
			}
		}
	}

	private class QueryPhoneContactsTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... arg0) {
			Cursor phoneCursor = ContactsApplication
					.getContext()
					.getContentResolver()
					.query(RawContacts.CONTENT_URI,
							null,
							RawContacts.INDICATE_PHONE_SIM
									+ " < 0 AND deleted < 1", null, null);
			int phoneContacts = 0;
			if (phoneCursor != null) {
				Log.i(TAG,
						"[Phone->Sim] Phone Contacts****"
								+ phoneCursor.getCount());
				phoneContacts = phoneCursor.getCount();
				phoneCursor.close();
			}
			return phoneContacts;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			phoneQuerying = true;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (phoneListener != null) {
				phoneListener.onResult(result);
			}
			phoneQuerying = false;
			simListener = null;
			phoneContatcts = null;
		}

	}

	private class QuerySimContactsTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... arg0) {
			Cursor simCursor = ContactsApplication
					.getContext()
					.getContentResolver()
					.query(RawContacts.CONTENT_URI,
							null,
							RawContacts.INDICATE_PHONE_SIM
									+ " >= 0 AND deleted <1", null, null);
			int simContacts = 0;
			if (simCursor != null) {
				Log.i(TAG,
						"[Sim->Phone] SIM Contacts****" + simCursor.getCount());
				simContacts = simCursor.getCount();
				simCursor.close();
			}
			return simContacts;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			simQuerying = true;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (simListener != null) {
				simListener.onResult(result);
			}
			simQuerying = false;
			phoneListener = null;
			simContatcts = null;
		}
	}

	public interface ResultListener {
		void onResult(Integer result);
	}

}
