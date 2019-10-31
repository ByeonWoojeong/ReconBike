package app.cosmos.reconbike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class PhoneChangeActivity1 extends AppCompatActivity {

    FrameLayout phone_con;
    TextView id, phone, phone_text;
    ImageView back, next;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefLoginChecked = PhoneChangeActivity1.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
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
        setContentView(R.layout.activity_phone_change1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(PhoneChangeActivity1.this);
                setMeizuDarkMode(PhoneChangeActivity1.this);
            }
        }
        phone_con = (FrameLayout) findViewById(R.id.phone_con);
        phone_text = (TextView) findViewById(R.id.phone_text);
        id = (TextView) findViewById(R.id.id);
        phone = (TextView) findViewById(R.id.phone);
        back = (ImageView) findViewById(R.id.back);
        next = (ImageView) findViewById(R.id.next);
        phone_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.callOnClick();
            }
        });
        phone_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.callOnClick();
            }
        });
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.callOnClick();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhoneChangeActivity1.this, PhoneChangeActivity2.class);
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
}
