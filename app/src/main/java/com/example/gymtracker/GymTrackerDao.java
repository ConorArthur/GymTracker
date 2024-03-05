package com.example.gymtracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.List;

    @Dao
    @TypeConverters(DataConverter.class)
    public interface GymTrackerDao {
        @Insert
        long insert(NameEntity nameentity);
        @Query("SELECT * FROM nameentity")
        List<NameEntity> getAllNames();
        @Query("SELECT * FROM NameEntity WHERE name = :name")
        NameEntity getExerciseName(String name);

        @Query("SELECT id FROM nameentity WHERE name = :name")
        long getNameIdFromName(String name);
        @Query("SELECT * FROM typeentity")
        List<TypeEntity> getType();
        @Insert
        long insertName(NameEntity name);
        @Insert
        long insert(TypeEntity type);
        @Query("DELETE FROM dataentity")
        void deleteAllData();

        @Query("SELECT * FROM DataEntity WHERE exerciseName = :exerciseName ORDER BY dateAdded")
        List<DataEntity> getAllDataForExerciseName(String exerciseName);

        @Query("SELECT * FROM DataEntity WHERE exerciseName = :exerciseName")
        DataEntity getExerciseDataForExercise(String exerciseName);
        @Query("DELETE FROM NameEntity WHERE id = :nameId")
        void deleteExerciseNameAndData(long nameId);
        @Query("SELECT * FROM dataentity WHERE id=:id")
        DataEntity getData(long id);
        @Query("SELECT * FROM nameentity WHERE typeId = :typeId")
        List<NameEntity> getNamesByType(Long typeId);

        @Query("SELECT id FROM typeentity WHERE name = :typeName")
        long getTypeIdByName(String typeName);
        @Insert
        long insert(DataEntity co);
        @Query("UPDATE dataentity SET nameId = :nameId, weight = :weight, " +
                "reps = :reps, sets = :sets, dateAdded = :dateAdded WHERE id = :id")
        void update(long nameId, double weight, List<Integer> reps, int sets, Date dateAdded, long id);

        @Query("SELECT dataentity.id, dataentity.nameId, dataentity.typeId, dataentity.weight, " +
                "dataentity.sets, dataentity.reps, dataentity.dateAdded " +
                "FROM nameentity, dataentity, typeentity " +
                "WHERE typeentity.name = 'Cardio' " +
                "AND nameentity.typeId = typeentity.id " +
                "AND dataentity.nameId = nameentity.id")
        List<DataEntity> getCardioExercises();

        @Query("SELECT dataentity.id, dataentity.nameId, dataentity.typeId, dataentity.weight, " +
                "dataentity.sets, dataentity.reps, dataentity.dateAdded " +
                "FROM nameentity, dataentity, typeentity " +
                "WHERE typeentity.name = 'Weight' " +
                "AND nameentity.typeId = typeentity.id " +
                "AND dataentity.nameId = nameentity.id")
        List<DataEntity> getWeightExercises();

        @Query("SELECT * FROM DataEntity WHERE exerciseName = :exerciseName ORDER BY dateAdded DESC LIMIT 1")
        DataEntity getLatestDataForExerciseName(String exerciseName);
    }
