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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class JoinActivity extends AppCompatActivity {

    AQuery aQuery = null;
    ImageView back;
    TextView auth_number_btn, next;
    EditText id, password, password_re;
    ImageView join_ok, join_x;
    FrameLayout next_con;
    View under_line1, under_line2, under_line3;
    OneBtnDialog oneBtnDialog;
    boolean is_id_checked = false;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(JoinActivity.this);
                setMeizuDarkMode(JoinActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        auth_number_btn = (TextView) findViewById(R.id.auth_number_btn);
        id = (EditText) findViewById(R.id.id);
        password = (EditText) findViewById(R.id.password);
        password_re = (EditText) findViewById(R.id.password_re);
        next_con = (FrameLayout) findViewById(R.id.next_con);
        join_ok = (ImageView) findViewById(R.id.join_ok);
        join_x = (ImageView) findViewById(R.id.join_x);
        under_line1 = findViewById(R.id.under_line1);
        under_line2 = findViewById(R.id.under_line2);
        under_line3 = findViewById(R.id.under_line3);
        next = (TextView) findViewById(R.id.next);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                is_id_checked = false;
                if ("".equals(id.getText().toString())) {
                    under_line1.setBackgroundColor(Color.parseColor("#d7d7d7"));
                } else {
                    under_line1.setBackgroundColor(Color.parseColor("#212121"));
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(password.getText().toString())) {
                    join_ok.setVisibility(View.GONE);
                    join_x.setVisibility(View.GONE);
                    under_line2.setBackgroundColor(Color.parseColor("#d7d7d7"));
                } else {
                    if (password.getText().toString().trim().equals(password_re.getText().toString().trim())) {
                        join_ok.setVisibility(View.VISIBLE);
                        join_x.setVisibility(View.GONE);
                    } else {
                        if ("".equals(password_re.getText().toString())) {
                            join_ok.setVisibility(View.GONE);
                            join_x.setVisibility(View.GONE);
                        } else {
                            join_ok.setVisibility(View.GONE);
                            join_x.setVisibility(View.VISIBLE);
                        }
                    }
                    under_line2.setBackgroundColor(Color.parseColor("#212121"));
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        password_re.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(password_re.getText().toString())) {
                    join_ok.setVisibility(View.GONE);
                    join_x.setVisibility(View.GONE);
                    under_line3.setBackgroundColor(Color.parseColor("#d7d7d7"));
                } else {
                    if (password.getText().toString().trim().equals(password_re.getText().toString().trim())) {
                        join_ok.setVisibility(View.VISIBLE);
                        join_x.setVisibility(View.GONE);
                    } else {
                        if ("".equals(password.getText().toString())) {
                            join_ok.setVisibility(View.GONE);
                            join_x.setVisibility(View.GONE);
                        } else {
                            join_ok.setVisibility(View.GONE);
                            join_x.setVisibility(View.VISIBLE);
                        }
                    }
                    under_line3.setBackgroundColor(Color.parseColor("#212121"));
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        auth_number_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(id.getText().toString().trim()) || "" == id.getText().toString().trim()) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "아이디를 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/login/checkid";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id.getText().toString().trim());
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                oneBtnDialog = new OneBtnDialog(JoinActivity.this, "사용 가능한 아이디입니다.", "1", "확인");
                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                oneBtnDialog.setCancelable(false);
                                oneBtnDialog.show();
                                is_id_checked = true;
                            } else if ("false".equals(jsonObject.getString("return"))) {
                                oneBtnDialog = new OneBtnDialog(JoinActivity.this, "이미 사용중인 아이디입니다.\n다른 아이디로 변경해주세요.", "1", "확인");
                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                oneBtnDialog.setCancelable(false);
                                oneBtnDialog.show();
                                is_id_checked = false;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
        next_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.callOnClick();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(id.getText().toString()) || "" == id.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "아이디를 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if (!is_id_checked) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "아이디 중복학인을\n체크해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if ("".equals(password.getText().toString()) || "" == password.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "비밀번호를 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if (4 > password.getText().toString().length()) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "비밀번호는 최소 4자\n이상으로 작성해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if ("".equals(password_re.getText().toString()) || "" == password_re.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "비밀번호를 확인을\n입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if (!password.getText().toString().equals(password_re.getText().toString())) {
                    oneBtnDialog = new OneBtnDialog(JoinActivity.this, "비밀번호 확인이 다릅니다.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/login/id";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id.getText().toString().trim());
                params.put("pass", password.getText().toString().trim());
                params.put("join", "1");
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                Intent intent = new Intent(JoinActivity.this, JoinAuthActivity.class);
                                startActivityForResult(intent, 1);
                            } else if ("false".equals(jsonObject.getString("return"))) {
                                Toast.makeText(getApplicationContext(), "네트워크 에러. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
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
            if ("1".equals(text2)) {
                title2.setVisibility(View.GONE);
            }
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
