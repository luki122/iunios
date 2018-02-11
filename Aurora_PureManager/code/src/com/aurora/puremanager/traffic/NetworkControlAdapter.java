package com.aurora.puremanager.traffic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraSwitch;

import com.aurora.puremanager.R;
import com.aurora.puremanager.imageloader.ImageLoader;
import com.aurora.puremanager.permission.AsyncIconImageLoader;

public class NetworkControlAdapter extends BaseAdapter {

//    private DataChangeListener mListener;
    private ArrayList<NetworkAppInfo> mAppListInfo = new ArrayList<NetworkAppInfo>();
    private AppNetworkControl mAppNetworkControl;
    private LayoutInflater mInflater;
    private Context mContext;
    private AsyncIconImageLoader mAsyncIconImageLoader;
    private ListView mListView;
    private int mIndex = 0;
    private View parent;

    public NetworkControlAdapter(Context context, ListView listView) {
        mContext = context;
//        mListener = listener;
        mInflater = LayoutInflater.from(mContext);
        mAppNetworkControl = AppNetworkControl.getInstance(mContext);
        mAsyncIconImageLoader = new AsyncIconImageLoader();
        mListView = listView;
    }

    public void notifyDataSetChanged(ArrayList<NetworkAppInfo> appListInfo, int position) {
        mAppListInfo = appListInfo;
        mIndex = position;
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mAppListInfo.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mAppListInfo.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        // TODO Auto-generated method stub
    	this.parent = parent;
        final ViewHolder view;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.trafficassistant_app_list_item, parent, false);
            view = new ViewHolder();
            view.layout = (RelativeLayout) convertView.findViewById(R.id.item_layout);
            view.icon = (ImageView) convertView.findViewById(R.id.icon);
            view.title = (TextView) convertView.findViewById(R.id.title);
            view.aswitch = (AuroraSwitch) convertView.findViewById(R.id.switch_action);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }
		if (null != mAppListInfo && mAppListInfo.size() > 0) {
			final NetworkAppInfo appInfo = (NetworkAppInfo) mAppListInfo.get(position);
			view.layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					view.aswitch.setChecked(!view.aswitch.isChecked());
				}
			});
            {
            	String iconViewTag = appInfo.getAppPackageName()+"@app_icon";
            	view.icon.setTag(iconViewTag);
                Drawable cachedImage = ImageLoader.getInstance(mContext).displayImage(view.icon, appInfo.getAppPackageName(), iconViewTag, 
                		new com.aurora.puremanager.imageloader.ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable, Object viewTag) {
						// TODO Auto-generated method stub
						if(parent == null || 
    							imageDrawable == null || 
    									viewTag == null){
    						return ;
    					}
    					ImageView imageViewByTag = (ImageView)parent.findViewWithTag(viewTag);
    					if (imageViewByTag != null) {
    						imageViewByTag.setImageDrawable(imageDrawable);
    					}
					}
				});
            		if (cachedImage != null) {
            			view.icon.setImageDrawable(cachedImage);
            		}else{
            			view.icon.setImageResource(R.drawable.def_app_icon);
            		}
            }

            view.title.setText(appInfo.getAppName());

            view.aswitch.setOnCheckedChangeListener(null);
            if (getStatus(mIndex, mAppListInfo.get(position)) == 1) {
                view.aswitch.setChecked(true);
            } else {
                view.aswitch.setChecked(false);
            }

            bindView(position, view, convertView);

        }
        return convertView;
    }

    public PackageInfo queryPackage(PackageManager pkgManager, String packageName) {
        List<PackageInfo> packages = pkgManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                return packageInfo;
            }
        }
        return packages.get(0);
    }

    public void bindView(int position, ViewHolder view, View convertView) {
        // TODO Auto-generated method stub
        final NetworkAppInfo appInfo = (NetworkAppInfo) mAppListInfo.get(position);
        view.aswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                SparseIntArray disableArray = NetworkDisableUids.getInstance().getDisableUids(mIndex);
                int uid = appInfo.getAppUid();
                boolean isFlag = true;

                if (getStatus(mIndex, appInfo) == 1) {
                    setStatus(mIndex, appInfo, 0);
                    disableArray.put(uid, 1);
                    isFlag = true;
                } else {
                    setStatus(mIndex, appInfo, 1);
                    disableArray.delete(uid);
                    isFlag = false;
                }
                mAppNetworkControl.reflectNetworkControlAction(appInfo.getAppUid(), mIndex, isFlag);

                NetworkDisableUids.getInstance().resetDisableUidsRules(mIndex, disableArray);

                if (mIndex == Constant.MOBILE) {
//                    mAppNetworkControl.singleNetworkControl(mContext, appInfo.getAppPackageName(), isFlag);
                }

//                mListener.onDataChanged();
            }
        });
    }

   /* private String getStringDescri(Context context, int flag) {
        String value = "";
        if (flag == 1) {
            value = context.getString(R.string.action_prohibit);
        } else {
            value = context.getString(R.string.action_allow);
        }
        return value;
    }*/

    private int getStatus(int networkType, NetworkAppInfo appinfo) {
        int status = 0;
        switch (networkType) {
            case Constant.MOBILE:
                status = appinfo.getAppMobileStatus();
                break;

            case Constant.WIFI:
                status = appinfo.getAppWifiStatus();
                break;

            default:
                break;
        }
        return status;
    }

    private void setStatus(int networkType, NetworkAppInfo appInfo, int status) {
        switch (networkType) {
            case Constant.MOBILE:
                appInfo.setAppMobileStatus(status);
                break;

            case Constant.WIFI:
                appInfo.setAppWifiStatus(status);
                break;

            default:
                break;
        }
    }

    private static class ViewHolder {
    	RelativeLayout layout;
        ImageView icon;
        TextView title;
        AuroraSwitch aswitch;
    }
}
