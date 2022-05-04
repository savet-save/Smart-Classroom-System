package com.example.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Seat {

    final static private int[] colors = {R.color.cGray, R.color.cRed, R.color.cGreen};
    final static private int revColor = R.color.cBlue;
    private static final String TAG = "Seat";
    private static Seat mSeat = null;
    private static LinearLayout[] layout = null;
    private static Activity activity = null;
    private static Handler mHandler = null;
    private static TextView statisticalText1 = null;
    private static TextView statisticalText2 = null;
    private static int[] statisticalData = null;

    private Seat(Activity activity) {
        //获得id为ll的线性布局
        layout = new LinearLayout[]{(LinearLayout) activity.findViewById(R.id.lineLayout1),
                (LinearLayout) activity.findViewById(R.id.lineLayout2), (LinearLayout) activity.findViewById(R.id.lineLayout3),
                (LinearLayout) activity.findViewById(R.id.lineLayout4), (LinearLayout) activity.findViewById(R.id.lineLayout5),
                (LinearLayout) activity.findViewById(R.id.lineLayout6), (LinearLayout) activity.findViewById(R.id.lineLayout7),
                (LinearLayout) activity.findViewById(R.id.lineLayout8)};
        Seat.activity = activity;

        statisticalText1 = activity.findViewById(R.id.statisticalText1);
        statisticalText2 = activity.findViewById(R.id.statisticalText2);
        statisticalData = new int[]{0, 0, 0};
    }

    public static Seat instance(Activity activity, Handler handler) {
        if (mSeat == null) {
            mSeat = new Seat(activity);
            mHandler = handler;
        }
        return mSeat;
    }

    public static void clear() {
        mSeat = null;
    }

    public void show(byte[] data) {
        for (LinearLayout linearLayout : layout) { //清空座位
            linearLayout.removeAllViews();
        }
        for (int i = 0; i < statisticalData.length; i++) { //清空数据
            statisticalData[i] = 0;
        }

        //创建imageView的组件(显示座位）
        for (int i = 0; i < data.length; i++) {
            final ImageView imge = new ImageView(activity);
            imge.setContentDescription(String.valueOf(i));
            if (data[i] > colors.length) {//防止溢出
                data[i] = 0;
            }

            if(data[i] == 0) {
                imge.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(StateUtils.selfSeat[1] == -1) {
                            CharSequence str = imge.getContentDescription();
                            int id = Integer.parseInt(str.toString());
                            StateUtils.selfSeat[0] = StateUtils.currClass;
                            StateUtils.selfSeat[1] = id;
                            imge.setBackgroundColor(activity.getResources().getColor(revColor));
                            new Thread() {
                                @Override
                                public void run() {
                                    SendUtils.instance().reservation();
                                    ReceiveUtils.instance().receiveMessage();
                                }
                            }.start();

                        }

                    }
                });
            }

            statisticalData[data[i]]++;
            int color = activity.getResources().getColor(colors[data[i]]);
            if(StateUtils.currClass == StateUtils.selfSeat[0] && StateUtils.selfSeat[1] == i) {
                color = activity.getResources().getColor(revColor);
            }

            imge.setBackgroundColor(color);
            imge.setPadding(30, 30, 30, 30); //设置padding 属性(设置大小）

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            lp.rightMargin = 20;
            lp.topMargin = 27;
            lp.leftMargin = 20;
            imge.setLayoutParams(lp); //设置layout 属性（设置间隔）
//            将imageView组件添加到id=ll的线性布局里去
            layout[i % layout.length].addView(imge);
        }
        float sum = data.length;
        statisticalText1.setText(activity.getResources().getString(R.string.statistical1) +
                " 空座位 ：" + String.format("%.2f", (float) statisticalData[0] / sum * 100) + "%" +
                "   预定座位：" + String.format("%.2f", (float) statisticalData[1] / sum * 100) + "%" +
                "   有人座位：" + String.format("%.2f", (float) statisticalData[2] / sum * 100) + "%");
        statisticalText2.setText(activity.getResources().getString(R.string.statistical2) +
                " 空座位 ：" + statisticalData[0] + "个" +
                "   预定座位：" + statisticalData[1] + "个" +
                "   有人座位：" + statisticalData[2] + "个");
        if(StateUtils.isFirst && statisticalData[0] / sum < 0.5f) {
            StateUtils.isFirst = false;
            mHandler.sendEmptyMessage(StateUtils.MSG_WHAT_NOT_SPACE);
        }
    }
}
