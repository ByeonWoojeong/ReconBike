package app.cosmos.reconbike;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Random;

public class TutorialPagerAdapter extends PagerAdapter {

    Context context;
    int mSize;

    public TutorialPagerAdapter(Context context, int count) {
        this.context = context;
        mSize = count;
    }

    @Override public int getCount() {
        return mSize;
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override public void destroyItem(ViewGroup view, int position, Object object) {
        view.removeView((View) object);
    }

    @Override public Object instantiateItem(ViewGroup view, int position) {
        ImageView imageView = new ImageView(view.getContext());
        final int imageResource = context.getResources().getIdentifier("@drawable/tutorial_img" + position, null, "app.cosmos.reconbike");
        imageView.setBackgroundResource(imageResource);
        view.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return imageView;
    }
}