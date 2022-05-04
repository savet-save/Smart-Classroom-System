package com.example.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;

public class ClassroomAdapter extends BaseAdapter {
    private Context mContext;
    private LinkedList<Classroom> mData;

    public ClassroomAdapter() {}

    public ClassroomAdapter(LinkedList<Classroom> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    public void add(Classroom data) {
        if (mData == null) {
            mData = new LinkedList<>();
        }
        mData.add(data);
        notifyDataSetChanged();
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if(convertView == null){

            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item,parent,false);

            holder = new ViewHolder();

            holder.class_name = (TextView) convertView.findViewById(R.id.class_name);
            holder.class_info = (TextView) convertView.findViewById(R.id.class_info);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.class_name.setText(mData.get(position).getClassroomName());
        holder.class_info.setText(mData.get(position).getClassroomInfo());
        return convertView;
    }

    private class ViewHolder{
        TextView class_name;
        TextView class_info;
    }
}
