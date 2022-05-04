package com.example.client;

import java.lang.reflect.Array;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class StateUtils {
    public static int selfSeat[] = {-1, -1};
    public static byte[] seatData = null;
    public static int currClass = 1;
    public static final int PORT = 9000;//服务器端口号
    public static final String IP = "192.168.101.2"; //服务器ip
    public static DatagramSocket socket = null;
    final public static int MSG_WHAT_UPDATE_SHOW = 123;
    final public static int MSG_WHAT_DEAL_WITH_REV = 124;//提示接收数据错误信息
    final public static int MSG_WHAT_NOT_SPACE = 125;//标识空位率小于50%
    final public static int MSG_WHAT_UPDATE_CLASSROOM = 223;//更新教室界面信息
    public static int revTimeS = 5;//设定预定时间（秒为单位）
    public static int revTimeM = 0;//设定预定时间（分为单位）
    public static int revTimeH = 0;//设定预定时间（小时为单位）
    final static int MAX_REC_TIME_H = 3;
    public static boolean isFirst = true;//是否是点击教室选择按键,用于提示人数
    public static boolean isFirstTips = true;//用于提示教室有没有
    public static String classroomId = "classroomId";
    public static ArrayList<Byte>[] allClassData = null;
    public static final byte ERROR_INFO = 10;
    public static boolean onTips = false;

    private StateUtils() { // no use

    }
}
