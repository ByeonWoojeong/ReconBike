package app.cosmos.reconbike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.tsengvn.typekit.TypekitContextWrapper;

public class MyPageActivity extends AppCompatActivity {

    AQuery aQuery = null;
    ImageView back, setting, update, billing1, billing2;
    FrameLayout billing_con, expiry_date_con;
    LinearLayout menu1_con, menu2_con, menu3_con, menu4_con, menu5_con, call_con_con, call_con;
    TextView id, billing_title1, billing_title2, expiry_date_title1, expiry_date_title2, expiry_date_title3, expiry_date, menu1_text, menu2_text, menu3_text, menu4_text, menu5_text, call_text;
    ImageView menu1, menu2, menu3, menu4, menu5, call;
    NPTextView phone;
    String expire, expire_minute;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefLoginChecked = MyPageActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
        if ("nomal".equals(prefLoginChecked.getString("type", ""))) {
            id.setText(prefLoginChecked.getString("id", ""));
        } else if ("naver".equals(prefLoginChecked.getString("type", ""))) {
            id.setText("Naver ID");
        } else if ("kakao".equals(prefLoginChecked.getString("type", ""))) {
            id.setText("Kakao ID");
        } else if ("google".equals(prefLoginChecked.getString("type", ""))) {
            id.setText("Google ID");
        }
        phone.setText(prefLoginChecked.getString("phone", ""));
        if (expire_minute != null) {
            if ("0".equals(expire_minute)) {
                billing_con.setVisibility(View.VISIBLE);
            } else {
                expiry_date_con.setVisibility(View.VISIBLE);
                if ("0".equals(expire)) {
                    expiry_date.setText((Integer.parseInt(expire_minute) / 60) + "");
                    expiry_date_title2.setText("시간");
                } else {
                    expiry_date.setText(expire+"");
                    expiry_date_title2.setText("일");
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
        setContentView(R.layout.activity_my_page);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#212121"));
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        setting = (ImageView) findViewById(R.id.setting);
        update = (ImageView) findViewById(R.id.update);
        id = (TextView) findViewById(R.id.id);
        phone = (NPTextView) findViewById(R.id.phone);
        billing_con = (FrameLayout) findViewById(R.id.billing_con);
        expiry_date_con = (FrameLayout) findViewById(R.id.expiry_date_con);
        billing_title1 = (TextView) findViewById(R.id.billing_title1);
        billing_title2 = (TextView) findViewById(R.id.billing_title2);
        expiry_date_title1 = (TextView) findViewById(R.id.expiry_date_title1);
        expiry_date_title2 = (TextView) findViewById(R.id.expiry_date_title2);
        expiry_date_title3 = (TextView) findViewById(R.id.expiry_date_title3);
        expiry_date = (TextView) findViewById(R.id.expiry_date);
        billing1 = (ImageView) findViewById(R.id.billing1);
        billing2 = (ImageView) findViewById(R.id.billing2);
        menu1_con = (LinearLayout) findViewById(R.id.menu1_con);
        menu2_con = (LinearLayout) findViewById(R.id.menu2_con);
        menu3_con = (LinearLayout) findViewById(R.id.menu3_con);
        menu4_con = (LinearLayout) findViewById(R.id.menu4_con);
        menu5_con = (LinearLayout) findViewById(R.id.menu5_con);
        menu1_text = (TextView) findViewById(R.id.menu1_text);
        menu2_text = (TextView) findViewById(R.id.menu2_text);
        menu3_text = (TextView) findViewById(R.id.menu3_text);
        menu4_text = (TextView) findViewById(R.id.menu4_text);
        menu5_text = (TextView) findViewById(R.id.menu5_text);
        menu1 = (ImageView) findViewById(R.id.menu1);
        menu2 = (ImageView) findViewById(R.id.menu2);
        menu3 = (ImageView) findViewById(R.id.menu3);
        menu4 = (ImageView) findViewById(R.id.menu4);
        menu5 = (ImageView) findViewById(R.id.menu5);
        call_con_con = (LinearLayout) findViewById(R.id.call_con_con);
        call_con = (LinearLayout) findViewById(R.id.call_con);
        call_text = (TextView) findViewById(R.id.call_text);
        call = (ImageView) findViewById(R.id.call);
        Intent intent = getIntent();
        expire = intent.getStringExtra("expire");
        expire_minute = intent.getStringExtra("expire_minute");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, SettingActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, PhoneChangeActivity1.class);
                startActivityForResult(intent, 1);
            }
        });
        billing_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing1.callOnClick();
            }
        });
        billing_title1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing1.callOnClick();
            }
        });
        billing_title2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing1.callOnClick();
            }
        });
        billing1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, BillingListActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        expiry_date_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing2.callOnClick();
            }
        });
        expiry_date_title1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing2.callOnClick();
            }
        });
        expiry_date_title2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing2.callOnClick();
            }
        });
        expiry_date_title3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing2.callOnClick();
            }
        });
        expiry_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billing2.callOnClick();
            }
        });
        billing2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, BillingListActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        menu1_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu1.callOnClick();
            }
        });
        menu1_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu1.callOnClick();
            }
        });
        menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, UseHistoryActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        menu2_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu2.callOnClick();
            }
        });
        menu2_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu2.callOnClick();
            }
        });
        menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, ReportActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        menu3_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu3.callOnClick();
            }
        });
        menu3_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu3.callOnClick();
            }
        });
        menu3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, NoticeActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        menu4_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu4.callOnClick();
            }
        });
        menu4_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu4.callOnClick();
            }
        });
        menu4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPageActivity.this, FaqActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        menu5_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu5.callOnClick();
            }
        });
        menu5_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu5.callOnClick();
            }
        });
        menu5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        call_con_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call.callOnClick();
            }
        });
        call_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call.callOnClick();
            }
        });
        call_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call.callOnClick();
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:1234-5678"));
                startActivity(intent);
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
}
