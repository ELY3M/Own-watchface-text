package own.ownwatchfacetext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Bundle;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


//https://forecast-v3.weather.gov/point/43.1858,-76.1728

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String TAG = "ownwatchface Main";

    private static MainActivity main;

    private String url = "https://forecast.weather.gov/MapClick.php?";
    private String finalurl = "setup";
    private String visiturl = "setup";
    private handlejson obj;

    //settings to be global//
    int clockSize;
    int clocknosecsSize;
    int markerSize;
    int dateSize;
    int timeSize;
    int tempSize;
    int weatherSize;

    boolean clockAct;
    boolean clockDim;
    boolean clocknosecsAct;
    boolean clocknosecsDim;
    boolean markerDim;
    boolean dateDim;
    boolean timeDim;
    boolean tempDim;
    boolean weatherDim;
    boolean alwaysUtc;
    boolean showtime;
    boolean northernhemi;
    boolean useShortCards;

    private EditText clockSizeEditText;
    private EditText clocknosecsSizeEditText;
    private EditText markerSizeEditText;
    private EditText dateSizeEditText;
    private EditText timeSizeEditText;
    private EditText tempSizeEditText;
    private EditText weatherSizeEditText;
    private CheckBox clockactCheckBox;
    private CheckBox clockdimCheckBox;
    private CheckBox clocknosecsactCheckBox;
    private CheckBox clocknosecsdimCheckBox;
    private CheckBox markerCheckBox;
    private CheckBox dateCheckBox;
    private CheckBox timeCheckBox;
    private CheckBox tempCheckBox;
    private CheckBox weatherCheckBox;
    private CheckBox alwaysUtcCheckBox;
    private CheckBox showtimeCheckBox;
    private CheckBox northernhemiCheckBox;
    private CheckBox useShortCardsCheckBox;


    private TextView mygps;
    private double lat = 0.0;
    private double lon = 0.0;
    private String mylat = "0.0";
    private String mylon = "0.0";
    private String mytemp = "103°F";
    private String myicon = "skc";
    private String finalicon = "skc";
    private String myweather = "Clear";
    private int updatecount = 0;
    private TextView temp;
    private TextView weather;
    private ImageView icon;
    private TextView checkicon;
    private TextView checkfinalicon;
    private TextView lastupdatetime;
    private Button applyButton;
    private Button resetButton;
    private Button getweather;
    private Button startservice;
    private Button startupdates;
    private Button stopupdates;
    private Button visitnws;
    private GoogleApiClient GoogleApiClient;
    private boolean isConnectedToWearable = false;


    ///int interval = 10000; // 10 seconds
    ///int interval = 3600000; //TODO make it run per hr!
    ////int interval = 1800000; //every 30 mins
    public static final long GPSUPDATE_INTERVAL_IN_MILLISECONDS = 1800000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = GPSUPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private final static String REQUESTING_UPDATES_KEY = "requesting-updates-key";
    private final static String LOCATION_KEY = "location-key";
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingUpdates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        main = this;
        initializeGoogleAPI();
        setContentView(R.layout.activity_main);

        //start logging
        //new startlogging().execute();

        clockSizeEditText = (EditText) findViewById(R.id.clockSizeEditText);
        clocknosecsSizeEditText = (EditText) findViewById(R.id.clocknosecsSizeEditText);
        markerSizeEditText = (EditText) findViewById(R.id.markerSizeEditText);
        dateSizeEditText = (EditText) findViewById(R.id.dateSizeEditText);
        timeSizeEditText = (EditText) findViewById(R.id.timeSizeEditText);
        tempSizeEditText = (EditText) findViewById(R.id.tempSizeEditText);
        weatherSizeEditText = (EditText) findViewById(R.id.weatherSizeEditText);
        clockactCheckBox = (CheckBox) findViewById(R.id.clockactCheckBox);
        clockdimCheckBox = (CheckBox) findViewById(R.id.clockdimCheckBox);
        clocknosecsactCheckBox = (CheckBox) findViewById(R.id.clocknosecsactCheckBox);
        clocknosecsdimCheckBox = (CheckBox) findViewById(R.id.clocknosecsdimCheckBox);
        markerCheckBox = (CheckBox) findViewById(R.id.markerCheckBox);
        dateCheckBox = (CheckBox) findViewById(R.id.dateCheckBox);
        timeCheckBox = (CheckBox) findViewById(R.id.timeCheckBox);
        tempCheckBox = (CheckBox) findViewById(R.id.tempCheckBox);
        weatherCheckBox = (CheckBox) findViewById(R.id.weatherCheckBox);
        alwaysUtcCheckBox = (CheckBox) findViewById(R.id.alwaysUtcCheckBox);
        showtimeCheckBox = (CheckBox) findViewById(R.id.showtimeCheckBox);
        northernhemiCheckBox = (CheckBox) findViewById(R.id.northernhemiCheckBox);
        useShortCardsCheckBox = (CheckBox) findViewById(R.id.useShortCardsCheckBox);
        applyButton = (Button) findViewById(R.id.applyButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        getweather = (Button) findViewById(R.id.getweather);
        mygps = (TextView) findViewById(R.id.gpstext);
        temp = (TextView) findViewById(R.id.temp);
        weather = (TextView) findViewById(R.id.weather);
        icon = (ImageView) findViewById(R.id.icon);
        checkicon = (TextView) findViewById(R.id.checkicon);
        checkfinalicon = (TextView) findViewById(R.id.checkiconurl);
        lastupdatetime = (TextView) findViewById(R.id.lastupdatetime);
        visitnws = (Button) findViewById(R.id.visitnws);

        mRequestingUpdates = false;
        //start gps updates auto//
        updateValuesFromBundle(savedInstanceState);



        View.OnClickListener listener = new View.OnClickListener() {
            @Override public void onClick(View v) {
                showNumberPicker((EditText) v);
            }
        };

        clockSizeEditText.setOnClickListener(listener);
        clocknosecsSizeEditText.setOnClickListener(listener);
        markerSizeEditText.setOnClickListener(listener);
        dateSizeEditText.setOnClickListener(listener);
        timeSizeEditText.setOnClickListener(listener);
        tempSizeEditText.setOnClickListener(listener);
        weatherSizeEditText.setOnClickListener(listener);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                resetValues();
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                saveValues();
                pushValuesToWearable();
            }
        });

        loadValues();

    }

    public static MainActivity  getMain()
    {
        return main;
    }

    ////gps stufff///
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_UPDATES_KEY)) {
                mRequestingUpdates = savedInstanceState.getBoolean(REQUESTING_UPDATES_KEY);
                setButtonsEnabledState();
            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            lat = mCurrentLocation.getLatitude();
            lon = mCurrentLocation.getLongitude();
            mylat = String.valueOf(lat);
            mylon = String.valueOf(lon);
            mygps.setText("lat: " + mylat + " lon:" + mylon);
            //saveValues();
            //pushValuesToWearable();
            //make sure to setup latlon with weather url
            finalurl = url + "lat=" + mylat + "&lon=" + mylon + "&FcstType=json";
            visiturl = url + "lat=" + mylat + "&lon=" + mylon;
            Log.i(TAG, "updateValuesFromBundle finalurl: " + finalurl);
            Log.i(TAG, "updateValuesFromBundle lat: " + mylat + " lon: " + mylon);

        }
    }

    private void initializeGoogleAPI() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS) {
            GoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Wearable.API)
                    .build();
            createLocationRequest();
        }
    }



    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(GPSUPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startGPSUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(GoogleApiClient, mLocationRequest, this);
        mRequestingUpdates = true;
        setButtonsEnabledState();
        Toast.makeText(this, "GPS updates started", Toast.LENGTH_SHORT).show();

    }

    private void stopGPSUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(GoogleApiClient, this);
        mRequestingUpdates = false;
        setButtonsEnabledState();
    }



    public void weatherupdate() {
        ///get weather////
        Log.i(TAG,"weatherupdate() started");
        finalurl = url + "lat=" + mylat + "&lon=" + mylon + "&FcstType=json";
        visiturl = url + "lat=" + mylat + "&lon=" + mylon;
        Log.i(TAG, "finalurl: " + finalurl);
        //if (isOnlineSimple(getApplicationContext())) {
        if (isOnline(getApplicationContext())) {
            //if (checknet()) {
            if (GoogleApiClient.isConnected()) {
                //checkwear();
                //if (isConnectedToWearable) {
                obj = new handlejson(finalurl);
                obj.fetchJSON();
                while (obj.parsingComplete) ;
                mytemp = obj.getTemp();
                myicon = obj.getIcon();
                myweather = obj.getWeather();
                Log.i(TAG, "mytemp: " + mytemp);
                Log.i(TAG, "myicon: " + myicon);
                Log.i(TAG, " myweather: " + myweather);
                Pattern pattern = Pattern.compile("(.*?)(.png|.jpg|.gif)");
                Matcher geticon = pattern.matcher(myicon);
                while (geticon.find()) {
                    finalicon = geticon.group(1);
                }
                double finaltemp = Math.ceil(Double.valueOf(mytemp));
                mytemp = String.valueOf((int) finaltemp) + "°F";

                temp.setText(mytemp);
                weather.setText(myweather);
                int res = getResources().getIdentifier(finalicon, "drawable", getApplicationContext().getPackageName());
                icon.setImageResource(res);
                checkicon.setText(myicon);
                checkfinalicon.setText(finalicon);
                Log.i(TAG, "finalicon: " + finalicon);
                saveValues();
                pushValuesToWearable();
                SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
                Calendar c = Calendar.getInstance();
                String mytimestamp = timestamp.format(c.getTime());
                updatecount++;
                lastupdatetime.setTextSize(13);
                lastupdatetime.setText("Last Update: " + mytimestamp + "\nUpdate Count: " + updatecount);
                Log.i(TAG, "Last Update: " + mytimestamp + "\nUpdate Count: " + updatecount);
                ///Toast.makeText(getApplicationContext(), "Weather Update: " + mytimestamp + "  Update Count: " + updatecount, Toast.LENGTH_SHORT).show();

                //logging
                //SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
                //Calendar c = Calendar.getInstance();
                //String mytimestamp = timestamp.format(c.getTime());
                updatecount++;
                String LogString = "Temp : " + mytemp + " Icon: " + myicon + " Weather: " + myweather + "\nLast Update: " + mytimestamp + "\nUpdate Count: " + updatecount;
                Log.i(TAG, LogString);
                try {
                    FileWriter writer = new FileWriter("/sdcard/ownwatchtext-updates.txt", true);
                    BufferedWriter bufferedWriter = new BufferedWriter(writer);
                    bufferedWriter.write(LogString);
                    bufferedWriter.newLine();
                    bufferedWriter.write("-------------------------------------------------------------------");
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                } catch (IOException e) {
                    Log.i(TAG, "writer crash..." + e);
                    e.printStackTrace();
                }


                /*
        } else { ///wear connection
            SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
            Calendar c = Calendar.getInstance();
            String mytimestamp = timestamp.format(c.getTime());
            lastupdatetime.setTextSize(13);
            lastupdatetime.setText("Failed Update (Wear Disconnected): " + mytimestamp + "\nUpdate Count: " + updatecount);

        }*/
            } else { ///GoogleApi connection
                SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
                Calendar c = Calendar.getInstance();
                String mytimestamp = timestamp.format(c.getTime());
                updatecount++;
                lastupdatetime.setTextSize(13);
                lastupdatetime.setText("Failed Update (Google): " + mytimestamp + "\nUpdate Count: " + updatecount);
                Log.i(TAG, "Failed Update (Google): " + mytimestamp + "\nUpdate Count: " + updatecount);
            }

        } else { //internet check
            SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
            Calendar c = Calendar.getInstance();
            String mytimestamp = timestamp.format(c.getTime());
            updatecount++;
            lastupdatetime.setTextSize(13);
            lastupdatetime.setText("Failed Update (Offline): " + mytimestamp + "\nUpdate Count: " + updatecount);
            Log.i(TAG, "Failed Update (Offline): " + mytimestamp + "\nUpdate Count: " + updatecount);
        }


    }


    public void weatherupdatetest() {
        SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
        Calendar c = Calendar.getInstance();
        String mytimestamp = timestamp.format(c.getTime());
        updatecount++;
        lastupdatetime.setTextSize(13);
        lastupdatetime.setText("Last Update: " + mytimestamp + "  Update Count: " + updatecount);
        Toast.makeText(getApplicationContext(), "Weather Update: " + mytimestamp + "  Update Count: " + updatecount, Toast.LENGTH_SHORT).show();
    }

/*
public static boolean checknet()  {
        try {
            //check if we can dns forecast.weather.gov
            try {
                Log.i(TAG, "trying to resolve forecast.weather.gov");
                InetAddress Address = InetAddress.getByName("forecast.weather.gov");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.i(TAG, "cant resolve forecast.weather.gov");
                return false;
            }
            try {
                URL url = new URL("http://www.weather.gov/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                try {
                    con.connect();
                    Log.i(TAG, "url connect response code: " + con.getResponseCode());
                    if (con.getResponseCode() == 200){
                        Log.i(TAG, "Connection test passed");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i(TAG, "con.connect() failed");
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception exception) {
                Log.i(TAG, "Connection test failed");
                exception.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            Log.i(TAG, "Connection test exception");
            e.printStackTrace();
            return false;
        }
    return false;
    }
*/


    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Log.i(TAG, "network state = true");
            //do more checking
            try {
                //check if we can dns forecast.weather.gov
                try {
                    Log.i(TAG, "trying to resolve forecast.weather.gov");
                    InetAddress Address = InetAddress.getByName("forecast.weather.gov");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.i(TAG, "cant resolve forecast.weather.gov");
                    return false;
                }
                try {
                    URL url = new URL("https://www.weather.gov/");
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    try {
                        con.connect();
                        Log.i(TAG, "url connect response code: " + con.getResponseCode());
                        if (con.getResponseCode() == 200){
                            Log.i(TAG, "Connection test passed");
                            return true;
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "con.connect() failed");
                        e.printStackTrace();
                        return false;
                    }
                } catch (Exception exception) {
                    Log.i(TAG, "Connection test failed");
                    exception.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                Log.i(TAG, "Connection test exception");
                e.printStackTrace();
                return false;
            }

        } else {
            Log.i(TAG, "network state = false");
            return false;
        }

        return false;
    }



    ///Simple online checker///
    public boolean isOnlineSimple(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Log.i(TAG, "network state = true");
            return true;

        } else {
            Log.i(TAG, "network state = false");
            return false;
        }

    }


/*
    public void checkwear() {
        Wearable.NodeApi.getConnectedNodes(GoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                ///Toast.makeText(getApplicationContext(), "node size: " + nodes.size(), Toast.LENGTH_SHORT).show();
                if (nodes.size() >= 1) {
                    isConnectedToWearable = true;
                } else {
                    isConnectedToWearable = false;
                }
            }
        });
    }
*/

    @Override
    protected void onStart() {
        super.onStart();
        GoogleApiClient.connect();
    }

////those functions cause issues with Alarm Manager!
/*
    @Override
    public void onResume() {
        super.onResume();
        if (GoogleApiClient.isConnected() && mRequestingUpdates) {
            startUpdates();
        }
    }
*/
/*
    @Override
    protected void onPause() {
        super.onPause();
    }
*/
/*
    @Override
    protected void onStop() {
        super.onStop();
        if (GoogleApiClient.isConnected()) {
            GoogleApiClient.disconnect();
        }
    }

*/


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApiClient);
            lat = mCurrentLocation.getLatitude();
            lon = mCurrentLocation.getLongitude();
            mylat = String.valueOf(lat);
            mylon = String.valueOf(lon);
            mygps.setText("lat: " + mylat + " lon:" + mylon);
            //make sure to setup latlon with weather url
            finalurl = url + "lat=" + mylat + "&lon=" + mylon + "&FcstType=json";
            visiturl = url + "lat=" + mylat + "&lon=" + mylon;
            Log.i(TAG, "onConnected finalurl: " + finalurl);
            Log.i(TAG, "onConnected lat: " + mylat + " lon: " + mylon);
        }
        if (mRequestingUpdates) {
            startGPSUpdates();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        lat = mCurrentLocation.getLatitude();
        lon = mCurrentLocation.getLongitude();
        mylat = String.valueOf(lat);
        mylon = String.valueOf(lon);
        mygps.setText("lat: " + mylat + " lon:" + mylon);
        //make sure to setup latlon with weather url
        finalurl = url + "lat=" + mylat + "&lon=" + mylon + "&FcstType=json";
        visiturl = url + "lat=" + mylat + "&lon=" + mylon;
        Log.i(TAG, "onLocationChanged finalurl: " + finalurl);
        Log.i(TAG, "onLocationChanged lat: " + mylat + " lon: " + mylon);

    }


/////end of gps stuff//////

    public void getweather(View view) {
        weatherupdate();
    }


    public void StartService(View view) {
        Intent i = new Intent(this, WeatherService.class);
        this.setIntent(i);

        i.putExtra("clockSize", Integer.parseInt(clockSizeEditText.getText().toString()));
        i.putExtra("clocknosecsSize", Integer.parseInt(clocknosecsSizeEditText.getText().toString()));
        i.putExtra("markerSize", Integer.parseInt(markerSizeEditText.getText().toString()));
        i.putExtra("dateSize", Integer.parseInt(dateSizeEditText.getText().toString()));
        i.putExtra("timeSize", Integer.parseInt(timeSizeEditText.getText().toString()));
        i.putExtra("tempSize", Integer.parseInt(tempSizeEditText.getText().toString()));
        i.putExtra("weatherSize", Integer.parseInt(weatherSizeEditText.getText().toString()));
        i.putExtra("clockAct", clockactCheckBox.isChecked());
        i.putExtra("clockDim", clockdimCheckBox.isChecked());
        i.putExtra("clocknosecsAct", clocknosecsactCheckBox.isChecked());
        i.putExtra("clocknosecsDim", clocknosecsdimCheckBox.isChecked());
        i.putExtra("markerDim", markerCheckBox.isChecked());
        i.putExtra("dateDim", dateCheckBox.isChecked());
        i.putExtra("timeDim", timeCheckBox.isChecked());
        i.putExtra("tempDim", tempCheckBox.isChecked());
        i.putExtra("weatherDim", weatherCheckBox.isChecked());
        i.putExtra("alwaysUtc", alwaysUtcCheckBox.isChecked());
        i.putExtra("showtime", showtimeCheckBox.isChecked());
        i.putExtra("northernhemi", northernhemiCheckBox.isChecked());
        i.putExtra("useShortCards", useShortCardsCheckBox.isChecked());


        /*
        i.putExtra(Settings.KEY_CLOCK_SIZE, Integer.parseInt(clockSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_CLOCK_NOSECS_SIZE, Integer.parseInt(clocknosecsSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_MARKER_SIZE, Integer.parseInt(markerSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_DATE_SIZE, Integer.parseInt(dateSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_TIME_SIZE, Integer.parseInt(timeSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_TEMP_SIZE, Integer.parseInt(tempSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_WEATHER_SIZE, Integer.parseInt(weatherSizeEditText.getText().toString()));
        i.putExtra(Settings.KEY_CLOCK_ACT, clockactCheckBox.isChecked());
        i.putExtra(Settings.KEY_CLOCK_DIM, clockdimCheckBox.isChecked());
        i.putExtra(Settings.KEY_CLOCK_NOSECS_ACT, clocknosecsactCheckBox.isChecked());
        i.putExtra(Settings.KEY_CLOCK_NOSECS_DIM, clocknosecsdimCheckBox.isChecked());
        i.putExtra(Settings.KEY_MARKER_DIM, markerCheckBox.isChecked());
        i.putExtra(Settings.KEY_DATE_DIM, dateCheckBox.isChecked());
        i.putExtra(Settings.KEY_TIME_DIM, timeCheckBox.isChecked());
        i.putExtra(Settings.KEY_TEMP_DIM, tempCheckBox.isChecked());
        i.putExtra(Settings.KEY_WEATHER_DIM, weatherCheckBox.isChecked());
        i.putExtra(Settings.KEY_ALWAYS_UTC, alwaysUtcCheckBox.isChecked());
        i.putExtra(Settings.KEY_SHOW_TIME, showtimeCheckBox.isChecked());
        i.putExtra(Settings.KEY_NORTHERNHEMI, northernhemiCheckBox.isChecked());
        i.putExtra(Settings.KEY_USE_SHORT_CARDS, useShortCardsCheckBox.isChecked());
        */

        startService(i);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    public void StopService(View view) {
        Intent i = new Intent(this, WeatherService.class);
        stopService(i);
    }

    public void visitnws(View view) {

        Uri uri = Uri.parse(visiturl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    ///location and weather updates
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        GoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_UPDATES_KEY, mRequestingUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setButtonsEnabledState() {
        if (mRequestingUpdates) {
            startupdates.setEnabled(false);
            stopupdates.setEnabled(true);
        } else {
            startupdates.setEnabled(true);
            stopupdates.setEnabled(false);
        }
    }
////////////end of gps//////////////////////


    private void showNumberPicker(final EditText view) {
        RelativeLayout layout = new RelativeLayout(this);
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(99);
        numberPicker.setValue(Integer.parseInt(view.getText().toString()));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        layout.setLayoutParams(params);
        layout.addView(numberPicker, numPickerParams);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Font Size");
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                view.setText(String.valueOf(numberPicker.getValue()));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void saveValues() {
        Settings.setInt(this, Settings.KEY_CLOCK_SIZE, Integer.parseInt(clockSizeEditText.getText().toString()));
        Settings.setInt(this, Settings.KEY_CLOCK_NOSECS_SIZE, Integer.parseInt(clocknosecsSizeEditText.getText().toString()));
        Settings.setInt(this, Settings.KEY_MARKER_SIZE, Integer.parseInt(markerSizeEditText.getText().toString()));
        Settings.setInt(this, Settings.KEY_DATE_SIZE, Integer.parseInt(dateSizeEditText.getText().toString()));
        Settings.setInt(this, Settings.KEY_TIME_SIZE, Integer.parseInt(timeSizeEditText.getText().toString()));
        Settings.setInt(this, Settings.KEY_TEMP_SIZE, Integer.parseInt(tempSizeEditText.getText().toString()));
        Settings.setInt(this, Settings.KEY_WEATHER_SIZE, Integer.parseInt(weatherSizeEditText.getText().toString()));
        Settings.setBoolean(this, Settings.KEY_CLOCK_ACT, clockactCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_CLOCK_DIM, clockdimCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_CLOCK_NOSECS_ACT, clocknosecsactCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_CLOCK_NOSECS_DIM, clocknosecsdimCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_MARKER_DIM, markerCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_DATE_DIM, dateCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_TIME_DIM, timeCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_TEMP_DIM, tempCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_WEATHER_DIM, weatherCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_ALWAYS_UTC, alwaysUtcCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_SHOW_TIME, showtimeCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_NORTHERNHEMI, northernhemiCheckBox.isChecked());
        Settings.setBoolean(this, Settings.KEY_USE_SHORT_CARDS, useShortCardsCheckBox.isChecked());
        Settings.setString(this, Settings.KEY_LAT, mylat);
        Settings.setString(this, Settings.KEY_LON, mylon);
        Settings.setString(this, Settings.KEY_TEMP, mytemp);
        Settings.setString(this, Settings.KEY_ICON, finalicon);
        Settings.setString(this, Settings.KEY_WEATHER, myweather);
    }

    private void loadValues() {
        clockSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_CLOCK_SIZE, Settings.KEY_CLOCK_SIZE_DEF)));
        clocknosecsSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_CLOCK_NOSECS_SIZE, Settings.KEY_CLOCK_NOSECS_SIZE_DEF)));
        markerSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_MARKER_SIZE, Settings.KEY_MARKER_SIZE_DEF)));
        dateSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_DATE_SIZE, Settings.KEY_DATE_SIZE_DEF)));
        timeSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_TIME_SIZE, Settings.KEY_TIME_SIZE_DEF)));
        tempSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_TEMP_SIZE, Settings.KEY_TEMP_SIZE_DEF)));
        weatherSizeEditText.setText(String.valueOf(Settings.getInt(this, Settings.KEY_WEATHER_SIZE, Settings.KEY_WEATHER_SIZE_DEF)));
        clockactCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_CLOCK_ACT, Settings.KEY_CLOCK_ACT_DEF));
        clockdimCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_CLOCK_DIM, Settings.KEY_CLOCK_DIM_DEF));
        clocknosecsactCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_CLOCK_NOSECS_ACT, Settings.KEY_CLOCK_NOSECS_ACT_DEF));
        clocknosecsdimCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_CLOCK_NOSECS_DIM, Settings.KEY_CLOCK_NOSECS_DIM_DEF));
        markerCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_MARKER_DIM, Settings.KEY_MARKER_DIM_DEF));
        dateCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_DATE_DIM, Settings.KEY_DATE_DIM_DEF));
        timeCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_TIME_DIM, Settings.KEY_TIME_DIM_DEF));
        tempCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_TEMP_DIM, Settings.KEY_TEMP_DIM_DEF));
        weatherCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_WEATHER_DIM, Settings.KEY_WEATHER_DIM_DEF));
        alwaysUtcCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_ALWAYS_UTC, Settings.KEY_ALWAYS_UTC_DEF));
        showtimeCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_SHOW_TIME, Settings.KEY_SHOW_TIME_DEF));
        northernhemiCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_NORTHERNHEMI, Settings.KEY_NORTHERNHEMI_DEF));
        useShortCardsCheckBox.setChecked(Settings.getBoolean(this, Settings.KEY_USE_SHORT_CARDS, Settings.KEY_USE_SHORT_CARDS_DEF));

    }


    private void resetValues() {
        Settings.resetAllPrefs(this);
        loadValues();
    }

    public void pushValuesToWearable() {


        clockSize = Integer.parseInt(clockSizeEditText.getText().toString());
        clocknosecsSize = Integer.parseInt(clocknosecsSizeEditText.getText().toString());
        markerSize = Integer.parseInt(markerSizeEditText.getText().toString());
        dateSize = Integer.parseInt(dateSizeEditText.getText().toString());
        timeSize = Integer.parseInt(timeSizeEditText.getText().toString());
        tempSize = Integer.parseInt(tempSizeEditText.getText().toString());
        weatherSize = Integer.parseInt(weatherSizeEditText.getText().toString());

        clockAct = clockactCheckBox.isChecked();
        clockDim = clockdimCheckBox.isChecked();
        clocknosecsAct = clocknosecsactCheckBox.isChecked();
        clocknosecsDim = clocknosecsdimCheckBox.isChecked();
        markerDim = markerCheckBox.isChecked();
        dateDim = dateCheckBox.isChecked();
        timeDim = timeCheckBox.isChecked();
        tempDim = tempCheckBox.isChecked();
        weatherDim = weatherCheckBox.isChecked();
        alwaysUtc = alwaysUtcCheckBox.isChecked();
        showtime = showtimeCheckBox.isChecked();
        northernhemi = northernhemiCheckBox.isChecked();
        useShortCards = useShortCardsCheckBox.isChecked();

        PutDataMapRequest dataMap = PutDataMapRequest.create(Settings.PATH_WITH_FEATURE);

        dataMap.getDataMap().putInt(Settings.KEY_CLOCK_SIZE, clockSize);
        dataMap.getDataMap().putInt(Settings.KEY_CLOCK_NOSECS_SIZE, clocknosecsSize);
        dataMap.getDataMap().putInt(Settings.KEY_MARKER_SIZE, markerSize);
        dataMap.getDataMap().putInt(Settings.KEY_DATE_SIZE, dateSize);
        dataMap.getDataMap().putInt(Settings.KEY_TIME_SIZE, timeSize);
        dataMap.getDataMap().putInt(Settings.KEY_TEMP_SIZE, tempSize);
        dataMap.getDataMap().putInt(Settings.KEY_WEATHER_SIZE, weatherSize);
        dataMap.getDataMap().putBoolean(Settings.KEY_CLOCK_ACT, clockAct);
        dataMap.getDataMap().putBoolean(Settings.KEY_CLOCK_DIM, clockDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_CLOCK_NOSECS_ACT, clocknosecsAct);
        dataMap.getDataMap().putBoolean(Settings.KEY_CLOCK_NOSECS_DIM, clocknosecsDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_MARKER_DIM, markerDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_DATE_DIM, dateDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_TIME_DIM, timeDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_TEMP_DIM, tempDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_WEATHER_DIM, weatherDim);
        dataMap.getDataMap().putBoolean(Settings.KEY_ALWAYS_UTC, alwaysUtc);
        dataMap.getDataMap().putBoolean(Settings.KEY_SHOW_TIME, showtime);
        dataMap.getDataMap().putBoolean(Settings.KEY_NORTHERNHEMI, northernhemi);
        dataMap.getDataMap().putBoolean(Settings.KEY_USE_SHORT_CARDS, useShortCards);
        dataMap.getDataMap().putString(Settings.KEY_LAT, mylat);
        dataMap.getDataMap().putString(Settings.KEY_LON, mylon);
        dataMap.getDataMap().putString(Settings.KEY_TEMP, mytemp);
        dataMap.getDataMap().putString(Settings.KEY_ICON, finalicon);
        dataMap.getDataMap().putString(Settings.KEY_WEATHER, myweather);

        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(GoogleApiClient, request);
    }


    private class startlogging extends AsyncTask {

        @Override
        protected Object doInBackground(Object... arg0) {
            logging();
            return null;
        }
    }

    void logging() {

        //start logging logcat
        try {
            Log.i(TAG, "logcat started..................");
            Process process = Runtime.getRuntime().exec("logcat");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line);
            }

            final String logString = new String(log.toString());

            //create text file in SDCard
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/catlogs");
            dir.mkdirs();
            File file = new File(dir, "watchface_text.txt");

            try {
                //to write logcat in text file
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);

                // Write the string to the file
                osw.write(logString);
                osw.flush();
                osw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            Log.i(TAG, "logcat crashed..................");
            e.printStackTrace();
        }


    }



}
