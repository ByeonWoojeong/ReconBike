package app.cosmos.reconbike;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import app.cosmos.reconbike.DTO.PoiDTO;
import app.cosmos.reconbike.DTO.SearchPoiDTO;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class MainSearchActivity extends AppCompatActivity {

    AQuery aQuery = null;
    InputMethodManager ipm;
    SoftKeyBoard softKeyBoard;
    boolean isKeyBoardVisible;
    LinearLayout main_search;
    EditText search_text;
    ImageView back, search_close_btn, search_btn;
    ListView list_view;
    ArrayList<PoiDTO> poiData;
    MainSearchListAdapter mainSearchListAdapter;
    OneBtnDialog oneBtnDialog;
    Gson gson;
    String getLa, getLo;
    FrameLayout list_view_con;

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
        setContentView(R.layout.activity_main_search);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(MainSearchActivity.this);
                setMeizuDarkMode(MainSearchActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        main_search = (LinearLayout) findViewById(R.id.main_search);
        ipm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        softKeyBoard = new SoftKeyBoard(main_search, ipm);
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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipm.hideSoftInputFromWindow(search_text.getWindowToken(), 0);
                finish();
            }
        });
        search_text = (EditText) findViewById(R.id.search_text);
        search_close_btn = (ImageView) findViewById(R.id.search_close_btn);
        search_btn = (ImageView) findViewById(R.id.search_btn);
        list_view_con = (FrameLayout) findViewById(R.id.list_view_con);
        list_view = (ListView) findViewById(R.id.list_view);
        gson = new Gson();
        poiData = new ArrayList<PoiDTO>();
        mainSearchListAdapter = new MainSearchListAdapter(MainSearchActivity.this, R.layout.list_search, poiData, list_view);
        search_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_text.setText("");
            }
        });
        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(search_text.getText().toString())) {
                    search_close_btn.setVisibility(View.GONE);
                } else {
                    search_close_btn.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
                if ("".equals(search_text.getText().toString().trim()) || "" == search_text.getText().toString().trim()) {
                    oneBtnDialog = new OneBtnDialog(MainSearchActivity.this, "검색 내용을 입력해 주세요.", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                String url = "https://api2.sktelecom.com/tmap/pois?version=1&appKey=9241461c-cece-4c80-a03f-03da0124f1fe&page=1&count=30&searchKeyword=" + search_text.getText() + "&resCoordType=WGS84GEO&searchType=all&searchtypCd=A";
                aQuery.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if (isKeyBoardVisible) {
                                ipm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                            search_text.clearFocus();
                            poiData.clear();
                            if (jsonObject != null) {
                                SearchPoiDTO getSearchPoi = gson.fromJson(jsonObject.getString("searchPoiInfo"), SearchPoiDTO.class);
                                JsonArray list = getSearchPoi.getPois().getAsJsonArray("poi");
                                for (int i = 0; i < list.size(); i++) {
                                    poiData.add(gson.fromJson(list.get(i), PoiDTO.class));
                                }
                                mainSearchListAdapter.setSearchWord(search_text.getText().toString().trim());
                                list_view.setAdapter(mainSearchListAdapter);
                                mainSearchListAdapter.notifyDataSetChanged();
                                list_view_con.setVisibility(View.VISIBLE);
                            } else {
                                poiData.clear();
                                mainSearchListAdapter.setSearchWord("");
                                list_view.setAdapter(mainSearchListAdapter);
                                mainSearchListAdapter.notifyDataSetChanged();
                                oneBtnDialog = new OneBtnDialog(MainSearchActivity.this, "검색 결과가 없습니다.", "확인");
                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                oneBtnDialog.setCancelable(false);
                                oneBtnDialog.show();
                                list_view_con.setVisibility(View.GONE);
                            }
                            search_text.setText("");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        Intent intent = getIntent();
        String getIntent = intent.getStringExtra("intent");
        if (getIntent != null) {
            search_text.requestFocus();
            ipm.toggleSoftInput(ipm.SHOW_FORCED, ipm.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case 666:
                Intent intent = new Intent();
                intent.putExtra("la", getLa);
                intent.putExtra("lo", getLo);
                setResult(666, intent);
                finish();
                break;
        }
    }

    public class OneBtnDialog extends Dialog {
        OneBtnDialog oneBtnDialog = this;
        Context context;
        public OneBtnDialog(final Context context, final String text, final String btnText) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_one_btn_dialog);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView title1 = (TextView) findViewById(R.id.title1);
            TextView title2 = (TextView) findViewById(R.id.title2);
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            title2.setVisibility(View.GONE);
            title1.setText(text);
            btn1.setText(btnText);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oneBtnDialog.dismiss();
                }
            });
        }
    }
}
