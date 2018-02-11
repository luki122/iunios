package com.gionee.autommi;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GPSTest extends BaseActivity implements LocationListener, Listener {
	public static final String TAG = "GPSTest";
	LocationManager mgr;
	String prefered;
	private TextView tip;
	private TextView tip2;
	private TextView tip3;
    private int nosta;
    private String res;
	
	
	private Handler hd = new Handler() {
		private int count = 0;
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					count ++;
					String c = "用时 : " + count + "\n";
					tip.setText(c);
					this.sendEmptyMessageDelayed(1, 1000);
					break;
			}
		}
	};
	private PowerManager pm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip);
		tip2 = (TextView) this.findViewById(R.id.t2);
		tip2.setVisibility(View.VISIBLE);
		tip3 = (TextView) this.findViewById(R.id.t3);
		tip3.setVisibility(View.VISIBLE);
		initView();
		mgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                LocationManager.GPS_PROVIDER, true);
		for (String prov : mgr.getAllProviders()) {
			Log.d(TAG, "provider : " + prov);
		}	
		pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
	}

	private void initView() {
		// TODO Auto-generated method stub
		String c;
		c = "用时 ： " + "\n";
		tip.setText(c);
		c = "经度 : "  + "\n"
			     + "维度 : " +  "\n" 
	             + "海拔 : " + "\n";
		tip2.setText(c);
		c = "卫星数目 : ";
		tip3.setText(c);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		mgr.addGpsStatusListener(this);
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
		startCount();
	}

	private void startCount() {
		// TODO Auto-generated method stub
		hd.sendEmptyMessageDelayed(1,1000);
	}
	
	private void stopCount() {
		hd.removeMessages(1);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mgr.removeUpdates(this);
		mgr.removeGpsStatusListener(this);
		  Settings.Secure.setLocationProviderEnabled(getContentResolver(),
	                LocationManager.GPS_PROVIDER, false);
		 stopCount();
		 this.finish();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
        double la = location.getLatitude();
        double lo = location.getLongitude(); 
        double al = location.getAltitude();
		String c  = "经度 : " + la + "\n"
				     + "维度 : " + lo + "\n" 
		             + "海拔 : " + al + "\n";
	    if(null == res) { 	
             res = "" + nosta + "|" + la + "|" + lo + "|" + al;
		    ((AutoMMI)getApplication()).recordResult(TAG, res, "1");
        }
		tip2.setText(c);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, provider + " is enabled ", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(this, provider + " is disabled ", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
		switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				GpsStatus s = mgr.getGpsStatus(null);
				Iterator<GpsSatellite> it = s.getSatellites().iterator();
				int count = 0;
				StringBuilder sb = new StringBuilder();
				while(it.hasNext()) {
					count ++;
					GpsSatellite info = it.next();
					sb.append("卫星 " + count + " 信噪比: " + info.getSnr() + "\n");
				}
                nosta = count;
				tip3.setText(sb.toString());
				break;
		}	
	}	
	
	public boolean dispatchKeyEvent(KeyEvent event){
		return true;
	}
}
