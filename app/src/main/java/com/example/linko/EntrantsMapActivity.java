package com.example.linko;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.util.Map;

public class EntrantsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String eventId;
    private GoogleMap mMap;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private Event currentEvent;   // we’ll store the event here

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants_map);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());   // back to OrganizerEventDetailsActivity

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        listenForEntrantLocations();
    }

    private void listenForEntrantLocations() {
        // Listen to changes on this event document
        eventsRef.document(eventId)
                .addSnapshotListener((@Nullable DocumentSnapshot snapshot,
                                      @Nullable FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load entrant locations", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentEvent = snapshot.toObject(Event.class);
                    if (currentEvent == null) return;

                    if (mMap != null) {
                        drawMarkers();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        drawMarkers();
    }

    private void drawMarkers() {
        if (mMap == null || currentEvent == null) return;

        mMap.clear();
        LatLng first = null;

        // Firestore stores entrantLocations as Map<String, GeoPoint>
        Map<String, GeoPoint> entrantLocations = currentEvent.getEntrantLocations();
        if (entrantLocations == null || entrantLocations.isEmpty()) {
            Toast.makeText(this, "No entrant locations to display", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Map.Entry<String, GeoPoint> entry : entrantLocations.entrySet()) {
            String userId = entry.getKey();
            GeoPoint gp = entry.getValue();
            if (gp == null) continue;

            double lat = gp.getLatitude();
            double lng = gp.getLongitude();

            LatLng pos = new LatLng(lat, lng);
            if (first == null) first = pos;

            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("Entrant: " + userId));  // later you can resolve userId → name
        }

        if (first != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, 10f));
        } else {
            Toast.makeText(this, "No valid locations to display", Toast.LENGTH_SHORT).show();
        }
    }

}
