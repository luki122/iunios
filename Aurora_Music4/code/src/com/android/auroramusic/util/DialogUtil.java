package com.android.auroramusic.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import aurora.app.AuroraAlertDialog;

import com.android.auroramusic.adapter.AuroraAddSongAdapter;
import com.android.auroramusic.model.Playlist;
import com.android.music.MusicUtils;
import com.android.music.R;

/**
 * modify by chenhl 20140527 添加成功添加到歌单响应接口用于退出编辑模式
 * @author tangjie
 */
public class DialogUtil {
	private static String inputName;
	public static List<String> mSongList;
	private static OnAddPlaylistSuccessListener mListener;
	private static AuroraAlertDialog dialog;
	private static final String TAG ="DialogUtil";

	/*
	 * songList 要添加到歌单的歌曲ID数组
	 */
	public static void showAddDialog(final Context context, final List<String> songList, final OnAddPlaylistSuccessListener listener) {
		mSongList = songList;
		mListener = listener;
		ArrayList<Playlist> list = new ArrayList<Playlist>();
		final AuroraAddSongAdapter mPlaylistAdapter = new AuroraAddSongAdapter(context, list);
		dialog = new AuroraAlertDialog.Builder(context).setAdapter(mPlaylistAdapter, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int position) {
				// TODO Auto-generated method stub
				long pid = mPlaylistAdapter.getItem(position).mPlaylistId;
				if (pid == -1) {
					initCreatePlayList(context);
					dialog.dismiss();// add by chenhl 20140527
				} else {
					MusicUtils.addToPlaylist(context, songList, pid, mPlaylistAdapter.getItem(position).mPlaylistName);
					if (listener != null)
						listener.OnAddPlaylistSuccess();// add by chenhl
														// 20140527
				}
			}
		}).setTitle(context.getResources().getString(R.string.add_to_playlist)).create();
		dialog.show();
		dialog.setCanceledOnTouchOutside(true);// lory add 2014.10.20
		new GetPlayListTask(context, mPlaylistAdapter, list).executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor());
	}

	static class GetPlayListTask extends AsyncTask<String, String, String> {
		private Context mContext;
		private AuroraAddSongAdapter mPlaylistAdapter;
		private ArrayList<Playlist> srcArrayList;
		private ArrayList<Playlist> destArrayList;

		public GetPlayListTask(Context context, AuroraAddSongAdapter adapter, ArrayList<Playlist> list) {
			mContext = context;
			mPlaylistAdapter = adapter;
			srcArrayList = list;
		}

		@Override
		protected String doInBackground(String... params) {

			Cursor cursor = null;
			destArrayList = new ArrayList<Playlist>();
			Playlist frist = new Playlist(-1, mContext.getResources().getString(R.string.new_singer), R.drawable.create_song_list_icon_normal);
			destArrayList.add(frist);
			try {
				cursor = mContext.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Playlists.NAME + " LIKE ?",
						new String[] { Globals.AURORA_PLAYLIST_TIP + "%" }, null);
				if (cursor != null && cursor.moveToFirst()) {
					do {
						int id = cursor.getInt(0);
						String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME));
						name = name.substring(Globals.AURORA_PLAYLIST_TIP.length());
						Playlist info = new Playlist(id, name);
						destArrayList.add(info);
					} while (cursor.moveToNext());
				}
			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			if (mPlaylistAdapter != null) {
				srcArrayList.clear();
				srcArrayList.addAll(destArrayList);
				destArrayList.clear();
				LogUtil.d(TAG, "--------srcArrayList:"+srcArrayList.size());
				mPlaylistAdapter.notifyDataSetChanged();
			}
			super.onPostExecute(result);
		}

	}

	/**
	 * 新建歌单dialog
	 */
	public static void initCreatePlayList(final Context context) {
		TextInputDialog dialog = new TextInputDialog(context, context.getResources().getString(R.string.new_singer), (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
		dialog.show();
	}

	static class MakPlayListTask extends AsyncTask<String, String, String> {
		private Context mContext;
		String name;
		private OnAddPlaylistSuccessListener mOnAddPlaylistSuccessListener;

		public MakPlayListTask(Context context, OnAddPlaylistSuccessListener listener) {

			this.mContext = context;
			this.mOnAddPlaylistSuccessListener = listener;
		}

		@Override
		protected String doInBackground(String... params) {
			name = inputName;
			Uri uri = null;
			if (name != null && name.length() > 0) {
				ContentResolver resolver = mContext.getContentResolver();
				int id = AuroraMusicUtil.idForplaylist(name, mContext);

				if (id >= 0) {
					name = makePlaylistName(mContext, name);
				}
				ContentValues values = new ContentValues(1);
				String name2 = Globals.AURORA_PLAYLIST_TIP + name;
				values.put(MediaStore.Audio.Playlists.NAME, name2);
				uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);

				/*
				 * AuroraMainMenuData info = new AuroraMainMenuData();
				 * info.setName(name);
				 * info.setPlaylistId(Integer.parseInt(uri.getLastPathSegment
				 * ()));
				 */
				// MusicUtils.addToPlaylist(mContext, mSongList,
				// Integer.parseInt(uri.getLastPathSegment()));//add by chenhl
				// 20140527
			}
			return uri == null ? null : uri.getLastPathSegment();
		}

		@Override
		protected void onPostExecute(String result) {
			// showAddDialog(mContext, mSongList);//delete by chenhl 20140527
			if (result != null) {
				LogUtil.d("sssss", "result:" + result);
				if (!TextUtils.isEmpty(name.trim()))
					MusicUtils.addToPlaylist(mContext, mSongList, Long.parseLong(result), name);// add
																								// by
																								// chenhl
																								// 20140527
				if (mOnAddPlaylistSuccessListener != null)
					mOnAddPlaylistSuccessListener.OnAddPlaylistSuccess();
			}
			super.onPostExecute(result);
		}
	}

	private static String makePlaylistName(Context context, String template) {

		String[] cols = new String[] { MediaStore.Audio.Playlists.NAME };
		ContentResolver resolver = context.getContentResolver();
		String whereclause = MediaStore.Audio.Playlists.NAME + " Like ?";
		Cursor c = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cols, whereclause, new String[] { Globals.AURORA_PLAYLIST_TIP + "%" }, MediaStore.Audio.Playlists.NAME);
		if (c == null) {
			return template;
		}
		int num = 1;
		String suggestedname = String.format(template + "(%d)", num++);
		boolean done = false;
		while (!done) {
			done = true;
			c.moveToFirst();
			while (!c.isAfterLast()) {
				String playlistname = c.getString(0);
				if (playlistname.compareToIgnoreCase(Globals.AURORA_PLAYLIST_TIP + suggestedname) == 0) {
					suggestedname = String.format(template + "(%d)", num++);
					done = false;
				}
				c.moveToNext();
			}
		}
		c.close();
		return suggestedname;
	}

	public interface OnAddPlaylistSuccessListener {
		public void OnAddPlaylistSuccess();
	}

	public interface OnDeleteFileListener {
		public void OnDeleteFileSuccess();
	}
	
	public interface OnRemoveFileListener {
		public void OnRemoveFileSuccess();
	}

	static class TextInputDialog extends aurora.app.AuroraAlertDialog {
		protected static final String TAG = "TextInputDialog";
		private String mTitle;
		private Context mContext;
		private View mView;
		private EditText mFolderName;

		private android.widget.Button mDlgBtnDone = null;
		private android.widget.ImageView mDlgBtnClear = null;
		private int FILENAME_MAX_LENGTH = 60;
		private InputMethodManager inputMethodManager;

		public interface OnFinishListener {
			boolean onFinish(String text);
		}

		public TextInputDialog(Context context, String title, InputMethodManager in) {
			super(context);
			mTitle = title;
			mContext = context;
			inputMethodManager = in;
		}

		protected void onCreate(Bundle savedInstanceState) {
			mView = getLayoutInflater().inflate(R.layout.create_playlist_view, null);
			setTitle(mTitle);
			mFolderName = (EditText) mView.findViewById(R.id.aurora_edit_playlist_name);
			mDlgBtnClear = (ImageView) mView.findViewById(R.id.aurora_input_delete);
			mDlgBtnClear.setVisibility(View.GONE);
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			mDlgBtnClear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mFolderName.setText("");
				}
			});
			setCanceledOnTouchOutside(false);// touchu消失
			setView(mView);
			setButton(BUTTON_POSITIVE, mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						inputMethodManager.hideSoftInputFromWindow(TextInputDialog.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (which == BUTTON_POSITIVE) {
						inputName = mFolderName.getText().toString();
						new MakPlayListTask(mContext, mListener).executeOnExecutor(ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor());
					}
				}
			});
			setButton(BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == BUTTON_NEGATIVE) {
						try {
							inputMethodManager.hideSoftInputFromWindow(TextInputDialog.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			});
			super.onCreate(savedInstanceState);
			AuroraMusicUtil.showInputMethod(mContext);
			this.setTextChangedCallbackForInputDlg();
		}

		/**
		 * This method register callback and set filter to Edit, in order to
		 * make sure that user input is legal. The input can't be illegal
		 * filename and can't be too long.
		 */
		public void setTextChangedCallbackForInputDlg() {
			mDlgBtnDone = this.getButton(DialogInterface.BUTTON_POSITIVE);
			mDlgBtnDone.setEnabled(false);
			mFolderName.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable arg0) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (s.toString().length() <= 0 || s.toString().matches(".*[/\\\\:*?\"<>|'].*")) { // characters
																										// not
						mDlgBtnClear.setVisibility(View.GONE); // allowed
						mDlgBtnDone.setEnabled(false);
					} else if (s.toString().trim().length() == 0 || s.toString().trim().length() > FILENAME_MAX_LENGTH) {
						mDlgBtnDone.setEnabled(false);
						mDlgBtnClear.setVisibility(View.GONE);
					} else {
						mDlgBtnClear.setVisibility(View.VISIBLE);
						mDlgBtnDone.setEnabled(true);
					}
				}
			});
		}
	}

	public static void notifyListener() {
		if (mListener != null)
			mListener.OnAddPlaylistSuccess();
	}

	public static void dismissDialog() {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
}
