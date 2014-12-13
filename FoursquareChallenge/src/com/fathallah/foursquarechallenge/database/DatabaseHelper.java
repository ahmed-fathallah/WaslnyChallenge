package com.fathallah.foursquarechallenge.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fathallah.foursquarechallenge.model.Place;

public class DatabaseHelper extends SQLiteOpenHelper {

	// Database Version
	public static final int DATABASE_VERSION = 1;

	// Database Name
	public static final String DATABASE_NAME = "venues.db";

	// Table Names
	private static final String PLACES_TABLE = "places";

	// columns Names
	private static final String _ID = "id";
	private static final String VENUE_ID = "venue_id";
	private static final String VENUE_NAME = "name";
	private static final String LAT = "lat";
	private static final String Lng = "lng";
	private static final String IMAGE_URL = "imageUrl";

	private static final String CREATE_TABLE_PLACES = "CREATE TABLE "
			+ PLACES_TABLE + " ( " + _ID + " INTEGER PRIMARY KEY," + VENUE_ID
			+ " TEXT," + VENUE_NAME + " TEXT," + LAT + " INTEGER," + Lng
			+ " INTEGER," + IMAGE_URL + " TEXT" + " )";

	private SQLiteDatabase database;

	public DatabaseHelper(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		database = db;
		db.execSQL(CREATE_TABLE_PLACES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + PLACES_TABLE);
		onCreate(db);
	}

	public void deleteAllVenues() {
		database = getWritableDatabase();
		database.execSQL("delete from " + PLACES_TABLE);
		database.close();
	}

	public ArrayList<Place> getAllVenues() {
		ArrayList<Place> places = new ArrayList<>();
		database = getReadableDatabase();
		Cursor cursor = database
				.rawQuery("Select * from " + PLACES_TABLE, null);
		if (cursor.moveToFirst()) {
			do {
				Place place = new Place();
				place.setName(cursor.getString(cursor
						.getColumnIndex(VENUE_NAME)));
				place.setVenueId(cursor.getString(cursor
						.getColumnIndex(VENUE_ID)));
				place.setLatitude(cursor.getDouble(cursor.getColumnIndex(LAT)));
				place.setLongitude(cursor.getDouble(cursor.getColumnIndex(Lng)));
				place.setImageUrl(cursor.getString(cursor
						.getColumnIndex(IMAGE_URL)));
				places.add(place);
			} while (cursor.moveToNext());
			cursor.close();
		}
		database.close();
		return places;
	}

	public void insertAllVenues(ArrayList<Place> places) {
		this.database = getWritableDatabase();
		ContentValues values = new ContentValues();
		for (int i = 0; i < places.size(); i++) {

			values.put(IMAGE_URL, places.get(i).getImageUrl());
			values.put(LAT, places.get(i).getLatitude());
			values.put(Lng, places.get(i).getLongitude());
			values.put(VENUE_ID, places.get(i).getVenueId());
			values.put(VENUE_NAME, places.get(i).getName());

			this.database.insert(PLACES_TABLE, null, values);
		}

		this.database.close();
	}

}
