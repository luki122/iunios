package com.privacymanage.data;

/**
 * 账户完整信息
 *
 */
public class AccountData extends AidlAccountData{
	 public final static int NOM_ACCOUNT = 0; 
	 public final static int NO_ENTERED = 1;
	 public final static int ENTERED = 0;
     private String password;
     private String eMail;
     private long createTime;
     private int state = NO_ENTERED;
     
     public void setPassword(String password){
    	 this.password = password;
     }
     
     public void setEMail(String eMail){
    	 this.eMail = eMail;
     }
     
     /**
      * 当前账户的状态
      * @param state 
      *  (1)NO_ENTERED 表示该账户还没有进入过
      *  (2)ENTERED 表示该账户已经进入过	
      */
     public void setState(int state){
    	 this.state = state;
     }
     
     /**
      * 账户创建时间
      * @param createTime
      */
     public void setCreateTime(long createTime){
    	 this.createTime = createTime;
     }

     /**
      * 账户创建时间
      * @return
      */
     public long getCreateTime(){
    	 return this.createTime;
     }
     
     public String getPassword(){
    	 return this.password;
     }
     
     public String getEMail(){
    	 return this.eMail;
     }
     
     /**
      * 当前账户的状态
      * @return       
      *  (1)NO_ENTERED 表示该账户还没有进入过
      *  (2)ENTERED 表示该账户已经进入过
      */
     public int getState(){
    	 return this.state;
     }
     
     public void copy(AccountData tmpAccountData){
    	 if(tmpAccountData == null){
    		 return ;
    	 }
    	 setAccountId(tmpAccountData.getAccountId());
         setCreateTime(tmpAccountData.getCreateTime());
         setEMail(tmpAccountData.getEMail());
         setHomePath(tmpAccountData.getHomePath());
         setPassword(tmpAccountData.getPassword());
         setState(tmpAccountData.getState());
     }
     
     public void reset(){
    	 setAccountId(0);
         setCreateTime(0);
         setEMail("");
         setHomePath("");
         setPassword("");
         setState(NO_ENTERED);
     }
	
}
