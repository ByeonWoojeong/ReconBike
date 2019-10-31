package app.cosmos.reconbike;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.skp.Tmap.TMapView;
import com.tsengvn.typekit.TypekitContextWrapper;


import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import app.cosmos.reconbike.DTO.AddressDTO;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, MapView.MapViewEventListener {

    AQuery aQuery = null;
    LocationManager locManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    MapView mapView;
    TMapView mMapView;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BLECallback bleCallback;
    MapPOIItem my_location_marker, bike_marker_wait, bike_marker_use;
    ArrayList<MapPoint> zone_points, bike_points;
    ArrayList<String> zone_points_radius;
    double StartLatitude, StartLongitude;
    String expire, expire_minute, riding_type, getMyX, getMyY, getLa, getLo;
    boolean is_create, is_running_animation, is_locking, is_scanning;
    FrameLayout sub_title_con;
    TextView sub_title1, sub_title2;
    ImageView my_page, search_main, report, faq, refresh, gps, unlock;
    YoYo.YoYoString shake;
    Animation fade_in_slide_in_from_top, fade_in_slide_out_to_top;
    SpinKitView spinKitView;
    BackPressCloseHandler backPressCloseHandler;
    OneBtnDialog oneBtnDialog;
    Handler handler = new Handler();

    Runnable autoScanClose = new Runnable() {
        @Override
        public void run() {
            if (is_scanning) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            bleCallback.deviceIndex = 0;
                            bleCallback.devicesDiscovered.clear();
                            bleCallback.bluetoothLeScanner.stopScan(bleCallback.scanCallback);
                            if (!MainActivity.this.isFinishing()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        is_locking = false;
                                        oneBtnDialog = new OneBtnDialog(MainActivity.this, "자전거를 찾을 수 없습니다.\n자전거에 근접하여\n다시 시도해주세요. ", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                        spinKitView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
    };

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
        if (is_locking) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            mGoogleApiClient.disconnect();
            SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
            final String token = prefToken.getString("Token", "");
            String url = UrlManager.getBaseUrl() + "/member/riding/stop";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("lat", latitude+"");
            params.put("lng", longitude+"");
            aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                    try {
                        if ("true".equals(jsonObject.getString("return"))) {
                            l_st_cl();
//                            bleCallback.command = "&lstate";
//                            bleCallback.deviceIndex = 0;
//                            bleCallback.devicesDiscovered.clear();
//                            AsyncTask.execute(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                        is_scanning = true;
//                                        bleCallback.bluetoothLeScanner.startScan(bleCallback.scanCallback);
//                                    }
//                                }
//                            });
//                            handler.postDelayed(autoScanClose, 10000);
                        } else if ("false".equals(jsonObject.getString("return"))) {
                            if (!MainActivity.this.isFinishing()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        is_locking = false;
                                        handler.removeCallbacks(autoScanClose);
                                        if (oneBtnDialog == null) {
                                            oneBtnDialog = new OneBtnDialog(MainActivity.this, "빨간색 영역 안에서\n잠금을 시도해주세요.", "1", "확인");
                                            oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                            oneBtnDialog.setCancelable(false);
                                            oneBtnDialog.show();
                                        } else {
                                            if (!oneBtnDialog.isShowing()) {
                                                oneBtnDialog = new OneBtnDialog(MainActivity.this, "빨간색 영역 안에서\n잠금을 시도해주세요.", "1", "확인");
                                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                oneBtnDialog.setCancelable(false);
                                                oneBtnDialog.show();
                                            }
                                        }
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
        } else {
            StartLatitude = location.getLatitude();
            StartLongitude = location.getLongitude();
            mGoogleApiClient.disconnect();
            String url = "https://api2.sktelecom.com/tmap/geo/reversegeocoding?version=1&appKey=9241461c-cece-4c80-a03f-03da0124f1fe&lat=" + StartLatitude + "&lon=" + StartLongitude + "&coordType=WGS84GEO&addressType=A02";
            aQuery.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                    if (jsonObject != null) {
                        try {
                            Gson gson = new Gson();
                            AddressDTO getAddress = gson.fromJson(jsonObject.getString("addressInfo"),AddressDTO.class);
                            Toast.makeText(MainActivity.this, "현위치는 "+ getAddress.getFullAddress() + " 입니다.", Toast.LENGTH_SHORT).show();
                            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(StartLatitude, StartLongitude), 3, true);
                            SharedPreferences saveLocation = getSharedPreferences("saveLocation", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = saveLocation.edit();
                            editor.putString("myla", StartLatitude+"");
                            editor.putString("mylo", StartLongitude+"");
                            editor.commit();
                            gps.setSelected(false);
                            if (shake != null) {
                                shake.stop(true);
                            }
                            if (my_location_marker != null) {
                                mapView.removePOIItem(my_location_marker);
                            }
                            my_location_marker = new MapPOIItem();
                            my_location_marker.setShowCalloutBalloonOnTouch(false);
                            my_location_marker.setItemName("현재 위치");
                            my_location_marker.setMapPoint(MapPoint.mapPointWithGeoCoord(StartLatitude, StartLongitude));
                            my_location_marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            my_location_marker.setCustomImageResourceId(R.drawable.my_location);
                            my_location_marker.setCustomImageAutoscale(false);
                            my_location_marker.setCustomImageAnchor(0.5f, 0.5f);
                            mapView.addPOIItem(my_location_marker);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
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
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (locationAvailability.isLocationAvailable()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void addCircles(MapPoint mapPoint, int radius) {
        MapCircle getCircle = new MapCircle(mapPoint, radius, Color.parseColor("#1aeb1653"), Color.parseColor("#1aeb1653"));
        mapView.addCircle(getCircle);
    }

    void billing(String why_bill) {
//        Intent intent = new Intent(MainActivity.this, BillingListActivity.class);
//        intent.putExtra("why_bill", why_bill);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        startActivityForResult(intent, 1);
    }

    void refresh_marker() {
        Log.d("asdf", "refresh_marker: asdf");
        SharedPreferences prefToken = getApplicationContext().getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        final String token = prefToken.getString("Token", "");
        String url = UrlManager.getBaseUrl() + "/gps";
        Map<String, Object> params = new HashMap<String, Object>();
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                try {
                    Log.d("asdf", "callback: "+jsonObject.toString());
                    if ("true".equals(jsonObject.getString("return"))) {
                        mapView.removeAllPOIItems();
                        mapView.removeAllCircles();
                        if (!"".equals(getMyX)) {
                            my_location_marker = new MapPOIItem();
                            my_location_marker.setShowCalloutBalloonOnTouch(false);
                            my_location_marker.setItemName("마지막 위치");
                            my_location_marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(getMyX), Double.parseDouble(getMyY)));
                            my_location_marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                            my_location_marker.setCustomImageResourceId(R.drawable.my_location);
                            my_location_marker.setCustomImageAutoscale(false);
                            my_location_marker.setCustomImageAnchor(0.5f, 0.5f);
                            mapView.addPOIItem(my_location_marker);
                        }
                        bike_points.clear();
                        unlock.setVisibility(View.VISIBLE);
                        unlock.startAnimation(fade_in_slide_in_from_top);
                        if ("0".equals(jsonObject.getString("login_type"))) {
                            sub_title_con.setVisibility(View.VISIBLE);
                            sub_title1.setText("간편로그인");
                            sub_title2.setText("을 통해 서비스 이용이 가능합니다.");
                            unlock.setSelected(false);
                            SharedPreferences prefLoginChecked = MainActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefLoginChecked.edit();
                            editor.clear();
                            editor.commit();
                            expire = "0";
                            expire_minute = "0";
                            riding_type = "0";

                            JSONArray jsonArray = new JSONArray(jsonObject.getString("bike"));
                            if (jsonArray.length() != 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject getJsonObject = jsonArray.getJSONObject(i);
                                    bike_points.add(MapPoint.mapPointWithGeoCoord(Double.parseDouble(getJsonObject.getString("lat")), Double.parseDouble(getJsonObject.getString("lng"))));
                                    bike_marker_wait.setMapPoint(bike_points.get(i));
                                    mapView.addPOIItem(bike_marker_wait);
                                }
                            }
                        } else if ("1".equals(jsonObject.getString("login_type"))) {
                            if ("null".equals(jsonObject.getString("expire"))) {
                                sub_title_con.setVisibility(View.VISIBLE);
                                sub_title1.setText("정액제 상품을");
                                sub_title2.setText("결제하시면 서비스 이용이 가능합니다.");
                                expire = "0";
                                expire_minute = "0";
                            } else {
                                Date curDate = new Date();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                long day = 0, minute = 0;
                                try {
                                    Date reqDate = dateFormat.parse(jsonObject.getString("expire"));
                                    long reqDateTime = reqDate.getTime();
                                    curDate = dateFormat.parse(dateFormat.format(curDate));
                                    long curDateTime = curDate.getTime();
                                    day = (reqDateTime - curDateTime) / (24 * 60 * 60 * 1000);
                                    minute = (reqDateTime - curDateTime) / 60000;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (minute <= 0 ) {
                                    sub_title_con.setVisibility(View.VISIBLE);
                                    sub_title1.setText("정액제 상품을");
                                    sub_title2.setText("결제하시면 서비스 이용이 가능합니다.");
                                    expire = "0";
                                    expire_minute = "0";
                                } else {
                                    sub_title_con.setVisibility(View.GONE);
                                    sub_title1.setText("");
                                    sub_title2.setText("");
                                    expire = day + "";
                                    expire_minute = minute + "";
                                }
                            }
                            if ("0".equals(jsonObject.getString("riding_type"))) {
                                JSONArray jsonArray = new JSONArray(jsonObject.getString("bike"));
                                if (jsonArray.length() != 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject getJsonObject = jsonArray.getJSONObject(i);
                                        bike_points.add(MapPoint.mapPointWithGeoCoord(Double.parseDouble(getJsonObject.getString("lat")), Double.parseDouble(getJsonObject.getString("lng"))));
                                        bike_marker_wait.setMapPoint(bike_points.get(i));
                                        mapView.addPOIItem(bike_marker_wait);
                                    }
                                }
                                unlock.setSelected(false);
                                riding_type = "0";
                            } else if ("1".equals(jsonObject.getString("riding_type"))) {
                                bleCallback.device_id = jsonObject.getString("bike_id");
                                bike_points.add(MapPoint.mapPointWithGeoCoord(Double.parseDouble(jsonObject.getJSONObject("bike").getString("lat")), Double.parseDouble(jsonObject.getJSONObject("bike").getString("lng"))));
                                bike_marker_use.setMapPoint(bike_points.get(0));
                                mapView.addPOIItem(bike_marker_use);
                                unlock.setSelected(true);
                                riding_type = "1";
                            }
                        }
                        zone_points.clear();
                        zone_points_radius.clear();
                        JSONArray jsonArray2 = new JSONArray(jsonObject.getString("zone"));
                        if (jsonArray2.length() != 0) {
                            for (int i = 0; i < jsonArray2.length(); i++) {
                                JSONObject getJsonObject = jsonArray2.getJSONObject(i);
                                zone_points.add(MapPoint.mapPointWithGeoCoord(Double.parseDouble(getJsonObject.getString("lat")), Double.parseDouble(getJsonObject.getString("lng"))));
                                zone_points_radius.add(getJsonObject.getString("radius"));
                                addCircles(zone_points.get(i), Integer.parseInt(zone_points_radius.get(i)));

                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.header("User-Agent", "gh_mobile{" + token + "}"));
    }

    void l_st_op() {
        handler.removeCallbacks(autoScanClose);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                is_locking = false;
                if (oneBtnDialog == null) {
                    oneBtnDialog = new OneBtnDialog(MainActivity.this, "자전거 자물쇠를\n수동으로 잠금 후\n'잠금' 버튼을 눌러주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                } else {
                    if (!oneBtnDialog.isShowing()) {
                        oneBtnDialog = new OneBtnDialog(MainActivity.this, "자전거 자물쇠를\n수동으로 잠금 후\n'잠금' 버튼을 눌러주세요.", "1", "확인");
                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        oneBtnDialog.setCancelable(false);
                        oneBtnDialog.show();
                    }
                }
                spinKitView.setVisibility(View.GONE);
            }
        });
    }

    void l_st_cl() {
        Log.d("aaa","asdfg");
//        handler.removeCallbacks(autoScanClose);
        SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
        final String token = prefToken.getString("Token", "");
        String url = UrlManager.getBaseUrl() + "/member/riding/end";
        Map<String, Object> params = new HashMap<String, Object>();
        aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                try {
                    if ("true".equals(jsonObject.getString("return"))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh_marker();
                                is_locking = false;
                                if (oneBtnDialog == null) {
                                    oneBtnDialog = new OneBtnDialog(MainActivity.this, "잠금 되었습니다.\n이용해 주셔서 감사합니다.", "1", "확인");
                                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    oneBtnDialog.setCancelable(false);
                                    oneBtnDialog.show();
                                } else {
                                    if (!oneBtnDialog.isShowing()) {
                                        oneBtnDialog = new OneBtnDialog(MainActivity.this, "잠금 되었습니다.\n이용해 주셔서 감사합니다.", "1", "확인");
                                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        oneBtnDialog.setCancelable(false);
                                        oneBtnDialog.show();
                                    }
                                }
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

    void l_st_err() {
        handler.removeCallbacks(autoScanClose);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                is_locking = false;
                if (oneBtnDialog == null) {
                    oneBtnDialog = new OneBtnDialog(MainActivity.this, "자전거 디바이스가 정상적으로\n작동하지 않습니다.\n고장 및 불편\n신고를 해주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                } else {
                    if (!oneBtnDialog.isShowing()) {
                        oneBtnDialog = new OneBtnDialog(MainActivity.this, "자전거 디바이스가 정상적으로\n작동하지 않습니다.\n고장 및 불편\n신고를 해주세요.", "1", "확인");
                        oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        oneBtnDialog.setCancelable(false);
                        oneBtnDialog.show();
                    }
                }
                spinKitView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleCallback.bluetoothGatt != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bleCallback.bluetoothGatt.disconnect();
                bleCallback.bluetoothGatt.close();
                bleCallback.bluetoothGatt = null;
            }
        }
        handler.removeCallbacks(autoScanClose);
    }

    @Override
    protected void onResume() {
        super.onResume();
        is_locking = false;
        refresh_marker();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#212121"));
        }
        aQuery = new AQuery(this);
        backPressCloseHandler = new BackPressCloseHandler(this);
        sub_title_con = (FrameLayout) findViewById(R.id.sub_title_con);
        sub_title1 = (TextView) findViewById(R.id.sub_title1);
        sub_title2 = (TextView) findViewById(R.id.sub_title2);
        my_page = (ImageView) findViewById(R.id.my_page);
        search_main = (ImageView) findViewById(R.id.search_main);
        report = (ImageView) findViewById(R.id.report);
        faq = (ImageView) findViewById(R.id.faq);
        refresh = (ImageView) findViewById(R.id.refresh);
        gps = (ImageView) findViewById(R.id.gps);
        unlock = (ImageView) findViewById(R.id.unlock);
        spinKitView = (SpinKitView) findViewById(R.id.spinKitView);
        fade_in_slide_in_from_top = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_slide_in_from_top);
        fade_in_slide_out_to_top = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_slide_out_to_top);
        fade_in_slide_out_to_top.setAnimationListener(new Animation.AnimationListener(){
            public void onAnimationEnd(Animation animation){
                unlock.setVisibility(View.INVISIBLE);
            }
            public void onAnimationStart(Animation animation){;}
            public void onAnimationRepeat(Animation animation){;}
        });
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        BluetoothLeScanner bluetoothLeScanner = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        } else {
            oneBtnDialog = new OneBtnDialog(MainActivity.this, "안드로이드 버전이 낮아서\n이용할 수 없습니다.", "1", "확인");
            oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            oneBtnDialog.setCancelable(false);
            oneBtnDialog.show();
        }
        bleCallback = new BLECallback(MainActivity.this, bluetoothLeScanner);
        bleCallback.setMainActivity(MainActivity.this);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.zoomIn(true);
        mapView.zoomOut(true);
        mapView.setMapViewEventListener(this);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        SharedPreferences saveLocation = getSharedPreferences("saveLocation", Activity.MODE_PRIVATE);
        getMyX = saveLocation.getString("myla", "");
        getMyY = saveLocation.getString("mylo", "");
        getLa = saveLocation.getString("la", "");
        getLo = saveLocation.getString("lo", "");
        String getLevel = saveLocation.getString("level", "");
        if (!"".equals(getLa)) {
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(Double.parseDouble(getLa), Double.parseDouble(getLo)), Integer.parseInt(getLevel), true);
        } else {
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.572288, 126.977588), 3, true);
        }
        sub_title_con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sub_title2.callOnClick();
            }
        });
        sub_title1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sub_title2.callOnClick();
            }
        });
        sub_title2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefLoginChecked = MainActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                if (prefLoginChecked.getBoolean("loginChecked", false)) {
                    Intent intent = new Intent(MainActivity.this, BillingListActivity.class);
                    startActivityForResult(intent, 1);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginSelectActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });
        my_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefLoginChecked = MainActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                if (prefLoginChecked.getBoolean("loginChecked", false)) {
                    if (expire != null) {
                        Intent intent = new Intent(MainActivity.this, MyPageActivity.class);
                        intent.putExtra("expire", expire);
                        intent.putExtra("expire_minute", expire_minute);
                        startActivityForResult(intent, 1);
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginSelectActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });
        search_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainSearchActivity.class);
                intent.putExtra("intent", "1");
                startActivityForResult(intent, 1);
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
//                startActivityForResult(intent, 1);
                Intent intent = new Intent(MainActivity.this, Unlock2Activity.class);
                startActivityForResult(intent, 1);

            }
        });
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FaqActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh_marker();
            }
        });
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    gps.setSelected(false);
                    Toast.makeText(MainActivity.this, "GPS가 꺼져있습니다.'위치 서비스’에서 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                } else {
                    gps.setSelected(true);
                    mGoogleApiClient.connect();
                    shake = YoYo.with(Techniques.Pulse).duration(600).repeat(YoYo.INFINITE).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.gps));
                }
            }
        });
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefLoginChecked = MainActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                if (prefLoginChecked.getBoolean("loginChecked", false)) {
                    if (riding_type != null && "0".equals(riding_type)) {
                        if (riding_type != null && "0".equals(expire_minute)) {
                            Toast.makeText(MainActivity.this, "정액제 상품을 결제하시면\n서비스 이용이 가능합니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, BillingListActivity.class);
                            startActivityForResult(intent, 1);
                        } else if (riding_type != null && !"0".equals(expire_minute)) {
//                            Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                            Intent intent = new Intent(MainActivity.this, Unlock2Activity.class);
                            startActivityForResult(intent, 1);
                        }
                    } else if (riding_type != null && "1".equals(riding_type)) {
                        if (bleCallback.device_id == null) {
                            return;
                        }
                        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                            oneBtnDialog = new OneBtnDialog(MainActivity.this, "블루투스가 꺼져 있습니다.\n블루투스를 활성화 해주세요.", "1", "확인");
                            oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            oneBtnDialog.setCancelable(false);
                            oneBtnDialog.show();
                            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableIntent, 1);
                            return;
                        }
                        if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            gps.setSelected(false);
                            Toast.makeText(MainActivity.this, "GPS가 꺼져있습니다.'위치 서비스’에서 활성화 해주세요.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        } else {
                            if (!is_locking) {
                                is_locking = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        spinKitView.setVisibility(View.VISIBLE);
                                        mGoogleApiClient.connect();
                                    }
                                });
                            }
                        }
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginSelectActivity.class);
                    startActivityForResult(intent, 1);
                }
            }
        });
        zone_points = new ArrayList<>();
        zone_points_radius = new ArrayList<>();
        bike_points = new ArrayList<>();
        bike_marker_wait = new MapPOIItem();
        bike_marker_wait.setShowCalloutBalloonOnTouch(false);
        bike_marker_wait.setItemName("");
        bike_marker_wait.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        bike_marker_wait.setCustomImageResourceId(R.drawable.bike_wait);
        bike_marker_wait.setCustomImageAutoscale(false);
        bike_marker_wait.setCustomImageAnchor(0.5f, 1.0f);

        bike_marker_use = new MapPOIItem();
        bike_marker_use.setShowCalloutBalloonOnTouch(false);
        bike_marker_use.setItemName("");
        bike_marker_use.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        bike_marker_use.setCustomImageResourceId(R.drawable.bike_use);
        bike_marker_use.setCustomImageAutoscale(false);
        bike_marker_use.setCustomImageAnchor(0.5f, 1.0f);

        Location crntLocation = new Location("crntlocation");
        crntLocation.setLatitude(37.572288);
        crntLocation.setLongitude(126.977588);

        Location newLocation = new Location("newlocation");
        newLocation.setLatitude(37.578888);
        newLocation.setLongitude(126.971588);

        int distance = (int) crntLocation.distanceTo(newLocation);
        LogManager.print("두 좌표 사이의 거리 : " + distance + " 미터");
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
        if (is_create) {
            if (!is_running_animation) {
                is_running_animation = true;
                YoYo.with(Techniques.TakingOff).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.report));
                YoYo.with(Techniques.TakingOff).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.faq));
                YoYo.with(Techniques.TakingOff).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.refresh));
                YoYo.with(Techniques.TakingOff).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.gps));
            }
        }
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        if (is_create) {
            is_running_animation = true;
            YoYo.with(Techniques.TakingOff).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.report));
            YoYo.with(Techniques.TakingOff).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.faq));
            YoYo.with(Techniques.TakingOff).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.refresh));
            YoYo.with(Techniques.TakingOff).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.gps));
        }
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        if (is_create) {
            is_running_animation = false;
            YoYo.with(Techniques.Landing).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.report));
            YoYo.with(Techniques.Landing).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.faq));
            YoYo.with(Techniques.Landing).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.refresh));
            YoYo.with(Techniques.Landing).duration(300).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.gps));
            SharedPreferences saveLocation = getSharedPreferences("saveLocation", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = saveLocation.edit();

            editor.putString("la", mapView.getMapCenterPoint().getMapPointGeoCoord().latitude+"");
            editor.putString("lo", mapView.getMapCenterPoint().getMapPointGeoCoord().longitude+"");
            editor.putString("level", mapView.getZoomLevel()+"");
            editor.commit();
        }
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        if (is_create) {
            if (is_running_animation) {
                is_running_animation = false;
                YoYo.with(Techniques.Landing).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.report));
                YoYo.with(Techniques.Landing).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.faq));
                YoYo.with(Techniques.Landing).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.refresh));
                YoYo.with(Techniques.Landing).duration(600).repeat(0).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(findViewById(R.id.gps));
                SharedPreferences saveLocation = getSharedPreferences("saveLocation", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = saveLocation.edit();
                editor.putString("la", mapView.getMapCenterPoint().getMapPointGeoCoord().latitude+"");
                editor.putString("lo", mapView.getMapCenterPoint().getMapPointGeoCoord().longitude+"");
                editor.putString("level", mapView.getZoomLevel()+"");
                editor.commit();
            }
        }
        is_create = true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
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
            case 666:
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(Double.parseDouble(data.getStringExtra("la")), Double.parseDouble(data.getStringExtra("lo"))), 3, true);
                break;
            case 9999:
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
                break;
        }
    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;
        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
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
