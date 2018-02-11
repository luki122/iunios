package com.aurora.market.adapter;

import java.util.List;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import aurora.widget.AuroraSearchView;

import com.aurora.datauiapi.data.bean.SearchTimelyObject;
import com.aurora.datauiapi.data.bean.TimelyInfo;
import com.aurora.market.R;
import com.aurora.market.activity.module.MarketSearchActivity;
import com.aurora.market.util.Globals;

public class PopQueryResultAdapter extends ArrayAdapter{
    
    private LayoutInflater inflater;
    private Context mContext;
    private List<TimelyInfo> mList;
    private TextView mQueryText;

    private int mLayoutResId;
    private int mTxtViewId;
     private AuroraSearchView mSearchView;
    public PopQueryResultAdapter(Context pContext, int pResource, int pTextViewResourceId, List pList,AuroraSearchView mSearchView) {
        super(pContext, pResource, pTextViewResourceId, pList);
        // TODO Auto-generated constructor stub
        mContext = pContext;
        mLayoutResId = pResource;
        mTxtViewId = pTextViewResourceId;
        mList = pList;
        inflater = LayoutInflater.from(pContext);
        this.mSearchView = mSearchView;
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mList.size() > Globals.SHOW_SIZE_LIMIT ? Globals.SHOW_SIZE_LIMIT:mList.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int pPosition, View pView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (pView == null)
            pView = inflater.inflate(
                    mLayoutResId, null);
        
        mQueryText = (TextView)pView.findViewById(R.id.pop_text);
        
        SpannableStringBuilder lBuf = new SpannableStringBuilder();
        String lTitleStr = mList.get(pPosition).getTitle();
        
        int lStart = queryIndexOf(lTitleStr, mSearchView.getQuery().toString().trim());
        lBuf.append(lTitleStr);
        if( lStart != -1){
           lBuf.setSpan( new ForegroundColorSpan(
        		   mContext.getResources().getColor(R.color.pop_query_text_color)), 
                   lStart, lStart + mSearchView.getQuery().toString().trim().length(), 
                   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mQueryText.setText(lBuf);
        return pView;
    }
    
    private int queryIndexOf(String pParentStr, String pChildStr){
        
        int lIndex = -1;
        lIndex = pParentStr.indexOf(pChildStr);
        
        if( lIndex == -1)
            lIndex = pParentStr.indexOf(pChildStr.toLowerCase());
        
        if(lIndex == -1) 
            lIndex = pParentStr.indexOf(pChildStr.toUpperCase());
        
        return lIndex;
        
    }
    
}