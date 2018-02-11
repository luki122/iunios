package com.android.phone;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;

import com.android.internal.telephony.Connection;

public class SimIconUtils {
    private static final String LOG_TAG = "SimIconUtils";
    
    public static int getSimIcon(int slot) {
    	int result = -1;
    	if(isIconBySiminfo) {
	        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), slot);
	        if (simInfo != null) {          
	        	result = color2resId(simInfo.mColor); 
	        }
	      	if(result == -1) {
	        	if(slot == 0) {
	        		result = R.drawable.sim1_icon;
		    	} else {
		    		result = R.drawable.sim2_icon;
		    	}	
	    	}
    	} else {
    		result = getSimIconBySlot(slot);
    	}
        
    	return result;
    }
    
    public static int getSmallSimIcon(int slot) {
    	int result = -1;
    	if(isIconBySiminfo) {
	        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), slot);
	        if (simInfo != null) {          
	        	result = color2SmallResId(simInfo.mColor);
	 
	        }
	       	if(result == -1) {
	        	if(slot == 0) {
	        		result = R.drawable.sim1_icon_small;
		    	} else {
		    		result = R.drawable.sim2_icon_small;
		    	}	
	    	}
    	} else {
        	result = getSimIconBySlot(slot);
    	}
        
    	return result;
    }
    
    public static int getIconResFromViewId(int ViewId) {
    	int result = -1 ;    	
    	switch (ViewId) {
	        case R.id.sim_icon_1: {
	     		result = R.drawable.sim1_icon;
	     		break;
	        }
	        case R.id.sim_icon_2: {
	     		result = R.drawable.sim2_icon;
	     		break;
	        }         
	        case R.id.sim_icon_net: {
	     		result = R.drawable.net_icon;
	     		break;
	        }
	        case R.id.sim_icon_home: {
	     		result = R.drawable.home_icon;
	     		break;
	        }
	        case R.id.sim_icon_office: {
	     		result = R.drawable.office_icon;
	     		break;
	        }         
	        case R.id.sim_icon_dial: {
	     		result = R.drawable.phone_icon;
	     		break;
	        }
    	}
    	
    	return result;
    }
    
    
   
    
    public static int resId2color(int resId) {
    	int result = -1 ;    	
    	switch (resId) {
	        case R.drawable.sim1_icon: {
	     		result = 0;
	     		break;
	        }
	        case R.drawable.sim2_icon: {
	     		result = 1;
	     		break;
	        }         
	        case R.drawable.net_icon: {
	     		result = 2;
	     		break;
	        }
	        case R.drawable.home_icon: {
	     		result = 3;
	     		break;
	        }
	        case R.drawable.office_icon: {
	     		result = 4;
	     		break;
	        }         
	        case R.drawable.phone_icon: {
	     		result = 5;
	     		break;
	        }
    	}
    	
    	return result;
    }
    
    public static int getSimIconDefaultPhotoIncoming(int slotid) {
		int result = -1;
    	if(isIconBySiminfo) {
			SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), slotid);
			if (simInfo != null) {			
				switch(simInfo.mColor) {
                  case 0:
				  	result=R.drawable.photo_default_sim1;
				  	break;
				  case 1:
				  	result=R.drawable.photo_default_sim2;
				  	break;
				  case 2:
				  	result=R.drawable.photo_default_sime;
				  	break;
				  case 3:
				  	result=R.drawable.photo_default_simh;
				  	break;
				  case 4:
				  	result=R.drawable.photo_default_simo;
				  	break;
				  case 5:
				  	result=R.drawable.photo_default_simp;
				  	break;

				} 
			}
    	} 
		if(result == -1) {
			if(slotid == 0) {
				result = R.drawable.photo_default_sim1;
			} else {
				result = R.drawable.photo_default_sim2;
			}	
		}
		
		return result;
	}
   

    public static int getSimIconDefaultPhotoOutgoing(int slotid) {
		int result = -1;
    	if(isIconBySiminfo) {
			SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), slotid);
			if (simInfo != null) {			
				switch(simInfo.mColor) {
                  case 0:
				  	result=R.drawable.photo_default_outgoing_sim1;
				  	break;
				  case 1:
				  	result=R.drawable.photo_default_outgoing_sim2;
				  	break;
				  case 2:
				  	result=R.drawable.photo_default_outgoing_sime;
				  	break;
				  case 3:
				  	result=R.drawable.photo_default_outgoing_simh;
				  	break;
				  case 4:
				  	result=R.drawable.photo_default_outgoing_simo;
				  	break;
				  case 5:
				  	result=R.drawable.photo_default_outgoing_simp;
				  	break;

				}
			}
    	}
		if(result == -1) {
			if(slotid == 0) {
				result = R.drawable.photo_default_outgoing_sim1;
			} else {
				result = R.drawable.photo_default_outgoing_sim2;
			}	
		}
		
		return result;
	}

    
    
    public static int getSimIconNotification(int simId) {
		int result = -1;
		int slot = 0;
    	if(isIconBySiminfo) {
			SIMInfo simInfo = SIMInfo.getSIMInfoById(PhoneGlobals.getInstance(), simId);
			if (simInfo != null) {
				slot = simInfo.mSlot;
				switch(simInfo.mColor) {
	              case 0:
				  	result=R.drawable.sim_noti_1;
				  	break;
				  case 1:
				  	result=R.drawable.sim_noti_2;
				  	break;
				  case 2:
				  	result=R.drawable.sim_noti_net;
				  	break;
				  case 3:
				  	result=R.drawable.sim_noti_home;
				  	break;
				  case 4:
				  	result=R.drawable.sim_noti_office;
				  	break;
				  case 5:
				  	result=R.drawable.sim_noti_phone;
				  	break;

				}		
			}
			if(result == -1) {
				if(slot == 0) {
					result = R.drawable.sim_noti_1;
				} else {
					result = R.drawable.sim_noti_2;
				}	
			}
    	}  else {
			slot = AuroraSubUtils.getSlotBySubId(PhoneGlobals.getInstance(), simId);
			if(slot == 0) {
				result = R.drawable.sim_noti_11;
			} else {
				result = R.drawable.sim_noti_22;
			}	
    	}
		
		return result;
	}
    
    public static void setColorForSIM(int resId, int slot) {
        //int[] colors = new int[8];
        SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(), slot);
        if (simInfo!= null) {          
            ContentValues valueColor1 = new ContentValues(1);
            valueColor1.put(SimInfo.COLOR, resId);
            PhoneGlobals.getInstance().getContentResolver().update(ContentUris.withAppendedId(SimInfo.CONTENT_URI, simInfo.mSimId), 
                        valueColor1, null, null);   
            Log.v(LOG_TAG,"setColorForNewSIM SimInfo simColorRes is " + resId);
        }
    }
    
    public static int getNewCardSimColor(int slot) {
    	Context ctx = PhoneGlobals.getInstance();
        int insertCount = SIMInfo.getInsertedSIMCount(ctx);
        if(insertCount > 1) {
        	SIMInfo otherSimInfo = SIMInfo.getSIMInfoBySlot(ctx, AuroraPhoneUtils.getOtherSlot(slot));
            if(otherSimInfo.mColor == AuroraMSimConstants.SUB1) {
            	return AuroraMSimConstants.SUB2;
            } else {
            	return AuroraMSimConstants.SUB1;
            }
        } else {
        	return slot;
        }
    }
    
	  public static boolean isIconBySiminfo = false;
	  
	  private static int getSimIconBySlot(int slot) {
	    	if(slot == 0) {
      		return R.drawable.smallsim1;
	    	} else {
	    		return R.drawable.smallsim2;
	    	}	
	  }
	  
	  private static int color2resId(int color) {
	    	int result = -1 ;    	
	    	switch (color) {
		        case 0: {
		     		result = R.drawable.sim1_icon;
		     		break;
		        }
		        case 1: {
		     		result = R.drawable.sim2_icon;
		     		break;
		        }         
		        case 2: {
		     		result = R.drawable.net_icon;
		     		break;
		        }
		        case 3: {
		     		result = R.drawable.home_icon;
		     		break;
		        }
		        case 4: {
		     		result = R.drawable.office_icon;
		     		break;
		        }         
		        case 5: {
		     		result = R.drawable.phone_icon;
		     		break;
		        }
	    	}
	    	
	    	return result;
	    }
	    
	    private static int color2SmallResId(int color) {
	    	int result = -1 ;    	
	    	switch (color) {
		        case 0: {
		     		result = R.drawable.sim1_icon_small;
		     		break;
		        }
		        case 1: {
		     		result = R.drawable.sim2_icon_small;
		     		break;
		        }         
		        case 2: {
		     		result = R.drawable.net_icon_small;
		     		break;
		        }
		        case 3: {
		     		result = R.drawable.home_icon_small;
		     		break;
		        }
		        case 4: {
		     		result = R.drawable.office_icon_small;
		     		break;
		        }         
		        case 5: {
		     		result = R.drawable.phone_icon_small;
		     		break;
		        }
	    	}
	    	
	    	return result;
	    }
	
	
}