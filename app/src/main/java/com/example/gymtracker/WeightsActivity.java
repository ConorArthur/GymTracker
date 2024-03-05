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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeightsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseDataAdapter weightDataAdapter;
    private GymTrackerDao gymTrackerDao;

    private List<NameEntity> exerciseDataList = new ArrayList<>();



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        recyclerView = findViewById(R.id.recyclerView);
        weightDataAdapter = new ExerciseDataAdapter(this, new ExerciseItemClickListener(), new AddSetClickListener());


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(weightDataAdapter);



        gymTrackerDao = AppDatabase.getDatabase(this).GymTrackerDao();


        new LoadWeightExercisesTask().execute();


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
                weightDataAdapter.deleteItemByName(exerciseNameToDelete);
                DeleteExerciseTask deleteTask = new DeleteExerciseTask(exerciseDataList, weightDataAdapter);
                deleteTask.execute(exerciseNameToDelete);
            }
        }

        private class DeleteExerciseTask extends AsyncTask<String, Void, Boolean> {
            private int deletedPosition;
            private ExerciseDataAdapter weightDataAdapter;
            private List<NameEntity> exerciseData;

            public DeleteExerciseTask(List<NameEntity> exerciseData, ExerciseDataAdapter adapter) {
                this.exerciseData = exerciseData;
                this.weightDataAdapter = adapter;
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
                        weightDataAdapter.removeItem(deletedPosition);
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
                Log.e("GetExerciseDataAsyncTask", "Error retrieving exercise data: " + e.getMessage());
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

                Intent intent = new Intent(WeightsActivity.this, ResultsActivity.class);
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
        View dialogView = inflater.inflate(R.layout.activity_weight_add, null);
        dialogBuilder.setView(dialogView);

        TextView exerciseNameTextView = dialogView.findViewById(R.id.exerciseNameTextView);
        exerciseNameTextView.setText(exercise.getName());
        EditText valueEditText = dialogView.findViewById(R.id.valueEditText);
        EditText setsEditText = dialogView.findViewById(R.id.setsEditText);
        EditText repsEditText = dialogView.findViewById(R.id.repsEditText);
        Button addButton = dialogView.findViewById(R.id.addButton);


        AlertDialog dialog = dialogBuilder.create();


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String exerciseName = exerciseNameTextView.getText().toString();
                String valueStr = valueEditText.getText().toString();
                String setsStr = setsEditText.getText().toString();
                String repsStr = repsEditText.getText().toString();

                boolean isValid = true;

                if (exerciseName.isEmpty()) {
                    exerciseNameTextView.setError("Exercise name is required.");
                    isValid = false;
                }

                double value = 0;
                if (valueStr.isEmpty()) {
                    valueEditText.setError("Weight is required.");
                    isValid = false;
                } else {
                    try {
                        value = Double.parseDouble(valueStr);
                    } catch (NumberFormatException e) {
                        valueEditText.setError("Invalid distance format.");
                        isValid = false;
                    }
                }

                int sets = 0;
                if (setsStr.isEmpty()) {
                    setsEditText.setError("Gradient is required.");
                    isValid = false;
                } else {
                    try {
                        sets = Integer.parseInt(setsStr);
                        if (sets < 0) {
                            setsEditText.setError("Gradient must be greater than or equal to 0.");
                            isValid = false;
                        }
                    } catch (NumberFormatException e) {
                        setsEditText.setError("Invalid gradient format.");
                        isValid = false;
                    }
                }


                if (repsStr.isEmpty()) {
                    repsEditText.setError("Reps are required.");
                    isValid = false;
                } else {

                    String[] repsArray = repsStr.split(",");
                    List<Integer> repsList = new ArrayList<>();


                    for (String rep : repsArray) {
                        try {
                            int repValue = Integer.parseInt(rep.trim());
                            if (repValue <= 0) {
                                repsEditText.setError("Invalid rep value. Reps must be greater than 0.");
                                isValid = false;
                            } else {
                                repsList.add(repValue);
                            }
                        } catch (NumberFormatException e) {
                            repsEditText.setError("Invalid rep format. Please enter valid integers.");
                            isValid = false;
                        }
                    }


                    if (isValid) {
                        repsStr = TextUtils.join(",", repsList);
                    }
                }


                if (isValid) {
                    new WeightsActivity.AddSetTask(gymTrackerDao, dialog).execute(exerciseName, valueStr, setsStr, repsStr);
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
        protected Boolean doInBackground(String... params) {
            try {
                String exerciseName = params[0];
                int distance = Integer.parseInt(params[1]);
                int gradient = Integer.parseInt(params[2]);
                List<Integer> durationList = new ArrayList<>();
                String[] durationStrings = params[3].split(",");
                for (String durationString : durationStrings) {
                    durationList.add(Integer.parseInt(durationString));
                }


                long nameId = gymTrackerDao.getNameIdFromName(exerciseName);
                long typeId = gymTrackerDao.getTypeIdByName("Weights");

                Date currentDate = new Date(System.currentTimeMillis());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                String formattedDate = dateFormat.format(currentDate);

                DataEntity newSet = new DataEntity(nameId, typeId, distance, durationList, gradient, formattedDate, exerciseName);


                long insertedId = dao.insert(newSet);

                return insertedId != 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(WeightsActivity.this, "Set added to the database", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                new WeightsActivity.LoadWeightExercisesTask().execute();
            } else {
                Toast.makeText(WeightsActivity.this, "Failed to add set", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openAddItemDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_exercise_initial, null);
        dialogBuilder.setView(dialogView);

        EditText exerciseNameEditText = dialogView.findViewById(R.id.exerciseNameEditText);
        EditText valueEditText = dialogView.findViewById(R.id.valueEditText);
        EditText repsEditText = dialogView.findViewById(R.id.repsEditText);
        EditText setsEditText = dialogView.findViewById(R.id.setsEditText);
        Button addButton = dialogView.findViewById(R.id.addButton);


        String defaultType = "Weights";
        AlertDialog dialog = dialogBuilder.create();



        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String exerciseName = exerciseNameEditText.getText().toString();
                double value;
                int sets;
                String reps = repsEditText.getText().toString();
                ;


                try {
                    value = Double.parseDouble(valueEditText.getText().toString());
                    sets = Integer.parseInt(setsEditText.getText().toString());


                    valueEditText.setError(null);
                    setsEditText.setError(null);
                } catch (NumberFormatException e) {

                    if (valueEditText.getText().toString().isEmpty()) {
                        valueEditText.setError("Fill Field");
                    }
                    if (setsEditText.getText().toString().isEmpty()) {
                        setsEditText.setError("Fill Field");
                    }
                    if (exerciseName.isEmpty()) {
                        exerciseNameEditText.setError("Fill Field");
                    }
                    if (reps.isEmpty()) {
                        repsEditText.setError("Fill Field");
                    }

                    return;
                }


                boolean validReps = true;
                String[] repsArray = reps.split(",");
                List<Integer> repsList = new ArrayList<>();

                for (String rep : repsArray) {
                    try {
                        int repValue = Integer.parseInt(rep.trim());
                        if (repValue <= 0) {
                            validReps = false;
                            break;
                        }
                        repsList.add(repValue);
                    } catch (NumberFormatException e) {
                        repsEditText.setError("Invalid reps. Please enter a valid integer.");
                        return;
                    }
                }
                if (!validReps) {
                    repsEditText.setError("Invalid reps. Please enter a valid integer.");
                    return;
                }

                if (validReps) {

                    StringBuilder repsBuilder = new StringBuilder();
                    for (Integer repValue : repsList) {
                        repsBuilder.append(repValue).append(",");
                    }
                    if (repsBuilder.length() > 0) {
                        reps = repsBuilder.substring(0, repsBuilder.length() - 1);
                    } else {
                        reps = "";
                    }

                    new AddExerciseTask(gymTrackerDao, dialog).execute(exerciseName, String.valueOf(value), String.valueOf(sets), reps);
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
                Double value = Double.parseDouble(strings[1]);
                int sets = Integer.parseInt(strings[2]);
                String repsString = strings[3];

                NameEntity existingName = dao.getExerciseName(exerciseName);
                if (existingName != null) {
                    runOnUiThread(() -> Toast.makeText(WeightsActivity.this, "Exercise name already exists. Please use a unique name.", Toast.LENGTH_SHORT).show());
                    return null;
                }

                NameEntity nameEntity = new NameEntity(exerciseName);


                long typeId = gymTrackerDao.getTypeIdByName("Weights");


                nameEntity.setTypeId(typeId);

                long nameId = dao.insertName(nameEntity);

                if (nameId != 0) {
                    List<Integer> reps = new ArrayList<>();
                    String[] repsArray = repsString.split(",");
                    for (String rep : repsArray) {
                        try {
                            if (!rep.isEmpty()) {
                                int repValue = Integer.parseInt(rep.trim());
                                if (repValue <= 0) {
                                    runOnUiThread(() -> Toast.makeText(WeightsActivity.this, "Invalid reps. Please enter a valid integer.", Toast.LENGTH_SHORT).show());
                                    return null;
                                }
                                reps.add(repValue);
                            }
                        } catch (NumberFormatException e) {
                            runOnUiThread(() -> Toast.makeText(WeightsActivity.this, "Invalid reps. Please enter a valid integer.", Toast.LENGTH_SHORT).show());
                            return null;
                        }
                    }


                    Date currentDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                    String formattedDate = dateFormat.format(currentDate);


                    DataEntity dataEntity = new DataEntity(nameId, typeId, value, reps, sets, formattedDate, exerciseName);
                    return dao.insert(dataEntity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long dataEntityId) {
            if (dataEntityId != null) {
                Toast.makeText(WeightsActivity.this, "Exercise added to the database", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                new LoadWeightExercisesTask().execute();

            } else {
                Toast.makeText(WeightsActivity.this, "Failed to add exercise", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadWeightExercisesTask extends AsyncTask<Void, Void, List<NameEntity>> {
        @Override
        protected List<NameEntity> doInBackground(Void... voids) {
            long typeId = gymTrackerDao.getTypeIdByName("Weights");
            return gymTrackerDao.getNamesByType(typeId);
        }

        @Override
        protected void onPostExecute(List<NameEntity> exerciseNames) {
            weightDataAdapter.setExerciseData(exerciseNames);

            for (NameEntity entity : exerciseNames) {
            }
        }
    }
}