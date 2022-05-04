package com.example.client;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ClassroomActivity extends Activity {

    private ListView listview;
    private ClassroomAdapter classroomAdapter = null;
    private List<Classroom> mData = null;
    private int flag = 0;
    private String[] classroomNameArray = null;
    private final static String defaultInfo = "没有数据";
    private static final String TAG = "ClassroomActivity";
    private boolean isStop = false;
    private boolean isFinish = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StateUtils.MSG_WHAT_UPDATE_CLASSROOM:
                    for (int i = 0; i < classroomNameArray.length; i++) {
                        Classroom classroom = (Classroom) classroomAdapter.getItem(i);
                        if(classroom == null || StateUtils.allClassData[i] == null || StateUtils.allClassData[i].size() <= 0) {
                            //其他错误
                            classroom.setClassroomInfo(defaultInfo);
                            continue;
                        }
                        if (StateUtils.allClassData[i].get(0) == StateUtils.ERROR_INFO) {
                            //数据错误
                            classroom.setClassroomInfo(defaultInfo);
                            continue;
                        }
                        int sum[] = new int[]{0, 0, 0};
                        try {
                            for (byte date : StateUtils.allClassData[i]) {
                                sum[date]++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        classroom.setClassroomInfo("当前教室空余座位：" + sum[0] + "  拥有人数：" + (sum[1] + sum[2]));
                    }
                    classroomAdapter.update();
                    break;
                default:
                    Log.d(TAG, "handleMessage: unknown what");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classroom);
        init();
        listview = findViewById(R.id.list_view);

        mData = new LinkedList<>();
        classroomAdapter = new ClassroomAdapter((LinkedList<Classroom>) mData, ClassroomActivity.this);
        listview.setAdapter(classroomAdapter);
        for (String name : classroomNameArray) {
            classroomAdapter.add(new Classroom(name, defaultInfo));
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent startIntent = new Intent(ClassroomActivity.this, MainActivity.class);
                startIntent.putExtra(StateUtils.classroomId, position);
                isStop = true;
                StateUtils.onTips = true;
                startActivity(startIntent);
            }
        });


    }

    private void init() {
        Resources res = getResources();
        classroomNameArray = res.getStringArray(R.array.spinnerArr);
        SendUtils.instance();
        ReceiveUtils.instance();
        StateUtils.allClassData = new ArrayList[classroomNameArray.length];
        for(int i = 0; i < StateUtils.allClassData.length; i++) {
            StateUtils.allClassData[i] = new ArrayList<>();
        }
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "run: " + StateUtils.allClassData.length);
                while (!isFinish) {
                    for (int i = 0; i < StateUtils.allClassData.length && !isStop; i++) {
                        SendUtils.instance().inquire((byte) (i + 1));
                        ReceiveUtils.instance().receiveDataOnce(StateUtils.allClassData[i]);
                        if(StateUtils.allClassData[i].get(0) == StateUtils.ERROR_INFO) {
                            //认为没有后面的教室了
                            break;
                        }
                    }
                    handler.sendEmptyMessage(StateUtils.MSG_WHAT_UPDATE_CLASSROOM);
                    SleepThread(200);
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        SendUtils.closeSocket();
        isFinish = true;
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
        StateUtils.onTips = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStop = true;
    }

    private void SleepThread(int m) {
        try {
            Thread.sleep(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
