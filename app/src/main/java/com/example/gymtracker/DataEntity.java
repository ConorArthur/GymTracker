package com.example.gymtracker;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.List;

@Entity(foreignKeys = {
        @ForeignKey(entity = NameEntity.class, parentColumns = "id", childColumns = "nameId", onDelete = ForeignKey.CASCADE)
})
@TypeConverters(DataConverter.class)
public class DataEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long nameId; // Foreign key
    private double weight;
    private List<Integer> reps;
    private int sets;

    private String dateAdded;

    @ColumnInfo(name = "typeId")
    private long typeId;

    @ColumnInfo(name = "exerciseName")
    private String exerciseName;
    public DataEntity(long nameId,long typeId, double weight, List<Integer> reps, int sets, String dateAdded, String exerciseName) {
        this.nameId = nameId;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
        this.dateAdded = dateAdded;
        this.typeId = typeId;
        this.exerciseName = exerciseName;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }
    public long getId() {
        return id;
    }

    public long getNameId() {
        return nameId;
    }

    public double getWeight() {
        return weight;
    }

    public List<Integer> getReps() {
        return reps;
    }

    public int getSets() {
        return sets;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNameId(long nameId) {
        this.nameId = nameId;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setReps(List<Integer> reps) {
        this.reps = reps;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }
}