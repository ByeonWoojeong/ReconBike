package app.cosmos.reconbike;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.tsengvn.typekit.TypekitContextWrapper;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class BillingListActivity extends AppCompatActivity {

    AQuery aQuery = null;
    String menu, type;
    ImageView back;
    LinearLayout phone_con, card_con, wire_con;
    TextView phone_text, card_text, wire_text, next;
    ImageView phone, card, wire;
    FrameLayout next_con;
    OneBtnDialog oneBtnDialog;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(BillingListActivity.this);
                setMeizuDarkMode(BillingListActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        phone_con = (LinearLayout) findViewById(R.id.phone_con);
        card_con = (LinearLayout) findViewById(R.id.card_con);
        wire_con = (LinearLayout) findViewById(R.id.wire_con);
        phone_text = (TextView) findViewById(R.id.phone_text);
        card_text = (TextView) findViewById(R.id.card_text);
        wire_text = (TextView) findViewById(R.id.wire_text);
        phone = (ImageView) findViewById(R.id.phone);
        card = (ImageView) findViewById(R.id.card);
        wire = (ImageView) findViewById(R.id.wire);
        next_con = (FrameLayout) findViewById(R.id.next_con);
        next = (TextView) findViewById(R.id.next);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        phone_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone.callOnClick();
            }
        });
        phone_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone.callOnClick();
            }
        });
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_p));
                card_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_n));
                wire_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_n));
                phone_text.setTextColor(Color.parseColor("#f41758"));
                card_text.setTextColor(Color.parseColor("#b9b9b9"));
                wire_text.setTextColor(Color.parseColor("#b9b9b9"));
                phone.setSelected(true);
                card.setSelected(false);
                wire.setSelected(false);
                type = "phone";
            }
        });
        card_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card.callOnClick();
            }
        });
        card_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card.callOnClick();
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_n));
                card_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_p));
                wire_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_n));
                phone_text.setTextColor(Color.parseColor("#b9b9b9"));
                card_text.setTextColor(Color.parseColor("#f41758"));
                wire_text.setTextColor(Color.parseColor("#b9b9b9"));
                phone.setSelected(false);
                card.setSelected(true);
                wire.setSelected(false);
                type = "card";
            }
        });
        wire_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wire.callOnClick();
            }
        });
        wire_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wire.callOnClick();
            }
        });
        wire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_n));
                card_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_n));
                wire_con.setBackground(getResources().getDrawable(R.drawable.billing_type_back_p));
                phone_text.setTextColor(Color.parseColor("#b9b9b9"));
                card_text.setTextColor(Color.parseColor("#b9b9b9"));
                wire_text.setTextColor(Color.parseColor("#f41758"));
                phone.setSelected(false);
                card.setSelected(false);
                wire.setSelected(true);
                type = "wire";
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
//                if (menu == null) {
//                    oneBtnDialog = new OneBtnDialog(BillingListActivity.this, "결제 금액을\n선택해 주세요.", "1", "확인");
//                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                    oneBtnDialog.setCancelable(false);
//                    oneBtnDialog.show();
//                    return;
//                }
                if (type == null) {
                    oneBtnDialog = new OneBtnDialog(BillingListActivity.this, "결제 방식을\n선택해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
//                Intent intent = new Intent(BillingListActivity.this, BillingActivity.class);
//                if ("1".equals(menu)) {
//                    intent.putExtra("menu_name", "500콩");
//                    intent.putExtra("menu_price", "4800");
//                } else if ("2".equals(menu)) {
//                    intent.putExtra("menu_name", "1100콩");
//                    intent.putExtra("menu_price", "9800");
//                } else if ("3".equals(menu)) {
//                    intent.putExtra("menu_name", "2200콩");
//                    intent.putExtra("menu_price", "17800");
//                }
//                intent.putExtra("type",type);
//                startActivityForResult(intent, 1);
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
            case 444:
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
