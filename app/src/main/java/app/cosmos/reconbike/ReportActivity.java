package app.cosmos.reconbike;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tsengvn.typekit.TypekitContextWrapper;

import java.io.IOException;
import java.lang.reflect.Field;

import static app.cosmos.reconbike.GlobalApplication.applicationLifecycleHandler;

public class ReportActivity extends AppCompatActivity {

    SurfaceView cameraView;
    CameraSource cameraSource;
    Camera camera;
    Camera.Parameters camera_parameters;
    ImageView back, advice, advice2, flashlight, call;
    FrameLayout sub_title_con, faq_con;
    TextView sub_title, faq;
    boolean is_flashlight_on, is_serial_number_add;
    Handler handler = new Handler();

    Runnable autoFocus = new Runnable() {
        public void run() {
            if (!applicationLifecycleHandler.isInBackground()) {
                if (camera != null) {
                    try {
                        camera.autoFocus(mAutoFocus);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            handler.postDelayed(this, 5000);
        }
    };

    Camera.AutoFocusCallback mAutoFocus = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

    Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoFocus);
        if (camera != null) {
            camera.release();
            camera = null;
        }
        flashlight.setSelected(false);
        is_flashlight_on = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        is_serial_number_add = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_report);
        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1600, 1024).setRequestedFps(15.0f).build();
        flashlight = (ImageView) findViewById(R.id.flashlight);
        back = (ImageView) findViewById (R.id.back);
        advice = (ImageView) findViewById (R.id.advice);
        advice2 = (ImageView) findViewById (R.id.advice2);
        sub_title_con = (FrameLayout) findViewById(R.id.sub_title_con);
        sub_title = (TextView) findViewById(R.id.sub_title);
        call = (ImageView) findViewById(R.id.call);
        faq_con = (FrameLayout) findViewById(R.id.faq_con);
        faq = (TextView) findViewById(R.id.faq);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ReportActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraView.refreshDrawableState();
                    cameraSource.start(cameraView.getHolder());
                    camera = getCamera(cameraSource);
                    autoFocus.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    if (barcodes.valueAt(0).displayValue.length() == 12) {
                        if (!is_serial_number_add) {
                            is_serial_number_add = true;
                            Intent intent = new Intent(ReportActivity.this, ReportWriteActivity.class);
                            intent.putExtra("serial_number", barcodes.valueAt(0).displayValue);
                            startActivityForResult(intent, 1);
                        }
                    }
                }
            }
        });
        flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_flashlight_on) {
                    camera_parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera_parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    camera.setParameters(camera_parameters);
                    flashlight.setSelected(false);
                    is_flashlight_on = false;
                } else {
                    camera_parameters = camera.getParameters();
                    camera_parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera_parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    camera.setParameters(camera_parameters);
                    flashlight.setSelected(true);
                    is_flashlight_on = true;
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
                Intent intent = new Intent(ReportActivity.this, SerialNumberActivity.class);
                intent.putExtra("intent", "1");
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
                Intent intent = new Intent(ReportActivity.this, FaqActivity.class);
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
