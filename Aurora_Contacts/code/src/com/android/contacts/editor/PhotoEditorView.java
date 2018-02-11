/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.editor;

import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.util.ContactPhotoUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.DisplayPhoto;
import gionee.provider.GnContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import aurora.widget.AuroraRoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Simple editor for {@link Photo}.
 */
public class PhotoEditorView extends FrameLayout implements Editor {
	private static final String TAG = "PhotoEditorView";

	private AuroraRoundedImageView mPhotoImageView;
	private Button mChangeButton;

	private ValuesDelta mEntry;
	private EditorListener mListener;

	private boolean mHasSetPhoto = false;
	private boolean mReadOnly;
	private Context context;
	private ContactPhotoManager mContactPhotoManager;

	public PhotoEditorView(Context context) {
		super(context);
		this.context=context;
		mContactPhotoManager = ContactPhotoManager.getInstance(context);
	}

	public PhotoEditorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		mContactPhotoManager = ContactPhotoManager.getInstance(context);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.context=context;
	}

	/** {@inheritDoc} */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mPhotoImageView = (AuroraRoundedImageView) findViewById(R.id.photo);
		mChangeButton = (Button) findViewById(R.id.change_button);

		if (null != mChangeButton) {
			mChangeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null) {
						mListener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
					}
				}
			});
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onFieldChanged(String column, String value) {
		throw new UnsupportedOperationException(
				"Photos don't support direct field changes");
	}

	public static long contactId=0;
	/** {@inheritDoc} */
	@Override
	public void setValues(DataKind kind, ValuesDelta values, EntityDelta state,
			boolean readOnly, ViewIdGenerator vig) {
		mEntry = values;
		mReadOnly = readOnly;

		setId(vig.getId(state, kind, values, 0));

		if (values != null) {
			// Try decoding photo if actual entry
			final byte[] photoBytes = values.getAsByteArray(Photo.PHOTO);
			if (photoBytes != null) {
				final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes,
						0, photoBytes.length);
				

				mPhotoImageView.setImageBitmap(photo);// .setImageDrawable(new
														// BitmapDrawable(photo));
				mChangeButton.setEnabled(isEnabled());
				mHasSetPhoto = true;
				mEntry.setFromTemplate(false);

				// if (values.getAfter() == null ||
				// values.getAfter().get(Photo.PHOTO) == null) {
				// // If the user hasn't updated the PHOTO value, then
				// PHOTO_FILE_ID may contain
				// // a reference to a larger version of PHOTO that we can bind
				// to the UI.
				// // Otherwise, we need to wait for a call to
				// #setFullSizedPhoto() to update
				// // our full sized image.
				// final Integer photoFileId =
				// values.getAsInteger(Photo.PHOTO_FILE_ID);
				// if (photoFileId != null) {
				// final Uri photoUri = DisplayPhoto.CONTENT_URI.buildUpon()
				// .appendPath(photoFileId.toString()).build();
				// setFullSizedPhoto(photoUri);
				// }
				// }
				
				final Integer photoFileId =  values.getAsInteger(Photo.PHOTO_FILE_ID);
				if (photoFileId != null) {
					 final Uri photoUri = DisplayPhoto.CONTENT_URI.buildUpon()
							 .appendPath(photoFileId.toString()).build();
						Log.d(TAG, "Loading image photoUri = " + photoUri);
						mContactPhotoManager.loadPhoto(mPhotoImageView, photoUri, false, false);
				}
			} else {
				resetDefault();
			}
		} else {
			resetDefault();
		}
	}

	/**
	 * Return true if a valid {@link Photo} has been set.
	 */
	public boolean hasSetPhoto() {
		return mHasSetPhoto;
	}

	/**
	 * Assign the given {@link Bitmap} as the new value, updating UI and
	 * readying for persisting through {@link ValuesDelta}.
	 */
	public void setPhotoBitmap(Bitmap photo) {
		if (photo == null) {
			// Clear any existing photo and return
			mEntry.put(Photo.PHOTO, (byte[]) null);
			resetDefault();
			return;
		}

		final byte[] compressed = ContactPhotoUtils.compressBitmap(photo);

		if (compressed != null) {
			mEntry.put(Photo.PHOTO, compressed);
		}

		mPhotoImageView.setImageDrawable(new BitmapDrawable(photo));
		mChangeButton.setEnabled(isEnabled());
		mHasSetPhoto = true;
		mEntry.setFromTemplate(false);

		// When the user chooses a new photo mark it as super primary
		setSuperPrimary(true);

	}

	/**
	 * Set the super primary bit on the photo.
	 */
	public void setSuperPrimary(boolean superPrimary) {
		mEntry.put(Photo.IS_SUPER_PRIMARY, superPrimary ? 1 : 0);
	}

	protected void resetDefault() {
		// Invalid photo, show default "add photo" place-holder
		// gionee xuhz 20121207 modify for GIUI2.0 start
		if (mPhotoImageView != null) {
			if (ContactsApplication.sIsAuroraPrivacySupport
					&& AuroraContactEditorFragment.mIsPrivacyContact) {
				mPhotoImageView
						.setImageResource(R.drawable.aurora_privacy_contact_default_header);
			} else if (ContactsApplication.sIsGnDarkStyle) {
				mPhotoImageView
						.setImageResource(R.drawable.ic_contact_picture_holo_dark);
			} else {
				
				if(contactId>0){
					int index=(int) (contactId%(ResConstant.randomContactPhotoId.length));
					Log.d(TAG,"contactId:"+contactId+" index:"+index);
					Resources res = context.getResources();
					if(index<ResConstant.largeRandomContactPhotoId.length){
						Drawable a = res.getDrawable(ResConstant.largeRandomContactPhotoId[index]);
						mPhotoImageView.setImageDrawable(a);
					}else{
						mPhotoImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
					}
				}else{
					mPhotoImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.svg_dial_default_photo1));
				}
			}
		}
		// gionee xuhz 20121207 modify for GIUI2.0 end

		if(mChangeButton != null) {
			mChangeButton.setEnabled(!mReadOnly && isEnabled());
		}
		mHasSetPhoto = false;
		mEntry.setFromTemplate(true);
	}

	/** {@inheritDoc} */
	@Override
	public void setEditorListener(EditorListener listener) {
		mListener = listener;
	}

	@Override
	public void setDeletable(boolean deletable) {
		// Photo is not deletable
	}

	@Override
	public boolean isEmpty() {
		return !mHasSetPhoto;
	}

	@Override
	public void deleteEditor() {
		// Photo is not deletable
	}

	@Override
	public void clearAllFields() {
		resetDefault();
	}

	public View getChangeAnchorView() {
		return mChangeButton;
	}

//	public static Bitmap drawableToBitmap(Drawable drawable) {
//
//		Bitmap bitmap = Bitmap
//				.createBitmap(
//						drawable.getIntrinsicWidth(),
//						drawable.getIntrinsicHeight(),
//						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//								: Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(bitmap);
//		// canvas.setBitmap(bitmap);
//		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
//				drawable.getIntrinsicHeight());
//		drawable.draw(canvas);
//		return bitmap;
//	}
//	
//    
//    //Gionee:huangzy 20130404 add for CR00792022 start
//    private Bitmap gnCenterCropBitmap(Bitmap bitmap, float maxDim) {
//    	if (null == bitmap || maxDim < 0) {
//    		return bitmap;
//    	}
//    	
//    	Bitmap scaledBitmap = null;
//    	
//        int oriWidth = bitmap.getWidth();
//        int oriHeight = bitmap.getHeight();        
//        if (oriWidth != oriHeight) {
//        	int dstWidth = 0;
//        	int x = 0;
//        	int y = 0;
//            // Crop the image to the square at its center.
//            if (oriHeight > oriWidth) {
//            	y = (oriHeight - oriWidth)/2;
//            	dstWidth = oriWidth;
//            } else {
//            	x= (oriWidth - oriHeight)/2;
//            	dstWidth = oriHeight;
//            }
//            
//            scaledBitmap = Bitmap.createBitmap(bitmap, x, y, dstWidth, dstWidth);
//        }
//        
//        if (null == scaledBitmap) {
//        	scaledBitmap = bitmap;
//        }
//        
//        int scaledWidth = scaledBitmap.getWidth();
//        int scaledHeight = scaledBitmap.getHeight();
//        float scaleFactor = maxDim / Math.max(scaledWidth, scaledHeight);
//        if (scaleFactor < 1.0) {
//            // Need to scale or crop the photo.
//            Matrix matrix = new Matrix();
//            matrix.setScale(scaleFactor, scaleFactor);
//            scaledBitmap = Bitmap.createBitmap(
//            		scaledBitmap, 0, 0, scaledWidth, scaledHeight, matrix, false);
//        }
//        return ContactDetailDisplayUtils.toRoundBitmap(scaledBitmap);
//    }
    //Gionee:huangzy 20130404 add for CR00792022 end
    
	//Gionee <xuhz> <2013-07-02> add for CR00832030 begin

	// Gionee <xuhz> <2013-07-02> add for CR00832030 begin
	private final static long maxSupportSize = 800 * 1024;

	private void compressPhoto(Bitmap photo, ByteArrayOutputStream out) {
		if (out == null || photo == null) {
			return;
		}
		long photoSize = out.toByteArray().length;
		if (photoSize > maxSupportSize) {
			int quality = (int) (maxSupportSize * 100 / photoSize);
			if (quality < 0) {
				quality = 0;
			} else if (quality > 100) {
				quality = 100;
			}
			Log.w(TAG, "compressPhoto quality:" + quality);

			out.reset();
			photo.compress(Bitmap.CompressFormat.JPEG, quality, out);
		}
	}
	// Gionee <xuhz> <2013-07-02> add for CR00832030 end
}
