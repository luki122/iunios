package com.android.gallery3d.local.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;

public class GalleryAlbumItemView {

	private View rootView;
	private TextView local_albums_name;
	private ImageView first_img, second_img, third_img, fourth_img,checkbox_ok, checkbox_no;
	private LinearLayout local_albums;

	public GalleryAlbumItemView(View rootView) {
		super();
		this.rootView = rootView;
	}

	public TextView getLocalAlbumsName() {
		if(rootView==null){
			return null;
		}
		if(local_albums_name!=null){
			return local_albums_name;
		}
		
		return (TextView)rootView.findViewById(R.id.local_albums_name);
	}
	
	public TextView getLocalAlbumsNum() {
		if(rootView==null){
			return null;
		}
		if(local_albums_name!=null){
			return local_albums_name;
		}
		
		return (TextView)rootView.findViewById(R.id.local_albums_num);
	}

	
	public void initImageView(){
		if(rootView==null){
			return;
		}
		getFirstImg().setImageBitmap(null);
		getSecondImg().setImageBitmap(null);
		getThirdImg().setImageBitmap(null);
		getFourthImg().setImageBitmap(null);
	}

	public ImageView getFirstImg() {
		if(rootView==null){
			return null;
		}
		if(first_img!=null){
			return first_img;
		}
		return (ImageView)rootView.findViewById(R.id.first_img);
	}

	public ImageView getSecondImg() {
		if(rootView==null){
			return null;
		}
		if(second_img!=null){
			return second_img;
		}
		return (ImageView)rootView.findViewById(R.id.second_img);
	}

	public ImageView getThirdImg() {
		if(rootView==null){
			return null;
		}
		if(third_img!=null){
			return third_img;
		}
		return (ImageView)rootView.findViewById(R.id.third_img);
	}

	public ImageView getFourthImg() {
		if(rootView==null){
			return null;
		}
		if(fourth_img!=null){
			return fourth_img;
		}
		return (ImageView)rootView.findViewById(R.id.fourth_img);
	}

	public LinearLayout getLocal_albums() {
		if(rootView==null){
			return null;
		}
		if(local_albums!=null){
			return local_albums;
		}
		return (LinearLayout)rootView.findViewById(R.id.local_albums);
	}
	
	public ImageView getCheckboxOk() {
		if (checkbox_ok == null) {
			checkbox_ok = (ImageView) rootView.findViewById(R.id.checkbox_ok);
		}
		return checkbox_ok;
	}

	public ImageView getCheckboxNo() {
		if (checkbox_no == null) {
			checkbox_no = (ImageView) rootView.findViewById(R.id.checkbox_no);
		}
		return checkbox_no;
	}

	
	
}
