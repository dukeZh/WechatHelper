package com.duke.wechathelper.wechat
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import com.duke.wechathelper.*
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.layout_loading_dialog.*


/**
 * 描述：辅助服务控制页
 *
 */
class ControlActivity : AppCompatActivity() {

    lateinit var loadingDialog: CustomDialog
    val WXPACKAGENAME = "com.tencent.mm"

    /**
     * 正在上传提示的 loadingView
     */

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

        btn_get_wechat.setOnClickListener {

            // Check if we have write permission
            // Check if we have write permission
            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 101)
                return@setOnClickListener
            }
            getWxChatList()
        }
    }

    private fun getWxChatList() {
        //判断姓名是否为空
        if ( input_wechat.getText().toString().isEmpty()) {
            Toast.makeText(this, "请先输入您的微信号!", Toast.LENGTH_SHORT).show()
            return
        }
        //判断是否安装了微信
        if (isWeixinAvilible()) {
            loadingDialog = CustomDialog(this, R.style.customDialog, R.layout.layout_loading_dialog)
            loadingDialog.setCancelable(false)
            loadingDialog.show()
            loadingDialog.text.text = "正在从微信中导出聊天记录,请稍候"
            loadingDialog.loadingView.visibility = View.VISIBLE
            loadingDialog.iv_success.visibility = View.INVISIBLE
            loadingDialog.iv_fail.visibility = View.INVISIBLE
            MyTask(this).execute()
        } else {
            runOnUiThread { Toast.makeText(this, "请先安装微信", Toast.LENGTH_SHORT).show() }
        }

    }

    /**
     * 判断是否安装了微信
     *
     * @return
     */
    fun isWeixinAvilible(): Boolean {
        val packageManager: PackageManager = packageManager
        val pinfo = packageManager.getInstalledPackages(0)
        if (pinfo != null) {
            for (i in pinfo.indices) {
                val pn = pinfo[i].packageName
                if (pn == WXPACKAGENAME) {
                    return true
                }
            }
        }
        return false
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
