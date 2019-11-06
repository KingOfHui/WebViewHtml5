package com.film.farmermanor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private TextView mTxtHost;
    private EditText mEdtUrl;
    private Button mBtnSreach;

    private String mUrl;
    private TextView tv;
    private NumberProgressBar pb;

    private View view;
    private Context context;
    private TextView messageTv;
    private Button leftBtn;
    private Button rightBtn;
    private LinearLayout twoBtnLin;
    private ProgressBar progressBar;
    private String downloadUrl;
    private boolean isMustUpdate;
    /*是否正在打开安装包*/
    private boolean isOpenFile = false;
    private Message message = null;
    private boolean flag = true;
    private int size = 1;
    private int hasRead = 0;
    private int len = 0;
    private byte buffer[] = new byte[256];
    private volatile int index = 0;
    private UpdateHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        tv = (TextView) findViewById(R.id.tv);
        pb = (NumberProgressBar) findViewById(R.id.pb);
        String url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
            update(url);
        }
//        initViews();
//        initEvents();

    }

    @Override
    public void onBackPressed() {
    }
    public boolean update(final String urlStr) {
        handler = new UpdateHandler();
        final File file = new File(Environment.getExternalStorageDirectory() + "/" + "nongfuzhuangyuan.apk");
        new Thread(new Runnable() {
            public void run() {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    size = connection.getContentLength();
                    InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(file);
                    connection.connect();

                    if (connection.getResponseCode() >= 400) {
//                        showToast("下载失败");

                    } else {
                        while ((len = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, len);
                            hasRead += len;
                            index = (int) (hasRead * 100) / size;
                            message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }

                        inputStream.close();
                        outputStream.close();

                        openFile(file);

//                        dismiss();
                    }
                } catch (Exception e) {
                    flag = false;
                    e.printStackTrace();
                }
            }
        }).start();

        return flag;
    }


    /**
     * 打开APK程序代码
     *
     * @param file
     */
    private void openFile(File file) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.bxvip.app.dadazy.fileprovider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"权限不足，请授予相应权限",Toast.LENGTH_SHORT).show();
                        }
                    } );
                    startInstallPermissionSettingActivity();
                    return;
                }
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivity(intent);
        finish();
        isOpenFile = true;
    }

    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        //获取当前apk包URI，并设置到intent中（这一步设置，可让“未知应用权限设置界面”只显示当前应用的设置项）
        Uri packageURI = Uri.parse("package:"+getPackageName());
        intent.setData(packageURI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent,10086);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10086) {
//            FileUtil.openFile(context, dir + "app-debug.apk", "com.tianer.ch.fileprovider");
            openFile(new File(Environment.getExternalStorageDirectory() + "/" + "nongfuzhuangyuan.apk"));
        }
    }

    class UpdateHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
//                progressBar.setProgress(index);
//                tv.setText(index+"/100");
                pb.setProgress(index);
            }

            super.handleMessage(msg);
        }

    }

}
