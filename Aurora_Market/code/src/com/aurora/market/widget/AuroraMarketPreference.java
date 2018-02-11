package com.aurora.market.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import aurora.preference.AuroraPreference;

import com.aurora.market.R;

public class AuroraMarketPreference extends AuroraPreference {

	private View view;
	private int sum = 0;
	// private TextView ut;
	public AuroraMarketPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

	}

	public AuroraMarketPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected View onCreateView(ViewGroup pViewGroup) {
		// TODO Auto-generated method stub
		setWidgetLayoutResource(R.layout.market_manager_pref_layout);

		view = super.onCreateView(pViewGroup);
		TextView ut = (TextView) view.findViewById(R.id.message);

		if (sum > 0) {
			ut.setVisibility(View.VISIBLE);
			ut.setText(String.valueOf(sum));
		} else {
			ut.setVisibility(View.GONE);
			ut.setText(String.valueOf(0));
		}
		setView(view);
		// Log.i("AuroMarketpreference", "the OncreateView");
		return view;
	}

	@Override
	protected void onBindView(View arg0) {
		// TODO Auto-generated method stub
		super.onBindView(arg0);
		// Log.i("AuroMarketpreference", "the onBindView");

	}

	private void setView(View view) {
		this.view = view;
	}

	private View getView() {
		return view;
	}

	public void setSum(int sum)
	{
		this.sum = sum;
	}
	
	public void setDisUpSum(int sum) {
		View v = getView();
		if (v == null) {
			return;
		}
		TextView ut = (TextView) v.findViewById(R.id.message);

		if (sum > 0) {
			ut.setVisibility(View.VISIBLE);
			ut.setText(String.valueOf(sum));
		} else {
			ut.setVisibility(View.GONE);
			ut.setText(String.valueOf(0));
		}
	}
}
