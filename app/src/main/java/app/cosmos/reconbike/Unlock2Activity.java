package app.cosmos.reconbike;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class Unlock2Activity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener{

    private static final String TAG = "Unlock2Activity";

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private Boolean switchFlashlightButtonCheck;
    private ImageView flashlight, advice, back, call, advice2;
    FrameLayout sub_title_con, faq_con;
    TextView sub_title, faq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock2);

        switchFlashlightButtonCheck = true;

        barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
        flashlight = (ImageView) findViewById(R.id.flashlight);
        back = (ImageView) findViewById (R.id.back);
        advice = (ImageView) findViewById (R.id.advice);
        advice2 = (ImageView) findViewById (R.id.advice2);
        sub_title_con = (FrameLayout) findViewById(R.id.sub_title_con);
        sub_title = (TextView) findViewById(R.id.sub_title);
        call = (ImageView) findViewById(R.id.call);
        faq_con = (FrameLayout) findViewById(R.id.faq_con);
        faq = (TextView) findViewById(R.id.faq);

        if (!hasFlash()) {
            flashlight.setVisibility(View.GONE);
        }

        barcodeScannerView.setTorchListener(this);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchFlashlightButtonCheck) {
                    barcodeScannerView.setTorchOn();
                } else {
                    barcodeScannerView.setTorchOff();
                }
            }
        });

        sub_title_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sub_title.callOnClick();
            }
        });
        sub_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Unlock2Activity.this, SerialNumberActivity.class);
                intent.putExtra("intent", "2");
                startActivityForResult(intent, 1);
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:02-1234-1234"));
                startActivity(intent);
            }
        });
        faq_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                faq.callOnClick();
            }
        });
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Unlock2Activity.this, FaqActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        advice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        advice2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, " onActivityResult");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, " onResume");
        capture.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, " onPause");
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, " onDestroy");
        capture.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * TorchListener
     */
    @Override
    public void onTorchOn() {   //후레시 버튼 on
        flashlight.setImageResource(R.drawable.flashlight_n);
        switchFlashlightButtonCheck = false;
    }

    @Override
    public void onTorchOff() {  //후레시 버튼 off
        flashlight.setImageResource(R.drawable.flashlight_p);
        switchFlashlightButtonCheck = true;
    }
}
