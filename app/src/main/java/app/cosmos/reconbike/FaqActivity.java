package app.cosmos.reconbike;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.cosmos.reconbike.DTO.FaQ;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class FaqActivity extends AppCompatActivity {

    AQuery aQuery = null;
    LinearLayout faq;
    InputMethodManager ipm;
    SoftKeyBoard softKeyBoard;
    boolean isKeyBoardVisible;
    String token;
    ImageView back, visible_search_btn, search_back, search_btn, call;
    FrameLayout toolbar1, toolbar2;
    EditText search_text;
    ExpandableListView listView;
    ArrayList<FaQ> data = null;
    ArrayList<ArrayList<FaQ>> data2;
    ExpandableListAdapter adapter = null;
    JSONObject jsonObjectList;
    OneBtnDialog oneBtnDialog;

    @Override
    protected void onResume() {
        super.onResume();
        String url = UrlManager.getBaseUrl() + "/setting/faq";
        Map<String, Object> params = new HashMap<String, Object>();
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                data = new ArrayList<FaQ>();
                data2 = new ArrayList<ArrayList<FaQ>>();
                try {
                    if ("true".equals(jsonObject.getString("return"))) {
                        final JSONArray jsonArray = new JSONArray(jsonObject.getString("list"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObjectList = jsonArray.getJSONObject(i);
                            data.add(new FaQ(jsonObjectList.getString("title"), jsonObjectList.getString("content")));
                            data2.add(new ArrayList<FaQ>());
                            data2.get(i).add(new FaQ(jsonObjectList.getString("title"), jsonObjectList.getString("content")));
                        }
                        adapter = new FaQListAdapter(FaqActivity.this, listView, R.layout.list_faq1, R.layout.list_faq2, data, data2);
                        listView.setAdapter(adapter);
                    } else {
                        data.clear();
                        data2.clear();
                        adapter = new FaQListAdapter(FaqActivity.this, listView, R.layout.list_faq1, R.layout.list_faq2, data, data2);
                        listView.setAdapter(adapter);
                        oneBtnDialog = new OneBtnDialog(FaqActivity.this, "검색 결과가 없습니다.", "1", "확인");
                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        oneBtnDialog.setCancelable(false);
                        oneBtnDialog.show();
                    }
                    search_text.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.header("User-Agent", "gh_mobile{" + token + "}"));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        softKeyBoard.unRegisterSoftKeyboardCallback();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(FaqActivity.this);
                setMeizuDarkMode(FaqActivity.this);
            }
        }
        aQuery = new AQuery(this);
        faq = (LinearLayout) findViewById(R.id.faq);
        ipm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        softKeyBoard = new SoftKeyBoard(faq, ipm);
        softKeyBoard.setSoftKeyboardCallback(new SoftKeyBoard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        isKeyBoardVisible = false;
                    }
                });
            }
            @Override
            public void onSoftKeyboardShow() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        isKeyBoardVisible = true;
                    }
                });
            }
        });
        back = (ImageView) findViewById(R.id.back);
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        token = prefToken.getString("Token", "");
        toolbar1 = (FrameLayout) findViewById(R.id.toolbar1);
        toolbar2 = (FrameLayout) findViewById(R.id.toolbar2);
        search_back = (ImageView) findViewById(R.id.search_back);
        visible_search_btn = (ImageView) findViewById(R.id.visible_search_btn);
        listView = (ExpandableListView) findViewById(R.id.listView);
        search_text = (EditText) findViewById(R.id.search_text);
        search_btn = (ImageView) findViewById(R.id.search_btn);
        call = (ImageView) findViewById(R.id.call);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        visible_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);
                toolbar2.setVisibility(View.VISIBLE);
                search_text.requestFocus();
                ipm.toggleSoftInput(ipm.SHOW_FORCED, ipm.HIDE_IMPLICIT_ONLY);
            }
        });
        search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipm.hideSoftInputFromWindow(search_text.getWindowToken(), 0);
                toolbar1.setVisibility(View.VISIBLE);
                toolbar2.setVisibility(View.GONE);
                search_text.setText("");
                search_text.clearFocus();
                softKeyBoard.unRegisterSoftKeyboardCallback();
            }
        });
        search_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT:
                        search_btn.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_GO:
                        search_btn.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        search_btn.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_SEARCH:
                        search_btn.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_DONE:
                        search_btn.callOnClick();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = UrlManager.getBaseUrl() + "/setting/faq";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("search", search_text.getText().toString().trim());
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        data = new ArrayList<FaQ>();
                        data2 = new ArrayList<ArrayList<FaQ>>();
                        search_text.clearFocus();
                        ipm.hideSoftInputFromWindow(search_text.getWindowToken(), 0);
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                final JSONArray jsonArray = new JSONArray(jsonObject.getString("list"));
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    jsonObjectList = jsonArray.getJSONObject(i);
                                    data.add(new FaQ(jsonObjectList.getString("title"), jsonObjectList.getString("content")));
                                    data2.add(new ArrayList<FaQ>());
                                    data2.get(i).add(new FaQ(jsonObjectList.getString("title"), jsonObjectList.getString("content")));
                                }
                                adapter = new FaQListAdapter(FaqActivity.this, listView, R.layout.list_faq1, R.layout.list_faq2, data, data2);
                                listView.setAdapter(adapter);
                            } else {
                                data.clear();
                                data2.clear();
                                adapter = new FaQListAdapter(FaqActivity.this, listView, R.layout.list_faq1, R.layout.list_faq2, data, data2);
                                listView.setAdapter(adapter);
                                oneBtnDialog = new OneBtnDialog(FaqActivity.this, "검색 결과가 없습니다.", "1", "확인");
                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                oneBtnDialog.setCancelable(false);
                                oneBtnDialog.show();
                            }
                            search_text.setText("");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:02-1234-1234"));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (toolbar2.getVisibility() == View.VISIBLE) {
            search_back.callOnClick();
        } else {
            super.onBackPressed();
        }
    }

    public class OneBtnDialog extends Dialog {
        OneBtnDialog oneBtnDialog = this;
        Context context;
        public OneBtnDialog(final Context context, final String text1, final String text2, final String btnText) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_one_btn_dialog);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView title1 = (TextView) findViewById(R.id.title1);
            TextView title2 = (TextView) findViewById(R.id.title2);
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            title1.setText(text1);
            title2.setText(text2);
            btn1.setText(btnText);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oneBtnDialog.dismiss();
                }
            });
            if ("1".equals(text2)) {
                title2.setVisibility(View.GONE);
            } else if ("2".equals(text2)) {
                title2.setVisibility(View.GONE);
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        oneBtnDialog.dismiss();
                        finish();
                    }
                });
            }
        }
    }
}
