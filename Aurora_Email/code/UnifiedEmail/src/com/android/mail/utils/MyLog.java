package com.android.mail.utils;
import java.io.BufferedWriter; 
import java.io.File; 
import java.io.FileWriter; 
import java.io.IOException; 
import java.text.SimpleDateFormat; 
import java.util.Calendar; 
import java.util.Date; 
import android.util.Log; 
  
/** 
 * 带日志文件输入的，又可控开关的日志调试 
 *  
 * @author Julio_chan 
 * @version 1.0 
 * @data 2012-2-20 
 */
public class MyLog { 
	public static boolean MyTest=false;
	
    private static char    LOG_TYPE='v';                            // 输入日志类型，w代表只输出告警信息等，v代表输出所有信息 
    private static Boolean LOG_SWITCH = true;                       // 日志文件总开关 
    private static Boolean LOG_WRITE_TO_FILE = true;                // 日志写入文件开关 
      
    private static int  SDCARD_LOG_FILE_SAVE_DAYS = 0;              // sd卡中日志文件的最多保存天数 
  
    private static String LOGFILENAME = "UM";                  // 本类输出的日志文件名称 
    //private static String LOG_PATH_SDCARD_DIR = "/mnt/sdcard/1MyLog";         // 日志文件在sdcard中的路径
    
    private static String LOG_PATH_SDCARD_DIR = "/storage/sdcard0/1_SQF";
      
    private static SimpleDateFormat LogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   // 日志的输出格式 
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");           // 日志文件格式 
  
    public static void w(String tag, String text) { 
        log(tag, text, 'w'); 
    } 
  
    public static void e(String tag, String text) { 
        log(tag, text, 'e'); 
    } 
  
    public static void d(String tag, String text) { 
        log(tag, text, 'd'); 
    } 
  
    public static void i(String tag, String text) { 
        log(tag, text, 'i'); 
    } 
  
    public static void v(String tag, String text) { 
        log(tag, text, 'v'); 
    } 
    
    public static void i2(String tag, String text) { 
    	RuntimeException e = new RuntimeException("-->This RuntimeException is [JUST FOR TEST]");
    	e.fillInStackTrace();
        log(tag, text, 'i', e); 
    } 
    
    public static void e2(String tag, String text) { 
    	RuntimeException e = new RuntimeException("JustForTest");
    	e.fillInStackTrace();
        log(tag, text, 'e'); 
    } 
  
    /** 
     * 根据tag, msg和等级，输出日志 
     *  
     * @param tag 
     * @param msg 
     * @param level 
     * @return void 
     * @since v 1.0 
     */
    private static void log(String tag, String msg, char level) { 
    	
        if (LOG_SWITCH) { 
            if ('i' == level) { 
                Log.i(tag, msg); 
            } else if ('e' == level) { 
                Log.e(tag, msg); 
            } else if ('w' == level) { 
                Log.w(tag, msg); 
            } else if ('d' == level) { 
                Log.d(tag, msg); 
            }else { 
                Log.v(tag, msg); 
            } 
              
            if (LOG_WRITE_TO_FILE) 
                writeLogtoFile(String.valueOf(level), tag, msg); 
        } 
    } 
    
    private static void log(String tag, String msg, char level, Exception e) { 
    	
        if (LOG_SWITCH) { 
            if ('i' == level) { 
                Log.i(tag, msg, e); 
            } else if ('e' == level) { 
                Log.e(tag, msg, e); 
            } else if ('w' == level) { 
                Log.w(tag, msg, e); 
            } else if ('d' == level) { 
                Log.d(tag, msg, e); 
            }else { 
                Log.v(tag, msg, e); 
            } 
              
            if (LOG_WRITE_TO_FILE) 
                writeLogtoFile(String.valueOf(level), tag, msg + "\n" + e.toString()); 
        } 
    } 
  
    /** 
     * 打开日志文件并写入日志 
     *  
     * @return 
     * **/
    private static void writeLogtoFile(String mylogtype, String tag, String text) { 
//    	if(true)
//    		return;
    	
		try {

			Date nowtime = new Date();
			String needWriteFiel = logfile.format(nowtime);
			String needWriteMessage = LogSdf.format(nowtime) + " " + mylogtype
					+ " " + tag + " " + text;

			
			File dir = new File(LOG_PATH_SDCARD_DIR);
			File file = new File(LOG_PATH_SDCARD_DIR, LOGFILENAME + "_Log.txt");
			if(! dir.exists() || !dir.isDirectory()) {
				dir.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
			bufWriter.close();
			filerWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    } 
  
    /** 
     * 删除制定的日志文件 
     * */
    public static void delFile() { 
        String needDelFiel = logfile.format(getDateBefore()); 
        File file = new File(LOG_PATH_SDCARD_DIR, LOGFILENAME + needDelFiel +".txt"); 
        if (file.exists()) { 
            file.delete(); 
        } 
    } 
  
    /** 
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名 
     * */
    private static Date getDateBefore() { 
        Date nowtime = new Date(); 
        Calendar now = Calendar.getInstance(); 
        now.setTime(nowtime); 
        now.set(Calendar.DATE, now.get(Calendar.DATE) - SDCARD_LOG_FILE_SAVE_DAYS); 
        return now.getTime(); 
    } 
}
