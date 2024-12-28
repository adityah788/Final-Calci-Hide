package com.example.finalcalcihide.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.finalcalcihide.Model.Note;
import com.example.finalcalcihide.Database.NoteDao;
import com.example.finalcalcihide.Database.NotesDatabase;
import com.example.finalcalcihide.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NotesActivity extends AppCompatActivity {

    private EditText titleEditText, notesEditText;
    private TextView timestampTextView;
    private NoteDao noteDao;
    private Note note;
    private int noteId = -1;
    private ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        titleEditText = findViewById(R.id.titleEditText);
        notesEditText = findViewById(R.id.notesEditText);
        timestampTextView = findViewById(R.id.timestampTextView);
        ImageView doneButton = findViewById(R.id.doneButton);
        ImageView backButton = findViewById(R.id.notes_backButton);

        noteDao = NotesDatabase.getInstance(this).noteDao();

        // Get the note ID passed from NoteActivityRecyclerView
        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNoteById(noteId);
        }

        doneButton.setOnClickListener(v -> saveAndReturn());
        backButton.setOnClickListener(v -> finish());

        notesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateTimestamp();
            }
        });

        // Set the back button handler using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                // Handle saving the note before back press if there is any content
//                String title = titleEditText.getText().toString().trim();
//                String content = notesEditText.getText().toString().trim();

                // If title or content is not empty, save the note before going back
//                if (!title.isEmpty() || !content.isEmpty()) {
                    saveAndReturn();  // Save the note before finishing the activity
                Toast.makeText(NotesActivity.this, "Notes save kr diya", Toast.LENGTH_SHORT).show();
//                } else {
//                    // Otherwise, go back as usual
                    finish();
//                }
            }
        });


    }

    private void saveAndReturn() {
        String title = titleEditText.getText().toString().trim();
        String content = notesEditText.getText().toString().trim();

        // Check if both title and content are empty
        if (title.isEmpty() && content.isEmpty()) {
            // Optionally show a Toast or an alert to inform the user
            Toast.makeText(this, "Title and content cannot be empty!", Toast.LENGTH_SHORT).show();
            return;  // Don't save the note if title or content is empty
        }

        String date = new SimpleDateFormat("dd MMMM yyyy | HH:mm a", Locale.getDefault()).format(new Date());

        if (note == null) {
            // Create a new note if it doesn't exist
            note = new Note();
            note.date = date;
        }

        note.title = title;
        note.content = content;

        // Save the note directly without checking for duplicates
        Executors.newSingleThreadExecutor().execute(() -> {
            if (noteId == -1) {
                // Insert a new note if it's a new note
                noteDao.insert(note);
            } else {
                // Update the existing note if editing
                noteDao.update(note);
            }

        });
        finish(); // Close the current activity

    }



    private void updateTimestamp() {
        String date = new SimpleDateFormat("dd MMMM yyyy | HH:mm a", Locale.getDefault()).format(new Date());
        int characterCount = notesEditText.getText().length();
        timestampTextView.setText(date + " | " + characterCount + " characters");
    }

    private void loadNoteById(int noteId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            note = noteDao.getNoteById(noteId); // Load note by ID
            runOnUiThread(() -> {
                if (note != null) {
                    titleEditText.setText(note.title);
                    notesEditText.setText(note.content);
                    timestampTextView.setText(note.date); // Set date if note exists
                }
            });
        });
    }






}
