package com.example.gymtracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CardioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseDataAdapter cardioDataAdapter;
    private GymTrackerDao gymTrackerDao;

    private List<NameEntity> exerciseDataList = new ArrayList<>();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        recyclerView = findViewById(R.id.recyclerView);
        cardioDataAdapter = new ExerciseDataAdapter(this, new ExerciseItemClickListener(), new AddSetClickListener());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cardioDataAdapter);

        gymTrackerDao = AppDatabase.getDatabase(this).GymTrackerDao();

        new LoadCardioExercisesTask().execute();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddItemDialog();
            }
        });
    }

    private class ExerciseItemClickListener implements ExerciseDataAdapter.OnExerciseItemClickListener {
        @Override
        public void onItemClick(NameEntity exercise) {
            new GetExerciseDataAsyncTask().execute(exercise.getName());

        }

        @Override
        public void onDeleteClick(NameEntity exercise) {
            if (exercise != null) {
                String exerciseNameToDelete = exercise.getName();
                cardioDataAdapter.deleteItemByName(exerciseNameToDelete);
                DeleteExerciseTask deleteTask = new DeleteExerciseTask(exerciseDataList, cardioDataAdapter);
                deleteTask.execute(exerciseNameToDelete);
            }
        }

        private class DeleteExerciseTask extends AsyncTask<String, Void, Boolean> {
            private int deletedPosition;
            private ExerciseDataAdapter cardioDataAdapter;
            private List<NameEntity> exerciseData;

            public DeleteExerciseTask(List<NameEntity> exerciseData, ExerciseDataAdapter adapter) {
                this.exerciseData = exerciseData;
                this.cardioDataAdapter = adapter;
            }

            @Override
            protected Boolean doInBackground(String... exerciseNames) {
                if (exerciseNames.length > 0) {
                    String exerciseName = exerciseNames[0];
                    NameEntity exerciseToDelete = gymTrackerDao.getExerciseName(exerciseName);

                    if (exerciseToDelete != null) {
                        deletedPosition = exerciseData.indexOf(exerciseToDelete);
                        long exerciseIdToDelete = exerciseToDelete.getId();

                        gymTrackerDao.deleteExerciseNameAndData(exerciseIdToDelete);
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean deletionResult) {
                if (deletionResult) {
                    if (deletedPosition != -1) {
                        exerciseDataList.remove(deletedPosition);
                        cardioDataAdapter.removeItem(deletedPosition);
                    }
                }
            }
        }
    }
        private class GetExerciseDataAsyncTask extends AsyncTask<String, Void, DataEntity> {
            @Override
            protected DataEntity doInBackground(String... params) {
                String exerciseName = params[0];
                DataEntity exerciseData = null;

                try {
                    exerciseData = gymTrackerDao.getExerciseDataForExercise(exerciseName);
                } catch (Exception e) {
                }

                return exerciseData;
            }

            @Override
            protected void onPostExecute(DataEntity exerciseData) {
                if (exerciseData != null) {

                    List<Integer> exerciseDurationList = exerciseData.getReps();
                    StringBuilder durationStringBuilder = new StringBuilder();

                    for (int i = 0; i < exerciseDurationList.size(); i++) {
                        if (i > 0) {
                            durationStringBuilder.append(":");
                        }
                        durationStringBuilder.append(exerciseDurationList.get(i));
                    }

                    String formattedExerciseDuration = durationStringBuilder.toString();


                    Intent intent = new Intent(CardioActivity.this, ResultsActivity.class);
                    intent.putExtra("exerciseName", exerciseData.getExerciseName());

                    intent.putExtra("exerciseWeight", exerciseData.getWeight());
                    intent.putExtra("exerciseReps", new ArrayList<>(exerciseData.getReps()));
                    intent.putExtra("exerciseSet", exerciseData.getSets());
                    intent.putExtra("exerciseDateAdded", exerciseData.getDateAdded());
                    startActivity(intent);
                }
            }
        }


    private class AddSetClickListener implements ExerciseDataAdapter.OnAddSetClickListener {
        @Override
        public void onAddSetClick(NameEntity exercise) {

            showAddSetDialog(exercise);

        }
    }

    private void showAddSetDialog(NameEntity exercise) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_cardio_add, null);
        dialogBuilder.setView(dialogView);

        TextView exerciseNameTextView = dialogView.findViewById(R.id.exerciseNameTextView);
        exerciseNameTextView.setText(exercise.getName());
        EditText distanceEditText = dialogView.findViewById(R.id.distanceEditText);
        EditText gradientEditText = dialogView.findViewById(R.id.gradientEditText);
        EditText timeEditText = dialogView.findViewById(R.id.timeEditText);
        Button addButton = dialogView.findViewById(R.id.addButton);


        AlertDialog dialog = dialogBuilder.create();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String exerciseName = exerciseNameTextView.getText().toString();
                String distanceStr = distanceEditText.getText().toString();
                String gradientStr = gradientEditText.getText().toString();
                String durationStr = timeEditText.getText().toString();

                boolean isValid = true;

                if (exerciseName.isEmpty()) {
                    exerciseNameTextView.setError("Exercise name is required.");
                    isValid = false;
                }

                double distance = 0;
                if (distanceStr.isEmpty()) {
                    distanceEditText.setError("Distance is required.");
                    isValid = false;
                } else {
                    try {
                        distance = Double.parseDouble(distanceStr);
                        if (distance < 0) {
                            gradientEditText.setError("Gradient must be greater than or equal to 0.");
                            isValid = false;
                        }
                    } catch (NumberFormatException e) {
                        distanceEditText.setError("Invalid distance format.");
                        isValid = false;
                    }
                }

                int gradient = 0;
                if (gradientStr.isEmpty()) {
                    gradientEditText.setError("Gradient is required.");
                    isValid = false;
                } else {
                    try {
                        gradient = Integer.parseInt(gradientStr);
                        if (gradient < 0) {
                            gradientEditText.setError("Gradient must be greater than or equal to 0.");
                            isValid = false;
                        }
                    } catch (NumberFormatException e) {
                        gradientEditText.setError("Invalid gradient format.");
                        isValid = false;
                    }
                }



                if (!isValidTimeFormat(durationStr)) {
                    timeEditText.setError("Invalid time format. Please enter a valid time (HH:mm:ss).");
                    isValid = false;
                }

                if (isValid) {

                    String[] durationArray = durationStr.split(":");
                    int hours = Integer.parseInt(durationArray[0]);
                    int minutes = Integer.parseInt(durationArray[1]);
                    int seconds = Integer.parseInt(durationArray[2]);
                    int totalSeconds = (hours * 3600) + (minutes * 60) + seconds;

                    String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                    new AddSetTask(gymTrackerDao, dialog).execute(exerciseName, distanceStr, gradientStr, durationStr);
                }
            }
        });

        dialog.show();
    }

    private class AddSetTask extends AsyncTask<String, Integer, Boolean> {
        private GymTrackerDao dao;
        private AlertDialog dialog;

        public AddSetTask(GymTrackerDao dao, AlertDialog dialog) {
            this.dao = dao;
            this.dialog = dialog;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                String exerciseName = strings[0];
                double distanceStr = Double.parseDouble(strings[1]);
                int gradientStr = Integer.parseInt(strings[2]);
                String durationStr = strings[3];


                long nameId = gymTrackerDao.getNameIdFromName(exerciseName);
                long typeId = gymTrackerDao.getTypeIdByName("Cardio");

                List<Integer> durationList = new ArrayList<>();



                String[] timeParts = durationStr.split(":");
                if (timeParts.length == 3) {
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);

                    int totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
                    durationList.add(totalSeconds);

                    Date currentDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                    String formattedDate = dateFormat.format(currentDate);

                    DataEntity newSet = new DataEntity(nameId, typeId, distanceStr, durationList, gradientStr, formattedDate, exerciseName);


                    long insertedId = dao.insert(newSet);

                    return insertedId != 0;
                }
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(CardioActivity.this, "Set added to the database", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                new LoadCardioExercisesTask().execute();
            } else {

                Toast.makeText(CardioActivity.this, "Failed to add set", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openAddItemDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_cardio_initial, null);
        dialogBuilder.setView(dialogView);

        EditText exerciseNameEditText = dialogView.findViewById(R.id.exerciseNameEditText);
        EditText distanceEditText = dialogView.findViewById(R.id.distanceEditText);
        EditText durationEditText = dialogView.findViewById(R.id.timeEditText);
        EditText gradientEditText = dialogView.findViewById(R.id.gradientEditText);
        Button addButton = dialogView.findViewById(R.id.addButton);


        String defaultType = "Cardio";

        AlertDialog dialog = dialogBuilder.create();


        addButton.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             String exerciseName = exerciseNameEditText.getText().toString();
                                             String distance = distanceEditText.getText().toString();
                                             String gradientStr = gradientEditText.getText().toString();
                                             String durationStr = durationEditText.getText().toString();

                                             boolean isValid = true;


                                             try {
                                                 distance = distanceEditText.getText().toString();
                                                 distanceEditText.setError(null);
                                             } catch (NumberFormatException e) {
                                                 distanceEditText.setError("Invalid value format. Please enter a valid integer.");
                                                 isValid = false;
                                             }

                                             boolean validGradient = true;
                                             String[] gradientArray = gradientStr.split(":");
                                             List<Integer> gradientList = new ArrayList<>();

                                             for (String gradient : gradientArray) {
                                                 try {
                                                     int gradientValue = Integer.parseInt(gradient.trim());
                                                     if (gradientValue < 0) {
                                                         validGradient = false;
                                                         break;
                                                     }
                                                     gradientList.add(gradientValue);
                                                 } catch (NumberFormatException e) {
                                                     gradientEditText.setError("Invalid gradient format. Please enter a valid integer.");
                                                     isValid = false;
                                                     break;
                                                 }
                                             }

                                             if (!validGradient) {
                                                 gradientEditText.setError("Invalid gradient. Please enter a valid integer.");
                                                 isValid = false;
                                             }


                                             if (!isValidTimeFormat(durationStr)) {
                                                 durationEditText.setError("Invalid time format. Please enter a valid time (HH:mm:ss).");
                                                 isValid = false;
                                             }

                                             if (isValid) {
                                                 StringBuilder gradientBuilder = new StringBuilder();
                                                 for (Integer gradientValue : gradientList) {
                                                     gradientBuilder.append(gradientValue).append(",");
                                                 }
                                                 if (gradientBuilder.length() > 0) {
                                                     gradientStr = gradientBuilder.substring(0, gradientBuilder.length() - 1);
                                                 } else {
                                                     gradientStr = "";
                                                 }


                                                 new AddExerciseTask(gymTrackerDao, dialog).execute(exerciseName, distance, durationStr, gradientStr);
                                             }
                                         }
                                     });
        dialog.show();
    }

    private class AddExerciseTask extends AsyncTask<String, Void, Long> {
        private GymTrackerDao dao;
        private AlertDialog dialog;

        public AddExerciseTask(GymTrackerDao dao, AlertDialog dialog) {
            this.dao = dao;
            this.dialog = dialog;
        }

        @Override
        protected Long doInBackground(String... strings) {
            try {
                String exerciseName = strings[0];
                double distanceStr = Double.parseDouble(strings[1]);
                int gradientStr = Integer.parseInt(strings[3]);
                String durationStr = strings[2];

                NameEntity existingName = dao.getExerciseName(exerciseName);
                if (existingName != null) {
                    runOnUiThread(() -> Toast.makeText(CardioActivity.this, "Exercise name already exists. Please use a unique name.", Toast.LENGTH_SHORT).show());
                    return null;
                }


                NameEntity nameEntity = new NameEntity(exerciseName);


                long typeId = gymTrackerDao.getTypeIdByName("Cardio");


                nameEntity.setTypeId(typeId);

                long nameId = dao.insertName(nameEntity);

                if (nameId != 0) {
                    String[] timeParts = durationStr.split(":");
                    if (timeParts.length == 3) {
                        int hours = Integer.parseInt(timeParts[0]);
                        int minutes = Integer.parseInt(timeParts[1]);
                        int seconds = Integer.parseInt(timeParts[2]);

                        if (hours >= 0 && minutes >= 0 && seconds >= 0) {
                            int totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
                            List<Integer> durationList = new ArrayList<>();
                            durationList.add(totalSeconds);

                            Date currentDate = new Date(System.currentTimeMillis());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                            String formattedDate = dateFormat.format(currentDate);

                            DataEntity dataEntity = new DataEntity(nameId, typeId, distanceStr, durationList, gradientStr, formattedDate, exerciseName);
                            return dao.insert(dataEntity);
                        } else {
                            runOnUiThread(() -> Toast.makeText(CardioActivity.this, "Invalid duration format. Please use non-negative values.", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(CardioActivity.this, "Invalid time format. Please enter a valid time (HH:mm:ss).", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long dataEntityId) {
            if (dataEntityId != null) {
                Toast.makeText(CardioActivity.this, "Exercise added to the database", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                new LoadCardioExercisesTask().execute();

            } else {
                Toast.makeText(CardioActivity.this, "Failed to add exercise", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class LoadCardioExercisesTask extends AsyncTask<Void, Void, List<NameEntity>> {
        @Override
        protected List<NameEntity> doInBackground(Void... voids) {
            long typeId = gymTrackerDao.getTypeIdByName("Cardio");
            return gymTrackerDao.getNamesByType(typeId);
        }

        @Override
        protected void onPostExecute(List<NameEntity> exercisesNames) {
            cardioDataAdapter.setExerciseData(exercisesNames);
        }
    }
    private boolean isValidTimeFormat(String timeStr) {
        String timePattern = "^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";
        return timeStr.matches(timePattern);
    }
}