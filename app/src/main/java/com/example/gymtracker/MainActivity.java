package com.example.gymtracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private Button cardioButton;
    private Button weightsButton;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 102;

    private AppDatabase appDatabase;

    private TextView textViewStepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        cardioButton = findViewById(R.id.cardiobutton);
        weightsButton = findViewById(R.id.weightsbutton);
        textViewStepCount = findViewById(R.id.stepcounter);

        cardioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CardioActivity.class);
                startActivity(intent);
            }
        });

        weightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, WeightsActivity.class);
                startActivity(intent);
            }
        });


        textViewStepCount = findViewById(R.id.stepcounter);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
            } else {

                startStepTrackingService();
            }
        } else {

            startStepTrackingService();
        }


        int initialStepCount = getInitialStepCountFromSharedPreferences();
        updateStepCountText(initialStepCount);
    }

    private void startStepTrackingService() {
        Intent serviceIntent = new Intent(this, StepTrackingService.class);
        startService(serviceIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startStepTrackingService();
            } else {
                Toast.makeText(this, "Permission to access the step counter denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getInitialStepCountFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("stepCount", 0);
    }

    private void updateStepCountText(int stepCount) {
        textViewStepCount.setText("Step Count: " + stepCount);
    }
}