package com.example.memorymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.SearchException;
import com.tomtom.online.sdk.search.fuzzy.FuzzyOutcome;
import com.tomtom.online.sdk.search.fuzzy.FuzzyOutcomeCallback;
import com.tomtom.online.sdk.search.fuzzy.FuzzySearchSpecification;

import org.jetbrains.annotations.NotNull;

public class CreateActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private double latitude;
    private double longitude;
    private String loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        //set up action, to main activity
        Toolbar toolbar = (Toolbar)findViewById(R.id.create_toolbar);
        setSupportActionBar(toolbar);

        //map that shows new location selected
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    public void showLocation(View view) {
        gMap.clear();

        SearchApi searchApi = OnlineSearchApi.create(this, BuildConfig.TOMTOM_KEY);

        //fuzzy search output
        EditText input = findViewById(R.id.newLoc_editText);
        loc = input.getText().toString();

        FuzzySearchSpecification searchQuery = new FuzzySearchSpecification.Builder(loc).build();

        searchApi.search(searchQuery, new FuzzyOutcomeCallback(){
            @Override
            public void onSuccess(@NotNull FuzzyOutcome fuzzyOutcome) {
                //outputs first and most accurate
                /* latitude = fuzzyOutcome.getFuzzyDetailsList().get(0).getPosition().getLatitude();
                longitude = fuzzyOutcome.getFuzzyDetailsList().get(0).getPosition().getLongitude();
                gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(loc));
                gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude))); */

                Spinner spinner = findViewById(R.id.spinner);

                // add first ten matches to a dropdown
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                int limit = 10;
                if (fuzzyOutcome.getFuzzyDetailsList().size() < 10){
                    limit = fuzzyOutcome.getFuzzyDetailsList().size();
                }

                for (int i=0; i<limit; i++){
                    String street = fuzzyOutcome.getFuzzyDetailsList().get(i).getAddress().getStreetName();
                    String state = fuzzyOutcome.getFuzzyDetailsList().get(i).getAddress().getCountrySubdivisionName();
                    String country = fuzzyOutcome.getFuzzyDetailsList().get(i).getAddress().getCountry();
                    adapter.add(street + ", " + state + ", " + country);
                }
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        gMap.clear();

                        EditText loc_editText = findViewById(R.id.title_editText);
                        loc_editText.setText(input.getText().toString());

                        latitude = fuzzyOutcome.getFuzzyDetailsList().get(position).getPosition().getLatitude();
                        longitude = fuzzyOutcome.getFuzzyDetailsList().get(position).getPosition().getLongitude();

                        gMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }
            @Override
            public void onError(@NotNull SearchException e) {

            }
        });
    }

    public void addLocation(View view) {
        EditText tagEditText = findViewById(R.id.tag_editText);
        String tag = tagEditText.getText().toString();

        EditText triggerEditText = findViewById(R.id.keyWord_editText);
        String trigger = triggerEditText.getText().toString();

        EditText prasangEditText = findViewById(R.id.memory_editText);
        String prasang = prasangEditText.getText().toString();

        EditText locationEditText = findViewById(R.id.title_editText);
        String location = locationEditText.getText().toString();

        if (tag.equals("") || trigger.equals("")){
            Toast.makeText(this, "Please enter text in all the fields", Toast.LENGTH_LONG).show();
        } else{
            DatabaseAccess dbAccess = DatabaseAccess.getInstance(getApplicationContext());
            dbAccess.open();
            dbAccess.addNewLocation(latitude, longitude, location, tag, trigger, prasang);
            dbAccess.close();
        }
    }
}