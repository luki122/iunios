package com.baidu.xcloud.pluginAlbum.bean;

/**
 * This class defines error code
 * 
 * @author Baidu SDK & Support Team
 * @version 2.0
 */
public class ErrorCode {
    /** 没有错误 */
    public static final int No_Error = 0;

    /**
     * 默认的错误码 1: 如果传的参数无效，例如，源文件路径不存在 2: 一些未知错误，例如异常 3: 解析PCS服务器返回的 JSON时发生的错误
     */
    public static final int Error_DefaultError = -1;

    /** 不支持此接口 */
    public static final int Error_Unsupported_API = 3;

    /** 没有权限执行此操作 */
    public static final int Error_No_Permission = 4;

    /** IP未授权 */
    public static final int Error_Unauthorized_IP = 5;

    /** 数据库查询错误 */
    public static final int Error_DB_Query = 31001;

    /** 数据库连接错误 */
    public static final int Error_DB_Connect = 31002;

    /** 数据库返回空结果 */
    public static final int Error_DB_Result_Set_Empty = 31003;

    /** 网络错误 */
    public static final int Error_Network = 31021;

    /** 暂时无法连接服务器 */
    public static final int Error_Access_Server = 31022;

    /** 输入参数错误 */
    public static final int Error_Param = 31023;

    /** 应用软件 id为空 */
    public static final int Error_AppId_Empty = 31024;

    /** 后端存储错误 */
    public static final int Error_BCS = 31025;

    /** 用户的cookie不是合法的百度cookie */
    public static final int Error_Invalid_Bduss = 31041;

    /** 用户未登陆 */
    public static final int Error_User_Not_Login = 31042;

    /** 用户未激活 */
    public static final int Error_User_Not_Active = 31043;

    /** 用户未授权 */
    public static final int Error_User_Not_Authorized = 31044;

    /** 用户不存在 */
    public static final int Error_User_Not_Exist = 31045;

    /** 用户已经存在 */
    public static final int Error_User_Already_Exist = 31046;

    /** 文件已经存在 */
    public static final int Error_File_Already_Exist = 31061;

    /** 文件名非法 */
    public static final int Error_File_Name_Invaild = 31062;

    /** 文件父目录不存在 */
    public static final int Error_File_Parent_Path_Not_Exist = 31063;

    /** 无权访问此文件 */
    public static final int Error_File_Not_Authorized = 31064;

    /** 目录已满 */
    public static final int Error_Directory_Null = 31065;

    /** 文件不存在 */
    public static final int Error_File_Not_Exist = 31066;

    /** 文件处理出错 */
    public static final int Error_File_Deal_Failed = 31067;

    /** 文件创建失败 */
    public static final int Error_File_Create_Failed = 31068;

    /** 文件复制失败 */
    public static final int Error_File_Copy_Failed = 31069;

    /** 文件删除失败 */
    public static final int Error_File_Delete_Failed = 31070;

    /** 不能读取文件元信息 */
    public static final int Error_Get_File_Meta_Failed = 31071;

    /** 文件移动失败 */
    public static final int Error_File_Move_Failed = 31072;

    /** 文件重命名失败 */
    public static final int Error_File_Rename_Failed = 31073;

    /** 无法下载目录 */
    public static final int Error_Cannot_Download_Directory = 31074;

    /** 文件创建失败 */
    public static final int Error_SuperFile_Create_Failed = 31081;

    /** 块列表为空 */
    public static final int Error_SuperFile_Block_List_Empty = 31082;

    /** 更新失败 */
    public static final int Error_SuperFile_Update_Failed = 31083;

    /** 系统内部错误 */
    public static final int Error_Tag_Internal = 31101;

    /** 参数错误 */
    public static final int Error_Tag_Param = 31102;

    /** 系统错误 */
    public static final int Error_Tag_Database = 31103;

    /** 未授权设置此目录配额 */
    public static final int Error_Set_Quota_Denied = 31110;

    /** 配额管理只支持两级目录 */
    public static final int Error_Quota_Support_2_Level = 31111;

    /** 超出配额 */
    public static final int Error_Quota_Exceed = 31112;

    /** 配额不能超出目录祖先的配额 */
    public static final int Error_Quota_Bigger_Than_ParentDir = 31113;

    /** 配额不能比子目录配额小 */
    public static final int Error_Quota_Smaller_Than_SubDir = 31114;

    /** 请求缩略图服务失败 */
    public static final int Error_Thumbnail_Failed = 31141;

    /** 用户账号不正确或者已经失效，需要重新登录 */
    public static final int Error_Invalid_Access_Token = 110;
    /** 用户账号已过期，需要刷新一下 */
    public static final int Error_Access_Token_Expired = 111;

    /** 签名错误 */
    public static final int Error_Signature = 31201;

    /** 文件不存在 */
    public static final int Error_Object_Not_Exist = 31202;

    /** 设置访问控制列表失败 */
    public static final int Error_ACL_Put = 31203;

    /** 请求访问控制列表验证失败 */
    public static final int Error_ACL_Query = 31204;

    /** 获取访问控制列表失败 */
    public static final int Error_ACL_Get = 31205;

    /** 访问控制列表不存在 */
    public static final int Error_ACL_Not_Exist = 31206;

    /** 为用户分配的内存块已存在 */
    public static final int Error_Bucket_Already_Exist = 31207;

    /** 用户请求错误 */
    public static final int Error_Bad_Request = 31208;

    /** 服务器错误 */
    public static final int Error_BaiduBS_Internal_Error = 31209;

    /** 服务器不支持 */
    public static final int Error_Not_Support = 31210;

    /** 禁止访问 */
    public static final int Error_Access_Denied = 31211;

    /** 服务不可用 */
    public static final int Error_Service_Unavailable = 31212;

    /** 重试出错 */
    public static final int Error_Retry = 31213;

    /** 上传文件数据失败 */
    public static final int Error_Put_Object_Data = 31214;

    /** 上传文件的文件原信息失败 */
    public static final int Error_Put_Object_Meta = 31215;

    /** 下载文件数据失败 */
    public static final int Error_Get_Object_Data = 31216;

    /** 下载文件的文件原信息失败 */
    public static final int Error_Get_Object_Meta = 31217;

    /** 容量超出限额 */
    public static final int Error_Storage_Exceed_Limit = 31218;

    /** 请求数超出限额 */
    public static final int Error_Request_Exceed_Limit = 31219;

    /** 流量超出限额 */
    public static final int Error_Transfer_Exceed_Limit = 31220;

    /** 服务器返回值非法 */
    public static final int Error_Response_Key_Illegal = 31298;

    /** 服务器返回值不存在 */
    public static final int Error_Response_Key_Not_Exist = 31299;

    /** 秒传文件在服务器上不存在 */
    public static final int Error_CloudMatch_Not_Exist = 31079;

    /** 服务器内部错误 */
    public static final int Error_Internal_Server = 36000;

    /** 参数错误 */
    public static final int Error_Param_CloudDownaload = 36001;

    /** 应用 id为空 */
    public static final int Error_AppId_Is_Empty = 36002;

    /** 账号无效 */
    public static final int Error_Bduss_Is_Invalid = 36003;

    /** 没有权限执行此操作 */
    public static final int Error_No_Permission_CloudDownload = 36004;

    /** 用户未登陆 */
    public static final int Error_User_Not_Login_CloudDownload = 36005;

    /** 用户未激活 */
    public static final int Error_User_Not_Active_CloudDownload = 36006;

    /** 用户未授权 */
    public static final int Error_User_Not_Authorized_CloudDownload = 36007;

    /** 用户不存在 */
    public static final int Error_User_Not_Exsits = 36008;

    /** 用户空间不足 */
    public static final int Error_Storage_Exceed_Limit_CloudDownload = 36009;

    /** 源文件不存在 */
    public static final int Error_Object_Not_exists = 36010;

    /** 不支持此接口 */
    public static final int Error_Unsupported_Api = 36011;

    /** 请求时间戳验证超时 */
    public static final int Error_Request_Expires_Timeout = 36012;

    /** 这个是开发人员组加个基准错误 */
    public static final int Error_Code_Base = 39000;
    /** 非法文件类型 **/
    public static final int Error_Illegal_File_Type = 40000;
    /** 接口类对象参数为空 */
    public static final int Error_File_Client_Null = Error_Code_Base + 1;

    /** 监听器参数为空 */
    public static final int Error_File_Task_Listener_Null = Error_Code_Base + 2;

    /** 创建目录失败 */
    public static final int Error_Make_Dir_Failed = Error_Code_Base + 3;

    /** 本地存在未下载完成的同名文件 */
    public static final int Error_Same_Local_File_Already_Exist = Error_Code_Base + 4;

    /** 服务端存在未上传完成的同名文件 */
    public static final int Error_Same_Remote_File_Already_Exist = Error_Code_Base + 5;

    /** 任务的上一个操作未完成 */
    public static final int Error_Task_Not_Finished = Error_Code_Base + 6;

    /** 用户账号 为空 */
    public static final int Error_Access_Token_Null = Error_Code_Base + 7;

    /** 服务端返回 为空 */
    public static final int Error_Server_Response_Null = Error_Code_Base + 8;

    /**
     * 没有权限访问该文件
     */
    public static final int Error_NO_RIGHT = Error_Code_Base + 9;

    /** RemoteException 为空 */
    public static final int Error_Server_Remote_Exception = Error_Code_Base + 10;

    /** API不支持BDUSS验证方式 */
    public static final int Error_Not_Support_Bduss = Error_Code_Base + 11;
    /**
     * 请求为空
     */
    public static final int Error_Request_Null = Error_Code_Base + 12;
    // begin by huwenan add message for file not exist

    public static final String Message_Remote_Exception = "remote exception";

    /** API不支持BDUSS验证方式 */
    public static final String Message_Not_Support_Bduss = "The API don not support bduss!";

    /** 文件不存在 */
    public static final String Message_File_Not_Exist = "file not exist";
    // end

    // begin by wanghao add message for directory not exist and file size is 0
    /** 目录不存在 */
    public static final String Message_Directory_Not_Exist = "directory not exist";
    /** 文件大小为0 */
    public static final String Message_File_NoSize = "file size is zero";
    // end

    /** 秒传成功 */
    public static final String Message_Cloud_Upload_Success = "cloud upload success";

    /** 文件名非法 */
    public static final String Message_File_Name_Invaild = "invalid file name";

    /** 接口类对象参数为空 */
    public static final String Message_File_Client_Null = "fileClient is null";

    /** 监听器参数为空 */
    public static final String Message_File_Task_Listener_Null = "fileTaskListener is null";

    /** 创建目录失败 */
    public static final String Message_Make_Dir_Failed = "make dir failed";

    /** 本地存在未下载完成的同名文件 */
    public static final String Message_Same_Local_File_Already_Exist = "file with same name already exist";

    /** 服务端存在未上传完成的同名文件 */
    public static final String Message_Same_Remote_File_Already_Exist = "file with same name already exist";

    /** 任务的上一个操作未完成 */
    public static final String Message_Task_Not_Finished = "task not finished,cancel startTask";

    /** 用户账号 为空 */
    public static final String Message_Access_Token_Null = "Access Token is null";

    /** 服务端返回 为空 */
    public static final String Message_Server_Response_Null = "server respomse null";

    /** 任务开始执行 */
    public static final String Message_Task_Begin_Running = "task begin running";
    /** 没有权限访问该文件 */
    public static final String Message_NO_RIGHT = "no right to access the file";
    /** 任务完成 */
    public static final String Message_Task_Done = "task done";
    /**
     * 多个实例运行一个任务
     */
    public static final String Message_Multi_Instance_Running = "too many instance running";
    public static final String Message_Request_Null = "request is null";
    public static final String Message_Illegal_file_type = "Illegal File Type";
}
