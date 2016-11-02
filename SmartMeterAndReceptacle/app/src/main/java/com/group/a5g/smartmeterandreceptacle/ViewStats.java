package com.group.a5g.smartmeterandreceptacle;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;




/**
 * Created by Daniel Phillips on 10/25/2016.
 */
// This Activity Allows The User To View Usage Data
public class ViewStats  extends AppCompatActivity {
    LineChart mChart;
    File OutputLocation;
    Spinner ViewSpinner;
    TextView CostPerWatt;
    TextView WattsUsed ;
    TextView BillEstimate;
    TextView HighestConsumer;
    String KeyHash;
    ArrayAdapter<String> RemSpinnerAdapter;
    List<String> SpinnerList;
    String [] SpinnerValues;
    String HighestConsumerString ="";
    String TotalWattsString="";
    String CostPerWattString="";
    String BillEstimateString ="";
    float CostPerWattInt = 1;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_stats);
        setTitle("View Stats");
        final SharedPreferences data = PreferenceManager
                .getDefaultSharedPreferences(this);
        KeyHash = data.getString("MD5MeterKey", "");
        float StoredCostPerWatt  = data.getFloat("CostPerWatt", 0);
        CostPerWattInt = StoredCostPerWatt;
        SpinnerList = new ArrayList<String>();
        SpinnerList.add("Total Power");
        SpinnerValues = new String[SpinnerList.size()];
        SpinnerList.toArray(SpinnerValues);
        RemSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SpinnerList);
        RemSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChart = (LineChart) findViewById(R.id.view_stats_chart);
        CostPerWatt = (TextView) findViewById(R.id.view_stats_CostPerWatt);
        WattsUsed = (TextView) findViewById(R.id.view_stats_WattsUsed);
        BillEstimate = (TextView) findViewById(R.id.view_stats_BillEstimate);
        HighestConsumer = (TextView) findViewById(R.id.view_stats_Highest_Consumer);
        Button Refresh = (Button) findViewById(R.id.view_stats_refresh);
        ViewSpinner = (Spinner) findViewById(R.id.view_stats_Spinner);
        ViewSpinner.setAdapter(RemSpinnerAdapter);
        ViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                ReadFile();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DownloadReadings().execute("");
            }


        });

        //Downloads Relevant Readings once Activity Is Loaded
        new DownloadReadings().execute("");
    }
    private class DownloadReadings extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //Downloads Readings From Dropbox
            DbxRequestConfig config = new DbxRequestConfig("/", "en_US");
            DbxClientV2 client = new DbxClientV2(config, "aFhFx-elzFAAAAAAAAAAC_Wiu6wqOUS51LAIX9TiuHMa8Nf4N4NTw-CGGr5EULMd");


            ListFolderResult result = null;
            try {
                //Gets A List of All Files In DropBox
                result = client.files().listFolder("");
            } catch (DbxException e) {
                e.printStackTrace();
            }
            String MostRecientFile = "";


            while (true) {
                //Goes Through List of Files Obtained
                for (Metadata metadata : result.getEntries()) {
                    try {

                        String pathName = metadata.getPathLower().toString().replace("/", "");

                        String[] path = pathName.split("_");
                        //Gets the file containing the readings for the meter connected to this app
                        if ((path[1].equals("readings.txt")) && (path[0].equals(KeyHash))) {
                            MostRecientFile = metadata.getPathLower();

                        }
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }

                if (!result.getHasMore()) {
                    break;
                }

                try {
                    result = client.files().listFolderContinue(result.getCursor());
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }
            //Downloads the file with the readings and saves it to the cache directory
            File OutputLocationDir = new File(getExternalCacheDir() + "/");
            OutputLocation = new File(getExternalCacheDir() + MostRecientFile);
            OutputLocationDir.mkdir();
            try (OutputStream in = new FileOutputStream(OutputLocation)) {
                FileMetadata metadata = client.files().downloadBuilder(MostRecientFile)
                        .download(in);
                try (BufferedReader br = new BufferedReader(new FileReader(OutputLocation))) {
                    String line;
                    //Initalizes Values
                    float HighestValue = 0;
                    float HighestMonthCumPower = 0;
                    float HighestOldCumPower = 0;
                    String HighestName = "";
                    //Reads the readings file line by line
                    while ((line = br.readLine()) != null) {
                        try {
                            String[] linevalues = line.split(",");
                            try{
                                //Gets the Highest Consumer Value...i.e. the Receptacle that Consumed the Most Power
                                if ((Float.parseFloat(linevalues[1]) > HighestValue) && (!linevalues[0].equals("CumPower"))) {
                                    HighestValue = Float.parseFloat(linevalues[1]);
                                    HighestName = linevalues[0];
                                }
                                //Adds Each Receptacle Name in the Readings File to the Android Dropdown Menu
                                if (!SpinnerList.contains(linevalues[0]) && (!linevalues[0].toString().trim().equals("")) && (!linevalues[0].equals("CumPower"))){

                                    SpinnerList.add(linevalues[0]);

                                }
                                //Gets the Bill Estimate Power Values
                                if(linevalues[0].equals("CumPower")){
                                    DateTime dtNow = new DateTime();
                                    String[] CurrLineDate = linevalues[2].split("_");
                                    DateTime dtParse = new DateTime(Integer.parseInt(CurrLineDate[2]), Integer.parseInt(CurrLineDate[0]), Integer.parseInt(CurrLineDate[1]), 0, 0, 0, 0);
                                //Gets the Largest Power Value Before The Current Month
                                    if ((dtNow.isAfter(dtParse)) && (dtNow.getMonthOfYear()) != dtParse.getMonthOfYear()) {
                                        if (HighestOldCumPower < Float.parseFloat(linevalues[1])) {
                                            HighestOldCumPower = Float.parseFloat(linevalues[1]);

                                        }
                                    }
                                    //Gets the Largest Power Value for This Month
                                    if(dtNow.getMonthOfYear() == dtParse.getMonthOfYear()){
                                        if( HighestMonthCumPower < Float.parseFloat(linevalues[1])){
                                            HighestMonthCumPower = Float.parseFloat(linevalues[1]);

                                        }

                                    }

                                }
                            }catch (java.lang.NullPointerException e) {
                                e.printStackTrace();
                            }

                        }catch(ArrayIndexOutOfBoundsException e){
                            e.printStackTrace();
                        }

                    }
                    //Calculates the Relevant Values To Be Displayed
                    float MonthlyWatts = HighestMonthCumPower - HighestOldCumPower;
                    TotalWattsString = "Watts Used This Month :" + String.valueOf(MonthlyWatts);
                    BillEstimateString = "Bill Estimate :$" + String.valueOf(MonthlyWatts*CostPerWattInt);
                    HighestConsumerString = "Highest Consumer :" + HighestName;
                    CostPerWattString = "Cost Per Watt :$"+CostPerWattInt;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return "SuccessFul";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(ViewStats.this, "Completed",
                    Toast.LENGTH_LONG).show();
            //Sets Values In Activity
            ReadFile();
            SpinnerValues = new String[SpinnerList.size()];
            SpinnerList.toArray(SpinnerValues);
            WattsUsed.setText(TotalWattsString);
            BillEstimate.setText(BillEstimateString);
            HighestConsumer.setText(HighestConsumerString);
            CostPerWatt.setText(CostPerWattString);
            RemSpinnerAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(ViewStats.this, "Loading",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    private void ReadFile() {
        ArrayList<Entry> values = new ArrayList<Entry>();
        if (OutputLocation != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(OutputLocation))) {
                String line;
                int valuecounter = 0;
                while ((line = br.readLine()) != null) {
                    try {
                        //Adds Values to Graph
                        String[] linevalues = line.split(",");
                        //Adds Total Power Values If Selected.
                        if (ViewSpinner.getSelectedItemPosition() == 0) {
                            if (linevalues[0].equals("CumPower")) {
                                valuecounter++;
                                Log.d("value added", linevalues[1] + "," + valuecounter);
                                values.add(new Entry(valuecounter, Float.parseFloat(linevalues[1])));
                            }
                        } else {
                            //Adds Values to Graph For Receptacles If Selected.
                            if (linevalues[0].equals(ViewSpinner.getSelectedItem())) {
                                valuecounter++;
                                Log.d("value added", linevalues[1] + "," + valuecounter);
                                values.add(new Entry(valuecounter, Float.parseFloat(linevalues[1])));
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            LineDataSet set1;

            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                mChart.getData().notifyDataChanged();
                mChart.notifyDataSetChanged();
            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values, "Power Consumed");

                // set the line to be drawn like this "- - - - - -"
                set1.enableDashedLine(10f, 5f, 0f);
                set1.enableDashedHighlightLine(10f, 5f, 0f);
                set1.setColor(Color.BLACK);
                set1.setCircleColor(Color.BLACK);
                set1.setLineWidth(1f);
                set1.setCircleRadius(3f);
                set1.setDrawCircleHole(false);
                set1.setValueTextSize(9f);
                set1.setDrawFilled(true);
                set1.setFormLineWidth(1f);
                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                set1.setFormSize(15.f);


                set1.setFillColor(Color.BLACK);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData data = new LineData(dataSets);

                // set data
                mChart.setData(data);

            }

            //Update Graph Plot
            mChart.invalidate();

        }

    }}