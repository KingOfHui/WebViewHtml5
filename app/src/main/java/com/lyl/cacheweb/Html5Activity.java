package com.lyl.cacheweb;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Html5Activity extends Activity {

    private String mUrl;

    private FrameLayout mLayout;
    private SeekBar mSeekBar;
    private Html5WebView mWebView;
    private Call call;
    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_web);

//        getParameter();
        mUrl= "http://www.zgxiangcai.com/wap";
        mLayout = (FrameLayout) findViewById(R.id.web_layout);
        mSeekBar = (SeekBar) findViewById(R.id.web_sbr);

        // 创建 WebView
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView = new Html5WebView(getApplicationContext());
        mWebView.setLayoutParams(params);
        mLayout.addView(mWebView);
        mWebView.setWebChromeClient(new Html5WebChromeClient());

        mWebView.loadUrl(mUrl);
        okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(mUrl)
                .get()//默认就是GET请求，可以不写
                .build();
        call = okHttpClient.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d("dhdhdh", "onFailure: ");
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                Log.d("dhdhdh", "onResponse: " + response.body().string());
//
//            }
//        });

        try {
            checkUpdate(okHttpClient);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
                hideHtmlContent(webView);
                return super.shouldInterceptRequest(webView, s);
            }

            @Override
            public void onLoadResource(WebView webView, String s) {
                super.onLoadResource(webView, s);
                hideHtmlContent(webView);
            }
        });

    }

    private void hideHtmlContent(final WebView webView) {
        //编写 javaScript方法
        final String javascript =  "javascript:function hideOther() {" +
                "document.getElementsByClassName('hd-logo')[0].style.display='hidden';" +
                "document.getElementsByClassName('cate-content-ads')[0].style.display='none';" +
//                "var link=document.createElement('a');"+
//                "link.setAttribute('href','#');"+
//        "link.setAttribute('id','login');"+
//        "link.style.color = 'green'"+
//                "link.innerHTML = '农家庄园'"+
//        "document.getElementsByClassName('hd-logo')[0].appendChild(link);"+
//                "document.getElementsByClassName('class_qidian_wpa')[0].style.display='none';" +
//                "document.getElementsByClassName('app_foot')[0].style.display='none';" +
//                        "document.getElementsById('qidian_wpa_2852158374_59').style.display='none';" +
                "}";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
        //创建方法
        webView.loadUrl(javascript);

        //加载方法
        webView.loadUrl("javascript:hideOther();");

            }
        });
    }

    private void checkUpdate(OkHttpClient okHttpClient) throws IOException {
        final Request request2 = new Request.Builder()
                .url("http://103.71.48.84/getAppConfig.php?appid=154154")
                .get()//默认就是GET请求，可以不写
                .build();
        Call call2 = okHttpClient.newCall(request2);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("dhdhdh", "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("dhdhdh", "onResponse1: " + response);
                Log.d("dhdhdh", "onResponse1: " + response.body());
                String result = response.body().string();
                Log.d("dhdhdh", "onResponse2: " + result);

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    final String url = jsonObject.optString("Url");
                    String success = jsonObject.optString("success");
                    if ("true".equals(success)&& !TextUtils.isEmpty(url)) {
                        boolean avilible = NetStatusUtil.isAvilible(Html5Activity.this, "com.bxvip.app.dadazy");
                        if (avilible) {

                        }else {

                            Intent intent = new Intent(Html5Activity.this, MainActivity.class);
                            intent.putExtra("url", url);
                            startActivity(intent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    // 继承 WebView 里面实现的基类
    class Html5WebChromeClient extends Html5WebView.BaseWebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // 顶部显示网页加载进度
            mSeekBar.setProgress(newProgress);
        }
    }

    @Override
    protected void onDestroy() {
        // 销毁 WebView
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();

            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    public void getParameter() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mUrl = bundle.getString("url");
        } else {
            mUrl = "http://m.shuhua.com/";
        }
    }

    //============================= 下面是本 demo  的逻辑代码
    // ======================================================================================

    /**
     * 按目录键，弹出“关闭页面”的选项
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.close:
                Html5Activity.this.finish();
                return true;
            case R.id.copy:
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String url = mWebView.getUrl();
                ClipData clipData = ClipData.newPlainText("test", url);
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getApplicationContext(), "本页网址复制成功", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private long mOldTime;

    /**
     * 点击“返回键”，返回上一层
     * 双击“返回键”，返回到最开始进来时的网页
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mOldTime < 1500) {
                mWebView.clearHistory();
                mWebView.loadUrl(mUrl);
            } else if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                Html5Activity.this.finish();
            }
            mOldTime = System.currentTimeMillis();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}