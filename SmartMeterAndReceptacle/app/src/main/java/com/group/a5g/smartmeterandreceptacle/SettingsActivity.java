package com.group.a5g.smartmeterandreceptacle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Daniel Phillips on 10/30/2016.
 */
//This Activity Allows a User to Change Their Cost Per Watt
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Settings");
        setContentView(R.layout.activity_settings);
        ArrayList<String> SettingsListSet = new ArrayList<String>();
        SettingsListSet.add("Change Cost Per Watt");
        ListView SettingsListView = (ListView)(findViewById(R.id.settings_listview));
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, SettingsListSet);
        SettingsListView.setAdapter(listAdapter);
        //Dialog to change Cost Per Watt (In Reality This Should Be Cost Per kW but since we are prototyping this value was used)
        SettingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        final SharedPreferences data = PreferenceManager
                                .getDefaultSharedPreferences(SettingsActivity.this);
                        float StoredCostPerWatt  = data.getFloat("CostPerWatt", 0);
                        View dialongview = (LayoutInflater.from(SettingsActivity.this)).inflate(
                                R.layout.change_cost, null);
                        TextView CurrentCostPerWatt = (TextView) dialongview.findViewById(R.id.change_cost_currentTextView);
                        final EditText NewCostPerWatt = (EditText) dialongview.findViewById(R.id.change_cost_NewRate);

                        CurrentCostPerWatt.setText("Current Cost Per Watt :$"+StoredCostPerWatt);
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                                SettingsActivity.this);
                        alertBuilder.setView(dialongview);


                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("Change Cost Per Watt");
                        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        alertBuilder.setPositiveButton("Change",

                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        try {
                                            //Saves Value Entered by the User as Float
                                            SharedPreferences data = PreferenceManager
                                                    .getDefaultSharedPreferences(SettingsActivity.this);
                                            SharedPreferences.Editor editor = data.edit();
                                            editor.putFloat("CostPerWatt", Float.parseFloat(NewCostPerWatt.getText().toString()));
                                            editor.commit();
                                        }catch (NumberFormatException e){
                                            Toast.makeText(SettingsActivity.this, "An Error Has Occurred",
                                                    Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }

                                    }
                                });
                        Dialog dialog = alertBuilder.create();
                        dialog.show();
                        break;
                }
            }
        });

    }
}
