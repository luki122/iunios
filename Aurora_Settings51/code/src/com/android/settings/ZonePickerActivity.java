package com.android.settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

/**
 * Created by joy on 10/9/15.
 */
public class ZonePickerActivity extends AuroraActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAuroraContentView(R.layout.zone_picker_layout, AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.choose_timezone);

        ZonePicker zp = new ZonePicker();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_content, zp, "ZonePicker");
        fragmentTransaction.commit();
    }

}