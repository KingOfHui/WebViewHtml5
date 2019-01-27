package com.film.farmermanor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.bxvip.app.dadazy.fileprovider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        startActivity(intent);
        finish();
        isOpenFile = true;
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
