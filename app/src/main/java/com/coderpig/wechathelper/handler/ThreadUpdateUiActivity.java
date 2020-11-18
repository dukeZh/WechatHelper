package com.coderpig.wechathelper.handler;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coderpig.wechathelper.R;

public class ThreadUpdateUiActivity extends AppCompatActivity implements View.OnClickListener {
    private Button update_handler;
    private Button update_ViewPost;
    private Button update_handlerPost;
    private Button update_handlerPostDelay;
    private Button update_RunOnUiThread;
    private Button update_AsyncTask;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_update_ui);
        initView();
    }

    private void initView() {
        update_handler=findViewById(R.id.button1);
        update_ViewPost=findViewById(R.id.button2);
        update_handlerPost=findViewById(R.id.button3);
        update_handlerPostDelay=findViewById(R.id.button4);
        update_RunOnUiThread=findViewById(R.id.button5);
        update_AsyncTask=findViewById(R.id.button6);
        textView=findViewById(R.id.myword);

        update_handler.setOnClickListener(this);
        update_ViewPost.setOnClickListener(this);
        update_handlerPost.setOnClickListener(this);
        update_handlerPostDelay.setOnClickListener(this);
        update_RunOnUiThread.setOnClickListener(this);
        update_AsyncTask.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button1:
                update_handler();

                break;
            case R.id.button2:
                update_ViewPost();
                break;
            case R.id.button3:
                update_handlerPost();
                break;
            case R.id.button4:
                update_handlerPostDelay();
                break;
            case R.id.button5:
                update_RunOnUiThread();
                break;
            case R.id.button6:
                new updateAsyncTask().execute();
                break;
        }

    }



    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                textView.setText("小慕慕");
            }
        }
    };
    /*
    方法1
     */
    private void update_handler(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message=handler.obtainMessage();
                message.what=1;
                handler.sendMessage(message);

            }
        }).start();

    }
    /*
    方法2
     */
    private void update_ViewPost(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("小九九");
                    }
                });


            }
        }).start();
    }
    /*
    方法3
     */

    private void update_handlerPost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("小酒酒");
                    }
                });
            }
        }).start();
    }
    /*
    方法4
     */
    private void update_handlerPostDelay(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("九酒");
                    }
                },3000);
            }
        }).start();
    }
    /*
    方法5
     */
    private void update_RunOnUiThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("慕九");
                    }
                });
            }
        }).start();
    }
    /*
    方法6
     */
    class  updateAsyncTask extends AsyncTask<String,Integer,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView.setText("结束");
        }
    }

}

