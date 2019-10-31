package app.cosmos.reconbike;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
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
import java.util.regex.Pattern;

import static app.cosmos.reconbike.SMSReceive.phone_change_is_auth_apply;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class PhoneChangeActivity2 extends AppCompatActivity {

    AQuery aQuery = null;
    TextView auth_number_btn, count_text, next;
    ImageView back;
    View under_line1, under_line2;
    int minCount = 2, secCount = 60;
    EditText phone;
    static EditText auth_number;
    FrameLayout next_con;
    OneBtnDialog oneBtnDialog;
    Handler handler = new Handler();

    public static void inputAuthNumber(String authNumber) {
        if (authNumber != null) {
            auth_number.setText(authNumber);
        }
    }

    Runnable count = new Runnable() {
        public void run() {
            secCount --;
            handler.postDelayed(this, 1000);
            if (secCount == 0) {
                count_text.setText(minCount + " : " + secCount);
                minCount --;
                secCount = 60;
                if (minCount < 0) {
                    handler.removeCallbacks(count);
                    minCount = 2;
                    secCount = 60;
                    auth_number_btn.setEnabled(true);
                    count_text.setVisibility(View.GONE);
                    phone_change_is_auth_apply = false;
                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "인증번호 입력시간이\n초과 되었습니다.\n다시 시도해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                }
            } else {
                count_text.setText(minCount + " : " + secCount);
            }
        }
    };

    boolean checkPhoneNumber(String number){
        boolean checkPhoneNumber = Pattern.matches("(01[016789])(\\d{3,4})(\\d{4})", number);
        return checkPhoneNumber;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        phone_change_is_auth_apply = false;
        handler.removeCallbacks(count);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_change2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(PhoneChangeActivity2.this);
                setMeizuDarkMode(PhoneChangeActivity2.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        phone = (EditText) findViewById(R.id.phone);
        auth_number = (EditText) findViewById(R.id.auth_number);
        auth_number_btn = (TextView) findViewById(R.id.auth_number_btn);
        count_text = (TextView) findViewById(R.id.count);
        under_line1 = findViewById(R.id.under_line1);
        under_line2 = findViewById(R.id.under_line2);
        next_con = (FrameLayout) findViewById(R.id.next_con);
        next = (TextView) findViewById(R.id.next);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SharedPreferences prefLoginChecked = PhoneChangeActivity2.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
        phone.setText(prefLoginChecked.getString("phone", "유심 인식불가"));
        auth_number_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(phone.getText().toString())) {
                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "휴대폰 번호를\n입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if (!checkPhoneNumber(phone.getText().toString().replaceAll("-", "").replaceAll("\\)", "").trim())) {
                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "올바른 휴대폰\n번호가 아닙니다.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/login/checkphone";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("phone", phone.getText().toString());
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                Toast.makeText(PhoneChangeActivity2.this, "인증번호를 전송 하였습니다.", Toast.LENGTH_SHORT).show();
                                auth_number_btn.setEnabled(false);
                                count_text.setVisibility(View.VISIBLE);
                                count.run();
                                phone_change_is_auth_apply = true;
                            } else if ("false".equals(jsonObject.getString("return"))) {
                                phone_change_is_auth_apply = false;
                                if ("phone".equals(jsonObject.getString("type"))) {
                                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "이미 회원가입 되어있는\n휴대폰번호 입니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "SMS전송 서버 장애로 인해\n인증번호 문자를 발송 할 수\n없습니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(phone.getText().toString())) {
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
        auth_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(auth_number.getText().toString())) {
                    under_line2.setBackgroundColor(Color.parseColor("#d7d7d7"));
                } else {
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
        next_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.callOnClick();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(auth_number.getText().toString()) || "" == auth_number.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "인증번호를 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if (!phone_change_is_auth_apply) {
                    oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "인증번호 입력시간이\n지났습니다.\n다시 시도해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                final String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/login/checksms";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("phone", phone.getText().toString());
                params.put("number", auth_number.getText().toString());
                params.put("tel", "1");
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                SharedPreferences prefLoginChecked = PhoneChangeActivity2.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefLoginChecked.edit();
                                editor.putString("phone", phone.getText().toString());
                                editor.commit();

                                finish();
                                Toast.makeText(PhoneChangeActivity2.this, "변경 되었습니다.", Toast.LENGTH_SHORT).show();
                            } else if ("false".equals(jsonObject.getString("return"))) {
                                phone_change_is_auth_apply = false;
                                auth_number.setText("");
                                auth_number_btn.setEnabled(true);
                                handler.removeCallbacks(count);
                                minCount = 2;
                                secCount = 60;
                                count_text.setVisibility(View.GONE);
                                oneBtnDialog = new OneBtnDialog(PhoneChangeActivity2.this, "인증번호가 틀렸습니다.\n다시 휴대폰 인증을 해주세요.", "1", "확인");
                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                oneBtnDialog.setCancelable(false);
                                oneBtnDialog.show();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
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
