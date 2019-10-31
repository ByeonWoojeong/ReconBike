package app.cosmos.reconbike;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.kyleduo.switchbutton.SwitchButton;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class SettingActivity extends AppCompatActivity {

    AQuery aQuery = null;
    SwitchButton switch1, switch2;
    FrameLayout switch1_con, switch2_con, logout_con, leave_con;
    TextView switch1_text, switch2_text, logout, leave;
    ImageView back;
    TwoBtnDialog twoBtnDialog;
    LeaveDialog1 leaveDialog1;
    LeaveDialog2 leaveDialog2;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(SettingActivity.this);
                setMeizuDarkMode(SettingActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        switch1_con = (FrameLayout) findViewById(R.id.switch1_con);
        switch2_con = (FrameLayout) findViewById(R.id.switch2_con);
        switch1_text = (TextView) findViewById(R.id.switch1_text);
        switch2_text = (TextView) findViewById(R.id.switch2_text);
        switch1 = (SwitchButton) findViewById(R.id.switch1);
        switch2 = (SwitchButton) findViewById(R.id.switch2);
        logout_con = (FrameLayout) findViewById(R.id.logout_con);
        leave_con = (FrameLayout) findViewById(R.id.leave_con);
        logout = (TextView) findViewById(R.id.logout);
        leave = (TextView) findViewById(R.id.leave);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        switch1_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch1.performClick();
            }
        });
        switch1_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch1.performClick();
            }
        });
        switch2_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch2.performClick();
            }
        });
        switch2_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch2.performClick();
            }
        });
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                final String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/setting/setpush/notice";
                Map<String, Object> params = new HashMap<String, Object>();
                if (isChecked) {
                    params.put("value", "0");
                } else {
                    params.put("value", "1");
                }
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                final String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/setting/setpush/all";
                Map<String, Object> params = new HashMap<String, Object>();
                if (isChecked) {
                    params.put("value", "0");
                } else {
                    params.put("value", "1");
                }
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
        logout_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout.callOnClick();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                twoBtnDialog = new TwoBtnDialog(SettingActivity.this);
                twoBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                twoBtnDialog.setCancelable(false);
                twoBtnDialog.show();
            }
        });
        leave_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leave.callOnClick();
            }
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (false) {
                    leaveDialog1 = new LeaveDialog1(SettingActivity.this);
                    leaveDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    leaveDialog1.setCancelable(false);
                    leaveDialog1.show();
                } else {
                    leaveDialog2 = new LeaveDialog2(SettingActivity.this);
                    leaveDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    leaveDialog2.setCancelable(false);
                    leaveDialog2.show();
                }

            }
        });
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        final String token = prefToken.getString("Token", "");
        String url = UrlManager.getBaseUrl() + "/setting/getpush";
        Map<String, Object> params = new HashMap<String, Object>();
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                try {
                    if ("true".equals(jsonObject.getString("return"))) {
                        switch1.setVisibility(View.VISIBLE);
                        switch2.setVisibility(View.VISIBLE);
                        if ("1".equals(jsonObject.getString("all"))) {
                            switch2.setChecked(false);
                        } else if ("0".equals(jsonObject.getString("all"))) {
                            switch2.setChecked(true);
                        }
                        if ("1".equals(jsonObject.getString("notice"))) {
                            switch1.setChecked(false);
                        } else if ("0".equals(jsonObject.getString("notice"))) {
                            switch1.setChecked(true);
                        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case 999:
                setResult(999);
                finish();
                break;
        }
    }

    public class TwoBtnDialog extends Dialog {
        TwoBtnDialog twoBtnDialog = this;
        Context context;
        public TwoBtnDialog(final Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_two_btn_dialog);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView title1 = (TextView) findViewById(R.id.title1);
            TextView title2 = (TextView) findViewById(R.id.title2);
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            TextView btn2 = (TextView) findViewById(R.id.btn2);
            title2.setVisibility(View.GONE);
            title1.setText("로그아웃 하시겠습니까?");
            btn1.setText("확인");
            btn2.setText("취소");
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                    String token = prefToken.getString("Token", "");
                    String url = UrlManager.getBaseUrl() + "/member/logout";
                    Map<String, Object> params = new HashMap<String, Object>();
                    aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                        @Override
                        public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                            try {
                                if ("true".equals(jsonObject.getString("return"))) {
                                    Toast.makeText(context, "로그아웃 하였습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "로그아웃 하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                                SharedPreferences prefLoginChecked = getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefLoginChecked.edit();
                                editor.clear();
                                editor.commit();
                                twoBtnDialog.dismiss();
                                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                setResult(999);
                                startActivityForResult(intent, 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.header("User-Agent", "gh_mobile{" + token + "}"));
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    twoBtnDialog.dismiss();
                }
            });
        }
    }

    public class LeaveDialog1 extends Dialog {
        LeaveDialog1 leaveDialog1 = this;
        Context context;
        public LeaveDialog1(final Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_leave_dialog1);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveDialog1.dismiss();
                }
            });
        }
    }

    public class LeaveDialog2 extends Dialog {
        LeaveDialog2 leaveDialog2 = this;
        Context context;
        public LeaveDialog2(final Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_leave_dialog2);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            TextView btn2 = (TextView) findViewById(R.id.btn2);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                    String token = prefToken.getString("Token", "");
                    String url = UrlManager.getBaseUrl() + "/member/leave";
                    Map<String, Object> params = new HashMap<String, Object>();
                    aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                        @Override
                        public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                            try {
                                if ("true".equals(jsonObject.getString("return"))) {
                                    Toast.makeText(context, "탈퇴 하였습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "탈퇴 하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                                SharedPreferences prefLoginChecked = getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefLoginChecked.edit();
                                editor.clear();
                                editor.commit();
                                leaveDialog2.dismiss();
                                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                setResult(999);
                                startActivityForResult(intent, 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.header("User-Agent", "gh_mobile{" + token + "}"));
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveDialog2.dismiss();
                }
            });
        }
    }
}
