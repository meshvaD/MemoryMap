package com.example.memorymap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class InfoActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback{

    private static final int SELECT_PICTURE = 1;
    ImageView iconImageView;

    int num;
    String location;

    Button addImButton;
    DatabaseAccess dbAccess;

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        num = Integer.parseInt(getIntent().getStringExtra("markerTag"));

        //transparent until image chosen
        iconImageView = findViewById(R.id.iconImageView);
        dbAccess = DatabaseAccess.getInstance(this);
        dbAccess.open();
        byte[] previousBlob = dbAccess.getBlob(num);
        dbAccess.close();

        if (previousBlob != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(previousBlob, 0, previousBlob.length);
            iconImageView.setImageBitmap(bitmap);
        } else{
            iconImageView.setImageResource(android.R.color.transparent);
        }


        addImButton = findViewById(R.id.addImage_button);


        EditText prasang_editText = findViewById(R.id.memoryShow_editText);
        EditText trigger_editText = findViewById(R.id.triggerShow_edittext);
        EditText group_editText = findViewById(R.id.groupShow_editText);
        EditText loc_editText = findViewById(R.id.locShow_editText);

        DatabaseAccess dbAccess = DatabaseAccess.getInstance(this);
        dbAccess.open();

        location = MapsFragment.location.get(num);

        loc_editText.setText(location);
        prasang_editText.setText(dbAccess.getInfoColumn(3).get(num));
        trigger_editText.setText(dbAccess.getInfoColumn(7).get(num));
        group_editText.setText(dbAccess.getInfoColumn(5).get(num));

        dbAccess.close();

        //back button
        Toolbar toolbar = (Toolbar)findViewById(R.id.info_toolbar);
        toolbar.setTitle(location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            try{
                //get uri data from image gallery selection
                Uri uri = data.getData();
                InputStream imStream = getContentResolver().openInputStream(uri);
                Bitmap image = BitmapFactory.decodeStream(imStream);

                iconImageView.setImageBitmap(image);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                //image.recycle(); ERROR reg recycled bitmap

                dbAccess.open();
                dbAccess.addBlobInfo(num, byteArray);
                //byte[] blob = dbAccess.getBlob(num);
                dbAccess.close();

            } catch (Exception e){}
        }
    }

    public void imageButtonClick(View view) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        //activity opens image gallery
        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        photoIntent.setType("image/*");
        startActivityForResult(photoIntent, SELECT_PICTURE);
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
    }

    public void backButtonClick(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);


        EditText prasang_editText = findViewById(R.id.memoryShow_editText);
        EditText trigger_editText = findViewById(R.id.triggerShow_edittext);
        EditText group_editText = findViewById(R.id.groupShow_editText);

        DatabaseAccess dbAccess = DatabaseAccess.getInstance(this);
        dbAccess.open();
        dbAccess.updateRow(num, prasang_editText.getText().toString(), trigger_editText.getText().toString(),
                group_editText.getText().toString());
        dbAccess.close();
    }

    public void deleteButtonClick(View view) {
        builder = new AlertDialog.Builder(this);
        String message = "Delete all info associated with " + location;

        builder.setMessage(message).setTitle("Add new connection")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseAccess dbAccess = DatabaseAccess.getInstance(getApplicationContext());
                        dbAccess.open();
                        dbAccess.deleteRow(num);
                        dbAccess.close();

                        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}