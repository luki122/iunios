package interfaces;

import android.graphics.Canvas;

public interface IWeatherAnim {
	
	public void draw(Canvas canvas);
	
	public void onPause();
	
	public void onResume();
}
