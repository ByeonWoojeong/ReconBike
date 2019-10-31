package app.cosmos.reconbike;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bumptech.glide.Glide;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static app.cosmos.reconbike.GlobalApplication.setMeizuDarkMode;
import static app.cosmos.reconbike.GlobalApplication.setXiaomiDarkMode;

public class ReportWriteActivity extends AppCompatActivity {

    AQuery aQuery = null;
    ImageView back, camera;
    FrameLayout type_con;
    TextView type_text, next;
    EditText content;
    String serial_number;
    LinearLayout report_write;
    boolean userIsInteracting;
    SpinnerReselect type_spinner;
    InputMethodManager ipm;
    SoftKeyBoard softKeyBoard;
    boolean isKeyBoardVisible;
    ScrollView scrollView;
    FrameLayout imageview_con[] = new FrameLayout[5];
    ImageView imageView[] = new ImageView[5];
    ImageView closeImageView[] = new ImageView[5];
    ArrayList<Bitmap> originalBitmap =  new ArrayList<Bitmap>();
    ArrayList<Bitmap> resizeBitmap =  new ArrayList<Bitmap>();
    ArrayList<String> imagePath =  new ArrayList<String>();
    ArrayList<File> file =  new ArrayList<File>();
    ArrayList<String> getImageList =  new ArrayList<String>();
    ContentResolver contentResolver;
    int CAMERA = 700, GALLERY = 800, totalImageCount = 0, addImageCount = 0, getImageCount = 0, scroll_x = 0, scroll_y = 0;
    OneBtnDialog oneBtnDialog;
    TwoBtnDialog twoBtnDialog;
    CustomProgressDialog customProgressDialog;

    Uri getFileUri() {
        File dir = new File(getFilesDir(), "Picture");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, System.currentTimeMillis() + ".png");
        imagePath.add(file.getAbsolutePath()+"");
        return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
    }

    String getImageRealPathFromURI(ContentResolver contentResolver, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            int path = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String tmp = cursor.getString(path);
            cursor.close();
            return tmp;
        }
    }

    int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {

            }
        }
        return bitmap;
    }

    Bitmap resizeBitmap(String file, int width, int height) {
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }

    Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onPause() {
        super.onPause();
        scroll_x = scrollView.getScrollX();
        scroll_y = scrollView.getScrollY();
    }

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
        setContentView(R.layout.activity_report_write);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#ffffff"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                setXiaomiDarkMode(ReportWriteActivity.this);
                setMeizuDarkMode(ReportWriteActivity.this);
            }
        }
        Intent intent = getIntent();
        serial_number = intent.getStringExtra("serial_number");
        aQuery = new AQuery(this);
        customProgressDialog = new CustomProgressDialog(ReportWriteActivity.this, "접수 중 입니다.");
        customProgressDialog .getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);
        report_write = (LinearLayout) findViewById(R.id.report_write);
        ipm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        softKeyBoard = new SoftKeyBoard(report_write, ipm);
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
        contentResolver = getContentResolver();
        back = (ImageView) findViewById(R.id.back);
        type_con = (FrameLayout) findViewById(R.id.type_con);
        type_text = (TextView) findViewById(R.id.type_text);
        content = (EditText) findViewById(R.id.content);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        type_spinner = (SpinnerReselect) findViewById(R.id.type_spinner);
        next = (TextView) findViewById(R.id.next);
        String[] select_type = new String[]{
                "1",
                "2",
                "3",
                "4",
                "5"
        };
        final ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,R.layout.my_spinner, select_type);
        typeAdapter.setDropDownViewResource(R.layout.my_spinner);
        type_spinner.setAdapter(typeAdapter);
        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (userIsInteracting) {
                    type_text.setText(type_spinner.getSelectedItem().toString());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        camera = (ImageView) findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipm.hideSoftInputFromWindow(camera.getWindowToken(), 0);
                if (totalImageCount >= 5) {
                    oneBtnDialog = new OneBtnDialog(ReportWriteActivity.this, "이미지는 최대 5장까지\n첨부 가능합니다.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                twoBtnDialog = new TwoBtnDialog(ReportWriteActivity.this);
                twoBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                twoBtnDialog.show();
            }
        });
        for (int i = 0; i < imageView.length; i++) {
            int res1 = getResources().getIdentifier("image_con"+i, "id", getPackageName());
            int res2 = getResources().getIdentifier("image"+i, "id", getPackageName());
            int res3 = getResources().getIdentifier("image_close"+i, "id", getPackageName());
            imageview_con[i] = (FrameLayout) findViewById(res1);
            imageView[i] = (ImageView) findViewById(res2);
            closeImageView[i] = (ImageView) findViewById(res3);
            final int finalI = i;
            imageView[finalI].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ReportWriteActivity.this, InputImagePathActivity.class);
                    intent.putExtra("path", imagePath.get(finalI));
                    startActivityForResult(intent, 900);
                }
            });
            closeImageView[finalI].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    totalImageCount --;
                    imageview_con[totalImageCount].setVisibility(View.GONE);
                    imageView[totalImageCount].setImageBitmap(null);
                    imagePath.remove(finalI);
                    if (getImageList.size() > 0 && finalI < getImageList.size()) {
                        getImageCount --;
                        getImageList.remove(finalI);
                    }
                    if (addImageCount > 0) {
                        if (finalI > getImageList.size()) {
                            addImageCount --;
                            originalBitmap.remove(finalI - getImageCount);
                            resizeBitmap.remove(finalI - getImageCount);
                        }
                    }
                    for (int i = finalI; i < totalImageCount; i++){
                        imageView[i].setImageBitmap(null);
                        Glide.with(ReportWriteActivity.this).load(imagePath.get(i)).crossFade().centerCrop().into(imageView[i]);
                    }
                }
            });
        }
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isKeyBoardVisible) {
                    ipm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
                if ("유형을 선택해 주세요.".equals(type_text.getText().toString()) || "유형을 선택해 주세요." == type_text.getText().toString()) {
                    oneBtnDialog = new OneBtnDialog(ReportWriteActivity.this, "유형을 선택해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                if ("".equals(content.getText().toString().trim()) || "" == content.getText().toString().trim()) {
                    oneBtnDialog = new OneBtnDialog(ReportWriteActivity.this, "내용을 입력해 주세요.", "1", "확인");
                    oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    oneBtnDialog.setCancelable(false);
                    oneBtnDialog.show();
                    return;
                }
                customProgressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences get_token = getSharedPreferences("prefToken", Activity.MODE_PRIVATE);
                                final String getToken = get_token.getString("Token", "");
                                Map<String, Object> params = new HashMap<String, Object>();
                                String url = UrlManager.getBaseUrl() + "/setting/report";
                                params.put("target", serial_number);
                                params.put("type", type_text.getText().toString().trim());
                                params.put("content", content.getText().toString().trim());
                                file.clear();
                                for (int i = 0; i < addImageCount; i++){
                                    Uri fileUri = getImageUri(ReportWriteActivity.this, resizeBitmap.get(i));
                                    String filePath = getImageRealPathFromURI(ReportWriteActivity.this.getContentResolver(), fileUri);
                                    File makeFile = new File(filePath);
                                    file.add(makeFile);
                                    params.put("image["+i+"]", file.get(i));
                                }
                                aQuery.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                                    @Override
                                    public void callback(String url, JSONObject jsonObject, AjaxStatus status) {
                                        customProgressDialog.dismiss();
                                        for (int i = 0; i < addImageCount; i++){
                                            contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.ImageColumns.DATA + "=?" , new String[]{ file.get(i).toString() });
                                            file.get(i).delete();
                                        }
                                        try {
                                            if ("true".equals(jsonObject.getString("return"))) {
                                                oneBtnDialog = new OneBtnDialog(ReportWriteActivity.this, "접수를 완료 하였습니다.", "2", "확인");
                                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                oneBtnDialog.setCancelable(false);
                                                oneBtnDialog.show();
                                            } else if ("false".equals(jsonObject.getString("return"))) {
                                                oneBtnDialog = new OneBtnDialog(ReportWriteActivity.this, "접수를 실패 하였습니다.\n재시도 해주세요.", "2", "확인");
                                                oneBtnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                oneBtnDialog.setCancelable(false);
                                                oneBtnDialog.show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }.header("User-Agent", "gh_mobile{" + getToken + "}"));
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 700:
                if (resultCode == RESULT_CANCELED){
                    return;
                } else {
                    try {
                        if (Build.VERSION.SDK_INT < 21){
                            Uri imgUri = data.getData();
                            imagePath.add(getImageRealPathFromURI(ReportWriteActivity.this.getContentResolver(), imgUri));
                        }
                        ExifInterface exif = new ExifInterface(imagePath.get(totalImageCount));
                        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int exifDegree = exifOrientationToDegrees(exifOrientation);
                        originalBitmap.add(rotate(BitmapFactory.decodeFile(imagePath.get(totalImageCount)), exifDegree));
                        resizeBitmap.add(rotate(resizeBitmap(imagePath.get(totalImageCount), 1080, 1920), exifDegree));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (totalImageCount < 5) {
                        imageview_con[totalImageCount].setVisibility(View.VISIBLE);
                    }
                    Glide.with(ReportWriteActivity.this).load(imagePath.get(totalImageCount)).crossFade().centerCrop().into(imageView[totalImageCount]);
                    totalImageCount ++;
                    addImageCount ++;
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
                break;
            case 800:
                if (resultCode == RESULT_CANCELED){
                    return;
                } else {
                    try {
                        Uri imgUri = data.getData();
                        imagePath.add(getImageRealPathFromURI(ReportWriteActivity.this.getContentResolver(), imgUri));
                        ExifInterface exif = new ExifInterface(imagePath.get(totalImageCount));
                        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int exifDegree = exifOrientationToDegrees(exifOrientation);
                        originalBitmap.add(rotate(BitmapFactory.decodeFile(imagePath.get(totalImageCount)), exifDegree));
                        resizeBitmap.add(rotate(resizeBitmap(imagePath.get(totalImageCount), 1080, 1920), exifDegree));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (totalImageCount < 5) {
                        imageview_con[totalImageCount].setVisibility(View.VISIBLE);
                    }
                    Glide.with(ReportWriteActivity.this).load(imagePath.get(totalImageCount)).crossFade().centerCrop().into(imageView[totalImageCount]);
                    totalImageCount ++;
                    addImageCount ++;
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
                break;
            case 900:
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.setScrollX(scroll_x);
                        scrollView.setScrollY(scroll_y);
                    }
                });
                break;
            case RESULT_CANCELED:
                break;
        }
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
                        setResult(999);
                        finish();
                    }
                });
            }
        }
    }

    public class TwoBtnDialog extends Dialog {
        TwoBtnDialog twoBtnDialog = this;
        Context context;
        public TwoBtnDialog(final Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_two_btn_dialog);
            getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.context = context;
            TextView title1 = (TextView) findViewById(R.id.title1);
            TextView title2 = (TextView) findViewById(R.id.title2);
            TextView btn1 = (TextView) findViewById(R.id.btn1);
            TextView btn2 = (TextView) findViewById(R.id.btn2);
            title2.setVisibility(View.GONE);
            title1.setText("이미지 첨부방식을\n선택해 주세요 !");
            btn1.setText("카메라");
            btn2.setText("갤러리");
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    twoBtnDialog.dismiss();
                    if (Build.VERSION.SDK_INT > 21){
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri());
                        startActivityForResult(intent, CAMERA);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA);
                    }
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    twoBtnDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GALLERY);
                }
            });
        }
    }

    public class CustomProgressDialog extends Dialog {
        TextView title;
        public CustomProgressDialog(Context context, String text) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.custom_progress);
            title = (TextView) findViewById(R.id.title);
            title.setText(text);
        }
    }
}
