package app.cosmos.reconbike;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

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

import app.cosmos.reconbike.DTO.Notice;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class NoticeActivity extends AppCompatActivity {

    AQuery aQuery = null;
    String token;
    ImageView back;
    ExpandableListView listView;
    ArrayList<Notice> data = null;
    ArrayList<ArrayList<Notice>> data2;
    ExpandableListAdapter adapter = null;
    JSONObject jsonObjectList;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(NoticeActivity.this);
                setMeizuDarkMode(NoticeActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        token = prefToken.getString("Token", "");
        listView = (ExpandableListView) findViewById(R.id.listView);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String url = UrlManager.getBaseUrl() + "/setting/notice";
        Map<String, Object> params = new HashMap<String, Object>();
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                data = new ArrayList<Notice>();
                data2 = new ArrayList<ArrayList<Notice>>();
                try {
                    if ("true".equals(jsonObject.getString("return"))) {
                        final JSONArray jsonArray = new JSONArray(jsonObject.getString("list"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObjectList = jsonArray.getJSONObject(i);
                            data.add(new Notice(jsonObjectList.getString("title"), jsonObjectList.getString("content"), jsonObjectList.getString("image"), jsonObjectList.getString("type")));
                            data2.add(new ArrayList<Notice>());
                            data2.get(i).add(new Notice(jsonObjectList.getString("title"), jsonObjectList.getString("content"), jsonObjectList.getString("image"), jsonObjectList.getString("type")));
                        }
                        adapter = new NoticeListAdapter(NoticeActivity.this, listView, R.layout.list_notice1, R.layout.list_notice2, data, data2);
                        listView.setAdapter(adapter);
                    } else {
                        data.clear();
                        data2.clear();
                        adapter = new NoticeListAdapter(NoticeActivity.this, listView, R.layout.list_notice1, R.layout.list_notice2, data, data2);
                        listView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.header("User-Agent", "gh_mobile{" + token + "}"));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
