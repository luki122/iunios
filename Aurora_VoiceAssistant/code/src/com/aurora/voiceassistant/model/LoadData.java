package com.aurora.voiceassistant.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LoadData
{
	//For scrollView
	private int resultType;
	private QuizText requestText;
	private Response mResponse;
	//For scrollView
	
	private List<Item> list = new ArrayList<Item>();
	
	public class Item
	{
		private int type;
		private QuizText quiztext;
		private Response res;
		
		public int getType() 
		{
			return type;
		}
		public void setType(int type) 
		{
			this.type = type;
		}
		public QuizText getQuiztext() 
		{
			return quiztext;
		}
		public void setQuiztext(QuizText quiztext) 
		{
			this.quiztext = quiztext;
		}
		public Response getResponse() 
		{
			return res;
		}
		public void setResponse(Response response) 
		{
			this.res = response;
		}
	}
	
	public List<Item> getList() 
	{
		return list;
	}
	public int getListSize()
	{
		return list.size();
	}
	
	public void listAdd(int type,QuizText quiztext,Response response)
	{
		Item node = new Item();
		node.setQuiztext(quiztext);
		node.setResponse(response);
		node.setType(type);
		
		list.add(node);
		
		//For scrollView
		resultType = type;
		requestText = quiztext;
		mResponse = response;
		//For scrollView
	}
	public void listItemHidden()
	{
		Iterator<Item> iter = list.iterator();
		while(iter.hasNext())
		{
			Item item = iter.next();
			if(CFG.VIEW_TYPE_RES == item.getType())
			{
				Response rsp = item.getResponse();
				rsp.setVisible(false);
			}
		}
	}
	
	//For scrollView
	public int getResultType() {
		return resultType;
	}
	
	public QuizText getRequestText() {
		return requestText;
	}
	
	public Response getResponse() {
		return mResponse;
	}
	
	public void setResultType(int type) {
		resultType = type;
	}
	
	public void setRequestText(String string) {
		requestText.setQuiz(string);
	}
	//For scrollView
}
