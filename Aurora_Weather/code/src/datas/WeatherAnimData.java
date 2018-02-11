package datas;

/**
 * rely on weather type
 * @author tangjun
 *
 */
public class WeatherAnimData {
	
	//anim class name
	private String className;
	
	//background
	private int[] backgroundResids;
	
	//thumb in city list fragment,day and night
	private int mThumb[];
	
	private String mWidgetWeatherType;
	
	private int mWidgetThumb;
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public int[] getBackgroundResids() {
		return backgroundResids;
	}
	
	public void setBackgroundResids(int[] backgroundResids) {
		this.backgroundResids = backgroundResids;
	}
	
	public void setThumbID(int id[])
	{
		mThumb = id;
	}
	
	public int[] getThumbID()
	{
		return mThumb;
	}
	
	public String getWidgetWeatherType() {
		return mWidgetWeatherType;
	}
	
	public void setWidgetWeatherType(String widgetWeatherType) {
		this.mWidgetWeatherType = widgetWeatherType;
	}
	
	public void setWidgetThumbID(int id)
	{
		mWidgetThumb = id;
	}
	
	public int getWidgetThumbID()
	{
		return mWidgetThumb;
	}
}
