package com.duke.wechathelper.wechat;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 */
public class PasswordUtiles {

    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";

    private static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";
    private static FileInputStream in;
    private static DataOutputStream localDataOutputStream;


    /**
     * 根据imei和uin生成的md5码获取数据库的密码
     *
     * @return
     */
    public static String initDbPassword(final Activity mContext) {
        String imei = initPhoneIMEI(mContext);
        //以为不同手机微信拿到的识别码不一样,所以需要做特别处理,可能是MEID,可能是 IMEI1,可能是IMEI2
        if("868739046004754".equals(imei)){
            imei = "99001184251238";
        }
        else if("99001184249875".equals(imei)){
            imei = "868739045977497";
        }
        String uin = initCurrWxUin(mContext);
        try {
            if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "请确认是否授权root权限,并登录微信", Toast.LENGTH_SHORT).show();
                    }
                });
                return "";
            }
            String md5 = Md5Utils.md5Encode(imei + uin);
            String password = md5.substring(0, 7).toLowerCase();
            return password;
        } catch (Exception e) {
            Log.e("initDbPassword", e.getMessage());
        }
        return "";
    }


    /**
     * execRootCmd("chmod 777 -R /data/data/com.tencent.mm");
     * <p>
     * 执行linux指令
     */
    public static void execRootCmd(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            localObject = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
        }finally {
            if (localDataOutputStream!=null){
                try {
                    localDataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 获取手机的imei
     *
     * @return
     */
    private static String initPhoneIMEI(Context mContext) {

        String id;
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            id = mTelephony.getDeviceId();
        } else {
            //android.provider.Settings;
            id = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return id;

    }


    /**
     * 获取微信的uid
     * 微信的uid存储在SharedPreferences里面
     */
    public static String initCurrWxUin(final Activity context) {
        String mCurrWxUin = null;
     /*存储位置为\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml
    xml version='1.0' encoding='utf-8' standalone='yes' ?>
    <map>
    <int name="_auth_uin" value="1445190830" />
    </map>*/
        File file = new File(WX_SP_UIN_PATH);
        try {
            in = new FileInputStream(file);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                if ("_auth_uin".equals(element.attributeValue("name"))) {
                    mCurrWxUin = element.attributeValue("value");
                }
            }

            return mCurrWxUin;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("initCurrWxUin", "获取微信uid失败，请检查auth_info_key_prefs文件权限");
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "请确认是否授权root权限,并登录微信", Toast.LENGTH_SHORT).show();
                }
            });

        }finally {
            try {
                if(in!=null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
