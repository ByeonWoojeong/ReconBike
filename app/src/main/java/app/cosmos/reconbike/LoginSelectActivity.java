package app.cosmos.reconbike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class LoginSelectActivity extends AppCompatActivity {

    AQuery aQuery = null;
    OAuthLogin mOAuthLoginInstance;
    SessionCallback mKakaocallback;
    GoogleApiClient mGoogleApiClient;
    ImageView back, naver_login, kakao_login, google_login, nomal_login, join;
    TextView id_get, password_get;

    void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                LogManager.print("Hash Key : " + something);
            }
        } catch (Exception e) {
            LogManager.print("name not found" + e.toString());
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
        setContentView(R.layout.activity_login_select);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(LoginSelectActivity.this);
                setMeizuDarkMode(LoginSelectActivity.this);
            }
        }
        aQuery = new AQuery(this);
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(this, getResources().getString(R.string.naver_client_id), getResources().getString(R.string.naver_client_secret), "네이버 로그인");
        getAppKeyHash();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestProfile( ).requestIdToken(getString(R.string.google_client_id)).build( );
        mGoogleApiClient = new GoogleApiClient.Builder(LoginSelectActivity.this).enableAutoManage(LoginSelectActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed( @NonNull ConnectionResult connectionResult ) {
            }
        }).addApi( Auth.GOOGLE_SIGN_IN_API, gso ).build();
        back = (ImageView) findViewById(R.id.back);
        naver_login = (ImageView) findViewById(R.id.naver_login);
        kakao_login = (ImageView) findViewById(R.id.kakao_login);
        google_login = (ImageView) findViewById(R.id.google_login);
        nomal_login = (ImageView) findViewById(R.id.nomal_login);
        id_get = (TextView) findViewById(R.id.id_get);
        password_get = (TextView) findViewById(R.id.password_get);
        join = (ImageView) findViewById(R.id.join);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        naver_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginInstance.startOauthLoginActivity(LoginSelectActivity.this, mOAuthLoginHandler);
            }
        });
        kakao_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserManagement.requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mKakaocallback = new SessionCallback();
                                        com.kakao.auth.Session.getCurrentSession().removeCallback(mKakaocallback);
                                        com.kakao.auth.Session.getCurrentSession().addCallback(mKakaocallback);
                                        LoginButton loginButton = (LoginButton) findViewById(R.id.kakaotalk_login_original_btn);
                                        loginButton.callOnClick();
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }
        });
        google_login.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick( View view ) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleApiClient );
                startActivityForResult(signInIntent, 1);
            }
        } );
        nomal_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginSelectActivity.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        id_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginSelectActivity.this, IdGetActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        password_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginSelectActivity.this, PassGetActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginSelectActivity.this, JoinActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    // 네이버
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                LogManager.print("isLogin : " + mOAuthLoginInstance.getState(LoginSelectActivity.this).toString());
                new RequestApiTask().execute();
            } else {

            }
        }
    };

    private class RequestApiTask extends AsyncTask<Void, Void, Void> {
        String nick_name;
        String enc_id;
        String profile_image;
        String age;
        String gender;
        String userId;
        String name;
        String email;
        String birthday;

        public boolean checkEmail(String email){
            Pattern pattern = Pattern.compile("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$");
            Matcher matcher = pattern.matcher(email);
            return !matcher.matches();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml";
            String at = mOAuthLoginInstance.getAccessToken(LoginSelectActivity.this);
            Pasingversiondata(mOAuthLoginInstance.requestApi(LoginSelectActivity.this, at, url));
            return null;
        }

        protected void onPostExecute(Void content) {

        }

        private void Pasingversiondata(String data) {
            String f_array[] = new String[9];
            try {
                XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserCreator.newPullParser();
                InputStream input = new ByteArrayInputStream(data.getBytes("UTF-8"));
                parser.setInput(input, "UTF-8");
                int parserEvent = parser.getEventType();
                String tag;
                boolean inText = false;
                boolean lastMatTag = false;
                int colIdx = 0;

                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            tag = parser.getName();
                            if (tag.compareTo("xml") == 0) {
                                inText = false;
                            } else if (tag.compareTo("data") == 0) {
                                inText = false;
                            } else if (tag.compareTo("result") == 0) {
                                inText = false;
                            } else if (tag.compareTo("resultcode") == 0) {
                                inText = false;
                            } else if (tag.compareTo("message") == 0) {
                                inText = false;
                            } else if (tag.compareTo("response") == 0) {
                                inText = false;
                            } else {
                                inText = true;
                            }
                            break;

                        case XmlPullParser.TEXT:
                            tag = parser.getName();
                            if (inText) {
                                if (parser.getText() == null) {
                                    f_array[colIdx] = "";
                                } else {
                                    f_array[colIdx] = parser.getText().trim();
                                }
                                colIdx++;
                            }
                            inText = false;
                            break;

                        case XmlPullParser.END_TAG:
                            tag = parser.getName();
                            inText = false;
                            break;
                    }
                    parserEvent = parser.next();
                }

            } catch (Exception e) {
            }

            nick_name = f_array[0];
            enc_id = f_array[1];
            profile_image = f_array[2];
            age = f_array[3];
            gender = f_array[4];
            userId = f_array[5];
            if (f_array[7] == null){
                birthday = f_array[6];
            }
            if (f_array[7] != null && f_array[8] == null){
                if (checkEmail(f_array[6].toString())){
                    name = f_array[6];
                } else {
                    email = f_array[6];
                }
                birthday = f_array[7];
            }
            if (f_array[8] != null){
                name = f_array[6];
                email = f_array[7];
                birthday = f_array[8];
            }

            LogManager.print("AccessToken: " + mOAuthLoginInstance.getAccessToken(LoginSelectActivity.this));
            LogManager.print("id: " + userId);
            LogManager.print("email: " + email);
            LogManager.print("name: " + name);
            LogManager.print("url: " + profile_image);
            LogManager.print("age: " + age);
            LogManager.print("gender: " + gender);

            SharedPreferences naverData = LoginSelectActivity.this.getSharedPreferences("naverData", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = naverData.edit();
            editor.clear();
            editor.putString("id", userId+"");
            if (email != null){
                editor.putString("email", email+"");
            }
            if (name != null){
                editor.putString("name", name+"");
            }
            editor.putString("url", profile_image+"");
            editor.putString("age", age+"");
            editor.putString("gender", gender+"");
            editor.putString("token", mOAuthLoginInstance.getAccessToken(LoginSelectActivity.this)+"");
            editor.commit();

            SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
            String token = prefToken.getString("Token", "");
            String url = UrlManager.getBaseUrl() + "/login/naver";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("token", mOAuthLoginInstance.getAccessToken(LoginSelectActivity.this)+"");
            aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                    try {
                        if ("true".equals(jsonObject.getString("return"))) {
                            if ("ok".equals(jsonObject.getString("type"))) {
                                SharedPreferences prefLoginChecked = LoginSelectActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefLoginChecked.edit();
                                editor.clear();
                                editor.putBoolean("loginChecked", true);
                                editor.putString("type", "naver");
                                editor.putString("phone", jsonObject.getString("phone"));
                                editor.commit();

                                Intent intent = new Intent(LoginSelectActivity.this, MainActivity.class);
                                setResult(999);
                                startActivityForResult(intent, 1);
                                Toast.makeText(LoginSelectActivity.this, "로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                            } else if ("detail".equals(jsonObject.getString("type"))) {
                                Intent intent = new Intent(LoginSelectActivity.this, JoinAuthActivity.class);
                                startActivityForResult(intent, 1);
                            }
                        } else if ("false".equals(jsonObject.getString("return"))) {
                            Toast.makeText(getApplicationContext(), "네트워크 에러. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.header("User-Agent", "gh_mobile{" + token + "}"));
        }
    }

    // 카카오톡
    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            KakaorequestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Session.getCurrentSession().removeCallback(mKakaocallback);
            mKakaocallback = new SessionCallback();
            Session.getCurrentSession().addCallback(mKakaocallback);
        }
    }

    protected void KakaorequestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                int ErrorCode = errorResult.getErrorCode();
                int ClientErrorCode = -777;

                if (ErrorCode == ClientErrorCode) {
                    Toast.makeText(getApplicationContext(), "카카오톡 서버의 네트워크가 불안정합니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    LogManager.print("오류로 카카오 로그인 실패");
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                LogManager.print("오류로 카카오 로그인 실패");
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                final String userName = userProfile.getNickname();
                final String userId = String.valueOf(userProfile.getId());
                final String profileUrl = userProfile.getProfileImagePath();
                LogManager.print("profileUrl : " + profileUrl + " userId : " + userId + " userName : " + userName);
                LogManager.print("AccessToken: " + Session.getCurrentSession().getAccessToken());

                SharedPreferences kakaotalkData = LoginSelectActivity.this.getSharedPreferences("kakaotalkData", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = kakaotalkData.edit();
                editor.clear();
                editor.putString("kakao", userId + "");
                editor.putString("name", userName + "");
                editor.putString("url", profileUrl + "");
                editor.commit();

                SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                String token = prefToken.getString("Token", "");
                String url = UrlManager.getBaseUrl() + "/login/kakao";
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("token", Session.getCurrentSession().getAccessToken()+"");
                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                        try {
                            if ("true".equals(jsonObject.getString("return"))) {
                                if ("ok".equals(jsonObject.getString("type"))) {
                                    SharedPreferences prefLoginChecked = LoginSelectActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefLoginChecked.edit();
                                    editor.clear();
                                    editor.putBoolean("loginChecked", true);
                                    editor.putString("type", "kakao");
                                    editor.putString("phone", jsonObject.getString("phone"));
                                    editor.commit();

                                    Intent intent = new Intent(LoginSelectActivity.this, MainActivity.class);
                                    setResult(999);
                                    startActivityForResult(intent, 1);
                                    Toast.makeText(LoginSelectActivity.this, "로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                                } else if ("detail".equals(jsonObject.getString("type"))) {
                                    Intent intent = new Intent(LoginSelectActivity.this, JoinAuthActivity.class);
                                    startActivityForResult(intent, 1);
                                }
                            } else if ("false".equals(jsonObject.getString("return"))) {
                                Toast.makeText(getApplicationContext(), "네트워크 에러. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.header("User-Agent", "gh_mobile{" + token + "}"));
            }

            @Override
            public void onNotSignedUp() {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(mKakaocallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            Session.getCurrentSession().removeCallback(mKakaocallback);
            mKakaocallback = new SessionCallback();
            Session.getCurrentSession().addCallback(mKakaocallback);
            return;
        }
        switch (resultCode) {
            case RESULT_CANCELED:
                break;
            case 999:
                setResult(999);
                finish();
                break;
            default:
                switch (requestCode) {
                    case 1:
                        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent( data );
                        if (result.isSuccess()) {
                            LogManager.print("isLogin : OK");
                            GoogleSignInAccount acct = result.getSignInAccount();
                            LogManager.print("AccessToken: " + acct.getIdToken());
                            LogManager.print("id : "+acct.getId());
                            LogManager.print("name : "+acct.getDisplayName());
                            LogManager.print("email : "+acct.getEmail());

                            SharedPreferences googleData = LoginSelectActivity.this.getSharedPreferences("googleData", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = googleData.edit();
                            editor.clear();
                            editor.putString("id", acct.getId()+"");
                            editor.putString("name", acct.getDisplayName()+"");
                            editor.putString("email", acct.getEmail()+"");
                            editor.putString("token", acct.getIdToken()+"");
                            editor.commit();

                            SharedPreferences prefToken = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                            String token = prefToken.getString("Token", "");
                            String url = UrlManager.getBaseUrl() + "/login/google";
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("token", acct.getIdToken()+"");
                            aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                                @Override
                                public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                                    try {
                                        if ("true".equals(jsonObject.getString("return"))) {
                                            if ("ok".equals(jsonObject.getString("type"))) {
                                                SharedPreferences prefLoginChecked = LoginSelectActivity.this.getSharedPreferences("prefLoginChecked", Activity.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = prefLoginChecked.edit();
                                                editor.clear();
                                                editor.putBoolean("loginChecked", true);
                                                editor.putString("type", "google");
                                                editor.putString("phone", jsonObject.getString("phone"));
                                                editor.commit();

                                                Intent intent = new Intent(LoginSelectActivity.this, MainActivity.class);
                                                setResult(999);
                                                startActivityForResult(intent, 1);
                                                Toast.makeText(LoginSelectActivity.this, "로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                                            } else if ("detail".equals(jsonObject.getString("type"))) {
                                                Intent intent = new Intent(LoginSelectActivity.this, JoinAuthActivity.class);
                                                startActivityForResult(intent, 1);
                                            }
                                        } else if ("false".equals(jsonObject.getString("return"))) {
                                            Toast.makeText(getApplicationContext(), "네트워크 에러. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.header("User-Agent", "gh_mobile{" + token + "}"));
                        }
                        break;
                }
        }
    }
}
