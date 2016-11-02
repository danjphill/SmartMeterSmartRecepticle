package com.group.a5g.smartmeterandreceptacle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.security.Key;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * Created by Daniel Phillips on 10/25/2016.
 */
/*Main Menu where the user can select which part of the app they would like to go to */
public class MainMenu extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main);
        setTitle("SMSR 5G");
        final SharedPreferences data = PreferenceManager
                .getDefaultSharedPreferences(this);
        String Key = data.getString("MeterKey", "None");
        String KeyHash = data.getString("MD5MeterKey","None");
        //If No Key Or Hash Is Found The User Is Sent To the Activity to Add In Key
        if(KeyHash.equals("None")||Key.equals("None")){
            Intent AddKey = new Intent(this,ChangeKeyActivity.class);
            startActivity(AddKey);
        }
        float StoredCostPerWatt  = data.getFloat("CostPerWatt", 0);
        View parentLayout = findViewById(R.id.rootView);
        //If No Stored Cost Per Watt The User Is Notified
        if(StoredCostPerWatt == 0){
            Snackbar snackbar = Snackbar
                    .make(parentLayout, "Cost Per Watt Not Set!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("SET", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent SettingsIntent = new Intent(MainMenu.this,SettingsActivity.class);
                            startActivity(SettingsIntent);
                        }
                    });

            snackbar.show();
        }
        ArrayList<MainAdapterObject> ListViewSet = new ArrayList<MainAdapterObject>();
        ListView lv = (ListView) findViewById(R.id.main_listview);
        MainListAdapter adapter = new MainListAdapter(this, ListViewSet);

        //Adds Main Menu Buttons
        ListViewSet.add(new MainAdapterObject("Quick Stats","View Power Consumption Pie Chart",R.mipmap.quick_stats));
        ListViewSet.add(new MainAdapterObject("View Stats","View Line Graphs of Power Consumption for your Home",R.drawable.main_graph));
        ListViewSet.add(new MainAdapterObject("Change Key","Connect Your Meter To The App So That You Can Receive Readings",R.drawable.main_key));
        ListViewSet.add(new MainAdapterObject("My Outlets","Add and Control Your Outlets",R.drawable.main_plug));
        ListViewSet.add(new MainAdapterObject("Settings","Change Your Settings Such As Your Electricity Rate",R.drawable.main_settings));
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Launches Activities Based On The Button Clicked
                switch(i){
                    case 0:
                        Intent QuickStatsIntent = new Intent(MainMenu.this,QuickStats.class);
                        startActivity(QuickStatsIntent);
                        break;
                    case 1:
                        Intent GraphIntent = new Intent(MainMenu.this,ViewStats.class);
                        startActivity(GraphIntent);
                        break;
                    case 2:
                        Intent KeyIntent = new Intent(MainMenu.this,ChangeKeyActivity.class);
                        startActivity(KeyIntent);
                        break;
                    case 3:
                        Intent OutletsIntent = new Intent(MainMenu.this,MyOutlets.class);
                        startActivity(OutletsIntent);
                        break;
                    case 4:
                        Intent SettingsIntent = new Intent(MainMenu.this,SettingsActivity.class);
                        startActivity(SettingsIntent);
                        break;

                }
            }
        });
    }
}
