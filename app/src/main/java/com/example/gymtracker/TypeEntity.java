package com.example.gymtracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TypeEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;

    public TypeEntity(String name) {
        this.name = name;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}