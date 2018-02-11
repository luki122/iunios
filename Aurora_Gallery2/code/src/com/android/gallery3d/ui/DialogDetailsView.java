/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.gallery3d.ui;

import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Typeface;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.DetailsHelper.DetailsViewContainer;

import java.util.ArrayList;
import java.util.Map.Entry;

public class DialogDetailsView implements DetailsViewContainer {
    @SuppressWarnings("unused")
    private static final String TAG = "DialogDetailsView";

    private final AbstractGalleryActivity mActivity;
    private DetailsAdapter mAdapter;
    private MediaDetails mDetails;
    private final DetailsSource mSource;
    private int mIndex;
    private Dialog mDialog;
    private CloseListener mListener;
    
	//Aurora <SQF> <2014-05-09>  for NEW_UI begin
    //private Typeface mTypeface;
	//Aurora <SQF> <2014-05-09>  for NEW_UI end

    public DialogDetailsView(AbstractGalleryActivity activity, DetailsSource source) {
        mActivity = activity;
        mSource = source;
		//Aurora <SQF> <2014-05-09>  for NEW_UI begin
        //mTypeface = Typeface.createFromFile("system/fonts/SourceHanSansCN-Normal.ttf");
  		//Aurora <SQF> <2014-05-09>  for NEW_UI end
    }

    @Override
    public void show() {
        reloadDetails();
        mDialog.show();
    }

    @Override
    public void hide() {
        mDialog.hide();
    }

    @Override
    public void reloadDetails() {
//    	Log.i("SQF_LOG", "DialogDetailsView::reloadDetails");
        int index = mSource.setIndex();
        if (index == -1) return;
        MediaDetails details = mSource.getDetails();
        if (details != null) {
            if (mIndex == index && mDetails == details) return;
            mIndex = index;
            mDetails = details;
            setDetails(details);
        }
    }

    private void setDetails(MediaDetails details) {
        mAdapter = new DetailsAdapter(details);
		// Aurora <zhanggp> <2013-12-06> modified for gallery begin

        String title = mActivity.getAndroidContext().getString(R.string.details);
		/*
        String title = String.format(
                mActivity.getAndroidContext().getString(R.string.details_title),
                mIndex + 1, mSource.size());
        */
		// Aurora <zhanggp> <2013-12-06> modified for gallery end
        
        //Aurora <SQF> <2014-04-09>  for NEW_UI begin
        //Originally:
//        ListView detailsList = (ListView) LayoutInflater.from(mActivity.getAndroidContext()).inflate(
//                R.layout.details_list, null, false);
        //SQF modified to:
//        Log.i("SQF_LOG", "DialogDetailsView::setDetails--> title:" + title);
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(mActivity.getAndroidContext()).inflate(R.layout.details_list, null, false);
        ListView detailsList = (ListView) linearLayout.findViewById(R.id.details_list_view);
      //Aurora <SQF> <2014-04-09>  for NEW_UI end
        detailsList.setAdapter(mAdapter);        
        
        mDialog = new AuroraAlertDialog.Builder(mActivity)
        	.setView(linearLayout)//.setView(detailsList)
            .setTitle(title)
            .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    mDialog.dismiss();
                }
            })
            .create();
        
//        mDialog = new AlertDialog.Builder(mActivity.getAndroidContext())
//    	.setView(linearLayout)//.setView(detailsList)
//        .setTitle(title)
//        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int whichButton) {
//                mDialog.dismiss();
//            }
//        })
//        .create();

        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mListener != null) {
                    mListener.onClose();
                }
            }
        });
    }

    private class DetailsAdapter extends BaseAdapter implements AddressResolvingListener {
        private final ArrayList<String> mItems;
        private int mLocationIndex;

        public DetailsAdapter(MediaDetails details) {
            Context context = mActivity.getAndroidContext();
            mItems = new ArrayList<String>(details.size());
            mLocationIndex = -1;
            setDetails(context, details);
        }

        private void setDetails(Context context, MediaDetails details) {
            for (Entry<Integer, Object> detail : details) {
                String value;
                switch (detail.getKey()) {
                    case MediaDetails.INDEX_LOCATION: {
                        double[] latlng = (double[]) detail.getValue();
                        mLocationIndex = mItems.size();
                        value = DetailsHelper.resolveAddress(mActivity, latlng, this);
//                        Log.i("SQF_LOG", "MediaDetails.INDEX_LOCATION value:======================>" + value);
                        break;
                    }
                    case MediaDetails.INDEX_SIZE: {
                        value = Formatter.formatFileSize(
                                context, (Long) detail.getValue());
//                        Log.i("SQF_LOG", "MediaDetails.INDEX_SIZE value:======================>" + value);
                        break;
                    }
                    case MediaDetails.INDEX_WHITE_BALANCE: {
                        value = "1".equals(detail.getValue())
                                ? context.getString(R.string.manual)
                                : context.getString(R.string.auto);
//                        Log.i("SQF_LOG", "MediaDetails.INDEX_WHITE_BALANCE value:======================>" + value);
                        break;
                    }
                    case MediaDetails.INDEX_FLASH: {
                        MediaDetails.FlashState flash =
                                (MediaDetails.FlashState) detail.getValue();
                        // TODO: camera doesn't fill in the complete values, show more information
                        // when it is fixed.
                        if (flash.isFlashFired()) {
                            value = context.getString(R.string.flash_on);
                        } else {
                            value = context.getString(R.string.flash_off);
                        }
//                        Log.i("SQF_LOG", "MediaDetails.INDEX_FLASH value:======================>" + value);
                        break;
                    }
                    case MediaDetails.INDEX_EXPOSURE_TIME: {
                        value = (String) detail.getValue();
                        double time = Double.valueOf(value);
                        if (time < 1.0f) {
                            value = String.format("1/%d", (int) (0.5f + 1 / time));
                        } else {
                            int integer = (int) time;
                            time -= integer;
                            value = String.valueOf(integer) + "''";
                            if (time > 0.0001) {
                                value += String.format(" 1/%d", (int) (0.5f + 1 / time));
                            }
                        }
//                        Log.i("SQF_LOG", "MediaDetails.INDEX_EXPOSURE_TIME value:======================>" + value);
                        break;
                    }
                    case MediaDetails.INDEX_WIDTH: {
                    	value = String.valueOf(detail.getValue());
                    	break;
                    }
                    case MediaDetails.INDEX_HEIGHT: {
                    	value = String.valueOf(detail.getValue());
                    	break;
                    }
                    default: {
                        Object valueObj = detail.getValue();
                        // This shouldn't happen, log its key to help us diagnose the problem.
                        if (valueObj == null) {
                            Utils.fail("%s's value is Null",
                                    DetailsHelper.getDetailsName(context, detail.getKey()));
                        }
                        value = valueObj.toString();
//                        Log.i("SQF_LOG", "MediaDetails default value:======================>" + value);
                    }
                }
                int key = detail.getKey();
                if (details.hasUnit(key)) {
                    value = String.format("%s:%s %s", DetailsHelper.getDetailsName(
                            context, key), value, context.getString(details.getUnit(key)));
                } else {
                    value = String.format("%s:%s", DetailsHelper.getDetailsName(
                            context, key), value);
                }
                mItems.add(value);
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mDetails.getDetail(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	//Aurora <SQF> <2014-05-07>  for NEW_UI begin
        	//ORIGINALLY:
        	/*
            TextView tv;
            if (convertView == null) {
                tv = (TextView) LayoutInflater.from(mActivity.getAndroidContext()).inflate(
                        R.layout.details, parent, false);
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(mItems.get(position));
            return tv;
            */
        	//SQF MODIFIED TO:
        	TextView tv1 = null, tv2 = null;
        	View v = null;
            if (convertView == null) {
            	v = LayoutInflater.from(mActivity.getAndroidContext()).inflate(R.layout.details, parent, false);
                convertView = v;
            }
            tv1 = (TextView) convertView.findViewById(R.id.text1);
            tv2 = (TextView) convertView.findViewById(R.id.text2);
            String value = mItems.get(position);
            int indexOfColon = -1;
            if(null != value && !value.isEmpty() && (indexOfColon = value.indexOf(":")) != -1) {
            	String value1 = value.substring(0, indexOfColon + 1);
            	String value2 = value.substring(indexOfColon + 1);
            	//tv1.setTypeface(mTypeface);
            	//tv2.setTypeface(mTypeface);
                tv1.setText(value1 != null ? value1 : "");
                tv2.setText(value2 != null ? value2 : "");
            }
            return convertView; 
          //Aurora <SQF> <2014-05-07>  for NEW_UI end
        }

        @Override
        public void onAddressAvailable(String address) {
//        	Log.i("SQF_LOG", "======================>address" + address);
            mItems.set(mLocationIndex, address);
            notifyDataSetChanged();
        }
    }

    @Override
    public void setCloseListener(CloseListener listener) {
        mListener = listener;
    }
}
