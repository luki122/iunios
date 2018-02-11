/***
 * 权限管理列表展示
 */

package com.aurora.puremanager.permission;

import java.util.List;

import com.aurora.puremanager.R;
import com.aurora.puremanager.permission.AsyncIconImageLoader.ImageCallback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CompositeAdapter extends BaseAdapter {
    public List<ItemInfo> mData;
    protected LayoutInflater mInflater;
    private Context mContext;
    private AsyncIconImageLoader mAsyncIconImageLoader;
    public ListView mListView;

    public CompositeAdapter(Context context, List<ItemInfo> data) {
    	mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mAsyncIconImageLoader = new AsyncIconImageLoader();
    }

    public CompositeAdapter(Context context, List<ItemInfo> data, ListView listView){
        this(context, data);
        mListView = listView;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return this.mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return this.mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        if (convertView == null) {
            view = new ViewHolder();
            convertView = mInflater.inflate(R.layout.softmanager_adapter_layout_pre, parent, false);
            view.mIcon = ( ImageView ) convertView.findViewById(R.id.icon);
            view.mTitle = ( TextView ) convertView.findViewById(R.id.title);
            view.mSummary = ( TextView ) convertView.findViewById(R.id.summary);
            view.mAppSize = ( TextView ) convertView.findViewById(R.id.app_size);
            convertView.setTag(view);
        } else {
            view = ( ViewHolder ) convertView.getTag();
        }
        if (mData.size() > 0) {
        	if( mData.get(position).getIcon() != null){
        		view.mIcon.setImageDrawable(mData.get(position).getIcon());
            } else {
                String pkgName = mData.get(position).getPackageName();
                view.mIcon.setTag(pkgName);
                Drawable cachedImage = mAsyncIconImageLoader.loadDrawable(pkgName, mContext,
                        new ImageCallback() {
                            public void imageLoaded(Drawable imageDrawable, String pkgName) {
                                if (mListView == null) {
                                    return;
                                }
                                ImageView imageViewByTag = (ImageView) mListView.findViewWithTag(pkgName);
                                if (imageViewByTag != null) {
                                    imageViewByTag.setImageDrawable(imageDrawable);
                                }
                            }
                        });
                if (cachedImage != null) {
                    view.mIcon.setImageDrawable(cachedImage);
                }
            }	
            view.mTitle.setText(mData.get(position).getName());
            String summary = mData.get(position).getSummary();
            if (summary != null) {
                view.mSummary.setText(Html.fromHtml(mData.get(position).getSummary()));
                view.mSummary.setVisibility(View.VISIBLE);
            }
            else {
                view.mSummary.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}
