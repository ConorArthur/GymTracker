package com.example.gymtracker;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataConverter {
    @TypeConverter
    public static String fromList(List<Integer> reps) {
        if (reps == null) {
            return null;
        }
        return TextUtils.join(",", reps);
    }

    @TypeConverter
    public static List<Integer> toList(String repsString) {
        if (repsString == null) {
            return null;
        }
        String[] repsArray = repsString.split(",");
        List<Integer> repsList = new ArrayList<>();
        for (String rep : repsArray) {
            repsList.add(Integer.parseInt(rep));
        }
        return repsList;
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
}

