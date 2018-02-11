package com.aurora.puremanager.viewcache;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.puremanager.R;

public class PureBackgroundListItemCache {
    private View baseView;
    private ImageView appIcon;
    private TextView appName;
    private TextView subText;

    public PureBackgroundListItemCache(View baseView) {
        this.baseView = baseView;
    }

    public ImageView getAppIcon() {
        if (appIcon == null) {
            appIcon = (ImageView) baseView.findViewById(R.id.appIcon);
        }
        return appIcon;
    }

    public TextView getAppName() {
        if (appName == null) {
            appName = (TextView) baseView.findViewById(R.id.appName);
        }
        return appName;
    }

    public TextView getSubText() {
        if (subText == null) {
            subText = (TextView) baseView.findViewById(R.id.subText);
        }
        return subText;
    }
}
