package app.cosmos.reconbike;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;


import java.util.ArrayList;

import app.cosmos.reconbike.DTO.Notice;


public class NoticeListAdapter extends BaseExpandableListAdapter {

    Context context;
    int layout;
    int layout2;
    ArrayList<Notice> data;
    ArrayList<ArrayList<Notice>> data2;
    LayoutInflater inflater;
    ExpandableListView list;
    boolean[] mGroupClickState;
    String getImagePath, image_type;
    int lastExpandedGroupPosition = -1;
    Animation child_list = null;

    public NoticeListAdapter(Context context, ExpandableListView list, int layout, int layout2, ArrayList<Notice> data, ArrayList<ArrayList<Notice>> data2) {
        this.context = context;
        this.layout = layout;
        this.layout2 = layout2;
        this.data = data;
        this.data2 = data2;
        this.list = list;
        mGroupClickState = new boolean[data.size()];
        for (int i=0; i<mGroupClickState.length; i++) {
            mGroupClickState[i] = false;
        }
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data2.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data2.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    class ViewHolder{
        ImageView notice_img;
        TextView notice_text;
        TextView notice_title;
        ImageView notice_arrow;
    }

    class ViewHolder2{
        TextView notice_content;
        LinearLayout image_listview_con;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView==null) {
            convertView = View.inflate(context, layout, null);
            holder = new ViewHolder();
            holder.notice_img = (ImageView) convertView.findViewById(R.id.notice_img);
            holder.notice_text = (TextView) convertView.findViewById(R.id.notice_text);
            holder.notice_title = (TextView) convertView.findViewById(R.id.notice_title);
            holder.notice_arrow = (ImageView) convertView.findViewById(R.id.notice_arrow);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Notice item = data.get(groupPosition);
        if ("1".equals(item.getType().toString())) {
            holder.notice_img.setImageResource(R.drawable.notice_type1);
            holder.notice_text.setText("공지");
            holder.notice_text.setTextColor(Color.parseColor("#ff3944"));
        } else if ("2".equals(item.getType().toString())) {
            holder.notice_img.setImageResource(R.drawable.notice_type2);
            holder.notice_text.setText("이벤트");
            holder.notice_text.setTextColor(Color.parseColor("#ffa305"));
        }
        holder.notice_title.setText(item.getTitle());
        final ViewHolder finalHolder = holder;
        if (list.isGroupExpanded(groupPosition)) {
            finalHolder.notice_arrow.setImageResource(R.drawable.arrow_up);
        } else {
            finalHolder.notice_arrow.setImageResource(R.drawable.arrow_down);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list.isGroupExpanded(groupPosition)) {
                    list.collapseGroup(groupPosition);
                } else {
                    list.expandGroup(groupPosition);
                }
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder2 holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, layout2, null);
            holder = new ViewHolder2();
            holder.notice_content = (TextView) convertView.findViewById(R.id.notice_content);
            holder.image_listview_con = (LinearLayout) convertView.findViewById(R.id.image_listview_con);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder2) convertView.getTag();
        }
        final Notice item2 = data2.get(groupPosition).get(childPosition);
        child_list = AnimationUtils.loadAnimation(context, R.anim.child_list);
        if (child_list != null) {
            convertView.startAnimation(child_list);
            child_list = null;
        }
        holder.notice_content.setText(item2.getContent());
        holder.image_listview_con.removeAllViews();
        if (!"".equals(item2.getImage().toString().trim())) {
            String[] getImages = item2.getImage().toString().trim().split(",");
            for (int i = 0; i < getImages.length; i++) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View addView = inflater.inflate(R.layout.list_notice_image, null);
                ImageView image = (ImageView) addView.findViewById(R.id.image);
                GlideDrawableImageViewTarget gif_image = new GlideDrawableImageViewTarget(image);
                getImagePath = UrlManager.getBaseUrl() + "/download/image/" + getImages[i];
                Glide.with(context).load(getImagePath).crossFade().into(gif_image);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, InputImagePathActivity.class);
                        intent.putExtra("path",  getImagePath);
                        context.startActivity(intent);
                    }
                });
                holder.image_listview_con.addView(addView);
            }
        }
        return convertView;
    }
    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        mGroupClickState[groupPosition] = !mGroupClickState[groupPosition];
        if (lastExpandedGroupPosition != -1 && lastExpandedGroupPosition != groupPosition) {
            list.collapseGroup(lastExpandedGroupPosition);
            mGroupClickState[lastExpandedGroupPosition] = false;
        }
        lastExpandedGroupPosition = groupPosition;
        super.onGroupExpanded(groupPosition);
    }

}

