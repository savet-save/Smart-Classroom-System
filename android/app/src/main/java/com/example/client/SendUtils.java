package com.example.client;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.widget.DialogTitle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SendUtils {
    private static InetAddress mAddress = null;
    private static final String TAG = "SendUtils";
    private static SendUtils obj = null;

    private SendUtils() {

    }

    public static SendUtils instance() {
        if (obj == null) {
            obj = new SendUtils();
            try {
                if (StateUtils.socket == null) {
                    StateUtils.socket = new DatagramSocket();
                    StateUtils.socket.setSoTimeout(500);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                mAddress = InetAddress.getByName(StateUtils.IP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    private void sendMessage(final byte[] data) {
        //初始化socket
        if (StateUtils.socket == null) {
            Log.e(TAG, "SendMessage: socket is null");
            return;
        }
        if (mAddress == null) {
            Log.e(TAG, "SendMessage: mAddress is null");
            return;
        }

        if (StateUtils.socket.isClosed()) {
            Log.e(TAG, "SendMessage: socket is closed");
            return;
        }

        DatagramPacket recvPacket1 = new DatagramPacket(data, data.length, mAddress, StateUtils.PORT);
        try {
            StateUtils.socket.send(recvPacket1);
            Log.d(TAG, "已经发送数据 长度为" + data.length + "  ID:" + data[3]);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void closeSocket() {
        if (StateUtils.socket == null || StateUtils.socket.isClosed()) {
            return;
        }
        StateUtils.socket.close();
    }

    public synchronized void inquire() {
        byte data[] = new byte[]{(byte) 0xff, (byte) 0x04, (byte) 0x00, (byte) StateUtils.currClass};
        sendMessage(data);
    }

    public synchronized void inquire(byte classroomId) {
        byte data[] = new byte[]{(byte) 0xff, (byte) 0x04, (byte) 0x00, (byte) classroomId};
        sendMessage(data);
    }

    public synchronized void reservation() {
        if (StateUtils.selfSeat[1] == -1) {
            Log.d(TAG, "reservation: error seat id");
            return;
        }
        //start
        byte data[] = new byte[]{(byte) 0xff, (byte) 0x06, (byte) 0x01, (byte) StateUtils.selfSeat[0],
                (byte) 0x00, (byte) StateUtils.selfSeat[1]};
        sendMessage(data);

        //end
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep((StateUtils.revTimeS * 1000) +
                            (StateUtils.revTimeM * 1000 * 60) +
                            (StateUtils.revTimeH * 1000 * 60 * 60));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte endData[] = new byte[]{(byte) 0xff, (byte) 0x06, (byte) 0x01, (byte) StateUtils.selfSeat[0],
                        (byte) 0x01, (byte) StateUtils.selfSeat[1]};
                sendMessage(endData);
                ReceiveUtils.instance().receiveMessage();
                StateUtils.selfSeat[1] = -1; //表示没有预定
            }
        }.start();
    }
}