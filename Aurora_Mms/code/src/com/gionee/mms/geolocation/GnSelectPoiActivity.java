package com.gionee.mms.geolocation;

import java.util.ArrayList;

import com.android.mms.MmsApp;
import com.android.mms.R;
// Aurora liugj 2013-09-13 modified for aurora's new feature start
import android.app.ActionBar;
// Aurora liugj 2013-09-13 modified for aurora's new feature end
import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import aurora.widget.AuroraListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GnSelectPoiActivity extends AuroraActivity {

    private AuroraListView mListView;
    private PoiAdapter mAdapter;
    private ArrayList<String> mArrayPoi;
    private int mPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkStyle) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_location_poi_list);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        ActionBar actionBar = getActionBar();
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mArrayPoi = (ArrayList<String>) getIntent().getSerializableExtra("poi");
        mAdapter = new PoiAdapter(this);
        mAdapter.setData(mArrayPoi);
        mListView = (AuroraListView) findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);
    }

    private void updateUi(int pos) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PoiAdapter extends BaseAdapter {
        private ArrayList<String> mData = new ArrayList<String>();
        private LayoutInflater mInflater;

        public PoiAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<String> data) {
            mData = data;
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        public String getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            final int pos = position;
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.gn_location_poi_item, null);
                holder = new ViewHolder();
                holder.mTextView = (TextView) convertView
                        .findViewById(R.id.item_address);
                holder.mRadioButton = (RadioButton) convertView
                        .findViewById(R.id.radio_btn);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mTextView.setText(mData.get(position));
            holder.mRadioButton.setChecked(mPos == position);
            holder.mRadioButton.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    onItemClick(pos);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    onItemClick(pos);
                }
            });
            return convertView;
        }
        
        private void onItemClick(int pos) {
            if(mPos == pos) {
                mPos = -1;
            } else {
                mPos = pos;
            }
            notifyDataSetChanged();
            Intent i = new Intent("LOCATION");
            i.putExtra("poi", mAdapter.getItem(pos));
            setResult(RESULT_OK, i);
            finish();
        }

        private class ViewHolder {
            TextView mTextView;
            RadioButton mRadioButton;
        }
    }
}
