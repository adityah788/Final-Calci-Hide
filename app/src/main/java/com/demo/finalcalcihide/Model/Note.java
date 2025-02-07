package com.demo.finalcalcihide.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String title;
    public String content;
    public String date;
    public String path; // Path to the note file or related resource


    public Note(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public Note() {} // Default constructor
}
