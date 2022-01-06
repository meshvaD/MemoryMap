package com.example.memorymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsFragment extends Fragment implements OnMarkerClickListener, View.OnClickListener {

    boolean doublePress = false;
    boolean connectionPress = false;

    Marker firstConnMarker;
    int firstDoublePressMarkerTag;

    AlertDialog.Builder builder;

    public static List<String> location;
    public static List<String> longitude;
    public static List<String> latitude;
    public static List<String> tags;
    public static List<String> buildTagList;
    public static List<String> connList;
    public static List<String> triggerList;

    GoogleMap googleMap;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap gMap) {
            googleMap = gMap;

            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
            databaseAccess.open();
            //get info from each column of database
            location = databaseAccess.getInfoColumn(0);
            latitude = databaseAccess.getInfoColumn(1);
            longitude = databaseAccess.getInfoColumn(2);
            tags = databaseAccess.getInfoColumn(5);
            connList = databaseAccess.getInfoColumn(6);
            triggerList = databaseAccess.getInfoColumn(7);
            databaseAccess.close();

            //new AlertDialog.Builder(getContext()).setMessage(connList.toString()).show();

            //marker click listener
            googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (connectionPress){
                        firstConnMarker = marker;
                        connectionPress = false;
                    } else if (firstConnMarker != null){

                        builder = new AlertDialog.Builder(getContext());
                        String message = "Create new connection between " +
                                location.get(Integer.parseInt(firstConnMarker.getTag().toString())) + " and " +
                                location.get(Integer.parseInt(marker.getTag().toString()));

                        builder.setMessage(message).setTitle("Add new connection")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        drawLine(firstConnMarker.getPosition(), marker.getPosition());

                                        int firstIndex = Integer.parseInt(firstConnMarker.getTag().toString());
                                        String secondIndex = marker.getTag().toString();

                                        databaseAccess.open();
                                        databaseAccess.addConnInfo(firstIndex, secondIndex);
                                        databaseAccess.close();

                                        firstConnMarker = null;
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();

                    } else if (doublePress && firstDoublePressMarkerTag == (int)marker.getTag()){
                        Intent intent = new Intent(getContext(), InfoActivity.class);
                        //intent.putExtra("location", marker.getTitle());
                        intent.putExtra("markerTag", marker.getTag().toString());
                        startActivity(intent);
                    } else{
                        //once pressed (originally false), set to true, so new intent can be started at next click
                        doublePress = true;
                        firstDoublePressMarkerTag = (int) marker.getTag();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                doublePress = false;
                            }
                        }, 2000);
                    }

                    return false;
                }
            });

            buildTagList = new ArrayList<String>();
            List<BitmapDescriptor> colourList = new ArrayList<BitmapDescriptor>();

            ChipGroup chipGroup = getActivity().findViewById(R.id.chipGroup);

            chipGroup.setSingleSelection(true);
            chipGroup.setSelectionRequired(true);

            //create colours/ chip for each unique tag
            for (int i=0; i<location.size(); i++){
                //check if unique tag, assign colour/ give existing colour scheme
                int colIndex = isStringInList(tags.get(i), buildTagList);
                if (colIndex == -1){
                    buildTagList.add(tags.get(i));
                    colourList.add(randomColour());

                    //add new chip for each unique tag
                    Chip chip = new Chip(getContext());

                    chip.setText(tags.get(i));
                    chip.setCheckable(true);
                    chipGroup.addView(chip);
                }
            }

            chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ChipGroup group, int checkedId) {
                    Chip chip = chipGroup.findViewById(checkedId);
                    googleMap.clear();

                    //add marker based on chip selection
                    for (int i=0; i<longitude.size(); i++){
                        if (tags.get(i).equals(chip.getText())){
                            LatLng pos = new LatLng(Double.parseDouble(latitude.get(i)), Double.parseDouble(longitude.get(i)));

                            /* googleMap.addMarker(new MarkerOptions().position(pos)
                                    .title(i + " " + location.get(i))
                                    .icon(colourList.get(isStringInList(tags.get(i), buildTagList)))); */

                            MarkerOptions option = new MarkerOptions().position(pos)
                                    .title(location.get(i))
                                    .icon(colourList.get(isStringInList(tags.get(i), buildTagList)))
                                    .snippet(triggerList.get(i));

                            googleMap.addMarker(option).setTag(i);
                            drawAllConnOfMarker(i);
                        }
                    }
                }
            });

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(latitude.get(0)),
                    Double.parseDouble(longitude.get(0)))));

            Button connButton = getActivity().findViewById(R.id.connections_button);
            connButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectionPress = true;
                }
            });

        }
    };

    private void drawAllConnOfMarker (int markerIndex){
        //Only one connection per marker
        /* try{
            LatLng connPos = null;
            LatLng markerPos = null;
            String markerConns = connList.get(markerIndex);
            //List<Integer> connIndexes = null;

            markerPos = new LatLng(Double.parseDouble(latitude.get(markerIndex)),
                    Double.parseDouble(longitude.get(markerIndex)));

            connPos = new LatLng(Double.parseDouble(latitude.get(Integer.parseInt(markerConns))),
                    Double.parseDouble(longitude.get(Integer.parseInt(markerConns))));

            drawLine(markerPos, connPos);
        } catch (Exception e){}; */

        try{
            LatLng markerPos = new LatLng(Double.parseDouble(latitude.get(markerIndex)),
                    Double.parseDouble(longitude.get(markerIndex)));

            String markerConns = connList.get(markerIndex);
            StringBuilder buildNum = new StringBuilder();

            for (int i=4; i<markerConns.length(); i++) {
                if (markerConns.charAt(i) == ',') {
                    LatLng connPos = new LatLng(Double.parseDouble(latitude.get(Integer.parseInt(buildNum.toString()))),
                            Double.parseDouble(longitude.get(Integer.parseInt(buildNum.toString()))));
                    buildNum = new StringBuilder();

                    drawLine(markerPos, connPos);
                } else {
                    buildNum.append(markerConns.charAt(i));
                }
            }
        } catch(Exception e){};

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_maps, container, false);


        /* ChipGroup chipGroup = getActivity().findViewById(R.id.chipGroup);
        chipGroup.setOnClickListener(this::onClick); */

        /* Button connButton = (Button)layout.findViewById(R.id.connections_button);
        connButton.setOnClickListener(this::onClick); */

        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    private void drawLine(LatLng one, LatLng two){
        googleMap.addPolyline(new PolylineOptions()
        .add(one, two)
        .width(5)
        .color(Color.BLACK));
    }

    private BitmapDescriptor randomColour(){
        Random random = new Random();
        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);

        // format it as hexadecimal string (with hashtag and leading zeros)
        String colorCode = String.format("#%06x", nextInt);

        //to hsv, return bitmap discriptor, to change icon colour
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(colorCode), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    private int isStringInList(String string, List<String>list){
        int index = -1;
        for (int i=0; i<list.size(); i++){
            if (string.equals(list.get(i))){
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onClick(View v) {
    }

}