package com.group.a5g.smartmeterandreceptacle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Daniel Phillips on 10/25/2016.
 */
//Adapter for MainMenu ListView
public class MainListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<MainAdapterObject> objects;


    private class ViewHolder {
        public TextView MainText;
        public  TextView SubText;
        public ImageView Icon;


    }

    public MainListAdapter(Context context, ArrayList<MainAdapterObject> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }


    @Override
    public MainAdapterObject getItem(int position) {
        return objects.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return objects.size();
    }



    @Override
    public int getItemViewType(int position) {

        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(
                    R.layout.mainlist_adapter, null);
            holder.MainText = (TextView) convertView
                    .findViewById(R.id.main_adapter_titleText);
            holder.SubText = (TextView) convertView
                    .findViewById(R.id.man_adapter_SubTex);
            holder.Icon = (ImageView) convertView.findViewById(R.id.main_adapter_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.SubText.setText(objects.get(position).getSubTitle());
        holder.MainText.setText(objects.get(position).getTitle());
        holder.Icon.setImageResource(objects.get(position).getIcon());

        return convertView;
    }




}
