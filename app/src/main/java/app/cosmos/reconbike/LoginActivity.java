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
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    AQuery aQuery = null;
    ImageView back, login, join;
    EditText id, password;
    TextView id_get, password_get;
    OneBtnDialog oneBtnDialog;
    CustomProgressDialog customProgressDialog;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(LoginActivity.this);
                setMeizuDarkMode(LoginActivity.this);
            }
        }
        aQuery = new AQuery(this);
        customProgressDialog = new CustomProgressDialog(LoginActivity.this, "로그인 중 입니다.");
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);
        back = (ImageView) findViewById(R.id.back);
        id = (EditText) findViewById(R.id.id);
        password = (EditText) findViewById(R.id.password);
        login = (ImageView) findViewById(R.id.login);
        id_get = (TextView) findViewById(R.id.id_get);
        password_get = (TextView) findViewById(R.id.password_get);
        join = (ImageView) findViewById(R.id.join);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(id.getText().toString()) || "" == id.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(LoginActivity.this, "아이디를 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if ("".equals(password.getText().toString()) || "" == password.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(LoginActivity.this, "비밀번호를 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                customProgressDialog.show();
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/login/id";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id.getText().toString().trim());
                params.put("pass", password.getText().toString().trim());
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                if ("ok".equals(jsonObject.getString("type"))) {
                                    SharedPreferences prefLoginChecked = LoginActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefLoginChecked.edit();
                                    editor.clear();
                                    editor.putBoolean("loginChecked", true);
                                    editor.putString("type", "nomal");
                                    editor.putString("id", jsonObject.getString("id"));
                                    editor.putString("phone", jsonObject.getString("phone"));
                                    editor.commit();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    setResult(999);
                                    startActivityForResult(intent, 1);
                                    Toast.makeText(LoginActivity.this, "로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                                } else if ("detail".equals(jsonObject.getString("type"))) {
                                    Intent intent = new Intent(LoginActivity.this, JoinAuthActivity.class);
                                    startActivityForResult(intent, 1);
                                }
                            } else if ("false".equals(jsonObject.getString("return"))) {
                                Toast.makeText(getApplicationContext(), "네트워크 에러. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                            customProgressDialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
        id_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, IdGetActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        password_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, PassGetActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivityForResult(intent, 1);
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

    public class CustomProgressDialog extends Dialog {
        TextView title;
        public CustomProgressDialog(Context context, String text) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_progress);
            title = (TextView) findViewById(R.id.title);
            title.setText(text);
        }
    }
}
