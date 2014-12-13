package com.fathallah.foursquarechallenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;
import br.com.condesales.EasyFoursquareAsync;
import br.com.condesales.criterias.CheckInCriteria;
import br.com.condesales.criterias.VenuesCriteria;
import br.com.condesales.listeners.AccessTokenRequestListener;
import br.com.condesales.listeners.CheckInListener;
import br.com.condesales.listeners.FoursquareVenuesRequestListener;
import br.com.condesales.models.Checkin;
import br.com.condesales.models.Icon;
import br.com.condesales.models.Venue;

import com.fathallah.foursquarechallenge.database.DatabaseHelper;
import com.fathallah.foursquarechallenge.model.Place;
import com.fathallah.foursquarechallenge.utils.Utilities;
import com.fathallah.foursquarechallenge.views.MarkerAdapterView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MapActivity extends ActionBarActivity implements LocationListener,
		AccessTokenRequestListener, GoogleMap.OnInfoWindowClickListener {

	private GoogleMap sGoogleMap;
	private LocationManager sLocationManager;
	private EasyFoursquareAsync async;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1 * 1000;

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 60 * 60 * 1000;

	private DatabaseHelper sDatabaseHelper;

	private Map<Marker, Place> markerdata;
	private String accessToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		getSupportActionBar().hide();

		initialize();

		// TODO: remove this work around and replace it with background process

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		if (Utilities.checkNetworkConnection(this))
			loadMap();
		else
			Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG)
					.show();

	}

	private void initialize() {
		sGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		sDatabaseHelper = new DatabaseHelper(this);
		Utilities.initializeImageLoader(this);
		markerdata = new HashMap<>();
		async = new EasyFoursquareAsync(this);
		sGoogleMap.setOnInfoWindowClickListener(this);
	}

	private void checkin(String venueID) {

		CheckInCriteria criteria = new CheckInCriteria();
		criteria.setBroadcast(CheckInCriteria.BroadCastType.PUBLIC);
		criteria.setVenueId(venueID);

		async.checkIn(new CheckInListener() {
			@Override
			public void onCheckInDone(Checkin checkin) {
				Toast.makeText(MapActivity.this, R.string.checkin_complete,
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(MapActivity.this, R.string.error,
						Toast.LENGTH_LONG).show();
			}
		}, criteria);
	}

	private void getLocation() {
		sLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = null;

		Log.d("Network", "Network");
		if (sLocationManager != null) {

			if (sLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				sLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
						MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

			if (sLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER))
				sLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
						MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

			location = sLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location == null) {
				location = sLocationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if (location == null) {
				Utilities.showSettingsAlert(this);
			} else {
				sGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
						location.getLatitude(), location.getLongitude())));
				sGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000,
						null);
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Utilities.checkNetworkConnection(this))
			getLocation();
		else
			Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG)
					.show();

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sLocationManager != null)
			sLocationManager.removeUpdates(this);
	}

	private void loadMap() {
		ArrayList<Place> places = null;
		if (sGoogleMap != null) {
			sGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			sGoogleMap.setMyLocationEnabled(true);
			places = sDatabaseHelper.getAllVenues();
			if (places != null && places.size() != 0) {
				AddMarkers(places);
			}

		}

	}

	@Override
	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		sGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in, animating the camera.
		sGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
		getVenusFromBackend(location);

	}

	private void getVenusFromBackend(Location location) {

		VenuesCriteria criteria = new VenuesCriteria();
		criteria.setLocation(location);
		criteria.setQuantity(50);

		async.getVenuesNearby(new FoursquareVenuesRequestListener() {

			@Override
			public void onError(String errorMsg) {
				Toast.makeText(MapActivity.this, errorMsg, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onVenuesFetched(ArrayList<Venue> venues) {
				ArrayList<Place> places = new ArrayList<Place>();
				Place place = null;
				for (int i = 0; i < venues.size(); i++) {
					place = new Place();
					place.setName(venues.get(i).getName());
					place.setLatitude(venues.get(i).getLocation().getLat());
					place.setLongitude(venues.get(i).getLocation().getLng());
					place.setVenueId(venues.get(i).getId());
					if (venues.get(i).getCategories() != null
							&& (venues.get(i).getCategories().size() != 0)) {
						Icon icon = venues.get(i).getCategories().get(0)
								.getIcon();
						place.setImageUrl(icon.getPrefix() + "bg_88"
								+ icon.getSuffix());
					} else {
						place.setImageUrl(null);
					}
					places.add(place);
				}
				venues.clear();
				venues = null;
				if (sDatabaseHelper != null) {
					sDatabaseHelper.deleteAllVenues();
					sDatabaseHelper.insertAllVenues(places);
				}
				clearMarkers();
				AddMarkers(places);
			}
		}, criteria);

	}

	private void AddMarkers(ArrayList<Place> places) {

		for (int i = 0; i < places.size(); i++) {
			MarkerOptions options = new MarkerOptions().title(
					places.get(i).getName()).position(
					new LatLng(places.get(i).getLatitude(), places.get(i)
							.getLongitude()));
			if (places.get(i).getImageUrl() != null) {
				options.icon(BitmapDescriptorFactory.fromBitmap(ImageLoader
						.getInstance().loadImageSync(
								places.get(i).getImageUrl())));
			}
			MarkerAdapterView adapterView = new MarkerAdapterView(
					MapActivity.this);
			sGoogleMap.setInfoWindowAdapter(adapterView);
			markerdata.put(sGoogleMap.addMarker(options), places.get(i));
		}

	}

	private void clearMarkers() {
		sGoogleMap.clear();
		markerdata.clear();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(MapActivity.this, "Provider " + provider + " enabled!",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(MapActivity.this, "Provider " + provider + " disabled!",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onError(String errorMsg) {
		Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAccessGrant(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		sGoogleMap = null;
		sLocationManager = null;
		sDatabaseHelper = null;
		markerdata = null;
		async = null;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		if (this.accessToken != null && this.accessToken.trim() != "") {
			String venueId = "";
			if (markerdata.containsKey(marker)) {
				venueId = markerdata.get(marker).getVenueId();

				checkin(venueId);
			}

		} else {
			async.requestAccess(this);
		}
	}
}
