package com.example.finalcalcihide.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        titleEditText = findViewById(R.id.titleEditText);
        notesEditText = findViewById(R.id.notesEditText);
        timestampTextView = findViewById(R.id.timestampTextView);
        ImageView doneButton = findViewById(R.id.doneButton);
        ImageView backButton = findViewById(R.id.backButton);

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
    }

    private void saveAndReturn() {
        String title = titleEditText.getText().toString();
        String content = notesEditText.getText().toString();
        String date = new SimpleDateFormat("dd MMMM yyyy | HH:mm a", Locale.getDefault()).format(new Date());

        if (note == null) {
            // Create new note only if it doesn't exist
            note = new Note();
            note.date = date;
        }

        note.title = title;
        note.content = content;

        Executors.newSingleThreadExecutor().execute(() -> {
            if (noteId == -1) {
                // New note
                noteDao.insert(note);
            } else {
                // Update existing note
                noteDao.update(note);
            }
            finish();
        });
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
