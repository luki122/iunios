package com.aurora.voiceassistant.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;

public class RspXmlNb 
{
	public static final int RANK_TYPE_PRICE = 0;
	public static final int RANK_TYPE_DISTA = 1;
	public static final int RANK_TYPE_STAR = 2;
	public static final int RANK_TYPE_SCORE = 3;
	
	private String retState;
	private String sourceType;
	private String rankType;
	
	private ArrayList<RspXmlNb.Item> list = new ArrayList<RspXmlNb.Item>();
	
	
	public class Item
	{
		private String shopName;//店名
		private String star;//星级
		private String avgPrice;//人均
		private String distance;//距离
		private String category;//类别
		
		private String address;//店铺地址
		private String coordUrl;//在搜狗地图上的位置
		private String leftUrl;//到这里去 
		private String gotoUrl;//从这里出发
		private String phoneNo;//电话
		
		private String dishTags;//推荐菜
		private String score;//推荐值
		
		/*//查看 
		 * 查看联系人界面
		 * 联系人名称 ：店名
		 * 号码：店铺电话
		 */
		/*//转发  
		 * 发短信界面
		 * 短信内容:店名+地址+电话
		 * 
		 */
		private String shopUrl;//更多
		
		public String getShopName() 
		{
			return shopName;
		}

		public void setShopName(String shopName) 
		{
			this.shopName = shopName;
		}

		public String getStar() 
		{
			return star;
		}

		public void setStar(String star) 
		{
			this.star = star;
		}

		public String getAvgPrice() 
		{
			return avgPrice;
		}

		public void setAvgPrice(String avgPrice) 
		{
			if(avgPrice==null||avgPrice!=null&&"NULL".equals(avgPrice.toUpperCase())){
				avgPrice = "暂无";
			}
			this.avgPrice = avgPrice;
		}

		public String getDistance() 
		{
			return distance;
		}

		public void setDistance(String distance) 
		{
			this.distance = distance;
		}

		public String getCategory() 
		{
			return category;
		}

		public void setCategory(String category) 
		{
			this.category = category;
		}

		public String getAddress() 
		{
			return address;
		}

		public void setAddress(String address) 
		{
			this.address = address;
		}

		public String getCoordUrl() 
		{
			return coordUrl;
		}

		public void setCoordUrl(String coordUrl) 
		{
			this.coordUrl = coordUrl;
		}

		public String getLeftUrl() 
		{
			return leftUrl;
		}

		public void setLeftUrl(String leftUrl) 
		{
			this.leftUrl = leftUrl;
		}

		public String getGotoUrl() 
		{
			return gotoUrl;
		}

		public void setGotoUrl(String gotoUrl) 
		{
			this.gotoUrl = gotoUrl;
		}

		public String getPhoneNo() 
		{
			return phoneNo;
		}

		public void setPhoneNo(String phoneNo) 
		{
			this.phoneNo = phoneNo;
		}

		public String getDishTags() 
		{
			return dishTags;
		}

		public void setDishTags(String dishTags) 
		{
			//shigq add
			String aString = "";
			if (dishTags != null) {
				aString=dishTags.toUpperCase();
			}
			if ("NULL".equals(aString)) {
				dishTags = "暂无";
			}else{
				dishTags = dishTags.replaceAll("[,][0-9]*[|]", " ");
			}
			//shigq add
			this.dishTags = dishTags;
		}

		public String getShopUrl() 
		{
			return shopUrl;
		}

		public void setShopUrl(String shopUrl) 
		{
			this.shopUrl = shopUrl;
		}
		public String getScore() 
		{
			return score;
		}

		public void setScore(String score) 
		{
			this.score = score;
		}
	}
	
	public String getRetState() 
	{
		return retState;
	}

	public void setRetState(String retState) 
	{
		this.retState = retState;
	}

	public String getSourceType() 
	{
		return sourceType;
	}

	public void setSourceType(String sourceType) 
	{
		this.sourceType = sourceType;
	}

	public String getRankType() 
	{
		return rankType;
	}

	public void setRankType(String rankType) 
	{
		this.rankType = rankType;
	}
	
	
	public void listAdd(RspXmlNb.Item node)
	{
		list.add(node);
	}
	
	public int getListSize()
	{
		return list.size();
	}
	public ArrayList<RspXmlNb.Item> getList()
	{
		return list;
	}
	public void sortByDistance()
	{
		Collections.sort(list, new ComparatorUtil(RANK_TYPE_DISTA));
	}
	public void sortByStar()
	{
		Collections.sort(list, new ComparatorUtil(RANK_TYPE_STAR));
	}
	public void sortByScore()
	{
		Collections.sort(list, new ComparatorUtil(RANK_TYPE_SCORE));
	}
	
	public void sortByPrice()
	{
		Collections.sort(list, new ComparatorUtil(RANK_TYPE_PRICE));
	}
	
	
	public class ComparatorUtil implements Comparator<RspXmlNb.Item>
	{
		private int rankType = 0;
		
		ComparatorUtil(int rankType)
		{
			this.rankType = rankType;
		}
		
		
	    public int compare(RspXmlNb.Item obj1, RspXmlNb.Item obj2) 
	    { 
	    	float a = -1;
	    	float b = -1;
	    	switch(rankType)
	    	{
		    	case RANK_TYPE_PRICE:
		    		if (obj1.getAvgPrice() != null && !"暂无".equals(obj1.getAvgPrice())) {
	    				a = Float.valueOf(obj1.getAvgPrice());
	    			}
		    		if (obj2.getAvgPrice() != null && !"暂无".equals(obj2.getAvgPrice())) {
	    				b = Float.valueOf(obj2.getAvgPrice());
	    			}
		    		break;
		    	case RANK_TYPE_DISTA:
		    		if (obj1.getDistance() != null && !"暂无".equals(obj1.getDistance())) {
	    				a = Float.valueOf(obj1.getDistance());
	    			}
		    		if (obj2.getDistance() != null && !"暂无".equals(obj2.getDistance())) {
	    				b = Float.valueOf(obj2.getDistance());
	    			}
		    	    break;
		    	case RANK_TYPE_STAR:
		    		if (obj1.getStar() != null && !"暂无".equals(obj1.getStar())) {
	    				a = Float.valueOf(obj1.getStar());
	    			}
		    		if (obj2.getStar() != null && !"暂无".equals(obj2.getStar())) {
	    				b = Float.valueOf(obj2.getStar());
	    			}
		    		break;
		    	case RANK_TYPE_SCORE:
		    		if (obj1.getScore() != null && !"暂无".equals(obj1.getScore())) {
	    				a = Float.valueOf(obj1.getScore());
	    			}
		    		if (obj2.getScore() != null && !"暂无".equals(obj2.getScore())) {
	    				b = Float.valueOf(obj2.getScore());
	    			}
		    		break;
	    	}
	    	if (rankType == RANK_TYPE_SCORE) {
	    		if (a < b) {
	    			return 1;
	    		} else if (a > b){
					return -1;
				} else {
					return 0;
				}
	    	} else {
		    	if (a > b) {
	    			return 1;
	    		} else if (a < b){
					return -1;
				} else {
					return 0;
				}
	    	}
	    }
	}
	
}
