package datas;

/**
 * city information
 * @author liuwei
 *
 */
public class WeatherCityInfo {
	
	//name
	private String mName;
	
	//id for get Internet data
	private int mID; 
	
	//is local city
	private boolean mIsLocalCity;
	
	public void setCityName(String name)
	{
		mName = name;
	}
	
	public String getCityName()
	{
		return mName;
	}
	
	/**
	 * we should get id throw City DataBase
	 * @param id
	 */
	public void setID(int id)
	{
		mID = id;
	}
	
	public int getID()
	{
		return mID;
	}
	
	public void setIsLocalCity(boolean isLocalCity)
	{
		mIsLocalCity = isLocalCity;
	}
	
	public boolean getIsLocalCity()
	{
		return mIsLocalCity;
	}
}
