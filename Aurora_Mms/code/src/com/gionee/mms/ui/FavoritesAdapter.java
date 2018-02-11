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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.android.mms.R;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.mms.data.Contact;

import android.provider.Telephony.Sms;

/**
 * This Adapter is for each favorite item's layout
 */
public class FavoritesAdapter extends CursorAdapter {

    private final static String TAG = "FavoritesAdapter";

    private final LayoutInflater mInflater;

    public FavoritesAdapter(Context context, Cursor c) {
        super(context, c, false /** auto-requery */
        );
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(TAG, "--------------newView()-------------");
        final View view = mInflater.inflate(R.layout.gn_favorites_list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.i(TAG, "--------------bindView()-------------");
        TextView body = (TextView) view.findViewById(R.id.favorite_body);
        TextView date = (TextView) view.findViewById(R.id.favorite_date);
        TextView receipt = (TextView) view.findViewById(R.id.favorite_receipt);

        String bodyValue = cursor.getString(cursor.getColumnIndex(Sms.BODY));
        String dateValue = cursor.getString(cursor.getColumnIndex(Sms.DATE));
        String receiptValue = cursor.getString(cursor.getColumnIndex(Sms.ADDRESS));

        Log.i(TAG, "bodyValue=" + bodyValue + ",dateValue=" + dateValue + ",receiptValue="
                + receiptValue);

        // format date to "yyyy-MM-dd HH:mm:ss"
        Date formatDate = new Date(Long.parseLong(dateValue));
        SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateValue = bartDateFormat.format(formatDate);

        body.setText(bodyValue);
        date.setText(dateValue);
        receipt.setText(receiptValue);
    }

}
