package com.example.gymtracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = TypeEntity.class, parentColumns = "id", childColumns = "typeId")
})
public class NameEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private long typeId; // Foreign key

    public NameEntity(String name) {
        this.name = name;
    }


    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }
}