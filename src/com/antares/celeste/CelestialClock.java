package com.antares.celeste;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;  
import java.util.TimerTask;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
/*
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import android.text.format.Time;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.ProgressBar;
*/

@SuppressLint("SimpleDateFormat")
public class CelestialClock extends Activity {

	// A request to connect to Location Services
	// private LocationRequest mLocationRequest;

	// Stores the current instantiation of the location client in this object
	// private LocationClient mLocationClient;

	// constants
	private long MSEC_PER_DAY = 86400000;
	
	// Handles to UI widgets
	protected TextView locationText;
	private TextView localTimeText;
	private TextView sidTimeText;
	private TextView utcTimeText;
	private TextView solarTimeText;
	private long tzOffset;
	private double longitude = 144.97;
	private long solarOffset = (long)(240000 * longitude);   // seconds to make solar time from UTC

	private long sidereal( long utcMilliseconds, double longitude ) {
		// compute sidereal time at the given longitude
		long day = 205;
		long utcShift = (long) ((366.25/365.25) * ((day-264) % 365 ) * MSEC_PER_DAY);
		long sid = (utcMilliseconds + utcShift + solarOffset) % MSEC_PER_DAY;
		return sid;
	}
	
	@SuppressLint("DefaultLocale")
	private String timeString( long ms ) {
		long h = ms / 3600000;
		ms %= 3600000;
		long m = ms / 60000;
		long s = (ms % 60000) / 1000;
		return String.format("%02d:%02d:%02d",h,m,s);
	}
	
	// hooks for a timer-update event
	Timer t = new Timer();     
	TimerTask tTask = new TimerTask() {		  
		@Override  
		public void run() {  
			runOnUiThread(new Runnable() {  
				@Override  
				public void run() {
					Date now = new Date();
					long utcTime = now.getTime() % MSEC_PER_DAY;	// UTC time of day in seconds
					long localTime = (utcTime + tzOffset) % MSEC_PER_DAY;	// Local time in seconds
					long solarTime = (utcTime + solarOffset) % MSEC_PER_DAY;
					long sidTime = sidereal(now.getTime(),longitude);
					
					// update the display text
					localTimeText.setText(timeString(localTime));
					utcTimeText.setText(timeString(utcTime));
					sidTimeText.setText(timeString(sidTime));
					solarTimeText.setText(timeString(solarTime));
				}  
			});  
		}  
	};  


	// Handle to SharedPreferences for this app
	// SharedPreferences mPrefs;

	// Handle to a SharedPreferences editor
	// SharedPreferences.Editor mEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_celestial_clock);

		// get handles for mutable UI fields
		locationText = (TextView) findViewById(R.id.locationText);
		localTimeText = (TextView) findViewById(R.id.localTimeText);
		sidTimeText = (TextView) findViewById(R.id.sidTimeText);
		utcTimeText = (TextView) findViewById(R.id.utcTimeText);
		solarTimeText = (TextView) findViewById(R.id.solarTimeText);
		GregorianCalendar c = new GregorianCalendar();
		tzOffset = c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);  // milliseconds added to UTC		

		// Create a new global location parameters object
		// mLocationRequest = LocationRequest.create();

		// start a timer at 1-second intervals
		t.scheduleAtFixedRate(tTask, 0, 1000);  

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.celestial_clock, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);

	}
}
