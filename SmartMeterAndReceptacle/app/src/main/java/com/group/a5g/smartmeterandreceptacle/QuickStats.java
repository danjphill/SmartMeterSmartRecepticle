package com.group.a5g.smartmeterandreceptacle;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

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

import static android.R.attr.entries;

/**
 * Created by Daniel Phillips on 11/1/2016.
 */

public class QuickStats extends AppCompatActivity {
    PieChart mChart;
    File OutputLocation;
    List<String> ReceptacleList;
    List<Float> ReceptacleHighestValuesList;
    float TotalMonthlyPower;
    String KeyHash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_stats);
        setTitle("Quick Stats");
        mChart= (PieChart) findViewById(R.id.chart1);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);


        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(10f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(90);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);


        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        //mChart.spin(2000, 0, 360);


        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTextSize(12f);
        mChart.setDrawEntryLabels(false);
        ReceptacleList = new ArrayList<String>();
        ReceptacleHighestValuesList = new ArrayList<Float>();
        final SharedPreferences data = PreferenceManager
                .getDefaultSharedPreferences(this);
        KeyHash = data.getString("MD5MeterKey", "");
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
                    String line1;
                    //Reads the readings file line by line
                    float HighestOldCumPower = 0;
                    float HighestMonthCumPower = 0;

                    while ((line1 = br.readLine()) != null) {
                        try {
                            String[] linevalues = line1.split(",");
                            try {
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

                                if (!ReceptacleList.contains(linevalues[0]) && (!linevalues[0].toString().trim().equals("")) && (!linevalues[0].equals("CumPower"))) {

                                    ReceptacleList.add(linevalues[0]);

                                }
                            } catch (java.lang.NullPointerException e) {
                                e.printStackTrace();
                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                    }
                    TotalMonthlyPower = HighestMonthCumPower - HighestOldCumPower;

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


                float HighestValue = 0;
                for (int i = 0; i < ReceptacleList.size(); i++) {
                    try (BufferedReader br2 = new BufferedReader(new FileReader(OutputLocation))) {
                    String line2 = null;
                    Log.d("ValueofI",i+" ");
                    Log.d("ReceptacleList.get(i)",ReceptacleList.get(i));
                    while ((line2 = br2.readLine()) != null) {
                        try {
                            String[] linevalues = line2.split(",");
                            try {
                                if ((ReceptacleList.get(i).equals(linevalues[0]))) {
                                    if(HighestValue < Float.parseFloat(linevalues[1])) {
                                        HighestValue = Float.parseFloat(linevalues[1]);
                                        Log.d("HighestValue", HighestValue + " " + i);
                                    }

                                }
                            } catch (java.lang.NullPointerException e) {
                                e.printStackTrace();
                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d("HighestEnd",HighestValue+"");

                    ReceptacleHighestValuesList.add(HighestValue);
                    HighestValue = 0;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();


                    }

                }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(QuickStats.this, "Completed",
                    Toast.LENGTH_LONG).show();
            //Sets Values In Activity
            PlotStats();

        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(QuickStats.this, "Loading",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
    private void PlotStats() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();




        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        float TotalPower = 0;

        for(int i = 0; i<ReceptacleHighestValuesList.size();i++) {
            TotalPower +=ReceptacleHighestValuesList.get(i);
            Log.d("ReceptacleHighest",ReceptacleHighestValuesList.get(i)+"");
        }

        Log.d("total",TotalPower+"");

        for(int i = 0; i<ReceptacleHighestValuesList.size();i++) {
            entries.add(new PieEntry((ReceptacleHighestValuesList.get(i)),ReceptacleList.get(i)));
            Log.d("Enteries",""+ReceptacleHighestValuesList.get(i));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Power Consumption");

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.setCenterText(generateCenterSpannableText());
        mChart.invalidate();
        mChart.animateY(1400);

//        dataSet.setSliceSpace(3f);
//        dataSet.setSelectionShift(5f);
//
//        // add a lot of colors
//
//        ArrayList<Integer> colors = new ArrayList<Integer>();
//
////        for (int c : ColorTemplate.VORDIPLOM_COLORS)
////            colors.add(c);
////
////        for (int c : ColorTemplate.JOYFUL_COLORS)
////            colors.add(c);
////
////        for (int c : ColorTemplate.COLORFUL_COLORS)
////            colors.add(c);
////
////        for (int c : ColorTemplate.LIBERTY_COLORS)
////            colors.add(c);
//
//      for (int c : ColorTemplate.MATERIAL_COLORS)
//        colors.add(c);
//
//        //colors.add(ColorTemplate.getHoloBlue());
//
//        dataSet.setColors(colors);
//        dataSet.setSelectionShift(0f);
//
//        PieData data = new PieData(dataSet);
//        data.setValueFormatter(new PercentFormatter());
//        data.setValueTextSize(11f);
//        data.setValueTextColor(Color.BLACK);
//        mChart.setData(data);
//
//        // undo all highlights
//        mChart.highlightValues(null);
//
//        mChart.invalidate();


        }

    private SpannableString generateCenterSpannableText() {
        String text = Math.round( TotalMonthlyPower * 100.0 ) / 100.0+ " W \n Consumed";
        SpannableString s = new SpannableString(text);
        s.setSpan(new RelativeSizeSpan(1.7f), 0, text.indexOf("\n"), 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), text.indexOf("\n"),text.length() , 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY),text.indexOf("\n"), text.length(), 0);
        //s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        return s;
    }

        }
