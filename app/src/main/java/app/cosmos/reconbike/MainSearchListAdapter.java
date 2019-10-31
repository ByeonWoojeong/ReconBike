package app.cosmos.reconbike;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.gson.Gson;


import java.util.ArrayList;

import app.cosmos.reconbike.DTO.PoiDTO;

public class MainSearchListAdapter extends BaseAdapter{

    Context context;
    int layout;
    ArrayList<PoiDTO> data;
    ListView listView;
    AQuery aQuery = null;
    Gson gson;
    Animation animation;
    SpannableStringBuilder spannableStringBuilder;
    Typeface bold_font;
    String searchWord = "";

    public MainSearchListAdapter(Context context, int layout, ArrayList<PoiDTO> data, ListView listView) {
        this.context = context;
        this.layout = layout;
        this.data = data;
        this.listView = listView;
        gson = new Gson();
        bold_font = Typeface.createFromAsset(context.getAssets(), "NotoSans-Bold.otf");
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    class ViewHolder{
        TextView title;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder vh = null;
        aQuery = new AQuery(context);
        if (view == null) {
            view = View.inflate(context, layout, null);
            vh = new ViewHolder();
            vh.title = (TextView) view.findViewById(R.id.title);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        final PoiDTO item = data.get(i);
        if ("지역".equals(item.getMiddleBizName())) {
            vh.title.setText(item.getUpperAddrName() + " " + item.getMiddleAddrName() + " " + item.getLowerAddrName());
        } else {
            vh.title.setText(item.getUpperAddrName() + " " + item.getMiddleAddrName() + " " + item.getLowerAddrName() + " " + item.getName());
        }
        if (-1 != vh.title.getText().toString().indexOf(searchWord)) {
            spannableStringBuilder = new SpannableStringBuilder(vh.title.getText().toString());
            spannableStringBuilder.setSpan(new CustomTypefaceSpan("", bold_font), vh.title.getText().toString().indexOf(searchWord), vh.title.getText().toString().indexOf(searchWord) + searchWord.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#f41758")), vh.title.getText().toString().indexOf(searchWord), vh.title.getText().toString().indexOf(searchWord) + searchWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            vh.title.setText(spannableStringBuilder);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("la", item.getNoorLat());
                intent.putExtra("lo", item.getNoorLon());
                ((Activity)context).setResult(666, intent);
                ((Activity)context).finish();
            }
        });
        animation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        view.startAnimation(animation);
        return view;
    }
}
