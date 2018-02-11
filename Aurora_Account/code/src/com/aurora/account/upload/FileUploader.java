package com.aurora.account.upload;

import android.content.Context;
import android.util.Log;

import com.aurora.account.bean.UploadDataResult;
import com.aurora.account.bean.accessoryInfo;
import com.aurora.account.contentprovider.AccountsAdapter;
import com.aurora.account.db.ExtraFileUploadDao;
import com.aurora.account.http.HttpRequstData;
import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.util.AccountPreferencesUtil;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Globals;
import com.aurora.account.util.SystemUtils;
import com.aurora.datauiapi.data.bean.UpFileResultObject;

import org.apache.http.conn.ConnectTimeoutException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @ClassName: FileUploader
 * @Description: 文件上传类
 * @author jason
 * @date 2014年10月14日 下午4:13:27
 * 
 */
public class FileUploader implements Runnable {

	public static final String TAG = "FileUploader";

	public static final int STATUS_DEFAULT = 0; // 默认
	public static final int STATUS_WAIT = STATUS_DEFAULT + 1; // 等待
	public static final int STATUS_CONNECTING = STATUS_WAIT + 1; // 连接
	public static final int STATUS_UPLOADING = STATUS_CONNECTING + 1; // 正在上传
	public static final int STATUS_PAUSE = STATUS_UPLOADING + 1; // 暂停
	public static final int STATUS_PAUSE_NEED_CONTINUE = STATUS_PAUSE + 1; // 暂停需要继续
	public static final int STATUS_NO_NETWORK = STATUS_PAUSE_NEED_CONTINUE + 1; // 暂无网络
	public static final int STATUS_CONNECT_TIMEOUT = STATUS_NO_NETWORK + 1; // 连接超时
	public static final int STATUS_CONNECT_RETRY = STATUS_CONNECT_TIMEOUT + 1; // 重试
	public static final int STATUS_FAIL = STATUS_CONNECT_RETRY + 1; // 上传失败
	public static final int STATUS_FINISH = STATUS_FAIL + 1; // 上传完成

	private String m_packageName, m_uri,m_apptype; // 下载数据
	private String id;  //一条数据的标示
	private accessoryInfo accinfo; // 下载路径
	private Context m_context;
	private CountDownLatch m_countdown;
	private long uploadSize = 0; // 已下载文件长度

	private long createTime = 0; // 任务创建时间




	private int status = STATUS_DEFAULT; // 当前状态

	private boolean cancleFlag = false; // 取消标识
	private boolean retred = false; // 是否已经连接重试
	private boolean needRetry = false;
	private ExtraFileUploadDao dao;
	
	private UploadDataResult result = new UploadDataResult();
	/**
	 * 构建文件下载器
	 * 
	 * @param downloadData
	 *            下载信息
	 * @param fileSaveDir
	 *            文件保存目录
	 */
	public FileUploader(String packageName, String uri,String app_type, String id,accessoryInfo accinfo,
		 Context context,
			CountDownLatch countdown) {
		init(packageName, uri,app_type, id,accinfo, context, countdown);
	}

	@Override
	public void run() {
		startUpload();

	}

	/**
	 * 构建FileDownloader信息
	 * 
	 * @param downloadData
	 * @param fileSaveDir
	 * @param listener
	 */
	private void init(String packageName, String uri,String app_type,String id, accessoryInfo accinfo,
			 Context context,
			CountDownLatch countdown) {
		Log.i(TAG, "FileDownloader init: uri->" + packageName);
		this.m_packageName = packageName;
		this.m_uri = uri;
		this.m_apptype = app_type;
		this.id = id;

		this.accinfo = accinfo;
		this.m_context = context;
		this.m_countdown = countdown;
		
		
		this.dao = ExtraFileUpService.getExtraFileUploadDao();
		
		if (dao != null) {
			// 存在记录
			if (dao.isExist(id))
			{
				result = dao.getUpDataInfo(id);
			}
			else
			{
				result.setAccessoryid(accinfo.getAccessoryid());
				result.setFile_path(accinfo.getPath());
				result.setId(id);
				result.setPackage_name(m_packageName);
				result.setStatus(getStatus());
				result.setCreate_time(String.valueOf(System.currentTimeMillis()));
				
				createNewRecord();
			}
		}
			
		
	}

	/**
	 * 增加新记录
	 * 
	 */
	private void createNewRecord() {
		Log.i(TAG, "createNewRecord()");
		dao.insert(result);
	}

	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 * 
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */
	private void post(String actionUrl, Map<String, String> params,InputStream is,String filename,int length) {
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";
		needRetry = false;
		setStatus(STATUS_CONNECTING);

		DataOutputStream outStream = null;

		HttpURLConnection conn = null;
		
		UpFileResultObject up_result = new UpFileResultObject();
		try {
			URL uri = new URL(actionUrl);
			conn = (HttpURLConnection) uri.openConnection();
			//conn.setReadTimeout(15 * 1000); // 缓存的最长时间
			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST");
			// conn.setRequestProperty("Accept", "*/*");
			// conn.setRequestProperty("Accept-Language", "zh-cn");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
					+ ";boundary=" + BOUNDARY);
			
			//conn.setRequestProperty("Content-Range", "bytes " + 0 + "-" + (length-1) + "/" + length);//每次上传200K，或最后一段大小
          //conn.setRequestProperty("Session-ID", AccountPreferencesUtil.getUserKey());
			
          conn.connect(); 
			
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINEND);
				sb.append("Content-Disposition: form-data; name=\""
						+ entry.getKey() + "\"" + LINEND);
				sb.append("Content-Type: text/plain; charset=" + CHARSET +
				 LINEND);
				sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
				sb.append(LINEND);
				sb.append(entry.getValue());
				sb.append(LINEND);
			}
			
			StringBuilder sb1 = new StringBuilder();
			sb1.append(PREFIX);
			sb1.append(BOUNDARY);
			sb1.append(LINEND);
			sb1.append("Content-Length: " + length + LINEND); 
			sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
					+ filename + "\"" + LINEND);
			
			sb1.append("Content-Type: application/octet-stream; charset="
			  + CHARSET + LINEND);
			 
			sb1.append(LINEND);
			

			//int length = is.available();
			//Log.i(TAG, "zhangwei the length=" + length);
			//conn.setRequestProperty("Content-Length", String.valueOf(sb1.toString().getBytes().length+sb.toString().getBytes().length));
			// 首先组拼文本类型的参数
			outStream = new DataOutputStream(conn.getOutputStream());
			outStream.write(sb.toString().getBytes());
			Log.i(TAG, "sb=" + sb.toString());
			FileLog.i(TAG, "sb=" + sb.toString());
			// 发送文件数据
			if (is != null) {
				
				outStream.write(sb1.toString().getBytes());
				Log.i(TAG, "sb1=" + sb1.toString());
				FileLog.i(TAG, "sb1=" + sb1.toString());
				byte[] buffer = new byte[1024];
				int len = 0;
				
				while ((len = is.read(buffer)) != -1) {
					if (status == FileUploader.STATUS_PAUSE
							|| status == FileUploader.STATUS_NO_NETWORK
							|| status == FileUploader.STATUS_CONNECT_TIMEOUT) {
						break;
					}
					
					if (status != FileUploader.STATUS_UPLOADING) {
						setStatus(FileUploader.STATUS_UPLOADING);
						setRetred(false);
					}
					
					
					outStream.write(buffer, 0, len);
					uploadSize += len;
				}
				Log.i(TAG, "zhangwei the count=" + uploadSize);
				is.close();
				outStream.write(LINEND.getBytes());
			}
			// 请求结束标志
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
			outStream.write(end_data);
			outStream.flush();
			// 得到响应码
			StringBuilder sb2 = new StringBuilder();
			
			int res = conn.getResponseCode();
			if (res == 200) {
				InputStream inStream = conn.getInputStream();

				BufferedReader in = new BufferedReader(new InputStreamReader(
						inStream, "UTF-8"));
				String content = "";
				while ((content = in.readLine()) != null) {
					sb2.append(content);
				}
				inStream.close();
				in.close();
				Log.i(TAG, sb2.toString());
				FileLog.i(TAG, sb2.toString());
				ObjectMapper mapper = new ObjectMapper();
				up_result = (UpFileResultObject)mapper.readValue(sb2.toString(),UpFileResultObject.class);
			}
		}  catch (IOException e) {
			if (e instanceof ConnectTimeoutException || e instanceof ConnectException || 
					e instanceof SocketTimeoutException || 
					e instanceof UnknownHostException) {
				setStatus(STATUS_CONNECT_TIMEOUT);
				if (!retred) {
					retred = true;
					setStatus(STATUS_CONNECT_RETRY);
					needRetry = true;
				} else {
					setRetred(false);
					setStatus(STATUS_FAIL);
					ExtraFileUpService.pauseOperation(m_context,0);
					FileLog.e(TAG, "the id="+accinfo.getAccessoryid()+" the type="+ExtraFileUpService.getM_apptype());
				}
			} else {

				if (SystemUtils.hasNetwork()) {
					if (!retred) {
						retred = true;
						setStatus(STATUS_CONNECT_RETRY);
						needRetry = true;
					} else {
						setRetred(false);
						setStatus(STATUS_FAIL);
						ExtraFileUpService.PauseUploadAcc();
						FileLog.e(TAG, "the id="+accinfo.getAccessoryid()+" the type="+ExtraFileUpService.getM_apptype());
					}
				} else {
					setStatus(STATUS_NO_NETWORK);
				}
			}
			FileLog.e(TAG, e.toString());
			e.printStackTrace();
		} finally {


			try {
				if (is != null) {
					is.close();
				}

				if (outStream != null) {
					outStream.close();
				}
			} catch (IOException e) {
				FileLog.e(TAG, e.toString());
				e.printStackTrace();
			}

			conn.disconnect();

		}
		if (needRetry) {
			AccountsAdapter db = new AccountsAdapter(m_context, m_packageName,
					m_uri);
			InputStream is1 = db.getInputStreamByAccessory(m_packageName, accinfo);
			post(actionUrl,params,is1,filename,length);
			return;
		}
		
		// 如果已经下载完成, 删除正在下载数据库中数据
		if (this.status == STATUS_UPLOADING) {
			
			if(up_result.getCode() == UpFileResultObject.CODE_SUCCESS )
			{
				setStatus(STATUS_FINISH);
				dao.updateFileFinishTime(result.getAccessoryid(), System.currentTimeMillis());
			
				ArrayList<accessoryInfo> scc = ExtraFileUpService.getModule_obj().getSycndata().get(ExtraFileUpService.getIndex()).getAccOjb().getAccessory();
				
				FileLog.i(TAG, "STATUS_UPLOADING success the id="+result.getAccessoryid());
				for(int i =0; i < scc.size(); i++)
				{
					if(scc.get(i).getAccessoryid().equals(result.getAccessoryid()))
					{
						scc.get(i).setSyncid(up_result.getAttachId());
						FileLog.i(TAG, "getAttachId success");
						break;
					}
				}
			
			}
		}
		
		dao.updateStatus(result.getAccessoryid(), status);
		dao.updateUploadSize(result.getAccessoryid(), uploadSize);
		
		m_countdown.countDown();
		
	}

	/**
	 * 开始上传
	 * 
	 */
	private void startUpload() {

		

		if (cancleFlag)
			return;

		if (status == STATUS_PAUSE || status == STATUS_NO_NETWORK)
			return;
		Log.i(TAG, "start up");
						 

		
		String url = HttpRequstData.getURLStr(Globals.HTTP_REQUEST_URL,
                Globals.MODULE_ATTACHMENT, Globals.HTTPS_UPLOAD_METHOD);
		
		
		Map<String, String> params = new HashMap<String, String>();

		AccountPreferencesUtil pref = AccountPreferencesUtil.getInstance(m_context);
		params.put("userId", pref.getUserID());
		params.put("userKey", pref.getUserKey());
		params.put("part", m_apptype);

		AccountsAdapter db = new AccountsAdapter(m_context, m_packageName,
				m_uri);
		InputStream is = db.getInputStreamByAccessory(m_packageName, accinfo);
	/*	try
		{
		HttpRequstData.uploadFile(url, params, AccountPreferencesUtil.getUserID(), is);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}*/
		
		int length = 0;
		
		try
		{
			length = is.available();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		String path = accinfo.getPath().replace("/", ".");
		if(null != path)
			path = path.substring(1);
		dao.updateFileSize(result.getAccessoryid(), length);
		post(url, params,is,path,length);

	}

	/**
	 * 获取任务创建时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}

	// ============对外控制方法开始=============//

	/**
	 * 下载文件
	 * 
	 */
	public void uploadFile() {
		setStatus(STATUS_WAIT);

		ThreadPoolExecutor threadPool = FileUploadManage
				.getThreadPoolExecutor();
		threadPool.execute(this);
	}

	/**
	 * 暂停下载
	 * 
	 */
	public void pause() {
		Log.i(TAG, "call pause");

		setStatus(STATUS_PAUSE);
		// dao.updateStatus(extraFileData.getUri(), status);

		ThreadPoolExecutor threadPool = FileUploadManage
				.getThreadPoolExecutor();
		threadPool.remove(this);
	}

	/**
	 * @Title: getStatus
	 * @Description: 得到上传状态
	 * @param @return
	 * @return int
	 * @throws
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @Title: setStatus
	 * @Description: 设置上传状态
	 * @param @param status
	 * @return void
	 * @throws
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	
	/**
	 * 设置是否已经连接重试
	 * 
	 * @param retred
	 */
	public void setRetred(boolean retred) {
		this.retred = retred;
	}

}
