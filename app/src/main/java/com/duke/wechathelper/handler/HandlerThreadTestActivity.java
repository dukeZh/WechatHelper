package com.duke.wechathelper.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.duke.wechathelper.R;

public class HandlerThreadTestActivity extends AppCompatActivity {

    private TextView text1;
    private Button button1;
    private Button button2;
    private Button button3;

    private Handler mainHandler,workHandler;
    private HandlerThread mHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_thread_test);
        initView();
        //创建与主线程关联的Handler
        mainHandler = new Handler();
        /**
         * 步骤1：创建HandlerThread实列对象
         * 传入参数 = 线程名字，作用 = 标记该线程
         */
        mHandlerThread =  new HandlerThread("handlerThread");

        /**
         * 步骤2： 启动线程
         */
        mHandlerThread.start();

        /**
         * 步骤3 创建工作线程Handler&复写handlermessage()
         * 作用：关联HandlerThread 的Looper对象，实现消息处理操作 & 与其他线程进行通讯
         * 注：消息处理操作（HandlerMessage（）） 的执行线程 = mHandlerThread所创建的工作线程中执行
         */
        workHandler = new Handler(mHandlerThread.getLooper()){
            //消息处理的操作
            @Override
            public void handleMessage(Message msg) {
                //设置了两种消息处理操作，通过msg 来进行识别
                switch (msg.what)
                {
                    case 1:
                        try {
                            //延迟操作
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //通过主线程Handler.post方法进行在主线程的UI更新操作
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                text1.setText("我爱学习");
                            }
                        });
                        break;
                        //消息2
                    case 2:
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                              text1.setText("我不喜欢学习");
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        };
        /**
         * 步骤4：使用工作线程Handler 向工作线程的消息队列发消息
         * 在工作线程中，当消息循环时取出对应消息 & 在工作线程执行相关操作
         */
        setOnClick();
    }

    private void setOnClick() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 通过sendMessage（）发送
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 1; //消息的标识
                msg.obj = "A"; // 消息的存放
                // b. 通过Handler发送消息到其绑定的消息队列
                workHandler.sendMessage(msg);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 通过sendMessage（）发送
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 2; //消息的标识
                msg.obj = "B"; // 消息的存放
                // b. 通过Handler发送消息到其绑定的消息队列
                workHandler.sendMessage(msg);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandlerThread.quit();
            }
        });
    }

    private void initView() {
        text1 = (TextView) findViewById(R.id.text1);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
    }
}