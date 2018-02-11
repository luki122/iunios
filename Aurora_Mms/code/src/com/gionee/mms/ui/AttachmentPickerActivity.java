/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */

package com.gionee.mms.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import aurora.app.AuroraListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import aurora.widget.AuroraButton;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mms.R;
//Aurora xuyong 2013-11-15 modified for google adapt start
import com.aurora.android.mms.ContentType;
import com.aurora.android.mms.MmsException;
import com.aurora.android.mms.pdu.GenericPdu;
import com.aurora.android.mms.pdu.MultimediaMessagePdu;
import com.aurora.android.mms.pdu.PduBody;
import com.aurora.android.mms.pdu.PduHeaders;
import com.aurora.android.mms.pdu.PduPart;
import com.aurora.android.mms.pdu.PduPersister;
//Aurora xuyong 2013-11-15 modified for google adapt end
//gionee zhouyj 2012-05-03 added for CR00588621 start
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
//gionee zhouyj 2012-05-03 added for CR00588621 end
// gionee zhouyj 2012-05-29 add for CR00601178 start
import android.widget.PopupMenu;

import com.android.mms.ui.ConversationListAdapter;
import com.gionee.mms.ui.CustomMenu.DropDownMenu;
import com.android.mms.MmsApp;
// gionee zhouyj 2012-05-29 add for CR00601178 end
// gionee zhouyj 2012-06-16 add for CR00624438 start 
import com.aurora.featureoption.FeatureOption;
// gionee zhouyj 2012-06-16 add for CR00624438 end 
/**
 * show all the attachment of one mms, user can select some of them to save.
 */
public class AttachmentPickerActivity extends AuroraListActivity {

    private ContentResolver mContentResolver = null;
    private static final String TAG = "AttachmentPickerActivity";

    private AttachmentListAdapter mAdapter;
    private Context mContext;

    private AuroraButton mDone;
    private int mCheckCount = 0;
    private AuroraButton mSelAllButton;

    private static final int THUMBNAIL_BOUNDS_LIMIT = 480;

    private int mWidth;
    private int mHeight;
    
    // gionee zhouyj 2012-05-08 modified for CR00594316 start
    private DropDownMenu  mSelectionMenu;
    // gionee zhouyj 2012-05-08 modified for CR00594316 end
    
    private String mSelectSdcardPath = null;
    //gionee yewq 2012-11-14 modify for CR00724076 begin
    private String mFileSavedPath = null;
    //gionee yewq 2012-11-14 modify for CR00724076 end

    class PduPartExt {
        public PduPart mPduPart;
        public Bitmap mBm;

        public PduPartExt(PduPart pdupart, Bitmap bm) {
            mPduPart = pdupart;
            mBm = bm;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gionee gaoj 2012-6-27 added for CR00628364 start
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        //gionee gaoj 2012-6-27 added for CR00628364 end
        mContentResolver = getContentResolver();

        setContentView(R.layout.gn_attachment_list);
        setupAdapter();
        getListView().setChoiceMode(AuroraListView.CHOICE_MODE_MULTIPLE);
        //gionee zhouyj 2012-05-03 modified for CR00588621 start
        /**mSelAllButton = (AuroraButton) findViewById(R.id.attachement_sel_all_button);
        mDone = (AuroraButton) findViewById(R.id.attachement_done_button);
        mDone.setEnabled(false);
        setButtonAction();
        setTitle(R.string.title_attachments);
        */
        setupActionBar();
        setAllChecked(true);
        //gionee zhouyj 2012-05-03 modified for CR00588621 end
        // gionee zhouyj 2012-06-16 add for CR00624438 start 
        if(MmsApp.mGnMessageSupport) {
            // gionee zhouyj 2012-08-01 modify for CR00662594 start 
            if(MmsApp.mStorageMountedCount == 1) {
                mSelectSdcardPath = MmsApp.mSDCardPath;
                //gionee yewq 2012-11-14 modify for CR00724076 begin
                mFileSavedPath = getString(R.string.gn_chooser_internal_sdcard);
                //gionee yewq 2012-11-14 modify for CR00724076 end
            } else if(MmsApp.mStorageMountedCount == 2) {
                int position = getIntent().getIntExtra("position", 0);
                if(FeatureOption.MTK_2SDCARD_SWAP ^ position == 0) {
                    mSelectSdcardPath = MmsApp.mSDCard2Path;
                } else {
                    mSelectSdcardPath = MmsApp.mSDCardPath;
                }
                //gionee yewq 2012-11-14 modify for CR00724076 begin
                mFileSavedPath = mSelectSdcardPath.equals(MmsApp.mSDCardPath) ? getString(R.string.gn_chooser_external_sdcard) : getString(R.string.gn_chooser_internal_sdcard);
                //gionee yewq 2012-11-14 modify for CR00724076 end
            }
            // gionee zhouyj 2012-08-01 modify for CR00662594 end 
        }
        // gionee zhouyj 2012-06-16 add for CR00624438 end 
    }

    //gionee zhouyj 2012-05-03 modified for CR00588621 start
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.gn_attachment_picker_menu, menu);
//        if(getListView().getCheckedItemCount() == 0){
//            menu.getItem(0).setEnabled(false);
//        }
        return super.onCreateOptionsMenu(menu);
    }

    private MenuItem mAttachItem;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        mAttachItem = menu.findItem(R.id.select_done);
        Log.d("Gaoj", "onPrepareOptionsMenu");
        if (getListView().getCheckedItemCount() != 0) {
            /*if (MmsApp.mLightTheme) {
                mAttachItem.setIcon(R.drawable.gn_atttach_save);
            } else {
                mAttachItem.setIcon(R.drawable.gn_atttach_save_light);
            }*/
            mAttachItem.setEnabled(true);
        } else {
            /*if (MmsApp.mLightTheme) {
                mAttachItem.setIcon(R.drawable.gn_atttach_save_dis);
            } else {
                mAttachItem.setIcon(R.drawable.gn_atttach_save_light_dis);
            }*/
            mAttachItem.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case R.id.select_done:
            boolean result = true;
            if (getListView().getCheckedItemCount() == 0)
                return false;
            SparseBooleanArray bChecked = getListView().getCheckedItemPositions();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if (bChecked.get(i) == true) {
                    PduPartExt part = mAdapter.getItem(i);
                    result &= copyPart(part.mPduPart, " ");
                }
            }
            //gionee yewq 2012-11-14 modify for CR00724076 begin
            /*String str = result ? getString(R.string.save_att_success)
                    : getString(R.string.save_att_failed);*/
            String str = result ? (getString(R.string.save_att_success) + ": " + mFileSavedPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/")
                    : getString(R.string.save_att_failed);
            //gionee yewq 2012-11-14 modify for CR00724076 end
            Toast.makeText(AttachmentPickerActivity.this, str, Toast.LENGTH_SHORT).show();
            finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    //gionee zhouyj 2012-05-03 modified for CR00588621 end
    
    private void setupAdapter() {
        mAdapter = new AttachmentListAdapter(this);
        getListView().setAdapter(mAdapter);
    }

    public class AttachmentListAdapter extends ArrayAdapter<PduPartExt> {

        public AttachmentListAdapter(Context context) {
            super(context, 0);

            mContext = context;
            Uri uri = getIntent().getData();

            Log.i(TAG, "AttachmentListAdapter" + uri);

            try {
                // get all pdu part
                getAttachmentsInfo(uri);

            } catch (MmsException e) {

                Log.i(TAG, "AttachmentListAdapter get attach error");
                return;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.gn_attachment_list_item, null);
            } else {
                view = convertView;
            }
//            //gionee zhouyj 2012-05-03 modified for CR00588621 start
//            if(getListView().getCheckedItemPositions().get(position)) {
//                view.setBackgroundResource(R.drawable.list_selected_holo_light);
//            } else {
//                view.setBackgroundResource(R.drawable.listitem_background);
//            }
//            //gionee zhouyj 2012-05-03 modified for CR00588621 end
            bindView(view, getContext(), position);

            return view;
        }

        public void bindView(View view, Context context, int position) {

            PduPartExt part = getItem(position);
            ImageView attach_image = (ImageView) view.findViewById(R.id.attach_image);
            TextView attach_name = (TextView) view.findViewById(R.id.attach_name);
            TextView attach_info = (TextView) view.findViewById(R.id.attach_info);
            // we need reinitialize the image view
            attach_image.setImageBitmap(null);

            // set attach information include the size;
            int attSize = getAttachSize(part.mPduPart);
            //gionee zhouyj 2012-05-03 modified for CR00588621 start
            if (attSize >= 1024) {
                attach_info.setText(getString(R.string.attach_size) + (String.format("%.2f", (attSize / 1024.0)))
                        + "k");
            } else {
                attach_info.setText(getString(R.string.attach_size) + attSize + "byte");
            }
            //gionee zhouyj 2012-05-03 modified for CR00588621 end

            byte[] location = part.mPduPart.getContentLocation();
            if (location == null) {
                location = part.mPduPart.getFilename();
            }
            if (location == null) {
                location = part.mPduPart.getName();
            }

            // set attach name
            if (location != null) {
                attach_name.setText(new String(location));
            }

            String contentType = PduPersister.toIsoString(part.mPduPart.getContentType());

            // show the thumnail of image or the first frame of video
            if ((ContentType.isImageType(contentType) || ContentType.isVideoType(contentType)
                    && part.mBm != null)) {

                // is = mContentResolver.openInputStream(part.getDataUri());
                // attach_image.setImageBitmap(BitmapFactory.decodeStream(is));
                attach_image.setImageBitmap(part.mBm);

            } else if (ContentType.isAudioType(contentType)
                    || contentType.equals("application/ogg")) {

                // set transparent color
//                attach_image.setBackgroundColor(00000000);
                //attach_image.setImageResource(R.drawable.ic_mms_music);

            } else {

                // other attach type, set the default icon
//                attach_image.setBackgroundColor(00000000);
                //attach_image.setImageResource(R.drawable.ic_dialog_attach);
            }

            ((Checkable) view.findViewById(R.id.check)).setChecked(getListView()
                    .getCheckedItemPositions().get(position));
//
//            updateBtnDone(getListView().getCheckedItemPositions());
        }

        // get all the pdu parts and add it to list.
        private void getAttachmentsInfo(Uri msg) throws MmsException {
            PduPersister p = PduPersister.getPduPersister(mContext);

            GenericPdu pdu = p.load(msg);
            PduBody pduBody = null;

            int msgType = pdu.getMessageType();

            if ((msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ)
                    || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)) {
                pduBody = ((MultimediaMessagePdu) pdu).getBody();
            }

            // no pdu body , throw exception
            if (pduBody == null) {
                throw new MmsException();
            }

            int partNum = pduBody.getPartsNum();
            int i = 0;

            // add all pdu part (except some type, see code) to list
            while (i < partNum) {
                PduPartExt pdupartExt = new PduPartExt(pduBody.getPart(i), null);
                InputStream is = null;
                String type = new String(pduBody.getPart(i).getContentType());
                Log.e(TAG, "getAttachmentsInfo type " + type);


                if (ContentType.isImageType(type) ) {
                    pdupartExt.mBm = internalGetBitmap(pduBody.getPart(i).getDataUri());// BitmapFactory.decodeStream(is);
                } else if(ContentType.isVideoType(type)) {
                    pdupartExt.mBm = createVideoThumbnail(mContext, pduBody.getPart(i).getDataUri());
                }

                if (!type.equals("text/plain") && !type.equals("application/smil")) {
                    add(pdupartExt);
                }

                i++;
            }
        }
    }

    @Override
    protected void onListItemClick(AuroraListView arg0, View arg1, int arg2, long arg3) {
        super.onListItemClick(arg0, arg1, arg2, arg3);
        updateSelectDoneText();
    }

    void updateSelectDoneText() {
        //gionee zhouyj 2012-05-03 modified for CR00588621 start
        /** annotated
        SparseBooleanArray bChecked = getListView().getCheckedItemPositions();

        int checkedCount = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (bChecked.get(i) == true) {
                checkedCount++;
            }
        }

        if (checkedCount > 0) {
            mDone.setEnabled(true);
            mDone.setText(getString(R.string.gn_selected) + "(" + checkedCount + ")");
        } else {
            mDone.setEnabled(false);
            mDone.setText(getString(R.string.gn_selected));
        }

        if (checkedCount == mAdapter.getCount()) {
            mSelAllButton.setText(R.string.unselect_all);
        } else {
            mSelAllButton.setText(R.string.select_all);
        }*/
        updateSelectionMenu();
        invalidateOptionsMenu();
        mAdapter.notifyDataSetChanged();
    }

    public void setButtonAction() {
        //gionee zhouyj 2012-05-03 modified for CR00588621 start
        /** annotate
        mDone.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                boolean result = true;

                // notice
                SparseBooleanArray bChecked = getListView().getCheckedItemPositions();

                for (int i = 0; i < mAdapter.getCount(); i++) {
                    if (bChecked.get(i) == true) {
                        PduPartExt part = mAdapter.getItem(i);
                        result &= copyPart(part.mPduPart, " ");
                    }
                }

                String str = result ? getString(R.string.save_att_success)
                        : getString(R.string.save_att_failed);
                Toast.makeText(AttachmentPickerActivity.this, str, Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        ((AuroraButton) findViewById(R.id.attachement_cancel_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        mSelAllButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (getString(R.string.select_all).equals(mSelAllButton.getText())) {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        getListView().setItemChecked(i, true);
                        updateSelectDoneText();
                    }
                } else {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        getListView().setItemChecked(i, false);
                        updateSelectDoneText();
                    }
                }
            }
        });
         */
        //gionee zhouyj 2012-05-03 modified for CR00588621 start
    }

    private boolean copyPart(PduPart part, String fallback) {
        Uri uri = part.getDataUri();

        InputStream input = null;
        FileOutputStream fout = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                // TYRD: lanwzh modify for PR0D021111 20110107 begin
                byte[] location = part.getContentLocation();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getName();
                }
                // TYRD: lanwzh modify for PR0D021111 20110107 end
                //gionee zhouyj 2012-05-03 modified for CR00588621 start
                String fileName;
                if (location == null) {
                    // Use fallback name.
                    fileName = fallback;
                } else {
                    fileName = new String(location);
                }

                // Depending on the location, there may be an
                // extension already on the name or not
                String dir = Environment.getExternalStorageDirectory() + "/"
                        + Environment.DIRECTORY_DOWNLOADS + "/";
                // gionee zhouyj 2012-06-16 add for CR00624438 start 
                if(MmsApp.mGnMessageSupport) {
                    dir = mSelectSdcardPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/";
                //gionee yewq 2012-11-14 modify for CR00724076 begin
                }else if(MmsApp.mStorageMountedCount == 1){
                    mFileSavedPath = getString(R.string.gn_chooser_internal_sdcard);
                }else if(MmsApp.mStorageMountedCount == 2){
                    mFileSavedPath = getString(R.string.gn_chooser_external_sdcard);
                //gionee yewq 2012-11-14 modify for CR00724076 end
                }
                // gionee zhouyj 2012-06-16 add for CR00624438 end 
                String extension;
                int index;

                fileName = fileName.replace(' ', '_');
                fileName = fileName.replace(':', '-');

                if ((index = fileName.indexOf(".")) == -1) {
                    String type = new String(part.getContentType());
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                }

                File file = getUniqueDestination(dir + fileName, extension);

                // make sure the path is valid and directories created for this
                // file.
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                    return false;
                }

                fout = new FileOutputStream(file);

                byte[] buffer = new byte[8000];
                int size = 0;
                while ((size = fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, size);
                }

                Uri fileUri = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri);
                sendBroadcast(intent);
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }

    private File getUniqueDestination(String base, String extension) {
        File file = new File(base + "." + extension);

        for (int i = 2; file.exists(); i++) {
            file = new File(base + "_" + i + "." + extension);
        }
        return file;
    }

    // get the size of one pdu part
    private int getAttachSize(PduPart part) {
        Uri uri = part.getDataUri();

        int allSize = 0;

        InputStream input = null;
        FileOutputStream fout = null;

        try {

            input = mContentResolver.openInputStream(uri);
            int size = 0;
            byte[] buffer = new byte[8000];
            while ((size = input.read(buffer)) != -1) {
                allSize += size;
            }

        } catch (IOException e) {

            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return 0;

        } finally {

            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }

        return allSize;
    }

    private Bitmap internalGetBitmap(Uri uri) {
        Bitmap bm = null;

        try {
            bm = createThumbnailBitmap(THUMBNAIL_BOUNDS_LIMIT, uri);
        } catch (OutOfMemoryError ex) {
            // fall through and return a null bitmap. The callers can handle a null
            // result and show R.drawable.ic_missing_thumbnail_picture
        }

        Log.i(TAG, "internalGetBitmap " + bm);
        return bm;
    }
    private Bitmap createThumbnailBitmap(int thumbnailBoundsLimit, Uri uri) {
        decodeBoundsInfo(uri);
        int outWidth = mWidth;
        int outHeight = mHeight;

        int s = 1;
        while ((outWidth / s > thumbnailBoundsLimit)
                || (outHeight / s > thumbnailBoundsLimit)) {
            s *= 2;
        }

        Log.i(TAG, "createThumbnailBitmap: scale=" + s + ", w=" + outWidth / s
                + ", h=" + outHeight / s);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = s;

        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(input, null, options);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (OutOfMemoryError ex) {
            //MessageUtils.writeHprofDataToFile();
            throw ex;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    private void decodeBoundsInfo(Uri uri) {
        InputStream input = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opt);
            mWidth = opt.outWidth;
            mHeight = opt.outHeight;
        } catch (FileNotFoundException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening stream", e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
    }

    private Bitmap createVideoThumbnail(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            bitmap = retriever.getFrameAtTime(1000);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            Log.d(TAG, ex.getMessage());
        } catch (RuntimeException ex) {
            Log.d(TAG, ex.getMessage());
        }
        finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return bitmap;
    }
    
    //gionee zhouyj 2012-05-03 added for CR00588621 start
    private TextView mCountTitle;
    private CheckBox mSelectAll;
    private void setupActionBar() {
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        ViewGroup v = (ViewGroup)LayoutInflater.from(this)
            .inflate(R.layout.gn_attachment_picker_actionbar, null);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        ImageButton quit = (ImageButton) v.findViewById(R.id.back_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCountTitle = (TextView) v.findViewById(R.id.select_count_text);
        mSelectAll = (CheckBox) v.findViewById(R.id.gn_msg_select_check_box);
        mSelectAll.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                    if (getListView().getCheckedItemCount() != getListView().getCount()) {
                        setAllChecked(true);
                    } else {
                        setAllChecked(false);
                    }
                }
        });
        actionBar.setCustomView(v);
    }

    public void updateSelectionMenu() {
        int count = getListView().getCheckedItemCount();
        if (mSelectAll != null && mCountTitle != null) {
            if (getListView().getCheckedItemCount() == getListView().getCount()) {
                mSelectAll.setChecked(true);
            } else {
                mSelectAll.setChecked(false);
            }
            String text = getResources().getString(R.string.gn_select_conversation_more, count);
            mCountTitle.setText(text);
        }
    }
    
    private void setAllChecked(boolean checked) {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            getListView().setItemChecked(i, checked);
        }
        updateSelectDoneText();
    }
    //gionee zhouyj 2012-05-03 added for CR00588621 end
}
