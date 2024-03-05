package com.example.gymtracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {
    private GymTrackerDao gymTrackerDao;
    private List<DataEntity> exerciseDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        if (intent != null) {
            String exerciseName = intent.getStringExtra("exerciseName");

            AppDatabase appDatabase = AppDatabase.getDatabase(getApplicationContext());
            gymTrackerDao = appDatabase.GymTrackerDao();


            new GetLatestExerciseDataTask().execute(exerciseName);


            new GetHistoricalExerciseDataTask().execute(exerciseName);
        }
    }

    private class GetLatestExerciseDataTask extends AsyncTask<String, Void, DataEntity> {
        @Override
        protected DataEntity doInBackground(String... params) {

            DataEntity mostRecentExerciseData = gymTrackerDao.getLatestDataForExerciseName(params[0]);
            return mostRecentExerciseData;
        }

        @Override
        protected void onPostExecute(DataEntity exerciseData) {
            if (exerciseData != null) {

                TextView nameTextView = findViewById(R.id.nameTextView);
                TextView distanceTextView = findViewById(R.id.distanceTextView);
                TextView durationTextView = findViewById(R.id.durationTextView);
                TextView gradientTextView = findViewById(R.id.gradientTextView);
                TextView dateAddedTextView = findViewById(R.id.dateAddedTextView);

                nameTextView.setText(exerciseData.getExerciseName());
                distanceTextView.setText(String.valueOf(exerciseData.getWeight()));


                Object reps = exerciseData.getReps();

                if (reps instanceof List) {
                    List<Integer> repsList = (List<Integer>) reps;
                    if (!repsList.isEmpty()) {
                        if (repsList.size() == 1) {
                            int singleRep = repsList.get(0);
                            durationTextView.setText(formatDuration(singleRep));
                        } else {
                            StringBuilder repsString = new StringBuilder();
                            for (int i = 0; i < repsList.size(); i++) {
                                repsString.append(repsList.get(i));
                                if (i < repsList.size() - 1) {
                                    repsString.append(",");
                                }
                            }
                            durationTextView.setText(repsString.toString());
                        }
                    } else {
                        durationTextView.setText("");
                    }
                } else if (reps instanceof String) {
                    String repsString = (String) reps;
                    durationTextView.setText(repsString);
                } else {
                    durationTextView.setText(reps.toString());
                }


                gradientTextView.setText(String.valueOf(exerciseData.getSets()));
                dateAddedTextView.setText(exerciseData.getDateAdded());
            }
        }
    }

    private class GetHistoricalExerciseDataTask extends AsyncTask<String, Void, List<DataEntity>> {
        @Override
        protected List<DataEntity> doInBackground(String... params) {

            List<DataEntity> historicalData = gymTrackerDao.getAllDataForExerciseName(params[0]);
            return historicalData;
        }

        @Override
        protected void onPostExecute(List<DataEntity> historicalData) {
            List<Integer> distances = new ArrayList<>();
            List<String> formattedDates = new ArrayList<>();

            for (DataEntity exerciseData : historicalData) {
                distances.add((int) exerciseData.getWeight());
                formattedDates.add(formatDate(exerciseData.getDateAdded()));
            }


            LineGraphView lineGraphView = findViewById(R.id.lineGraphView);
            lineGraphView.setData(distances, formattedDates);
        }
    }

    private String formatDuration(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
    private boolean isDurationFormat(String input) {
        return input.matches("^\\d{2}:\\d{2}:\\d{2}$");
    }
}