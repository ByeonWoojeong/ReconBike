package app.cosmos.reconbike;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import app.cosmos.reconbike.DTO.UseHistory;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class UseHistoryActivity extends AppCompatActivity {

    AQuery aQuery = null;
    ImageView back;
    LinearLayout list_con;
    ArrayList<UseHistory> data = null;
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
        setContentView(R.layout.activity_use_history);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(UseHistoryActivity.this);
                setMeizuDarkMode(UseHistoryActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        list_con = (LinearLayout) findViewById(R.id.list_con);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        String token = prefToken.getString("Token", "");
        String url = UrlManager.getBaseUrl() + "/member/rentallist";
        Map<String, Object> params = new HashMap<String, Object>();
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                try {
                    if ("true".equals(jsonObject.getString("return"))) {
                        list_con.removeAllViews();
                        final JSONArray jsonArray = new JSONArray(jsonObject.getString("list"));
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String saveDate = null;
                        boolean isAddView1;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            isAddView1 = false;
                            jsonObjectList = jsonArray.getJSONObject(i);
                            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View addView1 = inflater.inflate(R.layout.list_use_history1, null);
                            View addView2 = inflater.inflate(R.layout.list_use_history2, null);
                            TextView date = (TextView) addView1.findViewById(R.id.date);
                            TextView week = (TextView) addView1.findViewById(R.id.week);
                            NPTextView device = (NPTextView) addView2.findViewById(R.id.device);
                            NPTextView time = (NPTextView) addView2.findViewById(R.id.time);
                            FrameLayout type_con = (FrameLayout) addView2.findViewById(R.id.type_con);
                            TextView type = (TextView) addView2.findViewById(R.id.type);
                            if (saveDate == null || !saveDate.equals(jsonObjectList.getString("date").substring(0, 10).replaceAll("-", "."))) {
                                isAddView1 = true;
                            }
                            saveDate = jsonObjectList.getString("date").substring(0, 10).replaceAll("-", ".");
                            date.setText(saveDate);
                            Date getDate = null;
                            try {
                                getDate = simpleDateFormat.parse(jsonObjectList.getString("date"));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(getDate);
                            int dayNumber = calendar.get(Calendar.DAY_OF_WEEK);
                            switch (dayNumber) {
                                case 1:
                                    week.setText("일요일");
                                    break ;
                                case 2:
                                    week.setText("월요일");
                                    break ;
                                case 3:
                                    week.setText("화요일");
                                    break ;
                                case 4:
                                    week.setText("수요일");
                                    break ;
                                case 5:
                                    week.setText("목요일");
                                    break ;
                                case 6:
                                    week.setText("금요일");
                                    break ;
                                case 7:
                                    week.setText("토요일");
                                    break ;
                            }
                            if (isAddView1) {
                                list_con.addView(addView1);
                            }
                            device.setText(jsonObjectList.getString("device"));
                            time.setText(jsonObjectList.getString("date").substring(11, 16));
                            if ("1".equals(jsonObjectList.getString("type"))) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    type_con.setBackgroundDrawable(getResources().getDrawable(R.drawable.use_history_type_back1));
                                } else {
                                    type_con.setBackground(getResources().getDrawable(R.drawable.use_history_type_back1));
                                }
                                type.setText("승차");
                            } else if ("0".equals(jsonObjectList.getString("type"))) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    type_con.setBackgroundDrawable(getResources().getDrawable(R.drawable.use_history_type_back2));
                                } else {
                                    type_con.setBackground(getResources().getDrawable(R.drawable.use_history_type_back2));
                                }
                                type.setText("하차");
                            }
                            list_con.addView(addView2);
                        }
//                        scrollView.post(new Runnable() {
//                            public void run() {
//                                scrollView.fullScroll(scrollView.FOCUS_UP);
//                            }
//                        });
                    } else if ("false".equals(jsonObject.getString("return"))) {
                        list_con.removeAllViews();
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
