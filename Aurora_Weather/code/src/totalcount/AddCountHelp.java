package totalcount;

import android.content.Context;

public class AddCountHelp {

	public static final String MOUDLE_KEY="270";//天气统计key
	public static final String ENTER_APP="001";//进入应用时统计
	public static final String SELF_REFRESH="002";//主动刷新时统计
	public static final String DELETE_CITY="003";//删除城市时统计
	public static final String SORT_CITY="004";//城市排序时统计
	public static final String ADD_CITY="005";//添加城市时统计
	public static final String ADD_WIDGET="006";//添加Widget时统计
	
	public static void addCount(String action,Context context){
		new TotalCount(context, MOUDLE_KEY, action, 1).CountData();
	}
	
}
