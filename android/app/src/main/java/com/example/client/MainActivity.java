package com.example.client;

import android.app.AlertDialog;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Seat seat = null;
    private static boolean isStop = false;
    private static final String TAG = "MainActivity";
    private boolean isEnd = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StateUtils.MSG_WHAT_UPDATE_SHOW:
                    if (StateUtils.seatData != null) {
                        try {
                            seat.show(StateUtils.seatData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        StateUtils.seatData = null;
                    }
                    break;
                case StateUtils.MSG_WHAT_DEAL_WITH_REV:
                    byte data = (byte) msg.arg1;
                    dealWithReservation(data);
                    break;
                case StateUtils.MSG_WHAT_NOT_SPACE:
                    Toast.makeText(getApplicationContext(), "当前教室超过50%上座率, 建议换间教室", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.d(TAG, "handleMessage: unknown msg what");
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Seat.clear();
        isStop = true;
//        isEnd = true;
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Seat.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        Button button = findViewById(R.id.button);

        init();

        //选择item的选择点击监听事件
//        spinner.setSelection(getIntent().getIntExtra(StateUtils.classroomId, 0));
//        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                // TODO Auto-generated method stub
//                // 将所选mySpinner 的值带入myTextView 中
//                StateUtils.currClass = arg2 + 1;
//                SendUtils.instance().inquire();
//                StateUtils.isFirst = true;
//                StateUtils.isFirstTips = true;
//            }
//
//            public void onNothingSelected(AdapterView<?> arg0) {
//                // TODO Auto-generated method stub
//                SendUtils.instance().inquire();
//            }
//        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    //初始化并弹出对话框方法
    private void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.setting, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        EditText editTextNumber_s = view.findViewById(R.id.editTextNumber_s);
        EditText editTextNumber_m = view.findViewById(R.id.editTextNumber_m);
        EditText editTextNumber_h = view.findViewById(R.id.editTextNumber_h);
        Button ok = view.findViewById(R.id.button_ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置秒
                String sec = editTextNumber_s.getText().toString();
                if (sec.length() > 0) {
                    int s = Integer.parseInt(sec);
                    if (s >= 0 && s < 60) {
                        StateUtils.revTimeS = s;
                    }
                }

                //设置分钟
                String min = editTextNumber_m.getText().toString();
                if (min.length() > 0) {
                    int m = Integer.parseInt(min);
                    if (m >= 0 && m < 60) {
                        StateUtils.revTimeM = m;
                    }
                }

                //设置小时
                String hour = editTextNumber_h.getText().toString();
                if (hour.length() > 0) {
                    int h = Integer.parseInt(hour);
                    if (h >= 0 && h < StateUtils.MAX_REC_TIME_H) {
                        StateUtils.revTimeM = h;
                    } else {
                        Toast.makeText(getApplicationContext(), "设置超过最大小时 : " + StateUtils.MAX_REC_TIME_H, Toast.LENGTH_SHORT).show();
                    }
                }

                dialog.dismiss();
            }
        });

        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this) / 4 * 3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }


    private void init() {
        seat = Seat.instance(this, handler);
        Log.d(TAG, "set currClass " + getIntent().getIntExtra(StateUtils.classroomId, 0));
        StateUtils.currClass = getIntent().getIntExtra(StateUtils.classroomId, 0) + 1;
        SendUtils.instance();
        ReceiveUtils.instance().setHandler(handler);

        StateUtils.isFirst = true;
        StateUtils.isFirstTips = true;

        new Thread(new Runnable() { //更新座位
            @Override
            public void run() {
                while (!isEnd) {
                    while(isStop) {
                        sleepThread(50);//等待恢复
                    }
                    SendUtils.instance().inquire();
                    ReceiveUtils.instance().receiveMessage();
                    handler.sendEmptyMessage(StateUtils.MSG_WHAT_UPDATE_SHOW);
                    sleepThread(300);
                }
            }
        }).start();
    }

    private void sleepThread(int m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void dealWithReservation(byte data) {
        switch (data) {
            case (byte) 0xff:
                Toast.makeText(getApplicationContext(), "没有该教室", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0xfe:
                Toast.makeText(getApplicationContext(), "座位已经有人", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0xfd:
                Toast.makeText(getApplicationContext(), "未知操作", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0xfc:
                Toast.makeText(getApplicationContext(), "取消占座失败，有人已上座", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0xfb:
                Toast.makeText(getApplicationContext(), "没有该座位", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x00:
                StateUtils.isFirstTips = true;
                if (StateUtils.selfSeat[1] != -1) {
                    Toast.makeText(getApplicationContext(), "预定成功" + StateUtils.revTimeH +
                            "小时" + StateUtils.revTimeM + "分钟" + StateUtils.revTimeS + "秒" +
                            "后结束预定", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "预定结束", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "未知错误", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}