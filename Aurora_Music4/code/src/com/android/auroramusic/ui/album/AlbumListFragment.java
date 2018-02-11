package com.android.auroramusic.ui.album;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.ListFragment;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

import com.android.auroramusic.adapter.AuroraAlbumListAdapter;
import com.android.auroramusic.adapter.AuroraPlayerListViewAdapter.ViewHolder;
import com.android.auroramusic.util.AuroraAlbum;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.DataConvertUtil;
import com.android.auroramusic.util.DialogUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.DialogUtil.OnAddPlaylistSuccessListener;
import com.android.auroramusic.util.LogUtil;
import com.android.music.MusicUtils;
import com.android.music.R;

/**
 * A list fragment representing a list of Tracks. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link AlbumDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
@SuppressLint("NewApi")
public class AlbumListFragment extends ListFragment {

	private AlbumListActivity mActivity;
	private AlbumQueryHandler mAlbumQueryHandler;
	private int selectedNumAlbum = 0;

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};
	
	private DialogUtil.OnAddPlaylistSuccessListener mAddPlaylistSuccessListener = new OnAddPlaylistSuccessListener() {

		@Override
		public void OnAddPlaylistSuccess() {
			quitEditMode();
		}
	};

	// Data
	ArrayList<AuroraAlbum> albums = new ArrayList<AuroraAlbum>();
	AuroraListView mListView;
	AuroraAlbumListAdapter mAdapter;
	AuroraAlbum selectedAlbum;

	class AlbumQueryHandler extends AsyncQueryHandler {

		AlbumQueryHandler(ContentResolver res) {
			super(res);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// Log.i("@@@", "query complete");
			if (cursor != null)
				init(cursor);
		}
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public AlbumListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAlbumQueryHandler = new AlbumQueryHandler(getActivity()
				.getContentResolver());
		getQueryCursor(mAlbumQueryHandler, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.album_list_content,
				container, false);
		mListView = (AuroraListView) rootView.findViewById(android.R.id.list);
		mListView.setSelector(R.drawable.aurora_playlist_item_clicked);
		return rootView;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		super.onViewCreated(view, savedInstanceState);

		if (null != mListView) {
			
			mListView.setVisibility(View.GONE);
			mListView.auroraSetNeedSlideDelete(true);
			mListView.auroraSetSelectorToContentBg(false);
			mListView.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {
				
				@Override
				public void auroraPrepareDraged(int arg0) {	
				}
				
				@Override
				public void auroraOnClick(int position) {
					AuroraAlbum mAlbum = albums.get(position);
					int title = R.string.delete;
					String message = getResources().getString(
							R.string.delete_album_desc, mAlbum.getAlbumName());
					AuroraAlertDialog mDeleteConDialog = new AuroraAlertDialog.Builder(
							mActivity,
							AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
							.setTitle(title)
							.setMessage(message)
							.setNegativeButton(android.R.string.cancel,
									null)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mAdapter.setDeleteAction(true);
											mListView.auroraDeleteSelectedItemAnim();
											mListView.auroraSetRubbishBack();
										}

									}).create();
					mDeleteConDialog.show();
					
				}
				
				@Override
				public void auroraDragedUnSuccess(int arg0) {
				}
				
				@Override
				public void auroraDragedSuccess(int arg0) {
				}
			});
			
			mListView.auroraSetDeleteItemListener(new AuroraDeleteItemListener() {
				
				@Override
				public void auroraDeleteItem(View v, int position) {
					AuroraAlbum mAlbum = albums.get(position);
					long aid = mAlbum.getAlbumId();
					long singerid = Long.parseLong(mActivity.getArtistId());
					long[] list = MusicUtils.getSongListForAuroraAlbum(mActivity, aid, singerid);
					MusicUtils.deleteTracks(mActivity, list);
					albums.remove(position);
					mAdapter.notifyDataSetChanged();
					mActivity.updateHeader(mActivity.getNumAlbums() - 1, mActivity.getNumTracks() - mAlbum.getTrackNumber());
					isNoAlbums();
				}
			});
			
			//长按事件
			mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (!mAdapter.isEditMode()) {
						
						enterEditMode();
						mAdapter.setNeedin(position);
						return true;
					}
					return false;
				}
				
			});
			//以下设置list各种属性
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;

		if (activity instanceof AlbumListActivity) {
			mActivity = (AlbumListActivity) activity;
		} else {
			throw new IllegalStateException(
					"Activity must extend AlbumListActivity.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
//		super.onListItemClick(listView, view, position, id);

		Object ob = mListView.getAdapter().getItem(position);
		if (ob instanceof AuroraAlbum) {
			
			AuroraAlbum album = (AuroraAlbum) ob;
			if (!mAdapter.isEditMode()) {
				Intent detailIntent = new Intent(mActivity, AlbumDetailActivity.class);
				Bundle bl = new Bundle();
				bl.putParcelable(Globals.KEY_ALBUM_ITEM, album);
				detailIntent.putExtra(Globals.KEY_ARTIST_ID, mActivity.getArtistId());
				detailIntent.putExtra(Globals.KEY_ARTIST_NAME, mActivity.getArtistName());
				detailIntent.putExtras(bl);
				startActivityForResult(detailIntent, Globals.REQUEST_CODE_BROWSER);
				
			} else {
				AuroraCheckBox checkBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				if (null != checkBox) {
					if (checkBox.isChecked()) {
						checkBox.auroraSetChecked(false, true);
						mAdapter.setCheckedArrayValue(position, false);
						mActivity.changeMenuState();
					} else {
						checkBox.auroraSetChecked(true, true);
						mAdapter.setCheckedArrayValue(position, true);
						mActivity.changeMenuState();
					}
				}
			}
			
		}
		
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
//		mCallbacks.onItemSelected(String.valueOf(albums.get(position).getAlbumId()));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void init(Cursor cursor) {
		if (cursor == null) {
			return;
		}
		
		albums = DataConvertUtil.ConvertToAlbum(cursor);
		int numOfAlbum = albums == null? 0 : albums.size();
		if (numOfAlbum == 0) {
			mActivity.updateHeader(0, 0);
			mActivity.finish();
		} else {
			Log.e("liumx", "albums.size()----------------" + albums.size());
			mActivity.updateHeader(numOfAlbum, getTotalTracks());
			mAdapter = new AuroraAlbumListAdapter(getActivity(), albums);
			setListAdapter(mAdapter);
			mListView.setVisibility(View.VISIBLE);
//			if (albums.size() < 4) {
//				Intent detailIntent = new Intent(mActivity, AlbumDetailActivity.class);
//				detailIntent.putExtra(Globals.KEY_ARTIST_ID, mActivity.getArtistId());
//				detailIntent.putExtra(Globals.KEY_ARTIST_NAME, mActivity.getArtistName());
//				startActivityForResult(detailIntent, Globals.REQUEST_CODE_ALBUM_TRACK_BROWSER);
//				mActivity.finish();
//			}
		}
	}
	
	public int getTotalTracks() {
		int count = 0;
		for (int i = 0; i < albums.size(); i++) {
			count += albums.get(i).getTrackNumber();
		}
		return count;
	}

	private Cursor getQueryCursor(AsyncQueryHandler async, String filter) {

		String id_row = DataConvertUtil.TRACK_ALBUM_ID + " AS "
				+ DataConvertUtil.ALBUM_ID;
		String albumdate_row = "MAX(" + DataConvertUtil.TRACK_YEAR + ") AS "
				+ DataConvertUtil.ALBUM_RELEASE_DATE;
		String numsongs_row = "count(*) AS "
				+ DataConvertUtil.ALBUM_TRACK_NUMBER;
		String[] cols = new String[] { id_row,
				DataConvertUtil.TRACK_ALBUM_NAME,
				DataConvertUtil.TRACK_ARTIST_NAME, numsongs_row, albumdate_row ,DataConvertUtil.TRACK_DATA};//fix bug 17671
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		StringBuilder where = new StringBuilder();

		Cursor ret = null;
		if (mActivity.getArtistId() != null) {
			// Uri uri =
			// MediaStore.Audio.Artists.Albums.getContentUri("external",
			// Long.valueOf(mActivity.getArtistId()));
			where.append(Globals.QUERY_SONG_FILTER+AuroraMusicUtil.getFileString(mActivity) + " AND "
					+ MediaStore.Audio.Media.ARTIST_ID + "="
					+ mActivity.getArtistId() + ") GROUP BY ("
					+ DataConvertUtil.TRACK_ALBUM_ID);

			if (!TextUtils.isEmpty(filter)) {
				uri = uri.buildUpon()
						.appendQueryParameter("filter", Uri.encode(filter))
						.build();
			}
			if (async != null) {
				async.startQuery(0, null, uri, cols, where.toString(), null,
						DataConvertUtil.ALBUM_RELEASE_DATE + " desc, "+ MediaStore.Audio.Media.ALBUM_KEY);
			} else {
				ret = MusicUtils.query(mActivity, uri, cols, where.toString(),
						null, DataConvertUtil.ALBUM_RELEASE_DATE + " desc, "+ MediaStore.Audio.Media.ALBUM_KEY);
			}
		} else {
			where.append(Globals.QUERY_SONG_FILTER+AuroraMusicUtil.getFileString(mActivity) + ") GROUP BY ("
					+ DataConvertUtil.TRACK_ALBUM_ID);
			// Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
			if (!TextUtils.isEmpty(filter)) {
				uri = uri.buildUpon()
						.appendQueryParameter("filter", Uri.encode(filter))
						.build();
			}
			if (async != null) {
				async.startQuery(0, null, uri, cols, where.toString(), null,
						DataConvertUtil.ALBUM_RELEASE_DATE + " desc, "+ MediaStore.Audio.Media.ALBUM_KEY);
			} else {
				ret = MusicUtils.query(mActivity, uri, cols, where.toString(),
						null, DataConvertUtil.ALBUM_RELEASE_DATE + " desc, "+ MediaStore.Audio.Media.ALBUM_KEY);
			}
		}
		return ret;
	}

	@Override
	public void onPause() {
		mListView.auroraOnPause();
//		if (mAdapter != null) {
//			mAdapter.imageCachePause();
//		}
		super.onPause();
	}

	@Override
	public void onResume() {
		mListView.auroraOnResume();
		if (mAdapter != null) {
//			mAdapter.imageCacheResume();
			mAdapter.notifyDataSetChanged();
		}
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
//		if (mAdapter != null) {
//			mAdapter.imageCacheDestroy();
//		}
		super.onDestroy();
	}
	
	public void enterEditMode() {
		if (!mActivity.getAuroraActionBar().auroraIsEntryEditModeAnimRunning()) {
			mActivity.getAuroraActionBar().setShowBottomBarMenu(true);
			mActivity.getAuroraActionBar().showActionBarDashBoard();
		}
		mActivity.getIvPlayAll().setEnabled(false);
		mListView.auroraSetNeedSlideDelete(false);
		mListView.auroraEnableSelector(false);
		mAdapter.setEditMode(true);
		mListView.setSelector(android.R.color.transparent);
	}
	
	public void  quitEditMode() {
		mActivity.setPlayAnimation();
		if (!mActivity.getAuroraActionBar().auroraIsExitEditModeAnimRunning()) {
			mActivity.getAuroraActionBar().setShowBottomBarMenu(false);
			mActivity.getAuroraActionBar().showActionBarDashBoard();
		}
		((TextView) mActivity.btn_selectAll).setText(mActivity.selectAll);
		mActivity.getIvPlayAll().setEnabled(true);
		getAdapter().setEditMode(false);
		getAdapter().notifyDataSetChanged();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mActivity.setPlayAnimation();
			}
		}, 500);
//		mAuroraHandler.sendEmptyMessageDelayed(MSG_SHOW_ANIMATION, 500);
		mListView.auroraSetNeedSlideDelete(true);
		mListView.auroraEnableSelector(true);
		mListView.setSelector(R.drawable.aurora_playlist_item_clicked);
	}
	
	public AuroraAlbumListAdapter getAdapter() {
		return mAdapter;
	}

	public AuroraListView getAuroraListView() {
		return mListView;
	}

	public ArrayList<Long> getAlbumsCheckedId() {
		selectedNumAlbum = 0;

		ArrayList<Long> checkedIds = new ArrayList<Long>();
		int numAlbum = mAdapter.getCount();
		for (int index = 0 ; index < numAlbum; index ++) {
			Log.e("liumx","index:"+index+"---"+mAdapter.getCheckedArrayValue(index));
			if (mAdapter.getCheckedArrayValue(index)) {
				AuroraAlbum album = (AuroraAlbum) mAdapter.getItem(index);
				selectedNumAlbum ++;
				long mCurrentAlbumId = album.getAlbumId();
				Log.e("liumx","position:"+index+"--------mselectedeAlbumId :"+"-----"+mCurrentAlbumId);
				checkedIds.add(mCurrentAlbumId);
			}
		}
		return checkedIds;
	} 
	
	public boolean deleteAlbums() {
		AuroraAlertDialog mDeleteDialog = new AuroraAlertDialog.Builder(mActivity,
				AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
				.setTitle(R.string.delete)
				.setMessage(R.string.dialog_delete_albums_con_message)
				.setNegativeButton(android.R.string.cancel,
						null)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							    realRemoveAlbums();
							    quitEditMode();
								mAdapter.notifyDataSetChanged();
								isNoAlbums();

							}
						}).create();//modify by tangjie 2014/08/25
		mDeleteDialog.show();
		return true;
	}
	
	private void realRemoveAlbums() {
		long aid = Long.parseLong(mActivity.getArtistId());
		long[] list = MusicUtils.getSongListForAuroraAlbums(
				mActivity, getAlbumsCheckedId(), aid);
		MusicUtils.deleteTracks(mActivity, list);
		mActivity.updateHeader(mActivity.getNumAlbums() - selectedNumAlbum, mActivity.getNumTracks() - list.length);
		getQueryCursor(mAlbumQueryHandler, null);
	}
	
	public boolean addAlbumsToPlaylist() {
		long aid = Long.parseLong(mActivity.getArtistId());
		long[] idList = MusicUtils.getSongListForAuroraAlbums(
				mActivity, getAlbumsCheckedId(), aid);
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < idList.length; i++) {
			list.add(String.valueOf(idList[i]));
		}
		DialogUtil.showAddDialog(mActivity,
				list, mAddPlaylistSuccessListener);
		return true;
	}
	
	public void isNoAlbums() {
		mActivity.setResult(Globals.RESULT_CODE_MODIFY);
		if (albums.isEmpty()) {
			mActivity.setResult(Globals.RESULT_CODE_MODIFY);
			mActivity.finish();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Globals.RESULT_CODE_MODIFY) {
			getQueryCursor(mAlbumQueryHandler, null);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void reloadData() {
		getQueryCursor(mAlbumQueryHandler, null);
	}
}
