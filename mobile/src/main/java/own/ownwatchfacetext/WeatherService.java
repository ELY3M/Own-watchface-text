package own.ownwatchfacetext;


import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WeatherService  extends Service implements ConnectionCallbacks, OnConnectionFailedListener {


	private static final String TAG = "ownwatchface WeatherService";



	private GoogleApiClient GoogleApiClient;
	private LocationManager mLocationManager;
	private Location        mLocation;


	private double lat = 0.0;
	private double lon = 0.0;
	private String mylat = "0.0";
	private String mylon = "0.0";
	private String mytemp = "0°F";
	private String myicon = "unknown";
	private String finalicon = "unknown";
	private String myweather = "unknown";
	private int updatecount = 0;
	private String url = "https://forecast.weather.gov/MapClick.php?";
	private String finalurl = "setup";
	private String visiturl = "setup";


	//TODO
	//settings to be global//
	int clockSize = 0;
	int clocknosecsSize = 0;
	int markerSize = 0;
	int dateSize = 0;
	int timeSize = 0;
	int tempSize = 0;
	int weatherSize = 0;
	boolean clockAct = true;
	boolean clockDim = false;
	boolean clocknosecsAct = false;
	boolean clocknosecsDim = true;
	boolean markerDim = true;
	boolean dateDim = true;
	boolean timeDim = false;
	boolean tempDim = false;
	boolean weatherDim = false;
	boolean alwaysUtc = true;
	boolean showtime = false;
	boolean northernhemi = true;
	boolean useShortCards = true;



	//int interval = 30000;
	//int interval = 90000;
	int interval = 1800000;
	private Context context;


	private Handler handler = new Handler();
	private Runnable runn = new Runnable() {
		@Override
		public void run() {

			Log.i(TAG, "handler run...");
			startTask();
			handler.postDelayed(runn, interval);
		}
	};
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(handler!=null){
			handler.removeCallbacks(runn);
		}
		handler = null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = this;


		clockSize = intent.getIntExtra("clockSize", clockSize);
		clocknosecsSize = intent.getIntExtra("clocknosecsSize", clocknosecsSize);
		markerSize = intent.getIntExtra("markerSize", markerSize);
		dateSize = intent.getIntExtra("dateSize", dateSize);
		timeSize = intent.getIntExtra("timeSize", timeSize);
		tempSize = intent.getIntExtra("tempSize", tempSize);
		weatherSize = intent.getIntExtra("weatherSize", weatherSize);
		clockAct = intent.getBooleanExtra("clockAct", clockAct);
		clockDim = intent.getBooleanExtra("clockDim", clockDim);
		clocknosecsAct = intent.getBooleanExtra("clocknosecsAct", clocknosecsAct);
		clocknosecsDim = intent.getBooleanExtra("clocknosecsDim", clocknosecsDim);
		markerDim = intent.getBooleanExtra("markerDim", markerDim);
		dateDim = intent.getBooleanExtra("dateDim", dateDim);
		timeDim = intent.getBooleanExtra("timeDim", timeDim);
		tempDim = intent.getBooleanExtra("tempDim", tempDim);
		weatherDim = intent.getBooleanExtra("weatherDim", weatherDim);
		alwaysUtc = intent.getBooleanExtra("alwaysUtc", alwaysUtc);
		showtime = intent.getBooleanExtra("showtime", showtime);
		northernhemi = intent.getBooleanExtra("northernhemi", northernhemi);
		useShortCards = intent.getBooleanExtra("useShortCards", useShortCards);


			handler.post(runn);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
			//createLocationRequest();
		}
	}

	private void startTask()
	{
		Log.i(TAG, "Start Weather AsyncTask" );

		///mGoogleApiClient = new GoogleApiClient.Builder( this ).addApi( Wearable.API ).build();

		mLocationManager = (LocationManager) MainActivity.getMain().getSystemService(Context.LOCATION_SERVICE);
		mLocation = mLocationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );

		if ( mLocation == null )
		{
			mLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
						@Override
					public void onLocationChanged( Location location )
					{
						Log.i( TAG, "onLocationChanged: " + location );
						mLocationManager.removeUpdates( this );
						mLocation = location;
						Task task = new Task();
						task.execute();
					}

					@Override
					public void onStatusChanged( String provider, int status, Bundle extras )
					{

					}

					@Override
					public void onProviderEnabled( String provider )
					{

					}

					@Override
					public void onProviderDisabled( String provider )
					{

					}
				}
			);
		}
		else
		{
			Task task = new Task();
			task.execute();
		}
	}

	private class Task extends AsyncTask
	{

		handlejson obj;

		@Override
		protected Object doInBackground( Object[] params )
		{
			try
			{
				Log.i( TAG, "Task Running" );


				initializeGoogleAPI();

				finalurl = url + "&lat=" + mLocation.getLatitude() + "&lon=" + mLocation.getLongitude() + "&FcstType=json";
				Log.i(TAG, "finalurl: " + finalurl);

				obj = new handlejson(finalurl);
				obj.fetchJSON();
				while (obj.parsingComplete);

				mytemp = obj.getTemp() + "°F";
				myweather = obj.getWeather();

				if(myweather.isEmpty()) {
					Log.i(TAG, "myweather is null!");
					Log.i(TAG, "setting myweather to unknown");
					myweather = "unknown";
				}



				Log.i(TAG, "mytemp: " + mytemp);
				Log.i(TAG, "myweather: " + myweather);

				//setting up icon name//
				Pattern pattern = Pattern.compile("(.*?)(.png|.jpg|.gif)");
				Matcher geticon = pattern.matcher(obj.getIcon());
				while (geticon.find()) {
					finalicon = geticon.group(1);
				}
				myicon = finalicon;
				Log.i(TAG, "obj.getIcon: " + obj.getIcon());
				Log.i(TAG, "myicon: " + myicon);


				//send to watch
				if (!GoogleApiClient.isConnected() )
				{GoogleApiClient.connect(); }

				pushValuesToWearable();

				//logging
				SimpleDateFormat timestamp = new SimpleDateFormat("EEE M-d-yy h:mm:ss a");
				Calendar c = Calendar.getInstance();
				String mytimestamp = timestamp.format(c.getTime());
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


			}
			catch ( Exception e )
			{
				Log.i(TAG, "Task Fail: " + e);
			}
			return null;
		}
	}


	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Connected to GoogleApiClient");

		if (mLocation == null) {
			mLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleApiClient);
			lat = mLocation.getLatitude();
			lon = mLocation.getLongitude();
			mylat = String.valueOf(lat);
			mylon = String.valueOf(lon);
			//MainActivity.getMain().mygps.setText("lat: " + mylat + " lon:" + mylon);
			//make sure to setup latlon with weather url
			finalurl = url + "lat=" + mylat + "&lon=" + mylon + "&FcstType=json";
			visiturl = url + "lat=" + mylat + "&lon=" + mylon;
			Log.i(TAG, "onConnected finalurl: " + finalurl);
			Log.i(TAG, "onConnected lat: " + mylat + " lon: " + mylon);

		}
	}



	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "Connection suspended");
		GoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}


	public void getValuesToWearable() {



		PutDataMapRequest dataMap = PutDataMapRequest.create(Settings.PATH_WITH_FEATURE);
		dataMap.getDataMap().getInt(Settings.KEY_CLOCK_SIZE, clockSize);
		dataMap.getDataMap().getInt(Settings.KEY_CLOCK_NOSECS_SIZE, clocknosecsSize);
		dataMap.getDataMap().getInt(Settings.KEY_MARKER_SIZE, markerSize);
		dataMap.getDataMap().getInt(Settings.KEY_DATE_SIZE, dateSize);
		dataMap.getDataMap().getInt(Settings.KEY_TIME_SIZE, timeSize);
		dataMap.getDataMap().getInt(Settings.KEY_TEMP_SIZE, tempSize);
		dataMap.getDataMap().getInt(Settings.KEY_WEATHER_SIZE, weatherSize);
		dataMap.getDataMap().getBoolean(Settings.KEY_CLOCK_ACT, clockAct);
		dataMap.getDataMap().getBoolean(Settings.KEY_CLOCK_DIM, clockDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_CLOCK_NOSECS_ACT, clocknosecsAct);
		dataMap.getDataMap().getBoolean(Settings.KEY_CLOCK_NOSECS_DIM, clocknosecsDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_MARKER_DIM, markerDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_DATE_DIM, dateDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_TIME_DIM, timeDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_TEMP_DIM, tempDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_WEATHER_DIM, weatherDim);
		dataMap.getDataMap().getBoolean(Settings.KEY_ALWAYS_UTC, alwaysUtc);
		dataMap.getDataMap().getBoolean(Settings.KEY_SHOW_TIME, showtime);
		dataMap.getDataMap().getBoolean(Settings.KEY_NORTHERNHEMI, northernhemi);
		dataMap.getDataMap().getBoolean(Settings.KEY_USE_SHORT_CARDS, useShortCards);

		Log.d(TAG, "clockSize: "+clockSize);
		Log.d(TAG, "clocknosecsSize: "+clocknosecsSize);

		Log.d(TAG, "showtime: "+showtime);


		/*
		dataMap.getDataMap().getString(Settings.KEY_LAT, mylat);
		dataMap.getDataMap().getString(Settings.KEY_LON, mylon);
		dataMap.getDataMap().getString(Settings.KEY_TEMP, mytemp);
		dataMap.getDataMap().getString(Settings.KEY_ICON, finalicon);
		dataMap.getDataMap().getString(Settings.KEY_WEATHER, myweather);
		*/

		PutDataRequest request = dataMap.asPutDataRequest();
		PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(GoogleApiClient, request);
	}



	public void pushValuesToWearable() {



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




}

