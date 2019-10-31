package app.cosmos.reconbike;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tsengvn.typekit.TypekitContextWrapper;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class BillingActivity extends AppCompatActivity {

    String menu_name, menu_price, type;
    ImageView back;
    TextView title;
    WebView webView, childView;
    ProgressBar progress;
    int returnScroll;
    Handler handler = new Handler();

    class AndroidBridge {
        @JavascriptInterface
        public void pay(final String[] arg) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setResult(444);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null){
            Uri uri = intent.getData();
            if (uri != null) {
                String schemaUrl = uri.toString();
                String urlString = null;
                if (schemaUrl.startsWith("kongnamul://ISPSuccess/")) {
                    urlString = schemaUrl.substring("kongnamul://ISPSuccess/".length());
                    webView.clearHistory();
                    webView.loadUrl(urlString);
                } else if(schemaUrl.startsWith("kongnamul://ISPCancel/")){
                    urlString = schemaUrl.substring("kongnamul://ISPCancel/".length());
                    webView.clearHistory();
                    webView.loadUrl(urlString);
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(BillingActivity.this);
                setMeizuDarkMode(BillingActivity.this);
            }
        }
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        String token = prefToken.getString("Token", "");
        Intent intent = getIntent();
        menu_name = intent.getStringExtra("menu_name");
        menu_price = intent.getStringExtra("menu_price");
        type = intent.getStringExtra("type");
        title  = (TextView) findViewById(R.id.title);
        if ("phone".equals(type)) {
            title.setText("휴대폰 결제");
        } else if ("card".equals(type)) {
            title.setText("카드 결제");
        } else if ("wire".equals(type)) {
            title.setText("계좌이체");
        }
        back = (ImageView) findViewById(R.id.back);
        progress = (ProgressBar) findViewById(R.id.progress);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        webView  = (WebView) findViewById(R.id.webview);
        webView.addJavascriptInterface(new AndroidBridge(), "reconbike");
        String userAgent = new WebView(getBaseContext()).getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + "gh_mobile{" + token + "}");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webView.getSettings().setTextZoom(100);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
        String postData = "&P_AMT=" + menu_price + "&P_GOODS=" + menu_name;
        webView.postUrl(UrlManager.getBaseUrl() + "/pay/payment/" + type, postData.getBytes());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        WebBackForwardList list = webView.copyBackForwardList();
        if (list.getCurrentIndex() <= 0 && !webView.canGoBack()) {
            if (childView != null) {
                if (childView.canGoBack()) {
                    childView.goBack();
                } else {
                    if (childView.isShown()) {
                        childView.setVisibility(View.GONE);
                        childView.removeView(childView);
                        childView = null;
                        webView.scrollTo(0, returnScroll);
                    }
                }
            } else {
                finish();
            }
        } else {
            if (childView != null) {
                if(childView.canGoBack()){
                    childView.goBack();
                } else {
                    if (childView.isShown()) {
                        childView.setVisibility(View.GONE);
                        childView.removeView(childView);
                        childView = null;
                        webView.scrollTo(0, returnScroll);
                    }
                }
            } else {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        }
    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progress.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(dial);
                return true;
            } else if (url.startsWith("sms:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(i);
                return true;
            } else if (url.startsWith("market://details?id=")) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    webView.scrollTo(0, returnScroll);
                    Toast.makeText(BillingActivity.this, "앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                }
            } else if (url.endsWith(".mp4") || url.endsWith(".swf")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                view.getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent intent = null;
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (webView.canGoBack()) {
                            webView.clearHistory();
                        }
                        String packageName = intent.getPackage();
                        if (packageName != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            return true;
                        }
                    }
                } else {
                    Intent intent = null;
                    try {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        if (webView.canGoBack()) {
                            webView.clearHistory();
                        }
                        String packageName = intent.getPackage();
                        if (packageName != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            return true;
                        }
                    }
                }
            }
            return true;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onCloseWindow(WebView w) {
            super.onCloseWindow(w);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(BillingActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(BillingActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            view.removeAllViews();
            childView = new WebView(view.getContext());
            childView.getSettings().setJavaScriptEnabled(true);
            childView.getSettings().setDomStorageEnabled(true);
            childView.getSettings().setAllowFileAccess(true);
            childView.getSettings().setAllowContentAccess(true);
            childView.getSettings().setLoadWithOverviewMode(true);
            childView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            childView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
            childView.getSettings().setUseWideViewPort(true);
            childView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            childView.setWebChromeClient(this);
            childView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progress.setVisibility(View.GONE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("tel:")) {
                        Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(dial);
                        return true;
                    } else if (url.startsWith("sms:")) {
                        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                        startActivity(i);
                        return true;
                    } else if (url.startsWith("market://details?id=")){
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                            childView.setVisibility(View.GONE);
                            childView.removeView(childView);
                            childView = null;
                            webView.scrollTo(0, returnScroll);
                            Toast.makeText(BillingActivity.this, "앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(i);
                        }
                    } else if (url.startsWith("intent:")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            try {
                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                childView.setVisibility(View.GONE);
                                childView.removeView(childView);
                                childView = null;
                                webView.scrollTo(0, returnScroll);
                            }
                        } else {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                childView.setVisibility(View.GONE);
                                childView.removeView(childView);
                                childView = null;
                                webView.scrollTo(0, returnScroll);
                            }
                        }
                    } else if (url.endsWith( ".mp4" ) || url.endsWith( ".swf" )) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(url), "video/*");
                        view.getContext().startActivity(intent);
                        return true;
                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
                        return false;
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Intent intent = null;
                            try {
                                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                String packageName = intent.getPackage();
                                if (packageName != null) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                                    return true;
                                }
                            }
                        } else {
                            Intent intent = null;
                            try {
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                String packageName = intent.getPackage();
                                if (packageName != null) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                                    return true;
                                }
                            }
                        }
                    }
                    return true;
                }
            });
            childView.setLayoutParams(new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            childView.findFocus();
            view.addView(childView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            returnScroll = view.getScrollY();
            view.scrollTo(0,0);
            return true;
        }
    }
}
