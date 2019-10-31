package app.cosmos.reconbike;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    String token;

    Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(getApplicationContext(), "최신 버전으로 업데이트 해주세요.", Toast.LENGTH_SHORT).show();
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg){
            SharedPreferences prefLoginChecked = getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
            Boolean loginChecked = prefLoginChecked.getBoolean("loginChecked", false);
            if (loginChecked) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                finish();
                startActivityForResult(intent, 1);
            } else {
                Intent intent = new Intent(SplashActivity.this, TutorialActivity.class);
                finish();
                startActivityForResult(intent, 1);
            }
        }
    };

    Runnable token_load = new Runnable() {
        public void run() {
            SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
            token = prefToken.getString("Token", "");
            handler.postDelayed(this, 500);
            if (!"".equals(token.toString())){
                handler.removeCallbacks(token_load);
                handler.sendEmptyMessageDelayed(0, 500);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        registerForContextMenu(findViewById(R.id.imageView));
        final PermissionListener permissionlistener4 = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                ConnectivityManager connectivityManager = (ConnectivityManager) SplashActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                String store_version = MarketVersionChecker.getMarketVersion(getPackageName());
//                                if (store_version != null) {
//                                    try {
//                                        String device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//                                        if (store_version.compareTo(device_version) > 0) {
//                                            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
//                                            marketLaunch.setData(Uri.parse("market://details?id=app.cosmos.reconbike"));
//                                            startActivity(marketLaunch);
//                                            Message msg = toastHandler.obtainMessage();
//                                            toastHandler.sendMessage(msg);
//                                            moveTaskToBack(true);
//                                            ActivityCompat.finishAffinity(SplashActivity.this);
//                                            finish();
//                                        } else {



                            token_load.run();



//                                        }
//                                    } catch (PackageManager.NameNotFoundException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        }).start();

                    } else {
                        Toast.makeText(SplashActivity.this, "인터넷이 연결되어 있지 않아 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                        setResult(999);
                        finish();
                    }
                } else {
                    Toast.makeText(SplashActivity.this, "인터넷이 연결되어 있지 않아 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                    setResult(999);
                    finish();
                }
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };

        final PermissionListener permissionlistener3 = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                new TedPermission(SplashActivity.this)
                        .setPermissionListener(permissionlistener4)
                        .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .check();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };


        final PermissionListener permissionlistener2 = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                new TedPermission(SplashActivity.this)
                        .setPermissionListener(permissionlistener3)
                        .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                        .check();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };

        final PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                new TedPermission(SplashActivity.this)
                        .setPermissionListener(permissionlistener2)
                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                finish();
            }
        };

        new TedPermission(SplashActivity.this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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