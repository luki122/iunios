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

import com.android.contacts.ContactsApplication;
import com.android.contacts.R;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import gionee.provider.GnContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.QuickContactBadge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Simple editor for {@link Photo}.
 */
public class PhotoEditorView extends FrameLayout implements Editor {
    private static final String TAG = "PhotoEditorView";

    private ImageView mPhotoImageView;
    
    private View mFrameView;

    private ValuesDelta mEntry;
    private EditorListener mListener;

    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;
    
    public PhotoEditorView(Context context) {
        super(context);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mFrameView.setEnabled(enabled);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPhotoImageView = (ImageView) findViewById(R.id.photo);
        mFrameView = findViewById(R.id.frame);

        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
        	mFrameView.setVisibility(View.GONE);
        	
        	if (null != mPhotoImageView) {
        		mFrameView = mPhotoImageView;	
        	}
        }
        
        if (null != mFrameView) {
        	mFrameView.setOnClickListener(new OnClickListener() {
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
        throw new UnsupportedOperationException("Photos don't support direct field changes");
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(DataKind kind, ValuesDelta values, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        mEntry = values;
        mReadOnly = readOnly;

        setId(vig.getId(state, kind, values, 0));

        if (values != null) {
            // Try decoding photo if actual entry
            final byte[] photoBytes = values.getAsByteArray(Photo.PHOTO);
            if (photoBytes != null) {
                final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
                        photoBytes.length);

                //Gionee:huangzy 20130404 add for CR00792022 start
                Bitmap tmp = gnCenterCropBitmap(photo, 120);
                //Gionee:huangzy 20130404 add for CR00792022 end
                mPhotoImageView.setImageDrawable(new BitmapDrawable(tmp));
                mFrameView.setEnabled(isEnabled());
                mHasSetPhoto = true;
                mEntry.setFromTemplate(false);
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
            mEntry.put(Photo.PHOTO, (byte[])null);
            resetDefault();
            return;
        }

        final int size = photo.getWidth() * photo.getHeight() * 4;
        final ByteArrayOutputStream out = new ByteArrayOutputStream(size);

        try {
        	//Gionee:huangzy 20130322 modify for CR00786613 start
            /*photo.compress(Bitmap.CompressFormat.PNG, 100, out);*/
            photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
        	//Gionee:huangzy 20130322 modify for CR00786613 end
            
        	//Gionee <xuhz> <2013-07-02> add for CR00832030 begin
            compressPhoto(photo, out);
            //Gionee <xuhz> <2013-07-02> add for CR00832030 end
            
            out.flush();
            out.close();

            mEntry.put(Photo.PHOTO, out.toByteArray());
            //Gionee:huangzy 20130404 add for CR00792022 start
            Bitmap tmp = gnCenterCropBitmap(photo, 120);
            //Gionee:huangzy 20130404 add for CR00792022 end
            mPhotoImageView.setImageDrawable(new BitmapDrawable(tmp));
            mFrameView.setEnabled(isEnabled());
            mHasSetPhoto = true;
            mEntry.setFromTemplate(false);

            // When the user chooses a new photo mark it as super primary
            mEntry.put(Photo.IS_SUPER_PRIMARY, 1);
        } catch (IOException e) {
            Log.w(TAG, "Unable to serialize photo: " + e.toString());
        }
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
        	if (ContactsApplication.sIsAuroraPrivacySupport && AuroraContactEditorFragment.mIsPrivacyContact) {
        		mPhotoImageView.setImageResource(R.drawable.aurora_privacy_contact_default_header);
        	} else if (ContactsApplication.sIsGnDarkStyle) {
                mPhotoImageView.setImageResource(R.drawable.ic_contact_picture_holo_dark);
            } else {
                mPhotoImageView.setImageResource(R.drawable.aurora_ic_contact_picture);
            }
        }
        // gionee xuhz 20121207 modify for GIUI2.0 end

        mFrameView.setEnabled(!mReadOnly && isEnabled());
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
    
    //Gionee:huangzy 20130404 add for CR00792022 start
    private Bitmap gnCenterCropBitmap(Bitmap bitmap, float maxDim) {
    	if (null == bitmap || maxDim < 0) {
    		return bitmap;
    	}
    	
    	Bitmap scaledBitmap = null;
    	
        int oriWidth = bitmap.getWidth();
        int oriHeight = bitmap.getHeight();        
        if (oriWidth != oriHeight) {
        	int dstWidth = 0;
        	int x = 0;
        	int y = 0;
            // Crop the image to the square at its center.
            if (oriHeight > oriWidth) {
            	y = (oriHeight - oriWidth)/2;
            	dstWidth = oriWidth;
            } else {
            	x= (oriWidth - oriHeight)/2;
            	dstWidth = oriHeight;
            }
            
            scaledBitmap = Bitmap.createBitmap(bitmap, x, y, dstWidth, dstWidth);
        }
        
        if (null == scaledBitmap) {
        	scaledBitmap = bitmap;
        }
        
        int scaledWidth = scaledBitmap.getWidth();
        int scaledHeight = scaledBitmap.getHeight();
        float scaleFactor = maxDim / Math.max(scaledWidth, scaledHeight);
        if (scaleFactor < 1.0) {
            // Need to scale or crop the photo.
            Matrix matrix = new Matrix();
            matrix.setScale(scaleFactor, scaleFactor);
            scaledBitmap = Bitmap.createBitmap(
            		scaledBitmap, 0, 0, scaledWidth, scaledHeight, matrix, false);
        }
        return ContactDetailDisplayUtils.toRoundBitmap(scaledBitmap);
    }
    //Gionee:huangzy 20130404 add for CR00792022 end
    
	//Gionee <xuhz> <2013-07-02> add for CR00832030 begin
    private final static long maxSupportSize = 800 * 1024;
    private void compressPhoto(Bitmap photo, ByteArrayOutputStream out) {
    	if (out == null || photo == null) {
    		return;
    	}
    	long photoSize = out.toByteArray().length;
    	if (photoSize > maxSupportSize) {
    		int quality = (int)(maxSupportSize * 100 / photoSize);
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
    //Gionee <xuhz> <2013-07-02> add for CR00832030 end
}
