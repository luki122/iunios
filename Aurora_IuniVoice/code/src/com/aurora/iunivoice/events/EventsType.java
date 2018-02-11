package com.aurora.iunivoice.events;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * 事件类型定义
 * @author JimXia
 *
 * @date 2014-9-28 下午4:58:51
 */
public class EventsType {
	
   private static volatile EventsType instance;
   private final ArrayList<String> eventsTypeList = new ArrayList<String>();
   
   // 所有的事件类型名必须以这个前缀开头
   private static final String EVENT_TYPE_NAME_PREFIX = "EVENT_";
   
   //action name must be in namespace
   public static final String EVENT_UPDATE_NICKNAME = "com.aurora.account.updateNickname";
   public static final String EVENT_UPDATE_PHONE = "com.aurora.account.updatePhone";
   public static final String EVENT_UPDATE_ACCOUNT_ICON = "com.aurora.account.updateAccountIcon";
   
   /**
    * !!! 所有系统的事件都在此加入事件列表
    */
   private EventsType() {
       initEventsTypeList();
//	   eventsTypeList.add(EVENT_UPDATE_NICKNAME);
   }
   
   private void initEventsTypeList() {
       Field[] allFields = getClass().getDeclaredFields();
        for (Field field : allFields) {
            if (field.getName().startsWith(EVENT_TYPE_NAME_PREFIX)) {
                try {
                    String fieldValue = field.get(this).toString();
                    if (!EVENT_TYPE_NAME_PREFIX.equals(fieldValue)) {
                        eventsTypeList.add(fieldValue);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
   }
   
   public static EventsType getInstance() {
	    if (instance == null) {
	        instance = new EventsType();
	    }
	    return instance;
   }
   
   public boolean contains(String eventType) {
	 return eventsTypeList.contains(eventType);
   }
}
