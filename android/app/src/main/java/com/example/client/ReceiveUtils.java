package com.example.client;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Handler;

public class ReceiveUtils {
    private DatagramPacket packet;
    private static ReceiveUtils obj = null;
    private static final String TAG = "ReceiveUtils";
    private static Handler handler = null;

    private ReceiveUtils() {

    }

    public static ReceiveUtils instance() {
        if (obj == null) {
            obj = new ReceiveUtils();
        }
        return obj;
    }

    public void setHandler(Handler hand) {
        handler = hand;
    }

    public synchronized void receiveDataOnce(ArrayList<Byte> array) {
        try {
            if (StateUtils.socket == null) {
                StateUtils.socket = new DatagramSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (packet == null) {
            byte[] receBuf = new byte[512];
            packet = new DatagramPacket(receBuf, receBuf.length);
        }
        try {
            StateUtils.socket.receive(packet);
            dealWithData(packet.getData(), packet.getLength(), array);
        } catch (java.net.SocketTimeoutException e) {
            //nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void receiveMessage() {
        try {
            if (StateUtils.socket == null) {
                StateUtils.socket = new DatagramSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (packet == null) {
            byte[] receBuf = new byte[512];
            packet = new DatagramPacket(receBuf, receBuf.length);
        }
        try {
            StateUtils.socket.receive(packet);
            Log.d(TAG, "run: receive data length " + packet.getLength());
            dealWithData(packet.getData(), packet.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealWithData(byte[] data, int length) {
        if (length < 3) {
            Log.d(TAG, "dealWithData: error data length");
            return;
        }
        if (data[0] != (byte) 0xff || data[1] != length) {
            Log.d(TAG, "dealWithData: error data head");
            return;
        }

        if (length == 3)//处理服务器返回的情况(占座/查询)
        {
            Message msg = new Message();
            msg.what = StateUtils.MSG_WHAT_DEAL_WITH_REV;
            msg.arg1 = data[2];
            if (StateUtils.isFirstTips && handler != null) {
                handler.sendMessage(msg);
                StateUtils.isFirstTips = false;
            }
            StateUtils.seatData = new byte[0];
            return;
        }

        //查询
        byte[] bufData = new byte[length - 2];//数据头为2个字节
        for (int i = 2; i < length; i++) {
            bufData[i - 2] = data[i];
        }
        StateUtils.seatData = bufData;

    }

    private void dealWithData(byte[] data, int length, ArrayList<Byte> array) {
        if (length < 3) {
            Log.d(TAG, "dealWithData: error data length");
            array.add(0, StateUtils.ERROR_INFO);
            return;
        }
        if (data[0] != (byte) 0xff || data[1] != length) {
            Log.d(TAG, "dealWithData: error data head");
            array.add(0, StateUtils.ERROR_INFO);
            return;
        }
        if (length == 3)//处理服务器返回的情况(占座/查询)
        {
            Message msg = new Message();
            msg.what = StateUtils.MSG_WHAT_DEAL_WITH_REV;
            msg.arg1 = data[2];
            if (StateUtils.isFirstTips && handler != null && StateUtils.onTips == true) {
                handler.sendMessage(msg);
                StateUtils.isFirstTips = false;
            }
            array.add(0, StateUtils.ERROR_INFO);
            return;
        }

        //查询
        array.clear();
        for (int i = 2; i < length; i++) {
            array.add(i - 2, data[i]);
        }


    }
}
