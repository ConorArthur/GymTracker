package com.example.gymtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymtracker.NameEntity;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDataAdapter extends RecyclerView.Adapter<ExerciseDataAdapter.ExerciseViewHolder> {
    private final Context context;
    private final List<NameEntity> exerciseData;
    private OnExerciseItemClickListener exerciseItemClickListener;
    private OnAddSetClickListener addSetClickListener;

    public interface OnExerciseItemClickListener {
        void onItemClick(NameEntity exercise);
        void onDeleteClick(NameEntity exercise);
    }

    public interface OnAddSetClickListener {
        void onAddSetClick(NameEntity exercise);
    }

    public ExerciseDataAdapter(Context context, OnExerciseItemClickListener exerciseItemClickListener, OnAddSetClickListener addSetClickListener) {
        this.context = context;
        this.exerciseData = new ArrayList<>();
        this.exerciseItemClickListener = exerciseItemClickListener;
        this.addSetClickListener = addSetClickListener;
    }


    public void setExerciseData(List<NameEntity> exerciseData) {
        this.exerciseData.clear();
        this.exerciseData.addAll(exerciseData);
        notifyDataSetChanged();
    }
    public List<NameEntity> getExerciseData() {
        return exerciseData;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exercise_item, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        NameEntity exercise = exerciseData.get(position);
        holder.bind(exercise);
        holder.exerciseInfo = exercise;

        holder.exerciseNameButton.setText(exercise.getName());

        holder.exerciseNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra("exerciseName", exercise.getName());
                context.startActivity(intent);
            }
        });

        holder.addSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addSetClickListener != null) {
                    addSetClickListener.onAddSetClick(exercise);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseData.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < exerciseData.size()) {
            exerciseData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, exerciseData.size());
        }
    }
    public void deleteItemByName(String exerciseName) {
        for (int i = 0; i < exerciseData.size(); i++) {
            NameEntity exercise = exerciseData.get(i);
            if (exercise.getName().equals(exerciseName)) {
                exerciseData.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, exerciseData.size());
                break;
            }
        }
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final Button exerciseNameButton;
        private final Button addSetButton;
        private NameEntity exerciseInfo;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameButton = itemView.findViewById(R.id.exerciseNameButton);
            addSetButton = itemView.findViewById(R.id.addSetButton);

            exerciseNameButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDeleteConfirmationDialog(exerciseInfo);
                    return true;
                }
            });

            exerciseNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exerciseItemClickListener != null) {
                        exerciseItemClickListener.onItemClick(exerciseInfo);
                    }
                }
            });

            addSetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (addSetClickListener != null) {
                        addSetClickListener.onAddSetClick(exerciseInfo);
                    }
                }
            });
        }

        public void bind(NameEntity exercise) {
            exerciseInfo = exercise;
            exerciseNameButton.setText(exercise.getName());
        }

        private void showDeleteConfirmationDialog(final NameEntity exerciseInfo) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete Exercise");
            builder.setMessage("Are you sure you want to delete this exercise?");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (exerciseItemClickListener != null) {
                        exerciseItemClickListener.onDeleteClick(exerciseInfo);
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}