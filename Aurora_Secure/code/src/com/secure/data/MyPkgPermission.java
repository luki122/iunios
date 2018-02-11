package com.secure.data;

import com.alibaba.fastjson.JSONObject;

/**
 * 记录某个应用 
 * 1.哪些权限应用接受，
 * 2.哪些权限应该拒绝，
 * 3.哪些要提示
 * @author chengrq
 *
 */
public class MyPkgPermission extends BaseData {
	private String packageName;
	private long permissionsAccept;
	private long permissionsReject;
	private long permissionsPrompt;

	public MyPkgPermission() {
		super("MyPkgPermission");
	}
	
	public void setPackageName(String packageName){
		this.packageName = packageName;
	}
	
	public void setPermissionsAccept(long permissionsAccept){
		this.permissionsAccept = permissionsAccept;
	}
	
	public void setPermissionsReject(long permissionsReject){
		this.permissionsReject = permissionsReject;
	}
	
	public void setPermissionsPrompt(long permissionsPrompt){
		this.permissionsPrompt = permissionsPrompt;
	}
		
	public String getPackageName(){
		return this.packageName;
	}
	
	public long getPermissionsAccept(){
		return this.permissionsAccept;
	}
	
	public long getPermissionsReject(){
		return this.permissionsReject;
	}
	
	public long getPermissionsPrompt(){
		return this.permissionsPrompt;
	}
	
	public JSONObject getJson() {
		JSONObject json = new JSONObject();	
		json.put("packageName", packageName);
		json.put("permissionsAccept", permissionsAccept);
		json.put("permissionsReject", permissionsReject);
		json.put("permissionsPrompt", permissionsPrompt);
		return json;
	}
	
	/**
	 * 解析json对象
	 * @param json
	 * @return true 解析成功  false 解析失败
	 */
	public boolean parseJson(JSONObject json) throws Exception{	
		boolean result = false;
		  if (json != null && !json.isEmpty()) {
			  packageName = json.getString("packageName");
			  permissionsAccept = json.getLongValue("permissionsAccept");
			  permissionsReject = json.getLongValue("permissionsReject");
			  permissionsPrompt = json.getLongValue("permissionsPrompt");
			  result = true;			 			 
		  }		  
		  return result;
	}
	
	

}
