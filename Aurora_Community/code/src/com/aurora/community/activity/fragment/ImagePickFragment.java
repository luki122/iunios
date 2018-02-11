package com.aurora.community.activity.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.aurora.community.R;
import com.aurora.community.activity.picBrowser.ImagePickerActvity;
import com.aurora.community.activity.picBrowser.ImagePreviewActivity;
import com.aurora.community.adapter.ImageAlbumAdapter;
import com.aurora.community.adapter.ImagePickerPhotoAdapter;
import com.aurora.community.bean.AlbumInfo;
import com.aurora.community.bean.GalleryHolder;
import com.aurora.community.bean.PhotoInfo;
import com.aurora.community.bean.PhotoSerializable;
import com.aurora.community.common.MessageHandler;
import com.aurora.community.utils.ThumbnailsUtil;
import com.aurora.datauiapi.data.CommunityManager;
import com.aurora.datauiapi.data.bean.NewsInfoHolder;
import com.aurora.datauiapi.data.implement.Command;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.datauiapi.data.interf.INotifiableController;
import com.aurora.datauiapi.data.interf.INotifiableManager;



public class ImagePickFragment extends BaseFragment implements  MessageHandler,
INotifiableController{

	private GridView mGridPhoto;
	
	public ImagePickerPhotoAdapter adapter;
	
	private ArrayList<AlbumInfo> albumList = new ArrayList<AlbumInfo>();
	
	private  ArrayList<PhotoInfo> mPhotoList = new ArrayList<PhotoInfo>();
	
	/**ablum list in order to save each album item*/
	private  ArrayList<PhotoInfo> mAblumItemList = new ArrayList<PhotoInfo>();
	/**selected list is save user choosed item*/
	private ArrayList<PhotoInfo> mSelectedList = new ArrayList<PhotoInfo>();
	
	private int hasSelect = 1;
	
	private int currentPosition;
	
	private ContentResolver cr ;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mComanager = new CommunityManager(this);
		cr  = getActivity().getContentResolver();
		
		getGalleryInfo();
		
		Log.e("linp", "~~~~~~~~~~~~~~~~~~~~~ImagePickerFragment onActivityCreated");

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return  inflater
				.inflate(R.layout.image_picker_photo_fragment, container, false);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		Log.e("linp", "onHiddenChanged");
		super.onHiddenChanged(hidden);
		if(!hidden){
			if(adapter!=null){
				List<PhotoInfo> tmpList = getAblumItemPhotoList();
				if(tmpList.size()!=0){
					mPhotoList.clear();
					mPhotoList.addAll(tmpList);
				}
				adapter.notifyDataSetChanged();
			}
				
		}
		
			
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		mGridPhoto = (GridView)getView().findViewById(R.id.gv_photo);
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager,
			Command<?> source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager,
			Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		// TODO Auto-generated method stub
		mHandler.post(response);
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}
	
	private void getGalleryInfo(){
//		mComanager.getGalleryInfo(new DataResponse<GalleryHolder>() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				Log.e("linp", "~~~~~~~~~~value.getPhotoList()="+value.getPhotoList().size());
//				Log.e("linp", "~~~~~~~~~~value.getPhotoList()="+value.getAlbumList().size());
//				if(isAdded()){
//					mPhotoList = value.getPhotoList();
//					setAlbumList(value.getAlbumList());
//					if (adapter != null) {
//						adapter.notifyDataSetChanged();
//					} else {
//						setupPhotoListView(mPhotoList);
//					}
//				}else{
//					Log.e("linp", "ImagePickFragment in getGalleryInfo will not attach Activity");
//				}
//			}
//		}, getActivity().getBaseContext());
		new ImageAsyncTask().execute();
	};
	
	private void setupPhotoListView( final ArrayList<PhotoInfo> list){
		adapter = new ImagePickerPhotoAdapter(getActivity(), list,mGridPhoto,((ImagePickerActvity)getActivity()).getCurrentLeft());
		mGridPhoto.setAdapter(adapter);
		mGridPhoto.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				PhotoSerializable photoSerializable = new PhotoSerializable();
				Bundle args = new Bundle();
				photoSerializable.setList(getPhotoList());
				args.putSerializable("list", photoSerializable);
				
				args.putInt("current", position);
				args.putInt("left", ((ImagePickerActvity)getActivity()).getCurrentLeft());
				Intent intent = new Intent();
				intent.setClass(getActivity(), ImagePreviewActivity.class);
				intent.putExtras(args);
				startActivity(intent);
				//getActivity().finish();
				//setCurrentClickPosition(position);
				//adapter.refreshView(position);

			}
		});
		
	}
	
	public void setAlbumList(ArrayList<AlbumInfo> list){
			this.albumList = list;
			//expand album list. contains all list
			ArrayList<PhotoInfo> tmpList = new ArrayList<PhotoInfo>();
			tmpList.clear();
			tmpList.addAll(mPhotoList);
			
			AlbumInfo item  =list.get(0);
			AlbumInfo info = new AlbumInfo();
			info.setImage_id(item.getImage_id());
			info.setList(tmpList);
			info.setName_album(getString(R.string.image_picker_actionbar_title));
			info.setPath_absolute(item.getPath_absolute());
			info.setPath_file(item.getPath_file());
			info.setSelected(true);
			this.albumList.add(0, info);
	}
	
	public ArrayList<AlbumInfo> getAlbumList(){
		return this.albumList;
	}
	
	public void setAblumItemPhotoList(ArrayList<PhotoInfo> p){
		this.mAblumItemList = p;
	}
	
	public ArrayList<PhotoInfo> getAblumItemPhotoList(){
		return this.mAblumItemList;
	}
	
	public ArrayList<PhotoInfo> getPhotoList(){
		return this.mPhotoList;
	}

	public ArrayList<PhotoInfo> getSelectedList(){
			return adapter.getSelectedList();
	}

	public void setCurrentClickPosition(int p){
		currentPosition =  p;
	}
	
	public int getCurrentClickPosition(){
		return this.currentPosition;
	}
	

	private class ImageAsyncTask extends AsyncTask<Void, Void, Object>{

		@Override
		protected Object doInBackground(Void... params) {
			
			ThumbnailsUtil.clear();
			mPhotoList.clear();
			String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
					Thumbnails.DATA };
			Cursor cur = cr.query(Thumbnails.EXTERNAL_CONTENT_URI,
					projection, null, null, null);

			if (cur != null && cur.moveToFirst()) {
				int image_id;
				String image_path;
				int image_idColumn = cur
						.getColumnIndex(Thumbnails.IMAGE_ID);
				int dataColumn = cur.getColumnIndex(Thumbnails.DATA);
				do {
					image_id = cur.getInt(image_idColumn);
					image_path = cur.getString(dataColumn);
					ThumbnailsUtil.put(image_id, "file://" + image_path);
				} while (cur.moveToNext());
			}

			Cursor cursor = cr.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
					null, null, "date_modified DESC");

			String _path = "_data";
			String _album = "bucket_display_name";

			HashMap<String, AlbumInfo> myhash = new HashMap<String, AlbumInfo>();
			AlbumInfo albumInfo = null;
			PhotoInfo photoInfo = null;
			if (cursor != null && cursor.moveToFirst()) {
				do {
					int index = 0;
					int _id = cursor.getInt(cursor.getColumnIndex("_id"));
					String path = cursor.getString(cursor
							.getColumnIndex(_path));
					String album = cursor.getString(cursor
							.getColumnIndex(_album));
					ArrayList<PhotoInfo> stringList = new ArrayList<PhotoInfo>();
					photoInfo = new PhotoInfo();
					if (myhash.containsKey(album)) {
						albumInfo = myhash.remove(album);
						if (albumList.contains(albumInfo))
							index = albumList.indexOf(albumInfo);
						photoInfo.setImage_id(_id);
						photoInfo.setPath_file("file://" + path);
						photoInfo.setPath_absolute(path);
						albumInfo.getList().add(photoInfo);
						mPhotoList.add(photoInfo);
						albumList.set(index, albumInfo);
						myhash.put(album, albumInfo);
					} else {
						albumInfo = new AlbumInfo();
						stringList.clear();
						photoInfo.setImage_id(_id);
						photoInfo.setPath_file("file://" + path);
						photoInfo.setPath_absolute(path);
						stringList.add(photoInfo);
						mPhotoList.add(photoInfo);
						albumInfo.setImage_id(_id);
						albumInfo.setPath_file("file://" + path);
						albumInfo.setPath_absolute(path);
						albumInfo.setName_album(album);
						albumInfo.setList(stringList);
						albumList.add(albumInfo);
						myhash.put(album, albumInfo);
					}
				}while (cursor.moveToNext());

			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(isAdded()){
				
				setAlbumList(albumList);
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				} else {
					setupPhotoListView(mPhotoList);
				}
				for(PhotoInfo info : mPhotoList){
					Log.e("linp", "#############info="+info.getPath_absolute()+";"+"info path="+info.getPath_file());
				}
			}else{
				Log.e("linp", "ImagePickFragment in getGalleryInfo will not attach Activity");
			}
		}
	} 
	
	
}
