package com.aurora.puremanager.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.utils.Consts;
import com.aurora.puremanager.utils.PowerTimeUtil;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

/**
 * Created by joy on 12/17/15.
 */
public class SuperStandbyAcitivity extends AuroraActivity implements View.OnClickListener {

    private Context mContext;
    public static final String LEVEL_KEY = "power_level";
    private int percent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setAuroraContentView(R.layout.activity_super_standby, AuroraActionBar.Type.Normal);
        initActionBar();
        initData();
        initViews();
        setListeners();
    }

    private void initViews() {
        ((TextView)findViewById(R.id.standby_hint)).setText(PowerTimeUtil.getTimeStrInSuperMode(percent));
    }

    private void initData() {
        percent = getIntent().getIntExtra(LEVEL_KEY, 0);
        if (percent == 0) {
            finish();
        }
    }

    private void setListeners() {
        findViewById(R.id.enter_super_ll).setOnClickListener(this);
    }

    public void initActionBar() {
        AuroraActionBar bar = getAuroraActionBar();
        bar.setBackgroundResource(R.color.power_green);
        bar.setTitle(R.string.long_lift_standby);
    }

    private void intoSuperSaveMode(Context context) {
        Intent intent = new Intent(context, WaitingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("power_flag", 0);
        bundle.putInt("from", Consts.NONE_MODE);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enter_super_ll:
                intoSuperSaveMode(mContext);
                break;

            default:
                break;
        }
    }
}