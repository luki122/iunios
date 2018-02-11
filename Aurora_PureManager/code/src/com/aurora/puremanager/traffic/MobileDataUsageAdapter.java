package com.aurora.puremanager.traffic;

import static com.android.internal.util.Preconditions.checkNotNull;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.imageloader.ImageLoader;

public class MobileDataUsageAdapter extends BaseAdapter {

    private final UidDetailProvider mProvider;
    private final int mInsetSide;
    private ArrayList<AppItem> mItems = new ArrayList<AppItem>();// Lists.newArrayList();
    private long mLargest;
    private static View mParent;

    public MobileDataUsageAdapter(UidDetailProvider provider, int insetSide, ArrayList<AppItem> items) {
        mProvider = checkNotNull(provider);
        mInsetSide = insetSide;
        mItems = items;
    }

    public void notifyDataSetChanged(ArrayList<AppItem> items, long largest) {
        mItems = items;
        mLargest = largest;
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return mItems.get(position).key;
    }

    @Override
    public boolean isEnabled(int position) {
        if(mItems.get(position).key < 10000){
        	return false;
        }else{
        	return true;
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
    	mParent = parent;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.trafficassistant_data_usage_item, parent, false);

            if (mInsetSide > 0) {
                convertView.setPadding(mInsetSide, 0, mInsetSide, 0);
            }
        }

        final Context context = parent.getContext();
        final TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);

        final ProgressBar progress = (ProgressBar) convertView.findViewById(android.R.id.progress);

        final AppItem item = mItems.get(position);
        UidDetailTask.bindView(mProvider, item, convertView);

        if (item.restricted && item.total < 0) {
            text1.setText(R.string.data_usage_app_restricted);
            progress.setVisibility(View.GONE);
        } else {
            text1.setText(Formatter.formatFileSize(context, item.total));
            progress.setVisibility(View.VISIBLE);
        }

        final int percentTotal = (mLargest != 0) ? (int) (item.total * 100 / mLargest) : 0;
        progress.setProgress(percentTotal);

        return convertView;
    }

    private static class UidDetailTask extends AsyncTask<Void, Void, UidDetail> {
        private final UidDetailProvider mProvider;
        private final AppItem mItem;
        private final View mTarget;

        private UidDetailTask(UidDetailProvider provider, AppItem item, View target) {
            mProvider = checkNotNull(provider);
            mItem = checkNotNull(item);
            mTarget = checkNotNull(target);
        }

        public static void bindView(UidDetailProvider provider, AppItem item, View target) {

            final UidDetailTask existing = (UidDetailTask) target.getTag();
            if (existing != null) {
                existing.cancel(false);
            }

            final UidDetail cachedDetail = provider.getUidDetail(item.key, false);

            if (cachedDetail != null) {
                bindView(cachedDetail, target);
            } else {
                target.setTag(new UidDetailTask(provider, item, target)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
            }
        }

        private static void bindView(UidDetail detail, View target) {
            final ImageView icon = (ImageView) target.findViewById(android.R.id.icon);
            final TextView title = (TextView) target.findViewById(android.R.id.title);

            if (detail != null) {
            	String iconViewTag = detail.packageName+"@app_icon";
            	icon.setTag(iconViewTag);
                Drawable cachedImage = ImageLoader.getInstance(mParent.getContext()).displayImage(icon, detail.packageName, iconViewTag, 
                		new com.aurora.puremanager.imageloader.ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable, Object viewTag) {
						// TODO Auto-generated method stub
						if(mParent == null ||
								imageDrawable == null || 
    									viewTag == null){
    						return ;
    					}
    					ImageView imageViewByTag = (ImageView)mParent.findViewWithTag(viewTag);
    					if (imageViewByTag != null) {
    						imageViewByTag.setImageDrawable(imageDrawable);
    					}
					}
				});
            		if (cachedImage != null) {
            			icon.setImageDrawable(cachedImage);
            		}else{
            			if(detail.packageName == null){
            				icon.setImageDrawable(detail.icon);
            			}else{
            				icon.setImageResource(R.drawable.def_app_icon);
            			}
            		}
                title.setText(detail.label);
            } else {
                icon.setImageDrawable(null);
                title.setText(null);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            bindView(null, mTarget);
        }

        @Override
        protected UidDetail doInBackground(Void... params) {
            // TODO Auto-generated method stub
            return mProvider.getUidDetail(mItem.key, true);
        }

        @Override
        protected void onPostExecute(UidDetail result) {
            // TODO Auto-generated method stub
            bindView(result, mTarget);
        }
    }
}
