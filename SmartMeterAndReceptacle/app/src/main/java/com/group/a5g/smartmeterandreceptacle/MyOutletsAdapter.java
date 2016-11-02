package com.group.a5g.smartmeterandreceptacle;

import android.content.Context;
import android.graphics.Color;
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
//Adapter for MyOutles ListView
public class MyOutletsAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<OutletObject> objects;


    private class ViewHolder2 {
        public TextView Name;
        public  TextView Address;
        public TextView Number;
        public ImageView Power;


    }

    public MyOutletsAdapter(Context context, ArrayList<OutletObject> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }


    @Override
    public OutletObject getItem(int position) {
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
        ViewHolder2 holder;
        if (convertView == null) {
            holder = new ViewHolder2();
            convertView = inflater.inflate(
                    R.layout.outlet_adapter, parent,false);
            holder.Power = (ImageView) convertView.findViewById(R.id.outlet_adapter_power);
            holder.Number = (TextView) convertView.findViewById(R.id.outlet_adapter_Number);
            holder.Name = (TextView) convertView.findViewById(R.id.outlet_adapter_Name);
            holder.Address = (TextView) convertView.findViewById(R.id.outlet_adapter_Address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder2) convertView.getTag();
        }

        String current_power = objects.get(position).getPower();
        if(current_power.equals("True")){
            holder.Power.setBackgroundColor(Color.rgb(0,255,68));
        }else{
            holder.Power.setBackgroundColor(Color.rgb(255,0,68));
        }
        holder.Address.setText(objects.get(position).getAddress());
        holder.Number.setText(objects.get(position).getNumber());
        holder.Name.setText(objects.get(position).getName());
        return convertView;
    }




}
