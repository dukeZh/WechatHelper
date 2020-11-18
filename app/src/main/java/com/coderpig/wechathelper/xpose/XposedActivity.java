package com.coderpig.wechathelper.xpose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.coderpig.wechathelper.R;

public class XposedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed);
        Button  button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(XposedActivity.this, toastMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String toastMessage() {
        return "我未被劫持";
    }
}