package com.aurora.voiceassistant.model;

import java.util.ArrayList;
import java.util.List;

public class Response {
	private String realType;
	private String realPoint;
	private String sysTime;
	private String city; 
	private String searchContent;
	private int firstNodeType;
	private boolean visible;
	private List<Item> list  = new ArrayList<Item>();
	public ArrayList<TabData> tabDataArray  = new ArrayList<TabData>();
	
	//For scrollView
	private String requestString;
	private String answerString;
	//For scrollView
	
	public Response() {
		list  = new ArrayList<Item>();
		visible = true;
	}
	
	//For scrollView
	public String getRequestString() {
		return requestString;
	}
	
	public String getAnswerString() {
		return answerString;
	}
	
	public void setRequestString(String string) {
		requestString = string;
	}
	
	public void setAnswerString(String string) {
		answerString = string;
	}
	//For scrollView
	
	public String getRealType() {
		return realType;
	}
	
	public void setRealType(String realType) {
		this.realType = realType;
	}
	
	public String getRealPoint() {
		return realPoint;
	}
	
	public void setRealPoint(String realPoint) {
		this.realPoint = realPoint;
	}
	
	public String getSysTime() {
		return sysTime;
	}
	
	public void setSysTime(String sysTime) {
		this.sysTime = sysTime;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public void listAdd(Item node) {
		list.add(node);
	}
	
	public int getListSize() {
		return list.size();
	}
	
	public List<Item> getList() {
		return list;
	}
	
	public int getFirstNodeType() {
		return firstNodeType;
	}

	public void setFirstNodeType(int firstNodeType) {
		this.firstNodeType = firstNodeType;
	}
	
	public boolean getVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public String getSearchContent() {
		return searchContent;
	}

	public void setSearchContent(String searchContent) {
		this.searchContent = searchContent;
	}
	
	public int getResultType(String resultType,String descri) {
		if(null == resultType || 0 == resultType.length()) {
			return CFG.VIEW_TYPE_NOTHING;
		}
		
		if(CFG.RESULTTYPE_TEXT.equals(resultType)) {
			return CFG.VIEW_TYPE_TEXT;
			
		} else if(CFG.RESULTTYPE_JSON.endsWith(resultType)) {
			return CFG.VIEW_TYPE_JOSN;
			
		} else if(CFG.RESULTTYPE_XML.endsWith(resultType)) {
			if(descri.equals(CFG.TAB_TYPE_XML_TUPIAN)) {
				return CFG.VIEW_TYPE_XML_PIC;
			} else if(descri.equals(CFG.TAB_TYPE_XML_TIANQI)) {
				return CFG.VIEW_TYPE_XML_TIANQI;
			} else if(descri.equals(CFG.TAB_TYPE_XML_NB_SH) || descri.equals(CFG.TAB_TYPE_XML_NB_CS) ) {
				return CFG.VIEW_TYPE_XML_NB;
			} else if(descri.equals(CFG.TAB_TYPE_XML_CX_MSG_LC)) {
				return CFG.VIEW_TYPE_XML_CX_MSG_LC;
			} else if(descri.equals(CFG.TAB_TYPE_XML_CX_KC)) {
				return CFG.VIEW_TYPE_XML_CX_KC;
			} else if(descri.equals(CFG.TAB_TYPE_XML_CX_LC)) {
				return CFG.VIEW_TYPE_XML_CX_LC;
			}
			/*
			else if(descri.equals(CFG.TAB_TYPE_XML_CX_MSG_HB))
			{
				return CFG.VIEW_TYPE_XML_CX_MSG_HB;
			}*/
			else if(descri.equals(CFG.TAB_TYPE_XML_CX_HB)) {
				return CFG.VIEW_TYPE_XML_CX_HB;
			} else if(descri.equals(CFG.TAB_TYPE_XML_BAIKE)) {
				return CFG.VIEW_TYPE_XML_BAIKE;
			} else  {
				return CFG.VIEW_TYPE_XML;
			}
			
		} else if(CFG.RESULTTYPE_WEBVIEW.endsWith(resultType)) {
			return CFG.VIEW_TYPE_WEBVIEW;
			
		} else if (CFG.RESULTTYPE_SOGOUMAP_URL.endsWith(resultType)) {
//			return CFG.VIEW_TYPE_WEBVIEW;
			return CFG.VIEW_TYPE_SOGOUMAP_URL;
			
		} else if (CFG.RESULTTYPE_TEXT_MUSIC.endsWith(resultType)) {
//			return CFG.VIEW_TYPE_WEBVIEW;
//			return CFG.VIEW_TYPE_TEXT_MUSIC;
			
		}
		
		return CFG.VIEW_TYPE_NOTHING;
	}
	
	public class Item {
		private  int type;
		private  RspWebview webview;
		private  RspJson json;
		private  RspText rsptext;
		private  RspXmlPic rspxmlpic;
		private  RspXmlTq rspxmltq;
		private  RspXmlNb rspxmlnb;
		private  RspXmlCxMsgLc rspxmlcxmsglc;
		private  RspXmlBk rspxmlbk;
		private  RspXmlCxKc rspxmlcxkc;
		private  RspXmlCxLc rspxmlcxlc;
		private  RspXmlCxMsgHb rspxmlcxmsghb;
		private  RspXmlCxHb rspxmlcxhb;
		
		public RspXmlCxLc getRspxmlcxlc() {
			return rspxmlcxlc;
		}

		public void setRspxmlcxlc(RspXmlCxLc rspxmlcxlc) {
			this.rspxmlcxlc = rspxmlcxlc;
		}

		public RspWebview getRspwebview() {
			return webview;
		}

		public void setRspwebview(RspWebview webview) {
			this.webview = webview;
		}

		public RspJson getRspjson() {
			return json;
		}

		public void setRspjson(RspJson json) {
			this.json = json;
		}

		public RspText getRestext() {
			return rsptext;
		}

		public void setRestext(RspText rsptext) {
			this.rsptext = rsptext;
		}

		public RspXmlPic getRspxmlpic() {
			return rspxmlpic;
		}

		public void setRspxmlpic(RspXmlPic rspxmlpic) {
			this.rspxmlpic = rspxmlpic;
		}
		
		public RspXmlTq getRspxmltq() {
			return rspxmltq;
		}

		public void setRspxmltq(RspXmlTq rspxmltq) {
			this.rspxmltq = rspxmltq;
		}
		
		public RspXmlNb getRspxmlnb() {
			return rspxmlnb;
		}

		public void setRspxmlnb(RspXmlNb rspxmlnb) {
			this.rspxmlnb = rspxmlnb;
		}
/*
		public QuizText getQuizText() 
		{
			return quizText;
		}

		public void setQuizText(QuizText quizText) 
		{
			this.quizText = quizText;
		}
*/
		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}
		
		public RspXmlCxMsgLc getRspxmlcxmsglc() {
			return rspxmlcxmsglc;
		}

		public void setRspxmlcxmsglc(RspXmlCxMsgLc rspxmlcxmsglc) {
			this.rspxmlcxmsglc = rspxmlcxmsglc;
		}
		
		public RspXmlBk getRspxmlbk() {
			return rspxmlbk;
		}

		public void setRspxmlbk(RspXmlBk rspxmlbk) {
			this.rspxmlbk = rspxmlbk;
		}
		
		public RspXmlCxKc getRspxmlcxkc() {
			return rspxmlcxkc;
		}

		public void setRspxmlcxkc(RspXmlCxKc rspxmlcxkc) {
			this.rspxmlcxkc = rspxmlcxkc;
		}
		
		public RspXmlCxMsgHb getRspxmlcxmsghb() {
			return rspxmlcxmsghb;
		}

		public void setRspxmlcxmsghb(RspXmlCxMsgHb rspxmlcxmsghb) {
			this.rspxmlcxmsghb = rspxmlcxmsghb;
		}

		public RspXmlCxHb getRspxmlcxhb() {
			return rspxmlcxhb;
		}

		public void setRspxmlcxhb(RspXmlCxHb rspxmlcxhb) {
			this.rspxmlcxhb = rspxmlcxhb;
		}
		
	}
}
