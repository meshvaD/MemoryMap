package com.example.memorymap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private NotificationManagerCompat notifManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notifManager = NotificationManagerCompat.from(this);

        ImageView imView = findViewById(R.id.imageView4);
        Bitmap bitmap = ((BitmapDrawable)imView.getDrawable()).getBitmap();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        double factor = (double) width / (double)bitmap.getWidth();
        int height = (int)(factor * bitmap.getHeight());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        imView.setLayoutParams(params);
    }

    //open about activity
    public void open_about(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    //open map activity
    public void open_map(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /* @RequiresApi(api = Build.VERSION_CODES.N)
    public void openReminder(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(android.icu.util.Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 20);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    } */

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showTimePicker(View view) {
        DialogFragment fragment = new TimePickerFragment();
        fragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}