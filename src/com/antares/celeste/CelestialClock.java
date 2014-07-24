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

/**
 * 
 * @author dels
 *
 * this app tells an astronomical observer the current solar and sidereal time
 * which simplifies the task of calculating Hour Angle to point a telescope
 * 
 * TODO: initialise the location using Android location services, convert to an address
 * TODO: let the user specify a location other than the current place
 * TODO: let the user set a fixed time and date instead of clock time
 * TODO: add an activity that enables the user to calculate Hour Angle from RA
 * TODO: ensure that the device is useful with no network and GPS off
 * 
 */
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
	private double longitude = 145.0;
	private long solarOffset = (long)(240000 * longitude);   // seconds to make solar time from UTC

/*** javascript code
var epochHour = 262980;     // 1.Jan.2000 12:00 UT as Unix time in hours.
var epochST = 6.697374558   // GMST at epoch Time
var deltaT = 0.002737909350795;
function getLAST(d,l){

     * Local Apparent Sidereal Time (for Date 'd' at Longitude 'l')
     * ref: http://aa.usno.navy.mil/faq/docs/GAST.php
     *
     * Given below is a simple algorithm for computing apparent sidereal time
     * to an accuracy of about 0.1 second, equivalent to about 1.5 arcseconds of sky.
     *
     * The algorithm uses an epoch of Jan 1, 2000 12:00 UT
     *
     * It derives two values representing the current time
     *      epDays  is the number of (mean solar) days from the epoch to 0000 UT of the current day
     *              (always has a fraction of .5)
     *      utHours is the number of hours since 0000 UT
     *
     * Then the Greenwich mean sidereal time in hours is
     *      GMST = 6.697374558 + 0.06570982441908 x epDays + 1.00273790935 x utHours
     * Local Apparent Sidereal Time is found by adding 4 minutes per degree of East longitude (1 hour per 15 degrees)
     *      LAST = GMST + longitude / 15
     * A modulus operation is applied to yield : 0.0 <= LAST < 24.0
     *
     * In simple terms,
     *      epochHour = 262980;         // Epoch time (1.Jan.2000 12:00 UT) as Unix time in hours.
     *      epochST = 6.697374558       // GMST at epoch Time
     *      deltaT = 0.002737909350795; // ( Sidereal hour - Solar Hour) (as Sidereal hours)
     * then
     *      let nowHour be the current UT time in hours
     *      GMST = epochST + (nowHour-epochHour)*deltaT + nowHour modulo 24
     *      LAST = (GMST + longitude/15) modulo 24

     nowHour = d.getTime()/3600000.;     // current Unix UT time in hours
     LAST = ( epochST + deltaT*(nowHour - epochHour) + nowHour + l/15 ) % 24;
     // Format this as HH:MM:SS
     r = new Date(LAST*3600000);
     return r.toUTCString().substr(17,8);
*/
	private long sidereal( long utcMilliseconds, double longitude ) {
		// compute sidereal time at the given longitude

		double epochTime = 262980;
		double epochSidTime = 6.697374558;
		double deltaT = 0.002737909350795;
		double now = (double)utcMilliseconds / 3600000.;
		
		return (long) (3600000 * ((epochSidTime + deltaT*(now - epochTime) + now + longitude/15.) % 24.));
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
