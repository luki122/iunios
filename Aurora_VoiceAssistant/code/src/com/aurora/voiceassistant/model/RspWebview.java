package com.aurora.voiceassistant.model;


public class RspWebview 
{
	private  String url;
	private  String resultName;
	private  int type;
	private  String description;
	
	//private  List<RspWebview> list;
	
	RspWebview()
	{
		//list =new ArrayList<RspWebview>();
	}
	
	public String getDescription() 
	{
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}
	public int getType() 
	{
		return type;
	}
	public void setType(int type) 
	{
		this.type = type;
	}
	public String getUrl() 
	{
		return url;
	}
	public void setUrl(String url) 
	{
		this.url = url;
	}
	public String getResultName() 
	{
		return resultName;
	}
	public void setResultName(String resultName) 
	{
		this.resultName = resultName;
	}
	/*
	public void listAdd(RspWebview node)
	{
		list.add(node);
	}
	public List<RspWebview> getList()
	{
		return list;
	}
	public int getListSize()
	{
		return list.size();
	}
	*/
}
