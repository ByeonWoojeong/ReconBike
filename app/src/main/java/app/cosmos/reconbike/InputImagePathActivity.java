package app.cosmos.reconbike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class InputImagePathActivity extends AppCompatActivity {

    String path;
    PhotoView image;
    PhotoViewAttacher attacher;

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_input_image_path);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        image = (PhotoView) findViewById(R.id.originalImage);
        attacher = new PhotoViewAttacher(image);
        Glide.with(this).load(path.toString()).crossFade().centerCrop().into(image);
        attacher.update();
    }
}
