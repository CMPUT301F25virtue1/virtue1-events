package com.example.linko;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrants_map);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        ImageView backButton = findViewById(R.id.button_back_button);
        backButton.setOnClickListener(v -> finish());   // back to OrganizerEventDetailsActivity

        // https://developers.google.com/maps/documentation/android-sdk/map#maps_android_on_map_ready_callback-java
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        drawMarkers();
    }

    private void drawMarkers() {
        // Listen to changes on this event document
        new EventDatabaseHandler().fetchEventById(eventId, new EventDatabaseHandler.EventFetched() {
            @Override
            public void eventFetch(Event event) {
                currentEvent = event;
                Map<String, GeoPoint> entrantLocations = currentEvent.getEntrantLocations();

                for (String entrantId : event.getEntrants()) {
                    new UserDatabaseHandler().fetchUserById(entrantId, new UserDatabaseHandler.UserFetchedFromId() {
                        @Override
                        public void userFetch(User user) {
                            GeoPoint gp = entrantLocations.get(user.getUserId());

                            double lat = gp.getLatitude();
                            double lng = gp.getLongitude();

                            LatLng pos = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title("Entrant: " + user.getFirstName() + " " + user.getLastName()));  // later you can resolve userId → name
                        }

                        @Override
                        public void userFetchFailed(Exception e) {

                        }
                    });
                }
            }

            @Override
            public void eventFetchFailed(Exception e) {

            }
        });
    }

}
