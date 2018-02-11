package com.aurora.community.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import com.aurora.community.R;
import com.aurora.community.activity.picBrowser.ImagePickerActvity;
import com.aurora.community.adapter.ImageAlbumAdapter;
import com.aurora.community.bean.AlbumInfo;
import com.aurora.community.bean.PhotoInfo;
import com.aurora.community.bean.PhotoSerializable;



import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ImageAlbumFragment extends BaseFragment{

	private ArrayList<AlbumInfo> mAlbumList;
	
	private ListView albumListView;
	
	private ImageAlbumAdapter adapter;
	
	public interface ImageAlbumItemController{
		public void onItemClick(ArrayList<PhotoInfo> list,String str);
	}
	
	private ImageAlbumItemController mDelegate;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		mDelegate = (ImagePickerActvity) activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		
		PhotoSerializable photoSerializable = (PhotoSerializable) args.getSerializable("list");
		mAlbumList = photoSerializable.getAlbumList();
		setupAlbumList();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return  inflater
				.inflate(R.layout.image_album_fragment, container, false);
	}
	
	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		albumListView = (ListView)getView().findViewById(R.id.lv_album);
	}
	
	private void setupAlbumList(){
		adapter = new ImageAlbumAdapter(mAlbumList, getActivity().getBaseContext(),albumListView);
		albumListView.setAdapter(adapter);
		//adapter.refreshView(0);
	
		albumListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
					AlbumInfo info  = mAlbumList.get(arg2);
				//	info.setSelected(true);
			         Log.e("linp", "~~~~~~~~~~~~~~~mAlbumList.size="+mAlbumList.size());
					onAlbumItemClick(info.getList(),info.getName_album());
					adapter.refreshView(arg2);
			}
		});
	}

	private void onAlbumItemClick( ArrayList<PhotoInfo> l,String str){
		if(mDelegate!=null){
			mDelegate.onItemClick(l,str);
		}
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}
	
//	@Override
//	public void onHiddenChanged(boolean hidden) {
//		// TODO Auto-generated method stub
//		super.onHiddenChanged(hidden);
//		if(!hidden){
//			if(adapter!=null)
//				adapter.notifyDataSetChanged();	
//		}
//	}
}
