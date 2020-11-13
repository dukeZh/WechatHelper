package com.coderpig.wechathelper

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.SimpleStringSplitter
import android.view.accessibility.AccessibilityManager
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_control.*


/**
 * 描述：辅助服务控制页
 *
 * @author CoderPig on 2018/04/12 10:50.
 */
class ControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        initView()
    }

    private fun initView() {
        btn_write.setOnClickListener {
            val phone = ed_friends.text.toString()
            Hawk.put(Constant.WX_PHONE, phone)
            shortToast("数据写入成功！")
        }

        btn_reset.setOnClickListener {
            Hawk.delete(Constant.WX_PHONE)
            ed_friends.setText("")
            shortToast("数据重置成功！")
        }

        btn_open_wechat.setOnClickListener {
            if (isAccessibilityEnabled(this))
            {
                if (!Hawk.contains(Constant.WX_PHONE))
                {
                    shortToast("当前没有手机号码！")
                    return@setOnClickListener
                }
                val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
                if (intent == null)
                {
                    shortToast("当前没有安装微信！")
                    return@setOnClickListener
                }
                startActivity(intent)
            }else
            {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
    }


    @Throws(RuntimeException::class)
    fun isAccessibilityEnabled(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        // 检查AccessibilityService是否开启
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isAccessibilityEnabled_flag = am.isEnabled

        return isAccessibilityEnabled_flag
    }



}
