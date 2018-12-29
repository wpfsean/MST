package com.tehike.mst.client.project.global;


/**
 * Created by Root on 2018/8/5.
 */

public class AppConfig {


    public AppConfig() {
        throw new UnsupportedOperationException("不能被实例化");
    }

    /**
     * 更新apk包的路径
     */
    public static  String UPDATE_APK_PATH = "http://19.0.0.20/zkth/auto_update/auto_update_apk.php";


    /**
     * 当前的方向(androidManifest设置)
     * 1 竖屏
     * 2 横屏
     */
    public static int APP_DIRECTION = 2;

    /**
     * 每隔15分钟去加载刷新一下数据
     */
    public static int REFRESH_DATA_TIME = 15 * 60 * 1000;

    /**
     * 控件
     * 发送心跳间隔(秒)
     */
    public static int SEND_HB_SPACING = 15 * 1000;

    /**
     * 远程 喊话
     */
    public static int REMOTE_PORT = 18720;

    /**
     * Send header to server id
     */
    public static String HEADER_HEADER_ID = "ZDHB";

    /**
     * 文件夹父路径
     */
    public static String SD_DIR = "tehike";


    public static String SHOT_DIR = "shotPic";

    /**
     * 存放资源的目录
     */
    public static String SOURCES_DIR = "sources";

    /**
     * 视频资源的文件名
     */
    public static String SOURCES_VIDEO = "videoResource.txt";

    /**
     * Sip资源的文件名
     */
    public static String SOURCES_SIP = "sipResource.txt";

    /**
     * Sip资源的文件名
     */
    public static String SYSINFO = "sysinfo.txt";


    /**
     * 报警类型颜色对象
     */
    public static String ALARM_COLOR = "alarmColor.txt";

    /**
     * SIp是否注册成功
     */
    public static boolean SIP_STATUS = false;

    /**
     * 悬浮窗口权限是否申请成功
     */
    public static boolean ARGEE_OVERLAY_PERMISSION = false;

    /**
     * 系统设置权限是否申请成功（用于修改系统亮度）
     */
    public static boolean ARGEE_WRITE_SETTINGS = false;

    /**
     * 本机Ip
     */
    public static String NATIVE_IP = "";

    /**
     * 本机Cpu信息
     */
    public static double DEVICE_CPU = 0;

    /**
     * 本机的Rom信息
     */
    public static double DEVICE_RAM = 0;

    /**
     * 本机的电量信息
     */
    public static int DEVICE_BATTERY = 0;

    /**
     * 本机的Wifi信息
     */
    public static int DEVICE_WIFI = 0;

    /**
     * 弹箱锁闭状态， 0-未锁闭，1-已锁闭
     */
    public static int AMMOBOX_STATE = 1;


    /**
     * 蓝牙连接状态， 0-未连接，1-已连接
     */
    public static int BLUETOOTH_STATE = 0;


    /**
     * 经纬度
     */
    public static double LOCATION_LAT = 0;

    public static double LOCATION_LOG = 0;

    /**
     * 请求数据的编码格式
     */
    public static String CMS_FORMAT = "GB2312";

    /**
     * Login CMS Port
     */
    public static int LOGIN_CMS_PORT = 2010;

    /**
     * 是否解析主码流或播放声音
     */
    public static boolean IS_MAIN_STREAM = false;

    public static boolean ISVIDEOSOUNDS = false;


    /**
     * 当前的用户名
     */
    public static String USERNAME = "";

    /**
     * 当前的密码
     */
    public static String PWD = "";

    /**
     * 当前的服务器地址
     */
    public static String SERVERIP = "";

    /**
     * 报警类型
     */
    public static String ALERT_TYPE = "暴狱";


    /**
     * webApi接口的host
     */
    public static String WEB_HOST = "http://";

    /**
     * webapi获取sysinfo
     */
    public static String _SYSINFO = ":8080/webapi/sysinfo";

    /**
     * webapi获取sip分组
     */
    public static String _USIPGROUPS = ":8080/webapi/sipgroups";

    /**
     * webapi根据sip组id获取当前组数据
     */
    public static String _USIPGROUPS_GROUP = ":8080/webapi/sips?groupid=";

    /**
     * webapi获取所有的视频组
     */
    public static String _VIDEO_GROUP = ":8080/webapi/videogroups";

    /**
     * webapi根据组Id获取某个组内数据
     */
    public static String _VIDEO_GROUP_ITEM = ":8080/webapi/videos?groupid=";

    /**
     * webapi获取当前sip用户状态
     */
    public static String _SIS_STATUS = ":8080/webapi/sipstatus";

    /**
     * 报警颜色入类型对应表
     */
    public static String _ALARM_COLOR = ":8080/webapi/alertdefines";

    /**
     * 获取webapi上全部的video数据
     */
    public static String _WEBAPI_VIDEO_SOURCE = ":8080/webapi/videos?groupid=0";

    /**
     * 获取webapi上全部的Sip数据
     */
    public static String _WEBAPI_SIP_SOURCE = ":8080/webapi/sips?groupid=0";


    /**
     * 解析sip资源完成时发送广播的Action
     */
    public  static String RESOLVE_SIP_DONE_ACTION = "resolveSipSourceDoneAction";

    /**
     * 解析video资源完成时发送广播的Action
     */
    public  static String RESOLVE_VIDEO_DONE_ACTION = "resolveSVdieoSourceDoneAction";








}
