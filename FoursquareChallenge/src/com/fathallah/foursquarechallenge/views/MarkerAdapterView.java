package com.fathallah.foursquarechallenge.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class MarkerAdapterView implements InfoWindowAdapter {

	LayoutInflater inflater = null;

	private Context context;

	public MarkerAdapterView(Context context) {
		this.inflater = inflater;
		this.context = context;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return (null);
	}

	@Override
	public View getInfoContents(Marker marker) {
		// View popup=inflater.inflate(R.layout.popup, null);

		// TextView tv=(TextView)popup.findViewById(R.id.title);

		TextView titleTextView = new TextView(context);
		titleTextView.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		titleTextView.setText(marker.getTitle());

		return (titleTextView);
	}
}
