package com.mediatek.contacts.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.contacts.R;
import com.android.contacts.editor.PhotoEditorView;
import com.android.contacts.editor.ViewIdGenerator;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.model.DataKind;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityDelta.ValuesDelta;

public class SimPhotoEditorView extends PhotoEditorView{
    
    private static final String TAG = "SimPhotoEditorView";

    private ImageView mPhotoImageView;
    private View mFrameView;

    private ValuesDelta mEntry;
    private EditorListener mListener;

    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;

    public SimPhotoEditorView(Context context) {
        super(context);
    }

    public SimPhotoEditorView(Context context, AttributeSet attrs) {
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
        mPhotoImageView = (ImageView) findViewById(R.id.sim_photo);
        mFrameView = findViewById(R.id.frame);
        mFrameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
                }
            }
        });
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
        Log.w(TAG,"the values is = "+ values);
        setId(vig.getId(state, kind, values, 0));

        if (values != null) {
            // Try decoding photo if actual entry
            final byte[] photoBytes = values.getAsByteArray(Photo.PHOTO);
            if (photoBytes != null) {
                final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
                        photoBytes.length);
                Log.w(TAG,"the setValues");
                mPhotoImageView.setImageBitmap(photo);
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
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            mEntry.put(Photo.PHOTO, out.toByteArray());
            Log.w(TAG,"the setPhotoBitmap");
            mPhotoImageView.setImageBitmap(photo);
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
        mPhotoImageView.setImageResource(R.drawable.ic_contact_picture_sim_contact);
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
}