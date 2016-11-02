package com.group.a5g.smartmeterandreceptacle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.github.mikephil.charting.data.Entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Daniel Phillips on 10/26/2016.
 */
// This activity allows users to Add Remove and Turn On and Off Receptacles.
public class MyOutlets extends AppCompatActivity {
    ArrayList<OutletObject> ListViewSet;
    MyOutletsAdapter adapter;
    boolean Edit = false;
    boolean Delete = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myoutlets_activity);
        setTitle("My Outlets");
        ListViewSet = new ArrayList<>();
        ListView lv = (ListView) findViewById(R.id.myoutlets_listview);
        adapter = new MyOutletsAdapter(MyOutlets.this, ListViewSet);
        registerForContextMenu(lv);
        //Opening Configuration File Stored On Device
        File config_dir = new File(getExternalFilesDir(null)+"/Config/");
        config_dir.mkdir();
        File confilg_file = new File(config_dir+"/MasterConfig.cfg");
        if(confilg_file.exists()){
            try (BufferedReader br = new BufferedReader(new FileReader(confilg_file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] linevalues = line.split(",");
                        String Number = linevalues[1].replace("Name","");
                        ListViewSet.add(new OutletObject(linevalues[0],linevalues[2],Number,linevalues[3]));
                    }catch(ArrayIndexOutOfBoundsException e){
                        e.printStackTrace();
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //Message Shown if Config File Not Found
            Toast.makeText(getBaseContext(),"No Config File, Add New Outlets",
                    Toast.LENGTH_SHORT).show();
        }
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //Shows Dialog When Item Is CLicked in ListView
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                View dialongview = (LayoutInflater.from(MyOutlets.this)).inflate(
                        R.layout.outlets_dialog, null);


                final EditText dialog_Name = (EditText) dialongview.findViewById(R.id.outlets_dialog_Name);
                final EditText dialog_Address = (EditText) dialongview.findViewById(R.id.outlets_dialog_Address);
                final EditText dialog_Number = (EditText) dialongview.findViewById(R.id.outlets_dialog_Number);
                final ToggleButton dialog_Power = (ToggleButton) dialongview.findViewById(R.id.outlets_dialog_button);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        MyOutlets.this);
                alertBuilder.setView(dialongview);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Outlet Settings");
                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertBuilder.setPositiveButton("Save",

                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String Power;
                                //Checks Power Toggle Button
                                if(dialog_Power.isChecked()){
                                    Power = "True";
                                }else{
                                    Power = "False";
                                }

                                try {
                                    Delete = false;
                                    Edit = true; //Lets The Update Function Know That This is An Edit
                                    //Update Function Called
                                    updateConfig(dialog_Name.getText().toString(),dialog_Address.getText().toString(),dialog_Number.getText().toString(),Power,i);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }


                                dialog.dismiss();

                            }
                        });
                //Sets the value of the Dialog Items to those of the Item Clicked
                dialog_Address.setText(adapter.getItem(i).getAddress());
                dialog_Name.setText(adapter.getItem(i).getName());
                dialog_Number.setText(adapter.getItem(i).getNumber().replace("Name",""));
                if(adapter.getItem(i).getPower().equals("True")) {
                    dialog_Power.setChecked(true);
                }else{
                    dialog_Power.setChecked(false);
                }

                Dialog dialog = alertBuilder.create();
                dialog.show();


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.myoutletsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new:

                View dialongview = (LayoutInflater.from(MyOutlets.this)).inflate(
                        R.layout.outlets_dialog, null);


                final EditText dialog_Name = (EditText) dialongview.findViewById(R.id.outlets_dialog_Name);
                final EditText dialog_Address = (EditText) dialongview.findViewById(R.id.outlets_dialog_Address);
                final EditText dialog_Number = (EditText) dialongview.findViewById(R.id.outlets_dialog_Number);
                final ToggleButton dialog_Power = (ToggleButton) dialongview.findViewById(R.id.outlets_dialog_button);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        MyOutlets.this);
                alertBuilder.setView(dialongview);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Outlet Settings");
                alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertBuilder.setPositiveButton("Save",

                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                String Power;
                                if(dialog_Power.isChecked()){
                                    Power = "True";
                                }else{
                                    Power = "False";
                                }

                                try {
                                    // Sets Both Items False so the Update Function Knows This is a new Item
                                    Edit = false;
                                    Delete = false;
                                    updateConfig(dialog_Name.getText().toString(),dialog_Address.getText().toString(),dialog_Number.getText().toString(),Power,0);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                Dialog dialog = alertBuilder.create();
                dialog.show();


        break;

    }
    return super.onOptionsItemSelected(item);
}
    private void updateConfig(String Name, String Address, String Number, String Power,int Position) throws FileNotFoundException, UnsupportedEncodingException {
        if(Edit){
            //if Editing Item the Item Properties are Taken From the Set
            ListViewSet.get(Position).setAddress(Address);
            ListViewSet.get(Position).setNumber(Number);
            ListViewSet.get(Position).setName(Name);
            ListViewSet.get(Position).setPower(Power);
            Edit = false;

        }else if (Delete) {
            Delete = false;
            //if Editing Item is removed From the Set
            ListViewSet.remove(Position);
        }else{
            //Adds New Item To ListSet
            ListViewSet.add(new OutletObject(Address, Name, Number, Power));
        }

        File config_dir = new File(getExternalFilesDir(null)+"/Config/");
        config_dir.mkdir();
        File confilg_file = new File(config_dir+"/MasterConfig.cfg");
        PrintWriter writer = new PrintWriter(confilg_file, "UTF-8");
        //The ConfigFile is Updated Based on the Items in the ListViewSet
        for(int i = 0;i<ListViewSet.size();i++){
            writer.println(ListViewSet.get(i).getAddress()+",Name"+ListViewSet.get(i).getNumber()+","+ListViewSet.get(i).getName()+","+ListViewSet.get(i).getPower());
        }

        writer.close();





        adapter.notifyDataSetChanged();
        //Calls Function to Place Config In the Dropbox so that the meter can access it.
        UploadConfig(confilg_file);
    }

    DbxRequestConfig config = new DbxRequestConfig("/", "en_US");
    DbxClientV2 client = new DbxClientV2(config, "aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd");
    private void UploadConfig(final File Location){
        final SharedPreferences data = PreferenceManager
                .getDefaultSharedPreferences(this);
        final String KeyHash = data.getString("MD5MeterKey","");
        //Uploads Config to Dropbox
        new Thread(new Runnable() {
            public void run() {
                try (InputStream in = new FileInputStream(Location)) {
                    FileMetadata metadata = client.files().uploadBuilder("/"+KeyHash+"_"+Location.getName())
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(in);
                } catch (DbxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }}).start();
        //Lets the User Know the Config Was Updated
        Toast.makeText(getBaseContext(),"Config Updated",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
            //Adds The Delete Button on to ListView
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Menu");
           menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            //Runs when Delete Button Is Pressed
        try {
            Delete = true;
            Edit = false;
            updateConfig(ListViewSet.get(info.position).getName(),ListViewSet.get(info.position).getAddress(),ListViewSet.get(info.position).getNumber(),ListViewSet.get(info.position).getPower(),info.position);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return super.onContextItemSelected(item);


    }
}
