package app.cosmos.reconbike;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.tsengvn.typekit.TypekitContextWrapper;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static app.cosmos.reconbike.BLECallback.getBLECallback;
import static app.cosmos.reconbike.GlobalApplication.applicationLifecycleHandler;

public class UnlockActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    AQuery aQuery = null;
    LocationManager locManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    SurfaceView cameraView;
    CameraSource cameraSource;
    Camera camera;
    Camera.Parameters camera_parameters;
    ImageView back, advice, advice2, flashlight, call;
    FrameLayout sub_title_con, faq_con;
    TextView sub_title, faq;
    boolean is_flashlight_on, is_serial_number_adding;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BLECallback bleCallback;
    String get_qrcode;
    SpinKitView spinKitView;
    OneBtnDialog oneBtnDialog;
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

    Runnable autoScanClose = new Runnable() {
        @Override
        public void run() {
            if (is_serial_number_adding) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bleCallback.deviceIndex = 0;
                        bleCallback.devicesDiscovered.clear();
                        bleCallback.bluetoothLeScanner.stopScan(bleCallback.scanCallback);
                        if (!UnlockActivity.this.isFinishing()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    is_serial_number_adding = false;
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "시간이 오버 되었습니다.\n자전거에 근접하여\n다시 시도해주세요. ", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                    spinKitView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
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
        handler.removeCallbacks(autoScanClose);
        if (camera != null) {
            camera.release();
            camera = null;
        }
        flashlight.setSelected(false);
        is_flashlight_on = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1500);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CheckPermission();
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mGoogleApiClient.disconnect();
        if (!is_serial_number_adding) {
            is_serial_number_adding = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    spinKitView.setVisibility(View.VISIBLE);
                }
            });
            SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
            final String token = prefToken.getString("Token", "");
            String url = UrlManager.getBaseUrl() + "/member/riding/start";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ble", get_qrcode);
            params.put("lat", latitude);
            params.put("lng", longitude);
            aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                    try {
                        if ("true".equals(jsonObject.getString("return"))) {
                            bleCallback.device_id = jsonObject.getString("bike_id");
                            bleCallback.command = "&lopen";
                            bleCallback.deviceIndex = 0;
                            bleCallback.devicesDiscovered.clear();
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    bleCallback.bluetoothLeScanner.startScan(bleCallback.scanCallback);
                                }
                            });
                            handler.postDelayed(autoScanClose, 10000);
                        } else if ("false".equals(jsonObject.getString("return"))) {
                            if ("free".equals(jsonObject.getString("type"))) {
                                if (oneBtnDialog == null) {
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "남은 기간을\n연장 해주세요.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    if (!oneBtnDialog.isShowing()) {
                                        oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "남은 기간을\n연장 해주세요.", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                    }
                                }
                            } else if ("bike".equals(jsonObject.getString("type"))) {
                                if (oneBtnDialog == null) {
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "존재하지 않는\nQR CODE 입니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    if (!oneBtnDialog.isShowing()) {
                                        oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "존재하지 않는\nQR CODE 입니다.", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                    }
                                }
                            } else if ("lock".equals(jsonObject.getString("type"))) {
                                if (oneBtnDialog == null) {
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "탑승 불가능한\n자전거 입니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    if (!oneBtnDialog.isShowing()) {
                                        oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "탑승 불가능한\n자전거 입니다.", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                    }
                                }
                            } else if ("riding".equals(jsonObject.getString("type"))) {
                                if (oneBtnDialog == null) {
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "이미 탑승중인\n자전거 입니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    if (!oneBtnDialog.isShowing()) {
                                        oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "이미 탑승중인\n자전거 입니다.", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                    }
                                }
                            } else if ("zone".equals(jsonObject.getString("type"))) {
                                if (oneBtnDialog == null) {
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "탑승 가능한\n지역이 아닙니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    if (!oneBtnDialog.isShowing()) {
                                        oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "탑승 가능한\n지역이 아닙니다.", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                    }
                                }
                            }
                            is_serial_number_adding = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handler.removeCallbacks(autoScanClose);
                                    spinKitView.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.header("User-Agent", "gh_mobile{" + token + "}"));
        }
    }

    private void CheckPermission() {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_CONTACTS}, 2);
            return;
        }
        hasWriteContactsPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_CONTACTS}, 2);
            return;
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    void l_op_alrdy() {
        handler.removeCallbacks(autoScanClose);
        if (!UnlockActivity.this.isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    is_serial_number_adding = false;
                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "이미 열려 있습니다.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    spinKitView.setVisibility(View.GONE);
                }
            });
        }
    }

    void l_op_ok() {
        handler.removeCallbacks(autoScanClose);
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        final String token = prefToken.getString("Token", "");
        String url = UrlManager.getBaseUrl() + "/member/riding/open";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ble", get_qrcode);
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                try {
                    if ("true".equals(jsonObject.getString("return"))) {
                        if (!UnlockActivity.this.isFinishing()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    is_serial_number_adding = false;
                                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "잠금이 열렸습니다.", "2", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                    spinKitView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.header("User-Agent", "gh_mobile{" + token + "}"));
    }

    void l_op_err() {
        handler.removeCallbacks(autoScanClose);
        if (!UnlockActivity.this.isFinishing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    is_serial_number_adding = false;
                    oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "자전거 디바이스가 정상적으로\n작동하지 않습니다.\n고장 및 불편\n신고를 해주세요.", "2", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    spinKitView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        is_serial_number_adding = false;
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
        setContentView(R.layout.activity_unlock);
        aQuery = new AQuery(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
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
        spinKitView = (SpinKitView) findViewById(R.id.spinKitView);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "안드로이드 버전이 낮아서\n이용할 수 없습니다.", "2", "확인");
            oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            oneBtnDialog.setCancelable(false);
            oneBtnDialog.show();
        }
        bleCallback = getBLECallback();
        bleCallback.setUnlockActivity(UnlockActivity.this);
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            oneBtnDialog = new OneBtnDialog(UnlockActivity.this, "블루투스가 꺼져 있습니다.\n블루투스를 활성화 해주세요.", "2", "확인");
            oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            oneBtnDialog.setCancelable(false);
            oneBtnDialog.show();
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
            return;
        }
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
                    if (ActivityCompat.checkSelfPermission(UnlockActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                        if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            Toast.makeText(UnlockActivity.this, "GPS가 꺼져있습니다.'위치 서비스’에서 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        } else {
                            get_qrcode = barcodes.valueAt(0).displayValue;
                            mGoogleApiClient.connect();
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
                Intent intent = new Intent(UnlockActivity.this, SerialNumberActivity.class);
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
                Intent intent = new Intent(UnlockActivity.this, FaqActivity.class);
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
            case 777:
                if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Toast.makeText(UnlockActivity.this, "GPS가 꺼져있습니다.'위치 서비스’에서 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                } else {
                    get_qrcode = data.getStringExtra("serial_number");
                    mGoogleApiClient.connect();
                }
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
            btn1.setText(btnText);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    oneBtnDialog.dismiss();
                }
            });
            if ("1".equals(text2)) {
                title2.setVisibility(View.GONE);
            } else if ("2".equals(text2)) {
                title2.setVisibility(View.GONE);
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        oneBtnDialog.dismiss();
                        finish();
                    }
                });
            }
        }
    }
}
