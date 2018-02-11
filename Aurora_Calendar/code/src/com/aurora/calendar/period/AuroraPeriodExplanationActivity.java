package com.aurora.calendar.period;

import com.android.calendar.R;

import android.os.Bundle;
import aurora.app.AuroraActivity;

public class AuroraPeriodExplanationActivity extends AuroraActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.aurora_period_explanation_activity);
		getAuroraActionBar().setTitle(R.string.aurora_period_explanation);
	}

}
