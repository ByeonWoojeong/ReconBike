package app.cosmos.reconbike;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.tsengvn.typekit.TypekitContextWrapper;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class SerialNumberActivity extends AppCompatActivity {

    AQuery aQuery = null;
    InputMethodManager ipm;
    SoftKeyBoard softKeyBoard;
    boolean isKeyBoardVisible;
    LinearLayout serial_number;
    EditText serial_number_text;
    ImageView back, next;
    View under_line;
    String getIntent;
    OneBtnDialog oneBtnDialog;

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
        setContentView(R.layout.activity_serial_number);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(SerialNumberActivity.this);
                setMeizuDarkMode(SerialNumberActivity.this);
            }
        }
        aQuery = new AQuery(this);
        back = (ImageView) findViewById(R.id.back);
        serial_number = (LinearLayout) findViewById(R.id.serial_number);
        serial_number_text = (EditText) findViewById(R.id.serial_number_text);
        next = (ImageView) findViewById(R.id.next);
        under_line = findViewById(R.id.under_line);
        ipm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        softKeyBoard = new SoftKeyBoard(serial_number, ipm);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipm.hideSoftInputFromWindow(serial_number_text.getWindowToken(), 0);
                finish();
            }
        });
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
        serial_number_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("".equals(serial_number_text.getText().toString())) {
                    under_line.setBackgroundColor(Color.parseColor("#c3c3c3"));
                } else {
                    under_line.setBackgroundColor(Color.parseColor("#212121"));
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
        serial_number_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT:
                        next.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_GO:
                        next.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_SEND:
                        next.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_SEARCH:
                        next.callOnClick();
                        break;
                    case EditorInfo.IME_ACTION_DONE:
                        next.callOnClick();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("".equals(serial_number_text.getText().toString().trim()) || "" == serial_number_text.getText().toString().trim()) {
                    oneBtnDialog = new OneBtnDialog(SerialNumberActivity.this, "일련번호를 입력해 주세요.", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if (serial_number_text.getText().toString().trim().length() != 12) {
                    oneBtnDialog = new OneBtnDialog(SerialNumberActivity.this, "일련번호를 정확히\n입력해 주세요.", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
//                oneBtnDialog = new OneBtnDialog(SerialNumberActivity.this, "일치하는 일련번호가 없습니다.\n다시 입력해 주세요.", "확인");
//                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                oneBtnDialog.setCancelable(false);
//                oneBtnDialog.show();
                if ("2".equals(getIntent)) {
                    ipm.hideSoftInputFromWindow(serial_number_text.getWindowToken(), 0);
                    Intent intent = new Intent();
                    intent.putExtra("serial_number", serial_number_text.getText().toString().trim());
                    setResult(777, intent);
                    finish();
                } else {
                    Intent intent = new Intent(SerialNumberActivity.this, ReportWriteActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });
        Intent intent = getIntent();
        getIntent = intent.getStringExtra("intent");
        if (getIntent != null) {
            serial_number_text.requestFocus();
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
            case 999:
                setResult(999);
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
