package com.example.memorymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MapActivity extends AppCompatActivity {
    Bitmap markerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        /* markerImage = (Bitmap) getIntent().getParcelableExtra("ImageBitmap");
        int iconChangeIndex = Integer.parseInt(getIntent().getStringExtra("num")); */
    }

    public void searchLocation(View view) {

        EditText loc_editText = findViewById(R.id.location_editText);
        String loc = loc_editText.getText().toString();
        loc = loc.toLowerCase();

        List<String> location = MapsFragment.location;

        Boolean locFound = false;
        if (loc.length() > 3){ //ensure search is not too short
            for (int i=0; i<location.size(); i++){ //compare w each entry, find correct reference to start InfoActivity
                String reference = location.get(i).toLowerCase();
                if (reference.contains(loc)){
                    locFound = true;
                    Intent intent = new Intent(this, InfoActivity.class);

                    intent.putExtra("markerTag", String.valueOf(i));
                    startActivity(intent);
                    break;
                }
            }
        }
        if (!locFound){ //doesn't show text for some reason???
            Toast.makeText(this, "Not Found", Toast.LENGTH_LONG).show();
        }

    }

    public void backButtonClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openCreate(View view) {
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    public void connectionPressed(View view) {

    }
}