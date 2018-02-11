package com.gionee.mms.ui;
//gionee zhouyj 2012-07-13 create for CR00647101

import com.android.mms.MmsApp;
import com.android.mms.R;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import aurora.widget.AuroraButton;
import android.widget.CheckedTextView;
import aurora.widget.AuroraListView;
import android.widget.TextView;

public class ChooseSdcardActivity extends AuroraActivity implements View.OnClickListener, AdapterView.OnItemClickListener{
    private int mCheckedPosition = 0;
    private AuroraListView mListView;
    private AuroraButton mBtnNext;
    private TextView mTextTips;
    private ChooseListAdapter mListAdapter;
    private String mStringUri = null;
    
    private int [] mData = {R.string.gn_chooser_external_sdcard, R.string.gn_chooser_internal_sdcard};
    
    @Override
    protected void onCreate(Bundle state) {
        // TODO Auto-generated method stub
        if (MmsApp.mTransparent) {
            setTheme(R.style.TabActivityTheme);
        } else if (MmsApp.mLightTheme) {
            setTheme(R.style.GnMmsLightTheme);
        } else if (MmsApp.mDarkTheme) {
            setTheme(R.style.GnMmsDarkTheme);
        }
        super.onCreate(state);
        // Aurora liugj 2013-09-13 modified for aurora's new feature start
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Aurora liugj 2013-09-13 modified for aurora's new feature end
        setContentView(R.layout.gn_multi_export_choose_sdcard);
        mTextTips = (TextView) findViewById(R.id.tips);
        String tip = getIntent().getStringExtra("tips");
        mTextTips.setText(tip);
        if(tip.equals(getString(R.string.import_export_tip))) {
            setTitle(R.string.gn_export_btn_text);
        } else if(tip.equals(getString(R.string.copy_attachment_to))) {
            setTitle(R.string.copy_to_sdcard);
        }
        mStringUri = getIntent().getStringExtra("uri");
        mListAdapter = new ChooseListAdapter(this);
        mListView = (AuroraListView) findViewById(R.id.list_view);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(this);
        mBtnNext = (AuroraButton) findViewById(R.id.btn_action);
        mBtnNext.setOnClickListener(this);
    }
    
    //Gionee <zhouyj> <2013-04-23> add for CR00801232 start
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MmsApp.mIsSafeModeSupport) {
            finish();
        }
    }
    //Gionee <zhouyj> <2013-04-23> add for CR00801232 end
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if( v == mBtnNext) {
            Intent i = new Intent("ChooseSdcard");
            i.putExtra("position", mCheckedPosition);
            i.putExtra("uri", mStringUri);
            setResult(RESULT_OK, i);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // TODO Auto-generated method stub
        mCheckedPosition = position;
        mListAdapter.notifyDataSetChanged();
    }
    
    private class ChooseListAdapter extends BaseAdapter{
        private LayoutInflater mLayoutInflater;
        private Context mContext;
        
        public ChooseListAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return mData[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final CheckedTextView view;
            if (convertView != null) {
                view = (CheckedTextView) convertView;
            } else {
                view = (CheckedTextView) mLayoutInflater.inflate(R.layout.gn_export_choose_list_item, parent, false);
            }
            view.setText(mContext.getString(mData[position]));
            view.setChecked(position == mCheckedPosition);
            return view;
        }
        
    }
}
